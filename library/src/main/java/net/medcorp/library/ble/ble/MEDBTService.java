/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */

package net.medcorp.library.ble.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import net.medcorp.library.ble.datasource.GattAttributesDataSource;
import net.medcorp.library.ble.event.BLEConnectionStateChangedEvent;
import net.medcorp.library.ble.event.BLEFirmwareVersionReceivedEvent;
import net.medcorp.library.ble.event.BLEPairStateChangedEvent;
import net.medcorp.library.ble.event.BLEResponseDataEvent;
import net.medcorp.library.ble.exception.BLEUnstableException;
import net.medcorp.library.ble.kernel.MEDBT;
import net.medcorp.library.ble.model.request.BLERequestData;
import net.medcorp.library.ble.model.response.BLEResponseData;
import net.medcorp.library.ble.model.response.DataFactory;
import net.medcorp.library.ble.util.Constants;
import net.medcorp.library.ble.util.Optional;
import net.medcorp.library.ble.util.QueuedMainThreadHandler;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 * WARNING ! DO NOT RENAME OF MOVE THIS CLASS, BECAUSE IT HAVE TO BE DECLARED IN THE MANIFEST
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
public class MEDBTService extends Service {

	/**
	 * Bluetooth adapter
	 */
	private BluetoothAdapter bluetoothAdapter;

	/**
	 * save all the connected BLE Gatt profiles, use LinkedHashMap<> class, default access order is FIFO
	 * make sure one service only connected one device
	 */
	private  Map<String,BluetoothGatt> bluetoothGattMap = new LinkedHashMap<String,BluetoothGatt>();

	private QueuedMainThreadHandler queuedMainThread;

	private static final int RETRY_DELAY = 3000;

	private GattAttributesDataSource dataSource;

	private  String bluetoothVersion = null;
	private  String mcuVersion = null;

	/**
	 * This binder is the bridge between the ImazeBTImpl and this Service
	 * @author Hugo
	 */
	public class LocalBinder extends Binder{

		/**
		 * Sets all the required callbacks
		 */
		public void initialize(GattAttributesDataSource source){
			MEDBTService.this.initialize(source);
		}

		/**
		 * @return the current connection state
		 */
		public boolean isConnected(String address){
			return bluetoothGattMap.containsKey(address);
		}

		/**
		 * @return true if no device is currently connected
		 */
		public boolean isDisconnected(){
			return MEDBTService.this.bluetoothGattMap.isEmpty();
		}

		/**
		 * Connect to the given device (if possible)
		 */
		public void connect(String address){
			MEDBTService.this.autoConnect(address);
		}

		/**
		 * Disconnects to the given device (if possible)
		 */
		public void disconnect(String address){
			MEDBTService.this.disconnect(address);
		}

		/**
		 * Helps to kill this object
		 */
		public void destroy(){
			stopSelf();
			close();
		}

		/**
		 * Checks if a device already covers the given service.
		 * @return the address of the connected device (if any) or an empty Optional if there's no device currently covering this service
		 */
		public Optional<String> isServiceConnected(UUID uuid){
			return MEDBTService.this.isServiceConnected(uuid);
		}

		/**
		 * Checks if a device already covers on of the given services
		 * @return the address of the connected device (if any) or an empty Optional if there's no device currently covering this service
		 */
		public boolean isOneOfThoseServiceConnected(List<UUID> uuids){
			for(UUID uuid : uuids) {
				if(MEDBTService.this.isServiceConnected(uuid).notEmpty()){
					return true;
				}
			}

			return false;
		}

		/**
		 * Sends a request to the device that supports the given service (if any)
		 * @param request
		 */
		public void sendRequest(BLERequestData request){
			MEDBTService.this.sendRequest(request);
		}

		/**
		 *
		 * @return BLE firmware version
		 */
		public String getBluetoothVersion()
		{
			return bluetoothVersion;
		}

		/**
		 *
		 * @return MCU software version
		 */
		public String getSoftwareVersion()
		{
			return mcuVersion;
		}

		/**
		 * Pings the currently attached device (if any) in order to check if it is connected
		 */
		public void ping() { MEDBTService.this.ping(); }

	}

	/*
     * (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
	@Override
	public IBinder onBind(Intent intent) {
		Log.v(MEDBT.TAG, "ImazeBTService onBind() called");
		return new LocalBinder();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onUnbind(android.content.Intent)
	 */
	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that BluetoothGatt.close() is called
		// such that resources are cleaned up properly.  In this particular example, close() is
		// invoked when the UI is disconnected from the Service.
		Log.v(MEDBT.TAG,"ImazeBTService onUnbind() called");
		close();
		return super.onUnbind(intent);
	}

	private boolean initialize(GattAttributesDataSource source) {

		queuedMainThread = QueuedMainThreadHandler.getInstance(QueuedMainThreadHandler.QueueType.MEDBT);
		queuedMainThread.clear();
		dataSource = source;

		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		if (bluetoothManager == null) {
			Log.e(MEDBT.TAG, "Unable to initialize BluetoothManager.");
			return false;
		}

		bluetoothAdapter = bluetoothManager.getAdapter();
		if (bluetoothAdapter == null) {
			Log.e(MEDBT.TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Automatically try to connect to this address.
	 * It will try until it succeeds.
	 * @param address
	 */
	private void autoConnect(final String address){
		//If it doesn't connect, we'll retry a bit later
		if(!connect(address)){
			Log.v(MEDBT.TAG, "Reschedueling a connection");
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					//Time to retry !
					Log.v(MEDBT.TAG, "Retrying to connect");
					autoConnect(address);
				}
			}, RETRY_DELAY);

		}
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * If the device is unavailable, we'll connect to it as soon as possible.
	 *
	 * @param address The device address of the destination device.
	 *
	 * @return Return true if the connection is initiated successfully. The connection result
	 *         is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	private boolean connect(final String address) {
		if (bluetoothAdapter == null) {
			Log.w(MEDBT.TAG, "BluetoothAdapter not initialized");
			return false;
		}

		// if the device has a Gatt service, it means we are connected already, do nothing. disable many times connected the same device
		if(bluetoothGattMap.containsKey(address))
		{
			Log.w(MEDBT.TAG, "this device has got connected,disable connect the same device many times.");
			return true;
		}
		final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			Log.w(MEDBT.TAG, "Device not found.  Unable to connect.");
			return false;
		}

		//All discoveries should be canceled before we try to connect
		bluetoothAdapter.cancelDiscovery();
		// We don't know if the device is available, so we try to connect to it
		//We should do this on the UI thread (for some reason)... http://stackoverflow.com/questions/6369287/accessing-ui-thread-handler-from-a-service
		queuedMainThread.post(new Runnable() {
			;

			@Override
			public void run() {
				Log.d(MEDBT.TAG, "Connecting to Gatt : " + address);
				device.connectGatt(MEDBTService.this, false, mGattCallback);
			}
		});


		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The disconnection result
	 * is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	private void disconnect(final String address) {
		if (bluetoothAdapter == null) {
			Log.w(MEDBT.TAG, "BluetoothAdapter not initialized");
			return;
		}
		//For some reason we have to do it on the UI thread...
		//But we don't do it in the Queued Handler, because we can't reliabily queuedMainThread.next(); on disconnect
		//Because a disconnection can come from a lot of things
		new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				Log.i(MEDBT.TAG, "Disconnecting "+address);
				if(bluetoothGattMap.containsKey(address))
				{
					bluetoothGattMap.get(address).disconnect();
				}
			}
		});

	}

	/**
	 * Clears the device cache. After uploading new firmware the DFU target will have other services than before.
	 *
	 * @param gatt
	 *            the GATT device to be refreshed
	 */
	private void refreshDeviceCache(final BluetoothGatt gatt) {
		/*
		 * There is a refresh() method in BluetoothGatt class but for now it's hidden. We will call it using reflections.
		 */
		try {
			final Method refresh = gatt.getClass().getMethod("refresh");
			if (refresh != null) {
				final boolean success = (Boolean) refresh.invoke(gatt);
				Log.i(MEDBT.TAG,"Refreshing result: " + success);
			}
		} catch (Exception e) {
			Log.i(MEDBT.TAG,"An exception occurred while refreshing device", e);
		}
	}
	/**
	 * Implements callback methods for GATT events that the app cares about.  For example,
	 * connection change and services discovered.
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
			if(gatt == null || gatt.getDevice() == null || gatt.getDevice().getAddress() == null) {
				//If the gatt is null, something's wrong. Let's just stop here.
				Log.w(MEDBT.TAG,"mBluetoothGatt is null");
				return;
			}

			final String address = gatt.getDevice().getAddress();

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				//sometimes, onConnectionStateChange() can be invoked twice
				if(!bluetoothGattMap.containsKey(address)) {
					queuedMainThread.next();

					bluetoothGattMap.put(address, gatt);

					Log.i(MEDBT.TAG, "Connected to GATT server : " + address);

					// Attempts to discover services after successful connection.
					Log.v(MEDBT.TAG, "Attempting to start service discovery");
					//fixed by Gailly, add 200ms defer to do discover services, let all services get ready
					/**
					 * I add 200ms delay to invoke discoverServices() in our MED-library MEDBTService
					 * but in some phone model, I still find that discover service returns nothing, perhaps the waiting time isn't enough,
					 * I get it from Nordic nrf-toolbox source code,so I can change this value from 600ms~1600ms.
					 * this comment comes from Android-nRF-Toolbox project that is produced by Nordic ,here it is:https://github.com/NordicSemiconductor/Android-DFU-Library
					 * no.nordicsemi.android.dfu.DfuBaseService line 677~684  as below:
						 if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
						 logi("Waiting 1600 ms for a possible Service Changed indication...");
						 waitFor(1600);
						 // After 1.6s the services are already discovered so the following gatt.discoverServices() finishes almost immediately.

						 // NOTE: This also works with shorted waiting time. The gatt.discoverServices() must be called after the indication is received which is
						 // about 600ms after establishing connection. Values 600 - 1600ms should be OK.
						 }
					 */
					final long waitingTime = 200;
					queuedMainThread.postDelayed(new Runnable() {
						@Override
						public void run() {
							Log.d(MEDBT.TAG, "Discovering services : " + address);
							if (gatt != null) gatt.discoverServices();
						}
					}, waitingTime);
				}
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

				Log.e(MEDBT.TAG, "Disconnected from GATT server : " + address);
                EventBus.getDefault().post(new BLEConnectionStateChangedEvent(false, address));

				//close this server for next reconnect!!!
				refreshDeviceCache(gatt);
                gatt.close();
				bluetoothGattMap.remove(address);
				//we don't know why the Gatt server disconnected, so no need again connect, for example: BLE devices power off or go away
				return;
			} else {

				Log.e(MEDBT.TAG, "Unknown state for "+ address);
				//No matter what, if the device is not connected, we remove it from the list of connected devices
				bluetoothGattMap.remove(address);
			}
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
			queuedMainThread.next();

			if(gatt == null || gatt.getDevice() == null || gatt.getDevice().getAddress() == null) {
				//If the gatt is null, something's wrong. Let's just stop here.
				Log.w(MEDBT.TAG,"mBluetoothGatt is null");
				return;
			}

			final String address = gatt.getDevice().getAddress();

			Log.d(MEDBT.TAG, "Services discovered : "+address);

			//WARNING ! For some reasons, device.connectGatt(this, true, mGattCallback); will give us services with empty characteristics...
			//Looks like the bluetooh layers crash, with a aclStateChangeCallback: Device is NULL  (at least on android 4.3)

			if(getSupportedGattServices(gatt).isEmpty()) Log.w(MEDBT.TAG, "No services discovered for : "+address);
			else Log.v(MEDBT.TAG,  getSupportedGattServices(gatt).size() +  " services discovered for : "+address);

			//At least one characteristic should be chosen, or there's a problem
			boolean characteristicChosen = false;

			for(BluetoothGattService service : getSupportedGattServices(gatt)){

				if(service.getCharacteristics().isEmpty()) Log.w(MEDBT.TAG, "No characteristic discovered for : "+service.getUuid());
				else Log.v(MEDBT.TAG, service.getCharacteristics().size() + " characteristic discovered for : "+service.getUuid() );

				//Since it takes some time to connect to a device, maybe in the mean time we've just connected to another device with similar services.
				//Let's check if there's an address connected to one of those services
				Optional<String> device = isServiceConnected(service.getUuid());
				//If yes, maybe it's this device address. If not, then we shouldn't connect this device, let's disconnect.
				if(device.notEmpty()&&!device.get().equals(address)) {
					Log.w(MEDBT.TAG, "disconnect the second BLE device (same service UUID,eg:  the 2nd. Nevo): "+address);
					disconnect(address);
					return;
				}

				//For each characteristics of each supported services, we'll try to get notified
				for(final BluetoothGattCharacteristic characteristic : service.getCharacteristics()){

					final UUID uuid = characteristic.getUuid();
					//read firmware/software version
					if(service.getUuid().equals(dataSource.getDeviceInfoUDID()))
					{
						if(characteristic.getUuid().equals(dataSource.getDeviceInfoBluetoothVersion())
								|| characteristic.getUuid().equals(dataSource.getDeviceInfoSoftwareVersion()))
						{
							queuedMainThread.post(new Runnable() {
								@Override
								public void run() {
									Log.v(MEDBT.TAG, "start read version: " + uuid);
									gatt.readCharacteristic(characteristic);
								}
							});
						}
					}

					//Is this characteristic supported ?
					Log.v(MEDBT.TAG,"Characteristic UUID:" + uuid);
					if (GattAttributes.supportedBLECharacteristic(dataSource, uuid))
					{
						Log.i(MEDBT.TAG, "Activating supported characteristic : "+address+" "+uuid);
						setCharacteristicNotification(gatt, characteristic, true);
						characteristicChosen = true;

					}
				}
			}

			if(!characteristicChosen){
				Log.w(MEDBT.TAG, "No characteristic chosen, maybe the bluetooth is unstable : " + address);
                EventBus.getDefault().post(new BLEUnstableException());
			}
			else
			{
				//here only connect one SERVICE, the first SERVICE by scan to find out
				bluetoothGattMap.put(gatt.getDevice().getAddress(), gatt);
                EventBus.getDefault().post(new BLEConnectionStateChangedEvent(true, gatt.getDevice().getAddress()));
			}

		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic,
										 int status) {
			queuedMainThread.next();
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (dataSource.getDeviceInfoBluetoothVersion().equals(characteristic.getUuid())){
					bluetoothVersion = StringUtils.newStringUsAscii(characteristic.getValue());
					Log.i(MEDBT.TAG, "FIRMWARE VERSION **************** " + bluetoothVersion);
                    EventBus.getDefault().post(new BLEFirmwareVersionReceivedEvent(Constants.DfuFirmwareTypes.BLUETOOTH, bluetoothVersion));
				}
				else if (dataSource.getDeviceInfoSoftwareVersion().equals(characteristic.getUuid())){
					mcuVersion = StringUtils.newStringUsAscii(characteristic.getValue());
					Log.i(MEDBT.TAG,"SOFTWARE VERSION **************** "+ mcuVersion);
                    EventBus.getDefault().post(new BLEFirmwareVersionReceivedEvent(Constants.DfuFirmwareTypes.MCU, mcuVersion));
				}
			}
		}

		@Override
		public void onCharacteristicChanged(final BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			dataReceived(characteristic, gatt.getDevice().getAddress());
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			queuedMainThread.next();
		};

		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			queuedMainThread.next();
		};

		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			queuedMainThread.next();
		};

		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			queuedMainThread.next();
		};

		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			queuedMainThread.next();
		};

	};

	/**
	 * After using a given BLE device, the app must call this method to ensure resources are
	 * released properly.
	 */
	private void close() {
		if(queuedMainThread !=null) queuedMainThread.clear();
        /*
         * perhapse unbindService and LocalBinder.destroy() both call it
         */
		if(bluetoothGattMap!=null &&bluetoothGattMap.isEmpty() == false)
		{ //use disconnect() replace close(),disconnect() will invoke callback function, but close can't
			for(BluetoothGatt b : bluetoothGattMap.values()) b.disconnect();
			bluetoothGattMap.clear();
		}
	}

	/**
	 * Broadcast the data updates
	 * @param characteristic
	 */
	private void dataReceived(final BluetoothGattCharacteristic characteristic, final String address) {

		BLEResponseData data = DataFactory.fromBluetoothGattCharacteristic(dataSource, characteristic, address);

        EventBus.getDefault().post(new BLEResponseDataEvent(data));
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
	 * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 *
	 * @param characteristic The characteristic to read from.
	 */
	//Not used, but kept for possible later use
	private void readCharacteristic(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
		if (bluetoothAdapter == null || gatt == null) {
			Log.w(MEDBT.TAG, "BluetoothAdapter not initialized");
			return;
		}
		int charaProp = characteristic.getProperties();

		Log.v(MEDBT.TAG, "characteristic.getProperties() is: " + charaProp);

		if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ)== BluetoothGattCharacteristic.PROPERTY_READ)
		{
			queuedMainThread.post(new Runnable() {
				@Override
				public void run() {
					Log.v(MEDBT.TAG, "Reading characteristic");
					if (gatt != null) gatt.readCharacteristic(characteristic);
				}
			});
		}


	}

	/**
	 * Enables or disables notification on a give characteristic.
	 *
	 * @param characteristic Characteristic to act on.
	 * @param enabled If true, enable notification.  False otherwise.
	 */
	private void setCharacteristicNotification(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic,
											   final boolean enabled) {
		if (bluetoothAdapter == null || gatt == null) {
			Log.w(MEDBT.TAG, "BluetoothAdapter not initialized");
			return;
		}
		queuedMainThread.post(new Runnable() {
			@Override
			public void run() {
				if (gatt != null) gatt.setCharacteristicNotification(characteristic, enabled);
				new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
					@Override
					public void run() {
						queuedMainThread.next();
					}
				}, 1000);
			}
		});

		final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(dataSource.getClientCharacteristicConfig());
		if(descriptor!=null){
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			queuedMainThread.post(new Runnable() {
				@Override
				public void run() {
					if (gatt != null) gatt.writeDescriptor(descriptor);
				}
			});
		}

	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This should be
	 * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
	 *
	 * @return A {@code List} of supported services.
	 */
	private List<BluetoothGattService> getSupportedGattServices(BluetoothGatt gatt) {
		if (gatt == null) {
			return new ArrayList<BluetoothGattService>();
		}
		return gatt.getServices();
	}

	/**
	 * Checks if a device already covers the given service.
	 * @param service the service that we are looking up. We'll try to see if a connected device provides this service.
	 * @return the address of the connected device (if any) or an empty Optional if there's no device currently covering this service
	 */
	private Optional<String> isServiceConnected(UUID service) {
		if(bluetoothGattMap == null || bluetoothGattMap.isEmpty())  return new Optional<String>();

		Collection<BluetoothGatt>  gatts = bluetoothGattMap.values();

		for(BluetoothGatt gatt : gatts)
		{

			for(BluetoothGattService ser : gatt.getServices())
			{
				if(ser.getUuid().equals(service)
						&& GattAttributes.supportedBLEService(dataSource,service))
				{
					return new Optional<String>(gatt.getDevice().getAddress());
				}
			}
		}
		return new Optional<String>();
	}

	private void sendRequest(BLERequestData request) {
		UUID serviceUUID = request.getServiceUUID();
		UUID characteristicUUID = request.getInputCharacteristicUUID();
		final byte[] rawData = request.getRawData();
		byte[][] rawDatas = request.getRawDataEx();
		if(bluetoothGattMap == null || bluetoothGattMap.isEmpty())  {
			Log.w(MEDBT.TAG, "Send failed. No device connected" );
			return;
		}

		boolean sent = false;

		for(final BluetoothGatt gatt : bluetoothGattMap.values())
		{
			//For each connected device, we'll see if they have the right service
			BluetoothGattService service = gatt.getService(serviceUUID);

			if(service!=null) {
				final BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
				if(characteristic!=null) {
					//Now we've found the right characteristic, we modify it, then send it to the device
					if(rawDatas != null)
					{
						for(final byte[] data : rawDatas)
						{
							//make sure every packet is sent one by one with the low level Queue: QueueType.MEDBT
							queuedMainThread.post(new Runnable() {
								@Override
								public void run() {
									Log.i(MEDBT.TAG, "Send requestEx " + new String(Hex.encodeHex(data)));
									characteristic.setValue(data);
									gatt.writeCharacteristic(characteristic);
								}
							});
						}
					}
					else
					{
						if(rawData != null)
						{
							//make sure every packet is sent one by one with the low level Queue: QueueType.MEDBT
							queuedMainThread.post(new Runnable() {
								@Override
								public void run() {
									Log.i(MEDBT.TAG, "Send request " + new String(Hex.encodeHex(rawData)));
									characteristic.setValue(rawData);
									gatt.writeCharacteristic(characteristic);
								}
							});
						}
					}

					sent=true;
				}
			}
		}

		if(!sent) Log.w(MEDBT.TAG, "Send failed. No device have the right service and characteristic" );

	}

	private void ping()
	{
		if(bluetoothGattMap == null || bluetoothGattMap.isEmpty())  {
			Log.w(MEDBT.TAG, "Get failed. No device connected" );
			return;
		}

		boolean sent = false;

		for(BluetoothGatt gatt : bluetoothGattMap.values())
		{
			//For each connected device, we'll see if they have the right service
			BluetoothGattService service = gatt.getService(dataSource.getDeviceInfoUDID());

			if(service!=null) {
				BluetoothGattCharacteristic characteristic = service.getCharacteristic(dataSource.getDeviceInfoBluetoothVersion());
				if(characteristic!=null) {
					//Now we've found the right characteristic, we modify it, then send it to the device
					readCharacteristic(gatt,characteristic);
					sent=true;
				}
			}
		}

		if(!sent) {
			Log.w(MEDBT.TAG, "Get failed. No device have the right service and characteristic" );
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w("Karl","Service died");
		unregisterReceiver(MEDBTServiceReceiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY_COMPATIBILITY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		registerReceiver(MEDBTServiceReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
	}

	static BroadcastReceiver MEDBTServiceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				int connectState = device.getBondState();
				Log.i(MEDBT.TAG, "Ble pair state got changed:" + connectState + ",device:" + device.getAddress());
				EventBus.getDefault().post(new BLEPairStateChangedEvent(connectState, device.getAddress()));
			}
		}
	};
}

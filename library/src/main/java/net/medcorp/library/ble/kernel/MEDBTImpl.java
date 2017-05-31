/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.kernel;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import net.medcorp.library.ble.ble.GattAttributes;
import net.medcorp.library.ble.ble.GattAttributes.SupportedService;
import net.medcorp.library.ble.ble.MEDBTService;
import net.medcorp.library.ble.controller.ConnectionController;
import net.medcorp.library.ble.datasource.GattAttributesDataSource;
import net.medcorp.library.ble.event.BLEPairStateChangedEvent;
import net.medcorp.library.ble.event.BLESearchEvent;
import net.medcorp.library.ble.exception.BLENotSupportedException;
import net.medcorp.library.ble.exception.BluetoothDisabledException;
import net.medcorp.library.ble.model.request.BLERequestData;
import net.medcorp.library.ble.util.Optional;
import net.medcorp.library.ble.util.QueuedMainThreadHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
public class MEDBTImpl implements MEDBT {
	/*
	 * Here's how it works under the hood.
	 * This is our Kernel.
	 * Each time it finds a compatible device (After a successful scan) it will create a Service to connect to it.
	 * It can connect to a certain number of Devices at the same time
	 * The communication is as follow :
	 * When the Kernel needs to call a function in the service, it does it this way :
	 * ImazeBTImpl -- ImazeBTService.LocalBinder --> ImazeBTService
	 * 
	 * When the Service need to send data back to the kernel, it calls a callback (That was given to him through the binder)
	 * For example
	 * ImazeBTService -- OnDataReceivedListener --> ImazeBTImpl
	 * 
	 * In this whole package, all the *Impl classes, are Package wide classes and all the callable functions are called through an interface
	 * Also, instanciation is only done Through Builders
	 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
	 */

	private BluetoothAdapter bluetoothAdapter;

	private Context context;

	/**
	 * The list of currently binded services.
	 * Warning though, alway check they haven't stopped
	 */
	private Optional<MEDBTService.LocalBinder> mCurrentService = new Optional<MEDBTService.LocalBinder>();

	private Optional<ServiceConnection> mCurrentServiceConnection = new Optional<ServiceConnection>();

    private static final long SCAN_PERIOD = 8000;

    /*
	 *  here use one List to save the scanned devices's MAC address
	 *  This is only to prevent multi-connections (a recurent bug on Nexus 5)
	 */
    private List<String> mPreviousAddress  = new ArrayList<String>();

    /**
     * If we want to connect to a particular address, here's the place to say it
     */
    private Optional<String> mPreferredAddress = new Optional<String>();

	/*
	 * save the supported BLE service,avoid connect the same service with the same model BLE device
	 * more sensors, such as heart rate/ power/ combo,  for every model sensor ,only one device can connect
	 */
    private List<SupportedService> mSupportServicelist = new ArrayList<GattAttributes.SupportedService>();

    private boolean isScanning = false;

	private GattAttributesDataSource dataSource;

	private BluetoothLeScannerCompat bluetoothLeScanner;
	private DeviceScanCallback deviceScanCallback;
    /**
     * Simple constructor
     * @param context
     * @throws BLENotSupportedException
     * @throws BluetoothDisabledException
     */
	public MEDBTImpl(Context context, GattAttributesDataSource dataSource){
		this.context = context;
		this.dataSource = dataSource;
		EventBus.getDefault().register(this);
		initBluetoothAdapter();
		this.bluetoothLeScanner = BluetoothLeScannerCompat.getScanner();
    }

    @Override
	public synchronized void startScan(final List<SupportedService> serviceList, final Optional<String> preferredAddress) {
		if(isScanning){
            Log.i(TAG, "Scanning......return ******");
            return;
        }

		//We check if bluetooth is enabled and/or if the device isn't ble capable
		if(bluetoothAdapter == null )
		{
			Log.w(TAG, "ble feature is not support,return");
			return;
		}
		if(!bluetoothAdapter.isEnabled())
		{
			Log.w(TAG, "bluetooth is off,return");
			return;
		}

        //For some reason we have to do it on the UI thread...
        new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				/*
				 * firstly remove all saved devices
				 */
				mPreviousAddress.clear();

                mPreferredAddress = preferredAddress;
                mSupportServicelist = serviceList;

                //clear Queue before every connect
                QueuedMainThreadHandler.getInstance(QueuedMainThreadHandler.QueueType.MEDBT).clear();
				startScan();
				// Stops scanning after a pre-defined scan period.
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						stopScan();
					}
				}, SCAN_PERIOD);
			}
		});
	}

	private void startScan(){
		ScanSettings scanSettings = new ScanSettings.Builder()
				.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
				.setUseHardwareBatchingIfSupported(false)
				.build();

		List<ScanFilter> scanFilterList = new ArrayList<>();
		if(mPreferredAddress.notEmpty()) {
			scanFilterList.add(new ScanFilter.Builder().setDeviceAddress(mPreferredAddress.get()).build());
		}
		else {
			if(mSupportServicelist.contains(GattAttributes.SupportedService.SERVICE)) {
				scanFilterList.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(dataSource.getService())).build());
			}
			if(mSupportServicelist.contains(GattAttributes.SupportedService.OTA_SERVICE)) {
				scanFilterList.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(dataSource.getOtaService())).build());
			}
		}

		deviceScanCallback = new DeviceScanCallback();
		EventBus.getDefault().post(new BLESearchEvent(BLESearchEvent.SEARCH_EVENT.ON_SEARCHING));
		bluetoothLeScanner.startScan(scanFilterList,scanSettings,deviceScanCallback);
		isScanning = true;
	}

	@Override
	public void stopScan() {
		if(bluetoothAdapter !=null && isScanning) {
			if (bluetoothAdapter.isEnabled()){
				bluetoothLeScanner.stopScan(deviceScanCallback);
				Log.v(TAG, "stopLeScan");
			}
			isScanning = false;
		}
	}

	@Override
	public int getBluetoothStatus() {
		if(bluetoothAdapter == null) {
			//-1 means ble feature is not supported.
			return -1;
		}
		return bluetoothAdapter.getState();
	}

	private class DeviceScanCallback extends no.nordicsemi.android.support.v18.scanner.ScanCallback {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			if(result.getScanRecord()==null) {return;}

			BluetoothDevice device = result.getDevice();
			final String deviceAddress = device.getAddress();

			if(mPreviousAddress.contains(deviceAddress)){
				//We are in a case, Due to this issue : http://stackoverflow.com/questions/19502853/android-4-3-ble-filtering-behaviour-of-startlescan
				//Where the callback has been called twice for the same address
				//We should not do anything
			} else {
				if(result.getScanRecord().getServiceUuids()!=null && result.getScanRecord().getServiceUuids().size()>0)
 				{
					Log.d(TAG, "<<<<<<<<<<<Device "+deviceAddress+" found to support service " + ",name: " + device.getName()+">>>>>>>>>");

					EventBus.getDefault().post(new BLESearchEvent(BLESearchEvent.SEARCH_EVENT.ON_SEARCH_SUCCESS));
					//If yes, let's bind this device !
					if(mCurrentService.isEmpty()) {
						bindNewService(deviceAddress);
					} else {
						//reset MEDservice.queuedMainThread for this new connection
						mCurrentService.get().initialize(dataSource);
						EventBus.getDefault().post(new BLESearchEvent(BLESearchEvent.SEARCH_EVENT.ON_CONNECTING));
						//now connect this device
						connectDevice(deviceAddress);
					}
				}
			}
			if(!mPreviousAddress.contains(deviceAddress)) {
				mPreviousAddress.add(deviceAddress);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imaze.sdk.kernel.ImazeBT#sendRequest(fr.imaze.sdk.model.request.SensorRequest, fr.imaze.sdk.ble.SupportedService)
	 */
	@Override
	public void sendRequest(BLERequestData request) {
		if(mCurrentService.notEmpty()) {
			mCurrentService.get().sendRequest(request);
		} else {
			 Log.w(MEDBT.TAG, "Send failed. Service not started");
             ////fixed by Gailly,rebind service if empty, perhaps kill service and right now reconnect watch, but the service doesn't get ready
             if(mPreferredAddress.notEmpty()){
				 bindNewService(mPreferredAddress.get());
			 }
		}
	}


	@Override
    public void ping() {
        if(mCurrentService.notEmpty()) {
            mCurrentService.get().ping();
        } else {
            Log.w(MEDBT.TAG, "Ping failed. Service not started" );
        }
    }

    @Override
    public Set<BluetoothDevice> getDevices() {
        return bluetoothAdapter.getBondedDevices();
    }

    /*
     * (non-Javadoc)
     * @see fr.imaze.sdk.ImazeBT#disconnect(java.lang.String)
     */
	@Override
	public void disconnect() {
        //Let's kill the connection in the most violent way possible.
        isScanning = false;
        killService();
        if(bluetoothAdapter !=null){
			stopScan();
		}


	}

	/*
	 * (non-Javadoc)
	 * @see fr.imaze.sdk.kernel.ImazeBT#isDisconnected()
	 */
	public  boolean isDisconnected()
	{
		if(mCurrentService.isEmpty() || mCurrentService.get().isDisconnected()) {
			return true;
		}
		return false;
	}

	
	/*
	 * END of the Functions coming from the interface
	 */

	/*
	 * killService() , will close all connected BLE, and destory the BT Service
	 * so be careful call this function,  Imaze Zen and Imaze fitness both Bind BT Service
	 * perhaps, we should keep BT service always running backgrand!
	 */
	private void killService(){

		try{
			// when this Service Class is bound,	unbindService will call Service.onDestroy()
			// so  no need call mCurrentService.destroy();,but you should redo Service.onUnbind()
			// to close the BluetoothGatt
			//Discovery should be canceled if we really want to kill the service
			initBluetoothAdapter().cancelDiscovery();

			if(mCurrentServiceConnection.notEmpty()) {
				context.getApplicationContext().unbindService(mCurrentServiceConnection.get());
				mCurrentServiceConnection.set(null);
			}

			//Or it is null, so we disconnect all of them
			if(mCurrentService.notEmpty()) {
				mCurrentService.get().destroy();
				mCurrentService.set(null);
			}

		} catch ( Throwable t) {
			t.printStackTrace();
		}

	}

	/**
	 * This function will create a new Service (if no service currently exists)
	 * @param deviceAddress
	 */
	private void bindNewService(final String deviceAddress) {
		Log.v(TAG,"start bindNewService by " + deviceAddress);
		//We will create a Service that will handle the actual Bluetooth low level job
		Intent intent = new Intent(context,
				MEDBTService.class);

		//This object will be the bridge between this object and the Service
		//It is used to retreive the binder and unbind the service
		mCurrentServiceConnection = new Optional<ServiceConnection>( new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.v(MEDBT.TAG, name+" Service disconnected");
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.v(MEDBT.TAG, name+" Service connected");

				//This object is the bridge to get informations and control the service
				mCurrentService = new Optional<MEDBTService.LocalBinder> ( (MEDBTService.LocalBinder) service );

				//We launch a conenction to the given device
				mCurrentService.get().initialize(dataSource);

                EventBus.getDefault().post(new BLESearchEvent(BLESearchEvent.SEARCH_EVENT.ON_CONNECTING));
				connectDevice(deviceAddress);
			}
		} );

		//We start the actual binding
		//Note that the service will restart as long as it is binded, because we have set : Activity.BIND_AUTO_CREATE
		context.getApplicationContext().bindService(intent,mCurrentServiceConnection.get(),Activity.BIND_AUTO_CREATE);
		Log.v(MEDBT.TAG, "context.bindService");
	}

	private BluetoothAdapter initBluetoothAdapter()  {

		//If BLE is not supported, we throw an error
		if (!context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			bluetoothAdapter = null;
			return bluetoothAdapter;
		}
		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		bluetoothAdapter = ((BluetoothManager) context
				.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

		if(bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
		return bluetoothAdapter;
	}

	/**
	 * Set the current context. Useful if we killed the parent activity
	 * @param ctx
	 */
	/*package*/ void setContext(Context ctx){
		this.context = ctx;
	}

    @Override
    public String getBluetoothVersion() {
        return (mCurrentService.notEmpty())?mCurrentService.get().getBluetoothVersion():null;
    }

    @Override
    public String getSoftwareVersion() {
        return (mCurrentService.notEmpty())?mCurrentService.get().getSoftwareVersion():null;
    }

	@Subscribe
	public void onEvent(BLEPairStateChangedEvent stateChangedEvent) {
		if(stateChangedEvent.getPairState() == BluetoothDevice.BOND_BONDED)
		{
			if(mCurrentService !=null && mCurrentService.notEmpty()) {
				Log.i(MEDBT.TAG, "******Device paired successfully,connecting..." + stateChangedEvent.getAddress());
				mCurrentService.get().connect(stateChangedEvent.getAddress());
			}
			else {
				Log.e(MEDBT.TAG, "******Service is killed");
			}
		}
	}

	/**
	 *  * connect BLE device workflow:
	 * 															/ : paired -->directly connect it
	 *                                                        /
	 * case 1: normal connect-->scan BLE-->check pair state
	 *                                                        \
	 *                                                          \ : unpaired--> firstly pair it-->got paired successfully-->connect it
	 *
	 *
	 * 														/ : paired--> firstly unpair it ---> delay 1s-->connect it directly -->doing OTA by otaController or DFU library(Nordic)
	 * 													   /
	 * case 2: OTA connect-->scan BLE-->check pair state
	 * 													   \
	 *                                                      \ :unpaired-->connect it directly-->doing OTA by otaController or DFU library(Nordic)

	 * @param deviceAddress
     */
	private void connectDevice(final String deviceAddress)
	{
		if (mSupportServicelist.contains(GattAttributes.SupportedService.OTA_SERVICE)) {
			if (bluetoothAdapter.getRemoteDevice(deviceAddress).getBondState() == BluetoothDevice.BOND_BONDED) {
				ConnectionController.Singleton.getInstance(context, dataSource).unPairDevice(deviceAddress);
			}
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					mCurrentService.get().connect(deviceAddress);
				}
			}, 1000);
		}
		else {
			//now connect this device
			if (bluetoothAdapter.getRemoteDevice(deviceAddress).getBondState() != BluetoothDevice.BOND_BONDED) {
				ConnectionController.Singleton.getInstance(context, dataSource).pairDevice(deviceAddress);
			} else {
				if (mCurrentService != null && mCurrentService.notEmpty()) {
					mCurrentService.get().connect(deviceAddress);
				}
			}
		}
	}
}

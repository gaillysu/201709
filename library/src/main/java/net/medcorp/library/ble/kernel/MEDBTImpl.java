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
import android.util.Log;

import net.medcorp.library.ble.ble.GattAttributes;
import net.medcorp.library.ble.ble.GattAttributes.SupportedService;
import net.medcorp.library.ble.ble.MEDBTService;
import net.medcorp.library.ble.datasource.GattAttributesDataSource;
import net.medcorp.library.ble.event.BLEBluetoothOffEvent;
import net.medcorp.library.ble.event.BLEExceptionEvent;
import net.medcorp.library.ble.event.BLESearchEvent;
import net.medcorp.library.ble.exception.BLENotSupportedException;
import net.medcorp.library.ble.exception.BluetoothDisabledException;
import net.medcorp.library.ble.model.request.BLERequestData;
import net.medcorp.library.ble.util.Optional;
import net.medcorp.library.ble.util.QueuedMainThreadHandler;

import org.greenrobot.eventbus.EventBus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    /**
     * Simple constructor
     * @param context
     * @throws BLENotSupportedException
     * @throws BluetoothDisabledException
     */
	public MEDBTImpl(Context context, GattAttributesDataSource dataSource){
		this.context = context;
		this.dataSource = dataSource;
		
		try {
			initBluetoothAdapter();
		} catch (BluetoothDisabledException e) {
            EventBus.getDefault().post(new BLEBluetoothOffEvent());
        } catch (BLENotSupportedException e) {
            EventBus.getDefault().post(new BLEExceptionEvent(e));
        }
    }


    @Override
	public synchronized void startScan(final List<SupportedService> serviceList, final Optional<String> preferredAddress) {
		if(isScanning){
            Log.i(TAG, "Scanning......return ******");
            return;
        }
        //If we're already conected to this address, no need to go any further
        if (preferredAddress.notEmpty() && isAlreadyConnected(preferredAddress.get()) ) {return;}

        //Ok, so we're not connected to this address. If we're connected to another one, we should disconnect
        if (!isDisconnected()){
			disconnect();
		}

		//We check if bluetooth is enabled and/or if the device isn't ble capable
		try {
			initBluetoothAdapter();
        } catch (BluetoothDisabledException e) {
            EventBus.getDefault().post(new BLEBluetoothOffEvent());
            return;
        } catch (BLENotSupportedException e) {
            EventBus.getDefault().post(new BLEExceptionEvent(e));
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

				//We start a scan
				if(bluetoothAdapter !=null) {
                    EventBus.getDefault().post(new BLESearchEvent(BLESearchEvent.SEARCH_EVENT.ON_SEARCHING));
					isScanning = true;
					bluetoothAdapter.startLeScan(mLeScanCallback);
				}
				
		        // Stops scanning after a pre-defined scan period.
		        new Handler().postDelayed(new Runnable() {
		            @Override
		            public void run() {
		            	if(bluetoothAdapter !=null && isScanning)
						{
							bluetoothAdapter.stopLeScan(mLeScanCallback);
							Log.v(TAG, "stopLeScan");
							isScanning = false;
						}
		            }
		        }, SCAN_PERIOD);
			}
		});
	}

	@Override
	public void stopScan() {
		if(bluetoothAdapter !=null && isScanning)
		{
			bluetoothAdapter.stopLeScan(mLeScanCallback);
			Log.v(TAG, "stopLeScan");
			isScanning = false;
		}
	}

	/**
     *  Device scan callback.This callback is called for all devices founds by the scanner
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        /*
         * (non-Javadoc)
         * @see android.bluetooth.BluetoothAdapter.LeScanCallback#onLeScan(android.bluetooth.BluetoothDevice, int, byte[])
         */
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            final String deviceAddress = device.getAddress();

            if(mPreviousAddress.contains(deviceAddress)){
                //We are in a case, Due to this issue : http://stackoverflow.com/questions/19502853/android-4-3-ble-filtering-behaviour-of-startlescan
                //Where the callback has been called twice for the same address
                //We should not do anything
            } else {
                //For each alloweds service, we should have one and only one device.
                //And it should be active at the moment

                //If we're already connected to this address, no need to pursue
                if(isAlreadyConnected(deviceAddress)){
					return;
				}

                //If we have a preferred address and it's not this one, let's not connect
                if(mPreferredAddress.notEmpty() && !mPreferredAddress.get().equals(deviceAddress) ){
					return;
				}


                //We will browse the advertised UUIDs to check if one of them correspond to a supported service
                List<UUID> advertisedUUIDs = parseUUIDs(scanRecord);
                for(UUID u : advertisedUUIDs){
                    Log.v(TAG, deviceAddress+" advertises "+u.toString());
                }
                //The address shouldn't be previously connected, no other device should support this service and it should be an allowed service (for this scan at least)
                //Also if a pairing is known to be needed, It should have already been paired : !GattAttributes.shouldPairBeforeUse(advertisedUUIDs) || (GattAttributes.shouldPairBeforeUse(advertisedUUIDs) && device.getBondState()==BluetoothDevice.BOND_BONDED)
                // Either : No need to pair before use Or : (Need to pair before use and we are actually paired)
                if ((mCurrentService.isEmpty() || !mCurrentService.get().isOneOfThoseServiceConnected(advertisedUUIDs))
                        && !GattAttributes.supportedBLEServiceByEnum(dataSource, advertisedUUIDs, mSupportServicelist).isEmpty()) {

                    Log.d(TAG, "Device "+deviceAddress+" found to support service : "+GattAttributes.supportedBLEServiceByEnum(dataSource,advertisedUUIDs, mSupportServicelist).get(0));

                    EventBus.getDefault().post(new BLESearchEvent(BLESearchEvent.SEARCH_EVENT.ON_SEARCH_SUCCESS));
                    //If yes, let's bind this device !
                    if(mCurrentService.isEmpty()) {
                        bindNewService(deviceAddress);
                    } else {
                        EventBus.getDefault().post(new BLESearchEvent(BLESearchEvent.SEARCH_EVENT.ON_CONNECTING));
                        mCurrentService.get().connect(deviceAddress);
                    }
                }
            }
            if(!mPreviousAddress.contains(deviceAddress)) {
                mPreviousAddress.add(deviceAddress);
            }
        }
    };

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
			bluetoothAdapter.stopLeScan(mLeScanCallback);
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
	
	private boolean isAlreadyConnected(String deviceAddress) {
		//If current service isn't null
		return (mCurrentService.notEmpty()
			//And it is still binded
			&& mCurrentService.get().pingBinder()
			//And the device is still connected
			&& mCurrentService.get().isConnected(deviceAddress)
			//And the given device address is not null
			&& deviceAddress!=null
			);
			//Congrats ! No need to connect, the device is already connected !
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
				//now connect this device
				mCurrentService.get().connect(deviceAddress);
			}
		} );

		//We start the actual binding
		//Note that the service will restart as long as it is binded, because we have set : Activity.BIND_AUTO_CREATE
		context.getApplicationContext().bindService(intent,mCurrentServiceConnection.get(),Activity.BIND_AUTO_CREATE);
		Log.v(MEDBT.TAG, "context.bindService");
	}

	private BluetoothAdapter initBluetoothAdapter() throws BLENotSupportedException, BluetoothDisabledException {
		//If BLE is not supported, we throw an error
		if (!context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			throw new BLENotSupportedException();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		bluetoothAdapter = ((BluetoothManager) context
				.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
		    throw new BluetoothDisabledException();
		}
		
		// Checks if Bluetooth is supported on the device.
		if (bluetoothAdapter == null) {
			throw new BLENotSupportedException();
		}
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


    /*
	 * Util Functions
	 */

    /**
     * This function will help us decrypt the advertise Data and turn them into readable UUIDs
     * @param advertisedData
     * @return a list of UUIDs
     */
    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData,
                                    offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            Log.e(TAG, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }
}

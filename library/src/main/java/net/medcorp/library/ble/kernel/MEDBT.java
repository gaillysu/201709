/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.kernel;

import net.medcorp.library.ble.ble.GattAttributes;
import net.medcorp.library.ble.exception.BLENotSupportedException;
import net.medcorp.library.ble.exception.BluetoothDisabledException;
import net.medcorp.library.ble.model.request.BLERequestData;
import net.medcorp.library.ble.util.Optional;

import java.util.List;


/**
 * The Interface MEDBT is the core manager for the bluetooth interface. (deepest layer)
 * In order to connect to a bluetooth device, we should instantiate it, then add a OnDataReceivedListener.
 * The OnDataReceivedListener will handle all the callbacks coming from the peripherals.
 * 
 * OnDataReceivedListener will receive data in the form of a SensorData, it will be able to get the type of sensor by calling data.getType()
 * Then, once the right sensor is found, it could return the data by calling data.getHeartrate() (if the sensor is a heart rate monitor)
 * 
 * To find nearby devices, use the startScan function.
 * 
 * WARNING !
 * Don't forget to add the following in the manifest :
     
       <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="true" />

    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    
    And in the <application> tag
             <service
            android:name="OUR BT SERVICE"
            android:enabled="true" />
            
 * Also, the minimum target should be android 4.3 !
 * 
 * And don't forget to disconnect(null) before stopping the parent activity
 *
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
public interface MEDBT {
	
	/**
	 * THe logcat tag used by the SDK
	 */
	public static String TAG = "MED BT SDK";


	/**
     * Start scanning for nearby devices supporting the given services, it should connect automatically to the first device encountered.
     * The scan will stop after 10 seconds
     * @param serviceList, the list of services we are looking for
     * @throws BLENotSupportedException
     * @throws BluetoothDisabledException
     */
    void startScan(List<GattAttributes.SupportedService> serviceList, Optional<String> preferredAddress);

    void stopScan();

    /**
     * WARNING ! You should disconnect(Empty Optional) before stopping the parent activity
     */
    void disconnect();

    /**
	 * Send request. Sends a write request to all the devices that supports the right service and characteristic.
	 */
	void sendRequest(BLERequestData request);

    /**
     *
     * @return the SERVICE 's firmware version, it means the BLE firware version
     */
    String getBluetoothVersion();

    /**
     *
     * @return the SERVICE's software version, it means the MCU firmware version
     */
    String getSoftwareVersion();

    /**
     * Pings the currently connected device (if any) to check it is actually connected
     */
    void ping();
}

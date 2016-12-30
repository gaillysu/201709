package net.medcorp.library.ble.controller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import net.medcorp.library.ble.datasource.GattAttributesDataSource;
import net.medcorp.library.ble.model.request.BLERequestData;

import java.util.Set;

/**
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
public interface ConnectionController {

    public  class Singleton {
        private static ConnectionControllerImpl sInstance = null;

        public static ConnectionController getInstance(Context context, GattAttributesDataSource source) {
            if(null == sInstance )
            {
                sInstance = new ConnectionControllerImpl(context, source);
            } else {
                sInstance.setContext(context);
            }
            return sInstance;
        }
        public static void destroy() {
            if(null != sInstance )
            {
                sInstance.destroy();
                sInstance = null;
            }
        }
    }

    public void connect();

    /**
     * used for OTA reconnect ONLY
     */
    public void reconnect();

    public boolean isConnected();

    public void disconnect();

    public void forgetSavedAddress();

    public void sendRequest(BLERequestData request);

    public String getBluetoothVersion();

    public String getSoftwareVersion();

    public void setOTAMode(boolean otaMode, boolean disConnect);

    /**
     While in OTA mode, the ConnectionController will stop responding to normal commands
     */
    public boolean inOTAMode();

    /**
     restore the saved address. BLE OTA use it
     Usage:forgetSavedAddress()/restoreSavedAddress(), if not call forgetSavedAddress()
     before call it, do nothing
     */
    public void restoreSavedAddress();

    public boolean hasSavedAddress();

    public void scan();

    public void pairDevice(String address);
    public void unPairDevice(String address);

    public String getSaveAddress();

    public Set<BluetoothDevice> getDevice();

    public int getBluetoothStatus();


}

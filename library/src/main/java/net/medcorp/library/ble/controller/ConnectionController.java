package net.medcorp.library.ble.controller;

import android.content.Context;

import net.medcorp.library.ble.datasource.GattAttributesDataSource;
import net.medcorp.library.ble.listener.OnConnectListener;
import net.medcorp.library.ble.listener.OnDataReceivedListener;
import net.medcorp.library.ble.listener.OnExceptionListener;
import net.medcorp.library.ble.listener.OnFirmwareVersionListener;
import net.medcorp.library.ble.model.request.RequestData;

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

    public void sendRequest(RequestData request);

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

    public void pairDevice();
    public void unPairDevice();

    public void setOnExceptionListener(OnExceptionListener listener);
    public void setOnDataReceivedListener(OnDataReceivedListener listener);
    public void setOnConnectListener(OnConnectListener listener);
    public void setOnFirmwareVersionListener(OnFirmwareVersionListener listener);

}

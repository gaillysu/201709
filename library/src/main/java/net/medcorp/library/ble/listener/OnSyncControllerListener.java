package net.medcorp.library.ble.listener;

import net.medcorp.library.ble.model.response.ResponseData;
import net.medcorp.library.ble.util.Constants;

/**
 * Created by gaillysu on 15/4/1.
 */
public interface OnSyncControllerListener {

    /**
    Called when a packet is received from the device
    */
    public void packetReceived(ResponseData packet);
    /**
    Called when a peripheral connects or disconnects
    */
    public void connectionStateChanged(boolean isConnected);

    /*
    called when get version info
     */
    public void firmwareVersionReceived(Constants.DfuFirmwareTypes whichfirmware, String version);

    /**
     * add searching functions: @onSearching,@onSearchSuccess,@onSearchFailure,@onConnecting
     */
    public void onSearching();

    public void onSearchSuccess();

    public void onSearchFailure();

    public void onConnecting();

    //the  two functions @onSyncStart,@onSyncEnd
    public void onSyncStart();

    public void onSyncEnd();

    public void onInitializeStart();

    public void onInitializeEnd();

}

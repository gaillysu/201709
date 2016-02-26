package net.medcorp.library.ble.listener;


import net.medcorp.library.ble.controller.OtaController;
import net.medcorp.library.ble.model.response.ResponseData;
import net.medcorp.library.ble.util.Constants;

/**
 * Created by gaillysu on 15/4/1.
 */
public interface OnNevoOtaControllerListener {
    /**
     Called when a packet is received from the device
     */
    public void onPrepareOTA(Constants.DfuFirmwareTypes which);
    public void packetReceived(ResponseData packet);
    public void connectionStateChanged(boolean isConnected);
    public void onDFUStarted();
    public void onDFUCancelled();
    public void onTransferPercentage(int percent);
    public void onSuccessfulFileTranferred();
    public void onError(OtaController.ERRORCODE errorcode);
    /**
     Call when finished OTA, will reconnect nevo and read firmware, refresh the firmware  to screen view
     @parameter whichfirmware, firmware type
     @parameter version, return the version
     */
    public void firmwareVersionReceived(Constants.DfuFirmwareTypes whichfirmware, String version);
}

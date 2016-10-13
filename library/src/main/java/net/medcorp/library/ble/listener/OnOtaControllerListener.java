package net.medcorp.library.ble.listener;


import net.medcorp.library.ble.controller.OtaController;
import net.medcorp.library.ble.model.response.BLEResponseData;
import net.medcorp.library.ble.util.Constants;

/**
 * Created by gaillysu on 15/4/1.
 */
public interface OnOtaControllerListener {
    /**
     Called when a packet is received from the device
     */
    public void onPrepareOTA(Constants.DfuFirmwareTypes which);
    public void packetReceived(BLEResponseData packet);
    public void connectionStateChanged(boolean isConnected);
    public void onDFUStarted();
    public void onDFUCancelled();
    public void onTransferPercentage(int percent);
    public void onSuccessfulFileTranfered();
    public void onError(OtaController.ERRORCODE errorcode);

    /**
     * use Nordic dfu library
     * @param dfuAddress : when BLE device comes into DFU mode, its MAC also got changed
     */
    public void onDFUServiceStarted(String dfuAddress);
    /**
     Call when finished OTA, will reconnect SERVICE and read firmware, refresh the firmware  to screen view
     @parameter whichfirmware, firmware type
     @parameter version, return the version
     */
    public void firmwareVersionReceived(Constants.DfuFirmwareTypes whichfirmware, String version);
}

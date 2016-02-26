package net.medcorp.library.ble.controller;


import net.medcorp.library.ble.listener.OnNevoOtaControllerListener;
import net.medcorp.library.ble.util.Constants;

/**
 * this class define some functions for firmware upgrade.
 * @author Gaillysu
 *
 */
public interface OtaController {

    /**
     * start OTA
     * @param filename
     * @param firmwareType
     */
    void performDFUOnFile(String filename, Constants.DfuFirmwareTypes firmwareType);

    /**
     * cancel OTA
     */
    void cancelDFU();

    /**
     * manualmode: false:normal OTA, nevo get connected and work normal
     *             true: manual OTA: both press Key A & B, insert battery, force nevo entry DFU mode.
     */
    void setManualMode(boolean manualmode);

    /**
     * get in charge of ConnectionController
     */
    void switch2OtaController();

    /**
     * set hight level listener, it should be a activity (OTA controller view:Activity or one fragment)
     */
    void setOnNevoOtaControllerListener(OnNevoOtaControllerListener listener);

    /**
     * read ConnectionController status
     * @return true or false
     */
    Boolean isConnected();

    /**
     * get/set state
     */
    Constants.DFUControllerState getState();

    void setState(Constants.DFUControllerState state);

    void switch2SyncController();

    /**
     * when OTA done successful or failure, reset it to normal mode
     * switch2SyncController: true / false ,which controller will be in charge of connectionController：
     * syncController or otaController
     *
     */
    void reset(boolean switch2SyncController);

    /**
     *
     * @return BLW FW version
     */
    String getFirmwareVersion();
    /**
     *
     * @return MCU FW version
     */
    String getSoftwareVersion();

    /**
     *
     * @param otaMode
     * @param disConnect
     */
    void setOtaMode(boolean otaMode, boolean disConnect);

    /**
     * when BLE OTA done, need unpair Nevo (forget it)
     */
    void forGetDevice();

    public enum ERRORCODE {
        NOCONNECTION,
        TIMEOUT,
        STARTDFUERROR,
        OPENFILEERROR,
        INVALIDRESPONSE,
        NOSUPPORTOLDDFU,
        EXCEPTION,
        CHECKSUMERROR,
        NODFUSERVICE,
        NOFINISHREADVERSION
    }

    public static String PREF_NAME = "nevoPrefs";
    public static String SYNCDATE = "nevoSyncdate";

}

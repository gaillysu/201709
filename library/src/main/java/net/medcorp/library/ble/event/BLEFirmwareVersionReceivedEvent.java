package net.medcorp.library.ble.event;

import net.medcorp.library.ble.util.Constants.DfuFirmwareTypes;

/**
 * Created by karl-john on 18/3/16.
 */
public class BLEFirmwareVersionReceivedEvent {
    private final DfuFirmwareTypes firmwareTypes;
    private final String version;

    public BLEFirmwareVersionReceivedEvent(DfuFirmwareTypes firmwareTypes, String version) {
        this.firmwareTypes = firmwareTypes;
        this.version = version;
    }

    public DfuFirmwareTypes getFirmwareTypes() {
        return firmwareTypes;
    }

    public String getVersion() {
        return version;
    }
}

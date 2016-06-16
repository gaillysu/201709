package net.medcorp.library.ble.event;

/**
 * Created by med on 16/6/10.
 */
public class BLEServerReadRequestEvent {

    final String charUUID;

    public BLEServerReadRequestEvent(String charUUID) {
        this.charUUID = charUUID;
    }

    public String getAddress() {
        return charUUID;
    }
}

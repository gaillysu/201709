package net.medcorp.library.ble.event;

/**
 * Created by med on 16/6/8.
 */
public class BLEServerWriteRequestEvent {
    final byte[] value;
    final String charUUID;

    public BLEServerWriteRequestEvent(byte[] value, String charUUID) {
        this.value = value;
        this.charUUID = charUUID;
    }

    public byte[] getValue() {
        return value;
    }

    public String getAddress() {
        return charUUID;
    }
}

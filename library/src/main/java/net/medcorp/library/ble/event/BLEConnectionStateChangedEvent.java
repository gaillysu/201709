package net.medcorp.library.ble.event;

/**
 * Created by karl-john on 18/3/16.
 */
public class BLEConnectionStateChangedEvent {

    private final boolean connected;
    private final String address;

    public BLEConnectionStateChangedEvent(boolean connected, String address) {
        this.connected = connected;
        this.address = address;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getAddress() {
        return address;
    }
}

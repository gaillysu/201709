package net.medcorp.library.ble.event;

/**
 * Created by karl-john on 18/3/16.
 */
public class BLENotificationEvent {
    private final boolean connected;

    public BLENotificationEvent(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }
}

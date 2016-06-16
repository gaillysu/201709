package net.medcorp.library.ble.event;

/**
 * Created by med on 16/6/8.
 */
public class BLEServerConnectionStateChangedEvent {
    final boolean status;

    public BLEServerConnectionStateChangedEvent(boolean status) {
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }
}

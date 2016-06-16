package net.medcorp.library.ble.event;

/**
 * Created by med on 16/6/10.
 */
public class BLEServerNotificationSentEvent {
    final private int status;

    public BLEServerNotificationSentEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

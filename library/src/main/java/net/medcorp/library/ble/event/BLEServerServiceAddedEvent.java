package net.medcorp.library.ble.event;

/**
 * Created by med on 16/6/10.
 */
public class BLEServerServiceAddedEvent {
    final int status;
    final String serviceUUID;

    public BLEServerServiceAddedEvent(int status, String serviceUUID) {
        this.status = status;
        this.serviceUUID = serviceUUID;
    }

    public int getStatus() {
        return status;
    }

    public String getServiceUUID() {
        return serviceUUID;
    }
}

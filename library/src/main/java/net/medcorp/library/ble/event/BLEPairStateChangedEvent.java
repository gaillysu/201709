package net.medcorp.library.ble.event;

/**
 * Created by med on 16/10/20.
 */

public class BLEPairStateChangedEvent {
    private final int pairState;
    private final String address;

    public BLEPairStateChangedEvent(int pairState, String address) {
        this.pairState = pairState;
        this.address = address;
    }

    public int getPairState() {
        return pairState;
    }

    public String getAddress() {
        return address;
    }
}

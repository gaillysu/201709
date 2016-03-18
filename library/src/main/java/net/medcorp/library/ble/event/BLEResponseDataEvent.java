package net.medcorp.library.ble.event;

import net.medcorp.library.ble.model.response.BLEResponseData;

/**
 * Created by karl-john on 18/3/16.
 */
public class BLEResponseDataEvent {

    private final BLEResponseData data;

    public BLEResponseDataEvent(BLEResponseData data) {
        this.data = data;
    }

    public BLEResponseData getData() {
        return data;
    }
}

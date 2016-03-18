package net.medcorp.library.ble.event;

import net.medcorp.library.ble.exception.BaseBLEException;

/**
 * Created by karl-john on 18/3/16.
 */
public class BLEExceptionEvent {
    private final BaseBLEException bleException;

    public BLEExceptionEvent(BaseBLEException bleException) {
        this.bleException = bleException;
    }

    public BaseBLEException getBleException() {
        return bleException;
    }

}

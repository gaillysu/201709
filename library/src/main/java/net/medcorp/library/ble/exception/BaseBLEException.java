package net.medcorp.library.ble.exception;

import net.medcorp.library.ble.exception.visitor.BLEExceptionVisitable;
import net.medcorp.library.ble.exception.visitor.BLEExceptionVisitor;

/**
 * Created by Karl on 11/5/15.
 */
public abstract class BaseBLEException extends Exception implements BLEExceptionVisitable {

    @Override
    public <T> T accept(BLEExceptionVisitor<T> visitor) {
        return visitor.visit(this);
    }


}

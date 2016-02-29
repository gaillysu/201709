package net.medcorp.library.ble.exception;

import net.medcorp.library.ble.exception.visitor.BLEExceptionVisitor;


/**
 * Created by gaillysu on 15/5/11.
 * if send QuickBt command without bind a SERVICE, will throw this class
 */
public class QuickBTUnBindException extends BaseBLEException {

    @Override
    public <T> T accept(BLEExceptionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

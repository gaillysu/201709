package net.medcorp.library.ble.exception.visitor;

/**
 * Created by Karl on 11/5/15.
 */
public interface BLEExceptionVisitable {
    public <T> T accept(BLEExceptionVisitor<T> visitor);
}

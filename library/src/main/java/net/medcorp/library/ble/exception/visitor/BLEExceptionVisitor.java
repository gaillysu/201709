package net.medcorp.library.ble.exception.visitor;

import net.medcorp.library.ble.exception.BLEConnectTimeoutException;
import net.medcorp.library.ble.exception.BLENotSupportedException;
import net.medcorp.library.ble.exception.BLEUnstableException;
import net.medcorp.library.ble.exception.BaseBLEException;
import net.medcorp.library.ble.exception.BluetoothDisabledException;
import net.medcorp.library.ble.exception.QuickBTSendTimeoutException;
import net.medcorp.library.ble.exception.QuickBTUnBindException;

/**
 * Created by Karl on 11/5/15.
 */
public interface BLEExceptionVisitor<T> {
    
    T visit(QuickBTUnBindException e);

    T visit(BLEConnectTimeoutException e);

    T visit(BLENotSupportedException e);

    T visit(BLEUnstableException e);

    T visit(BluetoothDisabledException e);

    T visit(QuickBTSendTimeoutException e);

    T visit(BaseBLEException e);
}

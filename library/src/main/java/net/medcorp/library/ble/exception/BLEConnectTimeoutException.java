package net.medcorp.library.ble.exception;


import net.medcorp.library.ble.exception.visitor.BLEExceptionVisitor;

/**
 * Created by gaillysu on 15/4/24.
 */
public class BLEConnectTimeoutException extends BaseBLEException {

    /**
     *
     */
    private static final long serialVersionUID = 978984361354590335L;

    @Override
    public <T> T accept(BLEExceptionVisitor<T> visitor) {
        return visitor.visit(this);
    }

}

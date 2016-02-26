package net.medcorp.library.ble.exception;

import net.medcorp.library.ble.exception.visitor.BLEExceptionVisitor;

/**
 * BLE is unstable, the user should restart the bluetooth layer and/or his phone.
 * @author Hugo
 *
 */
public class BLEUnstableException extends BaseBLEException {

	private static final long serialVersionUID = 7967365228475902486L;

	@Override
	public <T> T accept(BLEExceptionVisitor<T> visitor) {
		return visitor.visit(this);
	}

}

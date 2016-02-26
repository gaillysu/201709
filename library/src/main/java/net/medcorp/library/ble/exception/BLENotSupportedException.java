/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.exception;

import net.medcorp.library.ble.exception.visitor.BLEExceptionVisitor;

public class BLENotSupportedException extends BaseBLEException {
	/**
	 *
	 */
	private static final long serialVersionUID = 833229361354590843L;

	@Override
	public <T> T accept(BLEExceptionVisitor<T> visitor) {
		return visitor.visit(this);
	}

}

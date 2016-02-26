/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.exception;

import net.medcorp.library.ble.exception.visitor.BLEExceptionVisitor;

public class BluetoothDisabledException extends BaseBLEException {

	private static final long serialVersionUID = 7450906170950978164L;

	@Override
	public <T> T accept(BLEExceptionVisitor<T> visitor) {
		return visitor.visit(this);
	}

}

package net.medcorp.library.ble.model.response;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import net.medcorp.library.ble.kernel.MEDBT;

import org.apache.commons.codec.binary.Hex;

import java.util.UUID;

/*
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
class MEDRawDataImpl extends MEDRawData {

	private static final long serialVersionUID = 1L;

	//This uuid is only usefull as long as the SDK is not fully integrated into the app, we should remove it after
	private	UUID mUuid;

	private String mAddress;

	private byte[] mRawData;

	public MEDRawDataImpl(BluetoothGattCharacteristic characteristic, String address) {
		mAddress = address;
		mUuid = characteristic.getUuid();
		mRawData = characteristic.getValue();
		Log.i(MEDBT.TAG, new String(Hex.encodeHex(mRawData)));
	}

	@Override
	public String getAddress() {
		return mAddress;
	}

	@Override
	public byte[] getRawData() {
		return mRawData;
	}

	public UUID getUuid() {
		return mUuid;
	}
}


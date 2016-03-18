package net.medcorp.library.ble.model.response;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import net.medcorp.library.ble.kernel.MEDBT;

import org.apache.commons.codec.binary.Hex;

import java.util.UUID;

public class FirmwareData implements BLEResponseData {

    String mAddress;

    UUID mUuid;

    byte[] mRawData;

    /** The TYPE of data, the getType function should return this value. */
    public final static String TYPE = "NevoFirmware";

    public FirmwareData(BluetoothGattCharacteristic characteristic, String address) {

        mAddress = address;

        mUuid = characteristic.getUuid();

        mRawData = characteristic.getValue();

        Log.i(MEDBT.TAG, "Receive Data " + new String(Hex.encodeHex(mRawData)));

    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public byte[] getRawData() {
        return mRawData;
    }

    public UUID getUuid() {
        return mUuid;
    }
}

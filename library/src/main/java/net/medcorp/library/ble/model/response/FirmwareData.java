package net.medcorp.library.ble.model.response;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

/**
 * Created by Hugo on 17/5/15.
 */
public class FirmwareData implements ResponseData {

    String mAddress;

    UUID mUuid;

    byte[] mRawData;

    /** The TYPE of data, the getType function should return this value. */
    public final static String TYPE = "NevoFirmware";

    public FirmwareData(BluetoothGattCharacteristic characteristic, String address) {

        mAddress = address;

        mUuid = characteristic.getUuid();

        mRawData = characteristic.getValue();


//        Log.i("Nevo Received", mUuid.toString() + " : " + new String(Hex.encodeHex(mRawData)));
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

package net.medcorp.library.ble.model.response;

/**
 * Created by karl-john on 26/2/16.
 */
public class UnknownData implements BLEResponseData {

    @Override
    public String getAddress() {
        return "00:00:00:00:00:00";
    }

    @Override
    public String getType() {
        return  "undefined type";
    }

    @Override
    public byte[] getRawData() {
        return new byte[0];
    }

}

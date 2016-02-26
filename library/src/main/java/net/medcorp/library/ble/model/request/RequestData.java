package net.medcorp.library.ble.model.request;

import android.content.Context;

import net.medcorp.library.ble.datasource.GattAttributesDataSource;

import java.util.UUID;


/**
 * Created by gaillysu on 15/4/1.
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */

public abstract class RequestData {

    protected Context context;
    private GattAttributesDataSource dataSource;

    public RequestData(Context context, GattAttributesDataSource dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    public UUID getServiceUUID() {
        return dataSource.getNevoService();
    }

    public UUID getCharacteristicUUID() {
        return dataSource.getNevoCallbackCharacteristic();
    }

    public UUID getInputCharacteristicUUID() {
        return dataSource.getNevoInputCharacteristic();
    }

    public UUID getOTACharacteristicUUID() {
        return dataSource.getNevoOtaCharacteristic();
    }

    public UUID getNotificationCharacteristicUUID() {
        return dataSource.getNevoNotificationCharacteristic();
    }

    /**
     * @return the raw data to be sent
     */
    public abstract byte[] getRawData();

    /**
     * @return the raw data to be sent, more  packets
     */
    public abstract byte[][] getRawDataEx();

    /**
     * @return the command 's value
     */
    public abstract byte getHeader();


}

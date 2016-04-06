package net.medcorp.library.ble.model.request;

import net.medcorp.library.ble.datasource.GattAttributesDataSource;

import java.util.UUID;


/**
 * Created by gaillysu on 15/4/1.
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */

public abstract class RequestData {

    private GattAttributesDataSource dataSource;

    public RequestData(GattAttributesDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UUID getServiceUUID() {
        return dataSource.getService();
    }

    public UUID getCharacteristicUUID() {
        return dataSource.getCallbackCharacteristic();
    }

    public UUID getInputCharacteristicUUID() {
        return dataSource.getInputCharacteristic();
    }

    public UUID getOTACharacteristicUUID() {
        return dataSource.getOtaCharacteristic();
    }

    public UUID getNotificationCharacteristicUUID() {
        return dataSource.getNotificationCharacteristic();
    }

    public UUID getOTAServiceUUID()
    {
        return dataSource.getOtaService();
    }

    public UUID getOTACallbackCharacteristicUUID()
    {
        return dataSource.getOtaCallbackCharacteristic();
    }

    public UUID getOTAControlCharacteristicUUID()
    {
        return dataSource.getOtaControlCharacteristic();
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

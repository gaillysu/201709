package net.medcorp.library.ble.datasource;

import java.util.UUID;

/**
 * Created by karl-john on 25/2/16.
 */
public interface  GattAttributesDataSource {

    public UUID getDeviceInfoUDID();

    public UUID getDeviceInfoBluetoothVersion();

    public UUID getDeviceInfoSoftwareVersion();

    public UUID getClientCharacteristicConfig();

    public UUID getService();

    public UUID getCallbackCharacteristic();

    public UUID getInputCharacteristic();

    public UUID getOtaCharacteristic();

    public UUID getNotificationCharacteristic();

    public UUID getOtaService();

    public UUID getOtaControlCharacteristic();

    public UUID getOtaCallbackCharacteristic();


}

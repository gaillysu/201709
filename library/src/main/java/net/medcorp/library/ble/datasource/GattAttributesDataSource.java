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

    public UUID getNevoService();

    public UUID getNevoCallbackCharacteristic();

    public UUID getNevoInputCharacteristic();

    public UUID getNevoOtaCharacteristic();

    public UUID getNevoNotificationCharacteristic();

    public UUID getNevoOtaService();

    public UUID getNevoOtaControlCharacteristic();

    public UUID getNevoOtaCallbackCharacteristic();


}

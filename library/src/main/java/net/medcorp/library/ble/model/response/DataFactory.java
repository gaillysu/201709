/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.model.response;

import android.bluetooth.BluetoothGattCharacteristic;

import net.medcorp.library.ble.datasource.GattAttributesDataSource;

public class DataFactory {
	
	public static BLEResponseData fromBluetoothGattCharacteristic(GattAttributesDataSource dataSource, final BluetoothGattCharacteristic characteristic, final String address) {
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		if (dataSource.getCallbackCharacteristic().equals(characteristic.getUuid())){
			return new MEDRawDataImpl(characteristic, address);
		} else if (dataSource.getOtaCallbackCharacteristic().equals(characteristic.getUuid())
				|| dataSource.getOtaCharacteristic().equals(characteristic.getUuid())){
			return new FirmwareData(characteristic, address);
		}
        return new UnknownData();
	}

}

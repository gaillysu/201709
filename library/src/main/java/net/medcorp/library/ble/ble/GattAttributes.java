/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.ble;

import android.bluetooth.BluetoothGattCharacteristic;

import net.medcorp.library.ble.datasource.GattAttributesDataSource;
import net.medcorp.library.ble.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
public class GattAttributes {

    public enum SupportedService {
        nevo,
        nevo_ota,
        allService
    }

    public static boolean supportedBLEService(GattAttributesDataSource source, UUID uuid) {
		if (uuid.equals(source.getNevoService())) {
            return true;
        }
		return false;
	}
    
    public static boolean supportedBLECharacteristic(GattAttributesDataSource source,UUID uuid){
        if (uuid.equals(source.getNevoCallbackCharacteristic())
                || uuid.equals(source.getNevoOtaCallbackCharacteristic())
                || uuid.equals(source.getNevoOtaCharacteristic())) {
            return true;
        }
		return false;
    }
    
    public static boolean shouldInitBLECharacteristic(UUID uuid){
		return false;
    }
    
    public static BluetoothGattCharacteristic initBLECharacteristic(UUID uuid, BluetoothGattCharacteristic characteristic){
		return characteristic;
    }

    public static boolean supportedBLEService(GattAttributesDataSource source, List<UUID> uuids) {
    	boolean supported = false;
    	for(UUID uuid : uuids){
    		if(supportedBLEService(source,uuid)) {
                return true;
            }
    	}
		return false;
	}
    
    public static Optional<SupportedService> TransferUUID2SupportedService(GattAttributesDataSource source, UUID uuid)
    {
        if(uuid.equals(source.getNevoService())){
        return new Optional<SupportedService>(SupportedService.nevo);
    }
        if(uuid.equals(source.getNevoOtaService())){
        return new Optional<SupportedService>(SupportedService.nevo_ota);
    }
       return new Optional<SupportedService>();
    }
    
    public static List<UUID> supportedBLEServiceByEnum(GattAttributesDataSource dataSource, List<UUID> uuids,List<SupportedService> supportServicelist) {
    	List<UUID> chosenServices = new ArrayList<UUID>();
    	
    	for(UUID uuid : uuids){
    		Optional<SupportedService> service = TransferUUID2SupportedService(dataSource, uuid);
    		
    		//If this service is unknown, no reason to pursue
    		if(service.isEmpty()){
                continue;
            }
    		
    		//If all services are supported, then we add each and every services we find.
    		//If the service we are investigating is in the supported services list, we add it too
    		if(supportServicelist.contains(SupportedService.allService)
    				|| supportServicelist.contains(service.get())) {
    			chosenServices.add(uuid);
    			continue;
    		}
    	}
		return chosenServices;
	}
}

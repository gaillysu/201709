/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.ble;

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
        SERVICE,
        OTA_SERVICE,
        ALL_SERVICE
    }

    public static boolean supportedBLEService(GattAttributesDataSource source, UUID uuid) {
		if (uuid.equals(source.getService())) {
            return true;
        }
		return false;
	}
    
    public static boolean supportedBLECharacteristic(GattAttributesDataSource source,UUID uuid){
        if (uuid.equals(source.getCallbackCharacteristic())
                || uuid.equals(source.getNotificationCharacteristic())
                || uuid.equals(source.getOtaCallbackCharacteristic())
                || uuid.equals(source.getOtaCharacteristic())) {
            return true;
        }
		return false;
    }

    public static boolean supportedBLEService(GattAttributesDataSource source, List<UUID> uuids) {
    	for(UUID uuid : uuids){
    		if(supportedBLEService(source,uuid)) {
                return true;
            }
    	}
		return false;
	}
    
    public static Optional<SupportedService> TransferUUID2SupportedService(GattAttributesDataSource source, UUID uuid)
    {
        if(uuid.equals(source.getService())){
        return new Optional<SupportedService>(SupportedService.SERVICE);
    }
        if(uuid.equals(source.getOtaService())){
        return new Optional<SupportedService>(SupportedService.OTA_SERVICE);
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
    		if(supportServicelist.contains(SupportedService.ALL_SERVICE)
    				|| supportServicelist.contains(service.get())) {
    			chosenServices.add(uuid);
    			continue;
    		}
    	}
		return chosenServices;
	}
}

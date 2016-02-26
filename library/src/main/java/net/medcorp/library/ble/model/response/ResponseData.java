/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.model.response;

import java.io.Serializable;

/**
 * The Interface SensorData is a wrapper for different types of SensorData.
 * It is given to the callback at regular intervals and contains all the data sent by the peripheral.
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
public interface ResponseData extends Serializable {
	
	/**
	 * The mac address of the peripheral that sent those data.
	 *
	 * @return the address
	 */
	public String getAddress();
	
	/**
	 * Gets the type of peripheral. It could return PowerSensorData.TYPE or ComboSensorData.TYPE or any other kind of data.
	 *
	 * @return the type
	 */
	public String getType();

	public abstract byte[] getRawData();

}

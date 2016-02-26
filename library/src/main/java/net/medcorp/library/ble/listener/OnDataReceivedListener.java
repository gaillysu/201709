/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.listener;


import net.medcorp.library.ble.model.response.ResponseData;

/**
 * The Interface OnDataReceivedListener should be implemented by the class that receives data from the peripherals.
 * It is a listener who's callback will be called every time the peripherals sends data.
 */
public interface OnDataReceivedListener {

	/**
	 * This callback function will be called periodically, with data coming from censors.
	 *
	 * @param data the data coming from peripherals. Several kinds of SensorData are possible depending on the connected peripheral.
	 */
	public void onDataReceived(ResponseData data);
	
}

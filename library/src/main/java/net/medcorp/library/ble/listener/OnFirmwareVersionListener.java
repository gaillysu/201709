/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.listener;


import net.medcorp.library.ble.util.Constants;

/**
 * This Listener will be Called when finish reading Firmware
 * @author Gaillysu
 *
 */
public interface OnFirmwareVersionListener {

    /**
     Call when finish reading Firmware
     @parameter firmwareTypes, firmware type
     @parameter version, return the version
     */
    public void  firmwareVersionReceived(Constants.DfuFirmwareTypes firmwareTypes, String version);
}

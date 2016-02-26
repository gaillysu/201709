/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.listener;


/**
 * This Listener will be called when a given peripheral is connected.
 * This call isn't reliable and can't be trusted fully
 * @author Hugo
 *
 */
public interface OnConnectListener {
	
	/**
	 * This function is called everytime a device is connected or disconnected
	 */
    public void onConnectionStateChanged(boolean connected, String address);

    /**
     * add searching functions: @onSearching,@onSearchSuccess,@onSearchFailure,@onConnecting
     */
    public void onSearching();

    public void onSearchSuccess();

    public void onSearchFailure();

    public void onConnecting();
}

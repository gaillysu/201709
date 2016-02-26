/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.listener;


import net.medcorp.library.ble.exception.BaseBLEException;


/**
 * This Listener will be called when an exception have been raised.
 * The user should be informed properly and react accordingly.
 * @author Hugo
 *
 */
	
public interface OnExceptionListener {
		/**
		 * This function is called everytime an important exception is raised.
		 * Up to you to inform the user and/or launch a re-scan
		 */
		public void onException(BaseBLEException e);


}

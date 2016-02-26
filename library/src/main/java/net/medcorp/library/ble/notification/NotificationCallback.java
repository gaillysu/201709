package net.medcorp.library.ble.notification;

import net.medcorp.library.ble.exception.BaseBLEException;

/**
 * Created by gaillysu on 15/4/30.
 */
public interface NotificationCallback {
    /**
     *
     * @param e : Exception when got error, link@QuickBTUnBindNevoException,QuickBTSendTimeoutException
     */
    public void onErrorDetected(BaseBLEException e);
}

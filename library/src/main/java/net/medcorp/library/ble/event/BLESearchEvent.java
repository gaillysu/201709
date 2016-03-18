package net.medcorp.library.ble.event;

/**
 * Created by karl-john on 18/3/16.
 */
public class BLESearchEvent {

    public enum SEARCH_EVENT{
        ON_SEARCHING,
        ON_SEARCH_SUCCESS,
        ON_SEARCH_FAILURE,
        ON_CONNECTING
    }

    private final SEARCH_EVENT searchEvent;

    public BLESearchEvent(SEARCH_EVENT searchEvent) {
        this.searchEvent = searchEvent;
    }

    public SEARCH_EVENT getSearchEvent() {
        return searchEvent;
    }
}

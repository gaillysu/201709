package net.medcorp.library.worldclock.event;

/**
 * Created by Karl on 8/10/16.
 */

public class WorldClockInitializeEvent {

    private final STATUS status;

    private Exception e;

    public WorldClockInitializeEvent(STATUS status) {
        this.status = status;
    }

    public WorldClockInitializeEvent(STATUS status, Exception e) {
        this.status = status;
        this.e = e;
    }

    public STATUS getStatus() {
        return status;
    }

    public enum STATUS{
        STARTED_CITIES,
        STARTED_TIMEZONES,
        STARTED,
        EXCEPTION,
        FINISHED_CITIES,
        FINISHED_TIMEZONES,
        FINISHED
        // If fatal error triggers, please tell Karl
    }

}

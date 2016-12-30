package net.medcorp.library.worldclock;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.realm.RealmObject;

import static net.medcorp.library.worldclock.util.DaylightSavingTimeUtil.getEndDST;
import static net.medcorp.library.worldclock.util.DaylightSavingTimeUtil.getStartDST;

/**
 * Created by Karl on 8/4/16.
 */

public class City extends RealmObject {

    private int id;

    private String name;

    private String country;

    private double lat;

    private double lng;

    @SerializedName("timezone_id")
    private int timezoneId;

    private TimeZone timezoneRef;

    private boolean selected;

    public City() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public TimeZone getTimezoneRef() {
        return timezoneRef;
    }

    public void setTimezoneRef(TimeZone timezoneRef) {
        this.timezoneRef = timezoneRef;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getTimezoneId() {
        return timezoneId;
    }

    public void setTimezoneId(int timezoneId) {
        this.timezoneId = timezoneId;
    }

    public boolean hasDST() {
        return getTimezoneRef().getDstTimeOffset() != 0;
    }

    public int getOffSetFromGMT() {
        if (hasDST()) {
            Calendar start = getStartDST(timezoneRef);
            Calendar end = getEndDST(timezoneRef);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSSSSS", Locale.US);
            if (start.before(Calendar.getInstance()) && end.after(Calendar.getInstance())) {
                return getTimezoneRef().getGmtTimeOffset() + getTimezoneRef().getDstTimeOffset();
            }
        }
        return getTimezoneRef().getGmtTimeOffset();
    }

    public void log(String tag) {
        Log.w(tag, "id       = " + getId());
        Log.w(tag, "name     = " + getName());
        Log.w(tag, "country  = " + getCountry());
        Log.w(tag, "lat      = " + getLat());
        Log.w(tag, "lng      = " + getLng());
        Log.w(tag, "tzid     = " + getTimezoneId());
        Log.w(tag, "tzr      = " + getTimezoneRef());
    }
}

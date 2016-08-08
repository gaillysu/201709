package net.medcorp.library.worldclock;

import android.util.Log;

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

    private String gmt;

    private String country;

    private double lat;

    private double lng;

    private String timezone;

    private TimeZone timezoneRef;

    private boolean selected;

    public City(){

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

    public String getGmt() {
        return gmt;
    }

    public void setGmt(String gmt) {
        this.gmt = gmt;
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

    public void setTimezone(TimeZone timezoneRef) {
        this.timezoneRef = timezoneRef;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimezone() {
        return timezone;
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

    public boolean hasDST(){
        return getTimezoneRef().getDst_time_offset() != 0;
    }

    public int getOffSetFromLocalTime() {
        if (hasDST()){
            Calendar start = getStartDST(timezoneRef);
            Calendar end = getEndDST(timezoneRef);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSSSSS",Locale.US);
            Log.w("Karl","CITY: " + getName());
            Log.w("Karl","Start = " + simpleDateFormat.format(start.getTimeInMillis()));
            Log.w("Karl","End = " + simpleDateFormat.format(end.getTimeInMillis()));
            if (start.after(Calendar.getInstance()) && end.before(Calendar.getInstance())){
                Log.w("Karl","Its daylight saving time now!");
                return getTimezoneRef().getDst_time_offset();
            }
        }
        return 0;
    }
}

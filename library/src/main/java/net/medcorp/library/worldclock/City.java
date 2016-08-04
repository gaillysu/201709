package net.medcorp.library.worldclock;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by Karl on 8/4/16.
 */

public class City extends RealmObject {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("gmt")
    private String gmtName;

    @SerializedName("country")
    private String country;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    @SerializedName("timezone_id")
    private TimeZone timezone;
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

    public String getGmtName() {
        return gmtName;
    }

    public void setGmtName(String gmtName) {
        this.gmtName = gmtName;
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

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }
}

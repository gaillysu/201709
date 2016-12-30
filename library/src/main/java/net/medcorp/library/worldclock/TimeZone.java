package net.medcorp.library.worldclock;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by Karl on 8/4/16.
 */

public class TimeZone extends RealmObject {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("gmt")
    private String gmt;

    @SerializedName("offset")
    private short gmtTimeOffset;

    @SerializedName("std_name")
    private String stdName;

    @SerializedName("dst_month_start")
    private short dstMonthStart;

    @SerializedName("day_in_month_start")
    private short dstDayInMonthStart;

    @SerializedName("dst_time_start")
    private String dstTimeStart;

    @SerializedName("dst_name")
    private String dstName;

    @SerializedName("dst_time_offset")
    private short dstTimeOffset;

    @SerializedName("dst_month_end")
    private short dstMonthEnd;

    @SerializedName("day_in_month_end")
    private short dstDayInMonthEnd;

    @SerializedName("dst_time_end")
    private String dstTimeEnd;


    public TimeZone() {

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

    public short getGmtTimeOffset() {
        return gmtTimeOffset;
    }

    public void setGmtTimeOffset(short gmtTimeOffset) {
        this.gmtTimeOffset = gmtTimeOffset;
    }

    public String getStdName() {
        return stdName;
    }

    public void setStdName(String stdName) {
        this.stdName = stdName;
    }

    public short getDstMonthStart() {
        return dstMonthStart;
    }

    public void setDstMonthStart(short dstMonthStart) {
        this.dstMonthStart = dstMonthStart;
    }

    public short getDstDayInMonthStart() {
        return dstDayInMonthStart;
    }

    public void setDstDayInMonthStart(short dstDayInMonthStart) {
        this.dstDayInMonthStart = dstDayInMonthStart;
    }

    public String getDstTimeStart() {
        return dstTimeStart;
    }

    public void setDstTimeStart(String dstTimeStart) {
        this.dstTimeStart = dstTimeStart;
    }

    public String getDstName() {
        return dstName;
    }

    public void setDstName(String dstName) {
        this.dstName = dstName;
    }

    public short getDstTimeOffset() {
        return dstTimeOffset;
    }

    public void setDstTimeOffset(short dstTimeOffset) {
        this.dstTimeOffset = dstTimeOffset;
    }

    public short getDstMonthEnd() {
        return dstMonthEnd;
    }

    public void setDstMonthEnd(short dstMonthEnd) {
        this.dstMonthEnd = dstMonthEnd;
    }

    public short getDstDayInMonthEnd() {
        return dstDayInMonthEnd;
    }

    public void setDstDayInMonthEnd(short dstDayInMonthEnd) {
        this.dstDayInMonthEnd = dstDayInMonthEnd;
    }

    public String getDstTimeEnd() {
        return dstTimeEnd;
    }

    public void setDstTimeEnd(String dstTimeEnd) {
        this.dstTimeEnd = dstTimeEnd;
    }

    public void log(String tag) {
        Log.w(tag, toString());
    }

    @Override
    public String toString() {
        return "Id = " + getId() + "\n" +
                "Name = " + getName() + "\n" +
                "Gmt = " + getGmt() + "\n" +
                "Offset = " + getGmtTimeOffset() + "\n" +
                "StdName = " + getStdName() + "\n" +
                "DstMonthStart = " + getDstMonthStart() + "\n" +
                "DstDayInMonthStart = " + getDstDayInMonthStart() + "\n" +
                "DstTimeStart = " + getDstTimeStart() + "\n" +
                "DstName = " + getDstName() + "\n" +
                "Name = " + getName() + "\n" +
                "DstMonthEnd = " + getDstMonthEnd() + "\n" +
                "DstDayInMonthEnd = " + getDstDayInMonthEnd() + "\n" +
                "DstTimeEnd = " + getDstTimeEnd() + "\n";
    }

}


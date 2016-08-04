package net.medcorp.library.worldclock;

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
    private short offset;

    @SerializedName("std_name")
    private String stdName;

    @SerializedName("dst_month_start")
    private short dstMonthStart;

    @SerializedName("dst_day_week_start")
    private short dstDayWeekStart;

    @SerializedName("dst_nth_day_start")
    private short dstNthDayStart;

    @SerializedName("dst_time_start")
    private String dstTimeStart;

    @SerializedName("dst_name")
    private String dstName;

    @SerializedName("dst_time_offset")
    private short dstTimeOffset;

    @SerializedName("dst_month_end")
    private short dstMonthEnd;

    @SerializedName("dst_day_week_end")
    private short dstDayWeekEnd;

    @SerializedName("dst_nth_day_end")
    private short dstNthDayEnd;

    @SerializedName("dst_time_end")
    private String dstTimeEnd;


    public TimeZone(){

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

    public short getOffset() {
        return offset;
    }

    public void setOffset(short offset) {
        this.offset = offset;
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

    public short getDstDayWeekStart() {
        return dstDayWeekStart;
    }

    public void setDstDayWeekStart(short dstDayWeekStart) {
        this.dstDayWeekStart = dstDayWeekStart;
    }

    public short getDstNthDayStart() {
        return dstNthDayStart;
    }

    public void setDstNthDayStart(short dstNthDayStart) {
        this.dstNthDayStart = dstNthDayStart;
    }

    public String getDstTimeStart() {
        return dstTimeStart;
    }

    public void setDstTimeStart(String dstTimeStart) {
        this.dstTimeStart = dstTimeStart;
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

    public short getDstDayWeekEnd() {
        return dstDayWeekEnd;
    }

    public void setDstDayWeekEnd(short dstDayWeekEnd) {
        this.dstDayWeekEnd = dstDayWeekEnd;
    }

    public short getDstNthDayEnd() {
        return dstNthDayEnd;
    }

    public void setDstNthDayEnd(short dstNthDayEnd) {
        this.dstNthDayEnd = dstNthDayEnd;
    }

    public String getDstTimeEnd() {
        return dstTimeEnd;
    }

    public void setDstTimeEnd(String dstTimeEnd) {
        this.dstTimeEnd = dstTimeEnd;
    }

    public String getDstName() {
        return dstName;
    }

    public void setDstName(String dstName) {
        this.dstName = dstName;
    }

    //    realm.beginTransaction();
    //    dbObj obj = realm.createObject(dbObj.class);
    //
    //    // increatement index
    //    int nextID = (int) (realm.where(dbObj.class).maximumInt("id") + 1);
    //    // insert new value
    //    obj.setId(nextID);
    //    obj.setName("thang");
    //    obj.setAge(10);
    //
    //    realm.commitTransaction();
}


package net.medcorp.library.worldclock;

import android.util.Log;

import io.realm.RealmObject;

/**
 * Created by Karl on 8/4/16.
 */

public class TimeZone extends RealmObject {
    private int id;

    private String name;

    private String gmt;

    private short gmt_offset;

    private String std_name;

    private short dst_month_start;

    private short dst_day_in_month_start;

    private String dst_time_start;

    private String dst_name;

    private short dst_time_offset;

    private short dst_month_end;

    private short dst_day_in_month_end;

    private String dst_time_end;


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

    public short getGmt_offset() {
        return gmt_offset;
    }

    public void setGmt_offset(short gmt_offset) {
        this.gmt_offset = gmt_offset;
    }

    public String getStd_name() {
        return std_name;
    }

    public void setStd_name(String std_name) {
        this.std_name = std_name;
    }

    public short getDst_month_start() {
        return dst_month_start;
    }

    public void setDst_month_start(short dst_month_start) {
        this.dst_month_start = dst_month_start;
    }


    public String getDst_time_start() {
        return dst_time_start;
    }

    public void setDst_time_start(String dst_time_start) {
        this.dst_time_start = dst_time_start;
    }

    public short getDst_time_offset() {
        return dst_time_offset;
    }

    public void setDst_time_offset(short dst_time_offset) {
        this.dst_time_offset = dst_time_offset;
    }

    public short getDst_month_end() {
        return dst_month_end;
    }

    public void setDst_month_end(short dst_month_end) {
        this.dst_month_end = dst_month_end;
    }



    public String getDst_time_end() {
        return dst_time_end;
    }

    public void setDst_time_end(String dst_time_end) {
        this.dst_time_end = dst_time_end;
    }

    public String getDst_name() {
        return dst_name;
    }

    public void setDst_name(String dst_name) {
        this.dst_name = dst_name;
    }

    public void log(String tag){
        Log.w(tag,toString());
    }

    @Override
    public String toString() {
        return  "Id = " + getId()+ "\n" +
                "Name = " + getName() + "\n" +
                "Gmt = " + getGmt() + "\n" +
                "Offset = " + getGmt_offset() + "\n" +
                "StdName = " + getStd_name() + "\n" +
                "DstMonthStart = " + getDst_month_start() + "\n" +
                "DstDayInMonthStart = " + getDst_day_in_month_start() + "\n" +
                "DstTimeStart = " + getDst_time_start() + "\n" +
                "DstName = " + getDst_name() + "\n" +
                "Name = " + getName() + "\n" +
                "DstMonthEnd = " + getDst_month_end() + "\n" +
                "DstDayInMonthEnd = " + getDst_day_in_month_end() + "\n" +
                "DstTimeEnd = " + getDst_time_end() + "\n";
    }

    public short getDst_day_in_month_start() {
        return dst_day_in_month_start;
    }

    public void setDst_day_in_month_start(short dst_day_in_month_start) {
        this.dst_day_in_month_start = dst_day_in_month_start;
    }

    public short getDst_day_in_month_end() {
        return dst_day_in_month_end;
    }

    public void setDst_day_in_month_end(short dst_day_in_month_end) {
        this.dst_day_in_month_end = dst_day_in_month_end;
    }
}


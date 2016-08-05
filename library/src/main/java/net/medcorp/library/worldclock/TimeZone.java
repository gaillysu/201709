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

    private short offset;

    private String std_name;

    private short dst_month_start;

    private short dst_day_week_start;

    private short dst_nth_day_start;

    private String dst_time_start;

    private String dst_name;

    private short dst_time_offset;

    private short dst_month_end;

    private short dst_day_week_end;

    private short dst_nth_day_end;

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

    public short getOffset() {
        return offset;
    }

    public void setOffset(short offset) {
        this.offset = offset;
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

    public short getDst_day_week_start() {
        return dst_day_week_start;
    }

    public void setDst_day_week_start(short dst_day_week_start) {
        this.dst_day_week_start = dst_day_week_start;
    }

    public short getDst_nth_day_start() {
        return dst_nth_day_start;
    }

    public void setDst_nth_day_start(short dst_nth_day_start) {
        this.dst_nth_day_start = dst_nth_day_start;
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

    public short getDst_day_week_end() {
        return dst_day_week_end;
    }

    public void setDst_day_week_end(short dst_day_week_end) {
        this.dst_day_week_end = dst_day_week_end;
    }

    public short getDst_nth_day_end() {
        return dst_nth_day_end;
    }

    public void setDst_nth_day_end(short dst_nth_day_end) {
        this.dst_nth_day_end = dst_nth_day_end;
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
                "Offset = " + getOffset() + "\n" +
                "StdName = " + getStd_name() + "\n" +
                "DstMonthStart = " + getDst_month_start() + "\n" +
                "DstDayWeekStart = " + getDst_day_week_start() + "\n" +
                "DstNthDayStart = " + getDst_nth_day_start() + "\n" +
                "DstTimeStart = " + getDst_time_start() + "\n" +
                "DstName = " + getDst_name() + "\n" +
                "Name = " + getName() + "\n" +
                "DstMonthEnd = " + getDst_month_end() + "\n" +
                "DstDayWeekEnd = " + getDst_day_week_end() + "\n" +
                "DstNthDayEnd = " + getDst_nth_day_end() + "\n" +
                "DstTimeEnd = " + getDst_time_end() + "\n";
    }

}


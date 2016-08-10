package net.medcorp.library.worldclock.util;

import net.medcorp.library.worldclock.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by karl-john on 8/8/2016.
 */

public class DaylightSavingTimeUtil {



    public static Calendar getStartDST(TimeZone timeZone){
        return getDateByTimeZoneVariable(timeZone.getDst_month_start(), timeZone.getDst_day_in_month_start(),timeZone.getDst_time_start());
    }

    public static Calendar getEndDST(TimeZone timeZone){
        return getDateByTimeZoneVariable(timeZone.getDst_month_end(), timeZone.getDst_day_in_month_end(),timeZone.getDst_time_end());
    }

    public static long getStartDSTTimeStamp(TimeZone timeZone){
        return getStartDST(timeZone).getTimeInMillis();
    }

    public static long getStopDSTTimeStamp(TimeZone timeZone){
        return getEndDST(timeZone).getTimeInMillis();
    }

    public static Calendar getDateByTimeZoneVariable(int month, int dayOfMonth, String time){
        DateTime dateTime = new DateTime();
        dateTime = dateTime.toDateTime(DateTimeZone.UTC);
        dateTime = dateTime.withYear(Calendar.getInstance().get(Calendar.YEAR));
        dateTime = dateTime.withMonthOfYear(month);
        dateTime = dateTime.withDayOfMonth(dayOfMonth);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        try {
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(sdf.parse(time));
            dateTime = dateTime.withTime(timeCalendar.get(Calendar.HOUR_OF_DAY),timeCalendar.get(Calendar.MINUTE),0,0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime.toCalendar(Locale.US);
    }

}

package net.medcorp.library.worldclock.util;

import net.medcorp.library.worldclock.TimeZone;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by karl-john on 8/8/2016.
 */

public class DaylightSavingTimeUtil {



    public static Calendar getStartDST(TimeZone timeZone){
        return getDateByTimeZoneVariable(timeZone.getDst_month_start(), timeZone.getDst_day_week_start(),timeZone.getDst_nth_day_start(),timeZone.getDst_time_start());
    }

    public static Calendar getEndDST(TimeZone timeZone){
        return getDateByTimeZoneVariable(timeZone.getDst_month_end(), timeZone.getDst_day_week_end(),timeZone.getDst_nth_day_end(),timeZone.getDst_time_end());
    }

    public static long getStartDSTTimeStamp(TimeZone timeZone){
        return getStartDST(timeZone).getTimeInMillis();
    }

    public static long getStopDSTTimeStamp(TimeZone timeZone){
        return getEndDST(timeZone).getTimeInMillis();
    }

    public static Calendar getDateByTimeZoneVariable(int month, int dayOfWeek, int weekOfMonth, String time){
        DateTime dateTime = new DateTime();
        dateTime = dateTime.withYear(Calendar.getInstance().get(Calendar.YEAR));
        dateTime = dateTime.withDayOfWeek(dayOfWeek+1);
        dateTime = dateTime.plusWeeks(weekOfMonth);

//        Calendar calendar = Calendar.getInstance();
//
//        // For some strange reason, January starts at 0. So we have to minutes one at the month.
//        calendar.set(Calendar.MONTH, month-1);
//
//        // for some strange reason, Sunday starts at 1, so add +1 since our DB starts at 0.
//        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek+1);
//        if(calendar.getMaximum(Calendar.WEEK_OF_MONTH) - calendar.getActualMaximum(Calendar.WEEK_OF_MONTH) == 1){
//            Log.w("KARL","Max IS 1");
//            calendar.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
//        }else {
//            Log.w("KARL","Max IS 0");
//            calendar.set(Calendar.WEEK_OF_MONTH, weekOfMonth+1);
//        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        try {
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(sdf.parse(time));
            dateTime.withTime(timeCalendar.get(Calendar.HOUR_OF_DAY),timeCalendar.get(Calendar.MINUTE),0,0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime.toCalendar(Locale.US);
    }

}

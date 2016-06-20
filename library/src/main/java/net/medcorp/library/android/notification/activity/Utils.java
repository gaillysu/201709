package net.medcorp.library.android.notification.activity;

import android.content.*;
import android.os.*;
import android.app.*;

public final class Utils
{
    public static void notify(final Context context) {
        final NotificationManager notificationManager = (NotificationManager)context.getSystemService("notification");
        final PendingIntent activity = PendingIntent.getActivity(context, 0, new Intent(context, (Class)ConnectionActivity.class), 1073741824);
        Notification notification;
        if (Build.VERSION.SDK_INT >= 20) {
            notification = new Notification.Builder(context).setSmallIcon(2130903040).setContentTitle((CharSequence)"Notification!").setContentText((CharSequence)"Hello!").addAction(new Notification.Action(2130903040, (CharSequence)"Accept", activity)).setContentIntent(activity).build();
        }
        else {
            notification = new Notification.Builder(context).setSmallIcon(2130903040).setContentTitle((CharSequence)"Notification!").setContentText((CharSequence)"Hello!").setContentIntent(activity).build();
        }
        notificationManager.notify(0, notification);
    }
}

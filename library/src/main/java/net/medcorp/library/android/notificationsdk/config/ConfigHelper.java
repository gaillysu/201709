package net.medcorp.library.android.notificationsdk.config;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.Nullable;

import net.medcorp.library.android.notificationsdk.R;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class ConfigHelper
{
    public static Set<String> getANSCallPackages() {
        return new HashSet<String>(ConfigConstants.ANS_DIALER_APPS);
    }
    
    public static Set<String> getANSSMSPackages() {
        return new HashSet<String>(ConfigConstants.ANS_SMS_APPS);
    }
    
    public static Set<String> getAllPackages(final Context context) {
        final HashSet<String> set = new HashSet<String>();
        final Iterator<PackageInfo> iterator = context.getPackageManager().getInstalledPackages(0).iterator();
        while (iterator.hasNext()) {
            set.add(iterator.next().packageName);
        }
        return set;
    }
    
    public static Set<String> getCallPackages(final Context context) {
        final HashSet<String> set = new HashSet<String>();
        final HashSet<String> set2 = new HashSet<String>(ConfigConstants.DIALER_APPS);
        set2.retainAll(getAllPackages(context));
        set.addAll(set2);
        return (Set<String>)set;
    }
    
    public static Set<String> getEmailPackages(final Context context) {
        final HashSet<String> set = new HashSet<String>();
        final HashSet<String> set2 = new HashSet<String>(ConfigConstants.EMAIL_APPS);
        set2.retainAll(getAllPackages(context));
        set.addAll(set2);
        return (Set<String>)set;
    }
    
    public static Set<String> getMessengerPackages(final Context context) {
        final HashSet<String> set = new HashSet<String>();
        final HashSet<String> set2 = new HashSet<String>(ConfigConstants.MESSENGER_APPS);
        set2.retainAll(getAllPackages(context));
        set.addAll(set2);
        return (Set<String>)set;
    }
    
    @Nullable
    public static String getNotificationServiceUUID(final Context context) {
        return context.getSharedPreferences("notification_settings", 4).getString("service_uuid", (String)null);
    }
    
    public static Set<String> getOtherPackages(final Context context) {
        final HashSet<String> set = new HashSet<String>();
        for (final PackageInfo packageInfo : context.getPackageManager().getInstalledPackages(0)) {
            if ((packageInfo.applicationInfo.flags & 0x1) == 0x0) {
                set.add(packageInfo.packageName);
            }
        }
        set.removeAll(getCallPackages(context));
        set.removeAll(getANSCallPackages());
        set.removeAll(getSMSPackages(context));
        set.removeAll(getANSSMSPackages());
        set.removeAll(getMessengerPackages(context));
        set.removeAll(getEmailPackages(context));
        set.removeAll(getSocialPackages(context));
        return set;
    }
    
    public static Drawable getPackageIcon(final Context context, final String s) {
        try {
            return context.getPackageManager().getApplicationIcon(s);
        }
        catch (PackageManager.NameNotFoundException ex) {
            return null;
        }
    }
    
    public static String getPackageLabel(final Context context, final String s) {
        if ("ans_incoming_call".equals(s)) {
            return context.getString(R.string.incoming_call);
        }
        if ("ans_missed_call".equals(s)) {
            return context.getString(R.string.missed_call);
        }
        if ("ans_sms".equals(s)) {
            return context.getString(R.string.unread_sms);
        }
        try {
            return context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(s, 0)).toString();
        }
        catch (PackageManager.NameNotFoundException ex) {
            return null;
        }
    }
    
    public static Set<String> getSMSPackages(final Context context) {
        final HashSet<String> set = new HashSet<String>();
        if (Build.VERSION.SDK_INT >= 19) {
            set.add(Telephony.Sms.getDefaultSmsPackage(context));
        }
        final HashSet<String> set2 = new HashSet<String>(ConfigConstants.SMS_APPS);
        set2.retainAll(getAllPackages(context));
        set.addAll(set2);
        return (Set<String>)set;
    }
    
    public static Set<String> getSocialPackages(final Context context) {
        final HashSet<String> set = new HashSet<String>();
        final HashSet<String> set2 = new HashSet<String>(ConfigConstants.SOCIAL_APPS);
        set2.retainAll(getAllPackages(context));
        set.addAll(set2);
        return (Set<String>)set;
    }
    
    public static void setNotificationServiceUUID(final Context context, final String s) {
        context.getSharedPreferences("notification_settings", 4).edit().putString("service_uuid", s).apply();
    }
    
    public static void startNotificationListenerSettings(final Context context) {
        if (Build.VERSION.SDK_INT >= 19) {
            context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            return;
        }
        context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }
}

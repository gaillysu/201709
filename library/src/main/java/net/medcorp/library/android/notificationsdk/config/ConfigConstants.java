package net.medcorp.library.android.notificationsdk.config;

import java.util.*;

public final class ConfigConstants
{
    public static Set<String> ANS_DIALER_APPS;
    public static Set<String> ANS_SMS_APPS;
    public static final String CATEGORY_FILTER = "category_filter";
    public static final String CATEGORY_OVERRIDE = "category_override";
    public static final String CONTACT_FILTER = "contact_filter";
    public static Set<String> DIALER_APPS;
    public static Set<String> EMAIL_APPS;
    public static final String FILTER_MODE = "filter_mode";
    public static final String FILTER_SET = "filter_set";
    public static Set<String> MESSENGER_APPS;
    public static final String OVERRIDE_FALLBACK = "override_fallback";
    public static final String OVERRIDE_MODE = "override_mode";
    public static final String PACKAGE = "net.medcorp.library.android.notificationserver.config";
    public static final String PACKAGE_FILTER = "package_filter";
    public static final String PRIORITY_FILTER = "priority_filter";
    public static final String PRIORITY_OVERRIDE = "priority_override";
    public static final String SERVICE_UUID = "service_uuid";
    public static final String SETTINGS = "notification_settings";
    public static Set<String> SMS_APPS;
    public static Set<String> SOCIAL_APPS;
    
    static {
        ConfigConstants.DIALER_APPS = new HashSet<String>(Arrays.asList("com.android.dialer", "com.android.server.telecom", "com.android.providers.telephony", "com.android.incallui", "com.android.phone"));
        ConfigConstants.ANS_DIALER_APPS = new HashSet<String>(Arrays.asList("ans_incoming_call", "ans_missed_call"));
        ConfigConstants.SMS_APPS = new HashSet<String>(Arrays.asList("com.android.mms"));
        ConfigConstants.ANS_SMS_APPS = new HashSet<String>(Arrays.asList("ans_sms"));
        ConfigConstants.MESSENGER_APPS = new HashSet<String>(Arrays.asList("com.whatsapp", "com.facebook.orca", "com.tencent.mm", "jp.naver.line.android", "com.google.android.talk", "org.telegram.messenger"));
        ConfigConstants.EMAIL_APPS = new HashSet<String>(Arrays.asList("com.yahoo.mobile.client.android.mail", "com.google.android.gm", "com.google.android.apps.inbox", "com.microsoft.office.outlook"));
        ConfigConstants.SOCIAL_APPS = new HashSet<String>(Arrays.asList("com.facebook.katana", "com.google.android.apps.plus", "com.instagram.android", "com.snapchat.android", "com.linkedin.android", "com.twitter.android", "com.pinterest"));
    }
}

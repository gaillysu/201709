package net.medcorp.library.android.notification;

public final class BuildConfig
{
    public static final String APPLICATION_ID = "com.dayton.drone";
    public static final String BUILD_TYPE = "debug";
    public static final boolean DEBUG;
    public static final String FLAVOR = "";
    public static final int VERSION_CODE = 1;
    public static final String VERSION_NAME = "1.0";
    
    static {
        DEBUG = Boolean.parseBoolean("true");
    }
}

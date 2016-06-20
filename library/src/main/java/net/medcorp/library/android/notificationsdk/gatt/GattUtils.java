package net.medcorp.library.android.notificationsdk.gatt;

final class GattUtils
{
    public static byte getByte(final int n) {
        return (byte)n;
    }
    
    public static byte[] getBytes(final String s) {
        return s.getBytes();
    }
    
    public static int getInt(final byte b) {
        return b & 0xFF;
    }
    
    public static short getShort(final int n) {
        return (short)n;
    }
}

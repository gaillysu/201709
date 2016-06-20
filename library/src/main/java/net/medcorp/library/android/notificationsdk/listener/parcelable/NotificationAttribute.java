package net.medcorp.library.android.notificationsdk.listener.parcelable;

import android.os.*;

public class NotificationAttribute implements Parcelable
{
    public static final Parcelable.Creator<NotificationAttribute> CREATOR;
    private int code;
    private byte[] value;
    
    static {
        CREATOR = (Parcelable.Creator)new Parcelable.Creator<NotificationAttribute>() {
            public NotificationAttribute createFromParcel(final Parcel parcel) {
                return new NotificationAttribute(parcel);
            }
            
            public NotificationAttribute[] newArray(final int n) {
                return new NotificationAttribute[n];
            }
        };
    }
    
    public NotificationAttribute(final int code, final byte[] value) {
        this.code = code;
        this.value = value;
    }
    
    public NotificationAttribute(final Parcel parcel) {
        this.code = parcel.readInt();
        final int int1 = parcel.readInt();
        if (int1 > 0) {
            parcel.readByteArray(this.value = new byte[int1]);
            return;
        }
        this.value = null;
    }
    
    public int describeContents() {
        return 0;
    }
    
    public int getCode() {
        return this.code;
    }
    
    public byte[] getValue() {
        return this.value;
    }
    
    public void writeToParcel(final Parcel parcel, final int n) {
        parcel.writeInt(this.code);
        if (this.value != null && this.value.length > 0) {
            parcel.writeInt(this.value.length);
            parcel.writeByteArray(this.value);
            return;
        }
        parcel.writeInt(0);
    }
}

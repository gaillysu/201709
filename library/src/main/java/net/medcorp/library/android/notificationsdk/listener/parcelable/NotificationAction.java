package net.medcorp.library.android.notificationsdk.listener.parcelable;

import android.os.*;

public class NotificationAction implements Parcelable
{
    public static final Parcelable.Creator<NotificationAction> CREATOR;
    private int code;
    private String label;
    
    static {
        CREATOR = (Parcelable.Creator)new Parcelable.Creator<NotificationAction>() {
            public NotificationAction createFromParcel(final Parcel parcel) {
                return new NotificationAction(parcel);
            }
            
            public NotificationAction[] newArray(final int n) {
                return new NotificationAction[n];
            }
        };
    }
    
    public NotificationAction(final int code, final String label) {
        this.code = code;
        this.label = label;
    }
    
    public NotificationAction(final Parcel parcel) {
        this.code = parcel.readInt();
        this.label = parcel.readString();
    }
    
    public int describeContents() {
        return 0;
    }
    
    public int getCode() {
        return this.code;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public void writeToParcel(final Parcel parcel, final int n) {
        parcel.writeInt(this.code);
        parcel.writeString(this.label);
    }
}

package net.medcorp.library.android.notificationsdk.listener.parcelable;

import android.os.*;
import java.util.*;

public class NotificationAttributeList implements Parcelable
{
    public static final Parcelable.Creator<NotificationActionList> CREATOR;
    private ArrayList<NotificationAttribute> attributes;
    private int id;
    
    static {
        CREATOR = (Parcelable.Creator)new Parcelable.Creator<NotificationActionList>() {
            public NotificationActionList createFromParcel(final Parcel parcel) {
                return new NotificationActionList(parcel);
            }
            
            public NotificationActionList[] newArray(final int n) {
                return new NotificationActionList[n];
            }
        };
    }
    
    public NotificationAttributeList(final int id, final ArrayList<NotificationAttribute> attributes) {
        this.id = id;
        this.attributes = attributes;
    }
    
    public NotificationAttributeList(final Parcel parcel) {
        this.id = parcel.readInt();
        parcel.readTypedList((List)this.attributes, (Parcelable.Creator)NotificationAttribute.CREATOR);
    }
    
    public int describeContents() {
        return 0;
    }
    
    public ArrayList<NotificationAttribute> getAttributes() {
        return this.attributes;
    }
    
    public int getId() {
        return this.id;
    }
    
    public void writeToParcel(final Parcel parcel, final int n) {
        parcel.writeInt(this.id);
        parcel.writeTypedList((List)this.attributes);
    }
}

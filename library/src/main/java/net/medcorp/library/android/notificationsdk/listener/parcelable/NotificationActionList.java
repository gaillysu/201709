package net.medcorp.library.android.notificationsdk.listener.parcelable;

import android.os.*;
import java.util.*;

public class NotificationActionList implements Parcelable
{
    public static final Parcelable.Creator<NotificationActionList> CREATOR;
    private ArrayList<NotificationAction> actions;
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
    
    public NotificationActionList(final int id, final ArrayList<NotificationAction> actions) {
        this.id = id;
        this.actions = actions;
    }
    
    public NotificationActionList(final Parcel parcel) {
        this.id = parcel.readInt();
        parcel.readTypedList((List)this.actions, (Parcelable.Creator)NotificationAction.CREATOR);
    }
    
    public int describeContents() {
        return 0;
    }
    
    public ArrayList<NotificationAction> getActions() {
        return this.actions;
    }
    
    public int getId() {
        return this.id;
    }
    
    public void writeToParcel(final Parcel parcel, final int n) {
        parcel.writeInt(this.id);
        parcel.writeTypedList((List)this.actions);
    }
}

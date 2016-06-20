package net.medcorp.library.android.notificationsdk.listener.parcelable;

import android.os.*;

public class NotificationSummary implements Parcelable
{
    public static final Parcelable.Creator<NotificationSummary> CREATOR;
    private int category;
    private int id;
    private int number;
    private int priority;
    private int visibility;
    
    static {
        CREATOR = (Parcelable.Creator)new Parcelable.Creator<NotificationSummary>() {
            public NotificationSummary createFromParcel(final Parcel parcel) {
                return new NotificationSummary(parcel);
            }
            
            public NotificationSummary[] newArray(final int n) {
                return new NotificationSummary[n];
            }
        };
    }
    
    public NotificationSummary(final int id, final int category, final int number, final int priority, final int visibility) {
        this.id = id;
        this.category = category;
        this.number = number;
        this.priority = priority;
        this.visibility = visibility;
    }
    
    public NotificationSummary(final Parcel parcel) {
        this.id = parcel.readInt();
        this.category = parcel.readInt();
        this.number = parcel.readInt();
        this.priority = parcel.readInt();
        this.visibility = parcel.readInt();
    }
    
    private boolean equals(final NotificationSummary notificationSummary) {
        return this.id == notificationSummary.id && this.category == notificationSummary.category && this.number == notificationSummary.number && this.priority == notificationSummary.priority && this.visibility == notificationSummary.visibility;
    }
    
    public int describeContents() {
        return 0;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o != null && o instanceof NotificationSummary && this.equals((NotificationSummary)o);
    }
    
    public int getCategory() {
        return this.category;
    }
    
    public int getId() {
        return this.id;
    }
    
    public int getNumber() {
        return this.number;
    }
    
    public int getPriority() {
        return this.priority;
    }
    
    public int getVisibility() {
        return this.visibility;
    }
    
    public void writeToParcel(final Parcel parcel, final int n) {
        parcel.writeInt(this.id);
        parcel.writeInt(this.category);
        parcel.writeInt(this.number);
        parcel.writeInt(this.priority);
        parcel.writeInt(this.visibility);
    }
}

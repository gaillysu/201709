package net.medcorp.library.android.notificationsdk.listener.adapter;

import android.app.Notification;

import java.io.Serializable;

public class AlertAdapter implements NotificationAdapter
{
    public static final String INCOMING_CALL_PACKAGE = "ans_incoming_call";
    public static final String MISSED_CALL_PACKAGE = "ans_missed_call";
    private static final String NUMBER_URI_PREFIX = "tel:";
    public static final String SMS_PACKAGE = "ans_sms";
    private int mCategory;
    private String mNumber;
    private String mPackage;
    private int mPriority;
    private String mText;
    private String mTitle;
    private long mWhen;
    
    public AlertAdapter(final String mPackage, final String mNumber, final String mTitle, final String mText, final long mWhen, final int mCategory, final int mPriority) {
        this.mPackage = mPackage;
        this.mNumber = mNumber;
        this.mTitle = mTitle;
        this.mText = mText;
        this.mWhen = mWhen;
        this.mCategory = mCategory;
        this.mPriority = mPriority;
    }
    
    @Override
    public String getActionTitle(final Notification.Action notification$Action) {
        return null;
    }
    
    @Override
    public Notification.Action[] getActions() {
        return new Notification.Action[0];
    }
    
    @Override
    public int getCategory() {
        return this.mCategory;
    }
    
    @Override
    public String getKey() {
        final StringBuilder append = new StringBuilder().append(this.mPackage).append(this.mCategory).append(this.mNumber);
        Serializable value;
        if (!this.mPackage.equals("ans_incoming_call")) {
            value = this.mWhen;
        }
        else {
            value = "";
        }
        return append.append(value).toString();
    }
    
    @Override
    public int getNumber() {
        return 1;
    }
    
    @Override
    public String getPackageName() {
        return this.mPackage;
    }
    
    @Override
    public String[] getPeople() {
        return new String[] { "tel:" + this.mNumber };
    }
    
    @Override
    public int getPriority() {
        return this.mPriority;
    }
    
    @Override
    public String getSubtext() {
        return null;
    }
    
    @Override
    public String getText() {
        return this.mText;
    }
    
    @Override
    public String getTitle() {
        return this.mTitle;
    }
    
    @Override
    public int getVisibility() {
        return 1;
    }
    
    @Override
    public long getWhen() {
        return this.mWhen;
    }
    
    @Override
    public boolean isArtificial() {
        return true;
    }
    
    @Override
    public boolean triggerAction(final int n) {
        return false;
    }
    
    @Override
    public boolean triggerNotification() {
        return false;
    }
}

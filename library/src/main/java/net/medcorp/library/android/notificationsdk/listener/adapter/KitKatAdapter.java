package net.medcorp.library.android.notificationsdk.listener.adapter;

import android.service.notification.*;
import android.annotation.*;

public class KitKatAdapter extends JellyBeanAdapter
{
    public KitKatAdapter(final StatusBarNotification statusBarNotification) {
        super(statusBarNotification);
    }
    
    @TargetApi(19)
    @Override
    public String[] getPeople() {
        if (this.getExtras().get("android.people") != null) {
            return (String[])this.getExtras().get("android.people");
        }
        return new String[0];
    }
    
    @TargetApi(19)
    @Override
    public String getSubtext() {
        if (this.getExtras().get("android.subText") != null) {
            return this.getExtras().get("android.subText").toString();
        }
        return null;
    }
    
    @TargetApi(19)
    @Override
    public String getText() {
        if (this.getExtras().get("android.text") != null) {
            return this.getExtras().get("android.text").toString();
        }
        return null;
    }
    
    @TargetApi(19)
    @Override
    public String getTitle() {
        if (this.getExtras().get("android.title") != null) {
            return this.getExtras().get("android.title").toString();
        }
        if (this.mSbn.getNotification().tickerText != null) {
            return this.mSbn.getNotification().tickerText.toString();
        }
        return null;
    }
    
    @Override
    public boolean isArtificial() {
        return super.isArtificial();
    }
}

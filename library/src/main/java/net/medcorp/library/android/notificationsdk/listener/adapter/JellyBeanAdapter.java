package net.medcorp.library.android.notificationsdk.listener.adapter;

import android.service.notification.*;
import android.annotation.*;
import android.os.*;
import android.app.*;

public class JellyBeanAdapter implements NotificationAdapter
{
    private static final String KEY_SEPARATOR = "-";
    protected StatusBarNotification mSbn;
    
    public JellyBeanAdapter(final StatusBarNotification mSbn) {
        this.mSbn = mSbn;
    }
    
    public static String[] breakKey(final String s) {
        return s.split("-");
    }
    
    @TargetApi(18)
    @Override
    public String getActionTitle(final Notification.Action notification$Action) {
        try {
            return notification$Action.getClass().getDeclaredField("title").get(notification$Action).toString();
        }
        catch (NoSuchFieldException ex) {
            return null;
        }
        catch (IllegalAccessException ex2) {
            return null;
        }
    }
    
    @TargetApi(18)
    @Override
    public Notification.Action[] getActions() {
        try {
            return (Notification.Action[])this.mSbn.getNotification().getClass().getDeclaredField("actions").get(this.mSbn.getNotification());
        }
        catch (NoSuchFieldException ex) {
            return new Notification.Action[0];
        }
        catch (IllegalAccessException ex2) {
            return new Notification.Action[0];
        }
    }
    
    @TargetApi(18)
    @Override
    public int getCategory() {
        return 255;
    }
    
    @TargetApi(18)
    protected Bundle getExtras() {
        try {
            return (Bundle)this.mSbn.getNotification().getClass().getDeclaredField("extras").get(this.mSbn.getNotification());
        }
        catch (NoSuchFieldException ex) {
            return new Bundle();
        }
        catch (IllegalAccessException ex2) {
            return new Bundle();
        }
    }
    
    @TargetApi(18)
    @Override
    public String getKey() {
        return this.mSbn.getPackageName() + "-" + this.mSbn.getTag() + "-" + String.valueOf(this.mSbn.getId());
    }
    
    @TargetApi(18)
    @Override
    public int getNumber() {
        return Math.max(this.mSbn.getNotification().number, 1);
    }
    
    @TargetApi(18)
    @Override
    public String getPackageName() {
        return this.mSbn.getPackageName();
    }
    
    @TargetApi(18)
    @Override
    public String[] getPeople() {
        if (this.getExtras().get("android.people") != null) {
            return (String[])this.getExtras().get("android.people");
        }
        return new String[0];
    }
    
    @TargetApi(18)
    @Override
    public int getPriority() {
        switch (this.mSbn.getNotification().priority) {
            default: {
                return 255;
            }
            case -2: {
                return 1;
            }
            case -1: {
                return 2;
            }
            case 0: {
                return 3;
            }
            case 1: {
                return 4;
            }
            case 2: {
                return 5;
            }
        }
    }
    
    @TargetApi(18)
    @Override
    public String getSubtext() {
        if (this.getExtras().get("android.subText") != null) {
            return this.getExtras().get("android.subText").toString();
        }
        return null;
    }
    
    @TargetApi(18)
    @Override
    public String getText() {
        if (this.getExtras().get("android.text") != null) {
            return this.getExtras().get("android.text").toString();
        }
        return null;
    }
    
    @TargetApi(18)
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
    
    @TargetApi(18)
    @Override
    public int getVisibility() {
        return 255;
    }
    
    @TargetApi(18)
    @Override
    public long getWhen() {
        return this.mSbn.getNotification().when;
    }
    
    @Override
    public boolean isArtificial() {
        return false;
    }
    
    @TargetApi(18)
    @Override
    public boolean triggerAction(final int n) {
        if (this.getActions().length >= n) {
            try {
                ((PendingIntent)this.getActions()[n].getClass().getDeclaredField("actionIntent").get(this.getActions()[n])).send();
                return true;
            }
            catch (PendingIntent.CanceledException ex) {
                return false;
            }
            catch (NoSuchFieldException ex2) {
                return false;
            }
            catch (IllegalAccessException ex3) {
                return false;
            }
        }
        return false;
    }
    
    @TargetApi(18)
    @Override
    public boolean triggerNotification() {
        boolean b = false;
        if (this.mSbn.getNotification().contentIntent == null) {
            return b;
        }
        try {
            this.mSbn.getNotification().contentIntent.send();
            b = true;
            return b;
        }
        catch (PendingIntent.CanceledException ex) {
            return false;
        }
    }
}

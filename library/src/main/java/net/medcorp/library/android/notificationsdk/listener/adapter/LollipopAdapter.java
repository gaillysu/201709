package net.medcorp.library.android.notificationsdk.listener.adapter;

import android.service.notification.*;
import android.annotation.*;

import net.medcorp.library.android.notificationsdk.config.ConfigConstants;

public class LollipopAdapter extends KitKatAdapter
{
    public LollipopAdapter(final StatusBarNotification statusBarNotification) {
        super(statusBarNotification);
    }
    
    @TargetApi(21)
    @Override
    public int getCategory() {
        int n = 1;
        if (this.mSbn.getNotification().category == null)
        {
            n = 255;
        }
        else {
            final String category = this.mSbn.getNotification().category;
            switch (category) {
                case "alarm": {
                    break;
                }
                default: {
                    return 255;
                }
                case "call": {
                    return 2;
                }
                case "email": {
                    return 3;
                }
                case "err": {
                    return 4;
                }
                case "event": {
                    return 5;
                }
                case "msg":
                {
                    return 242;//6 is the official define(pls refer to Notification.java), here 242 is defined by MED, 0xF2, it means unread message.
                }
                case "progress": {
                    return 7;
                }
                case "promo": {
                    return 8;
                }
                case "recommendation": {
                    return 9;
                }
                case "service": {
                    return 10;
                }
                case "social": {
                    return 11;
                }
                case "status": {
                    return 12;
                }
                case "sys": {
                    return 13;
                }
                case "transport": {
                    return 14;
                }
            }
        }
        //TODO here make a patch that on xiaomi phone, the mSbn.getNotification().category is null, we continue it with package name
        //we should list all the possible packages of more apps,pls refer to ConfigConstants.java
        if(ConfigConstants.THIRD_PARTY_MMS_APPS.contains(mSbn.getPackageName()))
        {
            return 242;
        }
        else if(ConfigConstants.THIRD_PARTY_EMAIL_APPS.contains(mSbn.getPackageName()))
        {
            return 3;
        }
        else if(ConfigConstants.THIRD_PARTY_IM_APPS.contains(mSbn.getPackageName()))
        {
            return 11;
        }

        return n;
    }
    
    @TargetApi(21)
    @Override
    public String getKey() {
        return this.mSbn.getKey();
    }
    
    @TargetApi(21)
    @Override
    public String getTitle() {
        if (this.getExtras().get("android.title") != null) {
            return this.getExtras().get("android.title").toString();
        }
        return null;
    }
    
    @TargetApi(21)
    @Override
    public int getVisibility() {
        switch (this.mSbn.getNotification().visibility) {
            default: {
                return 255;
            }
            case 1: {
                return 1;
            }
            case 0: {
                return 2;
            }
            case -1: {
                return 3;
            }
        }
    }
    
    @Override
    public boolean isArtificial() {
        return super.isArtificial();
    }
}

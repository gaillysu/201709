package net.medcorp.library.android.notificationsdk.listener.adapter;

import android.app.*;

public interface NotificationAdapter
{
    String getActionTitle(final Notification.Action p0);

    Notification.Action[] getActions();
    
    int getCategory();
    
    String getKey();
    
    int getNumber();
    
    String getPackageName();
    
    String[] getPeople();
    
    int getPriority();
    
    String getSubtext();
    
    String getText();
    
    String getTitle();
    
    int getVisibility();
    
    long getWhen();
    
    boolean isArtificial();
    
    boolean triggerAction(final int p0);
    
    boolean triggerNotification();
}

package net.medcorp.library.android.notificationsdk.listener.adapter;

import android.app.Notification;

import net.medcorp.library.android.notificationsdk.config.ConfigMonitor;
import net.medcorp.library.android.notificationsdk.config.type.OverrideType;

public class OverrideAdapter implements NotificationAdapter
{
    private ConfigMonitor mConfigMonitor;
    private NotificationAdapter mNotificationAdapter;
    
    public OverrideAdapter(final NotificationAdapter mNotificationAdapter, final ConfigMonitor mConfigMonitor) {
        this.mNotificationAdapter = mNotificationAdapter;
        this.mConfigMonitor = mConfigMonitor;
    }
    
    @Override
    public String getActionTitle(final Notification.Action notification$Action) {
        return this.mNotificationAdapter.getActionTitle(notification$Action);
    }
    
    @Override
    public Notification.Action[] getActions() {
        return this.mNotificationAdapter.getActions();
    }
    
    @Override
    public int getCategory() {
        try {
            return Integer.valueOf(this.mConfigMonitor.getOverride(OverrideType.CATEGORY, this.mNotificationAdapter.getPackageName(), String.valueOf(this.mNotificationAdapter.getCategory()), String.valueOf(255)));
        }
        catch (NumberFormatException ex) {
            return this.mNotificationAdapter.getCategory();
        }
    }
    
    @Override
    public String getKey() {
        return this.mNotificationAdapter.getKey();
    }
    
    @Override
    public int getNumber() {
        return this.mNotificationAdapter.getNumber();
    }
    
    @Override
    public String getPackageName() {
        return this.mNotificationAdapter.getPackageName();
    }
    
    @Override
    public String[] getPeople() {
        return this.mNotificationAdapter.getPeople();
    }
    
    @Override
    public int getPriority() {
        try {
            return Integer.valueOf(this.mConfigMonitor.getOverride(OverrideType.PRIORITY, this.mNotificationAdapter.getPackageName(), String.valueOf(this.mNotificationAdapter.getPriority()), String.valueOf(255)));
        }
        catch (NumberFormatException ex) {
            return this.mNotificationAdapter.getPriority();
        }
    }
    
    @Override
    public String getSubtext() {
        return this.mNotificationAdapter.getSubtext();
    }
    
    @Override
    public String getText() {
        return this.mNotificationAdapter.getText();
    }
    
    @Override
    public String getTitle() {
        return this.mNotificationAdapter.getTitle();
    }
    
    @Override
    public int getVisibility() {
        return this.mNotificationAdapter.getVisibility();
    }
    
    @Override
    public long getWhen() {
        return this.mNotificationAdapter.getWhen();
    }
    
    @Override
    public boolean isArtificial() {
        return this.mNotificationAdapter.isArtificial();
    }
    
    @Override
    public boolean triggerAction(final int n) {
        return this.mNotificationAdapter.triggerAction(n);
    }
    
    @Override
    public boolean triggerNotification() {
        return this.mNotificationAdapter.triggerNotification();
    }
}

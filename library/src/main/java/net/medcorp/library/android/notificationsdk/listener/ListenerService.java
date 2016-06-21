package net.medcorp.library.android.notificationsdk.listener;

import android.annotation.TargetApi;
import android.app.Notification;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;

import net.medcorp.library.android.notificationsdk.config.ConfigHelper;
import net.medcorp.library.android.notificationsdk.config.ConfigMonitor;
import net.medcorp.library.android.notificationsdk.config.type.FilterType;
import net.medcorp.library.android.notificationsdk.gatt.GattServer;
import net.medcorp.library.android.notificationsdk.listener.adapter.AlertAdapter;
import net.medcorp.library.android.notificationsdk.listener.adapter.JellyBeanAdapter;
import net.medcorp.library.android.notificationsdk.listener.adapter.KitKatAdapter;
import net.medcorp.library.android.notificationsdk.listener.adapter.LollipopAdapter;
import net.medcorp.library.android.notificationsdk.listener.adapter.NotificationAdapter;
import net.medcorp.library.android.notificationsdk.listener.adapter.OverrideAdapter;
import net.medcorp.library.android.notificationsdk.listener.parcelable.NotificationAction;
import net.medcorp.library.android.notificationsdk.listener.parcelable.NotificationActionList;
import net.medcorp.library.android.notificationsdk.listener.parcelable.NotificationAttribute;
import net.medcorp.library.android.notificationsdk.listener.parcelable.NotificationAttributeList;
import net.medcorp.library.android.notificationsdk.listener.parcelable.NotificationSummary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ListenerService extends NotificationListenerService
{
    private static final String TAG;
    private static boolean mRunning;
    private Map<String, NotificationAdapter> mArtificialMap;
    private CallReceiver mCallReceiver;
    private ConfigListener mConfigListener;
    private ConfigMonitor mConfigMonitor;
    private SparseArray<String> mKeyMap;
    private NotificationReceiver mNotificationReceiver;
    private Map<String, NotificationSummary> mSummaryMap;
    
    static {
        TAG = "Karl";
    }
    
    public ListenerService() {
        Log.w(TAG,"Listener service!!?");
        this.mKeyMap = (SparseArray<String>)new SparseArray();
        this.mSummaryMap = new HashMap<String, NotificationSummary>();
        this.mArtificialMap = new HashMap<String, NotificationAdapter>();
    }
    
    private void cancelArtificialNotification(final NotificationAdapter notificationAdapter) {
        this.onNotificationRemoved(notificationAdapter);
    }
    
    private void cancelNotification(final NotificationAdapter notificationAdapter) {
        if (notificationAdapter.isArtificial()) {
            this.cancelArtificialNotification(notificationAdapter);
            return;
        }
        final String key = notificationAdapter.getKey();
        if (Build.VERSION.SDK_INT >= 21) {
            this.cancelNotificationLP(key);
            return;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            this.cancelNotificationKK(key);
            return;
        }
        this.cancelNotificationJB(key);
    }
    
    @TargetApi(18)
    private void cancelNotificationJB(final String s) {
        final String[] breakKey = JellyBeanAdapter.breakKey(s);
        this.cancelNotification(breakKey[0], breakKey[1], (int)Integer.valueOf(breakKey[2]));
    }
    
    @TargetApi(19)
    private void cancelNotificationKK(final String s) {
        this.cancelNotificationJB(s);
    }
    
    @TargetApi(21)
    private void cancelNotificationLP(final String s) {
        this.cancelNotification(s);
    }
    
    private String formatNumber(final String s) {
        String s2;
        if (Build.VERSION.SDK_INT >= 21) {
            s2 = this.formatNumberPostLP(s);
        }
        else {
            s2 = this.formatNumberPreLP(s);
        }
        if (s2 != null) {
            return s2;
        }
        return s;
    }
    
    @TargetApi(21)
    private String formatNumberPostLP(final String s) {
        return PhoneNumberUtils.formatNumber(s, Locale.getDefault().getCountry());
    }
    
    private String formatNumberPreLP(final String s) {
        try {
            return PhoneNumberUtils.formatNumber(s);
        }
        catch (Exception ex) {
            return s;
        }
    }
    
    private NotificationAdapter[] getAllNotifications() {
        final ArrayList<NotificationAdapter> list = new ArrayList<NotificationAdapter>();
        final StatusBarNotification[] activeNotifications = this.getActiveNotifications();
        for (int length = activeNotifications.length, i = 0; i < length; ++i) {
            final StatusBarNotification statusBarNotification = activeNotifications[i];
            final NotificationAdapter adapter = this.getAdapter(statusBarNotification);
            if (this.matchesServiceFilter(statusBarNotification)) {
                list.add(adapter);
            }
        }
        final Iterator<NotificationAdapter> iterator = this.getStoredArtificialNotifications().values().iterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list.toArray(new NotificationAdapter[list.size()]);
    }
    
    private String getDisplayText(final Context context, final String s) {
        final Cursor query = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(s)), new String[] { "_id", "display_name" }, (String)null, (String[])null, (String)null);
        if (query != null) {
            String s2;
            if (query.moveToNext()) {
                s2 = query.getString(query.getColumnIndex("display_name"));
            }
            else {
                s2 = this.formatNumber(s);
            }
            query.close();
            return s2;
        }
        return s;
    }
    
    private NotificationActionList getNotificationActionList(final NotificationAdapter notificationAdapter) {
        final ArrayList<NotificationAction> list = new ArrayList<NotificationAction>();
        final Notification.Action[] actions = notificationAdapter.getActions();
        if (actions != null) {
            for (int i = 0; i < actions.length; ++i) {
                list.add(new NotificationAction(i, notificationAdapter.getActionTitle(actions[i])));
            }
        }
        return new NotificationActionList(notificationAdapter.getKey().hashCode(), list);
    }
    
    private NotificationAttributeList getNotificationAttributeList(final NotificationAdapter notificationAdapter, final int[] array) {
        final ArrayList<NotificationAttribute> list = new ArrayList<NotificationAttribute>();
        for (int length = array.length, i = 0; i < length; ++i) {
            final int n = array[i];
            byte[] array2 = null;
            switch (n) {
                default: {
                    array2 = null;
                    break;
                }
                case 1: {
                    array2 = new byte[] { (byte)notificationAdapter.getCategory() };
                    break;
                }
                case 2: {
                    if (notificationAdapter.getPackageName() != null) {
                        array2 = notificationAdapter.getPackageName().getBytes();
                    }
                    else {
                        array2 = null;
                    }
                    break;
                }
                case 3: {
                    array2 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(notificationAdapter.getNumber()).array();
                    break;
                }
                case 4: {
                    array2 = new byte[] { (byte)notificationAdapter.getPriority() };
                    break;
                }
                case 5: {
                    array2 = new byte[] { (byte)notificationAdapter.getVisibility() };
                    break;
                }
                case 6: {
                    if (notificationAdapter.getTitle() != null) {
                        array2 = notificationAdapter.getTitle().getBytes();
                    }
                    else {
                        array2 = null;
                    }
                    break;
                }
                case 7: {
                    if (notificationAdapter.getSubtext() != null) {
                        array2 = notificationAdapter.getSubtext().getBytes();
                    }
                    else {
                        array2 = null;
                    }
                    break;
                }
                case 8: {
                    if (notificationAdapter.getText() != null) {
                        array2 = notificationAdapter.getText().getBytes();
                    }
                    else {
                        array2 = null;
                    }
                    break;
                }
                case 9: {
                    array2 = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(notificationAdapter.getWhen()).array();
                    break;
                }
                case 10: {
                    final String packageLabel = ConfigHelper.getPackageLabel((Context)this, notificationAdapter.getPackageName());
                    if (packageLabel != null) {
                        array2 = packageLabel.getBytes();
                    }
                    else {
                        array2 = null;
                    }
                    break;
                }
            }
            list.add(new NotificationAttribute(n, array2));
        }
        return new NotificationAttributeList(notificationAdapter.getKey().hashCode(), list);
    }
    
    private NotificationSummary getNotificationSummary(final NotificationAdapter notificationAdapter) {
        return new NotificationSummary(notificationAdapter.getKey().hashCode(), notificationAdapter.getCategory(), notificationAdapter.getNumber(), notificationAdapter.getPriority(), notificationAdapter.getVisibility());
    }
    
    private Map<String, NotificationAdapter> getStoredArtificialNotifications() {
        return this.mArtificialMap;
    }
    
    private String getStoredKey(final int n) {
        return (String)this.mKeyMap.get(n);
    }
    
    private Map<String, NotificationSummary> getStoredSummaries() {
        return this.mSummaryMap;
    }
    
    private NotificationSummary getStoredSummary(final String s) {
        return this.mSummaryMap.get(s);
    }
    
    private boolean isKeyStored(final int n) {
        return this.mKeyMap.get(n) != null;
    }
    
    private boolean isNotificationStored(final NotificationAdapter notificationAdapter) {
        return this.isKeyStored(notificationAdapter.getKey().hashCode()) && this.isSummaryStored(notificationAdapter.getKey());
    }
    
    public static boolean isRunning() {
        return ListenerService.mRunning;
    }
    
    private boolean isSummaryStored(final String s) {
        return this.mSummaryMap.containsKey(s);
    }
    
    private boolean matchesCategoryFilter(final NotificationAdapter notificationAdapter) {
        return this.mConfigMonitor.matchesFilter(FilterType.CATEGORY, String.valueOf(notificationAdapter.getCategory()));
    }
    
    private boolean matchesContactFilter(final NotificationAdapter notificationAdapter, final Set<String> set) {
        if (!set.contains(notificationAdapter.getPackageName())) {
            return true;
        }
        final String[] people = notificationAdapter.getPeople();
        if (people.length > 0) {
            for (int length = people.length, i = 0; i < length; ++i) {
                final String s = people[i];
                if (s.startsWith("tel")) {
                    if (!this.matchesNumberContactFilter(s)) {
                        return false;
                    }
                }
                else if (!this.matchesPersonContactFilter(s)) {
                    return false;
                }
            }
            return true;
        }
        return this.mConfigMonitor.matchesFilterIfNoAvailableInfo(FilterType.CONTACT);
    }
    
    private boolean matchesFilter(final NotificationAdapter notificationAdapter) {
        final HashSet<String> set = new HashSet<String>();
        set.addAll(ConfigHelper.getCallPackages((Context)this));
        set.addAll(ConfigHelper.getANSCallPackages());
        set.addAll(ConfigHelper.getSMSPackages((Context)this));
        set.addAll(ConfigHelper.getANSSMSPackages());
        return this.matchesFilter(notificationAdapter, (Set<String>)set);
    }
    
    private boolean matchesFilter(final NotificationAdapter notificationAdapter, final Set<String> set) {
        return this.matchesPackageFilter(notificationAdapter) && this.matchesCategoryFilter(notificationAdapter) && this.matchesPriorityFilter(notificationAdapter) && this.matchesContactFilter(notificationAdapter, set);
    }
    
    private boolean matchesNumberContactFilter(final String s) {
        final Cursor query = this.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(s)), new String[] { "_id" }, (String)null, (String[])null, (String)null);
        if (query != null) {
            final ArrayList<String> list = new ArrayList<String>();
            while (query.moveToNext()) {
                list.add(String.valueOf(query.getInt(query.getColumnIndex("_id"))));
            }
            query.close();
            final Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                if (!this.mConfigMonitor.matchesFilter(FilterType.CONTACT, iterator.next())) {
                    return false;
                }
            }
            return true;
        }
        return this.mConfigMonitor.matchesFilterIfNoAvailableInfo(FilterType.CONTACT);
    }
    
    private boolean matchesPackageFilter(final NotificationAdapter notificationAdapter) {
        return this.mConfigMonitor.matchesFilter(FilterType.PACKAGE, notificationAdapter.getPackageName());
    }
    
    private boolean matchesPersonContactFilter(final String s) {
        final Cursor query = this.getContentResolver().query(Uri.parse(s), new String[] { "_id" }, (String)null, (String[])null, (String)null);
        if (query != null) {
            final ArrayList<String> list = new ArrayList<String>();
            while (query.moveToNext()) {
                list.add(String.valueOf(query.getInt(query.getColumnIndex("_id"))));
            }
            query.close();
            final Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                if (!this.mConfigMonitor.matchesFilter(FilterType.CONTACT, iterator.next())) {
                    return false;
                }
            }
            return true;
        }
        return this.mConfigMonitor.matchesFilterIfNoAvailableInfo(FilterType.CONTACT);
    }
    
    private boolean matchesPriorityFilter(final NotificationAdapter notificationAdapter) {
        return this.mConfigMonitor.matchesFilter(FilterType.PRIORITY, String.valueOf(notificationAdapter.getPriority()));
    }
    
    private boolean matchesServiceFilter(final StatusBarNotification statusBarNotification) {
        return (statusBarNotification.getNotification().flags & 0x162) == 0x0;
    }
    
    private boolean matchesSummaryFilter(final StatusBarNotification statusBarNotification) {
        int i = 0;
        if (Build.VERSION.SDK_INT >= 21) {
            if ((statusBarNotification.getNotification().flags & 0x200) > 0) {
                final StatusBarNotification[] activeNotifications = this.getActiveNotifications();
                for (int length = activeNotifications.length, j = 0; j < length; ++j) {
                    final StatusBarNotification statusBarNotification2 = activeNotifications[j];
                    if (this.isNotificationStored(this.getAdapter(statusBarNotification2)) && !this.getAdapter(statusBarNotification2).getKey().equals(this.getAdapter(statusBarNotification).getKey()) && statusBarNotification2.getGroupKey().equals(statusBarNotification.getGroupKey()) && (statusBarNotification2.getNotification().flags & 0x200) == 0x0) {
                        return false;
                    }
                }
            }
            else {
                for (StatusBarNotification[] activeNotifications2 = this.getActiveNotifications(); i < activeNotifications2.length; ++i) {
                    final StatusBarNotification statusBarNotification3 = activeNotifications2[i];
                    if (this.isNotificationStored(this.getAdapter(statusBarNotification3)) && !this.getAdapter(statusBarNotification3).getKey().equals(this.getAdapter(statusBarNotification).getKey()) && statusBarNotification3.getGroupKey().equals(statusBarNotification.getGroupKey()) && (statusBarNotification3.getNotification().flags & 0x200) > 0) {
                        this.onNotificationRemoved(statusBarNotification3);
                    }
                }
            }
        }
        return true;
    }
    
    private void removeNotification(final NotificationAdapter notificationAdapter) {
        this.mKeyMap.remove(notificationAdapter.getKey().hashCode());
        this.mSummaryMap.remove(notificationAdapter.getKey());
        if (notificationAdapter.isArtificial()) {
            this.mArtificialMap.remove(notificationAdapter.getKey());
        }
    }
    
    private void storeNotification(final NotificationAdapter notificationAdapter) {
        this.mKeyMap.put(notificationAdapter.getKey().hashCode(), notificationAdapter.getKey());
        this.mSummaryMap.put(notificationAdapter.getKey(), this.getNotificationSummary(notificationAdapter));
        if (notificationAdapter.isArtificial()) {
            this.mArtificialMap.put(notificationAdapter.getKey(), notificationAdapter);
        }
    }
    
    private String toString(final Context context, final NotificationAdapter notificationAdapter) {
        final StringBuilder sb = new StringBuilder();
        final String property = System.getProperty("line.separator");
        sb.append("ID: ").append(notificationAdapter.getKey().hashCode()).append(property).append("Category: ").append(notificationAdapter.getCategory()).append(property).append("Application Package: ").append(notificationAdapter.getPackageName()).append(property).append("Number: ").append(notificationAdapter.getNumber()).append(property).append("Priority: ").append(notificationAdapter.getPriority()).append(property).append("Visibility: ").append(notificationAdapter.getVisibility()).append(property).append("Title: ").append(notificationAdapter.getTitle()).append(property).append("Subtext: ").append(notificationAdapter.getSubtext()).append(property).append("Text: ").append(notificationAdapter.getText()).append(property).append("When: ").append(notificationAdapter.getWhen()).append(property).append("Application Label: ").append(ConfigHelper.getPackageLabel(context, notificationAdapter.getPackageName()));
        return sb.toString();
    }
    
    private String toString(final Context context, final NotificationAdapter[] array) {
        final StringBuilder sb = new StringBuilder();
        final String property = System.getProperty("line.separator");
        for (int length = array.length, i = 0; i < length; ++i) {
            sb.append(this.toString(context, array[i])).append(property).append(property);
        }
        return sb.toString();
    }
    
    public NotificationAdapter getAdapter(final StatusBarNotification statusBarNotification) {
        JellyBeanAdapter jellyBeanAdapter;
        if (Build.VERSION.SDK_INT >= 21) {
            jellyBeanAdapter = new LollipopAdapter(statusBarNotification);
        }
        else if (Build.VERSION.SDK_INT >= 19) {
            jellyBeanAdapter = new KitKatAdapter(statusBarNotification);
        }
        else {
            jellyBeanAdapter = new JellyBeanAdapter(statusBarNotification);
        }
        return new OverrideAdapter(jellyBeanAdapter, this.mConfigMonitor);
    }
    
    public NotificationAdapter getAdapter(final String s) {
        final StatusBarNotification[] activeNotifications = this.getActiveNotifications();
        for (int length = activeNotifications.length, i = 0; i < length; ++i) {
            final NotificationAdapter adapter = this.getAdapter(activeNotifications[i]);
            if (adapter.getKey().equals(s)) {
                return adapter;
            }
        }
        for (final NotificationAdapter notificationAdapter : this.getStoredArtificialNotifications().values()) {
            if (notificationAdapter.getKey().equals(s)) {
                return notificationAdapter;
            }
        }
        return null;
    }
    
    public void onCreate() {
        super.onCreate();
        Log.d(ListenerService.TAG, "Listener created");
        GattServer.initialize((Context)this);
        this.mNotificationReceiver = new NotificationReceiver();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("net.medcorp.library.android.notificationserver.listener.ACTION_LIST");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.listener.ACTION_READ_ATTRIBUTES");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.listener.ACTION_READ_ACTIONS");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.listener.ACTION_TRIGGER_DISMISS");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.listener.ACTION_TRIGGER_OPEN");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.listener.ACTION_TRIGGER_CUSTOM");
        LocalBroadcastManager.getInstance((Context)this).registerReceiver(this.mNotificationReceiver, intentFilter);
        this.mConfigMonitor = new ConfigMonitor((Context)this);
        this.mConfigListener = new ConfigListener();
        this.mConfigMonitor.registerListener((ConfigMonitor.ConfigMonitorListener)this.mConfigListener);
        this.registerReceiver((BroadcastReceiver)(this.mCallReceiver = new CallReceiver((Context)this)), new IntentFilter("android.intent.action.PHONE_STATE"));
        ListenerService.mRunning = true;
    }
    
    public void onDestroy() {
        super.onDestroy();
        Log.d(ListenerService.TAG, "Listener destroyed");
        ListenerService.mRunning = false;
        this.unregisterReceiver((BroadcastReceiver)this.mCallReceiver);
        this.mCallReceiver = null;
        this.mConfigMonitor.unregisterListener((ConfigMonitor.ConfigMonitorListener)this.mConfigListener);
        this.mConfigListener = null;
        this.mConfigMonitor = null;
        LocalBroadcastManager.getInstance((Context)this).unregisterReceiver(this.mNotificationReceiver);
        this.mNotificationReceiver = null;
        GattServer.terminate();
        this.mSummaryMap.clear();
        this.mKeyMap.clear();
    }
    
    public void onListenerConnected() {
        Log.d(ListenerService.TAG, "Listener connected");
        final StatusBarNotification[] activeNotifications = this.getActiveNotifications();
        for (int length = activeNotifications.length, i = 0; i < length; ++i) {
            this.onNotificationPosted(activeNotifications[i]);
        }
    }
    
    public void onNotificationPosted(final StatusBarNotification statusBarNotification) {
        if (this.matchesServiceFilter(statusBarNotification) && this.matchesSummaryFilter(statusBarNotification)) {
            this.onNotificationPosted(this.getAdapter(statusBarNotification));
        }
    }
    
    public void onNotificationPosted(final NotificationAdapter notificationAdapter) {
        Log.d(ListenerService.TAG, "New event (notification posted)");
        if (this.matchesFilter(notificationAdapter)) {
            String s;
            if (this.isNotificationStored(notificationAdapter)) {
                s = "net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_UPDATED";
            }
            else {
                s = "net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_POSTED";
            }
            this.storeNotification(notificationAdapter);
            LocalBroadcastManager.getInstance((Context)this).sendBroadcast(new Intent(s).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_SUMMARY", (Parcelable)this.getNotificationSummary(notificationAdapter)));
            return;
        }
        Log.d(ListenerService.TAG, "Event has been ignored (filtered)");
    }
    
    public void onNotificationRemoved(final StatusBarNotification statusBarNotification) {
        if (this.matchesServiceFilter(statusBarNotification)) {
            this.onNotificationRemoved(this.getAdapter(statusBarNotification));
        }
    }
    
    public void onNotificationRemoved(final NotificationAdapter notificationAdapter) {
        Log.d(ListenerService.TAG, "New event (notification removed)");
        if (!this.matchesFilter(notificationAdapter)) {
            Log.d(ListenerService.TAG, "Event has been ignored (filtered)");
            return;
        }
        if (this.isNotificationStored(notificationAdapter)) {
            this.removeNotification(notificationAdapter);
            LocalBroadcastManager.getInstance((Context)this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_REMOVED").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_SUMMARY", (Parcelable)this.getNotificationSummary(notificationAdapter)));
            return;
        }
        Log.w(ListenerService.TAG, "Removed notification was not stored");
    }
    
    public class CallReceiver extends BroadcastReceiver
    {
        private final String TAG;
        private String mNumber;
        private String mState;
        
        public CallReceiver(final Context context) {
            this.TAG = CallReceiver.class.getSimpleName();
            final TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService("phone");
            this.mNumber = null;
            switch (telephonyManager.getCallState()) {
                default: {}
                case 0: {
                    this.mState = TelephonyManager.EXTRA_STATE_IDLE;
                }
                case 1: {
                    this.mState = TelephonyManager.EXTRA_STATE_RINGING;
                }
                case 2: {
                    this.mState = TelephonyManager.EXTRA_STATE_OFFHOOK;
                }
            }
        }
        
        private NotificationAdapter getIncomingCallNotification(final String s, final String s2) {
            return new OverrideAdapter(new AlertAdapter("ans_incoming_call", s, s2, null, System.currentTimeMillis(), 240, 5), ListenerService.this.mConfigMonitor);
        }
        
        private NotificationAdapter getMissedCallNotification(final String s, final String s2) {
            return new OverrideAdapter(new AlertAdapter("ans_missed_call", s, s2, null, System.currentTimeMillis(), 241, 3), ListenerService.this.mConfigMonitor);
        }
        
        private void onReceiveIncomingCall(final Context context, final String s) {
            Log.d(this.TAG, "Received incoming call");
            if (s != null) {
                ListenerService.this.onNotificationPosted(this.getIncomingCallNotification(s, ListenerService.this.getDisplayText(context, s)));
                return;
            }
            Log.w(this.TAG, "Number is null");
        }
        
        private void onReceiveMissedCall(final Context context, final String s) {
            Log.d(this.TAG, "Received missed call");
            if (s != null) {
                final String access$1500 = ListenerService.this.getDisplayText(context, s);
                ListenerService.this.onNotificationRemoved(this.getIncomingCallNotification(s, access$1500));
                ListenerService.this.onNotificationPosted(this.getMissedCallNotification(s, access$1500));
                return;
            }
            Log.w(this.TAG, "Number is null");
        }
        
        private void onReceiveOngoingCall(final Context context, final String s) {
            Log.d(this.TAG, "Received ongoing call");
            if (s != null) {
                ListenerService.this.onNotificationRemoved(this.getIncomingCallNotification(s, ListenerService.this.getDisplayText(context, s)));
                return;
            }
            Log.w(this.TAG, "Number is null");
        }
        
        public void onReceive(final Context context, final Intent intent) {
            if (!"android.intent.action.PHONE_STATE".equals(intent.getAction())) {
                Log.w(this.TAG, "Unknown broadcast received");
                return;
            }
            Log.d(this.TAG, "Received a phone alert");
            final Bundle extras = intent.getExtras();
            if (extras == null || extras.get("state") == null) {
                Log.w(this.TAG, "Broadcast is missing extras");
                return;
            }
            final String string = extras.get("state").toString();
            String string2;
            if (extras.get("incoming_number") != null) {
                string2 = extras.get("incoming_number").toString();
            }
            else {
                string2 = null;
            }
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(string)) {
                if (TelephonyManager.EXTRA_STATE_RINGING.equals(this.mState)) {
                    this.onReceiveMissedCall(context, this.mNumber);
                }
                this.mNumber = string2;
                this.mState = TelephonyManager.EXTRA_STATE_IDLE;
                return;
            }
            if (TelephonyManager.EXTRA_STATE_RINGING.equals(string)) {
                this.mNumber = string2;
                if (TelephonyManager.EXTRA_STATE_IDLE.equals(this.mState)) {
                    this.onReceiveIncomingCall(context, this.mNumber);
                }
                this.mState = TelephonyManager.EXTRA_STATE_RINGING;
                return;
            }
            if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(string)) {
                if (TelephonyManager.EXTRA_STATE_RINGING.equals(this.mState)) {
                    this.onReceiveOngoingCall(context, this.mNumber);
                }
                this.mNumber = string2;
                this.mState = TelephonyManager.EXTRA_STATE_OFFHOOK;
                return;
            }
            Log.w(this.TAG, "Unknown phone state received");
        }
    }
    
    private class ConfigListener implements ConfigMonitor.ConfigMonitorListener
    {
        private final String TAG;
        
        private ConfigListener() {
            this.TAG = ConfigListener.class.getSimpleName();
        }
        
        @Override
        public void onConfigChanged() {
            Log.d(this.TAG, "Config settings have been changed, cascading changes");
            final HashSet set = new HashSet();
            set.addAll(ConfigHelper.getCallPackages((Context)ListenerService.this));
            set.addAll(ConfigHelper.getANSCallPackages());
            set.addAll(ConfigHelper.getSMSPackages((Context)ListenerService.this));
            set.addAll(ConfigHelper.getANSSMSPackages());
            final NotificationAdapter[] access$200 = ListenerService.this.getAllNotifications();
            for (int length = access$200.length, i = 0; i < length; ++i) {
                final NotificationAdapter notificationAdapter = access$200[i];
                final boolean access$201 = ListenerService.this.isNotificationStored(notificationAdapter);
                final boolean access$202 = ListenerService.this.matchesFilter(notificationAdapter, set);
                if (access$201 && !access$202) {
                    ListenerService.this.removeNotification(notificationAdapter);
                    LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_REMOVED").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_SUMMARY", (Parcelable)ListenerService.this.getNotificationSummary(notificationAdapter)));
                }
                else if (!access$201 && access$202) {
                    ListenerService.this.storeNotification(notificationAdapter);
                    LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_POSTED").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_SUMMARY", (Parcelable)ListenerService.this.getNotificationSummary(notificationAdapter)));
                }
                else if (access$201 && !ListenerService.this.getStoredSummary(notificationAdapter.getKey()).equals((Object)ListenerService.this.getNotificationSummary(notificationAdapter))) {
                    ListenerService.this.storeNotification(notificationAdapter);
                    LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_UPDATED").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_SUMMARY", (Parcelable)ListenerService.this.getNotificationSummary(notificationAdapter)));
                }
            }
        }
    }
    
    private class NotificationReceiver extends BroadcastReceiver
    {
        private final String TAG;
        
        private NotificationReceiver() {
            this.TAG = NotificationReceiver.class.getSimpleName();
        }
        
        private void onReceiveCustom(final Bundle bundle) {
            Log.d(this.TAG, "Received a request (custom notification action)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_ACTION") == null) {
                Log.w(this.TAG, "Broadcast is missing extras");
                return;
            }
            final int int1 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID");
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE");
            final int int2 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID");
            final int int3 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_ACTION");
            String access$1100 = null;
            NotificationAdapter adapter = null;
            if (ListenerService.this.isKeyStored(int1)) {
                access$1100 = ListenerService.this.getStoredKey(int1);
                adapter = ListenerService.this.getAdapter(access$1100);
            }
            if (access$1100 == null || adapter == null) {
                Log.w(this.TAG, "Not an active notification");
                LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_NOTIFICATION_ID").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2));
                return;
            }
            if (adapter.triggerAction(int3)) {
                Log.d(this.TAG, "Custom action triggered");
                ListenerService.this.cancelNotification(adapter);
                LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ACTION_TRIGGERED").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2));
                return;
            }
            Log.w(this.TAG, "Unable to trigger custom action");
            LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_ACTION").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2));
        }
        
        private void onReceiveDismiss(final Bundle bundle) {
            Log.d(this.TAG, "Received a request (dismiss notification)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID") == null) {
                Log.w(this.TAG, "Broadcast is missing extras");
                return;
            }
            final int int1 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID");
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE");
            final int int2 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID");
            String access$1100 = null;
            NotificationAdapter adapter = null;
            if (ListenerService.this.isKeyStored(int1)) {
                access$1100 = ListenerService.this.getStoredKey(int1);
                adapter = ListenerService.this.getAdapter(access$1100);
            }
            if (access$1100 != null && adapter != null) {
                Log.d(this.TAG, "Dismiss triggered");
                ListenerService.this.cancelNotification(adapter);
                LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ACTION_TRIGGERED").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2));
                return;
            }
            Log.w(this.TAG, "Not an active notification");
            LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_NOTIFICATION_ID").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2));
        }
        
        private void onReceiveList(final Bundle bundle) {
            Log.d(this.TAG, "Received a request (list)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE") == null) {
                Log.w(this.TAG, "Broadcast is missing extras");
            }
            else {
                final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE");
                final Iterator<NotificationSummary> iterator = ListenerService.this.getStoredSummaries().values().iterator();
                while (iterator.hasNext()) {
                    LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_POSTED").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_SUMMARY", (Parcelable)iterator.next()));
                }
            }
        }
        
        private void onReceiveOpen(final Bundle bundle) {
            Log.d(this.TAG, "Received a request (open notification)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID") == null) {
                Log.w(this.TAG, "Broadcast is missing extras");
                return;
            }
            final int int1 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID");
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE");
            final int int2 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID");
            String access$1100 = null;
            NotificationAdapter adapter = null;
            if (ListenerService.this.isKeyStored(int1)) {
                access$1100 = ListenerService.this.getStoredKey(int1);
                adapter = ListenerService.this.getAdapter(access$1100);
            }
            if (access$1100 == null || adapter == null) {
                Log.w(this.TAG, "Not an active notification");
                LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_NOTIFICATION_ID").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2));
                return;
            }
            if (adapter.triggerNotification()) {
                Log.d(this.TAG, "Open triggered");
                ListenerService.this.cancelNotification(adapter);
                LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ACTION_TRIGGERED").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2));
                return;
            }
            Log.w(this.TAG, "Unable to trigger open");
            LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_ACTION").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2));
        }
        
        private void onReceiveReadActions(final Bundle bundle) {
            Log.d(this.TAG, "Received a request (read actions)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID") == null) {
                Log.w(this.TAG, "Broadcast is missing extras");
                return;
            }
            final int int1 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID");
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE");
            final int int2 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID");
            String access$1100 = null;
            NotificationAdapter adapter = null;
            if (ListenerService.this.isKeyStored(int1)) {
                access$1100 = ListenerService.this.getStoredKey(int1);
                adapter = ListenerService.this.getAdapter(access$1100);
            }
            if (access$1100 != null && adapter != null) {
                Log.d(this.TAG, "Actions were read");
                LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ACTIONS_READ").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_ACTIONS", (Parcelable)ListenerService.this.getNotificationActionList(adapter)));
                return;
            }
            Log.w(this.TAG, "Not an active notification");
            LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_NOTIFICATION_ID").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2));
        }
        
        private void onReceiveReadAttributes(final Bundle bundle) {
            Log.d(this.TAG, "Received a request (read attributes)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID") == null || bundle.get("net.medcorp.library.android.notificationserver.listener.EXTRA_ATTRIBUTES") == null) {
                Log.w(this.TAG, "Broadcast is missing extras");
                return;
            }
            final int int1 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID");
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE");
            final int int2 = bundle.getInt("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID");
            final int[] intArray = bundle.getIntArray("net.medcorp.library.android.notificationserver.listener.EXTRA_ATTRIBUTES");
            String access$1100 = null;
            NotificationAdapter adapter = null;
            if (ListenerService.this.isKeyStored(int1)) {
                access$1100 = ListenerService.this.getStoredKey(int1);
                adapter = ListenerService.this.getAdapter(access$1100);
            }
            if (access$1100 != null && adapter != null) {
                Log.d(this.TAG, "Attributes were read");
                LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ATTRIBUTES_READ").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_ATTRIBUTES", (Parcelable)ListenerService.this.getNotificationAttributeList(adapter, intArray)));
                return;
            }
            Log.w(this.TAG, "Not an active notification");
            LocalBroadcastManager.getInstance((Context)ListenerService.this).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_NOTIFICATION_ID").putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID", int2));
        }
        
        private void onReceiveUnknown() {
            Log.w(this.TAG, "Unknown broadcast received");
        }
        
        public void onReceive(final Context context, final Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Log.w(this.TAG, "Broadcast received with no specified action");
                return;
            }
            final Bundle extras = intent.getExtras();
            final String action = intent.getAction();
            switch (action) {
                default: {
                    this.onReceiveUnknown();
                }
                case "net.medcorp.library.android.notificationserver.listener.ACTION_LIST": {
                    this.onReceiveList(extras);
                }
                case "net.medcorp.library.android.notificationserver.listener.ACTION_READ_ATTRIBUTES": {
                    this.onReceiveReadAttributes(extras);
                }
                case "net.medcorp.library.android.notificationserver.listener.ACTION_READ_ACTIONS": {
                    this.onReceiveReadActions(extras);
                }
                case "net.medcorp.library.android.notificationserver.listener.ACTION_TRIGGER_DISMISS": {
                    this.onReceiveDismiss(extras);
                }
                case "net.medcorp.library.android.notificationserver.listener.ACTION_TRIGGER_OPEN": {
                    this.onReceiveOpen(extras);
                }
                case "net.medcorp.library.android.notificationserver.listener.ACTION_TRIGGER_CUSTOM": {
                    this.onReceiveCustom(extras);
                }
            }
        }
    }
}

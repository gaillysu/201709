package net.medcorp.library.android.notificationsdk.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import net.medcorp.library.android.notificationsdk.config.mode.FilterMode;
import net.medcorp.library.android.notificationsdk.config.mode.OverrideMode;
import net.medcorp.library.android.notificationsdk.config.type.FilterType;
import net.medcorp.library.android.notificationsdk.config.type.OverrideType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigMonitor
{
    private SharedPreferences.OnSharedPreferenceChangeListener mChangeListener;
    private Context mContext;
    private Map<FilterType, FilterMode> mFilterModes;
    private Map<FilterType, SharedPreferences> mFilterPreferences;
    private Map<FilterType, Set<String>> mFilterSets;
    private List<ConfigMonitorListener> mListeners;
    private Map<OverrideType, String> mOverrideFallbacks;
    private Map<OverrideType, Map<String, String>> mOverrideMaps;
    private Map<OverrideType, OverrideMode> mOverrideModes;
    private Map<OverrideType, SharedPreferences> mOverridePreferences;
    private ConfigMonitorReceiver mReceiver;
    
    public ConfigMonitor(final Context mContext) {
        final int n = 0;
        this.mListeners = new ArrayList<ConfigMonitorListener>();
        this.mContext = mContext;
        this.mFilterPreferences = new HashMap<FilterType, SharedPreferences>();
        final FilterType[] values = FilterType.values();
        for (int length = values.length, i = 0; i < length; ++i) {
            final FilterType filterType = values[i];
            this.mFilterPreferences.put(filterType, this.mContext.getSharedPreferences(filterType.getAlias(), 4));
        }
        this.mOverridePreferences = new HashMap<OverrideType, SharedPreferences>();
        final OverrideType[] values2 = OverrideType.values();
        for (int length2 = values2.length, j = n; j < length2; ++j) {
            final OverrideType overrideType = values2[j];
            this.mOverridePreferences.put(overrideType, this.mContext.getSharedPreferences(overrideType.getAlias(), 4));
        }
        this.mReceiver = new ConfigMonitorReceiver();
        this.rebuildCache();
    }
    
    private void rebuildCache() {
        this.mFilterModes = new HashMap<FilterType, FilterMode>();
        this.mFilterSets = new HashMap<FilterType, Set<String>>();
        final FilterType[] values = FilterType.values();
        for (int length = values.length, i = 0; i < length; ++i) {
            final FilterType filterType = values[i];
            final SharedPreferences sharedPreferences = this.mFilterPreferences.get(filterType);
            this.mFilterModes.put(filterType, FilterMode.valueOf(sharedPreferences.getString("filter_mode", FilterMode.DISABLED.name())));
            this.mFilterSets.put(filterType, new HashSet<String>(sharedPreferences.getStringSet("filter_set", (Set)new HashSet())));
        }
        this.mOverrideModes = new HashMap<OverrideType, OverrideMode>();
        this.mOverrideMaps = new HashMap<OverrideType, Map<String, String>>();
        this.mOverrideFallbacks = new HashMap<OverrideType, String>();
        final OverrideType[] values2 = OverrideType.values();
        for (int length2 = values2.length, j = 0; j < length2; ++j) {
            final OverrideType overrideType = values2[j];
            final SharedPreferences sharedPreferences2 = this.mOverridePreferences.get(overrideType);
            this.mOverrideModes.put(overrideType, OverrideMode.valueOf(sharedPreferences2.getString("override_mode", OverrideMode.DISABLED.name())));
            this.mOverrideFallbacks.put(overrideType, sharedPreferences2.getString("override_fallback", (String)null));
            final HashMap<String, String> hashMap = new HashMap<String, String>();
            final Map<String, ?> all = sharedPreferences2.getAll();
            if (all != null) {
                for (final Map.Entry<String, ?> entry : all.entrySet()) {
                    if (!entry.getKey().equals("override_mode") && !entry.getKey().equals("override_fallback")) {
                        hashMap.put(entry.getKey(), (String)entry.getValue());
                    }
                }
            }
            this.mOverrideMaps.put(overrideType, hashMap);
        }
    }
    
    public String getOverride(final OverrideType overrideType, final String s, final String s2, final String s3) {
        final OverrideMode overrideMode = this.mOverrideModes.get(overrideType);
        final String s4 = this.mOverrideFallbacks.get(overrideType);
        final Map<String, String> map = this.mOverrideMaps.get(overrideType);
        String s5 = s4;
        if (s4 == null) {
            s5 = s3;
        }
        if (overrideMode != null) {
            if (map.get(s) != null && overrideMode != OverrideMode.DISABLED) {
                return map.get(s);
            }
            if (map.get(s) == null && overrideMode == OverrideMode.STRICT) {
                return s5;
            }
        }
        return s2;
    }
    
    public boolean matchesFilter(final FilterType filterType, final String s) {
        final FilterMode filterMode = this.mFilterModes.get(filterType);
        final Set<String> set = this.mFilterSets.get(filterType);
        return filterMode == null || set == null || ((filterMode != FilterMode.WHITELIST || set.contains(s)) && (filterMode != FilterMode.BLACKLIST || !set.contains(s)));
    }
    
    public boolean matchesFilterIfNoAvailableInfo(final FilterType filterType) {
        final FilterMode filterMode = this.mFilterModes.get(filterType);
        return filterMode == null || filterMode != FilterMode.WHITELIST;
    }
    
    public void registerListener(final ConfigMonitorListener configMonitorListener) {
        if (this.mListeners.size() == 0) {
            LocalBroadcastManager.getInstance(this.mContext).registerReceiver(this.mReceiver, new IntentFilter("net.medcorp.library.android.notificationserver.config.ACTION_CONFIG_CHANGED"));
        }
        this.mListeners.add(configMonitorListener);
    }
    
    public void unregisterListener(final ConfigMonitorListener configMonitorListener) {
        this.mListeners.remove(configMonitorListener);
        if (this.mListeners.size() == 0) {
            LocalBroadcastManager.getInstance(this.mContext).unregisterReceiver(this.mReceiver);
        }
    }
    
    public interface ConfigMonitorListener
    {
        void onConfigChanged();
    }
    
    public class ConfigMonitorReceiver extends BroadcastReceiver
    {
        public void onReceive(final Context context, final Intent intent) {
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(final Void... array) {
                    ConfigMonitor.this.rebuildCache();
                    return null;
                }
                
                protected void onPostExecute(final Void void1) {
                    super.onPostExecute((Void) void1);
                    if (intent != null && intent.getAction() != null && intent.getAction().equals("net.medcorp.library.android.notificationserver.config.ACTION_CONFIG_CHANGED")) {
                        final Iterator<ConfigMonitorListener> iterator = ConfigMonitor.this.mListeners.iterator();
                        while (iterator.hasNext()) {
                            iterator.next().onConfigChanged();
                        }
                    }
                }
            }.execute((Void[]) new Void[0]);
        }
    }
}

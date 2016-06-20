package net.medcorp.library.android.notificationsdk.config;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import net.medcorp.library.android.notificationsdk.config.mode.FilterMode;
import net.medcorp.library.android.notificationsdk.config.mode.OverrideMode;
import net.medcorp.library.android.notificationsdk.config.type.FilterType;
import net.medcorp.library.android.notificationsdk.config.type.OverrideType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigEditor
{
    private Context mContext;
    private Map<FilterType, FilterMode> mFilterModes;
    private Map<FilterType, SharedPreferences> mFilterPreferences;
    private Map<FilterType, Set<String>> mFilterSets;
    private Map<OverrideType, String> mOverrideFallbacks;
    private Map<OverrideType, Map<String, String>> mOverrideMaps;
    private Map<OverrideType, OverrideMode> mOverrideModes;
    private Map<OverrideType, SharedPreferences> mOverridePreferences;
    
    public ConfigEditor(final Context mContext) {
        final int n = 0;
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
        this.reset();
    }
    
    public boolean addFilter(final FilterType filterType, final String s) {
        return this.mFilterSets.get(filterType).add(s);
    }
    
    public boolean addOverride(final OverrideType overrideType, final String s, final String s2) {
        this.mOverrideMaps.get(overrideType).put(s, s2);
        return true;
    }
    
    public void apply() {
        final FilterType[] values = FilterType.values();
        for (int length = values.length, i = 0; i < length; ++i) {
            final FilterType filterType = values[i];
            final SharedPreferences.Editor edit = this.mFilterPreferences.get(filterType).edit();
            edit.putString("filter_mode", this.mFilterModes.get(filterType).name());
            edit.putStringSet("filter_set", (Set)this.mFilterSets.get(filterType));
            edit.apply();
        }
        final OverrideType[] values2 = OverrideType.values();
        for (int length2 = values2.length, j = 0; j < length2; ++j) {
            final OverrideType overrideType = values2[j];
            final SharedPreferences.Editor edit2 = this.mOverridePreferences.get(overrideType).edit();
            edit2.putString("override_mode", this.mOverrideModes.get(overrideType).name());
            edit2.putString("override_fallback", (String)this.mOverrideFallbacks.get(overrideType));
            final Map<String, String> map = this.mOverrideMaps.get(overrideType);
            final Map<String, ?> all = this.mOverridePreferences.get(overrideType).getAll();
            if (all != null) {
                for (final Map.Entry<String, ?> entry : all.entrySet()) {
                    if (!entry.getKey().equals("override_mode") && !entry.getKey().equals("override_fallback") && !map.containsKey(entry.getKey())) {
                        edit2.remove((String)entry.getKey());
                    }
                }
            }
            for (final Map.Entry<String, String> entry2 : map.entrySet()) {
                edit2.putString((String)entry2.getKey(), (String)entry2.getValue());
            }
            edit2.apply();
        }
        LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.config.ACTION_CONFIG_CHANGED"));
    }
    
    public boolean containsFilter(final FilterType filterType, final String s) {
        return this.mFilterSets.get(filterType).contains(s);
    }
    
    public boolean containsOverride(final OverrideType overrideType, final String s) {
        return this.mOverrideMaps.get(overrideType).containsKey(s);
    }
    
    @NonNull
    public FilterMode getFilterMode(final FilterType filterType) {
        return this.mFilterModes.get(filterType);
    }
    
    @NonNull
    public Set<String> getFilterSet(final FilterType filterType) {
        return this.mFilterSets.get(filterType);
    }
    
    @Nullable
    public String getOverrideFallback(final OverrideType overrideType) {
        return this.mOverrideFallbacks.get(overrideType);
    }
    
    @NonNull
    public Map<String, String> getOverrideMap(final OverrideType overrideType) {
        return this.mOverrideMaps.get(overrideType);
    }
    
    @NonNull
    public OverrideMode getOverrideMode(final OverrideType overrideType) {
        return this.mOverrideModes.get(overrideType);
    }
    
    public boolean removeFilter(final FilterType filterType, final String s) {
        return this.mFilterSets.get(filterType).remove(s);
    }
    
    public boolean removeOverride(final OverrideType overrideType, final String s) {
        return this.mOverrideMaps.get(overrideType).remove(s) != null;
    }
    
    public void reset() {
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
            final Map<String, String> hashMap = new HashMap<String, String>();
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
    
    public void setFilterMode(final FilterType filterType, final FilterMode filterMode) {
        this.mFilterModes.put(filterType, filterMode);
    }
    
    public void setFilterSet(final FilterType filterType, final Set<String> set) {
        this.mFilterSets.put(filterType, set);
    }
    
    public void setOverrideFallback(final OverrideType overrideType, final String s) {
        this.mOverrideFallbacks.put(overrideType, s);
    }
    
    public void setOverrideMap(final OverrideType overrideType, final Map<String, String> map) {
        this.mOverrideMaps.put(overrideType, map);
    }
    
    public void setOverrideMode(final OverrideType overrideType, final OverrideMode overrideMode) {
        this.mOverrideModes.put(overrideType, overrideMode);
    }
    
    public void unsetOverrideFallback(final OverrideType overrideType) {
        this.mOverrideFallbacks.put(overrideType, null);
    }
}

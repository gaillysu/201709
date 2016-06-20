package net.medcorp.library.android.notification.activity;

import android.app.*;
import android.widget.*;
import net.medcorp.library.android.notificationsdk.config.*;
import android.view.*;
import net.medcorp.library.android.notificationsdk.config.type.*;
import android.content.*;
import android.os.*;
import net.medcorp.library.android.notificationsdk.config.mode.*;

public class ConfigActivity extends Activity
{
    EditText contactText;
    ConfigEditor mEditor;
    EditText packageText;
    EditText priorityFallbackText;
    EditText priorityPackageText;
    EditText priorityValueText;
    
    public void addContactFilter(final View view) {
        this.mEditor.addFilter(FilterType.CONTACT, this.contactText.getText().toString());
    }
    
    public void addPackageFilter(final View view) {
        this.mEditor.addFilter(FilterType.PACKAGE, this.packageText.getText().toString());
    }
    
    public void addPriority(final View view) {
        this.mEditor.addOverride(OverrideType.PRIORITY, this.priorityPackageText.getText().toString(), this.priorityValueText.getText().toString());
    }
    
    public void apply(final View view) {
        this.mEditor.apply();
    }
    
    public void notify(final View view) {
        Utils.notify((Context)this);
    }
    
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(2130968601);
        this.mEditor = new ConfigEditor((Context)this);
        (this.packageText = (EditText)this.findViewById(2131492943)).setText((CharSequence)this.getApplication().getPackageName());
        (this.contactText = (EditText)this.findViewById(2131492944)).setText((CharSequence)"244");
        (this.priorityPackageText = (EditText)this.findViewById(2131492945)).setText((CharSequence)this.getApplication().getPackageName());
        (this.priorityValueText = (EditText)this.findViewById(2131492946)).setText((CharSequence)String.valueOf("3"));
        (this.priorityFallbackText = (EditText)this.findViewById(2131492947)).setText((CharSequence)String.valueOf("255"));
    }
    
    public void removeContactFilter(final View view) {
        this.mEditor.removeFilter(FilterType.CONTACT, this.contactText.getText().toString());
    }
    
    public void removeFallback(final View view) {
        this.mEditor.unsetOverrideFallback(OverrideType.CATEGORY);
    }
    
    public void removePackageFilter(final View view) {
        this.mEditor.removeFilter(FilterType.PACKAGE, this.packageText.getText().toString());
    }
    
    public void removePriority(final View view) {
        this.mEditor.removeOverride(OverrideType.PRIORITY, this.priorityPackageText.getText().toString());
    }
    
    public void reset(final View view) {
        this.mEditor.reset();
    }
    
    public void setContactFilterBlacklist(final View view) {
        this.mEditor.setFilterMode(FilterType.CONTACT, FilterMode.BLACKLIST);
    }
    
    public void setContactFilterDisabled(final View view) {
        this.mEditor.setFilterMode(FilterType.CONTACT, FilterMode.DISABLED);
    }
    
    public void setContactFilterWhitelist(final View view) {
        this.mEditor.setFilterMode(FilterType.CONTACT, FilterMode.WHITELIST);
    }
    
    public void setFallback(final View view) {
        this.mEditor.setOverrideFallback(OverrideType.PRIORITY, this.priorityFallbackText.getText().toString());
    }
    
    public void setOverrideDefault(final View view) {
        this.mEditor.setOverrideMode(OverrideType.PRIORITY, OverrideMode.DEFAULT);
    }
    
    public void setOverrideDisabled(final View view) {
        this.mEditor.setOverrideMode(OverrideType.PRIORITY, OverrideMode.DISABLED);
    }
    
    public void setOverrideStrict(final View view) {
        this.mEditor.setOverrideMode(OverrideType.PRIORITY, OverrideMode.STRICT);
    }
    
    public void setPackageFilterBlacklist(final View view) {
        this.mEditor.setFilterMode(FilterType.PACKAGE, FilterMode.BLACKLIST);
    }
    
    public void setPackageFilterDisabled(final View view) {
        this.mEditor.setFilterMode(FilterType.PACKAGE, FilterMode.DISABLED);
    }
    
    public void setPackageFilterWhitelist(final View view) {
        this.mEditor.setFilterMode(FilterType.PACKAGE, FilterMode.WHITELIST);
    }
}

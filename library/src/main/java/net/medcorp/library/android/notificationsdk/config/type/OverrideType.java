package net.medcorp.library.android.notificationsdk.config.type;

public enum OverrideType
{
    CATEGORY("category_override"), 
    PRIORITY("priority_override");
    
    private final String mAlias;
    
    private OverrideType(final String mAlias) {
        this.mAlias = mAlias;
    }
    
    public String getAlias() {
        return this.mAlias;
    }
}

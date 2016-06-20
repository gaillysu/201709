package net.medcorp.library.android.notificationsdk.config.type;

public enum FilterType
{
    CATEGORY("category_filter"), 
    CONTACT("contact_filter"), 
    PACKAGE("package_filter"), 
    PRIORITY("priority_filter");
    
    private final String mAlias;
    
    private FilterType(final String mAlias) {
        this.mAlias = mAlias;
    }
    
    public String getAlias() {
        return this.mAlias;
    }
}

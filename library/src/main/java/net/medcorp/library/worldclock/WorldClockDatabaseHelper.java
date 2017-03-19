package net.medcorp.library.worldclock;

import android.content.Context;
import android.content.SharedPreferences;

import net.medcorp.library.R;
import net.medcorp.library.worldclock.event.WorldClockInitializeEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by Karl on 8/4/16.
 */

public class WorldClockDatabaseHelper {

    private Context context;
    private final int CITIES_VERSION;
    private final int TIMEZONE_VERSION;
    private final SharedPreferences pref;
    private Realm realm;
    private final String REALM_NAME = "med_library.realm";
    private RealmConfiguration mConfig;


    public WorldClockDatabaseHelper(Context context) {
        setDefaultRealmData();
        this.context = context;
        realm = Realm.getInstance(mConfig);
        CITIES_VERSION = context.getResources().getInteger(R.integer.config_preferences_cities_db_version_current);
        TIMEZONE_VERSION = context.getResources().getInteger(R.integer.config_preferences_timezone_db_version_current);
        pref = context.getSharedPreferences(context.getString(R.string.config_preferences_world_clock_preferences), Context.MODE_PRIVATE);
    }

    public void setupWorldClock() {
        if (getTimeZoneVersion() == 0 || getCitiesVersion() == 0) {
            bumpCitiesVersion();
            bumpTimeZoneVersion();
            setDefaultRealmData();
        } else if (getCitiesVersion() < CITIES_VERSION || getTimeZoneVersion() < TIMEZONE_VERSION) {
            realm = Realm.getInstance(mConfig);
            RealmResults<TimeZone> oldTimezones = realm.where(TimeZone.class).findAll();
            RealmResults<City> oldCities = realm.where(City.class).findAll();
            oldTimezones.deleteAllFromRealm();
            oldCities.deleteAllFromRealm();
            bumpCitiesVersion();
            bumpTimeZoneVersion();
            setDefaultRealmData();
        }
    }

    private void setDefaultRealmData() {
        EventBus.getDefault().post(new WorldClockInitializeEvent(WorldClockInitializeEvent.STATUS.STARTED));
        mConfig = new RealmConfiguration.Builder().name(REALM_NAME)
                .assetFile(REALM_NAME).modules(new WorldClockLibraryModule()).build();
        Realm.setDefaultConfiguration(mConfig);
        EventBus.getDefault().post(new WorldClockInitializeEvent(WorldClockInitializeEvent.STATUS.FINISHED));
    }

    private void bumpCitiesVersion() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(context.getString(R.string.config_preferences_cities_db_saved_version), CITIES_VERSION);
        editor.apply();
    }

    private void bumpTimeZoneVersion() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(context.getString(R.string.config_preferences_timezone_db_saved_version), TIMEZONE_VERSION);
        editor.apply();
    }

    private int getCitiesVersion() {
        return pref.getInt(context.getString(R.string.config_preferences_cities_db_saved_version), 0);
    }

    private int getTimeZoneVersion() {
        return pref.getInt(context.getString(R.string.config_preferences_timezone_db_saved_version), 0);
    }

    public City get(String name) {
        realm.beginTransaction();
        City alarm = realm.where(City.class).equalTo("name", name).findFirst();
        realm.commitTransaction();
        return alarm;
    }

    public City get(int id){
        realm.beginTransaction();
        City city = realm.where(City.class).equalTo("id", id).findFirst();
        realm.commitTransaction();
        return city;
    }

    public List<City> getAll() {
        realm.beginTransaction();
        RealmResults<City> allAlarms = realm.where(City.class).findAllSorted("name");
        realm.commitTransaction();
        return allAlarms;
    }
}
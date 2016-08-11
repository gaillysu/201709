package net.medcorp.library.worldclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import net.medcorp.library.R;
import net.medcorp.library.util.AssetsUtil;
import net.medcorp.library.worldclock.event.WorldClockInitializeEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Karl on 8/4/16.
 */

public class WorldClockDatabaseHelper{

    private Context context;
    private final int CITIES_VERSION;
    private final int TIMEZONE_VERSION;
    private final SharedPreferences pref;
    private final Realm realm;

    public WorldClockDatabaseHelper(Context context){
        this.context = context;
        realm = Realm.getDefaultInstance();
        CITIES_VERSION = context.getResources().getInteger(R.integer.config_preferences_cities_db_version_current);
        TIMEZONE_VERSION = context.getResources().getInteger(R.integer.config_preferences_timezone_db_version_current);
        pref = context.getSharedPreferences(context.getString(R.string.config_preferences_world_clock_preferences),Context.MODE_PRIVATE);
    }

    public void setupWorldClock() {
        realm.executeTransactionAsync(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {
                EventBus.getDefault().post(new WorldClockInitializeEvent(WorldClockInitializeEvent.STATUS.STARTED));
                boolean citiesSuccess = false;
                boolean timezonesSuccess = false;
                Gson gson = new Gson();
                final RealmResults<TimeZone> oldTimezones = realm.where(TimeZone.class).findAll();
                final RealmResults<City> oldCities = realm.where(City.class).findAll();
                final boolean forceSync = oldCities.size() == 0 || oldTimezones.size() == 0;
                if(getTimeZoneVersion() < TIMEZONE_VERSION || forceSync) {
                    EventBus.getDefault().post(new WorldClockInitializeEvent(WorldClockInitializeEvent.STATUS.STARTED_TIMEZONES));
                    try {
                        final JSONArray timezonesArray = AssetsUtil.getJSONArrayFromAssets(context, R.string.config_timezones_file_name);
                        for (int i = 0; i< timezonesArray.length(); i++) {
                            TimeZone timezone = gson.fromJson(timezonesArray.getJSONObject(i).toString(),TimeZone.class);
                            realm.copyToRealm(timezone);
                        }
                        timezonesSuccess = true;
                    } catch (IOException | JSONException e) {
                        EventBus.getDefault().post(new WorldClockInitializeEvent(WorldClockInitializeEvent.STATUS.EXCEPTION, e));
                    }
                } else {
                    Log.w("med-library", "Don't need to setup the timezone!");
                }
                EventBus.getDefault().post(new WorldClockInitializeEvent(WorldClockInitializeEvent.STATUS.FINISHED_TIMEZONES));
                if (getCitiesVersion() < CITIES_VERSION || forceSync){
                    EventBus.getDefault().post(new WorldClockInitializeEvent(WorldClockInitializeEvent.STATUS.STARTED_CITIES));
                    try {
                        final JSONArray citiesArray = AssetsUtil.getJSONArrayFromAssets(context, R.string.config_cities_file_name);
                        final RealmResults<TimeZone> results = realm.where(TimeZone.class).findAll();
                        for (int i = 0; i< citiesArray.length(); i++) {
                            City city = gson.fromJson(citiesArray.getJSONObject(i).toString(),City.class);
                            City realmCity = realm.copyToRealm(city);
                            for (TimeZone timezone: results) {
                                if (realmCity.getTimezoneId() == timezone.getId()){
                                    realmCity.setTimezoneRef(timezone);
                                    break;
                                }
                            }
                        }
                        citiesSuccess = true;
                    } catch (IOException | JSONException e) {
                        EventBus.getDefault().post(new WorldClockInitializeEvent(WorldClockInitializeEvent.STATUS.EXCEPTION, e));
                    }
                } else {
                    Log.w("med-library", "Don't need to setup the cities!");
                }
                EventBus.getDefault().post(new WorldClockInitializeEvent(WorldClockInitializeEvent.STATUS.FINISHED_CITIES));
                if (timezonesSuccess && citiesSuccess) {
                    oldTimezones.deleteAllFromRealm();
                    oldCities.deleteAllFromRealm();
                    bumpCitiesVersion();
                    bumpTimeZoneVersion();
                    EventBus.getDefault().post(new WorldClockInitializeEvent(WorldClockInitializeEvent.STATUS.FINISHED));
                }
            }
        });

    }

    private void bumpCitiesVersion(){
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(context.getString(R.string.config_preferences_cities_db_saved_version), CITIES_VERSION);
        editor.apply();
    }

    private void bumpTimeZoneVersion(){
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(context.getString(R.string.config_preferences_timezone_db_saved_version), TIMEZONE_VERSION);
        editor.apply();
    }

    private int getCitiesVersion(){
        return pref.getInt(context.getString(R.string.config_preferences_cities_db_saved_version), 0);
    }

    private int getTimeZoneVersion(){
        return pref.getInt(context.getString(R.string.config_preferences_timezone_db_saved_version), 0);
    }
}
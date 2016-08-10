package net.medcorp.library.worldclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.medcorp.library.R;
import net.medcorp.library.util.AssetsUtil;

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
        boolean citiesSuccess = false;
        boolean timezonesSuccess = false;
        Log.w("Karl","Current TimeZone version = " + getTimeZoneVersion());
        Log.w("Karl","Current City version     = " + getCitiesVersion());
        Log.w("Karl","Newest TimeZone version  = " + TIMEZONE_VERSION );
        Log.w("Karl","Newest City version      = " + CITIES_VERSION);
        boolean forceSync = false;
        final RealmResults<TimeZone> oldTimezones = realm.where(TimeZone.class).findAll();
        final RealmResults<City> oldCities = realm.where(City.class).findAll();
        if (oldCities.size() == 0 || oldTimezones.size() == 0){
            forceSync = true;
        }
        if(getTimeZoneVersion() < TIMEZONE_VERSION || forceSync) {
            try {
                final JSONArray timezonesArray = AssetsUtil.getJSONArrayFromAssets(context, R.string.config_timezones_file_name);
                for (int i = 0; i< timezonesArray.length(); i++) {
                    realm.beginTransaction();
                    realm.createObjectFromJson(TimeZone.class, timezonesArray.getJSONObject(i));
                    realm.commitTransaction();
                }
                timezonesSuccess = true;
            } catch (IOException e) {
                Log.w("med-library","Couldn't open the timezones json!");
            } catch (JSONException e) {
                Log.w("med-library","This shouldn't happen!");
            }
        } else {
            Log.w("med-library", "Don't need to setup the timezone!");
        }
        if (getCitiesVersion() < CITIES_VERSION || forceSync){
            try {
                final JSONArray citiesArray = AssetsUtil.getJSONArrayFromAssets(context, R.string.config_cities_file_name);
                final RealmResults<TimeZone> results = realm.where(TimeZone.class).findAll();
                for (int i = 0; i< citiesArray.length(); i++) {
                    final int finalI = i;
                    realm.beginTransaction();
                    City city = realm.createObjectFromJson(City.class,citiesArray.getJSONObject(finalI));
                    for (TimeZone timezone: results) {
                        if (city.getTimezone_id() == timezone.getId()){
                            city.setTimezoneRef(timezone);
                            break;
                        }
                    }
                    realm.commitTransaction();
                }
                citiesSuccess = true;
            } catch (IOException e) {
                Log.w("med-library","Couldn't open the cities json!");
            } catch (JSONException e) {
                Log.w("med-library","This shouldn't happen!");
            }
        } else {
            Log.w("med-library","Don't need to setup the cities!");
        }
        if (timezonesSuccess && citiesSuccess) {
            realm.beginTransaction();
            oldTimezones.deleteAllFromRealm();
            oldCities.deleteAllFromRealm();
            bumpCitiesVersion();
            bumpTimeZoneVersion();
            realm.commitTransaction();
        }
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
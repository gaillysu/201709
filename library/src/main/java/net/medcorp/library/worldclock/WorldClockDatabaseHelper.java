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

public class WorldClockDatabaseHelper {

    private Context context;
    private final int CITIES_VERSION;
    private final int TIMEZONE_VERSION;
    private final SharedPreferences pref;
    private final Realm realm = Realm.getDefaultInstance();

    public WorldClockDatabaseHelper(Context context){
        this.context = context;
        CITIES_VERSION = context.getResources().getInteger(R.integer.config_preferences_cities_db_version_current);
        TIMEZONE_VERSION = context.getResources().getInteger(R.integer.config_preferences_timezone_db_version_current);
        pref = context.getSharedPreferences(context.getString(R.string.config_preferences_world_clock_preferences),Context.MODE_PRIVATE);
    }

    public void setupWorldClock() {
        final RealmResults<TimeZone> oldTimezones = realm.where(TimeZone.class).findAll();
        final RealmResults<City> oldCities = realm.where(City.class).findAll();
        boolean citiesSuccess = false;
        boolean timezonesSuccess = false;

        if (getCitiesVersion() < CITIES_VERSION){
            try {
                final JSONArray citiesArray = AssetsUtil.getJSONArrayFromAssets(context, R.string.config_cities_file_name);
                for (int i = 0; i< citiesArray.length(); i++) {
                    final int finalI = i;
                    realm.executeTransaction(new Realm.Transaction(){
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.createObjectFromJson(City.class,citiesArray.getJSONObject(finalI));
                            } catch (JSONException e) {
                                Log.w("med-library","Couldn't parse json object while inserting!!");
                            }
                        }
                    });
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

        if(getTimeZoneVersion() < TIMEZONE_VERSION){

            try {
                final JSONArray timezonesArray = AssetsUtil.getJSONArrayFromAssets(context, R.string.config_timezones_file_name);
                for (int i = 0; i< timezonesArray.length(); i++) {
                    final int finalI = i;
                    realm.executeTransaction(new Realm.Transaction(){
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.createObjectFromJson(TimeZone.class,timezonesArray.getJSONObject(finalI));
                            } catch (JSONException e) {
                                Log.w("med-library","Couldn't parse json object while inserting!!");
                            }
                        }
                    });
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
        if (timezonesSuccess && citiesSuccess){
            oldTimezones.deleteAllFromRealm();
            oldCities.deleteAllFromRealm();
            bumpCitiesVersion();
            bumpTimeZoneVersion();
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

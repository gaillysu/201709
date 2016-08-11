package net.medcorp.library.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Karl on 8/4/16.
 */

public class AssetsUtil {

    public static JSONObject getJSONObjectFromAssets(Context context, @StringRes int fileName) throws IOException, JSONException{
        String jsonString = getStringFromAssets(context,fileName);
        if(isStringJSONObject(jsonString)){
            return new JSONObject(jsonString);
        }
        Log.w("med-library","TRIED TO PARSE INVALID JSON OBJECT FILE, FILE EMPTY OR INVALID JSON");
        return new JSONObject();
    }

    public static JSONArray getJSONArrayFromAssets(Context context, @StringRes int fileName) throws IOException, JSONException{
        String jsonArrayString = getStringFromAssets(context,fileName);
        if(isStringJSONArray(jsonArrayString)){
            return new JSONArray(jsonArrayString);
        }
        Log.w("med-library","TRIED TO PARSE INVALID JSON OBJECT FILE, FILE EMPTY OR INVALID JSON");
        return new JSONArray();
    }

    public static String getStringFromAssets(Context context, @StringRes int fileName) throws IOException{
        InputStream inputStream = context.getAssets().open(context.getString(fileName));
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder(inputStream.available());
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }

    public static boolean isStringJSONObject(String string){
        try {
            new JSONObject(string);
        } catch (JSONException exception) {
            return false;
        }
        return true;
    }

    public static boolean isStringJSONArray(String string){
        try {
            new JSONArray(string);
        } catch (JSONException exception) {
            return false;
        }
        return true;
    }
}

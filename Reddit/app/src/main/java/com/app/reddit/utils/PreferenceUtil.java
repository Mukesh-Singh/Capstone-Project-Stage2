package com.app.reddit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.app.reddit.events.AccessTokenExpiredEvent;
import com.app.reddit.models.Subreddit;
import com.google.gson.Gson;

import java.util.List;

import de.greenrobot.event.EventBus;


public class PreferenceUtil {
    private final String TAG = PreferenceUtil.class.getSimpleName();
    private final Context mContext;
    public static final String AUTH_CODE = "auth_code";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ACCESS_TOKEN_EXPIRE_IN = "access_token_expire_in";
    public static final String ACCESS_TOKEN_UPDATED_AT = "access_token_updated_at";
    public static final String USER_NAME = "user_name";
    public static final String SUBREDDITS_PREFS_KEY = "subreddits_prefs_key";
    private final SharedPreferences mSpref;
    public static final String GLOBAL_SHARED_PREF_NAME = "your_app_global_shared_pref";
    public static final String SHARED_PREF_NAME = "reddit_shared_pref";


    //public static final String KEY_GCM_REGISTRATION_ID="gcm_reg_id";

    public void save(String key, String value) {
        SharedPreferences.Editor editor = mSpref.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public void save(String key, boolean value) {
        SharedPreferences.Editor editor = mSpref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void save(String key, float value) {
        SharedPreferences.Editor editor = mSpref.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public void save(String key, int value) {
        SharedPreferences.Editor editor = mSpref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void save(String key, long value) {
        SharedPreferences.Editor editor = mSpref.edit();
        editor.putLong(key, value);
        editor.apply();
    }


    public String getStringValue(String key) {
        // if (mSpref.contains(key))
        return mSpref.getString(key, "");
       /* else
            Lg.e(TAG, "KEY NOT FOUND");

        return null;*/
    }

    public static SharedPreferences getDefaultSharedPrefernce(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    public static void clearDefaultPref(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
    }

    public boolean getBooleanValue(String key) {
        //  if (mSpref.contains(key))
        return mSpref.getBoolean(key, false);
        /*else
            Lg.e(TAG, "KEY NOT FOUND");

        return false;*/
    }

    public float getFloatValue(String key) {
        if (mSpref.contains(key))
            return mSpref.getFloat(key, 0f);
        else
            Log.e(TAG, "KEY NOT FOUND");

        return 0f;
    }

    public long getLongValue(String key) {
        if (mSpref.contains(key))
            return mSpref.getLong(key, 0L);
        else
            Log.e(TAG, "KEY NOT FOUND");

        return 0L;
    }

    public int getIntValue(String key) {
        if (mSpref.contains(key))
            return mSpref.getInt(key, 0);
        else
            Log.e(TAG, "KEY NOT FOUND");

        return 0;
    }

    public void clear() {
        mSpref.edit().clear().apply();
    }

    public void clearAll() {
        mSpref.edit().clear().apply();
        mContext.getSharedPreferences(GLOBAL_SHARED_PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }


    public PreferenceUtil(Context context) {
        mContext = context;
        mSpref = context.getSharedPreferences(
                SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPreferences() {
        return mSpref;
    }

    public boolean isUserAuthenticated(){
        return !getStringValue(AUTH_CODE).isEmpty();
    }

    public  void saveSubreddits(List<Subreddit> subreddits) {
        Gson gson = new Gson();
       save(PreferenceUtil.SUBREDDITS_PREFS_KEY,gson.toJson(subreddits));

    }

    public String getTokenIfNotExpired(){
        if (!(((System.currentTimeMillis() / 1000) - getLongValue(ACCESS_TOKEN_UPDATED_AT)) >= getLongValue(ACCESS_TOKEN_EXPIRE_IN)))
            return getStringValue(ACCESS_TOKEN);
        else{
            EventBus.getDefault().post(new AccessTokenExpiredEvent());
            return null;
        }


    }



}

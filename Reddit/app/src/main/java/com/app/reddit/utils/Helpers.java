package com.app.reddit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Helpers {
    private static final String SHARED_PREF_NAME = "reditt_share_pref";

    public static Map<String, String> parseUrlQueryParams(String url) {
        url = url.substring(url.indexOf("?") + 1, url.length());
        String[] split = url.split("&");

        Map<String, String> params = new HashMap<>();
        for (String item : split) {
            String[] split2 = item.split("=");
            params.put(split2[0], split2[1]);
        }

        return params;
    }

    public static void writeToPrefs(Context context, String key, String value) {
        SharedPreferences prefs = getSharedPref(context);
        prefs.edit().putString(key, value).apply();
    }

    public static String readFromPrefs(Context context, String key) {
        SharedPreferences prefs = getSharedPref(context);
        return prefs.getString(key, null);
    }

    public static void clearPrefs(Context context) {
        getSharedPref(context).edit().clear().apply();
    }

    public static void hideSoftKeyboard(Context context, IBinder windowToken) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
    }

    public static String humanizeTimestamp(String timestampSeconds) {
        Date createdAt = new Date(Double.valueOf(timestampSeconds).longValue() * 1000);
        long elapsed = System.currentTimeMillis() - createdAt.getTime();

        long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsed);
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
        long diffHours = TimeUnit.MILLISECONDS.toHours(elapsed);
        long diffDays = TimeUnit.MILLISECONDS.toDays(elapsed);
        long diffWeeks = diffDays / 7;

        if (diffWeeks > 0) {
            return diffWeeks + "w";
        } else if (diffDays > 0) {
            return diffDays + "d";
        } else if (diffHours > 0) {
            return diffHours + "h";
        } else if (diffMinutes > 0) {
            return diffMinutes + "m";
        } else {
            return diffSeconds + "s";
        }
    }

    public static float convertDpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static CharSequence trimTrailingWhitespace(CharSequence text) {
        while (text.charAt(text.length() - 1) == '\n') {
            text = text.subSequence(0, text.length() - 1);
        }

        return text;
    }

    private static SharedPreferences getSharedPref(Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

}

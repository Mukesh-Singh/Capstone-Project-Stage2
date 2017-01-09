package com.app.reddit.utils;

import android.widget.Toast;

import com.app.reddit.base.App;

/**
 * Created by mukesh on 28/12/16.
 */

public class AppUtils {
    public static String getFirstLetterCapsString(String string){
        if (string==null || string.isEmpty())
            return string;

        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static void showToastShort(String message){
        Toast.makeText(App.getAppInstance().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showToastLong(String message){
        Toast.makeText(App.getAppInstance().getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}

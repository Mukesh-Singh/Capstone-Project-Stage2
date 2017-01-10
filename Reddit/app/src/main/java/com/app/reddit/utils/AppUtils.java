package com.app.reddit.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.app.reddit.R;
import com.app.reddit.base.App;
import com.app.reddit.base.AppConstants;
import com.app.reddit.interfaces.AuthenticationListener;

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

    public static void showAuthDialog(final Activity activity, final AuthenticationListener listener){
        final PreferenceUtil mPref=new PreferenceUtil(activity);
        final Dialog auth_dialog = new Dialog(activity);
        auth_dialog.setContentView(R.layout.auth_dialog);
        WebView web = (WebView) auth_dialog.findViewById(R.id.webv);
        web.getSettings().setJavaScriptEnabled(true);
        String url = AppConstants.OAUTH_URL + "?client_id=" + AppConstants.CLIENT_ID + "&response_type=code&state=" + AppConstants.STATE + "&redirect_uri=" + AppConstants.REDIRECT_URI + "&scope=" + AppConstants.OAUTH_SCOPE+"&duration="+AppConstants.DURATION;
        web.loadUrl(url);


        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.contains("?code=") || url.contains("&code=")) {

                    Uri uri = Uri.parse(url);
                    String authCode = uri.getQueryParameter("code");
                    Log.i("", "CODE : " + authCode);
                    mPref.save(PreferenceUtil.AUTH_CODE, authCode);
                    auth_dialog.dismiss();
                    if (listener!=null)
                        listener.onSuccess(authCode);


                } else if (url.contains("error=access_denied")) {
                    Log.i("", "ACCESS_DENIED_HERE");
                    auth_dialog.dismiss();
                    if (listener!=null)
                        listener.onFailure(activity.getString(R.string.access_denied));

                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Authorize");
        auth_dialog.setCancelable(true);
    }


}

package com.app.reddit.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.reddit.R;
import com.app.reddit.api.Callback;
import com.app.reddit.api.RedditRestClient;
import com.app.reddit.base.AppConstants;
import com.app.reddit.utils.AppUtils;
import com.app.reddit.utils.PreferenceUtil;

import org.json.JSONException;

public class SplashActivity extends AppCompatActivity {

    private WebView web;
    private Button auth;
    private Dialog auth_dialog;
    private String DEVICE_ID = /*UUID.randomUUID().toString()*/"a390739c-95ca-43ed-bd18-c69d40853639";
    private String authCode;
    private boolean authComplete = false;
    private PreferenceUtil mPref;

    private Intent resultIntent = new Intent();
    private TextView textViewAppName;
    private AnimatorSet set1;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mPref = new PreferenceUtil(this);
        textViewAppName = (TextView) findViewById(R.id.lbl_app_name);
        progressBar = (ProgressBar) findViewById(R.id.splash_progress);
        progressBar.setVisibility(View.GONE);
        initAnimation();
        auth = (Button) findViewById(R.id.loginButton);

        authCode = mPref.getStringValue(PreferenceUtil.AUTH_CODE);
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                auth_dialog = new Dialog(SplashActivity.this);
                auth_dialog.setContentView(R.layout.auth_dialog);
                web = (WebView) auth_dialog.findViewById(R.id.webv);
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
                            authCode = uri.getQueryParameter("code");
                            Log.i("", "CODE : " + authCode);
                            authComplete = true;
                            resultIntent.putExtra("code", authCode);
                            SplashActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                            setResult(Activity.RESULT_CANCELED, resultIntent);
                            mPref.save(PreferenceUtil.AUTH_CODE, authCode);
                            auth_dialog.dismiss();
                            getAccessToken();


                        } else if (url.contains("error=access_denied")) {
                            Log.i("", "ACCESS_DENIED_HERE");
                            resultIntent.putExtra("code", authCode);
                            authComplete = true;
                            setResult(Activity.RESULT_CANCELED, resultIntent);
                            AppUtils.showToastShort(getString(R.string.error));
                            auth_dialog.dismiss();
                        }
                    }
                });
                auth_dialog.show();
                auth_dialog.setTitle("Authorize");
                auth_dialog.setCancelable(true);

            }
        });

        if (authCode == null || authCode.isEmpty()) {
            auth.setVisibility(View.VISIBLE);
        } else {
            getAccessToken();
        }


    }

    private void getAccessToken() {
        try {
            auth.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            new RedditRestClient(getApplicationContext()).getToken(AppConstants.TOKEN_URL, AppConstants.GRANT_TYPE2, DEVICE_ID, getCallBackForToken());
        } catch (JSONException e) {
            e.printStackTrace();
            AppUtils.showToastShort(getString(R.string.error));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        set1.start();


    }


    private Callback getCallBackForToken() {
        return new Callback() {
            @Override
            public void onSuccess(Object data) {
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onFailure(String message) {
                progressBar.setVisibility(View.GONE);
                auth.setVisibility(View.VISIBLE);
                AppUtils.showToastShort(message);
            }
        };
    }

    private void initAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(textViewAppName, "scaleX", 0.50f, 1.50f);
        //scaleX.setRepeatMode(ValueAnimator.REVERSE);
        //scaleX.setRepeatCount(5);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(textViewAppName, "scaleY", 0.50f, 1.50f);
        //scaleY.setRepeatMode(ValueAnimator.REVERSE);
        //scaleY.setRepeatCount(5);

        set1 = new AnimatorSet();
        set1.playTogether(scaleX, scaleY);
        set1.setInterpolator(new DecelerateInterpolator());
        set1.setDuration(3000);

    }


}



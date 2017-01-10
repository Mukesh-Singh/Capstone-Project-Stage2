package com.app.reddit.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.app.reddit.R;
import com.app.reddit.api.Callback;
import com.app.reddit.api.RedditRestClient;
import com.app.reddit.base.AppConstants;
import com.app.reddit.events.AccessTokenExpiredEvent;
import com.app.reddit.interfaces.AuthenticationListener;
import com.app.reddit.utils.AppUtils;
import com.app.reddit.utils.PreferenceUtil;

import org.json.JSONException;

import java.util.UUID;

import de.greenrobot.event.EventBus;

public class SplashActivity extends AppCompatActivity {

    private Button auth;
    private String DEVICE_ID = UUID.randomUUID().toString();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceUtil mPref = new PreferenceUtil(this);
        progressBar = (ProgressBar) findViewById(R.id.splash_progress);
        progressBar.setVisibility(View.GONE);
        //initAnimation();
        auth = (Button) findViewById(R.id.loginButton);

        String authCode = mPref.getStringValue(PreferenceUtil.AUTH_CODE);
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AppUtils.showAuthDialog(SplashActivity.this, new AuthenticationListener() {
                    @Override
                    public void onSuccess(String authCode) {
                        getAccessToken();
                    }

                    @Override
                    public void onFailure(String message) {
                        progressBar.setVisibility(View.GONE);
                        auth.setVisibility(View.VISIBLE);
                    }
                });
            }
        });


        if (mPref.getTokenIfNotExpired()!=null && !mPref.getTokenIfNotExpired().isEmpty()) {
            if (authCode == null || authCode.isEmpty()) {
                auth.setVisibility(View.VISIBLE);
            } else {
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                finish();
            }

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
        EventBus.getDefault().register(this);

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




    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(AccessTokenExpiredEvent event) {
        progressBar.setVisibility(View.GONE);
        auth.setVisibility(View.VISIBLE);
    }


}



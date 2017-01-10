package com.app.reddit.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.reddit.R;
import com.app.reddit.adapter.SubredditPagerAdapter;
import com.app.reddit.api.Callback;
import com.app.reddit.api.RedditRestClient;
import com.app.reddit.asyntask.LoadSubredditsAsynTask;
import com.app.reddit.base.AppConstants;
import com.app.reddit.events.AccessTokenExpiredEvent;
import com.app.reddit.events.HideContentViewerEvent;
import com.app.reddit.events.ShareContentEvent;
import com.app.reddit.events.SubredditPreferencesUpdatedEvent;
import com.app.reddit.events.ViewCommentsEvent;
import com.app.reddit.events.ViewContentEvent;
import com.app.reddit.events.ViewSubredditPostsEvent;
import com.app.reddit.interfaces.AuthenticationListener;
import com.app.reddit.models.Subreddit;
import com.app.reddit.ui.fragments.CommentsFragment;
import com.app.reddit.ui.fragments.ContentViewerFragment;
import com.app.reddit.ui.fragments.ManageSubredditsFragment;
import com.app.reddit.ui.fragments.PostsFragment;
import com.app.reddit.utils.AppUtils;
import com.app.reddit.utils.PreferenceUtil;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.app.reddit.base.AppConstants.ADMOB_APP_ID;
import static com.app.reddit.base.AppConstants.SORT_BUNDLE_KEY;
import static com.app.reddit.base.AppConstants.SUBREDDIT_BUNDLE_KEY;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar appBar;
    private TabLayout subredditTabs;
    private ViewPager viewPager;
    private SubredditPagerAdapter subredditPagerAdapter;
    private String subreddit;
    private String sort;
    private ProgressBar progressBar;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(getApplicationContext(),ADMOB_APP_ID);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Analytics instance
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        /**
         * Find views
         */

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        subredditTabs = (TabLayout) findViewById(R.id.subreddit_tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        /**
         * Restore saved values from instance state. If not available, set default vaules.
         */

        if (savedInstanceState == null) {
            subreddit = getResources().getString(R.string.front_page);
            sort = getResources().getString(R.string.action_sort_hot);
        } else {
            subreddit = savedInstanceState.getString(SUBREDDIT_BUNDLE_KEY);
            sort = savedInstanceState.getString(SORT_BUNDLE_KEY);
        }

        /**
         * Setup AppBar
         */

        setSupportActionBar(toolbar);
        appBar = getSupportActionBar();
        updateAppBarTitlesWithPostInfo();

        /**
         * Setup progress dialog which will be displayed while network operations are being performed
         */

        progressBar= (ProgressBar) findViewById(R.id.main_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        PreferenceUtil mPre=new PreferenceUtil(this);
        Log.e("username",mPre.getStringValue(PreferenceUtil.USER_NAME));
        loadSubreddits();

    }



    private void loadSubreddits(){
        new RedditRestClient(this).getSubreddits(new Callback<List<Subreddit>>() {

            @Override
            public void onSuccess(List<Subreddit> data) {
                progressBar.setVisibility(View.GONE);
                saveRedditListLocally(data);



            }

            @Override
            public void onFailure(String message) {
                progressBar.setVisibility(View.GONE);
                if (message != null)
                    AppUtils.showToastShort(message);
            }
        });
    }

    private void saveRedditListLocally(final List<Subreddit> data){
        new LoadSubredditsAsynTask(this,progressBar, data, new LoadSubredditsAsynTask.SubredditSavedLocallyCallback() {
            @Override
            public void onSave(boolean isSaved) {
                if (isSaved)
                    setupViewPagerAndTabs(data);
            }
        }).execute();

    }




    private void setupViewPagerAndTabs(final List<Subreddit> subreddits) {
        // keep selected subreddits only
        final List<Subreddit> selectedSubreddits = new ArrayList<Subreddit>();
        for (Subreddit subreddit : subreddits) {
            if (subreddit.isSelected()) {
                selectedSubreddits.add(subreddit);

                // Logs the subreddit using Analytics
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, subreddit.getId());
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, subreddit.getName());
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        }

        // remove any existing onPageChange listeners in order to prevent multiple listeners from being attached
        viewPager.clearOnPageChangeListeners();

        // setup view pager
        subredditPagerAdapter = new SubredditPagerAdapter(getSupportFragmentManager(),selectedSubreddits,sort);
        viewPager.setAdapter(subredditPagerAdapter);
        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                subreddit = selectedSubreddits.get(position).getName();
                updateAppBarTitlesWithPostInfo();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.addOnPageChangeListener(onPageChangeListener);
        // bind view pager to tabs
        subredditTabs.setupWithViewPager(viewPager);

        // select active tab
        for (int i = 0; i < subredditTabs.getTabCount(); i++) {
            TabLayout.Tab tab = subredditTabs.getTabAt(i);
            if (tab.getText().toString().equals(subreddit)) {
                tab.select();
                return;
            }
        }

        // if no active tab found, set first tab as active
        subredditTabs.getTabAt(0).select();
        viewPager.setCurrentItem(0);
        onPageChangeListener.onPageSelected(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onEventMainThread(ViewContentEvent event) {
        if (event.getUrl().contains("youtube.com") || event.getUrl().contains("youtu.be")) {
            Intent viewIntent = new Intent();
            viewIntent.setAction(Intent.ACTION_VIEW);
            viewIntent.setData(Uri.parse(event.getUrl()));

            if (viewIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(viewIntent);
                return;
            }
        }

        ContentViewerFragment contentViewerFragment =
                (ContentViewerFragment) getSupportFragmentManager().findFragmentByTag(ContentViewerFragment.TAG);

        if (contentViewerFragment == null) {
            contentViewerFragment = ContentViewerFragment.newInstance(event.getContentTitle(), event.getUrl());
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, 0)
                    .add(android.R.id.content, contentViewerFragment, ContentViewerFragment.TAG)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, 0)
                    .show(contentViewerFragment)
                    .commit();
            contentViewerFragment.loadContent(event.getContentTitle(), event.getUrl());
            if (contentViewerFragment.getView()!=null)
                contentViewerFragment.getView().bringToFront();
        }
    }

    public void onEventMainThread(HideContentViewerEvent event) {
        ContentViewerFragment contentViewerFragment =
                (ContentViewerFragment) getSupportFragmentManager().findFragmentByTag(ContentViewerFragment.TAG);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(0, R.anim.fade_out)
                .hide(contentViewerFragment)
                .commit();
    }

    public void onEventMainThread(SubredditPreferencesUpdatedEvent event) {
        setupViewPagerAndTabs(event.getSubreddits());
        updateAppBarTitlesWithPostInfo();
    }

    public void onEventMainThread(ViewCommentsEvent event) {
        CommentsFragment commentsFragment =
                CommentsFragment.newInstance(event.getSelectedPost());
        FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
        fTransaction.setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out);
        fTransaction.add(android.R.id.content, commentsFragment);
        fTransaction.addToBackStack(null);
        fTransaction.commit();
    }


    public void onEventMainThread(ViewSubredditPostsEvent event) {
        FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
        fTransaction.setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out);
        fTransaction.add(android.R.id.content,
                PostsFragment.newInstance(event.getSubreddit(), event.getSort(), true));
        fTransaction.addToBackStack(null);
        fTransaction.commit();
    }

    public void onEventMainThread(ShareContentEvent event) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, event.getContent());
        sendIntent.setType(event.getMimeType());
        startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.label_share_via)));
    }

    public void onEventMainThread(AccessTokenExpiredEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(getString(R.string.auth_reqired))
                .setMessage(getString(R.string.auth_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.lbl_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        AppUtils.showAuthDialog(HomeActivity.this, new AuthenticationListener() {
                            @Override
                            public void onSuccess(String authCode) {
                                getAccessToken();
                            }

                            @Override
                            public void onFailure(String message) {
                                AppUtils.showToastShort(getString(R.string.error));
                                finish();
                            }
                        });
                    }
                });
        builder.show();
    }





    private void getAccessToken() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            new RedditRestClient(getApplicationContext()).getToken(AppConstants.TOKEN_URL, AppConstants.GRANT_TYPE2, "", getCallBackForToken());
        } catch (JSONException e) {
            e.printStackTrace();
            AppUtils.showToastShort(getString(R.string.error));
            finish();
        }
    }

    private Callback getCallBackForToken() {
        return new Callback() {
            @Override
            public void onSuccess(Object data) {
                AppUtils.showToastShort(getString(R.string.access_token_refreshed));
            }

            @Override
            public void onFailure(String message) {
                AppUtils.showToastShort(message);
                finish();
            }
        };
    }


    private void updateAppBarTitlesWithPostInfo() {
        appBar.setTitle(AppUtils.getFirstLetterCapsString(subreddit));
        appBar.setSubtitle(AppUtils.getFirstLetterCapsString(sort));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SUBREDDIT_BUNDLE_KEY, subreddit);
        outState.putString(SORT_BUNDLE_KEY, sort);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // called every time the menu is about to be shown
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort_hot || id == R.id.action_sort_new || id == R.id.action_sort_rising ||
                id == R.id.action_sort_controversial || id == R.id.action_sort_top) {
            sort = item.getTitle().toString();
            updateAppBarTitlesWithPostInfo();
            if (getCurrentFragment()!=null)
                getCurrentFragment().updateSort(sort);
            return true;
        } else if (id == R.id.action_go_to_subreddit) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_go_tp_subreddit, null);
            final EditText subredditEditText = (EditText) dialogView.findViewById(R.id.subreddit_edittext);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.action_go_to_subreddit)
                    .setView(dialogView)
                    .setPositiveButton(R.string.action_go, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String subredditName = subredditEditText.getText().toString();
                            if (TextUtils.isEmpty(subredditName)) {
                                Toast.makeText(HomeActivity.this, R.string.error_subreddit_name_required, Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                EventBus.getDefault().post(new ViewSubredditPostsEvent(subredditEditText.getText().toString(),
                                        getResources().getString(R.string.action_sort_hot)));
                            }
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .create();
            dialog.show();
        } else if (id == R.id.action_manage_subreddits) {
            FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
            fTransaction.setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out);
            fTransaction.add(android.R.id.content, new ManageSubredditsFragment());
            fTransaction.addToBackStack(null);
            fTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // if back button is pressed while content viewer fragment is visible, delegate call to its onBackPressed method
        // else, pop back stack (if possible)
        ContentViewerFragment contentViewerFragment =
                (ContentViewerFragment) getSupportFragmentManager().findFragmentByTag(ContentViewerFragment.TAG);
        if (contentViewerFragment != null && contentViewerFragment.isVisible()) {
            contentViewerFragment.onBackPressed();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public PostsFragment getCurrentFragment() {
        if (subredditPagerAdapter==null)
            return null;
        return (PostsFragment) subredditPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
    }
}

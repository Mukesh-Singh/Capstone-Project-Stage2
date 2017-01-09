package com.app.reddit.base;

/**
 * Created by mukesh on 13/12/16.
 */

public class AppConstants {
    public static final String USER_AGENT = "myRedditapp/0.1 by redditusername";
    public static String CLIENT_ID = ""/*Add your client id*/;
    public static String CLIENT_SECRET ="";
    public static String REDIRECT_URI="http://myreddit.com";
    public static String GRANT_TYPE="https://oauth.reddit.com/grants/installed_client";
    public static String GRANT_TYPE2="authorization_code";
    public static String TOKEN_URL ="access_token";
    public static String OAUTH_URL ="https://www.reddit.com/api/v1/authorize";
    public static String OAUTH_SCOPE="identity,mysubreddits,read,vote";
    public static String DURATION = "permanent";
    public static final String STATE = "my_reddit";
    public static final String UNAUTH_API_BASE = "https://www.reddit.com";
    public static final String AUTH_API_BASE = "https://oauth.reddit.com";


    public static final String BASE_URL = "https://www.reddit.com/api/v1/";

    
    public static final String ADMOB_APP_ID="ca-app-pub-3272459377808812~6691862481";
    public static final String SUBREDDIT_BUNDLE_KEY = "subreddit_key";
    public static final String SORT_BUNDLE_KEY = "sort_key";
    
    
}

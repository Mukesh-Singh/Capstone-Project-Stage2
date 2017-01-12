package com.app.reddit.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.app.reddit.R;
import com.app.reddit.base.App;
import com.app.reddit.base.AppConstants;
import com.app.reddit.models.Comment;
import com.app.reddit.models.Post;
import com.app.reddit.models.Subreddit;
import com.app.reddit.utils.PreferenceUtil;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**

 */
@SuppressWarnings({"deprecation"})
public class RedditRestClient {
    private static final String TAG = RedditRestClient.class.getName();
    String token;
    final Context context;
    private final PreferenceUtil mPref;

    public RedditRestClient(Context cnt){
        mPref=new PreferenceUtil(cnt);
        context = cnt;
    }
    private static final AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return AppConstants.BASE_URL + relativeUrl;
    }

    public void getToken(String relativeUrl, String grant_type, String device_id, final Callback callback) throws JSONException {
        client.setBasicAuth(AppConstants.CLIENT_ID,AppConstants.CLIENT_SECRET);
        String code =mPref.getStringValue(PreferenceUtil.AUTH_CODE);

        RequestParams requestParams = new RequestParams();
        requestParams.put("code",code);
        requestParams.put("grant_type",grant_type);
        requestParams.put("redirect_uri", AppConstants.REDIRECT_URI);

        post(relativeUrl, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("response",response.toString());
                try {
                    token = response.getString("access_token").toString();
                    final long expiresIn = response.getLong("expires_in");
                    mPref.save(PreferenceUtil.ACCESS_TOKEN,token);
                    mPref.save(PreferenceUtil.ACCESS_TOKEN_EXPIRE_IN,expiresIn);
                    mPref.save(PreferenceUtil.ACCESS_TOKEN_UPDATED_AT,System.currentTimeMillis() / 1000);
                    Log.i("Access_token",token);
                    //getUsername(callback);
                    if (callback!=null)
                        callback.onSuccess(response);
                }catch (JSONException j)
                {
                    if (callback!=null)
                        callback.onFailure(context.getString(R.string.error));
                    j.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.i("statusCode", "" + statusCode);
                if (callback!=null)
                    callback.onFailure(throwable.getMessage());

            }
        });

    }






    public void revokeToken()
    {
        if (mPref.getTokenIfNotExpired()==null || mPref.getTokenIfNotExpired().isEmpty())
            return;

        client.setBasicAuth(AppConstants.CLIENT_ID,AppConstants.CLIENT_SECRET);

        String access_token = mPref.getStringValue(PreferenceUtil.ACCESS_TOKEN);

        RequestParams requestParams = new RequestParams();
        requestParams.put("token",access_token);
        requestParams.put("token_type_hint","access_token");

        post("revoke_token",requestParams,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("response", response.toString());
                mPref.save(PreferenceUtil.ACCESS_TOKEN,"");

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.i("statusCode", "" + statusCode);
            }
        });
    }
	
	public void getUsername(final Callback callback ) {
        if (mPref.getTokenIfNotExpired()==null || mPref.getTokenIfNotExpired().isEmpty())
            return;

        Header[] headers = new Header[2];
        headers[0] = new BasicHeader("User-Agent", AppConstants.USER_AGENT);
        headers[1] = new BasicHeader("Authorization", "bearer " + mPref.getStringValue(PreferenceUtil.ACCESS_TOKEN));

        client.get(context, "https://oauth.reddit.com/api/v1/me.json", headers, null, new JsonHttpResponseHandler() {
            
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                  Log.i("response", response.toString());
                try {
                    String username = response.getString("name");
                    Log.e("UserName",username);
                    mPref.save(PreferenceUtil.USER_NAME,username);
                    if (callback!=null)
                        callback.onSuccess(response);
                } catch (JSONException j) {
                    j.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("response", errorResponse.toString());
                Log.i("statusCode", "" + statusCode);
                if (callback!=null)
                    callback.onFailure(throwable.getMessage());
            }
        });
    }

    public  void getSubreddits(final Callback<List<Subreddit>> callback) {
        if (mPref.getTokenIfNotExpired()==null || mPref.getTokenIfNotExpired().isEmpty())
            return;

        Map<String, String> urls = new HashMap<>();
        urls.put("defaults", "/subreddits/default.json?limit=100");
        urls.put("user", "/reddits/mine.json?limit=100");

        // check if requested subreddits are already cached or not
        // if yes, simply return them, else send request to server and then cache them
        Gson gson = new Gson();
        String subredditsString = mPref.getStringValue(PreferenceUtil.SUBREDDITS_PREFS_KEY);
        if (subredditsString != null && !subredditsString.isEmpty()) {
            List<Subreddit> subreddits = Arrays.asList(gson.fromJson(subredditsString, Subreddit[].class));
            if (callback != null) callback.onSuccess(subreddits);
        } else {
            String subredditsUrl;
            if (mPref.isUserAuthenticated()) {
                subredditsUrl = AppConstants.AUTH_API_BASE + urls.get("user");
            } else {
                subredditsUrl = AppConstants.UNAUTH_API_BASE + urls.get("defaults");
            }

            Header[] headers = new Header[2];
            headers[0] = new BasicHeader("User-Agent", AppConstants.USER_AGENT);
            headers[1] = new BasicHeader("Authorization", "bearer " + mPref.getStringValue(PreferenceUtil.ACCESS_TOKEN));
            client.get(context,subredditsUrl,headers,null,new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        JSONArray children = response.getJSONObject("data").getJSONArray("children");

                        List<Subreddit> subreddits = new ArrayList<>();

                        // add Front Page as first tab
                        subreddits.add(new Subreddit(null,
                                App.getAppInstance().getAppContext().getResources().getString(R.string.front_page), true));

                        for (int i = 0; i < children.length(); i++) {
                            JSONObject subredditData = children.getJSONObject(i).getJSONObject("data");

                            Subreddit subreddit = new Subreddit(subredditData.getString("id"),
                                    subredditData.getString("display_name"), true);

                            subreddits.add(subreddit);

                            mPref.saveSubreddits(subreddits);
                        }

                        if (callback != null) callback.onSuccess(subreddits);
                    } catch (JSONException e) {
                        if (callback != null)
                            callback.onFailure(App.getAppInstance().getAppContext().getResources().getString(R.string.error_parsing));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (callback != null) callback.onFailure(String.valueOf(statusCode));
                }
            });

        }
    }


    public  void vote(String itemFullName, int voteDir, final Callback<Void> callback) {
        if (mPref.getTokenIfNotExpired()==null || mPref.getTokenIfNotExpired().isEmpty())
            return;

        if (itemFullName == null) throw new IllegalArgumentException("'itemId' cannot be null");

        RequestParams params = new RequestParams();
        params.put("id", itemFullName);
        params.put("dir", voteDir);

        if (mPref.isUserAuthenticated()) {
            client.setUserAgent(AppConstants.USER_AGENT);
            client.addHeader("Authorization", "bearer " + mPref.getStringValue(PreferenceUtil.ACCESS_TOKEN));
            client.post(AppConstants.AUTH_API_BASE + "/api/vote", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.e("vote","success: "+response.toString());
                    if (callback != null) callback.onSuccess(null);

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e("vote","Failure: "+throwable.getMessage());
                    if (callback != null) callback.onFailure(throwable.getMessage());
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, String string, Throwable throwable) {
                    Log.e("Vote:","onFailure: "+statusCode+" "+throwable.toString());
                    if (callback != null) callback.onFailure(string);
                }
            });
        } else {
            if (callback != null) {
                String message=App.getAppInstance().getAppContext().getResources().getString(R.string.error_not_authorized);
                Log.e("vote","Failure: "+message);
                callback.onFailure(message);
            }
        }
    }

    public  void getPosts(String subreddit, String sort, String after, final Callback<List<Post>> callback) {
        if (mPref.getTokenIfNotExpired()==null || mPref.getTokenIfNotExpired().isEmpty())
            return;

        sort = sort.toLowerCase();

        // build posts url
        String postsUrl = mPref.isUserAuthenticated() ? AppConstants.AUTH_API_BASE : AppConstants.UNAUTH_API_BASE;
        if (subreddit.equals(App.getAppInstance().getAppContext().getResources().getString(R.string.front_page))) {
            postsUrl += "/" + sort + ".json";
        } else {
            postsUrl += "/r/" + subreddit + "/" + sort + ".json";
        }
        if (after != null) {
            postsUrl += "?after=" + after;
        }

        client.setUserAgent(AppConstants.USER_AGENT);
        client.addHeader("Authorization", "bearer " + mPref.getStringValue(PreferenceUtil.ACCESS_TOKEN));

        client.get(postsUrl, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("data");

                    String after = data.getString("after");
                    Log.i(TAG, after);
                    JSONArray children = data.getJSONArray("children");

                    List<Post> posts = new ArrayList<>();

                    for (int i=0; i<children.length(); i++) {
                        JSONObject postData = children.getJSONObject(i).getJSONObject("data");

                        Post post = new Post();
                        post.setId(postData.getString("id"));
                        post.setFullName(postData.getString("name"));
                        post.setLikes(!postData.isNull("likes") ? postData.getBoolean("likes") : null);
                        post.setDomain(postData.getString("domain"));
                        post.setSubreddit(postData.getString("subreddit"));
                        post.setAuthor(postData.getString("author"));
                        post.setScore(postData.getInt("score"));
                        post.setCreated(postData.getString("created_utc"));
                        post.setNsfw(postData.getBoolean("over_18"));
                        post.setThumbnail(postData.getString("thumbnail"));
                        post.setUrl(postData.getString("url"));
                        post.setTitle(postData.getString("title"));
                        post.setNumComments(postData.getInt("num_comments"));
                        post.setPermalink(postData.getString("permalink"));
                        post.setIsSelf(postData.getBoolean("is_self"));
                        post.setSelftext(postData.getString("selftext_html"));

                        if (i == children.length()-1) {
                            post.setAfter(after.equals("null") ? null : after);
                        } else {
                            post.setAfter(null);
                        }

                        posts.add(post);
                    }

                    if (callback != null) callback.onSuccess(posts);
                } catch (JSONException e) {
                    if (callback != null)
                        callback.onFailure(App.getAppInstance().getAppContext().getResources().getString(R.string.error_parsing));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) callback.onFailure(String.valueOf(statusCode));
            }
        });
    }

    public void getComments(String subreddit, String postId, String sort, final Callback<List<Comment>> callback) {
        if (mPref.getTokenIfNotExpired()==null || mPref.getTokenIfNotExpired().isEmpty())
            return;

        sort = sort.toLowerCase();

        // build comments URL
        String commentsUrl = mPref.isUserAuthenticated() ? AppConstants.AUTH_API_BASE : AppConstants.UNAUTH_API_BASE;
        commentsUrl += "/r/" + subreddit + "/comments/" + postId + "/.json?sort=" + sort;

        client.setUserAgent(AppConstants.USER_AGENT);
        client.addHeader("Authorization", "bearer " + mPref.getStringValue(PreferenceUtil.ACCESS_TOKEN));

        client.get(commentsUrl, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                List<Comment> comments = new ArrayList<>();

                try {
                    JSONArray children = jsonArray.getJSONObject(1).getJSONObject("data").getJSONArray("children");
                    for (int i = 0; i < children.length(); i++) {
                        parseComments(comments, children.getJSONObject(i), 0);
                    }
                } catch (JSONException e) {
                    if (callback != null)
                        callback.onFailure(App.getAppInstance().getAppContext().getString(R.string.error_parsing));
                }

                if (callback != null) callback.onSuccess(comments);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (callback != null) callback.onFailure(String.valueOf(statusCode));
            }
        });
    }

    private  void parseComments(List<Comment> comments, JSONObject thread, int level) throws JSONException {
        if (thread.getString("kind").equals("t1")) {
            JSONObject commentData = thread.getJSONObject("data");

            Comment comment = new Comment();
            comment.setFullName(commentData.getString("name"));
            comment.setBody(commentData.getString("body_html"));
            comment.setLikes(!commentData.isNull("likes") ? commentData.getBoolean("likes") : null);
            comment.setAuthor(commentData.getString("author"));
            comment.setScore(commentData.getInt("score"));
            comment.setCreated(commentData.getString("created_utc"));
            comment.setLevel(level);

            comments.add(comment);

            if (!TextUtils.isEmpty(commentData.getString("replies"))) {
                level++;
                JSONArray children = commentData.getJSONObject("replies").getJSONObject("data").getJSONArray("children");
                for (int i = 0; i < children.length(); i++) {
                    parseComments(comments, children.getJSONObject(i), level);
                }
            }
        }
    }



}

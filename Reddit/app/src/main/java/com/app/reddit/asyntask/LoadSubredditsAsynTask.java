package com.app.reddit.asyntask;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.app.reddit.models.Subreddit;
import com.app.reddit.provider.RedditProvider;

import java.util.List;

/**
 * Created by mukesh on 19/12/16.
 */

public class LoadSubredditsAsynTask extends AsyncTask<Void,Void, Boolean >{
    private final Context context;
    private final List<Subreddit> data;
    private final ProgressBar progressBar;
    private final SubredditSavedLocallyCallback callback;

    public interface SubredditSavedLocallyCallback{
        void onSave(boolean isSaved);
    }


    public LoadSubredditsAsynTask(Context context,ProgressBar progressBar,List<Subreddit> data,SubredditSavedLocallyCallback callback){
        this.context=context;
        this.progressBar=progressBar;
        this.data=data;
        this.callback=callback;
    }

    @Override
    protected void onPreExecute() {
        try {
            if (progressBar != null)
                progressBar.setVisibility(View.VISIBLE);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        super.onPreExecute();
    }

    private void loadSubreddits(){
        context.getContentResolver().delete(RedditProvider.CONTENT_URI, null, null);
        ContentValues cv = new ContentValues();
        for (Subreddit subr : data) {
            if (subr.isSelected()) {
                cv.put("subreddit_name", subr.getName());
                context.getContentResolver().insert(RedditProvider.CONTENT_URI, cv);
            }
        }


//        Cursor resultCursor = context.getContentResolver().query(RedditProvider.CONTENT_URI, null,
//                "@", null, null);
//        resultCursor.moveToFirst();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try{
            loadSubreddits();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean subreddits) {
        super.onPostExecute(subreddits);
        try {
            if (progressBar != null)
                progressBar.setVisibility(View.GONE);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if (callback!=null)
            callback.onSave(subreddits);

    }
}

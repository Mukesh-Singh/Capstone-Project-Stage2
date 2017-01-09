package com.app.reddit.events;

public class ViewSubredditPostsEvent {

    private String subreddit;
    private String sort;

    public ViewSubredditPostsEvent(String subreddit, String sort) {
        this.subreddit = subreddit;
        this.sort = sort;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getSort() {
        return sort;
    }
}

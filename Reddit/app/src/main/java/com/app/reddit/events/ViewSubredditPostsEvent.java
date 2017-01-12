package com.app.reddit.events;

public class ViewSubredditPostsEvent {

    private final String subreddit;
    private final String sort;

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

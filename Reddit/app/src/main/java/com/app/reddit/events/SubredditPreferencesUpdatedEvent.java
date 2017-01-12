package com.app.reddit.events;


import com.app.reddit.models.Subreddit;

import java.util.List;

public class SubredditPreferencesUpdatedEvent {

    private final List<Subreddit> subreddits;

    public SubredditPreferencesUpdatedEvent(List<Subreddit> subreddits) {
        this.subreddits = subreddits;
    }

    public List<Subreddit> getSubreddits() {
        return subreddits;
    }
}

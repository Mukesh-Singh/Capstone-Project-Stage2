package com.app.reddit.events;


import com.app.reddit.models.User;

public class AuthenticatedEvent {

    private final User user;

    public AuthenticatedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}

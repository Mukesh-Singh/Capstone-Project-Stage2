package com.app.reddit.events;

public class OauthCallbackEvent {

    private final String code;
    private final String state;
    private final String error;

    public OauthCallbackEvent(String code, String state, String error) {
        this.code = code;
        this.state = state;
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public String getState() {
        return state;
    }

    public String getError() {
        return error;
    }
}


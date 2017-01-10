package com.app.reddit.interfaces;

/**
 * Created by mukesh on 10/1/17.
 */

public interface AuthenticationListener {
    void onSuccess(String authCode);
    void onFailure(String message);
}

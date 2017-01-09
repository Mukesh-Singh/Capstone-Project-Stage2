package com.app.reddit.api;

public interface Callback<T> {
    void onSuccess(T data);

    void onFailure(String message);
}

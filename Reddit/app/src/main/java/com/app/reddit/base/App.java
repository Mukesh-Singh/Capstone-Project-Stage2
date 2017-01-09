package com.app.reddit.base;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private static App app;
    @Override
    public void onCreate() {
        super.onCreate();
        app=this;

    }

    public Context getAppContext() {
        return this.getApplicationContext();
    }

    public static App  getAppInstance(){
        return app;
    }
}

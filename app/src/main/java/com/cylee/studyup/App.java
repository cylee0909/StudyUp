package com.cylee.studyup;

import android.app.Application;

public class App extends Application {
    static Application application;
    public static Application getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }
}

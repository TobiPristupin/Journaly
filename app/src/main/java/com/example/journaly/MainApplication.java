package com.example.journaly;

import android.app.Application;

import es.dmoral.toasty.Toasty;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Toasty.Config.getInstance().allowQueue(true).apply();
    }
}

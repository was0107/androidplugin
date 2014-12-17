package com.example.fragmentloader;

import android.app.Application;

/**
 * Created by boguang on 14/12/16.
 */
public class MyApplication extends Application {

    private static  MyApplication application  = null;
    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static Application instance() {
        return  application;
    }
}

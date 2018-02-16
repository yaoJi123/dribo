package com.yao.dribo;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * initialization for the Fresco
 */

public class DriboApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
package com.shinelon.rxjavalearn;

import android.app.Application;

/**
 * Created by Shinelon on 2018/8/15.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        Utils.init(this);
        com.blankj.utilcode.util.Utils.init(this);
        super.onCreate();
    }
}

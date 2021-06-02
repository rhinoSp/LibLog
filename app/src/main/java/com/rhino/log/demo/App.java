package com.rhino.log.demo;

import android.app.Application;

import com.rhino.log.LogUtils;
import com.rhino.log.crash.CrashHandlerUtils;

/**
 * @author LuoLin
 * @since Create on 2021/6/2
 **/
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.init(this, true, true);
        CrashHandlerUtils.getInstance().init(this, new CrashHandler());
    }
}

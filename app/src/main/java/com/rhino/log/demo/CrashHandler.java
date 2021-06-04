package com.rhino.log.demo;

import android.content.Context;

import androidx.annotation.Nullable;

import com.rhino.log.crash.CrashTipsActivity;
import com.rhino.log.crash.DefaultCrashHandler;

/**
 * @author LuoLin
 * @since Create on 2021/6/2
 **/
public class CrashHandler extends DefaultCrashHandler {

    @Override
    public Class<?> getRestartActivity() {
        return MainActivity.class;
    }

    @Override
    public void onCrashCaught(Context context, @Nullable String debugFilePath, @Nullable String debugText) {
        CrashTipsActivity.startThis(context, this, debugFilePath, debugText);
    }

}

package com.rhino.log.crash;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rhino.log.LogUtils;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author LuoLin
 * @since Create on 2018/10/10.
 */
public class DefaultCrashHandler implements Serializable {


    @NonNull
    public String getDebugDirectory(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return new File(context.getExternalFilesDir(null).getParent(), "log").getAbsolutePath();
        } else {
            return new File(context.getCacheDir(), context.getPackageName()).getAbsolutePath();
        }
    }

    @NonNull
    public String getDebugFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        return "log_crash_" + sdf.format(new Date(System.currentTimeMillis())) + ".txt";
    }

    public void onCrashCaught(Context context, @Nullable String debugFilePath, @Nullable String debugText) {
        CrashService.startThisService(context, this, debugFilePath, debugText);
    }

    public void onCrashServerCreate() {
        LogUtils.i("onCrashServerCreate");
    }

    public void onCrashServerStart(Context context, @Nullable String debugFilePath, @Nullable String debugText) {
        LogUtils.i("onCrashServerStart: debugFilePath = " + debugFilePath);
        Intent intent = new Intent(context, CrashTipsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(CrashService.KEY_CRASH_HANDLE, this);
        intent.putExtra(CrashService.KEY_DEBUG_TEXT, debugText);
        context.startActivity(intent);
    }

    public void onCrashServerDestroy() {
        LogUtils.i("onCrashServerDestroy");
    }

    public Class<?> getRestartActivity() {
        return null;
    }

    public void uncaughtException(Throwable ex) {

    }
}

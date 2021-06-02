package com.rhino.log.crash;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rhino.log.LogUtils;

/**
 * @author LuoLin
 * @since Create on 2016/10/31.
 **/
public class CrashService extends Service {

    public static final String KEY_CRASH_HANDLE = "Crash.Handler";
    public static final String KEY_DEBUG_FILE_PATH = "Crash.debug.file.path";
    public static final String KEY_DEBUG_TEXT = "Crash.debug.text";
    private static final String TAG = CrashService.class.getName();

    private DefaultCrashHandler mICrashHandler;
    private String mFilePath;
    private String mDebugText;

    public static void startThisService(Context context, @NonNull DefaultCrashHandler crashHandler, String filePath, String debugText) {
        LogUtils.i(TAG, "startThisService called");
        Intent intent = new Intent(context, CrashService.class);
        intent.putExtra(KEY_CRASH_HANDLE, crashHandler);
        intent.putExtra(KEY_DEBUG_FILE_PATH, filePath);
        intent.putExtra(KEY_DEBUG_TEXT, debugText);
        context.startService(intent);
        LogUtils.i(TAG, "startThisService end");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i(TAG, "onCreate");
        if (null != mICrashHandler) {
            mICrashHandler.onCrashServerCreate();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG, "onStartCommand");
        if (intent != null) {
            mICrashHandler = (DefaultCrashHandler) intent.getSerializableExtra(KEY_CRASH_HANDLE);
            mFilePath = intent.getStringExtra(KEY_DEBUG_FILE_PATH);
            mDebugText = intent.getStringExtra(KEY_DEBUG_TEXT);
            if (null != mICrashHandler) {
                mICrashHandler.onCrashServerStart(getApplicationContext(), mFilePath, mDebugText);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i(TAG, "onDestroy");
        if (null != mICrashHandler) {
            mICrashHandler.onCrashServerDestroy();
        }
    }
}
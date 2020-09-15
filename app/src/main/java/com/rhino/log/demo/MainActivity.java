package com.rhino.log.demo;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.rhino.log.LogUtils;
import com.rhino.log.crash.CrashHandlerUtils;
import com.rhino.log.crash.DefaultCrashHandler;


public class MainActivity extends AppCompatActivity {

    public static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.init(this, true, true);
        CrashHandlerUtils.getInstance().init(this, new DefaultCrashHandler());

        ActivityCompat.requestPermissions(this, PERMISSIONS, 11);

        LogUtils.d("xxxxxxxxx");
        LogUtils.d(TAG, "xxxxxxxxx");
        LogUtils.i("xxxxxxxxx");
        LogUtils.i(TAG, "xxxxxxxxx");
        LogUtils.w("xxxxxxxxx");
        LogUtils.w(TAG, "xxxxxxxxx");
        LogUtils.e("xxxxxxxxx");
        LogUtils.e(TAG, "xxxxxxxxx");
        LogUtils.e(TAG, "xxxxxxxxx", null);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int i = 1/0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        LogUtils.d("xxxxxxxxx");

    }


}

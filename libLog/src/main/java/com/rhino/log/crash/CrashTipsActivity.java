package com.rhino.log.crash;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.rhino.log.R;


/**
 * @author LuoLin
 * @since Create on 2018/10/8.
 */
public final class CrashTipsActivity extends Activity implements View.OnClickListener {

    private DefaultCrashHandler mICrashHandler;
    private String mFilePath;
    private String mDebugText;
    private Class<?> mRestartActivity;

    public static void startThis(Context context, @NonNull DefaultCrashHandler crashHandler, String filePath, String debugText) {
        Intent intent = new Intent(context, CrashTipsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(CrashService.KEY_CRASH_HANDLE, crashHandler);
        intent.putExtra(CrashService.KEY_DEBUG_FILE_PATH, filePath);
        intent.putExtra(CrashService.KEY_DEBUG_TEXT, debugText);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mICrashHandler = (DefaultCrashHandler) getIntent().getSerializableExtra(CrashService.KEY_CRASH_HANDLE);
        mFilePath = getIntent().getStringExtra(CrashService.KEY_DEBUG_FILE_PATH);
        mDebugText = getIntent().getStringExtra(CrashService.KEY_DEBUG_TEXT);
        setContentView(R.layout.activity_crash_tips);
        findViewById(R.id.error_activity_restart_button).setOnClickListener(this);
        findViewById(R.id.error_activity_more_info_button).setOnClickListener(this);
        mRestartActivity = mICrashHandler.getRestartActivity();
        if (mRestartActivity == null) {
            ((Button) findViewById(R.id.error_activity_restart_button)).setText("关闭程序");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.error_activity_restart_button) {
            if (mRestartActivity != null) {
                Intent intent = new Intent(this, mRestartActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            finish();
            CrashHandlerUtils.killCurrentProcess();
        } else if (id == R.id.error_activity_more_info_button) {
            AlertDialog dialog = new AlertDialog.Builder(CrashTipsActivity.this)
                    .setTitle("错误详情")
                    .setMessage(mDebugText)
                    .setPositiveButton("关闭", null)
                    .setNeutralButton("复制到剪贴板", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            copyErrorToClipboard();
                            Toast.makeText(CrashTipsActivity.this, "复制到剪贴板", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        }
    }

    private void copyErrorToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("错误信息", mDebugText);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

}

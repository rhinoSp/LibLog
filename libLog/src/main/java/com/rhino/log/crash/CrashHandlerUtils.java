package com.rhino.log.crash;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.rhino.log.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * <p>
 * The handler about UncaughtException, will write the error info to file,
 * need the follow permission.
 * <p>"android.permission.WRITE_EXTERNAL_STORAGE"</p>
 * <p>"android.permission.MOUNT_UNMOUNT_FILESYSTEMS"</p>
 * </p>
 *
 * @author LuoLin
 * @since Create on 2016/10/31.
 **/
public class CrashHandlerUtils implements UncaughtExceptionHandler {

    /**
     * The log tag.
     */
    private static final String TAG = CrashHandlerUtils.class.getSimpleName();
    /**
     * The default handler of UncaughtException.
     */
    private UncaughtExceptionHandler mDefaultHandler;
    /**
     * The context.
     */
    private Context mContext;
    /**
     * The ICrashHandler.
     */
    private DefaultCrashHandler mICrashHandler;
    /**
     * The prefix of log msg.
     */
    private String mPrefixOfLogMsg = "";
    /**
     * SharedPreferences
     */
    private SharedPreferences mSharedPreferences;

    /**
     * CrashHandlerUtils
     */
    private static CrashHandlerUtils instance;

    public static CrashHandlerUtils getInstance() {
        if (instance == null) {
            instance = new CrashHandlerUtils();
        }
        return instance;
    }

    public CrashHandlerUtils() {
    }

    public void init(Context context, @NonNull DefaultCrashHandler crashHandler) {
        this.mContext = context.getApplicationContext();
        this.mICrashHandler = crashHandler;
        this.mPrefixOfLogMsg = context.getPackageName();
        this.mSharedPreferences = mContext.getSharedPreferences("share_preferences_log", Context.MODE_PRIVATE);
        if (crashAlways()) {
            // crash always
            log("init():crash always, do not init, deal by system");
            return;
        }
        // get the default handler of UncaughtException
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // set this CrashHandlerUtils to the default handler of UncaughtException
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * Set prefix of log msg
     */
    public void setPrefixOfLogMsg(String prefix) {
        mPrefixOfLogMsg = prefix;
    }

    /**
     * Called when UncaughtException happen
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //If the user does not handle the exception handler to allow the
            //system to handle the default
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            if (mICrashHandler != null) {
                mICrashHandler.uncaughtException(ex);
            }
            killCurrentProcess();
        }
    }

    /**
     * Collecting device parameter information and save error information to file
     *
     * @param ex the throwable
     * @return true:handled; false:not handled
     */
    private boolean handleException(Throwable ex) {
        // save error information to file
        String debugText = saveCrashInfo2File(ex);
        if (!TextUtils.isEmpty(debugText)) {
            log("handleException():crash always flag: " + crashAlways());
            Toast.makeText(mContext, "程序发生异常，即将退出！", Toast.LENGTH_LONG).show();
            if (!crashAlways()) {
                log("handleException():start service for error activity");
                saveLastCrashTimestamp(System.currentTimeMillis());
                CrashService.startThisService(mContext, mICrashHandler, debugText);
            } else {
                log("handleException():kill current process");
                saveLastCrashTimestamp(System.currentTimeMillis());
                killCurrentProcess();
            }
            return true;
        } else {
            log(ex == null ? "Throwable is null" : ex.toString());
        }
        return false;
    }

    /**
     * Save error information to file
     *
     * @param ex the throwable
     * @return the debug text
     */
    @Nullable
    private String saveCrashInfo2File(Throwable ex) {
        if (ex == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss",
                Locale.getDefault());
        sb.append("DATE=").append(sdf.format(new Date(System.currentTimeMillis()))).append("\n");

        String filePath = getFilePath();
        if (TextUtils.isEmpty(filePath)) {
            log("File path is null");
            return null;
        }
        log("filePath = " + filePath);
        sb.append("FILE_PATH=").append(filePath).append("\n");

        ApplicationInfo info = mContext.getApplicationInfo();
        boolean isDebugVersion = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        sb.append("IS_DEBUG_VERSION=").append(isDebugVersion).append("\n");

        String packageName = mContext.getPackageName();
        sb.append("PACKAGE_NAME=").append(packageName).append("\n");
        sb.append("APP_NAME=").append(mContext.getString(R.string.app_name)).append("\n");

        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                sb.append("VERSION_NAME=").append(pi.versionName).append("\n");
                sb.append("VERSION_CODE=").append(pi.versionCode).append("\n");
            }
        } catch (NameNotFoundException e) {
            log("Error when collect package info" + e.toString());
        }
        appendClassFields(sb, Build.class);
        appendClassFields(sb, Build.VERSION.class);

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        log(result);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filePath);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            log("Error when write file." + e.toString());
        }
        return sb.toString();
    }

    /**
     * 打印日志
     */
    private void log(String msg) {
        StringBuilder text = new StringBuilder();
        text.append(mPrefixOfLogMsg)
                .append("->")
                .append(msg);
        Log.e(TAG, text.toString());
    }

    /**
     * Append fields of class.
     *
     * @param sb  StringBuilder
     * @param cls Class<?>
     */
    private void appendClassFields(StringBuilder sb, Class<?> cls) {
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value.getClass().isArray()) {
                    Object[] arr = (Object[]) value;
                    sb.append(field.getName()).append("=").append(Arrays.toString(arr)).append("\n");
                    log(field.getName() + " : " + Arrays.toString(arr));
                } else {
                    sb.append(field.getName()).append("=").append(value.toString()).append("\n");
                    log(field.getName() + " : " + value.toString());
                }
            } catch (Exception e) {
                log("Error when collect crash info" + e.toString());
            }
        }
    }

    /**
     * Get the debug file path
     *
     * @return the debug file path
     */
    @Nullable
    private String getFilePath() {
        String dir = mICrashHandler.getDebugDirectory(mContext);
        if (!createDir(dir)) {
            return null;
        }

        String filePath = dir + File.separator + mICrashHandler.getDebugFileName();
        if (!createFile(filePath)) {
            return null;
        }
        return filePath;
    }

    /**
     * Create directory
     *
     * @param dirPath the directory path
     * @return true success, false failed
     */
    private boolean createDir(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return false;
        }
        File file = new File(dirPath);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    /**
     * Create file
     *
     * @param filePath the file path
     * @return true success, false failed
     */
    private boolean createFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                log("filePath" + filePath + e.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * INTERNAL method that kills the current process.
     * It is used after restarting or killing the app.
     */
    public static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    /**
     * crash always
     */
    private boolean crashAlways() {
        return getLastCrashTimestamp() + 3000 > System.currentTimeMillis();
    }

    /**
     * save last crash time
     */
    public void saveLastCrashTimestamp(long value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        if (edit != null) {
            edit.putLong("lastCrashTimestamp", value);
            edit.commit();
        }
    }

    /**
     * get last crash time
     */
    public long getLastCrashTimestamp() {
        return mSharedPreferences.getLong("lastCrashTimestamp", 0L);
    }


}

package com.rhino.log;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * <p>
 * The log tool support write log to file, need the follow permission.
 * <p>"android.permission.WRITE_EXTERNAL_STORAGE"</p>
 * <p>"android.permission.MOUNT_UNMOUNT_FILESYSTEMS"</p>
 * </p>
 *
 * @author LuoLin
 * @since Create on 2016/10/31.
 **/
public class LogUtils {

    private static final String TAG = "LogUtils";

    /**
     * None log enable
     */
    public static final int LOG_FLAG_NONE = 0;
    /**
     * All log enable
     */
    public static final int LOG_FLAG_ALL = 15;
    /**
     * Debug log enable
     */
    public static final int LOG_FLAG_DEBUG = 1;
    /**
     * Info log enable
     */
    public static final int LOG_FLAG_INFO = 2;
    /**
     * Warm log enable
     */
    public static final int LOG_FLAG_WARM = 4;
    /**
     * Error log enable
     */
    public static final int LOG_FLAG_ERROR = 8;
    /**
     * The enable flag of log.
     */
    private static int mLogEnableFlag = 0;
    /**
     * The write enable flag of log.
     */
    private static int mWriteLogEnableFlag = 0;
    /**
     * The prefix of log file name.
     */
    private static String mPrefixOfLogFileName = "";
    /**
     * The prefix of log msg.
     */
    private static String mPrefixOfLogMsg = "";
    /**
     * The log dir parent path.
     */
    private static File mWriteLogDir = null;
    /**
     * The log dir.
     */
    private static final String DEFAULT_LOG_FILE_PATH = "log";
    /**
     * The format for date.
     */
    private static SimpleDateFormat mDateFormat = null;
    /**
     * The FileOutputStream for write log.
     */
    private static FileOutputStream mFileOutputStream = null;

    public static void init(Context context, boolean debug, boolean writeFile) {
        mLogEnableFlag = debug ? LOG_FLAG_ALL : LOG_FLAG_NONE;
        mWriteLogEnableFlag = writeFile ? LOG_FLAG_ALL : LOG_FLAG_NONE;
        mPrefixOfLogMsg = context.getPackageName();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mWriteLogDir = new File(context.getExternalFilesDir(null).getParent());
        } else {
            mWriteLogDir = new File(context.getCacheDir(), context.getPackageName());
        }
        mWriteLogDir = new File(mWriteLogDir, DEFAULT_LOG_FILE_PATH);
    }

    public static void setLogEnableFlag(int flag) {
        mLogEnableFlag = flag;
    }

    public static void setWriteLogEnableFlag(int flag) {
        mWriteLogEnableFlag = flag;
    }

    public static void setWriteLogDir(String dirPath) {
        mWriteLogDir = new File(dirPath);
    }

    public static void setPrefixOfLogFileName(String prefix) {
        mPrefixOfLogFileName = prefix;
    }

    public static void setPrefixOfLogMsg(String prefix) {
        mPrefixOfLogMsg = prefix;
    }

    public static void endCurrentLogFileWrite() {
        mFileOutputStream = null;
    }

    public static int getWriteLogFileFlag() {
        return mWriteLogEnableFlag;
    }

    public static void d(String msg) {
        if ((mLogEnableFlag & LOG_FLAG_DEBUG) == LOG_FLAG_DEBUG)
            log("d", TAG, buildMessage(msg), null);
    }

    public static void d(String tag, String msg) {
        if ((mLogEnableFlag & LOG_FLAG_DEBUG) == LOG_FLAG_DEBUG)
            log("d", tag, buildMessage(msg), null);
    }

    public static void i(String msg) {
        if ((mLogEnableFlag & LOG_FLAG_INFO) == LOG_FLAG_INFO)
            log("i", TAG, buildMessage(msg), null);
    }

    public static void i(String tag, String msg) {
        if ((mLogEnableFlag & LOG_FLAG_INFO) == LOG_FLAG_INFO)
            log("i", tag, buildMessage(msg), null);
    }

    public static void w(String msg) {
        if ((mLogEnableFlag & LOG_FLAG_WARM) == LOG_FLAG_WARM)
            log("w", TAG, buildMessage(msg), null);
    }

    public static void w(String tag, String msg) {
        if ((mLogEnableFlag & LOG_FLAG_WARM) == LOG_FLAG_WARM)
            log("w", tag, buildMessage(msg), null);
    }

    public static void e(String msg) {
        if ((mLogEnableFlag & LOG_FLAG_ERROR) == LOG_FLAG_ERROR)
            log("e", TAG, buildMessage(msg), null);
    }

    public static void e(Exception e) {
        if ((mLogEnableFlag & LOG_FLAG_ERROR) == LOG_FLAG_ERROR)
            log("e", TAG, buildMessage(e != null ? e.toString() : "Exception is null"), e);
    }

    public static void e(String msg, Exception e) {
        if ((mLogEnableFlag & LOG_FLAG_ERROR) == LOG_FLAG_ERROR)
            log("e", TAG, buildMessage(msg + ":" + (e != null ? e.toString() : "Exception is null")), e);
    }

    public static void e(String tag, String msg) {
        if ((mLogEnableFlag & LOG_FLAG_ERROR) == LOG_FLAG_ERROR)
            log("e", tag, buildMessage(msg), null);
    }

    public static void e(String tag, String msg, Exception e) {
        if ((mLogEnableFlag & LOG_FLAG_ERROR) == LOG_FLAG_ERROR)
            log("e", tag, buildMessage(msg + ":" + (e != null ? e.toString() : "Exception is null")), e);
    }

    private static void log(String level, String tag, String text, Exception e) {
        if ("d" .equals(level)) {
            android.util.Log.d(tag, text);
            checkWriteLogToFile(text, (mWriteLogEnableFlag & LOG_FLAG_DEBUG) == LOG_FLAG_DEBUG);
        } else if ("i" .equals(level)) {
            android.util.Log.i(tag, text);
            checkWriteLogToFile(text, (mWriteLogEnableFlag & LOG_FLAG_INFO) == LOG_FLAG_INFO);
        } else if ("w" .equals(level)) {
            android.util.Log.w(tag, text);
            checkWriteLogToFile(text, (mWriteLogEnableFlag & LOG_FLAG_WARM) == LOG_FLAG_WARM);
        } else if ("e" .equals(level)) {
            android.util.Log.e(tag, text, e);
            checkWriteLogToFile(text, (mWriteLogEnableFlag & LOG_FLAG_ERROR) == LOG_FLAG_ERROR);
        }
    }

    private static String buildMessage(String msg) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[4];
        StringBuilder text = new StringBuilder();
        text.append(mPrefixOfLogMsg)
                .append("->")
                .append("[").append(Thread.currentThread().getName()).append("]:")
                .append(caller.getFileName().replace(".java", "").replace(".kt", ""))
                .append(".")
                .append(caller.getMethodName())
                .append("[")
                .append(caller.getLineNumber())
                .append("]:")
                .append(msg);
        return text.toString();
    }

    private static void checkWriteLogToFile(String text, boolean writeEnable) {
        if (writeEnable) writeLog2File(text);
    }

    private static void writeLog2File(String text) {
        try {
            if (null == mFileOutputStream) {
                mDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
                String prefix = mPrefixOfLogFileName;
                if (TextUtils.isEmpty(prefix)) {
                    prefix = "log";
                }
                File file = new File(mWriteLogDir, prefix + "_" + mDateFormat.format(new Date()) + ".txt");
                if (!file.exists()) {
                    File parentFile = file.getParentFile();
                    if (null != parentFile && !parentFile.exists() && !parentFile.mkdirs()) {
                        // has parent dir, but make failed.
                        android.util.Log.e(TAG, "mkdirs is " + false);
                        return;
                    }
                    if (!file.exists()) {
                        if (!file.createNewFile()) {
                            // has not file, but create failed.
                            android.util.Log.e(TAG, "createNewFile is " + false);
                            return;
                        }
                    }
                }
                mFileOutputStream = new FileOutputStream(file, true);
                StringBuilder sb = new StringBuilder();
                DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                sb.append("DATE=").append(sdf.format(new Date(System.currentTimeMillis()))).append("\n");
                sb.append("FILE_PATH=").append(file.getAbsolutePath()).append("\n");
                mFileOutputStream.write(sb.toString().getBytes());
            }
            String log = mDateFormat.format(new Date()) + ":(" + TAG + ")" + " >> " + text + "\n";
            mFileOutputStream.write(log.getBytes());
            mFileOutputStream.flush();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Write error: " + e.toString());
        }
    }
}

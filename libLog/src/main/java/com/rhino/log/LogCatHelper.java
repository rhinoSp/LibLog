package com.rhino.log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

/**
 * @author rhino
 * @since Create on 2021/3/16.
 **/
public class LogCatHelper {

    private String logDirPath;
    private Thread logThread;
    private LogCollectRunnable logCollectRunnable;

    private static class Build {
        private static LogCatHelper instance = new LogCatHelper();
    }

    public static LogCatHelper getInstance() {
        return Build.instance;
    }

    private LogCatHelper() {
    }

    public LogCatHelper setLogDirPath(String logDirPath) {
        this.logDirPath = logDirPath;
        return this;
    }

    /**
     * 启动log日志保存
     */
    public void start() {
        if (logThread == null || logCollectRunnable == null) {
            logCollectRunnable = new LogCollectRunnable(logDirPath);
            logThread = new Thread(logCollectRunnable);
        }
        logThread.start();
    }

    /**
     * 终止写日志
     */
    public void stop() {
        if (logCollectRunnable != null) {
            logCollectRunnable.stop();
        }
        if (logThread != null) {
            logThread.interrupt();
        }
        logCollectRunnable = null;
        logThread = null;
    }

    private static class LogCollectRunnable implements Runnable {

        private Process mProcess;
        private String logDirPath;
        private String logFileName;
        private FileOutputStream fileOutputStream;
        private BufferedReader mReader;
        private String cmd;
        private String mPid;
        private int fileCount = 0;
        private int fileMaxLineCount = 10000;
        private int writeLineCount = 0;
        private boolean run = true;

        private LogCollectRunnable(String logDirPath) {
            this.logDirPath = logDirPath;
            this.logFileName = "log_" + getFormatDate();
            this.mPid = "" + android.os.Process.myPid();
//            cmd = "logcat *:v | grep \"(" + mPid + ")\"";
//            cmd = "logcat *:e | grep \"(" + mPid + ")\"";
//            cmd = "logcat | grep \"(" + mPid + ")\"";//打印所有日志信息
//            cmd = "logcat -s way";//打印标签过滤信息
//            cmd = "logcat *:e *:i | grep \"(" + mPid + ")\"";

            this.cmd = "logcat ";
        }

        @Override
        public void run() {
            try {
                mProcess = Runtime.getRuntime().exec(cmd);
                mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()), 1024);
                String line;
                while (run && (line = mReader.readLine()) != null) {
                    if (line.length() == 0) {
                        continue;
                    }
                    try {
                        fileOutputStream = checkCreateFileOutputStream();
                        if (fileOutputStream != null) {
                            writeLineCount++;
                            fileOutputStream.write((line + "\n").getBytes());
                        }
                    } catch (Exception e) {
                        LogUtils.e(e);
                        writeLineCount = 0;
                        safeClose(fileOutputStream);
                    }
                }
            } catch (Exception e) {
                LogUtils.e(e);
                run = false;
            } finally {
                safeClose(mReader);
                safeClose(fileOutputStream);
            }
        }

        public void stop() {
            run = false;
            if (mProcess != null) {
                mProcess.destroy();
                mProcess = null;
            }
        }

        private FileOutputStream checkCreateFileOutputStream() {
            if (fileOutputStream == null || writeLineCount >= fileMaxLineCount) {
                safeClose(fileOutputStream);
                fileCount++;
                writeLineCount = 0;
                File logFile = new File(logDirPath, logFileName + "_" + fileCount + ".txt");
                fileOutputStream = createFileOutputStream(logFile);
            }
            return fileOutputStream;
        }
    }

    private static void safeClose(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
                closeable = null;
            }
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }

    public static FileOutputStream createFileOutputStream(File file) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            return new FileOutputStream(file, true);
        } catch (Exception e) {
            LogUtils.e(e);
        }
        return null;
    }

    public static String getFormatDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        return sdf.format(System.currentTimeMillis());
    }
}

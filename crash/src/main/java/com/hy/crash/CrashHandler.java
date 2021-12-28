package com.hy.crash;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.StringRes;

/**
 * Created time : 2018/11/21 9:56.
 *
 * @author HY
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (null == sInstance) {
            sInstance = new CrashHandler();
        }
        return sInstance;
    }

    private static CrashHandler sInstance;
    private static Application sApplication;
    private static CrashModule mCrashModule;

    private Calendar mCalendar = Calendar.getInstance();

    Class<? extends Activity> getLaunchActivity() {
        return mCrashModule.getLaunchActivity();
    }

    boolean debug() {
        return mCrashModule.debug();
    }

    private static final String DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os";

    public void install(Application app, CrashModule crashModule) {
        sApplication = app;
        mCrashModule = crashModule;
        Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (oldHandler == this) {
            Log.d("CrashHandler", "You have already installed crashManager, doing nothing!");
            return;
        }
        if (oldHandler != null && !oldHandler.getClass().getName().startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
            Log.d("CrashManager", "IMPORTANT WARNING! You already have an UncaughtExceptionHandler, are you sure this is correct? If you use ACRA, Crashlytics or similar libraries, you must initialize them AFTER crashmanager! Installing anyway, but your original handler will not be called.");
        }
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {

        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        Throwable cause = e.getCause();
        // 循环着把所有的异常信息写入writer中
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();// 记得关闭
        final String crashMsg = writer.toString();
        final String filePath;
        int year = mCalendar.get(Calendar.YEAR);
        String month = String.format(Locale.getDefault(), "%02d", mCalendar.get(Calendar.MONTH) + 1);
        String day = String.format(Locale.getDefault(), "%02d", mCalendar.get(Calendar.DATE));
        String path = year + File.separator + month + "-" + day + File.separator;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File filesDir = sApplication.getExternalFilesDir("crash");
            if (filesDir != null) {
                filePath = filesDir.getPath() + File.separator + path;
            } else {
                filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + "Android"
                        + File.separator + "data"
                        + File.separator + sApplication.getPackageName()
                        + File.separator + "files"
                        + File.separator + "crash"
                        + File.separator + path;
            }

        } else {
            filePath = sApplication.getFilesDir().getPath()
                    + File.separator + "crash"
                    + File.separator + path;
        }

        final String date = year + "-" + month + "-" + day;

        String hour = String.format(Locale.getDefault(), "%02d", mCalendar.get(Calendar.HOUR_OF_DAY));
        String minute = String.format(Locale.getDefault(), "%02d", mCalendar.get(Calendar.MINUTE));
        String second = String.format(Locale.getDefault(), "%02d", mCalendar.get(Calendar.SECOND));

        final String time = hour + ":" + minute + ":" + second;

        final String fileName = "crash-" + date + "-" + hour + "-" + minute + "-" + second + ".log";
        new Thread(() -> saveCrashInfo2File(filePath, crashMsg, fileName, date, time)).start();


        sApplication.startActivity(new Intent(sApplication, CrashErrorActivity.class)
                .putExtra("path", filePath)
                .putExtra("file", filePath + File.separator + fileName)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        android.os.Process.killProcess(android.os.Process.myPid());

    }

    private void saveCrashInfo2File(String filePath, String crashMsg, String fileName, String date, String time) {
        // 保存文件
        File dir = new File(filePath);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }

        File crashFile = new File(dir, fileName);

        StringBuilder sbl = new StringBuilder();
        sbl.append(getString(R.string.crash_str_device_model)).append(getDeviceModelName()).append("\n");
        sbl.append(getString(R.string.crash_str_time)).append(date)
                .append(" ").append(time)
                .append("\n");
        String pkgName = sApplication.getPackageName();

        try {
            PackageInfo packageInfo = sApplication.getPackageManager().getPackageInfo(pkgName, 0);
            sbl.append(getString(R.string.crash_str_version)).append(packageInfo.versionName).append("\n");
        } catch (PackageManager.NameNotFoundException ignore) {
            sbl.append(getString(R.string.crash_str_version)).append(getString(R.string.crash_str_unknown)).append("\n");
        }
        sbl.append(getString(R.string.crash_str_crash_log)).append("\n");
        sbl.append(crashMsg);
        try {
            if (!crashFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                crashFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(crashFile);
            fos.write(sbl.toString().getBytes());
            fos.close();
        } catch (Exception ignore) {

        }

    }

    private String getString(@StringRes int resId) {
        return sApplication.getString(resId);
    }

    void upload(String filePath) {
        mCrashModule.upload(filePath);
    }

    /**
     * INTERNAL method that returns the device model name with correct capitalization.
     * Taken from: http://stackoverflow.com/a/12707479/1254846
     *
     * @return The device model name (i.e., "LGE Nexus 5")
     */
    private static String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * INTERNAL method that capitalizes the first character of a string
     *
     * @param s The string to capitalize
     * @return The capitalized string
     */
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}

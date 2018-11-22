package com.hy.crash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created time : 2018/11/21 11:43.
 *
 * @author HY
 */
public class CommonUtils {
    /**
     * 改变状态栏字体颜色为黑色, 要求MIUI6以上
     *
     * @param lightStatusBar 为真时表示黑色字体
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void processMIUI(Activity activity, boolean lightStatusBar) {

        Window window = activity.getWindow();
        //针对安卓6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && lightStatusBar) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(activity, android.R.color.white));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        String brand = Build.BRAND;
        if ("Xiaomi".equalsIgnoreCase(brand)) {
            //针对小米
            Class clazz = window.getClass();
            try {
                int darkModeFlag;

                @SuppressLint("PrivateApi")
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");

                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");

                darkModeFlag = field.getInt(layoutParams);

                @SuppressWarnings("unchecked")
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);

                extraFlagField.invoke(window, lightStatusBar ? darkModeFlag : 0, darkModeFlag);

            } catch (Exception ignore) {
            }
        } else if ("Meizu".equalsIgnoreCase(brand)) {
            //针对魅族
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                value = lightStatusBar ? value | bit : value & bit;
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
            } catch (Exception ignore) {
            }

        }
    }

    /**
     * attrs status color or black
     */
    public static boolean getTypeValueBoolean(Context mContext, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = mContext.obtainStyledAttributes(typedValue.resourceId, attribute);
        boolean statusFont = array.getBoolean(0, false);
        array.recycle();
        return statusFont;
    }
}

package com.hy.crash;

import android.app.Activity;

/**
 * Created time : 2018/11/21 9:57.
 *
 * @author HY
 */
public interface CrashModule {


    Class<? extends Activity> getLaunchActivity();

    void upload(String filePath);

    boolean debug();
}

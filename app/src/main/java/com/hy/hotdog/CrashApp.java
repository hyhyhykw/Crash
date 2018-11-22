package com.hy.hotdog;

import android.app.Activity;
import android.app.Application;
import android.widget.Toast;

import com.hy.crash.CrashHandler;
import com.hy.crash.CrashModule;

/**
 * Created time : 2018/11/21 15:47.
 *
 * @author HY
 */
public class CrashApp extends Application implements CrashModule {
    //异常处理器
//    private CrashHandler crashHandler = null;


    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().install(this, this);
//        crashHandler = new CrashHandler(this);
//        Thread.setDefaultUncaughtExceptionHandler(crashHandler);

    }
//    class CrashHandler implements Thread.UncaughtExceptionHandler {
//
//        private Application app;
//
//        public CrashHandler(Application app) {
//            this.app = app;
//        }
//
//        @Override
//        public void uncaughtException(Thread thread, Throwable ex) {
//            ex.printStackTrace();
//            // 此处示例发生异常后，重新启动应用
//            Intent intent = new Intent(app, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            app.startActivity(intent);
//            android.os.Process.killProcess(android.os.Process.myPid());
//        }
//    }

    @Override
    public Class<? extends Activity> getLaunchActivity() {

        return MainActivity.class;
    }

    @Override
    public void upload(String filePath) {
        Toast.makeText(this, "点击了上传", Toast.LENGTH_SHORT).show();
    }
}

package com.hy.crash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created time : 2018/11/21 11:42.
 *
 * @author HY
 */
public class CrashErrorActivity extends AppCompatActivity {

    private String mPath;
    private String mFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.crash_error_activity);
        boolean isStatusBlack = CommonUtils.getTypeValueBoolean(this, R.attr.crash_light_toolbar);
        CommonUtils.processMIUI(this, isStatusBlack);

        Intent intent = getIntent();
        mPath = intent.getStringExtra("path");
        mFile = intent.getStringExtra("file");
    }

    public void view(View view) {
        startActivity(new Intent(this, CrashErrorLogListActivity.class)
                .putExtra("path", mPath)
                .putExtra("file", mFile));
    }

    public void restart(View view) {
        startActivity(new Intent(this, CrashHandler.getInstance().getLaunchActivity())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}

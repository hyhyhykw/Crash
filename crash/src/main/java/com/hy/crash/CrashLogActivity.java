package com.hy.crash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created time : 2018/11/21 15:31.
 *
 * @author HY
 */
public class CrashLogActivity extends AppCompatActivity {

    private String mFilePath;
    private TextView mTvContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_log_view);
        boolean isStatusBlack = CommonUtils.getTypeValueBoolean(this, R.attr.crash_light_toolbar);
        CommonUtils.processMIUI(this, isStatusBlack);
        Intent intent = getIntent();
        mFilePath = intent.getStringExtra("file");

        View mLytBack = findViewById(R.id.crash_lyt_back);
        TextView mTvTitle = findViewById(R.id.crash_tv_title);
        mTvContent = findViewById(R.id.crash_tv_log);
        View mLytUpload = findViewById(R.id.crash_lyt_upload);

        File file = new File(mFilePath);
        mTvTitle.setText(file.getName());

        mLytBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mLytUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrashHandler.getInstance().upload(mFilePath);
            }
        });

        initData();
    }

    private void initData() {
        new Thread() {
            @Override
            public void run() {
                final StringBuilder sbl = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mFilePath)));
                    String line = br.readLine();
                    while (null != line) {
                        sbl.append(line).append("\n");
                        line = br.readLine();
                    }
                } catch (IOException ignore) {
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvContent.setText(sbl.toString());
                    }
                });
            }
        }.start();
    }
}

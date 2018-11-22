package com.hy.hotdog;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = null;

    }

    public void click(View view) {
        Log.e("TAG", "" + text.length());
//        startActivity(new Intent(this,CrashErrorActivity.class));
    }
}

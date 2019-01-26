package com.hy.hotdog;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

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

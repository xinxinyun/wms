package com.anji.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.anji.R;
import com.anji.service.BackService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "仓储实时库存监听";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main2);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(MainActivity.this, BackService.class);
                startService(intent);
            }
        }).start();

    }
}

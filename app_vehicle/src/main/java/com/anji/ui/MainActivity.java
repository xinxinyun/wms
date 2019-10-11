package com.anji.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.anji.R;
import com.anji.service.DataService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "仓储实时库存监听";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(MainActivity.this, DataService.class);
                startService(intent);
            }
        }).start();

    }
}

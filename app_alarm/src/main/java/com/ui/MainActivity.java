package com.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.service.DataService;
import com.uhf.uhf.R;

public class MainActivity extends Activity {

    private static final String TAG = "门店报警监听";

    @Override
    protected void onCreate(Bundle savedInstanceState){

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

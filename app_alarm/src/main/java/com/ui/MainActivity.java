package com.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.hoho.android.usbserial.util.HexDump;
import com.uhf.uhf.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "仓储实时库存监听";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("仓库过库程序");

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent=new Intent(MainActivity.this, DataService.class);
//                startService(intent);
//            }
//        }).start();
        Log.d(TAG, HexDump.hexStringToByteArray("0110001A0001000FD8")+"------------------------->");
    }
}
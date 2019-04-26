package com.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.service.DataService;
import com.uhf.uhf.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "门店报警监听";

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("门店报警监听");

        MediaPlayer player=MediaPlayer.create(getBaseContext(),R.raw.jb);
        player.start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(MainActivity.this, DataService.class);
                startService(intent);
            }
        }).start();

    }
}

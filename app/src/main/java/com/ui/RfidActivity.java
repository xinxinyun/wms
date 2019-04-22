package com.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;

public class RfidActivity extends AppCompatActivity {

    ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper mReader;

    RXObserver rxObserver = new RXObserver(){
        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            Log.d("TAG",tag.strEPC);
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd endTag) {
            mReader.realTimeInventory((byte) 0xff,(byte)0x01);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (connector.connectCom("dev/ttyS4",115200)) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rxObserver);

                Thread.currentThread().sleep(500);

                mReader.realTimeInventory((byte) 0xff,(byte)0x01);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReader != null) {
            mReader.unRegisterObserver(rxObserver);
        }
        if (connector != null) {
            connector.disConnect();
        }

        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().release();
    }



}

package com.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.com.tools.Beeper;
import com.contants.WmsContanst;
import com.job.StorgeJob;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.UHFApplication;

import java.util.ArrayList;

public class DataService extends Service {

    private static final String TAG = "仓储库存监听";
    ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper mReader;

    private JobManager jobManager;

    /**
     * 缓存EPC码
     */
    private ArrayList<String> epcCodeList = new ArrayList<>();

    /**
     * RFID监听
     */
    RXObserver rxObserver = new RXObserver() {
        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            String epcCode = tag.strEPC;
            if (!epcCodeList.contains(epcCode)) {
                epcCodeList.add(epcCode);
                //添加识别码到消息队列。
                jobManager.addJobInBackground(new StorgeJob(epcCode));
                //调用蜂鸣声提示已扫描到商品
                Beeper.beep(Beeper.BEEPER_SHORT);
            }
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd endTag) {
            mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        jobManager = UHFApplication.getJobManager();
        startup();
        Log.v(TAG, "OnCreate 服务启动时调用");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shutdown();
        Log.v(TAG, "onDestroy 服务关闭时");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onDestroy 服务关闭时");
        shutdown();
        return super.onUnbind(intent);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onDestroy 服务关闭时");
        return null;
    }

    /**
     * 启动程序开始扫描
     */
    private void startup() {

        //实时扫描多少个物资
        if (!connector.connectCom(WmsContanst.TTYS1, WmsContanst.baud)) {
            return;
        }

        ModuleManager.newInstance().setUHFStatus(true);

        try {
            mReader = RFIDReaderHelper.getDefaultHelper();
            mReader.registerObserver(rxObserver);
            //设定读取间隔时间
            Thread.currentThread().sleep(500);
            mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
        } catch (Exception e) {
            Log.v(TAG, "RFID设备调取失败"+e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    private void shutdown() {
        //RFID模块下线
        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().release();
    }
}

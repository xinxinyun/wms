package com.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.contants.WmsContanst;
import com.job.StorgeJob;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.UHFApplication;

/**
 * 数据服务
 */
public class DataService extends Service {

    private static final String TAG = "门店警报监听";

    private JobManager jobManager;

    private RFIDReaderHelper rfidReaderHelper;

    private ModuleConnector connector;

    @Override
    public void onCreate() {
        super.onCreate();
        jobManager = UHFApplication.getJobManager();
        startRfidDevice();
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
    private void startRfidDevice() {

        ///初始化连接器
        connector = new ReaderConnector();

        //如果设备在连接状态，则直接退出
        if (connector.isConnected() ||
                    !connector.connectCom(WmsContanst.TTYMXC3, WmsContanst.BJQ_BAUD)) {
            return;
        }

        try {
            //FRID模块上电
            ModuleManager.newInstance().setUHFStatus(true);

            rfidReaderHelper = RFIDReaderHelper.getDefaultHelper();

            rfidReaderHelper.registerObserver(new RXObserver() {
                @Override
                protected void onInventoryTag(RXInventoryTag tag) {
                    //实时监听返回读取数据
                    String epcCode = tag.strEPC;
                    //加入任务队列
                    jobManager.addJobInBackground(new StorgeJob(epcCode));
                }

                @Override
                protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd endTag) {
                    rfidReaderHelper.realTimeInventory((byte) 0xff, (byte) 0x01);
                }
            });

            //设定读取间隔时间
            Thread.currentThread().sleep(1000);
            rfidReaderHelper.realTimeInventory((byte) 0xff, (byte) 0x01);
            //设置天线输出功率
            //设置四根天线的功率，默认为26
            rfidReaderHelper.setOutputPower((byte)0xff, (byte) 20,(byte) 20,(byte)0,(byte)0 );

        } catch (Exception e) {
            Log.v(TAG, "RFID设备调取失败" + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    /**
     * 关闭RFID设备
     */
    private void shutdown() {
        connector.disConnect();
        //RFID模块下线
        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().release();
    }
}

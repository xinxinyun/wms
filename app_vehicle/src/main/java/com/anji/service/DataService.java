package com.anji.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * @author 周宇
 * 数据服务
 */
public class DataService extends Service {

    private static final String TAG = "车辆盘点后台服务";

    @Override
    public void onCreate() {
        super.onCreate();
        RFIDManager rfidManager = new RFIDManager(this);
        try {
            rfidManager.startupRfidDevice();
        } catch (Exception e) {
            Log.v(TAG, "RFID设备调取失败" + e.getMessage());
            e.printStackTrace();
        }
        Log.v(TAG, "DataService服务启动----->");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "DataService服务关闭");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind 服务关闭时");
        return super.onUnbind(intent);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}

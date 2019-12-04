package com.anji.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.anji.util.LogUtil;

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
            rfidManager.startupRFIDDevice();
        } catch (Exception e) {
            LogUtil.e(TAG, "RFID设备调取失败" + e.getMessage());
            e.printStackTrace();
        }
        LogUtil.d(TAG, "DataService服务启动----->");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "DataService服务关闭");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "onUnbind 服务关闭时");
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

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

    private RFIDManager rfidManager;

    @Override
    public void onCreate() {
        super.onCreate();
        rfidManager = new RFIDManager(this);
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
        //如果设备管理器未关闭，则关闭设备管理器
        if(rfidManager!=null){
            //清空RFID码缓存
            rfidManager.clearEpcCache();
            rfidManager.shutdownRFIDevice();
        }
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

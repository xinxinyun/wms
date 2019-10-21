package com.anji.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * 版权：xx公司 版权所有
 *
 * @author 周宇
 * 版本：1.0
 * 创建日期：${date}${hour}
 * 描述：MainActivity
 */
public class BackService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        InitSocketThread initSocketThread=new InitSocketThread(this);
        initSocketThread.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent("com.anji.service.backService.destory");
        sendBroadcast(intent);
//        if (initSocketThread != null) {
//            initSocketThread.closeWebSocket();
//        }
    }

}

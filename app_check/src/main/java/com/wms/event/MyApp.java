package com.wms.event;

import android.app.Application;
import android.content.Context;

import com.wms.util.Beeper;
import com.wms.util.PreferenceUtil;

import realid.rfidlib.MyLib;


/**
 * author CYD
 * date 2018/11/19
 * email chengyd@idatachina.com
 */
public class MyApp extends Application {

    public static byte[] UHF = {0x01, 0x02, 0x03};
    private MyLib idataLib;
    private static MyApp myApp;
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        myApp = this;
        idataLib = new MyLib(this);
        mContext = getApplicationContext();
        PreferenceUtil.init(mContext);
        Beeper.init(mContext);
        //   MLog.ifShown = false;// 默认true开启日志调试，false关闭

    }

    public static MyApp getMyApp() {
        return myApp;
    }

    public MyLib getIdataLib() {
        return idataLib;
    }

}

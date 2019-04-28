package com.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.bean.ResultBean;
import com.birbit.android.jobqueue.JobManager;
import com.contants.WmsContanst;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.UHFApplication;
import com.util.CallBackUtil;
import com.util.OkhttpUtil;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;

/**
 * 数据服务
 */
public class DataService extends Service {

    private static final String TAG = "仓储库存监听";
    ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper mReader;

    private JobManager jobManager;

    /**
     * 缓存EPC码
     */
    private ArrayList<String> epcCodeList = new ArrayList<>();

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    String epcCode=(String)msg.obj;
                    submitInventory(epcCode);

            }
        }
    };
    /**
     * RFID监听
     */
    RXObserver rxObserver = new RXObserver() {

        @Override
        protected void onInventoryTag(RXInventoryTag tag) {
            String epcCode = tag.strEPC;
            //防止重复读取RFID信息
            if (!epcCodeList.contains(epcCode)) {

                Log.i(TAG,"------------------>"+epcCode);
                epcCodeList.add(epcCode);

                //jobManager.addJobInBackground(new StorgeJob(epcCode));
                //submitInventory(epcCode);
                //调用蜂鸣声提示已扫描到商品
               // Beeper.beep(Beeper.BEEPER_SHORT);

                Message message= Message.obtain();
                message.what=1;
                message.obj=epcCode;
                handler.sendMessage(message);
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

        //如果设备在连接状态，则直接退出
        if(connector.isConnected()){
            return;
        }

        //实时扫描多少个物资
        if (!connector.connectCom(WmsContanst.TTYMXC2, WmsContanst.baud)) {
            return;
        }

        ModuleManager.newInstance().setUHFStatus(true);

        try {
            mReader = RFIDReaderHelper.getDefaultHelper();
            mReader.registerObserver(rxObserver);
            //设定读取间隔时间
            Thread.currentThread().sleep(500);
            mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
            //设置工作天线频率
            //mReader.setOutputPower();
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

    private void submitInventory(final String epcCode) {

        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, Object> paramsMap = new HashMap<>();

        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);//头部信息
        paramsMap.put("token", "wms");

        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", "1");
        ArrayList<String> fridList=new ArrayList<>();
        fridList.add(epcCode);
        dataMap.put("list", fridList);

        paramsMap.put("data", dataMap);
        //Log.d(TAG, JSON.toJSONString(paramsMap));
        OkhttpUtil.okHttpPostJson(WmsContanst.STORGE_MATERIALINFL_INVENTORY_SUBMIT,
                JSON.toJSONString(paramsMap),headerMap, new CallBackUtil.CallBackString() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.d(TAG, e.getMessage());
                        Log.d(TAG, "[" + epcCode + "]仓储库出入库失败");
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            ResultBean resultBean = JSON.parseObject(response, ResultBean.class);
                            //提交成功后从当前缓存中移除EPC码
                            epcCodeList.remove(epcCode);
                            String respMsg = resultBean.getCode() == 0 ? "成功" : "失败";
                            Log.d(TAG, "[" + epcCode + "]仓储库出入库"+respMsg);
                        } catch (Exception e) {
                            Log.d(TAG, "[" + epcCode + "]仓储库出入库失败");
                            Log.d(TAG, e.toString());
                        }
                    }
                });
    }
}

package com.anji.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.anji.bean.ResultBean;
import com.anji.contants.VehicleContanst;
import com.anji.util.ASCUtil;
import com.anji.util.CallBackUtil;
import com.anji.util.OkhttpUtil;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 数据服务
 */
public class DataService extends Service {

    private static final String TAG = "车辆盘点后台服务";
    private ModuleConnector connector = new ReaderConnector();
    private RFIDReaderHelper mReader;

    /**
     * 缓存EPC码
     */
    private ArrayList<String> epcCodeList = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String epcCode = (String) msg.obj;
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
            //Log.i(TAG, "------------------>" + epcCode);
            //防止重复读取RFID信息
            if (!epcCodeList.contains(epcCode)) {

                Log.i(TAG, "------------------>" + epcCode);
                epcCodeList.add(epcCode);

                //调用蜂鸣声提示已扫描到商品
                //Beeper.beep(Beeper.BEEPER_SHORT);

                Message message = Message.obtain();
                message.what = 1;
                message.obj = epcCode;
                handler.sendMessage(message);
            }
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd endTag) {
            //当前工作天线
            int antId = endTag.mCurrentAnt;
            //Log.d(TAG, "当前工作天线------>[" + antId + "]");
            //动态切换，如果在1号天线工作，当前盘存结束后切换到2号天线
            mReader.setWorkAntenna((byte) 0xff, antId == 0 ? (byte) 0x01 : (byte) 0x00);
            try {
                //Thread.currentThread().sleep(60);
                Thread.sleep(60);
            } catch (Exception e) {
                Log.d(TAG, "设置天线失败");
            }
            //mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
            mReader.customizedSessionTargetInventory((byte) 0xff, (byte) 0x01, (byte) 0x00,
                    (byte) 0x01);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        startup();
        Log.v(TAG, "OnCreate 服务启动时调用");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent("com.anji.service.dataService.destory");
        sendBroadcast(intent);
        Log.v(TAG, "onDestroy 服务关闭时");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onDestroy 服务关闭时");
        return super.onUnbind(intent);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onDestroy 服务关闭时");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    /**
     * 启动程序开始扫描
     */
    private void startup() {

        //如果设备在连接状态，则直接退出
        if (connector.isConnected()) {
            return;
        }

        //连接RFID设备
        if (!connector.connectCom(VehicleContanst.TTYMXC2, VehicleContanst.baud)) {
            return;
        }

        ModuleManager.newInstance().setUHFStatus(true);

        try {
            mReader = RFIDReaderHelper.getDefaultHelper();
            mReader.registerObserver(rxObserver);
            //Thread.currentThread().sleep(500);
            //mReader.setWorkAntenna((byte) 0xff, (byte) 0x01);
            //设定读取间隔时间
            //Thread.currentThread().sleep(500);
            Thread.sleep(500);
            //mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
            mReader.customizedSessionTargetInventory((byte) 0xff, (byte) 0x01, (byte) 0x00,
                    (byte) 0x01);
            //设置工作天线频率
            //mReader.setOutputPower();
            mReader.setOutputPower((byte) 0xff, (byte) 21, (byte) 21, (byte) 0, (byte) 0);
        } catch (Exception e) {
            Log.v(TAG, "RFID设备调取失败" + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    /**
     * RFID模块下线
     */
    private void shutdown() {
        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().release();
    }

    /**
     * 提交盘点结果
     *
     * @param epcCode
     */
    private void submitInventory(final String epcCode) {

        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, Object> paramsMap = new HashMap<>();
        //头部信息
        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);

        //客户ID
        paramsMap.put("uid", "3");
        //数据来源3=无人车
        paramsMap.put("channel", "3");
        paramsMap.put("time", System.currentTimeMillis());
        paramsMap.put("warehouseId", "3");
        paramsMap.put("sign", "c0618a58bd9559c9f5ce78961529448e");

        HashMap<String, List<Map<String, Object>>> dataMap = new HashMap<>();

        ArrayList<Map<String, Object>> carsList = new ArrayList<>();

        //经纬度存储为0
        List<Map<String, Object>> gpsList = new ArrayList<>();
        Map<String, Object> gpsMap = new HashMap<>();
        gpsMap.put("latitude", "0");
        gpsMap.put("longitude", "0");
        gpsList.add(gpsMap);

        Map<String, Object> carInfoMap = new HashMap<>();
        //12位ASC码转换为17位的字符串
        final String vehicleCode=ASCUtil.str12to17(epcCode);
        carInfoMap.put("vin", vehicleCode);
        carInfoMap.put("GPS", gpsList);

        carsList.add(carInfoMap);

        dataMap.put("cars", carsList);
        paramsMap.put("reqData", dataMap);

        String paramJsonStr=JSON.toJSONString(paramsMap);

        Log.d("------------->"+TAG, paramJsonStr);

        OkhttpUtil.okHttpPostJson(VehicleContanst.VEHICLE_INVENTORY_ACCESSDATA,
                paramJsonStr, headerMap, new CallBackUtil.CallBackString() {

                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.d(TAG, e.getMessage());
                        Log.d(TAG, "[" + epcCode + "]+["+vehicleCode+"]盘点结果提交失败");
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            ResultBean resultBean = JSON.parseObject(response, ResultBean.class);
                            String respMsg = resultBean.getRepCode() == "0000" ? "成功" : "失败";
                            Log.d(TAG, "[\" + epcCode + \"]+[\"+vehicleCode+\"]盘点结果提交" + respMsg);
                        } catch (Exception e) {
                            Log.d(TAG, "[\" + epcCode + \"]+[\"+vehicleCode+\"]盘点结果提交失败");
                            Log.d(TAG, e.toString());
                        }
                    }

                });
    }

}

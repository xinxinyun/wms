package com.anji.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.anji.R;
import com.anji.contants.VehicleContanst;
import com.anji.util.ASCUtil;
import com.anji.util.Beeper;
import com.anji.util.CallBackUtil;
import com.anji.util.MD5Utils;
import com.anji.util.OkhttpUtil;
import com.anji.util.PreferenceUtil;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;

/**
 * @author 周宇
 * 数据服务
 */
public class DataService extends Service {

    private static final String TAG = "车辆盘点后台服务";
    private ModuleConnector connector = new ReaderConnector();
    private RFIDReaderHelper mReader;

    /**
     * 缓存EPC码
     */
    private ArrayList<String> epcCodeList = new ArrayList<>(1000);

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

            if(TextUtils.isEmpty(epcCode)){
                return;
            }

            //防止重复读取RFID信息
            if (!epcCodeList.contains(epcCode)) {

                Log.i(TAG, "------------------>" + epcCode);

                ///epcCode = "LSGKE54H7HW09946";
                epcCodeList.add(epcCode);

                //调用蜂鸣声提示已扫描到商品
                Beeper.beep(Beeper.BEEPER_SHORT);

                Message message = Message.obtain();
                message.what = 1;
                message.obj = epcCode;
                handler.sendMessage(message);
            }
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd endTag) {
            //当前工作天线
           /* int antId = endTag.mCurrentAnt;
            //Log.d(TAG, "当前工作天线------>[" + antId + "]");
            //动态切换，如果在1号天线工作，当前盘存结束后切换到2号天线
            mReader.setWorkAntenna((byte) 0xff, antId == 0 ? (byte) 0x01 : (byte) 0x00);
            try {
                //Thread.currentThread().sleep(60);
                Thread.sleep(60);
            } catch (Exception e) {
                Log.d(TAG, "设置天线失败");
            }*/
            mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
            //mReader.customizedSessionTargetInventory((byte) 0xff, (byte) 0x01, (byte) 0x00,(byte) 0x01);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        startup();
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
        //testSubmit();
        return START_STICKY;
    }

    /**
     * 启动程序开始扫描
     */
    private boolean startup() {

        //如果设备在连接状态，则直接退出
        boolean isConnected = connector.isConnected();
        if (isConnected) {
            return false;
        }

        //连接RFID设备
        boolean connectStatus = connector.connectCom(VehicleContanst.TTYMXC2,
                VehicleContanst.baud);
        if (!connectStatus) {
            return false;
        }

        ModuleManager.newInstance().setUHFStatus(true);

        try {

            MediaPlayer player = MediaPlayer.create(getApplicationContext(),
                    R.raw.begin_voice);
            if (!player.isPlaying()) {
                player.start();
            }
            Thread.sleep(3500);
            player.stop();

            mReader = RFIDReaderHelper.getDefaultHelper();
            //注册监听器
            mReader.registerObserver(rxObserver);
            //设定读取间隔时间
            Thread.sleep(500);
            mReader.realTimeInventory((byte) 0xff,(byte)0x01);
            mReader.setOutputPower((byte) 0xff,(byte) 30);

            //群读模式
            /*mReader.customizedSessionTargetInventory((byte) 0xff,
                    (byte) 0x01, (byte) 0x00,
                    (byte) 0x01);
            //设置工作天线频率
            mReader.setOutputPower((byte) 0xff, (byte) 21,
                    (byte) 21, (byte) 0, (byte) 0);*/
        } catch (Exception e) {
            Log.v(TAG, "RFID设备调取失败" + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 提交盘点结果
     *
     * @param epcCode
     */
    private void submitInventory(final String epcCode) {

        HashMap<String, String> headerMap = new HashMap<>();
        //头部信息
        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);

        String timeStr = String.valueOf(System.currentTimeMillis()).substring(0, 10);
        //final String vehicleCode = ASCUtil.str12to17("1C 8E 64 D2 09 E8 06 61 E0 02 93 14");
        final String vehicleCode = ASCUtil.str12to17(epcCode);

        JSONObject paramObj = new JSONObject();

        JSONObject dataObj = new JSONObject();

        dataObj.put("identity", "87082d29af4cb1cfd26ad32fafd806ad");
        dataObj.put("warehouseId", PreferenceUtil.getLong("warehouseId", 0));
        dataObj.put("inventoryPlanId", PreferenceUtil.getLong("inventoryPlanId", 0));
        dataObj.put("inventoryMethod", "1");
        dataObj.put("vin", vehicleCode);
        dataObj.put("longitude", "0");
        dataObj.put("latitude", "0");

        paramObj.put("reqData", dataObj);
        paramObj.put("time", timeStr);
        paramObj.put("sign", MD5Utils.getMD5("reqData=" + dataObj + "&time=" + timeStr));
        paramObj.put("token", "");
        paramObj.put("userId", "3");

        OkhttpUtil.okHttpPostJson(VehicleContanst.VEHICLE_INVENTORY_ACCESSDATA,
                paramObj.toString(), headerMap, new CallBackUtil.CallBackString() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.d(TAG, "[" + epcCode + "]+[" + vehicleCode + "]" +
                                "盘点结果提交失败[错误信息]" + e.getMessage());
                    }
                    @Override
                    public void onResponse(String response) {
                        try {
                            HashMap<String, Object> respMap = JSON.parseObject(response,
                                    HashMap.class);
                            Log.d(TAG, "[" + epcCode + "]+["+vehicleCode+"]盘点提交结果[响应码]" +
                                    respMap.get("repCode").equals("0000") + "&响应消息[repMsg]" + respMap.get("repMsg"));
                        } catch (Exception e) {
                            Log.d(TAG, "[" + epcCode + "]+["+vehicleCode+"]盘点结果提交失败");
                            Log.d(TAG, e.toString());
                        }
                    }

                });
    }

    public void testSubmit() {

        HashMap<String, String> headerMap = new HashMap<>();

        //头部信息
        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);

        String timeStr = String.valueOf(System.currentTimeMillis()).substring(0, 10);

        JSONObject paramObj = new JSONObject();

        JSONObject dataObj = new JSONObject();

        final String vehicleCode = ASCUtil.str12to17("1C 8E 64 D2 09 E8 06 61 E0 02 93 14");

        dataObj.put("identity", "87082d29af4cb1cfd26ad32fafd806ad");
        dataObj.put("warehouseId", PreferenceUtil.getLong("warehouseId", 0));
        dataObj.put("inventoryPlanId", PreferenceUtil.getLong("inventoryPlanId", 0));
        dataObj.put("inventoryMethod", "1");
        dataObj.put("vin", vehicleCode);
        dataObj.put("longitude", "0");
        dataObj.put("latitude", "0");

        paramObj.put("reqData", dataObj);
        paramObj.put("time", timeStr);
        paramObj.put("sign", MD5Utils.getMD5("reqData=" + dataObj + "&time=" + timeStr));
        paramObj.put("token", "");
        paramObj.put("userId", "3");

        System.out.println(paramObj.toString());

        OkhttpUtil.okHttpPostJson(VehicleContanst.VEHICLE_INVENTORY_ACCESSDATA,
                paramObj.toString(), headerMap, new CallBackUtil.CallBackString() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.d(TAG, "[" + vehicleCode + "]+[" + vehicleCode + "]" +
                                "盘点结果提交失败[错误信息]" + e.getMessage());
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            HashMap<String, Object> respMap = JSON.parseObject(response,
                                    HashMap.class);
                            Log.d(TAG, "[" + vehicleCode + "]+[" + vehicleCode + "]盘点提交结果[响应码]" +
                                    respMap.get("repCode").equals("0000") + "&响应消息[repMsg]" + respMap.get("repMsg"));
                        } catch (Exception e) {
                            Log.d(TAG, "[" + vehicleCode + "]+[" + vehicleCode + "]盘点结果提交失败");
                            Log.d(TAG, e.toString());
                        }
                    }

                });
    }

    public static void main(String[] args) {
        System.out.println(String.valueOf(System.currentTimeMillis()).substring(0,10));
    }
}

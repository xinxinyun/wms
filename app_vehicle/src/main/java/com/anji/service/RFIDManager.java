package com.anji.service;

import android.app.Service;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.anji.contants.VehicleContanst;
import com.anji.util.ASCUtil;
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
 * 版本：1.0
 * 创建日期：${date}
 * 描述：
 */
public class RFIDManager extends RXObserver {

    private static final String TAG = "车辆盘点后台服务RFID管理器";

    private ModuleConnector connector = new ReaderConnector();
    private RFIDReaderHelper mReader;

    private ArrayList<String> epcCodeList = new ArrayList<>(1000);

    private Service service;

    public RFIDManager() {
    }

    public RFIDManager(Service service) {
        this.service = service;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String epcCode = (String) msg.obj;
                    //submitInventory(epcCode);

            }
        }
    };

    @Override
    protected void onInventoryTag(RXInventoryTag tag) {
        String epcCode = tag.strEPC;
        if (TextUtils.isEmpty(epcCode)) {
            return;
        }
        //防止重复读取RFID信息
        if (!epcCodeList.contains(epcCode)) {
            Log.i(TAG, "------------------>" + epcCode);
            ///epcCode = "LSGKE54H7HW09946";
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
        //mReader.customizedSessionTargetInventory((byte) 0xff, (byte) 0x01, (byte) 0x00,(byte)
        // 0x01);
    }

    /**
     * 启动程序开始扫描
     */
    public boolean startupRfidDevice() throws Exception {

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

        mReader = RFIDReaderHelper.getDefaultHelper();
        //注册监听器
        mReader.registerObserver(this);
        //设定读取间隔时间
        Thread.sleep(500);
        mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
        mReader.setOutputPower((byte) 0xff, (byte) 30);

        //群读模式
        /*mReader.customizedSessionTargetInventory((byte) 0xff,
                    (byte) 0x01, (byte) 0x00,
                    (byte) 0x01);
        //设置工作天线频率
        mReader.setOutputPower((byte) 0xff, (byte) 21,
                    (byte) 21, (byte) 0, (byte) 0);*/


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
                            Log.d(TAG, "[" + epcCode + "]+[" + vehicleCode + "]盘点提交结果[响应码]" +
                                    respMap.get("repCode").equals("0000") + "&响应消息[repMsg]" + respMap.get("repMsg"));
                        } catch (Exception e) {
                            Log.d(TAG, "[" + epcCode + "]+[" + vehicleCode + "]盘点结果提交失败");
                            Log.d(TAG, e.toString());
                        }
                    }

                });
    }

    /**
     * 清空EPC码重复内容
     *
     * @return
     */
    public boolean clearEpcCache() {
        epcCodeList.clear();
        return true;
    }

    /*public static void main(String[] args) {
        String str= "1C 8D 76 D5 14 40 44 6E 70 C7 05 58,1C 8D 7A A2 14 31 45 6D 80 45 85 06,1C 8D 77 9C 20 37 06 71 10 04 05 72,1C 8D 79 51 20 47 08 6D 60 05 40 40,1C 8D 79 51 20 47 03 6D 60 06 45 38,1C 8D 7A A2 14 31 45 6D 80 40 98 55,1C 8D 7A 9A 14 37 07 71 80 01 91 62,1C 8D 77 9C 20 37 00 71 10 02 25 19,1C 8D 76 E2 14 37 05 71 10 03 62 06,1C 8D 7A 97 14 37 01 6E 30 40 71 41";
        String[] aa=str.split(",");
        for(String s:aa){
            System.out.println(ASCUtil.str12to17(s));
        }
    }*/
}

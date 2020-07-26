package com.anji.service;

import android.app.Service;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.anji.R;
import com.anji.contants.VehicleContanst;
import com.anji.util.ASCUtil;
import com.anji.util.CallBackUtil;
import com.anji.util.LogUtil;
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
 * 创建日期：20191128
 * 描述：
 */
public class RFIDManager extends RXObserver {

    private static final String TAG = "车辆盘点后台服务RFID管理器";

    private ModuleConnector connector;
    private RFIDReaderHelper mReader;

    private static ArrayList<String> epcCodeList = new ArrayList<>(5000);

    private Service service;

    private JSONObject paramObj = new JSONObject();

    private JSONObject dataObj = new JSONObject();

    private HashMap<String, String> headerMap = new HashMap<>();

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
                    submitInventory(epcCode);
            }
        }
    };

    @Override
    protected void onInventoryTag(RXInventoryTag tag) {
        String epcCode = tag.strEPC;
        //防止重复读取RFID信息
        if (!TextUtils.isEmpty(epcCode) &&
                !epcCodeList.contains(epcCode)
                && epcCode.length() == 35) {
            LogUtil.d(TAG, "------------------>" + epcCode);
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
        int antId = endTag.mCurrentAnt;
        //Log.d(TAG, "当前工作天线------>[" + antId + "]");
        //动态切换，如果在1号天线工作，当前盘存结束后切换到2号天线
        mReader.setWorkAntenna((byte) 0xff, antId == 0 ? (byte) 0x01 : (byte) 0x00);
        try {
            //Thread.currentThread().sleep(60);
            Thread.sleep(60);
        } catch (Exception e) {
            Log.e(TAG, "设置天线失败" + e.getMessage());
        }
        //mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
        mReader.customizedSessionTargetInventory((byte) 0xff, (byte) 0x01, (byte) 0x00, (byte)
                0x01);
    }

    /**
     * 启动程序开始扫描
     */
    public boolean startupRFIDDevice() throws Exception {

        connector = new ReaderConnector();

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

        //语音播报已打开开关可以进行盘点
        initMediaPlayer(R.raw.begin_voice);

        mReader = RFIDReaderHelper.getDefaultHelper();
        //注册监听器
        mReader.registerObserver(this);
        //设定读取间隔时间
        Thread.sleep(500);
        //mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
        //mReader.setOutputPower((byte) 0xff, (byte) 30);

        //群读模式
        mReader.customizedSessionTargetInventory((byte) 0xff,
                (byte) 0x01, (byte) 0x00,
                (byte) 0x01);
        //设置工作天线频率
        mReader.setOutputPower((byte) 0xff, (byte) 33,
                (byte) 33, (byte) 0, (byte) 0);


        this.initParam();

        return true;
    }

    /**
     * 部分参数提前初始化，减少GC性能抖动
     */
    private void initParam() {

        //头部信息
        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);

        dataObj.put("identity", MD5Utils.getMD5(VehicleContanst.IDENGITY));
        dataObj.put("warehouseId", PreferenceUtil.getLong("warehouseId", 0));
        dataObj.put("inventoryPlanId", PreferenceUtil.getLong("inventoryPlanId", 0));
        dataObj.put("inventoryMethod", "1");
        dataObj.put("longitude", "0");
        dataObj.put("latitude", "0");

        paramObj.put("token", "");
        paramObj.put("userId", VehicleContanst.USER_ID);
    }

    /**
     * 提交盘点结果
     *
     * @param epcCode
     */
    private void submitInventory(final String epcCode) {
        String timeStr = String.valueOf(System.currentTimeMillis()).substring(0, 10);
        //final String vehicleCode = ASCUtil.str12to17("1C 8E 64 D2 09 E8 06 61 E0 02 93 14");
        final String vehicleCode = ASCUtil.str12to17(epcCode);
        dataObj.put("vin", vehicleCode);
        paramObj.put("reqData", dataObj);
        paramObj.put("time", timeStr);
        paramObj.put("sign", MD5Utils.getMD5("reqData=" + dataObj + "&time=" + timeStr));

        OkhttpUtil.okHttpPostJson(VehicleContanst.VEHICLE_INVENTORY_ACCESSDATA,
                paramObj.toString(), headerMap, new CallBackUtil.CallBackString() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.e(TAG, "[" + epcCode + "]+[" + vehicleCode + "]" +
                                "盘点结果提交失败[错误信息]" + e.getMessage());
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            HashMap<String, Object> respMap = JSON.parseObject(response,
                                    HashMap.class);
                            LogUtil.d(TAG, "[" + epcCode + "]+[" + vehicleCode + "]盘点提交结果[响应码]" +
                                    respMap.get("repCode").equals("0000") + "&响应消息[repMsg]" + respMap.get("repMsg"));
                        } catch (Exception e) {
                            LogUtil.e(TAG,
                                    "[" + epcCode + "]+[" + vehicleCode + "]盘点结果提交失败,[错误信息]" + e.getMessage());
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

    /**
     * 打开音频控制器播放音频并关闭
     *
     * @param sourceId
     */
    private void initMediaPlayer(int sourceId) {
        //语音播报上电成功
        MediaPlayer player = MediaPlayer.create(service,
                sourceId);
        if (!player.isPlaying()) {
            player.start();
        }
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        player.stop();
        player.release();
    }

    /**
     * 关闭RFID设备
     */
    public void shutdownRFIDevice() {
        if (connector != null) {
            connector.disConnect();
        }
        ModuleManager.newInstance().setUHFStatus(false);
    }

    public static void main(String[] args) {
        System.out.println(ASCUtil.str12to17("1C 8E 69 5A 18 09 00 70 20 06 22 73"));
        //System.out.println("1C 8E 64 D2 09 E8 06 61 E0 02 93 14".length());
    }
}

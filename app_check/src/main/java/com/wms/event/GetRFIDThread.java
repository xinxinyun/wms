package com.wms.event;


import com.wms.util.MLog;

/**
 * author CYD
 * date 2018/11/19
 * email chengyd@idatachina.com
 */
public class GetRFIDThread extends Thread {

    private BackResult ba;

    private boolean ifPostMsg = false;

    private boolean flag = true;

    public static GetRFIDThread getInstance() {
        return MySingleton.instance;
    }

    static class MySingleton {
        static final GetRFIDThread instance = new GetRFIDThread();
    }

    public void setBackResult(BackResult ba) {
        this.ba = ba;
    }

    public boolean isIfPostMsg() {
        return ifPostMsg;
    }

    public void destoryThread() {
        flag = false;
    }

    public void setIfPostMsg(boolean ifPostMsg) {
        this.ifPostMsg = ifPostMsg;
    }

    @Override
    public void run() {
        while (flag) {
            if (ifPostMsg) {
                String[] tagData = MyApp.getMyApp().getIdataLib().readTagFromBuffer();
                if (tagData != null) {
                    //Beeper.beep(Beeper.BEEPER_SHORT);
                    ba.postResult(tagData[1]);
                    StringBuilder rssiStr = new StringBuilder((short) Integer.parseInt(tagData[2]
                            , 16) + "");
                    String rssi = rssiStr.insert(rssiStr.length() - 1, ".").toString();
                    MLog.e("tid_user = " + tagData[0] + " epc = " + tagData[1] + " rssi = " + rssi);
                }
            }
        }
    }
}

package com.uhf.uhf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.com.tools.OtgStreamManage;
import com.reader.helper.ReaderHelper;

/**
 * Created by Administrator on 7/17/2017.
 */

public class ConnectOtg extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (OtgStreamManage.newInstance().initSerialPort()) {
                ReaderHelper.getDefaultHelper().setReader(OtgStreamManage.newInstance().getInputStream(), OtgStreamManage.newInstance().getOutputStream());
                context.startActivity(new Intent().setClass(context, MainActivity.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.anji.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anji.service.BackService;

/**
 * @author 周宇
 * @Description 开机广播接收器
 */
public class DataReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent backIntent = new Intent(context, BackService.class);
        context.startService(backIntent);
    }

}

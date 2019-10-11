package com.anji.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anji.service.DataService;

public class DataReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, DataService.class);
        context.startService(service);
    }

}

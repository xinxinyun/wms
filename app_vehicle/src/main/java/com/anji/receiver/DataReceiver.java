package com.anji.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anji.service.DataService;

public class DataReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

//        if (intent != null) {
//            String action = intent.getAction();
//            //取得MyReceiver所在的App的包名
//            String localPkgName = context.getPackageName();
//            Uri data = intent.getData();
//            //取得安装的Apk的包名，只在该app覆盖安装后自启动
//            String installedPkgName = data.getSchemeSpecificPart();
//            if ((action.equals(Intent.ACTION_PACKAGE_ADDED)
//                    || action.equals(Intent.ACTION_PACKAGE_REPLACED)) && installedPkgName
//                    .equals(localPkgName)) {
//                Intent launchIntent = new Intent(context, MainActivity.class);
//                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(launchIntent);
//            }else{
//                Intent backIntent = new Intent(context, DataService.class);
//                context.startService(backIntent);
//            }
//        } else {
            Intent backIntent = new Intent(context, DataService.class);
            context.startService(backIntent);
        //}
    }

}

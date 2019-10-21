package com.anji.util;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 *
 * @author 周宇
 * 版本：1.0
 * 创建日期：${date}
 * 描述：ServiceUtils
 */
public class ServiceUtils {

    /**
     * 判断服务是否开启
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(serviceName)) {
                return true;
            }
        }
        return false;
    }
}

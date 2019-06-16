package com.wms.util;

import android.text.TextUtils;
import android.util.Log;

/**
 * author CYD
 * date 2018/11/27
 * email chengyd@idatachina.com
 */

public class MLog {
    public static boolean ifShown = true; //是否启用Log
    //   private static boolean ifSave = false;//是否保存Log
    private static String TAG = "MLog";


    public static void setTAG(String TAG) {
        MLog.TAG = TAG;
    }

    public static void setTAG(Object cls) {
        if (cls != null)
            MLog.TAG = cls.getClass().getSimpleName();
    }

    public static String getTAG() {
        return TAG;
    }


    public static void e(String msg) {
        if (!ifShown)
            return;
        e(null, msg);
    }

    public static void e(String flag, String msg) {
        StackTraceElement targetStackTraceElement = getTargetStackTraceElement();
        if (TextUtils.isEmpty(flag)) {
            TAG = targetStackTraceElement.getMethodName();
        }
        Log.e(TAG, "(" + targetStackTraceElement.getFileName() + ":"
                + targetStackTraceElement.getLineNumber() + ") : " + msg);
    }


    public static void d(String msg) {
        if (!ifShown)
            return;
        StackTraceElement targetStackTraceElement = getTargetStackTraceElement();
        Log.d(TAG, "(" + targetStackTraceElement.getFileName() + ":"
                + targetStackTraceElement.getLineNumber() + ") : "+msg);

    }


    private static StackTraceElement getTargetStackTraceElement() {
        StackTraceElement targetStackTrace = null;
        boolean shouldTrace = false;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            boolean isLogMethod = stackTraceElement.getClassName().equals(MLog.class.getName());
            if (shouldTrace && !isLogMethod) {
                targetStackTrace = stackTraceElement;
                break;
            }
            shouldTrace = isLogMethod;
        }
        return targetStackTrace;
    }
}

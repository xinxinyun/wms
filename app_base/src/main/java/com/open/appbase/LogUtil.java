/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: LogUtil
 * Author: spring
 * Date: 2019/10/24 14:22
 * Description: 日志工具类
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.open.appbase;

import android.util.Log;

/**
 * @ClassName: LogUtil
 * @Description: 日志工具类
 * @Author: spring
 * @Date: 2019/10/24 14:22
 */
public class LogUtil {

    public static final int VERBOSE = 1;
    public static final int INFO = 2;
    public static final int DEBUG = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;
    public static final int LEVEL = VERBOSE;

    public static void v(String tag, String msg) {
        if (LEVEL <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (LEVEL <= DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (LEVEL <= INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (LEVEL <= WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (LEVEL <= ERROR) {
            Log.e(tag, msg);
        }
    }
}
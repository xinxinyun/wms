/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: ActivityController
 * Author: spring
 * Date: 2019/10/24 14:03
 * Description: dd
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.open.appbase;

import android.app.Activity;

import java.util.ArrayList;

/**
 * @ClassName: ActivityController
 * @Description: java类作用描述
 * @Author: spring
 * @Date: 2019/10/24 14:03
 */
public class ActivityController {
    private static ArrayList<Activity> activities=new ArrayList<>();

    public static void addAcitivty(Activity activity){
        activities.add(activity);
    }

    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }

    public static void finishActivity(){
        for(Activity activity:activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
    }
}
package com.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.bean.ResultBean;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.contants.WmsContanst;
import com.util.CallBackUtil;
import com.util.OkhttpUtil;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;

public class StorgeJob extends Job {

    public static final int PRIORITY = 1;
    private String epcCode;
    String TAG = "仓储库存监听队列任务";
    int sleepTime;

    public StorgeJob(String epcCode) {
        // A job should be persisted in case the application exits
        // before job is completed.
        super(new Params(PRIORITY).persist());
        this.epcCode = epcCode;
        sleepTime = 5;
        Log.i(TAG, "[" + epcCode + "]goin");
    }

    @Override
    public void onAdded() {
        Log.i(TAG, "[" + epcCode + "]onAdded");
    }

    @Override
    public void onRun() throws Throwable {
        Log.i(TAG, "[" + epcCode + "]onRun");
        this.submitInventory(epcCode);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        //重试机制为3次，再次失败则放弃任务
        return RetryConstraint.createExponentialBackoff(runCount, 1);
    }

    /**
     * 销售库存扣减
     */
    private void submitInventory(final String epcCode) {

        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, Object> paramsMap = new HashMap<>();

        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);//头部信息
        paramsMap.put("token", "wms");

        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", "1");
        ArrayList<String> fridList=new ArrayList<>();
        fridList.add(epcCode);
        dataMap.put("list", fridList);

        paramsMap.put("data", dataMap);
        Log.d(TAG,JSON.toJSONString(paramsMap));
        OkhttpUtil.okHttpPostJson(WmsContanst.STORGE_MATERIALINFL_INVENTORY_SUBMIT,
                JSON.toJSONString(paramsMap),headerMap, new CallBackUtil.CallBackString() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.d(TAG, e.getMessage());
                        Log.d(TAG, "[" + epcCode + "]仓储库出入库失败");
                    }

                    @Override
                    public void onResponse(String response) {
                        try {

                            ResultBean resultBean = JSON.parseObject(response, ResultBean.class);
                            String respMsg = resultBean.getCode() == 0 ? "成功" : "失败";
                            Log.d(TAG, "[" + epcCode + "]仓储库出入库"+respMsg);
                        } catch (Exception e) {
                            Log.d(TAG, "[" + epcCode + "]仓储库出入库失败");
                            Log.d(TAG, e.toString());
                        }
                    }
                });
    }
}

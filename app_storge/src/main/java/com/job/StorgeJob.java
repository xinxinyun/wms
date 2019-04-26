package com.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.contants.WmsContanst;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.util.CallBackUtil;
import com.util.OkhttpUtil;

import java.lang.reflect.Type;
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
        //this.submitInventory(epcCode);
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        //重试机制为3次，再次失败则放弃任务
        return RetryConstraint.createExponentialBackoff(runCount, 3);
    }

    /**
     * 销售库存扣减
     */
    private void submitInventory(final String epcCode) {

        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, String> paramsMap = new HashMap<>();

        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);//头部信息
        paramsMap.put("data", epcCode);

        final Gson gson = new Gson();
        OkhttpUtil.okHttpPostJson(WmsContanst.STORGE_MATERIALINFL_INVENTORY_SUBMIT,
                gson.toJson(paramsMap), new CallBackUtil.CallBackString() {//回调
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.d(TAG, e.getMessage());
                        Log.d(TAG, "["+epcCode+"]库存检验失败");
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            Type type = new TypeToken<HashMap<String, String>>() {
                            }.getType();
                            HashMap<String, String> respData = gson.fromJson(response, type);
                            if ("0".equals(respData.get("code"))) {
                                Log.d(TAG, "["+epcCode+"]库存检验成功");
                            }else{
                                Log.d(TAG, "["+epcCode+"]库存检验失败");
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "["+epcCode+"]库存检验失败");
                            Log.d(TAG, e.toString());
                        }
                    }
                });
    }
}

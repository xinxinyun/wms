package com.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.contants.WmsContanst;
import com.util.CallBackUtil;
import com.util.OkhttpUtil;

import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;

public class StorgeJob extends Job {

    public static final int PRIORITY = 1;
    private String epcCode;
    String TAG = "StorgeJob";
    int sleepTime;

    public StorgeJob(String epcCode) {
        // A job should be persisted in case the application exits
        // before job is completed.
        super(new Params(PRIORITY).persist());
        this.epcCode = epcCode;
        sleepTime = 5;
        Log.i(TAG, epcCode + "  goin");
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
       this.submitInventory(epcCode);
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
    private void submitInventory(String epcCode) {

        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, String> paramsMap = new HashMap<>();

        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);//头部信息
        paramsMap.put("epcCode", epcCode);

        OkhttpUtil.okHttpPost(WmsContanst.HOST + WmsContanst.STORGE_MATERIALINFL_INVENTORY_SUBMIT,
                paramsMap, headerMap, new CallBackUtil.CallBackDefault() {//回调
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.d(TAG,e.getMessage());
                    }

                    @Override
                    public void onResponse(Response response) {
                        try {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "提交成功");
                            }
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }
}

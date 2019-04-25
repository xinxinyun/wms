package com;

import android.app.Application;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;

public class JobQueueApplication extends Application {

    private JobManager jobManager;
    private static JobQueueApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;//1. Application的实例
        configureJobManager();//2. 配置JobMananger
    }

    //私有构造器
    private JobQueueApplication() {
        instance = this;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public static JobQueueApplication getInstance() {
        return instance;
    }

    private void configureJobManager() {
        //3. JobManager的配置器，利用Builder模式
        Configuration configuration = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }

                    @Override
                    public void v(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .build();

        jobManager = new JobManager(configuration);
    }
}

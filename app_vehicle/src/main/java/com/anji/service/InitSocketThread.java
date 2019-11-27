package com.anji.service;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.anji.R;
import com.anji.bean.CheckPlan;
import com.anji.util.PreferenceUtil;
import com.anji.util.ServiceUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * 版权：
 *
 * @author 周宇
 * 版本：1.0
 * 创建日期：20191017
 * 描述：InitSocketThread
 */
public class InitSocketThread extends Thread {

    private static final String TAG = "InitSocketThread";

    /**
     * 每隔15秒进行一次对长连接的心跳检测
     * 心跳检测时间
     */
    private static final long HEART_BEAT_RATE = 5 * 1000;

    /**
     * websocket连接地址
     */
    private static final String WEBSOCKET_HOST_AND_PORT = "ws://visp.anji-logistics" +
            ".com/websocket/SGM20191128C";

    private WebSocket mWebSocket;

    private long sendTime = 0L;

    private BackService service;

    // 发送心跳包
    private Handler mHandler = new Handler();

    public InitSocketThread() {
    }

    public InitSocketThread(BackService service) {
        this.service = service;
    }

    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                //发送一个空消息给服务器，通过发送消息的成功失败来判断长连接的连接状态
                if (mWebSocket != null) {
                    boolean isSuccess = mWebSocket.send("");
                    if (!isSuccess) {//长连接已断开
                        mHandler.removeCallbacks(heartBeatRunnable);
                        //取消掉以前的长连接
                        mWebSocket.cancel();
                        //创建一个新的连接
                        new InitSocketThread().start();
                    }
                    sendTime = System.currentTimeMillis();
                }
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    @Override
    public void run() {
        super.run();
        try {
            initSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化socket
     *
     * @throws UnknownHostException
     * @throws IOException
     */
    private void initSocket() throws Exception {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0,
                TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder().url(WEBSOCKET_HOST_AND_PORT).build();
        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                //开启长连接成功的回调
                super.onOpen(webSocket, response);
                mWebSocket = webSocket;
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("---->websocket长连接响应消息", text);
                //接收消息的回调
                super.onMessage(webSocket, text);
//                if (!JSON.isValid(text) && "0\r\n".equals(text)) {
//                    return;
//                }
                if (text.contains("inventoryPlanId")) {
                    //收到服务器端传过来的消息text
                    CheckPlan checkPlan = JSON.parseObject(text, CheckPlan.class);
                    //如果开关打开，则启动RFID读写器开始盘点
                    if (checkPlan.getRfSwitch()) {
                        PreferenceUtil.commitLong("inventoryPlanId", checkPlan.getPlanId());
                        PreferenceUtil.commitLong("warehouseId", checkPlan.getWarehouseId());
                        //判断RFID盘点服务是否在运行
                        if (!ServiceUtils.isServiceRunning(service.getApplicationContext(), "com" +
                                ".anji" +
                                ".service.DataService")) {
                            Intent intent = new Intent(service, DataService.class);
                            service.startService(intent);
                        } else {
                            initMediaPlayer(R.raw.begin_voice);
                        }
                    } else {
                        //远程开关未打开则语音播报提示打开远程开关
                        initMediaPlayer(R.raw.open_rfid);
                    }
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.e(TAG, "onClosing-->reason-[" + code + "]-->" + reason);
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.e(TAG, "onClosing-->reason-[" + code + "]-->" + reason);
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                //长连接连接失败的回调
                if (response != null) {
                    Log.e(TAG, "reason--->" + response.message());
                }
                super.onFailure(webSocket, t, response);
            }
        });
        client.dispatcher().executorService().shutdown();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//开启心跳检测
    }

    /**
     * 关闭websocket连接
     *
     * @return
     */
    public boolean closeWebSocket() {
        boolean flag = false;
        if (mWebSocket != null) {
            flag = mWebSocket.close(1000, null);
        }
        return flag;
    }

    /**
     * 打开音频控制器播放音频并关闭
     *
     * @param sourceId
     */
    private void initMediaPlayer(int sourceId) {
        //语音播报上电成功
        MediaPlayer player = MediaPlayer.create(service,
                sourceId);
        if (!player.isPlaying()) {
            player.start();
        }
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        player.stop();
        player.release();
    }
}

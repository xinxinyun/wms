package com.wms.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wms.adapter.SchduleOnAdapter;
import com.wms.bean.MaterialInfo;
import com.wms.bean.MaterialOnSchedule;
import com.wms.contants.WmsContanst;
import com.wms.event.BackResult;
import com.wms.event.GetRFIDThread;
import com.wms.event.MyApp;

import com.wms.util.Beeper;
import com.wms.util.CallBackUtil;
import com.wms.util.MLog;
import com.wms.util.OkhttpUtil;
import com.wms.util.SimpleFooter;
import com.wms.util.SimpleHeader;
import com.wms.util.StatusBarUtil;
import com.wms.util.ZrcListView;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;

public class ScheduleCheckActivity extends AppCompatActivity implements BackResult {

    private static final String TAG = "临期商品盘点";

    private ZrcListView listView;
    private ArrayList<MaterialOnSchedule> materialInfoList;
    private SchduleOnAdapter adapter;
    private GetRFIDThread rfidThread ;//RFID标签信息获取线程
    private ArrayList<String> rfidList = new ArrayList<>();
    private SweetAlertDialog pTipDialog;

    private int epcSize = 0;

    /**
     * 缓存EPC码
     */
    private ArrayList<String> epcCodeList = new ArrayList<>();

    /**
     * 异步回调刷新数据
     */
    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //动态更新列表内容
                    materialInfoList = JSON.parseObject(msg.obj.toString(),
                            new TypeReference<ArrayList<MaterialOnSchedule>>() {
                            });

                    if (materialInfoList == null || materialInfoList.size() == 0) {
                        View view = findViewById(R.id.noResult);
                        listView.setEmptyView(view);
                        return;
                    }

                    //转换数据结构，方便实时查找
                    for (MaterialOnSchedule materialInfo : materialInfoList) {
                        rfidList.add(materialInfo.getFridCode());
                    }

                    adapter = new SchduleOnAdapter(getBaseContext(), materialInfoList);
                    listView.setAdapter(adapter);

                    SweetAlertDialog sweetAlertDialog =
                            new SweetAlertDialog(ScheduleCheckActivity.this
                                    , SweetAlertDialog.SUCCESS_TYPE);
                    sweetAlertDialog.setContentText("物资清单下载成功！");
                    sweetAlertDialog.setConfirmButton("开始盘点",
                            new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.hide();
                                    startInventory();

                                }
                            });
                    sweetAlertDialog.setCancelable(true);
                    sweetAlertDialog.show();
                    break;
                case 2:
                    //报警提示物资已找到
                    Beeper.beep(Beeper.BEEPER_SHORT);
                    pTipDialog.setContentText("您当前已找到" + epcSize + "件物资");
                    break;
            }
        }
    };

    @Override
    public void postResult(String epcCode) {
        epcCode = epcCode.replaceAll(" ", "");
        //如果不是重复扫描并且包含在物资盘点清单中，则直接蜂鸣声音并更新数量&& rfidList.contains(epcCode)
        if (!epcCodeList.contains(epcCode)
                && rfidList.contains(epcCode)) {
            Log.d(TAG, "已读取到RFID码【" + epcCode + "】");
            Beeper.beep(Beeper.BEEPER_SHORT);
            epcCodeList.add(epcCode);
            epcSize++;
            Message message = Message.obtain();
            message.what = 2;
            myHandler.sendMessage(message);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_out_time);

        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.outtimeToolbar);
        setSupportActionBar(mToolbarTb);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbarTb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        StatusBarUtil.setStatusBarColor(this, Color.parseColor("#00CCFF"));

        pTipDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        pTipDialog.setCustomImage(R.drawable.blue_button_background);
        listView = (ZrcListView) findViewById(R.id.zOutTimeListView);

        // 设置下拉刷新的样式（可选，但如果没有Header则无法下拉刷新）
        SimpleHeader header = new SimpleHeader(this);
        header.setTextColor(0xff0066aa);
        header.setCircleColor(0xff33bbee);
        listView.setHeadable(header);

        // 设置加载更多的样式（可选）
        SimpleFooter footer = new SimpleFooter(this);
        footer.setCircleColor(0xff33bbee);
        listView.setFootable(footer);

        // 设置列表项出现动画（可选）
        listView.setItemAnimForTopIn(R.anim.topitem_in);
        listView.setItemAnimForBottomIn(R.anim.bottomitem_in);

        listView.refresh(); // 主动下拉刷新

        // 下拉刷新事件回调（可选）
        listView.setOnRefreshStartListener(new ZrcListView.OnStartListener() {
            @Override
            public void onStart() {
                initData();
            }
        });

        MLog.e("poweron = " + MyApp.getMyApp().getIdataLib().powerOn());
        rfidThread=new GetRFIDThread();
        rfidThread.setBackResult(this);
        rfidThread.start();
    }

    /**
     * 下载仓储区域盘点物资清单
     */
    private void initData() {

        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("token", "wms");

        OkhttpUtil.okHttpPostJson(WmsContanst.OUTTIME_INVENTORY_SUBMIT, JSON.toJSONString(paramMap),
                new CallBackUtil.CallBackString() {//回调
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.e(TAG, e.toString());
                        String errMsg = "物资清单下载失败！";
                        if (e instanceof SocketTimeoutException) {
                            errMsg = "网络连接超时,请下拉刷新重试！";
                        } else if (e instanceof ConnectException) {
                            errMsg = "网络连接失败,请连接网络！";
                        }
                        listView.setRefreshSuccess(errMsg);
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            listView.setRefreshSuccess("加载成功");
                            Message message = Message.obtain();
                            message.what = 1;
                            message.obj = response;
                            myHandler.sendMessage(message);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                            String errMsg = "物资清单下载失败！";
                            if (e instanceof SocketTimeoutException) {
                                errMsg = "网络连接超时,请下拉刷新重试！";
                            }
                            listView.setRefreshSuccess(errMsg);
                        }
                    }
                });
    }


    /**
     * 开始扫描
     */
    private void startInventory() {
        //如果物资计划列表为空，则不进行盘点
        if (materialInfoList == null || materialInfoList.size() == 0) {
            final SweetAlertDialog sweetAlertDialog2 =
                    new SweetAlertDialog(ScheduleCheckActivity.this, SweetAlertDialog.WARNING_TYPE);
            sweetAlertDialog2.setContentText("未下载到物资清单，请下拉刷新重试！");
            sweetAlertDialog2.setConfirmButton("确定",
                    new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog2.hide();
                            listView.refresh();
                        }
                    });
            sweetAlertDialog2.show();
            return;
        }
        //开启RFID盘存
        if (!rfidThread.isIfPostMsg()) {
            rfidThread.setIfPostMsg(true);
            MLog.e("RFID开始盘存 = " + MyApp.getMyApp().getIdataLib().startInventoryTag());
        }
        pTipDialog.setContentText("您当前已盘点" + epcSize + "件物资");
        pTipDialog.setCancelable(false);
        //结束操作
        pTipDialog.setConfirmButton("查看盘点结果", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                //停止盘存
                rfidThread.setIfPostMsg(false);
                MyApp.getMyApp().getIdataLib().stopInventory();
                pTipDialog.hide();
                //汇总计划列表
                for (MaterialInfo materialInfo : materialInfoList) {
                    String fridCode = materialInfo.getFridCode();
                    if (rfidList.contains(fridCode)) {
                        materialInfo.setCheckQuantity(1);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        pTipDialog.show();
    }

    /**
     * rfid模块下线
     */
    private void offlineRFIDModule() {
        if(rfidThread!=null) {
            rfidThread.destoryThread();
        }
        MLog.e("powerOff = " + MyApp.getMyApp().getIdataLib().powerOff());
        rfidThread=null;
    }

    /**
     * 清除已扫描到的RFID码的缓存数据
     */
    private void clearScanRFID() {
        epcSize = 0;
        epcCodeList.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //开始盘存
            case R.id.menu_schedule_beginInventonry:
                //开始盘存
                this.clearScanRFID();
                this.startInventory();
                break;
            case R.id.menu_schedule_revertInventory:
                //盘存复查
                pTipDialog.show();
                startInventory();
                break;
            case R.id.menu_schedule_endInventory:
                clearScanRFID();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        offlineRFIDModule();
        if (pTipDialog != null) {
            pTipDialog.dismiss();
            pTipDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
            offlineRFIDModule();
            finish();
    }
}

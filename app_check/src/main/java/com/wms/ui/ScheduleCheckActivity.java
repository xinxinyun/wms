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
import com.wms.bean.MaterialOnSchedule;
import com.wms.contants.WmsContanst;
import com.wms.util.CallBackUtil;
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

public class ScheduleCheckActivity extends AppCompatActivity {

    private static final String TAG = "临期商品盘点";

    private ZrcListView listView;
    private ArrayList<MaterialOnSchedule> materialInfoList;
    private SchduleOnAdapter adapter;

    private ArrayList<String> rfidList = new ArrayList<>();

    private SweetAlertDialog pTipDialog;

    private int epcSize = 0;

    private SweetAlertDialog prgorssDialog;

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
//                    Gson gson = new Gson();
//                    Type type = new TypeToken<ArrayList<MaterialOnSchedule>>() {
//                    }.getType();

                    materialInfoList = JSON.parseObject(msg.obj.toString(),
                            new TypeReference<ArrayList<MaterialOnSchedule>>() {
                            });

//                    materialInfoList = gson.fromJson(msg.obj.toString(), type);

                    //转换数据结构，方便实时查找
                    for (MaterialOnSchedule materialInfo : materialInfoList) {
                        rfidList.add(materialInfo.getFridCode());
                    }

                    if (materialInfoList == null || materialInfoList.size() == 0) {
                        return;
                    }

                    adapter = new SchduleOnAdapter(getBaseContext(), materialInfoList);
                    listView.setAdapter(adapter);

                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ScheduleCheckActivity.this
                            , SweetAlertDialog.SUCCESS_TYPE);
                    sweetAlertDialog.setContentText("物资清单下载成功！");
                    sweetAlertDialog.setConfirmButton("开始盘点",
                            new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.hide();
                                    inventoryAction("begin");

                                }
                            });
                    sweetAlertDialog.setCancelable(true);
                    sweetAlertDialog.show();
                    break;
                case 2:
                    //报警提示物资已找到
                    //Beeper.beep(Beeper.BEEPER_SHORT);
                    pTipDialog.setContentText("您当前已找到" + epcSize + "件物资");
                    break;
            }
        }
    };

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
    private void inventoryAction(String flag) {


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
                inventoryAction("begin");
                break;
            case R.id.menu_schedule_revertInventory:
                //盘存复查
                pTipDialog.show();
                inventoryAction("continue");
                break;
            case R.id.menu_schedule_endInventory:
                epcCodeList.clear();
                epcSize=0;
                //结束复查，纯粹是为了实现RFID模块掉电的功能
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

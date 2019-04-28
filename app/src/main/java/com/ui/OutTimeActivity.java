package com.ui;

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

import com.adpter.SchduleOnAdapter;
import com.bean.MaterialInfo;
import com.bean.MaterialOnSchedule;
import com.com.tools.Beeper;
import com.com.tools.SimpleFooter;
import com.com.tools.SimpleHeader;
import com.com.tools.ZrcListView;
import com.contants.WmsContanst;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.uhf.uhf.R;
import com.util.CallBackUtil;
import com.util.OkhttpUtil;
import com.util.StatusBarUtil;

import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;

public class OutTimeActivity extends AppCompatActivity {

    private static final String TAG = "临期商品盘点";

    ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper mReader;
    ModuleManager moduleManager = ModuleManager.newInstance();

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
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<MaterialOnSchedule>>() {
                    }.getType();
                    materialInfoList = gson.fromJson(msg.obj.toString(), type);

                    //转换数据结构，方便实时查找
                    for (MaterialOnSchedule materialInfo : materialInfoList) {
                        rfidList.add(materialInfo.getFridCode());
                    }

                    if (materialInfoList == null || materialInfoList.size() == 0) {
                        return;
                    }

                    adapter = new SchduleOnAdapter(getBaseContext(), materialInfoList);
                    listView.setAdapter(adapter);

                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(OutTimeActivity.this
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
                    Beeper.beep(Beeper.BEEPER_SHORT);
                    pTipDialog.setContentText("您当前已找到" + epcSize + "件物资");
                    break;
            }
        }
    };

    /**
     * RFID监听
     */
    RXObserver rxObserver = new RXObserver() {
        @Override
        protected void onInventoryTag(RXInventoryTag tag) {

            String epcCode = tag.strEPC;

            //如果不是重复扫描并且包含在物资盘点清单中，则直接蜂鸣声音并更新数量&& rfidList.contains(epcCode)
            if (!epcCodeList.contains(epcCode)
                        && rfidList.contains(epcCode)) {
                Log.d(TAG, "已读取到RFID码【" + epcCode + "】");
                epcCodeList.add(epcCode);
                epcSize++;

                Message message = Message.obtain();
                message.what = 2;
                myHandler.sendMessage(message);
            }
        }

        @Override
        protected void onInventoryTagEnd(RXInventoryTag.RXInventoryTagEnd endTag) {
            mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
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

        // prgorssDialog = new SweetAlertDialog(OutTimeActivity.this, SweetAlertDialog
        // .PROGRESS_TYPE);
//        prgorssDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
//        prgorssDialog.setTitleText("正在读取物资盘点清单，请稍候");
//        prgorssDialog.setCancelable(false);
//        prgorssDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        }).start();

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

        Gson gson = new Gson();
        String paramValue = gson.toJson(paramMap);

        OkhttpUtil.okHttpPostJson(WmsContanst.OUTTIME_INVENTORY_SUBMIT, paramValue,
                new CallBackUtil.CallBackString() {//回调
                    @Override
                    public void onFailure(Call call, Exception e) {

                        listView.setRefreshFail("加载失败");

                        Log.e(TAG, e.toString());

                        String errMsg = "物资清单下载失败！";
                        if (e instanceof SocketTimeoutException) {
                            errMsg = "网络连接超时,请下拉刷新重试！";
                        }

                        SweetAlertDialog sweetAlertDialog =
                                new SweetAlertDialog(OutTimeActivity.this,
                                        SweetAlertDialog.ERROR_TYPE);
                        sweetAlertDialog.setContentText(errMsg);
                        sweetAlertDialog.setConfirmButton("确定",
                                new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.hide();
                                    }
                                });
                        sweetAlertDialog.show();
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
                            SweetAlertDialog sweetAlertDialog =
                                    new SweetAlertDialog(OutTimeActivity.this,
                                            SweetAlertDialog.ERROR_TYPE);
                            sweetAlertDialog.setContentText("物资清单下载失败！");
                            sweetAlertDialog.show();
                        }
                    }
                });
    }


    /**
     * 开始扫描
     */
    private void inventoryAction(String flag) {

        //开始盘存则清空之前数据，重新盘存,//继续盘存则维持原来数据，累加盘存
        if ("begin".equals(flag)) {
            epcSize = 0;
            //rfidList.clear();
            epcCodeList.clear();
        }

        try {

            if (!moduleManager.getUHFStatus()) {
                moduleManager.setUHFStatus(true);
            }

            if (!connector.isConnected()) {
                //实时扫描多少个物资
                if (!connector.connectCom(WmsContanst.TTYS1, WmsContanst.baud)) {
                    final SweetAlertDialog sweetAlertDialog3 =
                            new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog3.setContentText("RFID设备模块读取失败！");
                    sweetAlertDialog3.setConfirmButton("确定",
                            new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog3.hide();
                                }
                            });
                    sweetAlertDialog3.show();
                    return;
                }
            }
            if (mReader == null) {
                mReader = RFIDReaderHelper.getDefaultHelper();
            }
            mReader.registerObserver(rxObserver);
            //设定读取间隔时间
            Thread.currentThread().sleep(500);
            mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext(),
                    SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialog.setContentText("RFID设备模块读取失败！");
            sweetAlertDialog.show();
            return;
        }

        pTipDialog.setContentText("您当前已盘点" + epcSize + "件物资");
        pTipDialog.setCancelable(false);

        //结束操作
        pTipDialog.setConfirmButton("查看盘点结果", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                //RFID模块下线
                //moduleManager.setUHFStatus(false);
                //moduleManager.release();
                //当前Activity销售则让RFID模块下线
                //moduleManager.setUHFStatus(false);
                moduleManager.release();
                mReader.unRegisterObserver(rxObserver);
                pTipDialog.hide();

//                SweetAlertDialog playDialog = new SweetAlertDialog(OutTimeActivity.this,
//                SweetAlertDialog.PROGRESS_TYPE);
//                playDialog.setContentText("正在汇总盘点数据，请稍候");
//                playDialog.show();

                //汇总计划列表
                for (MaterialInfo materialInfo : materialInfoList) {
                    String fridCode = materialInfo.getFridCode();
                    if (rfidList.contains(fridCode)) {
                        materialInfo.setCheckQuantity(1);
                    }
                }
                adapter.notifyDataSetChanged();
                //playDialog.hide();
            }
        });

        pTipDialog.show();
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
                //结束复查，纯粹是为了实现RFID模块掉电的功能
                ModuleManager.newInstance().setUHFStatus(false);
                ModuleManager.newInstance().release();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReader != null) {
            mReader.unRegisterObserver(rxObserver);
        }

        //当前Activity销售则让RFID模块下线
        moduleManager.setUHFStatus(false);
        moduleManager.release();
        //epcCodeList.clear();
//        if (connector != null) {
//            connector.disConnect();
//        }
        if (pTipDialog != null) {
            pTipDialog.dismiss();
            pTipDialog = null;
            //     prgorssDialog.dismiss();
            //   prgorssDialog = null;
        }
    }
}

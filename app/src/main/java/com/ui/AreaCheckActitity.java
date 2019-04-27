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
import android.widget.Toast;

import com.adpter.StorgerAdapter;
import com.bean.MaterialInfo;
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

/**
 * 仓储区域物资盘点
 */
public class AreaCheckActitity extends AppCompatActivity {

    private static final String TAG = "仓储区域盘点";
    ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper mReader;
    private ZrcListView listView;
    private ArrayList<MaterialInfo> materialInfoList;
    private StorgerAdapter adapter;

    /**
     * 缓存EPC码
     */
    private ArrayList<String> epcCodeList = new ArrayList<>();

    private SweetAlertDialog pTipDialog;

    private SweetAlertDialog prgorssDialog;

    private int epcSize = 0;

    /**
     * 小类汇总,初始10000个大小
     */
    private HashMap<String, Integer> playMap = new HashMap<String, Integer>(10000);

    private boolean isSubmit = false;
    /**
     * 异步回调刷新数据
     */
    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //动态更新列表内容
                    //动态更新列表内容
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<MaterialInfo>>() {
                    }.getType();
                    materialInfoList = gson.fromJson(msg.obj.toString(), type);

                    adapter = new StorgerAdapter(getBaseContext(), materialInfoList);
                    listView.setAdapter(adapter);

                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(AreaCheckActitity.this, SweetAlertDialog.SUCCESS_TYPE);
                    sweetAlertDialog.setContentText("物资清单下载成功！");
                    sweetAlertDialog.setConfirmButton("开始盘点", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            inventoryAction("begin");
                            sweetAlertDialog.hide();
                        }
                    });
                    sweetAlertDialog.setCancelable(true);
                    sweetAlertDialog.show();
                    break;
                case 2:
                    pTipDialog.setContentText("您当前已盘点" + epcSize + "件物资");
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

            Log.d(TAG, epcCode);

            if (!epcCodeList.contains(epcCode)) {

                epcCodeList.add(epcCode);
                epcSize++;

                //获取条形码值
                String barCode = epcCode.substring(0, 8);
                if (playMap.containsKey(barCode)) {
                    playMap.put(barCode, playMap.get(barCode).intValue() + 1);
                } else {
                    playMap.put(barCode, 0);
                }

                Message message = Message.obtain();
                message.what = 2;
                myHandler.sendMessage(message);

                //调用蜂鸣声提示已扫描到商品
                Beeper.beep(Beeper.BEEPER_SHORT);
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

        setContentView(R.layout.activity_area_check_actitity);

        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.toolbarhh);
        setSupportActionBar(mToolbarTb);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbarTb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (epcCodeList.size() != 0 && !isSubmit) {
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(AreaCheckActitity.this, SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog.setContentText("您的盘点结果未提交，请提交盘点结果！");
                    sweetAlertDialog.setConfirmButton("提交", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            submitInventory();
                        }
                    });
                    sweetAlertDialog.show();
                    return;
                } else {
                    finish();
                }
            }
        });

        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        StatusBarUtil.setStatusBarColor(this, Color.parseColor("#3fb1f0"));

        pTipDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        pTipDialog.setCustomImage(R.drawable.blue_button_background);
        listView = (ZrcListView) findViewById(R.id.zListView);

        // 设置下拉刷新的样式（可选，但如果没有Header则无法下拉刷新）
        final SimpleHeader header = new SimpleHeader(this);
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

        prgorssDialog = new SweetAlertDialog(AreaCheckActitity.this, SweetAlertDialog.PROGRESS_TYPE);
        prgorssDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        prgorssDialog.setTitleText("正在读取物资盘点清单，请稍候");
        prgorssDialog.setCancelable(false);
        prgorssDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        }).start();

    }

    private void refresh() {
        this.initData();
    }

    /**
     * 下载仓储区域盘点物资清单
     */
    private void initData() {

        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, String> paramsMap = new HashMap<>();

        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);//头部信息
        paramsMap.put("token", "wms");//参数
        paramsMap.put("data", "1");//参数
        Gson gson = new Gson();

        OkhttpUtil.okHttpPostJson(WmsContanst.STORGE_MATERIALINFL,
                gson.toJson(paramsMap), headerMap, new CallBackUtil.CallBackString() {//回调
                    @Override
                    public void onFailure(Call call, Exception e) {
                        prgorssDialog.hide();
                        String errMsg = "物资清单下载失败！";
                        if (e instanceof SocketTimeoutException) {
                            errMsg = "网络连接超时";
                        }
                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(AreaCheckActitity.this, SweetAlertDialog.ERROR_TYPE);
                        sweetAlertDialog.setContentText(errMsg);
                        sweetAlertDialog.setConfirmButton("确定", new SweetAlertDialog.OnSweetClickListener() {
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
                            prgorssDialog.hide();

                            Message message = Message.obtain();
                            message.what = 1;
                            message.obj = response;

                            myHandler.sendMessage(message);
                        } catch (Exception e) {
                            prgorssDialog.hide();
                            Log.e(TAG, e.toString());
                            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(AreaCheckActitity.this, SweetAlertDialog.ERROR_TYPE);
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
            epcCodeList.clear();
        }

        //int j=mReader.getFirmwareVersion((byte)0xff);
        if (connector.isConnected()) {
            return;
        }

        //实时扫描多少个物资
        if (connector.connectCom(WmsContanst.TTYS1, WmsContanst.baud)) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rxObserver);
                //设定读取间隔时间
                Thread.currentThread().sleep(500);
                mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
                //int i=mReader.getFirmwareVersion((byte)0xff);
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setContentText("RFID设备模块读取失败！");
                sweetAlertDialog.show();
                return;
            }
        } else {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialog.setContentText("RFID设备模块读取失败！");
            sweetAlertDialog.show();
            return;
        }

        pTipDialog.setContentText("您当前已盘点" + epcSize + "件物资");
        pTipDialog.setCancelable(true);

        //结束操作
        pTipDialog.setConfirmButton("查看盘点结果", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                //RFID模块下线
                ModuleManager.newInstance().setUHFStatus(false);
                ModuleManager.newInstance().release();
                pTipDialog.hide();

                SweetAlertDialog playDialog = new SweetAlertDialog(AreaCheckActitity.this, SweetAlertDialog.PROGRESS_TYPE);
                playDialog.setContentText("正在汇总盘点数据，请稍候");
                playDialog.show();

                //汇总计划列表
                //转换数据结构，汇总结果
                for (MaterialInfo materialInfo : materialInfoList) {
                    String materialBarCode = materialInfo.getMaterialBarcode();
                    if (playMap.containsKey(materialBarCode)) {
                        materialInfo.setCheckQuantity(playMap.get(materialBarCode));
                    }
                }
                adapter.notifyDataSetChanged();
                playDialog.hide();

            }
        });

        pTipDialog.show();
    }

    /**
     * 提交盘点结果
     */
    private void submitInventory() {
        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, Object> paramsMap = new HashMap<>();

        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);//头部信息
        paramsMap.put("token","wms");

        Gson gson = new Gson();

        HashMap<String,Object> dataMap=new HashMap<>();
        dataMap.put("type","1");
        dataMap.put("list",materialInfoList);

        paramsMap.put("data",dataMap);

        final SweetAlertDialog pDialog = new SweetAlertDialog(AreaCheckActitity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("正在提交物资盘点清单，请稍候");
        pDialog.setCancelable(false);
        pDialog.show();

        OkhttpUtil.okHttpPostJson(WmsContanst.STORGE_MATERIALINFL_INVENTORY_SUBMIT,
                gson.toJson(paramsMap), headerMap, new CallBackUtil.CallBackString() {//回调
                    @Override
                    public void onFailure(Call call, Exception e) {
                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE);
                        sweetAlertDialog.setContentText("提交盘存结果失败！");
                        sweetAlertDialog.show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            //if (response.isSuccessful()) {
                            pDialog.hide();
                            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(AreaCheckActitity.this, SweetAlertDialog.SUCCESS_TYPE);
                            sweetAlertDialog.setContentText("销售区域盘点结果提交成功！");
                            sweetAlertDialog.setConfirmButton("确定", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.hide();
                                }
                            });
                            sweetAlertDialog.setCancelable(true);
                            sweetAlertDialog.show();
                            //}
                        } catch (Exception e) {
                            pDialog.hide();
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE);
                            sweetAlertDialog.setContentText("提交盘存结果失败！");
                            sweetAlertDialog.show();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ccbdmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //开始盘存
            case R.id.menu_beginInventonry:
                inventoryAction("begin");
                break;
            case R.id.menu_revertInventory:
                //盘存复查
                pTipDialog.show();
                inventoryAction("continue");
                break;
            case R.id.menu_endInventory:
                //结束复查，纯粹是为了实现RFID模块掉电的功能
                ModuleManager.newInstance().setUHFStatus(false);
                ModuleManager.newInstance().release();
                break;
            case R.id.menu_submitInventory:
                submitInventory();
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
        if (connector != null) {
            connector.disConnect();
        }

        //当前Activity销售则让RFID模块下线
        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().release();
        //epcCodeList.clear();
        if (pTipDialog != null) {
            pTipDialog.dismiss();
            pTipDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (epcCodeList.size() != 0 && !isSubmit) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(AreaCheckActitity.this, SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialog.setContentText("您的盘点结果未提交，请提交盘点结果！");
            sweetAlertDialog.setConfirmButton("提交", new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    submitInventory();
                }
            });
            sweetAlertDialog.show();
        } else {
            finish();
        }
    }
}
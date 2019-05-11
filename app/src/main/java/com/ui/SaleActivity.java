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

import com.adpter.SaleAdapter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.bean.MaterialInfo;
import com.bean.ResultBean;
import com.com.tools.Beeper;
import com.com.tools.SimpleFooter;
import com.com.tools.SimpleHeader;
import com.com.tools.ZrcListView;
import com.contants.WmsContanst;
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

import java.math.BigInteger;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;

public class SaleActivity extends AppCompatActivity {

    private static final String TAG = "销售区域盘点";

    ModuleConnector connector = new ReaderConnector();
    RFIDReaderHelper mReader;
    ModuleManager moduleManager = ModuleManager.newInstance();

    private ZrcListView listView;
    private ArrayList<MaterialInfo> materialInfoList;
    private SaleAdapter adapter;

    /**
     * 缓存EPC码
     */
    private ArrayList<String> epcCodeList = new ArrayList<>();

    private SweetAlertDialog pTipDialog;

    private int epcSize = 0;

    /**
     * 小类汇总,初始10000个大小
     */
    private HashMap<String, Integer> playMap = new HashMap<String, Integer>(800);

    private boolean isSubmit = false;

    /**
     * 异步回调刷新数据
     */
    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    materialInfoList = JSON.parseObject(msg.obj.toString(),
                            new TypeReference<ArrayList<MaterialInfo>>() {
                    });
                    if (materialInfoList == null || materialInfoList.size() == 0) {
                        return;
                    }

                    adapter = new SaleAdapter(getBaseContext(), materialInfoList);
                    listView.setAdapter(adapter);

                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(SaleActivity.this,
                            SweetAlertDialog.SUCCESS_TYPE);
                    sweetAlertDialog.setContentText("物资清单下载成功！");
                    sweetAlertDialog.setConfirmButton("开始盘点",
                            new SweetAlertDialog.OnSweetClickListener() {
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
                    //调用蜂鸣声提示已扫描到商品
                    Beeper.beep(Beeper.BEEPER_SHORT);
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
            if (!epcCodeList.contains(epcCode)) {

                Log.d(TAG, "已读取到RFID码【" + epcCode + "】");

                epcCodeList.add(epcCode);
                epcSize++;

                epcCode=epcCode.replaceAll(" ","").substring(0,20);
                //获取条形码值
                String barCode=new BigInteger(epcCode, 16).
                        toString(10).substring(0,13);
                if (playMap.containsKey(barCode)) {
                    playMap.put(barCode, playMap.get(barCode).intValue() + 1);
                } else {
                    playMap.put(barCode, 1);
                }

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

        setContentView(R.layout.activity_sale);

        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.saletoolbar);
        setSupportActionBar(mToolbarTb);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbarTb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (epcCodeList.size() != 0 && !isSubmit) {
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(SaleActivity.this,
                            SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog.setContentText("您的盘点结果未提交，请提交盘点结果！");
                    sweetAlertDialog.setCancelable(false);
                    sweetAlertDialog.setConfirmButton("提交",
                            new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.hide();
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

        StatusBarUtil.setStatusBarColor(this, Color.parseColor("#00CCFF"));

        pTipDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        pTipDialog.setCustomImage(R.drawable.blue_button_background);
        listView = (ZrcListView) findViewById(R.id.zsaleListView);

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

        // 主动下拉刷新
        listView.refresh();

        // 下拉刷新事件回调（可选）
        listView.setOnRefreshStartListener(new ZrcListView.OnStartListener() {
            @Override
            public void onStart() {
                playMap.clear();
                epcSize = 0;
                epcCodeList.clear();
                initData();
            }
        });
    }

    /**
     * 下载仓储区域盘点物资清单
     */
    private void initData() {

        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, String> paramsMap = new HashMap<>();

        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);//头部信息
        paramsMap.put("token", "wms");//参数
        paramsMap.put("data", "2");//参数

        OkhttpUtil.okHttpPostJson(WmsContanst.STORGE_MATERIALINFL,
                JSON.toJSONString(paramsMap), headerMap, new CallBackUtil.CallBackString() {//回调
                    @Override
                    public void onFailure(Call call, Exception e) {
                        Log.e(TAG, e.toString());
                        String errMsg = "物资清单下载失败！";
                        if (e instanceof SocketTimeoutException) {
                            errMsg = "网络连接超时,请下拉刷新重试！";
                        }else if(e instanceof ConnectException){
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
                            SweetAlertDialog sweetAlertDialog =
                                    new SweetAlertDialog(getBaseContext(),
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

        //如果物资计划列表为空，则不进行盘点
        if(materialInfoList==null||materialInfoList.size()==0){
            final SweetAlertDialog sweetAlertDialog2 =
                    new SweetAlertDialog(SaleActivity.this, SweetAlertDialog.WARNING_TYPE);
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

        //开始盘存则清空之前数据，重新盘存,//继续盘存则维持原来数据，累加盘存
        if ("begin".equals(flag)) {
            epcSize = 0;
            epcCodeList.clear();
        }

        //int j=mReader.getFirmwareVersion((byte)0xff);
        if ("continue".equals(flag)) {
            if (isSubmit) {
                final SweetAlertDialog sweetAlertDialog2 = new SweetAlertDialog(SaleActivity.this
                        , SweetAlertDialog.WARNING_TYPE);
                sweetAlertDialog2.setContentText("盘点结果已经提交，请重新开始盘点！");
                sweetAlertDialog2.setConfirmButton("确定",
                        new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog2.hide();
                    }
                });
                sweetAlertDialog2.show();
                return;
            }
            pTipDialog.show();
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
                moduleManager.release();
                mReader.unRegisterObserver(rxObserver);
                pTipDialog.hide();

//                SweetAlertDialog playDialog = new SweetAlertDialog(SaleActivity.this,
//                SweetAlertDialog.PROGRESS_TYPE);
//                playDialog.setContentText("正在汇总盘点数据，请稍候");
//                playDialog.show();

                //汇总计划列表
                //转换数据结构，汇总结果
                for (MaterialInfo materialInfo : materialInfoList) {
                    String materialBarCode = materialInfo.getMaterialBarcode();
                    if (playMap.containsKey(materialBarCode)) {
                        materialInfo.setCheckQuantity(playMap.get(materialBarCode));
                    }
                }
                adapter.notifyDataSetChanged();
                //playDialog.hide();
            }
        });

        pTipDialog.show();
    }

    /**
     * 提交盘点结果
     */
    private void submitInventory() {

        //如果物资计划列表为空，则不进行盘点
        if(materialInfoList==null||materialInfoList.size()==0){
            final SweetAlertDialog sweetAlertDialog2 =
                    new SweetAlertDialog(SaleActivity.this, SweetAlertDialog.WARNING_TYPE);
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

        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, Object> paramsMap = new HashMap<>();

        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);//头部信息

        paramsMap.put("token", "wms");

        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("type", "2");
        dataMap.put("list", materialInfoList);

        paramsMap.put("data", dataMap);

        final SweetAlertDialog pDialog = new SweetAlertDialog(SaleActivity.this,
                SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("正在提交物资盘点清单，请稍候");
        pDialog.setCancelable(false);
        pDialog.show();

        OkhttpUtil.okHttpPostJson(WmsContanst.STORGE_MATERIALINFL_INVENTORY_SUBMIT,
                JSON.toJSONString(paramsMap), headerMap, new CallBackUtil.CallBackString() {//回调
                    @Override
                    public void onFailure(Call call, Exception e) {
                        pDialog.hide();
                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext()
                                , SweetAlertDialog.ERROR_TYPE);
                        sweetAlertDialog.setContentText("提交盘存结果失败！");
                        sweetAlertDialog.show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {

                            epcSize = 0;
                            epcCodeList.clear();

                            isSubmit = true;

                            pDialog.hide();

                            ResultBean resultBean = JSON.parseObject(response, ResultBean.class);
                            String respMsg = 0 == resultBean.getCode() ? "成功" :
                                    resultBean.getErrorMsg();

                            SweetAlertDialog sweetAlertDialog =
                                    new SweetAlertDialog(SaleActivity.this,
                                    SweetAlertDialog.SUCCESS_TYPE);
                            sweetAlertDialog.setContentText("销售区域盘点结果提交" + respMsg + "！");
                            sweetAlertDialog.setConfirmButton("确定",
                                    new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.hide();
                                }
                            });
                            sweetAlertDialog.setCancelable(true);
                            sweetAlertDialog.show();
                        } catch (Exception e) {
                            Log.d(TAG, e.getMessage());
                            e.printStackTrace();
                            pDialog.hide();
                            SweetAlertDialog sweetAlertDialog =
                                    new SweetAlertDialog(getBaseContext(),
                                            SweetAlertDialog.ERROR_TYPE);
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
                inventoryAction("continue");
                break;
//            case R.id.menu_endInventory:
//                //结束复查，纯粹是为了实现RFID模块掉电的功能
//                ModuleManager.newInstance().setUHFStatus(false);
//                ModuleManager.newInstance().release();
//                break;
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
//        if (connector != null) {
//            connector.disConnect();
//        }

        //当前Activity销售则让RFID模块下线
        ModuleManager.newInstance().setUHFStatus(false);
        ModuleManager.newInstance().release();
        //epcCodeList.clear();
        if (pTipDialog != null) {
            pTipDialog.dismiss();
            // prgorssDialog.dismiss();
            pTipDialog = null;
            //prgorssDialog=null;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (epcCodeList.size() != 0 && !isSubmit) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(SaleActivity.this,
                    SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialog.setContentText("您的盘点结果未提交，请提交盘点结果！");
            sweetAlertDialog.setCancelable(false);
            sweetAlertDialog.setConfirmButton("提交", new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.hide();
                    submitInventory();
                }
            });
            sweetAlertDialog.show();
        } else {
            finish();
        }
    }
}

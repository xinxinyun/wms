package com.uhf.uhf;

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
import com.module.interaction.ModuleConnector;
import com.nativec.tools.ModuleManager;
import com.rfid.RFIDReaderHelper;
import com.rfid.ReaderConnector;
import com.rfid.rxobserver.RXObserver;
import com.rfid.rxobserver.bean.RXInventoryTag;
import com.util.CallBackUtil;
import com.util.DatabaseUtils;
import com.util.OkhttpUtil;
import com.util.StatusBarUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Response;

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
                    adapter.notifyDataSetChanged();
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
               /* String barCode = epcCode.substring(0, 8);
                if (playMap.containsKey(barCode)) {
                    playMap.put(barCode, playMap.get(barCode).intValue() + 1);
                } else {
                    playMap.put(barCode, 0);
                }*/

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
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        /*if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }*/
        StatusBarUtil.setStatusBarColor(this, Color.parseColor("#3fb1f0"));

        /*SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("正在读取物资盘点计划，请稍候");
        pDialog.setCancelable(true);
        pDialog.show();*/
        pTipDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        pTipDialog.setCustomImage(R.drawable.blue_button_background);
        listView = (ZrcListView) findViewById(R.id.zListView);

        // 设置默认偏移量，主要用于实现透明标题栏功能。（可选）
        //float density = getResources().getDisplayMetrics().density;
        //listView.setFirstTopOffset((int) (50 * density));

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

        List<MaterialInfo> materialInfos = new ArrayList<MaterialInfo>() {
            {
                MaterialInfo materialInfo = new MaterialInfo();
                materialInfo.setId(1);
                materialInfo.setMaterialName("泸州老窖定制酒U/3 52°500ml");
                materialInfo.setMaterialCode("2000102300426");
                materialInfo.setSource(30);

                MaterialInfo materialInfo2 = new MaterialInfo();
                materialInfo2.setId(2);
                materialInfo2.setMaterialCode("2000102300426");
                materialInfo2.setMaterialName("青岛纯生啤酒(瓶装)500ml");
                materialInfo2.setSource(20);

                MaterialInfo materialInfo3 = new MaterialInfo();
                materialInfo3.setId(3);
                materialInfo3.setMaterialCode("2000102300426");
                materialInfo3.setMaterialName("名庄荟奔富麦克斯大师承诺西拉干红葡萄酒");
                materialInfo3.setSource(50);

                MaterialInfo materialInfo4 = new MaterialInfo();
                materialInfo4.setId(3);
                materialInfo4.setMaterialCode("2000102300426");
                materialInfo4.setMaterialName("名庄荟长城梦坡家园珍酿干红葡萄酒750ml");
                materialInfo4.setSource(50);

                MaterialInfo materialInfo5 = new MaterialInfo();
                materialInfo5.setId(3);
                materialInfo5.setMaterialCode("2000102300426");
                materialInfo5.setMaterialName("青岛崂山啤酒330ml");
                materialInfo5.setSource(50);

                MaterialInfo materialInfo6 = new MaterialInfo();
                materialInfo6.setId(3);
                materialInfo6.setMaterialCode("2000102300426");
                materialInfo6.setMaterialName("娃哈哈C驱动柠檬汁碳酸饮料530ml");
                materialInfo6.setSource(50);

                MaterialInfo materialInfo7 = new MaterialInfo();
                materialInfo7.setId(3);
                materialInfo7.setMaterialCode("2000102300426");
                materialInfo7.setMaterialName("耐豹顺洁滤清器PUK2845(欧曼)1*1");
                materialInfo7.setSource(50);

                MaterialInfo materialInfo78 = new MaterialInfo();
                materialInfo78.setId(3);
                materialInfo78.setMaterialCode("2000102300426");
                materialInfo78.setMaterialName("耐豹顺洁滤清器PUK2845(欧曼)1*1");
                materialInfo78.setSource(50);

                MaterialInfo materialInfo79 = new MaterialInfo();
                materialInfo79.setId(3);
                materialInfo79.setMaterialCode("2000102300426");
                materialInfo79.setMaterialName("耐豹顺洁滤清器PUK2845(欧曼)1*1");
                materialInfo79.setSource(50);

                MaterialInfo materialInfo73 = new MaterialInfo();
                materialInfo73.setId(3);
                materialInfo73.setMaterialCode("2000102300426");
                materialInfo73.setMaterialName("耐豹顺洁滤清器PUK2845(欧曼)1*1");
                materialInfo73.setSource(50);

                add(materialInfo);
                add(materialInfo2);
                add(materialInfo3);
                add(materialInfo4);
                add(materialInfo5);
                add(materialInfo6);
                add(materialInfo7);
                add(materialInfo78);
                add(materialInfo79);
                add(materialInfo73);

            }
        };

        //initData();
        adapter = new StorgerAdapter(getBaseContext(), materialInfos);
        listView.setAdapter(adapter);
        //listView.refresh(); // 主动下拉刷新*/
        //pDialog.hide();

    }

    /**
     * 下载仓储区域盘点物资清单
     */
    private void initData() {

        HashMap<String, String> headerMap = new HashMap<>();
        HashMap<String, String> paramsMap = new HashMap<>();

        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);//头部信息
        paramsMap.put("userName", "1111");//参数
        paramsMap.put("password", "2222");

        OkhttpUtil.okHttpPost(WmsContanst.HOST + WmsContanst.STORGE_MATERIALINFL,
                paramsMap, headerMap, new CallBackUtil.CallBackDefault() {//回调
                    @Override
                    public void onFailure(Call call, Exception e) {
                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE);
                        sweetAlertDialog.setContentText("物资清单下载失败！");
                        sweetAlertDialog.show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        try {
                            if (response.isSuccessful()) {
                                //下载物资清单
                                String responseBody = response.body().string();
                                Gson gson = new Gson();
                                List<MaterialInfo> waitMaterialList = gson.fromJson(responseBody, List.class);

                                //adapter = new MyAdapter(getBaseContext(), waitMaterialList);

                                listView.setAdapter(adapter);
                                //String waterialInfoJson=gson.toJson(waitMaterialList);

                                //插入数据
                                //必须先初始化
                                //DatabaseUtils.initHelper(getApplication(), "wms.db");
                                DatabaseUtils.getHelper().saveAll(waitMaterialList);
                                Log.d(TAG, "插入数据成功");


                            }
                        } catch (Exception e) {
                            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE);
                            sweetAlertDialog.setContentText("物资清单下载失败！");
                            sweetAlertDialog.show();
                        }
                /*try {
                    isLogin = true;
                    Headers responseHeaders = response.headers();
                    String c = responseHeaders.get("Set-Cookie");//cookie的处理
                    TVAppUtil.setCookie(c);
                    updateReportData();
                    HandlerUtil.sendTimingRequest();
                } catch (Exception e) {
                    LogUtil.e(TAG, e.getMessage());
                    TVAppUtil.showToast("登录异常！");
                } finally {
                    try {
                        response.body().close();
                    } catch (Exception e) {
                    }
                }*/
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

                SweetAlertDialog playDialog = new SweetAlertDialog(getApplicationContext(), SweetAlertDialog.PROGRESS_TYPE);
                playDialog.setContentText("正在汇总盘点数据，请稍候");
                playDialog.show();

                //汇总计划列表
                for (MaterialInfo materialInfo : materialInfoList) {
                    String materialBarcode = materialInfo.getMaterialBarcode();
                    materialInfo.setSource(playMap.get(materialBarcode));
                    adapter.notifyDataSetChanged();
                }

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
        HashMap<String, String> paramsMap = new HashMap<>();

        headerMap.put("Content-Type", OkhttpUtil.CONTENT_TYPE);//头部信息
        paramsMap.put("userName", "1111");//参数
        paramsMap.put("password", "2222");

        OkhttpUtil.okHttpPost(WmsContanst.HOST + WmsContanst.STORGE_MATERIALINFL_INVENTORY_SUBMIT,
                paramsMap, headerMap, new CallBackUtil.CallBackDefault() {//回调
                    @Override
                    public void onFailure(Call call, Exception e) {
                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getBaseContext(), SweetAlertDialog.ERROR_TYPE);
                        sweetAlertDialog.setContentText("提交盘存结果失败！");
                        sweetAlertDialog.show();
                    }

                    @Override
                    public void onResponse(Response response) {
                        try {
                            if (response.isSuccessful()) {
                                //清空数据
                                epcCodeList.clear();
                                epcSize = 0;
                                isSubmit = true;
                                Log.d(TAG, "插入数据成功");
                            }
                        } catch (Exception e) {
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
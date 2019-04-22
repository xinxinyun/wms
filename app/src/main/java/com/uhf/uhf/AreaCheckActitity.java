package com.uhf.uhf;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bean.MaterialInfo;
import com.com.tools.SimpleFooter;
import com.com.tools.SimpleHeader;
import com.com.tools.ZrcListView;
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
    /**
     * 波特率
     */
    private int baud = 115200;

    /**
     * 串口号
     */
    private static final String TTYS1 = "/dev/ttyS4";
    private ZrcListView listView;
    private ArrayList<MaterialInfo> materialInfoList;
    private MyAdapter adapter;

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
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
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
                scanFridLabel(epcCode);
            }
            epcCodeList.add(epcCode);
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
                finish();
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
        StatusBarUtil.setStatusBarColor(this, Color.parseColor("#00CCFF"));

        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("正在读取物资盘点计划，请稍候");
        pDialog.setCancelable(true);
        pDialog.show();

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
                materialInfo3.setMaterialName("雪花清爽500ml");
                materialInfo3.setSource(50);

                MaterialInfo materialInfo4 = new MaterialInfo();
                materialInfo4.setId(3);
                materialInfo4.setMaterialCode("2000102300426");
                materialInfo4.setMaterialName("雪花清爽500ml");
                materialInfo4.setSource(50);

                MaterialInfo materialInfo5 = new MaterialInfo();
                materialInfo5.setId(3);
                materialInfo5.setMaterialCode("2000102300426");
                materialInfo5.setMaterialName("雪花清爽500ml");
                materialInfo5.setSource(50);

                MaterialInfo materialInfo6 = new MaterialInfo();
                materialInfo6.setId(3);
                materialInfo6.setMaterialCode("2000102300426");
                materialInfo6.setMaterialName("雪花清爽500ml");
                materialInfo6.setSource(50);

                MaterialInfo materialInfo7 = new MaterialInfo();
                materialInfo7.setId(3);
                materialInfo7.setMaterialCode("2000102300426");
                materialInfo7.setMaterialName("雪花清爽500ml");
                materialInfo7.setSource(50);


                add(materialInfo);
                add(materialInfo2);
                add(materialInfo3);
                add(materialInfo4);
                add(materialInfo5);
                add(materialInfo6);
                add(materialInfo7);
            }
        };

        //initData();
        adapter = new MyAdapter(getBaseContext(), materialInfos);
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

        OkhttpUtil.okHttpPost("", paramsMap, headerMap, new CallBackUtil.CallBackDefault() {//回调
            @Override
            public void onFailure(Call call, Exception e) {
                //LogUtil.e(TAG, e.getMessage());
                //TVAppUtil.showToast("网络异常！请确认是否联网，以及服务器地址是否正确！");
                //SharedPreferencesUtil.sharePut("newServerAddress","");
            }

            @Override
            public void onResponse(Response response) {
                try {
                    if (response.isSuccessful()) {
                        //下载物资清单
                        String responseBody = response.body().string();
                        Gson gson = new Gson();
                        List<MaterialInfo> waitMaterialList = gson.fromJson(responseBody, List.class);

                        adapter = new MyAdapter(getBaseContext(), waitMaterialList);

                        listView.setAdapter(adapter);

                        //插入数据
                        //必须先初始化
                        DatabaseUtils.initHelper(getApplication(), "wms.db");
                        DatabaseUtils.getHelper().saveAll(waitMaterialList);
                        Log.d(TAG, "插入数据成功");


                    }
                } catch (Exception e) {

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

    private class MyAdapter extends BaseAdapter {

        private Context context;
        private List<MaterialInfo> materialInfoList;

        public MyAdapter(Context context,
                         List<MaterialInfo> materialInfoList) {
            this.context = context;
            this.materialInfoList = materialInfoList;
        }

        @Override
        public int getCount() {
            return materialInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return materialInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;
            if (viewHolder == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.simple_list_item_1, null);
                viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.txCodeTextView = (TextView) convertView.findViewById(R.id.tv_txcode);
                viewHolder.num = (TextView) convertView.findViewById(R.id.num);
                viewHolder.actualNum = convertView.findViewById(R.id.actualNum);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.iv_image);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            MaterialInfo waitMaterial = materialInfoList.get(position);

            viewHolder.title.setText(waitMaterial.getMaterialName());
            viewHolder.num.setText(waitMaterial.getSource().toString());
            viewHolder.txCodeTextView.setText(waitMaterial.getMaterialCode());
            viewHolder.actualNum.setText("待盘点");
            //viewHolder.textView.setOnClickListener(new OnItemChildClickListener(DELETE, position));

            return convertView;

        }

        // ViewHolder用于缓存控件，三个属性分别对应item布局文件的三个控件
        class ViewHolder {
            public TextView title;
            public TextView num;
            public TextView actualNum;
            public TextView txCodeTextView;
            public ImageView imageView;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ccbdmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_collection:
                materialInfoList.clear();
                distinguishFRIDCode();
                break;
            case R.id.toolbar_share:
                Toast.makeText(this, "分享", Toast.LENGTH_SHORT).show();
                break;
            case R.id.toolbar_fontsize:
                Toast.makeText(this, "字号", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 启动RFID扫描
     */
    private void distinguishFRIDCode() {
        if (connector.connectCom("dev/ttyS4", 115200)) {
            ModuleManager.newInstance().setUHFStatus(true);
            try {
                mReader = RFIDReaderHelper.getDefaultHelper();
                mReader.registerObserver(rxObserver);
                Thread.currentThread().sleep(500);
                mReader.realTimeInventory((byte) 0xff, (byte) 0x01);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getBaseContext(), "RFID连接失败", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 扫描标签数并做汇总
     *
     * @param epcCode
     */
    private void scanFridLabel(String epcCode) {
        MyAsyncTask task = new MyAsyncTask();
        task.execute(epcCode);
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
    }

    /**
     * 异步处理机制
     */
    class MyAsyncTask extends AsyncTask<String, Void, MaterialInfo> {

        //onPreExecute用于异步处理前的操作
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //此处将progressBar设置为可见.
        }

        //在doInBackground方法中进行异步任务的处理.
        @Override
        protected MaterialInfo doInBackground(String... params) {

            //RFID扫描返回RFID码
            String epcCode = params[0];

            //调用本库去查询本地数据库作数量比对
            String txBarCode=epcCode.substring(0,8);

            //必须先初始化
            //DatabaseUtils.initHelper(getBaseContext(), "wms.db");

            //创建学生类
            MaterialInfo materialInfo = new MaterialInfo();
            materialInfo.setMaterialMode("11111111111");

            //将学生类保存到数据库
            DatabaseUtils.getHelper().save(materialInfo);

            //DatabaseUtils.getHelper().queryById()
            materialInfoList.add(materialInfo);

            return materialInfo;
        }

        //onPostExecute用于UI的更新.此方法的参数为doInBackground方法返回的值.
        @Override
        protected void onPostExecute(MaterialInfo materialInfo) {
            super.onPostExecute(materialInfo);

            Message message = Message.obtain();
            message.what = 1;
            message.sendToTarget();
        }
    }
}
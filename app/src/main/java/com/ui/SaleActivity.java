package com.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bean.MaterialInfo;
import com.com.tools.SimpleFooter;
import com.com.tools.SimpleHeader;
import com.com.tools.ZrcListView;
import com.google.gson.Gson;
import com.uhf.uhf.R;
import com.util.CallBackUtil;
import com.util.DatabaseUtils;
import com.util.OkhttpUtil;
import com.util.StatusBarUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class SaleActivity extends AppCompatActivity {

    private static final String CONTENT_TYPE = "application/json; charset=utf-8";

    private static final String TAG = "AreaCheckActitity";

    private ZrcListView listView;
    private Handler handler;
    private ArrayList<MaterialInfo> materialInfoList;
    private int pageId = -1;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_area_check_actitity);

        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.toolbarhh);
        setSupportActionBar(mToolbarTb);
        getSupportActionBar().setTitle("        销售区域盘点");
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
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }

        listView = (ZrcListView) findViewById(R.id.zListView);
        handler = new Handler();

        // 设置默认偏移量，主要用于实现透明标题栏功能。（可选）
        float density = getResources().getDisplayMetrics().density;
        listView.setFirstTopOffset((int) (50 * density));

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

        // 下拉刷新事件回调（可选）
       /* listView.setOnRefreshStartListener(new OnStartListener() {
            @Override
            public void onStart() {
                refresh();
            }
        });*/

        // 加载更多事件回调（可选）
        /*listView.setOnLoadMoreStartListener(new ZrcListView.OnStartListener() {
            @Override
            public void onStart() {
                loadMore();
            }
        });*/



        List<MaterialInfo> materialInfos=new ArrayList<MaterialInfo>() {
            {
                MaterialInfo materialInfo=new MaterialInfo();
                materialInfo.setId(1);
                materialInfo.setMaterialName("泸州老窖定制酒U/3 52°500ml");
                materialInfo.setMaterialCode("2000102300426");
                materialInfo.setSource(30);

                MaterialInfo materialInfo2=new MaterialInfo();
                materialInfo2.setId(2);
                materialInfo2.setMaterialCode("2000102300426");
                materialInfo2.setMaterialName("青岛纯生啤酒(瓶装)500ml");
                materialInfo2.setSource(20);

                MaterialInfo materialInfo3=new MaterialInfo();
                materialInfo3.setId(3);
                materialInfo3.setMaterialCode("2000102300426");
                materialInfo3.setMaterialName("雪花清爽500ml");
                materialInfo3.setSource(50);

                MaterialInfo materialInfo4=new MaterialInfo();
                materialInfo4.setId(3);
                materialInfo4.setMaterialCode("2000102300426");
                materialInfo4.setMaterialName("雪花清爽500ml");
                materialInfo4.setSource(50);

                MaterialInfo materialInfo5=new MaterialInfo();
                materialInfo5.setId(3);
                materialInfo5.setMaterialCode("2000102300426");
                materialInfo5.setMaterialName("雪花清爽500ml");
                materialInfo5.setSource(50);

                MaterialInfo materialInfo6=new MaterialInfo();
                materialInfo6.setId(3);
                materialInfo6.setMaterialCode("2000102300426");
                materialInfo6.setMaterialName("雪花清爽500ml");
                materialInfo6.setSource(50);

                MaterialInfo materialInfo7=new MaterialInfo();
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
        adapter = new MyAdapter(getBaseContext(),materialInfos);
        listView.setAdapter(adapter);
        //listView.refresh(); // 主动下拉刷新*/
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

    /*private void refresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int rand = (int) (Math.random() * 2); // 随机数模拟成功失败。这里从有数据开始。
                if (rand == 0 || pageId == -1) {
                    pageId = 0;
                    msgs = new ArrayList<String>();
                    for (String name : names[0]) {
                        msgs.add(name);
                    }
                    adapter.notifyDataSetChanged();
                    listView.setRefreshSuccess("加载成功"); // 通知加载成功
                    listView.startLoadMore(); // 开启LoadingMore功能
                } else {
                    listView.setRefreshFail("加载失败");
                }
            }
        }, 2 * 1000);
    }*/

    /*private void loadMore() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pageId++;
                if (pageId < names.length) {
                    for (String name : names[pageId]) {
                        msgs.add(name);
                    }
                    adapter.notifyDataSetChanged();
                    listView.setLoadMoreSuccess();
                } else {
                    listView.stopLoadMore();
                }
            }
        }, 2 * 1000);
    }*/

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
                viewHolder = new MyAdapter.ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.simple_list_item_1, null);
                viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.num = (TextView) convertView.findViewById(R.id.num);
                viewHolder.txCodeTextView=(TextView)convertView.findViewById(R.id.tv_txcode);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.iv_image);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MyAdapter.ViewHolder) convertView.getTag();
            }

            MaterialInfo waitMaterial=materialInfoList.get(position);

            viewHolder.title.setText(waitMaterial.getMaterialName());
            viewHolder.num.setText(waitMaterial.getSource().toString());
            viewHolder.txCodeTextView.setText(waitMaterial.getMaterialCode());

            //viewHolder.textView.setOnClickListener(new OnItemChildClickListener(DELETE, position));

            return convertView;


           /* TextView textView;
            if (convertView == null) {
                textView = (TextView) getLayoutInflater().
                        inflate(android.R.layout.simple_list_item_1, null);
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            } else {
                textView = (TextView) convertView;
            }
            WaitMaterial waitMaterial=waitMaterialList.get(position);
            textView.setText();
            return textView;*/
        }

        // ViewHolder用于缓存控件，三个属性分别对应item布局文件的三个控件
        class ViewHolder {
            public TextView title;
            public TextView num;
            public TextView txCodeTextView;
            public ImageView imageView;

        }
    }

    //这里是在登录界面label上右上角添加三个点，里面可添加其他功能
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ccbdmenu, menu);//这里是调用menu文件夹中的main.xml，在登陆界面label右上角的三角里显示其他功能
        return true;
    }

}

package com.uhf.uhf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.com.tools.ExcelUtils;
import com.reader.base.ERROR;
import com.reader.base.ReaderBase;
import com.reader.helper.ControlGPIO;
import com.reader.helper.ISO180006BOperateTagBuffer;
import com.reader.helper.InventoryBuffer;
import com.reader.helper.OperateTagBuffer;
import com.reader.helper.ReaderHelper;
import com.reader.helper.ReaderSetting;
import com.uhf.uhf.PopupMenu.MENUITEM;
import com.uhf.uhf.PopupMenu.OnItemClickListener;
import com.uhf.uhf.spiner.DialogCustomed;
import com.uhf.uhf.tagpage.PageInventoryReal;
import com.uhf.uhf.tagpage.PageInventoryReal6B;
import com.uhf.uhf.tagpage.PageTag6BAccess;
import com.uhf.uhf.tagpage.PageTagAccess;
import com.ui.base.BaseActivity;
import com.ui.base.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity {
    private ViewPager mPager;
    private List<View> listViews;
    private TextView title[] = new TextView[2];
    private int currIndex = 0;

    private TextView mRefreshButton;

    private ReaderBase mReader;
    private ReaderHelper mReaderHelper;

    //test
    public static Activity activity ;

    public static boolean mIsMonitorOpen = false;

    private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;

    //private Setting mViewSetting;
    //private Tag	mTag;
    //private Monitor mMonitor;

    //private LogList mLogList;

    private ImageView iv_menu;
    private PopupMenu popupMenu;

    private LocalBroadcastManager lbm;

    private MENUITEM cur_item = MENUITEM.ITEM1;

    @Override
    protected void onResume() {
        if (mReader != null) {
            if (!mReader.IsAlive())
                mReader.StartWait();
        }
        super.onResume();
    }

    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((UHFApplication) getApplication()).addActivity(this);

        activity = this;

        // Storage Permissions
        ExcelUtils.verifyStoragePermissions(this);

        mRefreshButton = (TextView) findViewById(R.id.refresh);

        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cur_item == MENUITEM.ITEM1) {
                    if (0 == currIndex) {
                        ((PageInventoryReal) findViewById(R.id.view_PageInventoryReal)).refresh();
                    } else {
                        ((PageTagAccess) findViewById(R.id.view_PageTagAccess)).refresh();
                    }
                } else if (cur_item == MENUITEM.ITEM2) {
                    if (0 == currIndex) {
                        ((PageInventoryReal6B) findViewById(R.id.view_PageInventoryReal6B)).refresh();
                    } else {
                        ((PageTag6BAccess) findViewById(R.id.view_PageTag6BAccess)).refresh();
                    }
                }
            }
        });

        popupMenu = new PopupMenu(this);

        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        iv_menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupMenu.showLocation(R.id.iv_menu);
                popupMenu.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onClick(MENUITEM item, String str) {
                        if (item == MENUITEM.ITEM1) {
                            InitViewPager(MENUITEM.ITEM1);
                        } else if (item == MENUITEM.ITEM2) {
                            InitViewPager(MENUITEM.ITEM2);
                        } else if (item == MENUITEM.ITEM3) {
                            Intent intent;
                            intent = new Intent().setClass(MainActivity.this, Setting.class);
                            startActivity(intent);
                        } else if (item == MENUITEM.ITEM4) {
                            askForOut();
                        } else if (item == MENUITEM.ITEM5) {
                            if (str.equals("English")) {
                                PreferenceUtil.commitString("language", "en");
                            } else if (str.equals("中文")) {
                                PreferenceUtil.commitString("language", "zh");
                            }
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MainActivity.this.startActivity(intent);

                        } else if (item == MENUITEM.ITEM_add1) {
                            saveExcel();
                        }
                    }
                });
            }
        });

        InitTextView();
        InitViewPager(MENUITEM.ITEM1);

        try {
            mReaderHelper = ReaderHelper.getDefaultHelper();
            mReader = mReaderHelper.getReader();
        } catch (Exception e) {
            return;
        }

        m_curReaderSetting = mReaderHelper.getCurReaderSetting();
        m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
        m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
        m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

        //mLogList = (LogList) findViewById(R.id.log_list);

        //mViewSetting = (Setting) listViews.get(0).findViewById(R.id.view_setting);
        //mTag = (Tag) listViews.get(1).findViewById(R.id.view_tag);

        //mViewSetting.setLogList(mLogList);

        lbm = LocalBroadcastManager.getInstance(this);

        IntentFilter itent = new IntentFilter();
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_ISO18000_6B);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
        itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
        itent.addAction(ReaderHelper.BROADCAST_WRITE_DATA);

        lbm.registerReceiver(mRecv, itent);
    }

    private final BroadcastReceiver mRecv = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH)) {

            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_INVENTORY)) {

            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL)) {

            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_ISO18000_6B)) {

            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG)) {

            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
                byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
                //mViewSetting.refreshReaderSetting(btCmd);
            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_LOG)) {
                //mLogList.writeLog((String)intent.getStringExtra("log"), intent.getIntExtra("type", ERROR.SUCCESS));
            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_DATA) && PreferenceUtil.getBoolean(Monitor.mIsChecked, false) && !mIsMonitorOpen) {
                ((UHFApplication) getApplication()).writeMonitor((String) intent.getStringExtra("log"), intent.getIntExtra("type", ERROR.SUCCESS));
                //fixed by lei.li avoid observer serial port when the switch on

            }
        }
    };

    private void InitTextView() {
        title[0] = (TextView) findViewById(R.id.tab_index1);
        title[1] = (TextView) findViewById(R.id.tab_index2);

        title[0].setOnClickListener(new MyOnClickListener(0));
        title[1].setOnClickListener(new MyOnClickListener(1));
    }

    private void InitViewPager(MENUITEM item) {
        cur_item = item;
        mPager = (ViewPager) findViewById(R.id.vPager);
        listViews = new ArrayList<View>();
        LayoutInflater mInflater = getLayoutInflater();
        if (item == MENUITEM.ITEM1) {
            listViews.add(mInflater.inflate(R.layout.lay1, null));
            listViews.add(mInflater.inflate(R.layout.lay2, null));
        } else if (item == MENUITEM.ITEM2) {
            listViews.add(mInflater.inflate(R.layout.lay3, null));
            listViews.add(mInflater.inflate(R.layout.lay4, null));
        }
        mPager.setAdapter(new MyPagerAdapter(listViews));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
        currIndex = 0;
        title[1].setBackgroundResource(R.drawable.btn_select_background_select_left_down);
        title[0].setBackgroundResource(R.drawable.btn_select_background_select_right);
        title[1].setTextColor(Color.rgb(0x00, 0xBB, 0xF7));
        title[0].setTextColor(Color.rgb(0xFF, 0xFF, 0xFF));
    }

    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    public class MyOnClickListener implements OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    }

    ;

    public class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            currIndex = arg0;
            if (0 == currIndex) {
                title[1].setBackgroundResource(R.drawable.btn_select_background_select_left_down);
                title[0].setBackgroundResource(R.drawable.btn_select_background_select_right);
                //title[0].setBackgroundColor(Color.rgb(0x00, 0xBB, 0xF7));
                //title[1].setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF));
            } else {
                title[1].setBackgroundResource(R.drawable.btn_select_background_select_left);
                title[0].setBackgroundResource(R.drawable.btn_select_background_select_right_down);
                //title[0].setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF));
                //title[1].setBackgroundColor(Color.rgb(0x00, 0xBB, 0xF7));
            }

            title[1 - currIndex].setTextColor(Color.rgb(0x00, 0xBB, 0xF7));
            title[currIndex].setTextColor(Color.rgb(0xFF, 0xFF, 0xFF));
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

			/*if (!mLogList.tryClose())
				askForOut();*/
            askForOut();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void askForOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.alert_diag_title)).
                setMessage(getString(R.string.are_you_sure_to_exit)).
                setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //close the module
                                ControlGPIO.newInstance().JNIwriteGPIO(0);
                                getApplication().onTerminate();
                            }
                        }).setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setCancelable(false).show();
    }

    /**
     * Save the tags as excel file;
     */
    private void saveExcel() {
        DialogCustomed dialogCustomed = new DialogCustomed(this,R.layout.excel_save_dialog);
        dialogCustomed.setTags(m_curInventoryBuffer.lsTagList);
        dialogCustomed.getDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (lbm != null)
            lbm.unregisterReceiver(mRecv);
    }
}

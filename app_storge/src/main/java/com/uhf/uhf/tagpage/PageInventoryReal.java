package com.uhf.uhf.tagpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.reader.base.CMD;
import com.reader.base.ERROR;
import com.reader.base.ReaderBase;
import com.reader.helper.ISO180006BOperateTagBuffer;
import com.reader.helper.InventoryBuffer;
import com.reader.helper.OperateTagBuffer;
import com.reader.helper.ReaderHelper;
import com.reader.helper.ReaderSetting;
import com.uhf.uhf.LogList;
import com.uhf.uhf.R;
import com.uhf.uhf.R.id;
import com.uhf.uhf.R.layout;
import com.uhf.uhf.TagRealList;
import com.uhf.uhf.spiner.AbstractSpinerAdapter.IOnItemSelectListener;
import com.uhf.uhf.spiner.SpinerPopWindow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PageInventoryReal extends LinearLayout {
    private static final String TAG = "PageInventory";
    private static final boolean DEBUG = true;
    public static final int INT = 6;
    // fixed by lei.li 2016/11/09
    // private LogList mLogList;
    private LogList mLogList;
    // fixed by lei.li 2016/11/09
    private TextView mStartStop;
    // private TextView mRefreshButton;
    private LinearLayout mLayoutRealSet;
    private TextView mSessionIdTextView, mInventoriedFlagTextView;
    private TableRow mDropDownRow1, mDropDownRow2;
    private CheckBox mCbRealSet, mCbRealSession;
    private List<String> mSessionIdList;
    private List<String> mInventoriedFlagList;
    private SpinerPopWindow mSpinerPopWindow1, mSpinerPopWindow2;
    private EditText mRealRoundEditText;
    private TagRealList mTagRealList;
    private ReaderHelper mReaderHelper;
    private ReaderBase mReader;
    private int mPos1 = -1, mPos2 = -1;
    private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
    private TextView mTagsCountText, mTagsTotalText;
    private TextView mTagsSpeedText, mTagsTimeText, mTagsOpTimeText;
    private LocalBroadcastManager lbm;
    private long mRefreshTime;
    private Context mContext;

    // add ant switch inventory
    private CheckBox mAnt1, mAnt2, mAnt3, mAnt4, mAnt5, mAnt6, mAnt7, mAnt8;
    private Spinner mFastAnt1, mFastAnt2, mFastAnt3, mFastAnt4, mFastAnt5, mFastAnt6, mFastAnt7, mFastAnt8, mFastSessionTarget, mFastSessionFlag;
    private EditText mFastLoop1, mFastLoop2, mFastLoop3, mFastLoop4, mFastLoop5, mFastLoop6, mFastLoop7, mFastLoop8;
    private CheckBox mFastSession, mFastPhase, mFastDynamic, mFastSwitchCheck, mBufferInventoryCheck;
    private EditText mFastInterval, mFastRepeat, mFastReserve1, mFastReserve2, mFastReserve3, mFastReserve4, mFastReserve5, mFastOpitized;
    private EditText mFastContinus, mFastTargetQuantity, mFastRunTimes, mTimeInterval, mFastRepeat1, mFastRepeat2, mFastRepeat3, mFastRepeat4;
    private RadioButton mTypeAnt1, mTypeAnt4, mTypeAnt8;
    private RadioGroup mTypeAntSelect;
    private Button mGetBuffer,mGetAndClearBuffer,mClearBuffer,mQueryBufferCount;

    private View mFastSwitchSetPanel;
    private View mBufferSetPanel;
    private View mAntsPanel;
    private View mSessionPanel;

    public PageInventoryReal(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        LayoutInflater.from(context).inflate(layout.page_inventory_real, this);
        try {
            mReaderHelper = ReaderHelper.getDefaultHelper();
            mReader = mReaderHelper.getReader();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mSessionIdList = new ArrayList<String>();
        mInventoriedFlagList = new ArrayList<String>();
        m_curReaderSetting = mReaderHelper.getCurReaderSetting();
        m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
        m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
        m_curOperateTagISO18000Buffer = mReaderHelper
                .getCurOperateTagISO18000Buffer();

        mLogList = (LogList) findViewById(id.log_list);


        mStartStop = (TextView) findViewById(id.startstop);


        mCbRealSet = (CheckBox) findViewById(id.check_real_set);
        mLayoutRealSet = (LinearLayout) findViewById(id.layout_real_set);
        mCbRealSession = (CheckBox) findViewById(id.check_real_session);

        mSessionIdTextView = (TextView) findViewById(id.session_id_text);
        mInventoriedFlagTextView = (TextView) findViewById(id.inventoried_flag_text);
        mDropDownRow1 = (TableRow) findViewById(id.table_row_session_id);
        mDropDownRow2 = (TableRow) findViewById(id.table_row_inventoried_flag);
        mTagsCountText = (TextView) findViewById(id.tags_count_text);
        mTagsTotalText = (TextView) findViewById(id.tags_total_text);
        mTagsSpeedText = (TextView) findViewById(id.tags_speed_text);
        mTagsTimeText = (TextView) findViewById(id.tags_time_text);
        mTagsOpTimeText = (TextView) findViewById(id.tags_op_time_text);
        mTagRealList = (TagRealList) findViewById(id.tag_real_list);
        mRealRoundEditText = (EditText) findViewById(id.real_round_text);

        initAnts();
        initView();

        mStartStop.setOnClickListener(setInventoryRealOnClickListener);

        lbm = LocalBroadcastManager.getInstance(mContext);

        IntentFilter itent = new IntentFilter();
        itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH_TERMINAL);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH);
        lbm.registerReceiver(mRecv, itent);

        mDropDownRow1.setEnabled(mCbRealSession.isChecked());
        mDropDownRow2.setEnabled(mCbRealSession.isChecked());
        mDropDownRow1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showSpinWindow1();
            }
        });
        mDropDownRow2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showSpinWindow2();
            }
        });
        mSessionIdList.clear();
        mInventoriedFlagList.clear();
        String[] lists = getResources().getStringArray(R.array.session_id_list);
        for (int i = 0; i < lists.length; i++) {
            mSessionIdList.add(lists[i]);
        }
        lists = getResources().getStringArray(R.array.inventoried_flag_list);
        for (int i = 0; i < lists.length; i++) {
            mInventoriedFlagList.add(lists[i]);
        }

        mSpinerPopWindow1 = new SpinerPopWindow(mContext);
        mSpinerPopWindow1.refreshData(mSessionIdList, 0);
        mSpinerPopWindow1.setItemListener(new IOnItemSelectListener() {
            public void onItemClick(int pos) {
                setSessionIdText(pos);
            }
        });

        mSpinerPopWindow2 = new SpinerPopWindow(mContext);
        mSpinerPopWindow2.refreshData(mInventoriedFlagList, 0);
        mSpinerPopWindow2.setItemListener(new IOnItemSelectListener() {
            public void onItemClick(int pos) {
                setInventoriedFlagText(pos);
            }
        });

        updateView();

        mCbRealSet.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (mCbRealSet.isChecked()) {
                    mLayoutRealSet.setVisibility(View.VISIBLE);
                } else {
                    mLayoutRealSet.setVisibility(View.GONE);
                    mBufferInventoryCheck.setChecked(false);
                    mFastSwitchCheck.setChecked(false);
                    mTypeAnt1.setChecked(true);
                    mAnt1.setChecked(true);
                    mAnt2.setChecked(false);
                    mAnt3.setChecked(false);
                    mAnt4.setChecked(false);
                    mAnt5.setChecked(false);
                    mAnt6.setChecked(false);
                    mAnt7.setChecked(false);
                    mAnt8.setChecked(false);

                    mAnt1.setEnabled(true);
                    mAnt2.setEnabled(false);
                    mAnt3.setEnabled(false);
                    mAnt4.setEnabled(false);
                    mAnt5.setEnabled(false);
                    mAnt6.setEnabled(false);
                    mAnt7.setEnabled(false);
                    mAnt8.setEnabled(false);

                }

                mDropDownRow1.setEnabled(mCbRealSession.isChecked());
                mDropDownRow2.setEnabled(mCbRealSession.isChecked());
            }
        });

        mCbRealSession
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton arg0,
                                                 boolean arg1) {
                        mDropDownRow1.setEnabled(mCbRealSession.isChecked());
                        mDropDownRow2.setEnabled(mCbRealSession.isChecked());
                    }
                });

        if (mReaderHelper.getInventoryFlag()) {
            mHandler.removeCallbacks(mRefreshRunnable);
            mHandler.postDelayed(mRefreshRunnable, 2000);
        }

        // start_add by lei.li 2016/11/09
        //refreshText();
        //refreshList();
        refresh();
        // end_add by lei.li 2016/11/09

        refreshStartStop(mReaderHelper.getInventoryFlag());
    }

    OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            switch (id) {
                case R.id.select_ant1:
                    if (isChecked) {
                        m_curInventoryBuffer.lAntenna.add(new Byte((byte) 0x00));
                    } else {
                        m_curInventoryBuffer.lAntenna.remove(new Byte((byte) 0x00));
                        m_curInventoryBuffer.nIndexAntenna = 0;
                    }
                    break;
                case R.id.select_ant2:
                    if (isChecked) {
                        m_curInventoryBuffer.lAntenna.add(new Byte((byte) 0x01));
                    } else {
                        m_curInventoryBuffer.lAntenna.remove(new Byte((byte) 0x01));
                        m_curInventoryBuffer.nIndexAntenna = 0;
                    }
                    break;
                case R.id.select_ant3:
                    if (isChecked) {
                        m_curInventoryBuffer.lAntenna.add(new Byte((byte) 0x02));
                    } else {
                        m_curInventoryBuffer.lAntenna.remove(new Byte((byte) 0x02));
                        m_curInventoryBuffer.nIndexAntenna = 0;
                    }
                    break;
                case R.id.select_ant4:
                    if (isChecked) {
                        m_curInventoryBuffer.lAntenna.add(new Byte((byte) 0x03));
                    } else {
                        m_curInventoryBuffer.lAntenna.remove(new Byte((byte) 0x03));
                        m_curInventoryBuffer.nIndexAntenna = 0;
                    }
                    break;
                case R.id.select_ant5:
                    if (isChecked) {
                        m_curInventoryBuffer.lAntenna.add(new Byte((byte) 0x04));
                    } else {
                        m_curInventoryBuffer.lAntenna.remove(new Byte((byte) 0x04));
                        m_curInventoryBuffer.nIndexAntenna = 0;
                    }
                    break;
                case R.id.select_ant6:
                    if (isChecked) {
                        m_curInventoryBuffer.lAntenna.add(new Byte((byte) 0x05));
                    } else {
                        m_curInventoryBuffer.lAntenna.remove(new Byte((byte) 0x05));
                        m_curInventoryBuffer.nIndexAntenna = 0;
                    }
                    break;
                case R.id.select_ant7:
                    if (isChecked) {
                        m_curInventoryBuffer.lAntenna.add(new Byte((byte) 0x06));
                    } else {
                        m_curInventoryBuffer.lAntenna.remove(new Byte((byte) 0x06));
                        m_curInventoryBuffer.nIndexAntenna = 0;
                    }
                    break;
                case R.id.select_ant8:
                    if (isChecked) {
                        m_curInventoryBuffer.lAntenna.add(new Byte((byte) 0x07));
                    } else {
                        m_curInventoryBuffer.lAntenna.remove(new Byte((byte) 0x07));
                        m_curInventoryBuffer.nIndexAntenna = 0;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void initAnts() {
        mAnt1 = (CheckBox) findViewById(id.select_ant1);
        mAnt2 = (CheckBox) findViewById(id.select_ant2);
        mAnt3 = (CheckBox) findViewById(id.select_ant3);
        mAnt4 = (CheckBox) findViewById(id.select_ant4);
        mAnt5 = (CheckBox) findViewById(id.select_ant5);
        mAnt6 = (CheckBox) findViewById(id.select_ant6);
        mAnt7 = (CheckBox) findViewById(id.select_ant7);
        mAnt8 = (CheckBox) findViewById(id.select_ant8);
        mAnt1.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mAnt2.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mAnt3.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mAnt4.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mAnt5.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mAnt6.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mAnt7.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mAnt8.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    public void initView() {
        mFastAnt1 = findViewById(id.fast_switch_ant1);
        mFastAnt2 = findViewById(id.fast_switch_ant2);
        mFastAnt3 = findViewById(id.fast_switch_ant3);
        mFastAnt4 = findViewById(id.fast_switch_ant4);
        mFastAnt5 = findViewById(id.fast_switch_ant5);
        mFastAnt6 = findViewById(id.fast_switch_ant6);
        mFastAnt7 = findViewById(id.fast_switch_ant7);
        mFastAnt8 = findViewById(id.fast_switch_ant8);

        mFastAnt1.setSelection(0);
        mFastAnt2.setSelection(1);
        mFastAnt3.setSelection(2);
        mFastAnt4.setSelection(3);
        mFastAnt5.setSelection(4);
        mFastAnt6.setSelection(5);
        mFastAnt7.setSelection(6);
        mFastAnt8.setSelection(7);

        mFastLoop1 = findViewById(id.fast_switch_loop1);
        mFastLoop2 = findViewById(id.fast_switch_loop2);
        mFastLoop3 = findViewById(id.fast_switch_loop3);
        mFastLoop4 = findViewById(id.fast_switch_loop4);
        mFastLoop5 = findViewById(id.fast_switch_loop5);
        mFastLoop6 = findViewById(id.fast_switch_loop6);
        mFastLoop7 = findViewById(id.fast_switch_loop7);
        mFastLoop8 = findViewById(id.fast_switch_loop8);


        mFastRepeat1 = findViewById(id.fast_switch_repeat1);
        mFastRepeat2 = findViewById(id.fast_switch_repeat2);
        mFastRepeat3 = findViewById(id.fast_switch_repeat3);
        mFastRepeat4 = findViewById(id.fast_switch_repeat4);

        mFastReserve1 = findViewById(id.fast_switch_reserve1);
        mFastReserve2 = findViewById(id.fast_switch_reserve2);
        mFastReserve3 = findViewById(id.fast_switch_reserve3);
        mFastReserve4 = findViewById(id.fast_switch_reserve4);
        mFastReserve5 = findViewById(id.fast_switch_reserve5);

        mFastSwitchCheck = findViewById(id.fast_switch_antenna_inventory);
        mBufferInventoryCheck = findViewById(id.buffer_inventory);
        mTypeAnt1 = findViewById(id.ant_1);
        mTypeAnt4 = findViewById(id.ant_4);
        mTypeAnt8 = findViewById(id.ant_8);

        mFastSessionTarget = findViewById(id.fast_switch_session_select);
        mFastSessionFlag = findViewById(id.fast_swithc_session_flag);
        mFastSession = findViewById(id.fast_switch_session);
        mFastPhase = findViewById(id.fast_switch_phase);
        mFastDynamic = findViewById(id.fast_switch_dynamic_polling);
        mFastInterval = findViewById(id.fast_switch_interval);
        mFastRepeat = findViewById(id.fast_switch_repeat);
        mFastOpitized = findViewById(id.fast_switch_optimized);

        mFastContinus = findViewById(id.fast_switch_continua);
        mFastTargetQuantity = findViewById(id.fast_switch_target_quantity);
        mFastRunTimes = findViewById(id.fast_switch_run_times);
        mTimeInterval = findViewById(id.fast_switch_time_interval);

        mFastSwitchSetPanel = findViewById(id.fast_switch_set_panel);
        mBufferSetPanel = findViewById(id.buffer_inventory_panel);
        mAntsPanel = findViewById(id.ants_set_panel);
        mSessionPanel = findViewById(id.session_set_panel);

        mTypeAntSelect = findViewById(id.ant_type_select);

        mGetBuffer = findViewById(id.get_buffer);
        mGetAndClearBuffer = findViewById(id.get_and_clear_buffer);
        mClearBuffer = findViewById(id.clear_buffer);
        mQueryBufferCount = findViewById(id.query_buffer);

        OnClickListener bufferAction = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case id.get_buffer:
                        m_curInventoryBuffer.clearInventoryResult();
                        mReader.getInventoryBuffer(m_curReaderSetting.btReadId);
                        break;
                    case id.get_and_clear_buffer:
                        m_curInventoryBuffer.clearInventoryResult();
                        mReader.getAndResetInventoryBuffer(m_curReaderSetting.btReadId);
                        break;
                    case id.clear_buffer:
                        mReader.resetInventoryBuffer(m_curReaderSetting.btReadId);
                        break;
                    case id.query_buffer:
                        mReader.getInventoryBufferTagCount(m_curReaderSetting.btReadId);
                        break;
                }
            }
        };

        mGetBuffer.setOnClickListener(bufferAction);
        mGetAndClearBuffer.setOnClickListener(bufferAction);
        mClearBuffer.setOnClickListener(bufferAction);
        mQueryBufferCount.setOnClickListener(bufferAction);

        mFastSwitchCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mAntsPanel.setVisibility(View.GONE);
                    mFastSwitchSetPanel.setVisibility(View.VISIBLE);
                    mSessionPanel.setVisibility(View.GONE);
                    mBufferSetPanel.setVisibility(View.GONE);
                    if (mBufferInventoryCheck.isChecked()) {
                        mBufferInventoryCheck.setChecked(false);
                    }
                } else {
                    if (mBufferInventoryCheck.isChecked())
                        return;
                    mAntsPanel.setVisibility(View.VISIBLE);
                    mFastSwitchSetPanel.setVisibility(View.GONE);
                    mBufferSetPanel.setVisibility(View.GONE);
                    mSessionPanel.setVisibility(View.VISIBLE);
                }
            }
        });

        mBufferInventoryCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTagRealList.isCheckedBufferInventory(isChecked);
                if (isChecked) {
                    mBufferSetPanel.setVisibility(View.VISIBLE);
                    mAntsPanel.setVisibility(View.VISIBLE);
                    mSessionPanel.setVisibility(View.GONE);
                    mFastSwitchSetPanel.setVisibility(View.GONE);
                    if (mFastSwitchCheck.isChecked()) {
                        mFastSwitchCheck.setChecked(false);
                    }
                } else {
                    if (mFastSwitchCheck.isChecked())
                        return;
                    mAntsPanel.setVisibility(View.VISIBLE);
                    mFastSwitchSetPanel.setVisibility(View.GONE);
                    mBufferSetPanel.setVisibility(View.GONE);
                    mSessionPanel.setVisibility(View.VISIBLE);
                }
            }
        });

        mTypeAntSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case id.ant_1:
                        mAnt1.setEnabled(true);
                        mAnt2.setEnabled(false);
                        mAnt3.setEnabled(false);
                        mAnt4.setEnabled(false);
                        mAnt5.setEnabled(false);
                        mAnt6.setEnabled(false);
                        mAnt7.setEnabled(false);
                        mAnt8.setEnabled(false);
                        if (!mBufferInventoryCheck.isChecked())
                            mTagRealList.showSelectAntsCount(1);
                        break;
                    case id.ant_4:
                        mAnt1.setEnabled(true);
                        mAnt2.setEnabled(true);
                        mAnt3.setEnabled(true);
                        mAnt4.setEnabled(true);
                        mAnt5.setEnabled(false);
                        mAnt6.setEnabled(false);
                        mAnt7.setEnabled(false);
                        mAnt8.setEnabled(false);
                        if (!mBufferInventoryCheck.isChecked())
                            mTagRealList.showSelectAntsCount(4);
                        break;
                    case id.ant_8:
                        mAnt1.setEnabled(true);
                        mAnt2.setEnabled(true);
                        mAnt3.setEnabled(true);
                        mAnt4.setEnabled(true);
                        mAnt5.setEnabled(true);
                        mAnt6.setEnabled(true);
                        mAnt7.setEnabled(true);
                        mAnt8.setEnabled(true);
                        if (!mBufferInventoryCheck.isChecked())
                            mTagRealList.showSelectAntsCount(8);
                        break;
                }
            }
        });

        mFastPhase.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTagRealList.isShowPhase(isChecked);
                mReaderHelper.setShowPhase(isChecked);
            }
        });
        //mBufferSetPanel = findViewById(id.panel);
    }

    public void refresh() {
        m_curInventoryBuffer.clearInventoryRealResult();
        refreshList();
        refreshText();
        clearText();
        mRealRoundEditText.setText("1");
        //clearAnt();
    }

    private void clearAnt() {
        mAnt1.setChecked(true);
        mAnt2.setChecked(false);
        mAnt3.setChecked(false);
        mAnt4.setChecked(false);
        mAnt5.setChecked(false);
        mAnt6.setChecked(false);
        mAnt7.setChecked(false);
        mAnt8.setChecked(false);
        m_curInventoryBuffer.clearInventoryPar();
    }


    @SuppressWarnings("deprecation")
    private void refreshStartStop(boolean start) {
        if (start) {
            refresh();
            mStartStop.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.button_disenabled_background));
            mStartStop.setText(getResources()
                    .getString(R.string.stop_inventory));
        } else {
            mStartStop.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.button_background));
            mStartStop.setText(getResources().getString(
                    R.string.start_inventory));
        }
    }

    private Handler mHandler = new Handler();
    private Runnable mRefreshRunnable = new Runnable() {
        public void run() {
            //refreshList();
            mHandler.postDelayed(this, 2000);
        }
    };
    private Handler mLoopHandler = new Handler();
    private Runnable mLoopRunnable = new Runnable() {
        public void run() {
            /*
             * byte btWorkAntenna =
             * m_curInventoryBuffer.lAntenna.get(m_curInventoryBuffer
             * .nIndexAntenna); if (btWorkAntenna < 0) btWorkAntenna = 0;
             * mReader.setWorkAntenna(m_curReaderSetting.btReadId,
             * btWorkAntenna);
             */
            mReaderHelper.runLoopInventroy();
            mLoopHandler.postDelayed(this, 2000);
        }
    };

    private void setSessionIdText(int pos) {
        if (pos >= 0 && pos < mSessionIdList.size()) {
            String value = mSessionIdList.get(pos);
            mSessionIdTextView.setText(value);
            mPos1 = pos;
        }
    }

    private void setInventoriedFlagText(int pos) {
        if (pos >= 0 && pos < mInventoriedFlagList.size()) {
            String value = mInventoriedFlagList.get(pos);
            mInventoriedFlagTextView.setText(value);
            mPos2 = pos;
        }
    }

    private void showSpinWindow1() {
        mSpinerPopWindow1.setWidth(mDropDownRow1.getWidth());
        mSpinerPopWindow1.showAsDropDown(mDropDownRow1);
    }

    private void showSpinWindow2() {
        mSpinerPopWindow2.setWidth(mDropDownRow2.getWidth());
        mSpinerPopWindow2.showAsDropDown(mDropDownRow2);
    }

    private void updateView() {

        if (m_curInventoryBuffer.bLoopCustomizedSession) {
            mCbRealSet.setChecked(true);
            mLayoutRealSet.setVisibility(View.VISIBLE);
        } else {
            mCbRealSet.setChecked(false);
            mLayoutRealSet.setVisibility(View.GONE);
        }

        mPos1 = m_curInventoryBuffer.btSession;
        mPos2 = m_curInventoryBuffer.btTarget;

        if (m_curInventoryBuffer.lAntenna.size() <= 0)
            m_curInventoryBuffer.lAntenna.add(new Byte((byte) 0x00));

        int nRepeat = m_curInventoryBuffer.btRepeat & 0xFF;
        mRealRoundEditText.setText(String.valueOf(nRepeat <= 0 ? 1 : nRepeat));

        setSessionIdText(mPos1);
        setInventoriedFlagText(mPos2);
    }

    private void startstop() {
        bTmpInventoryFlag = false;
        //m_curInventoryBuffer.clearInventoryPar();
        m_curInventoryBuffer.btRepeat = 0x00;
        m_curInventoryBuffer.nIndexAntenna = 0;
        m_curInventoryBuffer.nCommond = 0;
        m_curInventoryBuffer.bLoopInventoryReal = false;

        //m_curInventoryBuffer.lAntenna.add((byte) 0x00);


        m_curInventoryBuffer.bLoopInventoryReal = true;
        m_curInventoryBuffer.btRepeat = 0;

        String strRepeat = mRealRoundEditText.getText().toString();
        if (strRepeat == null || strRepeat.length() <= 0) {
            Toast.makeText(mContext,
                    getResources().getString(R.string.repeat_empty),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        m_curInventoryBuffer.btRepeat = (byte) Integer.parseInt(strRepeat);

        if ((m_curInventoryBuffer.btRepeat & 0xFF) <= 0) {
            Toast.makeText(mContext,
                    getResources().getString(R.string.repeat_min),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //设置了高级选项
        if(mCbRealSet.isChecked()) {
            //mCbRealSession=自定义session参数
            if (mCbRealSet.isChecked() && mCbRealSession.isChecked()) {
                m_curInventoryBuffer.bLoopCustomizedSession = true;
                m_curInventoryBuffer.btSession = (byte) (mPos1 & 0xFF);
                m_curInventoryBuffer.btTarget = (byte) (mPos2 & 0xFF);
            }
            //缓存模式是否开启
            else if (mBufferInventoryCheck.isChecked()) {
                m_curInventoryBuffer.bLoopInventoryReal = false;
                m_curInventoryBuffer.bLoopInventory = true;
                m_curInventoryBuffer.bLoopCustomizedSession = false;
            }
            //快速切换天线
            else if(mFastSwitchCheck.isChecked()) {
                m_curInventoryBuffer.bLoopInventoryReal = false;
                m_curInventoryBuffer.bLoopInventory = true;
                m_curInventoryBuffer.bLoopCustomizedSession = false;
                initFastSwitchParamter();

                if (!mStartStop.getText().toString()
                        .equals(getResources().getString(R.string.start_inventory))) {
                    refreshText();
                    mReaderHelper.setInventoryFlag(false);
                    m_curInventoryBuffer.bLoopInventoryReal = false;
                    m_curInventoryBuffer.bLoopInventory = false;
                    m_curInventoryBuffer.bLoopCustomizedSession = false;
                    refreshStartStop(false);
                    refreshList();
                    return;
                } else {
                    mReaderHelper.runLoopFastSwitch();
                    refreshStartStop(true);
                }
                return;
            }else {
                m_curInventoryBuffer.bLoopInventory = false;
                m_curInventoryBuffer.bLoopCustomizedSession = false;
            }
        }

        //结束存盘
        if (!mStartStop.getText().toString()
                .equals(getResources().getString(R.string.start_inventory))) {
            refreshText();
            mReaderHelper.setInventoryFlag(false);
            m_curInventoryBuffer.bLoopInventoryReal = false;
            m_curInventoryBuffer.bLoopInventory = false;
            m_curInventoryBuffer.bLoopCustomizedSession = false;

            refreshStartStop(false);
            mLoopHandler.removeCallbacks(mLoopRunnable);
            mHandler.removeCallbacks(mRefreshRunnable);
            refreshList();
            return;
        } else {
            //至少选择一个天线
            if (m_curInventoryBuffer.lAntenna.size() <= 0) {
                Toast.makeText(mContext,
                        getResources().getString(R.string.antenna_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            //refreshStartStop(true);
        }

        // start_fixed by lei.li 2016/11/04 problem
        // m_curInventoryBuffer.clearInventoryRealResult();
        mReaderHelper.setInventoryFlag(true);
        // end_fixed by lei.li 2016/11/04

        mReaderHelper.clearInventoryTotal();
        refreshText();
        byte btWorkAntenna = 0;
        if (m_curInventoryBuffer.lAntenna.size() > 0) {
            btWorkAntenna = m_curInventoryBuffer.lAntenna
                    .get(m_curInventoryBuffer.nIndexAntenna);
            if (btWorkAntenna < 0)
                btWorkAntenna = 0;
        }

        mReader.setWorkAntenna(m_curReaderSetting.btReadId, btWorkAntenna);
        //mReaderHelper.runLoopInventroy();
        m_curReaderSetting.btWorkAntenna = btWorkAntenna;
        mRefreshTime = new Date().getTime();
        refreshStartStop(true);
        mLoopHandler.removeCallbacks(mLoopRunnable);
        mLoopHandler.postDelayed(mLoopRunnable, 2000);
        mHandler.removeCallbacks(mRefreshRunnable);
        mHandler.postDelayed(mRefreshRunnable, 2000);
    }

    private OnClickListener setInventoryRealOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View arg0) {
            if (arg0.getId() == id.startstop) {
                startstop();
            }
        }
    };

    private void initFastSwitchParamter() {
        try {
            byte[] antPara = null;
            byte[] session = null;
            byte[] repeats = null;
            if (mTypeAnt1.isChecked()) {
                antPara = new byte[16];
                antPara[0] = (byte)mFastAnt1.getSelectedItemPosition();
                antPara[2] =  (byte)0xFF;
                antPara[4] =  (byte)0xFF;
                antPara[6] =  (byte)0xFF;
                antPara[8] =  (byte)0xFF;
                antPara[10] = (byte)0xFF;
                antPara[12] = (byte)0xFF;
                antPara[14] = (byte)0xFF;

                antPara[1] = Byte.parseByte(mFastLoop1.getText().toString());
                antPara[3] = (byte)0x00;
                antPara[5] = (byte)0x00;
                antPara[7] = (byte)0x00;
                antPara[9] = (byte)0x00;
                antPara[11] =(byte)0x00;
                antPara[13] =(byte)0x00;
                antPara[15] =(byte)0x00;
            } else if (mTypeAnt4.isChecked()) {
                antPara = new byte[16];
                antPara[0] = (byte)mFastAnt1.getSelectedItemPosition();
                antPara[2] = (byte)mFastAnt2.getSelectedItemPosition();
                antPara[4] = (byte)mFastAnt3.getSelectedItemPosition();
                antPara[6] = (byte)mFastAnt4.getSelectedItemPosition();
                antPara[8] = (byte)0xFF;
                antPara[10] =(byte)0xFF;
                antPara[12] =(byte)0xFF;
                antPara[14] =(byte)0xFF;

                antPara[1] = Byte.parseByte(mFastLoop1.getText().toString());
                antPara[3] = Byte.parseByte(mFastLoop2.getText().toString());
                antPara[5] = Byte.parseByte(mFastLoop3.getText().toString());
                antPara[7] = Byte.parseByte(mFastLoop4.getText().toString());
                antPara[9] = (byte)0x00;
                antPara[11] =(byte)0x00;
                antPara[13] =(byte)0x00;
                antPara[15] =(byte)0x00;

            } else if (mTypeAnt8.isChecked()) {
                antPara = new byte[16];
                antPara[0] = (byte)mFastAnt1.getSelectedItemPosition();
                antPara[2] = (byte)mFastAnt2.getSelectedItemPosition();
                antPara[4] = (byte)mFastAnt3.getSelectedItemPosition();
                antPara[6] = (byte)mFastAnt4.getSelectedItemPosition();
                antPara[8] = (byte)mFastAnt5.getSelectedItemPosition();
                antPara[10] = (byte)mFastAnt6.getSelectedItemPosition();
                antPara[12] = (byte)mFastAnt7.getSelectedItemPosition();
                antPara[14] = (byte)mFastAnt8.getSelectedItemPosition();

                antPara[1] = Byte.parseByte(mFastLoop1.getText().toString());
                antPara[3] = Byte.parseByte(mFastLoop2.getText().toString());
                antPara[5] = Byte.parseByte(mFastLoop3.getText().toString());
                antPara[7] = Byte.parseByte(mFastLoop4.getText().toString());
                antPara[9] = Byte.parseByte(mFastLoop5.getText().toString());
                antPara[11] = Byte.parseByte(mFastLoop6.getText().toString());
                antPara[13] = Byte.parseByte(mFastLoop7.getText().toString());
                antPara[15] = Byte.parseByte(mFastLoop8.getText().toString());
            }

            if (mFastSession.isChecked()) {
                session = new byte[11];

                session[5] = (byte)mFastSessionFlag.getSelectedItemPosition();
                session[6] = (byte)mFastSessionTarget.getSelectedItemPosition();
                session[0] = Byte.parseByte(mFastReserve1.getText().toString());
                session[1] = Byte.parseByte(mFastReserve2.getText().toString());
                session[2] = Byte.parseByte(mFastReserve3.getText().toString());
                session[3] = Byte.parseByte(mFastReserve4.getText().toString());
                session[4] = Byte.parseByte(mFastReserve5.getText().toString());

                session[7] = Byte.parseByte(mFastOpitized.getText().toString());
                session[8] = Byte.parseByte(mFastContinus.getText().toString());
                session[9] = Byte.parseByte(mFastTargetQuantity.getText().toString());

                if (mFastPhase.isChecked()) {
                    session[10] = 0x01;
                }
            }

            if (mFastDynamic.isChecked()) {
                repeats = new byte[4];
                repeats[0] = Byte.parseByte(mFastRepeat1.getText().toString());
                repeats[1] = Byte.parseByte(mFastRepeat1.getText().toString());
                repeats[2] = Byte.parseByte(mFastRepeat2.getText().toString());
                repeats[3] = Byte.parseByte(mFastRepeat3.getText().toString());
            }

            byte[] param = null;
            if (!mFastSession.isChecked()) {
                param = new byte[antPara.length + 2];
                System.arraycopy(antPara,0,param,0,antPara.length);
                param[antPara.length] = Byte.parseByte(mFastInterval.getText().toString());
                param[antPara.length + 1] = Byte.parseByte(mFastRepeat.getText().toString());
            } else if (mFastSession.isChecked() && !mFastDynamic.isChecked()) {
                param = new byte[antPara.length + session.length + 2];
                System.arraycopy(antPara,0,param,0,antPara.length);
                param[antPara.length] = Byte.parseByte(mFastInterval.getText().toString());
                System.arraycopy(session,0,param,antPara.length + 1,session.length);
                param[param.length - 1] = Byte.parseByte(mFastRepeat.getText().toString());
            } else if (mFastSession.isChecked() && mFastDynamic.isChecked()) {
                param = new byte[antPara.length + session.length + 2 + repeats.length];
                System.arraycopy(antPara,0,param,0,antPara.length);
                param[antPara.length] = Byte.parseByte(mFastInterval.getText().toString());
                System.arraycopy(session,0,param,antPara.length + 1,session.length);
                System.arraycopy(repeats,0,param,antPara.length + 1 + session.length,repeats.length);
                param[param.length - 1] = Byte.parseByte(mFastRepeat.getText().toString());
            }
            m_curInventoryBuffer.nFastSwitchAntsParams = param;
            int runTimes = Integer.parseInt(mFastRunTimes.getText().toString());
            if (runTimes == 0) {
                throw new Exception();
            }
            m_curInventoryBuffer.nRunTimes = runTimes;
            m_curInventoryBuffer.nTimeInterval = Integer.parseInt(mTimeInterval.getText().toString());
        } catch (Exception e) {
            Toast.makeText(getContext(),"Invaild parameter Exception!",Toast.LENGTH_SHORT).show();
        }
    }


    private void refreshList() {
        mTagRealList.refreshList();
    }

    //acc the total time;
    private static long TotalTime = System.currentTimeMillis();
    //acc the total time

    private void refreshText() {
        mTagsCountText.setText(String.valueOf(m_curInventoryBuffer.lsTagList
                .size()));
        mTagsTotalText
                .setText(String.valueOf(mReaderHelper.getInventoryTotal()));
        // if (m_curInventoryBuffer.nReadRate > 0) {

        mTagsSpeedText.setText(String.valueOf(m_curInventoryBuffer.nReadRate));

        // }
        mTagsTimeText.setText(String
                .valueOf(m_curInventoryBuffer.dtEndInventory.getTime()
                        - m_curInventoryBuffer.dtStartInventory.getTime()));
        if (m_curInventoryBuffer.nReadRate > 0) {
            //rate by lei.li

            mTagsOpTimeText.setText(String
                    .valueOf(m_curInventoryBuffer.nDataCount * 1000
                            / m_curInventoryBuffer.nReadRate));
        } else {
            mTagsOpTimeText.setText("0");
        }
        mTagRealList.refreshText();
    }

    private void clearText() {
        mReaderHelper.setInventoryTotal(0);
        mTagsCountText.setText("0");
        mTagsTotalText.setText("0");
        mTagsSpeedText.setText("0");
        mTagsTimeText.setText("0");
        mTagsOpTimeText.setText("0");
        mTagRealList.clearText();
    }

    // add by lei.li 2016/11/14
    // private boolean bTmpInventoryFlag = false;
    private boolean bTmpInventoryFlag = true;
    // add by lei.li 2016/11/14
    private final BroadcastReceiver mRecv = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL)
                    || intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_INVENTORY)
                    || intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH)) {
                Log.d("Real time receive", Thread.currentThread().getName());
                byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
                switch (btCmd) {
                    case CMD.REAL_TIME_INVENTORY:
                    case CMD.CUSTOMIZED_SESSION_TARGET_INVENTORY:
                        // if (new Date().getTime() - mRefreshTime > 2000) {
                        // refreshList();
                        // mRefreshTime = new Date().getTime();
                        // }
                        // add by lei.li 2016/11/04
                        // refreshStartStop(true);
                        // add by lei.li 2016/11/04
                        // Log.e("zhebian", "?????????????????????????????");

                        if (m_curInventoryBuffer.lsTagList.size() > 0) ;
                    {

                        Log.d("TAGS", m_curInventoryBuffer.lsTagList.get(0).strEPC);
                    }

                    // add by lei.li 2016/11/14
                    if (!DEBUG) {
                        if (!mReaderHelper.getInventoryFlag()) {
                            if (!bTmpInventoryFlag) {
                                bTmpInventoryFlag = true;
                                mHandler.removeCallbacks(mRefreshRunnable);
                                mHandler.postDelayed(mRefreshRunnable, 2000);
                                // rm by lei.li 2016/11/04
                                // m_curInventoryBuffer.clearInventoryRealResult();
                                // mReaderHelper.clearInventoryTotal();
                            }
                        }
                    }
                    // add by lei.li 2016/11/14
                    //refreshList();
                    // add by lei.li 2016/11/14

                    mHandler.removeCallbacks(mRefreshRunnable);
                    mHandler.postDelayed(mRefreshRunnable, 2000);
                    // add by lei.li 2016/11/14
                    mLoopHandler.removeCallbacks(mLoopRunnable);
                    mLoopHandler.postDelayed(mLoopRunnable, 2000);
                    //refreshText();
                    break;
                    case CMD.GET_INVENTORY_BUFFER:
                    case CMD.GET_AND_RESET_INVENTORY_BUFFER:
                        refreshList();
                        refreshText();
                        break;
                    case ReaderHelper.INVENTORY_ERR:
                    case ReaderHelper.INVENTORY_ERR_END:
                    case ReaderHelper.INVENTORY_END:
                        // add by lei.li have some problem why it was annotation
                        // refreshList();
                        refreshList();
                        // add by lei.li

                        // add by lei.li 2016/11/
                        if (mReaderHelper.getInventoryFlag() /* || bTmpInventoryFlag */) {
                            mLoopHandler.removeCallbacks(mLoopRunnable);
                            mLoopHandler.postDelayed(mLoopRunnable, 2000);

                        } else {
                            mLoopHandler.removeCallbacks(mLoopRunnable);
                            // add by lei.li 2016/11/14
                            mHandler.removeCallbacks(mRefreshRunnable);
                            // add by lei.li 2016/11/14
                        }

                        // start_add by lei.li 2016/11/04
                        // refreshStartStop(false);
                        // end_add by lei.li 2016/11/04
                        // start_add by lei.li 2016/11/04
                        refreshText(); // fixed by lei.li 2016/11/04
                        break;
                }

            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_LOG)) {
                mLogList.writeLog((String) intent.getStringExtra("log"),
                        intent.getIntExtra("type", ERROR.SUCCESS));
            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH_TERMINAL)) {
                refreshStartStop(false);
            }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mLogList.tryClose())
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void doDestroy() {
        // TODO Auto-generated method stub

        if (lbm != null)
            lbm.unregisterReceiver(mRecv);
        mLoopHandler.removeCallbacks(mLoopRunnable);
        mHandler.removeCallbacks(mRefreshRunnable);
    }
}

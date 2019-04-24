package com.uhf.uhf.setpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.reader.base.CMD;
import com.reader.base.ERROR;
import com.reader.base.ReaderBase;
import com.reader.base.StringTool;
import com.reader.helper.ISO180006BOperateTagBuffer;
import com.reader.helper.InventoryBuffer;
import com.reader.helper.OperateTagBuffer;
import com.reader.helper.ReaderHelper;
import com.reader.helper.ReaderSetting;
import com.uhf.uhf.LogList;
import com.uhf.uhf.R;
import com.uhf.uhf.spiner.SpinerPopWindow;
import com.uhf.uhf.tagpage.MaskMapAdapter;
import com.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 6/15/2017.
 */

public class PageReaderMaskTag extends BaseActivity {
    //fixed by lei.li 2016/11/09
    //private LogList mLogList;
    private LogList mLogList;
    //fixed by lei.li 2016/11/09

    private TextView mSetStartAdd, mSetMaskLen, mSetMaskValue;
    private Spinner mSelectMaskNo;

    private ListView mListView;
    //private TextView mRefreshButton;
    private List<MaskMapAdapter.MaskMap> mapList = new ArrayList<MaskMapAdapter.MaskMap>();
    private MaskMapAdapter mMaskMapAdapter;

    private List<String> mAccessList;
    private Spinner mMaskNo, mSetTarget, mSetAction, mSetMembank, mGetTarget, mGetAction, mGetMembank;

    private SpinerPopWindow mSpinerPopWindow ;
    private List<String> listMap = new ArrayList<String>();
    private String[] listMapRes = new String[]{"Mask All","Mask No.1","Mask No.2","Mask No.3","Mask No.4","Mask No.5"};

    Button mSet, mGet, mClear;


    private ReaderHelper mReaderHelper;
    private ReaderBase mReader;

    private int mPos = -1;

    private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;

    private LocalBroadcastManager lbm;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_reader_mask_tag);
        mContext = getApplicationContext();
        try {
            mReaderHelper = ReaderHelper.getDefaultHelper();
            mReader = mReaderHelper.getReader();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mAccessList = new ArrayList<String>();

        m_curReaderSetting = mReaderHelper.getCurReaderSetting();
        m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
        m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
        m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

        mLogList = (LogList) findViewById(R.id.log_list);
        mListView = (ListView) findViewById(R.id.get_mask);
        mMaskMapAdapter = new MaskMapAdapter(mContext,mapList);
        mListView.setAdapter(mMaskMapAdapter);

        initView();

        lbm = LocalBroadcastManager.getInstance(mContext);

        IntentFilter itent = new IntentFilter();
        itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
        lbm.registerReceiver(mRecv, itent);

//		mRefreshButton = (TextView) findViewById(R.id.refresh);
//		mRefreshButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				refresh();
//			}
//		});
    }

    void initView() {
        mSetStartAdd = (TextView) findViewById(R.id.mask_start_address);
        mSetMaskLen = (TextView) findViewById(R.id.mask_len);
        mSetMaskValue = (TextView) findViewById(R.id.mask_value);

        /*mGetStartAdd = (TextView) findViewById(R.id.get_mask_start_address);
        mGetMaskLen = (TextView) findViewById(R.id.get_mask_len);
        mGetMaskValue = (TextView) findViewById(R.id.mask_value);*/
        mMaskNo = (Spinner) findViewById(R.id.mask_no);
        mSetTarget = (Spinner) findViewById(R.id.mask_target);
        mSetAction = (Spinner) findViewById(R.id.mask_action);
        mSetMembank = (Spinner) findViewById(R.id.mask_membank);

        mSelectMaskNo = (Spinner) findViewById(R.id.select_mask_no);

       /* mGetTarget = (Spinner) findViewById(R.id.get_mask_target);
        mGetAction = (Spinner) findViewById(R.id.get_mask_action);
        mGetMembank = (Spinner) findViewById(R.id.get_mask_membank);*/

        mSet = (Button) findViewById(R.id.set_mask);
        mGet = (Button) findViewById(R.id.get_mask_button);
        mClear = (Button) findViewById(R.id.clear_mask);

        mSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMask();
            }
        });

        mGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMask();
            }
        });

        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });

        //mClear = (Button)findViewById(R.id.);
    }

    private void setMask() {

        byte maskNo = (byte) (mMaskNo.getSelectedItemId() + 1);
        byte target = (byte) mSetTarget.getSelectedItemId();
        byte action = (byte) mSetAction.getSelectedItemId();
        byte membank = (byte) mSetMembank.getSelectedItemId();

        boolean lenVaild = (mSetStartAdd.getText().length() == 2) ? ((mSetMaskLen.getText().length() == 2) ? true : false) : false;
        if (!lenVaild ) {
            Toast.makeText(mContext, "Mask start address and mask length must be 00 - FF", Toast.LENGTH_SHORT).show();
            return;
        }
        byte nStartAdd = (byte) 0xFF;
        byte nMaskLen = (byte) 0xFF;
        byte[] nMaskValue;
        String[] tmp1 = StringTool.stringToStringArray(mSetStartAdd.getText().toString().trim(), 2);
        String[] tmp2 = StringTool.stringToStringArray(mSetMaskLen.getText().toString().trim(), 2);
        nStartAdd = StringTool.stringArrayToByteArray(tmp1, tmp1.length)[0];
        nMaskLen = StringTool.stringArrayToByteArray(tmp2, tmp2.length)[0];
        String str = mSetMaskValue.getText().toString();
        if (str.length() == 0 || str == null || str == "" || str.length() % 2 != 0) {
            Toast.makeText(mContext,"Mask value format invalid!",Toast.LENGTH_SHORT).show();
            return;
        }
        String[] tmp = StringTool.stringToStringArray(mSetMaskValue.getText().toString().trim(), 2);
        nMaskValue = StringTool.stringArrayToByteArray(tmp, tmp.length);
        mReader.setTagMask((byte) 0x01,maskNo, target, action, membank, nStartAdd, nMaskLen, nMaskValue);
    }

    private void clear() {
        byte maskNo = (byte) mSelectMaskNo.getSelectedItemId();
        mReader.clearTagMask((byte) 0xFF,maskNo);
    }

    private void getMask() {
        mapList.clear();
        mMaskMapAdapter.notifyDataSetChanged();
        mReader.getTagMask((byte) 0xFF);
    }

    private void updateGetValue() {
        if (m_curReaderSetting.btsGetMaskValue == null) {
            return;
        }
        Log.d("Return value", StringTool.byteArrayToString(m_curReaderSetting.btsGetMaskValue,0,m_curReaderSetting.btsGetMaskValue.length));
        MaskMapAdapter.MaskMap map = new MaskMapAdapter.MaskMap();
        map.mMaskNo = String.format("%02X",m_curReaderSetting.btsGetMaskValue[0]);
        byte nTarget = m_curReaderSetting.btsGetMaskValue[2];
        switch (nTarget) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            default:
                break;

        }
        map.mTarget = String.format("%02X",m_curReaderSetting.btsGetMaskValue[2]);
        map.mAction = String.format("%02X",m_curReaderSetting.btsGetMaskValue[3]);
        map.mMembank = String.format("%02X",m_curReaderSetting.btsGetMaskValue[4]);
        map.mStartMaskAdd = String.format("%02X",m_curReaderSetting.btsGetMaskValue[5]);
        map.mMaskLen = String.format("%02X",m_curReaderSetting.btsGetMaskValue[6]);
        byte[] tmp = new byte[m_curReaderSetting.btsGetMaskValue.length - 8];
        System.arraycopy(m_curReaderSetting.btsGetMaskValue,7,tmp,0,tmp.length);
        map.mMaskValue = StringTool.byteArrayToString(tmp,0,tmp.length);
        mapList.add(map);
        mMaskMapAdapter.notifyDataSetChanged();
    }


    public void refresh() {
        m_curOperateTagBuffer.clearBuffer();
        //add by lei.li 2017/1/16
        mAccessList.clear();
        m_curInventoryBuffer.lsTagList.clear();
        mAccessList.add("cancel");
    }


    private final BroadcastReceiver mRecv = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
                byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
                Log.d("This place","This place! " + btCmd);
                switch (btCmd) {
                    case CMD.GET_ACCESS_EPC_MATCH:
                        break;
                    case CMD.OPERATE_TAG_MASK:
                        updateGetValue();
                        break;
                    default:
                        break;
                }
            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_LOG)) {
                remind(intent);
            }
        }
    };

    private void remind(Intent intent) {
        int type = intent.getIntExtra("type", ERROR.SUCCESS);
        /*if (type == 0x10) {
            Toast.makeText(mContext, "Command execute Success!", Toast.LENGTH_SHORT).show();
        } else if (type == 0x11) {
            Toast.makeText(mContext, "Command execute error!", Toast.LENGTH_SHORT).show();
        }*/
        mLogList.writeLog((String) intent.getStringExtra("log"), intent.getIntExtra("type", ERROR.SUCCESS));
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mLogList.tryClose()) return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void doDestroy() {
        // TODO Auto-generated method stub
        if (lbm != null)
            lbm.unregisterReceiver(mRecv);
    }
}

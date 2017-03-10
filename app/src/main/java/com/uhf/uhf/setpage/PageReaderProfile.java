package com.uhf.uhf.setpage;


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
import com.uhf.uhf.UHFApplication;
import com.uhf.uhf.R.id;
import com.uhf.uhf.R.layout;
import com.ui.base.BaseActivity;

import android.R.integer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class PageReaderProfile extends BaseActivity {
	private LogList mLogList;
	
	private TextView mSet, mGet;
	
	private RadioGroup mGroupProfile;
	
	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;
	
	private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
    
    private LocalBroadcastManager lbm = null;
    
    @Override
    protected void onResume() {
    	if (mReader != null) {
    		if (!mReader.IsAlive())
    			mReader.StartWait();
    	}
    	super.onResume();
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.page_reader_profile);
		((UHFApplication) getApplication()).addActivity(this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

		mLogList = (LogList) findViewById(id.log_list);
		mSet = (TextView) findViewById(id.set);
		mGet = (TextView) findViewById(id.get);
		mGroupProfile =  (RadioGroup) findViewById(id.group_profile);
		
		mSet.setOnClickListener(setProfileOnClickListener);
		mGet.setOnClickListener(setProfileOnClickListener);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
		lbm.registerReceiver(mRecv, itent);
		
		updateView();
	}
	
	private OnClickListener setProfileOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			switch(arg0.getId()) {
			case id.get:
				mReader.getRfLinkProfile(m_curReaderSetting.btReadId);
				break;
			case id.set:
				byte btProfile = 0;
				switch (mGroupProfile.getCheckedRadioButtonId()) {
				case id.set_profile_option0:
					btProfile = (byte) 0xD0;
					break;
				case id.set_profile_option1:
					btProfile = (byte) 0xD1;
					break;
				case id.set_profile_option2:
					btProfile = (byte) 0xD2;
					break;
				case id.set_profile_option3:
					btProfile = (byte) 0xD3;
					break;
				default:
					return;
				}
				mReader.setRfLinkProfile(m_curReaderSetting.btReadId, btProfile);
				m_curReaderSetting.btRfLinkProfile = btProfile;
			}
		}
	};
	
	private void updateView() {
		if ((m_curReaderSetting.btRfLinkProfile & 0xFF) == 0xD0) {
			mGroupProfile.check(id.set_profile_option0);
		} else if ((m_curReaderSetting.btRfLinkProfile & 0xFF) == 0xD1) {
			mGroupProfile.check(id.set_profile_option1);
		} else if ((m_curReaderSetting.btRfLinkProfile & 0xFF) == 0xD2) {
			mGroupProfile.check(id.set_profile_option2);
		} else if ((m_curReaderSetting.btRfLinkProfile & 0xFF) == 0xD3) {
			mGroupProfile.check(id.set_profile_option3);
		}
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				if (btCmd == CMD.GET_RF_LINK_PROFILE || btCmd == CMD.SET_RF_LINK_PROFILE) {
					updateView();
				}
            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_LOG)) {
            	mLogList.writeLog((String)intent.getStringExtra("log"), intent.getIntExtra("type", ERROR.SUCCESS));
            }
		}
	};
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mLogList.tryClose()) return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (lbm != null)
			lbm.unregisterReceiver(mRecv);
	}
}


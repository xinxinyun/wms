package com.uhf.uhf.setpage;


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
import com.uhf.uhf.R.id;
import com.uhf.uhf.R.layout;
import com.uhf.uhf.VehicleApplication;
import com.ui.base.BaseActivity;

public class PageReaderOutPower extends BaseActivity {
	private LogList mLogList;
	
	private TextView mSet;
	private TextView mGet;
	
	private EditText mOutPowerText1;
	private EditText mOutPowerText2;
	private EditText mOutPowerText3;
	private EditText mOutPowerText4;
	private EditText mOutPowerText5;
	private EditText mOutPowerText6;
	private EditText mOutPowerText7;
	private EditText mOutPowerText8;

	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;
	
	private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
    
    private LocalBroadcastManager lbm;
    
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
		setContentView(layout.page_reader_out_power);
		((VehicleApplication) getApplication()).addActivity(this);
		
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
		mOutPowerText1 = (EditText) findViewById(id.out_power_text1);
		mOutPowerText2 = (EditText) findViewById(id.out_power_text2);
		mOutPowerText3 = (EditText) findViewById(id.out_power_text3);
		mOutPowerText4 = (EditText) findViewById(id.out_power_text4);
		mOutPowerText5 = (EditText) findViewById(id.out_power_text5);
		mOutPowerText6 = (EditText) findViewById(id.out_power_text6);
		mOutPowerText7 = (EditText) findViewById(id.out_power_text7);
		mOutPowerText8 = (EditText) findViewById(id.out_power_text8);

		mSet.setOnClickListener(setOutPowerOnClickListener);
		
		mGet.setOnClickListener(setOutPowerOnClickListener);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
		lbm.registerReceiver(mRecv, itent);
		
		updateView();
	}
	
	private void updateView() {
		
		if (m_curReaderSetting.btAryOutputPower != null) {
			mOutPowerText1.setText(String.valueOf(m_curReaderSetting.btAryOutputPower[0] & 0xFF));
			mOutPowerText2.setText(String.valueOf(m_curReaderSetting.btAryOutputPower[1] & 0xFF));
			mOutPowerText3.setText(String.valueOf(m_curReaderSetting.btAryOutputPower[2] & 0xFF));
			mOutPowerText4.setText(String.valueOf(m_curReaderSetting.btAryOutputPower[3] & 0xFF));
			mOutPowerText5.setText(String.valueOf(m_curReaderSetting.btAryOutputPower[4] & 0xFF));
			mOutPowerText6.setText(String.valueOf(m_curReaderSetting.btAryOutputPower[5] & 0xFF));
			mOutPowerText7.setText(String.valueOf(m_curReaderSetting.btAryOutputPower[6] & 0xFF));
			mOutPowerText8.setText(String.valueOf(m_curReaderSetting.btAryOutputPower[7] & 0xFF));
		}
	}
	
	private OnClickListener setOutPowerOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			switch(arg0.getId()) {
			case id.get:
				mReader.getOutputPower(m_curReaderSetting.btReadId);
				break;
			case id.set:
				byte btOutputPower[] = new byte[8];
				try {
					btOutputPower[0] = (byte)Integer.parseInt(mOutPowerText1.getText().toString());
					btOutputPower[1] = (byte)Integer.parseInt(mOutPowerText2.getText().toString());
					btOutputPower[2] = (byte)Integer.parseInt(mOutPowerText3.getText().toString());
					btOutputPower[3] = (byte)Integer.parseInt(mOutPowerText4.getText().toString());
					btOutputPower[4] = (byte)Integer.parseInt(mOutPowerText5.getText().toString());
					btOutputPower[5] = (byte)Integer.parseInt(mOutPowerText6.getText().toString());
					btOutputPower[6] = (byte)Integer.parseInt(mOutPowerText7.getText().toString());
					btOutputPower[7] = (byte)Integer.parseInt(mOutPowerText8.getText().toString());
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),"InValid number",Toast.LENGTH_SHORT).show();
					return;
				}
				
				mReader.setOutputPower(m_curReaderSetting.btReadId, btOutputPower);
				m_curReaderSetting.btAryOutputPower = btOutputPower;
				break;
			}
		}
	};
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				if (btCmd == CMD.GET_OUTPUT_POWER || btCmd == CMD.SET_OUTPUT_POWER) {
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


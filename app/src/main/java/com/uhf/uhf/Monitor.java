package com.uhf.uhf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.reader.base.Converter;
import com.reader.base.ERROR;
import com.reader.base.MessageTran;
import com.reader.base.ReaderBase;
import com.reader.base.StringTool;
import com.reader.helper.ISO180006BOperateTagBuffer;
import com.reader.helper.InventoryBuffer;
import com.reader.helper.OperateTagBuffer;
import com.reader.helper.ReaderHelper;
import com.reader.helper.ReaderSetting;
import com.ui.base.BaseActivity;
import com.ui.base.PreferenceUtil;

public class Monitor extends BaseActivity {
	
	public static final String mIsChecked = "MonitorisOpen";
	
	private Context mContext;
	
	private TextView mDataSendButton;
	private HexEditTextBox mDataText;
	private HexEditTextBox mDataCheck;
	
	private CheckBox mCheckOpenMonitor;
	private TextView mRefreshButton;
	private ListView mMonitorList;
	private ArrayAdapter<CharSequence> mMonitorListAdapter;
	
	private ReaderBase mReader;
	private ReaderHelper mReaderHelper;
	
	private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;

    private LocalBroadcastManager lbm;
    
    private UHFApplication app;
    
    @Override
    protected void onResume() {
        super.onResume();
    	if (mReader != null) {
    		if (!mReader.IsAlive())
    			mReader.StartWait();
    	}
    	MainActivity.mIsMonitorOpen = true;
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monitor);
		((UHFApplication) getApplication()).addActivity(this);
		mContext = this;
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			return ;
		}
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();
		
		mCheckOpenMonitor = (CheckBox) findViewById(R.id.check_open_monitor);
		if (PreferenceUtil.getBoolean(mIsChecked, false)) {
			mCheckOpenMonitor.setChecked(true);
		}
		
		mCheckOpenMonitor.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mReader.refreshBuffer();
					PreferenceUtil.commitBoolean(mIsChecked, true);
				} else {
					PreferenceUtil.commitBoolean(mIsChecked, false);
				}
			}
		});
		

		mMonitorList = (ListView)findViewById(R.id.monitor_list_view);
		
		app = (UHFApplication) getApplication();

		lbm  = LocalBroadcastManager.getInstance(this);		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG);
		itent.addAction(ReaderHelper.BROADCAST_WRITE_DATA);
		lbm.registerReceiver(mRecv, itent);

		mMonitorListAdapter = new ArrayAdapter<CharSequence>(mContext, R.layout.monitor_list_item, app.mMonitorListItem);
		
		mMonitorList.setAdapter(mMonitorListAdapter);
		
		refreshMonitor();
		
		mRefreshButton = (TextView) findViewById(R.id.refresh);
		mRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mDataText.setText("");
				app.mMonitorListItem.clear();
				mMonitorListAdapter.notifyDataSetChanged();
				//mReader.refreshBuffer();
			}
		});
		
		mDataText = (HexEditTextBox) findViewById(R.id.data_send_text);
		
		mDataText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				String[] result = StringTool.stringToStringArray(mDataText.getText().toString().toUpperCase(), 2);
				try {
					byte[] buf = StringTool.stringArrayToByteArray(result, result.length);
					MessageTran msgTran = new MessageTran();
					byte check = msgTran.checkSum(buf, 0, buf.length);
					mDataCheck.setText("" + Converter.byteToHex((int)(check & 0xFF)/16)  + Converter.byteToHex((check & 0xFF)%16));
				} catch (Exception e) {
					};
				}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mDataSendButton = (TextView) findViewById(R.id.send);
		mDataSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String cmd = mDataText.getText().toString().toUpperCase() + mDataCheck.getText().toString().toUpperCase();
				String[] result = StringTool.stringToStringArray(cmd, 2);
				if (result != null && result.length > 0) {
					mReader.sendBuffer(StringTool.stringArrayToByteArray(result, result.length));
				} else {
					Toast.makeText(mContext, "Command not allow empty", Toast.LENGTH_SHORT).show();
				}
			}
		});
		mDataCheck = (HexEditTextBox) findViewById(R.id.data_send_check);
		
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_DATA) && mCheckOpenMonitor.isChecked()) {
            	app.writeMonitor((String)intent.getStringExtra("log"), intent.getIntExtra("type", ERROR.SUCCESS));
            	refreshMonitor();
            } 
		}
	};
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (lbm != null)
			lbm.unregisterReceiver(mRecv);
        MainActivity.mIsMonitorOpen = false;
	}
		
	public final void refreshMonitor() {
		if (!mCheckOpenMonitor.isChecked()) return;
		mMonitorListAdapter.notifyDataSetChanged();
	}
	
}

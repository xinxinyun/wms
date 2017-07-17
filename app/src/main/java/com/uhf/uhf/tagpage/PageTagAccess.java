package com.uhf.uhf.tagpage;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableRow;
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
import com.uhf.uhf.HexEditTextBox;
import com.uhf.uhf.LogList;
import com.uhf.uhf.R;
import com.uhf.uhf.TagAccessList;
import com.uhf.uhf.spiner.AbstractSpinerAdapter.IOnItemSelectListener;
import com.uhf.uhf.spiner.SpinerPopWindow;

import java.util.ArrayList;
import java.util.List;


public class PageTagAccess extends LinearLayout {
	
	//fixed by lei.li 2016/11/09
	//private LogList mLogList;
	private LogList mLogList;
	//fixed by lei.li 2016/11/09

	private TextView mGet, mRead, mSelect, mWrite, mLock, mKill;
	
	//private TextView mRefreshButton;

	private TextView mTagAccessListText;
	private TableRow mDropDownRow;
	
	private List<String> mAccessList;
	private SpinerPopWindow mSpinerPopWindow;
	
	private HexEditTextBox mPasswordEditText;
	private EditText mStartAddrEditText;
	private EditText mDataLenEditText;
	private HexEditTextBox mDataEditText;
	private HexEditTextBox mLockPasswordEditText;
	private HexEditTextBox mKillPasswordEditText;
	
	private RadioGroup mGroupAccessAreaType;
	private RadioGroup mGroupLockAreaType;
	private RadioGroup mGroupLockType;
	
	private TagAccessList mTagAccessList;
	
	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;

	private int mPos = -1;
	
	private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
    
    private LocalBroadcastManager lbm;
    
    private Context mContext;
    
	public PageTagAccess(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.page_tag_access, this);
		
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
		mGet = (TextView) findViewById(R.id.get);
		mRead = (TextView) findViewById(R.id.read);
		mSelect = (TextView) findViewById(R.id.select);
		mWrite = (TextView) findViewById(R.id.write);
		mLock = (TextView) findViewById(R.id.lock);
		mKill = (TextView) findViewById(R.id.kill);
		mGet.setOnClickListener(setAccessOnClickListener);
		mRead.setOnClickListener(setAccessOnClickListener);
		mSelect.setOnClickListener(setAccessOnClickListener);
		mWrite.setOnClickListener(setAccessOnClickListener);
		mLock.setOnClickListener(setAccessOnClickListener);
		mKill.setOnClickListener(setAccessOnClickListener);
		
		mPasswordEditText = (HexEditTextBox) findViewById(R.id.password_text);
		mStartAddrEditText = (EditText) findViewById(R.id.start_addr_text);
		mDataLenEditText = (EditText) findViewById(R.id.data_length_text);
		mDataEditText = (HexEditTextBox) findViewById(R.id.data_write_text);
		mLockPasswordEditText = (HexEditTextBox) findViewById(R.id.lock_password_text);
		mKillPasswordEditText = (HexEditTextBox) findViewById(R.id.kill_password_text);
		
		mGroupAccessAreaType = (RadioGroup) findViewById(R.id.group_access_area_type);
		mGroupLockAreaType = (RadioGroup) findViewById(R.id.group_lock_area_type);
		mGroupLockType = (RadioGroup) findViewById(R.id.group_lock_type);
		
		mTagAccessListText =  (TextView) findViewById(R.id.tag_access_list_text);
		mDropDownRow = (TableRow) findViewById(R.id.table_row_tag_access_list);

		mTagAccessList = (TagAccessList) findViewById(R.id.tag_access_list);
		
		lbm  = LocalBroadcastManager.getInstance(mContext);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG);
		lbm.registerReceiver(mRecv, itent);
		
		mDropDownRow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSpinWindow();
			}
		});
		
		mAccessList.clear();
		mAccessList.add("Cancel");
		for(int i = 0; i < m_curInventoryBuffer.lsTagList.size(); i++){
			mAccessList.add(m_curInventoryBuffer.lsTagList.get(i).strEPC);
		}
		
		mSpinerPopWindow = new SpinerPopWindow(mContext);
		mSpinerPopWindow.refreshData(mAccessList, 0);
		mSpinerPopWindow.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setAccessSelectText(pos);
			}
		});
		
		updateView();
		
//		mRefreshButton = (TextView) findViewById(R.id.refresh);
//		mRefreshButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				refresh();
//			}
//		});
	}
	
	public void refresh()
	{
		mTagAccessListText.setText("");
		mPos = -1;
		
		mStartAddrEditText.setText("");
		mDataLenEditText.setText("");
		mDataEditText.setText("");
		mPasswordEditText.setText("");
		mLockPasswordEditText.setText("");
		mKillPasswordEditText.setText("");
		
		mGroupAccessAreaType.check(R.id.set_access_area_password);
		mGroupLockAreaType.check(R.id.set_lock_area_access_password);
		mGroupLockType.check(R.id.set_lock_free);
		
		m_curOperateTagBuffer.clearBuffer();
		
		//add by lei.li 2017/1/16
		mAccessList.clear();
		m_curInventoryBuffer.lsTagList.clear();
		mAccessList.add("cancel");
		//add by lei.li 2017/1/16
		refreshList();
		refreshText();
		clearText();
	}
	
	private void setAccessSelectText(int pos){
		if (pos >= 0 && pos < mAccessList.size()){
			String value = mAccessList.get(pos);
			mTagAccessListText.setText(value);
			mPos = pos;
		}
	}
	
	private void showSpinWindow() {
		//show tags readed by uhf
		for(int i = 0; i < m_curInventoryBuffer.lsTagList.size(); i++){
			if (!mAccessList.contains(m_curInventoryBuffer.lsTagList.get(i).strEPC))
			    mAccessList.add(m_curInventoryBuffer.lsTagList.get(i).strEPC);
		}
		mSpinerPopWindow.refreshData(mAccessList, 0);
		mSpinerPopWindow.setWidth(mDropDownRow.getWidth());
		mSpinerPopWindow.showAsDropDown(mDropDownRow);
	}
	
	private void updateView() {
		if (mPos < 0) mPos = 0;
		setAccessSelectText(mPos);
	}
	
	private OnClickListener setAccessOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.get:
			{
				mReader.getAccessEpcMatch(m_curReaderSetting.btReadId);
				break;
			}
			case R.id.select:
			{
			if (mPos <= 0) {
					mReader.cancelAccessEpcMatch(m_curReaderSetting.btReadId);
				} else {
					byte[] btAryEpc = null;
					
					try {
						String[] result = StringTool.stringToStringArray(mAccessList.get(mPos).toUpperCase(), 2);
						btAryEpc = StringTool.stringArrayToByteArray(result, result.length);
					} catch (Exception e) {
						Toast.makeText(
								mContext,
								getResources().getString(R.string.param_unknown_error),
								Toast.LENGTH_SHORT).show();
						return;
					}
					
					if (btAryEpc == null) {
						Toast.makeText(
								mContext,
								getResources().getString(R.string.param_unknown_error),
								Toast.LENGTH_SHORT).show();
						return;
					}
					
					mReader.setAccessEpcMatch(m_curReaderSetting.btReadId, (byte)(btAryEpc.length & 0xFF), btAryEpc);
				}
				break;
			}
			case R.id.read:
			case R.id.write:
			{
				byte btMemBank = 0x00;
                byte btWordAdd = 0x00;
                byte btWordCnt = 0x00;
                byte []btAryPassWord = null;
                if (mGroupAccessAreaType.getCheckedRadioButtonId() == R.id.set_access_area_password) {
                    btMemBank = 0x00;
                } else if (mGroupAccessAreaType.getCheckedRadioButtonId() == R.id.set_access_area_epc) {
                    btMemBank = 0x01;
                } else if (mGroupAccessAreaType.getCheckedRadioButtonId() == R.id.set_access_area_tid) {
                    btMemBank = 0x02;
                } else if (mGroupAccessAreaType.getCheckedRadioButtonId() == R.id.set_access_area_user) {
                    btMemBank = 0x03;
                }
                
                try {
                	btWordAdd = (byte) Integer.parseInt(mStartAddrEditText.getText().toString());
                } catch (Exception e) {
                	Toast.makeText(
                			mContext,
							getResources().getString(R.string.param_start_addr_error),
							Toast.LENGTH_SHORT).show();
					return;
                }

                try {
                	String[] reslut = StringTool.stringToStringArray(mPasswordEditText.getText().toString().toUpperCase(), 2);
                	btAryPassWord = StringTool.stringArrayToByteArray(reslut, 4);
                } catch (Exception e) {
                	Toast.makeText(
                			mContext,
							getResources().getString(R.string.param_password_error),
							Toast.LENGTH_SHORT).show();
					return;
                }
                
                if (arg0.getId() == R.id.read) {
                	try {
                    	btWordCnt = (byte) (Integer.parseInt(mDataLenEditText.getText().toString()));
                	} catch (Exception e) {
                		Toast.makeText(
                				mContext,
    							getResources().getString(R.string.param_data_len_error),
    							Toast.LENGTH_SHORT).show();
    					return;
                	}
                	
                	if ((btWordCnt & 0xFF) <= 0) {
                		Toast.makeText(
                				mContext,
    							getResources().getString(R.string.param_data_len_error),
    							Toast.LENGTH_SHORT).show();
    					return;
                	}
                	
                	m_curOperateTagBuffer.clearBuffer();
	                refreshList();
	                mReader.readTag(m_curReaderSetting.btReadId, btMemBank, btWordAdd, btWordCnt, btAryPassWord);
                } else {
                	byte[] btAryData = null;
                	String[] result = null;
                	try {
                    	result = StringTool.stringToStringArray(mDataEditText.getText().toString().toUpperCase(), 2);
                    	btAryData = StringTool.stringArrayToByteArray(result, result.length);
                    	btWordCnt = (byte)((result.length / 2 + result.length % 2) & 0xFF);
                	} catch (Exception e) {
                    	Toast.makeText(
                    			mContext,
    							getResources().getString(R.string.param_data_error),
    							Toast.LENGTH_SHORT).show();
    					return;
                    }
                	
                	if (btAryData == null || btAryData.length <= 0) {
                		Toast.makeText(
                				mContext,
    							getResources().getString(R.string.param_data_error),
    							Toast.LENGTH_SHORT).show();
    					return;
                	}
                	
                	if (btAryPassWord == null || btAryPassWord.length < 4) {
                    	Toast.makeText(
                    			mContext,
    							getResources().getString(R.string.param_password_error),
    							Toast.LENGTH_SHORT).show();
    					return;
                    }
                	
                	mDataLenEditText.setText(String.valueOf(btWordCnt & 0xFF));
                    
                	m_curOperateTagBuffer.clearBuffer();
	                refreshList();
	                mReader.writeTag(m_curReaderSetting.btReadId, btAryPassWord, btMemBank, btWordAdd, btWordCnt, btAryData);
                }
                
				break;
			}
			case R.id.lock:
			{
				byte btMemBank = 0x00;
	            byte btLockType = 0x00;
	            byte []btAryPassWord = null;
	            if (mGroupLockAreaType.getCheckedRadioButtonId() == R.id.set_lock_area_access_password) {
                    btMemBank = 0x04;
                } else if (mGroupLockAreaType.getCheckedRadioButtonId() == R.id.set_lock_area_kill_password) {
                    btMemBank = 0x05;
                } else if (mGroupLockAreaType.getCheckedRadioButtonId() == R.id.set_lock_area_epc) {
                    btMemBank = 0x03;
                } else if (mGroupLockAreaType.getCheckedRadioButtonId() == R.id.set_lock_area_tid) {
                    btMemBank = 0x02;
                } else if (mGroupLockAreaType.getCheckedRadioButtonId() == R.id.set_lock_area_user) {
                    btMemBank = 0x01;
                }
	            
	            if (mGroupLockType.getCheckedRadioButtonId() == R.id.set_lock_free) {
	            	btLockType = 0x00;
                } else if (mGroupLockType.getCheckedRadioButtonId() == R.id.set_lock_free_ever) {
                	btLockType = 0x02;
                } else if (mGroupLockType.getCheckedRadioButtonId() == R.id.set_lock_lock) {
                	btLockType = 0x01;
                } else if (mGroupLockType.getCheckedRadioButtonId() == R.id.set_lock_lock_ever) {
                	btLockType = 0x03;
                }
	            
	            try {
                	String[] reslut = StringTool.stringToStringArray(mLockPasswordEditText.getText().toString().toUpperCase(), 2);
                	btAryPassWord = StringTool.stringArrayToByteArray(reslut, 4);
	            } catch (Exception e) {
                	Toast.makeText(
                			mContext,
							getResources().getString(R.string.param_lockpassword_error),
							Toast.LENGTH_SHORT).show();
					return;
                }
	            
	            if (btAryPassWord == null || btAryPassWord.length < 4) {
                	Toast.makeText(
                			mContext,
							getResources().getString(R.string.param_lockpassword_error),
							Toast.LENGTH_SHORT).show();
					return;
                }
	            
	            m_curOperateTagBuffer.clearBuffer();
                refreshList();
	            mReader.lockTag(m_curReaderSetting.btReadId, btAryPassWord, btMemBank, btLockType);
	            
				break;
			}
			case R.id.kill:
			{
				byte []btAryPassWord = null;
				try {
					String[] reslut = StringTool.stringToStringArray(mKillPasswordEditText.getText().toString().toUpperCase(), 2);
					btAryPassWord = StringTool.stringArrayToByteArray(reslut, 4);
				} catch (Exception e) {
                	Toast.makeText(
                			mContext,
							getResources().getString(R.string.param_killpassword_error),
							Toast.LENGTH_SHORT).show();
					return;
                }
				
				if (btAryPassWord == null || btAryPassWord.length < 4) {
                	Toast.makeText(
                			mContext,
							getResources().getString(R.string.param_killpassword_error),
							Toast.LENGTH_SHORT).show();
					return;
                }
				
				m_curOperateTagBuffer.clearBuffer();
                refreshList();
                mReader.killTag(m_curReaderSetting.btReadId, btAryPassWord);
				break;
			}
			}
		}
	};
	
	private void refreshList() {
		mTagAccessList.refreshList();
	}
	
	private void refreshText() {
		mTagAccessList.refreshText();
	}
	
	private void clearText() {
		mTagAccessList.clearText();
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				switch (btCmd) {
				case CMD.GET_ACCESS_EPC_MATCH:
					mTagAccessListText.setText(m_curOperateTagBuffer.strAccessEpcMatch);
					break;
				case CMD.READ_TAG:
				case CMD.WRITE_TAG:
				case CMD.LOCK_TAG:
				case CMD.KILL_TAG:
					refreshList();
					refreshText();
					break;
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
	
	public void doDestroy() {
		// TODO Auto-generated method stub

		if (lbm != null)
			lbm.unregisterReceiver(mRecv);
	}
}


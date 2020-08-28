package com.uhf.uhf.tagpage;


import com.reader.base.CMD;
import com.reader.base.ERROR;
import com.reader.base.ReaderBase;
import com.reader.helper.ISO180006BOperateTagBuffer;
import com.reader.helper.ReaderHelper;
import com.reader.helper.ReaderSetting;
import com.uhf.uhf.LogList;
import com.uhf.uhf.R;
import com.uhf.uhf.TagReal6BList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


public class PageInventoryReal6B extends LinearLayout {
	//fixed by lei.li 2016/11/09
	//private LogList mLogList;
	private LogList mLogList;
	//fixed by lei.li 2016/11/09
	private TextView mStartStop;
	
	//private TextView mRefreshButton;
	
	private TagReal6BList mTagReal6BList;
	
	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;

	private static ReaderSetting m_curReaderSetting;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
    
    private LocalBroadcastManager lbm;
    
    private Context mContext;
    
	public PageInventoryReal6B(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.page_inventory_real_6b, this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

		mLogList = (LogList) findViewById(R.id.log_list);
		mStartStop = (TextView) findViewById(R.id.startstop6b);
		
		mTagReal6BList = (TagReal6BList) findViewById(R.id.tag_real_6b_list);

		mStartStop.setOnClickListener(setInventoryReal6BOnClickListener);
		
		lbm  = LocalBroadcastManager.getInstance(mContext);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_ISO18000_6B);
		lbm.registerReceiver(mRecv, itent);
		
		if (mReaderHelper.getInventoryFlag()) {
			mHandler.removeCallbacks(mRefreshRunnable);
			mHandler.postDelayed(mRefreshRunnable,2000);
		}
		
		refreshStartStop(mReaderHelper.getISO6BContinue());
		
//		mRefreshButton = (TextView) findViewById(R.id.refresh);
//		mRefreshButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				m_curOperateTagISO18000Buffer.clearBuffer();
//				
//				refreshList();
//				refreshText();
//				clearText();
//			}
//		});
	}
	
	public void refresh()
	{
		m_curOperateTagISO18000Buffer.clearBuffer();
		
		refreshList();
		refreshText();
		clearText();
	}
	
	@SuppressWarnings("deprecation")
	private void refreshStartStop( boolean start ) {
		if( start ) {
			mStartStop.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_disenabled_background));
			mStartStop.setText(getResources().getString(R.string.stop_inventory));
		} else {
			mStartStop.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_background));
			mStartStop.setText(getResources().getString(R.string.start_inventory));
		}
	}
	
	private Handler mHandler = new Handler();
    private Runnable mRefreshRunnable = new Runnable() {
         public void run () {
        	 refreshList();
        	 mHandler.postDelayed(this, 2000); 
      }
    };
    
	private Handler mLoopHandler = new Handler();
    private Runnable mLoopRunnable = new Runnable() {
         public void run () {
        	 mReader.iso180006BInventory(m_curReaderSetting.btReadId);
        	 mLoopHandler.postDelayed(this, 2000); 
         }
    };
	
    private void startstop()
    {
		if( !mStartStop.getText().toString().equals(getResources().getString(R.string.start_inventory)) ) {
			mReaderHelper.setISO6BContinue(false);
			
			mLoopHandler.removeCallbacks(mLoopRunnable);
			mHandler.removeCallbacks(mRefreshRunnable);
			refreshStartStop(false);
		} else {
			mReaderHelper.setISO6BContinue(true);
			//m_curOperateTagISO18000Buffer.clearBuffer();
			mReader.iso180006BInventory(m_curReaderSetting.btReadId);
			
			mLoopHandler.removeCallbacks(mLoopRunnable);
			mLoopHandler.postDelayed(mLoopRunnable,2000);
			mHandler.removeCallbacks(mRefreshRunnable);
			mHandler.postDelayed(mRefreshRunnable,2000);
			refreshStartStop(true);
		}
		refreshList();
    }
    
	private OnClickListener setInventoryReal6BOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if(arg0.getId() == R.id.startstop6b) {
				startstop();
			}
		}
	};
	
	private void refreshList() {
		mTagReal6BList.refreshList();
	}
	
	private void refreshText() {
		mTagReal6BList.refreshText();
	}
	
	private void clearText() {
		mTagReal6BList.clearText();
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_ISO18000_6B)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				switch (btCmd) {
				case CMD.ISO18000_6B_INVENTORY:
					/*if( !mReaderHelper.getISO6BContinue() ) {
						startstop();
					}*/
					
					refreshText();
					mLoopHandler.removeCallbacks(mLoopRunnable);
					mLoopHandler.postDelayed(mLoopRunnable,2000);
					break;
				case ReaderHelper.INVENTORY_ERR:
				case ReaderHelper.INVENTORY_ERR_END:
				case ReaderHelper.INVENTORY_END:
//					refreshList();
					refreshText();
					if (mReaderHelper.getISO6BContinue()) {
						mLoopHandler.removeCallbacks(mLoopRunnable);
						mLoopHandler.postDelayed(mLoopRunnable,2000);
					} else {
						mLoopHandler.removeCallbacks(mLoopRunnable);
					}
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
		
		mLoopHandler.removeCallbacks(mLoopRunnable);
		mHandler.removeCallbacks(mRefreshRunnable);
	}
}


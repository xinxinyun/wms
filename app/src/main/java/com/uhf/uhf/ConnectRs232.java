package com.uhf.uhf;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.reader.helper.ControlGPIO;
import com.reader.helper.ReaderHelper;
import com.uhf.uhf.serialport.SerialPort;
import com.uhf.uhf.serialport.SerialPortFinder;
import com.uhf.uhf.spiner.AbstractSpinerAdapter.IOnItemSelectListener;
import com.uhf.uhf.spiner.SpinerPopWindow;
import com.ui.base.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("HandlerLeak")
/**
 * @author Administrator
 *
 */
public class ConnectRs232 extends BaseActivity {
	
	//add by lei.li 2016//11/14
	private static final String TAG = "COONECTRS232";
	private static final String TTYS1 = "ttyS4 (rk_serial)";
	private static final boolean DEBUG = true;
	//add by lei.li 2016//11/14
	private TextView mConectButton;
	
	private static final int CONNECTING = 0x10;
	private static final int CONNECT_TIMEOUT = 0x100;
	private static final int CONNECT_FAIL = 0x101;
	private static final int CONNECT_SUCCESS = 0x102;
	
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 0;
	
	private ReaderHelper mReaderHelper;
	
	private List<String> mPortList = new ArrayList<String>();
	
	private TextView mPortTextView, mBaud115200View, mBaud38400View;
	private TableRow mDropPort;
	private SpinerPopWindow mSpinerPort;
	
	private int mPosPort = -1;
	
	private SerialPortFinder mSerialPortFinder;
	
	String[] entries = null;
	String[] entryValues = null;
	
	public static SerialPort mSerialPort = null;
	private int baud = 115200;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_rs232);
		
		((UHFApplication) getApplication()).addActivity(this);
		
		mSerialPortFinder = new SerialPortFinder();
		
		entries = mSerialPortFinder.getAllDevices();
        entryValues = mSerialPortFinder.getAllDevicesPath();
		
		mConectButton = (TextView) findViewById(R.id.textview_connect);

		mPortTextView =  (TextView) findViewById(R.id.comport_text);
		mBaud115200View =  (TextView) findViewById(R.id.baud_115200);
		mBaud38400View =  (TextView) findViewById(R.id.baud_38400);
		mDropPort = (TableRow) findViewById(R.id.table_row_spiner_comport);
		
		//add by lei.li 2016/11/14 set default serial port 
		mPortTextView.setText(TTYS1);
		//add by lei.li 2016/11/14
		
		baud = 115200;
//		mBaud115200View.setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF));
//		mBaud38400View.setBackgroundColor(Color.rgb(0xCC, 0xCC, 0xCC));
		
		mBaud115200View.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				mBaud115200View.setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF));
//				mBaud38400View.setBackgroundColor(Color.rgb(0xCC, 0xCC, 0xCC));
				baud = 115200;
			}
		});
		
		mBaud38400View.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBaud38400View.setBackgroundColor(Color.rgb(0xFF, 0xFF, 0xFF));
				mBaud115200View.setBackgroundColor(Color.rgb(0xCC, 0xCC, 0xCC));
				baud = 38400;
			}
		});

		mConectButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (mPortList.indexOf(mPortTextView.getText().toString()) >= 0)
					mPosPort = mPortList.indexOf(mPortTextView.getText().toString());
				
				if (DEBUG)
					Log.e(TAG, "test the value of mPosPort::" + mPosPort);
				
				if (mPosPort < 0) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.rs232_error),
							Toast.LENGTH_SHORT).show();
					return ;
				}
				
				try {
				
					mSerialPort = new SerialPort(new File(entryValues[mPosPort]), baud, 0);
					
					if (DEBUG)
						Log.e(TAG,"ttys1 value :::" + entryValues[mPosPort]);
					
					try {
						mReaderHelper = ReaderHelper.getDefaultHelper();
						mReaderHelper.setReader(mSerialPort.getInputStream(), mSerialPort.getOutputStream());
					} catch (Exception e) {
						e.printStackTrace();
						
						return ;
					}
					
					Intent intent;
					intent = new Intent().setClass(ConnectRs232.this, MainActivity.class);
					startActivity(intent);
					ControlGPIO.newInstance().JNIwriteGPIO(1);
					//finish();
				} catch (SecurityException e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.error_security),
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.error_unknown),
							Toast.LENGTH_SHORT).show();
				} catch (InvalidParameterException e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.error_configuration),
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		mDropPort.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showPortSpinWindow();
			}
		});
		
		String[] lists = entries;
		for(int i = 0; i < lists.length; i++){
			mPortList.add(lists[i]);
		}
		
		mSpinerPort = new SpinerPopWindow(this);
		mSpinerPort.refreshData(mPortList, 0);
		mSpinerPort.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setPortText(pos);
			}
		});
	}
	
	private void showPortSpinWindow() {
		mSpinerPort.setWidth(mDropPort.getWidth());
		mSpinerPort.showAsDropDown(mDropPort);
	}

	private void setPortText(int pos){
		if (pos >= 0 && pos < mPortList.size()){
			String value = mPortList.get(pos);
			mPortTextView.setText(value);
			mPosPort = pos;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			finish();

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}

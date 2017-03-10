package com.ui.base;

import java.util.Locale;

import com.reader.helper.ControlGPIO;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class BaseActivity extends Activity {
	
	private static final String TAG = "BaseActivity";
	private static final boolean DEBUG = true;
	
	protected boolean mSwitchFlag = false;
	private VirtualKeyListenerBroadcastReceiver mVirtualKeyListenerBroadcastReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceUtil.init(this);
	    switchLanguage(PreferenceUtil.getString("language", "zh"));
	}
	
	/**
	 * Changing the language of application through setting the local configuration.
	 * @param language
	 */
	protected void switchLanguage(String language) {
		Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
       if (language.equals("en")) {
            config.locale = Locale.ENGLISH;
        } else {
        	 config.locale = Locale.SIMPLIFIED_CHINESE;
        }
        resources.updateConfiguration(config, dm);
        
        PreferenceUtil.commitString("language", language);
    }
	
	@Override
	protected void onResume(){
		super.onResume();
		if (mSwitchFlag) {
			Log.e("+++++++++++", "Listener the KEYCODE_APP_SWITCH");
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mVirtualKeyListenerBroadcastReceiver = new VirtualKeyListenerBroadcastReceiver();
    	IntentFilter intentFilter=new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    	this.registerReceiver(mVirtualKeyListenerBroadcastReceiver,intentFilter);
		if (mSwitchFlag) {
			ControlGPIO.newInstance().JNIwriteGPIO(ControlGPIO.ON);
		}
		//ControlGPIO.newInstance().JNIwriteGPIO(1);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		//ControlGPIO.newInstance().JNIwriteGPIO(0);
	}
	/**
	 * Using this Method to close UHF 
	 * This method is invoked when the application is killed by android system.
	 * 
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState){
		//ControlGPIO.newInstance().JNIwriteGPIO(ControlGPIO.OFF);
		//debug info
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle outState){
		//ControlGPIO.newInstance().JNIwriteGPIO(ControlGPIO.ON);
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			mSwitchFlag = true;
		}
		return super.onKeyDown(keyCode, event);
		
	} 
	
	private class VirtualKeyListenerBroadcastReceiver extends BroadcastReceiver{
		private final String SYSTEM_REASON = "reason";
		private final String SYSTEM_HOME_KEY = "homekey";
		private final String SYSTEM_RECENT_APPS = "recentapps";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String systemReason = intent.getStringExtra(SYSTEM_REASON);
				if (systemReason != null) {
					if (systemReason.equals(SYSTEM_HOME_KEY)) {
						System.out.println("Press HOME key");
					} else if (systemReason.equals(SYSTEM_RECENT_APPS)) {
						System.out.println("Press RECENT_APPS key");
						ControlGPIO.newInstance().JNIwriteGPIO(ControlGPIO.OFF);
						mSwitchFlag = true;
					}
				}
			}
		}

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(mVirtualKeyListenerBroadcastReceiver);
	}
}

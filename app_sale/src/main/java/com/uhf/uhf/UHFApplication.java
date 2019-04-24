package com.uhf.uhf;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.com.tools.Beeper;
import com.com.tools.OtgStreamManage;
import com.reader.base.ERROR;
import com.reader.helper.ReaderHelper;
import com.util.PreferenceUtil;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UHFApplication extends Application {

	//add by lei.li 2016/11/12
	private static Context mContext;
	//add by lei.li 2016/11/12
	
	private Socket mTcpSocket = null;
	private BluetoothSocket mBtSocket = null;

	public ArrayList<CharSequence> mMonitorListItem = new ArrayList<CharSequence>();

	private static JobManager jobManager;

	public final void writeMonitor(String strLog, int type) {
		Date now = new Date();
		SimpleDateFormat temp = new SimpleDateFormat("kk:mm:ss");
		SpannableString tSS = new SpannableString(temp.format(now) + ":\n"
				+ strLog);
		tSS.setSpan(new ForegroundColorSpan(type == ERROR.SUCCESS ? Color.BLACK
				: Color.RED), 0, tSS.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
		mMonitorListItem.add(tSS);
		if (mMonitorListItem.size() > 1000)
			mMonitorListItem.remove(0);
	}

	private List<Activity> activities = new ArrayList<Activity>();

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			ReaderHelper.setContext(getApplicationContext());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mContext = getApplicationContext();
		// add by lei.li support OTG
		OtgStreamManage.newInstance().init(mContext);
		PreferenceUtil.init(mContext);
		try {
			Beeper.init(mContext);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		configureJobManager();//2. 配置JobMananger
		/*CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());*/
	}

	public void addActivity(Activity activity) {
		activities.add(activity);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		//ControlGPIO.newInstance().JNIwriteGPIO(0);
		for (Activity activity : activities) {
			try {
				activity.finish();
			} catch (Exception e) {

			}
		}

		try {
			if (mTcpSocket != null)
				mTcpSocket.close();
			if (mBtSocket != null)
				mBtSocket.close();
		} catch (IOException e) {
		}

		mTcpSocket = null;
		mBtSocket = null;
		/*if (ConnectRs232.mSerialPort != null) {
			Log.e("close serial", "serial");
			ConnectRs232.mSerialPort.close();
		}
*/
		if (BluetoothAdapter.getDefaultAdapter() != null)
			BluetoothAdapter.getDefaultAdapter().disable();

		System.exit(0);
	};

	public void setTcpSocket(Socket socket) {
		this.mTcpSocket = socket;
	}

	public void setBtSocket(BluetoothSocket socket) {
		this.mBtSocket = socket;
	}
	
	public static Context getContext(){
		return mContext;
	}


	public static JobManager getJobManager() {
		return jobManager;
	}

	private void configureJobManager() {
		//3. JobManager的配置器，利用Builder模式
		Configuration configuration = new Configuration.Builder(this)
				.customLogger(new CustomLogger() {
					private static final String TAG = "JOBS";

					@Override
					public boolean isDebugEnabled() {
						return true;
					}

					@Override
					public void d(String text, Object... args) {
						Log.d(TAG, String.format(text, args));
					}

					@Override
					public void e(Throwable t, String text, Object... args) {
						Log.e(TAG, String.format(text, args), t);
					}

					@Override
					public void e(String text, Object... args) {
						Log.e(TAG, String.format(text, args));
					}

					@Override
					public void v(String text, Object... args) {
						Log.e(TAG, String.format(text, args));
					}
				})
				.minConsumerCount(1)//always keep at least one consumer alive
				.maxConsumerCount(3)//up to 3 consumers at a time
				.loadFactor(3)//3 jobs per consumer
				.consumerKeepAlive(120)//wait 2 minute
				.build();

		jobManager = new JobManager(configuration);
	}
}

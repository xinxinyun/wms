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

import com.com.tools.Beeper;
import com.com.tools.OtgStreamManage;
import com.http.OKHttpUpdateHttpService;
import com.reader.base.ERROR;
import com.reader.helper.ReaderHelper;
import com.ui.base.PreferenceUtil;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.XHttpSDK;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.utils.UpdateUtils;
import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.tip.ToastUtils;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

public class VehicleApplication extends Application {

	//add by lei.li 2016/11/12
	private static Context mContext;
	//add by lei.li 2016/11/12
	
	private Socket mTcpSocket = null;
	private BluetoothSocket mBtSocket = null;

	public ArrayList<CharSequence> mMonitorListItem = new ArrayList<CharSequence>();

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

		XUtil.init(this);
		XUtil.debug(true);

//		PageConfig.getInstance().setPageConfiguration(new PageConfiguration() { //页面注册
//			@Override
//			public List<PageInfo> registerPages(Context context) {
//				return AppPageConfig.getInstance().getPages(); //自动注册页面
//			}
//		}).debug("PageLog").enableWatcher(true).init(this);
//
//		XAOP.init(this); //初始化插件
//		XAOP.debug(true); //日志打印切片开启
		//设置动态申请权限切片 申请权限被拒绝的事件响应监听
//		XAOP.setOnPermissionDeniedListener(new PermissionUtils.OnPermissionDeniedListener() {
//			@Override
//			public void onDenied(List<String> permissionsDenied) {
//				ToastUtils.toast("权限申请被拒绝:" + StringUtils.listToString(permissionsDenied, ","));
//			}
//
//		});

		initXHttp();

		initUpdate();
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
		if (ConnectRs232.mSerialPort != null) {
			Log.e("close serial", "serial");
			ConnectRs232.mSerialPort.close();
		}

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

	private void initUpdate() {
		XUpdate.get()
				.debug(true)
				.isWifiOnly(true)                                               //默认设置只在wifi下检查版本更新
				.isGet(true)                                                    //默认设置使用get请求检查版本
				.isAutoMode(false)                                              //默认设置非自动模式，可根据具体使用配置
				.param("versionCode", UpdateUtils.getVersionCode(this))  //设置默认公共请求参数
				.param("appKey", getPackageName())
				.setOnUpdateFailureListener(new OnUpdateFailureListener() { //设置版本更新出错的监听
					@Override
					public void onFailure(UpdateError error) {
						if (error.getCode() != CHECK_NO_NEW_VERSION) {          //对不同错误进行处理
							ToastUtils.toast(error.toString());
						}
					}
				})
				.supportSilentInstall(true)                                     //设置是否支持静默安装，默认是true
				.setIUpdateHttpService(new OKHttpUpdateHttpService())           //这个必须设置！实现网络请求功能。
				.init(this);                                          //这个必须初始化

	}

	private void initXHttp() {
		XHttpSDK.init(this);   //初始化网络请求框架，必须首先执行
		XHttpSDK.debug("XHttp");  //需要调试的时候执行
		XHttp.getInstance().setTimeout(20000);
	}
}

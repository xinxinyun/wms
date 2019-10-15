package com.anji;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.anji.reader.base.ERROR;
import com.anji.util.Beeper;
import com.anji.util.PreferenceUtil;
import com.anji.reader.helper.ReaderHelper;
import com.anji.util.OtgStreamManage;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        if (mMonitorListItem.size() > 1000){
            mMonitorListItem.remove(0);}
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

        //configureJobManager();//2. 配置JobMananger
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
            if (mTcpSocket != null) {
                mTcpSocket.close();
            }
            if (mBtSocket != null) {
                mBtSocket.close();
            }
        } catch (IOException e) {
        }

        mTcpSocket = null;
        mBtSocket = null;
		/*if (ConnectRs232.mSerialPort != null) {
			Log.e("close serial", "serial");
			ConnectRs232.mSerialPort.close();
		}
*/
        if (BluetoothAdapter.getDefaultAdapter() != null) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
        System.exit(0);
    }

    ;

    public void setTcpSocket(Socket socket) {
        this.mTcpSocket = socket;
    }

    public void setBtSocket(BluetoothSocket socket) {
        this.mBtSocket = socket;
    }

    public static Context getContext() {
        return mContext;
    }


}

package com.uhf.uhf;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.com.tools.OtgStreamManage;
import com.reader.helper.ReaderHelper;

import java.io.IOException;

public class ConnectActivity extends Activity {

    private TextView mConectRs232;
    private TextView mConectTcpIp;
    private TextView mConectBluetooth;
    private TextView mConnectOtg;

    private final int REQUEST_ENABLE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ((UHFApplication)getApplication()).addActivity(this);

        mConectRs232 = (TextView) findViewById(R.id.textview_connect_rs232);
        mConectTcpIp = (TextView) findViewById(R.id.textview_connect_tcp_ip);
        mConectBluetooth = (TextView) findViewById(R.id.textview_connect_bluetooth);
        mConnectOtg = (TextView) findViewById(R.id.textview_connect_OTG);

        TextView textView = (TextView) findViewById(R.id.test_textview);
        try {
            Process process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG);
        }

        mConectRs232.setOnClickListener(setConnectOnClickListener);
        mConectTcpIp.setOnClickListener(setConnectOnClickListener);
        mConectBluetooth.setOnClickListener(setConnectOnClickListener);
        mConnectOtg.setOnClickListener(setConnectOnClickListener);
    }

    private OnClickListener setConnectOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.textview_connect_rs232:
                    startActivity(new Intent().setClass(getApplicationContext(), ConnectRs232.class));
                    break;
                case R.id.textview_connect_tcp_ip:
                    startActivity(new Intent().setClass(getApplicationContext(), ConnectTcpIp.class));
                    break;
                case R.id.textview_connect_OTG:
                    try {
                        if (!OtgStreamManage.newInstance().requestPermission()) {
                            OtgStreamManage.newInstance().initSerialPort();
                            ReaderHelper.getDefaultHelper().setReader(OtgStreamManage.newInstance().getInputStream(), OtgStreamManage.newInstance().getOutputStream());
                            ConnectActivity.this.startActivity(new Intent().setClass(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                    }
                    return;
                case R.id.textview_connect_bluetooth:
                    if (BluetoothAdapter.getDefaultAdapter() == null) {
                        Toast.makeText(
                                getApplicationContext(),
                                getResources().getString(R.string.no_bluetooth),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),REQUEST_ENABLE);
                    break;
            }
            //finish();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                startActivity(new Intent().setClass(getApplicationContext(), ConnectBlueTooth.class));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}

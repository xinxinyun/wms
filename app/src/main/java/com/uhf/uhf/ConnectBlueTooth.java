package com.uhf.uhf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.ble.BleCallBack;
import com.ble.ble.BleService;
import com.ble.ble.constants.BleUUIDS;
import com.ble.ble.util.GattUtil;
import com.reader.helper.ReaderHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@SuppressLint("HandlerLeak")
public class ConnectBlueTooth extends Activity {

    private TextView mConectButton;

    private static final int CONNECTING = 0x10;
    private static final int CONNECT_TIMEOUT = 0x100;
    private static final int CONNECT_FAIL = 0x101;
    private static final int CONNECT_SUCCESS = 0x102;

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 0;

    private ReaderHelper mReaderHelper;

    private static BluetoothManager mBluetoothManager;
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothGatt mBluetoothGatt;
    private static BluetoothGattCharacteristic mBluetoothGattCharacteristic;

    private static final String Server_UUID = "00001000-0000-1000-8000-00805f9b34fb";
    private static final String Write_UUID = "00001001-0000-1000-8000-00805f9b34fb";
    private static final String Read_UUID = "00001002-0000-1000-8000-00805f9b34fb";

    private static BluetoothSocket mSocket;
    private static BleService mBleService;
    private static String address;

    private static Context mContext;
    private static EditText mSendEdit;
    private static Button mButton;

    private static PipedOutputStream mPipedOutputStream ;
    private static PipedInputStream mPipedInputStream ;


    class Wrapper{
        public int readNum;
    }

   BleCallBack mBleCallBack = new BleCallBack() {
       @Override
       public void onConnected(String s) {
           super.onConnected(s);
           Log.d("TAG","onConnected" + "::" + s);
           new Timer().schedule(new ServicesDiscoveredTask(address), 300, 100);
       }

       @Override
       public void onConnectTimeout(String s) {
           super.onConnectTimeout(s);
       }

       @Override
       public void onConnectionError(String s, int i, int i1) {
           super.onConnectionError(s, i, i1);
       }

       @Override
       public void onDisconnected(String s) {
           super.onDisconnected(s);
       }

       @Override
       public void onServicesDiscovered(String s) {
           super.onServicesDiscovered(s);
           //mHandler.obtainMessage(CONNECT_SUCCESS).sendToTarget();
           Log.d("TAG",s);
       }

       @Override
       public void onServicesUndiscovered(String s, int i) {
           super.onServicesUndiscovered(s, i);
           mHandler.obtainMessage(CONNECT_FAIL).sendToTarget();
       }

       @Override
       public void onCharacteristicRead(String s, byte[] bytes, int i) {
           super.onCharacteristicRead(s, bytes, i);
       }

       @Override
       public void onCharacteristicRead(String s, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
           super.onCharacteristicRead(s, bluetoothGattCharacteristic, i);
       }

       @Override
       public void onCharacteristicWrite(String s, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
           Log.d("onCharacteristicWrite","Write data to BLE device success!");
           super.onCharacteristicWrite(s, bluetoothGattCharacteristic, i);
       }

       @Override
       public void onCharacteristicChanged(String s, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
           super.onCharacteristicChanged(s, bluetoothGattCharacteristic);
           byte[] data = bluetoothGattCharacteristic.getValue();
           if (data != null && data.length != 0) {
               try {
                   mPipedOutputStream.write(data);
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }
   };

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w("TAG", "onServiceDisconnected()");
            mBleService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((BleService.LocalBinder)
                    service).getService(mBleCallBack);
            if (mBleService != null) {
                mBleService.initialize();
                mBleService.setDecode(true);
                mBleService.setConnectTimeout(5000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.connect_bluetooth);

        mContext = getApplicationContext();
        checkBLEFeature();
        bindService(new Intent(this, BleService.class), mConnection, BIND_AUTO_CREATE);
        ((VehicleApplication) getApplication()).addActivity(this);
        if (mBluetoothAdapter == null) {
            Toast.makeText(
                    getApplicationContext(),
                    getResources().getString(R.string.no_bluetooth),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mConectButton = (TextView) findViewById(R.id.textview_connect);
        mPipedOutputStream = new PipedOutputStream();
        try {
            mPipedInputStream = new PipedInputStream(mPipedOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();

        mConectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent().setClass(ConnectBlueTooth.this, DeviceListActivity.class);
                mBluetoothAdapter.cancelDiscovery();
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });
    }

    private void checkBLEFeature() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this,"error bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(final Message msg) {
            Intent intent = null;
            switch (msg.what) {
                case CONNECTING:
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(R.string.bluetooth_connecting),
                            Toast.LENGTH_SHORT).show();
                    break;
                case CONNECT_TIMEOUT:
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(R.string.bluetooth_connect_timeout),
                            Toast.LENGTH_SHORT).show();
                    break;
                case CONNECT_FAIL:
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(R.string.bluetooth_connect_fail),
                            Toast.LENGTH_SHORT).show();
                    break;
                case CONNECT_SUCCESS:
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(R.string.bluetooth_connect_success),
                            Toast.LENGTH_SHORT).show();
                    try {
                        mReaderHelper = ReaderHelper.getDefaultHelper();
                        mReaderHelper.setReader(mPipedInputStream, new OutputStream() {
                            @Override
                            public void write(int b) throws IOException {
                            }
                            @Override
                            public void write(byte[] buff) {
                               mBleService.send(address,buff,true);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    intent = new Intent().setClass(ConnectBlueTooth.this, MainActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    mBleService.connect(address,true);
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(R.string.bluetooth_connecting),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent().setClass(ConnectBlueTooth.this, ConnectActivity.class));
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unbindService(mConnection);
    }

    public boolean enableNotification(String address, UUID serUuid, UUID charUuid){
        BluetoothGatt gatt = mBleService.getBluetoothGatt(address);
        BluetoothGattCharacteristic c = GattUtil.getGattCharacteristic(gatt, serUuid, charUuid);
        return setCharacteristicNotification(gatt, c, true);
    }

    public boolean setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic c, boolean enable){
        if (mBleService != null) {
            return mBleService.setCharacteristicNotification(gatt, c, enable);
        }
        return false;
    }

    //TODO 刚连上线做的一些准备工作
    private class ServicesDiscoveredTask extends TimerTask {
        String address;
        int i;

        ServicesDiscoveredTask(String address){
            this.address = address;
        }

        void cancelTask(){
            //准备工作完成，向外发送广播
        }

        @Override
        public void run() {
            switch (i) {
                case 0:
                    //打开模组默认的数据接收通道【0x1002】，这一步成功才能保证APP收到数据
                    boolean success = enableNotification(address, BleUUIDS.PRIMARY_SERVICE, BleUUIDS.CHARACTERS[1]);
                    while (!success) {
                        success = enableNotification(address, BleUUIDS.PRIMARY_SERVICE, BleUUIDS.CHARACTERS[1]);
                    }
                    Log.i("TAG", "Enable 0x1002 notification: " + success);
                    mHandler.obtainMessage(CONNECT_SUCCESS).sendToTarget();
                    break;

//                case 1:
//                    //适配CC2541透传模块与部分手机的连接问题（就是连线后不走onServicesDiscovered()方法，一段时间后自动断开），
//                    //初次成功需要重启模块，2.6以下版本还要重启手机蓝牙或者断线时调用mBleService.refresh()，
//                    //不过mBleService.refresh()会清除手机缓存的uuid，影响再次连接的速度
//                    AdaptionUtil au = new AdaptionUtil(mBleService);
//                    au.setOnResultListener(mAdaptionResultListener);
//                    au.writeAdaptionConfigs(address);
//                    break;

                default:
                    cancelTask();
                    break;
            }
            i++;
        }
    }
}

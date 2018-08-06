package com.com.tools;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 7/14/2017.
 */

public class OtgStreamManage {

    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final OtgStreamManage mOtgStreamManage = new OtgStreamManage();
    private UsbManager mUsbManager;
    private UsbSerialPort mPort;
    List<UsbSerialDriver> mAvailableDrivers;
    UsbSerialDriver mDriver;
    UsbDeviceConnection mConnection;
    UsbDevice mUsbDevice;
    private Context mContext;

    private OtgStreamManage() {
    }

    public static OtgStreamManage newInstance() {
        return mOtgStreamManage;
    }

    public void init(Context context) {
        mContext = context;
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        /*try {
            initSerialPort();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /**
     * @return
     * @throws Exception
     */
    public boolean requestPermission() throws Exception {
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        if (mUsbManager.getDeviceList().isEmpty()) {
            throw new Exception("You not have available device!");
        }
        for (Map.Entry<String, UsbDevice> usbDeviceEntry : mUsbManager.getDeviceList().entrySet()) {
            mUsbDevice = usbDeviceEntry.getValue();
            if (!mUsbManager.hasPermission(usbDeviceEntry.getValue())) {
                mUsbManager.requestPermission(usbDeviceEntry.getValue(), mPermissionIntent);
                return true;
            }
        }
        return false;
    }

    /**
     * Judge the device is or not obtain the permission.
     * @return
     */
    public boolean hasPermission() {
        if (mUsbDevice == null || mUsbManager == null) {
            if (mUsbDevice == null) {
                Log.d("mUsbDevice","mUsbDevice");
            }
            return false;
        }
        return mUsbManager.hasPermission(mUsbDevice);
    }

    /**
     * Get the InputStream.
     * @return
     */
    public InputStream getInputStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }

            @Override
            public int read(byte[] buffer) throws IOException {
                return mPort.read(buffer,0);
            }

            @Override
            public void close() throws IOException {
                mPort.close();
            }
        };
    }

    /**
     * Get the OutputStream.
     * @return
     */
    public OutputStream getOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }

            @Override
            public void write(byte[] buff) throws IOException {
                mPort.write(buff,0);
            }

            @Override
            public void close() throws IOException {
                mPort.close();
            }
        };
    }

    public boolean initSerialPort() throws Exception {
        // Find all available drivers from attached devices.
        if (mUsbManager.getDeviceList().isEmpty()) {
            return false;
        }
        mAvailableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (mAvailableDrivers.isEmpty()) {
            return false;
        }
        // Open a connection to the first available driver.
        mDriver = mAvailableDrivers.get(0);
        mConnection = mUsbManager.openDevice(mDriver.getDevice());
        if (mConnection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            //requestPermission();
        }
        // Read some data! Most have just one port (port 0).
        mPort = mDriver.getPorts().get(0);
        try {
            mPort.open(mConnection);
            mPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            // Deal with error.
            e.printStackTrace();
            try {
                if (mPort != null)
                    mPort.close();
            } catch (IOException ie) {
                ie.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * The close method has delegation to the InputStream and OutputStream;
     * @throws IOException
     */
    @Deprecated
    public void close() throws IOException {
        if (mPort != null) {
            mPort.close();
        }
    }

}

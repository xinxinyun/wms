package com.util;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

public class APIUtil {
    public static boolean isSupport(int apiNo){
        return Build.VERSION.SDK_INT >= apiNo;
    }

    /**
     * serialport object
     * @author Administrator
     *
     */
    public static class SerialPort {

        private static final String TAG = "SerialPort";

        /*
         * Do not remove or rename the field mFd: it is used by native method close();
         */
        private FileDescriptor mFd;
        private FileInputStream mFileInputStream;
        private FileOutputStream mFileOutputStream;

        /**
         * Build SerialPort Object
         * @param device device file
         * @param baudrate baudrate
         * @param flags read and write mode of device files
         * @throws IOException Device file read and write exceptions
         */
        public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

            /* Check access permission */
            if (!device.canRead() || !device.canWrite()) {
                try {
                    /* Missing read/write permission, trying to chmod the file */
                    Process su;
                    su = Runtime.getRuntime().exec("/system/bin/su");
                    String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                            + "exit\n";
                    su.getOutputStream().write(cmd.getBytes());
                    if ((su.waitFor() != 0) || !device.canRead()
                            || !device.canWrite()) {
                        throw new SecurityException();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new SecurityException();
                }
            }

            mFd = open(device.getAbsolutePath(), baudrate, flags);
            if (mFd == null) {
                Log.e(TAG, "native open returns null");
                throw new IOException();
            }
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
        }

        /**
         * Get the files Inputstream
         * @return Returns the Inputstream object
         */
        public InputStream getInputStream() {
            return mFileInputStream;
        }

        /**
         * Get the files Outputstream
         * @return Returns the Outputstream object
         */
        public OutputStream getOutputStream() {
            return mFileOutputStream;
        }

        /**
         * Open serialport device file
         * @param path Device file path
         * @param baudrate Set the serialport baud rate
         * @param flags Read and write mode of device files
         * @return Device file description object
         */
        private native static FileDescriptor open(String path, int baudrate, int flags);

        /**
         * Close device file
         */
        public native void close();
        static {
            System.loadLibrary("serial_port");
        }
    }

    /**
     * Object to find serialport device;
     * @author Administrator
     *
     */
    public static class SerialPortFinder {

        /**
         * Object to describe  hardware device
         * @author Administrator
         *
         */
        public class Driver {
            public Driver(String name, String root) {
                mDriverName = name;
                mDeviceRoot = root;
            }
            private String mDriverName;
            private String mDeviceRoot;
            Vector<File> mDevices = null;

            /**
             * All the objects to describe hardware device
             * @return devices container
             */
            public Vector<File> getDevices() {
                if (mDevices == null) {
                    mDevices = new Vector<File>();
                    File dev = new File("/dev");
                    File[] files = dev.listFiles();
                    int i;
                    for (i=0; i<files.length; i++) {
                        if (files[i].getAbsolutePath().startsWith(mDeviceRoot)) {
                            Log.d(TAG, "Found new device: " + files[i]);
                            mDevices.add(files[i]);
                        }
                    }
                }
                return mDevices;
            }
            public String getName() {
                return mDriverName;
            }
        }

        private static final String TAG = "SerialPort";

        private Vector<com.uhf.uhf.serialport.SerialPortFinder.Driver> mDrivers = null;

        Vector<com.uhf.uhf.serialport.SerialPortFinder.Driver> getDrivers() throws IOException {
            if (mDrivers == null) {
                mDrivers = new Vector<com.uhf.uhf.serialport.SerialPortFinder.Driver>();
                LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
                String l;
                while((l = r.readLine()) != null) {
                    // Issue 3:
                    // Since driver name may contain spaces, we do not extract driver name with split()
                    String drivername = l.substring(0, 0x15).trim();
                    String[] w = l.split(" +");
                    if ((w.length >= 5) && (w[w.length-1].equals("serial"))) {
                        Log.d(TAG, "Found new driver " + drivername + " on " + w[w.length-4]);
                        mDrivers.add(new com.uhf.uhf.serialport.SerialPortFinder().new Driver(drivername, w[w.length-4]));
                    }
                }
                r.close();
            }
            return mDrivers;
        }

        /**
         * Obtain all the devices filenames
         * @return all the filename characters
         */
        public String[] getAllDevices() {
            Vector<String> devices = new Vector<String>();
            // Parse each driver
            Iterator<com.uhf.uhf.serialport.SerialPortFinder.Driver> itdriv;
            try {
                itdriv = getDrivers().iterator();
                while(itdriv.hasNext()) {
                    com.uhf.uhf.serialport.SerialPortFinder.Driver driver = itdriv.next();
                    Iterator<File> itdev = driver.getDevices().iterator();
                    while(itdev.hasNext()) {
                        String device = itdev.next().getName();
                        String value = String.format("%s (%s)", device, driver.getName());
                        devices.add(value);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return devices.toArray(new String[devices.size()]);
        }

        /**
         * Obtain all the filepath
         * @return all the filepath string array
         */
        public String[] getAllDevicesPath() {
            Vector<String> devices = new Vector<String>();
            // Parse each driver
            Iterator<com.uhf.uhf.serialport.SerialPortFinder.Driver> itdriv;
            try {
                itdriv = getDrivers().iterator();
                while(itdriv.hasNext()) {
                    com.uhf.uhf.serialport.SerialPortFinder.Driver driver = itdriv.next();
                    Iterator<File> itdev = driver.getDevices().iterator();
                    while(itdev.hasNext()) {
                        String device = itdev.next().getAbsolutePath();
                        devices.add(device);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return devices.toArray(new String[devices.size()]);
        }
    }
}

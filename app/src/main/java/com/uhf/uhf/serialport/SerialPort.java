/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.uhf.uhf.serialport;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
 * serialport object
 * @author Administrator
 *
 */
public class SerialPort {

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
	 * @throws SecurityException Unauthorized read file
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

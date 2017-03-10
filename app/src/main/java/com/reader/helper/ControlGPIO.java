package com.reader.helper;

/**
 * GPIO basic operation use this class you can control the status of GPIO .
 */

public class ControlGPIO {
	/** mark to turn off module  */
	public static final int OFF = 0;
	/** mark to turn on module*/
	public static final int ON = 1;
	/** mark to indicate that module switch is setted successfully  */
	public static final int SET_SUCCESSED = 1;
	/** mark to indicate that module switch is not setted successfully */
	public static final int SET_FAILED = 0;
	private static ControlGPIO mControlGPIO = new ControlGPIO();
	private ControlGPIO(){
			System.loadLibrary("ControlGPIO");
	}

	/**
	 * Get the  ControlGPIO Instance
	 * @return ControlGPIO Instance 
	 */
	public static ControlGPIO newInstance(){
		return mControlGPIO;
	}

	/*public int readGPIO(){
		return JNIreadGPIO();
	}

	public int writeGPIO(int value){
		return  JNIwriteGPIO(value);
	}*/

    /**
     * Read the status of module  
     * @return status value of module, value equals ControlGPIO.ON indicate that module is on, or equals ControlGPIO.OFF indicate that side module is off  
     */
	public native int JNIreadGPIO();
	
	/**
	 * Open or close module   
	 * @param value   send ControlGPIO.ON to open module, send ControlGPIO.OFF to close module.
	 * @return        If the value returned equals ControlGPIO.SET_SUCCESSED,then it's setted successfully, if the value returned equals ControlGPIO.SET_FAILED, then it's not setted successfully. 
	 */
	public native int JNIwriteGPIO(int value);

}

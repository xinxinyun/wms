/**
 * filename:ReaderMethod.java
 * CopyRright (c) 2008-xxxx:
 * File Number:2014-03-12_001
 * Creater:Jie Zhu
 * Date:2014/03/12
 * Modified By:Jie Zhu
 * Date:2014/03/12
 * Description:Initial Version
 * Version:1.0.0
*/ 

package com.reader.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.reader.base.CMD;
import com.reader.base.HEAD;

public abstract class ReaderBase {
	private WaitThread mWaitThread = null;
	private InputStream mInStream = null;
	private OutputStream mOutStream = null;
	
	/**
	 * Connection Lost.
	 */
	public abstract void onLostConnect();
	
	/** 
	 * Rewritable function,This function will be called after parse a packet data.
	 * @param msgTran	Parsed packet
	 */
	public abstract void analyData(MessageTran msgTran);

	private byte[] m_btAryBuffer = new byte[4096];
	private int m_nLength = 0;
	
	/**
	 * With reference constructor. Construct a Reader with input and output streams.
	 * @param in	Input Stream
	 * @param out	Output Stream
	 */
	public ReaderBase(InputStream in, OutputStream out) {
		this.mInStream = in;
		this.mOutStream = out;
		
		StartWait();
	}
	
	public boolean IsAlive() {
		return mWaitThread != null && mWaitThread.isAlive();
	}
	
	public void StartWait() {
		mWaitThread = new WaitThread();
		mWaitThread.start();
	}

	/**
	 * Loop receiving data thread.
	 * @author Jie
	 */
	private class WaitThread extends Thread {
		private boolean mShouldRunning = true;

		public WaitThread() {
			mShouldRunning = true;
		}

		@Override
		public void run() {
			byte[] btAryBuffer = new byte[4096];
			while (mShouldRunning) {
				try {
					int nLenRead = mInStream.read(btAryBuffer);
					
					if (nLenRead > 0) {
						byte[] btAryReceiveData = new byte[nLenRead];
						System.arraycopy(btAryBuffer, 0, btAryReceiveData, 0,
								nLenRead);
						runReceiveDataCallback(btAryReceiveData);
					}
				} catch (IOException e) {
					onLostConnect();
					return ;
				} catch (Exception e) {
					onLostConnect();
					return ;
				}
			}
		}

		public void signOut() {
			mShouldRunning = false;
			interrupt();
		}
	};

	/**
	 * Exit receive thread.
	 */
	public final void signOut() {
		mWaitThread.signOut();
		try {
			mInStream.close();
			mOutStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * After reading the data from Instream, it will call this function.
	 * @param btAryReceiveData	Received Data
	 */
	private void runReceiveDataCallback(byte[] btAryReceiveData) {
		try {
			reciveData(btAryReceiveData);

			int nCount = btAryReceiveData.length;
			byte[] btAryBuffer = new byte[nCount + m_nLength];
			
			System.arraycopy(m_btAryBuffer, 0, btAryBuffer, 0, m_nLength);
			System.arraycopy(btAryReceiveData, 0, btAryBuffer, m_nLength,
					btAryReceiveData.length);
			// Array.Copy(m_btAryBuffer, btAryBuffer, m_nLenth);
			// Array.Copy(btAryReceiveData, 0, btAryBuffer, m_nLenth,
			// btAryReceiveData.Length);

			int nIndex = 0; 
			int nMarkIndex = 0; 
			for (int nLoop = 0; nLoop < btAryBuffer.length; nLoop++) {
				if (btAryBuffer.length > nLoop + 1) {
					if (btAryBuffer[nLoop] == HEAD.HEAD) {
						int nLen = btAryBuffer[nLoop + 1] & 0xFF;
						if (nLoop + 1 + nLen < btAryBuffer.length) {
							byte[] btAryAnaly = new byte[nLen + 2];
							System.arraycopy(btAryBuffer, nLoop, btAryAnaly, 0,
									nLen + 2);
							// Array.Copy(btAryBuffer, nLoop, btAryAnaly, 0,
							// nLen + 2);

							MessageTran msgTran = new MessageTran(btAryAnaly);
							analyData(msgTran);

							nLoop += 1 + nLen;
							nIndex = nLoop + 1;
						} else {
							nLoop += 1 + nLen;
						}
					} else {
						nMarkIndex = nLoop;
					}
				}
			}

			if (nIndex < nMarkIndex) {
				nIndex = nMarkIndex + 1;
			}

			if (nIndex < btAryBuffer.length) {
				m_nLength = btAryBuffer.length - nIndex;
				Arrays.fill(m_btAryBuffer, 0, 4096, (byte) 0);
				System.arraycopy(btAryBuffer, nIndex, m_btAryBuffer, 0,
						btAryBuffer.length - nIndex);
				// Array.Clear(m_btAryBuffer, 0, 4096);
				// Array.Copy(btAryBuffer, nIndex, m_btAryBuffer, 0,
				// btAryBuffer.Length - nIndex);
			} else {
				m_nLength = 0;
			}
		} catch (Exception e) {

		}
	}
	
	/** 
	 * Rewritable function,This function will be called when data is received.
	 * @param btAryReceiveData	Received Data
	 */
	public void reciveData(byte[] btAryReceiveData) {};

	/** 
	 * Rewritable function,This function will be called after sending data.
	 * @param btArySendData	Transmitted Data
	 */
	public void sendData(byte[] btArySendData) {};


	/**
	 * refresh the data buffer
	 */
	public void refreshBuffer() {};
	
	/**
	 * Send data,Use synchronized() to prevent concurrent operation.
	 * @param btArySenderData	To send data
	 * @return	Succeeded :0, Failed:-1
	 */
	private int sendMessage(byte[] btArySenderData) {
		
		try {
			synchronized (mOutStream) {		
				mOutStream.write(btArySenderData);
			}
		} catch (IOException e) {
			onLostConnect();
			return -1;
		} catch (Exception e) {
			return -1;
		}

		sendData(btArySenderData);
		
		return 0;
	}

	private int sendMessage(byte btReadId, byte btCmd) {
		MessageTran msgTran = new MessageTran(btReadId, btCmd);

		return sendMessage(msgTran.getAryTranData());
	}

	private int sendMessage(byte btReadId, byte btCmd, byte[] btAryData) {
		MessageTran msgTran = new MessageTran(btReadId, btCmd, btAryData);

		return sendMessage(msgTran.getAryTranData());
	}

	/**
	 * Reset the specified address reader.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int reset(byte btReadId) {
		
		byte btCmd = CMD.RESET;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Set the Serial Communication Baudrate.
	 * @param btReadId			Reader Address(0xFF Public Address)
	 * @param nIndexBaudrate	Baudrate(0x03: 38400bps, 0x04:115200 bps)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setUartBaudrate(byte btReadId, byte nIndexBaudrate) {
		byte btCmd = CMD.SET_UART_BAUDRATE;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = nIndexBaudrate;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Get Reader Firmware Version.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getFirmwareVersion(byte btReadId) {
		byte btCmd = CMD.GET_FIRMWARE_VERSION;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Set Reader Address.
	 * @param btReadId		Original Reader Address(0xFF Public Address)
	 * @param btNewReadId	New Reader Address,value range0-254(0x00-0xFE)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setReaderAddress(byte btReadId, byte btNewReadId) {
		byte btCmd = CMD.SET_READER_ADDRESS;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = btNewReadId;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Set work antenna.
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btWorkAntenna	Ant ID(0x00:Ant 1, 0x01:Ant 2, 0x02:Ant 3, 0x03:Ant 4)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setWorkAntenna(byte btReadId, byte btWorkAntenna) {
		byte btCmd = CMD.SET_WORK_ANTENNA;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = btWorkAntenna;
		
		int nResult = sendMessage(btReadId, btCmd, btAryData);
		
		return nResult;
	}

	/**
	 * Query working antenna.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getWorkAntenna(byte btReadId) {
		byte btCmd = CMD.GET_WORK_ANTENNA;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Set output power(Method 1).
	 * <br> This command consumes more than 100mS.
	 * <br> If you want you change the output power frequently, please use Cmd_set_temporary_output_power command, which doesn't reduce the life of the internal flash memory.
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btOutputPower	RF output power, range from 0 to 33(0x00 - 0x21), the unit is dBm.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setOutputPower(byte btReadId, byte btOutputPower) {
		byte btCmd = CMD.SET_OUTPUT_POWER;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = btOutputPower;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}
	
	/**
	 * Set output power(Method 2). <br>
	 * This command consumes more than 100mS. <br>
	 * If you want you change the output power frequently, please use
	 * Cmd_set_temporary_output_power command, which doesn't reduce the life of
	 * the internal flash memory.
	 * 
	 * @param btReadId
	 *            Reader Address(0xFF Public Address)
	 * @param btPower1
	 *            Output power of antenna 1, range from 0 to 33(0x00 - 0x21),
	 *            the unit is dBm.
	 * @param btPower2
	 *            Output power of antenna 2, range from 0 to 33(0x00 - 0x21),
	 *            the unit is dBm.
	 * @param btPower3
	 *            Output power of antenna 3, range from 0 to 33(0x00 - 0x21),
	 *            the unit is dBm.
	 * @param btPower4
	 *            Output power of antenna 4, range from 0 to 33(0x00 - 0x21),
	 *            the unit is dBm.
	 * @return Succeeded :0, Failed:-1
	 */
	public final int setOutputPower(byte btReadId, byte btPower1, byte btPower2, byte btPower3, byte btPower4) {
		byte btCmd = CMD.SET_OUTPUT_POWER;
		byte[] btAryData = new byte[4];
		
		btAryData[0] = btPower1;
		btAryData[1] = btPower2;
		btAryData[2] = btPower3;
		btAryData[3] = btPower4;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Query output power.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getOutputPower(byte btReadId) {
		byte btCmd = CMD.GET_OUTPUT_POWER;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Set frequency region(system default frequencies).
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btRegion		Spectrum regulation(0x01:FCC, 0x02:ETSI, 0x03:CHN)
	 * @param btStartRegion	Start frequency of the spectrum,
	 * @param btEndRegion	End frequency of the spectrum,Setup the range of the RF output spectrum. The rules are: 1,Start frequency and end frequency should be in the range of the specified regulation. 2,Start frequency should be equal or lower than end frequency. 3, End frequency equals start frequency means use single frequency point.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setFrequencyRegion(byte btReadId, byte btRegion,
			byte btStartRegion, byte btEndRegion) {
		byte btCmd = CMD.SET_FREQUENCY_REGION;
		byte[] btAryData = new byte[3];
		
		btAryData[0] = btRegion;
		btAryData[1] = btStartRegion;
		btAryData[2] = btEndRegion;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Set frequency region(user defined frequencies).
	 * @param btReadId			Reader Address(0xFF Public Address)
	 * @param btFreqInterval	Frequency space, frequency space = FreqSpace x 10KHz.
	 * @param btChannelQuantity	Frequency Quantity, this quantity includes the start frequency, if set this byte to 1, it means use start frequency as the single carrier frequency . This byte should be larger than 0.
	 * @param nStartFreq		Start Frequency, the unit is KHz. Set the start frequency with hex format, for example, 915000KHz = 0D F6 38 KHz.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setUserDefineFrequency(byte btReadId, byte btFreqInterval, 
			byte btChannelQuantity, int nStartFreq) {
		byte btCmd = CMD.SET_FREQUENCY_REGION;
		byte[] btAryFreq = new byte[3];
		byte[] btAryData = new byte[6];
		
		btAryFreq = Converter.getBytes(nStartFreq, Converter.BIG_ENDIAN);

		btAryData[0] = 4;
		btAryData[1] = btFreqInterval;
		btAryData[2] = btChannelQuantity;
		btAryData[3] = btAryFreq[2];
		btAryData[4] = btAryFreq[1];
		btAryData[5] = btAryFreq[0];

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Query frequency region.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getFrequencyRegion(byte btReadId) {
		byte btCmd = CMD.GET_FREQUENCY_REGION;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Set Buzzer behavior.
	 * <br>Buzzer behavior 0x02(Beep after every tag has identified) occupies CPU process time that affects anti-collision algorithm significantly. It is recommended that this option should be used for tag test.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btMode	Buzzer behavior(0x00:Quiet, 0x01:Beep after every inventory round if tag(s) identified, 0x02:Beep after every tag has identified.)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setBeeperMode(byte btReadId, byte btMode) {
		byte btCmd = CMD.SET_BEEPER_MODE;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = btMode;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Query internal temperature.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getReaderTemperature(byte btReadId) {
		byte btCmd = CMD.GET_READER_TEMPERATURE;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Read GPIO Level.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int readGpioValue(byte btReadId) {
		byte btCmd = CMD.READ_GPIO_VALUE;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Write GPIO Level.
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btChooseGpio	ChooseGpio(0x03:Set GPIO3, 0x04:Set GPIO4)
	 * @param btGpioValue	GpioValue(0x00:Set to low level, 0x01:Set to high level)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int writeGpioValue(byte btReadId, byte btChooseGpio, byte btGpioValue) {
		byte btCmd = CMD.WRITE_GPIO_VALUE;
		byte[] btAryData = new byte[2];
		
		btAryData[0] = btChooseGpio;
		btAryData[1] = btGpioValue;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Set antenna connection detector status.
	 * @param btReadId			Reader Address(0xFF Public Address)
	 * @param btDetectorStatus	status(0x00:close detector of antenna connection, other values:sensitivity of antenna connection detector(return loss of port),unit dB. The higher the value, the greater the impedance matching requirements for port
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setAntConnectionDetector(byte btReadId, byte btDetectorStatus) {
		byte btCmd = CMD.SET_ANT_CONNECTION_DETECTOR;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = btDetectorStatus;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Get antenna connection detector status.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getAntConnectionDetector(byte btReadId) {
		byte btCmd = CMD.GET_ANT_CONNECTION_DETECTOR;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Set temporary output power.
	 * <br>The output power value will Not be saved to the internal flash memory so that the output power will be restored from the internal flash memory after restart or power off.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btRfPower	RF output power, range from 20-33(0x14 - 0x21), the unit is dBm.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setTemporaryOutputPower(byte btReadId, byte btRfPower) {
		byte btCmd = CMD.SET_TEMPORARY_OUTPUT_POWER;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = btRfPower;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Set Reader Identifier.
	 * <br>The identifier is stored in internal flash.
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btAryIdentifier	Reader's identifier (12 bytes).
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setReaderIdentifier(byte btReadId, byte[] btAryIdentifier) {
		byte btCmd = CMD.SET_READER_IDENTIFIER;

		int nResult = sendMessage(btReadId, btCmd, btAryIdentifier);

		return nResult;
	}

	/**
	 * Get Reader Identifier.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getReaderIdentifier(byte btReadId) {
		byte btCmd = CMD.GET_READER_IDENTIFIER;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * RF Link Setup.
	 * <br>If this command succeeded, reader will be reset, and the profile configuration is stored in the internal flash.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btProfile	Communication rate(0xD0:Tari 25uS,FM0 40KHz, 0xD1:Tari 25uS,Miller 4 250KHz, 0xD2:Tari 25uS,Miller 4 300KHz, 0xD3:Tari 6.25uS,FM0 400KHz)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setRfLinkProfile(byte btReadId, byte btProfile) {
		byte btCmd = CMD.SET_RF_LINK_PROFILE;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = btProfile;
		
		int nResult = sendMessage(btReadId, btCmd, btAryData);
		
		return nResult;
	}

	/**
	 * Read RF Link.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getRfLinkProfile(byte btReadId) {
		byte btCmd = CMD.GET_RF_LINK_PROFILE;

		int nResult = sendMessage(btReadId, btCmd);
		
		return nResult;
	}

	/**
	 * Measure RF Port Return Loss.
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btFrequency	FreqParameter, system will measure the return loss of current antenna port at the desired frequency.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getRfPortReturnLoss(byte btReadId, byte btFrequency) {
		byte btCmd = CMD.GET_RF_PORT_RETURN_LOSS;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = btFrequency;
		
		int nResult = sendMessage(btReadId, btCmd, btAryData);
		
		return nResult;
	}

	/**
	 * Inventory Tag.
	 * <br>When reader gets this command, the inventory for EPC GEN2 tags starts, tag data will be stored in the internal buffer.
	 * <br>Attention:
	 * <br>When sets Repeat parameter to 255(0xFF), the anti-collision algorithm is optimized for applications with small tag quantity, which provide better efficiency and less response time.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btRepeat	Repeat time of inventory round. When Repeat = 255, The inventory duration is minimized. For example, if the RF field only has one or two tags, the inventory duration could be only 30-50 mS, this function provides a possibility for fast antenna switch applications on multi-ant devices.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int inventory(byte btReadId, byte btRepeat) {
		byte btCmd = CMD.INVENTORY;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = btRepeat;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Read Tag.
	 * <br>Attention:
	 * <br>If two tags have the same EPC, but different read data, then these two tags are considered different tags.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btMemBank	Tag memory bank(0x00:RESERVED, 0x01:EPC, 0x02:TID, 0x03:USER)
	 * @param btWordAdd	Read start address,please see the tag's spec for more information.
	 * @param btWordCnt	Read data length,Data length in WORD(16bits) unit. Please see the tag's spec for more information.
	 * @param btAryPassWord	Access password,4 bytes.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int readTag(byte btReadId, byte btMemBank, byte btWordAdd,
			byte btWordCnt, byte[] btAryPassWord) {
		byte btCmd = CMD.READ_TAG;
		byte[] btAryData = null;
		if (btAryPassWord == null || btAryPassWord.length < 4) {
			btAryPassWord = null;
			btAryData = new byte[3];
		} else {
			btAryData = new byte[3 + 4];
		}
		
		btAryData[0] = btMemBank;
		btAryData[1] = btWordAdd;
		btAryData[2] = btWordCnt;
		
		if (btAryPassWord != null) {
			System.arraycopy(btAryPassWord, 0, btAryData, 3, btAryPassWord.length);
		}

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Write Tag.
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btAryPassWord	Access password, 4 bytes.
	 * @param btMemBank		Tag memory bank(0x00:RESERVED, 0x01:EPC, 0x02:TID, 0x03:USER)
	 * @param btWordAdd		Write start address,WORD(16 bits). When write EPC area, notice that EPC starts from address 02, the first two 2 words are for PC+CRC.
	 * @param btWordCnt		WORD(16 bits), please see the tag's spec for more information.
	 * @param btAryData		Write data, btWordCnt*2 bytes.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int writeTag(byte btReadId, byte[] btAryPassWord, byte btMemBank,
			byte btWordAdd, byte btWordCnt, byte[] btAryData) {
		byte btCmd = CMD.WRITE_TAG;
		byte[] btAryBuffer = new byte[btAryData.length + 7];
		
		System.arraycopy(btAryPassWord, 0, btAryBuffer, 0, btAryPassWord.length);
		// btAryPassWord.CopyTo(btAryBuffer, 0);
		btAryBuffer[4] = btMemBank;
		btAryBuffer[5] = btWordAdd;
		btAryBuffer[6] = btWordCnt;
		System.arraycopy(btAryData, 0, btAryBuffer, 7, btAryData.length);
		// btAryData.CopyTo(btAryBuffer, 7);

		int nResult = sendMessage(btReadId, btCmd, btAryBuffer);

		return nResult;
	}

	/**
	 * Lock Tag.
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btAryPassWord	Access password, 4 bytes.
	 * @param btMembank		Tag memory bank(0x01:User Memory, 0x02:TID Memory, 0x03:EPC Memory, 0x04:Access Password, 0x05:Kill Password)
	 * @param btLockType	Lock operation type(0x00:Open, 0x01:Lock, 0x02:Permanent open, 0x03:Permanent lock)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int lockTag(byte btReadId, byte[] btAryPassWord, byte btMemBank,
			byte btLockType) {
		byte btCmd = CMD.LOCK_TAG;
		byte[] btAryData = new byte[6];
		
		System.arraycopy(btAryPassWord, 0, btAryData, 0, btAryPassWord.length);
		// btAryPassWord.CopyTo(btAryData, 0);
		btAryData[4] = btMemBank;
		btAryData[5] = btLockType;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Kill Tag.
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btAryPassWord	Kill password,4 bytes
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int killTag(byte btReadId, byte[] btAryPassWord) {
		byte btCmd = CMD.KILL_TAG;
		byte[] btAryData = new byte[4];
		
		System.arraycopy(btAryPassWord, 0, btAryData, 0, btAryPassWord.length);
		// btAryPassWord.CopyTo(btAryData, 0);

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Set Access EPC match(EPC match is effective,until next refresh).
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btEpcLen	Length of EPC.
	 * @param btAryEpc	EPC, Length equals EpcLen.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setAccessEpcMatch(byte btReadId, byte btEpcLen,
			byte[] btAryEpc) {
		byte btCmd = CMD.SET_ACCESS_EPC_MATCH;
		int nLen = (btEpcLen & 0xFF) + 2;
		byte[] btAryData = new byte[nLen];
		
		btAryData[0] = 0x00;
		btAryData[1] = btEpcLen;
		System.arraycopy(btAryEpc, 0, btAryData, 2, btAryEpc.length);
		// btAryEpc.CopyTo(btAryData, 2);
		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Clear EPC match.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int cancelAccessEpcMatch(byte btReadId) {
		byte btCmd = CMD.SET_ACCESS_EPC_MATCH;
		byte[] btAryData = new byte[1];
        btAryData[0] = 0x01;

        int nResult = sendMessage(btReadId, btCmd, btAryData);

        return nResult;
	}

	/**
	 * Query match EPC status.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getAccessEpcMatch(byte btReadId) {
		byte btCmd = CMD.GET_ACCESS_EPC_MATCH;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Inventory Tag(Read Time Mode).
	 * <br>Attention:
	 * <br> The hardware has a dual CPU architecture, main CPU is responsible for tag inventory, and assistant CPU is responsible for data management. Inventory and data transfer are parallel and simultaneous. So the data transfer via serial port doesn't affect the efficiency of reader.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btRepeat	Repeat time of inventory round. When Repeat = 255, The inventory duration is minimized. For example, if the RF field only has one or two tags, the inventory duration could be only 30-50 mS, this function provides a possibility for fast antenna switch applications on multi-ant devices.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int realTimeInventory(byte btReadId, byte btRepeat) {
		byte btCmd = CMD.REAL_TIME_INVENTORY;
		byte[] btAryData = new byte[1];
		
		btAryData[0] = btRepeat;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}
	
	/**
	 * Fast Switch Antenna Mode.
	 * <br>Attention:
	 * <br> The hardware has a dual CPU architecture, main CPU is responsible for tag inventory, and assistant CPU is responsible for data management. Inventory and data transfer are parallel and simultaneous. So the data transfer via serial port doesn't affect the efficiency of reader.
	 * <br>In massive tag applications, please use cmd_real_time_inventory command which is more effective for large tag quantity applications.
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btA			First working ant (00 - 03). If set this byte above 03 means ignore it.
	 * @param btStayA		Inventory round for an antenna.
	 * @param btB			Second working ant (00 - 03). If set this byte above 03 means ignore it.
	 * @param btStayB		Inventory round for an antenna.
	 * @param btC			Third working ant (00 - 03). If set this byte above 03 means ignore it.
	 * @param btStayC		Inventory round for an antenna.
	 * @param btD			Fourth working ant (00 - 03). If set this byte above 03 means ignore it.
	 * @param btStayD		Inventory round for an antenna.
	 * @param btInterval	Rest time between switching antennas. During the cause of rest, RF output will be cancelled, thus power consumption and heat generation are both reduced.
	 * @param btRepeat		Repeat the inventory with above ant switch sequence.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int fastSwitchAntInventory(byte btReadId,
			byte btA, byte btStayA, byte btB, byte btStayB, 
			byte btC, byte btStayC, byte btD, byte btStayD, 
			byte btInterval, byte btRepeat) {
		
		byte btCmd = CMD.FAST_SWITCH_ANT_INVENTORY;
		byte[] btAryData = new byte[10];
		
		btAryData[0] = btA;
		btAryData[1] = btStayA;
		btAryData[2] = btB;
		btAryData[3] = btStayB;
		btAryData[4] = btC;
		btAryData[5] = btStayC;
		btAryData[6] = btD;
		btAryData[7] = btStayD;
		btAryData[8] = btInterval;
		btAryData[9] = btRepeat;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * User define session and target inventory.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btSession	Desired session ID
	 * @param btTarget	Desired Inventoried Flag(0x00:A, 0x01:B)
	 * @param btRepeat	Number of times of repeating this inventory.
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int customizedSessionTargetInventory(byte btReadId,
			byte btSession, byte btTarget, byte btRepeat) {
		
		byte btCmd = CMD.CUSTOMIZED_SESSION_TARGET_INVENTORY;
		byte[] btAryData = new byte[3];
		
		btAryData[0] = btSession;
		btAryData[1] = btTarget;
		btAryData[2] = btRepeat;
		
		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Set Impinj Monza FastTID.
	 * <br>Attention:
	 * <br>This function is only affective for some of Impinj Monza tag types.
	 * <br>This function improves the performance of identifying tag's TID.
	 * <br>When this function takes effect, tag's TID will be included to tag's EPC, therefore, tag's EPC will be altered; the original data (PC + EPC) will be changed to altered PC + EPC + EPC's CRC + TID.
	 * <br>If error occurred during identifying TID, only the original data (PC + EPC) will be sent.
	 * <br>If you don't need this function, please turn it off, otherwise there will be unnecessary time consumption.
	 * <br>This command doesn't store the status to internal flash. After reset or power on, the value stored in flash will be restored.
	 * @param btReadId	Reader Address(0xFF public address)
	 * @param blnOpen	Whether to open FastTID
	 * @param blnSave	Whether to store in FLASH
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int setImpinjFastTid(byte btReadId, boolean blnOpen, boolean blnSave) {
		
		byte btCmd = (blnSave ? CMD.SET_AND_SAVE_IMPINJ_FAST_TID : CMD.SET_IMPINJ_FAST_TID);
		byte[] btAryData = new byte[1];
		
		btAryData[0] = (byte) (blnOpen ? 0x8D : 0x00);
		
		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Query current set of FastTID.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getImpinjFastTid(byte btReadId) {
		byte btCmd = CMD.GET_IMPINJ_FAST_TID;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Inventory 18000-6B Tag.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int iso180006BInventory(byte btReadId) {
		byte btCmd = CMD.ISO18000_6B_INVENTORY;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Read 18000-6B Tag.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btAryUID	Operated Tag's UID, 8 bytes
	 * @param btWordAdd	Read data first address
	 * @param btWordCnt	Read data length
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int iso180006BReadTag(byte btReadId, byte[] btAryUID, byte btWordAdd, byte btWordCnt) {
		byte btCmd = CMD.ISO18000_6B_READ_TAG;
		int nLen = btAryUID.length + 2;
		byte[] btAryData = new byte[nLen];
		
		System.arraycopy(btAryUID, 0, btAryData, 0, btAryUID.length);
		// btAryUID.CopyTo(btAryData, 0);
		btAryData[nLen - 2] = btWordAdd;
		btAryData[nLen - 1] = btWordCnt;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Write 18000-6B Tag.
	 * @param btReadId		Reader Address(0xFF Public Address)
	 * @param btAryUID		Operated Tag's UID, 8 bytes
	 * @param btWordAdd		Write data first address
	 * @param btWordCnt		Write data length
	 * @param btAryBuffer	Write data
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int iso180006BWriteTag(byte btReadId, byte[] btAryUID, byte btWordAdd,
			byte btWordCnt, byte[] btAryBuffer) {
		byte btCmd = CMD.ISO18000_6B_WRITE_TAG;
		int nLen = btAryUID.length + 2 + btAryBuffer.length;
		byte[] btAryData = new byte[nLen];
		
		System.arraycopy(btAryUID, 0, btAryData, 0, btAryUID.length);
		// btAryUID.CopyTo(btAryData, 0);
		btAryData[btAryUID.length] = btWordAdd;
		btAryData[btAryUID.length + 1] = btWordCnt;
		System.arraycopy(btAryBuffer, 0, btAryData, btAryUID.length + 2,
				btAryBuffer.length);
		// btAryBuffer.CopyTo(btAryData, btAryUID.length + 2);

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Lock 18000-6B Tag.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btAryUID	Operated Tag's UID, 8 bytes
	 * @param btWordAdd	Locked address
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int iso180006BLockTag(byte btReadId, byte[] btAryUID, byte btWordAdd) {
		byte btCmd = CMD.ISO18000_6B_LOCK_TAG;
		int nLen = btAryUID.length + 1;
		byte[] btAryData = new byte[nLen];
		
		System.arraycopy(btAryUID, 0, btAryData, 0, btAryUID.length);
		// btAryUID.CopyTo(btAryData, 0);
		btAryData[nLen - 1] = btWordAdd;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Query 18000-6B Tag.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @param btAryUID	Operated Tag's UID, 8 bytes
	 * @param btWordAdd	To query address
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int iso180006BQueryLockTag(byte btReadId, byte[] btAryUID, byte btWordAdd) {
		byte btCmd = CMD.ISO18000_6B_QUERY_LOCK_TAG;
		int nLen = btAryUID.length + 1;
		byte[] btAryData = new byte[nLen];
		
		System.arraycopy(btAryUID, 0, btAryData, 0, btAryUID.length);
		// btAryUID.CopyTo(btAryData, 0);
		btAryData[nLen - 1] = btWordAdd;

		int nResult = sendMessage(btReadId, btCmd, btAryData);

		return nResult;
	}

	/**
	 * Get tag data and keep buffer.
	 * <br>Attention:
	 * <br>The data in the buffer won't be lost after execution of this command.
	 * <br>If the cmd_inventory is executed again, the tag data escalate in the buffer.
	 * <br>Other 18000-6C commands can clear the buffer.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getInventoryBuffer(byte btReadId) {
		byte btCmd = CMD.GET_INVENTORY_BUFFER;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Get tag data and clear buffer.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getAndResetInventoryBuffer(byte btReadId) {
		byte btCmd = CMD.GET_AND_RESET_INVENTORY_BUFFER;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Query tag quantity in buffer.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int getInventoryBufferTagCount(byte btReadId) {
		byte btCmd = CMD.GET_INVENTORY_BUFFER_TAG_COUNT;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Clear buffer of tag data.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int resetInventoryBuffer(byte btReadId) {
		byte btCmd = CMD.RESET_INVENTORY_BUFFER;

		int nResult = sendMessage(btReadId, btCmd);

		return nResult;
	}

	/**
	 * Set the mask filter the Tag.
	 * @param btReadId The reader address.
	 * @param btTarget Set the inventory way(s0,s1,s2 or s3),you must use you set target to inventory the tag.
	 * @param btAction The match tag or not Action,you can see the detail of this command.
	 * @param btMembank The select mask region,EPC,TID or USER.
	 * @param btStartAdd The mask start address(according to bit).
	 * @param btMaskLen The mask length (according to bit).
	 * @param maskValue The mask value.
	 * @return
	 */
	public final int setTagMask(byte btReadId,byte btMaskNo,byte btTarget,byte btAction,byte btMembank,byte btStartAdd,byte btMaskLen,byte[] maskValue)
	{
		byte[] btAryData = new byte[7 + maskValue.length];
		btAryData[0] = btMaskNo;
		btAryData[1] = btTarget;
		btAryData[2] = btAction;
		btAryData[3] = btMembank;
		btAryData[4] = btStartAdd;
		btAryData[5] = btMaskLen;
		System.arraycopy(maskValue, 0, btAryData, 6, maskValue.length);
		btAryData[btAryData.length - 1] = (byte)0x00;

		int nResult = sendMessage(btReadId,(byte)0x98,btAryData);

		return nResult;
	}

	/**
	 * Get the mask setting.
	 * @param btReadId the reader address.
	 * @return
	 */
	public final int getTagMask(byte btReadId)
	{
		byte[] btAryData = { (byte)0x20 };

		int nResult = sendMessage(btReadId,(byte)0x98,btAryData);

		return nResult;
	}

	/**
	 * Clear the mask setting.
	 * @param btReadId the reader address.
	 * @return
	 */
	public final int clearTagMask(byte btReadId,byte btMaskNo)
	{
		byte[] btAryData = { btMaskNo };

		int nResult = sendMessage(btReadId,(byte)0x98,btAryData);

		return nResult;
	}
	
	/**
	 * Send naked data.
	 * @param btReadId	Reader Address(0xFF Public Address)
	 * @return	Succeeded :0, Failed:-1
	 */
	public final int sendBuffer(byte[] btAryBuf) {

		int nResult = sendMessage(btAryBuf);

		return nResult;
	}
}

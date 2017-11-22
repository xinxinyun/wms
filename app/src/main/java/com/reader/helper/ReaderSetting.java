package com.reader.helper;

/**
 * Reader setting describe object
 * @author Administrator
 *
 */
public class ReaderSetting {

	/** Reader ID*/
	public byte btReadId;
	/** Reader major version NO*/
	public byte btMajor;
	/** Reader Minor version NO*/
	public byte btMinor;
	@Deprecated
	public byte btIndexBaudrate;
	/**Reader temperature symbol*/
	public byte btPlusMinus;
	/**Reader temperature value */
	public byte btTemperature;
	/**Output power 1-4 byte*/
	public byte []btAryOutputPower;
	/** Work Antenna*/
	public byte btWorkAntenna;
	@Deprecated
	public byte btDrmMode;
	/** Frequency Region*/
	public byte btRegion;
	/** Frequency Start value*/
	public byte btFrequencyStart;
	/** Frequency End value*/
	public byte btFrequencyEnd;
	/** Beeper Mode*/
	public byte btBeeperMode;
	/** Reader GPI01 */
	public byte btGpio1Value;
	/** Reader GPI02 */
	public byte btGpio2Value;
	/** Reader GPI03 */
	public byte btGpio3Value;
	/** Reader GPI04 */
	public byte btGpio4Value;
	
	/**Antenna sensitivity*/
	public byte btAntDetector;
	/** Monza TID switch status*/
	public byte btMonzaStatus;
	/** Monza Whether save the status*/
	public boolean blnMonzaStore;
	/**Identifier fixed 12 bytes*/
	public byte []btAryReaderIdentifier;
	/** Return loss */
	public byte btReturnLoss;
	@Deprecated
	public byte btImpedanceFrequency;
	
	/** Custom starting frequency*/
	public int nUserDefineStartFrequency;
	/** Custom channel spacing*/
	public byte btUserDefineFrequencyInterval;
	/**  custom number frequency point */
	public byte btUserDefineChannelQuantity;
	/** Rf communications link*/
	public byte btRfLinkProfile;

	/** Get the mask setting value*/
	public byte[] btsGetMaskValue;

	/**
	 * Set the object's default constructor
	 */
	public ReaderSetting() {
		btReadId = (byte) 0xFF;
		btMajor = 0x00;
		btMinor = 0x00;
		btIndexBaudrate = 0x00;
		btPlusMinus = 0x00;
		btTemperature = 0x00;
		btAryOutputPower = null;
		btWorkAntenna = 0x00;
		btDrmMode = 0x00;
		btRegion = 0x00;
		btFrequencyStart = 0x00;
		btFrequencyEnd = 0x00;
		btBeeperMode = 0x00;
		blnMonzaStore = false;
		btGpio1Value = 0x00;
		btGpio2Value = 0x00;
		btGpio3Value = 0x00;
		btGpio4Value = 0x00;
		btAntDetector = 0x00;
		btMonzaStatus = 0x00;
		btAryReaderIdentifier = new byte[12];
		btReturnLoss = 0x00;
		btImpedanceFrequency = 0x00;
		nUserDefineStartFrequency = 0x00;
		btUserDefineFrequencyInterval = 0x00;
		btUserDefineChannelQuantity = 0x00;
		btRfLinkProfile = 0x00;
	}

}

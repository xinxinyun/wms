package com.reader.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
  *cache object of 6C tag inventoried 
  *
  */
public class InventoryBuffer {
	/**
	 * object discribe 6C tag
	 * @author Administrator
	 *
	 */
    public static class InventoryTagMap {
    	/** PC value*/
		public String strPC;
		/** CRC value*/
		public String strCRC;
		/** EPC value*/
		public String strEPC;
		/** antenna ID*/
		public byte btAntId;
		/** RSSI value*/
		public String strRSSI;
		/** read times */
		public int nReadCount;
		/**carrier frequency*/
		public String strFreq;
		/** work antenna 1*/
		public int nAnt1;
		/** work antenna 2*/
		public int nAnt2;
		/** work antenna 3*/
		public int nAnt3;
		/** work antenna 4*/
		public int nAnt4;
		
		/**
		 * Defaulted constructor
		 */
		public InventoryTagMap() {
			strPC = "";
			strCRC = "";
			strEPC = "";
			btAntId = 0;
			strRSSI = "";
			nReadCount = 0;
			strFreq = "";
			nAnt1 = 0;
			nAnt2 = 0;
			nAnt3 = 0;
			nAnt4 = 0;
		}
    }

    /** inventory times for each tag */
	public byte btRepeat;
	/** Customized Session */
	public byte btSession;
	/** Session custom tag type  */
	public byte btTarget;
	/** A-D 4 work antennas*/
	public byte btA, btB, btC, btD;
	/** Antenna for inventory*/
	public byte btStayA, btStayB, btStayC, btStayD;
	/** The time for antenna switch */
	public byte btInterval;
	/** Inventory tag again by antennas switch sequence before */
	public byte btFastRepeat;
	
	/**antenna list*/
	public ArrayList<Byte> lAntenna;
	/** loop inventory tag*/
	public boolean bLoopInventory;
	/** antenna index*/
	public int nIndexAntenna;
	/** mark for antenna loop inventory */
	public int nCommond;
	/** real time inventory  */
	public boolean bLoopInventoryReal;
	/** Custom Session loop inventory tag mark*/
	public boolean bLoopCustomizedSession;
	
	/** tag quantity */
	public int nTagCount;
	/** The tag quantity returned after a command exected */
	public int nDataCount;
	/** Time for excuting command  */
	public int nCommandDuration;
	/** Reading speed of tag*/
	public int nReadRate;
	/** working antenne*/
	public int nCurrentAnt;
	/**accumulate Data returned*/
	public int nTotalRead;
	/** The time when inventory starts*/
	public Date dtStartInventory;
	/** The time when inventory ends*/
	public Date dtEndInventory;
	/** Max RSSI value */
	public int nMaxRSSI;
	/** Min RSSI value */
	public int nMinRSSI;
	/** EPC is putted in specific place of tag containner*/
	public Map<String, Integer> dtIndexMap;
	/** Containner for saving tags */
	public List<InventoryTagMap> lsTagList;
	/**cache object of Tag inventory*/
    public InventoryBuffer() {
		btRepeat = 0x00;
		btFastRepeat = 0x00;
		
		/** If it's bigger than 3, it doesn't inventory  */
		btA = btB = btC = btD = (byte) 0xFF;
		btStayA = btStayB = btStayC = btStayD = 0x01;
		
		lAntenna = new ArrayList<Byte>();
		bLoopInventory = false;
		nIndexAntenna = 0;
		nCommond = 0;
		bLoopInventoryReal = false;
		
		nTagCount = 0;
		nReadRate = 0;
		nTotalRead = 0;
		dtStartInventory = new Date();
		dtEndInventory = dtStartInventory;
		nMaxRSSI = 0;
		nMinRSSI = 0;
		
		dtIndexMap = new LinkedHashMap<String, Integer>();
		lsTagList = new ArrayList<InventoryTagMap>();
    }

    /**
     *  reset setting 
     */
    public final void clearInventoryPar() {
		btRepeat = 0x00;
		lAntenna.clear();
		//bLoopInventory = false;
		//lAntenna.add((byte)0x00);
		nIndexAntenna = 0;
		nCommond = 0;
		bLoopInventoryReal = false;
    }

    /**
     * reset result of inventory 
     */
    public final void clearInventoryResult() {
		nTagCount = 0;
		nReadRate = 0;
		nTotalRead = 0;
		dtStartInventory = new Date();
		dtEndInventory = dtStartInventory;
		nMaxRSSI = 0;
		nMinRSSI = 0;
		clearTagMap();
    }

    /**
     * reset result of real-time inventory 
     */
    public final void clearInventoryRealResult() {
		nTagCount = 0;
		nReadRate = 0;
		nTotalRead = 0;
		dtStartInventory = new Date();
		dtEndInventory = dtStartInventory;
		nMaxRSSI = 0;
		nMinRSSI = 0;
		clearTagMap();
    }
    
    /**
     * Clear tags in cache 
     */
    public final void clearTagMap() {
    	dtIndexMap.clear();
		lsTagList.clear();
    }

}

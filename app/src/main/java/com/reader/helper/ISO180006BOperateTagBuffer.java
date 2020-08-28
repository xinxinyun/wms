package com.reader.helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ISO180006BOperateTagBuffer {
	
	/**
	 * Cache object of 6B tag's operations(including of all operations)
	 * @author Administrator
	 *
	 */
    public static class ISO180006BOperateTagMap {
    	/** Antenna ID*/
    	public byte btAntId;
    	/** UID value*/
    	public String strUID;
    	/** Total tags operated*/
    	public int nTotal;
    	/** Setting the starting address of tag */
    	public byte btStartAdd;
    	/** Setting the length of tag data */
    	public int nLength;
    	/** tag data operated  */
    	public String strData;
		/** the status of tag*/
    	public byte btStatus;
		
    	/**
    	 * Constructor of the  6B tag  
    	 */
		public ISO180006BOperateTagMap() {
			btAntId = 0;
			strUID = "";
			nTotal = 0;
			btStartAdd = 0;
			nLength = 0;
			strData = "";
			btStatus = 0;
		}
    }

    /** The tag position in cache  */
    public Map<String, Integer> dtIndexMap;
    /** Cache  of tags */
	public List<ISO180006BOperateTagMap> lsTagList;
	/** antenna ID */
	public byte btAntId;
	/** antenna quantity */
	public int nTagCount;
	/** tag data */
	public String strReadData;
	/** write length of data */
	public byte btWriteLength;
	/**status of tag */
	public byte btStatus;

	/**
	 * Cache object of 6B tags
	 */
    public ISO180006BOperateTagBuffer() {
		btAntId = (byte) 0xFF;
		nTagCount = 0;
		strReadData = "";
		btWriteLength = 0x00;
		btStatus = 0x00;
		
		dtIndexMap = new LinkedHashMap<String, Integer>();
		lsTagList = new ArrayList<ISO180006BOperateTagMap>();
    }

    /**
     * Reset setting
     */
    public void clearBuffer() {
		btAntId = (byte) 0xFF;
		nTagCount = 0;
		strReadData = "";
		btWriteLength = 0x00;
		btStatus = 0x00;

		clearTagMap();
    }
    
    /**
     * Clear cache 
     */
    public final void clearTagMap() {
    	dtIndexMap.clear();
		lsTagList.clear();
    }

}

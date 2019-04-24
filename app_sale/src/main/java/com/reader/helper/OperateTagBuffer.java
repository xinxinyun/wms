package com.reader.helper;

import java.util.ArrayList;
import java.util.List;

/**
 * cache object of 6C tag's Operations (including writing and killing) 
 * @author Administrator
 *
 */
public class OperateTagBuffer {
	/**
	 * described object of 6C tag's Operations (including writing and killing)
	 * @author Administrator
	 *
	 */
    public static class OperateTagMap {
    	/** PC value*/
    	public String strPC;
    	/** CRC value*/
    	public String strCRC;
    	/** EPC value*/
    	public String strEPC;
    	/** Data in total */
    	public String strData;
    	/** Length of data */
    	public int nDataLen;
    	/** Antenna ID*/
    	public byte btAntId;
    	/** Operating times */
    	public int nReadCount;
		/**
		 *Operations (including writing and killing)tag default constructor
		 */
		public OperateTagMap() {
			strPC = "";
			strCRC = "";
			strEPC = "";
			strData = "";
			nDataLen = 0;
			btAntId = 0;
			nReadCount = 0;
		}
    }
    
    /** Pick specific tag from the cache object*/
	public String strAccessEpcMatch;
	/** cache object */
	public List<OperateTagMap> lsTagList;
	
	/**
	 * Operate cache of tags, including writing and killing  
	 */
	public OperateTagBuffer() {
		strAccessEpcMatch = "";
		lsTagList = new ArrayList<OperateTagMap>();
	}
	
	/**
	 * Clear cache  of tags 
	 */
    public final void clearBuffer() {
    	lsTagList.clear();
    }

}

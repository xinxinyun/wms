package com.uhf.uhf.tagpage;

import java.util.List;

import com.reader.helper.InventoryBuffer.InventoryTagMap;
import com.reader.helper.OperateTagBuffer.OperateTagMap;
import com.uhf.uhf.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AccessListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;

	private Context mContext;
	
	private List<OperateTagMap> listMap;
	
	public final class ListItemView{                
		public TextView mIdText;
		public TextView mPCText;
		public TextView mCRCText;
		public TextView mEpcText;
		public TextView mDataText;
		public TextView mDataLenText;
		public TextView mAntennaText;
		public TextView mTimesText;
    }

	public AccessListAdapter(Context context, List<OperateTagMap> listMap) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.listMap = listMap;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listMap.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListItemView  listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			convertView = mInflater.inflate(R.layout.tag_access_list_item, null);
			listItemView.mIdText = (TextView)convertView.findViewById(R.id.id_text);
			listItemView.mPCText = (TextView)convertView.findViewById(R.id.pc_text);
			listItemView.mCRCText = (TextView)convertView.findViewById(R.id.crc_text);
			listItemView.mEpcText = (TextView)convertView.findViewById(R.id.epc_text);
			listItemView.mDataText = (TextView)convertView.findViewById(R.id.data_text);
			
			
			listItemView.mDataLenText = (TextView)convertView.findViewById(R.id.data_len_text);
			listItemView.mAntennaText = (TextView)convertView.findViewById(R.id.antenna_text);
			listItemView.mTimesText = (TextView)convertView.findViewById(R.id.times_text);
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}
		
		/*//add by lei.li 2016/11/11
		if (!listMap.isEmpty()) {
			 listItemView.mEpcText.getLayoutParams().width = lengthestEPC();
			 listItemView.mDataText.getLayoutParams().width = lengthestData();
		}
		//add by lei.li 2016/11/11
*/		
		OperateTagMap map = listMap.get(position);
		
		listItemView.mIdText.setText(String.valueOf(position + 1));
		listItemView.mPCText.setText(map.strPC);
		listItemView.mCRCText.setText(map.strCRC);
		listItemView.mEpcText.setText(map.strEPC);
		listItemView.mDataText.setText(map.strData);
		listItemView.mDataLenText.setText(String.valueOf(map.nDataLen));
		listItemView.mAntennaText.setText(String.valueOf(map.btAntId & 0xFF));
		listItemView.mTimesText.setText(String.valueOf(map.nReadCount & 0xFF));

		return convertView;

	}	
	
	/**
	 * get lengthest data in listMap 
	 * @return the max show area
	 */
	private int lengthestEPC() {
		int length = 0;
		for (OperateTagMap itm : listMap){
			 if (length < itm.strEPC.length())
				 length = itm.strEPC.length();
		}
		return length * 16;
	}
	
	private int lengthestData() {
		int length = 0;
		for (OperateTagMap itm : listMap){
			 if (length < itm.strData.length())
				 length = itm.strData.length();
		}
		return length * 16;
	}
}

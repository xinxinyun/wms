package com.uhf.uhf.tagpage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.reader.helper.InventoryBuffer.InventoryTagMap;
import com.uhf.uhf.R.id;
import com.uhf.uhf.R.layout;

import java.util.List;

public class RealListAdapter extends BaseAdapter {
    //add by lei.li 2016/11/10
    public static int mWidthest = 0;

    private LayoutInflater mInflater;

    private Context mContext;

    private List<InventoryTagMap> listMap;

    private boolean isPhaseVisiable = false;

    private int mUseAntCount = 1;

    public final class ListItemView {
        public TextView mIdText;
        public TextView mEpcText;
        public TextView mPcText;
        public TextView mTimesText;
        public TextView mRssiText;
        public TextView mFreqText;
        public TextView mPhaseText;
    }

    public RealListAdapter(Context context, List<InventoryTagMap> listMap) {
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

        ListItemView listItemView = null;
        if (convertView == null) {
            listItemView = new ListItemView();
            convertView = mInflater.inflate(layout.tag_real_list_item, null);
            listItemView.mIdText = (TextView) convertView.findViewById(id.id_text);
            listItemView.mEpcText = (TextView) convertView.findViewById(id.epc_text);
            //add by lei.li 2016/11/11
			/*if (!listMap.isEmpty()) {
			    listItemView.mEpcText.getLayoutParams().width = lengthestData();
			    mWidthest = listItemView.mEpcText.getLayoutParams().width;
			}*/
            //add by lei.li 2016/11/11

            //add by lei.li 2016/11/14
            //add by lei.li 2016/11/14


            listItemView.mPcText = (TextView) convertView.findViewById(id.pc_text);
            listItemView.mTimesText = (TextView) convertView.findViewById(id.times_text);
            listItemView.mRssiText = (TextView) convertView.findViewById(id.rssi_text);
            listItemView.mPhaseText = (TextView) convertView.findViewById(id.phase_text);
            if (isPhaseVisiable)
                listItemView.mPhaseText.setVisibility(View.VISIBLE);
            else
                listItemView.mPhaseText.setVisibility(View.GONE);
            listItemView.mFreqText = (TextView) convertView.findViewById(id.freq_text);
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();
            if (isPhaseVisiable)
                listItemView.mPhaseText.setVisibility(View.VISIBLE);
            else
                listItemView.mPhaseText.setVisibility(View.GONE);
            //add by lei.li 2016/11/11
			/*if (!listMap.isEmpty())
			    listItemView.mEpcText.getLayoutParams().width = lengthestData();*/
            //add by lei.li 2016/11/11
        }

        //add by lei.li 2016/

        InventoryTagMap map = listMap.get(position);

        listItemView.mIdText.setText(String.valueOf(position + 1));
        listItemView.mEpcText.setText(map.strEPC);
        listItemView.mPcText.setText(map.strPC);
        String readTimes = "";
        if (!map.isBufferTag) {
            switch (mUseAntCount) {
                case 1:
                    readTimes = map.nAnt1 + "";
                    break;
                case 4:
                    readTimes = map.nAnt1 + "/" + map.nAnt2 + "/" + map.nAnt3 + "/" + map.nAnt4;
                    break;
                case 8:
                    readTimes = map.nAnt1 + "/" + map.nAnt2 + "/" + map.nAnt3 + "/" + map.nAnt4 + "/"
                            +   map.nAnt5 + "/" + map.nAnt6 + "/" + map.nAnt7 + "/" + map.nAnt8;
                    break;
                default:
                    readTimes = map.nAnt1 + "";
                    break;
            }
        } else {
            readTimes = map.nReadCount + "";
        }
        listItemView.mTimesText.setText(readTimes);
        try {
            listItemView.mRssiText.setText((Integer.parseInt(map.strRSSI) - 129) + "dBm");
        } catch (Exception e) {
            listItemView.mRssiText.setText("");
        }
        listItemView.mPhaseText.setText(map.strPhase);
        listItemView.mFreqText.setText(map.strFreq);
        return convertView;

    }

    /**
     * get lengthest data in listMap
     *
     * @return the max show area
     */
    public int lengthestData() {
        int length = 0;
        for (InventoryTagMap itm : listMap) {
            if (length < itm.strEPC.length())
                length = itm.strEPC.length();
        }
        return length * 16;
    }

    public boolean isPhaseVisiable() {
        return isPhaseVisiable;
    }

    public void setPhaseVisiable(boolean phaseVisiable) {
        isPhaseVisiable = phaseVisiable;
    }

    public int getmUseAntCount() {
        return mUseAntCount;
    }

    public void setmUseAntCount(int mUseAntCount) {
        this.mUseAntCount = mUseAntCount;
    }
}

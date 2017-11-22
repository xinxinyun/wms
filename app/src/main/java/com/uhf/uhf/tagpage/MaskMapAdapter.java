package com.uhf.uhf.tagpage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uhf.uhf.R;

import java.util.List;

/**
 * Created by Administrator on 6/8/2017.
 */

public class MaskMapAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private Context mContext;

    private List<MaskMap> listMap;

public final class ListItemView {
    public TextView mMaskNo;
    public TextView mTarget;
    public TextView mAction;
    public TextView mMembank;
    public TextView mStartMaskAdd;
    public TextView mMaskLen;
    public TextView mMaskValue;
}

    public static class MaskMap {
        public String mMaskNo;
        public String mTarget;
        public String mAction;
        public String mMembank;
        public String mStartMaskAdd;
        public String mMaskLen;
        public String mMaskValue;
    }


    public MaskMapAdapter(Context context, List<MaskMap> listMap) {
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
            convertView = mInflater.inflate(R.layout.get_mask_value_list_item, null);
            listItemView.mMaskNo = (TextView) convertView.findViewById(R.id.mask_no);
            listItemView.mTarget = (TextView) convertView.findViewById(R.id.get_target);


            listItemView.mAction = (TextView) convertView.findViewById(R.id.get_action);
            listItemView.mMembank = (TextView) convertView.findViewById(R.id.get_membank);
            listItemView.mStartMaskAdd = (TextView) convertView.findViewById(R.id.get_start_mask_add);
            listItemView.mMaskLen = (TextView) convertView.findViewById(R.id.mask_len);
            listItemView.mMaskValue = (TextView) convertView.findViewById(R.id.mask_value);
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();

            MaskMap map = listMap.get(position);

            listItemView.mMaskNo.setText(map.mMaskNo);
            listItemView.mTarget.setText(map.mTarget);
            listItemView.mAction.setText(map.mAction);
            listItemView.mMembank.setText(map.mMembank);
            listItemView.mStartMaskAdd.setText(map.mStartMaskAdd);
            listItemView.mMaskLen.setText(map.mMaskLen);
            listItemView.mMaskValue.setText(map.mMaskValue);


            return convertView;

        }
        return convertView;
    }
}

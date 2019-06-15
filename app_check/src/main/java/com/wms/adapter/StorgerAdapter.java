package com.wms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wms.bean.MaterialInfo;
import com.wms.ui.R;

import java.util.List;


public class StorgerAdapter extends BaseAdapter {

    private Context context;
    private List<MaterialInfo> materialInfoList;
    private LayoutInflater mInflater;//布局装载器对象

    public StorgerAdapter(Context context,
                          List<MaterialInfo> materialInfoList) {
        this.context = context;
        this.materialInfoList = materialInfoList;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return materialInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return materialInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.simple_list_item_1, null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.txCodeTextView = (TextView) convertView.findViewById(R.id.tv_txcode);
            viewHolder.num = (TextView) convertView.findViewById(R.id.num);
            viewHolder.actualNum = convertView.findViewById(R.id.actualNum);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.iv_storgeImage);
            viewHolder.scheduleImageLayout = (LinearLayout) convertView.findViewById(R.id.storgeLayout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MaterialInfo waitMaterial = materialInfoList.get(position);
        Integer num = waitMaterial.getAccountQuantity() == null ? 0 : waitMaterial.getAccountQuantity();
        Integer actualNum = waitMaterial.getCheckQuantity() == null ? 0 : waitMaterial.getCheckQuantity();

        viewHolder.title.setText(waitMaterial.getMaterialName());
        viewHolder.num.setText(num.toString());
        viewHolder.txCodeTextView.setText(waitMaterial.getMaterialBarcode());
        viewHolder.actualNum.setText(actualNum.toString());
        //viewHolder.imageView.setImageResource(R.drawable.right);

        if (actualNum == num) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.scheduleImageLayout.getLayoutParams();
            params.setMargins(35, 0, 0, 0);
            viewHolder.imageView.setImageResource(R.drawable.right);
            //viewHolder.actualNum.setText(""+actualNum);
        } else if (actualNum > num) {
            viewHolder.imageView.setImageResource(R.drawable.nocheck);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.scheduleImageLayout.getLayoutParams();
            params.setMargins(35, 0, 0, 0);
            //viewHolder.actualNum.setText("" + (actualNum - num));
        } else if (actualNum < num && actualNum != 0) {
            //viewHolder.actualNum.setText(""+actualNum);
            viewHolder.imageView.setBackgroundResource(R.drawable.nocheck);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.scheduleImageLayout.getLayoutParams();
            params.setMargins(35, 0, 0, 0);
        }

        return convertView;

    }

    // ViewHolder用于缓存控件，三个属性分别对应item布局文件的三个控件
    class ViewHolder {
        public TextView title;
        public TextView num;
        public TextView actualNum;
        public TextView txCodeTextView;
        public ImageView imageView;
        public LinearLayout scheduleImageLayout;
    }
}

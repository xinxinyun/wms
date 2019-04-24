package com.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bean.MaterialInfo;
import com.uhf.uhf.R;

import java.util.List;

public class SaleAdapter extends BaseAdapter {

    private Context context;
    private List<MaterialInfo> materialInfoList;
    private LayoutInflater mInflater;//布局装载器对象

    public SaleAdapter(Context context,
                       List<MaterialInfo> materialInfoList) {
        this.context = context;
        this.materialInfoList = materialInfoList;
        this.mInflater=LayoutInflater.from(context);
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
            convertView = mInflater.inflate(R.layout.simple_list_item_saleotime, null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.tv_sale_title);
            viewHolder.txCodeTextView = (TextView) convertView.findViewById(R.id.tv_saleCode);
            viewHolder.num = (TextView) convertView.findViewById(R.id.saleNum);
            viewHolder.actualNum = convertView.findViewById(R.id.actualSaleNum);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.iv_saleImage);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MaterialInfo waitMaterial = materialInfoList.get(position);
        Integer num = waitMaterial.getSource() == null ? 0 : waitMaterial.getSource();
        Integer actualNum = waitMaterial.getActualNum() == null ? 0 : waitMaterial.getActualNum();

        viewHolder.title.setText(waitMaterial.getMaterialName());
        viewHolder.num.setText(num.toString());
        viewHolder.txCodeTextView.setText(waitMaterial.getMaterialCode());
        viewHolder.actualNum.setText(actualNum==0?"未盘点":actualNum.toString());

        //不同状态右侧图标显示不同
        if (actualNum == num) {
            viewHolder.imageView.setBackgroundResource(R.drawable.right);
        } else if (actualNum > num) {
            viewHolder.imageView.setVisibility(View.GONE);
            viewHolder.actualNum.setText("+" + (actualNum - num));
        } else if (actualNum < num && actualNum != 0) {
            viewHolder.imageView.setVisibility(View.GONE);
            viewHolder.actualNum.setText("" + (actualNum - num));
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

    }
}

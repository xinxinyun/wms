package com.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
            viewHolder.saleLayout = (LinearLayout) convertView.findViewById(R.id.saleLayout);
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

        //不同状态右侧图标显示不同
        if (actualNum == num) {
            viewHolder.actualNum.setText("   " + actualNum);
            viewHolder.imageView.setImageResource(R.drawable.right);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.saleLayout.getLayoutParams();
            params.setMargins(65, 0, 0, 0);
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
        public LinearLayout saleLayout;
    }
}

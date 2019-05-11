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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorgerAdapter extends BaseAdapter {

    private Context context;
    private List<MaterialInfo> materialInfoList;
    private LayoutInflater mInflater;//布局装载器对象
    private Map<Integer, View> map = new HashMap<Integer, View>();
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
        View view;
        ViewHolder viewHolder = null;
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            view = mInflater.inflate(R.layout.simple_list_item_1, null);
            viewHolder.title = (TextView) view.findViewById(R.id.tv_title);
            viewHolder.txCodeTextView = (TextView) view.findViewById(R.id.tv_txcode);
            viewHolder.num = (TextView) view.findViewById(R.id.num);
            viewHolder.actualNum = view.findViewById(R.id.actualNum);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.iv_storgeImage);
            viewHolder.scheduleImageLayout = (LinearLayout) view.findViewById(R.id.storgeLayout);
            view.setTag(viewHolder);
        } else {
            view = map.get(position);
            viewHolder = (ViewHolder) view.getTag();
        }

        MaterialInfo waitMaterial = materialInfoList.get(position);
        Integer num = waitMaterial.getAccountQuantity() == null ? 0 : waitMaterial.getAccountQuantity();
        Integer actualNum = waitMaterial.getCheckQuantity() == null ? 0 : waitMaterial.getCheckQuantity();

        viewHolder.title.setText(waitMaterial.getMaterialName());
        viewHolder.num.setText(num.toString());
        viewHolder.txCodeTextView.setText(waitMaterial.getMaterialBarcode());
        viewHolder.actualNum.setText(actualNum.toString());

        if (actualNum == num) {
            viewHolder.actualNum.setText(""+actualNum);
            viewHolder.imageView.setTag("");
            viewHolder.imageView.setImageResource(R.drawable.right);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.scheduleImageLayout.getLayoutParams();
            params.setMargins(35, 0, 0, 0);
        } else if (actualNum > num) {
            viewHolder.imageView.setTag("");
            viewHolder.imageView.setImageResource(R.drawable.nocheck);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.scheduleImageLayout.getLayoutParams();
            params.setMargins(35, 0, 0, 0);
            viewHolder.actualNum.setText("" + (actualNum - num));
        } else if (actualNum < num && actualNum != 0) {
            viewHolder.imageView.setTag("");
            viewHolder.imageView.setBackgroundResource(R.drawable.nocheck);
            viewHolder.actualNum.setText(""+actualNum);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.scheduleImageLayout.getLayoutParams();
            params.setMargins(35, 0, 0, 0);
        }

        return view;

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

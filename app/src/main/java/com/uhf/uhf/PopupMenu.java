package com.uhf.uhf;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopupMenu extends PopupWindow implements OnClickListener {

	private Activity activity;
	private View popView;

	private View v_item1;
	private View v_item2;
	private View v_item3;
	private View v_item4;
	private View v_item5;
	private View v_item_add1;

	private OnItemClickListener onItemClickListener;

	public enum MENUITEM {
		ITEM1, ITEM2, ITEM3, ITEM4, ITEM5, ITEM_add1
	}

	public PopupMenu(Activity activity) {
		super(activity);
		this.activity = activity;
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popView = inflater.inflate(R.layout.popup_menu, null);
		this.setContentView(popView);
		this.setWidth(sp2px(activity, 250));
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		this.setTouchable(true); 
		this.setOutsideTouchable(true); 
		ColorDrawable dw = new ColorDrawable(0x00000000);
		this.setBackgroundDrawable(dw);


		v_item1 = popView.findViewById(R.id.ly_item1);
		v_item2 = popView.findViewById(R.id.ly_item2);
		v_item3 = popView.findViewById(R.id.ly_item3);
		v_item4 = popView.findViewById(R.id.ly_item4);
		v_item5 = popView.findViewById(R.id.ly_item5);
		v_item_add1 = popView.findViewById(R.id.ly_item1_add1);

		
		
		v_item1.setOnClickListener(this);
		v_item2.setOnClickListener(this);
		v_item3.setOnClickListener(this);
		v_item4.setOnClickListener(this);
		v_item5.setOnClickListener(this);
		v_item_add1.setOnClickListener(this);

	}


	public void showLocation(int resourId) {
		showAsDropDown(activity.findViewById(resourId), dip2px(activity, 0),
				dip2px(activity, -8));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		MENUITEM menuitem = null;
		String str = "";
		if (v == v_item1) {
			menuitem = MENUITEM.ITEM1;
			str = "item1";
		} else if (v == v_item2) {
			menuitem = MENUITEM.ITEM2;
			str = "item2";
		} else if (v == v_item3) {
			menuitem = MENUITEM.ITEM3;
			str = "item3";
		} else if (v == v_item4) {
			menuitem = MENUITEM.ITEM4;
			str = "item4";
		} else if (v == v_item5) {
			menuitem = MENUITEM.ITEM5;
			str = (String) ((TextView)v_item5.findViewById(R.id.langague)).getText();
			Log.e("debug", "biiiiiiiiiiiiiiiiiiiiiiiiiiiin + popupMenu " + str);
		} else if (v == v_item_add1) {
			menuitem = MENUITEM.ITEM_add1;
			str = "itemadd";
		}
		if (onItemClickListener != null) {
			onItemClickListener.onClick(menuitem, str);
		}
		dismiss();
	}

	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	
	public static int sp2px(Context context, float spValue) {  
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (spValue * fontScale + 0.5f);  
    }

	
	public interface OnItemClickListener {
		public void onClick(MENUITEM item, String str);
	}

	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

}

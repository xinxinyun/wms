package com.uhf.uhf;

import java.util.ArrayList;
import java.util.List;

import com.reader.helper.OperateTagBuffer;
import com.reader.helper.ReaderHelper;
import com.reader.helper.OperateTagBuffer.OperateTagMap;
import com.uhf.uhf.tagpage.AccessListAdapter;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


public class TagAccessList extends LinearLayout {
	private Context mContext;
	private TableRow mTagAccessRow;
	private ImageView mTagAccessImage;
	private TextView mListTextInfo;
	
	private ReaderHelper mReaderHelper;
	
	private List<OperateTagMap> data;
	private AccessListAdapter mAccessListAdapter;
	private ListView mTagAccessList;
	
	private View mTagsAccessListScrollView;
	private WindowManager wm;
	
	private static OperateTagBuffer m_curOperateTagBuffer;
	
	public TagAccessList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initContext(context);
	}
	
	public TagAccessList(Context context) {  
        super(context);
        initContext(context);
    }

	private void initContext(Context context) {
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.tag_access_list, this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		data = new ArrayList<OperateTagMap>();
		
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		
		mTagsAccessListScrollView = findViewById(R.id.tags_access_list_scroll_view);
		wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		LayoutParams lp = (LayoutParams) mTagsAccessListScrollView.getLayoutParams();
		lp.height = 0;
		mTagsAccessListScrollView.setLayoutParams(lp);
		mTagsAccessListScrollView.invalidate();
		
		mTagAccessRow = (TableRow) findViewById(R.id.table_row_tag_access);
		mTagAccessImage = (ImageView) findViewById(R.id.image_prompt);
		mTagAccessImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
		mListTextInfo = (TextView) findViewById(R.id.list_text_info);
		mListTextInfo.setText(getResources().getString(R.string.open_tag_list));

		mTagAccessRow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				LayoutParams lp = (LayoutParams) mTagsAccessListScrollView.getLayoutParams();
				
				if (lp.height <= 0) {
					lp.height = wm.getDefaultDisplay().getHeight() / 3;
					mTagsAccessListScrollView.setLayoutParams(lp);
					mTagsAccessListScrollView.invalidate();

					mTagAccessImage.setImageDrawable(getResources().getDrawable(R.drawable.down));
					mListTextInfo.setText(getResources().getString(R.string.close_tag_list));
				} else {
					//mTagsRealListScrollView.setVisibility(View.GONE);

					lp.height = 0;
					mTagsAccessListScrollView.setLayoutParams(lp);
					mTagsAccessListScrollView.invalidate();

					mTagAccessImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
					mListTextInfo.setText(getResources().getString(R.string.open_tag_list));
				}
				
				// add by lei.li 2016/11/12 this is method not perfect.
				Log.e("execute this code!", "UUUUUUUUUUUUUUUUUUUUUUUUUUU");
				/*
				if (mTagAccessList.getChildCount() != 0) {
					mTagsAccessListScrollView.findViewById(R.id.tag_access_type)
							.getLayoutParams().width = mTagAccessList.getChildAt(0)
							.getWidth();
					mTagsAccessListScrollView.findViewById(R.id.epc_text)
							.getLayoutParams().width = lengthestData("epc");
					mTagsAccessListScrollView.findViewById(R.id.data_text).getLayoutParams().width = lengthestData("data");
				//	mTagsAccessListScrollView.findViewById(R.id.tag_access_type).invalidate();
					Log.e("execute this code!", "exexexexexexexexexe");
				} */
		      // add by lei.li 2016/11/12
			}
		});
		
		mTagAccessList = (ListView) findViewById(R.id.tag_real_list_view);
		mAccessListAdapter = new AccessListAdapter(mContext, data);
		mTagAccessList.setAdapter(mAccessListAdapter);
	}
	
	public final void clearText() {
		;
	}
	
	public final void refreshText() {
		;
	}
	
	public final void refreshList() {
		data.clear();
		data.addAll(m_curOperateTagBuffer.lsTagList);
		mAccessListAdapter.notifyDataSetChanged();
		// add by lei.li 2016/11/12 this contion never 
		/*Log.e("execute this code!", "UUUUUUUUUUUUUUUUUUUUUUUUUUU");
		if (mTagAccessList.getChildCount() != 0) {
			mTagsAccessListScrollView.findViewById(R.id.tag_access_type)
					.getLayoutParams().width = mTagAccessList.getChildAt(0)
					.getWidth();
			mTagsAccessListScrollView.findViewById(R.id.epc_text)
					.getLayoutParams().width = lengthestData("epc");
			mTagsAccessListScrollView.findViewById(R.id.data_text).getLayoutParams().width = lengthestData("data");
		//	mTagsAccessListScrollView.findViewById(R.id.tag_access_type).invalidate();
			Log.e("execute this code!", "exexexexexexexexexe");
		}*/
      // add by lei.li 2016/11/12
	}

	// add by lei.li 2016/11/12
	private int lengthestData(String str) {
		// TODO Auto-generated method stub
		int widest = 0;
		if ("epc".equals(str)) {
		for (OperateTagMap otm : m_curOperateTagBuffer.lsTagList) {
				if (widest < otm.strEPC.length())
					widest = otm.strEPC.length();
			}
		}
		if ("data".equals(str)) {
			for (OperateTagMap otm : m_curOperateTagBuffer.lsTagList) {
				if (widest < otm.strData.length())
					widest = otm.strData.length();
			}
		}
		return widest * 16;
	}
	// add by lei.li 2016/11/12
}

package com.wms.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.wms.util.StatusBarUtil;

/**
 * 版权：xx公司 版权所有
 * 商品拆包
 * @author 周宇
 * 版本：1.0
 * 创建日期：${date}${hour}
 * 描述：RtivePackActivity
 */
public class RtivePackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retpack);

        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.rpToolBar);

        setSupportActionBar(mToolbarTb);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbarTb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    finish();
            }
        });
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        StatusBarUtil.setStatusBarColor(this, Color.parseColor("#3fb1f0"));
    }
}

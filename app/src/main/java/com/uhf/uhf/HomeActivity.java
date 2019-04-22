package com.uhf.uhf;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import com.ui.DbTestActivity;
import com.ui.RfidActivity;
import com.ui.base.BaseActivity;
import com.util.StatusBarUtil;

//
public class HomeActivity extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        StatusBarUtil.setRootViewFitsSystemWindows(this,true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        //if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            //StatusBarUtil.setStatusBarColor(this,0x55000000);00CCFF
            StatusBarUtil.setStatusBarColor(this, Color.parseColor("#00CCFF"));
        //}

        CardView cardView = (CardView) findViewById(R.id.cardView);
        CardView saleCardView = (CardView) findViewById(R.id.saleCardView);

        CardView outTimeCardView = (CardView) findViewById(R.id.outTimeCardView);

        cardView.setOnClickListener(this);
        saleCardView.setOnClickListener(this);
        outTimeCardView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cardView:
                Intent areaIntent = new Intent().setClass(HomeActivity.this, AreaCheckActitity.class);
                startActivity(areaIntent);
                break;
            case R.id.saleCardView:
                Intent outTimeIntent = new Intent().setClass(HomeActivity.this, DbTestActivity.class);
                startActivity(outTimeIntent);
                break;
            case R.id.outTimeCardView:
                Intent allAreacardViewIntent = new Intent().setClass(HomeActivity.this, RfidActivity.class);
                startActivity(allAreacardViewIntent);
                break;
            default:
                return;
        }
    }
}
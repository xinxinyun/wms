package com.uhf.uhf;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import com.ui.OutTimeActivity;
import com.ui.SaleActivity;
import com.ui.base.BaseActivity;
import com.util.StatusBarUtil;

import cn.pedant.SweetAlert.SweetAlertDialog;

//
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        //if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
        //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
        //这样半透明+白=灰, 状态栏的文字能看得清

        //StatusBarUtil.setStatusBarColor(this,0x55000000);//00CCFF  #00CCFF
        StatusBarUtil.setStatusBarColor(this, Color.parseColor("#f6f6f6"));
        //}
        StatusBarUtil.getStatusBarLightMode(getWindow());


        CardView cardView = findViewById(R.id.cardView);
        CardView saleCardView = findViewById(R.id.saleCardView);

        CardView outTimeCardView = findViewById(R.id.outTimeCardView);
        CardView aboutCardView = findViewById(R.id.aboutCardView);

        cardView.setOnClickListener(this);
        saleCardView.setOnClickListener(this);
        outTimeCardView.setOnClickListener(this);
        aboutCardView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cardView:
                Intent areaIntent = new Intent().setClass(HomeActivity.this, AreaCheckActitity.class);
                startActivity(areaIntent);
                break;
            case R.id.saleCardView:
                Intent outTimeIntent = new Intent().setClass(HomeActivity.this, SaleActivity.class);
                startActivity(outTimeIntent);
                break;
            case R.id.outTimeCardView:
                Intent allAreacardViewIntent = new Intent().setClass(HomeActivity.this, OutTimeActivity.class);
                startActivity(allAreacardViewIntent);
                break;
            case R.id.aboutCardView:
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
                sweetAlertDialog.setContentText("睿平台是艾睿默公司倾情打造的智能仓库管理平台，欢迎您的使用。");
                sweetAlertDialog.show();
            default:
                return;
        }
    }
}
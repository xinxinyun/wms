package com.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.uhf.uhf.R;

/**
 * 系统开屏页面
 */
public class SplashScreenActivity extends Activity {

    /**
     * 图标标签
     */
    private ImageView ivIcon;
    /**
     * 延迟时间
     */
    private static final int DELAY_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        // 利用布局资源文件设置用户界面
        setContentView(R.layout.activity_splash_screen);

        // 通过资源标识获得控件实例
        ivIcon = (ImageView) findViewById(R.id.appImage);

        // 加载动画配置文件，启动动画
        ivIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.topitem_in));

        // 利用消息处理器实现延迟跳转到登录窗口
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 启动登录窗口
                startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                // 关闭启动画面
                finish();
            }
        }, DELAY_TIME);
    }
}

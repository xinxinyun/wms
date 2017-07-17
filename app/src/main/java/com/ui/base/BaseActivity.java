package com.ui.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

import com.reader.helper.ControlGPIO;
import com.uhf.uhf.R;

import java.util.Locale;

public class BaseActivity extends Activity {

    private static final String TAG = "BaseActivity";
    private static final boolean DEBUG = true;

    protected boolean mSwitchFlag = false;
    private VirtualKeyListenerBroadcastReceiver mVirtualKeyListenerBroadcastReceiver;
    protected AudioManager mAudioManager = null;
    protected AudioManager.OnAudioFocusChangeListener mAudioFocusChange = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            //do something
            final AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);

            builder.setTitle(getString(R.string.alert_diag_title)).
                    setMessage(getString(R.string.music_is_play_must_exit)).
                    setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mAudioManager.isMusicActive()) {
                                dialog.cancel();
                                mAudioManager.requestAudioFocus(mAudioFocusChange, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                                builder.show();
                                return;
                            }
                            dialog.cancel();
                        }
                    }).
                    setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //close the module
                                    getApplication().onTerminate();
                                }
                            }).setCancelable(false).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceUtil.init(this);
        switchLanguage(PreferenceUtil.getString("language", "zh"));
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }

        if (mAudioManager.isMusicActive()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(getString(R.string.alert_diag_title)).
                    setMessage(getString(R.string.music_is_play_must_exit)).
                    setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getApplication().onTerminate();
                                }
                            }).setCancelable(false).show();
        }

    }

    /**
     * Changing the language of application through setting the local configuration.
     *
     * @param language
     */
    protected void switchLanguage(String language) {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (language.equals("en")) {
            config.locale = Locale.ENGLISH;
        } else {
            config.locale = Locale.SIMPLIFIED_CHINESE;
        }
        resources.updateConfiguration(config, dm);

        PreferenceUtil.commitString("language", language);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Thread.currentThread().sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mAudioManager.requestAudioFocus(mAudioFocusChange, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mVirtualKeyListenerBroadcastReceiver = new VirtualKeyListenerBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        this.registerReceiver(mVirtualKeyListenerBroadcastReceiver, intentFilter);
        if (mSwitchFlag) {
            ControlGPIO.newInstance().JNIwriteGPIO(ControlGPIO.ON);
        }
        //ControlGPIO.newInstance().JNIwriteGPIO(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAudioManager.abandonAudioFocus(mAudioFocusChange);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //ControlGPIO.newInstance().JNIwriteGPIO(0);
    }

    /**
     * Using this Method to close UHF
     * This method is invoked when the application is killed by android system.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //ControlGPIO.newInstance().JNIwriteGPIO(ControlGPIO.OFF);
        //debug info
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        //ControlGPIO.newInstance().JNIwriteGPIO(ControlGPIO.ON);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mSwitchFlag = true;
        }
        return super.onKeyDown(keyCode, event);

    }

    private class VirtualKeyListenerBroadcastReceiver extends BroadcastReceiver {
        private final String SYSTEM_REASON = "reason";
        private final String SYSTEM_HOME_KEY = "homekey";
        private final String SYSTEM_RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String systemReason = intent.getStringExtra(SYSTEM_REASON);
                if (systemReason != null) {
                    if (systemReason.equals(SYSTEM_HOME_KEY)) {
                        System.out.println("Press HOME key");
                    } else if (systemReason.equals(SYSTEM_RECENT_APPS)) {
                        System.out.println("Press RECENT_APPS key");
                        ControlGPIO.newInstance().JNIwriteGPIO(ControlGPIO.OFF);
                        mSwitchFlag = true;
                    }
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        this.unregisterReceiver(mVirtualKeyListenerBroadcastReceiver);
    }
}

package com.wms.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.wms.ui.R;

/**
 * Created by Administrator on 7/6/2017.
 */

public class Beeper {
    public static final int BEEPER = 1;
    public static final int BEEPER_SHORT = 2;
    private static boolean mQuite = false;
    private static boolean mBeepInventoried = false;
    public static String BEEPER_MODEL = "beeper_model";
    private static boolean mBeepPerTag = true;
    private static final SoundPool mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 5);

    static {
        int beeper = PreferenceUtil.getInt(BEEPER_MODEL, 0);
        switch (beeper) {
            case 0:
                mQuite = mBeepInventoried = false;
                mBeepPerTag = true;
                break;
            case 1:
                mQuite = true;
                mBeepPerTag = mBeepInventoried = false;
                break;
            case 2:
                mBeepInventoried = true;
                mQuite = mBeepPerTag = false;
                break;
            case 3:
                mQuite = mBeepInventoried = false;
                mBeepPerTag = true;
                break;
        }
    }

    public enum BeepMode {QUITE, BEEP_INVENTORIED, BEEP_PER_TAG}

    ;

    public static void init(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null)
            return;

        mSoundPool.load(context, R.raw.beeper, BEEPER);
        mSoundPool.load(context, R.raw.beeper_short, BEEPER_SHORT);
    }

    public static void beep(int soundID) {
        if (mQuite) {
            return;
        }
        if (soundID == BEEPER && mBeepInventoried) {
            mSoundPool.play(BEEPER, 1, 1, 0, 0, 1);
            mBeepInventoried = false;
        } else if (soundID == BEEPER_SHORT && mBeepPerTag) {
            mSoundPool.play(BEEPER_SHORT, 1, 1, 0, 0, 1);
        } else if (soundID == BEEPER_SHORT && !mBeepPerTag) {
            mBeepInventoried = true;
        } else {

        }
    }

    /**
     * Set the beep mode.
     *
     * @param beepMode
     */
    public static void setBeepMode(BeepMode beepMode) {
        switch (beepMode) {
            case QUITE:
                mQuite = true;
                mBeepInventoried = mBeepPerTag = false;
                break;
            case BEEP_INVENTORIED:
                mBeepInventoried = mQuite = mBeepPerTag = false;
                break;
            case BEEP_PER_TAG:
                mBeepPerTag = true;
                mQuite = mBeepInventoried = false;
                break;
            default:
                mBeepInventoried = mQuite = mBeepPerTag = false;
                break;
        }

    }

    public static void release() {
        if (mSoundPool != null) {
            mSoundPool.release();
        }
    }
}

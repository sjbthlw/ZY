package com.hzsun.mpos.thread;


import android.util.Log;

import static com.hzsun.mpos.Sound.SoundPlay.VoiceMoneyPlay;

public class soundPlayThread extends Thread {

    private String TAG = getClass().getSimpleName();
    private long s_lngPayMoney;
    public soundPlayThread(long lngPayMoney) {
        Log.i(TAG, "soundPlayThread 构造");
        s_lngPayMoney=lngPayMoney;
    }

    public void StopThread() {

        try {
            this.join();
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void run() {
        VoiceMoneyPlay(s_lngPayMoney, 0);
        return;
    }
}

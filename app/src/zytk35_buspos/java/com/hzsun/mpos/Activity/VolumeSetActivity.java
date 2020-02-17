package com.hzsun.mpos.Activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.hzsun.mpos.R;
import com.hzsun.mpos.views.MarkSeekBar;

import static com.hzsun.mpos.Sound.TTSPlay.TTSVoicePlay;

/**
 * 音量设置界面
 */
public class VolumeSetActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private MarkSeekBar msb_volume_set;
    private Button bt_confirm, bt_cancle;
    private int sMax, sVolume;
    private AudioManager mAudioManager;
    private SeekBar seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume_set);
        initViews();
        initListener();
    }

    private void initViews() {
        setTitle("音量设置");
        msb_volume_set = ((MarkSeekBar) findViewById(R.id.msb_volume_set));
        msb_volume_set.isMarkPercent(false);

        bt_confirm = (Button) findViewById(R.id.bt_confirm);
        bt_cancle = (Button) findViewById(R.id.bt_cancle);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        sVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        msb_volume_set.setMax(sMax);
        msb_volume_set.setProgress(sVolume);
    }


    private void initListener() {
        bt_confirm.setOnClickListener(this);
        bt_cancle.setOnClickListener(this);
        msb_volume_set.setOnSeekBarChangeListener(this);

        TTSVoicePlay("欢迎使用", null);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_cancle:
                finish();
                break;
            case R.id.bt_confirm:
                msb_volume_set.getProgress();
                finish();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        int volumeNow;

        volumeNow = msb_volume_set.getProgress();

        mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                volumeNow,
                AudioManager.FLAG_PLAY_SOUND);

        TTSVoicePlay("欢迎使用", null);
    }
}

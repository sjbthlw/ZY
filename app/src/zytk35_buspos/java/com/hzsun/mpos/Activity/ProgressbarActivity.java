package com.hzsun.mpos.Activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hzsun.mpos.R;
import com.hzsun.mpos.Sound.SoundPlay;

import java.util.ArrayList;

public class ProgressbarActivity extends BaseActivity {

    private ImageView ivLeft;
    private ImageView ivRight;
    private TextView tvNotice;
    private String TAG = getClass().getSimpleName();
    private ImageView ivCheckMenu;
    private TextView textTitle;
    private SeekBar seekbar;

    private int iType;  //类型
    private int iPosition; //选中的选项
    private String strTitle;    //标题
    private ArrayList<String> OptionItemList = new ArrayList<String>(); //选项内容
    private int image;
    private int progressValue = 0;

    private AudioManager mAudioManager;
    private int sMax, sVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progressbar);
        initData();
        initViews();
    }

    private void initData() {

        iType = getIntent().getIntExtra("Type", -1);
        iPosition = getIntent().getIntExtra("Position", -1);
        strTitle = getIntent().getStringExtra("strTitle");
        OptionItemList = getIntent().getStringArrayListExtra("OptionItemList");
        image = getIntent().getIntExtra("Image", -1);

        try {
            progressValue = Integer.parseInt(OptionItemList.get(0));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        ivLeft = ((ImageView) findViewById(R.id.iv_left));
        ivRight = ((ImageView) findViewById(R.id.iv_right));
        tvNotice = ((TextView) findViewById(R.id.tv_notice));
        ivCheckMenu = ((ImageView) findViewById(R.id.iv_check_menu));
        textTitle = ((TextView) findViewById(R.id.text_title));
        seekbar = ((SeekBar) findViewById(R.id.seekbar));
        textTitle.setText(strTitle);
        ivCheckMenu.setImageResource(image);

//        ivLeft.setImageResource(R.mipmap.s_light1);
//        ivRight.setImageResource(R.mipmap.s_light2);
//        tvNotice.setText(R.string.lcd_light_hint);
        ivLeft.setImageResource(R.mipmap.s_volume1);
        ivRight.setImageResource(R.mipmap.s_volume2);
        tvNotice.setText("使用上下键设置音量大小");

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        sVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekbar.setMax(sMax);
        seekbar.setProgress(sVolume);
        //播放声音
        startAlarm(getApplicationContext());
        SoundPlay.VoicePlay("welcome");
    }

    //播放系统声音
    private static void startAlarm(Context context) {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (notification == null)
            return;
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }

    //Activity返回参数
    private void PutResult(int iType, int iRet) {
        Intent intent = new Intent();
        Log.i(TAG, "iType:" + iType + " result:" + iRet);
        intent.putExtra("result", iRet);
        setResult(iType, intent);
        finish();//此处一定要调用finish()方法
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int current;
        int KeyValue;

//        sMax = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
//        sVolume = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );

        Log.i("AudioManager", "volume:" + sVolume);

        KeyValue = event.getKeyCode();
        //LogUtil.e(TAG, "KeyValue:" + KeyValue);
        if (KeyValue == KeyEvent.KEYCODE_FUNCTION ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_DOT ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_MULTIPLY ||
                KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD ||
                ((KeyValue >= KeyEvent.KEYCODE_NUMPAD_0) && (KeyValue <= KeyEvent.KEYCODE_NUMPAD_9))) {
            return true;
        }
        switch (KeyValue) {
            case KeyEvent.KEYCODE_DPAD_UP:
                //上键
                if (sVolume < sMax)
                    sVolume++;

                seekbar.setProgress(sVolume);
                mAudioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        sVolume,
                        //AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                        AudioManager.FLAG_PLAY_SOUND);

                //播放声音
                //startAlarm(getApplicationContext());
                SoundPlay.VoicePlay("welcome");
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                //下键
                if (sVolume > 0)
                    sVolume--;

                seekbar.setProgress(sVolume);
                mAudioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        sVolume,
                        AudioManager.FLAG_PLAY_SOUND);

                //播放声音
                //startAlarm(getApplicationContext());
                SoundPlay.VoicePlay("welcome");
                break;

            case KeyEvent.KEYCODE_BACK:
                PutResult(100, 0);//后退不处理返回值
                break;

            case KeyEvent.KEYCODE_ENTER:
                PutResult(iType, sVolume);//后退不处理返回值
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}

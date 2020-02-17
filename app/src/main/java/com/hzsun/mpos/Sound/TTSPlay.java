package com.hzsun.mpos.Sound;

import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

public class TTSPlay {

    private static final String TAG = "TTSPlay";
    // 语音合成对象
    private static SpeechSynthesizer mTts;


    //播放音频
    public static int TTSVoicePlay(String strSound, SynthesizerListener var2) {
        int code = 0;

//        Object object = new Object();
//        synchronized (object)
//        {
//            mTts = myApp.getmTts();
//
//            code= mTts.startSpeaking(strSound, null);
//            if (code != ErrorCode.SUCCESS) {
//                Log.i("语音合成失败,错误码: " ,""+ code);
//            }
//        }
        return code;
    }
}

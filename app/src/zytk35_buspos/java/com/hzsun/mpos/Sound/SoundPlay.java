package com.hzsun.mpos.Sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.hzsun.mpos.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_MapIDSound;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;

public class SoundPlay {

    private static final String TAG = "SoundPlay";
    private static String SoundPath = Environment.getExternalStorageDirectory().toString() + "/zytk/zytk35_buspos/sound/";
    // 上下文
    private static Context mContext;
    private static SoundPool soundPool;
    //创建一个集合存放音频数据
    private static HashMap<Integer, Integer> Soundmap = new HashMap<>();
    private static List<String> SoundNamelist = new ArrayList<String>();
    private static int sSoundCnt;
    private static int sSoundID;
    private static long slngStart;

    private static String sStrSoundTmp;

    public SoundPlay() {
        Log.i(TAG, "SoundPlay: 构造");
    }

//    public static void Init() {
//        Log.i(TAG, "SoundPlay 初始化");
//
//        File folder = new File(SoundPath);
//        if(!folder.exists()){
//            return;
//        }
//
//        String[] fileNames = folder.list(new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                if(name.endsWith(".wav")){
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        int i=0;
//        //创建音频对象
//        soundPool = new SoundPool(50, AudioManager.STREAM_MUSIC, 0);
//        for(String fileName : fileNames) {
//            //SoundPath + fileName
//            SoundNamelist.add(i,fileName);
//            String strSoundFileName=SoundPath + fileName;
//            //给集合设置数据
//            Soundmap.put(i++, soundPool.load(strSoundFileName, 100));
//        }
//    }
//
//    //播放音频
//    public static int VoicePlay(String strSound)
//    {
//        Object object = new Object();
//        int i=0;
//        int iSoundID=0;
//
//        synchronized (object){
//            //获取soundid
//            strSound=strSound+".wav";
//            for(i=0;i<SoundNamelist.size();i++)
//            {
//                if(strSound.equals(SoundNamelist.get(i)))
//                {
//                    iSoundID=i;
//                    break;
//                }
//            }
//            if(i>=SoundNamelist.size())
//            {
//                Log.i(TAG, "未找到关联的wav文件");
//                return 1;
//            }
//            soundPool.play(Soundmap.get(iSoundID), 1, 1, 100, 0, 1);
//            return 0;
//        }
//    }

    public static void Init(Context context) {
        Log.i(TAG, "SoundPlay 初始化");
        int rawId = 0;
        sSoundID = 0;
        slngStart = 0;
        sStrSoundTmp = "";
        mContext = context;

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        Field[] fields = R.raw.class.getDeclaredFields();
        sSoundCnt = fields.length;
        String rawName;
        for (int i = 0; i < fields.length; i++) {
            rawName = fields[i].getName();
            SoundNamelist.add(i, rawName);
            //rawName就是文件名称，如果想要id的话可以通过下面的代码拿到，希望被采纳~
            try {
                rawId = fields[i].getInt(R.raw.class);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            //给集合设置数据
            Soundmap.put(i, soundPool.load(mContext, rawId, 1));
        }
    }

    //播放音频
    public static synchronized void VoicePlay(String strSound) {
        int soundID = 0;

        if (g_WorkInfo.cTestState == 1)
            return;
        //Log.e(TAG,"播放的声音:"+strSound);
        //获取strSoundName对应的id
        for (int i = 0; i < sSoundCnt; i++) {
            if (strSound.equals(SoundNamelist.get(i))) {
                soundID = Soundmap.get(i);
                break;
            }
        }
        if (soundPool != null) {
            //判断同一个语音在同一时刻是否重复 (System.currentTimeMillis()-lngStart))
            if (soundID == sSoundID) {
                if ((System.currentTimeMillis() - slngStart) < 1000)
                    return;
            }
            soundPool.play(soundID, 1, 1, 0, 0, 1);
            sSoundID = soundID;
            slngStart = System.currentTimeMillis();
        }
    }

    //根据身份类别播放(语音)
    public static synchronized void VoicePerPlay(int cConsumerID) {
        if (g_WorkInfo.cTestState == 1)
            return;

        if (g_LocalInfo.cIDSoundFlag == 1) {
            String strSoundID = "";
            if (g_MapIDSound.size() != 0)
                strSoundID = g_MapIDSound.get(String.valueOf(cConsumerID));

            if (!TextUtils.isEmpty(strSoundID)) {
                VoicePlay(strSoundID);
                return;
            }
        }
        VoicePlay("zhifuchenggong");
    }


    //具体金额语音播放
    public static synchronized void VoiceMoneyPlay(long lngPaymentMoney, int cMode) {
        int i = 0;
        String cShowTemp = "";
        List<String> Soundlist = new ArrayList<String>();

        if (g_WorkInfo.cTestState == 1)
            return;

        if (cMode == 0)
            Soundlist.add("delmoney"); //消费
        else
            Soundlist.add("addmoney");    //充值

        cShowTemp = String.format("%d.%02d", lngPaymentMoney / 100, lngPaymentMoney % 100);
        String[] cTemp = cShowTemp.split("");
        for (i = 1; i < cTemp.length; i++) {
            if (cTemp[i].equals("."))
                Soundlist.add("point");
            else
                Soundlist.add("v" + cTemp[i]);
        }
        Soundlist.add("yuan");
        for (i = 0; i < Soundlist.size(); i++) {
            VoicePlay(Soundlist.get(i));
            try {
                if (i == 0)
                    Thread.sleep(1200);//延时
                else
                    Thread.sleep(400);//延时
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void VoicePlay(int type, Object obj) {
        SoundPlayThread thread = new SoundPlayThread(type, obj);
        thread.start();
    }


    public static class SoundPlayThread extends Thread {
        private String TAG = getClass().getSimpleName();
        private int sType;
        private Object obj;

        public SoundPlayThread(int iType, Object obj) {
            Log.i(TAG, "SoundPlayThread 构造");
            sType = iType;
            this.obj = obj;
        }

        public void StopThread() {
            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }

        @Override
        public void run() {
            if (sType == 0) {
                if (obj instanceof String) {
                    VoicePlay((String) obj);
                } else {
                    Log.i(TAG, "传入参数的类型不对");
                }
            } else {
                if (obj instanceof Long) {
                    VoiceMoneyPlay((Long) obj, 0);
                } else {
                    Log.i(TAG, "传入参数的类型不对");
                }
            }
        }
    }


}

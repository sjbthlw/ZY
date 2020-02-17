package com.hzsun.mpos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.huiyuenet.faceCheck.FaceCheck;
import com.hzsun.mpos.Activity.StartActivity;
import com.hzsun.mpos.CardWork.CardWorkTask;
import com.hzsun.mpos.FaceApp.FaceWorkTask;
import com.hzsun.mpos.Http.FaceCodeTask;
import com.hzsun.mpos.NetWork.NetWorkStateReceiver;
import com.hzsun.mpos.NetWork.NetWorkTask;
import com.hzsun.mpos.Public.CrashHandler;
import com.hzsun.mpos.Public.FileUtils;
import com.hzsun.mpos.Public.NetWorkCallback;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.QRCodeWork.QRCodeWorkTask;
import com.hzsun.mpos.SerialWork.SerialWorkTask;
import com.hzsun.mpos.thread.LoadFeatureThread;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.util.ResourceUtil;
import com.usface.activation.asynctask.IDoInBackground;
import com.usface.activation.asynctask.IPostExecute;
import com.usface.activation.asynctask.IPreExecute;
import com.usface.activation.asynctask.IPublishProgress;
import com.usface.activation.asynctask.MyAsyncTask;
import com.usface.activation.net.AuthAlgoRes;
import com.usface.activation.net.HttpUtil;

import java.io.File;

import static com.hzsun.mpos.Global.Global.ERROR_INVALID_COUNT;
import static com.hzsun.mpos.Global.Global.ERROR_INVALID_DATE;
import static com.hzsun.mpos.Global.Global.ERROR_INVALID_DEVICE;
import static com.hzsun.mpos.Global.Global.FACELIBCOPY;
import static com.hzsun.mpos.Global.Global.IAPAPPFILE_NAME;
import static com.hzsun.mpos.Global.Global.IAPPath;
import static com.hzsun.mpos.Global.Global.ShellPath;
import static com.hzsun.mpos.Global.Global.ZYTKFacePath;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.NetWork.NetWorkTask.RECONNECTCOUNT;
import static com.hzsun.mpos.Public.Publicfun.InstallApk;
import static com.hzsun.mpos.Public.Publicfun.ReadAssetsSofeVerInfoFile;
import static com.hzsun.mpos.Public.Publicfun.ReadIAPSofeVerInfoFile;
import static com.hzsun.mpos.Public.Publicfun.RunShellCmd;
import static com.hzsun.mpos.Public.Publicfun.SendPowSaveCmd;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.FAIL;
import static com.hzsun.mpos.Public.ToastUtils.SUCCESS;

public class MyApplication extends Application implements NetWorkCallback {

    private static final String TAG = "MyApplication";

    //WIFI对象
    public static WifiManager gWifiManager;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认本地发音人
    public static String voicerLocal = "xiaoyan";
    public static MyApplication myApp;

    private String rootDir = Environment.getExternalStorageDirectory().toString() + "/zytk/FaceResource/";
    private String readDir = rootDir + "readResource/";
    private String write = rootDir + "write/";

    private static final int FACE_INIT_TOAST = 0x02;
    private static final int FACE_INIT_ERROR = 0x03;
    private static final int FACE_EQUIPMENT_NOT_AUTHOR = 0x04;

    public static UIHandler uiHandler;
    private Activity activity;
    private NetWorkStateReceiver netWorkStateReceiver;
    public static NetWorkCallback netWorkCallback;
    private int AUTHORCnt = 0;

    //    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
//        @Override
//        public void uncaughtException(Thread t, Throwable e) {
//            Log.e(TAG, "========发生崩溃异常时,重启应用========");
//            Log.e(TAG, e.getMessage());
//            //restartApp(); //发生崩溃异常时,重启应用
//        }
//    };
    //程序异常重启
    private void restartApp() {
        Intent intent = new Intent(this, StartActivity.class);
        @SuppressLint("WrongConstant") PendingIntent restartIntent = PendingIntent.getActivity(
                myApp.getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
        //退出程序
        AlarmManager mgr = (AlarmManager) myApp.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                restartIntent); // 1秒钟后重启应用

        //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Log.e(TAG, "-------------人脸库特征加载完成-------------");
                    g_WorkInfo.cFaceInitState = 2;
                    ToastUtils.showText(MyApplication.this, "人脸库特征加载完成！", SUCCESS, BOTTOM, Toast.LENGTH_LONG);
                    break;
                case FACE_INIT_ERROR:
                    ToastUtils.showText(MyApplication.this, "" + msg.obj, 0, 4, Toast.LENGTH_SHORT);
                    break;
                case FACE_INIT_TOAST:
                    ToastUtils.showText(MyApplication.this, "" + msg.obj, 1, 4, Toast.LENGTH_SHORT);
                    break;
                case FACE_EQUIPMENT_NOT_AUTHOR:
                    Log.e(TAG, "-------------激活人脸库-------------");
                    //startAuth("ZhengYuanTest","test1106");
                    startAuth("AndroidZhengYuan", "ZhengYuan1015");
                    break;
                case 100:
                    Log.e(TAG, "-------------人脸库特征加载失败-------------");
                    ToastUtils.showText(MyApplication.this, "人脸库特征加载失败！", FAIL, BOTTOM, Toast.LENGTH_LONG);
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApp = this;
        // 程序崩溃时触发线程  以下用来捕获程序崩溃异常
        //Thread.setDefaultUncaughtExceptionHandler(handler);

        CrashHandler crashHandler = CrashHandler.getInstance();
        Intent intent = new Intent(this, StartActivity.class);
        crashHandler.init(getApplicationContext(), intent);

        uiHandler = new UIHandler();

        AUTHORCnt = 0;
        netWorkCallback = this;
        RegistReceiver();
        SendPowSaveCmd(2);//打开lcd
        Publicfun.InitWorkPara();
        Publicfun.RF_ChipInit();
        Publicfun.InitSysParam();
        Publicfun.InitHWDevice();//硬件驱动初始化
        CreateShellFile();
        CreateIAPVerFile();
        //获取WIFI对象
        gWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //科大讯飞合成语音初始化
        //InitTTSPara();
        //初始化人脸参数
        InitFacePara();

        Log.d(TAG, "开始读写卡线程:");
        CardWorkTask CardWork = new CardWorkTask();
        CardWork.Init();

        Log.d(TAG, "开始扫码线程:");
        QRCodeWorkTask QRCodeWork = new QRCodeWorkTask();
        QRCodeWork.Init();

        Log.d(TAG, "开始人脸检测线程:");
        FaceWorkTask FaceWork = new FaceWorkTask();
        FaceWork.Init();

        Log.d(TAG, "开始网络线程:");
        NetWorkTask NetWork = new NetWorkTask();
        NetWork.Init();

        Log.d(TAG, "开始HTTP线程:");
        FaceCodeTask FaceCode = new FaceCodeTask();
        FaceCode.Init();

        if (g_LocalInfo.cDockposFlag == 1) {
            Log.d(TAG, "开始串口线程:");
            SerialWorkTask serialWorkTask = new SerialWorkTask();
            serialWorkTask.Init();
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    //初始化人脸参数
    private void InitFacePara() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int iResult = -1;
                g_WorkInfo.cFaceInitState = 0;
                CreateFaceDir();
                try {
                    Thread.sleep(200);//延时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                iResult = FaceCheck.Init(readDir, write, MyApplication.this);
                Log.d(TAG, "初始化人脸参数：" + iResult);
                if (iResult >= 0) {
                    iResult = FaceCheck.InitFaceEngine(readDir, write);
                    Log.d(TAG, "初始化人脸比对引擎：" + iResult);
                    if (iResult >= 0) {
                        iResult = FaceCheck.allocateFeatureMemory(g_LocalInfo.iMaxFaceNum);
                        Log.d(TAG, "初始化特征内存空间：" + iResult);
                        if (iResult >= 0) {
                            Log.d(TAG, "人脸初始化成功,加载人脸数据===");
                            g_WorkInfo.cFaceInitState = 1;
                            if (g_SystemInfo.cFaceDetectFlag == 1) {
                                LoadFeatureThread ThreadLoadFeature = new LoadFeatureThread(uiHandler);
                                ThreadLoadFeature.start();
                            }
                            Log.d(TAG, "人脸初始化成功,加载人脸数据完成：" + iResult);
                        }
                    }
                } else if (iResult == ERROR_INVALID_DEVICE) {
                    Log.e(TAG, "初始化人脸参数失败：" + iResult);
                    AUTHORCnt++;
                    if (AUTHORCnt < 10)//激活10次 不在激活
                        uiHandler.sendEmptyMessage(FACE_EQUIPMENT_NOT_AUTHOR);
                } else if (iResult == ERROR_INVALID_DATE) {
                    Log.e(TAG, "算法过期：" + iResult);
                    AUTHORCnt++;
                    if (AUTHORCnt < 10)//激活10次 不在激活
                        uiHandler.sendEmptyMessage(FACE_EQUIPMENT_NOT_AUTHOR);
                } else if (iResult == ERROR_INVALID_COUNT) {
                    Log.e(TAG, "超出设备授权数量：" + iResult);
                    AUTHORCnt++;
                    if (AUTHORCnt < 10)//激活10次 不在激活
                        uiHandler.sendEmptyMessage(FACE_EQUIPMENT_NOT_AUTHOR);
                }
            }
        });
        t.start();
    }

    private void startAuth(final String account, final String password) {
        MyAsyncTask.<Void, Void, AuthAlgoRes>newBuilder()
                .setPreExecute(new IPreExecute() {
                    @Override
                    public void onPreExecute() {
                    }
                })
                .setDoInBackground(new IDoInBackground<Void, Void, AuthAlgoRes>() {
                    @Override
                    public AuthAlgoRes doInBackground(IPublishProgress<Void> publishProgress, Void... params) {
                        return HttpUtil.verifyAlgo(MyApplication.this, account, password);
                    }
                })
                .setPostExecute(new IPostExecute<AuthAlgoRes>() {
                    @Override
                    public void onPostExecute(AuthAlgoRes result) {
                        processAuthResult(result);
                    }
                })
                .start();
    }

    private void processAuthResult(AuthAlgoRes result) {
        Message msg = Message.obtain();
        if (result != null) {
            String code = result.code;
            if (code.equals("1000")) {
                msg.what = FACE_INIT_TOAST;
                msg.obj = "授权激活成功！";
                Log.e(TAG, "授权激活成功");
                InitFacePara();  // 激活成功后进行算法初始化操作
            } else {     // 激活失败，重新开启激活
                msg.obj = "授权激活失败，错误码：" + result.code + "，错误信息：" + result.msg;
                AUTHORCnt++;
                if (AUTHORCnt < 10)//激活10次 不在激活
                    uiHandler.sendEmptyMessageDelayed(FACE_EQUIPMENT_NOT_AUTHOR, 100);
            }
            uiHandler.sendMessageDelayed(msg, 0);
        }
    }

    //获取语音合成对象
    public SpeechSynthesizer getmTts() {
        return mTts;
    }

    //初始化语音
    private void InitTTSPara() {
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5c0e028d");
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //设置合成
        //设置使用本地引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicerLocal);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "55");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "100");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
    }

    //获取发音人资源路径
    private String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/" + MyApplication.voicerLocal + ".jet"));
        return tempBuffer.toString();
    }

    //创建人脸目录
    private void CreateFaceDir() {
        File file = new File(readDir);
        //创建资源文件目录
        if (!file.exists()) {
            file.mkdirs();
        }
        if (FACELIBCOPY == 1)
            FileUtils.MoveAllFaceAssetsFile(this, readDir);
        //创建可写目录
        file = new File(write);
        if (!file.exists()) {
            file.mkdirs();
        }
        //创建人脸特征目录
        file = new File(ZYTKFacePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    //创建shell文件
    private void CreateShellFile() {
        File file = new File(ShellPath);
        //创建资源文件目录
        if (!file.exists()) {
            file.mkdirs();
            FileUtils.MoveAssetsSHFile(this, ShellPath);
        } else
            return;
    }

     //创建shell文件
    private void CreateIAPVerFile() {
        int  iRet;
        File file = new File(IAPPath);
        //创建资源文件目录
        if (!file.exists()) {
            file.mkdirs();
            FileUtils.MoveIAPAssetsFile(this, IAPPath);
        }
        //判断引导程序版本号是否一致
        String strAssetsVer=ReadAssetsSofeVerInfoFile(this);
        if(strAssetsVer.equals("")){
            Log.d(TAG,"Assets无程序");
        }else{
            String strIapVer=ReadIAPSofeVerInfoFile();
            if(strIapVer.equals(strAssetsVer)){
                Log.d(TAG,"引导程序版本一致");
            }else{
                //判断是否存在apk
                File appfile = new File(IAPPath+IAPAPPFILE_NAME);
                if (!appfile.exists())
                    FileUtils.MoveIAPAssetsFile(this, IAPPath);

                Log.d(TAG,"升级引导程序:"+strAssetsVer+"--"+strIapVer);
                InstallApk(IAPPath + IAPAPPFILE_NAME, 1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RunShellCmd("reboot");
            }
        }
        return;
    }

    private void RegistReceiver() {
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, intentFilter);
    }

    @Override
    public void onStatus(int code) {
        //0:没有网络 1:wifi网络 9:以太网络
        if (code == 9) {
            Log.d(TAG, "网络当前为以太网络");
            g_WorkInfo.cNetlinkStatus = 1;
        } else if (code == 1) {
            Log.d(TAG, "网络当前为WIFI");
            g_WorkInfo.cNetlinkStatus = 2;
        } else {
            Log.e(TAG, "网络当前无");
            g_WorkInfo.cNetlinkStatus = 0;
        }
        //判断是否立即联机
        if (g_WorkInfo.cNetlinkStatus != 0) {
            g_WorkInfo.cRunState = 2; //重新连接统一调度服务
            g_WorkInfo.lngOffLineTime = g_WorkInfo.lngOSTime;
            g_WorkInfo.lngOnLineTime = g_WorkInfo.lngOSTime;
            g_WorkInfo.lngOSTime = g_WorkInfo.lngOnLineTime + RECONNECTCOUNT * 10;
            Log.d(TAG, "网络连接,重新连接统一调度服务");
        }
    }

}
package com.hzsun.mpos.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzsun.mpos.Public.LongClickUtils;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.progressutils.ProgressManage;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static com.hzsun.mpos.Global.Global.APPINSTALL;
import static com.hzsun.mpos.Global.Global.APPSTARTUP;
import static com.hzsun.mpos.Global.Global.APPUPABN;
import static com.hzsun.mpos.Global.Global.APPUPING;
import static com.hzsun.mpos.Global.Global.APPUPOVER;
import static com.hzsun.mpos.Global.Global.CONNECTOK;
import static com.hzsun.mpos.Global.Global.DOWNPARAOVER;
import static com.hzsun.mpos.Global.Global.MONEYDEVNO;
import static com.hzsun.mpos.Global.Global.ONLINEACK;
import static com.hzsun.mpos.Global.Global.POWERSAVEDEAL;
import static com.hzsun.mpos.Global.Global.ROMUPABN;
import static com.hzsun.mpos.Global.Global.ROMUPING;
import static com.hzsun.mpos.Global.Global.ROMUPOVER;
import static com.hzsun.mpos.Global.Global.SDPath;
import static com.hzsun.mpos.Global.Global.SETERR_MSG;
import static com.hzsun.mpos.Global.Global.ZYTK35Path;
import static com.hzsun.mpos.Global.Global.gUIMainHandler;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.GetAppFileName;
import static com.hzsun.mpos.Public.Publicfun.InstallApk;
import static com.hzsun.mpos.Public.Publicfun.ReadSofeVerInfoFile;
import static com.hzsun.mpos.Public.Publicfun.RunShellCmd;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.SUCCESS;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Handler sTimerHandler;//定时器
    private Timer timer;
    private ImageView iv_NetState;
    private ImageView iv_qrState;
    private TextView tv_Prompt, tv_Date, tv_Time1, tv_Time2, tv_Time3;
    private FrameLayout fl_wait;
    private ProgressManage progressManage;
    private int sTimeBeatState = 0;
    private long slngTimer;
    private int sQRDevStatus;
    private int sNetlinkStatus;
    private int sRunState;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "============开机进入主界面=============");
        // 初始化所有控件
        iv_NetState = (ImageView) findViewById(R.id.iv_netState);
        iv_qrState = (ImageView) findViewById(R.id.iv_qrState);
        tv_Prompt = (TextView) findViewById(R.id.prompt);
        tv_Date = (TextView) findViewById(R.id.tv_datetime);
        tv_Time1 = (TextView) findViewById(R.id.time1);
        tv_Time2 = (TextView) findViewById(R.id.time2);
        tv_Time3 = (TextView) findViewById(R.id.time3);
        fl_wait = (FrameLayout) findViewById(R.id.FL_wait);
        tv_Prompt.setText("数据加载中...");
        sQRDevStatus = 0;
        sRunState = -1;
        sNetlinkStatus = -1;
        slngTimer = 0;
        initListener();
        //初始化音频文件
        //TTSVoicePlay("欢迎使用正元智慧多媒体支付pos", null);
        //SoundPlay.VoicePlay("welcome");
        sTimerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 10:
                        TimeScanWork();
                        break;
                }
            }
        };
        TimerTask task = new TimerTask() {
            public void run() {
                //TimeScanWork();//定时器线程不能直接显示控件
                if (sTimerHandler != null)
                    sTimerHandler.sendEmptyMessage(10);
            }
        };
        timer = new Timer();
        timer.schedule(task, 500, 500);

        gUIMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTOK:
                        Log.d(TAG, "主界面ui--->连接服务成功:");
                        break;

                    case ONLINEACK:
                        Log.d(TAG, "主界面ui--->在线服务应答:");
                        break;

                    case DOWNPARAOVER:
                        Log.d(TAG, "主界面ui--->下载参数结束:");
                        tv_Prompt.setText("下载参数结束");
                        CheckGotoPayCardMenu();//判断是否进入刷卡交易界面
                        break;

                    case MONEYDEVNO:
                        Log.d(TAG, "主界面ui--->不支持充值机");
                        tv_Prompt.setText("不支持充值机");
                        break;

                    case APPSTARTUP:
                        Log.d(TAG, "主界面ui--->开始更新应用程序:");
                        tv_Prompt.setText("应用程序APP开始更新");
                        progressManage = new ProgressManage(MainActivity.this, tv_Prompt, getWindow());
                        progressManage.setMessage("应用程序APP更新中...");
                        progressManage.show();
                        break;

                    case APPUPING:
                        Log.d(TAG, "主界面ui--->更新应用程序中:");
                        int iRate = (int) msg.obj;
						if (progressManage != null) {
	                        progressManage.setProgress(iRate);
	                        progressManage.setMessage("应用程序APP更新中...");
						}
                        tv_Prompt.setText("应用程序APP更新中...");
                        break;

                    case ROMUPING:
                        Log.d(TAG, "主界面ui--->更新系统OTA中:");
                        int iRateA = (int) msg.obj;
                        if (progressManage != null){
                            progressManage.setProgress(iRateA);
                            progressManage.setMessage("系统OTA更新中...");
                            tv_Prompt.setText("系统OTA更新中...");
                        }
                        break;

                    case APPUPABN:
                        Log.d(TAG, "主界面ui--->更新应用程序异常:");
                        if (progressManage != null){
                            progressManage.dismiss();
                        }
                        tv_Prompt.setText("应用程序APP更新中异常");
                        g_WorkInfo.cUpdateState = 0;
                        break;

                    case ROMUPABN:
                        Log.d(TAG, "主界面ui--->更新系统OTA异常:");
                        if (progressManage != null){
                            progressManage.dismiss();
                        }
                        tv_Prompt.setText("更新系统OTA更新中异常");
                        g_WorkInfo.cUpdateState = 0;
                        //删除update_temp.zip包
                        RunShellCmd("rm -rf " + SDPath + "update_temp.zip");
                        break;

                    case APPUPOVER:
                        Log.d(TAG, "主界面ui--->应用程序APP更新完成:");
                        tv_Prompt.setText("应用程序APP更新完成");
                        if (progressManage != null){
                            progressManage.setMessage("应用程序APP更新完成");
                            progressManage.setProgress(100);
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    progressManage.dismiss();
                                }
                            }, 1000);
                        }
                        //获取升级app文件名
                        String strAppFileName = GetAppFileName();
                        Log.d(TAG, "升级程序：" + strAppFileName);
                        if (!strAppFileName.equals("")) {
                            int iRet = InstallApk(ZYTK35Path + strAppFileName, 0);
                            if (iRet != 0) {
                                Log.d(TAG, "应用程序安装失败");
                                tv_Prompt.setText("应用程序安装失败");
                                g_WorkInfo.cUpdateState = 0;
                            }
                        }
                        break;

                    case ROMUPOVER:
                        Log.d(TAG, "主界面ui--->系统ROM更新完成:");
                        tv_Prompt.setText("系统ROM更新完成");
                        if (progressManage != null){
                            progressManage.setMessage("系统ROM更新完成");
                            progressManage.setProgress(100);
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    progressManage.dismiss();
                                }
                            }, 2000);
                        }
                        //判断update.zip的包是否存在
                        File SystemUpdateOTA = new File(SDPath+"/update.zip");
                        if (SystemUpdateOTA.exists()) {
                            Log.d(TAG, "准备升级系统安装包,请勿断电");
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    tv_Prompt.setText("准备升级系统安装包,请勿断电");
                                }
                            }, 2000);
                            RunShellCmd("reboot");
                        }
                        break;

                    case APPINSTALL:
                        tv_Prompt.setText("应用程序安装中...");
                        ToastUtils.showText(MainActivity.this, "应用程序安装中！", SUCCESS, BOTTOM, Toast.LENGTH_LONG);
                        break;

                    case SETERR_MSG:
                        //显示故障信息
                        String strErrInfo = (String) msg.obj;
                        Log.e(TAG, "显示故障信息:" + strErrInfo);
                        tv_Prompt.setText(strErrInfo);
                        tv_Prompt.setTextColor(Color.RED);
                        break;

                    case POWERSAVEDEAL:
                        GotoStandbyActivity();
                        break;
                }
            }
        };
    }

    //定时器工作
    private void TimeScanWork() {
        slngTimer++;

        if (g_WorkInfo.cInMainMenuState == 1) {
            if (slngTimer % 10 == 0) {
                //ShowCPUTemp();
            }
            ShowDateTime();
            CheckGotoPayCardMenu();//判断是否进入刷卡交易界面

            if ((sNetlinkStatus != g_WorkInfo.cNetlinkStatus)
                    || (sRunState != g_WorkInfo.cRunState)) {
                Log.e(TAG, "网络状态发生变化:" + g_WorkInfo.cNetlinkStatus);
                sNetlinkStatus = g_WorkInfo.cNetlinkStatus;
                sRunState = g_WorkInfo.cRunState;
                ShowNetState(g_WorkInfo.cNetlinkStatus);
            }

            if (sQRDevStatus != g_WorkInfo.cQRDevStatus) {
                showQRScanState();
                sQRDevStatus = g_WorkInfo.cQRDevStatus;
            }
            if ((sNetlinkStatus != g_WorkInfo.cNetlinkStatus)
                    || (sRunState != g_WorkInfo.cRunState)) {
                ShowNetState(g_WorkInfo.cNetlinkStatus);
                sNetlinkStatus = g_WorkInfo.cNetlinkStatus;
                sRunState = g_WorkInfo.cRunState;
            }
        }
    }

    //显示时间
    private void ShowDateTime() {
        String strData;
        String strWeek;
        String strHour;
        String strMin;
        String strSec;

        String strTmp = Publicfun.getFullTime("yyyy-MM-dd HH:mm:ss E");
        strData = strTmp.substring(0, 10);
        strWeek = Publicfun.GetWeekName(strTmp);
        tv_Date.setText(strData + "        " + strWeek);
        if (sTimeBeatState == 0) {
            sTimeBeatState = 1;
            strHour = strTmp.substring(11, 13);
            strMin = strTmp.substring(14, 16);
            strSec = ":";
        } else {
            sTimeBeatState = 0;
            strHour = strTmp.substring(11, 13);
            strMin = strTmp.substring(14, 16);
            strSec = " ";
        }
        tv_Time1.setText(strHour);
        tv_Time2.setText(strSec);
        tv_Time3.setText(strMin);
    }

    //显示网络状态
    private void ShowNetState(int iNetState) {
        int iState = 0;
        if (iNetState == 1) {
            if (g_WorkInfo.cRunState == 1)
                iState = 2;//联网
            else
                iState = 1;//脱网
        } else if (iNetState == 2) {
            if (g_WorkInfo.cRunState == 1)
                iState = 4;//联网
            else
                iState = 3;//脱网
        } else {
            iState = 0;
        }

        switch (iState) {
            case 0: //没有网络
                iv_NetState.setImageResource(R.mipmap.s_net_null);
                break;
            case 1: //以太网络 网络故障 脱机
                iv_NetState.setImageResource(R.mipmap.s_net_etherr);
                break;
            case 2: //以太网络 联机运行
                iv_NetState.setImageResource(R.mipmap.s_net_eth);
                break;
            case 3: //wifi网络 网络故障 脱机
                iv_NetState.setImageResource(R.mipmap.s_net_wifierr);
                break;
            case 4: //wifi网络 联机运行
                iv_NetState.setImageResource(R.mipmap.s_net_wifi);
                break;
        }
    }

    private void showQRScanState() {
        switch (g_WorkInfo.cQRDevStatus) {
            case 0:
                iv_qrState.setVisibility(View.INVISIBLE);
                break;
            case 1:
                iv_qrState.setImageResource(R.mipmap.s_qr);
                iv_qrState.setVisibility(View.VISIBLE);
                break;
            case 2:
                iv_qrState.setImageResource(R.mipmap.s_qr_usb);
                iv_qrState.setVisibility(View.VISIBLE);
                break;
        }


    }

    //进入刷卡交易界面
    private void GotoPayCardMenu() {
        if (g_WorkInfo.cInMainMenuState == 1) {
            Log.d(TAG, "进入刷卡交易界面1");
            g_WorkInfo.cCardEnableFlag = 1;
            g_WorkInfo.cInMainMenuState = 0;
            if (g_LocalInfo.iStyle == 0) {
                startActivity(new Intent(this, CardCommonActivity.class));
            } else {
                startActivity(new Intent(this, CardActivity.class));
            }
            finish();
        }
    }

    //判断进入刷卡交易界面
    private void CheckGotoPayCardMenu() {
        int cResult = 0;

        if ((!g_WorkInfo.strAppFileName.equals("")) && (g_WorkInfo.cUpdateState == 0)) {
            Log.e(TAG, "升级apk名称:" + g_WorkInfo.strAppFileName);

            if (g_WorkInfo.strAppFileName.equals("zytk35_buspos.apk")) {
                Log.e(TAG, "删除U盘导入的apk文件:" + g_WorkInfo.strAppFileName);
                String strCmd = "rm -r " + ZYTK35Path + "*.apk";
                //String strCmd="pm install -r  /storage/emulated/0/zytk/zytk35_buspos.apk";
                Log.i(TAG, strCmd);
                RunShellCmd(strCmd);
                g_WorkInfo.strAppFileName = "";
            } else {
                //zytk35_buspos_3.5.19.0509.apk
                if (g_WorkInfo.strAppFileName.length() > 23) {
                    String strLocalAppVer = ReadSofeVerInfoFile().substring(0, 11);
                    String strTmpVer = g_WorkInfo.strAppFileName.substring(14, 25);
                    if (strTmpVer.equals(strLocalAppVer)) {
                        Log.e(TAG, "版本号一致，删除apk文件:" + strLocalAppVer);
                        //String strCmd="rm -r "+ZYTK35Path+g_WorkInfo.strAppFileName;
                        String strCmd = "rm -r " + ZYTK35Path + "*.apk";
                        //String strCmd="pm install -r  /storage/emulated/0/zytk/zytk35_buspos.apk";
                        Log.i(TAG, strCmd);
                        RunShellCmd(strCmd);
                        g_WorkInfo.strAppFileName = "";
                    } else {
                        Log.e(TAG, "版本号不一致，安装apk:" + strLocalAppVer);
                        Message msga = Message.obtain();
                        msga.what = APPINSTALL;
                        if (gUIMainHandler != null)
                            gUIMainHandler.sendMessage(msga);

                        String strAppFileName = ZYTK35Path + g_WorkInfo.strAppFileName;
                        if (!strAppFileName.equals(""))
                            InstallApk(strAppFileName, 0);
                        else
                            g_WorkInfo.strAppFileName = "";
                    }
                } else
                    g_WorkInfo.strAppFileName = "";
            }
            return;
        }

        //判断记录流水文件故障标记(流水记录业务部分)
        if (g_WorkInfo.cRecordErrFlag == 1) {
            Log.e(TAG, "记录流水文件故障,进入主界面");
            tv_Prompt.setTextColor(Color.RED);
            tv_Prompt.setText(("记录流水文件故障"));
            g_WorkInfo.cCardEnableFlag = 0;
            return;
        }
        //判断终端机号和客户号是否一致(网络业务部分)
        if (g_WorkInfo.cNetworkErrFlag != 0) {
            Log.e(TAG, "设备错误");
            g_WorkInfo.cCardEnableFlag = 0;
            return;
        }
        if ((g_WorkInfo.cUpdateState == 1) || (g_WorkInfo.cUpdateState == 2)) {
            tv_Prompt.setTextColor(Color.RED);
            tv_Prompt.setText(("程序升级中"));
            g_WorkInfo.cCardEnableFlag = 0;
            return;
        }
        if (g_BasicInfo.cSystemState != 100) {
            tv_Prompt.setTextColor(Color.RED);
            tv_Prompt.setText(("设备维护中"));
            g_WorkInfo.cCardEnableFlag = 0;
            return;
        } else {
            if (((g_WorkInfo.cRunState == 1) || (g_WorkInfo.cRunState == 2))) {
                Publicfun.CompareBusinessDate(g_WorkInfo.cCurDateTime);
                if (g_WorkInfo.cBusinessState == 0) {
                    if (g_WorkInfo.cRunState == 2) {
                        //判断脱机天数
                        cResult = Publicfun.CompareCanOffCount(g_WorkInfo.cCurDateTime);
                        if ((g_StationInfo.cCanOffPayment == 0) || (cResult != 1)) {
                            tv_Prompt.setTextColor(Color.RED);
                            tv_Prompt.setText(("不允许脱机"));
                            g_WorkInfo.cCardEnableFlag = 0;
                        } else {
                            if (g_WorkInfo.cUpdateState == 0) {
                                GotoPayCardMenu();
                            }
                        }
                    } else {
                        if (g_WorkInfo.cUpdateState == 0) {
                            GotoPayCardMenu();
                        }
                    }
                } else {
                    tv_Prompt.setTextColor(Color.RED);
                    tv_Prompt.setText(("停止营业"));
                    g_WorkInfo.cCardEnableFlag = 0;
                }
            } else {
                if (g_WorkInfo.cRunState == 5) {
                    if (g_BasicInfo.cSystemState != 100) {
                        tv_Prompt.setTextColor(Color.RED);
                        tv_Prompt.setText(("数据加载中..."));
                    } else {
                        if (g_WorkInfo.cBusinessState != 0) {
                            tv_Prompt.setTextColor(Color.RED);
                            tv_Prompt.setText(("停止营业..."));
                        } else {
                            tv_Prompt.setTextColor(Color.RED);
                            tv_Prompt.setText(("数据加载中..."));
                        }
                    }
                }
                g_WorkInfo.cCardEnableFlag = 0;
            }
        }
    }

    private void GotoFnMenu() {
        startActivity(new Intent(MainActivity.this, MenuActivity.class));
    }

    //进入休眠界面
    private void GotoStandbyActivity() {
        Log.i(TAG, "主界面进入休眠界面");
        g_WorkInfo.cBackActivityState = 1;
        startActivity(new Intent(this, StandbyActivity.class));
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //判断是否有焦点
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    //在onResume()方法注册
    @Override
    protected void onResume() {

        Log.e(TAG, "注册");
        g_WorkInfo.lngPowerSaveCnt = System.currentTimeMillis();
        g_WorkInfo.cInUserPWDFlag = 0;//取消用户密码键盘
        g_WorkInfo.cStartReWDialogFlag = 0;//取消重写卡
        g_WorkInfo.cInMainMenuState = 1;
        hideBottomUIMenu();

        CheckGotoPayCardMenu();//判断是否进入刷卡交易界面
        ShowDateTime();
        if (sQRDevStatus != g_WorkInfo.cQRDevStatus) {
            showQRScanState();
            sQRDevStatus = g_WorkInfo.cQRDevStatus;
        }
        if ((sNetlinkStatus != g_WorkInfo.cNetlinkStatus)
                || (sRunState != g_WorkInfo.cRunState)) {
            ShowNetState(g_WorkInfo.cNetlinkStatus);
            sNetlinkStatus = g_WorkInfo.cNetlinkStatus;
            sRunState = g_WorkInfo.cRunState;
        }
        super.onResume();
    }

    //方法注销
    @Override
    protected void onPause() {
        //unregisterReceiver(netWorkStateReceiver);
        Log.e(TAG, "注销");
        //g_WorkInfo.cInMainMenuState = 0;
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        Log.e(TAG, "销毁");
        g_WorkInfo.cInMainMenuState = 0;

        if (gUIMainHandler != null) {
            gUIMainHandler.removeCallbacksAndMessages(null);
            gUIMainHandler = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void initListener() {
        LongClickUtils.setLongClick(new Handler(), fl_wait, 5 * 1000, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //startVideoView(false);
                //g_Nlib.LED_Control(0);
                GotoFnMenu();
                return true;
            }
        });
    }
}

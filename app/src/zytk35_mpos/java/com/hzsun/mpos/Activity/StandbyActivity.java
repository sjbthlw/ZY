package com.hzsun.mpos.Activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.hzsun.mpos.R;

import java.util.Timer;
import java.util.TimerTask;

import static com.hzsun.mpos.Global.Global.g_ExtDisplay;
import static com.hzsun.mpos.Global.Global.g_UICardHandler;
import static com.hzsun.mpos.Global.Global.g_UIMainHandler;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Keyoperpro.DeleteKeyFun;
import static com.hzsun.mpos.Public.Publicfun.LCD7BacklightControl;
import static com.hzsun.mpos.Public.Publicfun.RunShellCmd;
import static com.hzsun.mpos.Public.Publicfun.getProp;

public class StandbyActivity extends AppCompatActivity {

    private static final String TAG = "StandbyActivity";
    private int s_ExitState = 0;
    private int s_TimerStart = 0;
    private Handler s_TimerHandler;//定时器
    private Timer timer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standby);
        s_ExitState = 0;
        s_TimerStart =0;
        s_TimerHandler = new Handler() {
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
                if (s_TimerHandler != null)
                    s_TimerHandler.sendEmptyMessage(10);
            }
        };
        timer = new Timer();
        timer.schedule(task, 2000, 1000);
    }

    //定时器工作
    private void TimeScanWork() {
        int iResult;
        byte[] bCardUID = new byte[8];
        byte[] bSAK = new byte[8];
        if (s_ExitState == 0) {
            s_TimerStart=1;
            //检测刷卡和QR模块
            iResult = g_Nlib.ReadCardAttrib(bCardUID, bSAK);
            if (iResult == 0) {
                ExitPowerSaveMenu();
                return;
            }
            //移动侦测状态查询
            iResult = g_Nlib.QR_GetDisSenseRet(5);
            if (iResult == 1) {
                ExitPowerSaveMenu();
                return;
            }
        }
    }

    //退出省电模式界面
    private void ExitPowerSaveMenu() {

        Log.e(TAG, "==========准备--退出省电模式界面===========");
        if((g_UICardHandler != null)&&(g_WorkInfo.cBackActivityState==2)){
            Log.e(TAG, "==========刷卡界面没有销毁，等待销毁===========");
            return;
        }
        if ((g_UIMainHandler != null) && (g_WorkInfo.cBackActivityState == 1)) {
            Log.e(TAG, "==========主界面没有销毁，等待销毁===========");
            return;
        }

        if(s_ExitState==0){//做下判断互斥(这里不做判断，会导致重入)
            s_ExitState=1;
            Log.e(TAG, "==========打开7寸屏幕显示===========");
            g_WorkInfo.lngPowerSaveCnt=System.currentTimeMillis();
            for(int i=0;i<3;i++){
                LCD7Control(1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //获取副屏信息
                DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
                Display[] presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
                Log.d(TAG, "获取副屏信息:" + presentationDisplays.length);
                if (presentationDisplays.length > 0) {
                    g_ExtDisplay = presentationDisplays[0];//第一个合适的
                    break;
                }
            }
            LCD7BacklightControl(1);
            if (g_WorkInfo.cBackActivityState == 2) {
                Log.e(TAG, "==========退出省电模式,进入刷卡界面===========");
                startActivity(new Intent(this, CardActivity.class));
            } else {
                Log.e(TAG, "==========退出省电模式,进入主界面===========");
                startActivity(new Intent(this, MainActivity.class));
            }
            g_WorkInfo.cBackActivityState = 0;
            finish();
        }
    }

    //发送shell命令一定要放在界面下
    private void SendShellCmd(String strShellcmd) {
        Log.e(TAG, "发送系统指令:" + strShellcmd);
        Intent intent1 = new Intent();
        intent1.setAction("ZYTK_BROADCAST");
        intent1.putExtra("Type", 1);
        intent1.putExtra("ShellCmd", strShellcmd);
        sendBroadcast(intent1);
    }

    //控制7寸屏幕开关
    private  void LCD7Control(int iState)
    {
        String strValue  = getProp("ro.product.zytkdevice");
        if(strValue.equals("rk3399_yt223_v2")){
            if (iState == 1) {
                RunShellCmd("echo on > /sys/devices/platform/display-subsystem/drm/card0/card0-HDMI-A-1/status");
            } else {
                RunShellCmd("echo off > /sys/devices/platform/display-subsystem/drm/card0/card0-HDMI-A-1/status");
            }
        }else {
            if (iState == 1) {
                SendShellCmd("echo on > /sys/devices/platform/display-subsystem/drm/card0/card0-HDMI-A-1/status");
            } else {
                SendShellCmd("echo off > /sys/devices/platform/display-subsystem/drm/card0/card0-HDMI-A-1/status");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "注册 进入省电模式");
        DeleteKeyFun();
        LCD7Control(0);
        LCD7BacklightControl(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        Log.e(TAG, "销毁 退出省电模式");
        g_Nlib.QR_ClearRecvData(5);
        g_WorkInfo.lngPowerSaveCnt = System.currentTimeMillis();
        if (s_TimerHandler != null) {
            s_TimerHandler.removeCallbacksAndMessages(null);
            s_TimerHandler = null;
        }
        if (timer != null)
            timer.cancel();

        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(s_TimerStart==1){
            Log.d(TAG,"触摸退出省电模式");
            ExitPowerSaveMenu();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if(s_TimerStart==1){
            Log.e(TAG,"按键退出省电模式");
            ExitPowerSaveMenu();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onBackPressed() {
        //不执行回退功能
        //一定要加入回退键不响应的处理，否则退到startactivity将不能打开大屏，使得调用大屏失败导致异常
//        long secondTime = System.currentTimeMillis();
//        if (secondTime - firstTime > 2000) {
//            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
//            firstTime = secondTime;
//        } else {
//            super.onBackPressed();
//        }
    }

}

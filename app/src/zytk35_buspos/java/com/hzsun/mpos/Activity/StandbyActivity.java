package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;

import com.huiyuenet.faceCheck.THFI_Param;
import com.hzsun.mpos.FaceApp.FaceWorkTask;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.R;
import com.hzsun.mpos.camera.CameraProxy;

import java.util.Timer;
import java.util.TimerTask;

import static com.hzsun.mpos.FaceApp.FaceWorkTask.StartDetecte;
import static com.hzsun.mpos.Global.Global.gUICardHandler;
import static com.hzsun.mpos.Global.Global.gUIMainHandler;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.SendPowSaveCmd;


public class StandbyActivity extends AppCompatActivity {

    private static final String TAG = "StandbyActivity";
    private int s_PowSaveState = 0;
    private int s_ExitState = 0;
    private long s_lngShowStandbyCnt;
    private Handler s_TimerHandler;//定时器
    private Timer timer;
    private CameraProxy mCameraProxy;
    private SurfaceTexture surfaceTexture = new SurfaceTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;
    private Bitmap gBitmap;
    private Matrix mMatrix;
    private long s_lngTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standby);
        s_lngTimer = 0;
        s_ExitState = 0;
        s_PowSaveState = 0;
        g_WorkInfo.cDetectFaceState = 0;
        s_lngShowStandbyCnt = System.currentTimeMillis();
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
        timer.schedule(task, 1000, 500);

        rs = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        mMatrix = new Matrix();
        mMatrix.postRotate(90);
        mMatrix.postScale(-1, 1);
    }

    //打开摄像头
    private void openCamera() {
        StartDetecte(true);//打开人脸获取
        mCameraProxy = new CameraProxy(this);
        mCameraProxy.openCamera();
        mCameraProxy.startPreview(surfaceTexture);
        mCameraProxy.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (yuvType == null) {
                    yuvType = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
                    in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

                    rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(THFI_Param.IMG_WIDTH).setY(THFI_Param.IMG_HEIGHT);
                    out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
                }
                in.copyFrom(data);
                yuvToRgbIntrinsic.setInput(in);
                yuvToRgbIntrinsic.forEach(out);

                gBitmap = Bitmap.createBitmap(THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, Bitmap.Config.ARGB_8888);
                out.copyTo(gBitmap);
                Bitmap bmp = Bitmap.createBitmap(gBitmap, 0, 0, THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, mMatrix, true);
                FaceWorkTask.DetectFaceThread.setBitmap(bmp);
            }
        });
    }

    //定时器工作
    private void TimeScanWork() {
        int iResult;
        byte[] bCardUID = new byte[8];
        byte[] bSAK = new byte[8];
        if (s_ExitState == 0) {
            //检测刷卡和QR模块
            iResult = g_Nlib.ReadCardAttrib(bCardUID, bSAK);
            if (iResult == 0) {
                Log.i(TAG, "检测刷卡和QR模块");
                ExitPowerSaveMenu();
                return;
            }
            //移动侦测状态查询
            iResult = g_Nlib.QR_GetDisSenseRet(5);
            if (iResult == 1) {
                Log.i(TAG, "移动侦测状态查询");
                ExitPowerSaveMenu();
                return;
            }
            //人脸检测到人脸
            if (g_WorkInfo.cDetectFaceState == 1) {
                Log.i(TAG, "人脸检测到人脸");
                ExitPowerSaveMenu();
                return;
            }
        }
        //由显示界面进入屏幕关闭
        if (s_PowSaveState == 0) {
            if ((System.currentTimeMillis() - s_lngShowStandbyCnt) > 10 * 1000) {
                s_lngShowStandbyCnt = System.currentTimeMillis();
                s_PowSaveState = 1;
                SendPowSaveCmd(1);
            }
        }
        s_lngTimer++;
        if (s_lngTimer % 10 == 0)
            Publicfun.RunShellCmdA("cat /sys/class/thermal/thermal_zone1/temp");
    }

    //退出省电模式界面
    private void ExitPowerSaveMenu() {

        Log.e(TAG, "==========准备--退出省电模式界面===========");
        if ((gUICardHandler != null) && (g_WorkInfo.cBackActivityState == 2)) {
            Log.e(TAG, "==========刷卡界面没有销毁，等待销毁===========");
            return;
        }
        if ((gUIMainHandler != null) && (g_WorkInfo.cBackActivityState == 1)) {
            Log.e(TAG, "==========主界面没有销毁，等待销毁===========");
            return;
        }

        if (s_ExitState == 0) {//做下判断互斥(这里不做判断，会导致重入)
            s_ExitState = 1;
            s_PowSaveState = 1;
            g_WorkInfo.lngPowerSaveCnt = System.currentTimeMillis();
            StartDetecte(false);
            Log.e(TAG, "==========退出省电模式界面===========");
            SendPowSaveCmd(2);
            if (g_WorkInfo.cBackActivityState == 2) {
                if (g_LocalInfo.iStyle == 0) {
                    startActivity(new Intent(this, CardCommonActivity.class));
                } else {
                    startActivity(new Intent(this, CardActivity.class));
                }
            } else if (g_WorkInfo.cBackActivityState == 1) {
                startActivity(new Intent(this, MainActivity.class));
            }
            g_WorkInfo.cBackActivityState = 0;
            finish();
        }
    }

    private void SendShellCmd(String strShellcmd) {
        Log.e(TAG, "发送系统指令:" + strShellcmd);
        Intent intent1 = new Intent();
        intent1.setAction("ZYTK_BROADCAST");
        intent1.putExtra("Type", 3);
        intent1.putExtra("ShellCmd", strShellcmd);
        sendBroadcast(intent1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "注册 进入省电模式");
        g_WorkInfo.cDetectFaceState = 0;
        Publicfun.LED_AllClear();
        if (g_LocalInfo.iFaceWakeupFlag == 1)
            openCamera();
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

        if (mCameraProxy != null) {
            mCameraProxy.releaseCamera();
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "点击退出省电模式");
                ExitPowerSaveMenu();
                break;
        }
        return super.onTouchEvent(event);
    }


}

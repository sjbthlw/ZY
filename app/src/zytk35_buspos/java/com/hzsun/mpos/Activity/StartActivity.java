package com.hzsun.mpos.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.hzsun.mpos.R;
import com.hzsun.mpos.Sound.SoundPlay;

import java.util.ArrayList;

import static com.hzsun.mpos.Global.Global.gUIStartHandler;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";

    public static final int FACE_UPDATE_MSG = 10;
    public static final int SET_TIME_MSG = 20;
    public static final int SET_USERCARD_MSG = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        requestPermission(
                new String[]{
                        "android.permission.INTERNET",
                        "android.permission.WRITE_EXTERNAL_STORAGE",
                        "android.permission.READ_EXTERNAL_STORAGE",
                        //"android.permission.EXTRA_PERMISSION_GRANTED",
                        "android.permission.CAMERA"
                }
        );
        gUIStartHandler = new StartHandler(this);

        //初始化音频文件
        SoundPlay.Init(this);
        //TTSVoicePlay("欢迎使用正元智慧多媒体支付pos", null);
        SetCameraID();

        if (g_BasicInfo.cSystemState == 80) {
            if (gUIStartHandler != null) {
                Message msg = Message.obtain();
                msg.what = SET_USERCARD_MSG;
                gUIStartHandler.sendMessage(msg);
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    GotoMainMenu();
                }
            }, 2000);

        }
    }

    private class StartHandler extends Handler {

        public StartHandler(StartActivity startActivity) {

        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_TIME_MSG:
                    //设置时钟
                    String strDateTime = (String) msg.obj;
                    Log.e(TAG, "发送系统日期时间:" + strDateTime);
                    Intent intent = new Intent();
                    intent.setAction("ZYTK_BROADCAST");
                    //strDateTime="180921103030";
                    intent.putExtra("Type", 0);
                    intent.putExtra("DateTime", strDateTime);
                    sendBroadcast(intent);
                    break;

                case SET_USERCARD_MSG:
                    //设置客户设密码
                    SetUserSecretCard(0);
                    break;
            }
        }
    }


    //进入刷卡交易界面
    private void GotoMainMenu() {
        Log.i(TAG, "------开机进入主界面------");
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //设置客户设密卡
    private void SetUserSecretCard(int iPosition) {
        int iType = 1;
        int image = R.mipmap.s_local_set;
        ArrayList<String> OptionItemList = new ArrayList<String>();
        String strTemp = "";
        String strTitle = "设置客户设密卡";

        //请刷客户设密卡
        OptionItemList.add("请刷客户设密卡!");

        startActivityForResult(new Intent(this, ScanCardActivity.class)

                .putExtra("Type", iType)
                .putExtra("Position", iPosition)
                .putExtra("strTitle", strTitle)
                .putStringArrayListExtra("OptionItemList", OptionItemList)
                .putExtra("Image", image), iType);

        return;
    }


    public void SetCameraID() {

        Camera camera = null;
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (Exception e) {
            Toast.makeText(StartActivity.this, "Camera ID 输入有误，请重新输入", Toast.LENGTH_LONG).show();
        } finally {
            if (camera != null) {
                camera.release();
            }
        }
    }

    private final int PERMISSION_REQUEST_CODE = 2000;

    @SuppressLint("WrongConstant")
    private void requestPermission(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int resultCode;
            for (String str : permissions) {
                resultCode = checkSelfPermission(str);
                if (resultCode != 0) {
                    if (resultCode == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                        break;
                    }
                }
            }
        }
    }

    // 方法，获取位置权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限通过
            } else {
                // 权限拒绝
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    // 禁止后不再询问了！
                } else {
                    // 用户此次选择了禁止权限
                }
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //在onResume()方法注册
    @Override
    protected void onResume() {
        Log.e(TAG, "注册");
        super.onResume();
    }

    //onPause()方法注销
    @Override
    protected void onPause() {
        Log.e(TAG, "注销");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "销毁");
        super.onDestroy();
    }

    /**
     * 为了得到传回的数据，必须在前面的Activity中（指MainActivity类）重写onActivityResult方法
     * <p>
     * requestCode 请求码，即调用startActivityForResult()传递过去的值
     * resultCode 结果码，结果码用于标识返回数据来自哪个新Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int iRet;

        switch (resultCode) {
            case 1:  //设置客户设密卡
                iRet = data.getExtras().getInt("result");//得到新Activity 关闭后返回的数据
                if (iRet == 1) {
                    GotoMainMenu();
                }
                break;

        }
    }

}
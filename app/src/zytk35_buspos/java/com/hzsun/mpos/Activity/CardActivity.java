package com.hzsun.mpos.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huiyuenet.faceCheck.THFI_Param;
import com.hzsun.mpos.Adapter.OnDoubleClickListener;
import com.hzsun.mpos.CardWork.CardBasicParaInfo;
import com.hzsun.mpos.FaceApp.FaceWorkTask;
import com.hzsun.mpos.Global.Global;
import com.hzsun.mpos.Public.CircleTransform;
import com.hzsun.mpos.Public.LongClickUtils;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.QRCodeWork.QRScanHelper;
import com.hzsun.mpos.R;
import com.hzsun.mpos.SerialWork.SerialWorkTask;
import com.hzsun.mpos.camera.CameraProxy;
import com.hzsun.mpos.camera.CameraTextureView;
import com.hzsun.mpos.camera.FaceOverlayView;
import com.hzsun.mpos.data.ShopQRCodeInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.hzsun.mpos.Global.Global.MAXBOOKSCOUNT;
import static com.hzsun.mpos.Global.Global.POWERSAVEDEAL;
import static com.hzsun.mpos.Global.Global.PhotoPath;
import static com.hzsun.mpos.Global.Global.SOFTWAREVER;
import static com.hzsun.mpos.Global.Global.gHttpCommInfo;
import static com.hzsun.mpos.Global.Global.gUICardHandler;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardBasicTmpInfo;
import static com.hzsun.mpos.Global.Global.g_CardInfo;
import static com.hzsun.mpos.Global.Global.g_FaceIdentInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WasteBookInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.ByteToString;
import static com.hzsun.mpos.Public.Publicfun.CheckDayAndBusinessTotal;
import static com.hzsun.mpos.Public.Publicfun.GetUserName;
import static com.hzsun.mpos.Public.Publicfun.RelayControl;
import static com.hzsun.mpos.Public.Publicfun.getProp;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.WARN;
import static com.hzsun.mpos.Sound.SoundPlay.VoicePlay;

public class CardActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CardActivity";

    private StringBuilder strBuilder = new StringBuilder();
    private QRScanHelper hQRScanhelper;
    private Handler sTimerHandler;//定时器
    private Timer timer;
    public static final int CARD_NULL = 0;
    public static final int CARD_INMONEY = 1;
    public static final int CARD_READOK = 2;
    public static final int CARD_PAYOK = 3;    //支付成功
    public static final int CARD_PAYALLOK = 4; //支付成功(工作钱包-追扣钱包)
    public static final int CARD_PWDKBOPEN = 5; //密码键盘打开
    public static final int CARD_PWDKBCLOSE = 6;//密码键盘关闭
    public static final int CARD_SHOWTOTAL = 7; //显示全部金额字段
    public static final int CARD_RWSTART = 8;   //卡片重读卡开始
    public static final int CARD_RWEND = 9;     //卡片重读卡结束
    public static final int QRCODE_SHOW = 10;
    public static final int CARD_PAYING = 11;
    public static final int ONLING_PAYOK = 12;    //在线支付成功

    public static final int CARD_ERR = 50;     //卡片错误
    public static final int CARDPAY_ERR = 100; //刷卡支付
    public static final int FACEPAY_ERR = 200; //刷脸支付

    public static final int HTTPPHOTO = 20;

    private static final int WHAT_DRAW = 0x01;
    private static final int QRCODE_RECV = 0x02;

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    private CameraTextureView cameraPreview;
    private FaceOverlayView faceOverlayView;
    private int inversibleTimes = 0;

    private CameraProxy mCameraProxy;
    private Bitmap gBitmap;
    private Matrix mMatrix;

    private RelativeLayout RL_Statusbar, RL_loading;
    private LinearLayout LL_Payfail, LL_Paysuccess;
    private FrameLayout FL_Payways, FL_Facepay;
    private ImageView iv_Userpic, iv_QrState, iv_NetState, iv_Facepay;
    private TextView tv_ShoperName, tv_Prompt, tv_EPrompt, tv_FPrompt, tv_Name, tv_Paymoney, tv_FPaymoney, tv_SPaymoney, tv_Bursemoney, tv_Datetime, tv_Othermoney;
    private TextView tv_CpuT, tv_version;
    private FrameLayout fl_container;
    private TextView tv_paytip;

    private int sFaceInitState;
    private int sRunState;
    private int sQRDevStatus;
    private int sNetlinkStatus;
    private int sBookMoney;
    private short sBookSureMode;
    private short sTestState;
    private long slngTimer;
    private byte sTypeTemp;
    private byte sSelfPressOk;
    private long slngPaymentMoney;        //输入的交易金额整行(最大60000)
    private long slngAccountID;
    private int sType;

    private char code;
    private String result;
    private boolean isGoFn;
    private boolean isChangeStyle;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_DRAW:
                    DrawImage();
                    break;

                case QRCODE_RECV:
                    String strQRCode = (String) msg.obj;
                    Log.e(TAG, " 接收到的QRCode:" + strQRCode);
                    int len = strQRCode.length();
                    hQRScanhelper.setResult(strQRCode);
                    strBuilder.delete(0, len);
                    result = null;
                    break;
            }
        }
    };

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        Log.i(TAG, "=========开始刷卡界面========");
        Global.STYLE = g_LocalInfo.iStyle;
        hQRScanhelper = new QRScanHelper();
        RL_Statusbar = (RelativeLayout) findViewById(R.id.rl_statusbar);
        LL_Payfail = (LinearLayout) findViewById(R.id.ll_payfail);
        LL_Paysuccess = (LinearLayout) findViewById(R.id.ll_paysuccess);
        FL_Payways = (FrameLayout) findViewById(R.id.fl_payways);
        FL_Facepay = (FrameLayout) findViewById(R.id.fl_facepay);
        RL_loading = (RelativeLayout) findViewById(R.id.RL_loading);

        iv_NetState = (ImageView) findViewById(R.id.iv_netState);
        iv_QrState = (ImageView) findViewById(R.id.iv_qrState);
        iv_Facepay = (ImageView) findViewById(R.id.iv_facepay);
        tv_Datetime = ((TextView) findViewById(R.id.tv_datetime));
        //tv_ShoperName = ((TextView) findViewById(R.id.tv_shoperName));

        tv_FPaymoney = (TextView) findViewById(R.id.tv_paymoney1);
        tv_Paymoney = (TextView) findViewById(R.id.tv_paymoney);
        tv_Prompt = (TextView) findViewById(R.id.tv_prompt);
        tv_EPrompt = (TextView) findViewById(R.id.tv_eprompt);
        tv_FPrompt = (TextView) findViewById(R.id.tv_fprompt);
        tv_Name = (TextView) findViewById(R.id.tv_sname);
        tv_SPaymoney = (TextView) findViewById(R.id.tv_spaymoney);
        tv_Bursemoney = (TextView) findViewById(R.id.tv_sbursemoney);
        tv_Othermoney = (TextView) findViewById(R.id.tv_sothermoney);
        iv_Userpic = (ImageView) findViewById(R.id.iv_suserpic);
        tv_CpuT = (TextView) findViewById(R.id.tv_cput);
        tv_version = (TextView) findViewById(R.id.tv_version);
        fl_container = (FrameLayout) findViewById(R.id.fl_container);
        iv_Facepay.setOnClickListener(this);

        cameraPreview = ((CameraTextureView) findViewById(R.id.preview));
        faceOverlayView = ((FaceOverlayView) findViewById(R.id.faceOverlayView));

        tv_paytip = (TextView) findViewById(R.id.tv_paytip);

        mCameraProxy = cameraPreview.getCameraProxy();
        cameraPreview.setPreviewCallback(mCamePreviewCallback);
        cameraPreview.setOnTouchListener(new OnDoubleClickListener(() -> ShowFacePay(false)));

        mMatrix = new Matrix();
        mMatrix.postRotate(90);
        mMatrix.postScale(-1, 1);

        rs = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        tv_EPrompt.setText("");
        tv_Name.setText("");
        tv_Othermoney.setText("0.00");
        tv_Paymoney.setText("0.00");
        tv_SPaymoney.setText("0.00");
        tv_Bursemoney.setText("0.00");

        sFaceInitState = 0;
        sQRDevStatus = 0;
        sRunState = -1;
        sNetlinkStatus = -1;
        sBookMoney = 0;
        sBookSureMode = 0;
        sTestState = 0;
        slngTimer = 0;
        sTypeTemp = 0;
        sSelfPressOk = 0;
        slngPaymentMoney = 0;
        initListener();
        ShowCPUTemp();
        ShowSystemVer();
        gUICardHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case CARD_NULL:
                    case CARD_INMONEY:
                    case CARD_READOK:
                    case CARD_PAYOK:
                    case CARD_PAYALLOK:
                    case ONLING_PAYOK:
                    case CARD_SHOWTOTAL:
                        CardBasicParaInfo tCardBasicInfo = (CardBasicParaInfo) msg.obj;
                        ShowCardInfo(tCardBasicInfo, (byte) msg.what);
                        break;

                    case CARD_PAYING:
                        ShowCardPaying("正在支付中...");
                        if (g_WorkInfo.cOtherQRFlag == 2 && RL_loading.getVisibility() != View.VISIBLE) {
                            //人脸支付
                            RL_loading.setVisibility(View.VISIBLE);
                        }
                        break;

                    case QRCODE_SHOW:
                        ShopQRCodeInfo tShopQRCodeInfo = (ShopQRCodeInfo) msg.obj;
                        break;

                    case CARD_ERR:  //刷卡校验
                        List<String> Strlist = new ArrayList<String>();
                        Strlist = (List<String>) msg.obj;
                        String strTemp = Strlist.get(0);
                        int iErrorCode = Integer.parseInt(strTemp);
                        String strErrorCode = Strlist.get(1);
                        Log.e(TAG, "失败:" + strErrorCode);
                        ShowCardErrInfo(strErrorCode, (byte) msg.what);
                        break;

                    case CARDPAY_ERR:   //支付交易
                        List<String> StrlistA = new ArrayList<String>();
                        StrlistA = (List<String>) msg.obj;
                        String strTempA = StrlistA.get(0);
                        int iErrorCodeA = Integer.parseInt(strTempA);
                        String strErrorCodeA = StrlistA.get(1);
                        Log.e(TAG, "失败:" + strErrorCodeA);
                        ShowCardErrInfoA(strErrorCodeA, (byte) msg.what);
                        break;

                    case FACEPAY_ERR:   //刷脸支付交易失败
                        List<String> StrlistB = new ArrayList<String>();
                        StrlistB = (List<String>) msg.obj;
                        String strTempB = StrlistB.get(0);
                        int iErrorCodeB = Integer.parseInt(strTempB);
                        String strErrorCodeB = StrlistB.get(1);
                        Log.e(TAG, "失败:" + strErrorCodeB);
                        ShowFaceErrInfo(strErrorCodeB, (byte) 1);
                        break;

                    case CARD_RWSTART:
                        tv_Prompt.setText("等待卡片重刷");
                        tv_Prompt.setTextColor(Color.RED);
                        VoicePlay("card_reswipe");
                        break;

                    case CARD_RWEND:
                        Log.e(TAG, "等待卡片重刷结束");
                        break;

                    case HTTPPHOTO:
                        String strAccnum = (String) msg.obj;
                        long lngAccountID = Long.parseLong(strAccnum);
                        ShowHttpUserPhoto(lngAccountID);
                        break;

                    case POWERSAVEDEAL:
                        GotoStandbyActivity();
                        break;
                }
            }
        };


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
                if ((g_WorkInfo.cCardEnableFlag == 1) && (sTimerHandler != null))
                    sTimerHandler.sendEmptyMessage(10);
            }
        };
        timer = new Timer();
        timer.schedule(task, 1000, 1000);
    }

    //进入休眠界面
    private void GotoStandbyActivity() {
        Log.i(TAG, "刷卡界面进入休眠界面");
        g_WorkInfo.cBackActivityState = 2;
        startActivity(new Intent(this, StandbyActivity.class));
        finish();
    }


    //定时器工作
    private void TimeScanWork() {
        slngTimer++;
        if (g_WorkInfo.cCardEnableFlag == 1) {

            if (slngTimer % 30 == 0) {
                ShowCPUTemp();
                ShowDateTime();
                ShowShoperName();
            }
            if (sQRDevStatus != g_WorkInfo.cQRDevStatus) {
                showQRScanState();
                sQRDevStatus = g_WorkInfo.cQRDevStatus;
            }
            if ((sNetlinkStatus != g_WorkInfo.cNetlinkStatus)
                    || (sRunState != g_WorkInfo.cRunState)) {
                Log.e(TAG, "网络状态发生变化:" + g_WorkInfo.cNetlinkStatus);
                ShowNetState(g_WorkInfo.cNetlinkStatus);
                sNetlinkStatus = g_WorkInfo.cNetlinkStatus;
                sRunState = g_WorkInfo.cRunState;
            }
            if ((g_SystemInfo.cFaceDetectFlag == 1)//启用人脸
                    && (g_LocalInfo.cFaceModeFlag==1)
                    && (g_WorkInfo.cRunState == 1)
                    && ((g_WorkInfo.cSelfPressOk == 1) || ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0)))
                    && (g_CardInfo.cExistState != 1)) {
                if (sFaceInitState != g_WorkInfo.cFaceInitState) {
                    sFaceInitState = g_WorkInfo.cFaceInitState;
                    if ((g_WorkInfo.cFaceInitState == 2) && (g_WorkInfo.cTestState == 0)) {
                        FaceWorkTask.StartDetecte(true);
                    }
                }
            }
            //判断是否切换了交易模式
            if ((sBookMoney != g_WorkInfo.wBookMoney)
                    || (sBookSureMode != g_LocalInfo.cBookSureMode)
                    || (sTestState != g_WorkInfo.cTestState)) {
                //Log.e(TAG,"交易模式和金额发生变化:"+g_WorkInfo.wBookMoney);
                g_WorkInfo.cQrCodestatus = 0;
                g_WorkInfo.cPayDisPlayStatus = 0;
                sBookMoney = g_WorkInfo.wBookMoney;
                sBookSureMode = g_LocalInfo.cBookSureMode;
                sTestState = g_WorkInfo.cTestState;

                if ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0)) {
                    //设置自动感应模式
                    g_Nlib.QR_SetDeviceReadMode(4);
                } else {
                    g_Nlib.QR_SetDeviceReadMode(2);//识度模式 $100002-C9FF 开关持续 2
                    g_Nlib.QR_SetDeviceReadEnable(2);//命令触发 //$108003-F8E3 结束识读 2
                }
                if (g_LocalInfo.cInputMode == 3) {
                    g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
                    String cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
                    tv_Paymoney.setText(cShowTemp);
                }
            }
        }
        CheckExitPayCardMenu();
    }

    //进入主界面
    private void GotoPayMainMenu() {
        if (g_WorkInfo.cCardEnableFlag == 1) {
            Log.i(TAG, "进入主界面");
            g_WorkInfo.cCardEnableFlag = 0;
            g_Nlib.LedShow((char) 1, 0);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    //判断是否退出刷卡交易界面
    private void CheckExitPayCardMenu() {
        int cResult;
        //判断记录流水文件故障标记(流水记录业务部分)
        if ((g_WorkInfo.cRecordErrFlag == 1)
                || (g_WorkInfo.cNetworkErrFlag == 1)
                || (g_BasicInfo.cSystemState != 100)) {
            if (g_WorkInfo.cRecordErrFlag == 1)
                Log.e(TAG, "记录流水文件故障,进入主界面");
            if (g_WorkInfo.cNetworkErrFlag == 1)
                Log.e(TAG, "终端机号或客户号不一致,进入主界面");
            if (g_BasicInfo.cSystemState != 100)
                Log.e(TAG, "参数不完整，退出卡片交易界面,进入主界面");

            GotoPayMainMenu();
            return;
        }

        if (((g_WorkInfo.cRunState == 1) || (g_WorkInfo.cRunState == 2))) {
            if (g_WorkInfo.cBusinessState != 0) {
                Log.d(TAG, "=============不在营业分组时段================");
                GotoPayMainMenu();
                return;
            }
            if (g_WorkInfo.cRunState == 2) {
                cResult = Publicfun.CompareCanOffCount(g_WorkInfo.cCurDateTime);//判断脱机天数
                if ((g_StationInfo.cCanOffPayment == 0) || (cResult != 1)) {
                    Log.d(TAG, "=============不允许脱机================");
                    GotoPayMainMenu();
                    return;
                }
            }
        }
        return;
    }

    //显示网络状态
    private void ShowNetState(int cNetlinkStatus) {
        int iState = 0;
        //物理网线状态(1:有线eth0,2:无线wifi wlan0,0:无)
        if (cNetlinkStatus == 1) {
            if (g_WorkInfo.cRunState == 1)
                iState = 2;//联网
            else
                iState = 1;//脱网
        } else if (cNetlinkStatus == 2) {
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

    //显示QR状态
    private void showQRScanState() {
        switch (g_WorkInfo.cQRDevStatus) {
            case 0:
                iv_QrState.setVisibility(View.INVISIBLE);
                break;
            case 1:
                iv_QrState.setImageResource(R.mipmap.s_qr);
                iv_QrState.setVisibility(View.VISIBLE);
                break;
            case 2:
                iv_QrState.setImageResource(R.mipmap.s_qr_usb);
                iv_QrState.setVisibility(View.VISIBLE);
                break;
        }
    }

    //显示时间
    private void ShowDateTime() {
        String strTmp = Publicfun.getFullTime("yyyy-MM-dd HH:mm:ss E");
        String strWeek = Publicfun.GetWeekName(strTmp);
        String strDataTime = strTmp.substring(5, 10) + " " + strTmp.substring(11, 16) + " " + strWeek;
        tv_Datetime.setText(strDataTime);
    }

    //显示温度
    private void ShowCPUTemp() {
        String cmdShell;
        cmdShell = Publicfun.RunShellCmdA("cat /sys/class/thermal/thermal_zone1/temp");
        tv_CpuT.setText("CPU: " + cmdShell);
    }

    //显示商户
    private void ShowShoperName() {
//        String strShowTemp="";
//        tv_ShoperName.setText(strShowTemp);
    }

    //显示系统版本号和固件版本
    private void ShowSystemVer() {
        String value  = getProp("ro.product.zytkdevice");
        tv_version.setText(value+"-"+SOFTWAREVER);
    }

    //显示照片
    public void ShowHttpUserPhoto(long lngAccountID) {
        int iResult = 0;

        //判断photo是否有照片
        String FilePath = PhotoPath + String.format("%d.jpg", lngAccountID);
        iResult = Publicfun.FileIsExists(FilePath);
        if (iResult == 0) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(FilePath);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                if (bitmap == null)
                    return;
                CircleTransform Circle = new CircleTransform();
                Bitmap bitmapA = Circle.transform(bitmap);
                iv_Userpic.setImageBitmap(bitmapA);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
        }
    }

    //显示照片
    public void ShowUserPhoto(long lngAccountID, int iType) {

        int iResult = 0;

        if ((iType == sType) && (slngAccountID == lngAccountID))
            return;

        slngAccountID = lngAccountID;
        sType = iType;

        if (iType == 0)    //待机状态
        {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
        } else if (iType == 1)//输入状态
        {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
        } else if (iType == 2)   //读卡成功状态
        {
            //判断photo是否有照片
            g_WorkInfo.strAccCode=""+lngAccountID;
            String FilePath = PhotoPath + String.format("%d.jpg", lngAccountID);
            iResult = Publicfun.FileIsExists(FilePath);
            if (iResult == 0) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(FilePath);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    if (bitmap == null)
                        return;
                    CircleTransform Circle = new CircleTransform();
                    Bitmap bitmapA = Circle.transform(bitmap);
                    iv_Userpic.setImageBitmap(bitmapA);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //0x00000008 //获取用户照片数据地址
                gHttpCommInfo.lngSendHttpStatus |= 0x00000008;
            } else {
                iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
                //0x00000008 //获取用户照片数据地址
                gHttpCommInfo.lngSendHttpStatus |= 0x00000008;
            }
        } else if ((iType == 3) || (iType == 4))  //支付成功状态
        {
            //判断photo是否有照片
            g_WorkInfo.strAccCode=""+lngAccountID;
            String FilePath = PhotoPath + String.format("%d.jpg", lngAccountID);
            iResult = Publicfun.FileIsExists(FilePath);
            if (iResult == 0) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(FilePath);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    if (bitmap == null)
                        return;
                    CircleTransform Circle = new CircleTransform();
                    Bitmap bitmapA = Circle.transform(bitmap);
                    iv_Userpic.setImageBitmap(bitmapA);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //0x00000008 //获取用户照片数据地址
                gHttpCommInfo.lngSendHttpStatus |= 0x00000008;
            } else {
                iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
                //0x00000008 //获取用户照片数据地址
                gHttpCommInfo.lngSendHttpStatus |= 0x00000008;
            }
        }

    }

    //显示刷卡信息 ucType: 0 清除卡片信息 1 显示卡片界面 2 卡片刷卡失败
    private void ShowCardInfo(CardBasicParaInfo pCardBasicInfo, byte ucType) {
        long lngTemp;
        String cShowTemp = "";

        //卡片信息赋值
        if ((ucType == sTypeTemp)
                && (sSelfPressOk == g_WorkInfo.cSelfPressOk)
                && (slngPaymentMoney == g_WorkInfo.lngPaymentMoney)
                && (pCardBasicInfo.lngAccountID == g_CardBasicTmpInfo.lngAccountID)
                && (pCardBasicInfo.lngCardID == g_CardBasicTmpInfo.lngCardID)) {
            //Log.e(TAG,"---------卡信息相同，退出---------");
            return;
        }

        g_CardInfo.ucType = (byte) ucType;
        sTypeTemp = ucType;
        slngPaymentMoney = g_WorkInfo.lngPaymentMoney;
        sSelfPressOk = g_WorkInfo.cSelfPressOk;
        g_CardBasicTmpInfo = new CardBasicParaInfo();
        try {
            g_CardBasicTmpInfo = (CardBasicParaInfo) pCardBasicInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if (sTypeTemp == CARD_NULL) {
            FL_Payways.setVisibility(View.VISIBLE);
            LL_Payfail.setVisibility(View.INVISIBLE);
            LL_Paysuccess.setVisibility(View.INVISIBLE);
            tv_Prompt.setText("");
            tv_paytip.setVisibility(View.GONE);
            tv_paytip.setText("");
            if (g_LocalInfo.cInputMode != 3) {
                tv_Paymoney.setText("0.00");
                tv_FPaymoney.setText("0.00");
            }
        } else if (sTypeTemp == CARD_INMONEY) {
            FL_Payways.setVisibility(View.VISIBLE);
            LL_Payfail.setVisibility(View.INVISIBLE);
            LL_Paysuccess.setVisibility(View.INVISIBLE);

            if (g_LocalInfo.cInputMode == 3) {
                Log.i(TAG, "固定定额模式");
                g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
                Log.i(TAG, String.format("定额:%d", g_WorkInfo.wBookMoney));
            }
            cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
            tv_Paymoney.setText(cShowTemp);
            tv_FPaymoney.setText(cShowTemp);
            tv_Prompt.setText("");
            if ((g_SystemInfo.cFaceDetectFlag == 1)
                    && (g_WorkInfo.cRunState == 1)
                    && (g_LocalInfo.cFaceModeFlag == 1)
                    && (g_CardInfo.cExistState != 1)) {
                ShowFacePay(true);
            }
        } else if ((sTypeTemp == CARD_PAYOK)||(sTypeTemp ==ONLING_PAYOK)) {
            g_WorkInfo.lngAccSameCnt = System.currentTimeMillis();
            g_WorkInfo.lngAccountID = pCardBasicInfo.lngAccountID;//记录卡账号
            ShowFacePay(false);
            FL_Payways.setVisibility(View.INVISIBLE);
            LL_Payfail.setVisibility(View.INVISIBLE);
            LL_Paysuccess.setVisibility(View.VISIBLE);
            if ((g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex >= MAXBOOKSCOUNT - 200) &&
                    g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex < MAXBOOKSCOUNT - 20) {
                tv_paytip.setText("脱机交易流水快满了,请尽快上传");
                tv_paytip.setVisibility(View.VISIBLE);
            } else {
                tv_paytip.setText("");
                tv_paytip.setVisibility(View.GONE);
            }
            if (RL_loading.getVisibility() == View.VISIBLE) {
                //人脸支付
                RL_loading.setVisibility(View.GONE);
            }
            //卡片信息
            byte[] accName = GetUserName(g_CardBasicTmpInfo.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Name.setText(cShowTemp);
            //显示照片
            ShowUserPhoto(g_CardBasicTmpInfo.lngAccountID, sTypeTemp);
            //支付金额
            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngPayMoney / 100, g_CardBasicTmpInfo.lngPayMoney % 100);
            tv_SPaymoney.setText(cShowTemp);
            //钱包余额
            if (g_CardBasicTmpInfo.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngWorkBurseMoney / 100, g_CardBasicTmpInfo.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfo.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }
            cShowTemp = "";
            if (g_CardBasicTmpInfo.lngPriMoney != 0)//优惠金额
                cShowTemp += String.format("优惠(元) %d.%02d", g_CardBasicTmpInfo.lngPriMoney / 100, g_CardBasicTmpInfo.lngPriMoney % 100);
            if (g_CardBasicTmpInfo.lngManageMoney != 0)//管理费
                cShowTemp += String.format("管理费(元) %d.%02d", g_CardBasicTmpInfo.lngManageMoney / 100, g_CardBasicTmpInfo.lngManageMoney % 100);
            if (g_CardBasicTmpInfo.lngSubMoney != 0)//补助金额
            {
                if (g_CardBasicTmpInfo.lngSubMoney >= 0) {
                    cShowTemp += String.format("补助(元) %d.%02d", g_CardBasicTmpInfo.lngSubMoney / 100, g_CardBasicTmpInfo.lngSubMoney % 100);
                } else {
                    lngTemp = -g_CardBasicTmpInfo.lngSubMoney;
                    cShowTemp += String.format("补助(元) -%d.%02d", lngTemp / 100, lngTemp % 100);
                }
            }
            tv_Othermoney.setText(cShowTemp);
            RelayControl();//继电器动作
        } else if (ucType == CARD_PAYALLOK) {

            g_WorkInfo.lngAccSameCnt = System.currentTimeMillis();
            g_WorkInfo.lngAccountID = pCardBasicInfo.lngAccountID;//记录卡账号
            ShowFacePay(false);
            FL_Payways.setVisibility(View.INVISIBLE);
            LL_Payfail.setVisibility(View.INVISIBLE);
            LL_Paysuccess.setVisibility(View.VISIBLE);
            if ((g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex >= MAXBOOKSCOUNT - 200) &&
                    g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex < MAXBOOKSCOUNT - 20) {
                tv_paytip.setText("脱机交易流水快满了,请尽快上传");
                tv_paytip.setVisibility(View.VISIBLE);
            } else {
                tv_paytip.setText("");
                tv_paytip.setVisibility(View.GONE);
            }
            //卡片信息
            byte[] accName = GetUserName(g_CardBasicTmpInfo.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Name.setText(cShowTemp);
            //显示照片
            ShowUserPhoto(g_CardBasicTmpInfo.lngAccountID, sTypeTemp);
            //支付金额
            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngPayMoney / 100, g_CardBasicTmpInfo.lngPayMoney % 100);
            tv_SPaymoney.setText(cShowTemp);
            //钱包余额
            if ((g_CardBasicTmpInfo.lngWorkBurseMoney + g_CardBasicTmpInfo.lngChaseBurseMoney) >= 0) {
                cShowTemp = String.format("%d.%02d", (g_CardBasicTmpInfo.lngWorkBurseMoney + g_CardBasicTmpInfo.lngChaseBurseMoney) / 100,
                        (g_CardBasicTmpInfo.lngWorkBurseMoney + g_CardBasicTmpInfo.lngChaseBurseMoney) % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -(g_CardBasicTmpInfo.lngWorkBurseMoney + g_CardBasicTmpInfo.lngChaseBurseMoney);
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }
            cShowTemp = "";
            if (g_CardBasicTmpInfo.lngPriMoney != 0)//优惠金额
                cShowTemp += String.format("优惠(元) %d.%02d", g_CardBasicTmpInfo.lngPriMoney / 100, g_CardBasicTmpInfo.lngPriMoney % 100);
            if (g_CardBasicTmpInfo.lngManageMoney != 0)//管理费
                cShowTemp += String.format("管理费(元) %d.%02d", g_CardBasicTmpInfo.lngManageMoney / 100, g_CardBasicTmpInfo.lngManageMoney % 100);
            if (g_CardBasicTmpInfo.lngSubMoney != 0)//补助金额
            {
                if (g_CardBasicTmpInfo.lngSubMoney >= 0) {
                    cShowTemp += String.format("补助(元) %d.%02d", g_CardBasicTmpInfo.lngSubMoney / 100, g_CardBasicTmpInfo.lngSubMoney % 100);
                } else {
                    lngTemp = -g_CardBasicTmpInfo.lngSubMoney;
                    cShowTemp += String.format("补助(元) -%d.%02d", lngTemp / 100, lngTemp % 100);
                }
            }
            tv_Othermoney.setText(cShowTemp);
            RelayControl();//继电器动作
        }
    }

    //显示错误信息
    public void ShowCardErrInfo(String strErrorInfo, byte ucType) {
        ShowFacePay(false);
        FL_Payways.setVisibility(View.VISIBLE);
        LL_Payfail.setVisibility(View.INVISIBLE);
        LL_Paysuccess.setVisibility(View.INVISIBLE);

        sTypeTemp = ucType;
        tv_Prompt.setTextColor(Color.RED);
        tv_Prompt.setText(strErrorInfo);
    }

    //显示错误信息
    public void ShowCardErrInfoA(String strErrorInfo, byte ucType) {
        ShowFacePay(false);
        FL_Payways.setVisibility(View.INVISIBLE);
        LL_Payfail.setVisibility(View.VISIBLE);
        LL_Paysuccess.setVisibility(View.INVISIBLE);

        sTypeTemp = ucType;
        tv_EPrompt.setTextColor(Color.RED);
        tv_EPrompt.setText(strErrorInfo);
    }

    //显示人脸错误信息
    public void ShowFaceErrInfo(String strErrorInfo, byte ucType) {
        if (g_WorkInfo.cBackActivityState != 0) {
            return;
        }
        FL_Facepay.setVisibility(View.VISIBLE);
        if ((ucType == 1) && (!strErrorInfo.equals(""))) {
            tv_FPrompt.setVisibility(View.VISIBLE);
            tv_FPrompt.setTextColor(Color.RED);
            tv_FPrompt.setText(strErrorInfo);
        } else {
            tv_FPrompt.setVisibility(View.GONE);
        }

        if (RL_loading.getVisibility() == View.VISIBLE) {
            RL_loading.setVisibility(View.GONE);
        }
    }

    //显示正在支付中
    public void ShowCardPaying(String strInfo) {
        tv_Prompt.setTextColor(Color.RED);
        tv_Prompt.setText(strInfo);
    }

    //显示人脸支付界面
    public void ShowFacePay(boolean bStatus) {
        if (RL_loading.getVisibility() == View.VISIBLE) {
            RL_loading.setVisibility(View.GONE);
        }
        if (bStatus) {
            g_Nlib.LedShow((char) 1, 1);
            FaceWorkTask.StartDetecte(true);
            FL_Facepay.setVisibility(View.VISIBLE);
            RL_Statusbar.setVisibility(View.GONE);
            FL_Payways.setVisibility(View.GONE);
            LL_Payfail.setVisibility(View.GONE);
            LL_Paysuccess.setVisibility(View.GONE);

            if (g_LocalInfo.cInputMode == 3) {
                Log.i(TAG, "自动定额模式");
                g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
            }
            String cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
            tv_FPaymoney.setText(cShowTemp);

        } else {
            g_Nlib.LedShow((char) 1, 0);
            FaceWorkTask.StartDetecte(false);
            FL_Facepay.setVisibility(View.GONE);
            RL_Statusbar.setVisibility(View.VISIBLE);
            FL_Payways.setVisibility(View.VISIBLE);
            LL_Payfail.setVisibility(View.GONE);
            LL_Paysuccess.setVisibility(View.GONE);
        }
    }

    private void GotoFnMenu() {
        g_Nlib.LedShow((char) 1, 0);
        startActivity(new Intent(CardActivity.this, MenuActivity.class));
        isGoFn = true;
        //finish();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_facepay:
                if (g_SystemInfo.cFaceDetectFlag == 1)//启用人脸
                {
                    if ((g_WorkInfo.cFaceInitState == 2)
                            && (g_WorkInfo.cRunState == 1)
                            && (g_CardInfo.cExistState != 1)
                            && (g_WorkInfo.cTestState == 0) && (g_LocalInfo.cDockposFlag != 1 || (g_LocalInfo.cDockposFlag == 1 && (SerialWorkTask.getTradeState() == SerialWorkTask.STATE_PAYING || SerialWorkTask.getTradeState() == SerialWorkTask.STATE_QUERYING)))) {
                        Log.i(TAG, "处理人脸流程");
                        ShowFacePay(true);
                    } else {
                        if ((g_WorkInfo.cFaceInitState != 2) && (g_FaceIdentInfo.iListNum != 0)) {
                            ToastUtils.showText(this, "特征码数据加载中，不启动人脸!", WARN, BOTTOM, Toast.LENGTH_LONG);//特征码数据加载中不支持扫脸
                        } else if (g_WorkInfo.cRunState == 2) {
                            ToastUtils.showText(this, "通讯故障,请检查网络!", WARN, BOTTOM, Toast.LENGTH_LONG);
                        } else {
                            ToastUtils.showText(this, "不启动人脸!", WARN, BOTTOM, Toast.LENGTH_LONG);
                        }
                    }
                } else {
                    ToastUtils.showText(this, "不启动人脸!", WARN, BOTTOM, Toast.LENGTH_LONG);
                }
                break;
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        hQRScanhelper.checkLetterStatus(event);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (hQRScanhelper.isScannerInput(this, event)) {
                code = hQRScanhelper.getInputCode(event);
                if (event.getKeyCode() == KeyEvent.KEYCODE_SHIFT_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_SHIFT_RIGHT) {
                    return false;
                }
                strBuilder.append(code);
                result = strBuilder.toString().trim();
                if (!TextUtils.isEmpty(result)) {
                    Message message = handler.obtainMessage();
                    message.what = QRCODE_RECV;
                    message.obj = result;
                    handler.removeMessages(QRCODE_RECV);
                    handler.sendMessageDelayed(message, 60);
                }
            }
        }
        return super.dispatchKeyEvent(event);
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
        LongClickUtils.setLongClick(new Handler(), fl_container, 5 * 1000, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //startVideoView(false);
                //g_Nlib.LED_Control(0);
                GotoFnMenu();
                return true;
            }
        });
    }

    /**
     * Android Camera类的回调
     */
    private Camera.PreviewCallback mCamePreviewCallback = new Camera.PreviewCallback() {
        private int frameIndex = 0;

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
            if (gBitmap != null) {
                handler.sendEmptyMessage(WHAT_DRAW);
            }
        }
    };


    //相机预览画面
    private void DrawImage() {
        Bitmap bmp = Bitmap.createBitmap(gBitmap, 0, 0, THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, mMatrix, true);
        FaceWorkTask.DetectFaceThread.setBitmap(bmp);
        Rect[] rects = FaceWorkTask.DetectFaceThread.getFaceRect();
        if (rects != null) {
            inversibleTimes = 0;
            faceOverlayView.setVisibility(View.VISIBLE);
            faceOverlayView.update(rects);
        } else {
            inversibleTimes++;
            if (inversibleTimes > 5) {
                faceOverlayView.setVisibility(View.INVISIBLE);
            }
        }
    }


    @Override
    protected void onResume() {
        Log.i(TAG, "注册");
        g_WorkInfo.lngPowerSaveCnt = System.currentTimeMillis();
        isGoFn = false;
        g_WorkInfo.cCardEnableFlag = 1;
        faceOverlayView.setPreviewSize(THFI_Param.IMG_HEIGHT, THFI_Param.IMG_WIDTH);
        hideBottomUIMenu();
        CheckDayAndBusinessTotal();
        ShowCPUTemp();
        ShowDateTime();
        if ((g_SystemInfo.cFaceDetectFlag == 1)
                && (g_WorkInfo.cRunState == 1)
                && (g_LocalInfo.cFaceModeFlag == 1)
                && (g_CardInfo.cExistState != 1)) {
            ShowFacePay(true);
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
        if (Global.STYLE != g_LocalInfo.iStyle) {
            isChangeStyle = true;
            g_WorkInfo.cCardEnableFlag = 1;
            g_WorkInfo.cInMainMenuState = 0;
            Global.STYLE = g_LocalInfo.iStyle;
            if (g_LocalInfo.iStyle == 0) {
                startActivity(new Intent(this, CardCommonActivity.class));
            } else {
                startActivity(new Intent(this, CardActivity.class));
            }
            finish();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {

        Log.i(TAG, "注销");
        //g_WorkInfo.cCardEnableFlag=0;
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {

        Log.i(TAG, "销毁");
        if (!isChangeStyle) {
            g_WorkInfo.cCardEnableFlag = 0;
            if (gUICardHandler != null) {
                gUICardHandler.removeCallbacksAndMessages(null);
                gUICardHandler = null;
            }
            if (timer != null)
                timer.cancel();

            if (gBitmap != null) {
                gBitmap.recycle();
                gBitmap = null;
            }
        }
        super.onDestroy();
    }

}

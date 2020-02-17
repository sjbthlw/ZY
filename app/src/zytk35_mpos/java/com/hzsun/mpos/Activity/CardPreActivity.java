package com.hzsun.mpos.Activity;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huiyuenet.faceCheck.THFI_Param;
import com.hzsun.mpos.CardWork.CardBasicParaInfo;
import com.hzsun.mpos.FaceApp.CameraWrapper;
import com.hzsun.mpos.FaceApp.FaceWorkTask;
import com.hzsun.mpos.Public.CircleTransform;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.ShopQRCodeInfo;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import static com.hzsun.mpos.Activity.CardActivity.CARD_INMONEY;
import static com.hzsun.mpos.Activity.CardActivity.CARD_NULL;
import static com.hzsun.mpos.Activity.CardActivity.CARD_PAYALLOK;
import static com.hzsun.mpos.Activity.CardActivity.CARD_PAYOK;
import static com.hzsun.mpos.Activity.CardActivity.CARD_READOK;
import static com.hzsun.mpos.Activity.CardActivity.ONLING_PAYOK;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_INMONEY;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_NULL;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_ONLING;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_PAYOK;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_PAYOKONLING;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_READOK;
import static com.hzsun.mpos.Global.Global.CAMERA_NUM;
import static com.hzsun.mpos.Global.Global.PhotoPath;
import static com.hzsun.mpos.Global.Global.SOFTWAREVER;
import static com.hzsun.mpos.Global.Global.g_CardBasicTmpInfoA;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.ByteToString;
import static com.hzsun.mpos.Public.Publicfun.GetUserName;
import static com.hzsun.mpos.Public.Publicfun.getProp;

public class CardPreActivity extends Presentation {

    private static final String TAG = "CardPreActivity";
    private static final int WHAT_DRAW = 0x01;
    private static final int WHAT_DRAW_INFR = 0x02;
    public TextView tv_faceNotice;
    private LinearLayout ll_keyboard;
    public Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btnCancle, btn0, btnConfirm;
    public EditText et_Password;
    private Activity s_Activity;
    private FrameLayout FL_ad, FL_face, FL_payok;
    private RelativeLayout RL_userInfo, RL_payState, RL_Faceclick, Rl_Qrcode, Rl_Infrface;
    private TextView tv_CpuTemp;
    private TextView tv_version;

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    private SurfaceView mSurfaceView, mSurfaceViewInfr;
    private SurfaceHolder mSurfaceHolder, mSurfaceHolderInfr;
    private CameraWrapper mCamera, mCameraInfr;
    private Matrix mMatrix, mMatrixInfr;

    public ImageView iv_Userpic, iv_NetState, iv_Qrcode;
    private TextView tv_Datetime;
    private TextView tv_Payprompt;
    public TextView tv_ShoperName;
    public TextView tv_Prompt, tv_Code;
    public TextView tv_Paymoney;
    private TextView tv_Primoney, tv_Managemoney, tv_Subsidymoney, tv_Bursemoney;

    private boolean isShowCameraPre = false;
    private byte sTypeTemp;
    private byte sSelfPressOk;
    private long slngPaymentMoney;        //输入的交易金额整行(最大60000)
    private long slngOrderNum;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_DRAW:
                    DrawImage((Bitmap) msg.obj);
                    break;
                case WHAT_DRAW_INFR:
                    DrawImageInfr((Bitmap) msg.obj);
                    break;
            }
        }
    };

    public CardPreActivity(Activity activity, Context outerContext, Display display) {
        super(outerContext, display);
        s_Activity = activity;
        setContentView(R.layout.activity_card_pre);
        iv_NetState = ((ImageView) findViewById(R.id.iv_netStatePer));
        FL_face = (FrameLayout) findViewById(R.id.fl_face);
        FL_ad = (FrameLayout) findViewById(R.id.fl_ad);
        FL_payok = (FrameLayout) findViewById(R.id.fl_payok);
        RL_payState = (RelativeLayout) findViewById(R.id.rl_payState);
        RL_Faceclick = (RelativeLayout) findViewById(R.id.rl_faceclick);
        Rl_Qrcode = (RelativeLayout) findViewById(R.id.rl_qrcode);
        Rl_Infrface = (RelativeLayout) findViewById(R.id.rl_Infrface);
        tv_faceNotice = ((TextView) findViewById(R.id.tv_faceNotice));

        iv_Userpic = (ImageView) findViewById(R.id.iv_userpic);
        iv_Qrcode = (ImageView) findViewById(R.id.iv_qrcode);

        tv_Datetime = (TextView) findViewById(R.id.tv_datetime);
        tv_Payprompt = (TextView) findViewById(R.id.tv_payprompt);
        tv_ShoperName = (TextView) findViewById(R.id.tv_shoperName);

        tv_Prompt = (TextView) findViewById(R.id.tv_prompt);
        tv_Paymoney = (TextView) findViewById(R.id.tv_paymoney);
        tv_Primoney = (TextView) findViewById(R.id.tv_primoney);
        tv_Managemoney = (TextView) findViewById(R.id.tv_managemoney);
        tv_Subsidymoney = (TextView) findViewById(R.id.tv_subsidymoney);
        tv_Bursemoney = (TextView) findViewById(R.id.tv_bursemoney);

        //密码键盘
        ll_keyboard = ((LinearLayout) findViewById(R.id.ll_keyboard));
        et_Password = ((EditText) findViewById(R.id.et_password));
        btn1 = ((Button) findViewById(R.id.btn1));
        btn2 = ((Button) findViewById(R.id.btn2));
        btn3 = ((Button) findViewById(R.id.btn3));
        btn4 = ((Button) findViewById(R.id.btn4));
        btn5 = ((Button) findViewById(R.id.btn5));
        btn6 = ((Button) findViewById(R.id.btn6));
        btn7 = ((Button) findViewById(R.id.btn7));
        btn8 = ((Button) findViewById(R.id.btn8));
        btn9 = ((Button) findViewById(R.id.btn9));
        btn0 = ((Button) findViewById(R.id.btn0));
        btnCancle = ((Button) findViewById(R.id.btnCanale));
        btnConfirm = ((Button) findViewById(R.id.btnConfirm));
        tv_CpuTemp =findViewById(R.id.tv_cput);
        tv_version=findViewById(R.id.tv_version);

        initBGRCameraParams();
        initINFRCameraParams();

        rs = RenderScript.create(outerContext);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        sTypeTemp = 0;
        sSelfPressOk = 0;
        slngPaymentMoney = 0;

        if (g_LocalInfo.iDisplayInfr == 0)
            Rl_Infrface.setVisibility(View.GONE);
        else
            Rl_Infrface.setVisibility(View.VISIBLE);
        ShowSystemVer();
    }

    //打开摄像头
    public void openAllCamera() {
        //打开摄像头计时300s后自动关闭摄像头(保护摄像头不被损坏);
        if (!mCamera.isPreviewing) {
            Log.d(TAG,"打开RGB摄像头");
            boolean isOpened = mCamera.openCamera(s_Activity, Camera.CameraInfo.CAMERA_FACING_BACK);
            if (isOpened){
                mCamera.startPreview();
            }
        }
        if (CAMERA_NUM == 2) {
            if (!mCameraInfr.isPreviewing) {
                Log.d(TAG,"打开红外摄像头");
                boolean isOpened = mCameraInfr.openCamera(s_Activity, Camera.CameraInfo.CAMERA_FACING_FRONT);
                if (isOpened){
                    mCameraInfr.startPreview();
                }
            }
        }
    }

    //关闭摄像头(RGB和红外)
    public void closeAllCamera() {
        if (mCamera != null) {
            if (mCamera.isPreviewing){
                Log.d(TAG,"关闭RGB摄像头");
                mCamera.closeCamera();
            }
        }
        if (CAMERA_NUM == 2) {
            if (mCameraInfr != null) {
                if (mCameraInfr.isPreviewing){
                    Log.d(TAG,"关闭红外摄像头");
                    mCameraInfr.closeCamera();
                }
            }
        }
        //关闭摄像头后清除残影
        Message msg = Message.obtain();
        msg.obj = null;
        msg.what = WHAT_DRAW;
        handler.sendMessage(msg);
    }

    private void initBGRCameraParams() {
        mSurfaceView = findViewById(R.id.sv_face);
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(480, 560);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(THFI_Param.IMG_HEIGHT, THFI_Param.IMG_HEIGHT);
        mCamera = new CameraWrapper(s_Activity, mCamePreviewCallback);
        params.gravity = Gravity.CENTER;
        mSurfaceView.setLayoutParams(params);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(surfaceCallback);

        mMatrix = new Matrix();
        mMatrix.postRotate(270);
        mMatrix.postScale(-1, 1);
    }

    private void initINFRCameraParams() {
        if (CAMERA_NUM == 2) {
            mCameraInfr = new CameraWrapper(s_Activity, mCamePreviewCallbackInfr);
            mSurfaceViewInfr = ((SurfaceView) findViewById(R.id.previewInfr));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(160, 213);
            mSurfaceViewInfr.setLayoutParams(params);
            mSurfaceHolderInfr = mSurfaceViewInfr.getHolder();

            mMatrixInfr = new Matrix();
            mMatrixInfr.postRotate(90);
        }
    }

    //显示时间日期
    public void ShowDateTime(String strDateTime) {
        tv_Datetime.setText(strDateTime);
    }

    //显示温度
    public void ShowCPUTemp(String strTemp) {
        tv_CpuTemp.setText(strTemp);
    }

    //显示系统版本号和固件版本
    private void ShowSystemVer() {
        String value  = getProp("ro.product.zytkdevice");
        tv_version.setText(value+"-"+SOFTWAREVER);
    }
    //显示商户
    public void ShowShoperName(String strShowTemp) {
        tv_ShoperName.setText(strShowTemp);
    }

    //显示网络状态
    public void ShowNetState(int cNetlinkStatus) {
        int iState = 0;
        if (cNetlinkStatus == 1) {
            if (g_WorkInfo.cRunState == 1)
                iState = 2;//联网
            else
                iState = 1;//脱网
        } else if (cNetlinkStatus == 2) {
            if (g_WorkInfo.cRunState == 1)
                iState = 4;//联网
            else
                iState = 5;//脱网
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

    //显示人脸界面
    public void ShowFaceDetecte() {
        FL_payok.setVisibility(View.INVISIBLE);
        FL_ad.setVisibility(View.INVISIBLE);
        RL_Faceclick.setVisibility(View.INVISIBLE);
        FL_face.setVisibility(View.VISIBLE);
        isShowCameraPre = true;
    }

    //显示人脸界面
    public void NoShowFaceDetecte() {
        FL_face.setVisibility(View.INVISIBLE);
        isShowCameraPre = false;
    }

    //显示商户二维码界面
    public void ShowQRCode() {
        FL_payok.setVisibility(View.INVISIBLE);
        FL_ad.setVisibility(View.VISIBLE);
        RL_Faceclick.setVisibility(View.INVISIBLE);
        Rl_Qrcode.setVisibility(View.VISIBLE);
    }

    //显示照片
    public void ShowUserPhoto(long lngAccountID, int iType) {

        int iResult = 0;
        if (iType == 0)    //待机状态
        {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(s_Activity.getApplicationContext(), R.mipmap.b_head_bg));
        } else if (iType == 1)   //读卡成功状态
        {
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
                    iv_Userpic.setImageBitmap(Circle.transform(bitmap));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ShowUserPhoto Exception:" + e.getMessage());
                }
            } else {
                iv_Userpic.setImageDrawable(ContextCompat.getDrawable(s_Activity.getApplicationContext(), R.mipmap.b_head_bg));
            }
        } else if (iType == 2)   //交易成功状态
        {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(s_Activity.getApplicationContext(), R.mipmap.b_pay_success));
        } else if (iType == 3)   //交易失败状态
        {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(s_Activity.getApplicationContext(), R.mipmap.b_fail));
        }
    }

    //显示刷卡信息 ucType: 0 清除卡片信息 1 显示交易金额 2 卡片刷卡成功 3 卡片交易成功 5 显示卡片错误信息
    public void ShowCardInfo(CardBasicParaInfo pCardBasicInfo, byte ucType) {
        long lngTemp;
        String cShowTemp = "";

        if ((ucType == sTypeTemp)
                && (sSelfPressOk == g_WorkInfo.cSelfPressOk)
                && (slngPaymentMoney == g_WorkInfo.lngPaymentMoney)
                && (pCardBasicInfo.lngAccountID == g_CardBasicTmpInfoA.lngAccountID)
                && (pCardBasicInfo.lngCardID == g_CardBasicTmpInfoA.lngCardID)
                && (pCardBasicInfo.lngSubMoney == g_CardBasicTmpInfoA.lngSubMoney)
                && (slngOrderNum == g_WorkInfo.lngOrderNum)) {
            //Log.e(TAG,"---------卡信息相同，退出---------");
            return;
        }

        sTypeTemp = ucType;
        slngPaymentMoney = g_WorkInfo.lngPaymentMoney;
        sSelfPressOk = g_WorkInfo.cSelfPressOk;
        slngOrderNum = g_WorkInfo.lngOrderNum;
        g_CardBasicTmpInfoA = new CardBasicParaInfo();
        try {
            g_CardBasicTmpInfoA = (CardBasicParaInfo) pCardBasicInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if (ucType == CARD_NULL) {
            FL_payok.setVisibility(View.INVISIBLE);
            FL_ad.setVisibility(View.VISIBLE);
            RL_Faceclick.setVisibility(View.INVISIBLE);
            Rl_Qrcode.setVisibility(View.INVISIBLE);
            NoShowFaceDetecte();
            tv_Paymoney.setTextColor(Color.RED);
            tv_Paymoney.setText("0.00");
            tv_Bursemoney.setText("0.00");
            tv_Primoney.setText("0.00");
            tv_Managemoney.setText("0.00");
            tv_Subsidymoney.setText("0.00");
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
            tv_Prompt.setText("欢迎使用");
            ShowUserPhoto(0, 0);
        } else if (ucType == CARD_INMONEY) {
            cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
            tv_Paymoney.setText(cShowTemp);
            tv_Bursemoney.setText("0.00");
            tv_Primoney.setText("0.00");
            tv_Managemoney.setText("0.00");
            tv_Subsidymoney.setText("0.00");

            //定额自动时显示请刷卡
            if ((g_WorkInfo.cSelfPressOk == 1)
                    || ((g_LocalInfo.cBookSureMode == 0) && (g_LocalInfo.cInputMode == 3))) {
                if ((g_SystemInfo.cFaceDetectFlag == 1)
                        && (g_WorkInfo.cFaceInitState == 2)
                        && (g_WorkInfo.cRunState == 1)
                        && (g_WorkInfo.cTestState == 0)) {
                    FL_payok.setVisibility(View.INVISIBLE);
                    if (g_LocalInfo.cFaceModeFlag == 0){
                        FL_ad.setVisibility(View.VISIBLE);
                        RL_Faceclick.setVisibility(View.VISIBLE);
                        if (g_LocalInfo.iBusinessQRState == 1)
                            Rl_Qrcode.setVisibility(View.VISIBLE);
                    }else{
                    }
                } else {
                    //无人脸应用
                    FL_payok.setVisibility(View.INVISIBLE);
                    RL_Faceclick.setVisibility(View.INVISIBLE);
                    NoShowFaceDetecte();
                    FL_ad.setVisibility(View.VISIBLE);
                }
                tv_Paymoney.setTextColor(Color.BLACK);
                tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
                tv_Prompt.setText("请支付...");
            } else {
                tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
                tv_Prompt.setText("欢迎使用");
                FL_payok.setVisibility(View.INVISIBLE);
                RL_Faceclick.setVisibility(View.INVISIBLE);
                NoShowFaceDetecte();
                FL_ad.setVisibility(View.VISIBLE);
                ShowUserPhoto(0, 0);
            }
            if (g_WorkInfo.cTestState == 1) {
                Log.i(TAG, "测试模式");
                g_WorkInfo.lngPaymentMoney = 1;
                cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
                tv_Paymoney.setText(cShowTemp);
                tv_Paymoney.setTextColor(Color.BLACK);
            } else {
                if (g_LocalInfo.cInputMode == 3) {
                    //Log.i(TAG,"定额金额:%d",g_CardBasicTmpInfo.lngInPayMoney);
                    g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
                    cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
                    tv_Paymoney.setText(cShowTemp);
                    if ((g_LocalInfo.cBookSureMode == 1) && (g_WorkInfo.cSelfPressOk == 0)) {
                        tv_Paymoney.setTextColor(Color.RED);
                        Rl_Qrcode.setVisibility(View.INVISIBLE);
                    } else {
                        tv_Paymoney.setTextColor(Color.BLACK);
                    }
                }
            }
        } else if (ucType == CARD_READOK) {
            //卡片信息
            FL_ad.setVisibility(View.INVISIBLE);
            FL_payok.setVisibility(View.VISIBLE);
            RL_payState.setVisibility(View.VISIBLE);
            //姓名
            byte[] accName = GetUserName(g_CardBasicTmpInfoA.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Payprompt.setText(cShowTemp);
            ShowUserPhoto(g_CardBasicTmpInfoA.lngAccountID, 1);

            //钱包交易信息
            if (g_WorkInfo.cInDispelCardFlag == 1)
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngInPayMoney / 100, g_CardBasicTmpInfoA.lngInPayMoney % 100);
            else
                cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
            tv_Paymoney.setText(cShowTemp);

            if (g_CardBasicTmpInfoA.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngWorkBurseMoney / 100, g_CardBasicTmpInfoA.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }
            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPriMoney / 100, g_CardBasicTmpInfoA.lngPriMoney % 100);
            tv_Primoney.setText(cShowTemp);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngManageMoney / 100, g_CardBasicTmpInfoA.lngManageMoney % 100);
            tv_Managemoney.setText(cShowTemp);

            if (g_CardBasicTmpInfoA.lngSubMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngSubMoney / 100, g_CardBasicTmpInfoA.lngSubMoney % 100);
                tv_Subsidymoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngSubMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Subsidymoney.setText(cShowTemp);
            }
            tv_Prompt.setText("欢迎使用");
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
        } else if ((ucType == CARD_PAYOK)||(ucType == ONLING_PAYOK)) {
            //卡片信息
            FL_ad.setVisibility(View.INVISIBLE);
            FL_payok.setVisibility(View.VISIBLE);
            //RL_userInfo.setVisibility(View.INVISIBLE);
            RL_payState.setVisibility(View.VISIBLE);
            //姓名
            byte[] accName = GetUserName(g_CardBasicTmpInfoA.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Payprompt.setText(cShowTemp);
            ShowUserPhoto(g_CardBasicTmpInfoA.lngAccountID, 2);

            //钱包交易信息
            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPayMoney / 100, g_CardBasicTmpInfoA.lngPayMoney % 100);
            tv_Paymoney.setText(cShowTemp);
            tv_Paymoney.setTextColor(Color.BLACK);

            if (g_CardBasicTmpInfoA.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngWorkBurseMoney / 100, g_CardBasicTmpInfoA.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPriMoney / 100, g_CardBasicTmpInfoA.lngPriMoney % 100);
            tv_Primoney.setText(cShowTemp);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngManageMoney / 100, g_CardBasicTmpInfoA.lngManageMoney % 100);
            tv_Managemoney.setText(cShowTemp);

            if (g_CardBasicTmpInfoA.lngSubMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngSubMoney / 100, g_CardBasicTmpInfoA.lngSubMoney % 100);
                tv_Subsidymoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngSubMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Subsidymoney.setText(cShowTemp);
            }
            iv_Userpic.setVisibility(View.VISIBLE);
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(s_Activity.getApplicationContext(), R.mipmap.b_pay_success));
            tv_Prompt.setText("支付成功");
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
        } else if (ucType == CARD_PAYALLOK) {
            //卡片信息
            FL_ad.setVisibility(View.INVISIBLE);
            FL_payok.setVisibility(View.VISIBLE);
            //RL_userInfo.setVisibility(View.INVISIBLE);
            RL_payState.setVisibility(View.VISIBLE);

            //姓名
            byte[] accName = GetUserName(g_CardBasicTmpInfoA.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Payprompt.setText(cShowTemp);
            ShowUserPhoto(g_CardBasicTmpInfoA.lngAccountID, 2);

            //钱包交易信息
            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPayMoney / 100, g_CardBasicTmpInfoA.lngPayMoney % 100);
            tv_Paymoney.setText(cShowTemp);
            tv_Paymoney.setTextColor(Color.BLACK);

            if ((g_CardBasicTmpInfoA.lngWorkBurseMoney + g_CardBasicTmpInfoA.lngChaseBurseMoney) >= 0) {
                cShowTemp = String.format("%d.%02d", (g_CardBasicTmpInfoA.lngWorkBurseMoney + g_CardBasicTmpInfoA.lngChaseBurseMoney) / 100,
                        (g_CardBasicTmpInfoA.lngWorkBurseMoney + g_CardBasicTmpInfoA.lngChaseBurseMoney) % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -(g_CardBasicTmpInfoA.lngWorkBurseMoney + g_CardBasicTmpInfoA.lngChaseBurseMoney);
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPriMoney / 100, g_CardBasicTmpInfoA.lngPriMoney % 100);
            tv_Primoney.setText(cShowTemp);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngManageMoney / 100, g_CardBasicTmpInfoA.lngManageMoney % 100);
            tv_Managemoney.setText(cShowTemp);

            if (g_CardBasicTmpInfoA.lngSubMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngSubMoney / 100, g_CardBasicTmpInfoA.lngSubMoney % 100);
                tv_Subsidymoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngSubMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Subsidymoney.setText(cShowTemp);
            }
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(s_Activity.getApplicationContext(), R.mipmap.b_pay_success));
            tv_Prompt.setText("支付成功");
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
        }
    }

    public void ShowQRCodeInfo(ShopQRCodeInfo pShopQRCodeInfo, byte ucType) {
        //显示商户二维码
        if ((g_LocalInfo.iBusinessQRState == 1)
                && (g_WorkInfo.cTestState == 0)) {
            Rl_Qrcode.setVisibility(View.VISIBLE);
            //g_ShopQRCodeInfo=Publicfun.SetShopQRCodeInfo((byte) 3);
            String strEncrypInfo = Publicfun.QRCodeInfoEncryp(pShopQRCodeInfo);
            Bitmap btQRCode = Publicfun.CreateQRBitmap(strEncrypInfo, 380, 380);
            iv_Qrcode.setImageBitmap(btQRCode);
        }
    }

    //显示刷卡信息 ucType: 0 清除卡片信息 1 显示交易金额 2 卡片刷卡成功 3 卡片交易成功 5 显示卡片错误信息
    public void ShowCardInfo_Dispel(CardBasicParaInfo pCardBasicInfo, byte ucType) {
        long lngTemp;
        String cShowTemp = "";

        if ((ucType == sTypeTemp)
                && (slngPaymentMoney == g_WorkInfo.lngPaymentMoney)
                && (pCardBasicInfo.lngAccountID == g_CardBasicTmpInfoA.lngAccountID)
                && (pCardBasicInfo.lngCardID == g_CardBasicTmpInfoA.lngCardID)) {
            //Log.e(TAG,"---------卡信息相同，退出---------");
            return;
        }

        sTypeTemp = ucType;
        slngPaymentMoney = g_WorkInfo.lngPaymentMoney;
        sSelfPressOk = g_WorkInfo.cSelfPressOk;
        g_CardBasicTmpInfoA = new CardBasicParaInfo();
        try {
            g_CardBasicTmpInfoA = (CardBasicParaInfo) pCardBasicInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        if (ucType == RDCARD_NULL) {
            FL_payok.setVisibility(View.INVISIBLE);
            FL_ad.setVisibility(View.VISIBLE);
            RL_Faceclick.setVisibility(View.INVISIBLE);
            FL_face.setVisibility(View.INVISIBLE);
            tv_Paymoney.setTextColor(Color.RED);
            tv_Paymoney.setText("0.00");
            tv_Bursemoney.setText("0.00");
            tv_Primoney.setText("0.00");
            tv_Managemoney.setText("0.00");
            tv_Subsidymoney.setText("0.00");
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
            tv_Prompt.setText("欢迎使用");
            ShowUserPhoto(0, 0);

        } else if (ucType == RDCARD_INMONEY) {
            cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
            tv_Paymoney.setText(cShowTemp);
            tv_Bursemoney.setText("0.00");
            tv_Primoney.setText("0.00");
            tv_Managemoney.setText("0.00");
            tv_Subsidymoney.setText("0.00");

            tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
            tv_Prompt.setText("请冲正");
            FL_payok.setVisibility(View.INVISIBLE);
            RL_Faceclick.setVisibility(View.INVISIBLE);
            FL_face.setVisibility(View.INVISIBLE);
            FL_ad.setVisibility(View.VISIBLE);
            ShowUserPhoto(0, 0);

        } else if (ucType == RDCARD_READOK) {
            //卡片信息
            FL_ad.setVisibility(View.INVISIBLE);
            FL_payok.setVisibility(View.VISIBLE);
            RL_payState.setVisibility(View.VISIBLE);
            //姓名
            byte[] accName = GetUserName(g_CardBasicTmpInfoA.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Payprompt.setText(cShowTemp);
            ShowUserPhoto(g_CardBasicTmpInfoA.lngAccountID, 1);

            //钱包交易信息
            if (g_WorkInfo.cInDispelCardFlag == 1)
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngInPayMoney / 100, g_CardBasicTmpInfoA.lngInPayMoney % 100);
            else
                cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
            tv_Paymoney.setText(cShowTemp);

            if (g_CardBasicTmpInfoA.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngWorkBurseMoney / 100, g_CardBasicTmpInfoA.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }
            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPriMoney / 100, g_CardBasicTmpInfoA.lngPriMoney % 100);
            tv_Primoney.setText(cShowTemp);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngManageMoney / 100, g_CardBasicTmpInfoA.lngManageMoney % 100);
            tv_Managemoney.setText(cShowTemp);

            if (g_CardBasicTmpInfoA.lngSubMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngSubMoney / 100, g_CardBasicTmpInfoA.lngSubMoney % 100);
                tv_Subsidymoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngSubMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Subsidymoney.setText(cShowTemp);
            }
            tv_Prompt.setText("请确认冲正信息");
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
        } else if (ucType == RDCARD_PAYOK) {
            //卡片信息
            FL_ad.setVisibility(View.INVISIBLE);
            FL_payok.setVisibility(View.VISIBLE);
            //RL_userInfo.setVisibility(View.INVISIBLE);
            RL_payState.setVisibility(View.VISIBLE);
            //姓名
            byte[] accName = GetUserName(g_CardBasicTmpInfoA.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Payprompt.setText(cShowTemp);
            ShowUserPhoto(g_CardBasicTmpInfoA.lngAccountID, 2);

            //钱包交易信息
            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPayMoney / 100, g_CardBasicTmpInfoA.lngPayMoney % 100);
            tv_Paymoney.setText(cShowTemp);
            tv_Paymoney.setTextColor(Color.BLACK);

            if (g_CardBasicTmpInfoA.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngWorkBurseMoney / 100, g_CardBasicTmpInfoA.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPriMoney / 100, g_CardBasicTmpInfoA.lngPriMoney % 100);
            tv_Primoney.setText(cShowTemp);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngManageMoney / 100, g_CardBasicTmpInfoA.lngManageMoney % 100);
            tv_Managemoney.setText(cShowTemp);

            if (g_CardBasicTmpInfoA.lngSubMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngSubMoney / 100, g_CardBasicTmpInfoA.lngSubMoney % 100);
                tv_Subsidymoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngSubMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Subsidymoney.setText(cShowTemp);
            }
            iv_Userpic.setVisibility(View.VISIBLE);
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(s_Activity.getApplicationContext(), R.mipmap.b_pay_success));
            tv_Prompt.setText("冲正成功");
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
        } else if (ucType == RDCARD_PAYOKONLING) {
            //卡片信息
            FL_ad.setVisibility(View.INVISIBLE);
            FL_payok.setVisibility(View.VISIBLE);
            RL_payState.setVisibility(View.VISIBLE);
            //姓名
            byte[] accName = GetUserName(g_CardBasicTmpInfoA.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Payprompt.setText(cShowTemp);
            ShowUserPhoto(g_CardBasicTmpInfoA.lngAccountID, 2);

            //钱包交易信息
            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPayMoney / 100, g_CardBasicTmpInfoA.lngPayMoney % 100);
            tv_Paymoney.setText(cShowTemp);
            tv_Paymoney.setTextColor(Color.BLACK);

            if (g_CardBasicTmpInfoA.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngWorkBurseMoney / 100, g_CardBasicTmpInfoA.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }
            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPriMoney / 100, g_CardBasicTmpInfoA.lngPriMoney % 100);
            tv_Primoney.setText(cShowTemp);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngManageMoney / 100, g_CardBasicTmpInfoA.lngManageMoney % 100);
            tv_Managemoney.setText(cShowTemp);

            if (g_CardBasicTmpInfoA.lngSubMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngSubMoney / 100, g_CardBasicTmpInfoA.lngSubMoney % 100);
                tv_Subsidymoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngSubMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Subsidymoney.setText(cShowTemp);
            }
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(s_Activity.getApplicationContext(), R.mipmap.b_pay_success));
            tv_Prompt.setText("在线冲正成功");
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
        } else if (ucType == RDCARD_ONLING) {
            //卡片信息
            FL_ad.setVisibility(View.INVISIBLE);
            FL_payok.setVisibility(View.VISIBLE);
            RL_payState.setVisibility(View.VISIBLE);
            //姓名
            byte[] accName = GetUserName(g_CardBasicTmpInfoA.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Payprompt.setText(cShowTemp);
            ShowUserPhoto(g_CardBasicTmpInfoA.lngAccountID, 1);

            //钱包交易信息
            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngInPayMoney / 100, g_CardBasicTmpInfoA.lngInPayMoney % 100);
            tv_Paymoney.setText(cShowTemp);

            if (g_CardBasicTmpInfoA.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngWorkBurseMoney / 100, g_CardBasicTmpInfoA.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngPriMoney / 100, g_CardBasicTmpInfoA.lngPriMoney % 100);
            tv_Primoney.setText(cShowTemp);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngManageMoney / 100, g_CardBasicTmpInfoA.lngManageMoney % 100);
            tv_Managemoney.setText(cShowTemp);

            if (g_CardBasicTmpInfoA.lngSubMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfoA.lngSubMoney / 100, g_CardBasicTmpInfoA.lngSubMoney % 100);
                tv_Subsidymoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfoA.lngSubMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Subsidymoney.setText(cShowTemp);
            }
            tv_Prompt.setText("请确认冲正信息");
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorLGreen));
        }
    }

    // 显示交易密码键盘
    public void ShowPwdKeyboard(int iMode) {
        if (iMode == 0) {
            tv_Prompt.setText("请输入密码");
            ll_keyboard.setVisibility(View.VISIBLE);
            et_Password.setText("");
            FL_payok.setVisibility(View.INVISIBLE);
            FL_ad.setVisibility(View.INVISIBLE);
            NoShowFaceDetecte();
            RL_Faceclick.setVisibility(View.INVISIBLE);
            RL_payState.setVisibility(View.INVISIBLE);
        } else {
            tv_Prompt.setText("");
            ll_keyboard.setVisibility(View.INVISIBLE);
            FL_ad.setVisibility(View.VISIBLE);
        }
    }

    // 显示交易金额
    public void ShowPaymoney(long lngPaymentMoney) {
        //钱包交易信息
        String cShowTemp = String.format("%d.%02d", lngPaymentMoney / 100, lngPaymentMoney % 100);
        tv_Paymoney.setText(cShowTemp);
    }

    //显示错误信息
    public void ShowCardErrInfo(String strErrorInfo, byte ucType) {
        sTypeTemp = ucType;
        FL_payok.setVisibility(View.VISIBLE);
        RL_payState.setVisibility(View.VISIBLE);
        tv_Paymoney.setText("0.00");
        tv_Bursemoney.setText("0.00");
        tv_Primoney.setText("0.00");
        tv_Managemoney.setText("0.00");
        tv_Subsidymoney.setText("0.00");
        tv_Prompt.setTextColor(Color.RED);
        tv_Prompt.setText(strErrorInfo);
        tv_Payprompt.setText(strErrorInfo);
        iv_Userpic.setImageDrawable(ContextCompat.getDrawable(s_Activity.getApplicationContext(), R.mipmap.b_pay_error));
    }

    //显示正在支付中
    public void ShowCardPaying(String strInfo) {
        tv_Prompt.setTextColor(Color.RED);
        tv_Prompt.setText(strInfo);
    }

    //显示副屏
    public void ShowDisplay() {
        show();
    }

    //隐藏副屏
    public void HideDisplay() {
        hide();
    }

    /**
     * Android Camera RGB类的回调
     */
    private Camera.PreviewCallback mCamePreviewCallback = new Camera.PreviewCallback() {
        private int frameIndex = 0;

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (isShowCameraPre) {
                if (yuvType == null) {
                    yuvType = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
                    in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

                    rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(THFI_Param.IMG_WIDTH).setY(THFI_Param.IMG_HEIGHT);
                    out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
                }
                in.copyFrom(data);
                yuvToRgbIntrinsic.setInput(in);
                yuvToRgbIntrinsic.forEach(out);

                Bitmap bitmap = Bitmap.createBitmap(THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, Bitmap.Config.ARGB_8888);
                out.copyTo(bitmap);
                if (bitmap != null) {
                    Message msg = Message.obtain();
                    msg.what = WHAT_DRAW;
                    msg.obj = bitmap;
                    handler.sendMessage(msg);
                }
            }
        }
    };

    /**
     * Android Camera 红外类的回调
     */
    private Camera.PreviewCallback mCamePreviewCallbackInfr = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (isShowCameraPre) {
                Camera.Size perviewSize = camera.getParameters().getPreviewSize();
                int width = perviewSize.width;
                int height = perviewSize.height;
                if (yuvType == null) {
                    yuvType = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
                    in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

                    rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
                    out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
                }
                in.copyFrom(data);
                yuvToRgbIntrinsic.setInput(in);
                yuvToRgbIntrinsic.forEach(out);
                Bitmap gBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                out.copyTo(gBitmap);
                Bitmap bmpInfrared = Bitmap.createBitmap(gBitmap, 0, 0, gBitmap.getWidth(), gBitmap.getHeight(), mMatrixInfr, true);
                if (bmpInfrared != null) {
                    Message msg = Message.obtain();
                    msg.what = WHAT_DRAW_INFR;
                    msg.obj = bmpInfrared;
                    handler.sendMessage(msg);
                }
            }
        }
    };

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if(g_LocalInfo.cFaceModeFlag==1)
                openAllCamera();//打开摄像头
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                Log.e(TAG,"关闭RGB摄像头");
                mCamera.closeCamera();
            }
            if (mCameraInfr != null) {
                Log.e(TAG,"关闭红外摄像头");
                mCameraInfr.closeCamera();
            }
        }
    };

    //RGB相机预览画面
    private void DrawImage(Bitmap bitmap) {

        if (bitmap == null) {
            Canvas canvas = null;
            try {
                canvas = mSurfaceHolder.lockCanvas(null);
                canvas.drawColor(Color.BLACK);
            }catch (Exception e) {
                // TODO: handle exception
            }finally {
                if(canvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            return;
        }

        Canvas canvas = null;
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(bitmap, 0, 0, THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, mMatrix, true);
            FaceWorkTask.DetectFaceThread.setBitmap(bmp);

            canvas = mSurfaceHolder.lockCanvas();
            if (canvas != null) {
                Rect[] rects = FaceWorkTask.DetectFaceThread.getFaceRect();
                if (rects != null) {
                    Canvas canvasBmp = new Canvas(bmp);
                    Paint paint = new Paint();
                    paint.setColor(Color.GREEN);
                    paint.setStyle(Paint.Style.STROKE);//不填充
                    paint.setAntiAlias(true);
                    paint.setStrokeWidth(6);  //线的宽度
                    for (Rect rect : rects) {
                        Path mPath = new Path();
                        float width = (rect.right - rect.left);
                        int widthLength = (int) width / 4;
                        int heightLength = (int) width / 4;
                        mPath.moveTo(rect.left, rect.top + heightLength);
                        mPath.lineTo(rect.left, rect.top);
                        mPath.lineTo(rect.left + widthLength, rect.top);

                        mPath.moveTo(rect.left + widthLength, rect.bottom);
                        mPath.lineTo(rect.left, rect.bottom);
                        mPath.lineTo(rect.left, rect.bottom - heightLength);

                        mPath.moveTo(rect.right, rect.top + heightLength);
                        mPath.lineTo(rect.right, rect.top);
                        mPath.lineTo(rect.right - widthLength, rect.top);

                        mPath.moveTo(rect.right - widthLength, rect.bottom);
                        mPath.lineTo(rect.right, rect.bottom);
                        mPath.lineTo(rect.right, rect.bottom - heightLength);
                        canvasBmp.drawPath(mPath, paint);
                    }
                }
                canvas.drawBitmap(bmp, 0, 0, null);
            }
        } catch (Exception e) {
        } finally {
            if (canvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    //红外相机预览画面
    private void DrawImageInfr(Bitmap bitmap) {
        Canvas canvas = null;
        Bitmap bmp = null;
        FaceWorkTask.FaceCheckThread.setInfrBitmap(bitmap);
        try {
            //bmp = createThumbnail(bitmap);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] data = bos.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, bos.size(), options);
            options.inSampleSize = 3;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            canvas = mSurfaceHolderInfr.lockCanvas();
            if (canvas != null) {
                canvas.drawBitmap(bmp, 0, 0, null);
            }
        } catch (Exception e) {
        } finally {
            if (canvas != null) {
                mSurfaceHolderInfr.unlockCanvasAndPost(canvas);
            }
        }
    }

    //创建压缩图
    private Bitmap createThumbnail(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] data = bos.toByteArray();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, bos.size(), options);
        options.inSampleSize = 3;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        return bm;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    protected void onDestroy() {

//        if (gBitmap != null) {
//            gBitmap.recycle();
//            gBitmap = null;
//        }
        //super.onStop();
    }
}

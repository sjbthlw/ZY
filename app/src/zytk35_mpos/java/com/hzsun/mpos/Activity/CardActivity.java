package com.hzsun.mpos.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzsun.mpos.CardWork.CardBasicParaInfo;
import com.hzsun.mpos.FaceApp.FaceWorkTask;
import com.hzsun.mpos.Public.CircleTransform;
import com.hzsun.mpos.Public.Keyoperpro;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.ToastUtils;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.ShopQRCodeInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.hzsun.mpos.CardWork.CardWorkTask.CardSendHandler;
import static com.hzsun.mpos.CardWork.CardWorkTask.EVT_CardCheckOut;
import static com.hzsun.mpos.Global.Global.APPUPING;
import static com.hzsun.mpos.Global.Global.CAMERADEAL;
import static com.hzsun.mpos.Global.Global.LAN_EP_MONEYPOS;
import static com.hzsun.mpos.Global.Global.POWERSAVEDEAL;
import static com.hzsun.mpos.Global.Global.PhotoPath;
import static com.hzsun.mpos.Global.Global.ROMUPING;
import static com.hzsun.mpos.Global.Global.g_ExtDisplay;
import static com.hzsun.mpos.Global.Global.g_HttpCommInfo;
import static com.hzsun.mpos.Global.Global.g_UICardHandler;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardBasicTmpInfo;
import static com.hzsun.mpos.Global.Global.g_CardInfo;
import static com.hzsun.mpos.Global.Global.g_FaceIdentInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_RecordInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.ByteToString;
import static com.hzsun.mpos.Public.Publicfun.CheckDayAndBusinessTotal;
import static com.hzsun.mpos.Public.Publicfun.GetUserName;
import static com.hzsun.mpos.Public.Publicfun.RelayControl;
import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.WARN;
import static com.hzsun.mpos.Sound.SoundPlay.VoicePlay;


public class CardActivity extends AppCompatActivity {

    private static final String TAG = "CardActivity";

    private Handler sTimerHandler;//定时器
    private Timer timer;
    protected CardPreActivity cardPreActivity;
    public static final int CARD_NULL = 0;     //无卡片
    public static final int CARD_INMONEY = 1;  //输入金额
    public static final int CARD_READOK = 2;   //读卡成功
    public static final int CARD_PAYOK = 3;    //支付成功
    public static final int CARD_PAYALLOK = 4; //支付成功(工作钱包-追扣钱包)
    public static final int CARD_PWDKBOPEN = 5; //密码键盘打开
    public static final int CARD_PWDKBCLOSE = 6;//密码键盘关闭
    public static final int CARD_SHOWTOTAL = 7; //显示全部金额字段
    public static final int CARD_RWSTART = 8;   //卡片重读卡开始
    public static final int CARD_RWEND = 9;     //卡片重读卡结束
    public static final int QRCODE_SHOW = 10;   //二维码显示
    public static final int CARD_PAYING = 11;   //正在支付中
    public static final int ONLING_PAYOK = 12;    //在线支付成功

    public static final int HTTPPHOTO = 20;     //HTTP照片显示
    public static final int CARD_ERR = 100;    //卡片错误


    private ImageView iv_Userpic, iv_NetState;
    private TextView tv_ShoperName, tv_Prompt, tv_Name, tv_Paymoney, tv_Bursemoney, tv_Datetime, tv_CpuTemp,tv_Upper;
    private TextView tv_Daypaysum, tv_Daypaymoney, tv_Mealpaysum, tv_Mealpaymoney, tv_Mode;

    private int sFaceInitState;
    private int sRunState;
    private int sNetlinkStatus;
    private short sInputMode;
    private int sBookMoney;
    private short sBookSureMode;
    private short sTestState;
    private long slngTimer;
    private byte sTypeTemp;
    private byte sSelfPressOk;
    private long slngPaymentMoney;        //输入的交易金额整行(最大60000)
    private long slngAccountID;
    private int sType;
    private long slngStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card);
        //显示副屏
        if (g_ExtDisplay != null) {
            try {
                cardPreActivity = new CardPreActivity(CardActivity.this, this, g_ExtDisplay);
                cardPreActivity.ShowDisplay();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "刷卡显示副屏异常:" + e.getMessage());
            } finally {

            }
        }
        iv_NetState = (ImageView) findViewById(R.id.iv_netState);
        iv_Userpic = (ImageView) findViewById(R.id.iv_suserpic);

        tv_CpuTemp = ((TextView) findViewById(R.id.tv_cput));
        tv_Upper = ((TextView) findViewById(R.id.tv_upper));
        tv_ShoperName = ((TextView) findViewById(R.id.tv_shoperName));
        tv_Datetime = ((TextView) findViewById(R.id.tv_datetime));
        tv_Prompt = (TextView) findViewById(R.id.tv_sprompt);
        tv_Name = (TextView) findViewById(R.id.tv_sname);
        tv_Paymoney = (TextView) findViewById(R.id.tv_spaymoney);
        tv_Bursemoney = (TextView) findViewById(R.id.tv_sbursemoney);
        tv_Mode = (TextView) findViewById(R.id.tv_mode);

        tv_Daypaysum = (TextView) findViewById(R.id.tv_sdaypaysum);
        tv_Daypaymoney = (TextView) findViewById(R.id.tv_sdaypaymoney);
        tv_Mealpaysum = (TextView) findViewById(R.id.tv_smealpaysum);
        tv_Mealpaymoney = (TextView) findViewById(R.id.tv_smealpaymoney);

        Log.d(TAG, "=========开始刷卡界面========");
        tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
        tv_Prompt.setText("欢迎使用");
        tv_Name.setText("");
        tv_Paymoney.setText("0.00");
        tv_Bursemoney.setText("0.00");
        tv_Mode.setText(Publicfun.GetPayModeStr(g_LocalInfo.cInputMode));

        sFaceInitState = 0;
        sRunState = -1;
        sNetlinkStatus = -1;
        sInputMode = 0;
        sBookMoney = 0;
        sBookSureMode = 0;
        sTestState = 0;
        slngTimer = 0;
        sTypeTemp = 0;
        sSelfPressOk = 0;
        slngPaymentMoney = 0;
        slngStart = 0;

        g_UICardHandler = new Handler() {
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
                        if (g_ExtDisplay != null)
                            cardPreActivity.ShowCardInfo(tCardBasicInfo, (byte) msg.what);
                        break;

                    case CARD_PAYING:
                        ShowCardPaying("正在支付中...");
                        if (g_ExtDisplay != null)
                            cardPreActivity.ShowCardPaying("正在支付中...");
                        break;

                    case QRCODE_SHOW:
                        ShopQRCodeInfo tShopQRCodeInfo = (ShopQRCodeInfo) msg.obj;
                        if (g_ExtDisplay != null)
                            cardPreActivity.ShowQRCodeInfo(tShopQRCodeInfo, (byte) msg.what);
                        break;

                    case CARD_ERR:
                        List<String> Strlist = new ArrayList<String>();
                        Strlist = (List<String>) msg.obj;
                        String strTemp = Strlist.get(0);
                        int iErrorCode = Integer.parseInt(strTemp);
                        String strErrorCode = Strlist.get(1);
                        Log.e(TAG, "失败:" + strErrorCode);
                        ShowCardErrInfo(strErrorCode, (byte) msg.what);
                        if (g_ExtDisplay != null)
                            cardPreActivity.ShowCardErrInfo(strErrorCode, (byte) msg.what);
                        break;

                    case CARD_PWDKBOPEN:
                        tv_Prompt.setText("等待输入用户密码");
                        if (g_ExtDisplay != null)
                            cardPreActivity.ShowPwdKeyboard(0);
                        VoicePlay("inpassword");
                        break;

                    case CARD_PWDKBCLOSE:
                        Log.e(TAG, "关闭用户密码键盘");
                        if (g_ExtDisplay != null)
                            cardPreActivity.ShowPwdKeyboard(1);
                        break;

                    case CARD_RWSTART:
                        tv_Prompt.setText("等待卡片重刷");
                        tv_Prompt.setTextColor(Color.RED);
                        if (g_ExtDisplay != null) {
                            cardPreActivity.tv_Prompt.setText("请放回卡片");
                            cardPreActivity.tv_Prompt.setTextColor(Color.RED);
                        }
                        VoicePlay("card_reswipe");
                        break;

                    case CARD_RWEND:
                        Log.e(TAG, "等待卡片重刷结束");
                        break;

                    case HTTPPHOTO:
                        String strAccnum = (String) msg.obj;
                        try {
                            long lngAccountID = Long.parseLong(strAccnum);
                            ShowHttpUserPhoto(lngAccountID);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "数据转换异常:" + e.getMessage());
                        }
                        break;

                    case CAMERADEAL:
                        if ((g_ExtDisplay != null)) {
                            if(g_LocalInfo.cFaceModeFlag==0){
                                if ((int) msg.obj == 0)
                                    cardPreActivity.closeAllCamera();
                                else
                                    cardPreActivity.openAllCamera();
                            }
                        }
                        break;

                    case POWERSAVEDEAL:
                        GotoStandbyActivity();
                        break;

                    case APPUPING:
                    case ROMUPING:
                        int iRate = (int) msg.obj;
                        tv_Upper.setText("%"+iRate);
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

    //定时器工作
    private void TimeScanWork() {
        slngTimer++;
        if (g_WorkInfo.cCardEnableFlag == 1) {
            if (slngTimer % 10 == 0) {
                ShowCPUTemp();
            }
            if (slngTimer % 30 == 0) {
                ShowDateTime();
                ShowShoperName();
            }
            if ((sNetlinkStatus != g_WorkInfo.cNetlinkStatus)
                    || (sRunState != g_WorkInfo.cRunState)) {
                Log.e(TAG, "网络状态发生变化:" + g_WorkInfo.cNetlinkStatus);
                sNetlinkStatus = g_WorkInfo.cNetlinkStatus;
                sRunState = g_WorkInfo.cRunState;
                ShowNetState(g_WorkInfo.cNetlinkStatus);
            }
            if((g_SystemInfo.cFaceDetectFlag==1)//启用人脸
                    &&(g_WorkInfo.cRunState==1)
                    &&(g_LocalInfo.cFaceModeFlag==1)
                    &&((g_WorkInfo.cSelfPressOk==1)||((g_LocalInfo.cInputMode==3)&&(g_LocalInfo.cBookSureMode==0)))
                    &&(g_CardInfo.cExistState!=1))
            {
                if(sFaceInitState!=g_WorkInfo.cFaceInitState)
                {
                    sFaceInitState=g_WorkInfo.cFaceInitState;
                    if((g_WorkInfo.cFaceInitState==2)&&(g_WorkInfo.cTestState==0))
                    {
                        FaceWorkTask.StartDetecte(true);
                        cardPreActivity.ShowFaceDetecte();
                    }
                }
            }
            //更新交易模式和交易金额
            ShowMoneyInfo();
        }
        CheckExitPayCardMenu();
    }

    //进入主界面
    private void GotoPayMainMenu() {
        if (g_WorkInfo.cCardEnableFlag == 1) {
            g_WorkInfo.cCardEnableFlag = 0;
            Log.d(TAG, "进入主界面");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    //进入休眠界面
    private void GotoStandbyActivity() {
        Log.d(TAG, "刷卡界面进入休眠界面");
        g_WorkInfo.cBackActivityState = 2;
        startActivity(new Intent(CardActivity.this, StandbyActivity.class));
        finish();
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
            if (g_StationInfo.iShopUserID == 0) {
                Log.d(TAG, "=============不定商户不允许使用================");
                GotoPayMainMenu();
                return;
            }
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
        //cNetlinkStatus;         //物理网线状态(1:有线eth0,2:无线wifi wlan0,0:无)
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
                if (g_ExtDisplay != null)
                    cardPreActivity.iv_NetState.setImageResource(R.mipmap.s_net_null);
                break;
            case 1: //以太网络 网络故障 脱机
                iv_NetState.setImageResource(R.mipmap.s_net_etherr);
                if (g_ExtDisplay != null)
                    cardPreActivity.iv_NetState.setImageResource(R.mipmap.s_net_etherr);
                break;
            case 2: //以太网络 联机运行
                iv_NetState.setImageResource(R.mipmap.s_net_eth);
                if (g_ExtDisplay != null)
                    cardPreActivity.iv_NetState.setImageResource(R.mipmap.s_net_eth);
                break;
            case 3: //wifi网络 网络故障 脱机
                iv_NetState.setImageResource(R.mipmap.s_net_wifierr);
                if (g_ExtDisplay != null)
                    cardPreActivity.iv_NetState.setImageResource(R.mipmap.s_net_wifierr);
                break;
            case 4: //wifi网络 联机运行
                iv_NetState.setImageResource(R.mipmap.s_net_wifi);
                if (g_ExtDisplay != null)
                    cardPreActivity.iv_NetState.setImageResource(R.mipmap.s_net_wifi);
                break;
        }
    }

    //显示交易金额
    private void ShowMoneyInfo() {
        //判断是否切换了交易模式
        if ((sInputMode != g_LocalInfo.cInputMode)
                || (sBookMoney != g_WorkInfo.wBookMoney)
                || (sBookSureMode != g_LocalInfo.cBookSureMode)
                || (sTestState != g_WorkInfo.cTestState)) {
            Log.e(TAG, "交易模式和金额发生变化:" + g_WorkInfo.wBookMoney);
            g_WorkInfo.cQrCodestatus = 0;
            g_WorkInfo.cPayDisPlayStatus = 0;

            if (sInputMode != g_LocalInfo.cInputMode) {
                sInputMode = g_LocalInfo.cInputMode;
                tv_Mode.setText(Publicfun.GetPayModeStr(g_LocalInfo.cInputMode));
                //清空交易金额
                tv_Paymoney.setText("0.00");
                tv_Paymoney.setTextColor(Color.RED);
            }
            sBookMoney = g_WorkInfo.wBookMoney;
            sBookSureMode = g_LocalInfo.cBookSureMode;
            sTestState = g_WorkInfo.cTestState;

            if ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0)) {
                g_Nlib.QR_SetDeviceReadMode(4);//设置自动感应模式
            } else {
                g_Nlib.QR_SetDeviceReadMode(2);//识度模式 $100002-C9FF 开关持续 2
                g_Nlib.QR_SetDeviceReadEnable(2);//命令触发 //$108003-F8E3 结束识读 2
            }
            //判断是否是定额模式
            if (g_WorkInfo.cTestState == 1)//测试模式下
            {
                g_WorkInfo.lngPaymentMoney = 1;
                tv_Mode.setText(Publicfun.GetPayModeStr((short) 6));
            } else {
                if (g_LocalInfo.cInputMode == 3) {
                    g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
                    String cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
                    tv_Paymoney.setText(cShowTemp);
                    if (g_ExtDisplay != null)
                        cardPreActivity.tv_Paymoney.setText(cShowTemp);
                    if ((g_LocalInfo.cBookSureMode == 0) || (g_WorkInfo.cSelfPressOk == 1)) {
                        tv_Paymoney.setTextColor(Color.BLACK);
                        tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
                        tv_Prompt.setText("请支付");
                    } else {
                        tv_Paymoney.setTextColor(Color.RED);
                        tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
                        tv_Prompt.setText("欢迎使用");
                    }
                }
            }
        }
    }

    //显示温度
    private void ShowCPUTemp() {
        String cmdShell;
        cmdShell = Publicfun.RunShellCmdA("cat /sys/class/thermal/thermal_zone1/temp");
        tv_CpuTemp.setText("CPU: " + cmdShell);
        if (g_ExtDisplay != null)
            cardPreActivity.ShowCPUTemp("CPU: " + cmdShell);
    }

    //显示时间
    private void ShowDateTime() {
        String strTmp = Publicfun.getFullTime("yyyy-MM-dd HH:mm:ss E");
        String strData = strTmp.substring(5, 16);
        String strWeek = Publicfun.GetWeekName(strTmp);

//        String strTmp = Publicfun.GetFullDateWeekTime(Publicfun.toData(System.currentTimeMillis()));
//        String strWeek = Publicfun.GetWeekCHNName(strTmp);
//        String strDataTime = strTmp.substring(5, 10) + " " + strTmp.substring(14, 20) + " " + strWeek;

        String strDataTime = strData + " " + strWeek;
        tv_Datetime.setText(strDataTime);
        if (g_ExtDisplay != null)
            cardPreActivity.ShowDateTime(strDataTime);
    }

    //显示商户
    private void ShowShoperName() {
        //String strShowTemp="正元智慧";
        String strShowTemp = "";
        tv_ShoperName.setText(strShowTemp);
        if (g_ExtDisplay != null)
            cardPreActivity.tv_ShoperName.setText(strShowTemp);
    }

    //显示日餐累计
    private void ShowTotalMoneySun() {
        String cShowTemp = "";
        //时段营业笔数
        cShowTemp = String.format("%d", g_RecordInfo.wTotalBusinessSum);
        tv_Mealpaysum.setText(cShowTemp);
        //时段营业总额
        cShowTemp = String.format("%d.%02d", g_RecordInfo.lngTotalBusinessMoney / 100, g_RecordInfo.lngTotalBusinessMoney % 100);
        tv_Mealpaymoney.setText(cShowTemp);

        //当日交易笔数
        cShowTemp = String.format("%d", g_RecordInfo.wTodayPaymentSum);
        tv_Daypaysum.setText(cShowTemp);

        //当日交易总额
        cShowTemp = String.format("%d.%02d", g_RecordInfo.lngTodayPaymentMoney / 100, g_RecordInfo.lngTodayPaymentMoney % 100);
        tv_Daypaymoney.setText(cShowTemp);
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
                iv_Userpic.setImageBitmap(Circle.transform(bitmap));
                if (g_ExtDisplay != null)
                    cardPreActivity.iv_Userpic.setImageBitmap(Circle.transform(bitmap));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "ShowHttpUserPhoto Exception:" + e.getMessage());
            }
        } else {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
            if (g_ExtDisplay != null)
                cardPreActivity.iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
        }
    }

    //显示照片
    public void ShowUserPhoto(long lngAccountID, int iType) {

        int iResult = 0;

        if ((iType == sType) && (slngAccountID == lngAccountID))
            return;

        slngAccountID = lngAccountID;
        sType = iType;

        if (iType == CARD_NULL)    //待机状态
        {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
        } else if (iType == CARD_INMONEY)//输入状态
        {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
        } else if ((iType == CARD_READOK) || (iType == ONLING_PAYOK))   //读卡成功状态-在线支付状态
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
                    if (bitmap != null){
                        CircleTransform Circle = new CircleTransform();
                        iv_Userpic.setImageBitmap(Circle.transform(bitmap));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ShowUserPhoto Exception:" + e.getMessage());
                }
                //0x00000008 //获取用户照片数据地址
                g_HttpCommInfo.lngSendHttpStatus |= 0x00000008;
            } else {
                iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
                g_HttpCommInfo.lngSendHttpStatus |= 0x00000008;
            }
        } else if ((iType == CARD_PAYOK) || (iType == CARD_PAYALLOK))  //支付成功状态
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
                    iv_Userpic.setImageBitmap( Circle.transform(bitmap));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ShowUserPhoto Exception:" + e.getMessage());
                }
            } else {
                iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
            }
        } else if (iType == CARD_ERR)   //失败状态
        {
            //iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(),R.mipmap.b_fail));
            //cardPreActivity.iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(),R.mipmap.b_fail));
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
                && (pCardBasicInfo.lngCardID == g_CardBasicTmpInfo.lngCardID)
                && (pCardBasicInfo.lngSubMoney == g_CardBasicTmpInfo.lngSubMoney)) {
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
            //卡片信息
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
            tv_Prompt.setText("欢迎使用");
            tv_Name.setText("");
            tv_Bursemoney.setText("0.00");
            tv_Paymoney.setTextColor(Color.RED);
            tv_Paymoney.setText("0.00");
            ShowUserPhoto(0, sTypeTemp);
        }
        if (sTypeTemp == CARD_INMONEY) {
            //卡片信息
            tv_Name.setText("");
            tv_Bursemoney.setText("0.00");

            if ((g_WorkInfo.cSelfPressOk == 1)
                    || ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0))) {
                tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
                tv_Prompt.setText("请支付");
                ShowUserPhoto(0, sTypeTemp);
            } else {
                tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
                tv_Prompt.setText("欢迎使用");
                ShowUserPhoto(0, sTypeTemp);
            }
            if (g_WorkInfo.cTestState == 1) {
                Log.d(TAG, "测试模式");
                g_WorkInfo.lngPaymentMoney = 1;
                cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
                tv_Paymoney.setText(cShowTemp);
                tv_Paymoney.setTextColor(Color.BLACK);
            } else {
                if (g_LocalInfo.cInputMode == 3) {
                    Log.d(TAG, "定额模式");
                    g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
                    cShowTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
                    tv_Paymoney.setText(cShowTemp);
                    if ((g_LocalInfo.cBookSureMode == 1) && (g_WorkInfo.cSelfPressOk == 0)) {
                        tv_Paymoney.setTextColor(Color.RED);
                    } else {
                        tv_Paymoney.setTextColor(Color.BLACK);
                    }
                }
            }
        } else if (sTypeTemp == CARD_READOK) {
            //卡片信息
            byte[] accName = GetUserName(g_CardBasicTmpInfo.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (cShowTemp.length() > 10)
                tv_Name.setText(cShowTemp.substring(0, 10));
            else
                tv_Name.setText(cShowTemp);

            if (g_WorkInfo.cPayDisPlayStatus == 1) {
                g_WorkInfo.cPayDisPlayStatus = 0;
                tv_Paymoney.setText("0.00");
                tv_Paymoney.setTextColor(Color.RED);
            }
            if (g_CardBasicTmpInfo.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngWorkBurseMoney / 100, g_CardBasicTmpInfo.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfo.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
            tv_Prompt.setText("欢迎使用");
            ShowTotalMoneySun();
            ShowUserPhoto(g_CardBasicTmpInfo.lngAccountID, sTypeTemp);//显示照片
        } else if (sTypeTemp == CARD_PAYOK) {
            //卡片信息
            byte[] accName = GetUserName(g_CardBasicTmpInfo.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (cShowTemp.length() > 10)
                tv_Name.setText(cShowTemp.substring(0, 10));
            else
                tv_Name.setText(cShowTemp);

            //显示照片
            ShowUserPhoto(g_CardBasicTmpInfo.lngAccountID, sTypeTemp);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngPayMoney / 100, g_CardBasicTmpInfo.lngPayMoney % 100);
            tv_Paymoney.setText(cShowTemp);

            if (g_CardBasicTmpInfo.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngWorkBurseMoney / 100, g_CardBasicTmpInfo.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfo.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
            tv_Prompt.setText("支付成功");
            ShowTotalMoneySun();
            RelayControl();//继电器动作
        } else if (ucType == CARD_PAYALLOK) {
            //卡片信息
            byte[] accName = GetUserName(g_CardBasicTmpInfo.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (cShowTemp.length() > 10)
                tv_Name.setText(cShowTemp.substring(0, 10));
            else
                tv_Name.setText(cShowTemp);

            //显示照片
            ShowUserPhoto(g_CardBasicTmpInfo.lngAccountID, sTypeTemp);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngPayMoney / 100, g_CardBasicTmpInfo.lngPayMoney % 100);
            tv_Paymoney.setText(cShowTemp);

            if ((g_CardBasicTmpInfo.lngWorkBurseMoney + g_CardBasicTmpInfo.lngChaseBurseMoney) >= 0) {
                cShowTemp = String.format("%d.%02d", (g_CardBasicTmpInfo.lngWorkBurseMoney + g_CardBasicTmpInfo.lngChaseBurseMoney) / 100,
                        (g_CardBasicTmpInfo.lngWorkBurseMoney + g_CardBasicTmpInfo.lngChaseBurseMoney) % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -(g_CardBasicTmpInfo.lngWorkBurseMoney + g_CardBasicTmpInfo.lngChaseBurseMoney);
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
            tv_Prompt.setText("支付成功");
            ShowTotalMoneySun();
            RelayControl();//继电器动作
        } else if (sTypeTemp == ONLING_PAYOK) { //在线支付
            //卡片信息
            byte[] accName = GetUserName(g_CardBasicTmpInfo.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (cShowTemp.length() > 10)
                tv_Name.setText(cShowTemp.substring(0, 10));
            else
                tv_Name.setText(cShowTemp);

            //显示照片
            ShowUserPhoto(g_CardBasicTmpInfo.lngAccountID, sTypeTemp);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngPayMoney / 100, g_CardBasicTmpInfo.lngPayMoney % 100);
            tv_Paymoney.setText(cShowTemp);

            if (g_CardBasicTmpInfo.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngWorkBurseMoney / 100, g_CardBasicTmpInfo.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfo.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
            tv_Prompt.setText("支付成功");
            ShowTotalMoneySun();
            RelayControl();//继电器动作
        } else if (ucType == CARD_SHOWTOTAL) {
            ShowTotalMoneySun();
        }
    }

    //显示错误信息
    public void ShowCardErrInfo(String strErrorInfo, byte ucType) {
        sTypeTemp = ucType;
        tv_Prompt.setTextColor(Color.RED);
        tv_Prompt.setText(strErrorInfo);
        ShowUserPhoto(0, 0);
    }

    //显示正在支付中
    public void ShowCardPaying(String strInfo) {
        tv_Prompt.setTextColor(Color.RED);
        tv_Prompt.setText(strInfo);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int KeyValue;
        KeyValue = event.getKeyCode();
        g_WorkInfo.lngPowerSaveCnt = System.currentTimeMillis();
        if (g_LocalInfo.cDockposFlag == 1) {	//对接机只允许此功能
            if (event.getAction() == KeyEvent.ACTION_DOWN && KeyValue == KeyEvent.KEYCODE_FUNCTION) {
                GotoFnMenu(); //进入功能界面FN
            }
            return super.onKeyDown(keyCode, event);
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            event.startTracking();
            if (event.getRepeatCount() == 0) {
                //shortPress = true;
                //Log.e(TAG,"---------KeyValue按下:"+KeyValue);
            } else {
                return true;
            }
            if ((((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9))
                    || (KeyValue == KeyEvent.KEYCODE_PERIOD)) && (g_LocalInfo.cKeyLockState == 1)) {
                Log.d(TAG, "键盘已锁定");
                Publicfun.ShowErrorStrDialog(getApplicationContext(), "键盘已锁定,请解锁！");
                return true;
            }
            //无消费成功标记时
            if (g_WorkInfo.cCheckOutState == 0) {
                //判断输入模式
                if (g_LocalInfo.cInputMode == 1)    //普通模式
                {
                    //判断是否是数字和"."
                    //统一乘号键
                    if (Keyoperpro.NormalKeyMenu_One(KeyValue) != 0)
                        return true;
                } else if (g_LocalInfo.cInputMode == 2)   //单键模式
                {
                    Keyoperpro.OddKeyMenu(KeyValue);
                } else if (g_LocalInfo.cInputMode == 3)   //定额模式
                {
                } else if (g_LocalInfo.cInputMode == 4)   //商品编码模式
                {
                }
                if (g_WorkInfo.cKeyDownState == 1) {
                    if (g_WorkInfo.cOptionMark == 0) {
                        String strTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
                        tv_Paymoney.setText(strTemp);
                        if (g_ExtDisplay != null)
                            cardPreActivity.tv_Paymoney.setText(strTemp);
                        if (g_WorkInfo.cSelfPressOk == 0)
                            tv_Paymoney.setTextColor(Color.RED);
                    } else {
                        if ((KeyValue == KeyEvent.KEYCODE_NUMPAD_MULTIPLY) || (KeyValue == KeyEvent.KEYCODE_NUMPAD_ADD))//X和+
                        {
                            String strTemp = String.format("%d.%02d", g_WorkInfo.lngTotalMoney / 100, g_WorkInfo.lngTotalMoney % 100);
                            tv_Paymoney.setText(strTemp);
                            if (g_ExtDisplay != null)
                                cardPreActivity.tv_Paymoney.setText(strTemp);
                            if (g_WorkInfo.cSelfPressOk == 0)
                                tv_Paymoney.setTextColor(Color.RED);
                        } else {
                            if (KeyValue == KeyEvent.KEYCODE_ENTER)//确定键
                            {
                                String strTemp = String.format("%d.%02d", g_WorkInfo.lngTotalMoney / 100, g_WorkInfo.lngTotalMoney % 100);
                                tv_Paymoney.setText(strTemp);
                                if (g_ExtDisplay != null)
                                    cardPreActivity.tv_Paymoney.setText(strTemp);
                            } else {
                                String strTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
                                tv_Paymoney.setText(strTemp);
                                if (g_ExtDisplay != null)
                                    cardPreActivity.tv_Paymoney.setText(strTemp);
                                if (g_WorkInfo.cSelfPressOk == 0)
                                    tv_Paymoney.setTextColor(Color.RED);
                            }
                        }
                    }
                }
            }

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_FUNCTION:
                    GotoFnMenu(); //进入功能界面FN
                    break;

                case KeyEvent.KEYCODE_ENTER:
                    slngStart = System.currentTimeMillis();
                    if ((g_WorkInfo.cStartWCardFlag == 0) &&
                            (((g_WorkInfo.lngPaymentMoney >= 0) && (g_WorkInfo.cPaymentMoneyLen != 0) && (g_WorkInfo.cKeyDownState == 1))
                                    || ((g_WorkInfo.lngPaymentMoney >= 0) && (g_LocalInfo.cInputMode == 2) && (g_WorkInfo.cKeyDownState == 1))
                                    || ((g_WorkInfo.lngPaymentMoney >= 0) && (g_LocalInfo.cInputMode == 3))
                                    || ((g_WorkInfo.cOptionMark != 0) && (g_WorkInfo.lngTotalMoney > 0)))) {
                        Log.d(TAG, "====按了确定键====金额:" + g_WorkInfo.lngPaymentMoney);
                        tv_Paymoney.setTextColor(Color.BLACK);
                        Log.d(TAG, "清除QR串口数据");
                        g_Nlib.QR_ClearRecvData(5);
                        if (g_CardInfo.cExistState == 0) {
                            //充值机
                            if (g_StationInfo.iStationClass == LAN_EP_MONEYPOS) {
                                VoicePlay("card_swipe");
                            } else {
                                VoicePlay("money_pay");
                                if (g_WorkInfo.cScanQRCodeFlag == 0) {
                                    g_WorkInfo.cScanQRCodeFlag = 1;

                                    if ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0)) {
                                    } else {
                                        Log.d(TAG, "普通模式开始识读");
                                        g_Nlib.QR_SetDeviceReadEnable(1);
                                    }
                                    if (g_SystemInfo.cFaceDetectFlag == 1)//启用人脸
                                    {
                                        if ((g_WorkInfo.cFaceInitState == 2)
                                                && (g_WorkInfo.cRunState == 1)
                                                && (g_CardInfo.cExistState != 1)
                                                && (g_WorkInfo.cTestState == 0)) {
                                            if (g_ExtDisplay != null) {
                                                if (g_LocalInfo.cFaceModeFlag == 1) {
                                                    cardPreActivity.ShowFaceDetecte();
                                                    FaceWorkTask.StartDetecte(true);
                                                } else {
                                                    cardPreActivity.openAllCamera();
                                                }
                                            }
                                        } else {
                                            if ((g_WorkInfo.cFaceInitState != 2) && (g_FaceIdentInfo.iListNum != 0))
                                                ToastUtils.showText(this, "特征码数据加载中，不启动人脸！", WARN, BOTTOM, Toast.LENGTH_LONG);//特征码数据加载中不支持扫脸
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if ((g_LocalInfo.cInputMode != 3) ||
                            (g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 1))   //定额不自动模式
                    {
                        if (g_WorkInfo.cPayDisPlayStatus == 1)
                            g_WorkInfo.cPayDisPlayStatus = 0;
                    }
                    if ((g_WorkInfo.cStartWCardFlag == 0) && (g_WorkInfo.cCardEnableFlag == 1) && (CardSendHandler != null))
                        CardSendHandler.sendEmptyMessage(EVT_CardCheckOut);

                    break;

                case KeyEvent.KEYCODE_BACK:
                    Log.e(TAG, "----------按下修改键----------");
                    //判断是否按下确定键(防止误触)
                    if ((System.currentTimeMillis() - slngStart) < 600) {
                        Log.e(TAG, "----------误触了修改键----------");
                        break;
                    }
                    g_WorkInfo.cInUserPWDFlag = 0;//取消用户密码键盘
                    g_WorkInfo.cStartReWDialogFlag = 0;//取消重写卡

                    if ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0)) {
                        Log.d(TAG, "清除QR串口数据");
                        g_Nlib.QR_ClearRecvData(5);
                    }
                    if (g_WorkInfo.cStartWCardFlag == 0) {
                        Keyoperpro.DeleteKeyFun();

                        //判断是否是定额模式
                        if (g_WorkInfo.cTestState == 1) {
                            g_WorkInfo.lngPaymentMoney = 1;
                        } else if (g_LocalInfo.cInputMode == 3) {
                            g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
                        }
                        g_CardBasicInfo.lngPayMoney = g_WorkInfo.lngPaymentMoney;
                        String strTemp = String.format("%d.%02d", g_WorkInfo.lngPaymentMoney / 100, g_WorkInfo.lngPaymentMoney % 100);
                        tv_Paymoney.setText(strTemp);
                        if (g_ExtDisplay != null)
                            cardPreActivity.tv_Paymoney.setText(strTemp);
                        if ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0)) {
                        } else {
                            tv_Paymoney.setTextColor(Color.RED);
                        }
                    }
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void GotoFnMenu() {
        startActivity(new Intent(CardActivity.this, MenuActivity.class));
        //PowerSaveDeal(0);
    }

//    public boolean onTouch(View view, MotionEvent event) {
//
//        int x,y,rawx,rawy;
//
//        int eventaction = event.getAction();
//        switch (eventaction) {
//            case MotionEvent.ACTION_DOWN:
//                break;
//            case MotionEvent.ACTION_MOVE:
//                x = (int) event.getX();
//                y = (int) event.getY();
//                rawx = (int) event.getRawX();
//                rawy = (int) event.getRawY();
//                Log.d("DEBUG", "getX=" + x + "getY=" + y + "n" + "getRawX=" + rawx
//                        + "getRawY=" + rawy + "n");
//                break;
//
//            case MotionEvent.ACTION_UP:
//
//                break;
//        }
//        return false;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int mLastX = 0;
        int mLastY = 0;
        int moveX = 0;
        int moveY = 0;
        g_WorkInfo.lngPowerSaveCnt = System.currentTimeMillis();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getRawX();
                mLastY = (int) event.getRawY();

                if ((g_WorkInfo.cInUserPWDFlag == 1) && (g_ExtDisplay != null))//开启密码键盘
                {
                    if ((mLastX > 730) && (mLastX < 870)
                            && (mLastY > 240) && (mLastY < 330)) {   //1
                        cardPreActivity.btn1.setPressed(true);
                    } else if ((mLastX > 890) && (mLastX < 1050)
                            && (mLastY > 240) && (mLastY < 330)) {  //2
                        cardPreActivity.btn2.setPressed(true);
                    } else if ((mLastX > 1070) && (mLastX < 1250)
                            && (mLastY > 240) && (mLastY < 330)) {  //3
                        cardPreActivity.btn3.setPressed(true);
                    } else if ((mLastX > 730) && (mLastX < 870)
                            && (mLastY > 330) && (mLastY < 420)) {  //4
                        cardPreActivity.btn4.setPressed(true);
                    } else if ((mLastX > 890) && (mLastX < 1050)
                            && (mLastY > 330) && (mLastY < 420)) {  //5
                        cardPreActivity.btn5.setPressed(true);
                    } else if ((mLastX > 1070) && (mLastX < 1250)
                            && (mLastY > 330) && (mLastY < 420)) {  //6
                        cardPreActivity.btn6.setPressed(true);
                    } else if ((mLastX > 730) && (mLastX < 870)
                            && (mLastY > 450) && (mLastY < 550)) {  //7
                        cardPreActivity.btn7.setPressed(true);
                    } else if ((mLastX > 890) && (mLastX < 1050)
                            && (mLastY > 450) && (mLastY < 550)) {  //8
                        cardPreActivity.btn8.setPressed(true);
                    } else if ((mLastX > 1070) && (mLastX < 1250)
                            && (mLastY > 450) && (mLastY < 550)) {  //9
                        cardPreActivity.btn9.setPressed(true);
                    } else if ((mLastX > 730) && (mLastX < 870)
                            && (mLastY > 560) && (mLastY < 660)) {  //取消
                        cardPreActivity.btnCancle.setPressed(true);
                    } else if ((mLastX > 890) && (mLastX < 1050)
                            && (mLastY > 560) && (mLastY < 660)) {  //0
                        cardPreActivity.btn0.setPressed(true);
                    } else if ((mLastX > 1070) && (mLastX < 1250)
                            && (mLastY > 560) && (mLastY < 660)) {  //确定
                        cardPreActivity.btnConfirm.setPressed(true);
                    }
                }
                //Log.e("DragView", "ACTION_DOWN:" + "rowX:" + mLastX + ";rowY:" + mLastY + ";getX:" + event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = (int) event.getRawX();
                moveY = (int) event.getRawY();
                //Log.e("DragView","ACTION_MOVE:" + "rowX:" + moveX + ";rowY:" + moveY +";getX:"+event.getX());
                break;
            case MotionEvent.ACTION_UP:
                mLastX = (int) event.getRawX();
                mLastY = (int) event.getRawY();
                //Log.e("DragView","ACTION_UP:" + "rowX:" + mLastX + ";rowY:" + mLastY +";getX:"+event.getX());
                //判断是否按下人脸识别开关
                if ((g_WorkInfo.cFaceInitState == 2)
                        && (g_WorkInfo.cRunState == 1)
                        && ((g_WorkInfo.cSelfPressOk == 1) || ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0)))
                        && (g_CardInfo.cExistState != 1)
                        && (g_WorkInfo.cPayDisPlayStatus == 0)) {
                    if ((mLastX > 750) && (mLastX < 1150)
                            && (mLastY > 180) && (mLastY < 840)) {
                        Log.e(TAG, "点击到人脸识别开关");

                        if ((g_ExtDisplay != null) && (g_CardInfo.cExistState != 1) && (g_LocalInfo.cFaceModeFlag == 0)) {
                            //cardPreActivity.openAllCamera();
                            cardPreActivity.ShowFaceDetecte();
                            FaceWorkTask.StartDetecte(true);
                        }
                    }
                }
                if ((g_WorkInfo.cInUserPWDFlag == 1) && (g_ExtDisplay != null))//开启密码键盘
                {
                    if ((mLastX > 730) && (mLastX < 870)
                            && (mLastY > 240) && (mLastY < 330)) {   //1
                        cardPreActivity.btn1.setPressed(false);
                        cardPreActivity.et_Password.setText(cardPreActivity.et_Password.getText() + "1");
                    } else if ((mLastX > 890) && (mLastX < 1050)
                            && (mLastY > 240) && (mLastY < 330)) {  //2
                        cardPreActivity.btn2.setPressed(false);
                        cardPreActivity.et_Password.setText(cardPreActivity.et_Password.getText() + "2");
                    } else if ((mLastX > 1070) && (mLastX < 1250)
                            && (mLastY > 240) && (mLastY < 330)) {  //3
                        cardPreActivity.btn3.setPressed(false);
                        cardPreActivity.et_Password.setText(cardPreActivity.et_Password.getText() + "3");
                    } else if ((mLastX > 730) && (mLastX < 870)
                            && (mLastY > 330) && (mLastY < 420)) {  //4
                        cardPreActivity.btn4.setPressed(false);
                        cardPreActivity.et_Password.setText(cardPreActivity.et_Password.getText() + "4");
                    } else if ((mLastX > 890) && (mLastX < 1050)
                            && (mLastY > 330) && (mLastY < 420)) {  //5
                        cardPreActivity.btn5.setPressed(false);
                        cardPreActivity.et_Password.setText(cardPreActivity.et_Password.getText() + "5");
                    } else if ((mLastX > 1070) && (mLastX < 1250)
                            && (mLastY > 330) && (mLastY < 420)) {  //6
                        cardPreActivity.btn6.setPressed(false);
                        cardPreActivity.et_Password.setText(cardPreActivity.et_Password.getText() + "6");
                    } else if ((mLastX > 730) && (mLastX < 870)
                            && (mLastY > 450) && (mLastY < 550)) {  //7
                        cardPreActivity.btn7.setPressed(false);
                        cardPreActivity.et_Password.setText(cardPreActivity.et_Password.getText() + "7");
                    } else if ((mLastX > 890) && (mLastX < 1050)
                            && (mLastY > 450) && (mLastY < 550)) {  //8
                        cardPreActivity.btn8.setPressed(false);
                        cardPreActivity.et_Password.setText(cardPreActivity.et_Password.getText() + "8");
                    } else if ((mLastX > 1070) && (mLastX < 1250)
                            && (mLastY > 450) && (mLastY < 550)) {  //9
                        cardPreActivity.btn9.setPressed(false);
                        cardPreActivity.et_Password.setText(cardPreActivity.et_Password.getText() + "9");
                    } else if ((mLastX > 730) && (mLastX < 870)
                            && (mLastY > 560) && (mLastY < 660)) {  //取消
                        cardPreActivity.btnCancle.setPressed(false);
                        cardPreActivity.et_Password.setText("");
                        Log.e(TAG, "-----密码键盘取消------");
                    } else if ((mLastX > 890) && (mLastX < 1050)
                            && (mLastY > 560) && (mLastY < 660)) {  //0
                        cardPreActivity.btn0.setPressed(false);
                        cardPreActivity.et_Password.setText(cardPreActivity.et_Password.getText() + "0");
                    } else if ((mLastX > 1070) && (mLastX < 1250)
                            && (mLastY > 560) && (mLastY < 660)) {  //确定
                        cardPreActivity.btnConfirm.setPressed(false);
                        //获取键盘密码
                        g_WorkInfo.strUserPwd = cardPreActivity.et_Password.getText().toString();
                        Log.e(TAG, "-----密码键盘确认:" + g_WorkInfo.strUserPwd);
                        g_WorkInfo.cInUserPWDFlag = 0;
                    }
                }
                break;
        }
        return true;
    }

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    public void onBackPressed() {
        //不执行回退功能
//        long secondTime = System.currentTimeMillis();
//        if (secondTime - firstTime > 2000) {
//            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
//            firstTime = secondTime;
//        } else {
//            super.onBackPressed();
//        }
    }

//    private long clickTime=0;
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (SystemClock.uptimeMillis() - clickTime <= 1500) {
//                //如果两次的时间差＜1s，就不执行操作
//
//            } else {
//                //当前系统时间的毫秒值
//                clickTime = SystemClock.uptimeMillis();
//                Toast.makeText(MainUI.this, "再次点击退出", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        //判断是否有焦点
//        if(hasFocus && Build.VERSION.SDK_INT >= 19){
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            |View.SYSTEM_UI_FLAG_FULLSCREEN
//                            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//            );
//        }
//    }

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

    //在onResume()方法注册
    @Override
    protected void onResume() {

        Log.d(TAG, "注册");
        g_WorkInfo.cCardEnableFlag = 1;
        hideBottomUIMenu();
        CheckDayAndBusinessTotal();
        ShowTotalMoneySun();
        ShowCPUTemp();
        ShowDateTime();
        ShowShoperName();
        ShowNetState(g_WorkInfo.cNetlinkStatus);
        ShowMoneyInfo();
        super.onResume();
    }

    @Override
    protected void onPause() {

        Log.d(TAG, "注销");
        //g_WorkInfo.cCardEnableFlag=0;
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "销毁");
        if (g_UICardHandler != null) {
            g_UICardHandler.removeCallbacksAndMessages(null);
            g_UICardHandler = null;
        }
        if (timer != null)
            timer.cancel();

        if (g_ExtDisplay != null) {
            if (cardPreActivity.isShowing()) {
                cardPreActivity.dismiss();
            }
        }
        g_WorkInfo.cCardEnableFlag = 0;
        super.onDestroy();
    }

}

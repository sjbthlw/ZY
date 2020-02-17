package com.hzsun.mpos.Activity;

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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzsun.mpos.CardWork.CardBasicParaInfo;
import com.hzsun.mpos.Pos.Consume;
import com.hzsun.mpos.Pos.MoneyPoint;
import com.hzsun.mpos.Public.CircleTransform;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.R;
import com.hzsun.mpos.data.LastRecordPayInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.hzsun.mpos.Global.Global.LAN_EP_CONSUMEPOS;
import static com.hzsun.mpos.Global.Global.LAN_EP_MONEYPOS;
import static com.hzsun.mpos.Global.Global.NORUSHRECORD;
import static com.hzsun.mpos.Global.Global.OK;
import static com.hzsun.mpos.Global.Global.PhotoPath;
import static com.hzsun.mpos.Global.Global.gUIRDCardHandler;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardBasicTmpInfo;
import static com.hzsun.mpos.Global.Global.g_CardInfo;
import static com.hzsun.mpos.Global.Global.g_CommInfo;
import static com.hzsun.mpos.Global.Global.g_LastRecordPayInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_RecordInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.ByteToString;
import static com.hzsun.mpos.Public.Publicfun.GetUserName;
import static com.hzsun.mpos.Public.Utility.memcpy;
import static com.hzsun.mpos.Sound.SoundPlay.VoicePlay;
import static java.util.Arrays.fill;

public class RecordDispelActivity extends AppCompatActivity {

    private static final String TAG = "RecordDispelActivity";
    private Handler sTimerHandler;//定时器
    private Timer timer;


    public static final int RDCARD_NULL = 0;     //无卡片
    public static final int RDCARD_INMONEY = 1;  //输入金额
    public static final int RDCARD_READOK = 2;   //读卡成功
    public static final int RDCARD_PAYOK = 3;    //冲正成功
    public static final int RDCARD_PAYOKONLING = 4; //冲正成功(在线交易)
    public static final int RDCARD_ONLING = 5; //冲正在线交易
    //    public static final int RDCARD_PWDKBCLOSE = 6;//密码键盘关闭
//    public static final int RDCARD_SHOWTOTAL = 7; //显示全部金额字段
//    public static final int RDCARD_RWSTART = 8;   //卡片重读卡开始
//    public static final int RDCARD_RWEND = 9;     //卡片重读卡结束
    public static final int RDCARD_EXIT = 10;     //退出冲正界面
    public static final int RDCARD_ERR = 100;    //卡片错误

    private int sRunState;
    private int sNetlinkStatus;
    private long slngTimer;
    private ImageView iv_Userpic, iv_NetState;
    private TextView tv_ShoperName, tv_Prompt, tv_Name, tv_Paymoney, tv_Bursemoney, tv_Datetime;
    private TextView tv_Daypaysum, tv_Daypaymoney, tv_Mealpaysum, tv_Mealpaymoney, tv_Mode;

    private byte sTypeTemp;
    private int sDispelCardOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_dispel);

        hideBottomUIMenu();
        iv_NetState = (ImageView) findViewById(R.id.iv_netState);
        iv_Userpic = (ImageView) findViewById(R.id.iv_suserpic);

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

        tv_Prompt.setText("欢迎使用");
        tv_Name.setText("");
        tv_Paymoney.setText("0.00");
        tv_Bursemoney.setText("0.00");
        tv_Mode.setText(Publicfun.GetPayModeStr((short) 5));

        if (((g_CardInfo.cExistState == 1))
                || (g_WorkInfo.cRunState != 1)
                || (g_LastRecordPayInfo.cState != 1)) {
            Log.d(TAG, "卡片已存在");
            VoicePlay("go_recorddespel");
        } else {
            if (g_LastRecordPayInfo.cBusinessID != g_WorkInfo.cBusinessID) {
                Publicfun.ShowCardErrorInfo_Dispel(NORUSHRECORD);//无冲正交易记录
                return;
            }
            ShowOnLinePayInfo(g_LastRecordPayInfo, 0);
        }
        sDispelCardOK = 0;
        ShowDateTime();
        ShowShoperName();
        ShowTotalMoneySun();
        ShowNetState(g_WorkInfo.cNetlinkStatus);

        gUIRDCardHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 10:
                        CardBasicParaInfo tCardBasicInfo = (CardBasicParaInfo) msg.obj;
                        ShowCardInfo_Dispel(tCardBasicInfo, (byte) msg.what);
                        break;

                    case 100:
                        List<String> Strlist = new ArrayList<String>();
                        Strlist = (List<String>) msg.obj;
                        String strTemp = Strlist.get(0);
                        int iErrorCode = Integer.parseInt(strTemp);
                        String strErrorCode = Strlist.get(1);
                        Log.e(TAG, "失败:" + strErrorCode);
                        ShowCardErrInfo(strErrorCode, (byte) msg.what);
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
                if ((g_WorkInfo.cInDispelCardFlag == 1) && (sTimerHandler != null))
                    sTimerHandler.sendEmptyMessage(10);
            }
        };
        timer = new Timer();
        timer.schedule(task, 100, 500);
    }

    //定时器工作
    private void TimeScanWork() {
        slngTimer++;
        if (g_WorkInfo.cInDispelCardFlag == 1) {
            if (slngTimer % 30 == 0) {
                ShowDateTime();
            }
            if ((sNetlinkStatus != g_WorkInfo.cNetlinkStatus)
                    || (sRunState != g_WorkInfo.cRunState)) {
                Log.e(TAG, "网络状态发生变化:" + g_WorkInfo.cNetlinkStatus);
                sNetlinkStatus = g_WorkInfo.cNetlinkStatus;
                sRunState = g_WorkInfo.cRunState;
                ShowNetState(g_WorkInfo.cNetlinkStatus);
            }
        }
        //判断是否退出冲正界面
        if ((sDispelCardOK == 1) && (g_CardInfo.cExistState == 0))
            GotoPayCardMenu();
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

    //显示时间
    private void ShowDateTime() {
        String strTmp = Publicfun.GetFullDateWeekTime(Publicfun.toData(System.currentTimeMillis()));
        String strWeek = Publicfun.GetWeekCHNName(strTmp);
        String strDataTime = strTmp.substring(5, 10) + " " + strTmp.substring(14, 20) + " " + strWeek;
        //String strDataTime = strTmp.substring(5,20);
        tv_Datetime.setText(strDataTime);
    }

    //显示商户
    private void ShowShoperName() {
        String strShowTemp = "";
//        if((g_TerminalInfo.wDeviceType==LAN_EP_CONSUMEPOS)
//        ||(g_TerminalInfo.wDeviceType==LAN_EP_OLDCONSUMEPOS))
//        {
//            strShowTemp=GetBytesToStr(g_ShopUserInfo.cShoperName);
//        }
//        else
//        {
//            strShowTemp=GetBytesToStr(g_OperatorInfo.cOpetorName);
//        }
        tv_ShoperName.setText(strShowTemp);
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
    public void ShowUserPhoto(long lngAccountID, int iType) {

        int iResult = 0;
        if (iType == 0)    //待机状态
        {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
        } else if (iType == 1)//输入状态
        {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
        } else if (iType == 2)   //读卡成功状态
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
                    Bitmap bitmapA = Circle.transform(bitmap);
                    iv_Userpic.setImageBitmap(bitmapA);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
            }
        } else if (iType == 3)   //支付成功状态
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
                    Bitmap bitmapA = Circle.transform(bitmap);
                    iv_Userpic.setImageBitmap(bitmapA);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
            }
        } else if (iType == 4)   //失败状态
        {
            iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_fail));
        } else if (iType == 10)   //显示本地照片
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
                    Bitmap bitmapA = Circle.transform(bitmap);
                    iv_Userpic.setImageBitmap(bitmapA);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                iv_Userpic.setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.mipmap.b_head_bg));
            }
        }
    }

    //显示刷卡信息 ucType: 0 清除卡片信息 1 显示卡片界面 2 卡片刷卡失败
    private void ShowCardInfo_Dispel(CardBasicParaInfo pCardBasicInfo, byte ucType) {
        long lngTemp;
        String cShowTemp = "";

        //卡片信息赋值
        if ((ucType == sTypeTemp)
                && (pCardBasicInfo.lngAccountID == g_CardBasicTmpInfo.lngAccountID)
                && (pCardBasicInfo.lngCardID == g_CardBasicTmpInfo.lngCardID)) {
            //Log.e(TAG,"---------冲正卡信息相同，退出---------");
            return;
        }

        //Log.i(TAG,"设置卡片信息11:%s,%d,%d",g_CardBasicTmpInfo.cErrorCode,ucType,g_CardBasicTmpInfo.lngAccountID);
        g_CardInfo.ucType = (byte) ucType;
        sTypeTemp = ucType;
        g_CardBasicTmpInfo = new CardBasicParaInfo();
        try {
            g_CardBasicTmpInfo = (CardBasicParaInfo) pCardBasicInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        tv_Paymoney.setTextColor(Color.rgb(122, 185, 0));
        if (sTypeTemp == 0) {
            //卡片信息
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
            tv_Prompt.setText("欢迎使用");
            tv_Name.setText("");
            tv_Bursemoney.setText("0.00");
            tv_Paymoney.setText("0.00");
            ShowUserPhoto(0, 0);
        }
        if (sTypeTemp == 1) {
            //卡片信息
            tv_Name.setText("");
            tv_Bursemoney.setText("0.00");
            tv_Paymoney.setText("0.00");
            tv_Prompt.setText("请冲正");
        } else if (sTypeTemp == 2) {
            //卡片信息
            byte[] accName = GetUserName(g_CardBasicTmpInfo.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
                //cShowTemp = new String(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Name.setText(cShowTemp);
            ShowUserPhoto(g_CardBasicTmpInfo.lngAccountID, 2);//显示照片

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngInPayMoney / 100, g_CardBasicTmpInfo.lngInPayMoney % 100);
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
            tv_Prompt.setText("请确认冲正信息");
            ShowTotalMoneySun();

        } else if (sTypeTemp == 3) //显示卡片交易冲正成功
        {
            //卡片信息
            byte[] accName = GetUserName(g_CardBasicTmpInfo.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
                //cShowTemp = new String(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Name.setText(cShowTemp);

            //显示照片
            ShowUserPhoto(g_CardBasicTmpInfo.lngAccountID, 3);

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
            tv_Prompt.setText("冲正成功");
            sDispelCardOK = 1;
            ShowTotalMoneySun();
            //判断冲正是否结束
            if ((g_WorkInfo.IsRecordDispelOver == 1) && (g_CardInfo.cExistState == 0)) {
                Log.i(TAG, "冲正结束，退出冲正界面");
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        GotoPayCardMenu();
                    }
                }, 2000);
            }
        } else if (sTypeTemp == 4)   //显示在线交易冲正成功
        {
            //卡片信息
            byte[] accName = GetUserName(g_CardBasicTmpInfo.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
                //cShowTemp = new String(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Name.setText(cShowTemp);

            //显示照片
            ShowUserPhoto(g_CardBasicTmpInfo.lngAccountID, 10);

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
            tv_Prompt.setText("在线冲正成功");
            VoicePlay("recorddispel_ok");
            ShowTotalMoneySun();
            Log.i(TAG, "冲正结束，退出冲正界面");
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    GotoPayCardMenu();
                }
            }, 2000);
        } else if (sTypeTemp == 5)   //显示在线交易冲正信息
        {
            //在线冲正信息
            byte[] accName = GetUserName(g_CardBasicTmpInfo.cAccName);
            try {
                cShowTemp = ByteToString(accName, "GB2312");
                //cShowTemp = new String(accName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            tv_Name.setText(cShowTemp);

            //显示照片
            ShowUserPhoto(g_CardBasicTmpInfo.lngAccountID, 10);

            cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngInPayMoney / 100, g_CardBasicTmpInfo.lngInPayMoney % 100);
            tv_Paymoney.setText(cShowTemp);

            if (g_CardBasicTmpInfo.lngWorkBurseMoney >= 0) {
                cShowTemp = String.format("%d.%02d", g_CardBasicTmpInfo.lngWorkBurseMoney / 100, g_CardBasicTmpInfo.lngWorkBurseMoney % 100);
                tv_Bursemoney.setText(cShowTemp);
            } else {
                lngTemp = -g_CardBasicTmpInfo.lngWorkBurseMoney;
                cShowTemp = String.format("-%d.%02d", lngTemp / 100, lngTemp % 100);
                tv_Bursemoney.setText(cShowTemp);
            }
            g_WorkInfo.cReCordDispelState = 1;
            tv_Prompt.setTextColor(getResources().getColor(R.color.colorGreen));
            tv_Prompt.setText("请确认在线冲正信息");
            VoicePlay("chongzhengkaishi");

            ShowTotalMoneySun();
        } else if (sTypeTemp == 10) {
            tv_Prompt.setText("无冲正交易记录");
            tv_Name.setText("");
            tv_Bursemoney.setText("0.00");
            tv_Paymoney.setText("0.00");

            //判断冲正是否结束
            if (g_WorkInfo.IsRecordDispelOver == 1) {
                Log.i(TAG, "冲正结束，退出冲正界面");
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        GotoPayCardMenu();
                    }
                }, 2000);
            }
        }
    }

    //显示错误信息
    public void ShowCardErrInfo(String strErrorInfo, byte ucType) {
        sTypeTemp = ucType;
        tv_Prompt.setTextColor(Color.RED);
        tv_Prompt.setText(strErrorInfo);
        ShowUserPhoto(0, 0);
        Log.i(TAG, "冲正结束，退出冲正界面");
        new Handler().postDelayed(new Runnable() {
            public void run() {
                GotoPayCardMenu();
            }
        }, 2000);
    }

    //显示冲正信息
    public void ShowOnLinePayInfo(LastRecordPayInfo pLastRecordPayInfo, int cType) {
        CardBasicParaInfo pCardBasicInfo = new CardBasicParaInfo();
        memcpy(pCardBasicInfo.cAccName, pLastRecordPayInfo.cAccName, (pCardBasicInfo.cAccName.length));
        memcpy(pCardBasicInfo.cCardPerCode, pLastRecordPayInfo.cPerCode, (pCardBasicInfo.cCardPerCode.length));
        pCardBasicInfo.lngInPayMoney = pLastRecordPayInfo.lngPayMoney;
        pCardBasicInfo.lngWorkBurseMoney = pLastRecordPayInfo.lngBurseMoney;
        pCardBasicInfo.lngManageMoney = pLastRecordPayInfo.lngManageMoney;
        pCardBasicInfo.lngPriMoney = pLastRecordPayInfo.lngPriMoney;

        ShowCardInfo_Dispel(pCardBasicInfo, (byte) 5);
    }

    //进入刷卡交易界面
    private void GotoPayCardMenu() {
        if (g_WorkInfo.cInDispelCardFlag == 1)//置冲正标记
        {
            Log.i(TAG, "进入刷卡交易界面");
            g_WorkInfo.cInDispelCardFlag = 0;
            //startActivity(new Intent(this, CardActivity.class));
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int cResult = 0;
        int KeyValue;
        KeyValue = event.getKeyCode();
        //Log.e(TAG,"KeyValue:"+KeyValue);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            event.startTracking();
            if (event.getRepeatCount() == 0) {
                //shortPress = true;
                Log.e(TAG, "---------KeyValue按下:" + KeyValue);
            } else {
                return true;
            }
            if ((((KeyValue >= KeyEvent.KEYCODE_0) && (KeyValue <= KeyEvent.KEYCODE_9))
                    || (KeyValue == KeyEvent.KEYCODE_PERIOD)) && (g_LocalInfo.cKeyLockState == 1)) {
                Log.i(TAG, "键盘已锁定");
                Publicfun.ShowErrorStrDialog(getApplicationContext(), "键盘已锁定,请解锁！");
                return true;
            }
        }
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                Log.e(TAG, "----------按下修改键----------");
                g_WorkInfo.cInUserPWDFlag = 0;//取消用户密码键盘
                g_WorkInfo.cStartReWDialogFlag = 0;//取消重写卡
                GotoPayCardMenu();
                break;
            case KeyEvent.KEYCODE_ENTER:

                if ((g_WorkInfo.IsRecordDispelOver == 0) && (g_WorkInfo.IsDispelRecord == 1)) {
                    Log.i(TAG, "进入销帐写卡");
                    g_WorkInfo.cStartWCardFlag = 1;
                    //判断设备类型
                    if (g_StationInfo.iStationClass == LAN_EP_CONSUMEPOS) {
                        Log.i(TAG, "以太网电子现金消费机冲正");
                        cResult = Consume.Consume_WorkReverCheckOut();
                    } else if (g_StationInfo.iStationClass == LAN_EP_MONEYPOS) {
                        Log.i(TAG, "以太网电子现金充值机");
                        cResult = MoneyPoint.MoneyPoint_ReverCheckOut();
                    }
                    if (cResult == OK) {
                        VoicePlay("recorddispel_ok");
                        Publicfun.ShowCardInfo_Dispel(g_CardBasicInfo, 3);//显示
                    } else {
                        Publicfun.ShowCardErrorInfo_Dispel(cResult);
                        VoicePlay("recorddispel_error");
                        Log.i(TAG, "冲正交易失败:" + cResult);
                    }
                    g_WorkInfo.IsRecordDispelOver = 1;
                    g_WorkInfo.IsDispelRecord = 0;
                    g_WorkInfo.cStartWCardFlag = 0;
                }
                if (g_WorkInfo.cReCordDispelState == 1) {
                    Log.d(TAG, "发送冲正信息");
                    VoicePlay("zhengzaichongzheng");
                    g_CommInfo.lngSendComStatus |= 0x00000080;//在线冲正
                    g_CommInfo.cLastRecDisInfoStatus = 1;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
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

    @Override
    protected void onResume() {

        Log.i(TAG, "注册");
        g_WorkInfo.cCardEnableFlag = 1;
        g_WorkInfo.cInDispelCardFlag = 1;//置冲正标记
        g_WorkInfo.IsRecordDispelOver = 0;
        g_WorkInfo.IsDispelRecord = 0;
        fill(g_CardInfo.bCardDispelSerID, (byte) 0x00);
        tv_Mode.setText(Publicfun.GetPayModeStr((short) 5));
        super.onResume();
    }

    //onPause()方法注销
    @Override
    protected void onPause() {

        Log.i(TAG, "注销");
        //g_WorkInfo.cCardEnableFlag=0;
        g_WorkInfo.cInDispelCardFlag = 0;//清零冲正标记
        g_WorkInfo.IsDispelRecord = 0;
        g_WorkInfo.cReCordDispelState = 0;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "销毁");
        //g_WorkInfo.cCardEnableFlag=0;
        g_WorkInfo.cInDispelCardFlag = 0;//清零冲正标记
        g_WorkInfo.IsDispelRecord = 0;
        g_WorkInfo.cReCordDispelState = 0;

        if (gUIRDCardHandler != null) {
            gUIRDCardHandler.removeCallbacksAndMessages(null);
            gUIRDCardHandler = null;
        }
        if (sTimerHandler != null) {
            sTimerHandler.removeCallbacksAndMessages(null);
            sTimerHandler = null;
        }
        if (timer != null)
            timer.cancel();
        super.onDestroy();
    }

}

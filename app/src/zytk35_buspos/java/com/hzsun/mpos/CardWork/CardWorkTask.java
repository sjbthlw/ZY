package com.hzsun.mpos.CardWork;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.hzsun.mpos.FaceApp.FaceWorkTask;
import com.hzsun.mpos.Pos.Consume;
import com.hzsun.mpos.Pos.Subsidy;
import com.hzsun.mpos.Public.Keyoperpro;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.SerialWork.SerialWorkTask;
import com.hzsun.mpos.Sound.SoundPlay;
import com.hzsun.mpos.data.WasteBooksRW;
import com.hzsun.mpos.thread.soundPlayThread;

import java.util.Arrays;

import static com.hzsun.mpos.Activity.CardActivity.CARD_INMONEY;
import static com.hzsun.mpos.Activity.CardActivity.CARD_NULL;
import static com.hzsun.mpos.Activity.CardActivity.CARD_READOK;
import static com.hzsun.mpos.Activity.CardActivity.ONLING_PAYOK;
import static com.hzsun.mpos.Activity.CardActivity.QRCODE_SHOW;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_EXIT;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_NULL;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_ONLING;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_PAYOKONLING;
import static com.hzsun.mpos.Activity.RecordDispelActivity.RDCARD_READOK;
import static com.hzsun.mpos.CardWork.CardApp.GetCardAllInfoData;
import static com.hzsun.mpos.Global.Global.CARD_INVALIED;
import static com.hzsun.mpos.Global.Global.LAN_EP_CONSUMEPOS;
import static com.hzsun.mpos.Global.Global.LAN_EP_MONEYPOS;
import static com.hzsun.mpos.Global.Global.MAXBOOKSCOUNT;
import static com.hzsun.mpos.Global.Global.MEMORY_FAIL;
import static com.hzsun.mpos.Global.Global.NORUSHRECORD;
import static com.hzsun.mpos.Global.Global.OK;
import static com.hzsun.mpos.Global.Global.TESTMODETIMEER;
import static com.hzsun.mpos.Global.Global.g_ThirdCodeResultInfo;
import static com.hzsun.mpos.Global.Global.g_CardAttr;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardInfo;
import static com.hzsun.mpos.Global.Global.g_CommInfo;
import static com.hzsun.mpos.Global.Global.g_LastRecordPayInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_OnlinePayInfo;
import static com.hzsun.mpos.Global.Global.g_RecordInfo;
import static com.hzsun.mpos.Global.Global.g_ShopQRCodeInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WasteBookInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Pos.Consume.Consume_OnlingCheckOut;
import static com.hzsun.mpos.Public.Publicfun.RelayControlDeal;
import static com.hzsun.mpos.Public.Publicfun.ShowQRCodeInfo;
import static com.hzsun.mpos.Public.Publicfun.ShowQRErrorInfo;
import static com.hzsun.mpos.Public.Utility.memcmp;
import static com.hzsun.mpos.Public.Utility.memcpy;
import static com.hzsun.mpos.Sound.SoundPlay.VoicePlay;
import static java.util.Arrays.fill;


public class CardWorkTask {

    private static final String TAG = "CardWorkTask";

    private CardWorkThread ThreadCardWork;
    private CardCheckThread ThreadCardCheck;
    private RelayContrlThread ThreadRelayContrl;
    public static Handler CardSendHandler;
    public static Handler RelaySendHandler;
    public static final int EVT_RelayControl = 10;      //继电器控制
    public static final int EVT_CardCheckOut = 200;     //键盘按键支付

    private int s_TestModeTimer;
    private int s_PaydisCount = 0;
    private int s_InitRFCount = 0;

    public CardWorkTask() {
        Log.d(TAG, "CardWorkTask: 构造");
    }

    public void Init() {
        Log.d(TAG, "CardWorkTask 初始化");
        ThreadCardWork = new CardWorkThread();
        ThreadCardWork.start();

        ThreadCardCheck = new CardCheckThread();
        ThreadCardCheck.start();

        ThreadRelayContrl = new RelayContrlThread();
        ThreadRelayContrl.start();
    }
//卡片确定处理
    class CardCheckThread extends Thread {

        private boolean isStart = false;

        public boolean GetStatus() {
            return isStart;
        }

        public void StopThread() {
            isStart = false;
            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }

        @Override
        public void run() {
            super.run();
            isStart = true;
            if (true) {
                Looper.prepare();

                CardSendHandler = new Handler() {

                    public void handleMessage(Message msg) {
                        switch (msg.what) {

                            case EVT_CardCheckOut:
                                Log.e(TAG, "按键确定");
                                if (g_WorkInfo.cTestState == 0){
                                    Keyoperpro.CheckOut();
                                }
                                break;
                        }
                        super.handleMessage(msg);
                    }
                };
                Looper.loop();
            }
        }
    }

    //继电器处理
    class RelayContrlThread extends Thread {

        private boolean isStart = false;

        public boolean GetStatus() {
            return isStart;
        }

        public void StopThread() {
            isStart = false;
            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }

        @Override
        public void run() {
            super.run();
            isStart = true;
            if (true) {
                Looper.prepare();
                RelaySendHandler = new Handler() {

                    public void handleMessage(Message msg) {
                        switch (msg.what) {

                            case EVT_RelayControl:
                                Log.e(TAG, "继电器动作");
                                RelayControlDeal( g_LocalInfo.iRelayState, g_LocalInfo.iRelayMode, g_LocalInfo.iRelayOperTime, g_LocalInfo.iRelayOperCnt);
                                break;
                        }
                        super.handleMessage(msg);
                    }
                };
                Looper.loop();
            }
        }
    }

    

    //卡片业务线程
    class CardWorkThread extends Thread {

        private boolean isStart = true;

        @Override
        public void run() {
            super.run();

            while (isStart) {
                ScanCardProcess();
                try {
                    Thread.sleep(200);//延时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //卡片扫描处理
    public void ScanCardProcess() {

        //判断进入刷卡交互界面
        if ((g_WorkInfo.cCardEnableFlag == 0)
                || (g_WorkInfo.cInUserPWDFlag == 1)
                || (g_WorkInfo.cStartWCardFlag == 1)
                || (g_WorkInfo.cStartReWDialogFlag == 1)
                || (g_WorkInfo.cBackActivityState != 0)) {
            return;
        }

        if ((g_CommInfo.cQueryCardInfoStatus == 1) && (g_WorkInfo.cRunState == 1))    //注意：防止卡片扫描线程与网络写卡线程冲突(如果冲突将读写失败)做好线程互斥
        {
            return;
        }
        if ((g_WorkInfo.cRunState == 1 || g_WorkInfo.cRunState == 2)) {
            if (g_WorkInfo.cInDispelCardFlag == 0) {
                //卡片正常交易
                CardNormalPay();
            } else {
                //卡片交易冲正
                if (g_WorkInfo.cReCordDispelState != 1)//已经在线冲正 不读卡
                    CardRecordDispel();
            }
        }
    }

    //卡片正常交易
    public void CardNormalPay() {
        int cResult;

        s_InitRFCount++;
        long lngStart = (System.currentTimeMillis());
        cResult = CardApp.ReadCardAttrInfo(g_CardAttr);
        if (cResult == 0) {
            //Log.d(TAG,"卡片正常交易-读卡号成功");
            if (g_LocalInfo.cDockposFlag == 1) {
                if (!SerialWorkTask.isEnableScan()) {
                    return;
                }
            }
            g_CardInfo.cExistState = 1;
            g_CardInfo.cCardType = g_CardAttr.cCardType;
            g_WorkInfo.lngPowerSaveCnt=System.currentTimeMillis();
            System.arraycopy(g_CardAttr.cCardSID, 0, g_CardInfo.bCardSerialID, 0, 4);
        } else {
            //Log.d(TAG,"无卡片存在");
            if ((g_WorkInfo.cStartWCardFlag == 1) || (g_WorkInfo.cStartReWDialogFlag == 1)) {
                Log.e(TAG, "----------------写卡进行中------------");
                return;
            }
            if (!FaceWorkTask.isDetecteFlag()) {
                Publicfun.LedShow(1, 0);
            }
			if (s_InitRFCount > 100) {
                s_InitRFCount = 0;
                g_Nlib.ReaderClose();
                Publicfun.RF_ChipInit();
            }
            g_WorkInfo.cShowErrCount = 0;
            g_WorkInfo.cCheckOutState = 0;
            g_WorkInfo.cSubsidyState = 0;
            g_WorkInfo.cYearCheckState = 0;
            g_CardInfo.cExistState = 0;
            g_CardInfo.cAuthenState = 0;
            g_CardInfo.cCardStatus = 0;
            g_CardInfo = new CardInfo();
            g_CardBasicInfo = new CardBasicParaInfo();
            if ((g_WorkInfo.cOptionMark != 0) && (g_WorkInfo.lngTotalMoney > 0)) {
                g_CardBasicInfo.lngInPayMoney = g_WorkInfo.lngTotalMoney;            //输入金额
            } else {
                g_CardBasicInfo.lngInPayMoney = g_WorkInfo.lngPaymentMoney;            //输入金额
            }
            if (g_WorkInfo.cKeyDownState == 0) {
                if (g_WorkInfo.cPayDisPlayStatus != 1) {
                    s_PaydisCount = 0;
                    if (g_LocalInfo.cInputMode == 3)   //定额模式
                    {
                        Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_INMONEY);//显示
                    } else {
                        Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_NULL);//显示
                    }
                } else {
                    //延时显示二维码交易信息后清除
                    s_PaydisCount++;
                    if (s_PaydisCount > g_LocalInfo.iPayShowTime * 5) {
                        s_PaydisCount = 0;
                        g_WorkInfo.cPayDisPlayStatus = 0;
                    }
                }
            }
            if (g_WorkInfo.cSelfPressOk == 0) {
                if (g_WorkInfo.cKeyDownState == 0) {
                    g_WorkInfo.lngTotalMoney = 0;
                }
            }
            return;
        }
        //Log.d(TAG,"判断卡号是否和内存中一致");
        cResult = memcmp(g_CardInfo.bCardSerialID, g_CardInfo.bCardSerTempID, 4);
        if (cResult == 0) {
            if (g_WorkInfo.cCheckOutState == 0) {
                if ((g_WorkInfo.cOptionMark != 0) && (g_WorkInfo.lngTotalMoney > 0)) {
                    g_CardBasicInfo.lngInPayMoney = g_WorkInfo.lngTotalMoney;            //输入金额
                } else {
                    g_CardBasicInfo.lngInPayMoney = g_WorkInfo.lngPaymentMoney;            //输入金额
                }
                if (g_CardInfo.cAuthenState == 0) {
                } else {
                    Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_READOK);//显示
                }
                if ((g_WorkInfo.cTestState == 0) && (g_CardInfo.cAuthenState > 0))//卡片信息读取成功
                {
                    //Log.e(TAG,"==============================读卡信息或认证成功,卡号相同返回===================================");
                    return;
                } else if ((g_WorkInfo.cTestState == 1) && (g_CardInfo.cAuthenState > 0)) {
                    s_TestModeTimer++;
                    if (s_TestModeTimer < TESTMODETIMEER * 5) {
                        //Log.d(TAG,"==============================测试模式下,等待测试间隔:%d 后,卡号相同返回===================================",s_TestModeTimer);
                        return;
                    } else {
                        s_TestModeTimer = 0;
                    }
                }
            } else {
                if ((g_WorkInfo.cTestState == 0) && (g_CardInfo.cAuthenState > 0))//卡片信息读取成功
                {
                    //Log.d(TAG,"==============================交易结束后,卡号相同返回===================================");
                    return;
                }
            }
        } else {
            memcpy(g_CardInfo.bCardSerTempID, g_CardInfo.bCardSerialID, 8);
        }

        Log.d(TAG, "====================卡片校验开始======================");
        Publicfun.LedShow(1, 1);
        //获取卡片所有的信息
        g_CardBasicInfo.cReWriteCardFlag = 0;
        cResult = GetCardAllInfoData(g_CardBasicInfo);
        Log.e(TAG, String.format("=========读卡全部数据时长:%d==========", (System.currentTimeMillis() - lngStart)));
        if (cResult == 0) {
            Log.d(TAG, "==================获取卡片所有的信息成功======================");
            g_WorkInfo.cOtherQRFlag = 3;
            if (g_LocalInfo.cDockposFlag == 1 && SerialWorkTask.getTradeState() == SerialWorkTask.STATE_QUERYING) {
                SerialWorkTask.onQueryDone(3);
                return;
            }
            //断点恢复流水
            if (g_CardBasicInfo.cReWriteCardFlag != 0) {
                Log.d(TAG, "==================断点恢复流水======================");
                cResult = Consume.WriteAllCardPayRecord(g_CardBasicInfo.cReWriteCardFlag);
                if (cResult != 0) {
                    Log.d(TAG, "记录断点恢复流水失败");
                    Publicfun.ShowCardErrorInfo(cResult);
                }
                g_CardBasicInfo.cReWriteCardFlag = 0;
                return;
            }
            //判断代理号,客户序号
            if ((g_SystemInfo.cAgentID != g_CardBasicInfo.cAgentID) || (g_SystemInfo.iGuestID != g_CardBasicInfo.iGuestID)) {
                g_CardInfo.cAuthenState = 0;
                Publicfun.ShowCardErrorInfo(4);
                return;
            } else {
                g_CardInfo.cAuthenState = 1;  //卡片信息读取成功
            }
            //读取身份参数
            Log.d(TAG, "消费身份:" + g_CardBasicInfo.cStatusID);

            if ((g_WorkInfo.cOptionMark != 0) && (g_WorkInfo.lngTotalMoney > 0)) {
                g_CardBasicInfo.lngInPayMoney = g_WorkInfo.lngTotalMoney;            //输入金额
            } else {
                g_CardBasicInfo.lngInPayMoney = g_WorkInfo.lngPaymentMoney;            //输入金额
            }
            //联机校验
            if ((g_WorkInfo.cRunState == 1) && (g_WorkInfo.cTestState == 0)) {
                Log.d(TAG, "============联网卡户校验开始:===============" + g_CommInfo.cRecvWaitState);
                //联机取卡片信息
                g_CommInfo.lngSendComStatus |= 0x00000010;
                g_CommInfo.cQueryCardInfoStatus = 1;
            }
            //脱机校验
            else {
                Log.d(TAG, "脱机校验");
                g_WorkInfo.cInfoStepState = 1;
                cResult = SetOfflineCheckCardInfo(g_CardBasicInfo.lngCardID);
                g_WorkInfo.cInfoStepState = 0;
                if (cResult == OK) {
                    Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_READOK);//显示
                    Publicfun.LedShow(1, 0);
                    Publicfun.LedShow(3, 1);

                    if ((g_WorkInfo.cSelfPressOk == 1) || ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0))) {
                        Log.d(TAG, "脱机校验 自动结帐");
                        Keyoperpro.CheckOut();
                    }
                } else {
                    g_CardInfo.cAuthenState = 0;
                    Publicfun.ShowCardErrorInfo(cResult);
                }
            }
        } else {
            Log.d(TAG, "读卡片错误信息:" + cResult);
            g_WorkInfo.cShowErrCount++;
            if (g_WorkInfo.cShowErrCount > 2) {
                if (g_WorkInfo.cPayDisPlayStatus == 1) {
                    if (g_LocalInfo.cInputMode == 3)   //定额模式
                    {
                        Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_INMONEY);//显示
                    } else {
                        Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_NULL);//显示
                    }
                }
                g_WorkInfo.cShowErrCount = 0;
                g_WorkInfo.cPayDisPlayStatus = 0;
                s_PaydisCount = 0;
                Publicfun.ShowCardErrorInfo(cResult);
            }
            return;
        }
    }

    //卡片交易冲正
    public void CardRecordDispel() {
        int cResult;

        cResult = CardApp.ReadCardAttrInfo(g_CardAttr);
        if (cResult == OK) {
            Publicfun.LedShow(1, 1);
            g_CardInfo.cExistState = 1;
            g_CardInfo.cCardType = g_CardAttr.cCardType;
            g_WorkInfo.lngPowerSaveCnt=System.currentTimeMillis();
            System.arraycopy(g_CardAttr.cCardSID, 0, g_CardInfo.bCardSerialID, 0, 8);
        } else {
            //Log.d(TAG,"无卡片存在");
            if ((g_WorkInfo.cStartWCardFlag == 1) || (g_WorkInfo.cStartReWDialogFlag == 1)) {
                Log.e(TAG, "----------------写卡进行中------------");
                return;
            }
            g_CardInfo = new CardInfo();
            g_CardBasicInfo = new CardBasicParaInfo();
            g_WorkInfo.IsDispelRecord = 0;
            Publicfun.ShowCardInfo_Dispel(g_CardBasicInfo, RDCARD_NULL);//显示
            Publicfun.LED_AllClear();
            return;
        }
        //Log.d(TAG,"判断卡号是否和内存中一致");
        if (Arrays.equals(g_CardInfo.bCardSerialID, g_CardInfo.bCardDispelSerID)) {
            return;
        } else {
            System.arraycopy(g_CardInfo.bCardSerialID, 0, g_CardInfo.bCardDispelSerID, 0, 8);
        }

        Log.d(TAG, "====================冲正模式卡片校验开始======================");
        //判断交易流水数量
        if (g_RecordInfo.lngPaymentRecordID - g_RecordInfo.lngPaymentSendID >= (MAXBOOKSCOUNT - 20)) {
            Publicfun.ShowCardErrorInfo_Dispel(99);
            return;
        }
        // 时间合法性检查
        //Publicfun.GetCurrDateTime(cCurDateTime);    //设置当前系统时间日期
        //SetCurDateTime(cCurDateTime);
        //获取卡片所有的信息
        cResult = GetCardAllInfoData(g_CardBasicInfo);
        if (cResult != 0) {
            g_CardInfo.cAuthenState = 0;
            Publicfun.ShowCardErrorInfo_Dispel(cResult);
            return;
        } else {
            Log.d(TAG, "==================获取卡片所有的信息成功======================");
            g_CardInfo.cAuthenState = 1;
        }

        //再次验证卡片有效性
        cResult = CardApp.CheckCardInfoAgain();
        if (cResult == OK) {
            Publicfun.LedShow(1, 0);
            Publicfun.LedShow(3, 1);
            Log.d(TAG, "冲正卡户验证有效");
            g_WorkInfo.cShowErrCount = 0;
            g_CardInfo.cAuthenState = 2;
            //记录有效的卡片卡号
            System.arraycopy(g_CardInfo.bCardSerialID, 0, g_CardInfo.bCardSerRecordID, 0, 4);
        } else {
            g_CardInfo.cAuthenState = 0;
            Publicfun.ShowCardErrorInfo_Dispel(cResult);
            return;
        }

        //判断末笔是不是这个账号消费的
        if ((g_LastRecordPayInfo.cState == 1) && (g_WorkInfo.cRunState == 1)) {
            if (g_CardBasicInfo.lngAccountID == g_LastRecordPayInfo.lngAccountID) {
                if ((g_LastRecordPayInfo.cBusinessID == g_WorkInfo.cBusinessID)) {
                    g_WorkInfo.IsDispelRecord = 2;

                    CardBasicParaInfo pCardBasicInfo = new CardBasicParaInfo();
                    try {
                        pCardBasicInfo = (CardBasicParaInfo) g_CardBasicInfo.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    memcpy(pCardBasicInfo.cAccName, g_LastRecordPayInfo.cAccName, (pCardBasicInfo.cAccName.length));
                    memcpy(pCardBasicInfo.cCardPerCode, g_LastRecordPayInfo.cPerCode, (pCardBasicInfo.cCardPerCode.length));
                    pCardBasicInfo.lngInPayMoney = g_LastRecordPayInfo.lngPayMoney;
                    pCardBasicInfo.lngWorkBurseMoney = g_LastRecordPayInfo.lngBurseMoney;
                    pCardBasicInfo.lngManageMoney = g_LastRecordPayInfo.lngManageMoney;
                    pCardBasicInfo.lngPriMoney = g_LastRecordPayInfo.lngPriMoney;
                    Log.d(TAG, "显示冲正在线交易信息");
                    Log.d(TAG, String.format("pCardBasicInfo.lngInPayMoney:%d,pCardBasicInfo.lngWorkBurseMoney:%d,pCardBasicInfo.lngManageMoney:%d",
                            pCardBasicInfo.lngInPayMoney,pCardBasicInfo.lngWorkBurseMoney,pCardBasicInfo.lngManageMoney));
                    Publicfun.ShowCardInfo_Dispel(pCardBasicInfo, RDCARD_ONLING);//显示
                    return;
                }
            }
        }

        Log.d(TAG, "卡片论证成功,查找流水");
        cResult = FindDispelRecord();
        if (cResult == OK) {
            Log.d(TAG, "查找流水成功");
            g_WorkInfo.IsDispelRecord = 1;
            VoicePlay("card_dispel");
        } else {
            Log.d(TAG, String.format("查找流水失败:%d", cResult));
            Publicfun.ShowCardErrorInfo_Dispel(NORUSHRECORD);
            g_WorkInfo.IsRecordDispelOver = 1;
            g_WorkInfo.IsDispelRecord = 0;
            Publicfun.ShowCardInfo_Dispel(g_CardBasicInfo, RDCARD_EXIT);//退出冲正界面
            return;
        }

        Log.d(TAG, "===================冲正发送卡片所有的信息到界面======================");
        Log.d(TAG, String.format("g_WorkInfo.lngReManageMoney:%d,g_WorkInfo.lngReWorkPayMoney:%d,g_WorkInfo.lngReChasePayMoney:%d",
                g_WorkInfo.lngReManageMoney, g_WorkInfo.lngReWorkPayMoney, g_WorkInfo.lngReChasePayMoney));

        g_CardBasicInfo.lngInPayMoney = g_WorkInfo.lngReManageMoney
                + g_WorkInfo.lngReWorkPayMoney
                + g_WorkInfo.lngReChasePayMoney;

        g_CardBasicInfo.lngManageMoney = g_WorkInfo.lngReManageMoney;//管理费金额

        Log.d(TAG, String.format("g_CardBasicInfo.lngInPayMoney:%d,g_CardBasicInfo.lngManageMoney:%d",
                g_CardBasicInfo.lngInPayMoney,g_CardBasicInfo.lngManageMoney));

        Publicfun.ShowCardInfo_Dispel(g_CardBasicInfo, RDCARD_READOK);//显示
    }

    //寻找冲正流水
    private int FindDispelRecord() {
        int cResult = 0;
        long lngRecordID;
        int cWorkBurseID, cChaseBurseID;
        int i;
        long lngTemp;
        int cBusinessID;        //营业分组时段序号		1字节
        int wShopUserID;    //商户序号		2字节
        long lngCardID;        //卡内编号		3字节
        int cBurseID;            //电子现金钱包序号			1字节
        int wBurseSID;        //钱包流水号		2字节
        int wSubSID;        //补助流水号		2字节
        long lngDevPayRecordID;    //设备终端流水号		4字节
        long lngPayRecordID;     //终端流水号		3字节
        int cPayType;            //交易类型		1字节
        long lngPayMoney;        //交易金额		3字节
        int iYear, iMonth, iDay;
        byte[] cLastPayDate = new byte[3];
        byte[] bTempContext = new byte[264 * 10];

        lngRecordID = g_WasteBookInfo.WriterIndex;
        Log.d(TAG, "已记录流水lngRecordID=" + lngRecordID);

        //工作钱包
        cWorkBurseID = g_StationInfo.cWorkBurseID;
        //追扣钱包
        cChaseBurseID = g_StationInfo.cChaseBurseID;
        Log.d(TAG, String.format("工作钱包:%d, 追扣钱包:%d", cWorkBurseID, cChaseBurseID));

        g_WorkInfo.lngReManageMoney = 0;             //冲正管理费金额
        g_WorkInfo.lngReWorkPayMoney = 0;             //冲正工作钱包交易金额
        g_WorkInfo.lngReChasePayMoney = 0;             //冲正追扣钱包交易金额

        //寻找卡片末笔交易流水
        while (true) {
            if ((lngRecordID <= 0) || ((g_WasteBookInfo.WriterIndex - lngRecordID) >= 200)) {
                Log.d(TAG, "退出查询,未找到相应冲正流水");
                return 1;
            }
            int cPayRecordNum = 1;
            cResult = WasteBooksRW.ReadPayRecordsData(bTempContext, lngRecordID, cPayRecordNum);
            if (cResult == OK) {
                i = 0;
                //站点号	2字节
                i = i + 2;

                //设备终端流水号		4字节
                lngDevPayRecordID = (bTempContext[i++] & 0xff);
                lngDevPayRecordID += (bTempContext[i++] & 0xff) * 256;
                lngDevPayRecordID += (bTempContext[i++] & 0xff) * 256 * 256;
                lngDevPayRecordID += ((bTempContext[i++] & 0xff) * 256 * 256 * 256);

                //终端机号			1字节
                i = i + 1;
                //营业分组时段序号		1字节
                cBusinessID = (bTempContext[i++] & 0xff);
                //商户序号		2字节
                wShopUserID = (bTempContext[i++] & 0xff);
                wShopUserID += (bTempContext[i++] & 0xff) * 256;
                //卡内编号		3字节
                lngCardID = (bTempContext[i++] & 0xff);
                lngCardID += (bTempContext[i++] & 0xff) * 256;
                lngCardID += (bTempContext[i++] & 0xff) * 256 * 256;
                //交易钱包序号			1字节
                cBurseID = (bTempContext[i++] & 0xff);
                //钱包流水号		2字节
                wBurseSID = (bTempContext[i++] & 0xff);
                wBurseSID += (bTempContext[i++] & 0xff) * 256;
                //补助流水号		2字节
                wSubSID = (bTempContext[i++] & 0xff);
                wSubSID += (bTempContext[i++] & 0xff) * 256;

                //终端流水号		3字节
                lngPayRecordID = (bTempContext[i++] & 0xff);
                lngPayRecordID += (bTempContext[i++] & 0xff) * 256;
                lngPayRecordID += (bTempContext[i++] & 0xff) * 256 * 256;

                //交易日期时间(年6位 月4位 日 5位 小时5位 分6位 秒6位	)4字节
                lngTemp = (bTempContext[i++] & 0xff) * 256 * 256 * 256;
                lngTemp += (bTempContext[i++] & 0xff) * 256 * 256;
                lngTemp += (bTempContext[i++] & 0xff) * 256;
                lngTemp += (bTempContext[i++] & 0xff);

                iYear = (int) ((lngTemp & 0xFC000000) >> 26);
                iMonth = (int) ((lngTemp & 0x03C00000) >> 22);
                iDay = (int) ((lngTemp & 0x003E0000) >> 17);

                cLastPayDate[0] = (byte) iYear;
                cLastPayDate[1] = (byte) iMonth;
                cLastPayDate[2] = (byte) iDay;

                //交易类型		1字节
                cPayType = (bTempContext[i++] & 0xff);
                //交易金额		2字节
                lngPayMoney = (bTempContext[i++] & 0xff);
                lngPayMoney += (bTempContext[i++] & 0xff) * 256;
                //优惠金额			3字节
                i = i + 2;
                //钱包卡余额				3字节
                i = i + 3;
                //累计交易总金额		4字节
                i = i + 4;

                if ((cBurseID != cWorkBurseID) && (cBurseID != cChaseBurseID)) {
                    Log.d(TAG, "操作钱包不符合");
                    return 1;
                }

                if (g_CardBasicInfo.lngCardID == lngCardID) {
                    Log.d(TAG, "卡内编号比较成功:" + lngCardID);
                    //判断是否是同一天
                    cResult = Publicfun.CompareStatLastDate(cLastPayDate);
                    if (cResult == 0) {
                        //判断是否是同一营业分组
                        if ((cBusinessID & 0x7f) != g_WorkInfo.cBusinessID) {
                            Log.d(TAG, "不在同一营业分组");
                            cResult = 2;
                            break;
                        }
                    } else {
                        Log.d(TAG, "不在同一天");
                        cResult = 3;
                        break;
                    }

                    //判断是否是销帐流水
                /*
                00:商务消费;30：以角为单位 60：元
                01:钱包转出；
                02:钱包转入；
                03:交易冲正；33 角  63元
                05:终端存款；35 角  65元
                06:存款冲正；36 角  66元
                07:余额复位；
                08:追扣消费；38角 68元
                09:终端圈存；
                21:信息圈存；
                22:卡户取款；52角 82元
                23:消费管理费 53角 83元
                24:消费管理费冲正； 54角 84元
                */
                    if ((cPayType == 3) || (cPayType == 33) || (cPayType == 63) //03:交易冲正；33 角  63元
                            || (cPayType == 6) || (cPayType == 36) || (cPayType == 66) //06:存款冲正；36 角  66元
                            || (cPayType == 24) || (cPayType == 54) || (cPayType == 84)) //24:消费管理费冲正； 54角 84元
                    {
                        Log.d(TAG, "流水为冲正流水不符合冲正操作");
                        cResult = 16;
                        break;
                    }

                    if ((cPayType == 00) || (cPayType == 30) || (cPayType == 60) //00:商务消费;30：角 60：元
                            || (cPayType == 8) || (cPayType == 38) || (cPayType == 68) //08:追扣消费；38角 68元
                            || (cPayType == 23) || (cPayType == 53) || (cPayType == 83) //23:消费管理费 53角 83元
                            || (cPayType == 5) || (cPayType == 35) || (cPayType == 65)) //05:终端存款；35 角  65元
                    {
                        if ((cPayType == 23) || (cPayType == 53) || (cPayType == 83))//23:消费管理费 53角 83元
                        {
                            //有管理费的交易不存在追扣流水
                            if (cPayType == 53) {
                                lngPayMoney *= 10;
                            } else if (cPayType == 83) {
                                lngPayMoney *= 100;
                            }
                            g_WorkInfo.lngReManageMoney = (int) lngPayMoney;        //管理费金额
                            g_WorkInfo.lngReManageRecordID = lngRecordID;  //被冲正工作钱包的流水号

                            Log.d(TAG, String.format("管理费金额:%d, 流水号:%d,",
                                    g_WorkInfo.lngReManageMoney, g_WorkInfo.lngReManageRecordID));

                            lngRecordID = lngRecordID - 1;
                            Log.d(TAG, "查找后续流水");
                            continue;
                        } else {
                            //充值流水(只有工作钱包)
                            if ((cPayType == 5) || (cPayType == 35) || (cPayType == 65)) //05:终端存款；35 角  65元
                            {
                                if (cBurseID != cWorkBurseID) {
                                    Log.d(TAG, "流水不符合冲正操作,退出");
                                    cResult = 1;
                                    break;
                                }
                                g_WorkInfo.lngReWorkPayMoney = (int) lngPayMoney;        //交易金额
                                Log.d(TAG, " 冲正交易金额:" + g_WorkInfo.lngReWorkPayMoney);

                                return OK;
                            } else    //消费流水
                            {
                                if ((cPayType == 00) || (cPayType == 30) || (cPayType == 60) //00:商务消费;30：角 60：元
                                        || (cPayType == 8) || (cPayType == 38) || (cPayType == 68)) //08:追扣消费；38角 68元
                                {
                                    if (cBurseID == cWorkBurseID) {
                                        if (cPayType == 30 || cPayType == 38) {
                                            lngPayMoney *= 10;
                                        } else if (cPayType == 60 || cPayType == 68) {
                                            lngPayMoney *= 100;
                                        }
                                        g_WorkInfo.lngReWorkPayMoney = (int) lngPayMoney;        //交易金额
                                        Log.d(TAG, "冲正工作钱包交易金额:" + g_WorkInfo.lngReWorkPayMoney);
                                        return OK;
                                    } else if (cBurseID == cChaseBurseID) {
                                        if (g_WorkInfo.lngReChasePayMoney != 0) {
                                            Log.d(TAG, "已经查找追扣钱包流水");
                                            return OK;
                                        }
                                        if (cPayType == 30 || cPayType == 38) {
                                            lngPayMoney *= 10;
                                        } else if (cPayType == 60 || cPayType == 68) {
                                            lngPayMoney *= 100;
                                        }
                                        g_WorkInfo.lngReChasePayMoney = (int) lngPayMoney;        //追扣交易金额
                                        Log.d(TAG, "冲正追扣钱包交易金额:" + g_WorkInfo.lngReChasePayMoney);

                                        lngRecordID = lngRecordID - 1;
                                        Log.d(TAG, "查找后续流水");
                                        continue;
                                    }
                                } else {
                                    lngRecordID = lngRecordID - 1;
                                    Log.d(TAG, "查找后续流水");
                                    continue;
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "流水不符合冲正操作，查找后续流水");
                        lngRecordID = lngRecordID - 1;
                        continue;
                    }
                } else {
                    Log.d(TAG, "卡内编号比较失败");
                    if ((g_WorkInfo.lngReChasePayMoney == 0) && (g_WorkInfo.lngReWorkPayMoney == 0)) {
                        lngRecordID = lngRecordID - 1;
                        Log.d(TAG, "查找后续流水");
                        continue;
                    } else {
                        Log.d(TAG, String.format("已经查找充正流水:%d %d", g_WorkInfo.lngReChasePayMoney, g_WorkInfo.lngReWorkPayMoney));
                        return OK;
                    }
                }
            } else {
                return MEMORY_FAIL;
            }
        }

        if ((g_WorkInfo.lngReChasePayMoney == 0) && (g_WorkInfo.lngReWorkPayMoney == 0)) {
            Log.d(TAG, "查找流水结束:" + cResult);
            return cResult;
        } else {
            Log.d(TAG, String.format("已经查找充正流水:%d %d", g_WorkInfo.lngReChasePayMoney, g_WorkInfo.lngReWorkPayMoney));
            return OK;
        }
    }

    //超时卡户校验
    public static int SetOfflineCheckCardInfo(long lngCardID) {
        int cResult = 0;
        byte cCardState = 0;
        Log.d(TAG, "超时或脱机卡户校验");
        Log.d(TAG, "黑白名单判断lngCardID:" + lngCardID);
        if (g_WorkInfo.cTestState == 0) {
            if ((g_CardInfo.cCardType != 4) && (g_CardInfo.cCardType != 5) && (g_CardInfo.cCardType != 6)) {
                //判断卡片状态,卡户状态(后4位)
                cCardState = (byte) (g_CardBasicInfo.cCardState & 0x01);
                if (cCardState == 0) {
                    Log.d(TAG, "卡片状态：" + cCardState);
                    return CARD_INVALIED;
                }
            }
            //判断设备类型
            if (g_StationInfo.iStationClass == LAN_EP_CONSUMEPOS) {
                Log.d(TAG, "判断黑白名单");
                cResult = CardPublic.ReadBWListState(lngCardID);
            } else if (g_StationInfo.iStationClass == LAN_EP_MONEYPOS) {
                cResult = OK;
            }
            if (cResult == OK) {
                Log.d(TAG, "余额复位");
                cResult = CardPublic.CardResetProcess();
                if (cResult != OK) {
                    return cResult;
                }
            } else {
                //卡片中是白名单，需要修改成黑名单
                CardPublic.ModifyCardStatus((byte) 0);
                return CARD_INVALIED;
            }
        }
        //再次验证卡片有效性
        cResult = CardApp.CheckCardInfoAgain();
        if (cResult == OK) {
            Log.d(TAG, "卡户验证有效");
            g_CardInfo.cAuthenState = 2;
            //记录有效的卡片卡号
            System.arraycopy(g_CardInfo.bCardSerialID, 0, g_CardInfo.bCardSerRecordID, 0, 4);
            return OK;
        } else {
            g_CardInfo.cAuthenState = 0;
            return cResult;
        }
    }

    //联网卡片校验
    public static void NetCheckCardInfo(CardBasicParaInfo pCardBasicInfo, SubsidyInfo pSubsidyInfo, int cResult) {
        int cRet = 0;
        int cResultTemp = 0;
        //Log.d(TAG,"=======================获取到网络数据==================================");
        if ((g_CommInfo.cQueryCardInfoStatus == 0) && (cResult != 0xff)) {
            Log.d(TAG, "============获取到网络数据信号重复,退出============");
            return;
        }

        CardBasicParaInfo s_CardBasicInfo = new CardBasicParaInfo();
        SubsidyInfo s_SubsidyInfo = new SubsidyInfo();
        //隔离网络接收数据
        try {
            s_CardBasicInfo = (CardBasicParaInfo) pCardBasicInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        try {
            s_SubsidyInfo = (SubsidyInfo) pSubsidyInfo.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        cResultTemp = cResult;

        //判断卡片存在
        if ((g_CardInfo.cExistState == 1) && (g_CardInfo.cAuthenState > 0)) {
            g_WorkInfo.cInfoStepState = 1;
            if (cResult == 0xff) {
                Log.d(TAG, "===========卡片校验超时=================");
                cRet = SetOfflineCheckCardInfo(g_CardBasicInfo.lngCardID);
            } else {
                Log.d(TAG, "===========卡片校验成功=================");
                cRet = SetNetCheckCardInfo(s_CardBasicInfo, s_SubsidyInfo, cResult);
            }
            g_WorkInfo.cInfoStepState = 0;
            if (cRet == OK) {
                Log.d(TAG, "============发射卡片信息到显示界面===============");
                g_CommInfo.cQueryCardInfoStatus = 0;
                Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_READOK);//显示

                Publicfun.LedShow(1, 0);
                Publicfun.LedShow(3, 1);

                if ((g_WorkInfo.cSelfPressOk == 1) || ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0))) {
                    Log.d(TAG, "自动结帐");
                    Keyoperpro.CheckOut();
                }
            } else {
                g_CardInfo.cAuthenState = 0;
                Log.d(TAG, "联网卡片校验错误:" + cRet);
                Publicfun.ShowCardErrorInfo(cRet);
            }
        } else {
            Log.d(TAG, String.format("联网卡片校验无有效卡片存在:%d--%d", g_CardInfo.cExistState, g_CardInfo.cAuthenState));
        }

        Log.d(TAG, "============联网卡户校验结束===============");
        g_CommInfo.cQueryCardInfoStatus = 0;
        return;
    }

    //联网卡片校验
    public static int SetNetCheckCardInfo(CardBasicParaInfo pCardBasicInfo, SubsidyInfo pSubsidyInfo, int cResult) {
        if (cResult == 0) {
            CardBasicParaInfo SCardBasicInfo = new CardBasicParaInfo();
            SubsidyInfo SSubsidyInfo = new SubsidyInfo();

            //隔离网络接收数据
            try {
                SCardBasicInfo = (CardBasicParaInfo) pCardBasicInfo.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            //隔离网络接收数据
            try {
                SSubsidyInfo = (SubsidyInfo) pSubsidyInfo.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

//            //判断是否有补助和圈存
//            if ((SCardBasicInfo.cInStepState == 1) || (SSubsidyInfo.cSubsidyState == 1)) {
//                Log.d(TAG, "存在信息圈存和补助");
//                g_WorkInfo.cInfoStepState = 1;
//            }
            if (SCardBasicInfo.cInStepState == 1) {
                Log.d(TAG, "需要信息圈存");
                //写卡片信息圈存信息
                cResult = CardApp.Card_InfoSyncOperate(SCardBasicInfo);
                if (cResult == OK) {
                    try {
                        g_CardBasicInfo = (CardBasicParaInfo) SCardBasicInfo.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_READOK);//显示
                    //记录信息圈存流水
                    cResult = Subsidy.WriteCardInStepRecord();
                    if (cResult == OK) {
                        Log.d(TAG, "卡片信息同步成功:" + cResult);
                    }
                }
                if ((cResult != OK) && (cResult != 1)) {
                    Log.d(TAG, "卡片信息同步失败:" + cResult);
                    return cResult;
                }
            }
            Log.d(TAG, "校验卡户信息成功");
            CardPublic.ModifyCardStatus((byte) 1);

            //再次验证卡片有效性
            cResult = CardApp.CheckCardInfoAgain();
            if (cResult == OK) {
                Log.d(TAG, "联网卡户验证有效");
                g_CardInfo.cAuthenState = 2;
                //记录有效的卡片卡号
                System.arraycopy(g_CardInfo.bCardSerialID, 0, g_CardInfo.bCardSerRecordID, 0, 4);
            } else {
                g_CardInfo.cAuthenState = 0;
                return cResult;
            }

            if (SCardBasicInfo.cBurseID == g_StationInfo.cWorkBurseID) {
                Log.d(TAG, "判断余额复位");
                cResult = CardPublic.CardResetProcess();
                if (cResult != OK) {
                    return 62;
                }
                //判断是否领取补助(判断补助钱包是否是当前的工作钱包)
                if ((SSubsidyInfo.cSubsidyState == 1) && (SSubsidyInfo.cSubBurseID == g_StationInfo.cWorkBurseID)) {
                    Log.d(TAG, String.format("领取补助金额:%d,补助钱包号:%d", SSubsidyInfo.lngSubMoney, SSubsidyInfo.cSubBurseID));
                    cResult = Subsidy.Subsidy_CheckOut(SSubsidyInfo.wSubSID, SSubsidyInfo.cSubBurseID, SSubsidyInfo.lngSubMoney);
                    if (cResult == OK) {
                        //显示钱包余额
                        Log.d(TAG, "领取补助成功");
                        Publicfun.ShowCardInfo(g_CardBasicInfo, CARD_READOK);//显示
                    } else {
                        Log.d(TAG, "领取补助失败:" + cResult);
                        Publicfun.ShowCardErrorInfo(cResult);
                        return 62;
                    }
                }
            } else {
                Log.d(TAG, "钱包号不匹配");
            }
            return OK;
        } else if (cResult == 1) {
            Log.d(TAG, "卡内编号不一致");
        } else if (cResult == 2) {
            Log.d(TAG, "卡片信息同步失败");
        } else if (cResult == 3) {
            Log.d(TAG, "卡片领取补助失败");
        } else if ((cResult == 91) || (cResult == 5) || (cResult == 4)) {
            Log.d(TAG, "卡片黑名单");
            CardPublic.ModifyCardStatus((byte) 0);
        }
        return 1;
    }

    //联网获取二维码订单号
    public static void NetGetQROrderNum(int lngQROrderNum, int cResult) {
        long lngTempQROrderNum;
        int cTempResult;

        cTempResult = cResult;
        lngTempQROrderNum = lngQROrderNum;

        if ((cTempResult == 0) && (lngQROrderNum > 0)) {
            //判断此订单号是否被使用过
            if (g_WorkInfo.lngLastOrderNum != lngTempQROrderNum) {
                g_WorkInfo.cQrCodestatus = 2;
                //发送带订单号的二维码
                g_WorkInfo.lngLastOrderNum = lngTempQROrderNum;
                //发送显示商户二维码
                if (g_LocalInfo.iBusinessQRState == 1) {
                    g_ShopQRCodeInfo = Publicfun.SetShopQRCodeInfo((byte) 3);
                    ShowQRCodeInfo(g_ShopQRCodeInfo, QRCODE_SHOW);
                }
                //自动定额(自动定额人脸识别时需要触摸人脸支付才能打开摄像头)
                if ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0)) {
                        g_WorkInfo.cScanQRCodeFlag = 1;
                        g_Nlib.QR_ClearRecvData(5);

                    if((g_SystemInfo.cFaceDetectFlag==1)//启用人脸识别
                            &&(g_WorkInfo.cFaceInitState == 2)
                            &&(g_WorkInfo.cRunState==1)
                            &&(g_LocalInfo.cFaceModeFlag==1)
                            &&(g_CardInfo.cExistState!=1)){
                        Log.i(TAG, "人脸自动模式自动定额打开人脸");
                        FaceWorkTask.StartDetecte(true);
                    }
                 }
                if ((g_SystemInfo.cOnlyOnlineMode == 1)//卡在线交易
                        && ((g_CardInfo.cAuthenState == 2) && (g_CardInfo.cExistState == 1)))//卡片有效存在
                {
                    if((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0))//自动定额模式不进入
                    {
                        Log.d(TAG, "自动定额模式不进入");
                        return;
                    }
                    Log.d(TAG, "以太网在线交易A");
                    cResult = Consume_OnlingCheckOut();
                    if (cResult != 0)
                        Publicfun.ShowCardErrorInfo(cResult);
                    return;
                }
            }
        } else {
            Log.d(TAG, "重新获取二维码订单号");
            g_WorkInfo.cQrCodestatus = 0;
        }
        return;
    }

    //联网获取二维码订单交易结果（服务器推送）
    public static void NetGetQROrderResult( int cResult) {

        Log.d(TAG,"联网获取二维码订单交易结果（服务器推送）");
        Consume.WriteQrPayRecord(1);
        if (cResult == 0) {
            //同一个订单号
            g_WorkInfo.cPayDisPlayStatus = 1;
            CardBasicParaInfo pCardBasicInfo = new CardBasicParaInfo();
            System.arraycopy(g_OnlinePayInfo.cAccName, 0, pCardBasicInfo.cAccName, 0, 16);
            System.arraycopy(g_OnlinePayInfo.cPerCode, 0, pCardBasicInfo.cCardPerCode, 0, 16);
            pCardBasicInfo.lngPayMoney = g_OnlinePayInfo.lngPayMoney;
            pCardBasicInfo.lngWorkBurseMoney = g_OnlinePayInfo.lngBurseMoney;
            pCardBasicInfo.lngPriMoney = g_OnlinePayInfo.lngPriMoney;
            pCardBasicInfo.lngManageMoney = g_OnlinePayInfo.lngManageMoney;
            pCardBasicInfo.lngAccountID = g_OnlinePayInfo.lngAccountID;

            Consume.TodayConsumStat(pCardBasicInfo, (byte) 2);//开始日累统计
            Publicfun.ShowCardInfo(pCardBasicInfo, ONLING_PAYOK);//显示

            if (g_LocalInfo.cPlayVoiceMoneyFlag == 1) {
                soundPlayThread ThreadSoundPlay = new soundPlayThread(pCardBasicInfo.lngPayMoney);
                ThreadSoundPlay.start();
            } else {
                SoundPlay.VoicePlay("phonepayok");
            }
            g_WorkInfo.cSelfPressOk = 0;
            Publicfun.ClearPaymentInfo();
        }
        g_WorkInfo.lngOrderNum = 0;
        g_WorkInfo.cOptionMark = 0;
        g_WorkInfo.cKeyDownState = 0;
        g_WorkInfo.cQrCodestatus = 0;

        g_Nlib.QR_SetDeviceReadEnable(2);//结束识读
        if (g_SystemInfo.cFaceDetectFlag == 1)//是否启用人脸
            FaceWorkTask.StartDetecte(false);
    }

    //二维码交易信息(正元二维码交易信息)
    public static void NetGetQRCodeTradingInfo(long lngQROrderNum, int cResult) {
        int cTempResult;
        long lngTempOrderNum;

        Log.d(TAG, "=======================获取到网络二维码校验交易信息==================================");
        if ((g_CommInfo.cGetQRCodeInfoStatus == 0) && (cResult != 0xff)) {
            Log.d(TAG, "============获取到网络数据信号重复,退出============");
            return;
        }
        cTempResult = cResult;
        lngTempOrderNum = lngQROrderNum;
        g_WorkInfo.cPayDisPlayStatus = 1;

        if (cTempResult == OK) {
            //记录末次交易信息
            {
                //0 一卡通交易,1 其他第三方 卡在线0，二维码1，人脸2，其他一卡通交易3，支付宝9，微信10，银联11，其他第三方12
                //0:正元QR  1:第三方QR  2:人脸支付  3:在线卡片支付
                g_LastRecordPayInfo.cType = 0;
                g_LastRecordPayInfo.cState = 1;//1 未冲正
                g_LastRecordPayInfo.cBusinessID = g_WorkInfo.cBusinessID;
                g_LastRecordPayInfo.cStationID[0] = (byte) (g_StationInfo.iStationID & 0xff);
                g_LastRecordPayInfo.cStationID[1] = (byte) ((g_StationInfo.iStationID & 0xff00) >> 8);
                g_LastRecordPayInfo.lngAccountID = g_OnlinePayInfo.lngAccountID;
                memcpy(g_LastRecordPayInfo.cAccName, g_OnlinePayInfo.cAccName, (g_OnlinePayInfo.cAccName.length));
                memcpy(g_LastRecordPayInfo.cPerCode, g_OnlinePayInfo.cPerCode, (g_OnlinePayInfo.cPerCode.length));
                g_LastRecordPayInfo.lngBurseMoney = g_OnlinePayInfo.lngBurseMoney;
                g_LastRecordPayInfo.lngManageMoney = g_OnlinePayInfo.lngManageMoney;
                g_LastRecordPayInfo.lngPayMoney = g_OnlinePayInfo.lngPayMoney;
                g_LastRecordPayInfo.lngPriMoney = g_OnlinePayInfo.lngPriMoney;
                g_LastRecordPayInfo.lngOrderNum = g_OnlinePayInfo.lngLastOrderNum;
                Log.d(TAG, String.format("正元二维码交易成功：返回的订单号:%d", g_LastRecordPayInfo.lngOrderNum));
            }
            //交易成功,记录二维码流水
            if (g_WorkInfo.cOtherQRFlag == 2) {
                Consume.WriteFacePayRecord(0);
                Consume.WriteQrPayRecord(9);
            } else
                Consume.WriteQrPayRecord(0);

            CardBasicParaInfo pCardBasicInfo = new CardBasicParaInfo();
            memcpy(pCardBasicInfo.cAccName, g_OnlinePayInfo.cAccName, g_OnlinePayInfo.cAccName.length);
            memcpy(pCardBasicInfo.cCardPerCode, g_OnlinePayInfo.cPerCode, g_OnlinePayInfo.cPerCode.length);
            pCardBasicInfo.lngPayMoney = g_OnlinePayInfo.lngPayMoney;
            pCardBasicInfo.lngWorkBurseMoney = g_OnlinePayInfo.lngBurseMoney;
            pCardBasicInfo.lngPriMoney = g_OnlinePayInfo.lngPriMoney;
            pCardBasicInfo.lngManageMoney = g_OnlinePayInfo.lngManageMoney;
            pCardBasicInfo.lngAccountID = g_OnlinePayInfo.lngAccountID;

            Consume.TodayConsumStat(pCardBasicInfo, (byte) 2);//开始日累统计(先统计后显示)
            Publicfun.ShowCardInfo(pCardBasicInfo, ONLING_PAYOK);//显示

            if (g_LocalInfo.cPlayVoiceMoneyFlag == 1) {
                soundPlayThread ThreadSoundPlay = new soundPlayThread(pCardBasicInfo.lngPayMoney);
                ThreadSoundPlay.start();
            } else {
                SoundPlay.VoicePlay("zhifuchenggong");
            }

            g_WorkInfo.cSelfPressOk = 0;
            g_WorkInfo.cOptionMark = 0;
            g_WorkInfo.cKeyDownState = 0;
            g_WorkInfo.cQrCodestatus = 0;
            Publicfun.ClearPaymentInfo();
        } else if ((cTempResult >= 1) || (cTempResult == 0xff)) {
            Log.d(TAG, "============在线交易失败:"+cTempResult);
            ShowQRErrorInfo(cTempResult);
            if ((g_LocalInfo.cInputMode != 3) || ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 1))) {
                g_WorkInfo.cSelfPressOk = 0;
                g_CardBasicInfo.lngPayMoney = 0;
                Publicfun.ClearPaymentInfo();
            }
            g_WorkInfo.cQrCodestatus = 0;
            g_WorkInfo.cKeyDownState = 0;
        }
        g_Nlib.QR_SetDeviceReadEnable(2);//结束识读
        if (g_SystemInfo.cFaceDetectFlag == 1)//是否启用人脸
            FaceWorkTask.StartDetecte(false);

        if (g_WorkInfo.cOtherQRFlag == 3)//在线卡片支付
        {
            g_WorkInfo.cCheckOutState = 1;
            g_WorkInfo.cPayDisPlayStatus = 0;
        }
        Log.d(TAG, "============联网二维码校验结束===============");
        if (g_LocalInfo.cDockposFlag != 1) {
            g_WorkInfo.cOtherQRFlag = 0;
        }
        g_CommInfo.cGetQRCodeInfoStatus = 0;
    }

    //第三方联网获取二维码交易信息
    public static void NetGetThirdCodeTradingInfo(int cResult) {

        Log.d(TAG, "=======================获取到网络支付宝二维码校验交易信息==================================");
        if ((g_CommInfo.cGetQRCodeInfoStatus == 0) && (cResult != 0xff)) {
            Log.d(TAG, "============获取到网络数据信号重复,退出============");
            return;
        }
        int cTempResult = cResult;
        g_WorkInfo.cPayDisPlayStatus = 1;
        if (cTempResult == OK) {
            //记录末次交易信息
            {
//                1：支付宝；2：一卡通校园卡
//                3：微信；4：银联
//                5：云马；6：龙支付
//                7：微校；8：威富通
//                9：农业银行；10：翼支付
//                11：建行e码通；12：招行
//                13：银联商业服务部；14：工银融e联

                if (g_ThirdCodeResultInfo.cPaymentType == 2)//支付宝
                    g_LastRecordPayInfo.cType = 0;
                else
                    g_LastRecordPayInfo.cType = g_ThirdCodeResultInfo.cPaymentType;

                g_LastRecordPayInfo.cState = 1;// 1 未冲正
                g_LastRecordPayInfo.cBusinessID = g_WorkInfo.cBusinessID;
                g_LastRecordPayInfo.cStationID[0] = (byte) (g_StationInfo.iStationID & 0xff);
                g_LastRecordPayInfo.cStationID[1] = (byte) ((g_StationInfo.iStationID & 0xff00) >> 8);
                g_LastRecordPayInfo.lngAccountID = g_ThirdCodeResultInfo.lngAccountID;
                memcpy(g_LastRecordPayInfo.cAccName, g_ThirdCodeResultInfo.cAccName, (g_ThirdCodeResultInfo.cAccName.length));
                memcpy(g_LastRecordPayInfo.cPerCode, g_ThirdCodeResultInfo.cPerCode, (g_ThirdCodeResultInfo.cPerCode.length));
                g_LastRecordPayInfo.lngBurseMoney = g_ThirdCodeResultInfo.lngBurseMoney;
                g_LastRecordPayInfo.lngManageMoney = g_ThirdCodeResultInfo.wManageMoney;
                g_LastRecordPayInfo.lngPayMoney = (int) g_ThirdCodeResultInfo.lngPayMoney;
                g_LastRecordPayInfo.lngPriMoney = g_ThirdCodeResultInfo.wPrivelegeMoney;
                g_LastRecordPayInfo.lngOrderNum = g_ThirdCodeResultInfo.lngLastOrderNum;
                Log.d(TAG, String.format("第三方联网交易成功：返回的订单号:%d", g_LastRecordPayInfo.lngOrderNum));
            }

            if (g_ThirdCodeResultInfo.cPaymentType == 1)//支付宝
                Consume.WriteQrPayRecord(3);
            else if (g_ThirdCodeResultInfo.cPaymentType == 2)//一卡通校园卡
                Consume.WriteQrPayRecord(2);
            else if (g_ThirdCodeResultInfo.cPaymentType == 3)//微信
                Consume.WriteQrPayRecord(4);
            else
                Consume.WriteQrPayRecord(10);

            CardBasicParaInfo pCardBasicInfo = new CardBasicParaInfo();
            System.arraycopy(g_ThirdCodeResultInfo.cAccName, 0, pCardBasicInfo.cAccName, 0, 16);
            System.arraycopy(g_ThirdCodeResultInfo.cPerCode, 0, pCardBasicInfo.cCardPerCode, 0, 16);
            pCardBasicInfo.lngAccountID = g_ThirdCodeResultInfo.lngAccountID;
            pCardBasicInfo.lngPayMoney = g_ThirdCodeResultInfo.lngPayMoney;
            pCardBasicInfo.lngWorkBurseMoney = g_ThirdCodeResultInfo.lngBurseMoney;
            pCardBasicInfo.lngPriMoney = g_ThirdCodeResultInfo.wPrivelegeMoney;
            pCardBasicInfo.lngManageMoney = g_ThirdCodeResultInfo.wManageMoney;
            Consume.TodayConsumStat(pCardBasicInfo, (byte) 2);//开始日累统计
            Publicfun.ShowCardInfo(pCardBasicInfo, ONLING_PAYOK);//显示

            if (g_LocalInfo.cPlayVoiceMoneyFlag == 1) {
                soundPlayThread ThreadSoundPlay = new soundPlayThread(pCardBasicInfo.lngPayMoney);
                ThreadSoundPlay.start();
            } else {
                SoundPlay.VoicePlay("zhifuchenggong");
            }
        } else if ((cTempResult >= 1) || (cTempResult == 0xff)) {
            ShowQRErrorInfo(cTempResult);
            g_CardBasicInfo.lngPayMoney = 0;
        }
        g_WorkInfo.cQrCodestatus = 0;
        g_WorkInfo.cOptionMark = 0;
        g_WorkInfo.cKeyDownState = 0;
        g_WorkInfo.cSelfPressOk = 0;
        g_WorkInfo.cOtherQRFlag = 0;
        g_CommInfo.cGetQRCodeInfoStatus = 0;

        Publicfun.ClearPaymentInfo();
        g_Nlib.QR_SetDeviceReadEnable(2);//结束识读
        if (g_SystemInfo.cFaceDetectFlag == 1)//是否启用人脸
            FaceWorkTask.StartDetecte(false);

        Log.d(TAG, "============联网第三方二维码校验结束===============");
        if (g_LocalInfo.cDockposFlag != 1) {
            g_WorkInfo.cOtherQRFlag = 0;
        }
        g_CommInfo.cGetQRCodeInfoStatus = 0;
    }

    //在线末笔冲正
    public static void OnlingLastRecDispel(int cResult) {
        int cTempResult;

        Log.d(TAG, "=======================获取到网络在线末笔冲正信息==================================");
        if ((g_CommInfo.cLastRecDisInfoStatus == 0) && (cResult != 0xff)) {
            Log.d(TAG, "============获取到网络数据信号重复,退出============");
            return;
        }
        cTempResult = cResult;
        if (cTempResult == OK) {
            CardBasicParaInfo pCardBasicInfo = new CardBasicParaInfo();
            memcpy(pCardBasicInfo.cAccName, g_LastRecordPayInfo.cAccName, (pCardBasicInfo.cAccName.length));
            memcpy(pCardBasicInfo.cCardPerCode, g_LastRecordPayInfo.cPerCode, (pCardBasicInfo.cCardPerCode.length));
            pCardBasicInfo.lngAccountID = g_LastRecordPayInfo.lngAccountID;
            pCardBasicInfo.lngInPayMoney = g_LastRecordPayInfo.lngPayMoney;
            pCardBasicInfo.lngPayMoney = g_LastRecordPayInfo.lngPayMoney;
            pCardBasicInfo.lngWorkBurseMoney = g_LastRecordPayInfo.lngBurseMoney;
            pCardBasicInfo.lngManageMoney = g_LastRecordPayInfo.lngManageMoney;
            pCardBasicInfo.lngPriMoney = g_LastRecordPayInfo.lngPriMoney;
            g_LastRecordPayInfo.cState = 0;//1 未冲正 其他已经冲正
            Consume.TodayConsumStat(pCardBasicInfo, (byte) 3);//开始日累统计
            Publicfun.ShowCardInfo_Dispel(pCardBasicInfo, RDCARD_PAYOKONLING);//显示
        } else if ((cTempResult >= 1) || (cTempResult == 0xff)) {
            Publicfun.ShowCardErrorInfo_Dispel(cTempResult);//显示
        }
        Log.d(TAG, "============在线末笔冲正结束===============");
        if (g_LocalInfo.cDockposFlag != 1) {
            g_WorkInfo.cOtherQRFlag = 0;
        }
        g_CommInfo.cLastRecDisInfoStatus = 0;
    }

    //第三方代扣
    public static void OnlingWithholdTrading(int cResult) {

        Log.d(TAG, "=======================获取到网络第三方代扣信息==================================");
        if ((g_CommInfo.cWithholdInfoStatus == 0) && (cResult != 0xff)) {
            Log.d(TAG, "============获取到网络数据信号重复,退出============");
            return;
        }
        g_WorkInfo.cPayDisPlayStatus = 1;
        int cTempResult = cResult;
        if (cTempResult == OK) {
            //记录末次交易信息
            {
                //0 一卡通交易,1 其他第三方 卡在线0，二维码1，人脸2，其他一卡通交易3，支付宝9，微信10，银联11，其他第三方12
                //0:正元QR  1:第三方QR  2:人脸支付  3:在线卡片支付
                g_LastRecordPayInfo.cType = 12;
                g_LastRecordPayInfo.cState = 1;//1 未冲正
                g_LastRecordPayInfo.cBusinessID = g_WorkInfo.cBusinessID;
                g_LastRecordPayInfo.cStationID[0] = (byte) (g_StationInfo.iStationID & 0xff);
                g_LastRecordPayInfo.cStationID[1] = (byte) ((g_StationInfo.iStationID & 0xff00) >> 8);
                g_LastRecordPayInfo.lngAccountID = g_ThirdCodeResultInfo.lngAccountID;
                memcpy(g_LastRecordPayInfo.cAccName, g_ThirdCodeResultInfo.cAccName, (g_ThirdCodeResultInfo.cAccName.length));
                memcpy(g_LastRecordPayInfo.cPerCode, g_ThirdCodeResultInfo.cPerCode, (g_ThirdCodeResultInfo.cPerCode.length));
                g_LastRecordPayInfo.lngBurseMoney = g_ThirdCodeResultInfo.lngBurseMoney;
                g_LastRecordPayInfo.lngManageMoney = g_ThirdCodeResultInfo.wManageMoney;
                g_LastRecordPayInfo.lngPayMoney = (int) g_ThirdCodeResultInfo.lngPayMoney;
                g_LastRecordPayInfo.lngPriMoney = g_ThirdCodeResultInfo.wPrivelegeMoney;
                g_LastRecordPayInfo.lngOrderNum = g_ThirdCodeResultInfo.lngLastOrderNum;
                Log.d(TAG, String.format("第三方代扣交易成功：返回的订单号:%d", g_LastRecordPayInfo.lngOrderNum));
            }
            Consume.WriteQrPayRecord(5);
            CardBasicParaInfo pCardBasicInfo = new CardBasicParaInfo();
            memcpy(pCardBasicInfo.cAccName, g_ThirdCodeResultInfo.cAccName, g_ThirdCodeResultInfo.cAccName.length);
            memcpy(pCardBasicInfo.cCardPerCode, g_ThirdCodeResultInfo.cPerCode, g_ThirdCodeResultInfo.cPerCode.length);
            pCardBasicInfo.lngPayMoney = g_ThirdCodeResultInfo.lngPayMoney;
            pCardBasicInfo.lngWorkBurseMoney = g_ThirdCodeResultInfo.lngBurseMoney;
            pCardBasicInfo.lngPriMoney = g_ThirdCodeResultInfo.wPrivelegeMoney;
            pCardBasicInfo.lngManageMoney = g_ThirdCodeResultInfo.wManageMoney;
            pCardBasicInfo.lngAccountID = g_ThirdCodeResultInfo.lngAccountID;
            Consume.TodayConsumStat(pCardBasicInfo, (byte)2);//开始日累统计(先统计后显示)
            Publicfun.ShowCardInfo(pCardBasicInfo, ONLING_PAYOK);//显示

            if (g_LocalInfo.cPlayVoiceMoneyFlag == 1) {
                soundPlayThread ThreadSoundPlay = new soundPlayThread(pCardBasicInfo.lngPayMoney);
                ThreadSoundPlay.start();
            } else {
                SoundPlay.VoicePlay("zhifuchenggong");
            }
            g_WorkInfo.cSelfPressOk = 0;
            g_WorkInfo.cOptionMark = 0;
            g_WorkInfo.cKeyDownState = 0;
            g_WorkInfo.cQrCodestatus = 0;
            Publicfun.ClearPaymentInfo();
        } else if ((cTempResult >= 1) || (cTempResult == 0xff)) {
            ShowQRErrorInfo(cTempResult);
            if ((g_LocalInfo.cInputMode != 3) || ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 1))) {
                g_WorkInfo.cSelfPressOk = 0;
                g_CardBasicInfo.lngPayMoney = 0;
                Publicfun.ClearPaymentInfo();
            }
            g_WorkInfo.cQrCodestatus = 0;
            g_WorkInfo.cKeyDownState = 0;
        }

        g_Nlib.QR_SetDeviceReadEnable(2);//结束识读
        if (g_SystemInfo.cFaceDetectFlag == 1)//是否启用人脸
            FaceWorkTask.StartDetecte(false);

        if (g_CardInfo.cExistState == 1)//在线卡片代扣支付
        {
            g_WorkInfo.cCheckOutState = 1;
            g_WorkInfo.cPayDisPlayStatus = 0;
        }
        Log.d(TAG, "============在线代扣结束===============");
        if (g_LocalInfo.cDockposFlag != 1) {
            g_WorkInfo.cOtherQRFlag = 0;
        }
        g_CommInfo.cWithholdInfoStatus = 0;
    }

}

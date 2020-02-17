package com.hzsun.mpos.Public;

import android.util.Log;

import com.hzsun.mpos.FaceApp.FaceWorkTask;
import com.hzsun.mpos.Pos.MoneyPoint;
import com.hzsun.mpos.SerialWork.SerialWorkTask;
import com.hzsun.mpos.Sound.SoundPlay;

import static com.hzsun.mpos.Activity.CardCommonActivity.CADR_SHOWTOTAL;
import static com.hzsun.mpos.Global.Global.DATE_TIME_ERROR;
import static com.hzsun.mpos.Global.Global.LAN_EP_CONSUMEPOS;
import static com.hzsun.mpos.Global.Global.LAN_EP_MONEYPOS;
import static com.hzsun.mpos.Global.Global.MEMORY_FAIL;
import static com.hzsun.mpos.Global.Global.WRITE_CARD_ERROR;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardInfo;
import static com.hzsun.mpos.Global.Global.g_CommInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Pos.Consume.Consume_CheckOut;
import static com.hzsun.mpos.Pos.Consume.Consume_OnlingCheckOut;
import static com.hzsun.mpos.Pos.Consume.Consume_WithholdCheckOut;
import static com.hzsun.mpos.Sound.SoundPlay.VoiceMoneyPlay;
import static com.hzsun.mpos.Sound.SoundPlay.VoicePerPlay;

public class Keyoperpro {

    private static final String TAG = "Keyoperpro";
    private static final int OK = 0;

    //结帐交易
    public static void CheckOut() {
        int cResult = 0;
        long lngPaymentMoney;

        if (g_WorkInfo.lngTotalMoney > 0) {
            g_WorkInfo.lngPaymentMoney = g_WorkInfo.lngTotalMoney;
        }
        lngPaymentMoney = g_WorkInfo.lngPaymentMoney;

        if (g_LocalInfo.cDockposFlag == 1) {
            //启用对接机
            SerialWorkTask.setPayMoney();
        }

        if ((g_WorkInfo.cCheckOutState == 0)
                && (((lngPaymentMoney >= 0) && (g_WorkInfo.cPaymentMoneyLen != 0))
                || ((g_LocalInfo.cInputMode == 2) && (g_WorkInfo.cKeyDownState == 1))
                || (g_LocalInfo.cInputMode == 3)
                || ((g_WorkInfo.cOptionMark != 0) && (g_WorkInfo.lngTotalMoney > 0)))
                || g_LocalInfo.cDockposFlag == 1) {
            g_WorkInfo.cSelfPressOk = 1;
            if ((g_CardInfo.cAuthenState == 2) && (g_CardInfo.cExistState == 1) && (g_WorkInfo.cInfoStepState == 0)) {
                if (g_StationInfo.iStationClass == LAN_EP_CONSUMEPOS) {
                    if (g_LocalInfo.iWithholdState == 1) {
                        if (g_WorkInfo.cRunState == 1) {
                            Log.i(TAG, "以太网在线代扣交易");
                            cResult = Consume_WithholdCheckOut();
                            if (cResult != 0)
                                Publicfun.ShowCardErrorInfo(cResult);
                            return;
                        }
                    } else {
                        if ((g_SystemInfo.cOnlyOnlineMode == 1)
                                && (g_WorkInfo.cQrCodestatus == 2)
                                && (g_WorkInfo.cRunState == 1)) {
                            Log.i(TAG, "以太网在线交易");
                            cResult = Consume_OnlingCheckOut();
                            if (cResult != 0)
                                Publicfun.ShowCardErrorInfo(cResult);
                            return;
                        }
                    }
                }
                //判断设备类型
                g_CommInfo.cQueryCardInfoStatus = 1;
                g_WorkInfo.cStartWCardFlag = 1;
                if (g_StationInfo.iStationClass == LAN_EP_CONSUMEPOS) {
                    Log.i(TAG, "以太网电子现金消费机");
                    cResult = Consume_CheckOut();
                } else if (g_StationInfo.iStationClass == LAN_EP_MONEYPOS) {
                    Log.i(TAG, "以太网电子现金充值机");
                    cResult = MoneyPoint.MoneyPoint_CheckOut();
                }
                g_CommInfo.cQueryCardInfoStatus = 0;
                if (cResult == OK) {
                    if (g_LocalInfo.cPlayVoiceMoneyFlag == 1) {
                        if (g_StationInfo.iStationClass == LAN_EP_CONSUMEPOS)
                            VoiceMoneyPlay(g_CardBasicInfo.lngPayMoney, 0);
                        else if (g_StationInfo.iStationClass == LAN_EP_MONEYPOS)
                            VoiceMoneyPlay(g_CardBasicInfo.lngPayMoney, 1);
                        else
                            VoiceMoneyPlay(g_CardBasicInfo.lngPayMoney, 0);
                    } else {
                        VoicePerPlay(g_CardBasicInfo.cStatusID);
                    }
                    Publicfun.ShowCardInfo(g_CardBasicInfo, CADR_SHOWTOTAL); //发射卡片交易成功信息
                } else {
                    Publicfun.ShowCardErrorInfo(cResult);
                    //判断存储失败和时钟故障
                    if ((MEMORY_FAIL == cResult) || (DATE_TIME_ERROR == cResult)) {
                        Log.i(TAG, "存储或时钟故障");
                        g_WorkInfo.cRecordErrFlag = 1;
                    }
                    if (WRITE_CARD_ERROR == cResult) {
                        SoundPlay.VoicePlay("deal_error");
                    }
                    Log.i(TAG, "交易失败:" + cResult);
                    if (g_WorkInfo.cTestState == 1) {
                        Log.i(TAG, "退出测试模式");
                        //g_WorkInfo.cTestState=0;
                        //g_LocalInfo=LocalInfoRW.ReadLocalInfo();
                    }
                }

                if (g_LocalInfo.cDockposFlag == 1) {
                    if (cResult == 0) {
                        SerialWorkTask.onPayDone(0x00);
                    } else {
                        SerialWorkTask.onPayDone(0x99);
                    }
                }
                if (g_LocalInfo.cDockposFlag != 1) {
                    g_WorkInfo.cOtherQRFlag = 0;
                }
                g_WorkInfo.cPayDisPlayStatus = 0;
                g_WorkInfo.cCheckOutState = 1;
                g_WorkInfo.cSelfPressOk = 0;
                g_WorkInfo.cKeyDownState = 0;
                g_WorkInfo.cOptionMark = 0;
                g_WorkInfo.cScanQRCodeFlag = 0;
                g_WorkInfo.cStartWCardFlag = 0;

                if ((g_LocalInfo.cInputMode == 1 || g_LocalInfo.cInputMode == 2) || (g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode != 0)) {
                    Publicfun.ClearPaymentInfo();
                    g_Nlib.QR_SetDeviceReadEnable(2);//Log.i(TAG,"结束识读");
                    if (g_SystemInfo.cFaceDetectFlag == 1)//是否启用人脸
                    {
                        FaceWorkTask.StartDetecte(false);
                    }
                }
                g_WorkInfo.cQrCodestatus = 0;
                if (g_WorkInfo.cTestState == 1) {
                    g_WorkInfo.cCheckOutState = 0;
                }
                Log.i(TAG, "交易结束");
            }
        }
    }

//    //交易类按键输入
//    public static void NormalKeyMenu(int cdisk)
//    {
//        int lngTemp;
//
//        if(g_WorkInfo.cSelfPressOk==0)
//        {
//            if(((cdisk>=KeyEvent.KEYCODE_0)&&(cdisk<=KeyEvent.KEYCODE_9))||(cdisk==KeyEvent.KEYCODE_PERIOD))
//            {
//                //输入的字符数必须小于8，否则按键无效
//                if(cdisk<=KeyEvent.KEYCODE_9 || cdisk==KeyEvent.KEYCODE_PERIOD)
//                {
//                    // 对输入超出范围的判断
//                    if(g_WorkInfo.cPaymentDotPos==0)
//                    {
//                        if(g_WorkInfo.cPaymentMoneyLen>=6)
//                        {
//                            return;
//                        }
//                    }
//                    else
//                    {
//                        if(g_WorkInfo.cPaymentMoneyLen>6)
//                        {
//                            return;
//                        }
//                    }
//                    //如果是小数点
//                    if(cdisk==KeyEvent.KEYCODE_PERIOD)
//                    {
//                        if(g_WorkInfo.cPaymentMoneyLen==0)
//                        {
//                            g_WorkInfo.cPaymentMoney[g_WorkInfo.cPaymentMoneyLen++]='0';
//                            g_WorkInfo.cPaymentMoney[g_WorkInfo.cPaymentMoneyLen++]='.';
//                            g_WorkInfo.cPaymentDotPos=1;
//                        }
//                        else
//                        {
//                            if(g_WorkInfo.cPaymentDotPos==0)
//                            {
//                                g_WorkInfo.cPaymentDotPos=g_WorkInfo.cPaymentMoneyLen;
//                                g_WorkInfo.cPaymentMoney[g_WorkInfo.cPaymentMoneyLen++]='.';
//                            }
//                        }
//                    }
//                    else
//                    {
//                        if(g_WorkInfo.cPaymentDotPos==0)
//                        {
//                            g_WorkInfo.cPaymentMoney[g_WorkInfo.cPaymentMoneyLen++]= (byte) (cdisk+41);
//                        }
//                        else
//                        {
//                            if(g_WorkInfo.cPaymentMoneyLen<=g_WorkInfo.cPaymentDotPos+2)
//                            {
//                                g_WorkInfo.cPaymentMoney[g_WorkInfo.cPaymentMoneyLen++]= (byte) (cdisk+41);
//                            }
//                        }
//                    }
//                    //Log.i(TAG,g_WorkInfo.cPaymentMoney);
//                    //限制输入金额8000000
//                    lngTemp= (int) ((Publicfun.StrToFloat(g_WorkInfo.cPaymentMoney)+0.001)*100);
//                    if(lngTemp>65535)
//                    {
//                        g_WorkInfo.cPaymentMoneyLen= (byte) (g_WorkInfo.cPaymentMoneyLen-1);
//                        return;
//                    }
//                    g_WorkInfo.lngPaymentMoney=lngTemp;
//                    //Log.i(TAG,"g_WorkInfo.PaymentMoney值:%d",g_WorkInfo.lngPaymentMoney);
//                }
//                g_WorkInfo.cKeyDownState=1;
//                //Log.i(TAG,"交易额:%d",g_WorkInfo.lngPaymentMoney);
//            }
//
//            //加和减的功能
//            if((cdisk==KeyEvent.KEYCODE_NUMPAD_ADD)||(cdisk==KeyEvent.KEYCODE_NUMPAD_MULTIPLY))
//            {
//                if((cdisk==KeyEvent.KEYCODE_NUMPAD_ADD)&&(g_WorkInfo.lngPaymentMoney!=0)) //加
//                {
//                    Log.i(TAG,"加运算 g_WorkInfo.cOptionMark="+g_WorkInfo.cOptionMark);
//                    //在加之前先判断是否有减
//                    if(g_WorkInfo.cOptionMark==2)
//                    {
//                        if(g_WorkInfo.lngTotalMoney>=g_WorkInfo.lngPaymentMoney)
//                        {
//                            g_WorkInfo.lngTotalMoney=g_WorkInfo.lngTotalMoney-g_WorkInfo.lngPaymentMoney;
//                        }
//                        else
//                        {
//                            Log.i(TAG,"输入错误");
//                            g_WorkInfo.cOptionMark=0;
//                            Publicfun.ClearPaymentInfo();
//                            SoundPlay.VoicePlay("inputpay_error");
//                            return;
//                        }
//                    }
//                    else
//                    {
//                        //累加
//                        lngTemp= (int) (g_WorkInfo.lngTotalMoney+g_WorkInfo.lngPaymentMoney);
//                        if(lngTemp<=65535)
//                        {
//                            g_WorkInfo.lngTotalMoney=lngTemp;
//                        }
//                        else
//                        {
//                            Log.i(TAG,"输入错误");
//                            g_WorkInfo.cOptionMark=0;
//                            Publicfun.ClearPaymentInfo();
//                            SoundPlay.VoicePlay("inputpay_error");
//                            return;
//                        }
//                    }
//                    g_WorkInfo.cOptionMark=1;
//                    g_WorkInfo.lngPaymentMoney=0;
//                }
//                else if((cdisk==KeyEvent.KEYCODE_NUMPAD_MULTIPLY)&&(g_WorkInfo.lngPaymentMoney!=0))        //减
//                {
//                    Log.i(TAG,String.format("减运算cOptionMark=%d.%d",g_WorkInfo.cOptionMark,g_WorkInfo.lngTotalMoney));
//                    //在加之前先判断是否有减
//                    if(g_WorkInfo.cOptionMark==1)
//                    {
//                        lngTemp= (int) (g_WorkInfo.lngTotalMoney+g_WorkInfo.lngPaymentMoney);
//                        if(lngTemp<=65535)
//                        {
//                            g_WorkInfo.lngTotalMoney=lngTemp;
//                        }
//                        else
//                        {
//                            Log.i(TAG,"输入错误");
//                            g_WorkInfo.cOptionMark=0;
//                            Publicfun.ClearPaymentInfo();
//                            SoundPlay.VoicePlay("inputpay_error");
//                            return;
//                        }
//                    }
//                    else
//                    {
//                        if(g_WorkInfo.lngTotalMoney==0)
//                        {
//                            g_WorkInfo.lngTotalMoney=g_WorkInfo.lngPaymentMoney;
//                        }
//                        else
//                        {
//                            if(g_WorkInfo.lngTotalMoney>=g_WorkInfo.lngPaymentMoney)
//                            {
//                                g_WorkInfo.lngTotalMoney=g_WorkInfo.lngTotalMoney-g_WorkInfo.lngPaymentMoney;
//                            }
//                            else
//                            {
//                                Log.i(TAG,"输入错误");
//                                g_WorkInfo.cOptionMark=0;
//                                Publicfun.ClearPaymentInfo();
//                                SoundPlay.VoicePlay("inputpay_error");
//                                return;
//                            }
//                        }
//                    }
//                    g_WorkInfo.cOptionMark=2;
//                    g_WorkInfo.lngPaymentMoney=0;
//                }
//                if(cdisk==KeyEvent.KEYCODE_NUMPAD_ADD)
//                {
//                    g_WorkInfo.cOptionMark=1;
//                }
//                if(cdisk==KeyEvent.KEYCODE_NUMPAD_MULTIPLY)
//                {
//                    g_WorkInfo.cOptionMark=2;
//                }
//                Publicfun.ClearPaymentInfo();
//                g_WorkInfo.cKeyDownState=1;
//                Log.i(TAG,"g_WorkInfo.lngTotalMoney:"+g_WorkInfo.lngTotalMoney);
//            }
//            //加减功能后,按了确定键
//            if((g_WorkInfo.cOptionMark!=0)&&(cdisk ==KeyEvent.KEYCODE_ENTER))
//            {
//                Log.i(TAG,"加减功能后,按了确定键");
//                if(g_WorkInfo.cOptionMark==1)
//                {
//                    //累加
//                    Log.i(TAG,"累加");
//                    lngTemp= (int) (g_WorkInfo.lngTotalMoney+g_WorkInfo.lngPaymentMoney);
//                    if(lngTemp<=65535)
//                    {
//                        g_WorkInfo.lngTotalMoney=lngTemp;
//                    }
//                    else
//                    {
//                        Log.i(TAG,"输入错误");
//                        g_WorkInfo.cOptionMark=0;
//                        Publicfun.ClearPaymentInfo();
//                        SoundPlay.VoicePlay("inputpay_error");
//                        return;
//                    }
//                }
//                else if(g_WorkInfo.cOptionMark==2)
//                {
//                    Log.i(TAG,"减法");
//                    if(g_WorkInfo.lngTotalMoney>=g_WorkInfo.lngPaymentMoney)
//                    {
//                        g_WorkInfo.lngTotalMoney=g_WorkInfo.lngTotalMoney-g_WorkInfo.lngPaymentMoney;
//                        if(g_WorkInfo.lngTotalMoney==0)
//                        {
//                            g_WorkInfo.lngPaymentMoney=0;
//                        }
//                    }
//                    else
//                    {
//                        Log.i(TAG,"输入错误");
//                        g_WorkInfo.cOptionMark=0;
//                        Publicfun.ClearPaymentInfo();
//                        SoundPlay.VoicePlay("inputpay_error");
//                        return;
//                    }
//                }
//                g_WorkInfo.cKeyDownState=1;
//            }
//        }
//    }
//
//    //交易类按键输入(乘法)
//    public static int NormalKeyMenu_One(int cdisk)
//    {
//        long lngTemp;
//        long lngMoneyLim;
//
//        lngMoneyLim=8000000;//输入最大金额
//        if(g_WorkInfo.cSelfPressOk==0)
//        {
//            if(((cdisk>= KeyEvent.KEYCODE_0)&&(cdisk<=KeyEvent.KEYCODE_9))||(cdisk==KeyEvent.KEYCODE_PERIOD))
//            {
//                //输入的字符数必须小于8，否则按键无效
//                if(cdisk<=KeyEvent.KEYCODE_9 || cdisk==KeyEvent.KEYCODE_PERIOD)
//                {
//                    // 对输入超出范围的判断
//                    if(g_WorkInfo.cPaymentDotPos==0)
//                    {
//                        if(g_WorkInfo.cPaymentMoneyLen>=7)
//                        {
//                            Log.i(TAG,"返回1");
//                            return 2;
//                        }
//                    }
//                    else
//                    {
//                        if(g_WorkInfo.cPaymentMoneyLen>7)
//                        {
//                            Log.i(TAG,"返回2");
//                            return 2;
//                        }
//                    }
//                    //如果是小数点
//                    if(cdisk==KeyEvent.KEYCODE_PERIOD)
//                    {
//                        if(g_WorkInfo.cPaymentMoneyLen==0)
//                        {
//                            g_WorkInfo.cPaymentMoney[g_WorkInfo.cPaymentMoneyLen++]='0';
//                            g_WorkInfo.cPaymentMoney[g_WorkInfo.cPaymentMoneyLen++]='.';
//                            g_WorkInfo.cPaymentDotPos=1;
//                        }
//                        else
//                        {
//                            if(g_WorkInfo.cPaymentDotPos==0)
//                            {
//                                g_WorkInfo.cPaymentDotPos=g_WorkInfo.cPaymentMoneyLen;
//                                g_WorkInfo.cPaymentMoney[g_WorkInfo.cPaymentMoneyLen++]='.';
//                            }
//                        }
//                    }
//                    else
//                    {
//                        if(g_WorkInfo.cPaymentDotPos==0)//"0"-7  "9"-16
//                        {
//                            g_WorkInfo.cPaymentMoney[g_WorkInfo.cPaymentMoneyLen++]= (byte) (cdisk+41);
//                        }
//                        else
//                        {
//                            if(g_WorkInfo.cPaymentMoneyLen<=g_WorkInfo.cPaymentDotPos+2)
//                            {
//                                g_WorkInfo.cPaymentMoney[g_WorkInfo.cPaymentMoneyLen++]= (byte) (cdisk+41);
//                            }
//                        }
//                    }
//                    //限制输入金额8000000
//                    lngTemp=(int)((Publicfun.StrToFloat(g_WorkInfo.cPaymentMoney)+0.001)*100);
//                    if(lngTemp>lngMoneyLim)
//                    {
//                        g_WorkInfo.cPaymentMoneyLen= (byte) (g_WorkInfo.cPaymentMoneyLen-1);
//                        return 1;
//                    }
//                    g_WorkInfo.lngPaymentMoney=lngTemp;
//                    g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                    //Log.i(TAG,String.format("g_WorkInfo.PaymentMoney值:%d",g_WorkInfo.lngPaymentMoney));
//                }
//                g_WorkInfo.cKeyDownState=1;
//                //Log.i(TAG,String.format("交易额:%d",g_WorkInfo.lngPaymentMoney));
//            }
//
//            //加和乘的功能 2B+ 2D*
//            if((cdisk==KeyEvent.KEYCODE_NUMPAD_ADD)||(cdisk==KeyEvent.KEYCODE_NUMPAD_MULTIPLY))
//            {
//                if((cdisk==KeyEvent.KEYCODE_NUMPAD_ADD)&&(g_WorkInfo.lngPaymentMoney!=0)) //加
//                {
//                    Log.i(TAG,"加运算 g_WorkInfo.cOptionMark="+g_WorkInfo.cOptionMark);
//                    //在加之前先判断是否有乘
//                    if(g_WorkInfo.cOptionMark==2)
//                    {
//                        if(g_WorkInfo.lngTotalMoney!=0)
//                        {
//                            if((g_WorkInfo.lngTotalMoney*g_WorkInfo.lngPaymentMoney)>=100)
//                            {
//                                g_WorkInfo.lngTotalMoney=((g_WorkInfo.lngTotalMoney*g_WorkInfo.lngPaymentMoney)/100);
//                                if(g_WorkInfo.lngTotalMoney>lngMoneyLim)
//                                {
//                                    Log.i(TAG,"输入错误");
//                                    g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                                    g_WorkInfo.cOptionMark=0;
//                                    Publicfun.ClearPaymentInfo();
//                                    SoundPlay.VoicePlay("inputpay_error");
//                                    return 1;
//                                }
//                            }
//                            else
//                            {
//                                Log.i(TAG,"输入错误");
//                                g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                                g_WorkInfo.cOptionMark=0;
//                                Publicfun.ClearPaymentInfo();
//                                SoundPlay.VoicePlay("inputpay_error");
//                                return 1;
//                            }
//                        }
//                    }
//                    else
//                    {
//                        //累加
//                        lngTemp=g_WorkInfo.lngTotalMoney+g_WorkInfo.lngPaymentMoney;
//                        if(lngTemp<=lngMoneyLim)
//                        {
//                            g_WorkInfo.lngTotalMoney=lngTemp;
//                        }
//                        else
//                        {
//                            Log.i(TAG,"输入错误");
//                            g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                            g_WorkInfo.cOptionMark=0;
//                            Publicfun.ClearPaymentInfo();
//                            SoundPlay.VoicePlay("inputpay_error");
//                            return 1;
//                        }
//                    }
//                    g_WorkInfo.cOptionMark=1;
//                    g_WorkInfo.lngPaymentMoney=0;
//                    g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                }
//                else if((cdisk==KeyEvent.KEYCODE_NUMPAD_MULTIPLY)&&(g_WorkInfo.lngPaymentMoney!=0))        //乘
//                {
//                    Log.i(TAG,"乘运算 g_WorkInfo.cOptionMark="+g_WorkInfo.cOptionMark);
//                    //在加之前先判断是否有乘
//                    if(g_WorkInfo.cOptionMark==1)
//                    {
//                        lngTemp=g_WorkInfo.lngTotalMoney+g_WorkInfo.lngPaymentMoney;
//                        if(lngTemp<=lngMoneyLim)
//                        {
//                            g_WorkInfo.lngTotalMoney=lngTemp;
//                        }
//                        else
//                        {
//                            Log.i(TAG,"输入错误");
//                            g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                            g_WorkInfo.cOptionMark=0;
//                            Publicfun.ClearPaymentInfo();
//                            SoundPlay.VoicePlay("inputpay_error");
//                            return 1;
//                        }
//                    }
//                    else
//                    {
//                        if(g_WorkInfo.lngTotalMoney==0)
//                        {
//                            g_WorkInfo.lngTotalMoney=g_WorkInfo.lngPaymentMoney;
//                        }
//                        else
//                        {
//                            if(g_WorkInfo.lngTotalMoney!=0)
//                            {
//                                if((g_WorkInfo.lngTotalMoney*g_WorkInfo.lngPaymentMoney)>=100)
//                                {
//                                    g_WorkInfo.lngTotalMoney=((g_WorkInfo.lngTotalMoney*g_WorkInfo.lngPaymentMoney)/100);
//                                    if(g_WorkInfo.lngTotalMoney>lngMoneyLim)
//                                    {
//                                        Log.i(TAG,"输入错误");
//                                        g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                                        g_WorkInfo.cOptionMark=0;
//                                        Publicfun.ClearPaymentInfo();
//                                        SoundPlay.VoicePlay("inputpay_error");
//                                        return 1;
//                                    }
//                                }
//                                else
//                                {
//                                    Log.i(TAG,"输入错误");
//                                    g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                                    g_WorkInfo.cOptionMark=0;
//                                    Publicfun.ClearPaymentInfo();
//                                    SoundPlay.VoicePlay("inputpay_error");
//                                    return 1;
//                                }
//                            }
//                        }
//                    }
//                    g_WorkInfo.cOptionMark=2;
//                    g_WorkInfo.lngPaymentMoney=0;
//                    g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                }
//                if(cdisk==KeyEvent.KEYCODE_NUMPAD_ADD)
//                {
//                    g_WorkInfo.cOptionMark=1;
//                }
//                if(cdisk==KeyEvent.KEYCODE_NUMPAD_MULTIPLY)
//                {
//                    g_WorkInfo.cOptionMark=2;
//                }
//                fill(g_WorkInfo.cPaymentMoney, (byte) 0);
//                g_WorkInfo.cPaymentMoneyLen=0;
//                g_WorkInfo.cPaymentDotPos=0;
//                //Publicfun.ClearPaymentInfo();
//                g_WorkInfo.cKeyDownState=1;
//            }
//            //加乘功能后,按了确定键
//            if((g_WorkInfo.cOptionMark!=0)&&(cdisk == KeyEvent.KEYCODE_ENTER))
//            {
//                Log.i(TAG,"加乘功能后,按了确定键");
//                if(g_WorkInfo.cOptionMark==1)
//                {
//                    Log.i(TAG,"累加");
//                    lngTemp=g_WorkInfo.lngTotalMoney+g_WorkInfo.lngPaymentMoney;
//                    if(lngTemp<=lngMoneyLim)
//                    {
//                        g_WorkInfo.lngTotalMoney=lngTemp;
//                    }
//                    else
//                    {
//                        Log.i(TAG,"输入错误");
//                        g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                        g_WorkInfo.cOptionMark=0;
//                        Publicfun.ClearPaymentInfo();
//                        SoundPlay.VoicePlay("inputpay_error");
//                        return 1;
//                    }
//                }
//                else if(g_WorkInfo.cOptionMark==2)
//                {
//                    Log.i(TAG,"乘法:"+g_WorkInfo.lngTotalMoney);
//                    if(g_WorkInfo.lngTotalMoney!=0)
//                    {
//                        if((g_WorkInfo.lngTotalMoney*g_WorkInfo.lngPaymentMoney)>=100)
//                        {
//                            g_WorkInfo.lngTotalMoney=((g_WorkInfo.lngTotalMoney*g_WorkInfo.lngPaymentMoney)/100);
//                            if(g_WorkInfo.lngTotalMoney>lngMoneyLim)
//                            {
//                                Log.i(TAG,"输入错误");
//                                g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                                g_WorkInfo.cOptionMark=0;
//                                Publicfun.ClearPaymentInfo();
//                                SoundPlay.VoicePlay("inputpay_error");
//                                return 1;
//                            }
//                        }
//                        else
//                        {
//                            Log.i(TAG,"输入错误");
//                            g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                            g_WorkInfo.cOptionMark=0;
//                            Publicfun.ClearPaymentInfo();
//                            SoundPlay.VoicePlay("inputpay_error");
//                            return 1;
//                        }
//                    }
//                    else
//                    {
//                        Log.i(TAG,"输入错误");
//                        g_CardBasicInfo.lngPayMoney=g_WorkInfo.lngPaymentMoney;
//                        g_WorkInfo.cOptionMark=0;
//                        Publicfun.ClearPaymentInfo();
//                        SoundPlay.VoicePlay("inputpay_error");
//                        return 1;
//                    }
//                }
//                g_WorkInfo.cKeyDownState=1;
//            }
//        }
//        //2018.0201
//        if(g_StationInfo.cPaymentUnit==1)
//        {
//            if(g_WorkInfo.lngPaymentMoney%10>1)
//            {
//                g_WorkInfo.lngPaymentMoney=(g_WorkInfo.lngPaymentMoney/10)*10;
//            }
//        }
//        else if(g_StationInfo.cPaymentUnit==2)
//        {
//            if(g_WorkInfo.lngPaymentMoney%100>1)
//            {
//                g_WorkInfo.lngPaymentMoney=(g_WorkInfo.lngPaymentMoney/100)*100;
//            }
//        }
//        return 0;
//    }
//
//    //单键数据方式
//    public static void OddKeyMenu(int cdisk)
//    {
//        if(g_WorkInfo.cSelfPressOk==0)
//        {
//            if(((cdisk-KeyEvent.KEYCODE_0)<=9)&&((cdisk-KeyEvent.KEYCODE_0>=0)))
//            {
//                g_WorkInfo.lngPaymentMoney=g_WorkInfo.lngPaymentMoney+g_OddKeyInfo.wKeyMoney[cdisk-KeyEvent.KEYCODE_0];
//                g_WorkInfo.cKeyDownState=1;
//            }
//            Log.i(TAG,"交易金额:"+g_WorkInfo.lngPaymentMoney);
//        }
//    }
//
//    //删除修改键功能
//    public static void DeleteKeyFun()
//    {
//        //清除交易金额
//        if(g_LocalInfo.cInputMode==1 || g_LocalInfo.cInputMode==2)
//        {
//            //g_WorkInfo.cPayDisPlayStatus=0;
//            //总的交易金额
//            g_WorkInfo.cOptionMark=0;
//            g_WorkInfo.cKeyDownState=0;
//            Publicfun.ClearPaymentInfo();
//        }
//        if((g_LocalInfo.cInputMode!=3)||((g_LocalInfo.cInputMode==3)&&(g_LocalInfo.cBookSureMode!=0)))
//        {
//            //Log.i(TAG,"结束识读");
//            g_Nlib.QR_SetDeviceReadEnable(2);
//            if(g_SystemInfo.cFaceDetectFlag==1)//启用人脸
//            {
//                FaceWorkTask.StartDetecte(false);
//            }
//        }
//        //显示
//        if(g_CardInfo.cExistState==0)
//        {
//            Publicfun.ShowCardInfo(g_CardBasicInfo,1);//显示
//        }
//        else
//        {
//            //Publicfun.ShowCardInfo(g_CardBasicInfo,2);//显示
//        }
//        g_WorkInfo.cSelfPressOk=0;//清除确定键
//        g_CardInfo.cAuthenState=0;//清除认证
//        g_CardInfo.cExistState=0;//卡不存在
//        fill(g_CardInfo.bCardSerTempID, (byte) 0x00);//清除卡号
//        //输入后等待结帐计数
//        g_WorkInfo.cOtherQRFlag=0;
//        g_WorkInfo.cInputKeyState=0;
//        g_WorkInfo.cCheckOutState=0;
//        g_WorkInfo.cQrCodestatus=0;
//        g_WorkInfo.cPayDisPlayStatus=0;
//        g_WorkInfo.cScanQRCodeFlag=0;
//        g_CommInfo.cQueryCardInfoStatus=0;
//        g_CommInfo.cGetQRCodeInfoStatus=0;
//        g_CommInfo.cLastRecDisInfoStatus=0;
//    }

}

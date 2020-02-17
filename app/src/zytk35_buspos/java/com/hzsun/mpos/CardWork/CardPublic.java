package com.hzsun.mpos.CardWork;

import android.util.Log;

import com.hzsun.mpos.Public.Publicfun;

import static com.hzsun.mpos.CardWork.CardApp.Burse_ResetEWalletProcess;
import static com.hzsun.mpos.Global.Global.g_BlackWList;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_EP_BurseInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Pos.Consume.WriteCardResetRecord;

public class CardPublic {

    private static final String TAG = "CardPublic";
    private static final int OK = 0;

    private static final int CARD_INVALIED = 1;            //无效卡,黑名单   1
    private static final int WRITE_CARD_ERROR = 62;            //写卡片（密码不正确或校验码错误）  62

    //判断钱包是否余额复位 工作钱包
    public static int CheckCardBurseReset() {
        int cResult;
        byte cBurseID;
        long lngBurseMoney = 0;
        long lngLastTransDate = 0;
        char cMoneyClearUnit;            //复位周期单位	1	0年 1月
        byte[] cMoneyClearTime = new byte[2];         //复位周期	1

        cBurseID = g_StationInfo.cWorkBurseID;
        lngBurseMoney = g_CardBasicInfo.lngWorkBurseMoney;
        lngLastTransDate = g_CardBasicInfo.iWorkLastPayDate;

        //是否允许钱包复位
        if (g_EP_BurseInfo.get(cBurseID - 1).cCanMoneyClear == 1)//是否允许余额周期复位	1	0不允许 1允许
        {
            if (lngBurseMoney > 0) {
                cMoneyClearUnit = (char) g_EP_BurseInfo.get(cBurseID - 1).cMoneyClearUnit;
                System.arraycopy(g_EP_BurseInfo.get(cBurseID - 1).cMoneyClearTime, 0, cMoneyClearTime, 0, 2);
                cResult = CompareResetDate(cMoneyClearUnit, cMoneyClearTime, lngLastTransDate);
                if (cResult == 1) {
                    Log.i(TAG, "需要余额复位");
                    return 1;
                }
            }
        }
        return 0;
    }

    //卡片钱包余额复位
    public static int CardResetProcess() {
        int cResult;
        byte cPaymentUnit = 0x07;
        long lngResetMoney = 0;
        long lngResetMoney_Mod = 0;

        //判断钱包是否余额复位
        cResult = CheckCardBurseReset();
        if (cResult == 1) {
            Log.i(TAG, "余额复位开始");
            cResult = Burse_ResetEWalletProcess(g_CardBasicInfo);
            if (cResult == OK) {
                //记录流水0x07-余额复位
                if (g_CardBasicInfo.lngPayMoney > 0xffff) {
                    lngResetMoney = g_CardBasicInfo.lngPayMoney / 100;
                    lngResetMoney_Mod = g_CardBasicInfo.lngPayMoney % 100;
                    if (lngResetMoney_Mod != 0) {
                        cResult = WriteCardResetRecord(cPaymentUnit, g_CardBasicInfo.iWorkBurseSID - 1, lngResetMoney_Mod);
                        if (cResult == OK) {
                            Log.i(TAG, "余额复位成功1");
                        }
                    }
                    cPaymentUnit = 67;
                    cResult = WriteCardResetRecord(cPaymentUnit, g_CardBasicInfo.iWorkBurseSID, lngResetMoney);
                    if (cResult == OK) {
                        Log.i(TAG, "余额复位成功2");
                        return OK;
                    }
                } else {
                    cResult = WriteCardResetRecord(cPaymentUnit, g_CardBasicInfo.iWorkBurseSID, g_CardBasicInfo.lngPayMoney);
                    if (cResult == OK) {
                        Log.i(TAG, "余额复位成功");
                        return OK;
                    }
                }
            }
            return cResult;
        }
        return OK;
    }

    //读黑白名单状态(根据内卡号)
    public static int ReadBWListState(long lngCardNumber) {
        int cResult;
        byte cBWTemp = 0;
        long lngBytePos;   //字节的位置
        byte cBitPos;       //位的位置

        //再读初始区域1000000
        if ((lngCardNumber > 300000) || (lngCardNumber == 0)) {
            Log.i(TAG, "超出最大卡内编号:" + lngCardNumber);
            return CARD_INVALIED;
        }

        lngBytePos = (lngCardNumber - 1) / 8;
        cBitPos = (byte) ((lngCardNumber - 1) % 8);

        Log.i(TAG, String.format("卡内编号:%d,lngBytePos:%d,cBitPos:%d", lngCardNumber, lngBytePos, cBitPos));
        cBWTemp = g_BlackWList.BlackBit[(int) lngBytePos];
        cResult = (cBWTemp >> cBitPos) & 0x01;
        //黑白名单类型:1,黑名单,2:白名单
        //1.采用黑名单
        if (g_StationInfo.cBWListClass == 1) {
            if (cResult == 0) {
                return OK;
            } else {
                return CARD_INVALIED;
            }
        } else {
            if (cResult == 1) {
                return OK;
            } else {
                return CARD_INVALIED;
            }
        }
    }

    //修改卡的状态
    public static int ModifyCardStatus(byte cCardState) {
        int cResult;
        //无效卡
        if (cCardState == 0) {
            if ((g_CardBasicInfo.cCardState & 0x01) == 0x01) {
                g_CardBasicInfo.cCardState = 0;
            } else {
                return OK;
            }
        }
        //有效卡
        if (cCardState == 1) {
            if ((g_CardBasicInfo.cCardState & 0x01) == 0x00) {
                g_CardBasicInfo.cCardState = 1;
            } else {
                return OK;
            }
        }

        Log.i(TAG, "改变卡状态");
        cResult = CardApp.WriteCardStatus(cCardState);
        if (cResult != OK) {
            Log.i(TAG, "改变卡状态-->失败:" + cResult);
            return WRITE_CARD_ERROR;
        }
        return OK;
    }

    //计算当前日期为第几天
    public static int GetWhichday(int y, int m, int d) {
        int year;
        int sum = 0;
        int i;
        byte[] Y = new byte[]{31, 0, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        if (m > 12)
            return 0;

        year = y + 2000;
        if (((year % 400) == 0) || (((year % 4) == 0) && ((year % 100) != 0))) {
            Y[1] = 29;
        } else {
            Y[1] = 28;
        }
        for (i = 0; i < m - 1; i++) {
            sum += Y[i];
        }
        sum += d;
        return sum;
    }


    //计算当前日期为第几季度
    public static int GetWhichQuarter(byte[] cCurrentDate) {
	/*
	第一季度：1月－3月
	第二季度：4月－6月
	第三季度：7月－9月
	第四季度：10月－12月
	*/

        byte cQuarter = 0;
        byte cMonth;
        cMonth = cCurrentDate[1];
        switch (cMonth) {
            case 1:
            case 2:
            case 3:
                cQuarter = 1;
                break;
            case 4:
            case 5:
            case 6:
                cQuarter = 2;
                break;
            case 7:
            case 8:
            case 9:
                cQuarter = 3;
                break;
            case 10:
            case 11:
            case 12:
                cQuarter = 4;
                break;

        }
        return cQuarter;
    }

    //计算当前日期为第几周
    public static int GetWhichWeek(byte[] cCurrentDate) {
        int iDay;
        int nweek, wd;

        iDay = GetWhichday(cCurrentDate[0], cCurrentDate[1], cCurrentDate[2]) - 1;
        nweek = iDay / 7 + 1;    //以1月1日那周为第1周

        //算出今天是星期几

        wd = GetCurWeek(cCurrentDate);
        if ((wd - (iDay % 7)) < 0)/*表示是新的一周。*/ {
            nweek++;
        }
        Log.i(TAG, "周号：" + nweek);
        return nweek;
    }

    //计算当前星期返回周几
    public static int GetCurWeek(byte[] cCurrentDate) {
        byte cWeek = 0;
        byte[] mdays = new byte[]{0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30};
        int i, y, month, days;

        y = (cCurrentDate[0] + 2000) - 1;
        month = cCurrentDate[1];
        days = cCurrentDate[2];
        for (i = 0; i < month; ++i) {
            days += mdays[i];
        }
        if (month > 2) {
//            if (((((y + 1) & 3) == 0) && (((y + 1) % 100)))) {
//                ++days;
//            } else {
//                if ((((y + 1) % 400) == 0)) {
//                    ++days;
//                }
//            }
        }
        cWeek = (byte) ((y + y / 4 - y / 100 + y / 400 + days) % 7);
        if (cWeek > 7) {
            return 0;
        }
        if (cWeek == 0) {
            cWeek = 7;
        }
        return cWeek;
    }


    //判断卡内时间的末笔交易时间是否是同一天
    public static int CompareResetDate(int iType, byte[] cResetDate, long lngLastTransDate) {
        int iPaymentDate;
        int iLastYear, iLastMonth, iLastDay;
        int iNowYear, iNowMonth, iNowDay;
        int iClearMonth, iClearDay;

        //1、从卡内的时间中分离出年、月、日
        //年
        iPaymentDate = (int) lngLastTransDate;
        iLastYear = (iPaymentDate & 0x7E00) >> 9;
        //月
        iPaymentDate = (int) lngLastTransDate;
        iLastMonth = (iPaymentDate & 0x01E0) >> 5;
        //日
        iPaymentDate = (int) lngLastTransDate;
        iLastDay = iPaymentDate & 0x001F;

        //从参数中分离出年、月、日
        iClearMonth = cResetDate[0];
        iClearDay = cResetDate[1];

        //当前年、月、日
        byte[] cCurDateTime = new byte[6];
        Publicfun.GetCurrDateTime(cCurDateTime);
        iNowYear = cCurDateTime[0];
        iNowMonth = cCurDateTime[1];
        iNowDay = cCurDateTime[2];

        Log.i(TAG, String.format("现在时间:年%d,月%d,日%d", iNowYear, iNowMonth, iNowDay));
        Log.i(TAG, String.format("清除时间:月%d,日%d", iClearMonth, iClearDay));
        Log.i(TAG, String.format("末笔时间:年%d,月%d,日%d", iLastYear, iLastMonth, iLastDay));

        //按年比较
        if (iType == 1) {
            //1、跨度是两年
            //2、末笔交易时间比清除时间小 并且 现在时间比清除时间大 则需要清除
            //相差>1
            if (iNowYear - iLastYear >= 1) {
                if (iNowYear - iLastYear > 1) {
                    return 1;
                } else {
                    //相差1年
                    if (iClearMonth >= iLastMonth) {
                        if (iClearMonth > iLastMonth) {
                            return 1;
                        } else {
                            if (iClearDay > iLastDay) {
                                return 1;
                            }
                        }
                    }

                    if (iNowMonth >= iClearMonth) {
                        if (iNowMonth > iClearMonth) {
                            return 1;
                        } else {
                            if (iNowDay >= iClearDay) {
                                return 1;
                            }
                        }
                    }
                }
            } else {
                //同一年
                if (iLastMonth <= iClearMonth) {
                    if (iLastMonth < iClearMonth) {
                        if (iNowMonth >= iClearMonth) {
                            if (iNowMonth > iClearMonth) {
                                return 1;
                            } else {
                                if (iNowDay >= iClearDay) {
                                    return 1;
                                }
                            }
                        }
                    } else {
                        if (iLastDay < iClearDay) {
                            if (iNowMonth > iClearMonth) {
                                return 1;
                            } else {
                                if (iNowDay >= iClearDay) {
                                    return 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        //按月比较
        else {
            if (iNowYear - iLastYear >= 1) {
                //相差>1年
                if (iNowYear - iLastYear > 1) {
                    return 1;
                } else {
                    //相差1年
                    if (iClearDay > iLastDay) {
                        return 1;
                    }
                    if (iNowDay >= iClearDay) {
                        return 1;
                    }
                }
            } else {
                //同一年
                if (iNowMonth - iLastMonth >= 1) {
                    if (iNowMonth - iLastMonth > 1) {
                        return 1;
                    } else //相差一个月
                    {
                        if (iClearDay > iLastDay) {
                            return 1;
                        }
                        if (iNowDay >= iClearDay) {
                            return 1;
                        }
                    }
                } else //同一个月
                {
                    if (iLastDay < iClearDay) {
                        if (iNowDay >= iClearDay) {
                            return 1;
                        }
                    }
                }
            }
        }
        return 0;
    }


    //将日期时间转化为卡里形式的时间(6位年4位月5位日5位时6位分6位秒) 
    public static long GetCurrentCardDateTime(byte[] cCurDateTime) {
        int iNowYear;
        int iNowMonth;
        int iNowDay;
        int iNowHour;
        int iNowMin;
        int iNowSec;

        long lngCardDateTime;
        byte[] cPaymentDate = new byte[4];

        //年
        iNowYear = cCurDateTime[0];
        //月
        iNowMonth = cCurDateTime[1];
        //日
        iNowDay = cCurDateTime[2];
        //时
        iNowHour = cCurDateTime[3];
        //分
        iNowMin = cCurDateTime[4];
        //秒
        iNowSec = cCurDateTime[5];

//    Log.i(TAG,"末笔消费日期-当前时间：%d年%d月%d日%d时%d分%d秒",cCurDateTime[0],
//                cCurDateTime[1],cCurDateTime[2],cCurDateTime[3],cCurDateTime[4],
//                cCurDateTime[5]);

        //处理成卡内格式的时间
        cPaymentDate[3] = (byte) (iNowYear << 2);
        cPaymentDate[3] = (byte) (cPaymentDate[3] | iNowMonth >> 2);

        cPaymentDate[2] = (byte) (iNowMonth << 6);
        cPaymentDate[2] = (byte) (cPaymentDate[2] | iNowDay << 1);
        cPaymentDate[2] = (byte) (cPaymentDate[2] | iNowHour >> 4);

        cPaymentDate[1] = (byte) (iNowHour << 4);
        cPaymentDate[1] = (byte) (cPaymentDate[1] | iNowMin >> 2);

        cPaymentDate[0] = (byte) (iNowMin << 6);
        cPaymentDate[0] = (byte) (cPaymentDate[0] | iNowSec);

        //Log.i(TAG,"%02x",cPaymentDate[0]);
        //Log.i(TAG,"%02x",cPaymentDate[1]);
        //Log.i(TAG,"%02x",cPaymentDate[2]);
        //Log.i(TAG,"%02x",cPaymentDate[3]);
        lngCardDateTime = (cPaymentDate[0] & 0xff)
                + (cPaymentDate[1] & 0xff) * 256
                + (cPaymentDate[2] & 0xff) * 256 * 256
                + (cPaymentDate[3] & 0xff) * 256 * 256 * 256;

        return lngCardDateTime;
    }

    //将日期时间转化为卡里形式的时间(按日：日期 7位年4位月5位日 ) 
    public static int GetCurrentCardDate(byte[] cCurrentDate) {
        int iNowYear;
        int iNowMonth;
        int iNowDay;
        int wCardDate;
        byte[] cPaymentDate = new byte[2];

        Log.i(TAG, String.format("时间：%d年%d月%d日", cCurrentDate[0], cCurrentDate[1], cCurrentDate[2]));
        //年
        iNowYear = cCurrentDate[0];
        //月
        iNowMonth = cCurrentDate[1];
        //日
        iNowDay = cCurrentDate[2];

        //处理成卡内格式的时间
        cPaymentDate[1] = (byte) (iNowYear << 1);
        cPaymentDate[1] = (byte) (cPaymentDate[1] | iNowMonth >> 3);

        cPaymentDate[0] = (byte) (iNowMonth << 5);
        cPaymentDate[0] = (byte) (cPaymentDate[0] | iNowDay);

        Log.i(TAG, String.format("卡内限次周期：%02x.%02x", cPaymentDate[0], cPaymentDate[1]));
        wCardDate = (cPaymentDate[0] & 0xff) + (cPaymentDate[1] & 0xff) * 256;

        return wCardDate;
    }

    //
//    //将日期时间转化为卡里形式的时间按时段：( 5位月5位日6位时段号 )
//    public static  int  GetCurrCardBusiness(byte[] cCurrentDate, byte cBusinessID)
//    {
//        byte  cYear;
//        byte  cNowMonth;
//        byte  cNowDay;
//        byte  cNowHour;
//        byte  cNowMin;
//        byte[]  cEndTime=new byte[2];
//        byte  wEndTime;
//        int wNowtime;
//        int wCardDate;
//        byte[]  cPaymentDate=new byte[2];
//
//        cYear=cCurrentDate[0];
//        cNowMonth=cCurrentDate[1];
//        cNowDay=cCurrentDate[2];
//        cNowHour=cCurrentDate[3];
//        cNowMin=cCurrentDate[4];
//        wNowtime=cNowHour*60+cNowMin;
//
//        memcpy(cEndTime,s_BuinessInfo[(cBusinessID-1)%6].cEndTime,2);
//        ChangeToHexA(cEndTime,2);
//        wEndTime=cEndTime[0]*60+cEndTime[1];
//
//        //时段
//        if(s_cIsInterDay==1)
//        {
//            if((wNowtime>0)&&(wNowtime<=wEndTime))
//            {
//                if(cNowDay!=1)
//                {
//                    cNowDay=cNowDay-1;
//                }
//                else
//                {
//                    switch(cNowMonth)
//                    {
//                        case 1:
//                            cNowDay=31;
//                            cNowMonth=12;
//                            break;
//                        case 2:
//                            cNowDay=31;
//                            cNowMonth=1;
//                            break;
//                        case 3:
//                            if(ISLEAPYEAR(cYear))
//                            {
//                                cNowDay=29;
//                            }
//                            else
//                            {
//                                cNowDay=28;
//                            }
//                            cNowMonth=2;
//                            break;
//                        case 4:
//                            cNowDay=31;
//                            cNowMonth=3;
//                            break;
//                        case 5:
//                            cNowDay=30;
//                            cNowMonth=4;
//                            break;
//                        case 6:
//                            cNowDay=31;
//                            cNowMonth=5;
//                            break;
//                        case 7:
//                            cNowDay=30;
//                            cNowMonth=6;
//                            break;
//                        case 8:
//                            cNowDay=31;
//                            cNowMonth=7;
//                            break;
//                        case 9:
//                            cNowDay=31;
//                            cNowMonth=8;
//                            break;
//                        case 10:
//                            cNowDay=30;
//                            cNowMonth=9;
//                            break;
//                        case 11:
//                            cNowDay=31;
//                            cNowMonth=10;
//                            break;
//                        case 12:
//                            cNowDay=30;
//                            cNowMonth=11;
//                            break;
//                    }
//                }
//            }
//        }
//
//        //处理成卡内格式的时间
//        cPaymentDate[1]=cNowMonth<<3;
//        cPaymentDate[1]=cPaymentDate[1]|cNowDay>>2;
//
//        cPaymentDate[0]=cNowDay<<6;
//        cPaymentDate[0]=cPaymentDate[0]|cBusinessID;
//
////    Log.i(TAG,"现在:%d月%d日%d时段号",cNowMonth,cNowDay,cBusinessID);
////    Log.i(TAG,"写入卡内:%02x.%02x",cPaymentDate[1],cPaymentDate[0]);
//
//        wCardDate=(cPaymentDate[0]&0xff)+(cPaymentDate[1]&0xff)*256;
//
//        return wCardDate;
//    }
//
//    //钱包余额复位按年复位(返回1为需要复位)
//    public static  int  CompareResetYear(long lngLastTransDate,byte[] cResetDate)
//    {
//
//        byte iLastYear,iLastMonth,iLastDay;
//        byte iNowYear,iNowMonth,iNowDay;
//        byte iClearMonth,iClearDay;
//
//        //从参数中分离出年、月、日
//        iClearMonth= (byte) (((cResetDate[0]/16)*10)+(cResetDate[0]%16));
//        iClearDay= (byte) (((cResetDate[1]/16)*10)+(cResetDate[1]%16));
//
//        if((iClearMonth==0)||(iClearDay==0))
//        {
//            return 0;
//        }
//
//        //从终端设备中分离出年、月、日
//        iNowYear=s_cCurDateTime[0];
//        iNowMonth=s_cCurDateTime[1];
//        iNowDay=s_cCurDateTime[2];
//
//        //从卡内的时间中分离出年、月、日
//        //年
//        iLastYear=(lngLastTransDate&0xFC000000)>>26;
//        //月
//        iLastMonth=(lngLastTransDate&0x03C00000)>>22;
//        //日
//        iLastDay=(lngLastTransDate&0x003E0000)>>17;
//
//
//        Log.i(TAG,"现在时间:年%d,月%d,日%d",iNowYear,iNowMonth,iNowDay);
//
//        Log.i(TAG,"清除时间:月%d,日%d",iClearMonth,iClearDay);
//
//        Log.i(TAG,"末笔时间:年%d,月%d,日%d",iLastYear,iLastMonth,iLastDay);
//
//
//        //按年比较
//
//        //跨度是两年
//        //末笔交易时间比清除时间小 并且 现在时间比清除时间大 则需要清除
//        //相差>1
//        if(iNowYear-iLastYear>=1)
//        {
//            if(iNowYear-iLastYear>1)
//            {
//                return 1;
//            }
//            else
//            {
//                //相差1年
//                if(iClearMonth>=iLastMonth)
//                {
//                    if(iClearMonth>iLastMonth)
//                    {
//                        return 1;
//                    }
//                    else
//                    {
//                        if(iClearDay>iLastDay)
//                        {
//                            return 1;
//                        }
//                    }
//                }
//
//                if(iNowMonth>=iClearMonth)
//                {
//                    if(iNowMonth>iClearMonth)
//                    {
//                        return 1;
//                    }
//                    else
//                    {
//                        if(iNowDay>=iClearDay)
//                        {
//                            return 1;
//                        }
//                    }
//                }
//            }
//        }
//        else
//        {
//            //同一年
//            if(iLastMonth<=iClearMonth)
//            {
//                if(iLastMonth<iClearMonth)
//                {
//                    if(iNowMonth>=iClearMonth)
//                    {
//                        if(iNowMonth>iClearMonth)
//                        {
//                            return 1;
//                        }
//                        else
//                        {
//                            if(iNowDay>=iClearDay)
//                            {
//                                return 1;
//                            }
//                        }
//                    }
//                }
//                else
//                {
//                    if(iLastDay<iClearDay)
//                    {
//                        if(iNowMonth>iClearMonth)
//                        {
//                            return 1;
//                        }
//                        else
//                        {
//                            if(iNowDay>=iClearDay)
//                            {
//                                return 1;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return 0;
//    }
//
//    //钱包余额复位按月复位(返回1为需要复位)
//    public static  int  CompareResetMonth(long lngLastTransDate,byte cResetDate)
//    {
//
//        byte iLastYear,iLastMonth,iLastDay;
//        byte iNowYear,iNowMonth,iNowDay;
//        byte iClearDay;
//
//        //复位时间点CN 1，DD，不启用是为00，每月最后一天是为32
//        iClearDay= (byte) (((cResetDate/16)*10)+(cResetDate%16));
//        if(iClearDay==0)
//        {
//            return 0;
//        }
//
//
//        byte[] cCurDateTime=new byte[6];
//        Publicfun.GetCurrDateTime(cCurDateTime);
//        //从终端设备中分离出年、月、日
//        iNowYear=cCurDateTime[0];
//        iNowMonth=cCurDateTime[1];
//        iNowDay=cCurDateTime[2];
//
//        //从卡内的时间中分离出年、月、日
//        //年
//        iLastYear= (byte) ((lngLastTransDate&0xFC000000)>>26);
//        //月
//        iLastMonth= (byte) ((lngLastTransDate&0x03C00000)>>22);
//        //日
//        iLastDay= (byte) ((lngLastTransDate&0x003E0000)>>17);
//
//
////        Log.i(TAG,"现在时间:年%d,月%d,日%d",iNowYear,iNowMonth,iNowDay);
////
////        Log.i(TAG,"清除时间:日%d",iClearDay);
////
////        Log.i(TAG,"末笔时间:年%d,月%d,日%d",iLastYear,iLastMonth,iLastDay);
//
//
//        //按月比较
//        if(iNowYear-iLastYear>=1)
//        {
//            //相差>1年
//            if(iNowYear-iLastYear>1)
//            {
//                return 1;
//            }
//            else
//            {
//                //相差1年
//                if(iClearDay>iLastDay)
//                {
//                    return 1;
//                }
//                if(iNowDay>=iClearDay)
//                {
//                    return 1;
//                }
//            }
//        }
//        else
//        {
//            //同一年
//            if(iNowMonth-iLastMonth>=1)
//            {
//                if(iNowMonth-iLastMonth>1)
//                {
//                    return 1;
//                }
//                else //相差一个月
//                {
//                    if(iClearDay>iLastDay)
//                    {
//                        return 1;
//                    }
//                    if(iNowDay>=iClearDay)
//                    {
//                        return 1;
//                    }
//                }
//            }
//            else //同一个月
//            {
//                if(iLastDay<iClearDay)
//                {
//                    if(iNowDay>=iClearDay)
//                    {
//                        return 1;
//                    }
//                }
//            }
//        }
//
//        return 0;
//    }
//
//    //钱包余额复位按周复位(返回1为需要复位)
//    public static  int  CompareResetWeek(long lngLastTransDate,byte cResetDate)
//    {
//        byte[] cLastDate=new byte[3];
//        byte iLastYear,iLastMonth,iLastDay,iLastWeek,iLastWeekDay;
//        byte iNowYear,iNowMonth,iNowDay,iNowWeek,iNowWeekDay;
//        byte iClearWeekDay;
//
//        //从参数中分离出年、月、日
//        //按周复位时间点值范围1~7，分别指周一到周日。
//        iClearWeekDay=cResetDate;
//
//        //从终端设备中分离出年、月、日
//        iNowYear=s_cCurDateTime[0];
//        iNowMonth=s_cCurDateTime[1];
//        iNowDay=s_cCurDateTime[2];
//        iNowWeek=GetWhichWeek(s_cCurDateTime);
//        iNowWeekDay=GetCurWeek(s_cCurDateTime);
//
//        //从卡内的时间中分离出年、月、日
//        //年
//        iLastYear=(lngLastTransDate&0xFC000000)>>26;
//        //月
//        iLastMonth=(lngLastTransDate&0x03C00000)>>22;
//        //日
//        iLastDay=(lngLastTransDate&0x003E0000)>>17;
//        cLastDate[0]=iLastYear;
//        cLastDate[1]=iLastMonth;
//        cLastDate[2]=iLastDay;
//        iLastWeek=GetWhichWeek(cLastDate);
//        iLastWeekDay=GetCurWeek(cLastDate);
//
//        Log.i(TAG,"现在时间:年%d,月%d,日%d,%d周,周%d",iNowYear,iNowMonth,iNowDay,iNowWeek,iNowWeekDay);
//
//        Log.i(TAG,"清除时间:周%d",iClearWeekDay);
//
//        Log.i(TAG,"末笔时间:年%d,月%d,日%d,%d周,周%d",iLastYear,iLastMonth,iLastDay,iLastWeek,iLastWeekDay);
//
//
//        //按月比较
//        if(iNowYear-iLastYear>=1)
//        {
//            if(iNowYear-iLastYear>1)
//            {
//                return 1;
//            }
//            else
//            {
//                //相差一年
//                if(iNowMonth+12-iLastMonth>1)
//                {
//                    return 1;
//                }
//                else
//                {
//                    if(iNowWeek+52-iLastWeek>1)
//                    {
//                        return 1;
//                    }
//                    else
//                    {
//                        if((iLastWeekDay<iClearWeekDay)&&(iClearWeekDay<=iNowWeekDay))
//                        {
//                            return 1;
//                        }
//                    }
//
//                }
//            }
//        }
//        else
//        {
//            //同一年
//            if(iNowMonth-iLastMonth>=1)
//            {
//                return 1;
//            }
//            else
//            {
//                //同一个月
//                if(iNowWeek-iLastWeek>=1)
//                {
//                    return 1;
//                }
//                else
//                {
//                    //同一个周
//                    if((iLastWeekDay<iClearWeekDay)&&(iClearWeekDay<=iNowWeekDay))
//                    {
//                        return 1;
//                    }
//                }
//            }
//        }
//
//        return 0;
//    }
//
//    //钱包余额复位按日复位(返回1为需要复位)
//    public static  int  CompareResetDay(long lngLastTransDate)
//    {
//
//        byte iLastYear,iLastMonth,iLastDay;
//        byte iNowYear,iNowMonth,iNowDay;
//
//
//        //从终端设备中分离出年、月、日
//        iNowYear=s_cCurDateTime[0];
//        iNowMonth=s_cCurDateTime[1];
//        iNowDay=s_cCurDateTime[2];
//
//        //从卡内的时间中分离出年、月、日
//        //年
//        iLastYear=(lngLastTransDate&0xFC000000)>>26;
//        //月
//        iLastMonth=(lngLastTransDate&0x03C00000)>>22;
//        //日
//        iLastDay=(lngLastTransDate&0x003E0000)>>17;
//
//        Log.i(TAG,"现在时间:年%d,月%d,日%d",iNowYear,iNowMonth,iNowDay);
//        Log.i(TAG,"末笔时间:年%d,月%d,日%d",iLastYear,iLastMonth,iLastDay);
//
//
//        if(iNowYear>iLastYear)
//        {
//            return 1;
//        }
//        else
//        {
//            if(iNowMonth>iLastMonth)
//            {
//                return 1;
//            }
//            else
//            {
//                if(iNowDay>iLastDay)
//                {
//                    return 1;
//                }
//            }
//        }
//
//        return 0;
//    }
//
//
//    //周期复位判断(返回1为需要复位)
//    public static  int  ComparePeriodReset(byte cType,long lngLastDate)
//    {
//        byte[] cLastDate=new byte[3];
//        byte iLastYear,iLastMonth,iLastDay,iLastWeek,iLastWeekDay;
//        byte iNowYear,iNowMonth,iNowDay,iNowWeek,iNowWeekDay;
//
//
//        //从终端设备中分离出年、月、日
//        iNowYear=s_cCurDateTime[0];
//        iNowMonth=s_cCurDateTime[1];
//        iNowDay=s_cCurDateTime[2];
//        iNowWeek=GetWhichWeek(s_cCurDateTime);
//        iNowWeekDay=GetCurWeek(s_cCurDateTime);
//
//        //从卡内的时间中分离出年、月、日
//        //年
//        iLastYear=(lngLastDate&0xFC000000)>>26;
//        //月
//        iLastMonth=(lngLastDate&0x03C00000)>>22;
//        //日
//        iLastDay=(lngLastDate&0x003E0000)>>17;
//        cLastDate[0]=iLastYear;
//        cLastDate[1]=iLastMonth;
//        cLastDate[2]=iLastDay;
//        iLastWeek=GetWhichWeek(cLastDate);
//        iLastWeekDay=GetCurWeek(cLastDate);
//
//
//        Log.i(TAG,"现在时间:年%d,月%d,日%d,%d周,星期%d",iNowYear,iNowMonth,iNowDay,iNowWeek,iNowWeekDay);
//
//        Log.i(TAG,"末笔时间:年%d,月%d,日%d,%d周,星期%d",iLastYear,iLastMonth,iLastDay,iLastWeek,iLastWeekDay);
//
//        switch(cType)	//周期类别
//        {
//            case 1:
//                Log.i(TAG,"按年");
//                if((iNowYear-iLastYear>1)
//                        ||(((iNowYear-iLastYear)==1)&&((iNowMonth-iLastMonth)>1))
//                        ||(((iNowYear-iLastYear)==1)&&(iNowMonth>=iLastMonth)&&(iNowDay>=iLastDay)))
//                {
//                    return 1;
//                }
//
//                break;
//
//            case 2:
//                Log.i(TAG,"按月");
//                if((iNowYear-iLastYear>1)
//                        ||(((iNowYear-iLastYear)==1)&&(iNowDay>=iLastDay))
//                        ||(((iNowYear-iLastYear)==0)&&((iNowMonth-iLastMonth)>=1)&&(iNowDay>=iLastDay)))
//                {
//                    return 1;
//                }
//
//                break;
//
//            case 3:
//                Log.i(TAG,"按周");
//                if((iNowYear-iLastYear>1)
//                        ||(((iNowYear-iLastYear)==1)&&(iNowWeekDay>=iLastWeekDay))
//                        ||(((iNowYear-iLastYear)==0)&&((iNowWeek-iLastWeek)>=1)&&(iNowWeekDay>=iLastWeekDay)))
//                {
//                    return 1;
//                }
//
//                break;
//
//            case 4:
//                Log.i(TAG,"按日");
//                if((iNowYear-iLastYear>0)
//                        ||(((iNowYear-iLastYear)==0)&&(iNowMonth>iLastMonth))
//                        ||(((iNowYear-iLastYear)==0)&&(iNowMonth==iLastMonth)&&(iNowDay>=iLastDay)))
//                {
//                    return 1;
//                }
//
//                break;
//        }
//        return 0;
//    }
//
    //判断日期是否一致(6位年4位月5位日)
    public static int CompareDateSame(long LastPayDate, byte[] cCurrentDate) {

        int NowYear;
        int NowMonth;
        int NowDay;
        int CardYear;
        int CardMonth;
        int CardDay;
        //int CardWhichDay;
        //int NowWhichDay;

        CardYear = (int) ((LastPayDate & 0x7E00) >> 9);
        CardMonth = (int) ((LastPayDate & 0x01E0) >> 5);
        CardDay = (int) (LastPayDate & 0x001F);

        NowYear = cCurrentDate[0];
        NowMonth = cCurrentDate[1];
        NowDay = cCurrentDate[2];

//        Log.i(TAG,String.format("卡内日期6：%d年%d月%d日",CardYear,CardMonth,CardDay));
//        Log.i(TAG,String.format("现在日期6：%d年%d月%d日",NowYear,NowMonth,NowDay));
        //判断是否是同一天
        if ((CardYear == NowYear) && (CardMonth == NowMonth) && (CardDay == NowDay)) {
            return 0;
        } else {
            return 1;
        }
    }

    //
//    //比较末笔交易时间
//    public static  int  CompareLastDate(long LastPayDate)
//    {
//        int NowYear;
//        int NowMonth;
//        int NowDay;
//        int CardYear;
//        int CardMonth;
//        int CardDay;
//
//        CardYear=(LastPayDate&0xFD000000)>>26;
//        CardMonth=(LastPayDate&0x03D00000)>>22;
//        CardDay=(LastPayDate&0x003E0000)>>17;
//
//        NowYear=s_cCurDateTime[0];
//        NowMonth=s_cCurDateTime[1];
//        NowDay=s_cCurDateTime[2];
//
//        //判断是否是同一天
//        if((CardYear==NowYear) && (CardMonth==NowMonth) && (CardDay==NowDay))
//        {
//            return 0;
//        }
//        return 1;
//    }
//
//
//
//    //判断营业时段是否一致(按时段：时段号 5位月5位日6位时段号 )
//    public static  int  CompareBusinessIDSame(int wBusinessTime, byte[] cCurrentDate, byte BusinessID)
//    {
//
//        int NowMonth;
//        int NowDay;
//        int CardMonth;
//        int CardDay;
//        int CardBusinessID;
//
//
//        CardMonth=wBusinessTime>>11;
//        CardDay=(wBusinessTime&0x07C0)>>6;
//        CardBusinessID=(wBusinessTime&0x003f);
//        Log.i(TAG,"卡内：%d月,%d日,%d时段号",CardMonth,CardDay,CardBusinessID);
//        NowMonth=cCurrentDate[1];
//        NowDay=cCurrentDate[2];
//        Log.i(TAG,"现在：%d月,%d日,%d时段号",NowMonth,NowDay,BusinessID);
//
//        //判断是否同一个时段(根据营业分组参数判断)
//        if( (CardMonth==NowMonth) && (CardDay==NowDay) &&(CardBusinessID==BusinessID))
//        {
//            //同一时段
//            return 0;
//        }
//
//        return 1;
//    }
//
//    public static  int  CompareInterBusinessIDSame(int wBusinessTime,long lngLastPayTime,byte[] cCurrentDate,byte cBusinessID)
//    {
//        byte  cResult;
//        byte  cStartTime[2];
//        byte  cEndTime[2];
//        int wStartTime;
//        int wEndTime;
//        int wLastPaymentTime;
//        byte  CardYear;
//        byte  CardMonth;
//        byte  CardDay;
//        byte  CardHour;
//        byte  CardMin;
//        byte  CardBusinessID;
//        byte  cNowYear;
//        byte  cNowMonth;
//        byte  cNowDay;
//        byte  cNowHour;
//        byte  cNowMin;
//        int wNowTime;
//        long CardWhichDay;
//        long NowWhichDay;
//
//        CardYear=(lngLastPayTime&0xFC000000)>>26;
//        CardMonth=(lngLastPayTime&0x03C00000)>>22;
//        CardDay=(lngLastPayTime&0x003E0000)>>17;
//        CardHour=(lngLastPayTime&0x0001F000)>>12;
//        CardMin=(lngLastPayTime&0x000000FC0)>>6;
//        CardBusinessID=(wBusinessTime&0x003f);
//
//        cNowYear=cCurrentDate[0];
//        cNowMonth=cCurrentDate[1];
//        cNowDay=cCurrentDate[2];
//        cNowHour=cCurrentDate[3];
//        cNowMin=cCurrentDate[4];
//        wNowTime=cNowHour*60+cNowMin;
//
//        memcpy(cStartTime,s_BuinessInfo[(s_cBusinessID-1)%6].cStartTime,2);
//        ChangeToHexA(cStartTime,2);
//        wStartTime=cStartTime[0]*60+cStartTime[1];
//
//        memcpy(cEndTime,s_BuinessInfo[(s_cBusinessID-1)%6].cEndTime,2);;
//        ChangeToHexA(cEndTime,2);
//        wEndTime=cEndTime[0]*60+cEndTime[1];
//
//        wLastPaymentTime=CardHour*60+CardMin;
//
//        if((CardYear==cNowYear) && (CardMonth==cNowMonth) && (CardDay==cNowDay))
//        {
//            if(s_cBusinessID==CardBusinessID)
//            {
//                if((wLastPaymentTime>=wStartTime)&&(wLastPaymentTime<=(23*60+59)))
//                {
//                    if((wNowTime>=wStartTime)&&(wNowTime<=(23*60+59)))
//                    {
//                        return 0;
//                    }
//                    else
//                    {
//                        return 1;
//                    }
//                }
//                if((wLastPaymentTime>=0)&&(wLastPaymentTime<=wEndTime))
//                {
//                    if((wNowTime>=0)&&(wNowTime<=wEndTime))
//                    {
//                        return 0;
//                    }
//                    else
//                    {
//                        return 1;
//                    }
//                }
//
//            }
//            else
//            {
//                return 1;
//            }
//        }
//        else
//        {
//            if(CardYear==cNowYear)
//            {
//                CardWhichDay=GetWhichday(CardYear,CardMonth,CardDay);
//                NowWhichDay=GetWhichday(cNowYear,cNowMonth,cNowDay);
//                if(NowWhichDay-CardWhichDay==1)
//                {
//                    if(s_cBusinessID==CardBusinessID)
//                    {
//                        if((wLastPaymentTime>=wStartTime)&&(wLastPaymentTime<=(23*60+59)))
//                        {
//                            if((wNowTime>=0)&&(wNowTime<=wEndTime))
//                            {
//                                return 0;
//                            }
//                            else
//                            {
//                                return 1;
//                            }
//                        }
//                        if((wLastPaymentTime>=0)&&(wLastPaymentTime<=wEndTime))
//                        {
//                            return 1;
//                        }
//                    }
//                    else
//                    {
//                        return 1;
//                    }
//                }
//                else
//                {
//                    return 1;
//                }
//            }
//            else
//            {
//                CardWhichDay=GetWhichday(CardYear,CardMonth,CardDay);
//                NowWhichDay=GetWhichday(cNowYear,cNowMonth,cNowDay);
//                if(ISLEAPYEAR(CardYear))
//                {
//                    if((NowWhichDay+366-CardWhichDay)==1)
//                    {
//                        if(s_cBusinessID==CardBusinessID)
//                        {
//                            if((wLastPaymentTime>=wStartTime)&&(wLastPaymentTime<=(23*60+59)))
//                            {
//                                if((wNowTime>=0)&&(wNowTime<=wEndTime))
//                                {
//                                    return 0;
//                                }
//                                else
//                                {
//                                    return 1;
//                                }
//                            }
//                            if((wLastPaymentTime>=0)&&(wLastPaymentTime<=wEndTime))
//                            {
//                                return 1;
//                            }
//                        }
//                        else
//                        {
//                            return 1;
//                        }
//                    }
//                    else
//                    {
//                        return 1;
//                    }
//                }
//                else
//                {
//                    if((NowWhichDay+365-CardWhichDay)==1)
//                    {
//                        if(s_cBusinessID==CardBusinessID)
//                        {
//                            if((wLastPaymentTime>=wStartTime)&&(wLastPaymentTime<=(23*60+59)))
//                            {
//                                if((wNowTime>=0)&&(wNowTime<=wEndTime))
//                                {
//                                    return 0;
//                                }
//                                else
//                                {
//                                    return 1;
//                                }
//                            }
//                            if((wLastPaymentTime>=0)&&(wLastPaymentTime<=wEndTime))
//                            {
//                                return 1;
//                            }
//                        }
//                        else
//                        {
//                            return 1;
//                        }
//                    }
//                    else
//                    {
//                        return 1;
//                    }
//                }
//            }
//        }
//    }
//
//
//    //判断卡内的园区号是否在范围内
//    public static  int  CampusAreaVaild(int wCampusID)
//    {
//        int i,j;
//        int cResult;
//        //园区范围组
//        i=(wCampusID-1)/8;
//        //取具体的值
//        j=(wCampusID-1)%8;
//        cResult=(s_StationInfo.bCampusArea[i]>>j)&0x01;
//        if(cResult==1)
//        {
//            return 1;
//        }
//        return 0;
//    }
//
//
//    //获取当前卡片的周期号
//    public static  int  GetCurrCardCycleNum(byte[] cCurDateTime,byte cBusinessID, byte cMode)
//    {
//
//        int wCysNum;	//周期号按时段：时段次号(按时段：时段号 4位月5位日6位时段号 )
//        //按日：日期，验证末笔交易日期。日期 6位年4位月5位日
//        //按周：周号
//        //按月：月号
//        //按季：季号
//        //按年：年号
//
//        switch(cMode)
//        {
//            case 1://按时段
//                wCysNum=GetCurrCardBusiness(cCurDateTime,cBusinessID);
//                break;
//            case 2://按日
//                wCysNum=GetCurrentCardDate(cCurDateTime);
//                break;
//            case 3://按周
//                wCysNum=GetWhichWeek(cCurDateTime);
//                break;
//            case 4: //按月
//                wCysNum=cCurDateTime[1];
//                break;
//            case 5://按季度
//                wCysNum=GetWhichQuarter(cCurDateTime);
//                break;
//            case 6: //按年
//                wCysNum=cCurDateTime[0];
//                break;
//        }
//
//        return wCysNum;
//    }
//
//    //是否允许优惠
//    public static  int  BusinessCanPriv(void)
//    {
//        //不启用优惠限次
//        if(s_StatusWorkBurInfo.cPrivLimitOne==0)
//        {
//            Log.i(TAG,"不启动优惠限次");
//            return 1;
//        }
//        //启用优惠限次
//        if(s_StatusWorkBurInfo.cPrivLimitOne==1)
//        {
//            Log.i(TAG,"工作营业号:%02x,卡片末笔营业号:%02x",s_cBusinessID,s_CardBurseInfo.iLastBusinessID);
//            if((s_cBusinessID!=(s_CardBurseInfo.iLastBusinessID&0x7F))||((s_CardBurseInfo.iLastBusinessID&0x80)!=0x80))
//            {
//                //未优惠
//                Log.i(TAG,"未优惠");
//                return 1;
//            }
//        }
//        return 0;
//    }
//
//
//    //计算优惠金额
//    public static  long  ComputePriMoney(long lngInPayMoney,long lngPrivilegeValue,byte  cPrivilegeMode)
//    {
//
//        long lngOutPayMoney;
//        long lngPriMoney;
//
//        lngOutPayMoney=lngInPayMoney;
//        lngPriMoney=0;
//
//        switch(cPrivilegeMode)
//        {
//            case 1:
//                //折扣优惠
//                Log.i(TAG,"折扣优惠");
//                lngPriMoney=lngInPayMoney*lngPrivilegeValue/100;
//                lngOutPayMoney=lngInPayMoney-lngPriMoney;
//                break;
//            case 2:
//                //定额优惠
//                Log.i(TAG,"定额优惠");
//                lngPriMoney=lngPrivilegeValue;
//                lngOutPayMoney=lngInPayMoney-lngPriMoney;
//                if(lngOutPayMoney<0)
//                {
//                    lngOutPayMoney=0;
//                    lngPriMoney=lngInPayMoney;
//                }
//                break;
//            case 3:
//                //定额消费
//                Log.i(TAG,"定额消费");
//                lngPriMoney=lngOutPayMoney-lngPrivilegeValue;
//                if(lngPriMoney<0)
//                {
//                    lngPriMoney=0;
//                }
//                lngOutPayMoney=lngPrivilegeValue;
//                break;
//        }
//
//        s_lngPayMoney=lngOutPayMoney;
//        Log.i(TAG,"优惠金额：%d",lngPriMoney);
//        return lngPriMoney;
//
//    }
//
//
//
    //正负数的钱包余额处理
    public static long TransMoney(byte[] cMoneyContext) {
        byte bTemp;
        long lngTemp;
        byte[] cMoneyTmp = new byte[3];

        System.arraycopy(cMoneyContext, 0, cMoneyTmp, 0, cMoneyTmp.length);
        //钱包的余额(在这里正负的处理)
        if ((cMoneyTmp[2] & 0x80) == 0x80) {
            bTemp = cMoneyTmp[0];
            bTemp = (byte) ~bTemp;
            lngTemp = (bTemp & 0xff);

            bTemp = cMoneyTmp[1];
            bTemp = (byte) ~bTemp;
            lngTemp = lngTemp + (bTemp & 0xff) * 256;

            bTemp = cMoneyTmp[2];
            bTemp = (byte) ~bTemp;
            lngTemp = lngTemp + (bTemp & 0xff) * 256 * 256;

            lngTemp = lngTemp + 1;
            lngTemp = -lngTemp;
        } else {
            lngTemp = (cMoneyTmp[0] & 0xff);
            lngTemp = lngTemp + (cMoneyTmp[1] & 0xff) * 256;
            lngTemp = lngTemp + (cMoneyTmp[2] & 0xff) * 256 * 256;
        }
        return lngTemp;
    }

    //密钥转换
    public static byte[] PwdLongtoByte(long lngPaymentPsw)
    {
        byte[] cTradePWD=new byte[8];
        cTradePWD[0] = (byte) ((lngPaymentPsw / 100000) + 0x30);
        cTradePWD[1] = (byte) (((lngPaymentPsw / 10000) % 10) + 0x30);
        cTradePWD[2] = (byte) (((lngPaymentPsw / 1000) % 100 % 10) + 0x30);
        cTradePWD[3] = (byte) (((lngPaymentPsw / 100) % 1000 % 100 % 10) + 0x30);
        cTradePWD[4] = (byte) (((lngPaymentPsw / 10) % 10000 % 1000 % 100 % 10) + 0x30);
        cTradePWD[5] = (byte) ((lngPaymentPsw % 100000 % 10000 % 1000 % 100 % 10) + 0x30);

        return cTradePWD;
    }
//    //正负数的钱包余额处理
//    public static  long TransBurseMoney(byte[] cMoneyContext,int iPos)
//    {
//        byte bTemp;
//        long lngTemp;
//        byte[] cMoneyTmp=new byte[3];
//
//        System.arraycopy(cMoneyContext,iPos,cMoneyTmp,0,cMoneyTmp.length);
//        //钱包的余额(在这里正负的处理)
//        if((cMoneyTmp[2]&0x80)==0x80)
//        {
//            bTemp=cMoneyTmp[0];
//            bTemp= (byte) ~bTemp;
//            lngTemp=(bTemp&0xff);
//
//            bTemp=cMoneyTmp[1];
//            bTemp= (byte) ~bTemp;
//            lngTemp=lngTemp+(bTemp&0xff)*256;
//
//            bTemp=cMoneyTmp[2];
//            bTemp= (byte) ~bTemp;
//            lngTemp=lngTemp+(bTemp&0xff)*256*256;
//
//            lngTemp=lngTemp+1;
//            lngTemp=-lngTemp;
//        }
//        else
//        {
//            lngTemp=(cMoneyTmp[0]&0xff);
//            lngTemp=lngTemp+(cMoneyTmp[1]&0xff)*256;
//            lngTemp=lngTemp+(cMoneyTmp[2]&0xff)*256*256;
//        }
//        return lngTemp;
//    }


//
//
//
//    public static  int  GetShoperCardContext(byte[] cCardContext)
//    {
//        byte cResult;
//        byte[] cCardTempSID=new byte[8];
//        byte[] cPosFile=new byte[2];
//
//        cResult=ReadCPUCardSID(cCardTempSID);
//        if(cResult!=0)
//        {
//            return 2;
//        }
//        //选择MF文件
//        cResult=CPU_SelectCardMF();
//        if(cResult!=0)
//        {
//            return 3;
//        }
//        cPosFile[0]=0x00;
//        cPosFile[1]=0x18;
//        //选择0018文件
//        cResult=CPU_SelectFile(0x00,0x00,cPosFile,2);
//        if(cResult!=0)
//        {
//            return 4;
//        }
//        cResult=CPU_ReadBinary(0x98,0x00,cCardContext,16);
//        if(cResult!=0)
//        {
//            return 5;
//        }
//        return 0;
//    }
//
//
//    //十进制转换成十六进制
//    public static  void  ChangeToHexA(byte[] Context,byte Len)
//    {
//        int i;
//        for(i=0;i<Len;i++)
//        {
//            Context[i]=(((Context[i]/16)*10)+(Context[i]%16));
//        }
//    }
//

}

package com.hzsun.mpos.Public;

import android.util.Log;

import com.hzsun.mpos.data.WasteBooks;

import java.io.UnsupportedEncodingException;

import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Public.Utility.memset;

public class Printer {

    private static final String TAG = "Printer";
    private static final int ESC = 0x1B;
    private static final int LF = 0x0A;

    //初始打印机
    public static int InitPrinter() {
        String cShowTemp = "";
        int lResult;

        cShowTemp = String.format("%c%c", ESC, 0x40);
        byte[] szBuf = cShowTemp.getBytes();
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, 2)) < 0) {
            return 1;
        } else {
            Log.d(TAG, "init printer is ok ");
        }
        return 0;
    }

    //取打印机的状态
    public static int GetPrintStatus() {
        int lResult;
        int RecvLen = 0;
        byte[] buf = new byte[128];
        String cShowTemp = "";

        cShowTemp = String.format("%c%c", ESC, 0x76);
        byte[] szBuf = cShowTemp.getBytes();

        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, 2)) < 0) {
            return 1;
        } else {
            //Log.d(TAG,"发送串口指令成功函数\n");
        }
        if ((lResult = g_Nlib.UartPrinter_RecvData(buf, RecvLen, 200)) < 0) {
            return 2;
        } else {
            //Log.d(TAG,"接收串口指令成功函数\n");
        }
        /*
        //打印机状态
        if ( (buf[0] & 0x01) !=0)
        {
            return 3; //打印机机头过温
        }
        */
        if ((buf[0] & 0x04) != 0) {
            return 4; //打印机缺纸
        } else {
            //Log.d(TAG,"取打印机状态成功函数\n");
        }
        return 0;
    }

    //打印内容
    public static int DoPrint(WasteBooks stWasteBooks) {
        int i;
        String cShowTemp = "";
        byte[] szBuf = new byte[512];
        byte[] buf = new byte[512];
        byte[] bufTemp = new byte[512];
        byte[] strTime = new byte[128];
        byte[] bCurrentDate = new byte[6];
        int lResult;
        long lngTemp;

        //cShowTemp = new String(accName, "GB2312");
        //new String(xxx.getBytes("gbk"), "gbk")
        //项目名称
        cShowTemp = String.format("      校园一卡通系统\n");
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }
        //1凭证类型
        cShowTemp = String.format("         普通消费凭证\r\r");
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

        //2时间
        memset(buf, (byte) 0, 128);
        memset(bufTemp, (byte) 0, 128);
        lResult = Publicfun.GetCurrDateTime(bCurrentDate);
        if (lResult != 0) {
            return 1;
        }

        String strTmp = Publicfun.GetFullDateWeekTime(Publicfun.toData(System.currentTimeMillis()));
        cShowTemp = String.format("交易时间:" + strTmp + "\n");
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

        //4站点号
        lngTemp = (stWasteBooks.cStationID[0] & 0xff) + ((stWasteBooks.cStationID[1] & 0xff) * 256);
        cShowTemp = String.format("终端编号:%d\r", lngTemp);
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

        //4站点流水号
        lngTemp = (stWasteBooks.cPaymentRecordID[0] & 0xff)
                + (stWasteBooks.cPaymentRecordID[1] & 0xff) * 256
                + (stWasteBooks.cPaymentRecordID[2] & 0xff) * 256 * 256;
        cShowTemp = String.format("终端流水号:%08d\r", lngTemp);
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

        //5卡内编号
        lngTemp = (stWasteBooks.cCardID[0] & 0xff)
                + (stWasteBooks.cCardID[1] & 0xff) * 256
                + (stWasteBooks.cCardID[2] & 0xff) * 256 * 256;
        cShowTemp = String.format("卡内编号:%08d\r", lngTemp);
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

        //6姓名
        String cNameTemp = "";
        try {
            cNameTemp = new String(stWasteBooks.cAccName, "GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        cShowTemp = String.format("姓名:" + cNameTemp + "\n");
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

        //8(充值\消费)金额
        lngTemp = (stWasteBooks.cPaymentMoney[0] & 0xff) + (stWasteBooks.cPaymentMoney[1] & 0xff) * 256;
        //交易类型
        //23:消费管理费 53角 83元
        //24:消费管理费冲正； 54角 84元
        switch (stWasteBooks.cPaymentType) {
            case 0:
                cShowTemp = String.format("消费金额:%d.%02d元\r", lngTemp / 100, lngTemp % 100);
                break;
            case 30:
                cShowTemp = String.format("消费金额:%d.%01d元\r", lngTemp / 10, lngTemp % 10);
                break;
            case 60:
                cShowTemp = String.format("消费金额:%d元\r", lngTemp);
                break;
            case 8:
                cShowTemp = String.format("消费金额:%d.%02d元\r", lngTemp / 100, lngTemp % 100);
                break;
            case 38:
                cShowTemp = String.format("消费金额:%d.%01d元\r", lngTemp / 10, lngTemp % 10);
                break;
            case 68:
                cShowTemp = String.format("消费金额:%d元\r", lngTemp);
                break;
            case 5:
                cShowTemp = String.format("充值金额:%d.%02d元\r", lngTemp / 100, lngTemp % 100);
                break;
            case 35:
                cShowTemp = String.format("充值金额:%d.%01d元\r", lngTemp / 10, lngTemp % 10);
                break;
            case 65:
                cShowTemp = String.format("充值金额:%d元\r", lngTemp);
                break;
            case 3:
                cShowTemp = String.format("充正金额:%d.%02d元\r", lngTemp / 100, lngTemp % 100);
                break;
            case 33:
                cShowTemp = String.format("充正金额:%d.%01d元\r", lngTemp / 10, lngTemp % 10);
                break;
            case 63:
                cShowTemp = String.format("充正金额:%d元\r", lngTemp);
                break;
            case 6:
                cShowTemp = String.format("销帐金额:%d.%02d元\r", lngTemp / 100, lngTemp % 100);
                break;
            case 36:
                cShowTemp = String.format("销帐金额:%d.%01d元\r", lngTemp / 10, lngTemp % 10);
                break;
            case 66:
                cShowTemp = String.format("销帐金额:%d元\r", lngTemp);
                break;
            case 23:
                cShowTemp = String.format("消费管理费:%d.%02d元\r", lngTemp / 100, lngTemp % 100);
                break;
        }
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

//        //9优惠金额
//        lngTemp=(stWasteBooks.cPrivelegeMoney[0]&0xff)
//                +(stWasteBooks.cPrivelegeMoney[1]&0xff)*256;
//        cShowTemp=String.format("优惠金额:%d.%02d元\r",lngTemp/100,lngTemp%100);
//        try {
//            szBuf = cShowTemp.getBytes("gbk");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        if (( lResult = g_Nlib.UartPrinter_SendData(szBuf,szBuf.length)) < 0)
//        {
//            return 1;
//        }

        //9钱包余额
        lngTemp = (stWasteBooks.cBurseMoney[0] & 0xff)
                + (stWasteBooks.cBurseMoney[1] & 0xff) * 256
                + (stWasteBooks.cBurseMoney[2] & 0xff) * 256 * 256;
        cShowTemp = String.format("钱包余额:%d.%02d元\r", lngTemp / 100, lngTemp % 100);
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

        //10交易钱包
        cShowTemp = String.format("交易钱包:%d\r", stWasteBooks.cBurseID);
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

        //11(单位编号)
        cShowTemp = String.format("单位编号:%d-%04d\r", g_SystemInfo.cAgentID, g_SystemInfo.iGuestID);
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

        //12系统研发
        cShowTemp = String.format("技术支持:浙江正元智慧");
        try {
            szBuf = cShowTemp.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }

        cShowTemp = String.format("%c%c%c", ESC, 0x4A, 150);
        szBuf = cShowTemp.getBytes();
        if ((lResult = g_Nlib.UartPrinter_SendData(szBuf, szBuf.length)) < 0) {
            return 1;
        }
        return 0;
    }

    //接收打印
    public static int PrintReceipt(WasteBooks stWasteBooks) {
        int lResult;

        if (g_LocalInfo.cPrinterMode == 0) {
            return 1;
        }
        //初始化打印机
        lResult = InitPrinter();
        if (lResult != 0) {
            Log.d(TAG, "初始化打印机失败");
            return 2;
        }
        //执行打印
        lResult = DoPrint(stWasteBooks);
        if (lResult != 0) {
            Log.d(TAG, "打印失败");
            return 3;
        } else {
            Log.d(TAG, "打印内容成功\n");
        }
        return 0;
    }

}

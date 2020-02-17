package com.hzsun.mpos.Public;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.fs.UsbFile;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hzsun.mpos.Algorithm.QREncrypt;
import com.hzsun.mpos.CardWork.CardAttrInfo;
import com.hzsun.mpos.CardWork.CardBasicParaInfo;
import com.hzsun.mpos.CardWork.CardInfo;
import com.hzsun.mpos.Global.CommInfo;
import com.hzsun.mpos.Global.NetEventMsg;
import com.hzsun.mpos.Global.WorkInfo;
import com.hzsun.mpos.Http.HttpCommInfo;
import com.hzsun.mpos.QRCodeWork.QRScanHelper;
import com.hzsun.mpos.SerialWork.SerialFunc;
import com.hzsun.mpos.Sound.SoundPlay;
import com.hzsun.mpos.data.BWListAllInfo;
import com.hzsun.mpos.data.BWListInfoRW;
import com.hzsun.mpos.data.BasicInfo;
import com.hzsun.mpos.data.BasicInfoRW;
import com.hzsun.mpos.data.BuinessInfo;
import com.hzsun.mpos.data.BuinessInfoRW;
import com.hzsun.mpos.data.BurseInfo;
import com.hzsun.mpos.data.BurseInfoRW;
import com.hzsun.mpos.data.FTPInfo;
import com.hzsun.mpos.data.FTPInfoRW;
import com.hzsun.mpos.data.FaceCodeInfo;
import com.hzsun.mpos.data.FaceCodeInfoRW;
import com.hzsun.mpos.data.FaceIdentInfo;
import com.hzsun.mpos.data.FacePayInfo;
import com.hzsun.mpos.data.IdentitySoundRW;
import com.hzsun.mpos.data.LastOrderInfo;
import com.hzsun.mpos.data.LastRecordPayInfo;
import com.hzsun.mpos.data.LocalInfo;
import com.hzsun.mpos.data.LocalInfoRW;
import com.hzsun.mpos.data.LocalNetInfo;
import com.hzsun.mpos.data.LocalNetInfoRW;
import com.hzsun.mpos.data.LocalNetStrInfo;
import com.hzsun.mpos.data.OddKeyInfo;
import com.hzsun.mpos.data.OddKeyInfoRW;
import com.hzsun.mpos.data.OnlinePayInfo;
import com.hzsun.mpos.data.QRCodeALIInfo;
import com.hzsun.mpos.data.QRCodeCardHInfo;
import com.hzsun.mpos.data.QrCodeResultInfo;
import com.hzsun.mpos.data.RecordInfo;
import com.hzsun.mpos.data.RecordInfoRW;
import com.hzsun.mpos.data.SecretKeyInfo;
import com.hzsun.mpos.data.SecretKeyInfoRW;
import com.hzsun.mpos.data.ShopQRCodeInfo;
import com.hzsun.mpos.data.StationInfo;
import com.hzsun.mpos.data.StationInfoRW;
import com.hzsun.mpos.data.StatusBurInfo;
import com.hzsun.mpos.data.StatusBurInfoRW;
import com.hzsun.mpos.data.StatusInfo;
import com.hzsun.mpos.data.StatusPriInfo;
import com.hzsun.mpos.data.StatusPriInfoRW;
import com.hzsun.mpos.data.SystemInfo;
import com.hzsun.mpos.data.SystemInfoRW;
import com.hzsun.mpos.data.VersionInfo;
import com.hzsun.mpos.data.WasteBookInfo;
import com.hzsun.mpos.data.WasteBooks;
import com.hzsun.mpos.data.WasteBooksRW;
import com.hzsun.mpos.data.WasteFaceBookInfo;
import com.hzsun.mpos.data.WasteFacePayBooksRW;
import com.hzsun.mpos.data.WasteQrBookInfo;
import com.hzsun.mpos.data.WasteQrCodeBooks;
import com.hzsun.mpos.data.WasteQrCodeBooksRW;
import com.hzsun.mpos.data.WifiParaInfo;
import com.hzsun.mpos.nativelib.nativelib;
import com.hzsun.mpos.thread.LogWriteThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import struct.JavaStruct;
import struct.StructException;


import static com.hzsun.mpos.Activity.CardActivity.CARD_ERR;
import static com.hzsun.mpos.Activity.CardActivity.CARD_PAYING;
import static com.hzsun.mpos.Activity.StartActivity.SET_TIME_MSG;
import static com.hzsun.mpos.CardWork.CardWorkTask.EVT_RelayControl;
import static com.hzsun.mpos.CardWork.CardWorkTask.RelaySendHandler;
import static com.hzsun.mpos.Global.Global.*;
import static com.hzsun.mpos.Public.FileUtils.CreatDirectory;
import static com.hzsun.mpos.Public.RefuseCode.strParseRefuseCode;

import static com.hzsun.mpos.Public.Utility.memcpy;
import static com.hzsun.mpos.data.FaceCodeInfoRW.ReadFaceCodeInfoData;
import static java.util.Arrays.fill;

public class Publicfun {

    private static final String TAG = "Publicfun";

    /**
     * 是否打印日志
     */
    public static boolean PRINTLOG = true;

    private static final int DATE_TIME_ERROR = 54;            //时钟模块故障    54
    private static final int MEMORY_FAIL = 59;            //读写FLASH失败 59

    /**
     * 打印日志
     *
     * @param msg 详细信息
     */
    public static void printLog(String targer, String msg) {
        if (PRINTLOG)
            Log.i(targer, msg);
    }

    //设备硬件驱动初始化
    public static int InitHWDevice() {
        int iResult;
        byte[] cVerTemp = new byte[64];

        Led_Init((char) 0);
        //初始化QR串口
        iResult = g_Nlib.UartQR_Init(0);
        if (iResult != 0) {
            Log.i(TAG, "==初始化QR串口失败==");
            //return iResult;
        } else {
            //移动侦测状态查询
            iResult = g_Nlib.QR_GetDisSenseRet(5);
            if (iResult == -1) {
                Log.i(TAG, "==QR内置模块不存在==");
                g_WorkInfo.cQRDevStatus = 0;
                g_RecordInfo.cCurEquipmentState = g_RecordInfo.cCurEquipmentState | 0x20;
            } else {
                Log.i(TAG, "==QR内置模块存在==");
                g_WorkInfo.cQRDevStatus = 1;
                if ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0)) {
                    //设置自动感应模式
                    g_Nlib.QR_SetDeviceReadMode(4);
                } else {
                    //识度模式 $100002-C9FF 开关持续 2
                    g_Nlib.QR_SetDeviceReadMode(2);
                    //命令触发 //$108003-F8E3 结束识读 2
                    Log.i(TAG, "初始化 结束识读");
                    g_Nlib.QR_SetDeviceReadEnable(2);
                }
                //主机命令应答模式 $020B00-33A1 *无应答
                g_Nlib.QR_SetHostCommand();
                //关闭结束符
                g_Nlib.QR_SetEndMark();
                //$100100-D929 自动感应灵敏度最高
                g_Nlib.QR_SetAutoSenLevel(0);
				//$1003FF-FC16 相邻条码必须不同
                g_Nlib.QR_SetDeviceReadInterval(4);
            }
        }
        if (g_WorkInfo.cQRDevStatus == 0) {
            if (QRScanHelper.hasUSBQRSCanDevice()) {
                Log.d(TAG, "==QR内置USB模块存在==");
                g_WorkInfo.cQRDevStatus = 2;
            }
        }

        //初始化小票打印机
        iResult = g_Nlib.UartPrinter_Init(0);
        if (iResult != 0) {
            Log.e(TAG, "==初始化小票打印机失败==");
            //return iResult;
        }

        //初始化对接机
        iResult = g_Nlib.UartDockpos_Init(0);
        if (iResult != 0) {
            Log.e(TAG, "==初始化对接机串口失败==");
        } else {
            Log.i(TAG, "对接机串口初始化成功");
        }


//        //PSAM卡初始化
//        iResult=PSAM_InitAll();
//        if(iResult != 0)
//        {
//            Log.i(TAG,"==PSAM卡初始化失败==");
//            g_WorkInfo.cETPSAMState=0;
//            g_RecordInfo.cCurEquipmentState=g_RecordInfo.cCurEquipmentState|0x10;
//        }
//        else
//        {
//            Log.i(TAG,"==PSAM卡初始化成功==");
//            g_WorkInfo.cETPSAMState=1;
//        }
        GetCurrBaseDateTime();
        return 0;
    }

    //检测摄像头个数
    private static int getCameraNums() {
        CAMERA_NUM = Camera.getNumberOfCameras();
        Log.d(TAG,"摄像头个数:"+CAMERA_NUM);
        return CAMERA_NUM;
    }

    //设备PSAM卡初始化
    public static int PSAM_InitAll() {
        int i;
        int iResult = -1;
        Log.i(TAG, "==设备PSAM卡初始化==");

        for (i = 0; i < 3; i++) {
            //PSAM卡初始化
            iResult = g_Nlib.PSAMSP_Init();
            if (iResult == 0) {
                break;
            }
        }
        if (iResult != 0) {
            Log.i(TAG, "==PSAM卡初始化失败==");
            return -1;
        }
        return 0;
    }

    //初始化参数
    public static void InitWorkPara() {
        Log.e(TAG, "=======设备固件版本号:"+SOFTWAREVER);
        g_BasicInfo = new BasicInfo();
        g_LocalInfo = new LocalInfo();
        g_LocalNetInfo = new LocalNetInfo();
        g_LocalNetStrInfo = new LocalNetStrInfo();
        g_VersionInfo = new VersionInfo();
        g_NetVersionInfo = new VersionInfo();
        g_SecretKeyInfo = new SecretKeyInfo();
        g_SystemInfo = new SystemInfo();
        g_StationInfo = new StationInfo();
        g_OddKeyInfo = new OddKeyInfo();
        g_EP_BurseInfo = new ArrayList<BurseInfo>();
        g_BuinessInfo = new ArrayList<BuinessInfo>();
        g_StatusBurInfo = new StatusBurInfo();
        g_StatusInfo = new StatusInfo();
        g_StatusInfoArray = new ArrayList<StatusInfo>();
        g_StatusPriInfoArray = new ArrayList<StatusPriInfo>();
        g_FTPInfo = new FTPInfo();
        g_WifiParaInfo = new WifiParaInfo();
        g_BlackWList = new BWListAllInfo();
        g_RecordInfo = new RecordInfo();
        g_WasteBookInfo = new WasteBookInfo();
        g_WasteQrBookInfo = new WasteQrBookInfo();
        g_WasteFaceBookInfo = new WasteFaceBookInfo();
        g_CommInfo = new CommInfo();
        g_WorkInfo = new WorkInfo();
        s_NetEventMsg = new NetEventMsg();

        g_CardInfo = new CardInfo();
        g_CardAttr = new CardAttrInfo();
        g_CardBasicInfo = new CardBasicParaInfo();
        g_CardBasicTmpInfo = new CardBasicParaInfo();
        g_CardBasicTmpInfoA = new CardBasicParaInfo();

        g_LastOrderInfo = new LastOrderInfo();
        g_CardHQRCodeInfo = new QRCodeCardHInfo();
        g_FacePayInfo = new FacePayInfo();
        g_QrCodeResultInfo = new QrCodeResultInfo();
        g_ThirdCodeResultInfo = new QrCodeResultInfo();
        g_ThirdQRCodeInfo = new QRCodeALIInfo();
        g_FaceIdentInfo = new FaceIdentInfo();
        g_FaceCodeInfo = new FaceCodeInfo();
        gHttpCommInfo = new HttpCommInfo();
        g_OnlinePayInfo = new OnlinePayInfo();
        g_LastRecordPayInfo = new LastRecordPayInfo();
        g_ShopQRCodeInfo = new ShopQRCodeInfo();
        g_Nlib = new nativelib();
        g_WorkInfo.lngPowerSaveCnt = System.currentTimeMillis();
    }

    //系统工作参数的初始化
    public static int InitSysParam() {
        int ret;

        //读取基础参数文件
        g_BasicInfo = BasicInfoRW.ReadBasicInfo();
        if ((g_BasicInfo == null) || (g_BasicInfo.cSystemState < 80)) {
            //读取基础参数文件失败，系统初始化
            InitDeviceData();
        }
        //读取所有的参数文件
        ret = ReadAllSysParam();
        if (ret != 0) {
            return -1;
        }
        //车载自动定额
        g_LocalInfo.cInputMode = 3;                    //输入方式
        if (g_LocalInfo.cDockposFlag == 1) {
            g_LocalInfo.cInputMode = 1;                //启用对接机使用普通输入方式
        }
        g_LocalInfo.cBookSureMode = 0;
        if (g_LocalInfo.cLogFlag == 1) {
            Log.e(TAG, "=======开始打印日志=======");
            ReadSofeVerInfoFile();
            LogWriteThread ThreadLogWrite = new LogWriteThread("");
            ThreadLogWrite.start();
        }
        //打印所有参数
        ret = PrintAllSysParam();
        if (ret != 0) {
            return -1;
        }
        return 0;
    }

    //系统初始化
    public static int InitDeviceData() {
        //删除所有数据文件
        RunShellCmd("rm -f " + DATAPath + "*.dat");
        RunShellCmd("rm -rf " + PhotoPath);
        RunShellCmd("rm -rf " + LogPath);
        RunShellCmd("rm -rf " + LogCrashPath);
        RunShellCmd("rm -rf " + PicPath);
        RunShellCmd("rm -rf " + ZYTKFacePath);
        CreateAllFileInfo();//创建参数文件
        InitDevAllPara();//设置设备参数
        return 0;
    }

    //恢复出厂设置factory reset
    public static int FactoryReset() {
        //删除所有数据文件 和配置文件
        RunShellCmd("rm -rf " + ZYTKPath);
        RunShellCmd("reboot");
        return 0;
    }

    //初始化设备
    public static int InitDevice() {
        //删除所有数据文件
        RunShellCmd("rm -f " + DATAPath + "*.dat");
        RunShellCmd("rm -rf " + PhotoPath);
        RunShellCmd("rm -rf " + LogPath);
        RunShellCmd("rm -rf " + LogCrashPath);
        RunShellCmd("rm -rf " + PicPath);
        RunShellCmd("rm -rf " + ZYTKFacePath);
        RunShellCmd("rm -rf " + ZYTKPath + "DeviceNetPara.ini");

        Publicfun.RunShellCmd("reboot");
        return 0;
    }

    //创建所有的参数文件
    public static void CreateAllFileInfo() {
        //创建目录
        CreatDirectory(ZYTKPath);
        //创建zytk35目录
        CreatDirectory(ZYTK35Path);
        //创建人脸识别目录
        CreatDirectory(ZYTKFacePath);
        //创建照片目录
        CreatDirectory(PhotoPath);
        //创建log目录
        CreatDirectory(LogPath);

        //创建设备基础参数文件
        BasicInfoRW.CreateBasicInfo();

        //创建版本文件
        //g_VersionInfo_Fun.CreateVersionInfo();

        //工作密钥文件
        SecretKeyInfoRW.CreateSecretKeyInfo();

        //本地参数文件
        LocalInfoRW.CreateLocalInfo();

        //本地网络参数文件
        LocalNetInfoRW.CreateLocalNetInfo();

        //本地FTP参数文件
        FTPInfoRW.CreateFTPInfo();

        //系统参数
        SystemInfoRW.CreateSystemInfo();

        //终端站点参数
        StationInfoRW.CreateStationInfo();

        //钱包参数
        BurseInfoRW.CreateBurseInfo();

        //单键参数
        OddKeyInfoRW.CreateOddKeyInfo();

        //营业分组
        BuinessInfoRW.CreateBuinessInfo();

        //身份钱包
        StatusBurInfoRW.CreateStatusBurInfo();

        //身份优惠
        StatusPriInfoRW.CreateStatusPriInfo();

        //系统终端记录信息
        RecordInfoRW.CreateRecordInfo();

        //黑白名单
        BWListInfoRW.CreateBWListInfo();

        //流水信息
        WasteBooksRW.CreateWasteBooks();

        //备份流水信息
        //g_PayRecord.CreateConsumeBooksBak();

        //二维码记录信息
        WasteQrCodeBooksRW.CreateWasteQrCodeBooks();
        //人脸交易记录信息
        WasteFacePayBooksRW.CreateWasteFacePayBooks();
        //人脸特征信息
        FaceCodeInfoRW.CreateFaceCodeInfo();
        //写软件版本号
        Publicfun.WriteSofeVerInfoFile(SOFTWAREVER);
    }

    //设置设备参数
    public static void InitDevAllPara() {
        //设置基础参数
        SetBasicAllPara();
        //设置本地参数
        SetLocalAllPara();
        //设置本地网络参数
        SetLocalNetAllPara();
        //设置本地站点参数
        SetStationAllPara();
    }

    //设置基础参数
    public static void SetBasicAllPara() {
        int iResult;
        byte[] cTerInCode = new byte[4];

        g_BasicInfo = BasicInfoRW.ReadBasicInfo();
        if (g_BasicInfo == null) {
            Log.i(TAG, "读取基础参数失败");
            return;
        }

        String sLocalMac = NetManager.GetLocalMac();
        if (sLocalMac == null) {
            Log.i(TAG, "获取本地MAC失败");
            return;
        }
        byte[] cLocalMac = NumConvertUtils.hexStrToByteArray(sLocalMac);

        //终端序列号
        System.arraycopy(cLocalMac, 0, g_BasicInfo.cTerminalSerID, 0, cLocalMac.length);
        //终端内码
        GetTerInCode(cTerInCode);
        System.arraycopy(cTerInCode, 0, g_BasicInfo.cTerInCode, 0, 4);

        //终端软件版本号(IAP)
        //平台软件版本号
        //终端软件版本号(APP)
        //应用程序程序包数
        //应用程序CRC16校验
        //系统状态(置初始系统状态)
        g_BasicInfo.cSystemState = 80;
        //终端MAC地址
        System.arraycopy(cLocalMac, 0, g_BasicInfo.cMAC, 0, cLocalMac.length);
        //获取本地IP方式 1:DHCP自动获取 0:手动输入
        g_BasicInfo.cGetLocalIPMode = 1;

        iResult = BasicInfoRW.WriteAllBasicInfo(g_BasicInfo);
        if (iResult != 0) {
            Log.i(TAG, "设置基础参数失败");
            return;
        }
    }

    //设置本地参数
    public static void SetLocalAllPara() {
        int i;
        int iResult;

        g_LocalInfo.wShopUserID = 0;                //本机商户号
        g_LocalInfo.cInputMode = 3;                    //输入方式(车载自动定额 1)
        if (g_LocalInfo.cDockposFlag == 1) {
            g_LocalInfo.cInputMode = 1;                //启用对接机使用普通输入方式
        }
        //定义的定额值
        for (i = 0; i < 6; i++) {
            g_LocalInfo.wBookMoney[i] = (500 + i * 100);
        }
        //定义的车价值(单价改成车价)
        for (i = 0; i < 8; i++) {
            g_OddKeyInfo.wKeyMoney[i] = (5 + i * 1);
        }

        g_LocalInfo.cBookSureMode = 0;                //定额方式是否需要确认(车载自动定额 1)
        g_LocalInfo.cKeyLockState = 0;                //键盘锁定
        g_LocalInfo.cTradFailCall = 0;                //交易失败提示模式
        g_LocalInfo.cConServerMode = 0;                //连接服务器方式
        g_LocalInfo.cUpPamentSum = 1;                 //上传流水笔数
        g_LocalInfo.cMoneyDispMode = 0;             //余额显示方式0:默认显示工作钱包1:显示工作钱包和追扣钱包2:显示工作和追扣钱包之和
        g_LocalInfo.cPrinterMode = 0;                //打印小票模式      0:不打印 1:直接打印 2:打印提示
        g_LocalInfo.cLCDBacklightLevel = 5;           //LCD背光级数 (调节亮度 1-99 1:最亮)
        g_LocalInfo.cVolumeLevel = 5;               //语言音量级数 Playback 0 - 127
        g_LocalInfo.cPowerSaveTimeA = 5;             //启用省电模式时长 0-60
        g_LocalInfo.cLowPowerPercent = 15;           //电量阀值       5-20
        g_LocalInfo.cFaceDetectFlag = 1;              //是否启用人脸识别 0: 不启用 1:启用
        g_LocalInfo.iMaxFaceNum = 100000;           //人脸识别库大小 10000 (0-100000)
        g_LocalInfo.iPupilDistance = 10;  // 瞳距
        g_LocalInfo.fLiveThrehold = (float) 0.20; //人脸活体检测率
        g_LocalInfo.fFraction = (float) 0.65;      //人脸相识率
        g_LocalInfo.iPayShowTime = 3;             //在线支付成功显示时长（秒s）
        iResult = LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
        if (iResult != 0) {
            Log.i(TAG, "设置本地参数失败");
            return;
        }
    }

    //设置本地网络参数
    public static void SetLocalNetAllPara() {
        int iResult;

        //读取网络参数配置文件
        g_LocalNetInfo = ReadLocalNetCfg();
        if (g_LocalNetInfo == null) {
            return;
        }

//        //获取本机IP方式 0 DHCP 1 STATIC
//        g_LocalNetInfo.IPMode=1;
//        //本机IP地址
//        g_LocalNetInfo.LocalIP[0]= (byte) 192;
//        g_LocalNetInfo.LocalIP[1]= (byte) 168;
//        g_LocalNetInfo.LocalIP[2]= (byte) 1;
//        g_LocalNetInfo.LocalIP[3]= (byte) 144;
//        //子网掩码
//        g_LocalNetInfo.SubNetMask[0]= (byte) 255;
//        g_LocalNetInfo.SubNetMask[1]= (byte) 255;
//        g_LocalNetInfo.SubNetMask[2]= (byte) 255;
//        g_LocalNetInfo.SubNetMask[3]= (byte) 0;
//        //网关IP地址
//        g_LocalNetInfo.GateWay[0]= (byte) 192;
//        g_LocalNetInfo.GateWay[1]= (byte) 168;
//        g_LocalNetInfo.GateWay[2]= (byte) 1;
//        g_LocalNetInfo.GateWay[3]= (byte) 253;
//        //服务器IP地址
//        g_LocalNetInfo.ServerIP1[0]= (byte) 192;
//        g_LocalNetInfo.ServerIP1[1]= (byte) 168;
//        g_LocalNetInfo.ServerIP1[2]= (byte) 1;
//        g_LocalNetInfo.ServerIP1[3]= (byte) 27;
//        //
//        g_LocalNetInfo.ServerIP2[0]= (byte) 192;
//        g_LocalNetInfo.ServerIP2[1]= (byte) 168;
//        g_LocalNetInfo.ServerIP2[2]= (byte) 1;
//        g_LocalNetInfo.ServerIP2[3]= (byte) 31;
//        //服务器监听端口号
//        g_LocalNetInfo.ServerPort1=9050;
//        g_LocalNetInfo.ServerPort2=9051;
//        //主DNS
//        g_LocalNetInfo.MasterDNS[0]= (byte) 202;
//        g_LocalNetInfo.MasterDNS[1]= (byte) 101;
//        g_LocalNetInfo.MasterDNS[2]= (byte) 172;
//        g_LocalNetInfo.MasterDNS[3]= (byte) 35;
//        //从DNS
//        g_LocalNetInfo.SlaveDNS[0]= (byte) 202;
//        g_LocalNetInfo.SlaveDNS[1]= (byte) 101;
//        g_LocalNetInfo.SlaveDNS[2]= (byte) 172;
//        g_LocalNetInfo.SlaveDNS[3]= (byte) 46;
//        String sLocalMac=NetManager.GetLocalMac();
//        if(sLocalMac==null)
//        {
//            Log.i(TAG,"获取本地MAC失败");
//            return;
//        }
//        byte[] cLocalMac=NumConvertUtils.hexStrToByteArray(sLocalMac);
//        //终端序列号
//        System.arraycopy(cLocalMac,0,g_LocalNetInfo.cMAC,0,6);

        iResult = LocalNetInfoRW.WriteAllLocalNetInfo(g_LocalNetInfo);
        if (iResult != 0) {
            Log.i(TAG, "设置本地网络参数失败");
            return;
        }
    }

    //设置本地站点参数
    public static void SetStationAllPara() {
        int iResult;

        g_StationInfo.lngOptionPsw = 111111;              //操作密码
        g_StationInfo.lngSetupPsw = 222222;              //设置密码
        g_StationInfo.lngAdvancePsw = 333333;              //高级密码

        iResult = StationInfoRW.WriteAllStationInfo(g_StationInfo);
        if (iResult != 0) {
            Log.i(TAG, "设置本地站点参数失败");
            return;
        }
    }

    public static byte[] StrToIPAddr(String ipAddr) {
        byte[] addr = new byte[4];
        try {
            String[] ipArr = ipAddr.split("\\.");
            if (ipArr.length == 4) {
                addr[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
                addr[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
                addr[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
                addr[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);
            } else {
                addr[0] = (byte) (0);
                addr[1] = (byte) (0);
                addr[2] = (byte) (0);
                addr[3] = (byte) (0);
            }
            return addr;
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }
    }

    public static byte[] getMacBytes(String mac) {
        byte[] macBytes = new byte[6];
        String[] strArr = mac.split(":");

        if (strArr.length == 6) {
            for (int i = 0; i < strArr.length; i++) {
                int value = Integer.parseInt(strArr[i], 16);
                macBytes[i] = (byte) value;
            }
        } else {
            String[] strArr1 = mac.split("-");

            if (strArr1.length == 6) {
                for (int i = 0; i < strArr1.length; i++) {
                    int value = Integer.parseInt(strArr1[i], 16);
                    macBytes[i] = (byte) value;
                }
            }
        }
        return macBytes;
    }

    //读取本地网络配置工作参数文件
    public static LocalNetStrInfo ReadLocalNetStrCfg() {
        Map<String, String> NetInfoMap = new HashMap<>();

        String IPMode = "static";
        String ipAddr = "192.168.1.145";
        String netMask = "255.255.255.0";
        String gateway = "192.168.1.253";
        String dns1 = "8.8.4.4";
        String dns2 = "8.8.4.4";
        String mac = "00:00:00:00:00:00";
        String ServeripAddr1 = "192.168.1.42";
        String ServeripAddr2 = "192.168.1.42";
        String ServerPort1 = "9050";
        String ServerPort2 = "9050";

        //读取MAP数据
        String fileName = ZYTKPath + "DeviceNetPara.ini";
        NetInfoMap = FileUtils.ReadMapFileData(fileName);
        if (NetInfoMap != null) {
            IPMode = NetInfoMap.get("IPMode");
            ipAddr = NetInfoMap.get("ipAddr");
            netMask = NetInfoMap.get("netMask");
            gateway = NetInfoMap.get("gateway");
            dns1 = NetInfoMap.get("dns1");
            dns2 = NetInfoMap.get("dns2");
            mac = NetInfoMap.get("mac");
            ServeripAddr1 = NetInfoMap.get("ServeripAddr1");
            ServeripAddr2 = NetInfoMap.get("ServeripAddr2");
            ServerPort1 = NetInfoMap.get("ServerPort1");
            ServerPort2 = NetInfoMap.get("ServerPort2");
        }
        //解析网络数据
        LocalNetStrInfo StructData = new LocalNetStrInfo();
        if (IPMode.equals("static"))
            StructData.IPMode = 1;
        else
            StructData.IPMode = 0;

        StructData.strLocalIP = (ipAddr);
        StructData.strSubNetMask = (netMask);
        StructData.strGateWay = (gateway);
        StructData.strMasterDNS = (dns1);
        StructData.strSlaveDNS = (dns2);
        StructData.strMAC = (mac);
        StructData.strServerIP1 = (ServeripAddr1);
        StructData.strServerIP2 = (ServeripAddr2);
        try {
            StructData.ServerPort1 = Integer.parseInt(ServerPort1);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            StructData.ServerPort2 = Integer.parseInt(ServerPort2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return StructData;
    }

    //设置络参数
    public static void SetLocalNetStrCfg(LocalNetStrInfo pLocalNetStrInfo) {
        //写ini文件数据
        String fileName = ZYTKPath + "DeviceNetPara.ini";
        Map<String, String> NetInfoMap = new HashMap<>();

        NetInfoMap.put("IPMode", ("" + pLocalNetStrInfo.IPMode));
        NetInfoMap.put("ipAddr", ("" + pLocalNetStrInfo.strLocalIP));
        NetInfoMap.put("netMask", ("" + pLocalNetStrInfo.strSubNetMask));
        NetInfoMap.put("gateway", ("" + pLocalNetStrInfo.strGateWay));
        NetInfoMap.put("dns1", ("" + pLocalNetStrInfo.strMasterDNS));
        NetInfoMap.put("dns2", ("" + pLocalNetStrInfo.strSlaveDNS));
        NetInfoMap.put("mac", ("" + pLocalNetStrInfo.strMAC));
        NetInfoMap.put("ServeripAddr1", ("" + pLocalNetStrInfo.strServerIP1));
        NetInfoMap.put("ServeripAddr2", ("" + pLocalNetStrInfo.strServerIP2));
        NetInfoMap.put("ServerPort1", ("" + pLocalNetStrInfo.ServerPort1));
        NetInfoMap.put("ServerPort2", ("" + pLocalNetStrInfo.ServerPort2));

        FileUtils.WriteMapToFile(NetInfoMap, fileName);
    }


    //读取本地网络配置工作参数文件
    public static LocalNetInfo ReadLocalNetCfg() {
        Map<String, String> NetInfoMap = new HashMap<>();

        String IPMode = "static";
        String ipAddr = "192.168.1.145";
        String netMask = "255.255.255.0";
        String gateway = "192.168.1.253";
        String dns1 = "8.8.4.4";
        String dns2 = "8.8.4.4";
        String mac = "00:00:00:00:00:00";
        String ServeripAddr1 = "192.168.1.42";
        String ServeripAddr2 = "192.168.1.42";
        String ServerPort1 = "9050";
        String ServerPort2 = "9050";

        //读取MAP数据
        String fileName = ZYTKPath + "DeviceNetPara.ini";
        NetInfoMap = FileUtils.ReadMapFileData(fileName);
        if (NetInfoMap != null) {
            IPMode = NetInfoMap.get("IPMode");
            ipAddr = NetInfoMap.get("ipAddr");
            netMask = NetInfoMap.get("netMask");
            gateway = NetInfoMap.get("gateway");
            dns1 = NetInfoMap.get("dns1");
            dns2 = NetInfoMap.get("dns2");
            mac = NetInfoMap.get("mac");
            ServeripAddr1 = NetInfoMap.get("ServeripAddr1");
            ServeripAddr2 = NetInfoMap.get("ServeripAddr2");
            ServerPort1 = NetInfoMap.get("ServerPort1");
            ServerPort2 = NetInfoMap.get("ServerPort2");
        }
        //解析网络数据
        LocalNetInfo StructData = new LocalNetInfo();
        if (IPMode.equals("static"))
            StructData.IPMode = 1;
        else
            StructData.IPMode = 0;

        StructData.LocalIP = StrToIPAddr(ipAddr);
        StructData.SubNetMask = StrToIPAddr(netMask);
        StructData.GateWay = StrToIPAddr(gateway);
        StructData.MasterDNS = StrToIPAddr(dns1);
        StructData.SlaveDNS = StrToIPAddr(dns2);
        StructData.cMAC = getMacBytes(mac);
        StructData.ServerIP1 = StrToIPAddr(ServeripAddr1);
        StructData.ServerIP2 = StrToIPAddr(ServeripAddr2);
        try {
            StructData.ServerPort1 = Integer.parseInt(ServerPort1);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            StructData.ServerPort2 = Integer.parseInt(ServerPort2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return StructData;
    }

        //读取引导软件版本号
    public static String ReadIAPSofeVerInfoFile() {
        String strSoftVersion = "";
        String fileName = IAPPath + "iapversion.ini";

        String strTemp = FileUtils.GetFileInfo(fileName);
        if (strTemp != null) {
            strSoftVersion = strTemp;
        }
        Log.d(TAG, "引导软件版本号:" + strSoftVersion);
        return strSoftVersion;
    }

    //读取assets引导软件版本号
    public static String ReadAssetsSofeVerInfoFile(Context context) {
        //将目标文件移动至可目录中
        String assetfileName = "iap/version.ini";
        String strTemp = "";
        AssetManager assets = context.getAssets();

        try {
            InputStream is = assets.open(assetfileName);
            byte[] buffer = new byte[1024 * 1];
            int byteCount = is.read(buffer);
            if(byteCount!=0){
                strTemp = new String(buffer, 0, byteCount);
                return strTemp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strTemp;
    }

    //读取软件版本号
    public static String ReadSofeVerInfoFile() {
        String strSoftVersion = SOFTWAREVER;
        String fileName = ZYTK35Path + "VersionInfo.ini";

        String strTemp = FileUtils.GetFileInfo(fileName);
        if (strTemp != null) {
            strSoftVersion = strTemp;
        } else {
            WriteSofeVerInfoFile(SOFTWAREVER);
        }
        byte[] strByte = strSoftVersion.getBytes();
        System.arraycopy(strByte, 0, g_WorkInfo.cConfigAppSWVer, 0, g_WorkInfo.cConfigAppSWVer.length);
        Log.d(TAG, "软件版本号:" + strSoftVersion);
        return strSoftVersion;
    }

    //写软件版本号
    public static int WriteSofeVerInfoFile(String strSoftVersion) {
        //写ini文件数据
//        Map<String, String> VerInfoMap =  new HashMap<>();
//        String fileName=ZYEPPath+"VersionInfo.ini";
//
//        VerInfoMap.put("SoftVersion", strSoftVersion);
//        FileUtils.WriteMapToFile(VerInfoMap, fileName);

        String strVerInfo = strSoftVersion;
        Log.e(TAG,"写软件版本号:"+strVerInfo);
        String fileName = ZYTK35Path + "VersionInfo.ini";
        FileUtils.SaveFileInfo(strVerInfo, fileName);

        return 0;
    }

    public static VersionInfo ReadVersionInfo() {

        byte[] cVersion = new byte[4];
        VersionInfo pVersionInfo = new VersionInfo();

        System.arraycopy(g_SystemInfo.cVersion, 0, pVersionInfo.bSystemVer, 0, 4);                //系统参数版本号
        System.arraycopy(g_StationInfo.cVersion, 0, pVersionInfo.bStationVer, 0, 4);            //站点参数版本号


        cVersion = BurseInfoRW.ReadBurseVer();
        System.arraycopy(cVersion, 0, pVersionInfo.bBurseVer, 0, 4);            //钱包参数版本号

        cVersion = OddKeyInfoRW.ReadOddKeyVer();
        System.arraycopy(cVersion, 0, pVersionInfo.bOddKeyVer, 0, 4);            //单键参数版本号

        cVersion = StatusBurInfoRW.ReadStatusBurVer();
        System.arraycopy(cVersion, 0, pVersionInfo.bStatusVer, 0, 4);            //身份参数版本号

        cVersion = StatusPriInfoRW.ReadStatusPriVer();
        System.arraycopy(cVersion, 0, pVersionInfo.bStatusPrivVer, 0, 4);        //身份优惠版本号

        cVersion = BuinessInfoRW.ReadBuinessVer();
        System.arraycopy(cVersion, 0, pVersionInfo.bBusinessVer, 0, 4);            //营业参数版本号

        System.arraycopy(g_BlackWList.BlackWListInfo.Version, 0, pVersionInfo.bInitListVer, 0, 4);            //初始名单版本号
        System.arraycopy(g_BlackWList.BlackWListInfo.Version, 0, pVersionInfo.bChangeListVer, 0, 4);            //变更名单版本号

        System.arraycopy(g_BasicInfo.cAppSoftWareVer, 0, pVersionInfo.cAppSoftWareVer, 0, 12);        //终端软件版本号(APP)

        return pVersionInfo;
    }

    //读取所有的参数文件
    public static int ReadAllSysParam() {
        int j;
        int iResult;

        //读基本参数数据
        Log.i(TAG, "读基本参数数据");
        g_BasicInfo = BasicInfoRW.ReadBasicInfo();
        if (g_BasicInfo == null) {
            return -1;
        }
        if (g_BasicInfo.cSystemState == 80) {
            Log.i(TAG, "设置客户用户卡");
        } else {
            //计算密钥(32扇区)
            Log.i(TAG, "计算密钥");
            g_Nlib.UCardDecryptA(g_WorkInfo.bUCardAuthKeyA, g_BasicInfo.iGuestID, g_BasicInfo.cAgentID);
//        Log.i(TAG,"===============A密钥=================");
//        for(j=0;j<32;j++)
//        {
//            Log.i(TAG, String.format("%02x.%02x.%02x.%02x.%02x.%02x",
//                    g_WorkInfo.bUCardAuthKeyA[j][0],
//                    g_WorkInfo.bUCardAuthKeyA[j][1],
//                    g_WorkInfo.bUCardAuthKeyA[j][2],
//                    g_WorkInfo.bUCardAuthKeyA[j][3],
//                    g_WorkInfo.bUCardAuthKeyA[j][4],
//                    g_WorkInfo.bUCardAuthKeyA[j][5]));
//        }
            g_Nlib.UCardDecryptB(g_WorkInfo.bUCardAuthKeyB, g_BasicInfo.iGuestID, g_BasicInfo.cAgentID);
//        Log.i(TAG,"===============B密钥=================");
//        for(j=0;j<32;j++)
//        {
//            Log.i(TAG, String.format("%02x.%02x.%02x.%02x.%02x.%02x",
//                    g_WorkInfo.bUCardAuthKeyB[j][0],
//                    g_WorkInfo.bUCardAuthKeyB[j][1],
//                    g_WorkInfo.bUCardAuthKeyB[j][2],
//                    g_WorkInfo.bUCardAuthKeyB[j][3],
//                    g_WorkInfo.bUCardAuthKeyB[j][4],
//                    g_WorkInfo.bUCardAuthKeyB[j][5]));
//        }
            //设置卡片密钥
            g_Nlib.SetCardKey(g_WorkInfo.bUCardAuthKeyA, g_WorkInfo.bUCardAuthKeyB);
        }

        //读本机网络参数配置文件
        Log.i(TAG, "读本机网络配置文件");
        g_LocalNetStrInfo = ReadLocalNetStrCfg();
        if (g_LocalNetStrInfo == null) {
            return -1;
        }

        //读wifi网络参数配置文件
        Log.i(TAG, "读wifi网络配置文件");
        g_WifiParaInfo = ReadWifiInfoStrCfg();
        if (g_WifiParaInfo == null) {
            return -1;
        }

        //读软件版本号参数配置文件
        Log.i(TAG, "读软件版本号参数配置文件");
        ReadSofeVerInfoFile();

        g_WorkInfo.strAppFileName = GetAppFileName();
        Log.i(TAG, "读apk文件名：" + g_WorkInfo.strAppFileName);
        //读本机参数
        Log.i(TAG, "读本机参数");
        g_LocalInfo = LocalInfoRW.ReadLocalInfo();
        if (g_LocalInfo == null) {
            return -1;
        }
        Log.i(TAG, g_LocalInfo.toString());
        LocalInfoRW.SetLocalInfo();

        //读系统参数
        Log.i(TAG, "读系统参数信息");
        g_SystemInfo = SystemInfoRW.ReadSystemInfo();
        if (g_SystemInfo == null) {
            return -1;
        }

        //读所有的终端参数
        Log.i(TAG, "读终端参数信息");
        g_StationInfo = StationInfoRW.ReadStationInfo();
        if (g_StationInfo == null) {
            return -1;
        }

        //2016.10.17系统下发商户号不为0,使用系统下发商户号
        if (g_StationInfo.iShopUserID != 0) {
            g_LocalInfo.wShopUserID = g_StationInfo.iShopUserID;
        }

        //读取钱包参数
        Log.i(TAG, "读取钱包参数");
        g_EP_BurseInfo = BurseInfoRW.ReadAllBurseInfo();
        if (g_EP_BurseInfo == null) {
            return -1;
        }

        //读取单键参数
        Log.i(TAG, "读取单键参数");
        g_OddKeyInfo = OddKeyInfoRW.ReadOddKeyInfo();
        if (g_OddKeyInfo == null) {
            return -1;
        }

        //读取营业分组参数
        Log.i(TAG, "读取营业分组参数");
        g_BuinessInfo = BuinessInfoRW.ReadAllBuinessInfo();
        if (g_BuinessInfo == null) {
            return -1;
        }

        //读取身份钱包参数
        Log.i(TAG, "读取身份钱包参数");
        g_StatusInfoArray = StatusBurInfoRW.ReadAllStatusBurInfo();
        if (g_StatusInfoArray == null) {
            return -1;
        }

        //读取身份优惠参数
        Log.i(TAG, "读取身份优惠参数");
        g_StatusPriInfoArray = StatusPriInfoRW.ReadAllStatusPriInfo();
        if (g_StatusPriInfoArray == null) {
            return -1;
        }

        //读取黑白名单信息
        Log.i(TAG, "读取黑白名单信息");
        g_BlackWList = BWListInfoRW.ReadAllBWListInfo();
        if (g_BlackWList == null) {
            return -1;
        }

        //读参数版本信息
        Log.i(TAG, "读参数版本信息");
        g_VersionInfo = ReadVersionInfo();
        if (g_VersionInfo == null) {
            return -1;
        }

        //读取系统终端记录信息
        Log.i(TAG, "读取系统终端记录信息");
        g_RecordInfo = RecordInfoRW.ReadRecordInfo();
        if (g_RecordInfo == null) {
            return -1;
        }

        //读取流水指针信息
        Log.i(TAG, "读取流水指针信息");
        g_WasteBookInfo = WasteBooksRW.ReadWasteBookInfo();
        if (g_WasteBookInfo == null) {
            return -1;
        }

        //读取QR流水指针信息
        Log.i(TAG, "读取二维码记录指针信息");
        g_WasteQrBookInfo = WasteQrCodeBooksRW.ReadWasteQrCodeBookInfo();
        if (g_WasteQrBookInfo == null) {
            return -1;
        }

        //读取人脸流水指针信息
        Log.i(TAG, "读取人脸流水指针信息");
        g_WasteFaceBookInfo = WasteFacePayBooksRW.ReadWasteFacePayBookInfo();
        if (g_WasteFaceBookInfo == null) {
            return -1;
        }

        //读取人脸特征信息
        Log.i(TAG, "读取人脸特征信息");
        g_FaceCodeInfo = FaceCodeInfoRW.ReadFaceCodeInfo();
        if (g_FaceCodeInfo == null) {
            return -1;
        }

        Log.i(TAG, "读取身份语音信息");
        g_MapIDSound = IdentitySoundRW.ReadIdentitySoundFile();

        //读取人脸信息
        ReadFaceIdentInfo();

        //进入测试模式
        if ((TESTMODE == 1) || (g_WorkInfo.cTestState == 1)) {
            g_WorkInfo.cTestState = 1;
            g_LocalInfo.cInputMode = 3;
            if (g_LocalInfo.cDockposFlag == 1) {
                g_LocalInfo.cInputMode = 1;                //启用对接机使用普通输入方式
            }
        }
        //更新系统运行参数
        //ReflashSysRunPare();
        return 0;
    }

    //读取所有的下载参数文件
    public static int ReadAllDownParam() {
        int j;
        int iResult;

        //读系统参数
        Log.i(TAG, "读系统参数信息");
        g_SystemInfo = SystemInfoRW.ReadSystemInfo();
        if (g_SystemInfo == null) {
            return -1;
        }

        //读所有的终端参数
        Log.i(TAG, "读终端参数信息");
        g_StationInfo = StationInfoRW.ReadStationInfo();
        if (g_StationInfo == null) {
            return -1;
        }

        //读取钱包参数
        Log.i(TAG, "读取钱包参数");
        g_EP_BurseInfo = BurseInfoRW.ReadAllBurseInfo();
        if (g_EP_BurseInfo == null) {
            return -1;
        }

        //读取单键参数
        Log.i(TAG, "读取单键参数");
        g_OddKeyInfo = OddKeyInfoRW.ReadOddKeyInfo();
        if (g_OddKeyInfo == null) {
            return -1;
        }

        //读取身份钱包参数
        Log.i(TAG, "读取身份钱包参数");
        g_StatusInfoArray = StatusBurInfoRW.ReadAllStatusBurInfo();
        if (g_StatusInfoArray == null) {
            return -1;
        }

        //读取身份优惠参数
        Log.i(TAG, "读取身份优惠参数");
        g_StatusPriInfoArray = StatusPriInfoRW.ReadAllStatusPriInfo();
        if (g_StatusPriInfoArray == null) {
            return -1;
        }

        //读取营业分组参数
        Log.i(TAG, "读取营业分组参数");
        g_BuinessInfo = BuinessInfoRW.ReadAllBuinessInfo();
        if (g_BuinessInfo == null) {
            return -1;
        }

        //读取系统终端记录信息
        Log.i(TAG, "读取系统终端记录信息");
        g_RecordInfo = RecordInfoRW.ReadRecordInfo();
        if (g_RecordInfo == null) {
            return -1;
        }

        //读取黑白名单信息
        Log.i(TAG, "读取黑白名单信息");
        g_BlackWList = BWListInfoRW.ReadAllBWListInfo();
        if (g_BlackWList == null) {
            return -1;
        }

        //读取流水指针信息
        Log.i(TAG, "读取流水指针信息");
        g_WasteBookInfo = WasteBooksRW.ReadWasteBookInfo();
        if (g_WasteBookInfo == null) {
            return -1;
        }

        //进入测试模式
        if ((TESTMODE == 1) || (g_WorkInfo.cTestState == 1)) {
            g_WorkInfo.cTestState = 1;
            g_LocalInfo.cInputMode = 3;
            if (g_LocalInfo.cDockposFlag == 1) {
                g_LocalInfo.cInputMode = 1;                //启用对接机使用普通输入方式
            }
        }
        //更新系统运行参数
        //ReflashSysRunPare();
        return 0;
    }

    //打印所有的参数文件
    public static int PrintAllSysParam() {
        int i = 0;

        Log.i(TAG, "基础数据：" + g_BasicInfo.toString());
        Log.i(TAG, "本地网络数据：" + g_LocalNetInfo.toString());
        Log.i(TAG, "本地数据：" + g_LocalInfo.toString());
        Log.i(TAG, "版本号数据：" + g_VersionInfo.toString());
        Log.i(TAG, "系统数据：" + g_SystemInfo.toString());
        Log.i(TAG, "站点数据：" + g_StationInfo.toString());

        Log.i(TAG, "钱包数据：" + g_EP_BurseInfo.size());
        for (i = 0; i < g_EP_BurseInfo.size(); i++) {
            Log.i(TAG, g_EP_BurseInfo.get(i).toString());
        }
        Log.i(TAG, "单键数据：" + g_OddKeyInfo.toString());

        Log.i(TAG, "身份钱包数据：" + g_StatusInfoArray.size());
        for (i = 0; i < g_StatusInfoArray.size(); i++) {
            //Log.i(TAG,g_StatusInfoArray.get(i).toString());
        }
        Log.i(TAG, "身份限制数据：" + g_StatusPriInfoArray.size());
        for (i = 0; i < g_StatusPriInfoArray.size(); i++) {
            //Log.i(TAG,g_StatusPriInfoArray.get(i).toString());
        }
        Log.i(TAG, "营业分组数据：" + g_BuinessInfo.size());
        for (i = 0; i < g_BuinessInfo.size(); i++) {
            //Log.i(TAG,g_BuinessInfo.get(i).toString());
        }
        Log.i(TAG, "记录数据：" + g_RecordInfo.toString());
        Log.i(TAG, "流水信息数据：" + g_WasteBookInfo.toString());

        return 0;
    }

    //设备13.56M读头(PN512)初始化
    public static int RF_ChipInit() {
        int status = 0;
        Log.d(TAG, "==设备13.56M读头(PN512)初始化==");

        status = g_Nlib.ReaderInit();
        return status;
    }

    //获取系统日期时间周
    public static void GetSysDateTime(int[] bCurrDateTime) {
        Calendar c = Calendar.getInstance();
//        Log.i(TAG, "当前的年份为：" + c.get(Calendar.YEAR));
//        Log.i(TAG, "当前的月份为：" + (c.get(Calendar.MONTH) + 1));
//        Log.i(TAG, "DATE:" + c.get(Calendar.DATE));
//        Log.i(TAG, "DAY_OF_MONTH:" + c.get(Calendar.DAY_OF_MONTH));
//        Log.i(TAG, "今天是今年的第" + c.get(Calendar.DAY_OF_YEAR) + "天");
//        Log.i(TAG, "今天在本月的第" + c.get(Calendar.DAY_OF_WEEK_IN_MONTH) + "周");
//        Log.i(TAG, "现在是" + c.get(Calendar.HOUR) + "点");
//        if (c.get(Calendar.AM_PM) == Calendar.AM) {
//            Log.i(TAG, "现在是上午");
//        } else {
//            Log.i(TAG, "现在是下午");
//        }
//        Log.i(TAG, "现在是" + c.get(Calendar.HOUR_OF_DAY) + "点");
//        Log.i(TAG, "现在是" + c.get(Calendar.MINUTE) + "分");
//        Log.i(TAG, "现在是" + c.get(Calendar.SECOND) + "秒");
//        Log.i(TAG, "现在是" + c.get(Calendar.MILLISECOND) + "毫秒");

        bCurrDateTime[0] = (c.get(Calendar.YEAR));
        bCurrDateTime[1] = (c.get(Calendar.MONTH));
        bCurrDateTime[2] = (c.get(Calendar.DATE));
        bCurrDateTime[3] = (c.get(Calendar.HOUR_OF_DAY));
        bCurrDateTime[4] = (c.get(Calendar.MINUTE));
        bCurrDateTime[5] = (c.get(Calendar.SECOND));
        bCurrDateTime[6] = (c.get(Calendar.DAY_OF_WEEK_IN_MONTH));

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
//        Log.i(TAG, sdf.format(c.getTime()));
//
//        SimpleDateFormat sdfA = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Log.i(TAG, sdfA.format(c.getTime()));
    }

    //获取日期时间
    public static int GetCurrDateTime(byte[] bCurrDateTime) {
        int i;

//        Log.d(TAG,"当前毫秒："+System.currentTimeMillis());
        String[] data = DataTransfer.toData(System.currentTimeMillis());
        for (i = 0; i < 6; i++) {
            if (i == 0) {
                String strTermp = data[i].substring(2, 4);
                bCurrDateTime[i] = (byte) Integer.parseInt(strTermp);
            } else {
                bCurrDateTime[i] = (byte) Integer.parseInt(data[i]);
            }
        }

//        long lngTimeMillis=getCurrMilTimes(bCurrDateTime);
//        Log.d(TAG,"计算毫秒："+lngTimeMillis);
        //年
        if ((bCurrDateTime[0] > 100) || (bCurrDateTime[0] == 0)) {
            return 1;
        }
        //月
        if ((bCurrDateTime[1] > 12) || (bCurrDateTime[1] == 0)) {
            return 2;
        }
        //日
        if ((bCurrDateTime[2] > 31) || (bCurrDateTime[2] == 0)) {
            return 3;
        }
        //小时
        if (bCurrDateTime[3] >= 24) {
            return 4;
        }
        //分
        if (bCurrDateTime[4] >= 60) {
            return 5;
        }
        //秒
        if (bCurrDateTime[5] >= 60) {
            return 6;
        }
        return 0;
    }

    /**
     * 获取时间戳
     *
     * @return
     */
    public static long getCurrMilTimes(byte[] bCurrDateTime) {

        int year, month,  day;
        int hour,  minute, second;

        year=bCurrDateTime[0];
        month=bCurrDateTime[1];
        day=bCurrDateTime[2];
        hour=bCurrDateTime[3];
        minute=bCurrDateTime[4];
        second=bCurrDateTime[5];

        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.YEAR, year);
        //cal.add(Calendar.DAY_OF_MONTH, month);
        //cal.add(Calendar.DATE, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return  (cal.getTimeInMillis());
    }
    //获取周几
    public static String GetDayofWeek() {
        String strWeek = "";
        Calendar c = Calendar.getInstance();

        int iWeek = c.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        //Log.i(TAG,"星期:"+iWeek);

        switch (iWeek) {
            case 2:
                strWeek = ("星期一");
                break;

            case 3:
                strWeek = ("星期二");
                break;

            case 4:
                strWeek = ("星期三");
                break;

            case 5:
                strWeek = ("星期四");
                break;

            case 6:
                strWeek = ("星期五");
                break;

            case 7:
                strWeek = ("星期六");
                break;

            case 1:
                strWeek = ("星期日");
                break;

            default:
                strWeek = ("");
                break;
        }
        return strWeek;
    }

    //获取当前卡片日期
    public static int GetCurrCardDate(byte[] cLastPaymentDate) {
        int cResult;
        int iNowYear;
        int iNowMonth;
        int iNowDay;
        int iNowHour;
        int iNowMin;
        int iNowSec;
        byte[] CurrentDate = new byte[8];

        cResult = GetCurrDateTime(CurrentDate);
        if (cResult != 0) {
            Log.e(TAG,"时钟获取错误");
            return DATE_TIME_ERROR;
        }
        //年
        iNowYear = CurrentDate[0];
        //月
        iNowMonth = CurrentDate[1];
        //日
        iNowDay = CurrentDate[2];
        //时
        iNowHour = CurrentDate[3];
        //分
        iNowMin = CurrentDate[4];
        //秒
        iNowSec = CurrentDate[5];

        //处理成卡内格式的时间
        cLastPaymentDate[0] = (byte) (iNowYear << 2);
        cLastPaymentDate[0] = (byte) (cLastPaymentDate[0] | (iNowMonth >> 2));
        cLastPaymentDate[1] = (byte) (iNowMonth << 6);
        cLastPaymentDate[1] = (byte) (cLastPaymentDate[1] | (iNowDay << 1));
        cLastPaymentDate[1] = (byte) (cLastPaymentDate[1] | (iNowHour >> 4));
        cLastPaymentDate[2] = (byte) (iNowHour << 4);
        cLastPaymentDate[2] = (byte) (cLastPaymentDate[2] | (iNowMin >> 2));
        cLastPaymentDate[3] = (byte) (iNowMin << 6);
        cLastPaymentDate[3] = (byte) (cLastPaymentDate[3] | iNowSec);

        return 0;
    }

    //判断日期合法性
    public static int CheckDataTime(byte[] bCurrDateTime) {
        //年
        if ((bCurrDateTime[0] > 100) || (bCurrDateTime[0] == 0)) {
            return 1;
        }
        //月
        if ((bCurrDateTime[1] > 12) || (bCurrDateTime[1] == 0)) {
            return 2;
        }
        //日
        if ((bCurrDateTime[2] > 31) || (bCurrDateTime[2] == 0)) {
            return 3;
        }
        //小时
        if (bCurrDateTime[3] >= 24) {
            return 4;
        }
        //分
        if (bCurrDateTime[4] >= 60) {
            return 5;
        }
        //秒
        if (bCurrDateTime[5] >= 60) {
            return 6;
        }
        return 0;
    }

    //设置系统时钟
    public static int SetDateTime(byte[] cCurDateTime) {
        int i;
        int cResult;
        byte[] bDateTime = new byte[8];

        //判断年月日时分时分一致,一致则不同步时钟
        cResult = GetCurrDateTime(bDateTime);
        if (cResult != 0) {
            return DATE_TIME_ERROR;
        }
        for (i = 0; i < 5; i++) {
            if (cCurDateTime[i] != bDateTime[i])
                break;
        }
        if (i == 5) {
            Log.e(TAG, "--------时钟一致不同步--------");
            return 0;
        }
        //判断时钟时分合法
        if (CheckDataTime(cCurDateTime) != 0) {
            Log.e(TAG, "--------时钟时分不合法--------");
            return 0;
        }
        if (gUIStartHandler != null) {
            String StrDateTime = String.format("%02d%02d%02d%02d%02d%02d", cCurDateTime[0], cCurDateTime[1], cCurDateTime[2], cCurDateTime[3], cCurDateTime[4], cCurDateTime[5]);
            Log.e(TAG, "--------SetDateTime:" + StrDateTime);
            Message msg = Message.obtain();
            msg.obj = StrDateTime;
            msg.what = SET_TIME_MSG;
            gUIStartHandler.sendMessage(msg);
        }else {
            Log.e(TAG, "gUIStartHandler 不存在");
        }
        //将当前设置时钟赋值给省电时长(设置时钟需要通过系统设置，有一段延迟)
        g_WorkInfo.lngPowerSaveCnt=getCurrMilTimes(cCurDateTime);
        return 0;
    }


    public static void PrintArray(String msg, byte[] data) {
        StringBuilder builder = new StringBuilder();
        builder.append(msg);
        builder.append(" = [");
        for (byte d : data) {
            //builder.append(Integer.toHexString(d & 0xff));
            builder.append(String.format("%02X", d & 0xff));
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        Log.i(TAG, builder.toString());
    }

    public static void PrintArray(String msg, byte[] data, int iLen) {
        int i = 0;
        StringBuilder builder = new StringBuilder();
        builder.append(msg);
        builder.append(" = [");

        for (byte d : data) {
            //builder.append(Integer.toHexString(d & 0xff));
            builder.append(String.format("%02X", d & 0xff));
            builder.append(",");
            i++;
            if (i >= iLen)
                break;
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        Log.i(TAG, builder.toString());
    }

    //设备LCD大屏初始化
    public static int Led_Init(char cMode) {
        if (cMode == 0) {
            g_Nlib.LedShow((char) 1, 0);
        } else {
            g_Nlib.LedShow((char) 1, 1);
        }
        return 0;
    }

    //关闭LED
    public static void LED_AllClear() {
        g_Nlib.LedShow((char) 1, 0);
        g_Nlib.LedShow((char) 2, 0);
        g_Nlib.LedShow((char) 3, 0);
    }

    //LED 1:Red 2:Blue 3:Green
    public static void LedShow(int color, int level) {

        g_Nlib.LedShow((char) color, level);
    }

    //显示正在支付中
    public static void ShowCardPaying() {
        String cErrorVoice = "inpaying";
        SoundPlay.VoicePlay(cErrorVoice);

        Message message = Message.obtain();
        message.obj = cErrorVoice;
        message.what = CARD_PAYING;
        if (gUICardHandler != null)
            gUICardHandler.sendMessage(message);
    }

    //显示卡片错误信息
    public static void ShowCardErrorInfo(int iErrorCode) {
        String cErrorCode = "";
        String cErrorVoice = "";
        if (g_LocalInfo.cDockposFlag == 1) {
            SerialFunc.showCardError(iErrorCode);
        }
        if (iErrorCode > 0) {
            switch (iErrorCode) {
                case CARD_INVALIED:
                    cErrorCode = "无效卡";
                    cErrorVoice = "card_invalid";
                    break;
                case OUT_OF_CONSUMERANGE:
                    cErrorCode = "超出消费范围";
                    cErrorVoice = "card_limit";
                    break;
                case NOT_ENOUGH_MONEY:
                    if (g_StationInfo.iStationClass == LAN_EP_CONSUMEPOS) {
                        cErrorCode = "余额不足";
                        cErrorVoice = "money_lack";
                    } else if (g_StationInfo.iStationClass == LAN_EP_MONEYPOS) {
                        cErrorCode = "余额超限";
                        cErrorVoice = "money_lack";
                    }
                    break;
                case ILLEGAL_CARD:
                    cErrorCode = "非法卡";
                    cErrorVoice = "card_invalid";
                    break;
                case BEYOND_LIMIT_TIM:
                    cErrorCode = "超出有效期";
                    cErrorVoice = "card_limit";
                    break;
                case BEYOND_SINGLE_MONEY:
                    cErrorCode = "超出单笔消费限额";
                    cErrorVoice = "card_limit";
                    break;
                case BEYOND_DAY_TOTAL:
                    cErrorCode = "超出累计限制";
                    cErrorVoice = "card_limit";
                    break;
                case PSW_ERROR:
                    cErrorCode = "密码错误";
                    cErrorVoice = "pwd_error";
                    break;
                case CHASE_FAIL:
                    cErrorCode = "追扣失败";
                    cErrorVoice = "deal_error";
                    break;
                case CONSUME_LIMIT_COUNT:
                    cErrorCode = "消费限次";
                    cErrorVoice = "card_limit";
                    break;
                case CANNOT_CANCEL_PAYMENT:
                    cErrorCode = "不允许冲正";
                    break;
                case CANCEL_PAYMENT_FAIL:
                    cErrorCode = "冲正失败";
                    break;
                case PAY_PSWAUTHEN_FAIL:
                    cErrorCode = "密码验证失败";
                    cErrorVoice = "card_invalid";
                    break;
                case BALANCE_BEYOND_LIMIT:
                    cErrorCode = "超出限额";
                    cErrorVoice = "card_limit";
                    break;
                case NO_FIND_CARD:
                    cErrorCode = "未找到卡片";
                    cErrorVoice = "card_reswipe";
                    break;
                case READ_CARDNUM_FAIL:
                    cErrorCode = "读卡失败";
                    cErrorVoice = "card_invalid";
                    break;
                case STATUS_NUM_ERROR:
                    cErrorCode = "身份错误";
                    cErrorVoice = "card_disable";
                    break;

                case NOPASSWORD://用户密码未输入 40
                    cErrorCode = "交易失败";
                    cErrorVoice = "deal_error";
                    break;

                case OVER_MONEY://余额超出上限  46
                    cErrorCode = "余额超出上限";
                    cErrorVoice = "card_limit";
                    break;

                case NOFOUND_CHASEBURSE:
                    cErrorCode = "无追扣钱包";
                    break;
                case DATE_TIME_ERROR:
                    cErrorCode = "时钟模块故障";
                    break;

                case MEMORY_FAIL:
                    cErrorCode = "读写存储失败";
                    break;

                case READ_CARDTYPE_INVALIED:
                    cErrorCode = "卡非法模式";
                    break;
                case READ_CARD_ERROR:
                    cErrorCode = "读卡数据失败";
                    cErrorVoice = "card_invalid";

                    break;
                case WRITE_CARD_ERROR:
                    cErrorCode = "写卡数据失败";
                    cErrorVoice = "card_reswipe";

                    break;
                case BURSEBLOCK_DATA_ERROR:
                    cErrorCode = "钱包数据损坏";
                    cErrorVoice = "card_invalid";
                    break;
                case NOFOUND_PSAM:
                    cErrorCode = "PSAM错误";
                    break;
                case RefuseCode:
                    cErrorCode = "无效卡";
                    cErrorVoice = "card_invalid";
                    break;

                case INVALID_QRCODE:
                    cErrorCode = "无效二维码";
                    cErrorVoice = "invalid_qrcode";
                    break;

                case DISABLE_QRCODE:
                    cErrorCode = "不支持扫码";
                    cErrorVoice = "disableqr";
                    break;

                case NETCOM_ERR:
                    cErrorCode = "通讯故障";
                    cErrorVoice = "netcom_fail";
                    break;

                case RECORD_FULL:
                    cErrorCode = "交易记录已满";
                    break;

                default:
                    cErrorCode = "错误";
                    cErrorVoice = "card_reswipe";
                    break;
            }
            SoundPlay.VoicePlay(cErrorVoice);

            Message message = Message.obtain();
            List<String> Strlist = new ArrayList<String>();
            Strlist.add(0, String.valueOf(iErrorCode));
            Strlist.add(1, cErrorCode);
            message.obj = Strlist;
            message.what = CARD_ERR;
            if (gUICardHandler != null)
                gUICardHandler.sendMessage(message);
        }
    }

    //卡片业务统一错误信息提示(冲正模式)
    public static void ShowCardErrorInfo_Dispel(int iErrorCode) {
        String cErrorCode = "";
        String cErrorVoice = "";
        if (g_LocalInfo.cDockposFlag == 1) {
            SerialFunc.showCardError(iErrorCode);
        }
        if (iErrorCode > 0) {
            switch (iErrorCode) {
                case CARD_INVALIED:
                    cErrorCode = "无效卡";
                    cErrorVoice = "card_invalid";
                    break;
                case OUT_OF_CONSUMERANGE:
                    cErrorCode = "超出消费范围";
                    cErrorVoice = "card_limit";
                    break;
                case NOT_ENOUGH_MONEY:
                    cErrorCode = "余额超限";
                    cErrorVoice = "money_lack";
                    break;
                case ILLEGAL_CARD:
                    cErrorCode = "非法卡";
                    cErrorVoice = "card_invalid";
                    break;
                case BEYOND_LIMIT_TIM:
                    cErrorCode = "超出有效期";
                    cErrorVoice = "card_limit";
                    break;
                case BEYOND_SINGLE_MONEY:
                    cErrorCode = "超出单笔消费限额";
                    cErrorVoice = "card_limit";
                    break;
                case BEYOND_DAY_TOTAL:
                    cErrorCode = "超出累计限制";
                    cErrorVoice = "card_limit";
                    break;
                case CHASE_FAIL:
                    cErrorCode = "追扣失败";

                    break;
                case CONSUME_LIMIT_COUNT:
                    cErrorCode = "消费限次";
                    cErrorVoice = "card_limit";
                    break;
                case CANNOT_CANCEL_PAYMENT:
                    cErrorCode = "不允许冲正";
                    break;
                case CANCEL_PAYMENT_FAIL:
                    cErrorCode = "冲正失败";
                    break;
                case PAY_PSWAUTHEN_FAIL:
                    cErrorCode = "密码验证失败";
                    cErrorVoice = "card_invalid";
                    break;
                case BALANCE_BEYOND_LIMIT:
                    cErrorCode = "超出限额";
                    cErrorVoice = "card_limit";
                    break;
                case NO_FIND_CARD:
                    cErrorCode = "未找到卡片";
                    cErrorVoice = "card_reswipe";
                    break;
                case READ_CARDNUM_FAIL:
                    cErrorCode = "读卡失败";
                    cErrorVoice = "card_invalid";
                    break;
                case STATUS_NUM_ERROR:
                    cErrorCode = "身份错误";
                    cErrorVoice = "card_disable";

                    break;
                case NOFOUND_CHASEBURSE:
                    cErrorCode = "无追扣钱包";
                    break;
                case DATE_TIME_ERROR:
                    cErrorCode = "时钟模块故障";
                    break;

                case READ_CARDTYPE_INVALIED:
                    cErrorCode = "卡非法模式";
                    break;
                case READ_CARD_ERROR:
                    cErrorCode = "读卡数据失败";
                    cErrorVoice = "card_invalid";

                    break;
                case WRITE_CARD_ERROR:
                    cErrorCode = "写卡数据失败";
                    cErrorVoice = "card_reswipe";

                    break;
                case BURSEBLOCK_DATA_ERROR:
                    cErrorCode = "钱包数据损坏";
                    cErrorVoice = "card_invalid";
                    break;
                case NOFOUND_PSAM:
                    cErrorCode = "PSAM错误";
                    break;

                case NORUSHRECORD:
                    cErrorCode = "无冲正交易记录";
                    cErrorVoice = "norerecord";
                    break;

                case HASDISPEL:
                    cErrorCode = "已经冲正";
                    cErrorVoice = "yichongzheng";
                    break;

                case DISPELFAIL:
                    cErrorCode = "冲正失败";
                    cErrorVoice = "recorddispel_error";
                    break;

                case DISPELTOUT:
                    cErrorCode = "冲正超时";
                    cErrorVoice = "recorddispel_error";
                    break;

                case RefuseCode:
                    cErrorCode = "无效卡";
                    cErrorVoice = "card_invalid";
                    break;
                case RECORD_FULL:
                    cErrorCode = "交易记录已满";
                    break;

                default:
                    cErrorCode = "错误";
                    cErrorVoice = "card_reswipe";
                    break;
            }
            SoundPlay.VoicePlay(cErrorVoice);

            Message message = Message.obtain();
            List<String> Strlist = new ArrayList<String>();
            Strlist.add(0, String.valueOf(iErrorCode));
            Strlist.add(1, cErrorCode);
            message.obj = Strlist;
            message.what = 100;
            if (gUIRDCardHandler != null)
                gUIRDCardHandler.sendMessage(message);
        }
    }

    //二维码业务统一错误信息提示
    public static void ShowQRErrorInfo(int iErrorCode) {
        String cErrorCode = "";
        String cErrorVoice = "";
        /*
        1：E506/10404071-超出消费范围
        2：11100118/10404204/11100104-不是有效卡户
        3：10404070-已过有效期
        4：11100006-存储过程异常
        5：25401003-对接方不存在
        6：25401005/11100101-站点不存在
        7：25401001-没有未处理的流水
        8：25401002-不是唯一账号
        9：25401004-第三方流水号不存在
        10：11101105-重账流水
        11：11605004-操作超时
        12：11708003-不存在对应未结帐收费单据
        13：10404102-余额不足
        14：11100100-消费类型错误
        15：11100106-操作员不存在
        16：11100107-钱包未启用
        17：00000105-商户号不存在
        18：11124010-冲正流水不存在
        19：10603014/11120010-余额复位钱包或者开环电子钱包
        20：10001/20001/30001/40001-第三方处理失败
        21：E010-消费限次
        22：E503-日累限额
        23：E504-日累限次
        24：E501-单笔密码限额，输入密码
        25：E502-日累密码限次
        26：11100103-没有找到匿名账户
        27：A10012-该应用服务目前不支持
        28：A10013-服务通讯超时
        29：A10014银联交易错误
        30：时间格式化异常
        31：catch异常
        32：E507-密码错误
        33：J0000001-未配置，配置文件未配置url或密钥
        34：J0000002-签名验证失败
        35：J0000003-账号类型不存在
        36：J0000004-查询订单失败
        37：S10038-账户未绑定
        38：E011-开环钱包不允许该操作
        39：11100119-超出终端单笔限额
        */
        if (iErrorCode > 0) {
            String[] strTmp=strParseRefuseCode(iErrorCode);
            cErrorCode = strTmp[0];
            cErrorVoice = strTmp[1];

            SoundPlay.VoicePlay(cErrorVoice);
            Message message = Message.obtain();
            List<String> Strlist = new ArrayList<String>();
            Strlist.add(0, String.valueOf(iErrorCode));
            Strlist.add(1, cErrorCode);
            message.obj = Strlist;
            message.what = CARD_ERR;
            if (gUICardHandler != null)
                gUICardHandler.sendMessage(message);
        }
    }
	
    //人脸业务统一错误信息提示
    public static void ShowFaceErrorInfo(int iErrorCode) {
        String cErrorCode = "";
        String cErrorVoice = "";

        if (iErrorCode >= 0) {
            switch (iErrorCode) {
                case OK:
                    cErrorCode = "";
                    cErrorVoice = "";
                    break;

                case INVALID_QRCODE:
                    cErrorCode = "无效二维码";
                    cErrorVoice = "invalid_qrcode";
                    break;

                case DISABLE_QRCODE:
                    cErrorCode = "不支持扫码";
                    cErrorVoice = "disableqr";
                    break;

                case NETCOM_ERR:
                    cErrorCode = "通讯故障";
                    cErrorVoice = "netcom_fail";
                    break;

                case FACE_UNREGIST:
                    cErrorCode = "人脸信息未注册";
                    cErrorVoice = "face_unregist";
                    break;

                case FACE_INVALID:
                    cErrorCode = "非法人脸";
                    cErrorVoice = "face_invalid";
                    break;

                case FACE_SAMEACC:
                    cErrorCode = "相同人脸,请等待...";
                    //cErrorVoice = "face_invalid";
                    break;

                default:
                    cErrorCode = "错误";
                    cErrorVoice = "card_reswipe";
                    break;
            }
            if(iErrorCode!=OK)
                SoundPlay.VoicePlay(cErrorVoice);

            Message message = Message.obtain();
            List<String> Strlist = new ArrayList<String>();
            Strlist.add(0, String.valueOf(iErrorCode));
            Strlist.add(1, cErrorCode);
            message.obj = Strlist;
            message.what = 200;
            if (gUICardHandler != null)
                gUICardHandler.sendMessage(message);
        }
    }

    //卡片显示信息
    //CARD_NULL = 0;     //无卡片
    //CARD_INMONEY = 1;  //输入金额
    //CARD_READOK = 2;   //读卡成功
    //CARD_PAYOK = 3;    //支付成功
    //CARD_PAYALLOK = 4; //支付成功(工作钱包-追扣钱包)
    public static void ShowCardInfo(CardBasicParaInfo CardBasicInfo, int ucType) {

        Message message = Message.obtain();
        message.obj = CardBasicInfo;
        message.what = ucType;

        if (gUICardHandler != null)
            gUICardHandler.sendMessage(message);
    }

    //显示卡片交易信息(冲正)
    public static void ShowCardInfo_Dispel(CardBasicParaInfo CardBasicInfo, int ucType) {

        Message message = Message.obtain();
        message.obj = CardBasicInfo;
        message.what = ucType;
        if (gUIRDCardHandler != null)
            gUIRDCardHandler.sendMessage(message);
    }

    //显示QR交易信息
    public static void ShowQRCodeInfo(ShopQRCodeInfo pShopQRCodeInfo, int ucType) {

        Message message = Message.obtain();
        message.obj = pShopQRCodeInfo;
        message.what = ucType;

        if (gUICardHandler != null)
            gUICardHandler.sendMessage(message);
    }

    //进入低功耗模式
    public static void PowerSaveDeal( int cType) {

        Message message = Message.obtain();
        message.obj = cType;
        message.what = POWERSAVEDEAL;

        Log.d(TAG,"进入低功耗模式:"+cType);
        if (gUIMainHandler != null)
            gUIMainHandler.sendMessage(message);

        if (gUICardHandler != null)
            gUICardHandler.sendMessage(message);
    }

    //判断文件是否存在
    public static int FileIsExists(String sFileName) {

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            //Log.i(TAG,"文件存在："+sFileName);
            return 0;
        }
        return 1;
    }

    //判断卡内时间的有效性失效日期
    public static int LimitDateVaild(long lngValidTime) {
        long lngTemp;                 //临时变量
        long lngValidYear;             //有效年
        long lngValidMonth;             //有效月
        long lngValidDay;             //有效日
        int wIntervalDay;
        byte[] cInDate = new byte[3];

        // 从卡内的时间中分离出年、月、日
        //年
        lngTemp = lngValidTime;
        lngValidYear = (lngTemp & 0x0000FE00) >> 9;
        //Log.i(TAG,String.format("卡中的年:%d",lngValidYear));

        //月
        lngTemp = lngValidTime;
        lngValidMonth = (lngTemp & 0x000001E0) >> 5;
        //Log.i(TAG,String.format("卡中的月:%d",lngValidMonth));

        //日
        lngTemp = lngValidTime;
        lngValidDay = lngTemp & 0x0000001F;
        //Log.i(TAG,String.format("卡中的日:%d",lngValidDay));

        cInDate[0] = (byte) (lngValidYear & 0x00ff);
        cInDate[1] = (byte) (lngValidMonth & 0x00ff);
        cInDate[2] = (byte) (lngValidDay & 0x00ff);
        wIntervalDay = (int) CompareDayInterval(cInDate);
        if (wIntervalDay <= 0) {
            return 1;
        }
        //年检提醒
        if (wIntervalDay < 30) {
            g_WorkInfo.cYearCheckState = 1;
        }
        return 0;
    }

    //获取基线时间
    public static void GetCurrBaseDateTime() {
        int i;
        int cResult;
        int wIntervalDay;
        byte[] cDataTime = new byte[6];

        Log.d(TAG, "开机运行程序获取时钟基线");
        GetCurrDateTime(cDataTime);

        //判断是否小于末笔交易时间
        wIntervalDay = CompareDayOrDayInterval(g_RecordInfo.cLastPaymentDate, cDataTime);
        if (wIntervalDay > 0) {
            memcpy(cDataTime, g_RecordInfo.cLastPaymentDate, 6);
            Log.e(TAG, String.format("当前时间小于末笔交易时间,以末笔交易时间为准:%x.%x.%x.%x.%x.%x",
                    cDataTime[0],cDataTime[1],cDataTime[2],cDataTime[3],cDataTime[4],cDataTime[5]));
            Publicfun.SetDateTime(cDataTime);
        }
        memcpy(g_WorkInfo.cCurDateTime, cDataTime, 6);
        memcpy(g_WorkInfo.cOKDataTime, cDataTime, 6);
        memcpy(g_WorkInfo.cRecordDataTime, cDataTime, 6);
        g_WorkInfo.lngPowerSaveCnt = System.currentTimeMillis();
        return;
    }

    //解析卡中日期时间(6位年4位月5位日5位时6位分6位秒)
    public static void ParseCardDateTime(long LastPayDate) {
        int CardYear;
        int CardMonth;
        int CardDay;
        int CardHour;
        int CardMin;
        int CardSec;

        CardYear = (int) ((LastPayDate & 0xFC000000) >> 26);
        CardMonth = (int) ((LastPayDate & 0x03C00000) >> 22);
        CardDay = (int) ((LastPayDate & 0x003E0000) >> 17);

        CardHour = (int) ((LastPayDate & 0x0001F000) >> 12);
        CardMin = (int) ((LastPayDate & 0x00000FC0) >> 6);
        CardSec = (int) (LastPayDate & 0x0000003F);

        Log.i(TAG, String.format("卡内日期6：%d年%d月%d日", CardYear, CardMonth, CardDay));
        Log.i(TAG, String.format("卡内时间6：%d时%d分%d秒", CardHour, CardMin, CardSec));
    }

    //正负数的钱包余额处理
    public static long TransBurseMoney(byte[] cMoneyContext, int iPos) {
        byte bTemp;
        long lngTemp;
        byte[] cMoneyTmp = new byte[3];

        System.arraycopy(cMoneyContext, iPos, cMoneyTmp, 0, cMoneyTmp.length);
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

    //处理日期为卡中格式
    public static int ChangeCardDate(byte[] invalidTime) {
        int winvalidDate;
        int iYear, iMonth, iDay;
        byte[] cTempContext = new byte[2];
        //有效时限
        iYear = invalidTime[0];
        iMonth = invalidTime[1];
        iDay = invalidTime[2];

        cTempContext[1] = (byte) (iYear << 1);
        if (iMonth >= 8) {
            cTempContext[1] = (byte) (cTempContext[1] + 1);
        }
        cTempContext[0] = (byte) (iMonth << 5);
        cTempContext[0] = (byte) ((cTempContext[0] & 0xff) + iDay);

        //有效时限
        winvalidDate = (cTempContext[0] & 0xff) + (cTempContext[1] & 0xff) * 256;
        return winvalidDate;
    }

    //比较脱机时间天数
    public static int CompareCanOffCount(byte[] bCurDateTime) {

        long lngSignDate, lngNowDate;

//        Log.i(TAG,(String.format("签到时间:%d-%d-%d",
//                g_RecordInfo.cCurSignInDate[0],
//                g_RecordInfo.cCurSignInDate[1],
//                g_RecordInfo.cCurSignInDate[2])));

        lngSignDate = (g_RecordInfo.cCurSignInDate[0] & 0xff) * 365 + (g_RecordInfo.cCurSignInDate[1] & 0xff) * 30 + (g_RecordInfo.cCurSignInDate[2] & 0xff);
        lngNowDate = (bCurDateTime[0] & 0xff) * 365 + (bCurDateTime[1] & 0xff) * 30 + (bCurDateTime[2] & 0xff);
        //Log.i(TAG,String.format("签到时间:%d-%d-%d",lngNowDate,lngSignDate,g_StationInfo.cCanOffCount));
        if (lngNowDate <= lngSignDate + g_StationInfo.cCanOffCount) {
            return 1;
        }
        return 0;
    }

    public static void ClearPaymentInfo() {
        fill(g_WorkInfo.cPaymentMoney, (byte) 0);
        g_WorkInfo.cPaymentMoneyLen = 0;
        g_WorkInfo.cPaymentDotPos = 0;
        //g_WorkInfo.lngPaymentMoney=0;
        g_WorkInfo.lngTotalMoney = 0;
    }

    public static int CRC_Plus(WasteBooks stWasteBooks, int iLen) {

        int crc = 0;
        int lngCheckSum = 0;

        try {
            byte[] bytes = JavaStruct.pack(stWasteBooks, ByteOrder.LITTLE_ENDIAN);
            //校验码
            //iLen=bytes.length;
            for (int i = 0; i < iLen; i++) {
                lngCheckSum = lngCheckSum + (bytes[i] & 0xff);
            }
            crc = lngCheckSum & 0x0000FFFF;

        } catch (StructException e) {
            e.printStackTrace();
        }
        return crc;
    }

    public static int CRC_PlusA(Object obj, int iType) {

        int crc = 0;
        int iLen = 0;
        int lngCheckSum = 0;

        if (iType == 0) {
            WasteBooks stWasteBooks = (WasteBooks) (obj);
            try {
                byte[] bytes = JavaStruct.pack(stWasteBooks, ByteOrder.LITTLE_ENDIAN);
                //校验码
                iLen = bytes.length;
                for (int i = 0; i < iLen; i++) {
                    lngCheckSum = lngCheckSum + (bytes[i] & 0xff);
                }
                crc = lngCheckSum & 0x0000FFFF;

            } catch (StructException e) {
                e.printStackTrace();
            }
        } else {
            WasteQrCodeBooks stQrWasteBooks = (WasteQrCodeBooks) (obj);
            try {
                byte[] bytes = JavaStruct.pack(stQrWasteBooks, ByteOrder.LITTLE_ENDIAN);
                //校验码
                iLen = bytes.length;
                for (int i = 0; i < iLen; i++) {
                    lngCheckSum = lngCheckSum + bytes[i];
                }
                crc = lngCheckSum & 0xFFFF;

            } catch (StructException e) {
                e.printStackTrace();
            }
        }
        return crc;
    }

    //判断日累和餐累是否同一天
    public static void CheckDayAndBusinessTotal() {

        int cResult;
        int cBusinessID;
        //判断是否是同一天
        cResult = Publicfun.CompareStatLastDate(g_RecordInfo.cLastPaymentDate);
        if (cResult == 0) {
            //日累统计
            Log.i(TAG, "日累统计同一天");
            //餐累统计
            cBusinessID = GetCurrBusinessID();
            if ((cBusinessID != g_RecordInfo.cLastBusinessID) && (cBusinessID != 0)) {
                Log.i(TAG, "餐累统计不是同一餐");
                g_RecordInfo.lngTotalBusinessMoney = 0;
                g_RecordInfo.wTotalBusinessSum = 0;
            }
        } else {
            Log.i(TAG, "日累统计不是同一天");
            g_RecordInfo.lngTodayPaymentMoney = 0;
            g_RecordInfo.wTodayPaymentSum = 0;
            g_RecordInfo.lngTotalBusinessMoney = 0;
            g_RecordInfo.wTotalBusinessSum = 0;
        }
    }

    //判断末笔交易时间是否是同一天
    public static int CompareStatLastDate(byte[] cLastPaymentDate) {
        byte[] CurrentDate = new byte[6];
        int NowYear, NowMonth, NowDay;
        int iValidYear, iValidMonth, iValidDay;             //有效日
        int cResult;

        iValidYear = cLastPaymentDate[0];
        iValidMonth = cLastPaymentDate[1];
        iValidDay = cLastPaymentDate[2];

        cResult = GetCurrDateTime(CurrentDate);
        if (cResult != 0) {
            return DATE_TIME_ERROR;
        }
        NowYear = CurrentDate[0];
        NowMonth = CurrentDate[1];
        NowDay = CurrentDate[2];

        //判断是否是同一天
        if ((iValidYear == NowYear) && (iValidMonth == NowMonth) && (iValidDay == NowDay)) {
            return 0;
        }
        return 1;
    }

    public static void ShowToast(Context context, String info) {
        Toast toast = Toast.makeText(context, info, Toast.LENGTH_LONG);
        LinearLayout layout = (LinearLayout) toast.getView();
//        layout.setDividerPadding(30);
//        layout.setBackgroundColor(Color.parseColor("#11FFFF"));
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.RED);
        v.setTextSize(30);

//toast.setGravity(Gravity.CENTER, 12, 20);//setGravity用来设置Toast显示的位置，相当于xml中的android:gravity或android:layout_gravity
        toast.show();
    }

    public static void ShowErrorStrDialog(Context context, String strTemp) {

        ShowToast(context, strTemp);
    }

    public static double StrToFloat(byte[] Number) {
        int Pos, Len;
        double cResult, lDec, Sign;

        Len = Number.length;

        Pos = 0;
        while (((Number[Pos] == 0x20) || (Number[Pos] == 0x09)) && (Pos < Len)) Pos++;

        if (Number[Pos] == '-') {
            Sign = -1.0;
            Pos += 1;
        } else if (Number[Pos] == '+') {
            Sign = 1.0;
            Pos += 1;
        } else {
            Sign = 1.0;
        }

        cResult = 0.0;
        for (; Pos < Len; Pos++) {
            if ((Number[Pos] >= 0x30) && (Number[Pos] <= 0x39)) {
                cResult = cResult * 10 + (Number[Pos] - 0x30);
            } else {
                break;
            }
        }
        if (Number[Pos] == '.') {
            Pos++;
            lDec = 0.1;
            for (; Pos < Len; Pos++, lDec /= 10) {
                if ((Number[Pos] >= 0x30) && (Number[Pos] <= 0x39)) {
                    cResult = cResult + (Number[Pos] - 0x30) * lDec;
                } else {
                    break;
                }
            }
        }
        return Sign * cResult;
    }

    //获取终端内码
    public static void GetTerInCode(byte[] cTerInCode) {
        int i;

        Random random = new Random();
        String result = "";
        for (i = 0; i < 4; i++) {
            int iRand = random.nextInt(255);
            cTerInCode[i] = (byte) iRand;
            Log.i(TAG, String.format("%02x", cTerInCode[i] & 0xff));
            Log.i(TAG, "" + iRand);
        }
    }

    //计算当前日期为第几天
    public static int GetCurWhichDay(int y, int m, int d) {
        int year;
        int sum = 0;
        int i;
        byte[] Y = {31, 0, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        year = y + 2000;
        if ((year % 400 == 0) || (year % 4 == 0 && year % 100 != 0)) {
            Y[1] = 29;
        } else {
            Y[1] = 28;
        }
        for (i = 0; i < m - 1; i++) {
            if (i <= 11)
                sum += Y[i];
        }
        sum += d;
        return sum;
    }

    //比较传入日期与当前日期相隔几天
    public static long CompareDayInterval(byte[] cInDate) {
        int wIntervalYear;
        int wIntervalDay;
        byte[] CurrentDate = new byte[6];
        long lngInDate = 0, lngCurDate = 0;

        GetCurrDateTime(CurrentDate);

        if (cInDate[0] == CurrentDate[0])//年相同
        {
            lngInDate = GetCurWhichDay(cInDate[0], cInDate[1], cInDate[2]);
            lngCurDate = GetCurWhichDay(CurrentDate[0], CurrentDate[1], CurrentDate[2]);
        } else if (cInDate[0] > CurrentDate[0])//输入年大于当前年
        {
            wIntervalYear = cInDate[0] - CurrentDate[0];
            lngInDate = wIntervalYear * 365 + (GetCurWhichDay(cInDate[0], cInDate[1], cInDate[2]));
            lngCurDate = GetCurWhichDay(CurrentDate[0], CurrentDate[1], CurrentDate[2]);

        } else if (cInDate[0] < CurrentDate[0])//输入年小于当前年
        {
            wIntervalYear = CurrentDate[0] - cInDate[0];
            lngInDate = GetCurWhichDay(cInDate[0], cInDate[1], cInDate[2]);
            lngCurDate = wIntervalYear * 365 + GetCurWhichDay(CurrentDate[0], CurrentDate[1], CurrentDate[2]);
        }
        wIntervalDay = (int) (lngInDate - lngCurDate);

        return wIntervalDay;
    }

    //比较传入日期之间相隔几天
    public static int CompareDayOrDayInterval(byte[] cInDate, byte[] CurrentDate) {
        int wIntervalYear;
        int wIntervalDay;
        long lngInDate = 0, lngCurDate = 0;

        if (cInDate[0] == CurrentDate[0])//年相同
        {
            lngInDate = GetCurWhichDay(cInDate[0], cInDate[1], cInDate[2]);
            lngCurDate = GetCurWhichDay(CurrentDate[0], CurrentDate[1], CurrentDate[2]);
        } else if (cInDate[0] > CurrentDate[0])//输入年大于当前年
        {
            wIntervalYear = cInDate[0] - CurrentDate[0];
            lngInDate = wIntervalYear * 365 + (GetCurWhichDay(cInDate[0], cInDate[1], cInDate[2]));
            lngCurDate = GetCurWhichDay(CurrentDate[0], CurrentDate[1], CurrentDate[2]);

        } else if (cInDate[0] < CurrentDate[0])//输入年小于当前年
        {
            wIntervalYear = CurrentDate[0] - cInDate[0];
            lngInDate = GetCurWhichDay(cInDate[0], cInDate[1], cInDate[2]);
            lngCurDate = wIntervalYear * 365 + GetCurWhichDay(CurrentDate[0], CurrentDate[1], CurrentDate[2]);
        }
        wIntervalDay = (int) (lngInDate - lngCurDate);

        return wIntervalDay;
    }


    //判断日期是否跨天
    public static int CheckSpanDayProcess() {
        int cResult;
        byte[] CurrentTime = new byte[8];
        int cYear, cMonth, cDay;
        int cRecordYear, cRecordMonth, cRecordDay;
        int lngCurDate, lngRecordDate;

        cResult = Publicfun.GetCurrDateTime(CurrentTime);
        if (cResult != 0) {
            return 1;
        }
        cYear = CurrentTime[0];
        cMonth = CurrentTime[1];
        cDay = CurrentTime[2];
        lngCurDate = (cYear * 365 + GetCurWhichDay(cYear, cMonth, cDay));

        cRecordYear = g_WorkInfo.cRecordDataTime[0];
        cRecordMonth = g_WorkInfo.cRecordDataTime[1];
        cRecordDay = g_WorkInfo.cRecordDataTime[2];
        lngRecordDate = (cRecordYear * 365 + GetCurWhichDay(cRecordYear, cRecordMonth, cRecordDay));

        //判断是否日期变化
        if ((cYear != cRecordYear) || (cMonth != cRecordMonth) || (cDay != cRecordDay)) {
            //Log.i(TAG,String.format("日期发生变化 lngCurDate:%d,lngRecordDate:%d",lngCurDate,lngRecordDate));
            if ((lngCurDate > lngRecordDate) && (lngCurDate == (lngRecordDate + 1))) {
                Log.e(TAG, "日期发生增加的变化");
                System.arraycopy(CurrentTime, 0, g_WorkInfo.cRecordDataTime, 0, 6);
                return 0;
            }
        }
        return 1;
    }

    //清除餐累营业统计
    public static void ClearBusinessStat() {
        g_RecordInfo.wTotalBusinessSum = 0;
        g_RecordInfo.lngTotalBusinessMoney = 0;
        g_RecordInfo.cLastBusinessID = g_WorkInfo.cBusinessID;

        RecordInfoRW.WriteAllRecordInfo(g_RecordInfo);
        return;
    }

    //获取当前营业分组号
    public static int GetCurrBusinessID() {
        int i;
        int iTimeID;
        byte[] CurrentTime = new byte[6];
        int iNowTime, iStartTime, iEndTime;

        GetCurrDateTime(CurrentTime);

        iNowTime = CurrentTime[3] * 60 + CurrentTime[4];
        for (i = 0; i < 128; i++) {
            if (g_BuinessInfo.get(i).cBusinessID != 0) {
                iStartTime = g_BuinessInfo.get(i).cStartTime[0] * 60 + g_BuinessInfo.get(i).cStartTime[1];
                iEndTime = g_BuinessInfo.get(i).cEndTime[0] * 60 + g_BuinessInfo.get(i).cEndTime[1];

                //Log.i(TAG,String.format("%d,营业时间:%d-%d",iNowTime,iStartTime,iEndTime));
                if ((iNowTime >= iStartTime) && (iNowTime <= iEndTime)) {
                    //Log.i(TAG,String.format("当前在营业分组内：%d",g_BuinessInfo.get(i).cBusinessID));
                    g_WorkInfo.cBusinessID = (byte) g_BuinessInfo.get(i).cBusinessID;

                    //营业分组时间开始
                    System.arraycopy(g_BuinessInfo.get(i).cStartTime, 0, g_WorkInfo.cStartTime, 0, 2);
                    //营业分组时间结束
                    System.arraycopy(g_BuinessInfo.get(i).cEndTime, 0, g_WorkInfo.cEndTime, 0, 2);

                    iTimeID = (i % 8);
                    if (iTimeID > 5)
                        iTimeID = 5;
                    //Log.i(TAG,"餐次改变定额值");
                    g_WorkInfo.wBookMoney = g_OddKeyInfo.wKeyMoney[iTimeID];
                    if (g_LocalInfo.cInputMode == 3) {
                        //Log.i(TAG,"餐次改变交易金额值");
                        g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
                    }
                    g_WorkInfo.cIsInterDay = 0;
                    return g_WorkInfo.cBusinessID;
                }
            }
        }
        return 0;
    }

    //比较营业分组时间
    public static int CompareBusinessDate(byte[] CurrentTime) {
        int i;
        int iTimeID;
        int iNowTime, iStartTime, iEndTime;

        //判断参数是否存在
        if (g_BuinessInfo.size() == 0) {
            Log.i(TAG, "营业分组参数为空，退出");
            return 1;
        }
        iNowTime = CurrentTime[3] * 60 + CurrentTime[4];
        for (i = 0; i < 128; i++) {
            if (g_BuinessInfo.get(i).cBusinessID != 0) {
                iStartTime = g_BuinessInfo.get(i).cStartTime[0] * 60 + g_BuinessInfo.get(i).cStartTime[1];
                iEndTime = g_BuinessInfo.get(i).cEndTime[0] * 60 + g_BuinessInfo.get(i).cEndTime[1];

                //Log.i(TAG,String.format("%d,营业时间:%d-%d",iNowTime,iStartTime,iEndTime));
                if ((iNowTime >= iStartTime) && (iNowTime <= iEndTime)) {
                    //Log.i(TAG,String.format("当前在营业分组内：%d",g_BuinessInfo.get(i).cBusinessID));
                    g_WorkInfo.cBusinessID = (byte) g_BuinessInfo.get(i).cBusinessID;

                    //营业分组时间开始
                    System.arraycopy(g_BuinessInfo.get(i).cStartTime, 0, g_WorkInfo.cStartTime, 0, 2);
                    //营业分组时间结束
                    System.arraycopy(g_BuinessInfo.get(i).cEndTime, 0, g_WorkInfo.cEndTime, 0, 2);

                    iTimeID = (i % 8);
                    if (iTimeID > 5) {
                        iTimeID = 5;
                    }
                    //Log.i(TAG,"餐次改变定额值");
                    g_WorkInfo.wBookMoney = g_OddKeyInfo.wKeyMoney[iTimeID];
                    if (g_LocalInfo.cInputMode == 3) {
                        //Log.i(TAG,"餐次改变交易金额值");
                        g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
                    }
                    g_WorkInfo.cIsInterDay = 0;
                    return 0;
                }
            }
        }
        return 1;
    }

    //营业控制
    public static void ControlBusiness() {
        int cResult = 0;

        if ((g_BasicInfo.cSystemState != 100) || (g_StationInfo.iStationClass == LAN_EP_MONEYPOS)) {
            if (g_StationInfo.iStationClass == LAN_EP_MONEYPOS) {
                g_WorkInfo.wBookMoney = g_LocalInfo.wBookMoney[0];
                if (g_LocalInfo.cInputMode == 3) {
                    g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
                }
            }
            return;
        }
        g_WorkInfo.iBusinessCount++;
        if (g_WorkInfo.cRunState >= 0 || g_WorkInfo.cBusinessState > 0) {
            if (g_WorkInfo.iBusinessCount > BUSINESS_TIME) {
                g_WorkInfo.iBusinessCount = 0;
                //Log.i(TAG,"营业分组判断:"+g_WorkInfo.cBusinessID);
                Publicfun.GetCurrDateTime(g_WorkInfo.cCurDateTime);
                //判断是否在营业分组
                cResult = Publicfun.CompareBusinessDate(g_WorkInfo.cCurDateTime);
                if (cResult == OK) {
                    //Log.i(TAG,"进入营业分组:"+g_WorkInfo.cBusinessID);
                    //营业时段不同时并且有多个商户参数，需要刷商户卡进行选择商户
                    if (g_WorkInfo.cBusinessID != g_RecordInfo.cLastBusinessID) {
                        //多个商户切换
                        Log.i(TAG, "商户号:" + g_StationInfo.iShopUserID);
                        if (g_StationInfo.iShopUserID == 0) {
                            Log.e(TAG, "系统有多商户,不允许使用");
                            return;
                            /*
                            //提示刷商户卡
                            cResult=SetShopUserCard();
                            if(cResult==OK)
                            {
                                Log.i(TAG,"设置商户号成功");

                            }
                            else
                            {
                                Log.i(TAG,"设置商户号失败");
                                g_WorkInfo.iBusinessCount=30;
                                Publicfun.ShowErrorInfo(SHOPER_DISABLE);
                                return;
                            }
                            */
                        } else {
                            //记录系统商户号
                            g_LocalInfo.wShopUserID = g_StationInfo.iShopUserID;
                            Log.i(TAG, "只有一个商户:" + g_LocalInfo.wShopUserID);
                            cResult = LocalInfoRW.WriteAllLocalInfo(g_LocalInfo);
                            if (cResult != OK) {
                                Log.i(TAG, "写数据失败");
                            }
                        }
                        //清除营业段统计数据
                        Publicfun.ClearBusinessStat();
                    }

                    if (g_WorkInfo.cBusinessState == 1) {
                        //g_WorkInfo.cRunState=1;
                    }
                    if (g_WorkInfo.cBusinessState == 2) {
                        //g_WorkInfo.cRunState=2;
                    }
                    g_WorkInfo.cBusinessState = 0;
                } else {
                    Log.i(TAG, "不在营业分组");
                    if (g_WorkInfo.cRunState == 1) {
                        g_WorkInfo.cBusinessState = 1;
                    }
                    if (g_WorkInfo.cRunState == 2) {
                        g_WorkInfo.cBusinessState = 2;
                    }
                    if (g_WorkInfo.cRunState == 5) {
                        g_WorkInfo.cBusinessState = 5;
                    }
                }
                //判断是否跨天了
                cResult = Publicfun.CheckSpanDayProcess();
                if (cResult == OK) {
                    if ((g_CardInfo.cExistState == 0) && (g_WorkInfo.cTestState == 0)) {
                        Log.i(TAG, "跨天无卡片时处理");
                        //等待一分钟后重启，为了防止RTC时钟写错的概率
                        RunShellCmd("reboot");
                    }
                }
            }
        }
    }


    //获取交易模式名称
    public static String GetPayModeStr(short cInputMode) {
        String strInputMod[] = {"普通模式", "单键模式", "定额模式", "商品编码模式", "冲正模式", "测试模式"};

        return strInputMod[cInputMode - 1];
    }


    //判断支付宝的支付认证码
    public static int CheckAliPayAuthcode(byte[] cQRCodeInfo, int RecvLen) {
        int i;
        int iLen;
        int iAuthCode;

        iLen = RecvLen;
        //支付宝授权码，25~30开头的长度为16~24位的数字，实际字符串长度以开发者获取的付款码长度为准
        if ((iLen >= 16) && (iLen <= 24)) {
            //判断数字
            for (i = 0; i < iLen; i++) {
                if ((cQRCodeInfo[i] >= 0x30) && (cQRCodeInfo[i] <= 0x39)) {
                    continue;
                } else {
                    Log.i(TAG, "无效字符:" + cQRCodeInfo[i]);
                    return 1;
                }
            }
            //25~30开头
            iAuthCode = ((cQRCodeInfo[0] - 0x30) * 10 + (cQRCodeInfo[1] - 0x30));
            if ((iAuthCode >= 25) && (iAuthCode <= 30)) {
                return 0;
            }
        }
        return 1;
    }

    //判断支付认证码合法性
    public static int CheckPayAuthcode(byte[] cQRCodeInfo, int RecvLen) {
        int i;
        int iLen;
        int iAuthCode;

        iLen = RecvLen;
        if (iLen < 10) {
            return 1;
        }
        return 0;
    }

    //设置wifi网络参数
    public static void SetWifiParaFile(WifiParaInfo pWifiParaInfo) {
        //写ini文件数据
        String fileName = ZYTKPath + "DeviceWifiPara.ini";
        Map<String, String> DevWifiParaMap = new HashMap<>();

        DevWifiParaMap.put("InfoState", ("" + pWifiParaInfo.cInfoState)); //WIFI是否刷了设置卡
        DevWifiParaMap.put("IPMode", ("" + pWifiParaInfo.cIPMode));  //WIFI获取IP的方式 0x01(DHCP)    0x00(static IP)
        DevWifiParaMap.put("UserName", pWifiParaInfo.strUserName);    //wifi用户名
        DevWifiParaMap.put("Password", pWifiParaInfo.strPassword); //wifi密码

        String strToast = "写入WIFI配置文件的数据------" +
                "wifi状态:" + DevWifiParaMap.get("InfoState")
                + "--WIFI获取IP的方式:" + DevWifiParaMap.get("IPMode")
                + "--wifi用户名:" + DevWifiParaMap.get("UserName")
                + "--wifi密码:" + DevWifiParaMap.get("Password");

        //Log.i(TAG,strToast);
        Log.d(TAG, strToast);
        FileUtils.WriteMapToFile(DevWifiParaMap, fileName);
    }

    //读取WIFI网络配置工作参数文件
    public static WifiParaInfo ReadWifiInfoStrCfg() {
        Map<String, String> InfoMap = new HashMap<>();

        String cInfoState = "0";        //WIFI是否刷了设置卡
        String cIPMode = "0";         //WIFI获取IP的方式 0x01(DHCP)    0x00(static IP)
        String strUserName = "";        //wifi用户名
        String strPassword = "";        //wifi密码

        //读取MAP数据
        String fileName = ZYTKPath + "DeviceWifiPara.ini";
        InfoMap = FileUtils.ReadMapFileData(fileName);
        if (InfoMap != null) {
            cInfoState = InfoMap.get("InfoState");
            cIPMode = InfoMap.get("IPMode");
            strUserName = InfoMap.get("UserName");
            strPassword = InfoMap.get("Password");
        }
        //解析网络数据
        WifiParaInfo StructData = new WifiParaInfo();

        StructData.cInfoState = Integer.parseInt((cInfoState));
        StructData.cIPMode = Integer.parseInt((cIPMode));
        StructData.strUserName = (strUserName);
        StructData.strPassword = (strPassword);
        StructData.strSSID = (strUserName);
        StructData.iQualityLevel = (0);

        return StructData;
    }

    //读取人脸识别名单信息
    public static int ReadFaceIdentInfo() {
        //获取人脸名单数量
        g_FaceIdentInfo.FaceNameList = ReadFaceCodeInfoData();
        if (g_FaceIdentInfo.FaceNameList != null) {
            Log.e(TAG, "写备份特征码信息文件ini");
            g_FaceIdentInfo.iListNum = g_FaceIdentInfo.FaceNameList.size();
            //更新acc列表
            g_FaceIdentInfo.FaceAccList = new ArrayList<>();
            for (int i = 0; i < g_FaceIdentInfo.iListNum; i++) {
                String strTmp = g_FaceIdentInfo.FaceNameList.get(i);
                String[] split = strTmp.split(",");
                g_FaceIdentInfo.FaceAccList.add(split[0]);
            }
            //更新名单列表
            FileUtils.WriteListToFile((ArrayList) g_FaceIdentInfo.FaceNameList, ZYTKFacePath + "facename.ini");
        } else {
//            //判断是否有ini文件(兼容老版本)
//            g_FaceIdentInfo.FaceNameList=FileUtils.ReadListFileData(ZYTKFacePath+"facename.ini");
//            if(g_FaceIdentInfo.FaceNameList!=null)
//            {
//                Log.e(TAG,"无人脸特征码文件(兼容老版本)");
//                g_FaceIdentInfo.iListNum=g_FaceIdentInfo.FaceNameList.size();
//
//                long lngStart=System.currentTimeMillis();
//                byte[] sbAllTemp=new byte[64*100000];
//                for(int i=0;i<g_FaceIdentInfo.iListNum;i++)
//                {
//                    String strTemp=g_FaceIdentInfo.FaceNameList.get(i);
//                    byte[] sbTemp = strTemp.getBytes();
//                    memcpy(sbAllTemp,i*64,sbTemp,0,sbTemp.length);
//                    //FaceCodeInfoRW.WriteFaceCodeInfoData(sbTemp,i);
//                }
//                WriteFaceCodeInfoAllData(sbAllTemp);
//                Log.e(TAG,"=========耗时"+(System.currentTimeMillis()-lngStart));
//            }
//            else
//            {
//                Log.e(TAG,"无人脸特征码文件");
//                g_FaceIdentInfo.FaceAccList= new ArrayList<>();
//                g_FaceIdentInfo.FaceNameList= new ArrayList<>();
//                g_FaceIdentInfo.iListNum=0;
//                g_FaceCodeInfo.lngLocalVer=0;
//                g_FaceCodeInfo.lngPlatVer=0;
//            }
            Log.e(TAG, "无人脸特征码文件");
            g_FaceIdentInfo.FaceAccList = new ArrayList<>();
            g_FaceIdentInfo.FaceNameList = new ArrayList<>();
            g_FaceIdentInfo.iListNum = 0;
            g_FaceCodeInfo.lngLocalVer = 0;
            g_FaceCodeInfo.lngPlatVer = 0;
        }
        return g_FaceIdentInfo.iListNum;
    }

    //读取人脸识别名单信息
    public static void TestStrList() {
        List<String> StrList = new ArrayList<>(); //人脸名单列表
        long lngStart = 0;
        lngStart = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            String strTemp = "" + i + "," + i;
            //String strTemp= ""+i;
            StrList.add(strTemp);
        }
        Log.e(TAG, "=========耗时" + (System.currentTimeMillis() - lngStart));
        lngStart = System.currentTimeMillis();
        String str1 = "89999";
        int iLen = str1.length();
        for (int i = 0; i < StrList.size(); i++) {
            //判断是否一致
            String strTmp = StrList.get(i);
            if (iLen > strTmp.length())
                continue;
            //截取accnum
//            String[] split = strTmp.split(",");
//            if(split[0].equals("99999"))
            //if(strTmp.equals("99999"))

            //String str2=strTmp.substring(0, iLen);
            if (strTmp.substring(0, iLen).equals(str1)) {
                Log.e(TAG, "=========耗时1:" + (System.currentTimeMillis() - lngStart));
                break;
            }
        }
    }

    //获取文件夹文件名
    public static String[] GetFileName(String strFolder, final String strNameSuffix) {
        //判断是否存在文件夹
        File folder = new File(strFolder);
        if (!folder.exists()) {
            return null;
        }
        String[] strFileNames = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(strNameSuffix)) {
                    return true;
                }
                return false;
            }
        });
        return strFileNames;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath$Name String 原文件路径+文件名 如：data/user/0/com.test/files/abc.txt
     * @param newPath$Name String 复制后路径+文件名 如：data/user/0/com.test/cache/abc.txt
     * @return <code>true</code> if and only if the file was copied;
     * <code>false</code> otherwise
     */
    public boolean CopyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }

            /* 如果不需要打log，可以使用下面的语句
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            */
            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 复制文件夹及其中的文件
     *
     * @param oldPath String 原文件夹路径 如：data/user/0/com.test/files
     * @param newPath String 复制后的路径 如：data/user/0/com.test/cache
     * @return <code>true</code> if and only if the directory and files were copied;
     * <code>false</code> otherwise
     */
    public static boolean CopyFolder(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e("--Method--", "copyFolder: cannot create directory.");
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    CopyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.");
                    return false;
                } else if (!temp.isFile()) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.");
                    return false;
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.");
                    return false;
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024*1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }

                /* 如果不需要打log，可以使用下面的语句
                if (temp.isDirectory()) {   //如果是子文件夹
                    copyFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (temp.exists() && temp.isFile() && temp.canRead()) {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                 */
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 复制文件夹及其中的文件
     *
     * @param oldPath String 原文件夹路径 如：data/user/0/com.test/files
     * @param newPath String 复制后的路径 如：data/user/0/com.test/cache
     * @return <code>true</code> if and only if the directory and files were copied;
     * <code>false</code> otherwise
     */
    public static boolean CopyFaceLibFolder(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e("--Method--", "copyFolder: cannot create directory.");
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String[] files = oldFile.list();
            File temp;
            for (String file : files) {
                if(file.equals("libTHFaceLive_ko.so"))
                    continue;

                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }

                if (temp.isDirectory()) {   //如果是子文件夹
                    CopyFaceLibFolder(oldPath + "/" + file, newPath + "/" + file);
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.");
                    return false;
                } else if (!temp.isFile()) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.");
                    return false;
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.");
                    return false;
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[1024*1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //计算百分比i
    public static int CalcRatio(int iMole, int iDeno) {
        NumberFormat nt = NumberFormat.getPercentInstance();
        //设置百分数精确度2即保留两位小数
        nt.setMinimumFractionDigits(0);
        float percent = (float) iMole / iDeno;
        int iPercent = (int) (percent * 100);

        return iPercent;
    }

    //执行shell脚本命令
    public static int RunShellCmd(String command) {
        String strCmdRet = "";
        boolean isRoot = false;
        Log.i(TAG, "发送的shell指令:" + command);
        ShellUtils.CommandResult CommandResult = ShellUtils.execCommand(command, isRoot);

        Log.i("ShellUtils", "result:" + CommandResult.result);
        if (CommandResult.result == 0) {
            Log.i("ShellUtils", "successMsg:" + CommandResult.successMsg);
            strCmdRet = CommandResult.successMsg;
        } else {
            Log.i("ShellUtils", "errorMsg:" + CommandResult.errorMsg);
            strCmdRet = CommandResult.errorMsg;
        }
        return CommandResult.result;
    }

    //执行shell脚本命令
    public static String RunShellCmdA(String command) {
        String strCmdRet = "";
        boolean isRoot = false;
        //Log.i(TAG, "发送的shell指令:" + command);
        ShellUtils.CommandResult CommandResult = ShellUtils.execCommand(command, isRoot);

        Log.i("ShellUtils", "result:" + CommandResult.result);
        if (CommandResult.result == 0) {
            Log.i("ShellUtils", "successMsg:" + CommandResult.successMsg);
            strCmdRet = CommandResult.successMsg;
        } else {
            Log.i("ShellUtils", "errorMsg:" + CommandResult.errorMsg);
            strCmdRet = CommandResult.errorMsg;
        }
        return strCmdRet;
    }

    // 获取MAC地址 strNif "wlan0"  "eth0"
    public static String getAllIp(String strNif) {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase(strNif)) continue;
                byte[] macBytes = nif.getHardwareAddress();
                Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses();
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                    String strIP =inetAddress.getHostAddress().toString();
                    Log.d(TAG,"IP:"+strIP);
                    if (strIP != null) {
                        return strIP;
                    }
                }
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public static String getIP(Context context) {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    //以上是获取V4地址，如果要获取V6，可以将(inetAddress instanceof Inet4Address) 去掉即可。
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    /**
     * Get Ip address 自动获取IP地址
     *
     * @throws SocketException
     */
    public static String getIpAddress(String ipType) throws SocketException {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();

                if (ni.getName().equals(ipType)) {
                    Enumeration<InetAddress> ias = ni.getInetAddresses();
                    while (ias.hasMoreElements()) {
                        ia = ias.nextElement();
                        if (ia instanceof Inet6Address) {
                            continue;// skip ipv6
                        }
                        String ip = ia.getHostAddress();
                        String HostName = ia.getHostName();
                        Log.i("getIpAddress", "get the IpAddress-. " + ip + "");
                        Log.i("getHostName", "get the HostName-. " + HostName + "");
                        // 过滤掉127段的ip地址
                        if (!"127.0.0.1".equals(ip)) {
                            hostIp = ia.getHostAddress();
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.d("vivi", "get the IpAddress-. " + hostIp + "");
        return hostIp;
    }


    //计算流水CRC16校验
    public static int CRC16(WasteBooks stWasteBooks) {

        int crc = 0;
        int lngCheckSum = 0;

        try {
            byte[] bytes = JavaStruct.pack(stWasteBooks, ByteOrder.LITTLE_ENDIAN);
            //校验码
            int iLen = bytes.length;
            crc = Crc16.calcCrc16(bytes, bytes.length);

        } catch (StructException e) {
            e.printStackTrace();
        }
        return crc;
    }

    //计算流水CRC16校验
    public static int CRC16(WasteQrCodeBooks stWasteBooks) {

        int crc = 0;
        int lngCheckSum = 0;

        try {
            byte[] bytes = JavaStruct.pack(stWasteBooks, ByteOrder.LITTLE_ENDIAN);
            //校验码
            int iLen = bytes.length;
            crc = Crc16.calcCrc16(bytes, bytes.length);

        } catch (StructException e) {
            e.printStackTrace();
        }
        return crc;
    }

    //十进制转换成十六进制
    public static void ChangeToHex(byte[] Context, int iLen) {

        for (int i = 0; i < iLen; i++) {
            Context[i] = (byte) (((((Context[i] & 0xff) / 16) * 10) + ((Context[i] & 0xff) % 16)) & 0xff);
            //Log.d(TAG,String.format("%02x",Context[i]));
        }
    }

    /**
     * 毫秒转日期
     *
     * @param milliscond
     * @return
     */
    public static String toData(long milliscond) {
        Date date = new Date(milliscond);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String data = simpleDateFormat.format(gc.getTime());
        return data;
    }

    public static String toDataA(long milliscond) {
        Date date = new Date(milliscond);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");
        String data = simpleDateFormat.format(gc.getTime());
        return data;
    }

    /**
     * 根据日期转换为星期
     *
     * @param sDate
     * @return
     */
    public static String GetFullDateWeekTime(String sDate) {
        try {
            String formater = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat format = new SimpleDateFormat(formater);
            Date date = format.parse(sDate);
            format.applyPattern("yyyy-MM-dd E HH:mm:ss");
            return format.format(date);
        } catch (Exception ex) {
            System.out.println("TimeUtil  GetFullDateWeekTime" + ex.getMessage());
            return "";
        }
    }


    public static String getFullTime(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());

    }


    //获取人脸全名单列表账号位置
    public static int GetFaceListPos(String accnum, List<String> FaceInfoList) {
        int iPos = FaceInfoList.size();

        for (int i = 0; i < FaceInfoList.size(); i++) {
            //判断是否一致
            String strTmp = FaceInfoList.get(i);
            String[] split = strTmp.split(",");
            //截取accnum
            if (accnum.equals(split[0])) {
                iPos = i;
                break;
            }
        }
        return iPos;
    }

    //获取人脸accnum列表账号位置
    public static int GetFaceNumListPos(String accnum, List<String> FaceInfoList) {
        int iPos = FaceInfoList.size();

        for (int i = 0; i < FaceInfoList.size(); i++) {
            //判断是否一致
            String strTmp = FaceInfoList.get(i);
            if (accnum.equals(strTmp)) {
                iPos = i;
                break;
            }
        }
        return iPos;
    }

    public static String GetBytesToStr(byte[] bInTemp) {
        int i;
        for (i = 0; i < bInTemp.length; i++) {
            if (bInTemp[i] == 0x00)
                break;
        }
        byte[] bTemp = new byte[i];
        memcpy(bTemp, bInTemp, bTemp.length);
        String strTemp = null;
        try {
            strTemp = new String(bTemp, "GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return strTemp;
    }

    public static String GetMultiAccID(String strAccID) {
        String strTmpList[] = strAccID.split("-");
        return strTmpList[0];
    }

    //获取中文星期
    public static String GetWeekCHNName(String strTmp) {
//        星期一：Monday 缩写：Mon.
//        星期二：Tuesday 缩写：Tue.
//        星期三：Wednesday 缩写：Wed.
//        星期四：Thursday 缩写：Thur.
//        星期五：Friday 缩写：Fri.
//        星期六：Saturday 缩写：Sat.
//        星期日：Sunday 缩写：Sun.
        String strWeek = strTmp.substring(11, 14);
        if (strWeek.equals("Mon"))
            return "星期一";
        else if (strWeek.equals("Tue"))
            return "星期二";
        else if (strWeek.equals("Wed"))
            return "星期三";
        else if (strWeek.equals("Thu"))
            return "星期四";
        else if (strWeek.equals("Fri"))
            return "星期五";
        else if (strWeek.equals("Sat"))
            return "星期六";
        else if (strWeek.equals("Sun"))
            return "星期日";
        else
            return "星期";
    }

    //获取中文星期
    public static String GetWeekName(String strTmp) {
        strTmp = strTmp.substring(19).trim();
        if (strTmp.equals("周一") || strTmp.equals("Mon"))
            return "星期一";
        else if (strTmp.equals("周二") || strTmp.equals("Tue"))
            return "星期二";
        else if (strTmp.equals("周三") || strTmp.equals("Wed"))
            return "星期三";
        else if (strTmp.equals("周四") || strTmp.equals("Thu"))
            return "星期四";
        else if (strTmp.equals("周五") || strTmp.equals("Fri"))
            return "星期五";
        else if (strTmp.equals("周六") || strTmp.equals("Sat"))
            return "星期六";
        else if (strTmp.equals("周日") || strTmp.equals("Sun"))
            return "星期日";
        else
            return "星期";
    }

    //获取app文件名
    public static String GetAppFileName() {
        String strAppFileName = "";

        String strFoldPath = ZYTK35Path;
        File ZYTKFile = new File(strFoldPath);
        if (!ZYTKFile.exists()) {
            Log.e(TAG, "ZYTKFile is not exists");
            return strAppFileName;
        }
        if (ZYTKFile.isDirectory()) { //如果选择是个文件夹
            for (File sdFile : ZYTKFile.listFiles()) {
                if (sdFile.isFile()) {
                    String fileName = sdFile.getName();
                    if (fileName.endsWith(".apk") == true) {
                        if (fileName.length() > 23)
                            return fileName;
                    }
                }
            }
        }
        return strAppFileName;
    }

    //安装apk
    public static int InstallApk(String strAppName, int iMode) {
        int iRet = 0;
        String strVer = "";
        File fFile = new File(strAppName);
        if (fFile.exists() && fFile.isFile()) {
            if (iMode == 0)    //shell脚本执行
            {
                String strTmpList[] = strAppName.split("/");
                if (strTmpList.length > 1) {
                    String strTmpVer = strTmpList[strTmpList.length - 1];
                    if (strTmpVer.length() > 23) {
                        strVer = strTmpVer.substring(14, 25);
                    }
                }
                if (strVer.equals(""))
                    return 1;

                WriteSofeVerInfoFile(strVer);
                //pm install -i 包名 –user 0 apkpath
                //String strCmd="pm install -r  /storage/emulated/0/zytk/zytk35_buspos.apk";
                String strCmd = "pm install -r " + strAppName;
                iRet = Publicfun.RunShellCmd(strCmd);
                if (iRet != 0) {
                    Log.d(TAG, "应用程序升级失败，重新下载升级");
                    String strCmdA = "rm -r " + ZYTK35Path + "*.apk";
                    RunShellCmd(strCmdA);
                    //将版本号置成本机版本号
                    WriteSofeVerInfoFile(SOFTWAREVER);
                    return 1;
                }
                //Publicfun.RunShellCmd("reboot");
                g_WorkInfo.cUpdateState = 0;
                return 0;
            } else {
                //pm install -i 包名 –user 0 apkpath
                //String strCmd="pm install -r  /storage/emulated/0/zytk/zyep.apk";
                String strCmd = "pm install -r " + strAppName;
                Log.i(TAG, strCmd);
                iRet = Publicfun.RunShellCmd(strCmd);
                //Publicfun.RunShellCmd("reboot");
                return 0;
            }
        } else {
            Log.e(TAG, "-------未找到升级应用程序:" + strAppName);
        }
        return 1;
    }

    //获取处理用户名字
    public static byte[] GetUserName(byte[] cAccName) {
        byte NameState = 0, n = 0;
        byte[] accName = new byte[16];
        memcpy(accName, cAccName, 16);
        if (g_LocalInfo.cAccNameShowMode == 0) {
            //字母或者数字
            if ((accName[0] >= 48 && accName[0] <= 57) || (accName[0] >= 65 && accName[0] <= 90) || (accName[0] >= 97 && accName[0] <= 122)) {
                NameState = 1;
            }
            if ((accName[1] >= 48 && accName[1] <= 57) || (accName[1] >= 65 && accName[1] <= 90) || (accName[1] >= 97 && accName[1] <= 122)) {
                NameState |= 2;
            }
            if ((accName[2] >= 48 && accName[2] <= 57) || (accName[2] >= 65 && accName[2] <= 90) || (accName[2] >= 97 && accName[2] <= 122)) {
                NameState |= 4;
            }
            if (NameState == 1) {
                n = 1;
                System.arraycopy(accName, 3, accName, 4, 11);
                accName[n++] = 0x20;
                accName[n++] = 0x2A;
                accName[n++] = 0x20;
            } else if ((NameState == 3) || (NameState > 4)) {
                accName[1] = 0x2A;
            } else if (NameState == 4) {
                accName[2] = 0x2A;
            } else {
                n = 2;
                System.arraycopy(accName, 4, accName, 5, 10);
                accName[n++] = 0x20;
                accName[n++] = 0x2A;
                accName[n++] = 0x20;
            }
        }
        return accName;
    }

    //判断日志文件夹大小，并删除日志(保存30天日志)
    public static void CheckAndDelFile(int iLogCnt, String strPath) {
        List<String> fileNames = new ArrayList<>();
        File[] files = new File(strPath).listFiles();
        if (files != null) {
            if (files.length > iLogCnt) {
                for (File file : files) {
                    fileNames.add(file.getName());
                }
                //Collections.reverse(fileNames);
                List<String> fileNamesOut = StrSort(fileNames);
                for (int i = 0; i < fileNamesOut.size() - iLogCnt; i++) {
                    File file = new File(strPath + fileNamesOut.get(i));
                    if (file.exists() && file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }

    //判断日志文件夹大小，并删除日志(保存30天日志)
    public static void CheckAndDelPicFile(int iPicCnt, String strPath) {
        List<String> fileNames = new ArrayList<>();
        File[] files = new File(strPath).listFiles();
        if (files != null) {
            if (files.length > iPicCnt) {
                for (File file : files) {
                    String strFileNameAll = file.getName();
                    String[] strArr = strFileNameAll.split("\\.");
                    String[] strArrA = strArr[0].split("\\_");
                    fileNames.add(strArrA[0] + strArrA[1]);
                }
                //Collections.reverse(fileNames);
                List<String> fileNamesOut = StrSort(fileNames);
                for (int i = 0; i < fileNamesOut.size() - iPicCnt; i++) {
                    String strTemp = fileNamesOut.get(i);
                    String strFileName = strTemp.substring(0, 12) + "_" +
                            strTemp.substring(12, strTemp.length()) + ".jpg";
                    File file = new File(strPath + strFileName);
                    if (file.exists() && file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }

    public static List<String> StrSort(List<String> filelist) {
        List<String> filelistout = new ArrayList<>();

        //外层循环控制比较的次数
        for (int i = 0; i < filelist.size() - 1; i++) {
            //内层循环控制到达位置
            for (int j = 0; j < filelist.size() - i - 1; j++) {
                //前面的元素比后面大就交换
                if (Long.parseLong(filelist.get(j).substring(0, 12)) > Long.parseLong(filelist.get(j + 1).substring(0, 12))) {
                    String strTemp = filelist.get(j);
                    filelist.set(j, filelist.get(j + 1));
                    filelist.set(j + 1, strTemp);
                }
            }
        }
        return filelist;
    }

    public static void sort(int[] a) {
        //外层循环控制比较的次数
        for (int i = 0; i < a.length - 1; i++) {
            //内层循环控制到达位置
            for (int j = 0; j < a.length - i - 1; j++) {
                //前面的元素比后面大就交换
                if (a[j] > a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
    }

//    public static String ByteToString(byte[] bytes)
//    {
//
//        StringBuilder strBuilder = new StringBuilder();
//        for (int i = 0; i <bytes.length ; i++) {
//            if (bytes[i]!=0){
//                strBuilder.append((char)bytes[i]);
//            }else {
//                break;
//            }
//        }
//        return strBuilder.toString();
//    }

    public static String ByteToString(byte[] bytes) {
        int i = 0;
        int iLen = 0;
        for (i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0x00) {
                iLen = i;
                break;
            }
        }
        iLen = i;
        String strTemp = new String(bytes, 0, iLen);
        return strTemp;
    }

    public static String ByteToString(byte[] bytes, String charsetName) throws UnsupportedEncodingException {
        int i = 0;
        int iLen = 0;
        for (i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0x00) {
                break;
            }
        }
        iLen = i;
        String strTemp = new String(bytes, 0, iLen, charsetName);
        return strTemp;
    }

    public static ShopQRCodeInfo SetShopQRCodeInfo(byte cType) {
        ShopQRCodeInfo pShopQRCodeInfo = new ShopQRCodeInfo();
        pShopQRCodeInfo.cVersion = 1;    //版本号	数字，从1开始
        pShopQRCodeInfo.cType = cType;       //类型
        pShopQRCodeInfo.cAgentID = (g_SystemInfo.cAgentID & 0x0000ff);                 //代理号
        pShopQRCodeInfo.iGuestID = (g_SystemInfo.iGuestID & 0x0000ffff);                //客户号
        pShopQRCodeInfo.wShopUserID = g_LocalInfo.wShopUserID;                //本机商户号

        if (cType == 3) {
            if (g_LocalInfo.cInputMode == 3) {
                g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
            }
            Log.d(TAG, String.format("交易金额:%d", g_WorkInfo.lngPaymentMoney));
            pShopQRCodeInfo.lngPayMoney = g_WorkInfo.lngPaymentMoney;            //交易金额	单位分
        } else {
            pShopQRCodeInfo.lngPayMoney = 0;
        }

        pShopQRCodeInfo.cDeviceID[0] = (byte) (g_StationInfo.iStationID & 0x000000ff);
        pShopQRCodeInfo.cDeviceID[1] = (byte) ((g_StationInfo.iStationID & 0x0000ff00) >> 8);//设备编号--//站点号
        pShopQRCodeInfo.wTerminalID = 0x00;              //终端机号
        pShopQRCodeInfo.lngOrderNum = g_WorkInfo.lngOrderNum;//二维码订单号
        return pShopQRCodeInfo;
    }

    //解密二维码数据
    public static int QRCodeInfoDecrypt(String strQRDncrypData, QRCodeCardHInfo pCardHQRCodeInfo, byte[] GuestID) {
        int i, iLen;
        /*
        u8 cVersion;    //版本号	数字，从1开始
        u8 cType;       //类型	1 （帐户：1 ；商户：2 ；商户（带交易金额）：3；考勤 4 ；门禁 5）
        u8 cAgentID;                 //代理号
        u16 iGuestID; 				//客户号
        u32 lngAccCountID;          //账号
        u16 cAccName                        //姓名
        u16 wShopUserID; 		    	//本机商户号
        s32 lngPayMoney;            //交易金额	单位分

        u8 cDeviceID[4];             //设备编号--//站点号
        u8 wTerminalID;              //终端机号
        */

        byte[] desKey = QREncrypt.getKey(GuestID);
        byte[] qrData = QREncrypt.QR3DesDecrypt(desKey, strQRDncrypData);
        if (qrData == null) {
            Log.i(TAG, "DES解析错，无效二维码");
            return 2;
        }
        String qrString = null;
        try {
            qrString = new String(qrData, "GB2312").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] qrSeg = qrString.split(",");
        for (i = 0; i < qrSeg.length; i++) {
            Log.i(TAG, qrSeg[i]);
        }
        if (qrSeg.length < 10) {
            Log.i(TAG, "解析数组错，无效二维码");
            return 2;
        } else {
            //姓名
            try {
                pCardHQRCodeInfo.cVersion = (byte) Integer.parseInt(qrSeg[0]);
                pCardHQRCodeInfo.cType = (byte) Integer.parseInt(qrSeg[1]);
                pCardHQRCodeInfo.cAgentID = Integer.parseInt(qrSeg[2]);
                pCardHQRCodeInfo.iGuestID = Integer.parseInt(qrSeg[3]);
                pCardHQRCodeInfo.lngAccountID = Long.parseLong(qrSeg[4]);
                pCardHQRCodeInfo.cAccName = qrSeg[6].getBytes("GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e(TAG, "数据转换异常:" + e.getMessage());
            }
            String cShowTemp = null;
            try {
                pCardHQRCodeInfo.lngOrderNum = Long.parseLong(qrSeg[8]);//订单号
                cShowTemp = new String(pCardHQRCodeInfo.cAccName, "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e(TAG, "数据转换异常:" + e.getMessage());
            }
            Log.i(TAG, "姓名:" + cShowTemp);

            pCardHQRCodeInfo.cPerCode = qrSeg[7].getBytes();//人员编号
            pCardHQRCodeInfo.cRandom = qrSeg[9].getBytes(); //随机数

            Log.i(TAG, "版本号:" + pCardHQRCodeInfo.cVersion);
            Log.i(TAG, "类型:" + pCardHQRCodeInfo.cType);
            Log.i(TAG, "代理号:" + pCardHQRCodeInfo.cAgentID);
            Log.i(TAG, "客户号:" + pCardHQRCodeInfo.iGuestID);
            Log.i(TAG, "账号:" + pCardHQRCodeInfo.lngAccountID);
            Log.i(TAG, "姓名:" + pCardHQRCodeInfo.cAccName);
            Log.i(TAG, "人员编号:" + pCardHQRCodeInfo.cPerCode);
            Log.i(TAG, "订单号:" + pCardHQRCodeInfo.lngOrderNum);
            Log.i(TAG, "随机数:" + pCardHQRCodeInfo.cRandom);
        }
        return 0;
    }

    //加密二维码数据
    public static String QRCodeInfoEncryp(ShopQRCodeInfo pQRCodeInfo) {
        int i;
        int crc16 = 0;
        byte[] GuestID = new byte[4];

        String cDataTemp = "";
        String cEncrypInfo = "";

        /*
        u8 cVersion;    //版本号	数字，从1开始
        u8 cType;       //类型	1 （帐户：1 ；商户：2 ；商户（带交易金额）：3；考勤 4 ；门禁 5）
        u8 cAgentID;                 //代理号
        u16 iGuestID; 				//客户号
        u16 wShopUserID; 		    	//本机商户号
        s32 lngPayMoney;            //交易金额	单位分

        u8 cDeviceID[4];             //设备编号--//站点号
        u8 wTerminalID;              //终端机号
        */

        //根据客户号代理号作为3DES的加密因子进行加密
        GuestID[0] = (byte) pQRCodeInfo.cAgentID;
        GuestID[1] = (byte) (pQRCodeInfo.iGuestID & 0x00ff);
        GuestID[2] = (byte) ((pQRCodeInfo.iGuestID & 0xff00) >> 8);
        GuestID[3] = 0x00;

        Log.d(TAG, String.format("客户号:%02x.%02x.%02x.%02x", GuestID[0], GuestID[1], GuestID[2], GuestID[3]));
        byte[] cKey = QREncrypt.getKey(GuestID);
        if (pQRCodeInfo.cType == 2) {
            cDataTemp = String.format("%d,%d,%d,%d,%d,%d,%d",
                    pQRCodeInfo.cVersion,
                    pQRCodeInfo.cType,
                    pQRCodeInfo.cAgentID,
                    pQRCodeInfo.iGuestID,
                    pQRCodeInfo.wShopUserID,
                    ((pQRCodeInfo.cDeviceID[0] & 0xff) + (pQRCodeInfo.cDeviceID[1] & 0xff) * 256),
                    pQRCodeInfo.wTerminalID
            );
            Log.d(TAG, cDataTemp);
        } else if (pQRCodeInfo.cType == 3) {
            cDataTemp = String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d",
                    pQRCodeInfo.cVersion,
                    pQRCodeInfo.cType,
                    pQRCodeInfo.cAgentID,
                    pQRCodeInfo.iGuestID,
                    pQRCodeInfo.wShopUserID,
                    pQRCodeInfo.lngOrderNum,
                    pQRCodeInfo.lngPayMoney,
                    ((pQRCodeInfo.cDeviceID[0] & 0xff) + (pQRCodeInfo.cDeviceID[1] & 0xff) * 256),
                    pQRCodeInfo.wTerminalID
                    //站点号 467  金额 100 订单号 6706  商户号 1087
//                    cDataTemp=String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d",
//                            1,
//                            3,
//                            1,
//                            9999,
//                            1087,
//                            6706,
//                            100,
//                            467,
//                            0
            );
            Log.d(TAG, cDataTemp);
        }
        byte[] bEncrypData = cDataTemp.getBytes();
        cEncrypInfo = QREncrypt.QR3DesEncrypt(cKey, bEncrypData);
        //Log.d(TAG,"cEncrypInfo:"+cEncrypInfo);
        byte[] bDataTemp = cEncrypInfo.getBytes();
        crc16 = QREncrypt.crc16(bDataTemp, bDataTemp.length);
        String strEncrypInfo = String.format(cEncrypInfo + ",%d", crc16);
        //Log.d(TAG,"strEncrypInfo:"+strEncrypInfo);
        return strEncrypInfo;
    }

    //生成二维码
    public static Bitmap CreateQRBitmap(String content, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixles = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    //表示该点是否有数据
                    //如果有数据，则该点的颜色为黑色，否则该点的颜色为白色
                    boolean b = bitMatrix.get(i, j);
                    if (b) {
                        pixles[i * width + j] = 0x00000000;
                    } else {
                        pixles[i * width + j] = 0xffffffff;
                    }
                }
            }
            //1.像素数组，2.偏移量，3.水平方向摆放的像素个数，4.宽度，5.高度
            Bitmap bitmap = Bitmap.createBitmap(pixles, 0, width, width, height, Bitmap.Config.RGB_565);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取android属性
    public static String getProp(String propName) {
        Class<?> classType = null;
        String value = null;
        try {
            classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            value = (String) getMethod.invoke(classType, new Object[]{propName});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    //获取文件夹文件数
    public static int GetSDFileCnt(final File f) {
        int iFileCount = 0;
        UsbFile usbFile = null;
        if (f.isDirectory()) { //如果选择是个文件夹
            for (File sdFile : f.listFiles()) {
                if (sdFile.isDirectory())
                    //iFileCount += sdFile.list().length;
                    iFileCount += GetSDFileCnt(sdFile);
                else
                    iFileCount++;
            }
        }
        return iFileCount;
    }

    //获取在线人员账号
    public static byte[] GetOnlineAccNo(int iType, Object obj) {
        byte[] cTempNo = new byte[32];
        String strTempNo = "";
        //1-账号 2-物理卡号 3-卡内编号 4-个人编号 5-物理卡号 6-证件号 7-手机号 8-第三方号
        switch (iType) {
            case 1: //1-账号
                long lngAccountID = (long) obj;
                strTempNo = String.format("%d", lngAccountID);
                break;

        }
        cTempNo = strTempNo.getBytes();
        return cTempNo;
    }

//    iRelayState;  // 继电器 0:关 1:开
//    iRelayMode;  // 继电器模式 1:常开 0:常闭
//    iRelayOperTime;  // 继电器动作时长(ms)
//    iRelayOperCnt;  // 动作脉冲数
    public static  void RelayControlDeal(int iRelayState,int iRelayMode,int iRelayOperTime,int iRelayOperCnt)
    {
        int i=0;
        int control2 = iRelayMode;

        if (iRelayState == 0)
            return;

        //判断继电器配置类型去控制
        if (iRelayMode == 0)// 继电器模式 1:常开 0:常闭
        {
            if (control2 == 1) {
                control2 = 0;
            } else {
                control2 = 1;
            }
        } else {
            if (control2 == 0) {
                control2 = 1;
            } else {
                control2 = 0;
            }
        }

        if(control2 == 1)
        {
            //开启继电器
            for(i=0;i<iRelayOperCnt;i++){
                Publicfun.RunShellCmd("echo 1 > /sys/class/relay/control");
                try {
                    Thread.sleep(iRelayOperTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Publicfun.RunShellCmd("echo 0 > /sys/class/relay/control");
                try {
                    Thread.sleep(iRelayOperTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(control2 == 0)
        {
            //关闭继电器
            for(i=0;i<iRelayOperCnt;i++){
                Publicfun.RunShellCmd("echo 0 > /sys/class/relay/control");
                try {
                    Thread.sleep(iRelayOperTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Publicfun.RunShellCmd("echo 1 > /sys/class/relay/control");
                try {
                    Thread.sleep(iRelayOperTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
//        if(control == 1)
//        {
//            g_WorkInfo.lngRelayTime = System.currentTimeMillis();
//        }
//        else {
//            g_WorkInfo.lngRelayTime = 0;
//        }
    }

//    iRelayState;  // 继电器 0:关 1:开
//    iRelayMode;  // 继电器模式 1:常开 0:常闭
//    iRelayOperTime;  // 继电器动作时长(ms)
//    iRelayOperCnt;  // 动作脉冲数
    public static  void RelayControl()
    {
        if(g_LocalInfo.iRelayState==1)
            RelaySendHandler.sendEmptyMessage(EVT_RelayControl);
    }

    //设备休眠唤醒模式(新的系统才能关闭大屏数据)
    public static void SendPowSaveCmd(int iType) {
        if (iType == 1) {
            Log.e(TAG, "进入休眠模式");
            Publicfun.RunShellCmd("echo off > /sys/devices/platform/display-subsystem/drm/card0/card0-DSI-1/status");//lcd数据开关
            Publicfun.RunShellCmd("echo 0 > /sys/class/backlight/backlight/brightness");//lcd背光
        } else {
            Log.e(TAG, "退出休眠模式，唤醒");
            Publicfun.RunShellCmd("echo on > /sys/devices/platform/display-subsystem/drm/card0/card0-DSI-1/status");
            Publicfun.RunShellCmd("echo 1 > /sys/class/backlight/backlight/brightness");
        }
    }
}

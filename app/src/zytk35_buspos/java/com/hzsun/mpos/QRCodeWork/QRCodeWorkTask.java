package com.hzsun.mpos.QRCodeWork;

import android.util.Log;

import com.hzsun.mpos.Algorithm.QREncrypt;
import com.hzsun.mpos.FaceApp.FaceWorkTask;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.SerialWork.SerialWorkTask;
import com.hzsun.mpos.data.QRCodeCardHInfo;

import static com.hzsun.mpos.Global.Global.DISABLE_QRCODE;
import static com.hzsun.mpos.Global.Global.INVALID_QRCODE;
import static com.hzsun.mpos.Global.Global.LAN_EP_MONEYPOS;
import static com.hzsun.mpos.Global.Global.g_ThirdQRCodeInfo;
import static com.hzsun.mpos.Global.Global.g_CardHQRCodeInfo;
import static com.hzsun.mpos.Global.Global.g_CardInfo;
import static com.hzsun.mpos.Global.Global.g_CommInfo;
import static com.hzsun.mpos.Global.Global.g_LastOrderInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.PowerSaveDeal;
import static com.hzsun.mpos.Public.Publicfun.PrintArray;
import static com.hzsun.mpos.Public.Publicfun.ShowCardPaying;
import static com.hzsun.mpos.Public.Utility.memcmp;
import static com.hzsun.mpos.Public.Utility.memcpy;
import static com.hzsun.mpos.Sound.SoundPlay.VoicePlay;
import static java.util.Arrays.fill;

public class QRCodeWorkTask {

    private static final String TAG = "QRCodeWorkTask";
    private QRCodeWorkThread ThreadQRCodeWork;      //串口接收数据
    private ScanQRDeviceThread ThreadScanQRDevice;

    private static byte[] s_QRCodeInfoTemp = new byte[1024];
    private static int s_QRCodeSameCnt = 0;

    public QRCodeWorkTask() {
        Log.d(TAG, "QRCodeWorkTask: 构造");
    }

    public void Init() {
        Log.d(TAG, "QRCodeWorkTask 初始化");
        ThreadQRCodeWork = new QRCodeWorkThread();
        ThreadQRCodeWork.start();

        ThreadScanQRDevice = new ScanQRDeviceThread();
        ThreadScanQRDevice.start();
    }

    //扫码设备扫描
    class ScanQRDeviceThread extends Thread {
        private boolean isStart = true;

        @Override
        public void run() {
            super.run();

            while (isStart) {
                PowerSaveScan();
                ScanQRDevice();
                try {
                    Thread.sleep(100);//延时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //检测QR扫码设备是否存在
    public void ScanQRDevice() {
        if (g_WorkInfo.cQRDevStatus != 1) {
            if (QRScanHelper.hasUSBQRSCanDevice()) {
                //Log.d(TAG, "==QR内置USB模块存在==");
                g_WorkInfo.cQRDevStatus = 2;
            } else
                g_WorkInfo.cQRDevStatus = 0;
        }
    }

    //判断进入省电模式
    public void PowerSaveScan(){
        //按键 触摸 刷卡 刷码 刷脸
        if((g_WorkInfo.cBackActivityState==0)&&(g_LocalInfo.cPowerSaveTimeA!=0)){
            if((System.currentTimeMillis()-g_WorkInfo.lngPowerSaveCnt) > g_LocalInfo.cPowerSaveTimeA *60*1000){
                g_WorkInfo.lngPowerSaveCnt=System.currentTimeMillis();
                //在升级程序和U盘导数据时不进入省电模式
                if((g_WorkInfo.cUpdateState != 0)||(g_WorkInfo.cUDiskState!=0)){
                    Log.d(TAG,"在升级程序和U盘导数据时不进入省电模式");
                    return;
                }
                PowerSaveDeal(0);
            }
        }
    }

    //获取订单号
    public void GetOrderNum() {

        if (g_StationInfo.iStationClass == LAN_EP_MONEYPOS) {
            return;
        }
        //判断进入二维码扫描交互界面
        if ((g_WorkInfo.cCardEnableFlag == 0) || (g_WorkInfo.cInUserPWDFlag == 1)) {
            return;
        }
        //注意：防止卡片扫描线程与网络写卡线程冲突(如果冲突将读写失败)做好线程互斥
        if ((g_CommInfo.cGetQRCodeInfoStatus == 1) && (g_WorkInfo.cRunState == 1)) {
            return;
        }

        if ((g_WorkInfo.cSelfPressOk == 1)
                || ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0) && (g_WorkInfo.cPayDisPlayStatus ==0)))//等显示成功后在获取订单号
        {
            //二维码显示只有联机并且在营业分组内才能使用
            if ((g_WorkInfo.cRunState == 1)&&((g_WorkInfo.cBusinessState == 0)&&(g_WorkInfo.cBusinessID!=0)) ) {
                if (g_WorkInfo.cQrCodestatus == 0) {
                    Log.d(TAG, "发送获取订单号");
                    if (g_LocalInfo.cInputMode == 3) {
                        g_WorkInfo.lngPaymentMoney = g_WorkInfo.wBookMoney;
                    }
                    g_CommInfo.lngSendComStatus |= 0x00001000;
                    g_WorkInfo.cQrCodestatus = 1;
                }
            }
        }
    }

    //QRCode 业务线程
    class QRCodeWorkThread extends Thread {
        private boolean isStart = true;

        @Override
        public void run() {
            super.run();

            while (isStart) {
                GetOrderNum();
                QRCodeScan();
                try {
                    Thread.sleep(100);//延时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    int iRecvLen = 0;
    byte[] RecvData = new byte[4096];
    byte[] RecvQRCode = new byte[4096];
    byte[] RecvQRCodeTemp = new byte[4096];

    //QRCode 扫描
    public void QRCodeScan() {
        if ((g_StationInfo.iStationClass == LAN_EP_MONEYPOS) ||
                (g_WorkInfo.cInDispelCardFlag != 0) ||
                (g_WorkInfo.cPayDisPlayStatus != 0) ||
                (g_WorkInfo.cBackActivityState != 0) ||
                ((g_CardInfo.cExistState == 1) && (g_SystemInfo.cOnlyOnlineMode == 0))) //1.输入金额按下确定键后启用 2.有卡片存在不启用
        {
            return;
        }

        //Log.d(TAG,"=======二维码扫描处理=========");
        //判断进入二维码扫描交互界面
        if ((g_WorkInfo.cCardEnableFlag == 0) || (g_WorkInfo.cInUserPWDFlag == 1)) {
            return;
        }
        //注意：防止卡片扫描线程与网络写卡线程冲突(如果冲突将读写失败)做好线程互斥
        if ((g_CommInfo.cGetQRCodeInfoStatus == 1) && (g_WorkInfo.cRunState == 1)) {
            return;
        }

        if ((g_WorkInfo.cSelfPressOk == 1)
                || ((g_LocalInfo.cInputMode == 3) && (g_LocalInfo.cBookSureMode == 0))
                || (g_SystemInfo.cOnlyOnlineMode == 1)) {

            iRecvLen = 0;
            if (g_WorkInfo.cQRDevStatus == 1) {
                iRecvLen = g_Nlib.QR_ScanQRCode(RecvData);//QR内置模块存在
            } else if (g_WorkInfo.cQRDevStatus == 2) {
                iRecvLen = QRScanHelper.startReadQRCode(RecvData);//QR内置USB模块
            }
            if (iRecvLen > 0) {
                g_WorkInfo.lngPowerSaveCnt = System.currentTimeMillis();
                handleQRCode();
            }
        }
    }

    //QRCode 扫描
    private void handleQRCode() {
        if ((g_WorkInfo.cScanQRCodeFlag == 0) && (g_WorkInfo.cRunState == 1)) {
            Log.d(TAG, "cScanQRCodeFlag=0 退出");
            iRecvLen = 0;
            fill(RecvQRCode, (byte) 0);
            fill(RecvData, (byte) 0);
            return;
        }
        if (iRecvLen > 512) {
            iRecvLen = 0;
            fill(RecvQRCode, (byte) 0);
            fill(RecvData, (byte) 0);
            return;
        }
        if (g_WorkInfo.cBackActivityState != 0)
            return;
        memcpy(RecvQRCodeTemp, RecvData, iRecvLen);
        //判断非法字符串
        {
            //无物理移动返回：$380010-F50C，有物理移动返回：$380011-C63D
            String str1 = "$380010-F50C";
            String str2 = "$380011-C63D";
            String strcQRCodeInfo1 = new String(RecvQRCodeTemp, 0, 12);
            if (strcQRCodeInfo1.equals(str1)) {
                Log.e(TAG, "=================QR接收到非法字符:%s=================");
                return;
            }
            String strcQRCodeInfo2 = new String(RecvQRCodeTemp, 0, 12);
            if (strcQRCodeInfo2.equals(str2)) {
                Log.e(TAG, "=================QR接收到非法字符:%s=================");
                return;
            }
        }
        //判断支付宝字符串重复接收
        if (memcmp(RecvQRCodeTemp, 0, RecvQRCodeTemp, (iRecvLen / 2), iRecvLen / 2) == 0) {
            PrintArray("QR第三方字符串重复接收:", RecvQRCodeTemp, iRecvLen);
            memcpy(RecvQRCode, RecvQRCodeTemp, iRecvLen / 2);
            RecvQRCodeInfo(RecvQRCode, iRecvLen / 2);
            return;
        } else {
            PrintArray("获取到的QRCode:", RecvQRCodeTemp, iRecvLen);
            memcpy(RecvQRCode, RecvQRCodeTemp, iRecvLen);
            RecvQRCodeInfo(RecvQRCode, iRecvLen);
            return;
        }
    }

    //接收到 QRCode 信息
    public static void RecvQRCodeInfo(byte[] cQRCodeInfo, int iRecvLen) {
        int iResult;
        byte[] cQRCodeInfoTemp = new byte[1024];

        Log.d(TAG, "=======接收到 串口QRCode 信息============");
        if ((g_WorkInfo.cRunState != 1) || (g_WorkInfo.cQrCodestatus != 2)) {
            //网络故障不支持扫码
            Publicfun.ShowCardErrorInfo(DISABLE_QRCODE);
            return;
        }
        if (iRecvLen != 0) {
            //判断是否是重码
            memcpy(cQRCodeInfoTemp, cQRCodeInfo, iRecvLen);
            if (memcmp(s_QRCodeInfoTemp, cQRCodeInfoTemp, iRecvLen) == 0) {
                Log.e(TAG, "========相同码，退出==========");
                s_QRCodeSameCnt++;
                if (s_QRCodeSameCnt > 3) {
                    s_QRCodeSameCnt = 0;
                    Log.d(TAG, String.format("无效二维码:%s", cQRCodeInfoTemp));
                    VoicePlay("invalid_qrcode");
                }
                return;
            }
            memcpy(s_QRCodeInfoTemp, cQRCodeInfoTemp, iRecvLen);
            //接收到有效二维码信息
            g_CardHQRCodeInfo = new QRCodeCardHInfo();
            iResult = TreatQRRecvData(cQRCodeInfoTemp, g_CardHQRCodeInfo);
            if (g_LocalInfo.cDockposFlag != 1
                    || (g_LocalInfo.cDockposFlag == 1 && SerialWorkTask.getTradeState() == SerialWorkTask.STATE_PAYING)) {
                if (iResult == 0) {
                    ShowCardPaying();
                    s_QRCodeSameCnt = 0;
                    g_WorkInfo.cOtherQRFlag = 0;  //自主QR
                    g_WorkInfo.cScanQRCodeFlag = 0;
                    g_CommInfo.cGetQRCodeInfoStatus = 1;
                    g_CommInfo.lngSendComStatus |= 0x00000800;

                    //解析数据成功,判断二维码适用于本机,发送二维码校验报文
                    Log.d(TAG, "接收到正元二维码数据");
                    g_Nlib.QR_SetDeviceReadEnable(2);  //结束识读
                    if (g_SystemInfo.cFaceDetectFlag == 1) {
                        FaceWorkTask.StartDetecte(false);//关闭人脸
                    }
                } else {
                    if (iRecvLen > 10) {
                        ShowCardPaying();
                        if (Publicfun.CheckAliPayAuthcode(cQRCodeInfoTemp, iRecvLen) == 0) {
                            Log.d(TAG, "支付宝支付码");
                        }
                        //正在处理中
                        g_WorkInfo.cOtherQRFlag = 1;  //第三方QR
                        g_ThirdQRCodeInfo.iSignLen = 0;
                        g_ThirdQRCodeInfo.iLen = iRecvLen;
                        g_ThirdQRCodeInfo.cType = 1;
                        g_LastOrderInfo.cType = 1;
                        memcpy(g_ThirdQRCodeInfo.cQRCodeInfo, cQRCodeInfoTemp, iRecvLen);
                        g_CommInfo.lngSendComStatus |= 0x00002000;
                        g_WorkInfo.cScanQRCodeFlag = 0;
                        g_CommInfo.cGetQRCodeInfoStatus = 1;

                        PrintArray("接收到的第三方二维码数据", g_ThirdQRCodeInfo.cQRCodeInfo, g_ThirdQRCodeInfo.iLen);
                        g_Nlib.QR_SetDeviceReadEnable(2);  //结束识读
                        if (g_SystemInfo.cFaceDetectFlag == 1) {
                            FaceWorkTask.StartDetecte(false);//关闭人脸
                        }
                    } else {
                        //无效二维码
                        Log.d(TAG, String.format("无效二维码:%s", cQRCodeInfoTemp));
                        Publicfun.ShowCardErrorInfo(INVALID_QRCODE);
                    }
                }
            } else if (g_LocalInfo.cDockposFlag == 1 && SerialWorkTask.getTradeState() == SerialWorkTask.STATE_QUERYING) {
                SerialWorkTask.onQueryDone(0);
            }
        }
    }

    //接收到有效二维码信息
    /*
    版本号	数字，从1开始
    类型	1 （帐户：1 ；商户：2 ；商户（带交易金额）：3；考勤 4 ；门禁 5）
    代理号
    客户号
    帐号
    姓名	Utf-8 ,X2格式显示
    个人编号
    随机数
    订单号
    校验码
     */
    private static int TreatQRRecvData(byte[] cQRCodeInfo, QRCodeCardHInfo pCardHQRCodeInfo) {
        byte[] cTempData = new byte[256];
        byte[] cGuestID = new byte[4];

        g_CommInfo.cRecvWaitState = 0;
        //对二维码串进行CRC16校验(逗号前的字符串进行校验)
        String strcQRCodeInfo = new String(cQRCodeInfo).trim();
        //Log.d(TAG,"二维码内容 = " + strcQRCodeInfo);
        String[] strings = strcQRCodeInfo.split(",");
        if (strings.length != 2) {
            Log.d(TAG, "无效二维码");
            return 1;
        }
        int crc = QREncrypt.crc16(strings[0].getBytes(), strings[0].length());
        try {
            if (crc != Integer.parseInt(strings[1])) {
                Log.d(TAG, "无效二维码-CRC err");
                return 1;
            } else {

                cGuestID[0] = (byte) g_SystemInfo.cAgentID;
                cGuestID[1] = (byte) (g_SystemInfo.iGuestID & 0x00ff);
                cGuestID[2] = (byte) ((g_SystemInfo.iGuestID & 0xff00) >> 8);
                cGuestID[3] = 0x00;

                Log.d(TAG, String.format("本地客户号:%02x.%02x.%02x.%20x", cGuestID[0], cGuestID[1], cGuestID[2], cGuestID[3]));
                //通过本机的客户代理号进行3DES解密
                return Publicfun.QRCodeInfoDecrypt(strings[0], pCardHQRCodeInfo, cGuestID);
            }
        } catch (Exception e) {
            Log.d(TAG, "无效二维码-CRC 解析错误");
            return 1;
        }
    }
}

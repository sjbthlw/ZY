package com.hzsun.mpos.NetWork;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.hzsun.mpos.CardWork.CardBasicParaInfo;
import com.hzsun.mpos.CardWork.SubsidyInfo;
import com.hzsun.mpos.Public.DataTransfer;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Public.WifiUtil;
import com.hzsun.mpos.SerialWork.SerialWorkTask;
import com.hzsun.mpos.data.BWListInfoRW;
import com.hzsun.mpos.data.BasicInfoRW;
import com.hzsun.mpos.data.BuinessInfoRW;
import com.hzsun.mpos.data.BurseInfoRW;
import com.hzsun.mpos.data.FTPInfoRW;
import com.hzsun.mpos.data.LastRecordPayInfo;
import com.hzsun.mpos.data.OddKeyInfoRW;
import com.hzsun.mpos.data.RecordInfoRW;
import com.hzsun.mpos.data.StationInfoRW;
import com.hzsun.mpos.data.StatusBurInfoRW;
import com.hzsun.mpos.data.StatusPriInfoRW;
import com.hzsun.mpos.data.SystemInfoRW;
import com.hzsun.mpos.data.WasteBooksRW;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.hzsun.mpos.CardWork.CardWorkTask.NetCheckCardInfo;
import static com.hzsun.mpos.CardWork.CardWorkTask.NetGetQRCodeTradingInfo;
import static com.hzsun.mpos.CardWork.CardWorkTask.NetGetQROrderNum;
import static com.hzsun.mpos.CardWork.CardWorkTask.NetGetQROrderResult;
import static com.hzsun.mpos.CardWork.CardWorkTask.NetGetThirdCodeTradingInfo;
import static com.hzsun.mpos.CardWork.CardWorkTask.OnlingLastRecDispel;
import static com.hzsun.mpos.CardWork.CardWorkTask.OnlingWithholdTrading;
import static com.hzsun.mpos.Global.Global.BUSINESS_TIME;
import static com.hzsun.mpos.Global.Global.COMMUCATION_TIMEOUT;
import static com.hzsun.mpos.Global.Global.CONNECTOK;
import static com.hzsun.mpos.Global.Global.DISPELFAIL;
import static com.hzsun.mpos.Global.Global.DOWNPARAOVER;
import static com.hzsun.mpos.Global.Global.HASDISPEL;
import static com.hzsun.mpos.Global.Global.LAN_DEVTYPE;
import static com.hzsun.mpos.Global.Global.LAN_EP_MONEYPOS;
import static com.hzsun.mpos.Global.Global.MEMORY_FAIL;
import static com.hzsun.mpos.Global.Global.PAYMENTRECORD_LEN;
import static com.hzsun.mpos.Global.Global.SETERR_MSG;
import static com.hzsun.mpos.Global.Global.SOFTWAREVER;
import static com.hzsun.mpos.Global.Global.UPRECORDSUM;
import static com.hzsun.mpos.Global.Global.gHttpCommInfo;
import static com.hzsun.mpos.Global.Global.gUIMainHandler;
import static com.hzsun.mpos.Global.Global.g_ThirdCodeResultInfo;
import static com.hzsun.mpos.Global.Global.g_ThirdQRCodeInfo;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_BlackWList;
import static com.hzsun.mpos.Global.Global.g_BuinessInfo;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardHQRCodeInfo;
import static com.hzsun.mpos.Global.Global.g_CardInfo;
import static com.hzsun.mpos.Global.Global.g_CommInfo;
import static com.hzsun.mpos.Global.Global.g_EP_BurseInfo;
import static com.hzsun.mpos.Global.Global.g_FTPInfo;
import static com.hzsun.mpos.Global.Global.g_FacePayInfo;
import static com.hzsun.mpos.Global.Global.g_LastOrderInfo;
import static com.hzsun.mpos.Global.Global.g_LastRecordPayInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_LocalNetStrInfo;
import static com.hzsun.mpos.Global.Global.g_OddKeyInfo;
import static com.hzsun.mpos.Global.Global.g_OnlinePayInfo;
import static com.hzsun.mpos.Global.Global.g_RecordInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_StatusInfoArray;
import static com.hzsun.mpos.Global.Global.g_StatusPriInfoArray;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_VersionInfo;
import static com.hzsun.mpos.Global.Global.g_WasteBookInfo;
import static com.hzsun.mpos.Global.Global.g_WifiParaInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Global.Global.s_NetEventMsg;
import static com.hzsun.mpos.MyApplication.gWifiManager;
import static com.hzsun.mpos.Public.Publicfun.ByteToString;
import static com.hzsun.mpos.Public.Publicfun.ControlBusiness;
import static com.hzsun.mpos.Public.Publicfun.PrintArray;
import static com.hzsun.mpos.Public.Utility.memcpy;

public class NetWorkTask {

    private static final String TAG = "NetWorkTask";
    private static final int OK = 0;

    // 主线程Handler 用于将从服务器获取的消息显示出来
    public Handler SendHandler;
    // Socket变量
    private Socket socket = null;
    // 输入流对象
    private InputStream inputStream;
    // 输入流读取器对象
    private InputStreamReader isr;
    private BufferedReader br;
    // 输出流对象
    private OutputStream outputStream;
    //提示信息
    private String s_cTipsInfo = "";

    private static final int EVT_NULL = 0;
    private static final int EVT_CONNECT = 1;                //连接接入服务器
    private static final int EVT_DEVICEJOIN = 2;             //设备接入
    private static final int EVT_SIGNIN = 3;                 //设备签到
    private static final int EVT_SIGNOUT = 4;                //设备签退
    private static final int EVT_ONGOING = 5;                    //网络系统运行中
    private static final int EVT_CARDCHECK = 6;                    //卡户校验
    private static final int EVT_QROrderNum = 7;//二维码订单号信息
    private static final int EVT_QROrderResult = 8;//二维码订单结果信息
    private static final int EVT_QRTradeInfo = 9;//正元二维码交易结果信息
    private static final int EVT_QRThirdTradeInfo = 10;//第三方二维码交易结果信息
    private static final int EVT_ONLRDispel = 11;       //在线冲正
    private static final int EVT_Withhold = 12;       //第三方代扣

    private static final int EVT_CardCheckOver = 36;     //卡户校验超时
    private static final int EVT_QRTradeInfoOver = 39;//正元二维码交易结果信息超时
    private static final int EVT_QRALITradeInfoOver = 40;//第三方二维码交易结果信息超时
    private static final int EVT_ONLRDispelOver = 41;       //在线冲正超时
    private static final int EVT_WithholdOver = 42;       //第三方代扣超时

    private static final int EVT_NETTIMEOUT = 90; //网络超时错误
    private static final int EVT_DISCONNECT = 100; //网络断开


    private static final int COMTIMEOUT = 5;  //通讯连接服务器超时时间(秒)
    private static final int MSGTIMEOUT = 5;  //通讯发送报文超时时间(秒)

    public static final int RECONNECTCOUNT = 10;               //网络重连间隔时间
    public static final int NODATARECVTIME = 35;             //未收到服务器数据时长
    private static final int MSGTIMEOUTCOUNT = 3;  //通讯发送报文超时次数

    private byte[] s_cSendDataBuf;            //发送的具体报文内容
    private byte[] s_cRecvDataBuf;            //收到的具体报文内容
    private byte[] s_cRecvDataBuf_N;            //收到的具体报文内容
    private byte[] s_cRecvDataBuf_P;            //收到的具体报文内容
    private byte[] bContextData;
    private int s_wProtocolID;

    private int s_lngRecvBufferLen;            //报文的全部长度
    private int s_lngRecvDataLen;            //接收到的数据的长度(主动发送返回数据长度)

    private boolean isOnline = false;

    private RecvDataThread ThreadRecvData;      //网络接收数据
    private UserDataThread ThreadUserData;      //连接服务器接入签到
    private HandleRecvThread ThreadHandleRecv;  //处理接收数据
    private ScanNetWorkThread ThreadScanNetWork;//处理重连和上次流水

    private Timer NetOverTimer;
    public TimerTask NetOverTimetask;

    private byte[] cStatusState;
    private byte[] cStatusPriState;
    private byte[] cBusinessState;
    private long s_lngInitBWListSum;
    private int s_iChangeBWListSum;
    private long s_lngRecvRecordID;
    private long s_lngStartRecordID;
    private byte s_cReflashPareState;

    public NetWorkTask() {
        Log.i(TAG, "NetWorkTask: 构造");
    }

    public void Init() {
        Log.i(TAG, "NetWorkTask 初始化");
        cStatusState = new byte[128];
        cStatusPriState = new byte[128];
        cBusinessState = new byte[128];
        s_lngInitBWListSum = 0;
        s_iChangeBWListSum = 0;
        s_lngRecvRecordID = 0;
        s_lngStartRecordID = 0;
        s_cReflashPareState = 0;

        s_cSendDataBuf = new byte[1024 * 4];
        s_cRecvDataBuf = new byte[1024 * 4];
        s_cRecvDataBuf_N = new byte[1024 * 4];
        s_cRecvDataBuf_P = new byte[1024 * 4];
        bContextData = new byte[1024];
        s_lngRecvBufferLen = 0;
        s_lngRecvDataLen = 0;

        g_WorkInfo.cRunState = 5; //重新连接统一调度服务
        g_WorkInfo.lngOffLineTime = g_WorkInfo.lngOSTime;
        g_WorkInfo.lngOnLineTime = g_WorkInfo.lngOSTime;
        g_WorkInfo.lngOSTime = g_WorkInfo.lngOnLineTime + RECONNECTCOUNT * 10;

        g_WorkInfo.iBusinessCount = BUSINESS_TIME + 1;
        ControlBusiness();
        //TCP连接服务器
        s_NetEventMsg.type = EVT_NULL;
        s_NetEventMsg.cConnectState = 0;
        s_NetEventMsg.cMsgResState = 0;
        s_NetEventMsg.lngNetTimeout = 0;
        s_NetEventMsg.wMsgTimeoutCount = 0;
        isOnline = false;

        ThreadUserData = new UserDataThread();
        ThreadRecvData = new RecvDataThread();
        ThreadHandleRecv = new HandleRecvThread();
        ThreadScanNetWork = new ScanNetWorkThread();

        NetOverTimer = new Timer();
        NetOverTimetask = new TimerTask() {
            @Override
            public void run() {

                NetTimeOverProcess();
                ControlBusiness();
                if (gUIMainHandler != null) {

                    if (ThreadUserData.GetStatus() == false)
                        ThreadUserData.start();

                    if (ThreadRecvData.GetStatus() == false)
                        ThreadRecvData.start();

                    if (ThreadHandleRecv.GetStatus() == false)
                        ThreadHandleRecv.start();

                    if (ThreadScanNetWork.GetStatus() == false)
                        ThreadScanNetWork.start();
                }
            }
        };
        // 参数：1000，延时1秒后执行。 100，每隔0.1秒执行1次task。
        NetOverTimer.schedule(NetOverTimetask, 100, 100);
    }

    //连接服务器
    private void ConnectServer(String strServerIP, int iServerPort, byte mode) {
        if (g_WorkInfo.cNetlinkStatus == 0) {
            //判断是否有WIFI
            if ((g_WifiParaInfo.cInfoState == 1)
                    && (!g_WifiParaInfo.strUserName.equals(""))) {
                WifiUtil.WifiConnect(gWifiManager, g_WifiParaInfo.strUserName, g_WifiParaInfo.strPassword);
            }
            if (g_WorkInfo.cRunState != 2)
                g_WorkInfo.cRunState = 5; //重新连接统一调度服务
            g_WorkInfo.lngOnLineTime = g_WorkInfo.lngOSTime;
            return;
        }
        s_NetEventMsg.cConnectState = 0;
        s_NetEventMsg.cMsgResState = 0;
        s_NetEventMsg.lngNetTimeout = 0;
        s_NetEventMsg.wMsgTimeoutCount = 0;

        try {
            if (socket != null) {
                Log.e(TAG, "socket 不为null，关闭socket");
                isOnline = false;
                inputStream.close();
                outputStream.close();
                if (socket != null)
                    socket.close();
                socket = null;
                return;
            }

            if (socket == null || socket.isClosed()) {
                socket = null;
                s_NetEventMsg.type = EVT_CONNECT;
                Log.d(TAG, "连接服务器 " + "strServerIP:" + strServerIP + " iServerPort:" + iServerPort);
                socket = new Socket(strServerIP, iServerPort);
            }
            socket.setTcpNoDelay(true);
            outputStream = socket.getOutputStream();//从Socket 获得输出流对象OutputStream
            inputStream = socket.getInputStream();//从Socket 获得输出流对象inputStream

            if ((socket.isConnected())
                    && (!socket.isClosed())
                    && (!socket.isOutputShutdown())) {
                Log.d(TAG, "连接服务器成功，开始设备接入签到");
                isOnline = true;
                if (SendHandler != null)
                    SendHandler.sendEmptyMessage(EVT_CONNECT);
            }
        } catch (UnknownHostException e) {
            Log.e(TAG, "连接服务器出错UnknownHostException:" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "连接服务器出错IOException:" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "连接服务器出错Exception:" + e.getMessage());
            e.printStackTrace();
        } catch (Throwable e) {
            Log.e(TAG, "连接服务器出错Throwable:" + e.getMessage());
            e.printStackTrace();
        }
    }

    // Thread to send content from Socket
    class UserDataThread extends Thread {

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
            //if (null != socket)
            {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                //Looper.prepare();

                SendHandler = new Handler() {

                    int cResult = 0;
                    String strTemp = "";
                    byte[] bytesTemp = new byte[1024 * 2];

                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case EVT_NULL:
                                break;

                            case EVT_CONNECT:
                                Log.i(TAG, "连接服务器成功，设备接入发送");
                                s_NetEventMsg.lngNetTimeout = 0;
                                g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus | 0x00000001;
                                DeviceJoin_Send();
                                s_NetEventMsg.type = EVT_DEVICEJOIN;

                                // 步骤4:通知主线程,将接收的消息显示到界面
                                if (gUIMainHandler != null)
                                    gUIMainHandler.sendEmptyMessage(CONNECTOK);
                                break;

                            case EVT_DEVICEJOIN:
                                Log.i(TAG, "设备接入接收成功");
                                s_NetEventMsg.lngNetTimeout = 0;
                                cResult = DeviceJoin_Recv();
                                if (cResult == OK) {
                                    Log.i(TAG, "设备签到发送");
                                    if (g_WorkInfo.cNetworkErrFlag == 1) {
                                        g_WorkInfo.cNetworkErrFlag = 0;
                                    }
                                    //设备签到发送
                                    g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus | 0x00000002;
                                    SignIn_Send();
                                    s_NetEventMsg.type = EVT_SIGNIN;
                                } else {
                                    g_WorkInfo.cNetworkErrFlag = 1;
                                    if (cResult == 17) {
                                        Log.i(TAG, "设备未注册");
                                        strTemp = "设备未注册";
                                    } else {
                                        Log.i(TAG, "设备接入异常");
                                        strTemp = "设备接入异常";
                                    }
                                    if (gUIMainHandler != null) {
                                        Message msga = Message.obtain();
                                        msga.obj = strTemp;
                                        msga.what = SETERR_MSG;
                                        gUIMainHandler.sendMessage(msga);
                                    }
                                }
                                break;

                            case EVT_SIGNIN:
                                Log.i(TAG, "设备签到接收成功");
                                s_NetEventMsg.lngNetTimeout = 0;
                                cResult = SignIn_Recv();
                                if (cResult == OK) {
                                    if (g_WorkInfo.cNetworkErrFlag == 1) {
                                        g_WorkInfo.cNetworkErrFlag = 0;
                                    }
                                    Log.i(TAG, "设备签到成功");
                                    s_NetEventMsg.type = EVT_ONGOING;
                                } else {
                                    g_WorkInfo.cNetworkErrFlag = 1;
                                    if (cResult == 4) {
                                        //平台客户号不一致 界面提示
                                        Log.i(TAG, "站点号不一致");
                                        strTemp = "站点号不一致";
                                    } else {
                                        Log.i(TAG, "设备签到异常");
                                        strTemp = "设备签到异常";
                                    }
                                    if (gUIMainHandler != null) {
                                        Message msga = Message.obtain();
                                        msga.obj = strTemp;
                                        msga.what = SETERR_MSG;
                                        gUIMainHandler.sendMessage(msga);
                                    }
                                }
                                break;

                            case EVT_SIGNOUT:
                                break;
                            case EVT_ONGOING:
                                break;

                            case EVT_CARDCHECK:
                                Log.i(TAG, "卡户校验");
                                bytesTemp = (byte[]) msg.obj;
                                SubsidyInfo pSubsidyInfo = new SubsidyInfo();
                                CardBasicParaInfo pCardBasicInfo = new CardBasicParaInfo();
                                try {
                                    pCardBasicInfo = (CardBasicParaInfo) g_CardBasicInfo.clone();
                                } catch (CloneNotSupportedException e) {
                                    e.printStackTrace();
                                }
                                cResult = CheckCardInfo_Recv(bytesTemp, pCardBasicInfo, pSubsidyInfo);
                                Log.i(TAG, "联网卡户校验结果:" + cResult);
                                NetCheckCardInfo(pCardBasicInfo, pSubsidyInfo, cResult);
                                break;

                            case EVT_CardCheckOver: //卡户校验超时
                                Log.e(TAG, "卡户校验超时");
                                cResult = 0xff;
                                g_CommInfo.cQueryCardInfoStatus = 0;
                                s_NetEventMsg.cMsgResState = 0;
                                s_NetEventMsg.lngNetTimeout = 0;
                                g_CommInfo.cRecvWaitState = 0;
                                g_CommInfo.lngSendComStatus = 0;

                                SubsidyInfo pSubsidyInfoA = new SubsidyInfo();
                                CardBasicParaInfo pCardBasicInfoA = new CardBasicParaInfo();
                                NetCheckCardInfo(pCardBasicInfoA, pSubsidyInfoA, cResult);
                                break;

                            case EVT_QROrderNum:
                                Log.i(TAG, "二维码订单号信息");
                                bytesTemp = (byte[]) msg.obj;
                                cResult = GetQrCodeID_Recv(bytesTemp);
                                Log.i(TAG, "获取二维码订单号结果:" + cResult);
                                NetGetQROrderNum((int) g_WorkInfo.lngOrderNum, cResult);
                                break;

                            case EVT_QROrderResult:
                                Log.i(TAG, "二维码订单交易结果信息");
                                bytesTemp = (byte[]) msg.obj;
                                cResult = GetQrCodeResult_Recv(bytesTemp);
                                Log.i(TAG, "获取二维码交易结果:" + cResult);
                                NetGetQROrderResult(cResult);
                                if (g_LocalInfo.cDockposFlag == 1) {
                                    if (cResult == 0) {
                                        SerialWorkTask.onPayDone(0x00);
                                    } else {
                                        SerialWorkTask.onPayDone(0x99);
                                    }
                                }
                                break;

                            case EVT_QRTradeInfo:
                                Log.i(TAG, "正元二维码交易结果信息");
                                bytesTemp = (byte[]) msg.obj;
                                cResult = GetZYQrCodeResult_Recv(bytesTemp);
                                Log.i(TAG, "正元二维码有效性验证结果:" + cResult);
                                NetGetQRCodeTradingInfo(g_OnlinePayInfo.lngOrderNum, cResult);
                                if (g_LocalInfo.cDockposFlag == 1) {
                                    if (cResult == 0) {
                                        SerialWorkTask.onPayDone(0x00);
                                    } else if ((cResult >= 1) || (cResult == 0xff)) {
                                        Log.i(TAG, "超时处理中");
                                    } else {
                                        SerialWorkTask.onPayDone(0x99);
                                    }
                                }
                                break;

                            case EVT_QRTradeInfoOver:
                                Log.i(TAG, "正元二维码交易结果信息超时");
                                NetGetQRCodeTradingInfo(0, 0xff);
                                if (g_LocalInfo.cDockposFlag == 1) {
                                    SerialWorkTask.onPayDone(0x99);
                                }
                                break;

                            case EVT_QRThirdTradeInfo:
                                Log.i(TAG, "第三方二维码交易结果信息");
                                bytesTemp = (byte[]) msg.obj;
                                cResult = GetThirdCodeResult_Recv(bytesTemp);
                                Log.i(TAG, "第三方二维码有效性验证结果:" + cResult);
                                NetGetThirdCodeTradingInfo(cResult);
                                if (g_LocalInfo.cDockposFlag == 1) {
                                    if (cResult == 0) {
                                        SerialWorkTask.onPayDone(0x00);
                                    } else {
                                        SerialWorkTask.onPayDone(0x99);
                                    }
                                }
                                break;

                            case EVT_QRALITradeInfoOver:
                                Log.i(TAG, "第三方二维码交易结果信息超时");
                                NetGetThirdCodeTradingInfo(0xff);
                                if (g_LocalInfo.cDockposFlag == 1) {
                                    SerialWorkTask.onPayDone(0x99);
                                }
                                break;

                            case EVT_ONLRDispel:       //在线冲正
                                Log.i(TAG, "在线冲正结果信息");
                                bytesTemp = (byte[]) msg.obj;
                                cResult = OnLine_RecordDispel_Recv(bytesTemp);
                                Log.i(TAG, "在线冲正结果:" + cResult);
                                OnlingLastRecDispel(cResult);
                                break;

                            case EVT_ONLRDispelOver:       //在线冲正超时
                                Log.i(TAG, "在线冲正超时");
                                OnlingLastRecDispel(0xff);
                                break;

                            case EVT_Withhold:       //第三方代扣
                                Log.i(TAG, "第三方代扣结果信息");
                                bytesTemp = (byte[]) msg.obj;
                                cResult = ThirdWithHold_Recv(bytesTemp);
                                Log.i(TAG, "第三方代扣结果信息:" + cResult);
                                OnlingWithholdTrading(cResult);
                                if (g_LocalInfo.cDockposFlag == 1) {
                                    if (cResult == 0) {
                                        SerialWorkTask.onPayDone(0x00);
                                    } else if ((cResult >= 1) || (cResult == 0xff)) {
                                        Log.i(TAG, "超时处理中");
                                    } else {
                                        SerialWorkTask.onPayDone(0x99);
                                    }
                                }
                                break;
                            case EVT_WithholdOver:       //第三方代扣超时
                                Log.i(TAG, "第三方代扣超时");
                                OnlingWithholdTrading(0xff);
                                SerialWorkTask.onPayDone(0x99);
                                break;
                            case EVT_DISCONNECT:
                                Log.d(TAG, "EVT_DISCONNECT,网络断开");
                                DisConnect();
                                g_WorkInfo.cRunState = 2;
                                break;
                        }
                        super.handleMessage(msg);
                    }
                };
                Looper.loop();
            }
        }
    }

    // Thread to Recv content from Socket
    class RecvDataThread extends Thread {

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
            //ConnectServer(g_LocalNetStrInfo.strServerIP1,g_LocalNetStrInfo.ServerPort1, (byte) 0);
            while (isStart) {
                SystemClock.sleep(10);
                if (g_WorkInfo.cNetlinkStatus == 0)
                    continue;
                if ((null == socket) || (isOnline == false)) {
                    continue;
                }
                if ((isOnline)
                        && (null != socket)
                        && (socket.isConnected())
                        && (!socket.isClosed())
                        && (!socket.isInputShutdown())) {
                    try {
                        s_lngRecvBufferLen = inputStream.read(s_cRecvDataBuf);
                        if (s_lngRecvBufferLen > 0) {
                            //Log.i(TAG, "接收到数据长度:" + s_lngRecvBufferLen);
                            byte[] bytesTemp = new byte[s_lngRecvBufferLen];
                            System.arraycopy(s_cRecvDataBuf, 0, bytesTemp, 0, s_lngRecvBufferLen);
                            PrintArray("接收到数据", bytesTemp);
                            //处理数据完毕
                            if (g_CommInfo.cRecvState == 0) {
                                if ((s_lngRecvBufferLen < 6) || ((s_lngRecvBufferLen) > 1024)) {
                                    Log.i(TAG, "接收数据太长或太短：%d,返回" + s_lngRecvBufferLen);
                                    continue;
                                }
                                if (CheckRecvData() == 0) {
                                    TreatRecvData();//处理接收到的数据
                                    g_WorkInfo.lngOnLineTime = g_WorkInfo.lngOSTime;
                                    g_WorkInfo.lngOffLineTime = g_WorkInfo.lngOSTime;
                                }
                            }
                        }
                    } catch (IOException e) {
                        isOnline = false;
                        SendHandler.sendEmptyMessage(EVT_DISCONNECT);
                        Log.e(TAG, "读取网络数据IOException:" + e.getMessage());
                        e.printStackTrace();
                    } catch (Throwable e) {
                        isOnline = false;
                        Log.e(TAG, "读取网络数据Throwable:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Thread to Recv content from Socket
    class HandleRecvThread extends Thread {

        private boolean isStart = false;
        private int cResult;

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
            //if ((null != socket)&&(!socket.isClosed()))
            {
                while (isStart) {
                    SystemClock.sleep(5);
                    if (g_WorkInfo.cNetlinkStatus == 0)
                        continue;
                    //------------------------- 接收报文处理-------------------------//
                    cResult = RecvMassageHand();
                    if (cResult != OK) {
                        Log.d(TAG, "接收报文失败");
                        g_CommInfo.cRecvState = 0;
                    }
                    //-------------------------发送报文处理-------------------------//
                    cResult = SendMassageHand();
                    if (cResult != OK) {
                        Log.d(TAG, "发送报文失败");
                        g_CommInfo.lngSendComStatus = 0;
                    }
                }
            }
        }
    }

    //处理网络重连和上传流水
    class ScanNetWorkThread extends Thread {
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
            //if ((null != socket)&&(!socket.isClosed()))
            {
                while (isStart) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (Exception e) {
                    }

                    ReConnectProcess();//重连服务流程
                    UpSendPayRecord();//上传流水流程

                    if (s_cReflashPareState == 1) {
                        if (g_WorkInfo.cRunState == 1) {
                            Log.d(TAG, "===================================更新网络参数数据信息===================================");
                            Publicfun.ReadAllSysParam();
                        }
                        s_cReflashPareState = 0;
                    }
                }
            }
        }
    }

    /*
     处理接收数据
     */
    private void TreatRecvData() {
        int cResult;
        int iContextLen;
        int wPacketLen;
        int wProtocolID = 0;

        if (g_CommInfo.lngSendComStatus != 0) {
            if (g_WorkInfo.cRunState == 1) {
                //Log.i(TAG, String.format("================设备运行中应答:协议ID:send:%04x-->recv:%04x================",s_wProtocolID,wProtocolID));
            }
        }

        //主动发送的报文
        byte cSendComState = 0;
        wPacketLen = DataTransfer.byte2Short(s_cRecvDataBuf, 1);//长度
        wProtocolID = DataTransfer.byte2ShortBIG(s_cRecvDataBuf, 9);//协议号
        //Log.i(TAG,String.format("接收到的报文:%04x-%d", wProtocolID,wPacketLen));
        if ((s_wProtocolID == wProtocolID) && (g_CommInfo.lngSendComStatus != 0)) {
            //Log.i(TAG,String.format("主动发送报文接收到的报文:%04x", wProtocolID));
            cSendComState = 1;
            //s_NetEventMsg.cMsgResState = 1;
            s_NetEventMsg.wMsgTimeoutCount = 0;
            System.arraycopy(s_cRecvDataBuf, 3, s_cRecvDataBuf_N, 0, wPacketLen - 3);

            switch (wProtocolID) {
                //0x00000001:接入请求
                //0x00000002:签到请求
                //0x00000004:签退请求
                //0x00000008:钥匙卡校验
                //0x00000010:卡号有效性验证
                //0x00000020:上传交易流水
                //0x00000040:以太网设备请求下载参数
                //0x00000080:在线冲正
                //0x00000100:确认RF-SIM补助信息
                //0x00000200:RFSIM卡充值计算MAC1
                //0x00000400:SIMPASS获取计算充值MAC的数据
                //0x00000800:扫码验证
                //0x00001000:获取二维码订单号
                case 0x0100:    //	设备接入
                    Log.i(TAG, "设备接入接收");
                    g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000001;
                    SendHandler.sendEmptyMessage(EVT_DEVICEJOIN);
                    break;

                case 0x0101:    //	设备签到
                    Log.i(TAG, "设备签到接收");
                    g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000002;
                    SendHandler.sendEmptyMessage(EVT_SIGNIN);
                    break;

                case 0x0102:    //	设备签退	(NO)
                    Log.i(TAG, "设备签退接收(NO)");
                    g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000004;
                    SendHandler.sendEmptyMessage(EVT_SIGNOUT);
                    break;

                case 0x0d02:    //	钥匙卡校验
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00000008) == 0x00000008) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000008;
                            Log.i(TAG, "钥匙卡校验校验接收");
                        }
                        g_CommInfo.cRecvState = 50;
                    }
                    break;

                case 0x0d03:    //	卡号有效性验证
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00000010) == 0x00000010) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000010;
                            Log.i(TAG, "卡内编号校验接收");
                        }
                        g_CommInfo.cRecvState = 51;
                    }
                    break;
                case 0x0d05:    //	扫码有效性验证
                case 0x0d12:    //	扫码有效性验证大金额
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00000800) == 0x00000800) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000800;
                            Log.i(TAG, "扫码有效性验证接收");
                        }
                        g_CommInfo.cRecvState = 58;
                    }
                    break;
                case 0x0d06:    //	获取二维码订单号
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00001000) == 0x00001000) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00001000;
                            Log.i(TAG, "获取二维码订单号接收");
                        }
                        g_CommInfo.cRecvState = 59;
                    }
                    break;


                case 0x0d08:    //	获取第三方二维码信息
                case 0x0d13:    //	获取第三方二维码信息
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00002000) == 0x00002000) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00002000;
                            Log.i(TAG, "获取第三方二维码信息接收");
                        }
                        g_CommInfo.cRecvState = 62;
                    }
                    break;
                case 0x0d09:    //	获取第三方二维码信息
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00008000) == 0x00008000) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00008000;
                            Log.i(TAG, "获取末笔交易信息接收");
                        }
                        g_CommInfo.cRecvState = 63;
                    }
                    break;
                case 0x0d10:    //	第三方代扣
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00010000) == 0x00010000) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00010000;
                            Log.i(TAG, "第三方代扣代扣接收");
                        }
                        g_CommInfo.cRecvState = 64;
                    }
                    break;
                case 0x0d15: //在线冲正
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00000080) == 0x00000080) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000080;
                            Log.i(TAG, "在线冲正接收");
                        }
                        g_CommInfo.cRecvState = 54;
                    }
                    break;
                case 0x0f02:    //上传交易流水
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00000020) == 0x00000020) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000020;
                            Log.i(TAG, "上传交易流水接收");
                        }
                        g_CommInfo.cRecvState = 52;
                    }
                    break;

                case 0x0c02:    //	以太网设备请求下载参数
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00000040) == 0x00000040) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000040;
                            Log.i(TAG, "以太网设备请求下载参数接收");
                        }
                        g_CommInfo.cRecvState = 53;
                    }
                    break;

                case 0x1401:    //	获取FTP服务器信息
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00004000) == 0x00004000) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00004000;
                            Log.i(TAG, "获取FTP服务器信息接收");
                        }
                        g_CommInfo.cRecvState = 61;
                    }
                    break;

//                case 0x0e01:    //	取RF-SIM卡户信息 卡号校验(补助)
//                    if (s_wProtocolID == wProtocolID) {
//                        if ((g_CommInfo.lngSendComStatus & 0x00000080) == 0x00000080) {
//                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000080;
//                           Log.i(TAG,"取RF-SIM卡户信息 卡号校验(补助)接收");
//                        }
//                        g_CommInfo.cRecvState = 54;
//                    }
//                    break;

                case 0x0e02:    //	补助确认
                    Log.i(TAG, "RF-SIM补助确认");
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00000100) == 0x00000100) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000100;
                            Log.i(TAG, "取RF-SIM卡户信息 卡号校验(补助)接收");
                        }
                        g_CommInfo.cRecvState = 55;
                    }
                    break;

                case 0x0e03:    //	充值计算MAC1
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00000200) == 0x00000200) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000200;
                            Log.i(TAG, "充值计算MAC1接收");
                        }
                        g_CommInfo.cRecvState = 56;
                    }
                    break;

                case 0x1001:    //	SIMPASS获取计算充值MAC的数据
                    if (s_wProtocolID == wProtocolID) {
                        if ((g_CommInfo.lngSendComStatus & 0x00000400) == 0x00000400) {
                            g_CommInfo.lngSendComStatus = g_CommInfo.lngSendComStatus - 0x00000400;
                            Log.i(TAG, "SIMPASS获取计算充值MAC的数据接收");
                        }
                        g_CommInfo.cRecvState = 57;
                    }
                    break;
            }
        }
        System.arraycopy(s_cRecvDataBuf, 3, s_cRecvDataBuf_P, 0, wPacketLen - 3);

        if (cSendComState == 1) {
            cSendComState = 0;
            s_lngRecvBufferLen = 0;
            //memset(s_cRecvDataBuf, 0, sizeof(s_cRecvDataBuf));
            return;
        }
        //报文时间+报文命令
        System.arraycopy(s_cRecvDataBuf, 3, bContextData, 0, 8);

        if (wProtocolID == 0x0d07) {
            System.arraycopy(s_cRecvDataBuf, 3, s_cRecvDataBuf_N, 0, wPacketLen - 3);
        }
        //被动接收到的报文
        switch (wProtocolID) {
            case 0x0201:  //设置系统时间
                Log.i(TAG, "设置系统时间");
                byte[] cCurDateTime = new byte[6];
                System.arraycopy(s_cRecvDataBuf_P, 8, cCurDateTime, 0, 6);
                System.arraycopy(cCurDateTime, 0, g_WorkInfo.cOKDataTime, 0, 6);
                cResult = Publicfun.SetDateTime(cCurDateTime);
                bContextData[8] = (byte) cResult;
                iContextLen = 9;
                AutoSendBuf(iContextLen, bContextData);
                g_CommInfo.cRecvState = 0;
                break;

            case 0x0202:  //读取系统时间
                Log.i(TAG, "读取系统时间");
                g_CommInfo.cRecvState = 2;
                byte[] cRCurDateTime = new byte[6];
                Publicfun.GetCurrDateTime(cRCurDateTime);
                System.arraycopy(cRCurDateTime, 0, bContextData, 8, 6);
                iContextLen = 8 + 6;
                AutoSendBuf(iContextLen, bContextData);
                g_CommInfo.cRecvState = 0;
                break;

            case 0x0301: //参数版本
                Log.i(TAG, "取参数版本");
                g_CommInfo.cRecvState = 3;
                iContextLen = GetParaVerInfo();
                AutoSendBuf(iContextLen, bContextData);
                g_CommInfo.cRecvState = 0;
                break;
            case 0x0302: //参数版本
                Log.i(TAG, "取黑白名单版本");
                g_CommInfo.cRecvState = 4;
                iContextLen = GetBWListVerInfo();
                AutoSendBuf(iContextLen, bContextData);
                g_CommInfo.cRecvState = 0;
                break;

            case 0x0401: //系统参数
                Log.i(TAG, "设置系统参数");
                g_CommInfo.cRecvState = 7;
                break;
            case 0x0402: //读取系统参数
                //Log.d(TAG,"读取系统参数");
                break;

            case 0x0501: //站点参数
                Log.i(TAG, "设置站点参数");
                g_CommInfo.cRecvState = 8;
                break;
            case 0x0502: //站点参数
                //Log.d(TAG,"读取站点参数");
                break;

            case 0x0601: //钱包参数
                Log.i(TAG, "清空钱包参数");
                g_CommInfo.cRecvState = 9;
                break;
            case 0x0602: //设置钱包参数
                Log.i(TAG, "设置钱包参数");
                g_CommInfo.cRecvState = 10;
                break;
            case 0x0603: //读取钱包参数
                //Log.d(TAG,"读取钱包参数");
                break;
            case 0x0604: //下载钱包完成
                Log.i(TAG, "下载钱包完成");
                g_CommInfo.cRecvState = 11;
                break;

            case 0x0701: //单键参数
                Log.i(TAG, "清空单键参数");
                g_CommInfo.cRecvState = 12;
                break;
            case 0x0702: //设置单键参数
                Log.i(TAG, "设置单键参数");
                g_CommInfo.cRecvState = 13;
                break;
            case 0x0703: //读取单键参数
                //Log.d(TAG,"读取单键参数");
                break;
            case 0x0704: //下载单键完成
                Log.i(TAG, "下载单键完成");
                g_CommInfo.cRecvState = 14;
                break;

            case 0x0801: //身份参数
                Log.i(TAG, "清空身份参数");
                g_CommInfo.cRecvState = 15;
                break;
            case 0x0802: //设置身份参数
                Log.i(TAG, "设置身份参数");
                g_CommInfo.cRecvState = 16;
                break;
            case 0x0803: //读取身份参数
                //Log.d(TAG,"读取身份参数");
                break;
            case 0x0804: //下载身份完成
                Log.i(TAG, "下载身份完成");
                g_CommInfo.cRecvState = 17;
                break;

            case 0x0901: //身份优惠
                Log.i(TAG, "清空身份优惠");
                g_CommInfo.cRecvState = 18;
                break;
            case 0x0902: //设置身份优惠
                Log.i(TAG, "设置身份优惠");
                g_CommInfo.cRecvState = 19;
                break;
            case 0x0903: //读取身份优惠
                Log.i(TAG, "读取身份优惠");
                break;
            case 0x0904: //下发身份优惠结束
                Log.i(TAG, "下发身份优惠结束");
                g_CommInfo.cRecvState = 20;
                break;

            case 0x0A01: //营业分组
                Log.i(TAG, "清空营业分组");
                g_CommInfo.cRecvState = 21;
                break;
            case 0x0A02: //设置营业分组
                Log.i(TAG, "设置营业分组");
                g_CommInfo.cRecvState = 22;
                break;
            case 0x0A03: //读取营业参数
                //Log.d(TAG,"读取营业参数");
                break;
            case 0x0A04: //下发营业分组完成
                Log.i(TAG, "下发营业分组完成");
                g_CommInfo.cRecvState = 23;
                break;

            case 0x0B01: //黑白名单
                Log.i(TAG, "清空黑名单");
                g_CommInfo.cRecvState = 24;
                break;
            case 0x0B02: //初始黑名单
                Log.i(TAG, "初始黑名单");
                g_CommInfo.cRecvState = 25;
                break;
            case 0x0B03: //初始黑名单结束
                Log.i(TAG, "初始黑名单结束");
                g_CommInfo.cRecvState = 26;
                break;
            case 0x0B04: //清除变更黑白名单
                Log.i(TAG, "清除变更黑白名单");
                g_CommInfo.cRecvState = 27;
                break;
            case 0x0B05: //变更黑白名单
                Log.i(TAG, "变更黑白名单");
                g_CommInfo.cRecvState = 28;
                break;
            case 0x0B06: //变更黑白名单结束
                Log.i(TAG, "变更黑白名单结束");
                g_CommInfo.cRecvState = 29;
                break;

            case 0x0C01:  //下载参数结束
                Log.i(TAG, "下载参数结束");
                g_CommInfo.cRecvState = 30;
                break;

            case 0x0C0F:  //服务在线应答
                //Log.i(TAG,"服务在线应答");
                iContextLen = 8;
                AutoSendBuf(iContextLen, bContextData);
                g_CommInfo.cRecvState = 0;
                break;

            case 0x0D01:  //取以太网设备状态
                //Log.i(TAG,"取以太网设备状态");
                iContextLen = GetStationState();
                AutoSendBuf(iContextLen, bContextData);
                g_CommInfo.cRecvState = 0;
                break;

            case 0x1101: //应用程序下载
                //Log.d(TAG,"应用程序下载开始(清空APP程序)");
                g_CommInfo.cRecvState = 34;
                break;
            case 0x1102: //下载应用程序内容
                //Log.d(TAG,"下载应用程序内容");
                g_CommInfo.cRecvState = 35;
                break;
            case 0x1103: //下载应用程序结束
                //Log.d(TAG,"下载应用程序结束");
                g_CommInfo.cRecvState = 36;
                break;

            case 0x0d07: //获取二维码结果信息
                Log.d(TAG, "获取二维码结果信息");
                g_CommInfo.cRecvState = 60;
                break;
        }
    }

    //统一接收数据,并校验数据
    private int CheckRecvData() {
        int i;
        short lngCheckSum = 0;
        short lngTemp;

        if (s_cRecvDataBuf[0] == 0x02) {
            //Log.d(TAG,"收到报文头");
            s_lngRecvDataLen = DataTransfer.byte2Short(s_cRecvDataBuf, 1);
        } else {
            return 1;
        }

        //校验接收到的数据
        for (i = 0; i < (s_lngRecvDataLen - 2); i++) {
            lngCheckSum += DataTransfer.byte2int(s_cRecvDataBuf[i]);
        }

        lngCheckSum = (short) (lngCheckSum & 0xFFFF);
        lngTemp = (short) DataTransfer.byte2Short(s_cRecvDataBuf, s_lngRecvDataLen - 2);
        if (lngTemp != lngCheckSum) {
            Log.i(TAG, String.format("接收报文CRC出错:%02x-%02x", lngTemp, lngCheckSum));
            return 2;
        }
        return 0;
    }

    //发送数据
    public int SendDataBuf(final int iContextLen, final byte[] bSendDataBuf) {
        int n = 0;
        int lngCheckSum = 0;
        byte[] byteSend = new byte[iContextLen + 6];

        //报头
        byteSend[n++] = 0x02;
        //报文长度
        byte[] bytesLength = DataTransfer.int2Bytes(iContextLen + 6, 2);
        System.arraycopy(bytesLength, 0, byteSend, n, bytesLength.length);
        n += 2;
        System.arraycopy(bSendDataBuf, 0, byteSend, n, iContextLen);
        n += iContextLen;
        //报尾
        byteSend[n++] = 0x03;

        for (int i = 0; i < n; i++) {
            lngCheckSum += DataTransfer.byte2int(byteSend[i]);
        }
        byte[] checkByte = DataTransfer.int2Bytes(lngCheckSum, 2);
        System.arraycopy(checkByte, 0, byteSend, n, checkByte.length);

        if (socket == null) {
            Log.e(TAG, "SendDataBuf发送数据失败 socket==null:");
            return -1;
        }
        try {
            if ((outputStream != null)
                    && (!socket.isClosed())
                    && (!socket.isOutputShutdown())) {
                outputStream.write(byteSend);
                outputStream.flush();
                PrintArray("SendDataBuf发送数据", byteSend);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "SendDataBuf发送数据失败:" + e.getMessage());
            return -1;
        }
        return 0;
    }

    //自动发送
    public int AutoSendBuf(final int iContextLen, final byte[] bSendDataBuf) {
        int n = 0;
        int lngCheckSum = 0;
        byte[] byteSend = new byte[iContextLen + 6];

        //报头
        byteSend[n++] = 0x02;
        //报文长度
        byte[] bytesLength = DataTransfer.int2Bytes(iContextLen + 6, 2);
        System.arraycopy(bytesLength, 0, byteSend, n, bytesLength.length);
        n += 2;
        System.arraycopy(bSendDataBuf, 0, byteSend, n, iContextLen);
        n += iContextLen;
        //报尾
        byteSend[n++] = 0x03;

        for (int i = 0; i < n; i++) {
            lngCheckSum += DataTransfer.byte2int(byteSend[i]);
        }
        byte[] checkByte = DataTransfer.int2Bytes(lngCheckSum, 2);
        System.arraycopy(checkByte, 0, byteSend, n, checkByte.length);
        if (socket == null) {
            Log.e(TAG, "AutoSendBuf发送数据失败 socket==null:");
            return -1;
        }
        try {
            if ((outputStream != null)
                    && (!socket.isClosed())
                    && (!socket.isOutputShutdown())) {
                outputStream.write(byteSend);
                outputStream.flush();
                //PrintArray("AutoSendBuf发送数据", byteSend);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "AutoSendBuf发送数据失败:" + e.getMessage());
            return -1;
        }
        return 0;
    }

    //发送报文处理
    public int SendMassageHand() {
        int cResult = 0;

        //0x00000001:接入请求
        //0x00000002:签到请求
        //0x00000004:签退请求
        //0x00000008:钥匙卡校验
        //0x00000010:卡号有效性验证
        //0x00000020:上传交易流水
        //0x00000040:以太网设备请求下载参数
        //0x00000080:在线冲正
        //0x00000100:确认RF-SIM补助信息
        //0x00000200:RFSIM卡充值计算MAC1
        //0x00000400:SIMPASS获取计算充值MAC的数据
        //0x00000800:扫码验证

        if ((g_CommInfo.lngSendComStatus > 0) && (g_CommInfo.cRecvWaitState == 0)) {
            s_NetEventMsg.cMsgResState = 0;
            s_NetEventMsg.lngNetTimeout = 0;

            //0x00000001:接入请求
            if ((g_CommInfo.lngSendComStatus & 0x00000001) == 0x00000001) {
            }
            //0x00000002:签到请求
            if ((g_CommInfo.lngSendComStatus & 0x00000002) == 0x00000002) {
            }
            //0x00000004:签退请求
            if ((g_CommInfo.lngSendComStatus & 0x00000004) == 0x00000004) {
            }
            //0x00000008:钥匙卡校验
            if ((g_CommInfo.lngSendComStatus & 0x00000008) == 0x00000008) {
                Log.i(TAG, String.format("操作员卡号B：%02x.%02x.%02x.%02x", g_WorkInfo.OpetorSerID[0], g_WorkInfo.OpetorSerID[1], g_WorkInfo.OpetorSerID[2], g_WorkInfo.OpetorSerID[3]));

                cResult = OptorKeyCheck_Send(g_WorkInfo.OpetorSerID, g_WorkInfo.lngOpetorKey);
                if (cResult == OK) {
                    g_CommInfo.cRecvWaitState = 1;//置位接收标记
                    //置位发送获取参数版本报文间隔时长
                    g_CommInfo.lngComInterval = 0;
                }
                return cResult;
            }
            //0x00000010:卡号有效性验证
            if ((g_CommInfo.lngSendComStatus & 0x00000010) == 0x00000010) {
                //置位接收标记
                g_CommInfo.cRecvWaitState = 1;
                //置位发送获取参数版本报文间隔时长
                g_CommInfo.lngComInterval = 0;
                cResult = CheckCardInfo_Send();
                if (cResult == OK) {
                    g_CommInfo.cRecvWaitState = 1;//置位接收标记
                    //置位发送获取参数版本报文间隔时长
                    g_CommInfo.lngComInterval = 0;
                }
                return cResult;
            }
            //二维码扫码有效性验证
            if ((g_CommInfo.lngSendComStatus & 0x00000800) == 0x00000800) {
                if (g_WorkInfo.cOtherQRFlag == 2)
                    cResult = GetZYFaceResult_Send();
                else
                    cResult = GetZYQrCodeResult_Send();

                if (cResult == OK) {
                    g_CommInfo.cRecvWaitState = 1;//置位接收标记
                }
                return cResult;
            }
            //获取二维码订单号
            if ((g_CommInfo.lngSendComStatus & 0x00001000) == 0x00001000) {
                cResult = GetQrCodeID_Send();
                if (cResult == OK) {
                    g_CommInfo.cRecvWaitState = 1;//置位接收标记
                }
                return cResult;
            }
            //获取二维码结果
            if ((g_CommInfo.lngSendComStatus & 0x00002000) == 0x00002000) {
                cResult = GetThirdCodeResult_Send();
                if (cResult == OK) {
                    g_CommInfo.cRecvWaitState = 1;//置位接收标记
                }
                return cResult;
            }
            //第三方代扣
            if ((g_CommInfo.lngSendComStatus & 0x00010000) == 0x00010000) {
                cResult = ThirdWithHold_Send();
                if (cResult == OK) {
                    g_CommInfo.cRecvWaitState = 1;//置位接收标记
                }
                return cResult;
            }
            //0x00000080:在线冲正
            if ((g_CommInfo.lngSendComStatus & 0x00000080) == 0x00000080) {
                cResult = OnLine_RecordDispel_Send();
                if (cResult == OK) {
                    g_CommInfo.cRecvWaitState = 1;//置位接收标记
                }
                return cResult;
            }
            //上传交易流水
            if ((g_CommInfo.lngSendComStatus & 0x00000020) == 0x00000020) {
                cResult = UpSendPaymentRecord_Send(g_WorkInfo.lngStartRecordID, g_WorkInfo.lngEndRecordID);
                if (cResult == OK) {
                    g_CommInfo.cRecvWaitState = 1;//置位接收标记
                }
                return cResult;
            }

            //0x000001000:获取ftp服务器信息
            if ((g_CommInfo.lngSendComStatus & 0x0004000) == 0x0004000) {
                cResult = GetFTPServerInfo_Send();
                if (cResult == OK) {
                    g_CommInfo.cRecvWaitState = 1;//置位接收标记
                }
                return cResult;
            }
            //获取第三方末笔订单信息
            if ((g_CommInfo.lngSendComStatus & 0x0008000) == 0x0008000) {
                cResult = GetThirdLastResult_Send();
                if (cResult == OK) {
                    g_CommInfo.cRecvWaitState = 1;//置位接收标记
                }
                return cResult;
            }
            //0x00000040:以太网设备请求下载参数
            if ((g_CommInfo.lngSendComStatus & 0x00000040) == 0x00000040) {
            }
            //0x00000200:RFSIM卡充值计算MAC1
            if ((g_CommInfo.lngSendComStatus & 0x00000200) == 0x00000200) {
            }
            //0x00000400:SIMPASS获取计算充值MAC的数据
            if ((g_CommInfo.lngSendComStatus & 0x00000400) == 0x00000400) {
            }
        }
        return 0;
    }

    //接收报文处理
    public int RecvMassageHand() {
        int i;
        int iContextLen;
        int cResult = 0;

        if (g_CommInfo.cRecvState > 0) {
            switch (g_CommInfo.cRecvState) {
                case 1:  //设置系统时间
                    break;

                case 7:  //设置系统参数
                    Log.i(TAG, "设置系统参数");
                    cResult = SetSystemInfo();
                    if (cResult != 0) {
                        g_WorkInfo.cNetworkErrFlag = 1;
                        if (cResult == 4) {
                            //平台客户号不一致 界面提示
                            Log.i(TAG, "平台客户号不一致");
                            if (gUIMainHandler != null) {
                                Message msg = Message.obtain();
                                msg.obj = "平台客户号不一致";
                                msg.what = SETERR_MSG;
                                gUIMainHandler.sendMessage(msg);
                            }
                        }
                    }
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 8:  //设置站点参数
                    Log.i(TAG, "设置站点参数");
                    cResult = SetStationInfo();
                    if (cResult != 0) {
                        g_WorkInfo.cNetworkErrFlag = 1;
                        if (cResult == 4) {
                            //站点号不一致 界面提示
                            Log.i(TAG, "站点号不一致");
                            if (gUIMainHandler != null) {
                                Message msg = Message.obtain();
                                msg.obj = "站点号不一致";
                                msg.what = SETERR_MSG;
                                gUIMainHandler.sendMessage(msg);
                            }
                        }
                    }
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 9:  //清空钱包参数
                    Log.i(TAG, "清空钱包参数");
                    cResult = ClearBurseInfo();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 10:  //设置钱包参数
                    Log.i(TAG, "设置钱包参数");
                    cResult = SetBurseInfo();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 11:  //下载钱包参数完成
                    Log.i(TAG, "下载钱包参数完成");
                    cResult = DownBurseOver();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 12: //清空单键参数
                    Log.i(TAG, "清空单键参数");
                    cResult = ClearOddKeyInfo();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 13:  //设置单键参数
                    Log.i(TAG, "设置单键参数");
                    cResult = SetOddKeyInfo();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 14: //下载单键参数完成
                    Log.i(TAG, "下载单键参数完成");
                    cResult = DownOddKeyOver();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 15: //清空身份参数
                    Log.i(TAG, "清空身份参数");
                    cResult = ClearStatusInfo();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 16:  //设置身份参数
                    Log.i(TAG, "设置身份参数");
                    cResult = SetStatusInfo();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 17:  //下载身份完成
                    Log.i(TAG, "下载身份完成");
                    cResult = DownStatusOver();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 18: //清空身份优惠
                    Log.i(TAG, "清空身份优惠");
                    cResult = ClearStatusPriv();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 19:  //设置身份优惠
                    Log.i(TAG, "设置身份优惠");
                    cResult = SetStatusPrivInfo();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 20:  //下发身份优惠结束
                    Log.i(TAG, "下发身份优惠结束");
                    cResult = DownStatusPrivOver();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 21:  //清空营业分组
                    Log.i(TAG, "清空营业分组");
                    cResult = ClearBusinessInfo();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 22:  //设置营业分组
                    Log.i(TAG, "设置营业分组");
                    cResult = SetBusinessInfo();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 23:  //下发营业分组完成
                    Log.i(TAG, "下发营业分组完成");
                    cResult = DownBusinessOver();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 24:  //清空黑名单
                    Log.i(TAG, "清空黑名单");
                    cResult = ClearAllBWList();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 25:  //初始黑名单
                    Log.i(TAG, "初始黑名单");
                    cResult = SetInitBWList();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 26:  //初始黑名单结束
                    Log.i(TAG, "初始黑名单结束");
                    cResult = DownInitListOver();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 27:  //清除变更黑白名单
                    Log.i(TAG, "清除变更黑白名单");
                    bContextData[8] = 0;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 28:  //变更黑白名单
                    Log.i(TAG, "变更黑白名单");
                    cResult = SetChangeBWList();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 29:  //变更黑白名单结束
                    Log.i(TAG, "变更黑白名单结束");
                    cResult = DownChangeListOver();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 30:  //下载参数结束
                    Log.i(TAG, "下载参数结束");
                    cResult = DownParaOver();
                    bContextData[8] = (byte) cResult;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 32:  //在线应答
                    break;

                case 33:  //读取设备状态
                    break;

                case 34:  //应用程序下载开始
                    Log.i(TAG,"应用程序下载开始");
                    bContextData[8] = 0;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 35:  //下载应用程序内容
                    Log.i(TAG,"下载应用程序内容");
                    bContextData[8] = 0;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                case 36:  //下载应用程序结束
                    Log.i(TAG,"下载应用程序结束");
                    DownAPPDataOver();
                    bContextData[8] = 0;
                    iContextLen = 9;
                    AutoSendBuf(iContextLen, bContextData);
                    break;

                //50:钥匙卡校验
                //51:卡号有效性验证
                //52:上传交易流水
                //53:取RF-SIM卡户信息 卡号校验(补助)
                //54:确认RF-SIM补助信息
                //55:RFSIM卡充值计算MAC1
                //56:SIMPASS获取计算充值MAC的数据

                case 50: //钥匙卡校验50
                    cResult = OptorKeyCheck_Recv();
                    if (cResult == OK) {
                        Log.i(TAG, "操作员卡校验成功");
                        if (gUIMainHandler != null) {
                            Message msg = Message.obtain();
                            msg.obj = "操作员卡校验成功";
                            msg.what = SETERR_MSG;
                            msg.arg1 = 0;
                            gUIMainHandler.sendMessage(msg);
                        }
                    } else {
                        Log.i(TAG, "操作员卡校验失败");
                        if (gUIMainHandler != null) {
                            Message msg = Message.obtain();
                            msg.obj = "操作员卡校验失败";
                            msg.what = SETERR_MSG;
                            msg.arg1 = 1;
                            gUIMainHandler.sendMessage(msg);
                        }
                    }
                    break;

                case 51: //卡户校验51
                    if ((g_CardInfo.cExistState == 1) && (g_CardInfo.cAuthenState > 0)) {
                        cResult = CheckCardInfoProcess();
                    } else {
                        Log.i(TAG, "卡户校验返回,无有效卡片存在");
                        g_CommInfo.cQueryCardInfoStatus = 0;
                    }
                    break;

                case 52: //上传交易流水52
                    Log.i(TAG, "上传交易流水");
                    cResult = UpSendPaymentRecord_Recv();
                    break;

                case 54://在线冲正
                    Log.i(TAG, "在线冲正");
                    cResult = OnLine_RecordDispel_Process();
                    break;

                case 58://扫码验证
                    Log.i(TAG, "扫码验证");
                    cResult = ScanCode_Process();
                    break;

                case 59://二维码订单号
                    Log.i(TAG, "获取二维码订单号");
                    cResult = GetQrCodeID_Process();
                    break;

                case 60://获取二维码交易结果(平台主动推送)
                    Log.i(TAG, "获取二维码交易结果(平台主动推送)");
                    cResult = GetQrCodeResult_Process();
                    break;

                case 61://获取FTP信息
                    Log.i(TAG, "获取FTP信息");
                    cResult = GetFTPServerInfo_Recv();
                    if (cResult == 0) {
                        g_FTPInfo = FTPInfoRW.ReadFTPInfo();
                    }
                    break;

                case 62://获取第三方二维码信息
                    Log.i(TAG, "获取第三方二维码信息");
                    cResult = ScanThirdCode_Process();
                    break;

                case 63://获取第三方末笔交易信息
                    Log.i(TAG, "获取第三方末笔交易信息");
                    cResult = GetThirdLastResult_Process();
                    break;

                case 64://第三方代扣
                    Log.i(TAG, "第三方代扣");
                    cResult = OnLine_ThirdWithHold_Process();
                    break;
                default:
                    break;
            }
            g_CommInfo.cRecvState = 0;
            g_CommInfo.cRecvWaitState = 0;
        }
        return OK;
    }

    //设备接入发送
    public void DeviceJoin_Send() {
        /*
        报文开始	1字节	＝0x02
        报文长度	2字节	低位在前，高位在后
        报文时间	6字节
        命令代码	2字节	＝0x0100
        设备型号	1字节	详见“设备类型列表”
        终端识别号	6字节	4个字节终端序列号
        终端软件版本号	12字节	3.5.12.0425字符串形式,不足补0x00
        报尾	1字节	＝0x03
        校验码	2字节	从“报文开始”到“报尾”的累加和，低位在前，高位在后
        */
        int n = 0;

        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x01;
        s_cSendDataBuf[n++] = 0x00;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号
        //设备型号：正元以太网多媒体POS机 223是0x24，324是0x25
        s_cSendDataBuf[n++] = LAN_DEVTYPE;

        //终端序列号
        System.arraycopy(g_BasicInfo.cTerminalSerID, 0, s_cSendDataBuf, n, 6);
        n = n + 6;

        //判断设备的软件版本号是否为0
        memcpy(g_WorkInfo.cConfigAppSWVer,g_BasicInfo.cAppSoftWareVer,0);
        if (g_WorkInfo.cConfigAppSWVer[0] == 0) {
            Log.i(TAG, "软件版本号1:" + SOFTWAREVER);
            byte[] bVersion = SOFTWAREVER.getBytes();
            System.arraycopy(bVersion, 0, s_cSendDataBuf, n, bVersion.length);
        } else {
            String strVer = new String(g_WorkInfo.cConfigAppSWVer);
            Log.i(TAG, "软件版本号2:" + strVer);
            System.arraycopy(g_WorkInfo.cConfigAppSWVer, 0, s_cSendDataBuf, n, g_WorkInfo.cConfigAppSWVer.length);
        }
        n += 12;
        byte[] SendBytes = new byte[n];
        s_cSendDataBuf[n - 1] = 0x00;
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "设备接入");
        SendDataBuf(n, SendBytes);
    }

    //设备接入接收
    public int DeviceJoin_Recv() {
        /*
        报文时间	6字节	 Yymmddhhmmss
        命令代码	2字节	＝0x0100
        成功标志	1字节	0=成功 1=服务错误 2=终端识别号不存在 0xff=未知错误
        平台软件版本号	12字节	3.5.12.0425 字符串形式,不足补0x00
         */

        //结果标记
        Log.i(TAG, "成功标志:" + s_cRecvDataBuf_N[8]);
        if (s_cRecvDataBuf_N[8] == 0)  //签到成功
        {
            //写平台软件版本号
            System.arraycopy(s_cRecvDataBuf_N, 9, g_BasicInfo.cPlatSoftWareVer, 0, 12);
            BasicInfoRW.WriteAllBasicInfo(g_BasicInfo);
            return 0;
        }
        if (s_cRecvDataBuf_N[8] == 1)  //连接数据库失败
        {
            return 95;
        }
        if (s_cRecvDataBuf_N[8] == 2)  //终端识别号未注册
        {
            Log.i(TAG, "终端识别号未注册");
            return 17;
        }
        Log.i(TAG, "设备接入失败");
        return 98;
    }

    //设备签到发送
    public void SignIn_Send() {
        /*
        报文开始	1字节	＝0x02
        报文长度	2字节	低位在前，高位在后
        报文时间	6字节
        命令代码	2字节	＝0x0101
        签到方式	1字节	0强制签到 1卡号+密码签到
        卡号	4字节	一般是充值机用，刷出纳员卡
        密码	4字节
        设备型号	1字节
        终端软件版本号	12字节	3.5.12.0425字符串形式
        终端有否未传流水	1字节	0:没有，1:有
        报尾	1字节	＝0x03
        校验码	2字节	从“报文开始”到“报尾”的累加和，低位在前，高位在后
         */
        int n = 0;

        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x01;
        s_cSendDataBuf[n++] = 0x01;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号
        //签到方式
        s_cSendDataBuf[n++] = 3;
        //卡号
        for (int i = 0; i < 4; i++) {
            s_cSendDataBuf[i + n] = 0x00;
        }
        n = n + 4;
        //密码
        for (int i = 0; i < 4; i++) {
            s_cSendDataBuf[i + n] = 0x00;
        }
        n = n + 4;
        //0x1C	正元以太网多媒体POS机 //设备型号：正元以太网多媒体POS机 223是0x24，324是0x25
        s_cSendDataBuf[n++] = LAN_DEVTYPE;

        //终端软件版本号
        //判断设备的软件版本号是否为0
        memcpy(g_WorkInfo.cConfigAppSWVer,g_BasicInfo.cAppSoftWareVer,0);
        if (g_WorkInfo.cConfigAppSWVer[0] == 0) {
            Log.i(TAG, "软件版本号1:" + SOFTWAREVER);
            byte[] cAppSoftWareVer = SOFTWAREVER.getBytes();
            System.arraycopy(cAppSoftWareVer, 0, s_cSendDataBuf, n, 12);
        } else {
            String strVer = new String(g_WorkInfo.cConfigAppSWVer);
            Log.i(TAG, "软件版本号2:" + strVer);
            System.arraycopy(g_WorkInfo.cConfigAppSWVer, 0, s_cSendDataBuf, n, 12);
        }
        n = n + 12;
        s_cSendDataBuf[n - 1] = 0x00;
        //终端有否未传流水	1字节	0:没有，1:有
        s_cSendDataBuf[n++] = 0x00;

        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "设备签到");
        SendDataBuf(n, SendBytes);
    }

    //设备签到接收
    public int SignIn_Recv() {
        int iResult;
        int iStationID;
        byte[] cAppSoftWareVer = new byte[11];
        String cTempAppSoftVer = "3.5.18.1019";//在线交易大金额版本

        g_WorkInfo.cUpdateState = 0;
        if (s_cRecvDataBuf_N[8] == 0)  //签到成功
        {
            iStationID = (s_cRecvDataBuf_N[9] & 0xff) + (s_cRecvDataBuf_N[10] & 0xff) * 256;
            Log.i(TAG, "签到站点号:" + iStationID);
            if ((g_StationInfo.iStationID != 0) && (iStationID != g_StationInfo.iStationID)) {
                Log.i(TAG, "站点号不正确");
                return 4;
            }
            g_WorkInfo.cUpdateState = 0;
            memcpy(cAppSoftWareVer, 0, s_cRecvDataBuf_N, 11, 11);
            Log.d(TAG, "平台版本号:" + new String(cAppSoftWareVer, 0, 11));
            if (cTempAppSoftVer.equals(new String(cAppSoftWareVer, 0, 11))) {
                //新版本 使用大金额
                g_WorkInfo.cPlatAppVerson = 1;
            } else {
                //老版本
                g_WorkInfo.cPlatAppVerson = 0;
            }
            Log.d(TAG, "获取应用程序app地址");
            gHttpCommInfo.lngSendHttpStatus |= 0x00000010;
            return 0;
        }
        if (s_cRecvDataBuf_N[8] == 1)  //连接数据库失败
        {
            return 95;
        }
        if (s_cRecvDataBuf_N[8] == 2)  //站点不存在
        {
            return 96;
        }
        if (s_cRecvDataBuf_N[8] == 9)  //有新程序升级
        {
            iStationID = (s_cRecvDataBuf_N[9] & 0xff) + (s_cRecvDataBuf_N[10] & 0xff) * 256;
            Log.i(TAG, "有新程序升级--签到站点号:" + iStationID);
            if ((g_StationInfo.iStationID != 0) && (iStationID != g_StationInfo.iStationID)) {
                Log.i(TAG, "站点号不正确");
                return 4;
            }
            g_WorkInfo.cUpdateState = 0;
            memcpy(cAppSoftWareVer, 0, s_cRecvDataBuf_N, 11, 11);
            Log.d(TAG, "平台版本号:" + new String(cAppSoftWareVer, 0, 11));
            if (cTempAppSoftVer.equals(new String(cAppSoftWareVer, 0, 11))) {
                //新版本 使用大金额
                g_WorkInfo.cPlatAppVerson = 1;
            } else {
                //老版本
                g_WorkInfo.cPlatAppVerson = 0;
            }
            //升级程序状态为1
            g_WorkInfo.cUpdateState = 1;
            Log.d(TAG, "获取应用程序app地址");
            gHttpCommInfo.lngSendHttpStatus |= 0x00000010;
            return 0;
        }
        Log.i(TAG, "设备签到失败");
        return 98;
    }

    //设备签退发送
    public void SignOut_Send() {
        int n = 0;

        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x01;
        s_cSendDataBuf[n++] = 0x00;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号

        //站点号
        int iStationID = 0;
        byte[] strByte = DataTransfer.int2Bytes(iStationID, 2);
        System.arraycopy(strByte, 0, s_cSendDataBuf, n, 2);
        n += 2;

        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "设备签退");
        SendDataBuf(n, SendBytes);
    }

    //设备签退接收
    public int SignOut_Recv() {
        //结果标记
        if (s_cRecvDataBuf_N[8] == 0)  //签退成功
        {
            return 0;
        }
        return 41;
    }

    //正元扫码验证发送
    public int GetZYQrCodeResult_Send() {
        int n = 0;
        int cResult;
        long lngTemp = 0;
        byte[] cPaymentTime = new byte[4];

        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        //命令代码
        if (g_WorkInfo.cPlatAppVerson == 1)//大金额
        {
            s_cSendDataBuf[n++] = 0x0d;
            s_cSendDataBuf[n++] = 0x12;
        } else {
            s_cSendDataBuf[n++] = 0x0d;
            s_cSendDataBuf[n++] = 0x05;
        }
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号
        //账号
        lngTemp = g_CardHQRCodeInfo.lngAccountID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x00FF0000) >> 16);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0xFF000000) >> 24);
        //站点号
        lngTemp = g_StationInfo.iStationID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        //终端机号
        s_cSendDataBuf[n++] = 0x00;
        //时间段序号
        s_cSendDataBuf[n++] = g_WorkInfo.cBusinessID;
        //商户号
        lngTemp = g_LocalInfo.wShopUserID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        //交易类型
        s_cSendDataBuf[n++] = 0x00;
        //交易钱包号
        s_cSendDataBuf[n++] = g_StationInfo.cWorkBurseID;
        //交易时间
        cResult = Publicfun.GetCurrCardDate(cPaymentTime);
        if (cResult != OK) {
            return cResult;
        }
        System.arraycopy(cPaymentTime, 0, s_cSendDataBuf, n, 4);
        n += 4;
        //交易金额
        if (g_LocalInfo.cDockposFlag == 1) {
            SerialWorkTask.setPayMoney();
        }
        lngTemp = g_WorkInfo.lngPaymentMoney;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        if (g_WorkInfo.cPlatAppVerson == 1)//大金额
        {
            s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF0000) >> 16);
        }
        //出纳员编号
        lngTemp = 0;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);

        //二维码订单号
        lngTemp = g_CardHQRCodeInfo.lngOrderNum;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x00FF0000) >> 16);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0xFF000000) >> 24);
        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "扫码验证发送");
        SendDataBuf(n, SendBytes);
        return OK;
    }

    //正元扫码验证发送(人脸)
    public int GetZYFaceResult_Send() {
        int n = 0;
        int cResult;
        long lngTemp = 0;
        byte[] cPaymentTime = new byte[4];

        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        //命令代码
        if (g_WorkInfo.cPlatAppVerson == 1)//大金额
        {
            s_cSendDataBuf[n++] = 0x0d;
            s_cSendDataBuf[n++] = 0x12;
        } else {
            s_cSendDataBuf[n++] = 0x0d;
            s_cSendDataBuf[n++] = 0x05;
        }
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号
        //账号
        lngTemp = g_FacePayInfo.lngAccountID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x00FF0000) >> 16);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0xFF000000) >> 24);

        //站点号
        lngTemp = g_StationInfo.iStationID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        //终端机号
        s_cSendDataBuf[n++] = 0x00;
        //时间段序号
        s_cSendDataBuf[n++] = g_WorkInfo.cBusinessID;
        //商户号
        lngTemp = g_LocalInfo.wShopUserID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        //交易类型
        s_cSendDataBuf[n++] = 0x00;
        //交易钱包号
        s_cSendDataBuf[n++] = g_StationInfo.cWorkBurseID;
        //交易时间
        cResult = Publicfun.GetCurrCardDate(cPaymentTime);
        if (cResult != OK) {
            return cResult;
        }
        System.arraycopy(cPaymentTime, 0, s_cSendDataBuf, n, 4);
        n += 4;
        //交易金额
        if (g_LocalInfo.cDockposFlag == 1) {
            SerialWorkTask.setPayMoney();
        }
        lngTemp = g_WorkInfo.lngPaymentMoney;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        if (g_WorkInfo.cPlatAppVerson == 1)//大金额
        {
            s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF0000) >> 16);
        }
        //出纳员编号
        lngTemp = 0;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);

        //二维码订单号
        lngTemp = g_FacePayInfo.lngOrderNum;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x00FF0000) >> 16);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0xFF000000) >> 24);
        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "扫码验证发送");
        SendDataBuf(n, SendBytes);
        return OK;
    }

    //自主扫码验证接收
    public int GetZYQrCodeResult_Recv(byte[] bytesTemp) {
        int i;
        int t;
        long lngAccount;
        long lngTemp;
        long lngTempA;
        int cResult = 0;

        t = 8;
        cResult = bytesTemp[16];
        if (cResult == 0) {
            lngAccount = (bytesTemp[t++] & 0xff);
            lngAccount = lngAccount + (bytesTemp[t++] & 0xff) * 256;
            lngAccount = lngAccount + (bytesTemp[t++] & 0xff) * 256 * 256;
            lngAccount = lngAccount + ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256;
            g_OnlinePayInfo.lngAccountID = lngAccount;
            //订单号
            lngTemp = (bytesTemp[t++] & 0xff);
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256;
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256 * 256;
            lngTemp = lngTemp + ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256;
            g_OnlinePayInfo.lngOrderNum = lngTemp;
            //成功标记 交易钱包号
            t = t + 2;
            //钱包余额
            lngTemp = (bytesTemp[t++] & 0xff);
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256;
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256 * 256;
            g_OnlinePayInfo.lngBurseMoney = (int) lngTemp;
            //交易金额
            lngTemp = (bytesTemp[t++] & 0xff);
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256;
            if (g_WorkInfo.cPlatAppVerson == 1)//大金额
                lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256 * 256;
            Log.i(TAG, "交易金额:" + lngTemp);
            g_OnlinePayInfo.lngPayMoney = (int) lngTemp;
            //优惠金额
            lngTemp = (bytesTemp[t++] & 0xff);
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256;
            if (g_WorkInfo.cPlatAppVerson == 1)//大金额
                lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256 * 256;
            g_OnlinePayInfo.lngPriMoney = (int) lngTemp;
            //消费管理费
            lngTemp = (bytesTemp[t++] & 0xff);
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256;
            if (g_WorkInfo.cPlatAppVerson == 1)//大金额
                lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256 * 256;
            g_OnlinePayInfo.lngManageMoney = (int) lngTemp;
            //姓名
            memcpy(g_OnlinePayInfo.cAccName, 0, bytesTemp, t, 16);
            try {
                String cShowTemp = ByteToString(g_OnlinePayInfo.cAccName, "GB2312");
                Log.i(TAG, "姓名:" + cShowTemp);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            t += 16;
            //末笔冲正订单号
            lngTempA = (bytesTemp[t++] & 0xff);
            lngTempA = lngTempA + (bytesTemp[t++] & 0xff) * 256;
            lngTempA = lngTempA + (bytesTemp[t++] & 0xff) * 256 * 256;
            lngTempA = (lngTempA + ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256);
            g_OnlinePayInfo.lngLastOrderNum = lngTempA;
            Log.e(TAG, String.format("订单号：%d", g_OnlinePayInfo.lngLastOrderNum));
            return OK;
        } else {
            return cResult;
        }
    }

    public int ScanCode_Process() {
        int cResult = 0;

        if (cResult == 0) {
            byte[] bytesTemp = new byte[512];
            System.arraycopy(s_cRecvDataBuf_N, 0, bytesTemp, 0, bytesTemp.length);
            Message message = Message.obtain();
            message.obj = bytesTemp;
            message.what = EVT_QRTradeInfo;
            SendHandler.sendMessage(message);
        }
        return cResult;
    }

    //获取二维码单号发送
    public int GetQrCodeID_Send() {
        int n = 0;
        int cResult;
        long lngTemp;

        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x0d;
        s_cSendDataBuf[n++] = 0x06;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号

        //站点号
        lngTemp = g_StationInfo.iStationID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        //终端机号
        s_cSendDataBuf[n++] = 0x00;
        //商户号
        lngTemp = g_LocalInfo.wShopUserID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);

        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "获取二维码单号发送");
        SendDataBuf(n, SendBytes);
        return OK;
    }

    //获取二维码单号接收
    public int GetQrCodeID_Recv(byte[] bytesTemp) {
        int i;
        int t;
        int wStationID;
        int wShopUserID;
        long lngTemp;
        int cResult = 0;

        t = 8;
        cResult = (bytesTemp[t++] & 0xff);
        if (cResult == 0) {//成功
            //站点号
            wStationID = ((bytesTemp[t++] & 0xff) + (bytesTemp[t++] & 0xff) * 256);
            //终端机号
            t = t + 1;
            //商户号
            wShopUserID = ((bytesTemp[t++] & 0xff) + (bytesTemp[t++] & 0xff) * 256);

            if ((wStationID == g_StationInfo.iStationID) && (wShopUserID == g_LocalInfo.wShopUserID)) {
                //二维码订单号
                lngTemp = (bytesTemp[t++] & 0xff);
                lngTemp += (bytesTemp[t++] & 0xff) * 256;
                lngTemp += (bytesTemp[t++] & 0xff) * 256 * 256;
                lngTemp += ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256;
                g_WorkInfo.lngOrderNum = lngTemp;
                return OK;
            } else {
                return 1;
            }
        } else {
            return cResult;
        }
    }

    public int GetQrCodeID_Process() {
        int cResult = 0;

        if (cResult == 0) {
            byte[] bytesTemp = new byte[512];
            System.arraycopy(s_cRecvDataBuf_N, 0, bytesTemp, 0, bytesTemp.length);
            Message message = Message.obtain();
            message.obj = bytesTemp;
            message.what = EVT_QROrderNum;
            SendHandler.sendMessage(message);
        }
        return cResult;
    }

    //获取二维码结果发送
    public int GetQrCodeResult_Send() {
        int n = 0;
        int cResult;
        long lngTemp;
        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x0d;
        s_cSendDataBuf[n++] = 0x07;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号
        //成功标志
        s_cSendDataBuf[n++] = 0x00;
        //二维码订单号
        lngTemp = 0;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x00FF0000) >> 16);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0xFF000000) >> 24);
        //站点号
        lngTemp = g_StationInfo.iStationID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        //终端机号
        s_cSendDataBuf[n++] = 0x00;
        //商户号
        lngTemp = g_LocalInfo.wShopUserID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);

        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "获取二维码结果发送");
        SendDataBuf(n, SendBytes);
        return OK;
    }

    //获取二维码结果接收
    public int GetQrCodeResult_Recv(byte[] bytesTemp) {
        int i;
        long lngTemp = 0;
        long lngOrderNum=0;

        i = 8;
        //二维码单号
        lngTemp = (bytesTemp[i++] & 0xff);
        lngTemp += (bytesTemp[i++] & 0xff) * 256;
        lngTemp += (bytesTemp[i++] & 0xff) * 256 * 256;
        lngTemp += ((long) bytesTemp[i++] & 0xff) * 256 * 256 * 256;
        lngOrderNum = lngTemp;
        g_OnlinePayInfo.lngOrderNum = lngTemp;

        //站点号
        i += 2;
        //终端机号
        i++;
        //商户号
        i += 2;
        //身份编号
        i++;
        //卡户姓名
        System.arraycopy(bytesTemp, i, g_OnlinePayInfo.cAccName, 0, 16);
        i += 16;
        //性别
        i += 2;
        //个人编号
        System.arraycopy(bytesTemp, i, g_OnlinePayInfo.cPerCode, 0, 16);
        i += 16;
        //主卡帐号
        lngTemp = (bytesTemp[i++] & 0xff);
        lngTemp += (bytesTemp[i++] & 0xff) * 256;
        lngTemp += (bytesTemp[i++] & 0xff) * 256 * 256;
        lngTemp += ((long) bytesTemp[i++] & 0xff) * 256 * 256 * 256;
        g_OnlinePayInfo.lngAccountID = lngTemp;
        //交易时间
        //System.arraycopy(bytesTemp,i,g_QrCodeResultInfo.cPaymentTime,0,4);
        i += 4;
        //交易类型
        g_OnlinePayInfo.cType = (byte) (bytesTemp[i++] & 0xff);
        //交易金额
        lngTemp = (bytesTemp[i++] & 0xff);
        lngTemp += (bytesTemp[i++] & 0xff) * 256;
        lngTemp += (bytesTemp[i++] & 0xff) * 256 * 256;
        g_OnlinePayInfo.lngPayMoney = (int) lngTemp;
        //优惠金额
        lngTemp = (bytesTemp[i++] & 0xff);
        lngTemp += (bytesTemp[i++] & 0xff) * 256;
        g_OnlinePayInfo.lngPriMoney = (int) lngTemp;
        //管理费
        lngTemp = (bytesTemp[i++] & 0xff);
        lngTemp += (bytesTemp[i++] & 0xff) * 256;
        g_OnlinePayInfo.lngManageMoney = (int) lngTemp;
        //钱包卡余额
        lngTemp = (bytesTemp[i++] & 0xff);
        lngTemp += (bytesTemp[i++] & 0xff) * 256;
        lngTemp += (bytesTemp[i++] & 0xff) * 256 * 256;
        g_OnlinePayInfo.lngBurseMoney = (int) lngTemp;

        if (lngOrderNum == g_WorkInfo.lngOrderNum)//判断是否是当前订单号
        {
            return OK;
        }else{
            return 1;
        }
    }

    //获取二维码交易结果流程(平台主动推送)
    public int GetQrCodeResult_Process() {
        int cResult = 0;

        if (cResult == 0) {
            byte[] bytesTemp = new byte[512];
            System.arraycopy(s_cRecvDataBuf_N, 0, bytesTemp, 0, bytesTemp.length);
            Message message = Message.obtain();
            message.obj = bytesTemp;
            message.what = EVT_QROrderResult;
            SendHandler.sendMessage(message);
        }
        return cResult;
    }

    //获取第三方末笔订单号结果发送
    public int GetThirdLastResult_Send() {
        int n = 0;
        int cResult;
        long lngTemp;
        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x0d;
        s_cSendDataBuf[n++] = 0x09;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号

        //条码内容长度	1字节
        s_cSendDataBuf[n++] = (byte) g_LastOrderInfo.cLen;
        System.arraycopy(g_LastOrderInfo.cQRCodeInfo, 0, s_cSendDataBuf, n, g_LastOrderInfo.cLen);
        n = n + g_LastOrderInfo.cLen;
        //类型
        s_cSendDataBuf[n++] = g_LastOrderInfo.cType;
        //站点号
        lngTemp = g_StationInfo.iStationID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);

        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "获取第三方末笔订单号结果发送");
        SendDataBuf(n, SendBytes);
        return OK;
    }

    //获取第三方末笔订单号结果接收
    public int GetThirdLastResult_Recv() {
        int i;
        int t;
        long lngAccount;
        long lngTemp;
        int cResult;

        t = 8;
        cResult = (s_cRecvDataBuf_N[t++] & 0xff);
        Log.i(TAG, "最后一笔交易有效性验证结果:" + cResult);
        if (cResult == 0) {
            //账号	4字节
            lngAccount = (s_cRecvDataBuf_N[t++] & 0xff);
            lngAccount = lngAccount + (s_cRecvDataBuf_N[t++] & 0xff) * 256;
            lngAccount = lngAccount + (s_cRecvDataBuf_N[t++] & 0xff) * 256 * 256;
            lngAccount = lngAccount + ((long) s_cRecvDataBuf_N[t++] & 0xff) * 256 * 256 * 256;
            g_LastOrderInfo.lngAccID = lngAccount;
            //交易钱包号	1字节 13
            g_LastOrderInfo.cBurseID = (byte) (s_cRecvDataBuf_N[t++] & 0xff);
            //交易金额 2字节
            lngTemp = (s_cRecvDataBuf_N[t++] & 0xff);
            lngTemp = lngTemp + (s_cRecvDataBuf_N[t++] & 0xff) * 256;
            g_LastOrderInfo.lngPayMoney = lngTemp;
            //优惠金额 2字节
            lngTemp = (s_cRecvDataBuf_N[t++] & 0xff);
            lngTemp = lngTemp + (s_cRecvDataBuf_N[t++] & 0xff) * 256;
            g_LastOrderInfo.wPriMoney = (int) lngTemp;
            //消费管理费 2字节
            lngTemp = (s_cRecvDataBuf_N[t++] & 0xff);
            lngTemp = lngTemp + (s_cRecvDataBuf_N[t++] & 0xff) * 256;
            g_LastOrderInfo.wManageMoney = (int) lngTemp;
            //姓名 16字节
            System.arraycopy(g_LastOrderInfo.cAccName, 0, s_cRecvDataBuf_N, 20, 16);
            //Log.i(TAG,"姓名:%s",g_LastOrderInfo.cAccName);
            //个人编号	16字节
            System.arraycopy(g_LastOrderInfo.cPerCode, 0, s_cRecvDataBuf_N, 36, 16);
            //Log.i(TAG,"个人编号:%s",g_LastOrderInfo.cPerCode);
            return OK;
        } else {
            return 1;
        }
    }

    //获取第三方末笔订单号结果流程
    public int GetThirdLastResult_Process() {
        int cResult = 0;

        cResult = GetThirdLastResult_Recv();
        Log.i(TAG, "获取第三方末笔订单号结果:" + cResult);
        return cResult;
    }

    //获取第三方二维码结果发送
    public int GetThirdCodeResult_Send() {
        int n = 0;
        int cResult;
        long lngTemp;
        byte[] PayDatetime = new byte[4];

        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        if (g_WorkInfo.cPlatAppVerson == 1) {
            s_cSendDataBuf[n++] = 0x0d;
            s_cSendDataBuf[n++] = 0x13;
        } else {
            s_cSendDataBuf[n++] = 0x0d;
            s_cSendDataBuf[n++] = 0x08;
        }
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号

        //条码内容长度	1字节 g_ThirdQRCodeInfo
        s_cSendDataBuf[n++] = (byte) g_ThirdQRCodeInfo.iLen;
        //条码内容	20字节	多媒体pos扫第三方二维码获取
        System.arraycopy(g_ThirdQRCodeInfo.cQRCodeInfo, 0, s_cSendDataBuf, n, g_ThirdQRCodeInfo.iLen);
        n = n + g_ThirdQRCodeInfo.iLen;
        //条码类型	1字节	1、二维码2、一维码
        s_cSendDataBuf[n++] = g_ThirdQRCodeInfo.cType;

        //第三方订单信息
        g_LastOrderInfo.lngLastOrderID = g_WorkInfo.lngOrderNum;
        g_LastOrderInfo.cLen = g_ThirdQRCodeInfo.iLen;

        System.arraycopy(g_ThirdQRCodeInfo.cQRCodeInfo, 0, g_LastOrderInfo.cQRCodeInfo, 0, g_ThirdQRCodeInfo.iLen);
        g_LastOrderInfo.cType = g_ThirdQRCodeInfo.cType;

        //站点号
        lngTemp = g_StationInfo.iStationID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        //终端机号
        s_cSendDataBuf[n++] = 0x00;
        //时间段序号	1字节
        s_cSendDataBuf[n++] = g_WorkInfo.cBusinessID;
        //商户号
        lngTemp = g_LocalInfo.wShopUserID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);

        //交易类型	1字节
        s_cSendDataBuf[n++] = 0;
        //交易钱包号	1字节
        s_cSendDataBuf[n++] = g_StationInfo.cWorkBurseID;
        //交易时间	4字节	年6位 月4位 日 5位 小时5位 分6位 秒6位
        Publicfun.GetCurrCardDate(PayDatetime);

        System.arraycopy(PayDatetime, 0, s_cSendDataBuf, n, 4);
        n = n + 4;
        //交易金额
        if (g_LocalInfo.cDockposFlag == 1) {
            SerialWorkTask.setPayMoney();
        }
        lngTemp = g_WorkInfo.lngPaymentMoney;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        if (g_WorkInfo.cPlatAppVerson == 1) {
            s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF0000) >> 16);
        }
        //出纳员编号
        lngTemp = 0;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);

        //第三方签名长度	2字节
        s_cSendDataBuf[n++] = (byte) (g_ThirdQRCodeInfo.iSignLen & 0x00FF);
        s_cSendDataBuf[n++] = (byte) ((g_ThirdQRCodeInfo.iSignLen & 0xFF00) >> 8);
        if (g_ThirdQRCodeInfo.iSignLen != 0) {
            //第三方签名	X字节	第三方的管控sdk签名数据
            System.arraycopy(g_ThirdQRCodeInfo.cSign, 0, s_cSendDataBuf, n, g_ThirdQRCodeInfo.iSignLen);
        }
        n = n + g_ThirdQRCodeInfo.iSignLen;

        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "获取第三方二维码结果发送");
        SendDataBuf(n, SendBytes);
        return OK;
    }

    //获取第三方二维码结果接收
    public int GetThirdCodeResult_Recv(byte[] bytesTemp) {
        int i;
        int t;
        long lngAccount;
        long lngTemp;
        int cResult;

        t = 8;
        cResult = (bytesTemp[t++] & 0xff);
        Log.i(TAG, "第三方扫码有效性验证结果:" + cResult);
        if (cResult == 0) {
            //账号	4字节
            lngAccount = (bytesTemp[t++] & 0xff);
            lngAccount = lngAccount + (bytesTemp[t++] & 0xff) * 256;
            lngAccount = lngAccount + (bytesTemp[t++] & 0xff) * 256 * 256;
            lngAccount = lngAccount + ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256;
            g_ThirdCodeResultInfo.lngAccountID = lngAccount;
            //交易钱包号	1字节 13
            g_ThirdCodeResultInfo.cBurseID = (byte) (bytesTemp[t++] & 0xff);
            //钱包余额 3字节
            lngTemp = (bytesTemp[t++] & 0xff);
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256;
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256 * 256;
            g_ThirdCodeResultInfo.lngBurseMoney = (int) lngTemp;
            //交易金额 2字节
            lngTemp = (bytesTemp[t++] & 0xff);
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256;
            if (g_WorkInfo.cPlatAppVerson == 1)//大金额
                lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256 * 256;
            g_ThirdCodeResultInfo.lngPayMoney = lngTemp;
            //优惠金额 2字节
            lngTemp = (bytesTemp[t++] & 0xff);
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256;
            if (g_WorkInfo.cPlatAppVerson == 1)//大金额
                lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256 * 256;
            g_ThirdCodeResultInfo.wPrivelegeMoney = (int) lngTemp;
            //消费管理费 2字节
            lngTemp = (bytesTemp[t++] & 0xff);
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256;
            if (g_WorkInfo.cPlatAppVerson == 1)//大金额
                lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256 * 256;
            g_ThirdCodeResultInfo.wManageMoney = (int) lngTemp;

            //姓名 16字节
            System.arraycopy(bytesTemp, t, g_ThirdCodeResultInfo.cAccName, 0, 16);
            t = t + 16;
            //个人编号	16字节
            System.arraycopy(bytesTemp, t, g_ThirdCodeResultInfo.cPerCode, 0, 16);
            t = t + 16;
            //支付类型
            g_ThirdCodeResultInfo.cPaymentType = bytesTemp[t++];
            Log.i(TAG, "获取第三方二维码结果接收成功：" + g_ThirdCodeResultInfo.cPaymentType);
            //末笔冲正用订单号
            lngTemp = (bytesTemp[t++] & 0xff);
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256;
            lngTemp = lngTemp + (bytesTemp[t++] & 0xff) * 256 * 256;
            lngTemp = lngTemp + ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256;
            g_ThirdCodeResultInfo.lngLastOrderNum = lngTemp;
            Log.e(TAG, String.format("订单号：%d", g_ThirdCodeResultInfo.lngLastOrderNum));
            return OK;
        } else {
            return cResult;
        }
    }

    //获取第三方二维码流程
    public int ScanThirdCode_Process() {
        int cResult = 0;

        if (cResult == 0) {
            byte[] bytesTemp = new byte[512];
            System.arraycopy(s_cRecvDataBuf_N, 0, bytesTemp, 0, bytesTemp.length);
            Message message = Message.obtain();
            message.obj = bytesTemp;
            message.what = EVT_QRThirdTradeInfo;
            SendHandler.sendMessage(message);
        }
        return cResult;
    }

    //校验卡户信息发送
    public int CheckCardInfo_Send() {
        int n = 0;
        int cResult;
        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x0d;
        s_cSendDataBuf[n++] = 0x03;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号

        //卡内编号
        s_cSendDataBuf[n++] = (byte) (g_CardBasicInfo.lngCardID & 0x000000ff);
        s_cSendDataBuf[n++] = (byte) ((g_CardBasicInfo.lngCardID & 0x0000ff00) >> 8);
        s_cSendDataBuf[n++] = (byte) ((g_CardBasicInfo.lngCardID & 0x00ff0000) >> 16);
        //电子现金钱包序号
        s_cSendDataBuf[n++] = g_StationInfo.cWorkBurseID;
        //电子现金钱包补助流水号
        s_cSendDataBuf[n++] = (byte) (g_CardBasicInfo.iWorkSubsidySID & 0x000000ff);
        s_cSendDataBuf[n++] = (byte) ((g_CardBasicInfo.iWorkSubsidySID & 0x0000ff00) >> 8);

        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "校验卡户信息发送");
        cResult = SendDataBuf(n, SendBytes);
        if (cResult != OK) {
            Log.e(TAG, "发送失败");
            return COMMUCATION_TIMEOUT;
        }
        return OK;
    }

    //校验卡户信息接收
    public int CheckCardInfo_Recv(byte[] bytesTemp, CardBasicParaInfo pCardBasicInfo, SubsidyInfo pSubsidyInfo) {
        int t, i;
        int cValidFlag;
        long lngCardID;
        int cBurseID;
        byte[] InValidTime = new byte[3];

        byte cSubsidyState;
        byte cSubBurseID;    //补助钱包
        long lngSubMoney;//补助金额
        int wSubSID;//补助流水号

        t = 8;
        //比较卡内编号
        //卡内编号
        lngCardID = (bytesTemp[t++] & 0xff);
        lngCardID = lngCardID + (bytesTemp[t++] & 0xff) * 256;
        lngCardID = lngCardID + (bytesTemp[t++] & 0xff) * 256 * 256;
        Log.i(TAG, "卡内编号:" + lngCardID);
        pCardBasicInfo.lngCardID = lngCardID;
        //判断卡内编号是否一致
        if (lngCardID != g_CardBasicInfo.lngCardID) {
            Log.i(TAG, "卡内编号不一致");
            return 92;
        }
        //判断有效标记
        cValidFlag = (bytesTemp[t++] & 0xff);
        if (cValidFlag != 0) {
            if (cValidFlag == 2)  //S70贴片卡新添加有效标志2
            {
                Log.i(TAG, "订购关系不存在");
                return 18;  //订购关系不存在
            } else {
                Log.i(TAG, "不正常");
                return 91;
            }
        } else {
            cBurseID = (bytesTemp[t++] & 0xff);//钱包号
            cSubBurseID = (byte) cBurseID;
            pCardBasicInfo.cBurseID = (byte) cBurseID;

            //是否有补助
            cSubsidyState = (byte) (bytesTemp[t++] & 0xff);
            //补助流水号
            wSubSID = (bytesTemp[t++] & 0xff);
            wSubSID = wSubSID + (bytesTemp[t++] & 0xff) * 256;
            //补助金额
            byte[] bytes = new byte[3];
            System.arraycopy(bytesTemp, 16, bytes, 0, 3);
            t = t + 3;
            lngSubMoney = Publicfun.TransBurseMoney(bytes, 0);

            pCardBasicInfo.cInStepState = (byte) (bytesTemp[t++] & 0xff);
            //扩展信息
            pCardBasicInfo.cCampusID = (bytesTemp[t++] & 0xff);                        //园区号
            pCardBasicInfo.cStatusID = (bytesTemp[t++] & 0xff);                        //身份编号
            System.arraycopy(bytesTemp, 22, pCardBasicInfo.cAccName, 0, 16);              //卡户姓名
            t = t + 16;
            System.arraycopy(bytesTemp, 38, pCardBasicInfo.cSexState, 0, 2);               //性别
            t = t + 2;
            System.arraycopy(bytesTemp, 40, pCardBasicInfo.cCreateCardDate, 0, 3);         //开户日期
            t = t + 3;
            pCardBasicInfo.iDepartID = (int) ((bytesTemp[t++] & 0xff) + (bytesTemp[t++] & 0xff) * 256);                    //部门编号
            System.arraycopy(bytesTemp, 45, pCardBasicInfo.cOtherLinkID, 0, 10);           //第三方对接关键字
            t = t + 10;
            System.arraycopy(bytesTemp, 55, pCardBasicInfo.cCardPerCode, 0, 16);           //个人编号
            t = t + 16;

            System.arraycopy(bytesTemp, 71, InValidTime, 0, 3);                //卡片有效时限
            t = t + 3;
            pCardBasicInfo.iValidTime = Publicfun.ChangeCardDate(InValidTime);

            g_WorkInfo.lngAccountID = (bytesTemp[t++] & 0xff)
                    + (bytesTemp[t++] & 0xff) * 256
                    + (bytesTemp[t++] & 0xff) * 256 * 256
                    + ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256;         //主卡帐号

            pCardBasicInfo.lngPaymentPsw = (bytesTemp[t++] & 0xff)
                    + (bytesTemp[t++] & 0xff) * 256
                    + (bytesTemp[t++] & 0xff) * 256 * 256;            //交易密码

            pSubsidyInfo.cSubsidyState = cSubsidyState;
            pSubsidyInfo.cSubBurseID = (cSubBurseID);
            pSubsidyInfo.lngSubMoney = lngSubMoney;
            pSubsidyInfo.wSubSID = wSubSID;

//            Log.i(TAG,"补助状态:"+cSubsidyState);
//            Log.i(TAG,"补助钱包:"+cSubBurseID);
//            Log.i(TAG,"补助金额:"+lngSubMoney);
//            Log.i(TAG,"补助流水号:"+wSubSID);
//
//            Log.i(TAG,String.format("园区号:%d",pCardBasicInfo.cCampusID));
//            Log.i(TAG,String.format("身份编号:%d",pCardBasicInfo.cStatusID));
//            Log.i(TAG,String.format("卡户姓名:%s",pCardBasicInfo.cAccName));
//            Log.i(TAG,String.format("性别:%s",pCardBasicInfo.cSexState));
//            Log.i(TAG,String.format("开户日期:%02x.%02x.%02x",pCardBasicInfo.cCreateCardDate[0],pCardBasicInfo.cCreateCardDate[1],pCardBasicInfo.cCreateCardDate[2]));
//            Log.i(TAG,String.format("部门编号:%d",pCardBasicInfo.iDepartID));
//            Log.i(TAG,String.format("第三方关键字:%s",pCardBasicInfo.cOtherLinkID));
//            Log.i(TAG,String.format("个人编号:%s",pCardBasicInfo.cCardPerCode));
//            Log.i(TAG,String.format("卡片有效时限:%02x.%02x.%02x",InValidTime[0],InValidTime[1],InValidTime[2]));
//            Log.i(TAG,String.format("主卡帐号:%d",g_WorkInfo.lngAccountID));
//            Log.i(TAG,String.format("交易密码:%d",pCardBasicInfo.lngPaymentPsw));

            return OK;
        }
    }

    //校验卡户信息
    public int CheckCardInfoProcess() {
        int cResult = 0;
        Log.i(TAG, "===============发射联网卡户校验数据==============");
        if (cResult == 0) {
            byte[] bytesTemp = new byte[512];
            System.arraycopy(s_cRecvDataBuf_N, 0, bytesTemp, 0, bytesTemp.length);
            Message message = Message.obtain();
            message.obj = bytesTemp;
            message.what = EVT_CARDCHECK;
            SendHandler.sendMessage(message);
        }
        return cResult;
    }

    //在线冲正发送
    public int OnLine_RecordDispel_Send() {
        int n = 0;
        int cResult;
        long lngTemp;
        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n = n + 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x0d;
        s_cSendDataBuf[n++] = 0x15;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号
        //账号
        lngTemp = g_LastRecordPayInfo.lngAccountID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x00FF0000) >> 16);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0xFF000000) >> 24);
        Log.e(TAG, String.format("在线冲正账号:%d", lngTemp));
        //站点号
        lngTemp = g_StationInfo.iStationID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        //时间段序号
        s_cSendDataBuf[n++] = g_WorkInfo.cBusinessID;
        //交易类型(代扣未第三方交易) 0，一卡通交易，1，第三方交易
        if (g_LastRecordPayInfo.cType == 0)
            s_cSendDataBuf[n++] = 0;
        else
            s_cSendDataBuf[n++] = 1;
        //交易钱包号
        s_cSendDataBuf[n++] = g_StationInfo.cWorkBurseID;
        //申请时间
        memcpy(s_cSendDataBuf, n, bCurrDateTime, 0, 6);
        n += 6;
        //订单号
        lngTemp = g_LastRecordPayInfo.lngOrderNum;//lngLastOrderNum
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x00FF0000) >> 16);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0xFF000000) >> 24);
        Log.d(TAG, String.format("卡在线冲正的订单号:%d", lngTemp));

        n += 16;
        cResult = SendDataBuf(n, s_cSendDataBuf);
        if (cResult != OK) {
            return COMMUCATION_TIMEOUT;
        }
        return OK;
    }

    //在线冲正接收
    public int OnLine_RecordDispel_Recv(byte[] bytesTemp) {
        int t;
        int cResult = 0;
        long lngTemp;

        LastRecordPayInfo LastRecordPayInfo = new LastRecordPayInfo();
        //账号
        t = 9;
        lngTemp = (bytesTemp[t++] & 0xff)
                + (bytesTemp[t++] & 0xff) * 256
                + (bytesTemp[t++] & 0xff) * 256 * 256
                + ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256;
        LastRecordPayInfo.lngAccountID = lngTemp;
        //订单号
        lngTemp = (bytesTemp[t++] & 0xff)
                + (bytesTemp[t++] & 0xff) * 256
                + (bytesTemp[t++] & 0xff) * 256 * 256
                + ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256;
        LastRecordPayInfo.lngOrderNum = lngTemp;
        Log.d(TAG, String.format("冲正结果：%d:%d,%d", bytesTemp[8], LastRecordPayInfo.lngOrderNum, g_LastRecordPayInfo.lngOrderNum));
        if (bytesTemp[8] == 0) {
            if (LastRecordPayInfo.lngOrderNum == g_LastRecordPayInfo.lngOrderNum)
                cResult = OK;
            else
                cResult = DISPELFAIL;
        } else if (bytesTemp[8] == 1)//已冲正
            cResult = HASDISPEL;
        else
            cResult = DISPELFAIL;
        //冲正金额
        g_WorkInfo.lngReWorkPayMoney = g_LastRecordPayInfo.lngPayMoney - g_LastRecordPayInfo.lngManageMoney;
        g_WorkInfo.lngReChasePayMoney = 0;
        return cResult;
    }

    //在线冲正流程
    public int OnLine_RecordDispel_Process() {
        int cResult = 0;

        if (cResult == 0) {
            byte[] bytesTemp = new byte[512];
            System.arraycopy(s_cRecvDataBuf_N, 0, bytesTemp, 0, bytesTemp.length);
            Message message = Message.obtain();
            message.obj = bytesTemp;
            message.what = EVT_ONLRDispel;
            SendHandler.sendMessage(message);
        }
        return cResult;
    }

    //第三方代扣发送
    public int ThirdWithHold_Send() {
        int n = 0;
        int cResult;
        long lngTemp;
        byte[] cPayDatetime = new byte[4];
        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n = n + 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x0d;
        s_cSendDataBuf[n++] = 0x10;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号
        //账号
        lngTemp = g_CardBasicInfo.lngAccountID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x00FF0000) >> 16);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0xFF000000) >> 24);
        Log.d(TAG, String.format("代扣发送账号:%d", g_CardBasicInfo.lngAccountID));
        //站点号
        lngTemp = g_StationInfo.iStationID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        //终端机号
        s_cSendDataBuf[n++] = 0x00;
        //时间段序号	1字节
        s_cSendDataBuf[n++] = g_WorkInfo.cBusinessID;
        //商户号
        lngTemp = g_LocalInfo.wShopUserID;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        //交易类型
        s_cSendDataBuf[n++] = 0x00;
        //交易钱包号	1字节
        s_cSendDataBuf[n++] = g_StationInfo.cWorkBurseID;
        //交易时间	4字节	年6位 月4位 日 5位 小时5位 分6位 秒6位
        Publicfun.GetCurrCardDate(cPayDatetime);
        memcpy(s_cSendDataBuf, n, cPayDatetime, 0, 4);
        n = n + 4;
        //交易金额
        if (g_LocalInfo.cDockposFlag == 1) {
            SerialWorkTask.setPayMoney();
        }
        lngTemp = g_WorkInfo.lngPaymentMoney;
        s_cSendDataBuf[n++] = (byte) (lngTemp & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF00) >> 8);
        if (g_WorkInfo.cPlatAppVerson == 1)//大金额
            s_cSendDataBuf[n++] = (byte) ((lngTemp & 0x0000FF0000) >> 16);
        //出纳员编号
        s_cSendDataBuf[n++] = 0x00;
        s_cSendDataBuf[n++] = 0x00;
        //条码内容长度
        s_cSendDataBuf[n++] = (byte) (g_ThirdQRCodeInfo.iSignLen & 0x00ff);
        s_cSendDataBuf[n++] = (byte) ((g_ThirdQRCodeInfo.iSignLen & 0xff00) >> 8);
        //条码内容
        if (g_ThirdQRCodeInfo.iSignLen != 0) {
            //第三方签名	X字节	第三方的管控sdk签名数据
            memcpy(s_cSendDataBuf, n, g_ThirdQRCodeInfo.cSign, 0, g_ThirdQRCodeInfo.iSignLen);
        }
        n = n + g_ThirdQRCodeInfo.iSignLen;
        //预留字段10字节
        //memset(s_cSendDataBuf+n,0,10);
        n = n + 10;

        cResult = SendDataBuf(n, s_cSendDataBuf);
        if (cResult != OK) {
            return COMMUCATION_TIMEOUT;
        }
//        g_LastDaikouInfo.lngAccID=g_CardBasicInfo.lngAccountID;
//        memcpy(g_LastDaikouInfo.cPayTime,cPayDatetime,4);

        return OK;
    }

    //第三方代扣接收
    public int ThirdWithHold_Recv(byte[] bytesTemp) {
        int t;
        long lngAccount;
        long lngTemp;
        int cResult = 0;

        cResult = bytesTemp[8];
        Log.d(TAG, "第三方代扣结果:" + cResult);
        if (bytesTemp[8] == 0) {
            //账号
            t = 9;
            lngTemp = (bytesTemp[t++] & 0xff)
                    + (bytesTemp[t++] & 0xff) * 256
                    + (bytesTemp[t++] & 0xff) * 256 * 256
                    + ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256;
            g_ThirdCodeResultInfo.lngAccountID = lngTemp;
            //交易钱包号	1字节 13
            g_ThirdCodeResultInfo.cBurseID = bytesTemp[t++];
            //钱包余额 3字节
            lngTemp = bytesTemp[t++];
            lngTemp = lngTemp + bytesTemp[t++] * 256;
            lngTemp = lngTemp + bytesTemp[t++] * 256 * 256;
            g_ThirdCodeResultInfo.lngBurseMoney = (int) lngTemp;
            //交易金额 2字节
            t = 17;
            lngTemp = bytesTemp[t++];
            lngTemp = lngTemp + bytesTemp[t++] * 256;
            if (g_WorkInfo.cPlatAppVerson == 1)//大金额
                lngTemp = lngTemp + (bytesTemp[t++] << 16);
            g_ThirdCodeResultInfo.lngPayMoney = lngTemp;
            //优惠金额 2字节
            lngTemp = bytesTemp[t++];
            lngTemp = lngTemp + bytesTemp[t++] * 256;
            if (g_WorkInfo.cPlatAppVerson == 1)//大金额
                lngTemp = lngTemp + (bytesTemp[t++] << 16);
            g_ThirdCodeResultInfo.wPrivelegeMoney = (int) lngTemp;
            //消费管理费 2字节
            lngTemp = bytesTemp[t++];
            lngTemp = lngTemp + bytesTemp[t++] * 256;
            if (g_WorkInfo.cPlatAppVerson == 1)//大金额
                lngTemp = lngTemp + (bytesTemp[t++] << 16);
            g_ThirdCodeResultInfo.wManageMoney = (int) lngTemp;
            //姓名 16字节
            memcpy(g_ThirdCodeResultInfo.cAccName, 0, bytesTemp, t, 16);
            t += 16;
            //个人编号	16字节
            memcpy(g_ThirdCodeResultInfo.cPerCode, 0, bytesTemp, t, 16);
            t += 16;
            //支付类型
            g_ThirdCodeResultInfo.cPaymentType = bytesTemp[t++];
            Log.d(TAG, "获取第三方代扣结果接收成功：" + g_ThirdCodeResultInfo.cPaymentType);
            //末笔冲正用订单号
            lngTemp = (bytesTemp[t++] & 0xff)
                    + (bytesTemp[t++] & 0xff) * 256
                    + (bytesTemp[t++] & 0xff) * 256 * 256
                    + ((long) bytesTemp[t++] & 0xff) * 256 * 256 * 256;
            g_ThirdCodeResultInfo.lngLastOrderNum = lngTemp;

            return OK;
        } else {
            return cResult;
        }
    }

    //代扣流程
    public int OnLine_ThirdWithHold_Process() {
        int cResult = 0;

        if (cResult == 0) {
            byte[] bytesTemp = new byte[512];
            System.arraycopy(s_cRecvDataBuf_N, 0, bytesTemp, 0, bytesTemp.length);
            Message message = Message.obtain();
            message.obj = bytesTemp;
            message.what = EVT_Withhold;
            SendHandler.sendMessage(message);
        }
        return cResult;
    }


    //操作员校验发送
    public int OptorKeyCheck_Send(byte[] cCasherSerID, long lngCasherPsw) {
        int n = 0;
        int t;
        int cResult;
        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x0d;
        s_cSendDataBuf[n++] = 0x02;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号

        //钥匙卡号
        Log.i(TAG, String.format("操作员卡号C：%02x.%02x.%02x.%02x", cCasherSerID[0], cCasherSerID[1], cCasherSerID[2], cCasherSerID[3]));

        System.arraycopy(cCasherSerID, 0, s_cSendDataBuf, n, 4);
        n = n + 4;

        //密码
        s_cSendDataBuf[n++] = (byte) (lngCasherPsw & 0x000000FF);
        s_cSendDataBuf[n++] = (byte) ((lngCasherPsw & 0x0000FF00) >> 8);
        s_cSendDataBuf[n++] = (byte) ((lngCasherPsw & 0x00FF0000) >> 16);
        s_cSendDataBuf[n++] = (byte) ((lngCasherPsw & 0xFF000000) >> 24);

        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "操作员校验发送");
        SendDataBuf(n, SendBytes);
        return OK;
    }

    //操作员校验接收
    public int OptorKeyCheck_Recv() {
        int i;
        int cResult = 0;

        //结果标记
        cResult = s_cRecvDataBuf_N[12];
        if (cResult == 0) {
            System.arraycopy(s_cRecvDataBuf_N, 13, g_WorkInfo.cCasherID, 0, 2);
            Log.i(TAG, String.format("出纳员编号:%02x.%02x", g_WorkInfo.cCasherID[0], g_WorkInfo.cCasherID[1]));
            return OK;
        } else {
            return cResult;
        }
    }

    //上传交易流水(按范围)发送
    public int UpSendPaymentRecord_Send(long lngStartRecordID, long lngEndRecordID) {
        int n = 0;
        int cResult;
        long lngTempIndex;
        int cPayRecordNum = 0;
        byte[] bReadDataBuf = new byte[2048];        //读出的内容

        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x0f;
        s_cSendDataBuf[n++] = 0x02;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号

        //报文重传标志
        s_cSendDataBuf[n++] = 0;
        //交易流水数量
        s_cSendDataBuf[n++] = (byte) (lngEndRecordID - lngStartRecordID);
        cPayRecordNum = (int) (lngEndRecordID - lngStartRecordID);
        //报文内容
        for (lngTempIndex = lngStartRecordID + 1; lngTempIndex <= lngEndRecordID; lngTempIndex++) {
            Log.i(TAG, "上传流水指针lngTempIndex:" + lngTempIndex);
            cResult = WasteBooksRW.ReadPayRecordsData(bReadDataBuf, lngTempIndex, 1);
            if (cResult != OK) {
                Log.i(TAG, "读取流水记录失败:" + cResult);
                return cResult;
            } else {
                //写流水重传标记
                if (lngTempIndex == lngStartRecordID + 1) {
                    s_lngStartRecordID = (bReadDataBuf[2] & 0xff)
                            + (bReadDataBuf[3] & 0xff) * 256
                            + (bReadDataBuf[4] & 0xff) * 256 * 256;
                }
            }
            System.arraycopy(bReadDataBuf, 0, s_cSendDataBuf, n, PAYMENTRECORD_LEN + 2);
            n = n + PAYMENTRECORD_LEN + 2;
        }
        //记录上次流水的流水号(20171228)
        //s_lngStartRecordID=lngStartRecordID;

        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        SendDataBuf(n, SendBytes);
        return OK;
    }

    //上传交易流水(按范围)接收
    public int UpSendPaymentRecord_Recv() {
        int i;
        int t;
        int cResult;
        long lngPaymentRecordID;//第一笔流水的终端流水号	3

        /*
        报文时间	6字节
        命令代码	2字节	＝0x0F02
        成功标志	1字节	0成功 1失败
        流水笔数	1字节
        第一笔流水的终端机号	1
        第一笔流水的终端流水号	3
        第一笔流水的终端交易时间	4
         **/

        t = 11;
        lngPaymentRecordID = (s_cRecvDataBuf_N[t++] & 0xff)
                + (s_cRecvDataBuf_N[t++] & 0xff) * 256
                + (s_cRecvDataBuf_N[t++] & 0xff) * 256 * 256;

        if (s_lngRecvRecordID == lngPaymentRecordID) {
            Log.i(TAG, "重复接收到的流水号:" + lngPaymentRecordID);
            //return 2;
        } else {
            s_lngRecvRecordID = lngPaymentRecordID;
        }

        //if((s_cRecvDataBuf_N[8]==0)&&(lngPaymentRecordID==(s_lngStartRecordID+1)))
        if (s_cRecvDataBuf_N[8] == 0) {
            if (lngPaymentRecordID != (s_lngStartRecordID)) {
                Log.e(TAG, "返回的发送流水号不正确-" + lngPaymentRecordID);
            }
            g_WasteBookInfo.TransferIndex = g_WorkInfo.lngEndRecordID;
            cResult = WasteBooksRW.WriteWasteBookInfo(g_WasteBookInfo);
            if (cResult == OK) {
                Log.i(TAG, String.format("已记录流水号:%d,已上传流水号:%d", g_WasteBookInfo.WriterIndex, g_WasteBookInfo.TransferIndex));
            } else {
                Log.i(TAG, "写参数数据失败:" + cResult);
                g_WorkInfo.cRecordErrFlag = 1;
                return MEMORY_FAIL;
            }
            return OK;
        } else {
            Log.e(TAG, String.format("接收流水返回数据失败:%d-%d", s_cRecvDataBuf_N[8], lngPaymentRecordID));
            return 1;
        }
    }

    //取参数版本
    public int GetParaVerInfo() {
        int n = 8;
        /*
        for (int i = 0; i < 4; i++) {
                VersionInfo.bSystemVer[i] = 0x00;
                VersionInfo.bStationVer[i] = 0x00;
                VersionInfo.bBurseVer[i] = 0x00;
                VersionInfo.bOddKeyVer[i] = 0x00;
                VersionInfo.bStatusVer[i] = 0x00;
                VersionInfo.bBusinessVer[i] = 0x00;
                VersionInfo.bStatusPrivVer[i] = 0x00;
            }
        */
        //系统参数版本
        System.arraycopy(g_VersionInfo.bSystemVer, 0, bContextData, n, 4);
        n = n + 4;
        //站点参数版本
        System.arraycopy(g_VersionInfo.bStationVer, 0, bContextData, n, 4);
        n = n + 4;
        //钱包参数版本
        System.arraycopy(g_VersionInfo.bBurseVer, 0, bContextData, n, 4);
        n = n + 4;
        //单键参数版本
        System.arraycopy(g_VersionInfo.bOddKeyVer, 0, bContextData, n, 4);
        n = n + 4;
        //身份参数版本
        System.arraycopy(g_VersionInfo.bStatusVer, 0, bContextData, n, 4);
        n = n + 4;
        //营业分组参数版本
        System.arraycopy(g_VersionInfo.bBusinessVer, 0, bContextData, n, 4);
        n = n + 4;
        //身份优惠版本
        System.arraycopy(g_VersionInfo.bStatusPrivVer, 0, bContextData, n, 4);
        n = n + 4;
        return n;
    }

    //取黑白名单版本
    public int GetBWListVerInfo() {
        int n = 8;
        long lngTemp;

//        for (int i = 0; i < 4; i++) {
//            g_VersionInfo.bInitListVer[i] = 0x00;
//            g_VersionInfo.bChangeListVer[i] = 0x00;
//            g_BlackWList.BlackWListInfo.Version[i] = 0x00;
//        }

        //初始名单版本g_BlackWList.BlackWListInfo.Version g_VersionInfo.bInitListVer
        System.arraycopy(g_BlackWList.BlackWListInfo.Version, 0, bContextData, n, 4);
        n = n + 4;
        //变更名单版本
        System.arraycopy(g_BlackWList.BlackWListInfo.Version, 0, bContextData, n, 4);
        n = n + 4;
        //变更名单数量
        lngTemp = g_BlackWList.BlackWListInfo.lngBWCount;
        int iTemp = 0;
        byte[] strByte = DataTransfer.int2Bytes(iTemp, 2);
        System.arraycopy(strByte, 0, bContextData, n, 2);
        n += 2;

        return n;
    }

    //取设备状态
    public int GetStationState() {
        int n = 8;
        long lngTemp;
        byte[] strByte = new byte[4];

        /*
        for (int i = 0; i < 4; i++) {
                VersionInfo.bSystemVer[i] = 0x00;
                VersionInfo.bStationVer[i] = 0x00;
                VersionInfo.bBurseVer[i] = 0x00;
                VersionInfo.bOddKeyVer[i] = 0x00;
                VersionInfo.bStatusVer[i] = 0x00;
                VersionInfo.bBusinessVer[i] = 0x00;
                VersionInfo.bStatusPrivVer[i] = 0x00;
            }
        */

        //站点号
        lngTemp = g_StationInfo.iStationID;
        strByte = DataTransfer.int2Bytes((int) lngTemp, 2);
        System.arraycopy(strByte, 0, bContextData, n, 2);
        n = n + 2;
        //系统参数版本
        System.arraycopy(g_VersionInfo.bSystemVer, 0, bContextData, n, 4);
        n = n + 4;
        //站点参数版本
        System.arraycopy(g_VersionInfo.bStationVer, 0, bContextData, n, 4);
        n = n + 4;
        //钱包参数版本
        System.arraycopy(g_VersionInfo.bBurseVer, 0, bContextData, n, 4);
        n = n + 4;
        //单键参数版本
        System.arraycopy(g_VersionInfo.bOddKeyVer, 0, bContextData, n, 4);
        n = n + 4;
        //身份参数版本
        System.arraycopy(g_VersionInfo.bStatusVer, 0, bContextData, n, 4);
        n = n + 4;
        //营业分组参数版本
        System.arraycopy(g_VersionInfo.bBusinessVer, 0, bContextData, n, 4);
        n = n + 4;
        //身份优惠版本
        System.arraycopy(g_VersionInfo.bStatusPrivVer, 0, bContextData, n, 4);
        n = n + 4;

        //初始名单版本
        //System.arraycopy(bContextData+n,g_BlackWList.BlackWListInfo.Version,4);
        System.arraycopy(g_BlackWList.BlackWListInfo.Version, 0, bContextData, n, 4);
        n = n + 4;
        //变更名单版本
        //System.arraycopy(bContextData+n,g_BlackWList.BlackWListInfo.Version,4);
        System.arraycopy(g_BlackWList.BlackWListInfo.Version, 0, bContextData, n, 4);
        n = n + 4;

        //未上传流水笔数
        lngTemp = g_RecordInfo.lngPaymentRecordID - g_RecordInfo.lngPaymentSendID;
        strByte = DataTransfer.int2Bytes((int) lngTemp, 4);
        System.arraycopy(strByte, 0, bContextData, n, 4);
        n = n + 4;

        //当餐营业笔数 2字节
        lngTemp = g_RecordInfo.wTotalBusinessSum;
        strByte = DataTransfer.int2Bytes((int) lngTemp, 2);
        System.arraycopy(strByte, 0, bContextData, n, 2);
        n = n + 2;

        //当餐营业总额	4字节
        lngTemp = g_RecordInfo.lngTotalBusinessMoney;
        strByte = DataTransfer.int2Bytes((int) lngTemp, 4);
        System.arraycopy(strByte, 0, bContextData, n, 4);
        n = n + 4;

        //当日累计次数	2字节
        lngTemp = g_RecordInfo.wTodayPaymentSum;
        strByte = DataTransfer.int2Bytes((int) lngTemp, 2);
        System.arraycopy(strByte, 0, bContextData, n, 2);
        n = n + 2;

        //当日累计金额	4字节
        lngTemp = g_RecordInfo.lngTodayPaymentMoney;
        strByte = DataTransfer.int2Bytes((int) lngTemp, 4);
        System.arraycopy(strByte, 0, bContextData, n, 4);
        n = n + 4;

        return n;
    }

    //设置系统工作参数
    public int SetSystemInfo() {
        int cAgentID;
        int iGuestID;
        int cResult;

        cAgentID = (s_cRecvDataBuf_P[12] & 0xff);
        iGuestID = (s_cRecvDataBuf_P[13] & 0xff) + (s_cRecvDataBuf_P[14] & 0xff) * 256;
        Log.i(TAG, String.format("下发代理号:%d,下发的客户号:%d", cAgentID, iGuestID));
        Log.i(TAG, String.format("已有的代理号:%d,已有的客户号:%d", g_BasicInfo.cAgentID, g_BasicInfo.iGuestID));

        if (((g_BasicInfo.cAgentID != 0) && (cAgentID != g_BasicInfo.cAgentID))
                || ((g_BasicInfo.iGuestID != 0) && (iGuestID != g_BasicInfo.iGuestID))) {
            Log.i(TAG, "代理号或客户号不正确");
            return 4;
        }
        //写系统参数数据
        byte[] bytsTemp = new byte[512];
        System.arraycopy(s_cRecvDataBuf_P, 8, bytsTemp, 0, bytsTemp.length);
        cResult = SystemInfoRW.SaveSystemInfo(bytsTemp);
        if (cResult != OK) {
            Log.i(TAG, "写参数数据失败:%d" + cResult);
            return cResult;
        }
        //读系统工作参数
        g_SystemInfo = SystemInfoRW.ReadSystemInfo();
        //更新版本号
        System.arraycopy(g_SystemInfo.cVersion, 0, g_VersionInfo.bSystemVer, 0, 4);

        //判断是否需要启动第三方管控SDK
        if (g_SystemInfo.cAliPayCtlSDK == 1) {
            //判断是否存在/home/alipay/alipay_net.conf
//            if(access("/home/alipay/alipay_net.conf",F_OK)!=-1)//access函数是查看文件是不是存在
//            {
//                Log.i(TAG,"/home/alipay/alipay_net.conf 存在 ");
//                //写代理服务器配置文件
//                Publicfun.WriteProxyServerFile("/home/alipay/alipay_net.conf");
//            }
        }
        g_WorkInfo.iSelfDownPara = g_WorkInfo.iSelfDownPara | 0x0002;
        return OK;
    }

    //设置站点工作参数
    public int SetStationInfo() {
        int cResult;
        int iStationID, iStationClass;

        //判断站点有无变化
        iStationID = ((s_cRecvDataBuf_P[12] & 0xff) + ((s_cRecvDataBuf_P[13] & 0xff) << 8));
        Log.i(TAG, String.format("下发站点号:%d,已有站点号:%d", iStationID, g_StationInfo.iStationID));
        if ((g_StationInfo.iStationID != 0) && (iStationID != g_StationInfo.iStationID)) {
            Log.i(TAG, "下发站点号和已有站点号不一致");
            return 4;
        }

        //判断设备类型有无变化
        iStationClass = ((s_cRecvDataBuf_P[14] & 0xff) + ((s_cRecvDataBuf_P[15] & 0xff) << 8));
        Log.i(TAG, String.format("下发类型号:%d,已有类型号:%d", iStationClass, g_StationInfo.iStationClass));
        if ((g_StationInfo.iStationClass != 0) && (iStationClass != g_StationInfo.iStationClass)) {
            Log.i(TAG, "下发类型号和已有类型号不一致");
            return 4;
        }

        //写站点参数数据
        byte[] bytsTemp = new byte[512];
        System.arraycopy(s_cRecvDataBuf_P, 8, bytsTemp, 0, bytsTemp.length);
        cResult = StationInfoRW.SaveStationInfo(bytsTemp);
        if (cResult != OK) {
            Log.i(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }

        //读站点工作参数
        g_StationInfo = StationInfoRW.ReadStationInfo();
        Log.d(TAG, "站点工作参数:" + g_StationInfo.toString());
        //更新版本号
        System.arraycopy(g_StationInfo.cVersion, 0, g_VersionInfo.bStationVer, 0, 4);

        //2016.10.17系统下发商户号不为0,使用系统下发商户号
        if (g_StationInfo.iShopUserID != 0) {
            g_LocalInfo.wShopUserID = g_StationInfo.iShopUserID;
        }
        g_WorkInfo.iSelfDownPara = g_WorkInfo.iSelfDownPara | 0x0004;
        return 0;
    }

    //清除钱包工作参数
    public int ClearBurseInfo() {
        int cResult;

        cResult = BurseInfoRW.ClearAllPurseInfo();
        if (cResult != 0) {
            //ShowErrorInfoA(59);
        }
        return 0;
    }

    //设置钱包工作参数
    public int SetBurseInfo() {
        int i;
        int cResult;
        int iBurseCount;

        iBurseCount = (s_cRecvDataBuf_P[8] & 0xff);
        Log.i(TAG, "iBurseCount:" + iBurseCount);

        //写电子钱包数据
        byte[] bytsTemp = new byte[512];
        System.arraycopy(s_cRecvDataBuf_P, 8, bytsTemp, 0, bytsTemp.length);
        cResult = BurseInfoRW.SaveInitPurseInfo(bytsTemp);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }
        return 0;
    }

    //下载钱包完成
    public int DownBurseOver() {
        int cResult;

        //写钱包版本号
        System.arraycopy(s_cRecvDataBuf_P, 8, g_VersionInfo.bBurseVer, 0, 4);
        cResult = BurseInfoRW.WriteBurseVer(g_VersionInfo.bBurseVer);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }
        //读钱包工作参数
        Log.i(TAG, "读取钱包参数\n");
        g_EP_BurseInfo = BurseInfoRW.ReadAllBurseInfo();

        g_WorkInfo.iSelfDownPara = g_WorkInfo.iSelfDownPara | 0x0010;
        return 0;
    }

    //清除单键金额参数
    int ClearOddKeyInfo() {
        Log.i(TAG, "清除单键金额参数");
        return 0;
    }

    //设置单键金额参数
    public int SetOddKeyInfo() {
        int cResult;

        byte[] bytsTemp = new byte[512];
        System.arraycopy(s_cRecvDataBuf_P, 8, bytsTemp, 0, bytsTemp.length);
        cResult = OddKeyInfoRW.SaveInitOddKeyInfo(bytsTemp);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }
        return OK;
    }

    //下载单键完成
    public int DownOddKeyOver() {
        int cResult;

        //写单键版本号
        System.arraycopy(s_cRecvDataBuf_P, 8, g_VersionInfo.bOddKeyVer, 0, 4);
        cResult = OddKeyInfoRW.WriteOddKeyVer(g_VersionInfo.bOddKeyVer);
        if (cResult != OK) {
            Log.e(TAG, "写参数版本失败:%d" + cResult);
            return cResult;
        }

        //读取单键参数
        Log.i(TAG, "读取单键参数\n");
        g_OddKeyInfo = OddKeyInfoRW.ReadOddKeyInfo();

        g_WorkInfo.iSelfDownPara = g_WorkInfo.iSelfDownPara | 0x0020;
        return OK;
    }

    //清除帐户身份参数
    public int ClearStatusInfo() {
        int i;

        Log.i(TAG, "清除帐户身份参数");
        for (i = 0; i < 64; i++) {
            cStatusState[i] = 0x00;
        }
        return 0;
    }

    //设置帐户身份参数
    public int SetStatusInfo() {
        int i;
        int cResult;
        int iStatusID;

        iStatusID = (s_cRecvDataBuf_P[8] & 0xff);
        Log.i(TAG, "身份号:" + iStatusID);
        if ((iStatusID > 64) || (iStatusID < 1)) {
            Log.i(TAG, "身份号超过范围");
            return 1;
        }
        byte[] bytsTemp = new byte[1024];
        System.arraycopy(s_cRecvDataBuf_P, 8, bytsTemp, 0, bytsTemp.length);
        cResult = StatusBurInfoRW.SaveInitStatusBurInfo(bytsTemp);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }
        cStatusState[iStatusID - 1] = 1;
        return 0;
    }

    //下载身份完成
    public int DownStatusOver() {
        int i;
        int cResult;

        Log.i(TAG, "清除无效的身份参数");
        for (i = 0; i < 64; i++) {
            if (cStatusState[i] != 1) {
                //Log.i(TAG,"清除无效的身份参数:%d",i+1);
                cResult = StatusBurInfoRW.ClearStatusBur(i + 1);
                if (cResult != OK) {
                    Log.i(TAG, "写参数数据失败:" + cResult);
                    return cResult;
                }
            }
        }

        //写版本号
        System.arraycopy(s_cRecvDataBuf_P, 8, g_VersionInfo.bStatusVer, 0, 4);
        cResult = StatusBurInfoRW.WriteStatusBurVer(g_VersionInfo.bStatusVer);
        if (cResult != OK) {
            Log.e(TAG, "写身份参数数据失败:" + cResult);
            return cResult;
        }
        //读取身份钱包参数
        Log.i(TAG, "读取所有的身份钱包参数");
        g_StatusInfoArray = StatusBurInfoRW.ReadAllStatusBurInfo();

        g_WorkInfo.iSelfDownPara = g_WorkInfo.iSelfDownPara | 0x0040;
        return OK;
    }

    //清空身份优惠
    public int ClearStatusPriv() {
        int i;
        Log.i(TAG, "清空身份优惠参数");
        for (i = 0; i < 64; i++) {
            cStatusPriState[i] = 0x00;
        }
        return 0;
    }

    //设置身份优惠
    public int SetStatusPrivInfo() {
        int j = 0;
        int i = 0;
        int cResult;
        int iStatusID;
        int iStatusPrivCount;

    /*
    数量	1字节	<=40

    身份号	1字节
    优惠时段范围	16字节
    校验码	2字节
    ……
     */

        j = 0;
        iStatusPrivCount = (s_cRecvDataBuf_P[8] & 0xff);
        Log.i(TAG, "身份优惠参数下载数量:" + iStatusPrivCount);

        //写身份优惠参数
        byte[] bytsTemp = new byte[512];
        System.arraycopy(s_cRecvDataBuf_P, 8, bytsTemp, 0, bytsTemp.length);
        cResult = StatusPriInfoRW.SaveInitStatusPri(bytsTemp);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }

        for (i = 0; i < iStatusPrivCount; i++) {
            iStatusID = (s_cRecvDataBuf_P[9 + i * 19] & 0xff);
            Log.i(TAG, "写身份优惠号:" + iStatusID);
            cStatusPriState[iStatusID - 1] = 1;
        }
        return OK;
    }

    //下载身份优惠完成
    public int DownStatusPrivOver() {
        int i;
        int cResult;

        Log.i(TAG, "清除无效的身份优惠参数");
        for (i = 0; i < 64; i++) {
            if (cStatusPriState[i] != 1) {
                //Log.i(TAG,"清除无效的身份参数:"+(i+1));
                cResult = StatusPriInfoRW.ClearStatusPriInfo((byte) (i));
                if (cResult != OK) {
                    Log.e(TAG, "写参数数据失败:" + cResult);
                    return cResult;
                }
            }
        }
        //写版本号
        System.arraycopy(s_cRecvDataBuf_P, 8, g_VersionInfo.bStatusPrivVer, 0, 4);
        cResult = StatusPriInfoRW.WriteStatusPriVer(g_VersionInfo.bStatusPrivVer);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }

        //读取身份优惠参数
        Log.i(TAG, "读取身份优惠参数");
        g_StatusPriInfoArray = StatusPriInfoRW.ReadAllStatusPriInfo();

        g_WorkInfo.iSelfDownPara = g_WorkInfo.iSelfDownPara | 0x0100;
        return 0;
    }

    //清空营业分组
    public int ClearBusinessInfo() {
        int i;
        for (i = 0; i < 128; i++) {
            cBusinessState[i] = 0x00;
        }
        return 0;
    }

    //设置营业分组
    public int SetBusinessInfo() {
        int i = 0;
        int iBusinessID = 0;
        int iBusinessCount = 0;
        int cResult;
    /*
    数量	1字节

    时间段序号	1字节
    起始时间	2字节
    结束时间	2字节
    校验码	2字节
    ……
     **/
        iBusinessCount = (s_cRecvDataBuf_P[8] & 0xff);
        iBusinessID = (s_cRecvDataBuf_P[9] & 0xff);

        byte[] bytsTemp = new byte[512];
        System.arraycopy(s_cRecvDataBuf_P, 8, bytsTemp, 0, bytsTemp.length);
        cResult = BuinessInfoRW.SaveInitBuinessInfo(bytsTemp);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }

        for (i = 0; i < iBusinessCount; i++) {
            iBusinessID = (s_cRecvDataBuf_P[9 + i * 7] & 0xff);
            if (iBusinessID > 128) {
                Log.i(TAG, "营业号超过范围");
                return 1;
            }
            cBusinessState[iBusinessID - 1] = 1;
        }
        return 0;
    }

    //下在营业分组完成
    public int DownBusinessOver() {
        int i;
        int cResult;

        //写营业分组版本号
        System.arraycopy(s_cRecvDataBuf_P, 8, g_VersionInfo.bBusinessVer, 0, 4);
        cResult = BuinessInfoRW.WriteBuinessVer(g_VersionInfo.bBusinessVer);
        if (cResult != OK) {
            Log.i(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }

        Log.i(TAG, "清除无效的营业分组参数");
        for (i = 0; i < 128; i++) {
            if (cBusinessState[i] != 1) {
                //Log.i(TAG,"清除无效的营业分组参数:%d",i+1);
                cResult = BuinessInfoRW.ClearBuinessInfo((byte) i);
                if (cResult != OK) {
                    Log.e(TAG, "写参数数据失败:" + cResult);
                    return cResult;
                }
            }
        }
        //读取营业分组参数
        Log.i(TAG, "读取营业分组参数");
        g_BuinessInfo = BuinessInfoRW.ReadAllBuinessInfo();
        g_WorkInfo.iSelfDownPara = g_WorkInfo.iSelfDownPara | 0x0080;
        return 0;
    }

    //清除黑白名单
    public int ClearAllBWList() {
        Log.i(TAG, "清除黑白名单内存数据");
        s_lngInitBWListSum = 0;
        s_iChangeBWListSum = 0;
        return 0;
    }

    //初始白名单序列号
    public int SetInitBWList() {
        int cResult;
        int iInitListSum;   //单包黑白名单字节数量
        long lngStartPos;    //存储地址

        /*
        黑白名单字节数量	2字节
        存储地址	4字节
        黑白名单bit位字节	n字节
        */
        iInitListSum = ((s_cRecvDataBuf_P[8] & 0xff) + (s_cRecvDataBuf_P[9] & 0xff) * 256);

        lngStartPos = (s_cRecvDataBuf_P[10] & 0xff);
        lngStartPos = lngStartPos + (s_cRecvDataBuf_P[11] & 0xff) * 256;
        lngStartPos = lngStartPos + (s_cRecvDataBuf_P[12] & 0xff) * 256 * 256;
        lngStartPos = lngStartPos + ((long) s_cRecvDataBuf_P[13] & 0xff) * 256 * 256 * 256;

        s_lngInitBWListSum = s_lngInitBWListSum + iInitListSum * 8;

        Log.i(TAG, "开始地址:" + lngStartPos + " 单包初始数量:" + iInitListSum * 8 + " 初始总数量:" + s_lngInitBWListSum);

        //写初始BIT黑白名单数据
        byte[] bytsTemp = new byte[1024];
        System.arraycopy(s_cRecvDataBuf_P, 8, bytsTemp, 0, bytsTemp.length);
        cResult = BWListInfoRW.SaveInitBitBWList(bytsTemp, g_BlackWList.BlackBit);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:%d" + cResult);
            return cResult;
        }
        return OK;
    }

    //初始名单下载结束
    public int DownInitListOver() {
        int cResult;
        long lngInitBWListSum;
        /*
        初始黑白名单版本号	4字节
        初始黑名单数量	4字节	下载的字节总数量
        */

        lngInitBWListSum = (s_cRecvDataBuf_P[12] & 0xff);
        lngInitBWListSum = lngInitBWListSum + (s_cRecvDataBuf_P[13] & 0xff) * 256;
        lngInitBWListSum = lngInitBWListSum + (s_cRecvDataBuf_P[14] & 0xff) * 256 * 256;
        lngInitBWListSum = lngInitBWListSum + ((long) s_cRecvDataBuf_P[15] & 0xff) * 256 * 256 * 256;

        //判断黑白名单数量
        Log.i(TAG, String.format("下载的初始数量:%d,计算初始总数量:%d", lngInitBWListSum, s_lngInitBWListSum));

        //更新黑白名单指针信息
        //写黑白名单数量
        g_BlackWList.BlackWListInfo.lngBWCount = lngInitBWListSum * 8;
        //写最大卡内编号
        g_BlackWList.BlackWListInfo.lngMaxCardID = lngInitBWListSum * 8;
        //写版本号
        System.arraycopy(s_cRecvDataBuf_P, 8, g_BlackWList.BlackWListInfo.Version, 0, 4);
        //初始名单版本g_BlackWList.BlackWListInfo.Version g_VersionInfo.bInitListVer
        System.arraycopy(g_BlackWList.BlackWListInfo.Version, 0, g_VersionInfo.bInitListVer, 0, 4);
        cResult = BWListInfoRW.WriteBWListInfo(g_BlackWList.BlackWListInfo);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }

        g_WorkInfo.iSelfDownPara = g_WorkInfo.iSelfDownPara | 0x0200;
        return OK;
    }

    //变更白名单序列号
    public int SetChangeBWList() {
        int cResult;
        int iChangeListSum;

        s_iChangeBWListSum = g_BlackWList.BlackWListInfo.iChangeBWListSum;

        iChangeListSum = (s_cRecvDataBuf_P[8] & 0xff);
        s_iChangeBWListSum = s_iChangeBWListSum + iChangeListSum;
        Log.i(TAG, String.format("变更数量:%d,总变更数量:%d", iChangeListSum, s_iChangeBWListSum));

        g_BlackWList.BlackWListInfo.iChangeBWListSum = (short) s_iChangeBWListSum;

        //写变更黑白名单文件数据
        byte[] bytsTemp = new byte[1024];
        System.arraycopy(s_cRecvDataBuf_P, 8, bytsTemp, 0, bytsTemp.length);
        cResult = BWListInfoRW.SaveChangeBYTEBWList(bytsTemp, g_BlackWList.BlackBit);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }
        return OK;
    }

    //下载变更黑白名单结束
    public int DownChangeListOver() {
        int cResult;

        //更新黑白名单指针信息

        //写版本号
        System.arraycopy(s_cRecvDataBuf_P, 8, g_BlackWList.BlackWListInfo.Version, 0, 4);
        System.arraycopy(s_cRecvDataBuf_P, 8, g_VersionInfo.bChangeListVer, 0, 4);

        cResult = BWListInfoRW.WriteBWListInfo(g_BlackWList.BlackWListInfo);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:%d" + cResult);
            return cResult;
        }

        g_WorkInfo.iSelfDownPara = g_WorkInfo.iSelfDownPara | 0x0400;
        return 0;
    }

    //获取ftp服务器信息
    public void GetFTPServerInfo() {
        if (g_BasicInfo.cSystemState != 100)
            return;

        if (g_WorkInfo.cRunState == 1)//联网并且无自动升级中
        {
            if (g_CommInfo.lngSendComStatus == 0) {
                Log.i(TAG, "向平台获取ftp服务器信息");
                g_CommInfo.lngSendComStatus |= 0x00004000;
            }
        }
    }

    //获取ftp服务器信息发送
    public int GetFTPServerInfo_Send() {
        int n = 0;
        int cResult;

        //报文时间
        byte[] bCurrDateTime = new byte[6];
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, s_cSendDataBuf, n, bCurrDateTime.length);
        n += 6;
        //命令代码
        s_cSendDataBuf[n++] = 0x14;
        s_cSendDataBuf[n++] = 0x01;
        s_wProtocolID = DataTransfer.byte2ShortBIG(s_cSendDataBuf, 6);//协议号

        Log.i(TAG, "发送获取ftp服务器信息请求");
        byte[] SendBytes = new byte[n];
        System.arraycopy(s_cSendDataBuf, 0, SendBytes, 0, n);
        Log.i(TAG, "获取ftp服务器信息发送");
        SendDataBuf(n, SendBytes);
        return OK;
    }

    //获取ftp服务器信息接收
    public int GetFTPServerInfo_Recv() {
        int iRet = 0;
        Log.i(TAG, "获取ftp服务器信息接收");
//        FTP服务器IP	4字节
//        FTP端口号	2字节	默认 21
//        FTP用户名	16字节	字符形式
//        FTP密码	16字节	字符形式
//        下载照片文件夹	16字节	字符形式，用户照片以账号命名，如“12345678.png”
//        上传照片文件夹	16字节	字符形式，抓拍照片以站点号+站点流水号+账号命名，如“6_1_12345678.png”

        //写ftp服务器信息数据
        byte[] bytsTemp = new byte[512];
        System.arraycopy(s_cRecvDataBuf_P, 8, bytsTemp, 0, bytsTemp.length);
        iRet = FTPInfoRW.SaveFTPServerInfo(bytsTemp);

        return iRet;
    }

    //下载应用程序结束
    public int DownAPPDataOver() {
        byte[] bAppVerContext = new byte[12];

        //程序版本号
        memcpy(bAppVerContext, 0, s_cRecvDataBuf_P, 9, 12);
        memcpy(g_BasicInfo.cAppSoftWareVer, bAppVerContext, 12);
        memcpy(g_WorkInfo.cConfigAppSWVer, bAppVerContext, 12);
        BasicInfoRW.WriteAllBasicInfo(g_BasicInfo);
        Log.d(TAG, String.format("程序下载版本号:%s", g_WorkInfo.cConfigAppSWVer));
        Log.d(TAG, "应用程序包下载成功");
        g_WorkInfo.cUpdateState = 0;
        if (g_BasicInfo.cSystemState == 100) {
            g_WorkInfo.cRunState = 2;
            g_WorkInfo.lngOffLineTime = 0;
        }
        return OK;

    }

    //下载工作参数结束
    public int DownParaOver() {
        int cResult;
        byte[] bCurrDateTime = new byte[6];

        if (g_BasicInfo.cSystemState != 100) {
            Log.i(TAG, "写系统状态");
            g_BasicInfo.cSystemState = 100;
            cResult = BasicInfoRW.WriteAllBasicInfo(g_BasicInfo);
            if (cResult != OK) {
                //ShowErrorInfoA(59);
                Log.i(TAG, "写系统参数数据失败:" + cResult);
                return cResult;
            }
        }
        //获取系统时钟
        Publicfun.GetCurrDateTime(bCurrDateTime);
        System.arraycopy(bCurrDateTime, 0, g_RecordInfo.cCurSignInDate, 0, 3);
        cResult = RecordInfoRW.WriteAllRecordInfo(g_RecordInfo);
        if (cResult != OK) {
            Log.e(TAG, "写参数数据失败:" + cResult);
            return cResult;
        }
        Log.i(TAG, "下载工作参数结束");
        //更新网络数据
        Publicfun.ReadAllDownParam();
        //判断是否是现金充值机
        if (LAN_EP_MONEYPOS == g_StationInfo.iStationClass) {
            Log.i(TAG, "现金充值机,刷操作员卡");
            Log.i(TAG, "=============================发射操作员卡校验数据==================================");
            if (gUIMainHandler != null)
                gUIMainHandler.sendEmptyMessage(DOWNPARAOVER);
        } else {
            //联机运行
            g_WorkInfo.cRunState = 1;
            g_WorkInfo.iBusinessCount = BUSINESS_TIME;    //置营业计数为30
            if (gUIMainHandler != null)
                gUIMainHandler.sendEmptyMessage(DOWNPARAOVER);
        }
        g_WorkInfo.cReConnectState = 0;
        g_WorkInfo.lngPowerSaveCnt = System.currentTimeMillis();
        return OK;
    }

    //联机达到一定数量上传未上传交易流水
    public void UpSendPayRecord() {
        if ((g_BasicInfo.cSystemState != 100)
                || (g_CommInfo.cRecvState != 0)//2017.0706 修改了lngEndRecordID被覆盖导致上传流水号>记录流水号的bug
                || (g_WasteBookInfo.WriterIndex <= g_WasteBookInfo.TransferIndex))//记录流水小于等于上传流水
            return;

        if (g_WorkInfo.cRunState == 1)//联网并且无自动升级中
        {
            if ((g_CommInfo.lngSendComStatus & 0x00000020) == 0x00000020) {
                //Log.i(TAG,"正在处理上传流水");
            } else {
                //判断流水打包数
                if ((g_WasteBookInfo.WriterIndex - g_WasteBookInfo.TransferIndex) > UPRECORDSUM) {
                    g_WorkInfo.cUpPamentSum = UPRECORDSUM;
                } else {
                    g_WorkInfo.cUpPamentSum = 1;
                }
                //判断记录流水指针和上传流水指针
                g_WorkInfo.lngStartRecordID = g_WasteBookInfo.TransferIndex;
                g_WorkInfo.lngEndRecordID = g_WasteBookInfo.TransferIndex + g_WorkInfo.cUpPamentSum;

                //达到一定数量上传未上传交易流水或达到一定上传的间隔时间
                if (g_WasteBookInfo.WriterIndex >= g_WorkInfo.lngEndRecordID) {
                    Log.i(TAG, String.format("g_WasteBookInfo.WriterIndex:%d, g_WorkInfo.lngStartRecordID:%d, g_WorkInfo.lngEndRecordID:%d", g_WasteBookInfo.WriterIndex, g_WorkInfo.lngStartRecordID, g_WorkInfo.lngEndRecordID));

                    //置上传交易流水标记0x00000020
                    g_CommInfo.lngSendComStatus |= 0x00000020;
                }
            }
        }
    }

    //网络重连过程
    public void ReConnectProcess() {
        String ServerIP = "";
        int ServerPort = 0;

        //有联机转换成脱机
        if ((g_WorkInfo.cRunState == 1)) {
            if ((g_WorkInfo.lngOSTime > (g_WorkInfo.lngOnLineTime + NODATARECVTIME * 10))) {
                Log.i(TAG, "有联机转换成脱机 网络断开");
                g_CommInfo.cQueryCardInfoStatus = 0;
                g_CommInfo.cGetQRCodeInfoStatus = 0;
                g_CommInfo.cLastRecDisInfoStatus = 0;
                g_CommInfo.lngSendComStatus = 0;
                g_WorkInfo.cRunState = 2;
                g_WorkInfo.cConnState = 0;
                g_WorkInfo.lngOffLineTime = g_WorkInfo.lngOSTime;
                SendHandler.sendEmptyMessage(EVT_DISCONNECT);
            }
        }

        //有脱机转换成联机
        if (g_WorkInfo.cRunState == 2) {
            if (g_WorkInfo.lngOSTime > g_WorkInfo.lngOffLineTime + NODATARECVTIME * 10) {
                Log.i(TAG, "开始脱机网络连接");
                g_WorkInfo.cConnState = 0;
                g_CommInfo.cQueryCardInfoStatus = 0;
                g_CommInfo.cGetQRCodeInfoStatus = 0;
                g_CommInfo.cLastRecDisInfoStatus = 0;
                g_CommInfo.lngSendComStatus = 0;
                ServerIP = g_LocalNetStrInfo.strServerIP1;
                ServerPort = g_LocalNetStrInfo.ServerPort1;
                ConnectServer(ServerIP, ServerPort, (byte) 0);
                g_WorkInfo.lngOffLineTime = g_WorkInfo.lngOSTime;
            }
        }
        // 重新连接签到服务
        if (g_WorkInfo.cRunState == 4) {
            if (g_WorkInfo.lngOSTime > g_WorkInfo.lngOnLineTime + 1 * 10) {
                Log.i(TAG, "=================重新连接签到服务器=====================");
                ServerIP = g_LocalNetStrInfo.strServerIP2;
                ServerPort = g_LocalNetStrInfo.ServerPort2;
                ConnectServer(ServerIP, ServerPort, (byte) 0);
                g_WorkInfo.lngOnLineTime = g_WorkInfo.lngOSTime;
            }
        }
        // 开机自动联机
        if (g_WorkInfo.cRunState == 5) {
            if (g_WorkInfo.lngOSTime > g_WorkInfo.lngOnLineTime + RECONNECTCOUNT * 10) {
                Log.i(TAG, "开始开机自动联机");
                //开机重连次数
                g_WorkInfo.cReConnectState++;
                g_WorkInfo.cConnState = 0;
                ServerIP = g_LocalNetStrInfo.strServerIP1;
                ServerPort = g_LocalNetStrInfo.ServerPort1;
                ConnectServer(ServerIP, ServerPort, (byte) 0);
                g_WorkInfo.lngOnLineTime = g_WorkInfo.lngOSTime;
            }
            if ((g_WorkInfo.cReConnectState >= 2)) {
                g_WorkInfo.cReConnectState = 0;
                g_CommInfo.cQueryCardInfoStatus = 0;
                g_CommInfo.cGetQRCodeInfoStatus = 0;
                g_CommInfo.cLastRecDisInfoStatus = 0;
                g_CommInfo.cWithholdInfoStatus = 0;
                g_WorkInfo.cRunState = 2;
                g_WorkInfo.cConnState = 0;
                g_WorkInfo.lngOffLineTime = g_WorkInfo.lngOSTime;
            }
        }
    }

    //网络超时定时器处理
    public void NetTimeOverProcess() {
        int cResult;
        //连接服务器超时
        if (s_NetEventMsg.type != EVT_NULL) {
            //连接服务器
            if (((s_NetEventMsg.type == EVT_CONNECT)) && (s_NetEventMsg.cConnectState == 0)) {
                s_NetEventMsg.lngNetTimeout++;
                if (s_NetEventMsg.lngNetTimeout >= COMTIMEOUT * 10) {
                    s_NetEventMsg.cConnectState = EVT_NETTIMEOUT;
                    s_NetEventMsg.lngNetTimeout = 0;
                    if ((g_WorkInfo.cRunState == 5) && (g_BasicInfo.cSystemState == 100)) {
                        Log.i(TAG, "NetTimeOver-->连接服务器超时");
                        g_WorkInfo.cUpdateState = 0;
                        g_WorkInfo.cRunState = 2; //脱机判断进入刷卡界面
                        DisConnect();
                    }
                }
            }

            //接收服务器报文(签到过程)
            if ((s_NetEventMsg.type == EVT_DEVICEJOIN)
                    || (s_NetEventMsg.type == EVT_SIGNIN)) {
                if ((s_NetEventMsg.cMsgResState == 0) && (s_NetEventMsg.cConnectState == 1)) {
                    s_NetEventMsg.lngNetTimeout++;
                    if (s_NetEventMsg.lngNetTimeout >= MSGTIMEOUT * 10) {
                        s_NetEventMsg.cMsgResState = EVT_NETTIMEOUT;
                        s_NetEventMsg.lngNetTimeout = 0;

                        if ((g_WorkInfo.cRunState == 5) && (g_BasicInfo.cSystemState == 100)) {
                            Log.i(TAG, "NetTimeOver-->接收服务器报文(签到过程)超时");
                            g_WorkInfo.cUpdateState = 0;
                            g_WorkInfo.cRunState = 2; //脱机判断进入刷卡界面
                        }
                    }
                }
            }

            //接收服务器报文(运行过程)
            if ((g_CommInfo.cRecvWaitState == 1) && (s_NetEventMsg.cMsgResState == 0)) {
                s_NetEventMsg.lngNetTimeout++;
                //判断卡户校验超时
                if ((g_CommInfo.lngSendComStatus & 0x00000010) == 0x00000010) {
                    if (s_NetEventMsg.lngNetTimeout >= 1 * 15) {
                        Log.i(TAG, "卡户校验报文超时");
                        Message message = Message.obtain();
                        message.what = EVT_CardCheckOver;
                        SendHandler.sendMessage(message);

                        s_NetEventMsg.cMsgResState = EVT_NETTIMEOUT;
                        g_CommInfo.cQueryCardInfoStatus = 0;
                        s_NetEventMsg.lngNetTimeout = 0;
                        g_CommInfo.cRecvWaitState = 0;
                        g_CommInfo.lngSendComStatus = 0;
                        s_NetEventMsg.wMsgTimeoutCount++;//记录超时次数，达到超时次数上限，重新连接服务器
                    }
                }
                //正元二维码有效性校验
                else if ((g_CommInfo.lngSendComStatus & 0x00000800) == 0x00000800) {
                    if ((s_NetEventMsg.lngNetTimeout >= 10 * 3)//超过2秒
                            || ((g_WorkInfo.cOtherQRFlag == 3) && (s_NetEventMsg.lngNetTimeout >= 25)))//在线卡片交易
                    {
                        if (g_WorkInfo.cOtherQRFlag == 3)
                            Log.i(TAG, "正元卡在线交易有效性校验超时");
                        else
                            Log.i(TAG, "正元二维码有效性校验超时");
                        Message message = Message.obtain();
                        message.what = EVT_QRTradeInfoOver;
                        SendHandler.sendMessage(message);

                        s_NetEventMsg.cMsgResState = EVT_NETTIMEOUT;
                        s_NetEventMsg.lngNetTimeout = 0;
                        g_CommInfo.cRecvWaitState = 0;
                        g_CommInfo.lngSendComStatus = 0;
                        s_NetEventMsg.wMsgTimeoutCount++;//记录超时次数，达到超时次数上限，重新连接服务器
                    }
                }
                //第三方二维码有效性校验
                else if ((g_CommInfo.lngSendComStatus & 0x00002000) == 0x00002000) {
                    if (s_NetEventMsg.lngNetTimeout >= 10 * 30)//超过20秒
                    {
                        Log.e(TAG, "第三方二维码有效性验证超时");
                        Message message = Message.obtain();
                        message.what = EVT_QRALITradeInfoOver;
                        SendHandler.sendMessage(message);

                        s_NetEventMsg.cMsgResState = EVT_NETTIMEOUT;
                        s_NetEventMsg.lngNetTimeout = 0;
                        g_CommInfo.cRecvWaitState = 0;
                        g_CommInfo.lngSendComStatus = 0;
                        s_NetEventMsg.wMsgTimeoutCount++;//记录超时次数，达到超时次数上限，重新连接服务器
                    }
                }
                //在线冲正超时
                else if ((g_CommInfo.lngSendComStatus & 0x000080) == 0x000080) {
                    if (s_NetEventMsg.lngNetTimeout >= 10 * 5)//超过10秒
                    {
                        Log.e(TAG, "在线冲正超时");
                        Message message = Message.obtain();
                        message.what = EVT_ONLRDispelOver;
                        SendHandler.sendMessage(message);

                        s_NetEventMsg.cMsgResState = EVT_NETTIMEOUT;
                        s_NetEventMsg.lngNetTimeout = 0;
                        g_CommInfo.cRecvWaitState = 0;
                        g_CommInfo.lngSendComStatus = 0;
                        s_NetEventMsg.wMsgTimeoutCount++;//记录超时次数，达到超时次数上限，重新连接服务器
                    }
                }
                //第三方代扣
                else if ((g_CommInfo.lngSendComStatus & 0x00010000) == 0x00010000) {
                    if (s_NetEventMsg.lngNetTimeout >= 10 * 10)//超过10秒
                    {
                        Log.d(TAG, "第三方代扣超时");
                        Message message = Message.obtain();
                        message.what = EVT_WithholdOver;
                        SendHandler.sendMessage(message);

                        s_NetEventMsg.cMsgResState = EVT_NETTIMEOUT;
                        s_NetEventMsg.lngNetTimeout = 0;
                        g_CommInfo.cRecvWaitState = 0;
                        g_CommInfo.lngSendComStatus = 0;
                        s_NetEventMsg.wMsgTimeoutCount++;//记录超时次数，达到超时次数上限，重新连接服务器
                    }
                } else if ((g_CommInfo.lngSendComStatus & 0x00001000) == 0x00001000) {
                    if (s_NetEventMsg.lngNetTimeout >= 10 * 3)//超过2秒
                    {
                        Log.i(TAG, "获取二维码订单号超时");
                        s_NetEventMsg.cMsgResState = EVT_NETTIMEOUT;
                        s_NetEventMsg.lngNetTimeout = 0;
                        g_CommInfo.cRecvWaitState = 0;
                        g_CommInfo.lngSendComStatus = 0;
                        g_WorkInfo.cQrCodestatus = 0;
                        s_NetEventMsg.wMsgTimeoutCount++;//记录超时次数，达到超时次数上限，重新连接服务器
                    }
                } else if ((g_CommInfo.lngSendComStatus & 0x00004000) == 0x00004000) {
                    if (s_NetEventMsg.lngNetTimeout >= 10 * 6)//超过3秒
                    {
                        Log.i(TAG, "获取ftp失败");
                        s_NetEventMsg.cMsgResState = EVT_NETTIMEOUT;
                        s_NetEventMsg.lngNetTimeout = 0;
                        g_CommInfo.cRecvWaitState = 0;
                        g_CommInfo.lngSendComStatus = 0;
                        s_NetEventMsg.wMsgTimeoutCount++;//记录超时次数，达到超时次数上限，重新连接服务器
                    }
                }
                //第三方末笔订单号查询
                else if ((g_CommInfo.lngSendComStatus & 0x00008000) == 0x00008000) {
                    if (s_NetEventMsg.lngNetTimeout >= 10 * 10)//超过3秒
                    {
                        Log.i(TAG, "第三方末笔订单号查询超时");
                        cResult = 0xff;
                        //emit ShowLastQRInfo(&g_LastOrderInfo,cResult);
                        s_NetEventMsg.cMsgResState = EVT_NETTIMEOUT;
                        s_NetEventMsg.lngNetTimeout = 0;
                        g_CommInfo.cRecvWaitState = 0;
                        g_CommInfo.lngSendComStatus = 0;
                        //记录超时次数，达到超时次数上限，重新连接服务器
                        s_NetEventMsg.wMsgTimeoutCount++;
                    }
                } else {
                    if (s_NetEventMsg.lngNetTimeout >= MSGTIMEOUT * 10) {
                        Log.i(TAG, "==========(运行过程)发送报文超时============");
                        s_NetEventMsg.cMsgResState = EVT_NETTIMEOUT;
                        s_NetEventMsg.lngNetTimeout = 0;
                        g_CommInfo.cRecvWaitState = 0;
                        g_CommInfo.lngSendComStatus = 0;
                        //记录超时次数，达到超时次数上限，重新连接服务器
                        s_NetEventMsg.wMsgTimeoutCount++;
                    }
                }
                if (s_NetEventMsg.wMsgTimeoutCount > MSGTIMEOUTCOUNT) {
                    Log.i(TAG, "==========(运行过程)发送报文超时次数超出上限============");
                    //memset((void*)(&s_NetEventMsg), 0, sizeof(NETEVENTMSG));
                    g_WorkInfo.cConnState = 0;
                    g_WorkInfo.cUpdateState = 0;
                    g_WorkInfo.cRunState = 2;
                    SendHandler.sendEmptyMessage(EVT_DISCONNECT);
                }
            }
        }
        g_WorkInfo.lngOSTime++; //计数器
    }

    //断开网络
    private void DisConnect() {

        //注：一定要判断socket是否存在，否则close将出现程序异常退出
        if (socket != null) {
            try {
                Log.i(TAG, "----------------主动断开网络----------------");
                inputStream.close();
                outputStream.close();
                if (socket != null)
                    socket.close();
                isOnline = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        } else {
            Log.i(TAG, "----------------主动断开网络(socket==null)不操作----------------");
        }
    }

    //判断网络是否连接
    public void CheckNetState() {
//        if((!socket.isConnected())
//                &&(!socket.isClosed())
//                && (!socket.isOutputShutdown()))
//        {
//            Log.i(TAG, "连接服务器成功，开始设备接入签到");
//            SendHandler.sendEmptyMessage(EVT_CONNECT);
//        }

        if (socket == null)
            return;

        if (socket.isConnected()) {
            Log.i(TAG, "socket isConnected");
        } else {
            Log.i(TAG, "socket isDISConnected");
        }

        if (socket.isClosed()) {
            Log.i(TAG, "socket isClosed");
        } else {
            Log.i(TAG, "socket is No Closed");
        }

        if (socket.isOutputShutdown()) {
            Log.i(TAG, "socket isOutputShutdown");
        } else {
            Log.i(TAG, "socket is No OutputShutdown");
        }

        if (socket.isInputShutdown()) {
            Log.i(TAG, "socket isInputShutdown");
        } else {
            Log.i(TAG, "socket is No InputShutdown");
        }
    }

}

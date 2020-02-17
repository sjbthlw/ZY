package com.hzsun.mpos.SerialWork;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.hzsun.mpos.MyApplication;
import com.hzsun.mpos.QRCodeWork.QRScanHelper;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import static com.hzsun.mpos.Global.Global.FINISH_QR_SCAN;
import static com.hzsun.mpos.Global.Global.GOTO_PAYCARD;
import static com.hzsun.mpos.Global.Global.HIDE_CAMERAVIEW;
import static com.hzsun.mpos.Global.Global.OK;
import static com.hzsun.mpos.Global.Global.gUICardHandler;
import static com.hzsun.mpos.Global.Global.gUIRDCardHandler;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CardInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.ShowCardInfo;
import static com.hzsun.mpos.Sound.SoundPlay.VoicePlay;

public class SerialWorkTask {

    private static final String TAG = SerialWorkTask.class.getSimpleName();
    private SerialWorkRecvThread serialWorkRecvThread;      //接收串口数据
    private byte[] orderBytes = new byte[8];
    private byte[] lngpaymoneyBytes = new byte[4];
    private int signState;  //0-未签到,1-已签到
    //0-空闲,1-查询中,2-查询完成,3-支付中,4-支付完成,5-退款中,6-退款完成
    public static int tradeState;
    public static final int STATE_IDEL = 0;
    public static final int STATE_QUERYING = 1;
    public static final int STATE_QUERY_FINISHED = 2;
    public static final int STATE_PAYING = 3;
    public static final int STATE_PAY_FINISHED = 4;
    public static final int STATE_DISPELING = 5;
    public static final int STATE_DISPEL_FINISHED = 6;
    //-1代表初始状态
    public static int tradeCode = -1;
    private int lastPacketOrder = -1; //包序号
    public static int lngPaymentMoney;  //对接机收到的支付金额

    private final int GO_TO_DISPEL = 0;

    private static final int TIMEOUT = 5 * 1000;
    private static MyHandler mHandler;
    private long ordernum;
    private long lastordernum;   //末笔支付订单号

    private static byte[] crcBytes = new byte[1024];
    private long lngAccountID;
    private byte[] cPerCode = new byte[16];
    private byte[] cCardSID = new byte[4];
    private byte[] cAccName = new byte[16];
    private byte[] phonenum = new byte[11];
    private long lngCardID;
    private byte[] cIDNo = new byte[18];
    private TradeHolderInfo tradeHolderInfo;
    private LastPayResult lastPayResult; //保存上一次的支付结果

    private static boolean isQRscan;
    private int detectCnt;

    public void Init() {
        serialWorkRecvThread = new SerialWorkRecvThread();
        serialWorkRecvThread.start();
        mHandler = new MyHandler(this);
        tradeHolderInfo = new TradeHolderInfo();
        lastPayResult = new LastPayResult();
    }

    class SerialWorkRecvThread extends Thread {

        private boolean isStart = true;

        @Override
        public void run() {
            super.run();
            while (isStart) {
                try {
                    Thread.sleep(100);//延时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (g_WorkInfo.cCardEnableFlag != 1) {
                    continue;
                }
                byte[] buf = new byte[1024];
                int len = g_Nlib.UartDockpos_RecvData(buf);
                if (len > 0) {
                    Log.e(TAG, "接收:" + SerialUtil.byte2HexStr(buf, len));
                    if (!checkPacketInvalde(buf, len)) {
                        continue;
                    }

                    int cmd = buf[3] & 0xFF;
                    switch (cmd) {
                        case 0xA0:
                            //设备签到请求
                            Log.e(TAG, "签到");
                            clearState();
                            signState = 1;
                            SerialFunc.sendSignReplyPacket();
                            break;
                        case 0xB1:
                            //发起身份查询
                            Log.e(TAG, "发起身份查询");
                            clearState();
                            SerialFunc.sendQueryReplyPacket();
                            tradeState = STATE_QUERYING;
                            g_Nlib.QR_SetDeviceReadEnable(1);//开始识读
                            isQRscan = true;
                            g_WorkInfo.cSelfPressOk = 1;              //用于打开人脸检测
                            g_WorkInfo.cKeyDownState = 1;
                            break;
                        case 0xB2:
                            //查询身份结果
                            Log.e(TAG, "查询身份结果");
                            sendQueryResult();
                            break;
                        case 0xB3:
                            //取消身份查询
                            Log.e(TAG, "取消身份查询");
                            SerialFunc.sendStopQueryReplyPacket();
                            clearState();
                            break;
                        case 0xC1:
                            //发起支付请求
                            Log.e(TAG, "发起支付请求");
                            if (tradeState == 4) {
                                SerialFunc.showErrorCode(0xC1, 0x01);
                                Log.e(TAG, "支付结果没有上传,无法重新支付");
                                break;
                            }
                            if (g_WorkInfo.cPayDisPlayStatus != 0) {
                                g_WorkInfo.cPayDisPlayStatus = 0;   //如果还在显示支付界面状态,则先清零,否则可能出现二维码扫描
                            }
                            clearState();
                            System.arraycopy(buf, 5, orderBytes, 0, 8);
                            System.arraycopy(buf, 13, lngpaymoneyBytes, 0, 4);
                            ordernum = SerialUtil.getOrdernum(orderBytes);
                            lngPaymentMoney = SerialUtil.byte2Int(lngpaymoneyBytes, 0);
                            g_WorkInfo.lngPaymentMoney = lngPaymentMoney;
                            Log.e(TAG, String.format("订单号:%d,金额:%d", ordernum, lngPaymentMoney));
                            if (ordernum != lastordernum) {
                                tradeState = 3;
                                VoicePlay("money_pay");
                                lastordernum = ordernum;
                                SerialFunc.sendPayReplyPacket();
                                g_WorkInfo.cSelfPressOk = 1;
                                g_WorkInfo.cKeyDownState = 1;
                                g_WorkInfo.cScanQRCodeFlag = 1;         //二维码扫码标志位
                                g_Nlib.QR_SetDeviceReadEnable(1);//开始识读
                                ShowCardInfo(g_CardBasicInfo, 1);//显示
                            } else {
                                SerialFunc.showErrorCode(0xC1, 0x96);
                                Log.e(TAG, "订单号错误");
                            }
                            break;
                        case 0xC2:
                            //查询支付结果
                            try {
                                System.arraycopy(buf, 5, orderBytes, 0, 8);
                                ordernum = SerialUtil.getOrdernum(orderBytes);
                                sendPayResult(ordernum);
                            } catch (ArrayIndexOutOfBoundsException e) {
                                Log.e(TAG, "获取订单号错误");
                            }
                            break;
                        case 0xC3:
                            //撤销支付请求
                            Log.e(TAG, "撤销支付请求");
                            if (tradeState == 3) {
                                if (g_WorkInfo.cOtherQRFlag == -1) {
                                    //还未开始支付可以取消支付
                                    SerialFunc.sendCancelPayReplyPacket();
                                    clearState();
                                    mHandler.removeMessages(FINISH_QR_SCAN);
                                    mHandler.sendEmptyMessageDelayed(FINISH_QR_SCAN, TIMEOUT);
                                    if (g_WorkInfo.cQrCodestatus != 0) {
                                        g_WorkInfo.cQrCodestatus = 0;
                                    }
                                } else if (g_WorkInfo.cOtherQRFlag == 2) {
                                    //人脸支付中,无法取消支付
                                    SerialFunc.showErrorCode(0xC3, 0x11);
                                } else {
                                    //卡片或二维码支付
                                    if ((g_WorkInfo.cOtherQRFlag == 0
                                            || g_WorkInfo.cOtherQRFlag == 1
                                            || g_WorkInfo.cOtherQRFlag == 3)
                                            && (tradeCode == 0x02 || tradeCode == 0x05 || tradeCode == 0x06 || tradeCode == 0x07 || tradeCode == 0x08 || tradeCode == 0x15)) {
                                        //可以取消支付
                                        SerialFunc.sendCancelPayReplyPacket();
                                        mHandler.removeMessages(FINISH_QR_SCAN);
                                        mHandler.sendEmptyMessageDelayed(FINISH_QR_SCAN, TIMEOUT);
                                        clearState();
                                    } else {
                                        //支付中,无法取消
                                        SerialFunc.showErrorCode(0xC3, 0x11);
                                    }
                                }

                            } else if (tradeState == 4 || tradeState == 0) {
                                if (tradeCode == 0x05
                                        || tradeCode == 0x99
                                        || tradeCode == 0x91
                                        || (tradeCode == -1 && !lastPayResult.isSuccess())
                                        || (tradeState == 0 && tradeCode == 0x97)) {
                                    //余额不足时或没有进行支付可以取消支付重新支付
                                    SerialFunc.sendCancelPayReplyPacket();
                                    clearState();
                                    mHandler.removeMessages(FINISH_QR_SCAN);
                                    mHandler.sendEmptyMessageDelayed(FINISH_QR_SCAN, TIMEOUT);
                                } else {
                                    SerialFunc.showErrorCode(0xC3, 0x98);
                                }
                            }
                            break;
                        case 0xC4:
                            //发起退款请求
                            Log.e(TAG, "发起退款请求");
                            clearState();
                            if (mHandler != null) {
                                mHandler.sendEmptyMessage(GO_TO_DISPEL);
                            }
                            tradeState = 5;
                            SerialFunc.sendDispelReplyPacket();
                            break;
                        case 0xC5:
                            //查询退款结果
                            Log.e(TAG, "查询退款结果");
                            sendDispelResult();
                            break;
                    }

                }
            }
        }
    }


    /**
     * 空闲中状态每隔30s监听串口状态
     */
    private void resetSerial() {
        detectCnt++;
        if (detectCnt > 300) {
            if (tradeState == 0) {
                int state = g_Nlib.QR_GetDisSenseRet(1);
                Log.e(TAG, String.format("state:%d", state));
                if (state == -1) {
                    g_Nlib.UartQR_Init(0);
                    Log.e(TAG, "重新初始化串口");
                }
                detectCnt = 0;
            }
        }

    }


    /**
     * 根据不同的支付方式方式相应报文
     */
    private void sendQueryResult() {
        if (tradeState == STATE_IDEL) {
            Log.e(TAG, "空闲中,未开始查询");
            tradeCode = 0x97;
            SerialFunc.showErrorCode(0xB2, tradeCode);
            return;
        }
        if (tradeState == STATE_QUERYING) {
            //查询中
            tradeCode = 0x01;
            SerialFunc.showErrorCode(0xB2, tradeCode);
            Log.e(TAG, "查询中");
            return;
        }
        if (tradeState == STATE_QUERY_FINISHED) {
            if (tradeCode == 0x00) {
                tradeHolderInfo.loadCardHolderInfo(g_WorkInfo.cOtherQRFlag);
                clearBytes();
                lngAccountID = tradeHolderInfo.getLngAccountID();
                cPerCode = tradeHolderInfo.getcPerCode();
                cCardSID = tradeHolderInfo.getcCardSID();
                cAccName = tradeHolderInfo.getcAccName();
                phonenum = tradeHolderInfo.getPhonenum();
                lngCardID = tradeHolderInfo.getLngCardID();
                cIDNo = tradeHolderInfo.getcIDNo();
                SerialFunc.sendQueryResultPacket(lngAccountID, cPerCode, cCardSID, cAccName, phonenum, lngCardID, cIDNo);
                Log.e(TAG, "返回查询结果成功报文");
            } else {
                SerialFunc.showErrorCode(0xB2, tradeCode);
                Log.e(TAG, "返回查询结果失败报文");
            }
            clearState();
        }
    }

    private void clearBytes() {
        Arrays.fill(cPerCode, (byte) 0);
        Arrays.fill(cCardSID, (byte) 0);
        Arrays.fill(cAccName, (byte) 0);
        Arrays.fill(phonenum, (byte) 0);
        Arrays.fill(cIDNo, (byte) 0);
    }

    private void sendPayResult(long ordernum) {
        if (tradeState != STATE_PAY_FINISHED) {
            if (tradeState == STATE_IDEL) {
                if (lastPayResult != null) {
                    //支付完成后,再次查询支付结果返回上一次的支付结果
                    if (ordernum == lastPayResult.getLastordernum() && lastPayResult.isSuccess()) {
                        sendPaySuccessPacket(lastPayResult.getHolderInfo());
                        mHandler.removeMessages(FINISH_QR_SCAN);
                        mHandler.sendEmptyMessage(FINISH_QR_SCAN);
                    } else {
                        SerialFunc.showErrorCode(0xC2, 0x99);
                        clearState();
                        mHandler.removeMessages(FINISH_QR_SCAN);
                        mHandler.sendEmptyMessage(FINISH_QR_SCAN);
                    }
                } else {
                    //空闲状态,没有支付过
                    tradeCode = 0x97;
                    SerialFunc.showErrorCode(0xC2, tradeCode);
                }
            } else if (tradeState == STATE_PAYING) {
                //发起支付请求还未完成支付状态
                tradeCode = 0x01;
                SerialFunc.showErrorCode(0xC2, 0x01);
            }
            return;
        }
        if (tradeState == STATE_PAY_FINISHED) {
            if (tradeCode == 0x00) {
                //支付成功
                tradeHolderInfo.loadCardHolderInfo(g_WorkInfo.cOtherQRFlag);
                lastPayResult.setState(true);
                lastPayResult.setLastordernum(lastordernum);
                lastPayResult.setHolderInfo(tradeHolderInfo);
                sendPaySuccessPacket(tradeHolderInfo);
                mHandler.removeMessages(FINISH_QR_SCAN);
                mHandler.sendEmptyMessage(FINISH_QR_SCAN);
            } else {
                //支付失败
                lastPayResult.setState(false);
                lastPayResult.setLastordernum(lastordernum);
                SerialFunc.showErrorCode(0xC2, tradeCode);
                mHandler.removeMessages(FINISH_QR_SCAN);
                mHandler.sendEmptyMessage(FINISH_QR_SCAN);
            }
            clearState();
        }
    }


    private void sendPaySuccessPacket(TradeHolderInfo tradeHolderInfo) {
        lngAccountID = tradeHolderInfo.getLngAccountID();
        cPerCode = tradeHolderInfo.getcPerCode();
        cCardSID = tradeHolderInfo.getcCardSID();
        cAccName = tradeHolderInfo.getcAccName();
        phonenum = tradeHolderInfo.getPhonenum();
        lngCardID = tradeHolderInfo.getLngCardID();
        cIDNo = tradeHolderInfo.getcIDNo();
        SerialFunc.sendPayResultPacket(lngAccountID, cPerCode, cCardSID, cAccName, phonenum);
        clearState();
    }

    private void sendDispelResult() {
        if (tradeState != STATE_DISPEL_FINISHED) {
            if (g_CardInfo.cExistState == 1 && g_WorkInfo.cOtherQRFlag == 3 && g_CardInfo.cAuthenState == 0) {
                //无效卡
                SerialFunc.showErrorCode(0xC5, 0x02);
                Log.e(TAG, "无效卡");
            } else {
                //查询中
                SerialFunc.showErrorCode(0xC5, 0x01);
                Log.e(TAG, "冲正查询中...");
            }
            return;
        }
        SerialFunc.sendDispelResultPacket();
        clearState();
    }

    /**
     * 是否可以扫卡或者扫码
     *
     * @return
     */
    public static boolean isEnableScan() {
        return (g_LocalInfo.cDockposFlag == 1
                && (tradeState == STATE_PAYING || tradeState == STATE_QUERYING));
    }

    /**
     * 是否可以进行支付
     *
     * @return
     */
    public static boolean isEnablePayfor() {
        return (g_LocalInfo.cDockposFlag == 1
                && tradeState == STATE_PAYING);
    }

    /**
     * 支付时设置接收到的对接机金额
     */
    public static void setPayMoney() {
        if (tradeState == STATE_PAYING && lngPaymentMoney >= 0) {
            g_WorkInfo.lngPaymentMoney = lngPaymentMoney;
            Log.e(TAG, String.format("设置对接金额:%d", lngPaymentMoney));
        } else {
            g_WorkInfo.lngPaymentMoney = 0;
        }
    }

    public static void clearState() {
        tradeState = STATE_IDEL;
        lngPaymentMoney = 0;
        g_WorkInfo.cOtherQRFlag = -1;
        g_WorkInfo.cSelfPressOk = 0;              //关闭人脸检测
        g_WorkInfo.cKeyDownState = 0;
        g_WorkInfo.lngPaymentMoney = 0;
        g_Nlib.QR_SetDeviceReadEnable(2);  //结束识读
        g_Nlib.QR_ClearRecvData(0);        //清除串口数据
        QRScanHelper.clearQRCode();
        Log.i(TAG, "清除串口数据");
        if (tradeState == STATE_DISPELING || tradeState == STATE_DISPEL_FINISHED) {
            if (gUIRDCardHandler != null) {
                gUIRDCardHandler.sendEmptyMessage(GOTO_PAYCARD);
            }
            tradeState = STATE_IDEL;
        }
    }

    public static int getTradeState() {
        return tradeState;
    }

    public static void onQueryDone(int queryWay) {
        g_WorkInfo.cScanQRCodeFlag = (byte) queryWay;
        tradeCode = 0x00;
        tradeState = STATE_QUERY_FINISHED;
    }

    public static void onPayDone(int payResult) {
        if (payResult == OK) {
            tradeCode = 0x00;
        } else {
            tradeCode = 0x99;
        }
        tradeState = STATE_PAY_FINISHED;
    }


    public static void setCode(int tradeCode) {
        SerialWorkTask.tradeCode = tradeCode;
    }

    /**
     * 对报文进行校验(报文格式合法性,包序号合法性,设备是否签到,报文至少8个字节)
     */
    private boolean checkPacketInvalde(byte[] buf, int len) {
        if (len < 8) {
            Log.e(TAG, "报文格式校验错误");
            if (len < 4) {
                //未收到命令码
                SerialFunc.showErrorCode(0xD1, 0x92);
            } else {
                if ((buf[0] & 0xFF) != 0x02
                        || (buf[len - 1] & 0xFF) != 0x03
                        || (buf[3] & 0xFF) != 0xA0
                        || (buf[3] & 0xFF) != 0xB1
                        || (buf[3] & 0xFF) != 0xB2
                        || (buf[3] & 0xFF) != 0xB3
                        || (buf[3] & 0xFF) != 0xC1
                        || (buf[3] & 0xFF) != 0xC2
                        || (buf[3] & 0xFF) != 0xC3
                        || (buf[3] & 0xFF) != 0xC4
                        || (buf[3] & 0xFF) != 0xC5
                        || (buf[3] & 0xFF) != 0xD1) {
                    SerialFunc.showErrorCode(buf[3], 0x92);
                }
            }
            return false;
        }

        int order = 0;
        order += buf[1] & 0xff;
        order += buf[2] & 0xff * 256;
//        Log.e(TAG, "包序号:" + order);
        SerialFunc.setCurrentPacketIndex(order);
        if (lastPacketOrder == order) {
            Log.e(TAG, "包序号校验错误");
            SerialFunc.showErrorCode(buf[3], 0x94);
            return false;
        }
        lastPacketOrder = order;

        System.arraycopy(buf, 1, crcBytes, 0, len - 4);
        int crc = SerialUtil.calcCRC16(crcBytes, len - 4);
//        Log.e(TAG, "计算出来的CRC:" + crc);

        int recvCrc = 0;
        recvCrc += buf[len - 3] & 0xff;
        recvCrc += (buf[len - 2] & 0xff) * 256;
//        Log.e(TAG, "接收到的CRC:" + recvCrc);
        if (recvCrc != crc) {
            Log.e(TAG, "crc校验错误");
            SerialFunc.showErrorCode(buf[3], 0x91);
            return false;
        }

        if (signState != 1) {
            //未签到
            int cmd = buf[3] & 0xFF;
            if (cmd != 0xA0) {
                Log.e(TAG, "设备未签到,请签到");
                SerialFunc.showErrorCode(buf[3], 0x93);
                return false;
            }
        }
        return true;
    }

    private static class MyHandler extends Handler {
        SerialWorkTask task;
        WeakReference<SerialWorkTask> reference;

        public MyHandler(SerialWorkTask task) {
            reference = new WeakReference<>(task);
        }

        public void handleMessage(Message message) {
            task = reference.get();
            if (task != null) {
                task.onHandleMessage(message);
            }
        }

    }

    private void onHandleMessage(Message message) {
        switch (message.what) {
            case GO_TO_DISPEL:
//                String activityname = getRunningActivityName();
//                if (!activityname.contains("RecordDispelActivity")) {
//                    MyApplication.myApp.startActivity(new Intent(MyApplication.myApp, RecordDispelActivity.class));
//                }
                break;
            case FINISH_QR_SCAN:
                if (isQRscan && tradeState != STATE_PAYING) {
                    Log.e(TAG, "结束识读");
                    g_Nlib.QR_SetDeviceReadEnable(2);//结束识读
                    gUICardHandler.sendEmptyMessage(HIDE_CAMERAVIEW);
                    isQRscan = false;
                }
                break;
        }
    }

    private String getRunningActivityName() {
        ActivityManager manager = (ActivityManager) MyApplication.myApp.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningTaskInfo info = manager.getRunningTasks(1).get(0);
        String shortClassName = info.topActivity.getShortClassName();    //类名
        return shortClassName;
    }


}

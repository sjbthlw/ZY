package com.hzsun.mpos.SerialWork;

import android.util.Log;

import static com.hzsun.mpos.Global.Global.SOFTWAREVER;
import static com.hzsun.mpos.Global.Global.g_CardBasicTmpInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_RecordInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_WasteBookInfo;


/*      设备签到请求	0xA0	取得正元POS机固件版本
        发起身份查询	0xB1	查询账户信息
        查询身份结果	0xB2	查询账户信息
        取消身份查询	0xB3	取消当前的查询
        发起支付请求	0xC1	根据交易金额，请求正元POS机交易
        查询支付结果	0xC2	还在获取响应数据：请求支付返回的结果数据
        撤销支付请求	0xC3	取消当前已发出请求支付的交易
        发起退款请求	0xC4	撤销最后一笔已成功支付的交易
        查询退款结果	0xC5	还在获取响应数据：请求支付返回的结果数据
        请求包格式:
        起始符+包序号+命令码+数据域长度+数据域+校验+结束符
        应答包格式:
        起始符+包序号+命令码+数据域长度+返回码+数据域+校验+结束符*/

public class SerialFunc {

    private static byte[] buf = new byte[1024];
    private static byte[] bytesSend = new byte[1024];
    private static byte[] crcBytes = new byte[1024];
    private static byte[] orderBytes = new byte[8];
    private static int packetIndex = 0;
    private static String TAG = SerialFunc.class.getSimpleName();


    /**
     * 正元POS向第三方设备发送签到应答报文
     */
    public static void sendSignReplyPacket() {
        int index = 0;
        //命令码
        buf[index++] = (byte) 0xA0;
        //数据域长度
        buf[index++] = 0x09;
        //返回码
        buf[index++] = 0x00;
        //固件版本
        byte[] version = SOFTWAREVER.getBytes();
        System.arraycopy(version, 0, buf, index, 8);
        index += 8;
        sendPacketData(index, buf);
    }


    /**
     * 正元POS机向第三方设备发送个人信息查询应答报文
     */
    public static void sendQueryReplyPacket() {
        int index = 0;
        //命令码
        buf[index++] = (byte) 0xB1;
        //数据域长度
        buf[index++] = 0x01;
        //返回码
        buf[index++] = 0x00;
        sendPacketData(index, buf);
    }


    /**
     * 正元POS机向第三方设备发送查询结果报文
     */
    public static void sendQueryResultPacket(long lngAccountID, byte[] cPerCode, byte[] cCardSID, byte[] cAccName, byte[] phonenum, long lngCardID, byte[] cIDNo) {
        int index = 0;
        //命令码
        buf[index++] = (byte) 0xB2;
        //数据域长度
        buf[index++] = 0x4A;
        //返回码
        buf[index++] = 0x00;
        //账号 4字节
        buf[index++] = (byte) (lngAccountID & 0xFF);
        buf[index++] = (byte) (lngAccountID >> 8 & 0xFF);
        buf[index++] = (byte) (lngAccountID >> 16 & 0xFF);
        buf[index++] = (byte) (lngAccountID >> 24 & 0xFF);
        //个人编号 16字节
        System.arraycopy(cPerCode, 0, buf, index, cPerCode.length);
        index += 16;
        //物理卡号 4字节
        System.arraycopy(cCardSID, 0, buf, index, cCardSID.length);
        index += 4;
        //姓名 16字节
        System.arraycopy(cAccName, 0, buf, index, cAccName.length);
        index += 16;
        //手机号 11字节
        System.arraycopy(phonenum, 0, buf, index, phonenum.length);
        index += 11;
        //卡内编号 4字节
        buf[index++] = (byte) (lngCardID & 0xFF);
        buf[index++] = (byte) (lngCardID >> 8 & 0xFF);
        buf[index++] = (byte) (lngCardID >> 16 & 0xFF);
        buf[index++] = (byte) (lngCardID >> 24 & 0xFF);
        //身份证号 18字节
        System.arraycopy(cIDNo, 0, buf, index, cIDNo.length);
        index += 18;
        sendPacketData(index, buf);

    }


    /**
     * 正元POS机向第三方设备发送结束查询应答报文
     */
    public static void sendStopQueryReplyPacket() {
        int index = 0;
        //命令码
        buf[index++] = (byte) 0xB3;
        //数据域长度
        buf[index++] = 0x01;
        //返回码
        buf[index++] = 0x00;
//        //数据域
//        for (int i = 0; i < 8; i++) {
//            buf[index++] = 0x00;
//        }
        sendPacketData(index, buf);
    }


    /**
     * 正元POS机收到支付请求后,向第三方设备发送支付应答报文
     */
    public static void sendPayReplyPacket() {
        int index = 0;
        //命令码
        buf[index++] = (byte) 0xC1;
        //数据域长度
        buf[index++] = 0x01;
        //返回码
        buf[index++] = 0x00;
        sendPacketData(index, buf);
    }


    /**
     * 正元POS机向第三方设备发送支付结果报文
     */
    public static void sendPayResultPacket(long lngAccountID, byte[] cPerCode, byte[] cCardSID, byte[] cAccName, byte[] phonenum) {
        int index = 0;
        //命令码
        buf[index++] = (byte) 0xC2;
        //数据域长度
        buf[index++] = 0x54;
        //返回码
        buf[index++] = 0x00;
        //订单号 8字节
        System.arraycopy(orderBytes, 0, buf, index, orderBytes.length);
        index += 8;
        //账号 4字节
        buf[index++] = (byte) (lngAccountID & 0xFF);
        buf[index++] = (byte) (lngAccountID >> 8 & 0xFF);
        buf[index++] = (byte) (lngAccountID >> 16 & 0xFF);
        buf[index++] = (byte) (lngAccountID >> 24 & 0xFF);
        //个人编号 16字节
        System.arraycopy(cPerCode, 0, buf, index, cPerCode.length);
        index += 16;
        //物理卡号 4字节
        System.arraycopy(cCardSID, 0, buf, index, cCardSID.length);
        index += 4;
        //姓名
        System.arraycopy(cAccName, 0, buf, index, cAccName.length);
        index += 16;
        //手机号 11字节
        System.arraycopy(phonenum, 0, buf, index, phonenum.length);
        index += 11;
        //终端站点号 2字节
        buf[index++] = (byte) (g_StationInfo.iStationID & 0xFF);
        buf[index++] = (byte) (g_StationInfo.iStationID >> 8 & 0xFF);
        //终端流水号  4字节
        long lngPayRecordID = g_WasteBookInfo.WriterIndex + 1;
//        Log.i(TAG, String.format("终端流水号:%d", lngPayRecordID));
        buf[index++] = (byte) (lngPayRecordID & 0xFF);
        buf[index++] = (byte) (lngPayRecordID >> 8 & 0xFF);
        buf[index++] = (byte) (lngPayRecordID >> 16 & 0xFF);
        buf[index++] = (byte) (lngPayRecordID >> 24 & 0xFF);
        //扣款金额  3字节
//        Log.i(TAG, String.format("扣款金额:%d", g_CardBasicTmpInfo.lngPayMoney));
        buf[index++] = (byte) (g_CardBasicTmpInfo.lngPayMoney & 0xFF);
        buf[index++] = (byte) (g_CardBasicTmpInfo.lngPayMoney >> 8 & 0xFF);
        buf[index++] = (byte) (g_CardBasicTmpInfo.lngPayMoney >> 16 & 0xFF);
        //扣款后余额  3字节
//        Log.i(TAG, String.format("扣款后余额:%d", g_CardBasicTmpInfo.getLngWorkBurseMoney()));
        buf[index++] = (byte) (g_CardBasicTmpInfo.getLngWorkBurseMoney() & 0xFF);
        buf[index++] = (byte) (g_CardBasicTmpInfo.getLngWorkBurseMoney() >> 8 & 0xFF);
        buf[index++] = (byte) (g_CardBasicTmpInfo.getLngWorkBurseMoney() >> 16 & 0xFF);
        //优惠金额    3字节
//        Log.i(TAG, String.format("优惠费:%d", g_CardBasicTmpInfo.lngPriMoney));
        buf[index++] = (byte) (g_CardBasicTmpInfo.lngPriMoney & 0xFF);
        buf[index++] = (byte) (g_CardBasicTmpInfo.lngPriMoney >> 8 & 0xFF);
        buf[index++] = (byte) (g_CardBasicTmpInfo.lngPriMoney >> 16 & 0xFF);
        //管理费  3字节
//        Log.i(TAG, String.format("管理费:%d", g_CardBasicTmpInfo.lngManageMoney));
        buf[index++] = (byte) (g_CardBasicTmpInfo.lngManageMoney & 0xFF);
        buf[index++] = (byte) (g_CardBasicTmpInfo.lngManageMoney >> 8 & 0xFF);
        buf[index++] = (byte) (g_CardBasicTmpInfo.lngManageMoney >> 16 & 0xFF);
        //交易时间  6字节
        System.arraycopy(g_RecordInfo.cLastPaymentDate, 0, buf, index, 6);
        index += 6;
        sendPacketData(index, buf);
    }

    /**
     * 正元POS机向第三方设备发送取消支付请求应答报文
     */
    public static void sendCancelPayReplyPacket() {
        int index = 0;
        //命令码
        buf[index++] = (byte) 0xC3;
        //数据域长度
        buf[index++] = 0x01;
        //返回码
        buf[index++] = 0x00;
        sendPacketData(index, buf);
    }


    /**
     * 正元POS机向第三方设备发送退款请求应答报文
     */
    public static void sendDispelReplyPacket() {
        int index = 0;
        //命令码
        buf[index++] = (byte) 0xC4;
        //数据域长度
        buf[index++] = 0x01;
        //返回码
        buf[index++] = 0x00;
        sendPacketData(index, buf);
    }


    /**
     * 正元POS机向第三方设备发送退款结果报文
     */
    public static void sendDispelResultPacket() {
        int index = 0;
        //命令码
        buf[index++] = (byte) 0xC5;
        //数据域长度
        buf[index++] = 0x01;
        //返回码
        buf[index++] = 0x00;
        sendPacketData(index, buf);
    }


    private static void sendPacketData(int len, byte[] buffer) {
        int index = 0;
        //起始符  1字节
        bytesSend[index++] = 0x02;
        //包序号  2字节
        if (packetIndex >= 65535) {
            packetIndex = 0;
        }
        byte buf1 = (byte) (packetIndex & 0xFF);
        byte buf2 = (byte) ((packetIndex >> 8) & 0xFF);
        bytesSend[index++] = buf1;
        bytesSend[index++] = buf2;
        //内容区
        System.arraycopy(buffer, 0, bytesSend, index, len);
        index += len;
        //校验  2字节
        System.arraycopy(bytesSend, 1, crcBytes, 0, index - 1);
        int crc = SerialUtil.calcCRC16(crcBytes, index - 1);
        bytesSend[index++] = (byte) (crc & 0xFF);
        bytesSend[index++] = (byte) (crc >> 8 & 0xFF);
        //结束符  1字节
        bytesSend[index++] = 0x03;
        g_Nlib.UartDockpos_SendData(bytesSend, index);
        Log.i("SerialWorkTask", "发送:" + SerialUtil.byte2HexStr(bytesSend, index));
    }

    /**
     * 发送错误码报文
     */
    public static void showErrorCode(int cmdCode, int errCode) {
        int index = 0;
        //命令码
        buf[index++] = (byte) cmdCode;
        //数据域长度
        buf[index++] = 0x01;
        //返回码
        buf[index++] = (byte) errCode;
        sendPacketData(index, buf);
        switch (errCode) {
            case 0x01:
                Log.e(TAG, "正在忙碌中,请等待(进行支付中,撤销支付中,请求退款中,请求查询等等)");
                break;
            case 0x02:
                Log.e(TAG, "卡户无效");
                break;
            case 0x03:
                Log.e(TAG, "请重新放卡片");
                break;
            case 0x04:
                Log.e(TAG, "读写卡错误");
                break;
            case 0x05:
                Log.e(TAG, "钱包余额不足");
                break;
            case 0x06:
                Log.e(TAG, "超出消费范围");
                break;
            case 0x07:
                Log.e(TAG, "超出有效期");
                break;
            case 0x08:
                Log.e(TAG, "单笔金额超限");
                break;
            case 0x09:
                Log.e(TAG, "日累金额超限");
                break;
            case 0x10:
                Log.e(TAG, "超出消费限次");
                break;
            case 0x11:
                Log.e(TAG, "已在支付,无法撤销");
                break;
            case 0x12:
                Log.e(TAG, "该笔交易已经退款");
                break;
            case 0x13:
                Log.e(TAG, "该笔订单号未找到,无法撤销或退款");
                break;
            case 0x14:
                Log.e(TAG, "请重新刷卡");
                break;
            case 0x91:
                Log.e(TAG, "CRC16校验错误");
                break;
            case 0x92:
                Log.e(TAG, "报文格式不正确");
                break;
            case 0x93:
                Log.e(TAG, "第三方设备未经授权");
                break;
            case 0x94:
                Log.e(TAG, "第三方设备的包序号重复");
                break;
            case 0x95:
                Log.e(TAG, "正元POS机无法运行(未营业中,有故障等等)");
                break;
            case 0x96:
                Log.e(TAG, "第三方设备发送的支付订单号错误");
                break;
            case 0x97:
                Log.e(TAG, "等待支付中,支付还未开始,此状态下可以取消支付");
                break;
            case 0x98:
                Log.e(TAG, "支付完成无法撤销");
                break;
            case 0x99:
                Log.e(TAG, "支付失败");
                break;
            case 0x9A:
                Log.e(TAG, "退款失败");
                break;
            case 0x9F:
                Log.e(TAG, "正元POS机响应超时");
                break;
        }
    }

    public static void setCurrentPacketIndex(int packetOrder) {
        packetIndex = packetOrder;
    }


    //二维码业务错误提示
    public static void showQRError(int iErrorCode) {
        String cErrorCode = "";
        if (iErrorCode > 0) {
            switch (iErrorCode) {
                case 1:
                    cErrorCode = "超出消费范围";
                    SerialWorkTask.s_TradeCode = 0x06;
                    break;
                case 13:
                    cErrorCode = "余额不足";
                    SerialWorkTask.s_TradeCode = 0x05;
                    break;
                case 11:
                    cErrorCode = "操作超时";
                    SerialWorkTask.s_TradeCode = 0x9F;
                    break;
                case 12:
                    cErrorCode = "订单号过期";
                    SerialWorkTask.s_TradeCode = 0x07;
                    break;
                case 2:
                case 26:
                    cErrorCode = "无效卡户";
                    SerialWorkTask.s_TradeCode = 0x02;
                    break;
                case 21:
                case 23:
                    cErrorCode = "消费限次";
                    SerialWorkTask.s_TradeCode = 0x10;
                    break;
                case 22:
                    cErrorCode = "超出日累限额";
                    SerialWorkTask.s_TradeCode = 0x09;
                    break;
                case 24:
                    cErrorCode = "超出单笔密码限额";
                    SerialWorkTask.s_TradeCode = 0x08;
                    break;
                case 97:
                    cErrorCode = "无效二维码";
                    SerialWorkTask.s_TradeCode = 0x15;
                    break;
                default:
                    cErrorCode = "支付失败";
                    SerialWorkTask.s_TradeCode = 0x99;
                    break;
            }

        }
        Log.i(TAG, cErrorCode);
    }

    public static void showCardError(int iErrorCode) {
        String cErrorCode = "";
        String cErrorVoice = "";

        if (iErrorCode > 0) {
            switch (iErrorCode) {
                case 1:
                case 4:
                case 61:
                    cErrorCode = "无效卡";
                    SerialWorkTask.s_TradeCode = 0x02;
                    break;
                case 2:
                    cErrorCode = "超出消费范围";
                    SerialWorkTask.s_TradeCode = 0x06;
                    break;
                case 3:
                    cErrorCode = "余额不足";
                    SerialWorkTask.s_TradeCode = 0x05;
                    break;
                case 5:
                    cErrorCode = "超出有效期";
                    SerialWorkTask.s_TradeCode = 0x07;
                    break;
                case 8:
                    cErrorCode = "等待卡片重刷";
                    SerialWorkTask.s_TradeCode = 0x14;
                    break;
                case 6:
                case 7:
                case 10:
                    cErrorCode = "超出单笔消费限额";
                    SerialWorkTask.s_TradeCode = 0x08;
                    break;
                case 15:
                case 16:
                    cErrorCode = "冲正失败";
                    SerialWorkTask.s_TradeCode = 0x9B;
                    break;
                case 32:
                    break;
                case 97:
                    cErrorCode = "无效二维码";
                    SerialWorkTask.s_TradeCode = 0x15;
                    break;
                default:
                    cErrorCode = "错误";
                    SerialWorkTask.s_TradeCode = 0x99;
                    break;
            }

        }
        Log.i(TAG, cErrorCode);
    }


}

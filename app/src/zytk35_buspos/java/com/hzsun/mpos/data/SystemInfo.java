package com.hzsun.mpos.data;

import java.io.Serializable;
import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class SystemInfo implements Serializable {

    @StructField(order = 0)
    public byte[] cVersion = new byte[4];    //版本号
    @StructField(order = 1)
    public int cAgentID;                //代理号
    @StructField(order = 2)
    public int iGuestID;                //客户号
    @StructField(order = 3)
    public byte cBasicSectorID;            //基本扇区号
    @StructField(order = 4)
    public byte cExtendSectorID;        //扩展扇区号
    @StructField(order = 5)
    public byte cCardMode;                //卡片识别模式 0：正常 1：只读M1
    @StructField(order = 6)
    public byte cAliPayCtlSDK;            ////支付宝管控启用 0：不启用 1：启用，2代扣
    @StructField(order = 7)
    public char cProxyServerLen;        //代理服务器长度	1字节
    @StructField(order = 8)
    public byte[] strProxyServer = new byte[256];    //代理服务器内容	X字节	支付宝管控SDK的代理服务器

    @StructField(order = 9)
    public char iFacehttpLength;                //人脸http地址长度 2字节
    @StructField(order = 10)
    public byte[] FaceHTTPServerAdr = new byte[256];          //人脸http地址

    @StructField(order = 11)
    public byte cFaceDetectFlag;          //是否支持人脸识别
    @StructField(order = 12)
    public byte cOnlyOnlineMode;            //在线交易模式 0：不启用 1：启用
    @StructField(order = 13)
    public byte[] Reserve = new byte[127];          //预留512
    @StructField(order = 14)
    public byte[] CRC16 = new byte[2];            //校验码       2字节

    public byte[] getcVersion() {
        return cVersion;
    }

    public void setcVersion(byte[] cVersion) {
        this.cVersion = cVersion;
    }

    public int getcAgentID() {
        return cAgentID;
    }

    public void setcAgentID(int cAgentID) {
        this.cAgentID = cAgentID;
    }

    public int getiGuestID() {
        return iGuestID;
    }

    public void setiGuestID(int iGuestID) {
        this.iGuestID = iGuestID;
    }

    public byte getcBasicSectorID() {
        return cBasicSectorID;
    }

    public void setcBasicSectorID(byte cBasicSectorID) {
        this.cBasicSectorID = cBasicSectorID;
    }

    public byte getcExtendSectorID() {
        return cExtendSectorID;
    }

    public void setcExtendSectorID(byte cExtendSectorID) {
        this.cExtendSectorID = cExtendSectorID;
    }

    public byte getcCardMode() {
        return cCardMode;
    }

    public void setcCardMode(byte cCardMode) {
        this.cCardMode = cCardMode;
    }

    public byte getcAliPayCtlSDK() {
        return cAliPayCtlSDK;
    }

    public void setcAliPayCtlSDK(byte cAliPayCtlSDK) {
        this.cAliPayCtlSDK = cAliPayCtlSDK;
    }

    public char getcProxyServerLen() {
        return cProxyServerLen;
    }

    public void setcProxyServerLen(char cProxyServerLen) {
        this.cProxyServerLen = cProxyServerLen;
    }

    public byte[] getStrProxyServer() {
        return strProxyServer;
    }

    public void setStrProxyServer(byte[] strProxyServer) {
        this.strProxyServer = strProxyServer;
    }

    public char getiFacehttpLength() {
        return iFacehttpLength;
    }

    public void setiFacehttpLength(char iFacehttpLength) {
        this.iFacehttpLength = iFacehttpLength;
    }

    public byte[] getFaceHTTPServerAdr() {
        return FaceHTTPServerAdr;
    }

    public void setFaceHTTPServerAdr(byte[] faceHTTPServerAdr) {
        FaceHTTPServerAdr = faceHTTPServerAdr;
    }

    public byte getcFaceDetectFlag() {
        return cFaceDetectFlag;
    }

    public void setcFaceDetectFlag(byte cFaceDetectFlag) {
        this.cFaceDetectFlag = cFaceDetectFlag;
    }

    public byte getcOnlyOnlineMode() {
        return cOnlyOnlineMode;
    }

    public void setcOnlyOnlineMode(byte cOnlyOnlineMode) {
        this.cOnlyOnlineMode = cOnlyOnlineMode;
    }

    public byte[] getReserve() {
        return Reserve;
    }

    public void setReserve(byte[] reserve) {
        Reserve = reserve;
    }

    public byte[] getCRC16() {
        return CRC16;
    }

    public void setCRC16(byte[] CRC16) {
        this.CRC16 = CRC16;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "cVersion=" + Arrays.toString(cVersion) +
                ", cAgentID=" + cAgentID +
                ", iGuestID=" + iGuestID +
                ", cBasicSectorID=" + cBasicSectorID +
                ", cExtendSectorID=" + cExtendSectorID +
                ", cCardMode=" + cCardMode +
                ", cAliPayCtlSDK=" + cAliPayCtlSDK +
                ", cProxyServerLen=" + cProxyServerLen +
                ", strProxyServer=" + Arrays.toString(strProxyServer) +
                ", iFacehttpLength=" + iFacehttpLength +
                ", FaceHTTPServerAdr=" + Arrays.toString(FaceHTTPServerAdr) +
                ", cFaceDetectFlag=" + cFaceDetectFlag +
                ", cOnlyOnlineMode=" + cOnlyOnlineMode +
                //", Reserve=" + Arrays.toString(Reserve) +
                ", CRC16=" + Arrays.toString(CRC16) +
                '}';
    }
}

package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class BasicInfo {

    @StructField(order = 0)
    public byte cDataFlashVer;               //存储Flash版本号
    @StructField(order = 1)
    public byte[] cTerminalSerID = new byte[6];             //终端序列号
    @StructField(order = 2)
    public byte[] cTerInCode = new byte[4];                 //终端内码
    @StructField(order = 3)
    public short cAgentID;                    //代理号
    @StructField(order = 4)
    public int iGuestID;                    //客户号
    @StructField(order = 5)
    public byte[] cCampusID = new byte[6];                //企业ID  (6字节)
    @StructField(order = 6)
    public byte[] cRFSIMKey = new byte[32];                //RFSIM卡使用密钥  (32字节)
    @StructField(order = 7)
    public byte[] cPlatSoftWareVer = new byte[12];       //平台软件版本号
    @StructField(order = 8)
    public byte[] cAppSoftWareVer = new byte[12];        //终端软件版本号(APP)
    @StructField(order = 9)
    public byte cSystemState;               //系统状态   80：初始化后的状态 90:设置网络参数开始
    @StructField(order = 10)
    public byte[] cMAC = new byte[6];                    //终端MAC地址
    @StructField(order = 11)
    public byte cGetLocalIPMode;               //获取本地IP方式 1:DHCP自动获取 0:手动输入

    @StructField(order = 12)
    public byte[] Reserve = new byte[512];          //预留512
    @StructField(order = 13)
    public byte[] CRC16 = new byte[2];            //校验码       2字节

    public byte getcDataFlashVer() {
        return cDataFlashVer;
    }

    public void setcDataFlashVer(byte cDataFlashVer) {
        this.cDataFlashVer = cDataFlashVer;
    }

    public byte[] getcTerminalSerID() {
        return cTerminalSerID;
    }

    public void setcTerminalSerID(byte[] cTerminalSerID) {
        this.cTerminalSerID = cTerminalSerID;
    }

    public byte[] getcTerInCode() {
        return cTerInCode;
    }

    public void setcTerInCode(byte[] cTerInCode) {
        this.cTerInCode = cTerInCode;
    }

    public short getcAgentID() {
        return cAgentID;
    }

    public void setcAgentID(short cAgentID) {
        this.cAgentID = cAgentID;
    }

    public int getiGuestID() {
        return iGuestID;
    }

    public void setiGuestID(int iGuestID) {
        this.iGuestID = iGuestID;
    }

    public byte[] getcCampusID() {
        return cCampusID;
    }

    public void setcCampusID(byte[] cCampusID) {
        this.cCampusID = cCampusID;
    }

    public byte[] getcRFSIMKey() {
        return cRFSIMKey;
    }

    public void setcRFSIMKey(byte[] cRFSIMKey) {
        this.cRFSIMKey = cRFSIMKey;
    }

    public byte[] getcPlatSoftWareVer() {
        return cPlatSoftWareVer;
    }

    public void setcPlatSoftWareVer(byte[] cPlatSoftWareVer) {
        this.cPlatSoftWareVer = cPlatSoftWareVer;
    }

    public byte[] getcAppSoftWareVer() {
        return cAppSoftWareVer;
    }

    public void setcAppSoftWareVer(byte[] cAppSoftWareVer) {
        this.cAppSoftWareVer = cAppSoftWareVer;
    }

    public byte getcSystemState() {
        return cSystemState;
    }

    public void setcSystemState(byte cSystemState) {
        this.cSystemState = cSystemState;
    }

    public byte[] getcMAC() {
        return cMAC;
    }

    public void setcMAC(byte[] cMAC) {
        this.cMAC = cMAC;
    }

    public byte getcGetLocalIPMode() {
        return cGetLocalIPMode;
    }

    public void setcGetLocalIPMode(byte cGetLocalIPMode) {
        this.cGetLocalIPMode = cGetLocalIPMode;
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
        return "BasicInfo{" +
                "cDataFlashVer=" + cDataFlashVer +
                ", cTerminalSerID=" + Arrays.toString(cTerminalSerID) +
                ", cTerInCode=" + Arrays.toString(cTerInCode) +
                ", cAgentID=" + cAgentID +
                ", iGuestID=" + iGuestID +
                ", cCampusID=" + Arrays.toString(cCampusID) +
                ", cRFSIMKey=" + Arrays.toString(cRFSIMKey) +
                ", cPlatSoftWareVer=" + Arrays.toString(cPlatSoftWareVer) +
                ", cAppSoftWareVer=" + Arrays.toString(cAppSoftWareVer) +
                ", cSystemState=" + cSystemState +
                ", cMAC=" + Arrays.toString(cMAC) +
                ", cGetLocalIPMode=" + cGetLocalIPMode +
                //", Reserve=" + Arrays.toString(Reserve) +
                ", CRC16=" + Arrays.toString(CRC16) +
                '}';
    }
}

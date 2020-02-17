package com.hzsun.mpos.CardWork;

import java.util.Arrays;

public class CardBaseInfo implements Cloneable {

    public int cAgentID;                //代理号
    public int iGuestID;               //客户号
    public int cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
    public byte[] cCardAuthenCode = new byte[4];      //卡认证码	4	通过卡号+代理号+客户序号+客户标识码
    public byte cCardState;         //卡片状态	1	取消原有卡户类型（临时卡/正式卡）数据，本字节全部用于存储卡片状态。
    //考虑到兼容原有卡结构，读写该字节时仍需取后4bit解析，原有读写代码不变。
    //0:无效卡/黑名单;1:有效卡
    public long lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
    public int cCampusID;         //园区号	1	范围为1~250
    public int cStatusID;          //身份编号	1	最大64种，1~64
    public int iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
    public byte[] bBasic1Context = new byte[16];            //基本扇区第1块内容

    public long lngAccountID;       //帐号	4	1～4294967296
    public byte[] cReservel = new byte[4];        //保留位	4	0
    public long lngPaymentPsw;       //交易密码	3	六位数字密码
    public byte cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
    public byte[] cReservel1 = new byte[4];        //保留位	4	补0
    public byte[] bBasic2Context = new byte[16];            //基本扇区第2块内容

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

    public int getcAuthenVer() {
        return cAuthenVer;
    }

    public void setcAuthenVer(int cAuthenVer) {
        this.cAuthenVer = cAuthenVer;
    }

    public byte[] getcCardAuthenCode() {
        return cCardAuthenCode;
    }

    public void setcCardAuthenCode(byte[] cCardAuthenCode) {
        this.cCardAuthenCode = cCardAuthenCode;
    }

    public byte getcCardState() {
        return cCardState;
    }

    public void setcCardState(byte cCardState) {
        this.cCardState = cCardState;
    }

    public long getLngCardID() {
        return lngCardID;
    }

    public void setLngCardID(long lngCardID) {
        this.lngCardID = lngCardID;
    }

    public int getcCampusID() {
        return cCampusID;
    }

    public void setcCampusID(int cCampusID) {
        this.cCampusID = cCampusID;
    }

    public int getcStatusID() {
        return cStatusID;
    }

    public void setcStatusID(int cStatusID) {
        this.cStatusID = cStatusID;
    }

    public int getiValidTime() {
        return iValidTime;
    }

    public void setiValidTime(int iValidTime) {
        this.iValidTime = iValidTime;
    }

    public byte[] getbBasic1Context() {
        return bBasic1Context;
    }

    public void setbBasic1Context(byte[] bBasic1Context) {
        this.bBasic1Context = bBasic1Context;
    }

    public long getLngAccountID() {
        return lngAccountID;
    }

    public void setLngAccountID(long lngAccountID) {
        this.lngAccountID = lngAccountID;
    }

    public byte[] getcReservel() {
        return cReservel;
    }

    public void setcReservel(byte[] cReservel) {
        this.cReservel = cReservel;
    }

    public long getLngPaymentPsw() {
        return lngPaymentPsw;
    }

    public void setLngPaymentPsw(long lngPaymentPsw) {
        this.lngPaymentPsw = lngPaymentPsw;
    }

    public byte getcCardStructVer() {
        return cCardStructVer;
    }

    public void setcCardStructVer(byte cCardStructVer) {
        this.cCardStructVer = cCardStructVer;
    }

    public byte[] getcReservel1() {
        return cReservel1;
    }

    public void setcReservel1(byte[] cReservel1) {
        this.cReservel1 = cReservel1;
    }

    public byte[] getbBasic2Context() {
        return bBasic2Context;
    }

    public void setbBasic2Context(byte[] bBasic2Context) {
        this.bBasic2Context = bBasic2Context;
    }

    //类的浅复制
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "CardBaseInfo{" +
                "cAgentID=" + cAgentID +
                ", iGuestID=" + iGuestID +
                ", cAuthenVer=" + cAuthenVer +
                ", cCardAuthenCode=" + Arrays.toString(cCardAuthenCode) +
                ", cCardState=" + cCardState +
                ", lngCardID=" + lngCardID +
                ", cCampusID=" + cCampusID +
                ", cStatusID=" + cStatusID +
                ", iValidTime=" + iValidTime +
                ", bBasic1Context=" + Arrays.toString(bBasic1Context) +
                ", lngAccountID=" + lngAccountID +
                ", cReservel=" + Arrays.toString(cReservel) +
                ", lngPaymentPsw=" + lngPaymentPsw +
                ", cCardStructVer=" + cCardStructVer +
                ", cReservel1=" + Arrays.toString(cReservel1) +
                ", bBasic2Context=" + Arrays.toString(bBasic2Context) +
                '}';
    }

}

package com.hzsun.mpos.CardWork;

import java.util.Arrays;

public class CardBurseInfo implements Cloneable {

    //钱包信息
    public int lngBurseMoney;             //钱包余额   3
    public int iSubsidySID;               //补助流水号  2
    public int iBurseSID;                    //钱包流水号 2
    public int iLastPayDate;              //钱包末笔交易日期   2
    public long lngDayPaymentTotal;         //当日消费累计额   2
    public int iLastBusinessID;            //最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
    public int iDayPaymentCount;            //当日消费累计次   1
    public byte[] cBurseAuthen = new byte[3];            //钱包认证码
    public byte[] bBurseContext = new byte[16];           //当前钱包块的内容
    public byte cIsBlockID;                  //判断是正本还是副本 0:正 1:副(如果是副本则需要记录一条断点恢复流水)

    public int getLngBurseMoney() {
        return lngBurseMoney;
    }

    public void setLngBurseMoney(int lngBurseMoney) {
        this.lngBurseMoney = lngBurseMoney;
    }

    public int getiSubsidySID() {
        return iSubsidySID;
    }

    public void setiSubsidySID(int iSubsidySID) {
        this.iSubsidySID = iSubsidySID;
    }

    public int getiBurseSID() {
        return iBurseSID;
    }

    public void setiBurseSID(int iBurseSID) {
        this.iBurseSID = iBurseSID;
    }

    public int getiLastPayDate() {
        return iLastPayDate;
    }

    public void setiLastPayDate(int iLastPayDate) {
        this.iLastPayDate = iLastPayDate;
    }

    public long getLngDayPaymentTotal() {
        return lngDayPaymentTotal;
    }

    public void setLngDayPaymentTotal(long lngDayPaymentTotal) {
        this.lngDayPaymentTotal = lngDayPaymentTotal;
    }

    public int getiLastBusinessID() {
        return iLastBusinessID;
    }

    public void setiLastBusinessID(int iLastBusinessID) {
        this.iLastBusinessID = iLastBusinessID;
    }

    public int getiDayPaymentCount() {
        return iDayPaymentCount;
    }

    public void setiDayPaymentCount(int iDayPaymentCount) {
        this.iDayPaymentCount = iDayPaymentCount;
    }

    public byte[] getcBurseAuthen() {
        return cBurseAuthen;
    }

    public void setcBurseAuthen(byte[] cBurseAuthen) {
        this.cBurseAuthen = cBurseAuthen;
    }

    public byte[] getbBurseContext() {
        return bBurseContext;
    }

    public void setbBurseContext(byte[] bBurseContext) {
        this.bBurseContext = bBurseContext;
    }

    public byte getcIsBlockID() {
        return cIsBlockID;
    }

    public void setcIsBlockID(byte cIsBlockID) {
        this.cIsBlockID = cIsBlockID;
    }

    //类的浅复制
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "CardBurseInfo{" +
                "lngBurseMoney=" + lngBurseMoney +
                ", iSubsidySID=" + iSubsidySID +
                ", iBurseSID=" + iBurseSID +
                ", iLastPayDate=" + iLastPayDate +
                ", lngDayPaymentTotal=" + lngDayPaymentTotal +
                ", iLastBusinessID=" + iLastBusinessID +
                ", iDayPaymentCount=" + iDayPaymentCount +
                ", cBurseAuthen=" + Arrays.toString(cBurseAuthen) +
                ", bBurseContext=" + Arrays.toString(bBurseContext) +
                ", cIsBlockID=" + cIsBlockID +
                '}';
    }
}

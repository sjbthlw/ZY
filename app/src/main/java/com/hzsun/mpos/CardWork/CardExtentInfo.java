package com.hzsun.mpos.CardWork;

import java.util.Arrays;

public class CardExtentInfo implements Cloneable {

    //扩展扇区
    public byte[] cAccName = new byte[16];         //卡户姓名
    public byte[] cSexState = new byte[2];          //性别
    public byte[] cCreateCardDate = new byte[3];    //开户日期 年月日
    public int iDepartID;            //部门编号
    public byte[] cOtherLinkID = new byte[10];      //第三方对接关键字
    public byte[] cCardPerCode = new byte[16];      //个人编号

    public byte[] getcAccName() {
        return cAccName;
    }

    public void setcAccName(byte[] cAccName) {
        this.cAccName = cAccName;
    }

    public byte[] getcSexState() {
        return cSexState;
    }

    public void setcSexState(byte[] cSexState) {
        this.cSexState = cSexState;
    }

    public byte[] getcCreateCardDate() {
        return cCreateCardDate;
    }

    public void setcCreateCardDate(byte[] cCreateCardDate) {
        this.cCreateCardDate = cCreateCardDate;
    }

    public int getiDepartID() {
        return iDepartID;
    }

    public void setiDepartID(int iDepartID) {
        this.iDepartID = iDepartID;
    }

    public byte[] getcOtherLinkID() {
        return cOtherLinkID;
    }

    public void setcOtherLinkID(byte[] cOtherLinkID) {
        this.cOtherLinkID = cOtherLinkID;
    }

    public byte[] getcCardPerCode() {
        return cCardPerCode;
    }

    public void setcCardPerCode(byte[] cCardPerCode) {
        this.cCardPerCode = cCardPerCode;
    }

    //类的浅复制
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "CardExtentInfo{" +
                "cAccName=" + Arrays.toString(cAccName) +
                ", cSexState=" + Arrays.toString(cSexState) +
                ", cCreateCardDate=" + Arrays.toString(cCreateCardDate) +
                ", iDepartID=" + iDepartID +
                ", cOtherLinkID=" + Arrays.toString(cOtherLinkID) +
                ", cCardPerCode=" + Arrays.toString(cCardPerCode) +
                '}';
    }
}

package com.hzsun.mpos.CardWork;

import java.util.Arrays;

public class CardAttrInfo implements Cloneable {

    public byte cCardType;        //卡片类型
    public byte cBasicSectorID;            //基本扇区号
    public byte cExtendSectorID;        //扩展扇区号

    public byte cWorkBurseID;        //工作钱包号
    public byte cChaseBurseID;       //追扣钱包号

    public byte[] cCardSID = new byte[16];        //卡号
    public byte cSAK;                //返回值 s70ka s50卡用

    public byte getcCardType() {
        return cCardType;
    }

    public void setcCardType(byte cCardType) {
        this.cCardType = cCardType;
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

    public byte getcWorkBurseID() {
        return cWorkBurseID;
    }

    public void setcWorkBurseID(byte cWorkBurseID) {
        this.cWorkBurseID = cWorkBurseID;
    }

    public byte getcChaseBurseID() {
        return cChaseBurseID;
    }

    public void setcChaseBurseID(byte cChaseBurseID) {
        this.cChaseBurseID = cChaseBurseID;
    }

    public byte[] getcCardSID() {
        return cCardSID;
    }

    public void setcCardSID(byte[] cCardSID) {
        this.cCardSID = cCardSID;
    }

    public byte getcSAK() {
        return cSAK;
    }

    public void setcSAK(byte cSAK) {
        this.cSAK = cSAK;
    }

    //类的浅复制
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "CardAttrInfo{" +
                "cCardType=" + cCardType +
                ", cBasicSectorID=" + cBasicSectorID +
                ", cExtendSectorID=" + cExtendSectorID +
                ", cWorkBurseID=" + cWorkBurseID +
                ", cChaseBurseID=" + cChaseBurseID +
                ", cCardSID=" + Arrays.toString(cCardSID) +
                ", cSAK=" + cSAK +
                '}';
    }
}

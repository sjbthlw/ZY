package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class BurseInfo implements Cloneable {
    @StructField(order = 0)
    public byte cBurseID;                 //钱包号	1字节
    @StructField(order = 1)
    public byte cCanPermitChase;            //是否允许追扣	1	0不允许 1允许
    @StructField(order = 2)
    public byte cCanMoneyClear;             //是否允许余额周期复位	1	0不允许 1允许
    @StructField(order = 3)
    public byte cMoneyClearUnit;            //复位周期单位	1	0年 1月
    @StructField(order = 4)
    public byte[] cMoneyClearTime = new byte[2];         //复位周期	1
    @StructField(order = 5)
    public byte cBlockID;                 //工作钱包对应块号	1	正本
    @StructField(order = 6)
    public byte cBakBlockID;             //工作钱包对应块号	1	备份

    @StructField(order = 7)
    public byte[] Reserve = new byte[32];          //预留32
    @StructField(order = 8)
    public byte[] CRC16 = new byte[2];            //校验码       2字节

    //类的浅复制
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    public byte getcBurseID() {
        return cBurseID;
    }

    public void setcBurseID(byte cBurseID) {
        this.cBurseID = cBurseID;
    }

    public byte getcCanPermitChase() {
        return cCanPermitChase;
    }

    public void setcCanPermitChase(byte cCanPermitChase) {
        this.cCanPermitChase = cCanPermitChase;
    }

    public byte getcCanMoneyClear() {
        return cCanMoneyClear;
    }

    public void setcCanMoneyClear(byte cCanMoneyClear) {
        this.cCanMoneyClear = cCanMoneyClear;
    }

    public byte getcMoneyClearUnit() {
        return cMoneyClearUnit;
    }

    public void setcMoneyClearUnit(byte cMoneyClearUnit) {
        this.cMoneyClearUnit = cMoneyClearUnit;
    }

    public byte[] getcMoneyClearTime() {
        return cMoneyClearTime;
    }

    public void setcMoneyClearTime(byte[] cMoneyClearTime) {
        this.cMoneyClearTime = cMoneyClearTime;
    }

    public byte getcBlockID() {
        return cBlockID;
    }

    public void setcBlockID(byte cBlockID) {
        this.cBlockID = cBlockID;
    }

    public byte getcBakBlockID() {
        return cBakBlockID;
    }

    public void setcBakBlockID(byte cBakBlockID) {
        this.cBakBlockID = cBakBlockID;
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
        return "BurseInfo{" +
                "cBurseID=" + cBurseID +
                ", cCanPermitChase=" + cCanPermitChase +
                ", cCanMoneyClear=" + cCanMoneyClear +
                ", cMoneyClearUnit=" + cMoneyClearUnit +
                ", cMoneyClearTime=" + Arrays.toString(cMoneyClearTime) +
                ", cBlockID=" + cBlockID +
                ", cBakBlockID=" + cBakBlockID +
                //", Reserve=" + Arrays.toString(Reserve) +
                ", CRC16=" + Arrays.toString(CRC16) +
                '}';
    }
}

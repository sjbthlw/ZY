package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class RecordInfo {

    @StructField(order = 0)
    public long lngMaxCardID;              //最大卡内编号
    @StructField(order = 1)
    public long lngPaymentRecordID;        //已记录最大交易流水号
    @StructField(order = 2)
    public long lngPaymentSendID;          //已上传最大交易流水号
    @StructField(order = 3)
    public int wTodayPaymentSum;          //当日交易笔数
    @StructField(order = 4)
    public long lngTodayPaymentMoney;      //当日交易总额
    @StructField(order = 5)
    public int wTotalBusinessSum;         //当餐营业笔数
    @StructField(order = 6)
    public long lngTotalBusinessMoney;     //当餐营业总额
    @StructField(order = 7)
    public long lngTotalPaymentMoney;      //累计交易总金额
    @StructField(order = 8)
    public byte[] cLastPaymentDate = new byte[6];       //末笔交易日期
    @StructField(order = 9)
    public byte cLastBusinessID;           //末笔交易营业号
    @StructField(order = 10)
    public byte[] cCurSignInDate = new byte[3];         //当前签到日期
    @StructField(order = 11)
    public int cCurEquipmentState;        //当前设备状态
    //第0位：存储故障；16进制值0x01。
    //第1位：键盘故障；16进制值0x02。
    //第2位：显示故障；16进制值0x04。
    //第3位: 	读头故障；16进制值0x08。


    public long getLngMaxCardID() {
        return lngMaxCardID;
    }

    public void setLngMaxCardID(long lngMaxCardID) {
        this.lngMaxCardID = lngMaxCardID;
    }

    public long getLngPaymentRecordID() {
        return lngPaymentRecordID;
    }

    public void setLngPaymentRecordID(long lngPaymentRecordID) {
        this.lngPaymentRecordID = lngPaymentRecordID;
    }

    public long getLngPaymentSendID() {
        return lngPaymentSendID;
    }

    public void setLngPaymentSendID(long lngPaymentSendID) {
        this.lngPaymentSendID = lngPaymentSendID;
    }

    public int getwTodayPaymentSum() {
        return wTodayPaymentSum;
    }

    public void setwTodayPaymentSum(int wTodayPaymentSum) {
        this.wTodayPaymentSum = wTodayPaymentSum;
    }

    public long getLngTodayPaymentMoney() {
        return lngTodayPaymentMoney;
    }

    public void setLngTodayPaymentMoney(long lngTodayPaymentMoney) {
        this.lngTodayPaymentMoney = lngTodayPaymentMoney;
    }

    public int getwTotalBusinessSum() {
        return wTotalBusinessSum;
    }

    public void setwTotalBusinessSum(int wTotalBusinessSum) {
        this.wTotalBusinessSum = wTotalBusinessSum;
    }

    public long getLngTotalBusinessMoney() {
        return lngTotalBusinessMoney;
    }

    public void setLngTotalBusinessMoney(long lngTotalBusinessMoney) {
        this.lngTotalBusinessMoney = lngTotalBusinessMoney;
    }

    public long getLngTotalPaymentMoney() {
        return lngTotalPaymentMoney;
    }

    public void setLngTotalPaymentMoney(long lngTotalPaymentMoney) {
        this.lngTotalPaymentMoney = lngTotalPaymentMoney;
    }

    public byte[] getcLastPaymentDate() {
        return cLastPaymentDate;
    }

    public void setcLastPaymentDate(byte[] cLastPaymentDate) {
        this.cLastPaymentDate = cLastPaymentDate;
    }

    public byte getcLastBusinessID() {
        return cLastBusinessID;
    }

    public void setcLastBusinessID(byte cLastBusinessID) {
        this.cLastBusinessID = cLastBusinessID;
    }

    public byte[] getcCurSignInDate() {
        return cCurSignInDate;
    }

    public void setcCurSignInDate(byte[] cCurSignInDate) {
        this.cCurSignInDate = cCurSignInDate;
    }

    public int getcCurEquipmentState() {
        return cCurEquipmentState;
    }

    public void setcCurEquipmentState(int cCurEquipmentState) {
        this.cCurEquipmentState = cCurEquipmentState;
    }

    @Override
    public String toString() {
        return "RecordInfo{" +
                "lngMaxCardID=" + lngMaxCardID +
                ", lngPaymentRecordID=" + lngPaymentRecordID +
                ", lngPaymentSendID=" + lngPaymentSendID +
                ", wTodayPaymentSum=" + wTodayPaymentSum +
                ", lngTodayPaymentMoney=" + lngTodayPaymentMoney +
                ", wTotalBusinessSum=" + wTotalBusinessSum +
                ", lngTotalBusinessMoney=" + lngTotalBusinessMoney +
                ", lngTotalPaymentMoney=" + lngTotalPaymentMoney +
                ", cLastPaymentDate=" + Arrays.toString(cLastPaymentDate) +
                ", cLastBusinessID=" + cLastBusinessID +
                ", cCurSignInDate=" + Arrays.toString(cCurSignInDate) +
                ", cCurEquipmentState=" + cCurEquipmentState +
                '}';
    }
}

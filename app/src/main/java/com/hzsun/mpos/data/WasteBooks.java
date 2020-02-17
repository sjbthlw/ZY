package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class WasteBooks implements Cloneable {

    //站点号
    @StructField(order = 0)
    public byte[] cStationID = new byte[2];
    //站点流水号
    @StructField(order = 1)
    public byte[] cPayRecordID = new byte[4];
    //终端机号
    @StructField(order = 2)
    public byte bWriteContext;
    //营业分组时段序号
    @StructField(order = 3)
    public byte cBusinessID;
    //商户号
    @StructField(order = 4)
    public byte[] cShopUserID = new byte[2];
    //卡内编号
    @StructField(order = 5)
    public byte[] cCardID = new byte[3];
    //交易钱包号
    @StructField(order = 6)
    public byte cBurseID;
    //钱包流水号
    @StructField(order = 7)
    public byte[] cBurseSID = new byte[2];
    //补助流水号
    @StructField(order = 8)
    public byte[] cSubsidySID = new byte[2];
    //终端流水号
    @StructField(order = 9)
    public byte[] cPaymentRecordID = new byte[3];
    //交易日期时间
    @StructField(order = 10)
    public byte[] bPaymentDate = new byte[4];
    //交易类型
    @StructField(order = 11)
    public byte cPaymentType;
    //交易金额
    @StructField(order = 12)
    public byte[] cPaymentMoney = new byte[2];
    //优惠金额
    @StructField(order = 13)
    public byte[] cPrivelegeMoney = new byte[2];
    //钱包卡余额
    @StructField(order = 14)
    public byte[] cBurseMoney = new byte[3];
    //累计交易总金额
    @StructField(order = 15)
    public byte[] cTotalPaymentMoney = new byte[4];
    //出纳员编号
    @StructField(order = 16)
    public byte[] CashierNum = new byte[2];
    //重传标志
    @StructField(order = 17)
    public byte cReSendFlag;
    //终端识别号
    @StructField(order = 18)
    public byte[] bEquipmentID = new byte[4];
    //校验码
    @StructField(order = 19)
    public byte[] CRC = new byte[2];

    //姓名
    @StructField(order = 20)
    public byte[] cAccName = new byte[16];
    //78字节
    //预留128-78=50字节
    @StructField(order = 21)
    public byte[] cReserve = new byte[50];

    //类的浅复制
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public byte[] getcStationID() {
        return cStationID;
    }

    public void setcStationID(byte[] cStationID) {
        this.cStationID = cStationID;
    }

    public byte[] getcPayRecordID() {
        return cPayRecordID;
    }

    public void setcPayRecordID(byte[] cPayRecordID) {
        this.cPayRecordID = cPayRecordID;
    }

    public byte getbWriteContext() {
        return bWriteContext;
    }

    public void setbWriteContext(byte bWriteContext) {
        this.bWriteContext = bWriteContext;
    }

    public byte getcBusinessID() {
        return cBusinessID;
    }

    public void setcBusinessID(byte cBusinessID) {
        this.cBusinessID = cBusinessID;
    }

    public byte[] getcShopUserID() {
        return cShopUserID;
    }

    public void setcShopUserID(byte[] cShopUserID) {
        this.cShopUserID = cShopUserID;
    }

    public byte[] getcCardID() {
        return cCardID;
    }

    public void setcCardID(byte[] cCardID) {
        this.cCardID = cCardID;
    }

    public byte getcBurseID() {
        return cBurseID;
    }

    public void setcBurseID(byte cBurseID) {
        this.cBurseID = cBurseID;
    }

    public byte[] getcBurseSID() {
        return cBurseSID;
    }

    public void setcBurseSID(byte[] cBurseSID) {
        this.cBurseSID = cBurseSID;
    }

    public byte[] getcSubsidySID() {
        return cSubsidySID;
    }

    public void setcSubsidySID(byte[] cSubsidySID) {
        this.cSubsidySID = cSubsidySID;
    }

    public byte[] getcPaymentRecordID() {
        return cPaymentRecordID;
    }

    public void setcPaymentRecordID(byte[] cPaymentRecordID) {
        this.cPaymentRecordID = cPaymentRecordID;
    }

    public byte[] getbPaymentDate() {
        return bPaymentDate;
    }

    public void setbPaymentDate(byte[] bPaymentDate) {
        this.bPaymentDate = bPaymentDate;
    }

    public byte getcPaymentType() {
        return cPaymentType;
    }

    public void setcPaymentType(byte cPaymentType) {
        this.cPaymentType = cPaymentType;
    }

    public byte[] getcPaymentMoney() {
        return cPaymentMoney;
    }

    public void setcPaymentMoney(byte[] cPaymentMoney) {
        this.cPaymentMoney = cPaymentMoney;
    }

    public byte[] getcPrivelegeMoney() {
        return cPrivelegeMoney;
    }

    public void setcPrivelegeMoney(byte[] cPrivelegeMoney) {
        this.cPrivelegeMoney = cPrivelegeMoney;
    }

    public byte[] getcBurseMoney() {
        return cBurseMoney;
    }

    public void setcBurseMoney(byte[] cBurseMoney) {
        this.cBurseMoney = cBurseMoney;
    }

    public byte[] getcTotalPaymentMoney() {
        return cTotalPaymentMoney;
    }

    public void setcTotalPaymentMoney(byte[] cTotalPaymentMoney) {
        this.cTotalPaymentMoney = cTotalPaymentMoney;
    }

    public byte[] getCashierNum() {
        return CashierNum;
    }

    public void setCashierNum(byte[] cashierNum) {
        CashierNum = cashierNum;
    }

    public byte getcReSendFlag() {
        return cReSendFlag;
    }

    public void setcReSendFlag(byte cReSendFlag) {
        this.cReSendFlag = cReSendFlag;
    }

    public byte[] getbEquipmentID() {
        return bEquipmentID;
    }

    public void setbEquipmentID(byte[] bEquipmentID) {
        this.bEquipmentID = bEquipmentID;
    }

    public byte[] getCRC() {
        return CRC;
    }

    public void setCRC(byte[] CRC) {
        this.CRC = CRC;
    }

    public byte[] getcAccName() {
        return cAccName;
    }

    public void setcAccName(byte[] cAccName) {
        this.cAccName = cAccName;
    }

    public byte[] getcReserve() {
        return cReserve;
    }

    public void setcReserve(byte[] cReserve) {
        this.cReserve = cReserve;
    }

    @Override
    public String toString() {
        return "WasteBooks{" +
                "cStationID=" + Arrays.toString(cStationID) +
                ", cPayRecordID=" + Arrays.toString(cPayRecordID) +
                ", bWriteContext=" + bWriteContext +
                ", cBusinessID=" + cBusinessID +
                ", cShopUserID=" + Arrays.toString(cShopUserID) +
                ", cCardID=" + Arrays.toString(cCardID) +
                ", cBurseID=" + cBurseID +
                ", cBurseSID=" + Arrays.toString(cBurseSID) +
                ", cSubsidySID=" + Arrays.toString(cSubsidySID) +
                ", cPaymentRecordID=" + Arrays.toString(cPaymentRecordID) +
                ", bPaymentDate=" + Arrays.toString(bPaymentDate) +
                ", cPaymentType=" + cPaymentType +
                ", cPaymentMoney=" + Arrays.toString(cPaymentMoney) +
                ", cPrivelegeMoney=" + Arrays.toString(cPrivelegeMoney) +
                ", cBurseMoney=" + Arrays.toString(cBurseMoney) +
                ", cTotalPaymentMoney=" + Arrays.toString(cTotalPaymentMoney) +
                ", CashierNum=" + Arrays.toString(CashierNum) +
                ", cReSendFlag=" + cReSendFlag +
                ", bEquipmentID=" + Arrays.toString(bEquipmentID) +
                ", CRC=" + Arrays.toString(CRC) +
                ", cAccName=" + Arrays.toString(cAccName) +
                ", cReserve=" + Arrays.toString(cReserve) +
                '}';
    }
}

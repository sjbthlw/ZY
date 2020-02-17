package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class WasteQrCodeBooks implements Cloneable {

    //站点号
    @StructField(order = 0)
    public byte[] cStationID = new byte[2];
    //商户号
    @StructField(order = 1)
    public byte[] cShopUserID = new byte[2];
    //二维码单号
    @StructField(order = 2)
    public byte[] cQrCodeID = new byte[4];
    //姓名
    @StructField(order = 3)
    public byte[] cAccName = new byte[16];
    //主卡帐号
    @StructField(order = 4)
    public byte[] cAccountID = new byte[4];
    //交易日期时间
    @StructField(order = 5)
    public byte[] cPaymentTime = new byte[4];
    //终端流水号
    @StructField(order = 6)
    public byte[] cPayRecordID = new byte[4];
    //交易类型
    @StructField(order = 7)
    public byte cPayType;
    //交易金额
    @StructField(order = 8)
    public byte[] cPaymentMoney = new byte[3];
    //优惠金额
    @StructField(order = 9)
    public byte[] cPrivelegeMoney = new byte[3];
    //管理费
    @StructField(order = 10)
    public byte[] cManageMoney = new byte[3];
    //钱包卡余额
    @StructField(order = 11)
    public byte[] cBurseMoney = new byte[3];
    //校验码
    @StructField(order = 12)
    public byte[] CRC = new byte[2];
    //48字节
    //预留128-51=77字节
    @StructField(order = 13)
    public byte[] cReserve = new byte[77];


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

    public byte[] getcShopUserID() {
        return cShopUserID;
    }

    public void setcShopUserID(byte[] cShopUserID) {
        this.cShopUserID = cShopUserID;
    }

    public byte[] getcQrCodeID() {
        return cQrCodeID;
    }

    public void setcQrCodeID(byte[] cQrCodeID) {
        this.cQrCodeID = cQrCodeID;
    }

    public byte[] getcAccName() {
        return cAccName;
    }

    public void setcAccName(byte[] cAccName) {
        this.cAccName = cAccName;
    }

    public byte[] getcAccountID() {
        return cAccountID;
    }

    public void setcAccountID(byte[] cAccountID) {
        this.cAccountID = cAccountID;
    }

    public byte[] getbPaymentDate() {
        return cPaymentTime;
    }

    public void setbPaymentDate(byte[] bPaymentDate) {
        this.cPaymentTime = bPaymentDate;
    }

    public byte[] getcPayRecordID() {
        return cPayRecordID;
    }

    public void setcPayRecordID(byte[] cPayRecordID) {
        this.cPayRecordID = cPayRecordID;
    }

    public byte getcPayType() {
        return cPayType;
    }

    public void setcPayType(byte cPayType) {
        this.cPayType = cPayType;
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

    public byte[] getcManageMoney() {
        return cManageMoney;
    }

    public void setcManageMoney(byte[] cManageMoney) {
        this.cManageMoney = cManageMoney;
    }

    public byte[] getcBurseMoney() {
        return cBurseMoney;
    }

    public void setcBurseMoney(byte[] cBurseMoney) {
        this.cBurseMoney = cBurseMoney;
    }

    public byte[] getCRC() {
        return CRC;
    }

    public void setCRC(byte[] CRC) {
        this.CRC = CRC;
    }

    public byte[] getcReserve() {
        return cReserve;
    }

    public void setcReserve(byte[] cReserve) {
        this.cReserve = cReserve;
    }

    @Override
    public String toString() {
        return "WasteQrCodeBooks{" +
                "cStationID=" + Arrays.toString(cStationID) +
                ", cShopUserID=" + Arrays.toString(cShopUserID) +
                ", cQrCodeID=" + Arrays.toString(cQrCodeID) +
                ", cAccName=" + Arrays.toString(cAccName) +
                ", cAccountID=" + Arrays.toString(cAccountID) +
                ", bPaymentDate=" + Arrays.toString(cPaymentTime) +
                ", cPayRecordID=" + Arrays.toString(cPayRecordID) +
                ", cPayType=" + cPayType +
                ", cPaymentMoney=" + Arrays.toString(cPaymentMoney) +
                ", cPrivelegeMoney=" + Arrays.toString(cPrivelegeMoney) +
                ", cManageMoney=" + Arrays.toString(cManageMoney) +
                ", cBurseMoney=" + Arrays.toString(cBurseMoney) +
                ", CRC=" + Arrays.toString(CRC) +
                ", cReserve=" + Arrays.toString(cReserve) +
                '}';
    }
}

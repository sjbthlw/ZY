package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class StatusBurInfo {

    @StructField(order = 0)
    public byte cBurseID;                //工作钱包号
    @StructField(order = 1)
    public byte cPrivilegeMode;         //优惠模式(0:折扣优惠 1:定额优惠 2:定额消费)
    @StructField(order = 2)
    public byte cPrivLimitOne;          //是否启用优惠限次
    @StructField(order = 3)
    public byte cPrivilegeDis;            //折扣优惠
    @StructField(order = 4)
    public int cBookPrivelege;        //定额优惠
    @StructField(order = 5)
    public int cBookMoney;            //定额消费
    @StructField(order = 6)
    public char cFundMoneyRate;         //存款手续费比率
    @StructField(order = 7)
    public char cFetchMoneyRate;        //取款手续费比率
    @StructField(order = 8)
    public long lngSinglePayPswLim;    //钱包单笔消费密码限额
    @StructField(order = 9)
    public long lngDayPayPswLim;        //钱包日累消费密码限额
    @StructField(order = 10)
    public long lngDayTotalMoneyLim;    //钱包日累消费金额上限
    @StructField(order = 11)
    public int cDayTotalCountLim;      //钱包日累消费次数上限
    @StructField(order = 12)
    public long lngBurseMoneyLim;        //钱包余额上限制
    @StructField(order = 13)
    public byte cManagementMode;        //钱包消费管理费模式	1字节	0:不启用,1:按比率管理费,2:按金额管理费
    @StructField(order = 14)
    public char cManageMoneyRate;        //钱包按比率管理费	1字节
    @StructField(order = 15)
    public int iManageMoney;            //钱包按金额管理费	2字节
    @StructField(order = 16)
    public byte[] Reserve = new byte[32];          //预留32
    @StructField(order = 17)
    public byte[] CRC16 = new byte[2];            //校验码       2字节

    public byte getcBurseID() {
        return cBurseID;
    }

    public void setcBurseID(byte cBurseID) {
        this.cBurseID = cBurseID;
    }

    public byte getcPrivilegeMode() {
        return cPrivilegeMode;
    }

    public void setcPrivilegeMode(byte cPrivilegeMode) {
        this.cPrivilegeMode = cPrivilegeMode;
    }

    public byte getcPrivLimitOne() {
        return cPrivLimitOne;
    }

    public void setcPrivLimitOne(byte cPrivLimitOne) {
        this.cPrivLimitOne = cPrivLimitOne;
    }

    public byte getcPrivilegeDis() {
        return cPrivilegeDis;
    }

    public void setcPrivilegeDis(byte cPrivilegeDis) {
        this.cPrivilegeDis = cPrivilegeDis;
    }

    public int getcBookPrivelege() {
        return cBookPrivelege;
    }

    public void setcBookPrivelege(int cBookPrivelege) {
        this.cBookPrivelege = cBookPrivelege;
    }

    public int getcBookMoney() {
        return cBookMoney;
    }

    public void setcBookMoney(int cBookMoney) {
        this.cBookMoney = cBookMoney;
    }

    public char getcFundMoneyRate() {
        return cFundMoneyRate;
    }

    public void setcFundMoneyRate(char cFundMoneyRate) {
        this.cFundMoneyRate = cFundMoneyRate;
    }

    public char getcFetchMoneyRate() {
        return cFetchMoneyRate;
    }

    public void setcFetchMoneyRate(char cFetchMoneyRate) {
        this.cFetchMoneyRate = cFetchMoneyRate;
    }

    public long getLngSinglePayPswLim() {
        return lngSinglePayPswLim;
    }

    public void setLngSinglePayPswLim(long lngSinglePayPswLim) {
        this.lngSinglePayPswLim = lngSinglePayPswLim;
    }

    public long getLngDayPayPswLim() {
        return lngDayPayPswLim;
    }

    public void setLngDayPayPswLim(long lngDayPayPswLim) {
        this.lngDayPayPswLim = lngDayPayPswLim;
    }

    public long getLngDayTotalMoneyLim() {
        return lngDayTotalMoneyLim;
    }

    public void setLngDayTotalMoneyLim(long lngDayTotalMoneyLim) {
        this.lngDayTotalMoneyLim = lngDayTotalMoneyLim;
    }

    public int getcDayTotalCountLim() {
        return cDayTotalCountLim;
    }

    public void setcDayTotalCountLim(int cDayTotalCountLim) {
        this.cDayTotalCountLim = cDayTotalCountLim;
    }

    public long getLngBurseMoneyLim() {
        return lngBurseMoneyLim;
    }

    public void setLngBurseMoneyLim(long lngBurseMoneyLim) {
        this.lngBurseMoneyLim = lngBurseMoneyLim;
    }

    public byte getcManagementMode() {
        return cManagementMode;
    }

    public void setcManagementMode(byte cManagementMode) {
        this.cManagementMode = cManagementMode;
    }

    public char getcManageMoneyRate() {
        return cManageMoneyRate;
    }

    public void setcManageMoneyRate(char cManageMoneyRate) {
        this.cManageMoneyRate = cManageMoneyRate;
    }

    public int getiManageMoney() {
        return iManageMoney;
    }

    public void setiManageMoney(int iManageMoney) {
        this.iManageMoney = iManageMoney;
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
        return "StatusBurInfo{" +
                "cBurseID=" + cBurseID +
                ", cPrivilegeMode=" + cPrivilegeMode +
                ", cPrivLimitOne=" + cPrivLimitOne +
                ", cPrivilegeDis=" + cPrivilegeDis +
                ", cBookPrivelege=" + cBookPrivelege +
                ", cBookMoney=" + cBookMoney +
                ", cFundMoneyRate=" + cFundMoneyRate +
                ", cFetchMoneyRate=" + cFetchMoneyRate +
                ", lngSinglePayPswLim=" + lngSinglePayPswLim +
                ", lngDayPayPswLim=" + lngDayPayPswLim +
                ", lngDayTotalMoneyLim=" + lngDayTotalMoneyLim +
                ", cDayTotalCountLim=" + cDayTotalCountLim +
                ", lngBurseMoneyLim=" + lngBurseMoneyLim +
                ", cManagementMode=" + cManagementMode +
                ", cManageMoneyRate=" + cManageMoneyRate +
                ", iManageMoney=" + iManageMoney +
                //", Reserve=" + Arrays.toString(Reserve) +
                ", CRC16=" + Arrays.toString(CRC16) +
                '}';
    }
}

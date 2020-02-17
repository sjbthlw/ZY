package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class StationInfo {

    @StructField(order = 0)
    public byte[] cVersion = new byte[4];              //版本号
    @StructField(order = 1)
    public int iStationID;              //站点号
    @StructField(order = 2)
    public int iStationClass;              //站点类型
    @StructField(order = 3)
    public byte cCanOffCount;              //允许脱机天数
    @StructField(order = 4)
    public byte[] bCampusArea = new byte[32];         //园区范围
    @StructField(order = 5)
    public byte cBWListClass;            //黑白名单类型
    @StructField(order = 6)
    public long lngOptionPsw;              //操作密码
    @StructField(order = 7)
    public long lngSetupPsw;              //设置密码
    @StructField(order = 8)
    public long lngAdvancePsw;              //高级密码
    @StructField(order = 9)
    public int iShopUserID;            //商户号
    @StructField(order = 10)
    public byte cWorkBurseID;           //工作钱包号
    @StructField(order = 11)
    public byte cChaseBurseID;         //被追扣钱包号
    @StructField(order = 12)
    public byte cTransBurseID;         //转帐钱包号
    @StructField(order = 13)
    public byte cUseCardClass;          //运用卡片类型
    @StructField(order = 14)
    public byte cRFUIMCardID;              //RFUIM卡运用几号卡
    @StructField(order = 15)
    public byte cSIMPassType;              //SIMPASS卡类型(M1,CPU部分)
    @StructField(order = 16)
    public byte cUseCardType;              //启用卡种(0-复旦微和SIMPASS卡都没有启用，1-只启用复旦微卡、2-只启用SIMPASS卡、3-复旦微和SIMPASS卡都启用)

    @StructField(order = 17)
    public byte cCanOffPayment;          //是否允许脱机交易
    @StructField(order = 18)
    public int lngOnPermitLimit;        //允许在线交易限额
    @StructField(order = 19)
    public int lngOffPermitLimit;       //脱机单笔交易限额
    @StructField(order = 20)
    public byte cCanPermitStat;         //是否允许查交易累计
    @StructField(order = 21)
    public byte cCanQuashPayment;       //是否允许撤销当前交易

    @StructField(order = 22)
    public byte cCanManagementFee;          //是否收取消费管理费
    @StructField(order = 23)
    public byte cPaymentUnit;              //消费单位 0:分 1:角 2:元
    @StructField(order = 24)
    public byte cCanStatusLimitFee;         //是否启用解除身份参数(日累超限、超额)限制 0:启用 1:不启用
    @StructField(order = 25)
    public byte cPaymentLimit;              //是否启用消费限次 0:no 1:yes
    @StructField(order = 26)
    public byte cCanChasePayment;          //启用消费限次后是否允许追扣消费 0:no 1 yes

    @StructField(order = 27)
    public byte[] Reserve = new byte[512];          //预留512
    @StructField(order = 28)
    public byte[] CRC16 = new byte[2];            //校验码       2字节

    public byte[] getcVersion() {
        return cVersion;
    }

    public void setcVersion(byte[] cVersion) {
        this.cVersion = cVersion;
    }

    public int getiStationID() {
        return iStationID;
    }

    public void setiStationID(int iStationID) {
        this.iStationID = iStationID;
    }

    public int getiStationClass() {
        return iStationClass;
    }

    public void setiStationClass(int iStationClass) {
        this.iStationClass = iStationClass;
    }

    public byte getcCanOffCount() {
        return cCanOffCount;
    }

    public void setcCanOffCount(byte cCanOffCount) {
        this.cCanOffCount = cCanOffCount;
    }

    public byte[] getbCampusArea() {
        return bCampusArea;
    }

    public void setbCampusArea(byte[] bCampusArea) {
        this.bCampusArea = bCampusArea;
    }

    public byte getcBWListClass() {
        return cBWListClass;
    }

    public void setcBWListClass(byte cBWListClass) {
        this.cBWListClass = cBWListClass;
    }

    public long getLngOptionPsw() {
        return lngOptionPsw;
    }

    public void setLngOptionPsw(long lngOptionPsw) {
        this.lngOptionPsw = lngOptionPsw;
    }

    public long getLngSetupPsw() {
        return lngSetupPsw;
    }

    public void setLngSetupPsw(long lngSetupPsw) {
        this.lngSetupPsw = lngSetupPsw;
    }

    public long getLngAdvancePsw() {
        return lngAdvancePsw;
    }

    public void setLngAdvancePsw(long lngAdvancePsw) {
        this.lngAdvancePsw = lngAdvancePsw;
    }

    public int getiShopUserID() {
        return iShopUserID;
    }

    public void setiShopUserID(int iShopUserID) {
        this.iShopUserID = iShopUserID;
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

    public byte getcTransBurseID() {
        return cTransBurseID;
    }

    public void setcTransBurseID(byte cTransBurseID) {
        this.cTransBurseID = cTransBurseID;
    }

    public byte getcUseCardClass() {
        return cUseCardClass;
    }

    public void setcUseCardClass(byte cUseCardClass) {
        this.cUseCardClass = cUseCardClass;
    }

    public byte getcRFUIMCardID() {
        return cRFUIMCardID;
    }

    public void setcRFUIMCardID(byte cRFUIMCardID) {
        this.cRFUIMCardID = cRFUIMCardID;
    }

    public byte getcSIMPassType() {
        return cSIMPassType;
    }

    public void setcSIMPassType(byte cSIMPassType) {
        this.cSIMPassType = cSIMPassType;
    }

    public byte getcUseCardType() {
        return cUseCardType;
    }

    public void setcUseCardType(byte cUseCardType) {
        this.cUseCardType = cUseCardType;
    }

    public byte getcCanOffPayment() {
        return cCanOffPayment;
    }

    public void setcCanOffPayment(byte cCanOffPayment) {
        this.cCanOffPayment = cCanOffPayment;
    }

    public int getLngOnPermitLimit() {
        return lngOnPermitLimit;
    }

    public void setLngOnPermitLimit(int lngOnPermitLimit) {
        this.lngOnPermitLimit = lngOnPermitLimit;
    }

    public int getLngOffPermitLimit() {
        return lngOffPermitLimit;
    }

    public void setLngOffPermitLimit(int lngOffPermitLimit) {
        this.lngOffPermitLimit = lngOffPermitLimit;
    }

    public byte getcCanPermitStat() {
        return cCanPermitStat;
    }

    public void setcCanPermitStat(byte cCanPermitStat) {
        this.cCanPermitStat = cCanPermitStat;
    }

    public byte getcCanQuashPayment() {
        return cCanQuashPayment;
    }

    public void setcCanQuashPayment(byte cCanQuashPayment) {
        this.cCanQuashPayment = cCanQuashPayment;
    }

    public byte getcCanManagementFee() {
        return cCanManagementFee;
    }

    public void setcCanManagementFee(byte cCanManagementFee) {
        this.cCanManagementFee = cCanManagementFee;
    }

    public byte getcPaymentUnit() {
        return cPaymentUnit;
    }

    public void setcPaymentUnit(byte cPaymentUnit) {
        this.cPaymentUnit = cPaymentUnit;
    }

    public byte getcCanStatusLimitFee() {
        return cCanStatusLimitFee;
    }

    public void setcCanStatusLimitFee(byte cCanStatusLimitFee) {
        this.cCanStatusLimitFee = cCanStatusLimitFee;
    }

    public byte getcPaymentLimit() {
        return cPaymentLimit;
    }

    public void setcPaymentLimit(byte cPaymentLimit) {
        this.cPaymentLimit = cPaymentLimit;
    }

    public byte getcCanChasePayment() {
        return cCanChasePayment;
    }

    public void setcCanChasePayment(byte cCanChasePayment) {
        this.cCanChasePayment = cCanChasePayment;
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
        return "StationInfo{" +
                "cVersion=" + Arrays.toString(cVersion) +
                ", iStationID=" + iStationID +
                ", iStationClass=" + iStationClass +
                ", cCanOffCount=" + cCanOffCount +
                ", bCampusArea=" + Arrays.toString(bCampusArea) +
                ", cBWListClass=" + cBWListClass +
                ", lngOptionPsw=" + lngOptionPsw +
                ", lngSetupPsw=" + lngSetupPsw +
                ", lngAdvancePsw=" + lngAdvancePsw +
                ", iShopUserID=" + iShopUserID +
                ", cWorkBurseID=" + cWorkBurseID +
                ", cChaseBurseID=" + cChaseBurseID +
                ", cTransBurseID=" + cTransBurseID +
                ", cUseCardClass=" + cUseCardClass +
                ", cRFUIMCardID=" + cRFUIMCardID +
                ", cSIMPassType=" + cSIMPassType +
                ", cUseCardType=" + cUseCardType +
                ", cCanOffPayment=" + cCanOffPayment +
                ", lngOnPermitLimit=" + lngOnPermitLimit +
                ", lngOffPermitLimit=" + lngOffPermitLimit +
                ", cCanPermitStat=" + cCanPermitStat +
                ", cCanQuashPayment=" + cCanQuashPayment +
                ", cCanManagementFee=" + cCanManagementFee +
                ", cPaymentUnit=" + cPaymentUnit +
                ", cCanStatusLimitFee=" + cCanStatusLimitFee +
                ", cPaymentLimit=" + cPaymentLimit +
                ", cCanChasePayment=" + cCanChasePayment +
                //", Reserve=" + Arrays.toString(Reserve) +
                ", CRC16=" + Arrays.toString(CRC16) +
                '}';
    }
}

package com.hzsun.mpos.CardWork;

import java.util.Arrays;

public class CardBasicParaInfo implements Cloneable {

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

    //扩展扇区
    public byte[] cAccName = new byte[16];         //卡户姓名
    public byte[] cSexState = new byte[2];          //性别
    public byte[] cCreateCardDate = new byte[3];    //开户日期 年月日
    public int iDepartID;            //部门编号
    public byte[] cOtherLinkID = new byte[10];      //第三方对接关键字
    public byte[] cCardPerCode = new byte[16];      //个人编号

    //钱包信息
    public int lngWorkBurseMoney;             //钱包余额   3
    public int iWorkSubsidySID;               //补助流水号  2
    public int iWorkBurseSID;                    //钱包流水号 2
    public int iWorkLastPayDate;              //钱包末笔交易日期   2
    public long lngWorkDayPaymentTotal;         //当日消费累计额   2
    public int iWorkLastBusinessID;            //最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
    public int iWorkDayPaymentCount;            //当日消费累计次   1
    public byte[] cWorkBurseAuthen = new byte[3];            //钱包认证码
    public byte[] bWorkBurseContext = new byte[16];           //当前钱包块的内容

    //追扣钱包信息
    public int lngChaseBurseMoney;             //主钱包余额
    public int iChaseSubsidySID;               //补助流水号
    public int iChaseBurseSID;                 //主钱包流水号
    public int iChaseLastPayDate;              //主钱包末笔交易日期
    public long lngChaseDayPaymentTotal;        //当日消费累计额
    public int iChaseLastBusinessID;           //末笔交易营业号
    public int iChaseDayPaymentCount;            //当日消费累计次
    public byte[] cChaseBurseAuthen = new byte[3];           //钱包认证码
    public byte[] bChaseBurseContext = new byte[16];          //当前钱包块的内容

    public byte cBurseID;                //当前钱包号
    public byte cInStepState;            //是否有圈存

    public byte cReWriteCardFlag;       //断点恢复标记     前4位是工作钱包    后4位是追扣钱包
    public byte cNOWriteCardFlag;       //断点拔卡标记     前4位是工作钱包    后4位是追扣钱包

    public long lngPayMoney;            //交易金额
    public long lngWorkPayMoney;        //工作钱包交易金额
    public long lngChasePayMoney;       //追扣钱包交易金额
    public long lngManageMoney;         //管理费金额
    public long lngPriMoney;            //优惠金额
    public long lngSubMoney;            //补助金额
    public int wSubsidySum;            //补助笔数(补助版本号之差)

    public long lngInPayMoney;            //输入金额

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

    public int getLngWorkBurseMoney() {
        return lngWorkBurseMoney;
    }

    public void setLngWorkBurseMoney(int lngWorkBurseMoney) {
        this.lngWorkBurseMoney = lngWorkBurseMoney;
    }

    public int getiWorkSubsidySID() {
        return iWorkSubsidySID;
    }

    public void setiWorkSubsidySID(int iWorkSubsidySID) {
        this.iWorkSubsidySID = iWorkSubsidySID;
    }

    public int getiWorkBurseSID() {
        return iWorkBurseSID;
    }

    public void setiWorkBurseSID(int iWorkBurseSID) {
        this.iWorkBurseSID = iWorkBurseSID;
    }

    public int getiWorkLastPayDate() {
        return iWorkLastPayDate;
    }

    public void setiWorkLastPayDate(int iWorkLastPayDate) {
        this.iWorkLastPayDate = iWorkLastPayDate;
    }

    public long getLngWorkDayPaymentTotal() {
        return lngWorkDayPaymentTotal;
    }

    public void setLngWorkDayPaymentTotal(long lngWorkDayPaymentTotal) {
        this.lngWorkDayPaymentTotal = lngWorkDayPaymentTotal;
    }

    public int getiWorkLastBusinessID() {
        return iWorkLastBusinessID;
    }

    public void setiWorkLastBusinessID(int iWorkLastBusinessID) {
        this.iWorkLastBusinessID = iWorkLastBusinessID;
    }

    public int getiWorkDayPaymentCount() {
        return iWorkDayPaymentCount;
    }

    public void setiWorkDayPaymentCount(int iWorkDayPaymentCount) {
        this.iWorkDayPaymentCount = iWorkDayPaymentCount;
    }

    public byte[] getcWorkBurseAuthen() {
        return cWorkBurseAuthen;
    }

    public void setcWorkBurseAuthen(byte[] cWorkBurseAuthen) {
        this.cWorkBurseAuthen = cWorkBurseAuthen;
    }

    public byte[] getbWorkBurseContext() {
        return bWorkBurseContext;
    }

    public void setbWorkBurseContext(byte[] bWorkBurseContext) {
        this.bWorkBurseContext = bWorkBurseContext;
    }

    public int getLngChaseBurseMoney() {
        return lngChaseBurseMoney;
    }

    public void setLngChaseBurseMoney(int lngChaseBurseMoney) {
        this.lngChaseBurseMoney = lngChaseBurseMoney;
    }

    public int getiChaseSubsidySID() {
        return iChaseSubsidySID;
    }

    public void setiChaseSubsidySID(int iChaseSubsidySID) {
        this.iChaseSubsidySID = iChaseSubsidySID;
    }

    public int getiChaseBurseSID() {
        return iChaseBurseSID;
    }

    public void setiChaseBurseSID(int iChaseBurseSID) {
        this.iChaseBurseSID = iChaseBurseSID;
    }

    public int getiChaseLastPayDate() {
        return iChaseLastPayDate;
    }

    public void setiChaseLastPayDate(int iChaseLastPayDate) {
        this.iChaseLastPayDate = iChaseLastPayDate;
    }

    public long getLngChaseDayPaymentTotal() {
        return lngChaseDayPaymentTotal;
    }

    public void setLngChaseDayPaymentTotal(long lngChaseDayPaymentTotal) {
        this.lngChaseDayPaymentTotal = lngChaseDayPaymentTotal;
    }

    public int getiChaseLastBusinessID() {
        return iChaseLastBusinessID;
    }

    public void setiChaseLastBusinessID(int iChaseLastBusinessID) {
        this.iChaseLastBusinessID = iChaseLastBusinessID;
    }

    public int getiChaseDayPaymentCount() {
        return iChaseDayPaymentCount;
    }

    public void setiChaseDayPaymentCount(int iChaseDayPaymentCount) {
        this.iChaseDayPaymentCount = iChaseDayPaymentCount;
    }

    public byte[] getcChaseBurseAuthen() {
        return cChaseBurseAuthen;
    }

    public void setcChaseBurseAuthen(byte[] cChaseBurseAuthen) {
        this.cChaseBurseAuthen = cChaseBurseAuthen;
    }

    public byte[] getbChaseBurseContext() {
        return bChaseBurseContext;
    }

    public void setbChaseBurseContext(byte[] bChaseBurseContext) {
        this.bChaseBurseContext = bChaseBurseContext;
    }

    public long getLngPayMoney() {
        return lngPayMoney;
    }

    public void setLngPayMoney(long lngPayMoney) {
        this.lngPayMoney = lngPayMoney;
    }

    public long getLngWorkPayMoney() {
        return lngWorkPayMoney;
    }

    public void setLngWorkPayMoney(long lngWorkPayMoney) {
        this.lngWorkPayMoney = lngWorkPayMoney;
    }

    public long getLngChasePayMoney() {
        return lngChasePayMoney;
    }

    public void setLngChasePayMoney(long lngChasePayMoney) {
        this.lngChasePayMoney = lngChasePayMoney;
    }

    public long getLngManageMoney() {
        return lngManageMoney;
    }

    public void setLngManageMoney(long lngManageMoney) {
        this.lngManageMoney = lngManageMoney;
    }

    public long getLngPriMoney() {
        return lngPriMoney;
    }

    public void setLngPriMoney(long lngPriMoney) {
        this.lngPriMoney = lngPriMoney;
    }

    public long getLngSubMoney() {
        return lngSubMoney;
    }

    public void setLngSubMoney(long lngSubMoney) {
        this.lngSubMoney = lngSubMoney;
    }

    public int getwSubsidySum() {
        return wSubsidySum;
    }

    public void setwSubsidySum(int wSubsidySum) {
        this.wSubsidySum = wSubsidySum;
    }

    public long getLngInPayMoney() {
        return lngInPayMoney;
    }

    public void setLngInPayMoney(long lngInPayMoney) {
        this.lngInPayMoney = lngInPayMoney;
    }

    //类的浅复制
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "CardBasicParaInfo{" +
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
                ", cAccName=" + Arrays.toString(cAccName) +
                ", cSexState=" + Arrays.toString(cSexState) +
                ", cCreateCardDate=" + Arrays.toString(cCreateCardDate) +
                ", iDepartID=" + iDepartID +
                ", cOtherLinkID=" + Arrays.toString(cOtherLinkID) +
                ", cCardPerCode=" + Arrays.toString(cCardPerCode) +
                ", lngWorkBurseMoney=" + lngWorkBurseMoney +
                ", iWorkSubsidySID=" + iWorkSubsidySID +
                ", iWorkBurseSID=" + iWorkBurseSID +
                ", iWorkLastPayDate=" + iWorkLastPayDate +
                ", lngWorkDayPaymentTotal=" + lngWorkDayPaymentTotal +
                ", iWorkLastBusinessID=" + iWorkLastBusinessID +
                ", iWorkDayPaymentCount=" + iWorkDayPaymentCount +
                ", cWorkBurseAuthen=" + Arrays.toString(cWorkBurseAuthen) +
                ", bWorkBurseContext=" + Arrays.toString(bWorkBurseContext) +
                ", lngChaseBurseMoney=" + lngChaseBurseMoney +
                ", iChaseSubsidySID=" + iChaseSubsidySID +
                ", iChaseBurseSID=" + iChaseBurseSID +
                ", iChaseLastPayDate=" + iChaseLastPayDate +
                ", lngChaseDayPaymentTotal=" + lngChaseDayPaymentTotal +
                ", iChaseLastBusinessID=" + iChaseLastBusinessID +
                ", iChaseDayPaymentCount=" + iChaseDayPaymentCount +
                ", cChaseBurseAuthen=" + Arrays.toString(cChaseBurseAuthen) +
                ", bChaseBurseContext=" + Arrays.toString(bChaseBurseContext) +
                ", lngPayMoney=" + lngPayMoney +
                ", lngWorkPayMoney=" + lngWorkPayMoney +
                ", lngChasePayMoney=" + lngChasePayMoney +
                ", lngManageMoney=" + lngManageMoney +
                ", lngPriMoney=" + lngPriMoney +
                ", lngSubMoney=" + lngSubMoney +
                ", wSubsidySum=" + wSubsidySum +
                ", lngInPayMoney=" + lngInPayMoney +
                '}';
    }
}

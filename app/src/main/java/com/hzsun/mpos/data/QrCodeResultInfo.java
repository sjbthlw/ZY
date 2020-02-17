package com.hzsun.mpos.data;

public class QrCodeResultInfo {

    //二维码单号
    public long lngOrderNum;    //订单号
    //卡户姓名
    public byte[] cAccName = new byte[16];
    //个人编号
    public byte[] cPerCode = new byte[16];    //人员编号	16
    //主卡帐号
    public long lngAccountID;       //账号	4
    //交易时间
    public byte[] cPaymentTime = new byte[4];
    //交易类型
    public byte cPaymentType;
    //交易金额
    public long lngPayMoney;
    //优惠金额
    public int wPrivelegeMoney;
    //管理费
    public int wManageMoney;
    //钱包卡余额
    public int lngBurseMoney;
    //交易钱包号	1字节
    public byte cBurseID;
    //末笔冲正用订单号
    public long lngLastOrderNum;

}

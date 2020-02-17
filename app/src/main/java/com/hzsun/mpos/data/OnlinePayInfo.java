package com.hzsun.mpos.data;

public class OnlinePayInfo {

    public byte cType;       //类型	1 （帐户：1 ；商户：2 ；商户（带交易金额）：3；考勤 4 ；门禁 5）
    public long lngAccountID;       //账号	4
    public long lngOrderNum;    //订单号
    public byte[] cAccName = new byte[16];       //姓名	Utf-8 ,X2格式显示
    public byte[] cPerCode = new byte[16];    //人员编号	16
    public int lngBurseMoney;  //余额
    public int lngPayMoney;    //交易金额
    public int lngPriMoney;    //优惠金额
    public int lngManageMoney; //管理费金额
    public long lngLastOrderNum;    //末笔冲正用订单号
}

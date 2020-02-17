package com.hzsun.mpos.data;

public class QRCodeCardHInfo {

    //持卡人二维码信息

    public byte cVersion;    //版本号	数字，从1开始
    public byte cType;       //类型	1 （帐户：1 ；商户：2 ；商户（带交易金额）：3；考勤 4 ；门禁 5）
    public int cAgentID;                 //代理号
    public int iGuestID;                //客户号
    public long lngAccountID;       //账号	4
    public byte[] cAccName = new byte[16];       //姓名	Utf-8 ,X2格式显示
    public byte[] cPerCode = new byte[16];    //人员编号	16
    public byte[] cRandom = new byte[4];       //随机数
    public long lngOrderNum;    //订单号

}

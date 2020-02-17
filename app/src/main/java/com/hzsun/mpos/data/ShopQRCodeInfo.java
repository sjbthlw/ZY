package com.hzsun.mpos.data;

public class ShopQRCodeInfo {

    public byte cVersion;    //版本号	数字，从1开始
    public byte cType;       //类型	1 （帐户：1 ；商户：2 ；商户（带交易金额）：3；考勤 4 ；门禁 5）
    public int cAgentID;                 //代理号
    public int iGuestID;                //客户号
    public int wShopUserID;                //本机商户号
    public long lngPayMoney;            //交易金额	单位分
    public byte[] cDeviceID = new byte[4];             //设备编号--//站点号
    public int wTerminalID;              //终端机号
    public long lngOrderNum;            //订单号

}

package com.hzsun.mpos.data;

//末笔订单号信息
public class LastOrderInfo {

    public long lngLastOrderID;
    public int cLen;
    public byte cType;
    public byte[] cQRCodeInfo = new byte[512];
    public long lngAccID;
    public byte cBurseID;
    public long lngPayMoney;
    public int wPriMoney;
    public int wManageMoney;
    public byte[] cAccName = new byte[16];
    public byte[] cPerCode = new byte[16];

}

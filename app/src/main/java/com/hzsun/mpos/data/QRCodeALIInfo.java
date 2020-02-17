package com.hzsun.mpos.data;

//支付宝QR信息
public class QRCodeALIInfo {

    public byte cVersion;            //版本号	数字，从1开始
    public int iLen;                //二维码长度
    public byte cType;               //二维码类型
    public byte[] cQRCodeInfo = new byte[1024];             //二维码内容
    public int iSignLen;
    public byte[] cSign = new byte[1024];        //支付宝管控签名

}

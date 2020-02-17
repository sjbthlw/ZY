package com.hzsun.mpos.data;

public class FacePayInfo {

    public byte cVersion;    //版本号	数字，从1开始
    public byte cType;       //类型	1 （帐户：1 ；商户：2 ；商户（带交易金额）：3；考勤 4 ；门禁 5）
    public long lngAccountID;       //账号	4
    public byte[] cPayDataTime = new byte[6];       //日期时间
    public byte[] cAccName = new byte[16];       //姓名	Utf-8 ,X2格式显示
    public byte[] cPerCode = new byte[16];    //人员编号	16
    public long lngOrderNum;    //订单号
    public float fMatchScore;   //人脸识别度
}

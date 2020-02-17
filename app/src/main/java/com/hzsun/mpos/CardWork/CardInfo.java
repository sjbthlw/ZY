package com.hzsun.mpos.CardWork;

public class CardInfo {

    public byte cExistState;                   //卡片存在状态
    public byte cAuthenState;                  //卡片认证状态
    public byte cCardType;                        //卡片类型
    public byte cCardStatus;                     //卡片类型状态
    public int iCasherID;                        //钥匙卡编号
    public byte[] bCasherSerID = new byte[4];               //钥匙卡序列号
    public long lngCasherPsw;                    //钥匙卡密钥
    public byte[] bCardSerialID = new byte[8];              //正式卡序列号
    public byte[] bCardSerTempID = new byte[8];            //临时卡序列号
    public byte[] bCardDispelSerID = new byte[8];            //冲正临时卡序列号
    public byte[] bCardSerRecordID = new byte[8];            //临时记录的卡序列号

    //界面显示类型
    public byte ucType;
}

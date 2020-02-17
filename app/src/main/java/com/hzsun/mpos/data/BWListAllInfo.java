package com.hzsun.mpos.data;

public class BWListAllInfo {

    private final static int MAXCARDBIT = 126976;//1000000/8(4096*31=126976)

    public BWListInfo BlackWListInfo = new BWListInfo();
    //黑白名单
    public byte[] BlackBit = new byte[MAXCARDBIT];
}

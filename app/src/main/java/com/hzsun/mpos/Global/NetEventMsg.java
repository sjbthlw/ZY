package com.hzsun.mpos.Global;

public class NetEventMsg {

    public byte type;
    public byte cConnectState;            //服务器连接状态
    public byte cMsgResState;            //报文应答状态1:接收到报文26:网络超时错误
    public long lngNetTimeout;
    public int wMsgTimeoutCount;       //发送报文超时次数
}

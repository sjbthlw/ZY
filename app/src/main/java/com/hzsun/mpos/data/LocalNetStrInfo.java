package com.hzsun.mpos.data;

public class LocalNetStrInfo {
    public int IPMode;//获取本机IP方式 0 DHCP 1 STATIC
    public String strLocalIP;//本机IP地址
    public String strSubNetMask;//子网掩码
    public String strGateWay;//网关IP地址
    public String strMasterDNS;//主DNS
    public String strSlaveDNS;//从DNS
    public String strMAC;//MAC地址
    public String strServerIP1;//统一调度服务前置//服务器IP地址
    public String strServerIP2;//设备接入网关//服务器IP地址
    public int ServerPort1;//统一调度服务前置//服务器监听端口号
    public int ServerPort2;//设备接入网关//服务器监听端口号
}

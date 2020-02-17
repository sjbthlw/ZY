package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class LocalNetInfo {

    @StructField(order = 0)
    public int IPMode;//获取本机IP方式 0 DHCP 1 STATIC
    @StructField(order = 1)
    public byte[] LocalIP = new byte[4];//本机IP地址
    @StructField(order = 2)
    public byte[] SubNetMask = new byte[4];//子网掩码
    @StructField(order = 3)
    public byte[] GateWay = new byte[4];//网关IP地址
    @StructField(order = 4)
    public byte[] MasterDNS = new byte[4];//主DNS
    @StructField(order = 5)
    public byte[] SlaveDNS = new byte[4];//从DNS
    @StructField(order = 6)
    public byte[] cMAC = new byte[6];//MAC地址
    @StructField(order = 7)
    public byte[] ServerIP1 = new byte[4];//统一调度服务前置//服务器IP地址
    @StructField(order = 8)
    public byte[] ServerIP2 = new byte[4];//设备接入网关//服务器IP地址
    @StructField(order = 9)
    public int ServerPort1;//统一调度服务前置//服务器监听端口号
    @StructField(order = 10)
    public int ServerPort2;//设备接入网关//服务器监听端口号


    public int getIPMode() {
        return IPMode;
    }

    public void setIPMode(int IPMode) {
        this.IPMode = IPMode;
    }

    public byte[] getLocalIP() {
        return LocalIP;
    }

    public void setLocalIP(byte[] localIP) {
        LocalIP = localIP;
    }

    public byte[] getSubNetMask() {
        return SubNetMask;
    }

    public void setSubNetMask(byte[] subNetMask) {
        SubNetMask = subNetMask;
    }

    public byte[] getGateWay() {
        return GateWay;
    }

    public void setGateWay(byte[] gateWay) {
        GateWay = gateWay;
    }

    public byte[] getMasterDNS() {
        return MasterDNS;
    }

    public void setMasterDNS(byte[] masterDNS) {
        MasterDNS = masterDNS;
    }

    public byte[] getSlaveDNS() {
        return SlaveDNS;
    }

    public void setSlaveDNS(byte[] slaveDNS) {
        SlaveDNS = slaveDNS;
    }

    public byte[] getcMAC() {
        return cMAC;
    }

    public void setcMAC(byte[] cMAC) {
        this.cMAC = cMAC;
    }

    public byte[] getServerIP1() {
        return ServerIP1;
    }

    public void setServerIP1(byte[] serverIP1) {
        ServerIP1 = serverIP1;
    }

    public byte[] getServerIP2() {
        return ServerIP2;
    }

    public void setServerIP2(byte[] serverIP2) {
        ServerIP2 = serverIP2;
    }

    public int getServerPort1() {
        return ServerPort1;
    }

    public void setServerPort1(int serverPort1) {
        ServerPort1 = serverPort1;
    }

    public int getServerPort2() {
        return ServerPort2;
    }

    public void setServerPort2(int serverPort2) {
        ServerPort2 = serverPort2;
    }

    @Override
    public String toString() {
        return "LocalNetInfo{" +
                "IPMode=" + IPMode +
                ", LocalIP=" + Arrays.toString(LocalIP) +
                ", SubNetMask=" + Arrays.toString(SubNetMask) +
                ", GateWay=" + Arrays.toString(GateWay) +
                ", MasterDNS=" + Arrays.toString(MasterDNS) +
                ", SlaveDNS=" + Arrays.toString(SlaveDNS) +
                ", cMAC=" + Arrays.toString(cMAC) +
                ", ServerIP1=" + Arrays.toString(ServerIP1) +
                ", ServerIP2=" + Arrays.toString(ServerIP2) +
                ", ServerPort1=" + ServerPort1 +
                ", ServerPort2=" + ServerPort2 +
                '}';
    }
}

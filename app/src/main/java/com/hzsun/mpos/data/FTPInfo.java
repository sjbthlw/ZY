package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class FTPInfo {

    @StructField(order = 0)
    public byte[] cFTPServerIP = new byte[4];     //    FTP服务IP地址	4字节	192.168.1.27
    @StructField(order = 1)
    public int wFTPServerPort;                 //FTP服务IP端口号	2字节	21
    @StructField(order = 2)
    public byte[] cFTPUserName = new byte[16];  //    FTP用户名	32字节	Easytong
    @StructField(order = 3)
    public byte[] cFTPPassWord = new byte[16];  //    FTP密码	32字节	123456
    @StructField(order = 4)
    public byte[] cUpFTPPath = new byte[16];  //    上传照片文件夹
    @StructField(order = 5)
    public byte[] cDownFTPPath = new byte[16];  //    下载照片文件夹

    public byte[] getcFTPServerIP() {
        return cFTPServerIP;
    }

    public void setcFTPServerIP(byte[] cFTPServerIP) {
        this.cFTPServerIP = cFTPServerIP;
    }

    public int getwFTPServerPort() {
        return wFTPServerPort;
    }

    public void setwFTPServerPort(int wFTPServerPort) {
        this.wFTPServerPort = wFTPServerPort;
    }

    public byte[] getcFTPUserName() {
        return cFTPUserName;
    }

    public void setcFTPUserName(byte[] cFTPUserName) {
        this.cFTPUserName = cFTPUserName;
    }

    public byte[] getcFTPPassWord() {
        return cFTPPassWord;
    }

    public void setcFTPPassWord(byte[] cFTPPassWord) {
        this.cFTPPassWord = cFTPPassWord;
    }

    public byte[] getcUpFTPPath() {
        return cUpFTPPath;
    }

    public void setcUpFTPPath(byte[] cUpFTPPath) {
        this.cUpFTPPath = cUpFTPPath;
    }

    public byte[] getcDownFTPPath() {
        return cDownFTPPath;
    }

    public void setcDownFTPPath(byte[] cDownFTPPath) {
        this.cDownFTPPath = cDownFTPPath;
    }

    @Override
    public String toString() {
        return "FTPInfo{" +
                "cFTPServerIP=" + Arrays.toString(cFTPServerIP) +
                ", wFTPServerPort=" + wFTPServerPort +
                ", cFTPUserName=" + Arrays.toString(cFTPUserName) +
                ", cFTPPassWord=" + Arrays.toString(cFTPPassWord) +
                ", cUpFTPPath=" + Arrays.toString(cUpFTPPath) +
                ", cDownFTPPath=" + Arrays.toString(cDownFTPPath) +
                '}';
    }
}

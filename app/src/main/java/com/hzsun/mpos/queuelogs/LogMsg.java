package com.hzsun.mpos.queuelogs;

public class LogMsg {
    private int devicenum;
    private String msg;
    private byte[] data;

    public int getDevicenum() {
        return devicenum;
    }

    public String getMsg() {
        return msg;
    }

    public byte[] getData() {
        return data;
    }

    public void setDevicenum(int devicenum) {
        this.devicenum = devicenum;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


}

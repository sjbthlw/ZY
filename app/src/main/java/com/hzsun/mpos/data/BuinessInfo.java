package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class BuinessInfo {

    @StructField(order = 0)
    public int cBusinessID;            //营业时段序号
    @StructField(order = 1)
    public byte[] cStartTime = new byte[2];        //起始时间BCD码
    @StructField(order = 2)
    public byte[] cEndTime = new byte[2];            //结束时间BCD码
    @StructField(order = 3)
    public byte[] Reserve = new byte[16];          //预留16
    @StructField(order = 4)
    public byte[] CRC16 = new byte[2];            //校验码       2字节

    public int getcBusinessID() {
        return cBusinessID;
    }

    public void setcBusinessID(int cBusinessID) {
        this.cBusinessID = cBusinessID;
    }

    public byte[] getcStartTime() {
        return cStartTime;
    }

    public void setcStartTime(byte[] cStartTime) {
        this.cStartTime = cStartTime;
    }

    public byte[] getcEndTime() {
        return cEndTime;
    }

    public void setcEndTime(byte[] cEndTime) {
        this.cEndTime = cEndTime;
    }

    public byte[] getReserve() {
        return Reserve;
    }

    public void setReserve(byte[] reserve) {
        Reserve = reserve;
    }

    public byte[] getCRC16() {
        return CRC16;
    }

    public void setCRC16(byte[] CRC16) {
        this.CRC16 = CRC16;
    }

    @Override
    public String toString() {
        return "BuinessInfo{" +
                "cBusinessID=" + cBusinessID +
                ", cStartTime=" + Arrays.toString(cStartTime) +
                ", cEndTime=" + Arrays.toString(cEndTime) +
                //", Reserve=" + Arrays.toString(Reserve) +
                ", CRC16=" + Arrays.toString(CRC16) +
                '}';
    }
}

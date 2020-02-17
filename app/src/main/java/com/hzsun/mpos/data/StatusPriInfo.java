package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class StatusPriInfo {

    @StructField(order = 0)
    public byte cStatusID;                //身份号
    @StructField(order = 1)
    public byte[] bPrivilegeTime = new byte[16];//优惠时段范围	16字节
    @StructField(order = 2)
    public byte[] Reserve = new byte[32];          //预留32
    @StructField(order = 3)
    public byte[] CRC16 = new byte[2];            //校验码       2字节


    public byte getcStatusID() {
        return cStatusID;
    }

    public void setcStatusID(byte cStatusID) {
        this.cStatusID = cStatusID;
    }

    public byte[] getbPrivilegeTime() {
        return bPrivilegeTime;
    }

    public void setbPrivilegeTime(byte[] bPrivilegeTime) {
        this.bPrivilegeTime = bPrivilegeTime;
    }

    public byte[] getCRC16() {
        return CRC16;
    }

    public void setCRC16(byte[] CRC16) {
        this.CRC16 = CRC16;
    }

    @Override
    public String toString() {
        return "StatusPriInfo{" +
                "cStatusID=" + cStatusID +
                ", bPrivilegeTime=" + Arrays.toString(bPrivilegeTime) +
                ", CRC16=" + Arrays.toString(CRC16) +
                '}';
    }
}

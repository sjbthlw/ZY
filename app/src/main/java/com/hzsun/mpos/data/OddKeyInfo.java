package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class OddKeyInfo {

    @StructField(order = 0)
    public byte cOddKeyID;               //单键组号
    @StructField(order = 1)
    public int[] wKeyMoney = new int[14];            //单键金额

    @StructField(order = 2)
    public byte[] Reserve = new byte[16];          //预留16
    @StructField(order = 3)
    public byte[] CRC16 = new byte[2];            //校验码       2字节

    public byte getcOddKeyID() {
        return cOddKeyID;
    }

    public void setcOddKeyID(byte cOddKeyID) {
        this.cOddKeyID = cOddKeyID;
    }

    public int[] getwKeyMoney() {
        return wKeyMoney;
    }

    public void setwKeyMoney(int[] wKeyMoney) {
        this.wKeyMoney = wKeyMoney;
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
        return "OddKeyInfo{" +
                "cOddKeyID=" + cOddKeyID +
                ", wKeyMoney=" + Arrays.toString(wKeyMoney) +
                //", Reserve=" + Arrays.toString(Reserve) +
                ", CRC16=" + Arrays.toString(CRC16) +
                '}';
    }
}

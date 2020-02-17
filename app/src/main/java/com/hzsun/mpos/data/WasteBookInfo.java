package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class WasteBookInfo implements Cloneable {

    //交易流水最大流水号
    @StructField(order = 0)
    public long MaxStationSID;
    //写记录流水指针
    @StructField(order = 1)
    public long WriterIndex;
    //上传流水指针
    @StructField(order = 2)
    public long TransferIndex;
    //未上传流水笔数
    @StructField(order = 3)
    public long UnTransferCount;

    @StructField(order = 4)
    public byte[] Reserve = new byte[32];          //预留16
    //校验码
    @StructField(order = 5)
    public byte[] CRC16 = new byte[2];

    //类的浅复制
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public long getMaxStationSID() {
        return MaxStationSID;
    }

    public void setMaxStationSID(long maxStationSID) {
        MaxStationSID = maxStationSID;
    }

    public long getWriterIndex() {
        return WriterIndex;
    }

    public void setWriterIndex(long writerIndex) {
        WriterIndex = writerIndex;
    }

    public long getTransferIndex() {
        return TransferIndex;
    }

    public void setTransferIndex(long transferIndex) {
        TransferIndex = transferIndex;
    }

    public long getUnTransferCount() {
        return UnTransferCount;
    }

    public void setUnTransferCount(long unTransferCount) {
        UnTransferCount = unTransferCount;
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
        return "WasteBookInfo{" +
                "MaxStationSID=" + MaxStationSID +
                ", WriterIndex=" + WriterIndex +
                ", TransferIndex=" + TransferIndex +
                ", UnTransferCount=" + UnTransferCount +
                //", Reserve=" + Arrays.toString(Reserve) +
                ", CRC16=" + Arrays.toString(CRC16) +
                '}';
    }
}

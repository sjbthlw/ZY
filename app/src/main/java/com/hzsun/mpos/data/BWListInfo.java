package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;
import struct.StructField;

@StructClass
public class BWListInfo {

    //黑白名单版本号
    @StructField(order = 0)
    public byte[] Version = new byte[4];
    //黑白名单数量
    @StructField(order = 1)
    public long lngBWCount;
    //最大卡内编号
    @StructField(order = 2)
    public long lngMaxCardID;
    //变黑白名单总数量
    @StructField(order = 3)
    public int iChangeBWListSum;

    public byte[] getVersion() {
        return Version;
    }

    public void setVersion(byte[] version) {
        Version = version;
    }

    public long getLngBWCount() {
        return lngBWCount;
    }

    public void setLngBWCount(long lngBWCount) {
        this.lngBWCount = lngBWCount;
    }

    public long getLngMaxCardID() {
        return lngMaxCardID;
    }

    public void setLngMaxCardID(long lngMaxCardID) {
        this.lngMaxCardID = lngMaxCardID;
    }

    public int getiChangeBWListSum() {
        return iChangeBWListSum;
    }

    public void setiChangeBWListSum(int iChangeBWListSum) {
        this.iChangeBWListSum = iChangeBWListSum;
    }

    @Override
    public String toString() {
        return "BWListInfo{" +
                "Version=" + Arrays.toString(Version) +
                ", lngBWCount=" + lngBWCount +
                ", lngMaxCardID=" + lngMaxCardID +
                ", iChangeBWListSum=" + iChangeBWListSum +
                '}';
    }
}

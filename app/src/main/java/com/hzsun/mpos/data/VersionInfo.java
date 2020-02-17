package com.hzsun.mpos.data;

import java.util.Arrays;

import struct.StructClass;

@StructClass
public class VersionInfo {

    public byte[] bSystemVer = new byte[4];                //系统参数版本号
    public byte[] bStationVer = new byte[4];            //站点参数版本号
    public byte[] bBurseVer = new byte[4];            //钱包参数版本号
    public byte[] bOddKeyVer = new byte[4];            //单键参数版本号
    public byte[] bStatusVer = new byte[4];                //身份参数版本号
    public byte[] bStatusPrivVer = new byte[4];        //身份优惠版本号
    public byte[] bBusinessVer = new byte[4];            //营业参数版本号
    public byte[] bInitListVer = new byte[4];        //初始名单版本号
    public byte[] bChangeListVer = new byte[4];        //变更名单版本号
    public byte[] cAppSoftWareVer = new byte[12];        //终端软件版本号


    public byte[] getbSystemVer() {
        return bSystemVer;
    }

    public void setbSystemVer(byte[] bSystemVer) {
        this.bSystemVer = bSystemVer;
    }

    public byte[] getbStationVer() {
        return bStationVer;
    }

    public void setbStationVer(byte[] bStationVer) {
        this.bStationVer = bStationVer;
    }

    public byte[] getbBurseVer() {
        return bBurseVer;
    }

    public void setbBurseVer(byte[] bBurseVer) {
        this.bBurseVer = bBurseVer;
    }

    public byte[] getbOddKeyVer() {
        return bOddKeyVer;
    }

    public void setbOddKeyVer(byte[] bOddKeyVer) {
        this.bOddKeyVer = bOddKeyVer;
    }

    public byte[] getbStatusVer() {
        return bStatusVer;
    }

    public void setbStatusVer(byte[] bStatusVer) {
        this.bStatusVer = bStatusVer;
    }

    public byte[] getbStatusPrivVer() {
        return bStatusPrivVer;
    }

    public void setbStatusPrivVer(byte[] bStatusPrivVer) {
        this.bStatusPrivVer = bStatusPrivVer;
    }

    public byte[] getbBusinessVer() {
        return bBusinessVer;
    }

    public void setbBusinessVer(byte[] bBusinessVer) {
        this.bBusinessVer = bBusinessVer;
    }

    public byte[] getbInitListVer() {
        return bInitListVer;
    }

    public void setbInitListVer(byte[] bInitListVer) {
        this.bInitListVer = bInitListVer;
    }

    public byte[] getbChangeListVer() {
        return bChangeListVer;
    }

    public void setbChangeListVer(byte[] bChangeListVer) {
        this.bChangeListVer = bChangeListVer;
    }

    public byte[] getcAppSoftWareVer() {
        return cAppSoftWareVer;
    }

    public void setcAppSoftWareVer(byte[] cAppSoftWareVer) {
        this.cAppSoftWareVer = cAppSoftWareVer;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "bSystemVer=" + Arrays.toString(bSystemVer) +
                ", bStationVer=" + Arrays.toString(bStationVer) +
                ", bBurseVer=" + Arrays.toString(bBurseVer) +
                ", bOddKeyVer=" + Arrays.toString(bOddKeyVer) +
                ", bStatusVer=" + Arrays.toString(bStatusVer) +
                ", bStatusPrivVer=" + Arrays.toString(bStatusPrivVer) +
                ", bBusinessVer=" + Arrays.toString(bBusinessVer) +
                ", bInitListVer=" + Arrays.toString(bInitListVer) +
                ", bChangeListVer=" + Arrays.toString(bChangeListVer) +
                ", cAppSoftWareVer=" + Arrays.toString(cAppSoftWareVer) +
                '}';
    }

}

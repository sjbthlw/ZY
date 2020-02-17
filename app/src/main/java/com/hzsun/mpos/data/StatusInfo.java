package com.hzsun.mpos.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusInfo {

    public byte cStatusID;                    //身份号
    //身份钱包参数 //每个身份包含8组钱包参数
    public List<StatusBurInfo> StatusInfolist = new ArrayList<StatusBurInfo>(8);
    public byte[] bPaymentLimitTime = new byte[16];     //消费限次时段

    public byte getcStatusID() {
        return cStatusID;
    }

    public void setcStatusID(byte cStatusID) {
        this.cStatusID = cStatusID;
    }

    public List<StatusBurInfo> getStatusInfolist() {
        return StatusInfolist;
    }

    public void setStatusInfolist(List<StatusBurInfo> statusInfolist) {
        StatusInfolist = statusInfolist;
    }

    public byte[] getbPaymentLimitTime() {
        return bPaymentLimitTime;
    }

    public void setbPaymentLimitTime(byte[] bPaymentLimitTime) {
        this.bPaymentLimitTime = bPaymentLimitTime;
    }

    @Override
    public String toString() {
        return "StatusInfo{" +
                "cStatusID=" + cStatusID +
                ", StatusInfolist=" + StatusInfolist +
                ", bPaymentLimitTime=" + Arrays.toString(bPaymentLimitTime) +
                '}';
    }
}

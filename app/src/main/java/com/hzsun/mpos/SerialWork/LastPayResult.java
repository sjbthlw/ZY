package com.hzsun.mpos.SerialWork;


public class LastPayResult {
    private long lastordernum;
    private boolean isSuccess;
    private TradeHolderInfo holderInfo;

    public long getLastordernum() {
        return lastordernum;
    }

    public void setLastordernum(long lastordernum) {
        this.lastordernum = lastordernum;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setState(boolean success) {
        isSuccess = success;
    }

    public TradeHolderInfo getHolderInfo() {
        return holderInfo;
    }

    public void setHolderInfo(TradeHolderInfo holderInfo) {
        this.holderInfo = holderInfo;
    }
}

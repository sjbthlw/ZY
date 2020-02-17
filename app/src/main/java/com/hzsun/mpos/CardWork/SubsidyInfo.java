package com.hzsun.mpos.CardWork;

public class SubsidyInfo implements Cloneable {

    //是否有补助
    public byte cSubsidyState;
    //补助钱包
    public byte cSubBurseID;
    //补助金额
    public long lngSubMoney;
    //补助流水号
    public int wSubSID;

    public byte getcSubsidyState() {
        return cSubsidyState;
    }

    public void setcSubsidyState(byte cSubsidyState) {
        this.cSubsidyState = cSubsidyState;
    }

    public byte getcSubBurseID() {
        return cSubBurseID;
    }

    public void setcSubBurseID(byte cSubBurseID) {
        this.cSubBurseID = cSubBurseID;
    }

    public long getLngSubMoney() {
        return lngSubMoney;
    }

    public void setLngSubMoney(long lngSubMoney) {
        this.lngSubMoney = lngSubMoney;
    }

    public int getwSubSID() {
        return wSubSID;
    }

    public void setwSubSID(int wSubSID) {
        this.wSubSID = wSubSID;
    }

    //类的浅复制
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "SubsidyInfo{" +
                "cSubsidyState=" + cSubsidyState +
                ", cSubBurseID=" + cSubBurseID +
                ", lngSubMoney=" + lngSubMoney +
                ", wSubSID=" + wSubSID +
                '}';
    }
}

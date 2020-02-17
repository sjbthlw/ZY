package com.hzsun.mpos.data;

public class WifiParaInfo implements Cloneable {

    public int cInfoState;        //WIFI是否刷了设置卡
    public int cIPMode;         //WIFI获取IP的方式 0x01(DHCP)    0x00(static IP)
    public String strUserName;        //wifi用户名
    public String strPassword;        //wifi密码
    public String strSSID;          //ESSID
    public int iQualityLevel;       //信号强度

    public int getcInfoState() {
        return cInfoState;
    }

    public void setcInfoState(int cInfoState) {
        this.cInfoState = cInfoState;
    }

    public int getcIPMode() {
        return cIPMode;
    }

    public void setcIPMode(int cIPMode) {
        this.cIPMode = cIPMode;
    }

    public String getStrUserName() {
        return strUserName;
    }

    public void setStrUserName(String strUserName) {
        this.strUserName = strUserName;
    }

    public String getStrPassword() {
        return strPassword;
    }

    public void setStrPassword(String strPassword) {
        this.strPassword = strPassword;
    }

    public String getStrSSID() {
        return strSSID;
    }

    public void setStrSSID(String strSSID) {
        this.strSSID = strSSID;
    }

    public int getiQualityLevel() {
        return iQualityLevel;
    }

    public void setiQualityLevel(int iQualityLevel) {
        this.iQualityLevel = iQualityLevel;
    }

    //类的浅复制
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    @Override
    public String toString() {
        return "WifiParaInfo{" +
                "cInfoState=" + cInfoState +
                ", cIPMode=" + cIPMode +
                ", strUserName='" + strUserName + '\'' +
                ", strPassword='" + strPassword + '\'' +
                ", strSSID='" + strSSID + '\'' +
                ", iQualityLevel=" + iQualityLevel +
                '}';
    }
}

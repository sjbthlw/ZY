package com.hzsun.mpos.data;

public class IdentityInfo {

    private String value;     //0~127 总共128种身份
    private String idtype; //linshicard,xueshengcard ...
    private boolean isSelected;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIdtype() {
        return idtype == null ? "" : idtype;
    }

    public void setIdtype(String idtype) {
        this.idtype = idtype;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

package com.hzsun.mpos.Http;

public class GetAppVer {

    private String code;
    private String msg;
    private String sign;

    public String softwareversion;            //String            	必选            	64            	程序版本号
    public String software_url;                //String	可选	32	程序下载地址
    public String typenum;                    //String            	必选	64            	设备类型
    public String modelnum;                    //String	必选	1	设备型号

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSoftwareversion() {
        return softwareversion;
    }

    public void setSoftwareversion(String softwareversion) {
        this.softwareversion = softwareversion;
    }

    public String getSoftware_url() {
        return software_url;
    }

    public void setSoftware_url(String software_url) {
        this.software_url = software_url;
    }

    public String getTypenum() {
        return typenum;
    }

    public void setTypenum(String typenum) {
        this.typenum = typenum;
    }

    public String getModelnum() {
        return modelnum;
    }

    public void setModelnum(String modelnum) {
        this.modelnum = modelnum;
    }
}

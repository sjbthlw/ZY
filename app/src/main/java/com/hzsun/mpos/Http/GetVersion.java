package com.hzsun.mpos.Http;

public class GetVersion {

    /**
     * version : 6
     * code : 00000
     * msg : SUCCESS
     * sign : kn+a8jpN2gzEw0Ju0wqonsxjo/I=
     */

    private String version;
    private String code;
    private String msg;
    private String sign;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

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
}

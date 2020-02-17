package com.hzsun.mpos.Http;

public class GetData {

    /**
     * code : 00000
     * msg : SUCCESS
     * user_info : [{"accnum":100001,"campusid":1,"changeid":"201810237024489793937473540","epid":1,"feature_url":"http://zhzyzh.cn:18080/featurecode/201810237024489793937473536","flag":1,"personcode":"100001","userid":"201810237024489793937473536","version":5},{"accnum":100001,"campusid":1,"changeid":"2018102319422048539377664","epid":1,"feature_url":"http://zhzyzh.cn:18080/featurecode/201810237024489793937473536","flag":3,"personcode":"100001","userid":"201810237024489793937473536","version":6}]
     * sign : 9TkSbssuDqQOqi53SJm0MexBwDw=
     */

    private String code;
    private String msg;
    private String user_info;
    private String sign;

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

    public String getUser_info() {
        return user_info;
    }

    public void setUser_info(String user_info) {
        this.user_info = user_info;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}

package com.hzsun.mpos.Http;

public class GetUpRecord {

//    code	String	是	-	网关返回码
//    msg	String	是	-	网关返回码描述
//    sign	String	是	-	签名

    private String code;
    private String msg;
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

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}

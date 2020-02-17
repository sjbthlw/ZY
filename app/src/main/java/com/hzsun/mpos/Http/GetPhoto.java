package com.hzsun.mpos.Http;

public class GetPhoto {

    /**
     * code : 00000
     * msg : SUCCESS
     * user_info : {"accnum":100001,"flag":2,"photo_url":"http://192.168.5.117:8089/featurecode/201810237024489793937473536","userid":"201810237024489793937473536","version":"15"}
     * sign : O+DjWFcGOB7KAZ6VEy5zgLI7DX4=
     */

//    accnum		String			必选     3                 账号
//    photo_url     	String            	必选	64            	  图片数据
//    flag		String            	必选	32		 标记
//    version		String            	必选    11            	   版本号

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

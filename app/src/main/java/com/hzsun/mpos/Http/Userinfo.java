package com.hzsun.mpos.Http;

public class Userinfo {

    public String accnum;        //String  必选    3       账号
    public String campusid;    //String	可选  	8	园区编号
    public String epid;        //String	可选  	8	使用单位
    public String accclassid;    //String	必选	8	账户身份
    public String personcode;    //String	必选	32	个人编号
    public String accname;        //String	必选	32	姓名
    public String cardcode;   //String	必选    20  一卡通卡内编号
    public String feature_url;        //String  可选	64      特征码地址
    public String pic_url;            //String  可选	64      图片地址
    public String flag;        //String  必选	32	标记 1新增 2 修改 3删除
    public String version;        //String  必选    11      版本号

}

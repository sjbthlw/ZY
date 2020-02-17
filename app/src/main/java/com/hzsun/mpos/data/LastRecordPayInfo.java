package com.hzsun.mpos.data;

public class LastRecordPayInfo {

    public byte cState;         //冲正状态，1 未冲，其他已冲正
    public byte cType;          //交易类型
//                1：支付宝；2：一卡通校园卡
//                3：微信；4：银联
//                5：云马；6：龙支付
//                7：微校；8：威富通
//                9：农业银行；10：翼支付
//                11：建行e码通；12：招行
//                13：银联商业服务部；14：工银融e联
    public long lngAccountID;   //账号
    public long lngOrderNum;    //订单号
    public byte[] cAccName = new byte[16];       //姓名	Utf-8 ,X2格式显示
    public byte[] cPerCode = new byte[16];    //人员编号	16
    public int lngBurseMoney;  //账户余额
    public int lngPayMoney;    //交易金额
    public int lngPriMoney;    //优惠金额
    public int lngManageMoney; //管理费金额
    public byte cBusinessID;     //营业分组时段序号
    public byte[] cStationID = new byte[2];   //站点号
}

package com.hzsun.mpos.Public;

import android.util.Log;

public class RefuseCode {

    private static final String TAG = "RefuseCode";
    //解析拒绝码
    /*
//1 超出消费范围卡
//2 不是有效卡户
//3 已过有效期
//4 存储过程异常
//5 对接方不存在
//6 站点不存在
//7 没有未处理的流水
//8 不是唯一账号
//9 第三方流水号不存在
//10 重账流水
//11 操作超时
//12 对应单据不存在
//13 余额不足
//14 消费类型错误
//15 操作员不存在
//16 钱包未启用
//17 商户号不存在
//18 冲正流水不存在
//19 余额复位钱包或者开环电子钱包
//20 第三方处理失败
//21 消费限次
//22 日累限额
//23 日累限次
//24 单笔密码限额
//25 日累密码限次
//26 没有找到匿名账户
//27 该应用服务目前不支持
//28 服务通讯超时
//29 银联交易错误
//30 时间格式化异常
//31 catch异常
//32 密码错误
//33 配置文件未配置
//34 签名验证失败
//35 账号类型不存在
//36 查询订单失败
//37 账户未绑定
//38 开环钱包不允许该操作
//39 超出终端单笔限额

    */
    public static String strRefuseCode=
            "//1 超出消费范围"+
            "//2 不是有效卡户"+
            "//3 已过有效期"+
            "//4 存储过程异常"+
            "//5 对接方不存在"+
            "//6 站点不存在"+
            "//7 没有未处理的流水"+
            "//8 不是唯一账号"+
            "//9 第三方流水号不存在"+
            "//10 重账流水"+
            "//11 操作超时"+
            "//12 对应单据不存在"+
            "//13 余额不足"+
            "//14 消费类型错误"+
            "//15 操作员不存在"+
            "//16 钱包未启用"+
            "//17 商户号不存在"+
            "//18 冲正流水不存在"+
            "//19 余额复位钱包"+
            "//20 第三方处理失败"+
            "//21 消费限次"+
            "//22 日累限额"+
            "//23 日累限次"+
            "//24 单笔密码限额"+
            "//25 日累密码限次"+
            "//26 没有找到匿名账户"+
            "//27 该应用服务不支持"+
            "//28 服务通讯超时"+
            "//29 银联交易错误"+
            "//30 不支持该银行卡!"+
            "//31 catch异常"+
            "//32 密码错误"+
            "//33 URL文件未配置"+
            "//34 签名验证失败"+
            "//35 账号类型不存在"+
            "//36 查询订单失败"+
            "//37 账户未绑定"+
            "//38 不允许该操作"+
            "//39 超出终端单笔限额";

    public static String[] strParseRefuseCode(long lngRefuseCode) {
        int i=0;
        String cErrorCode = "";
        String cErrorVoice = "";
        String[] strList = new String[2];

        if (lngRefuseCode == 0) {
            return null;
        }

        Log.d(TAG, String.format("拒绝码:%d", lngRefuseCode));
        String strRefuseCodeList[] = strRefuseCode.split("//");

        for(i=1;i<strRefuseCodeList.length;i++)
        {
            String[] strListTmp=strRefuseCodeList[i].split(" ");
            if(strListTmp[0].equals(""+lngRefuseCode))
            {
                Log.d(TAG,"拒绝码信息:"+strRefuseCodeList[i]);
                strList[0] = strListTmp[1];
                break;
            }
        }
        if(i>=strRefuseCodeList.length)
            strList[0] = "其他错误";

        switch ((int) lngRefuseCode) {


            case 1:    //1 超出消费范围
            case 2:    //2 不是有效卡户
            case 3:     //3 已过有效期
                cErrorVoice = "card_invalid";
                strList[1] = cErrorVoice;
                break;

            case 4:    //4 存储过程异常
            case 5:     //5 对接方不存在
            case 6:     //6 站点不存在
            case 7:     //7 没有未处理的流水
            case 8:     //8 不是唯一账号
            case 9:    //9 第三方流水号不存在
            case 10:    //10 重账流水
            case 11:   //11 操作超时
            case 12:    //12 对应单据不存在

            case 14: //14 消费类型错误
            case 15://15 操作员不存在
            case 16://16 钱包未启用
            case 17://17 商户号不存在
            case 18://18 冲正流水不存在
            case 19://19 余额复位钱包
            case 20://20 第三方处理失败
                cErrorVoice = "zhifushibai";
                strList[1] = cErrorVoice;
                break;

            case 13:    //13 余额不足
                cErrorVoice = "money_lack";
                strList[1] = cErrorVoice;
                break;


            case 21: //21 消费限次
            case 22://22 日累限额
            case 23://23 日累限次
            case 24://24 单笔密码限额
            case 25://25 日累密码限次
            case 39:  //39 超出终端单笔限额
                cErrorVoice = "card_limit";
                strList[1] = cErrorVoice;
                break;

            case 26://26 没有找到匿名账户
            case 27://27 该应用服务目前不支持
            case 28:   //28 服务通讯超时
            case 29://29 银联交易错误
            case 30://30 不支持该银行卡!
            case 31://31 catch异常
            case 32://32 密码错误
            case 33://33 配置文件未配置
            case 34://34 签名验证失败
            case 35://35 账号类型不存在
            case 36://36 查询订单失败
            case 38:   //38 开环钱包不允许该操作
                cErrorVoice = "zhifushibai";
                strList[1] = cErrorVoice;
                break;

            case 37://37 账户未绑定
                cErrorVoice = "userunbound";
                strList[1] = cErrorVoice;
                break;

            default:
                cErrorVoice = "msg_err";
                strList[1] = cErrorVoice;
                break;
        }
        return strList;
    }

}

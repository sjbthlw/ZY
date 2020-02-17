package com.hzsun.mpos.Global;

public class CommInfo {

//    // 授权常量
//    public static final String AUTHORITY = "com.hzsun.pmos.Global.CommInfo";
//    // 构造方法
//    public CommInfo() {}

    public byte cQueryCardInfoStatus;   //联网校验卡片状态
    public byte cGetQRCodeInfoStatus;   //联网获取二维码校验数据状态
    public byte cLastRecDisInfoStatus;   //联网获取在线末笔冲正数据状态
    public byte cWithholdInfoStatus;   //联网获取在线代扣数据状态

    public int lngComInterval;       //发送间隔
    public byte cRecvWaitState;      //等待接收状态
    public int iRecvDelayCount;    //接收状态延时时间

    public long lngSendComStatus;//发送命令状态位
    //0x00000001:接入请求
    //0x00000002:签到请求
    //0x00000004:签退请求
    //0x00000008:钥匙卡校验
    //0x00000010:卡号有效性验证
    //0x00000020:上传交易流水
    //0x00000040:以太网设备请求下载参数
    //0x00000080:取RF-SIM卡户信息 卡号校验(补助)
    //0x00000100:确认RF-SIM补助信息
    //0x00000200:RFSIM卡充值计算MAC1
    //0x00000400:SIMPASS获取计算充值MAC的数据

    public byte cRecvState;        //接收状态位
    //1:签到应答(正常)
    //2:签退应答(正常)
    //3:设置系统时间
    //4:读取系统时间
    //5:取参数版本
    //6:取黑白名单版本
    //7:设置系统参数
    //8:设置站点参数
    //9:清空钱包参数
    //10:设置钱包参数
    //11:下载钱包参数完成
    //12:清空单键参数
    //13:设置单键参数
    //14:下载单键参数完成
    //15:清空身份参数
    //16:设置身份参数
    //17:下载身份完成
    //18:清空身份优惠
    //19:设置身份优惠
    //20:下发身份优惠结束
    //21:清空营业分组
    //22:设置营业分组
    //23:下发营业分组完成
    //24:清空黑名单
    //25:初始黑名单
    //26:初始黑名单结束
    //27:清除变更黑白名单
    //28:变更黑白名单
    //29:变更黑白名单结束
    //30:下载参数结束
    //31:以太网设备请求下载参数
    //32:服务在线应答
    //33:读取设备状态

    //50:钥匙卡校验
    //51:卡号有效性验证
    //52:上传交易流水
    //53:取RF-SIM卡户信息 卡号校验(补助)
    //54:确认RF-SIM补助信息
    //55:RFSIM卡充值计算MAC1
    //56:SIMPASS获取计算充值MAC的数据


}

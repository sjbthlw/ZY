package com.hzsun.mpos.Http;

public class HttpCommInfo {

    public String strAccCode;
    public byte cRecvWaitState;      //等待接收状态
    public long lngSendHttpStatus;   //发送http状态
     /*
                             0x00000001 //获取特征码版本号
                             0x00000002 //获取初始特征码数据列表
                             0x00000004 //获取变更特征码数据列表
                             0x00000008 //获取用户照片数据地址
                             0x00000010 //获取程序升级app地址
                             0x00000020 //上传人脸照片消费记录
                             0x00000100 //获取初始图片数据列表
                             0x00000200 //获取变更图片数据列表
                             */

    public byte cGetFaceCodeState;      //获取特征码数据状态 1：正在获取特征码  0：
    public byte cDownMode;      //下载特征码模式 0：初始 1：变更
    public int iFaceServeType;  //服务器类型 1:老版本 0:新版本(图片特征码)
}

/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：QR.h
*
* 文件标识：QR
* 摘    要：QR基于串口1驱动的接口功能函数集合
*
*
* 当前版本：V1.0
* 作    者：wcp
* 完成日期：2015/8/21
* 编译环境：D:\Program Files (x86)\IAR Systems\Embedded Workbench 6.5\arm
*
* 历史信息：
*******************************************************************************/

#ifndef __UARTQR_H
#define __UARTQR_H

#ifdef __cplusplus
extern "C" {
#endif

#include "../config.h"

/**********************************************函数定义*****************************************************
* 函数名称: void QR_Init(u8 BaudID)
* 输入参数: u8 BaudID  BaudID 0=9600bps 1=38400bps 2=57600bps 3=115200bps
* 返回参数: void
* 功    能: 打印机串口初始化
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
u8 UartQR_Init(u8 BaudID);

/**********************************************函数定义*****************************************************
* 函数名称: u8 UartQR_SendData(u8 *data,u8 len)
* 输入参数: u8 *data 发送的数据 ,u8 len 发送数据的长度
* 返回参数: u8
* 功    能: 小票打印串口发送数据  （使用串行发送）
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
u8 UartQR_SendData(u8 *data, u16 len);

/**********************************************函数定义*****************************************************
* 函数名称: u8 UartQR_RecvData(u8 *data,u8 *RecvLen,u32 wait_time)
* 输入参数: u8 *data 接收到的数据, u8 *RecvLen 接收到的数据长度, u32 wait_time 接收数据时超时时间
* 返回参数: u8  0成功 1失败
* 功    能: 小票打印 串口接收数据
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
u8 UartQR_RecvData(u8 *data, u8 *iRecvLen, u32 wait_time);

u8 UartClearQR_RecvData(u32 wait_time);

u8 UartQR_RecvDataA(u8 *data, u8 *RecvLen, u32 wait_time);

//QR串口发送接收数据
u8 QRUart_EXCHANGE_CMD(u8 *Cmd_Buf, u16 Cmd_Len, u8 *Resp_Buf, u8 *Resp_len, u16 wait_time);

//====================QR设备应用==========================

//QRCode 扫描
int QR_ScanQRCode(u8 *cQRCodeInfo);

//串口发送低电平
u8 QR_UartSendLowlevel(void);

//$010500-EE19 设备重启
u8 QR_DeviceReset(void);

//识读提示音 cMode 0:$150100-9A28 关闭识读提示音 1:$150101-A919 *开启识读提示音
u8 QR_SetDevicePrompt(u8 cMode);

//识读LED灯指示 cMode 0:$150200-01F4 关闭LED灯指示 1:$150201-32C5 *开启LED灯指示
u8 QR_SetDeviceLed(u8 cMode);

//识读模式
//$100000-AF9D 一次触发
//$100001-9CAC 按键保持
//$100002-C9FF 开关持续
//$100003-FACE 持续识读
//$100004-6359 自动感应
u8 QR_SetDeviceReadMode(u8 cMode);

// 0:$108001-9E81 开始识读2   1:$108003-F8E3 结束识读
u8 QR_SetDeviceReadEnable(u8 cMode);

//识读间隔
u8 QR_SetDeviceReadInterval(u8 cType);

//串口波特率设置
u8 QR_SetBAUD(u8 BaudID);

//$010300-C980 读取设备信息
u8 QR_GetDeviceInfo(u8 *cDeviceInfo);

// 自动感应灵敏度
u8 QR_SetAutoSenLevel(u8 cLevel);

//DisSense 扫描  0:无变化 1:有变化
//移动侦测状态查询（只有识读模式为自动感应时结果才准确）
//$380000-C23C
int QR_GetDisSenseRet(int iCount);

//主机命令应答模式 $020B00-33A1 *无应答
int QR_SetHostCommand(void);

//$201000-DD4E *关闭结束符
int QR_SetEndMark(void);

//图片抓拍
//$380400-08CD
int QR_GetTakePhotos(void);


//图片抓拍成功 失败
//主机数据接收成功回复：cResult 0:$380511-7A78
//主机数据接收失败回复：cResult 1:$380510-4949
int QR_GetTakePhotosResult(u8 cRet);

//图片质量等级
//$380600-E5A5   //图片质量等级0，最低；
//$380601-D694   //图片质量等级1；
//$380602-83C7   //图片质量等级2；
//$380603-B0F6   //图片质量等级3；
//$380604-2961   //图片质量等级4，最高；
u8 QR_SetImageQualityLevel(u8 cLevel);


//QR内置模组USB电源复位
int QR_UsbpowerReset(u8 cDelays);

#ifdef __cplusplus
}
#endif
#endif




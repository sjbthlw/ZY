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

#ifndef __UartPrinter_H
#define __UartPrinter_H

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
u8 UartPrinter_Init(u8 BaudID);

/**********************************************函数定义*****************************************************
* 函数名称: u8 UartPrinter_SendData(u8 *data,u8 len)
* 输入参数: u8 *data 发送的数据 ,u8 len 发送数据的长度
* 返回参数: u8
* 功    能: 小票打印串口发送数据  （使用串行发送）
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
u8 UartPrinter_SendData(u8 *data, u16 len);

/**********************************************函数定义*****************************************************
* 函数名称: u8 UartPrinter_RecvData(u8 *data,u8 *RecvLen,u32 wait_time)
* 输入参数: u8 *data 接收到的数据, u8 *RecvLen 接收到的数据长度, u32 wait_time 接收数据时超时时间
* 返回参数: u8  0成功 1失败
* 功    能: 小票打印 串口接收数据
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
u8 UartPrinter_RecvData(u8 *data, u8 *iRecvLen, u32 wait_time);


#ifdef __cplusplus
}
#endif
#endif




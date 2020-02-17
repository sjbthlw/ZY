/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：Uart.h
*
* 文件标识：Uart.h
* 摘    要：SAM卡应用函数集合
*
*
* 当前版本：V1.0
* 作    者：wcp
* 完成日期：2015/12/3
* 编译环境：
*
* 历史信息：
*******************************************************************************/

#ifndef __UART_H
#define __UART_H

#ifdef __cplusplus
extern "C" {
#endif

extern int Uart_Init(int fd, int BaudID, char nEvent);

#ifdef __cplusplus
}
#endif

#endif




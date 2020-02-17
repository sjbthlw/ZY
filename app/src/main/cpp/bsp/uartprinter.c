/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：QR.c
*
* 文件标识：QR
* 摘    要：QR扫码基于串口3驱动的接口功能函数集合
*
*
* 当前版本：V1.0
* 作    者：wcp
* 完成日期：2015/8/21
* 编译环境：D:\Program Files (x86)\IAR Systems\Embedded Workbench 6.5\arm
*
* 历史信息：
*******************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>

#include "uartprinter.h"
#include "uart.h"
#include "../config.h"

static int uart_fd;        //串口文件描述符

static char *dev = "/dev/ttysWK0";    //232串口(和对接机的串口冲突)
#define     DELAYTIME           1000        // 延时时间1MS(延时1ms)

//毫秒级 延时
static void Sleepms(int ms) {
    struct timeval delay;
    delay.tv_sec = 0;
    delay.tv_usec = ms * 1000; // 20 ms
    select(0, NULL, NULL, NULL, &delay);
}

//微秒级 延时
static void Sleepus(int us) {
    struct timeval delay;
    delay.tv_sec = 0;
    delay.tv_usec = us; //  us
    select(0, NULL, NULL, NULL, &delay);
}

/**********************************************函数定义*****************************************************
* 函数名称: void UartPrinter_Init(u8 BaudID)
* 输入参数: u8 BaudID  BaudID 0=9600bps 1=38400bps 2=57600bps 3=115200bps
* 返回参数: void
* 功    能: 打印机串口初始化
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
u8 UartPrinter_Init(u8 BaudID) {
    uart_fd = open(dev, O_RDWR);
    if (uart_fd < 0) {
        //printf("Err:%s open error\n",dev);
        return 1;
    }
    //printf("ok:%s open ok\n",dev);
    Uart_Init(uart_fd, BaudID, 'E');
    return 0;
}

/**********************************************函数定义*****************************************************
* 函数名称: u8 UartPrinter_SendData(u8 *data,u8 len)
* 输入参数: u8 *data 发送的数据 ,u8 len 发送数据的长度
* 返回参数: u8
* 功    能: 小票打印串口发送数据  （使用串行发送）
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/

u8 UartPrinter_SendData(u8 *data, u16 len) {
    u16 i;
    int ret;
    u8 cSendData[1024];

    memset(cSendData, 0x00, sizeof(cSendData));
    memcpy(cSendData, data, len);

//    printf("============Send data============= \n");
//    for(i=0;i<len;i++)
//    {
//       printf("%02x,",cSendData[i]);
//    }
//    printf("\n");
    ret = write(uart_fd, cSendData, len);
    if (ret < 0) {
        //printf("M write device error\n");
        return 1;
    }
    Sleepms(1);
    return 0;
}


/**********************************************函数定义*****************************************************
* 函数名称: u8 UartPrinter_RecvData(u8 *data,u8 *RecvLen,u32 wait_time)
* 输入参数: u8 *data 接收到的数据, u8 *RecvLen 接收到的数据长度, u32 wait_time 接收数据时超时时间
* 返回参数: u8  0成功 1失败
* 功    能: 小票打印 串口接收数据
* 作    者: by wcp
* 日    期: 2015/9/6
 * 注意：底层的接口函数的变量传入定义一定要一致，否则将溢出，影响其他的参数，例如u8 *iRecvLen一定不能写成int *iRecvLen 因为外部传入的变量是u8的
************************************************************************************************************/


u8 UartPrinter_RecvData(u8 *data, u8 *RecvLen, u32 wait_time) {
    int ret;
    u16 recvlen;
    u16 waittime;
    u32 iTimerCount;
    u8 rcv_buf[256];
    u8 rcv_allbuf[256];

    ret = 0;
    recvlen = 0;
    waittime = 0;
    iTimerCount = 0;
    memset(rcv_buf, 0x00, sizeof(rcv_buf));
    memset(rcv_allbuf, 0x00, sizeof(rcv_allbuf));

    while (1) {
        iTimerCount++;
        if ((iTimerCount > 1 * 50) && (recvlen == 0)) {
            //printf("iTimerCount %d\n", iTimerCount);
            break;
        }
        ret = read(uart_fd, rcv_buf, 256);
        Sleepms(1);
        if (ret < 1) {
            if (recvlen != 0) {
                waittime++;
                if (waittime > wait_time) {
                    //printf("waittime %d -----Len %d\n",waittime, recvlen);
                    break;
                }
            }
            continue;
        } else {
            if (recvlen + ret >= 256) {
                break;
            }
            memcpy(rcv_allbuf + recvlen, rcv_buf, ret);
            recvlen += ret;
        }
    }
    if (recvlen != 0) {
        memcpy(data, rcv_allbuf, recvlen);
    }
    *RecvLen = recvlen;
    if (recvlen != 0)
        return 0;
    else
        return 1;
}

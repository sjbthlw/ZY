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

#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include <fcntl.h>
#include <sys/time.h>
#include <android/log.h>

#include "uartqr.h"
#include "uart.h"
#include "../config.h"
#include "public.h"

static int uart_fd;        //串口文件描述符

static char *dev = "/dev/ttysWK3";    //串口
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
* 函数名称: void UartQR_Init(u8 BaudID)
* 输入参数: u8 BaudID  BaudID 0=9600bps 1=38400bps 2=57600bps 3=115200bps
* 返回参数: void
* 功    能: QR串口初始化
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
u8 UartQR_Init(u8 BaudID) {
    uart_fd = open(dev, O_RDWR);
    if (uart_fd < 0) {
        //printf("Err:%s open error\n",dev);
        return 1;
    }
    //printf("ok:%s open ok\n",dev);
    Uart_Init(uart_fd, BaudID, 'N');
    return 0;
}

/**********************************************函数定义*****************************************************
* 函数名称: u8 UartQR_SendData(u8 *data,u8 len)
* 输入参数: u8 *data 发送的数据 ,u8 len 发送数据的长度
* 返回参数: u8
* 功    能: QR串口发送数据  （使用串行发送）
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/

u8 UartQR_SendData(u8 *data, u16 len) {
    u16 i;
    int ret;
    u8 cSendData[256];

    memset(cSendData, 0x00, 256);
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
* 函数名称: u8 UartQR_RecvData(u8 *data,u8 *RecvLen,u32 wait_time)
* 输入参数: u8 *data 接收到的数据, u8 *RecvLen 接收到的数据长度, u32 wait_time 接收数据时超时时间
* 返回参数: u8  0成功 1失败
* 功    能: QR 串口接收数据
* 作    者: by wcp
* 日    期: 2015/9/6
 * 注意：底层的接口函数的变量传入定义一定要一致，否则将溢出，影响其他的参数，例如u8 *iRecvLen一定不能写成int *iRecvLen 因为外部传入的变量是u8的
************************************************************************************************************/
u8 UartQR_RecvData(u8 *data, u8 *iRecvLen, u32 wait_time) {
    int ret = 0;
    u8 rcvflag=0;
    u16 recvlen = 0;
    u32 iTimerCount;
    u32 iTimerbytesCount;
    u8 rcv_buf[2048]={0};
    u8 rcv_allbuf[2048]={0};

    ret = 0;
    rcvflag=0;
    recvlen = 0;
    iTimerCount = 0;
    iTimerbytesCount=0;

    while (1) {
        iTimerCount++;
        if(rcvflag==1)
        {
            iTimerbytesCount++;
            if (iTimerbytesCount > 1 * 100) {
                LOGD("字节间隔超时退出 %d\n", iTimerbytesCount);
                break;
            }
        }
        else
        {
            if (iTimerCount > 2 * 100) {
                //LOGD("无数据超时退出 %d\n", iTimerCount);
                break;
            }
        }
        ret = read(uart_fd, rcv_buf, 256);
        Sleepms(1);
        if (ret < 1) {
            continue;
        } else {
            LOGD("Len %d\n", ret);
            rcvflag=1;
            if (recvlen >= 512) {
                break;
            }
            memcpy(rcv_allbuf + recvlen, rcv_buf, ret);
            recvlen += ret;
        }
    }

    if (recvlen != 0) {
        LOGD("RecvData Len :%d\n", recvlen);
        memcpy(data, rcv_allbuf, recvlen);
    }

    *iRecvLen = recvlen;
    if (recvlen != 0)
        return 0;
    else
        return 1;
}

u8 UartClearQR_RecvData(u32 wait_time) {
    int ret, i;
    u16 recvlen;
    u16 waittime;
    u32 iTimerCount;
    u8 rcv_buf[1024];
    u8 rcv_allbuf[1024];

    ret = 0;
    recvlen = 0;
    waittime = 0;
    iTimerCount = 0;
    memset(rcv_buf, 0x00, sizeof(rcv_buf));
    memset(rcv_allbuf, 0x00, sizeof(rcv_allbuf));

    while (1) {
        iTimerCount++;
        if ((iTimerCount > 10) && (recvlen == 0)) {
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
            //printf("Len %d\n", ret);
            recvlen += ret;
        }
    }
    if (recvlen != 0)
        return 0;
    else
        return 1;
}

u8 UartQR_RecvDataA(u8 *data, u8 *RecvLen, u32 wait_time) {
    int ret;
    u16 recvlen;
    u16 waittime;
    u32 iTimerCount;
    u8 rcv_buf[1024];
    u8 rcv_allbuf[1024];

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


//QR串口发送接收数据
u8 QRUart_EXCHANGE_CMD(u8 *Cmd_Buf, u16 Cmd_Len, u8 *Resp_Buf, u8 *Resp_len, u16 wait_time) {
    u8 i;
    u8 cResult;

    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {
        *Resp_len = 255;
        Sleepus(100);

        cResult = UartQR_RecvDataA(Resp_Buf, Resp_len, wait_time);
        if (cResult == 0) {
            return 0;
        }
    }
    return 1;
}

//====================QR设备应用==========================

//QRCode 扫描
int QR_ScanQRCode(u8 *cQRCodeInfo) {
    u8 data[2048];
    u8 RecvLen = 0;
    int iResult;

    memset(data, 0x00, sizeof(data));

    iResult = UartQR_RecvData(data, &RecvLen, 20);
    if (iResult == 0) {
        memcpy(cQRCodeInfo, data, RecvLen);
        return RecvLen;
    }
    return 0;
}

//串口发送低电平
u8 QR_UartSendLowlevel(void) {
    u8 Cmd_Buf[256];
    u16 Cmd_Len;

    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    Cmd_Len = sizeof(Cmd_Buf);
    UartQR_SendData(Cmd_Buf, Cmd_Len);
    return 0;
}


//$010500-EE19 设备重启
u8 QR_DeviceReset(void) {
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;
    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    strcpy(Cmd, "$010500-EE19");
    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;
    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {
    }
    return 0;
}

//识读提示音 cMode 0:$150100-9A28 关闭识读提示音 1:$150101-A919 *开启识读提示音
u8 QR_SetDevicePrompt(u8 cMode) {
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;
    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    if (cMode == 0) {
        strcpy(Cmd, "$150100-9A28");
    } else {
        strcpy(Cmd, "$150101-A919");
    }

    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;
    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {
    }
    return 0;
}

//识读LED灯指示 cMode 0:$150200-01F4 关闭LED灯指示 1:$150201-32C5 *开启LED灯指示
u8 QR_SetDeviceLed(u8 cMode) {
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;
    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    if (cMode == 0) {
        strcpy(Cmd, "$150200-01F4");
    } else {
        strcpy(Cmd, "$150201-32C5");
    }

    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;

    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {
    }
    return 0;
}

//识读模式
//$100000-AF9D 一次触发 0
//$100001-9CAC 按键保持 1
//$100002-C9FF 开关持续 2
//$100003-FACE 持续识读 3
//$100004-6359 自动感应 4
u8 QR_SetDeviceReadMode(u8 cMode) {
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;

    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    // 识读间隔
    switch (cMode) {
        case 0: //$100000-AF9D 一次触发
            strcpy(Cmd, "$100000-AF9D");

            break;
        case 1: //$100001-9CAC 按键保持
            strcpy(Cmd, "$100001-9CAC");

            break;
        case 2: //$100002-C9FF 开关持续
            strcpy(Cmd, "$100002-C9FF");

            break;
        case 3: //$100003-FACE 持续识读
            strcpy(Cmd, "$100003-FACE");

            break;
        case 4: //$100004-6359 自动感应
            strcpy(Cmd, "$100004-6359");
            break;

        default://$100002-C9FF 开关持续
            strcpy(Cmd, "$100002-C9FF");

            break;
    }

    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;

    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {

    }
    return 0;
}

//命令触发
//0:$108000-ADB0 开始识读1
//1:$108001-9E81 开始识读2
//2:$108003-F8E3 结束识读
u8 QR_SetDeviceReadEnable(u8 cMode) {
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;

    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    if (cMode == 0) {
        strcpy(Cmd, "$108000-ADB0"); //$108000-ADB0 开始识读1
    }
    if (cMode == 1) {
        strcpy(Cmd, "$108001-9E81"); //$108001-9E81 开始识读2
    } else if (cMode == 2) {
        strcpy(Cmd, "$108003-F8E3"); //$108003-F8E3 结束识读
    } else {
        strcpy(Cmd, "$108003-F8E3"); //$108003-F8E3 结束识读
    }

    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;

    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    return 0;
}

//识读间隔
u8 QR_SetDeviceReadInterval(u8 cType) {
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;

    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    // 识读间隔
    switch (cType) {
        case 0: //$100300-3441 无间隔
            strcpy(Cmd, "$100300-3441");

            break;
        case 1: //$100305-CBB4 *0.5秒
            strcpy(Cmd, "$100305-CBB4");

            break;
        case 2: //$10030A-0F29 1秒
            strcpy(Cmd, "$10030A-0F29");

            break;
        case 3: //$100314-CFB5 2秒
            strcpy(Cmd, "$100314-CFB5");

            break;
        case 4: //$1003FF-FC16 相邻条码必须不同
            strcpy(Cmd, "$1003FF-FC16");
            break;

        default://$1003FF-FC16 相邻条码必须不同
            strcpy(Cmd, "$1003FF-FC16");
            break;
    }
    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;
    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {

    }
    return 0;
}

//串口波特率设置
u8 QR_SetBAUD(u8 BaudID) {
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;

    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    // 设置波特率
    switch (BaudID) {
        case 0: //$020505-DF0C *波特率9600
            strcpy(Cmd, "$020505-DF0C");

            break;
        case 1: //$020506-8A5F 波特率19200
            strcpy(Cmd, "$020506-8A5F");

            break;
        case 2: //$020507-B96E 波特率38400
            strcpy(Cmd, "$020507-B96E");

            break;
        case 3: //$020508-A950 波特率57600
            strcpy(Cmd, "$020508-A950");

            break;
        case 4: //$020509-9A61 波特率115200
            strcpy(Cmd, "$020509-9A61");

            break;

        default://$020505-DF0C *波特率9600
            strcpy(Cmd, "$020505-DF0C");

            break;
    }

    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 10;

    cResult = QRUart_EXCHANGE_CMD(Cmd_Buf, Cmd_Len, Resp_Buf, &Resp_len, wait_time);
    if (cResult == 0) {

    }
    return 0;
}

//$010300-C980 读取设备信息
u8 QR_GetDeviceInfo(u8 *cDeviceInfo) {
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;

    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    strcpy(Cmd, "$010300-C980");
    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 10;

    cResult = QRUart_EXCHANGE_CMD(Cmd_Buf, Cmd_Len, Resp_Buf, &Resp_len, wait_time);
    if (cResult == 0) {
        memcpy(cDeviceInfo, Resp_Buf, Resp_len);
    }
    return 0;
}

// 自动感应灵敏度
u8 QR_SetAutoSenLevel(u8 cLevel) {
    u8 cResult;
    u8 Cmd_Buf[256]={0};
    u16 Cmd_Len;
    u8 Resp_Buf[256]={0};
    char Cmd[256]={0};

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));


    // 自动感应灵敏度
    switch (cLevel) {
        case 0: //$100100-D929 自动感应灵敏度最高
            strcpy(Cmd, "$100100-D929");
            break;
        case 1: //$100101-EA18 自动感应灵敏度高
            strcpy(Cmd, "$100101-EA18");
            break;
        case 2: //$100102-BF4B *自动感应灵敏度中
            strcpy(Cmd, "$100102-BF4B");
            break;
        case 3: //$100103-8C7A自动感应灵敏度低
            strcpy(Cmd, "$100103-8C7A");
            break;
        case 4: //$100104-15ED 自动感应灵敏度最低
            strcpy(Cmd, "$100104-15ED");
            break;
        default://$100100-D929 自动感应灵敏度最高
            strcpy(Cmd, "$100100-D929");
            break;
    }

    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);

    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {
    }
    return 0;
}

//DisSense 扫描  0:无变化 1:有变化
//移动侦测状态查询（只有识读模式为自动感应时结果才准确）
//$380000-C23C
int QR_GetDisSenseRet(int iCount) {
    int i;
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;

    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    //移动侦测状态查询（只有识读模式为自动感应时结果才准确）
    //$380000-C23C
    //无物理移动返回：$380010-F50C，有物理移动返回：$380011-C63D

    strcpy(Cmd, "$380000-C23C");
    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;

    for (i = 0; i < iCount; i++) {
        cResult = QRUart_EXCHANGE_CMD(Cmd_Buf, Cmd_Len, Resp_Buf, &Resp_len, wait_time);
        if (cResult == 0) {
            break;
        }
        Sleepms(10);
    }

    if (cResult == 0) {
        //无物理移动返回：$380010-F50C，有物理移动返回：$380011-C63D
        if (Resp_len == 12) {
            memset(Cmd, 0x00, sizeof(Cmd));
            memcpy(Cmd, Resp_Buf, Resp_len);
            if (strcmp(Cmd, "$380011-C63D") == 0) {
                //printf("=======距离感应有变化======\n");
                return 1;
            }

            if (strcmp(Cmd, "$380010-F50C") == 0) {
                //printf("=======距离感应无变化======\n");
                return 2;
            }
        }
        return -1;
    } else {
        return -1;
    }
}

//主机命令应答模式 $020B00-33A1 *无应答
int QR_SetHostCommand(void) {
    int i;
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;

    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    //主机命令应答模式 $020B00-33A1 *无应答
    //$020B00-33A1
    strcpy(Cmd, "$020B00-33A1");
    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;

    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {

    }
    return 0;
}

//$201000-DD4E *关闭结束符
int QR_SetEndMark(void) {
    int i;
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;
    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    //$201000-DD4E *关闭结束符
    strcpy(Cmd, "$201000-DD4E");
    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;

    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {
    }
    return 0;
}


//图片抓拍
//$380400-08CD
//设备根据“2.2主机命令应答模式”中的设置回复；
int QR_GetTakePhotos(void) {
    int i;
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;

    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    //图片抓拍
    //$380400-08CD
    strcpy(Cmd, "$380400-08CD");
    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;

    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {

    }
    return 0;
}

//图片抓拍成功 失败
//主机数据接收成功回复：cResult 0:$380511-7A78
//主机数据接收失败回复：cResult 1:$380510-4949
int QR_GetTakePhotosResult(u8 cRet) {
    int i;
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;

    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    if (cRet == 0) {
        //图片抓拍
        //主机数据接收成功回复：cResult 0:$380511-7A78
        strcpy(Cmd, "$380511-7A78");

    } else {
        //图片抓拍
        //主机数据接收失败回复：cResult 1:$380510-4949
        strcpy(Cmd, "$380510-4949");
    }
    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;

    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {
    }
    return 0;
}

//图片质量等级
//$380600-E5A5   //图片质量等级0，最低；
//$380601-D694   //图片质量等级1；
//$380602-83C7   //图片质量等级2；
//$380603-B0F6   //图片质量等级3；
//$380604-2961   //图片质量等级4，最高；
u8 QR_SetImageQualityLevel(u8 cLevel) {
    u8 cResult;
    u8 Cmd_Buf[256];
    u16 Cmd_Len;
    u8 Resp_Buf[256];
    u8 Resp_len;
    u16 wait_time;

    char Cmd[256];

    memset(Cmd, 0x00, sizeof(Cmd));
    memset(Cmd_Buf, 0x00, sizeof(Cmd_Buf));
    memset(Resp_Buf, 0x00, sizeof(Resp_Buf));
    Cmd_Len = 0;
    Resp_len = 0;

    //图片质量等级
    switch (cLevel) {
        case 0: //图片质量等级0，最低；
            strcpy(Cmd, "$380600-E5A5");

            break;
        case 1: //$380601-D694   //图片质量等级2；
            strcpy(Cmd, "$380601-D694");

            break;
        case 2: //$380602-83C7   //图片质量等级3；
            strcpy(Cmd, "$380602-83C7");

            break;
        case 3: //$380603-B0F6   //图片质量等级4；
            strcpy(Cmd, "$380603-B0F6");

            break;
        case 4: //$380604-2961   //图片质量等级5，最高；
            strcpy(Cmd, "$380604-2961");

            break;

        default://$380604-2961   //图片质量等级5，最高；
            strcpy(Cmd, "$380604-2961");

            break;
    }

    Cmd_Len = strlen(Cmd);
    memcpy(Cmd_Buf, Cmd, Cmd_Len);
    wait_time = 5;
    cResult = UartQR_SendData(Cmd_Buf, Cmd_Len);
    if (cResult == 0) {
    }
    return 0;
}

//QR内置模组USB电源复位
int QR_UsbpowerReset(u8 cDelays) {
//    u32 i=0;
//    u8 cResult;
//    u8 Cmd_Buf[256];
//    u16 Cmd_Len;

//    memset(Cmd_Buf,0x00,sizeof(Cmd_Buf));
//    Cmd_Len=sizeof(Cmd_Buf);

//    while(1)
//    {
//        g_WorkInfo.cUsbPowerFlag=1;
//        i++;
//        if(i==20)
//        {
//            printf("=======UsbPowerReset==========\n");
//            UsbPowerReset(5);
//        }
//        else if(i>200)
//        {
//            g_WorkInfo.cUsbPowerFlag=0;
//            break;
//        }
//        Sleepms(100);
//    }

    //UsbPowerReset(cDelays);
    return 0;
}

/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：SAM.c
*
* 文件标识：SAM
* 摘    要：SAM卡基于串口3驱动的接口功能函数集合
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
#include <android/log.h>

#include "sam.h"
#include "uart.h"
#include "public.h"
#include "../config.h"


static int uart_fd;        //串口文件描述符
static int s_cPSAMBaudID;

///sys/class/psam/mux0
///sys/class/psam/mux1
///sys/class/psam/mux2
///sys/class/psam/rst

static char *mux0 = "/sys/class/psam/mux0";    //串口
static char *mux1 = "/sys/class/psam/mux1";    //串口
static char *mux2 = "/sys/class/psam/mux2";    //串口
static char *rst = "/sys/class/psam/rst";    //串口

static char *dev = "/dev/ttysWK1";    //串口
#define     DELAYTIME           300        // 延时时间1MS(延时1ms)
#define     DELAYTIMEA          600         // 延时时间1MS(延时1ms)
#define     DELAYTIMEB          1300        // 延时时间1MS(延时1ms)

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
* 函数名称: void SAM_Init(u8 BaudID)
* 输入参数: u8 BaudID  BaudID 0=9600bps 1=38400bps 2=57600bps 3=115200bps
* 返回参数: void
* 功    能: SAM卡串口初始化
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
u8 SAM_Init(u8 BaudID) {
    sysfs_write(mux2, "0");//接收使能收

    uart_fd = open(dev, O_RDWR);
    if (uart_fd < 0) {
        printf("Err:%s open error\n", dev);
        return 1;
    }
    printf("ok:%s open ok\n", dev);
    Uart_Init(uart_fd, BaudID, 'E');

    return 0;
}


/**********************************************函数定义*****************************************************
* 函数名称: void SAMUart_Init(u8 BaudID)
* 输入参数: u8 BaudID  BaudID 0=9600bps 1=38400bps 2=57600bps 3=115200bps
* 返回参数: void
* 功    能: SAM卡串口初始化
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
u8 SAMUart_Init(u8 BaudID) {
    uart_fd = open(dev, O_RDWR);
    if (uart_fd < 0) {
        printf("Err:%s open error\n", dev);
        return 1;
    }
    printf("ok:%s open ok\n", dev);
    Uart_Init(uart_fd, BaudID, 'E');

    s_cPSAMBaudID = BaudID;

    return 0;
}

/**********************************************函数定义*****************************************************
* 函数名称: u8  SAM_RESET(u8 *bRespData,u8 *length)
* 输入参数: u8 Index Index 1/2/3/4卡座,u8 *bRespData 复位返回数据,u8 *length 返回数据长度
* 返回参数: u8   0：成功 1：失败
* 功    能: SAM卡复位
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
u8 SAM_SelectRESET(u8 Index, u8 *bRespData, u8 *length) {
    u8 cResult;

    switch (Index) {
        case 1:
            sysfs_write(mux0, "1");
            sysfs_write(mux1, "1");
            break;
        case 2:
            sysfs_write(mux0, "1");
            sysfs_write(mux1, "0");
            break;
        case 3:
            sysfs_write(mux0, "0");
            sysfs_write(mux1, "1");
            break;
        case 4:
            sysfs_write(mux0, "0");
            sysfs_write(mux1, "0");
            break;
        default:
            sysfs_write(mux0, "1");
            sysfs_write(mux1, "1");
            break;
    }
    sysfs_write(mux2, "0");
    Sleepus(50000);
    //复位过程 高->低->高
    //注意:SMA卡复位的高低电平之间的延时需要调整.
    sysfs_write(rst, "1");
    Sleepus(50000);
    sysfs_write(rst, "0");
    Sleepus(60000);
    sysfs_write(rst, "1");
    Sleepus(50000);
    //接收SAM卡数据 0成功 1失败
    cResult = UartSAM_RecvData(bRespData, length, 1);
    if (cResult == 0) {
        return 0;
    } else {
        return 1;
    }
}

/**********************************************函数定义*****************************************************
* 函数名称: void UartSAM_SendSET(void)
* 输入参数: void
* 返回参数: void
* 功    能: SAM卡 串口 发送数据设置
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
void UartSAM_SendSET(void) {
    sysfs_write(mux2, "1");//使能发送
}

/**********************************************函数定义*****************************************************
* 函数名称: void UartSAM_RecvSET(void)
* 输入参数: void
* 返回参数: void
* 功    能: SAM卡 串口 接收数据设置
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/
void UartSAM_RecvSET(void) {
    sysfs_write(mux2, "0");//使能接收
}

void UartSAMSendByte(char ucDat) {
    u8 data[16];

    data[0] = ucDat;
    write(uart_fd, data, 1);
}


u8 UartSAM_SendDataD(u8 *data, u16 len) {
    u16 i;
    int ret;
    u8 cSendData[256];

    memset(cSendData, 0x00, 256);

    UartSAM_SendSET();//使能发送
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
    Sleepus(980);
    UartSAM_RecvSET();//使能接收
    return 0;
}


/**********************************************函数定义*****************************************************
* 函数名称: u8 UartSAM_SendData(u8 *data,u8 len)
* 输入参数: u8 *data 发送的数据 ,u8 len 发送数据的长度
* 返回参数: u8
* 功    能: SAM卡串口发送数据  （使用串行发送）
* 作    者: by wcp
* 日    期: 2015/9/6
************************************************************************************************************/

u8 UartSAM_SendData(u8 *data, u8 len) {
    u16 i;
    u16 ret;
    unsigned char cSendData[256];
    unsigned char cSendDataTemp[2];

    memset(cSendData, 0x00, sizeof(cSendData));
    memset(cSendDataTemp, 0x00, sizeof(cSendDataTemp));

    memcpy(cSendData, data, len);
    UartSAM_SendSET();//使能发送

    for (i = 0; i < len - 1; i++) {
        cSendDataTemp[0] = cSendData[i];
        ret = write(uart_fd, cSendDataTemp, 1);
        if (ret < 0) {
            printf("M write device error\n");
            return 1;
        }
        if (len < 8) {
            if (i % 4 == 0) {
                Sleepus(3 * DELAYTIME);
            } else {
                Sleepus(1 * DELAYTIME);
            }
        } else {
            if (i < 8) {
                if (i % 4 == 0) {
                    Sleepus(3 * DELAYTIME);
                } else {
                    Sleepus(DELAYTIME);
                }
            } else {
                if (i % 64 == 0) {
                }
                if (i % 16 == 0) {
                    Sleepus(3 * DELAYTIME);
                } else {
                    Sleepus(DELAYTIME);
                }
            }
        }
    }
    cSendDataTemp[0] = cSendData[i];
    ret = write(uart_fd, cSendDataTemp, 1);
    if (ret < 0) {
        printf("M write device error\n");
        return 1;
    }
    //Sleepus(880);	//880 这个延时非常重要(不信你改改试试)
    //Sleepus(980);
    UartSAM_RecvSET();//使能接收

    return 0;
}

u8 UartSAM_SendDataA(u8 *data, u8 len) {
    u16 i, j;
    u16 ret;
    unsigned char cSendData[256];
    unsigned char cSendDataTemp[2];

    memcpy(cSendData, data, len);

    UartSAM_SendSET();    //使能发送

    for (i = 0; i < len - 1; i++) {
        cSendDataTemp[0] = cSendData[i];
        ret = write(uart_fd, cSendDataTemp, 1);
        if (ret < 0) {
            printf("M write device error\n");
            return 1;
        }
        if (len < 8) {
            if (i % 4 == 0) {
                Sleepus(3 * DELAYTIMEA);
            } else {
                Sleepus(1 * DELAYTIMEA);
            }

        } else {
            if (i < 8) {
                if (i % 4 == 0) {
                    Sleepus(3 * DELAYTIMEA);
                } else {
                    Sleepus(DELAYTIMEA);
                }
            } else {
                if (i % 64 == 0) {

                }

                if (i % 16 == 0) {
                    Sleepus(3 * DELAYTIMEA);
                } else {
                    Sleepus(DELAYTIMEA);
                }
            }
        }
    }

    cSendDataTemp[0] = cSendData[i];
    ret = write(uart_fd, cSendDataTemp, 1);
    if (ret < 0) {
        printf("M write device error\n");
        return 1;
    }
    //Sleepus(880);	//880 这个延时非常重要(不信你改改试试)
    //Sleepus(980);
    UartSAM_RecvSET();    //使能接收
    Sleepus(50);

    return 0;
}

u8 UartSAM_SendDataB(u8 *data, u8 len) {
    u16 i;
    u16 ret;
    unsigned char cSendData[256];
    unsigned char cSendDataTemp[2];

    memcpy(cSendData, data, len);

    UartSAM_SendSET();    //使能发送

    for (i = 0; i < len - 1; i++) {
        cSendDataTemp[0] = cSendData[i];
        ret = write(uart_fd, cSendDataTemp, 1);
        if (ret < 0) {
            printf("M write device error\n");
            return 1;
        }
        if (len < 8) {
            if (i % 4 == 0) {
                Sleepus(3 * DELAYTIMEB);
            } else {
                Sleepus(1 * DELAYTIMEB);
            }
        } else {
            if (i < 8) {
                if (i % 4 == 0) {
                    Sleepus(3 * DELAYTIMEB);
                } else {
                    Sleepus(DELAYTIMEB);
                }
            } else {
                if (i % 64 == 0) {

                }

                if (i % 16 == 0) {
                    Sleepus(3 * DELAYTIMEB);
                } else {
                    Sleepus(DELAYTIMEB);
                }
            }
        }
    }

    cSendDataTemp[0] = cSendData[i];
    ret = write(uart_fd, cSendDataTemp, 1);
    if (ret < 0) {
        printf("M write device error\n");
        return 1;
    }
    //Sleepus(880);	//880 这个延时非常重要(不信你改改试试)
    Sleepus(800);
    UartSAM_RecvSET();    //使能接收

    return 0;
}

u8 UartSAM_RecvData(u8 *data, u8 *RecvLen, u32 wait_time) {
    u16 ret, i;
    u32 iTimerCount;
    u8 rcv_buf[256];

    ret = 0x00;
    memset(rcv_buf, 0x00, sizeof(rcv_buf));
    iTimerCount = 0;

    while (1) {
        iTimerCount++;
        if (iTimerCount > 2 * 100) {
            //printf("iTimerCount %d\n", iTimerCount);
            break;
        }
        ret = read(uart_fd, rcv_buf, 128);
        Sleepms(1);
        //if(ret==0)
        if (ret < 1) {
            //printf("Len %d\n", ret);
            continue;
        } else {
            //printf("Len %d\n", ret);
            break;
        }
    }

    LOGD("接收数据:%d", ret);
    memset(Debug_Str, 0x00, sizeof(Debug_Str));
    for (i = 0; i < ret; i++) {
        sprintf(Debug_Str + i * 3, "%02x,", rcv_buf[i]);
    }
    LOGD("%s", Debug_Str);

    memcpy(data, rcv_buf, ret);
    *RecvLen = ret;
    if (ret != 0)
        return 0;
    else
        return 1;
}


u8 UartSAM_RecvDataA(u8 *data, u8 *RecvLen, u32 wait_time) {
    u16 ret, i;
    u32 iTimerCount;
    u8 rcv_buf[256];

    ret = 0x00;
    iTimerCount = 0;

    while (1) {
        ret = read(uart_fd, rcv_buf, 256);
        Sleepms(1);

        iTimerCount++;
        if (iTimerCount > 1 * 100)
            //if(iTimerCount>300*100)
        {
            //printf("iTimerCount %d\n", iTimerCount);
            break;
        }

        if (ret < 2) {
            //printf("Len %d\n", ret);
            continue;
        } else {
            //printf("Len %d\n", ret);
            break;
        }
    }

//    printf("接收数据\n");
//    for(i=0;i<ret;i++)
//    {
//        printf("%02x,",rcv_buf[i]);
//    }
//    printf("\n");

//    printf("RecvData Len :%d\n", ret);
    memcpy(data, rcv_buf, ret);
    *RecvLen = ret;
    if (ret != 0)
        return 0;
    else
        return 1;
}

/**********************************************函数定义*****************************************************
* 函数名称: long  SAM_EXCHANGE_APDU(u8 *APDU_Buf,long APDU_Len,u8r *response,u8 *data_len,long wait_time)
* 输入参数: u8 *APDU_Buf,long APDU_Len,u8r *response,u8 *data_len,long wait_time
* 返回参数: long
* 功    能: SAM卡发送APDU指令
* 作    者: by wcp
* 日    期: 2015/12/2
************************************************************************************************************/
u8 SAM_EXCHANGE_APDU(u8 *APDU_Buf, long APDU_Len, u8 *response, u8 *data_len, long wait_time) {
    u8 cResult;

    s_cPSAMBaudID = 0;
    if (s_cPSAMBaudID == 0) {
        LOGD("低速pps 0,发送数据：");
        memset(Debug_Str, 0x00, sizeof(Debug_Str));
        for (int i = 0; i < APDU_Len; i++) {
            sprintf(Debug_Str + i * 3, "%02x,", APDU_Buf[i]);
        }
        LOGD("%s", Debug_Str);

        cResult = UartSAM_SendDataD(APDU_Buf, APDU_Len);
        if (cResult == 0) {
            *data_len = 255;
//            if(wait_time!=0)
//                Sleepus(wait_time*30*1000);

            cResult = UartSAM_RecvData(response, data_len, wait_time);
            if (cResult == 0) {
                return 0;
            }
        }
    }
    if (s_cPSAMBaudID == 10) {
        //printf("低速pps 1\n");
        cResult = UartSAM_SendDataB(APDU_Buf, APDU_Len);
        if (cResult == 0) {
            *data_len = 255;
            if (wait_time != 0)
                Sleepus(wait_time * 30 * 1000);

            cResult = UartSAM_RecvData(response, data_len, wait_time);
            if (cResult == 0) {
                return 0;
            }
        }
    } else {
        //printf("高速pps\n");
        cResult = UartSAM_SendDataA(APDU_Buf, APDU_Len);
        if (cResult == 0) {
            *data_len = 255;
            if (wait_time != 0)
                Sleepus(wait_time * 30 * 1000);

            cResult = UartSAM_RecvDataA(response, data_len, wait_time);
            if (cResult == 0) {
                return 0;
            }
        }
    }
    return 1;
}



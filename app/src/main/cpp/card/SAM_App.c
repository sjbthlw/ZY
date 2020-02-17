/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：SAM_App.c
*
* 文件标识：SAM_App.c
* 摘    要：SAM卡应用函数集合
*
*
* 当前版本：V1.0
* 作    者：wcp
* 完成日期：2015/12/3
* 编译环境：D:\Program Files (x86)\IAR Systems\Embedded Workbench 6.5\arm
*
* 历史信息：
*******************************************************************************/

#include <android/log.h>
#include "SAM_App.h"
#include "../config.h"
#include "../bsp/sam.h"
#include "../bsp/public.h"


static u8 s_cPSAMIndex;
static u8 s_cPSAMBaudID;

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

//PSAM卡初始化
u8 PSAMSP_Init(void) {
    int i;
    char cResult;
    unsigned char ucRespLen;
    u8 bTempContext[64];
    u8 bRespContext[64];

    memset(bTempContext, 0, 64);
    memset(bRespContext, 0, 64);

    if (SAMUart_Init(0) != 0) {
        return 3;
    }
    UartSAM_RecvSET();
    Sleepus(10000);
    LOGD("==SAM卡复位==\n");
    for (i = 1; i <= 4; i++) {
        cResult = SAM_SelectRESET(i, bRespContext, &ucRespLen);
        if (cResult == 0) {
            LOGD("SAM卡:%d 复位成功，跳出", i);
            s_cPSAMIndex = i;
            break;
        }
    }
    if (cResult != 0) {
        LOGD("SAM卡复位失败");
        return 2;
    } else {
        LOGD("SAM卡复位信息");
        for (i = 0; i < ucRespLen; i++) {
            sprintf(Debug_Str + i * 3, "%02x,", bRespContext[i]);
        }
        LOGD("%s", Debug_Str);
    }
    //3b,6c,00,02,53,62,86,38,37,26,ef,07,7c,25,79,20,
    //3b,6c,00,02,53,62,86,38,37,26,ef,07,7c,25,79,20,
    //3b,fc,11,00,00,10,80,56,62,86,38,5c,df,6e,97,af,8f,cf,79,
    //3b,fc,11,00,00,10,80,56,62,86,38,5c,df,6e,97,af,8f,cf,79,

    //判断PSAM卡使用的波特率
    if (bRespContext[2] == 0x11) {
        LOGD("SAM卡波特率9600");
        s_cPSAMBaudID = 10;
        if (SAMUart_Init(10) != 0) {
            return 3;
        }
    } else if (bRespContext[2] == 0x13) {
        LOGD("SAM卡波特率38400");
        s_cPSAMBaudID = 1;
        if (SAMUart_Init(1) != 0) {
            return 3;
        }
    } else if (bRespContext[2] == 0x18) {
        LOGD("SAM卡波特率115200");
        s_cPSAMBaudID = 3;
        if (SAMUart_Init(3) != 0) {
            return 3;
        }
    } else {
        LOGD("SAM卡波特率9600(默认)");
        //判断新老版本的PSAM卡
        if (bRespContext[1] == 0x6c) {
            LOGD("SAM卡老版本");
            s_cPSAMBaudID = 0;
            if (SAMUart_Init(0) != 0) {
                return 3;
            }
        } else {
            LOGD("SAM卡新版本");
            s_cPSAMBaudID = 10;
            if (SAMUart_Init(10) != 0) {
                return 3;
            }
        }
    }

    //获取随机数
    cResult = PSAMSP_GetChallenge(bTempContext, 4);
    if (cResult != 0) {
        LOGD("获取随机数数据失败:%d", cResult);
        return 4;
    } else {
        LOGD("随机数数据");
        for (i = 0; i < 4; i++) {
            LOGD("%02x,", bTempContext[i]);
        }
    }
//
//    //选择PSAM卡易通目录
//    cResult=PSAMSP_SelectPayDF(2);
//    if(cResult!=0)
//    {
//        LOGD("选择PSAM卡易通目录失败:%d",cResult);
//        return 5;
//    }
//    else
//    {
//        LOGD("选择PSAM卡易通目录成功");
//    }

    return 0;
}

//指定卡槽PSAM卡初始化
u8 PSAMSPSlot_Init(void) {
    int i;
    char cResult;
    unsigned char ucRespLen;
    u8 bTempContext[64];
    u8 bRespContext[64];

    memset(bTempContext, 0, 64);
    memset(bRespContext, 0, 64);

    LOGD("SAM卡复位");

    cResult = SAM_SelectRESET(s_cPSAMIndex, bRespContext, &ucRespLen);
    if (cResult == 0) {
        LOGD("SAM卡:%d 复位成功");
        LOGD("SAM卡复位信息");
        for (i = 0; i < ucRespLen; i++) {
            LOGD("%02x,", bRespContext[i]);
        }
    } else {
        LOGD("SAM卡复位失败");
        return 2;
    }

    //获取随机数
    cResult = PSAMSP_GetChallenge(bTempContext, 4);
    if (cResult != 0) {
        LOGD("获取随机数数据失败");
        //return 3;
    } else {
        LOGD("随机数数据");
        for (i = 0; i < 4; i++) {
            LOGD("%02x,", bTempContext[i]);
        }
    }
    //选择PSAM卡易通目录
    cResult = PSAMSP_SelectPayDF(2);
    if (cResult != 0) {
        LOGD("选择PSAM卡易通目录失败");
        return 4;
    } else {
        LOGD("选择PSAM卡易通目录成功");
    }
    return 0;
}

//修改PSAM卡的PPS(波特率)
u8 PSAMSP_SetPsamPPS(u8 cBaudID) {
    char dwDataLen = 0;
    u8 bSendData[64];
    u8 bRecvData[64];
    u8 ucRespLen;
    char cResult;

    //80 39 30 01 01 01 设置专有/协商模式
    //80 39 30 00 01 18 设置波特率  11 9600 13 38400  18 115200
    /*
     * CLA
        0x00
        INS
        0x88
        P1
        0x00
        P2
        0x01或0x06或0x0C
        Le
        0x00
     * */
    // 设置专有/协商模式

    dwDataLen = 0;
    bSendData[dwDataLen++] = 0x80;
    bSendData[dwDataLen++] = 0x39;
    bSendData[dwDataLen++] = 0x30;
    bSendData[dwDataLen++] = 0x01;
    bSendData[dwDataLen++] = 0x01;
    bSendData[dwDataLen++] = 0x01;

    memset(bRecvData, 0, 64);
    cResult = SAM_EXCHANGE_APDU(bSendData, dwDataLen, bRecvData, &ucRespLen, 1);
    if (cResult != 0) {
        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        return 2;
    }

    //设置波特率  11 9600 13 38400  18 115200
    dwDataLen = 0;
    bSendData[dwDataLen++] = 0x80;
    bSendData[dwDataLen++] = 0x39;
    bSendData[dwDataLen++] = 0x30;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x01;

    bSendData[dwDataLen++] = 0x01;
    if (cBaudID == 0) {
        bSendData[dwDataLen++] = 0x11;   //11 9600
    } else if (cBaudID == 1) {
        bSendData[dwDataLen++] = 0x13;   //13 38400
    } else if (cBaudID == 2) {
        bSendData[dwDataLen++] = 0x18;   //18 115200
    }

    memset(bRecvData, 0, 64);
    cResult = SAM_EXCHANGE_APDU(bSendData, dwDataLen, bRecvData, &ucRespLen, 1);
    if (cResult != 0) {
        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        return 2;
    }

    return 0;
}

//获取相应数据(获取响应)
u8 PSAMSP_GetResponse(char cResponseLen, u8 *bResponseData) {
    int cResult;
    unsigned char i;
    unsigned char ucRespLen;
    unsigned char bTempContext[32];
    unsigned char bRespContext[256];

    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xC0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x00;

    bTempContext[i++] = cResponseLen;

    memset(bRespContext, 0, 256);
    ucRespLen = 255;

    cResult = SAM_EXCHANGE_APDU(bTempContext, i, bRespContext, &ucRespLen, 0);
    if (cResult != 0) {
        return 1;
    }

    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        LOGD("PSAMSP_GetResponse err:%02x-%02x\n", bRespContext[ucRespLen - 2],
             bRespContext[ucRespLen - 1]);
        return 2;
    }

    //判断接收数据长度是否一致
    if (cResponseLen != (ucRespLen - 2)) {
        LOGD("接收数据长度错误:%d\n", ucRespLen);
        return 3;
    }

    memcpy(bResponseData, bRespContext, ucRespLen);

    return 0;
}


//获取相应数据(获取响应M1)
u8 PSAMSP_M1GetResponse(char cResponseLen, u8 *bResponseData) {
    int cResult;
    unsigned char i;
    unsigned char ucRespLen;
    unsigned char bTempContext[32];
    unsigned char bRespContext[256];

    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xC0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x00;

    bTempContext[i++] = cResponseLen;

    memset(bRespContext, 0, 256);
    ucRespLen = 255;

    cResult = SAM_EXCHANGE_APDU(bTempContext, i, bRespContext, &ucRespLen, 1);
    if (cResult != 0) {
        return 1;
    }

    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        LOGD("PSAMSP_GetResponse err:%d--%02x-%02x\n", ucRespLen, bRespContext[ucRespLen - 2],
             bRespContext[ucRespLen - 1]);
        LOGD("接收数据\n");
        for (i = 0; i < ucRespLen; i++) {
            LOGD("%02x,", bRespContext[i]);
        }
        LOGD("\n");

        return 2;
    }

    //判断接收数据长度是否一致
    if (cResponseLen != (ucRespLen - 2)) {
        LOGD("接收数据长度错误:%d\n", ucRespLen);
        return 3;
    }
    memcpy(bResponseData, bRespContext, ucRespLen);

    return 0;
}


//取随机数
u8 PSAMSP_GetChallenge(u8 *bChallenge, u8 cLen) {
    char dwDataLen = 0;
    u8 bSendData[16];
    u8 bRecvData[64];
    unsigned char ucRespLen;
    char cResult;

    dwDataLen = 0;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x84;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x00;

    bSendData[dwDataLen++] = cLen; //要求卡片返回的随机数长度

    memset(bRecvData, 0, 64);
    cResult = SAM_EXCHANGE_APDU(bSendData, dwDataLen, bRecvData, &ucRespLen, 1);
    if (cResult != 0) {
        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    //取随机数
    memcpy(bChallenge, bRecvData + 1, cLen);
    return 0;
}


/// <summary>
/// PSAM选择文件
/// </summary>
/// <param name="p1">0x00(按文件标志符选择MF或DF) 0x02(选择EF) 0x04(按文件名选择应用)</param>
/// <param name="p2">0x00(第一个或仅有一个) 0x02(下一个)</param>
/// <param name="data">文件标识符或DF(MF)名称</param>
/// <param name="Length"></param>
/// <returns>成功返回0,失败返回错误码</returns>
u8 PSAMSP_SelectFile(u8 p1, u8 p2, u8 *data, u8 Length) {
    int iResult;

    u8 timeoutLen;
    u8 Le = 0;
    u8 cPostLen = 0;
    u8 bPostData[256];
    u8 cRespLen = 0;
    u8 bRespData[256];

    memset(bPostData, 0, sizeof(bPostData));
    memset(bRespData, 0, sizeof(bRespData));

    //超时时长
    timeoutLen = 1;
    // 选择文件
    bPostData[cPostLen++] = 0x00;
    bPostData[cPostLen++] = 0xA4;
    bPostData[cPostLen++] = p1;
    bPostData[cPostLen++] = p2;
    bPostData[cPostLen++] = Length;

    memcpy(bPostData + cPostLen, data, Length);
    cPostLen += Length;

    iResult = SAM_EXCHANGE_APDU(bPostData, cPostLen, bRespData, &cRespLen, timeoutLen);
    if (iResult != 0) {
        LOGD("PSAMSP_SelectFile err:%d\n", cRespLen);
        return 1;
    }

    if ((bRespData[cRespLen - 2] != 0x90) || (bRespData[cRespLen - 1] != 0x00)) {
        if (bRespData[cRespLen - 2] == 0x61) {
            Le = bRespData[cRespLen - 1];
        } else {
            return 2;
        }
    }

    // 获取相应信息 Get Response
    //超时时长
    timeoutLen = 1;
    cPostLen = 0;
    memset(bPostData, 0, sizeof(bPostData));
    bPostData[cPostLen++] = 0x00;
    bPostData[cPostLen++] = 0xC0;
    bPostData[cPostLen++] = 0x00;
    bPostData[cPostLen++] = 0x00;
    bPostData[cPostLen++] = Le;

    cRespLen = 0;
    memset(bRespData, 0, sizeof(bRespData));
    iResult = SAM_EXCHANGE_APDU(bPostData, cPostLen, bRespData, &cRespLen, timeoutLen);
    if (iResult != 0) {
        return 3;
    }
    if ((bRespData[cRespLen - 2] != 0x90) || (bRespData[cRespLen - 1] != 0x00)) {
        LOGD("PSAMSP_GetResponse err:%d--%02x-%02x\n", cRespLen, bRespData[cRespLen - 2],
             bRespData[cRespLen - 1]);
        return 4;
    }
    return 0;
}

//选择目录
u8 PSAMSP_SelectPayDF(u8 cPSAMcardtype) {
    int iResult;
    u8 cPostLen = 0;
    u8 bPostData[256];

    bPostData[cPostLen++] = 0x7F;
    if (cPSAMcardtype == 1) {
        bPostData[cPostLen++] = 0x01;
    } else {
        bPostData[cPostLen++] = 0x02;
    }
    iResult = PSAMSP_SelectFile(0x00, 0x00, bPostData, cPostLen);

    return iResult;
}


///PSAM通用DES计算初始化 用加密密钥初始化 成功返回0,失败返回错误码    
/// </summary>
/// <param name="p1"></param>
/// <param name="p2"></param>
/// <param name="data"></param>
/// <param name="Length"></param>
/// <returns></returns>
u8 PSAMSP_InitDESCrypt(u8 p1, u8 p2, u8 *data, u8 Length) {
    int iResult;
    u8 cPostLen = 0;
    u8 bPostData[256];
    u8 cRespLen = 0;
    u8 bRespData[256];

    //超时时长
    u8 timeoutLen = 1;

    bPostData[cPostLen++] = 0x80;
    bPostData[cPostLen++] = 0x1A;
    // 密钥用途 0x28 加密密钥
    bPostData[cPostLen++] = p1;
    // 密钥版本 0x01
    bPostData[cPostLen++] = p2;

    bPostData[cPostLen++] = Length;

    memcpy(bPostData + cPostLen, data, Length);
    cPostLen += Length;

    iResult = SAM_EXCHANGE_APDU(bPostData, cPostLen, bRespData, &cRespLen, timeoutLen);
    if (iResult != 0) {
        LOGD("SAM_EXCHANGE_APDU error\n");
        return 1;
    }
    if ((bRespData[cRespLen - 2] != 0x90) || (bRespData[cRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}


/**
 * PSAM卡通用DES计算
 * 
 * @Author: XiangGW
 * @Description: TODO
 * @Date: 2015-7-12
 * @param p1
 * @param p2
 * @param data
 * @param Length
 * @return 成功返回0,失败返回错误码
 */
u8 PSAMSP_DESCrypt(u8 *PSAM_RAND, u8 *PSAM_AUTH, u8 p1, u8 p2, u8 cPostLength, u8 *cRespLength) {
    int i;
    int iResult;
    u8 Le;
    u8 cPostLen = 0;
    u8 bPostData[256];
    u8 cRespLen = 0;
    u8 bRespData[256];

    //超时时长
    u8 timeoutLen = 1;

    bPostData[cPostLen++] = 0x80;
    bPostData[cPostLen++] = 0xFA;
    bPostData[cPostLen++] = p1;
    bPostData[cPostLen++] = p2;
    bPostData[cPostLen++] = cPostLength;

    memcpy(bPostData + cPostLen, PSAM_RAND, cPostLength);
    cPostLen += cPostLength;

    iResult = SAM_EXCHANGE_APDU(bPostData, cPostLen, bRespData, &cRespLen, timeoutLen);
    if (iResult != 0) {
        LOGD("SAM_EXCHANGE_APDU error\n");
        return 1;
    }
    if ((bRespData[cRespLen - 2] != 0x90) || (bRespData[cRespLen - 1] != 0x00)) {
        //返回信息61  XX  正确执行
        if (bRespData[cRespLen - 2] == 0x61) {
            Le = bRespData[cRespLen - 1];
        } else {
            return 2;
        }
    }

    // 获取相应信息 Get Response
    //LOGD("GetResponse Le:%d\n",Le);

    // 获取相应信息 Get Response
    cPostLen = 0;
    memset(bPostData, 0, sizeof(bPostData));
    bPostData[cPostLen++] = 0x00;
    bPostData[cPostLen++] = 0xC0;
    bPostData[cPostLen++] = 0x00;
    bPostData[cPostLen++] = 0x00;
    bPostData[cPostLen++] = Le;

    //超时时长
    timeoutLen = 0;
    cRespLen = 0;
    memset(bRespData, 0, sizeof(bRespData));
    iResult = SAM_EXCHANGE_APDU(bPostData, cPostLen, bRespData, &cRespLen, timeoutLen);
    if (iResult != 0) {
        return 3;
    }
    if ((bRespData[cRespLen - 2] != 0x90) || (bRespData[cRespLen - 1] != 0x00)) {
        LOGD("PSAMSP_GetResponse err:%d--%02x-%02x\n", cRespLen, bRespData[cRespLen - 2],
             bRespData[cRespLen - 1]);
        LOGD("接收数据\n");
        for (i = 0; i < cRespLen; i++) {
            LOGD("%02x,", bRespData[i]);
        }
        LOGD("\n");
        return 4;
    }

    //判断接收数据长度是否一致
    if (Le != (cRespLen - 2)) {
        LOGD("接收数据长度错误:%d\n", cRespLen);
        return 5;
    }

//	memset(bRespData,0,sizeof(bRespData));
//    iResult=PSAMSP_GetResponse(Le,bRespData);
//	if(iResult!=0)
//	{
//        if(iResult==1)
//        {
//            LOGD("SAM_EXCHANGE_APDU error\n");
//            return 1;
//        }
//        LOGD("PSAMSP_GetResponse:%d\n",iResult);
//        return 3;
//	}
    memcpy(PSAM_AUTH, bRespData, Le);

    //判断AUTH MAC的合法性
    if (PSAM_AUTH[0] == 0x00) {
        LOGD("PSAM_AUTH mac err\n");
        return 5;
    }
    return 0;
}


/**
 * PSAM卡通用DES计算
 *
 * @Author: XiangGW
 * @Description: TODO
 * @Date: 2015-7-12
 * @param p1
 * @param p2
 * @param data
 * @param Length
 * @return 成功返回0,失败返回错误码
 */
u8 PSAMSP_M1DESCrypt(u8 *PSAM_RAND, u8 *PSAM_AUTH, u8 p1, u8 p2, u8 cPostLength, u8 *cRespLength) {
    u8 i;
    u8 Le;
    int iResult;
    u8 cPostLen = 0;
    u8 bPostData[256];
    u8 cRespLen = 0;
    u8 bRespData[256];

    //超时时长
    u8 timeoutLen = 1;

    bPostData[cPostLen++] = 0x80;
    bPostData[cPostLen++] = 0xFA;

    bPostData[cPostLen++] = p1;

    bPostData[cPostLen++] = p2;
    bPostData[cPostLen++] = cPostLength;

    memcpy(bPostData + cPostLen, PSAM_RAND, cPostLength);
    cPostLen += cPostLength;

    iResult = SAM_EXCHANGE_APDU(bPostData, cPostLen, bRespData, &cRespLen, timeoutLen);
    if (iResult != 0) {
        LOGD("SAM_EXCHANGE_APDU error\n");
        return 1;
    }
    if ((bRespData[cRespLen - 2] != 0x90) || (bRespData[cRespLen - 1] != 0x00)) {
        //返回信息61  XX  正确执行
        if (bRespData[cRespLen - 2] == 0x61) {
            Le = bRespData[cRespLen - 1];
        } else {
            return 2;
        }
    }

    // 获取相应信息 Get Response
    memset(bRespData, 0, sizeof(bRespData));
    iResult = PSAMSP_M1GetResponse(Le, bRespData);
    if (iResult != 0) {
        if (iResult == 1) {
            LOGD("SAM_EXCHANGE_APDU error\n");
            return 1;
        }
        LOGD("PSAMSP_GetResponse:%d\n", iResult);
        return 3;
    }

//    // 获取相应信息 Get Response
//    cPostLen = 0;
//    memset(bPostData,0,sizeof(bPostData));
//    bPostData[cPostLen++] = 0x00;
//    bPostData[cPostLen++] = 0xC0;
//    bPostData[cPostLen++] = 0x00;
//    bPostData[cPostLen++] = 0x00;
//    bPostData[cPostLen++] = Le;

//    //超时时长
//    timeoutLen = 1;
//    cRespLen = 0;
//    memset(bRespData,0,sizeof(bRespData));
//    iResult = SAM_EXCHANGE_APDU(bPostData, cPostLen,  bRespData,  &cRespLen, timeoutLen);
//    if(iResult!=0)
//    {
//        return 3;
//    }
//    if ((bRespData[cRespLen - 2] != 0x90) || (bRespData[cRespLen - 1] != 0x00))
//    {
//        LOGD("PSAMSP_GetResponse err:%d--%02x-%02x\n",cRespLen,bRespData[cRespLen-2],bRespData[cRespLen-1]);

//        LOGD("接收数据\n");
//        for(i=0;i<cRespLen;i++)
//        {
//            LOGD("%02x,",bRespData[i]);
//        }
//        LOGD("\n");

//        return 4;
//    }


//    LOGD("psam接收数据\n");
//    for(i=0;i<Le;i++)
//    {
//        LOGD("%02x,",bRespData[i]);
//    }
//    LOGD("\n");

    memcpy(PSAM_AUTH, bRespData, Le);

    //判断AUTH MAC的合法性
    if (PSAM_AUTH[0] == 0x00) {
        //LOGD("PSAM_AUTH mac err m1\n");
        //return 4;
    }

    return 0;
}


//算出固定PSAM卡的MAC值
u8 PSAMSP_GETMAC(u8 *cMac) {
    u8 cResult, i;

    u8 cStartPos;
    u8 cInfoLen;

    u8 cCardSerialID[8];
    u8 CPU_RAND[8];
    u8 cWriteDataInfo[128];
    u8 MAC[4];

    u8 cPostLen = 0;
    u8 cPostData[256];
    u8 cRespLen = 0;
    u8 cRespData[256];


    memset(cCardSerialID, 0, 8);
    memset(CPU_RAND, 0, 8);
    memset(cPostData, 0, sizeof(cPostData));
    memset(cRespData, 0, sizeof(cRespData));

    memset(cWriteDataInfo, 0x01, sizeof(cWriteDataInfo));

    //LOGD("===============写数据，操作PSAM卡==================\n");

    u8 cKeyVerID = 1;
    int passWordver = cKeyVerID * 11 - 3;


    cCardSerialID[0] = 0x00;
    cCardSerialID[1] = 0x00;
    cCardSerialID[2] = 0x00;
    cCardSerialID[3] = 0x00;

    cResult = PSAMSP_InitDESCrypt(0x28, passWordver, cCardSerialID, 8);
    if (cResult != 0) {
        LOGD("PSAMSP_InitDESCrypt指令失败:%d\n", cResult);
        return 7;
    }
    // 算MAC值

    CPU_RAND[0] = 0x00;
    CPU_RAND[1] = 0x00;
    CPU_RAND[2] = 0x00;
    CPU_RAND[3] = 0x00;

    cStartPos = 0;
    cInfoLen = 64;

    cPostLen = 0;
    memcpy(cPostData, CPU_RAND, 8);
    cPostLen += sizeof(CPU_RAND);

    cPostData[cPostLen++] = 0x04;
    cPostData[cPostLen++] = 0xD6;
    cPostData[cPostLen++] = 0x00;
    cPostData[cPostLen++] = (cStartPos & 0xff);
    cPostData[cPostLen++] = (cInfoLen + 4);

    memcpy(cPostData + cPostLen, cWriteDataInfo, cInfoLen);
    cPostLen += cInfoLen;

    cPostData[cPostLen++] = 0x80;
    while (cPostLen % 8 != 0) {
        cPostData[cPostLen++] = 0x00;
    }

    memset(MAC, 0, sizeof(MAC));
    cResult = PSAMSP_DESCrypt(cPostData, MAC, 0x05, 0x00, cPostLen, &cRespLen);
    if (cResult != 0) {
        LOGD("PSAMSP_DESCrypt指令失败:%d\n", cResult);
        return 9;
    }

    LOGD("MAC值 \n");
    for (i = 0; i < 4; i++) {
        LOGD("%02x.", MAC[i]);
    }
    LOGD("\n");

    memcpy(cMac, MAC, 4);

    return 0;
}

//PSAM计算M1密钥
u8 PSAMSP_GETM1Key(u8 *cOutcKey) {
    u8 i;
    u8 cResult;
    u8 cKey[16];
    u8 bCardUID[8];
    u8 cSectorID = 1;

    u8 cPostLen = 0;
    u8 cPostData[128];
    u8 cRespLen = 0;
    u8 cRespData[128];

    memset(cPostData, 0, sizeof(cPostData));
    memset(cRespData, 0, sizeof(cRespData));

    memset(bCardUID, 0x11, sizeof(bCardUID));

    cKey[0] = (u8) (bCardUID[0] + bCardUID[1]);
    cKey[1] = (u8) (bCardUID[2] ^ (cSectorID * 4 + 3));
    cKey[2] = (u8) (bCardUID[3] | bCardUID[0]);
    cKey[3] = (u8) (bCardUID[0] ^ bCardUID[3]);
    cKey[4] = (u8) (bCardUID[1] ^ (1 + 0x58));
    cKey[5] = (u8) ((bCardUID[2] & bCardUID[0]) + (u8) 0x90);

    memcpy(cPostData, cKey, 6);

    cPostData[6] = 0x80;
    cPostData[7] = 0xF7;
    cPostData[8] = 0x87;
    cPostData[9] = 0x00;

    cKey[0] = (u8) (bCardUID[0] + bCardUID[1]);
    cKey[1] = (u8) (bCardUID[2] ^ (cSectorID * 4 + 3));
    cKey[2] = (u8) (bCardUID[3] | bCardUID[0]);
    cKey[3] = (u8) (bCardUID[0] ^ bCardUID[3]);
    cKey[4] = (u8) (bCardUID[1] ^ (2 + 0x58));
    cKey[5] = (u8) ((bCardUID[2] & bCardUID[0]) + (u8) 0x90);

    memcpy(cPostData + 10, cKey, 6);

    u8 cKeyVerID = 1;
    int passWordver = cKeyVerID * 11 - 5;

    cResult = PSAMSP_InitDESCrypt(0x28, passWordver, bCardUID, 8);
    if (cResult != 0) {
        LOGD("PSAMSP_InitDESCrypt:%d\n", cResult);
        if (cResult == 1) {
            return 99;
        }
        return 7;
    }
    Sleepus(5 * 1000);
    cPostLen = 16;

    cResult = PSAMSP_M1DESCrypt(cPostData, cRespData, 0x00, 0x00, cPostLen, &cRespLen);
    if (cResult != 0) {
        LOGD("PSAMSP_DESCrypt:%d\n", cResult);
        if (cResult == 1) {
            return 99;
        }
        return 8;
    }

    LOGD("KEY值 \n");
    for (i = 0; i < 16; i++) {
        LOGD("%02x.", cRespData[i]);
    }
    LOGD("\n");

    //获取到B密钥
    memcpy(cOutcKey, cRespData + 10, 6);

    return 0;
}



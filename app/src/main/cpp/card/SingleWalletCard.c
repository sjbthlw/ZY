/*******************************************************************************
* Copyright 2017, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：SingleWalletCard.c
*
* 文件标识： SingleWalletCard
* 摘    要：单钱包卡种功能函数
*           科思NFC卡
*           Simpass卡
*           湖南建行卡
*           Uimpass卡
*           *
* 当前版本：V1.0
* 作    者：linz
* 完成日期：2017/6/12
* 编译环境：qt
*
* 历史信息：
*******************************************************************************/
#include "CardInfo.h"
#include "CPUCard.h"
#include "SingleWalletCard.h"

#include "../config.h"
#include "../bsp/pn512.h"
#include "../Deslib.h"

//-------------------------------------simpass卡uimpass卡相关函数-------------------------------------//
//计算恒宝握奇通用版simpass卡写自定义数据MAC
long MSIM_Key16Mac(char cStartPos, char cInfoLen, u8 *bSelfInfo, u8 *bOutData) {
    u8 bChallenge[4];
    u8 bInitData[96];
    u8 bTempData[96];
    u8 bSelfMACData[96];
    u8 bCardKey[16];
    u8 kBuf[16];
    long lngResult;
    u8 i, j, aLen;

    memset(bCardKey, 0, sizeof(bCardKey));
    //SIMPASS初始应用维护密钥:1扇区A密钥+控制位+B密钥
//    memcpy(bCardKey,s_UCardAuthKeyA[1],6);
//    bCardKey[6]=0x80;
//    bCardKey[7]=0xF7;
//    bCardKey[8]=0x87;
//    bCardKey[9]=0x00;
//    memcpy(bCardKey+10,s_UCardAuthKeyB[1],6);

    //取随机数
    lngResult = CPU_GetRandom(bChallenge, 4);
    if (lngResult != 0) {
        //Debug_Print("取随机数失败");
        return 1;
    }

    //初始值(随机数+00 00 00 00)
    i = 0;
    memcpy(bInitData + i, bChallenge, 4);
    i = i + 4;
    bInitData[i++] = 0x00;
    bInitData[i++] = 0x00;
    bInitData[i++] = 0x00;
    bInitData[i++] = 0x00;

    //组织数据
    i = 0;
    bSelfMACData[i++] = 0x04;
    bSelfMACData[i++] = 0xD6;
    bSelfMACData[i++] = 0x00;
    bSelfMACData[i++] = cStartPos;
    bSelfMACData[i++] = cInfoLen + 4;
    memcpy(bSelfMACData + i, bSelfInfo, cInfoLen);
    i = i + cInfoLen;

    bSelfMACData[i++] = 0x80;
    while (i % 8 != 0) {
        bSelfMACData[i++] = 0x00;
    }

    aLen = i;

    //DES3算法
    for (i = 0; i < aLen / 8; i++) {
        for (j = 0; j < 8; j++) {
            bInitData[j] ^= bSelfMACData[i * 8 + j];
        }

        memcpy(kBuf, bCardKey, 16);
        DES3(kBuf, bInitData, bTempData, 0);
        for (j = 0; j < 8; j++) {
            bInitData[j] = bTempData[j];
        }
    }
    memcpy(kBuf, bCardKey, 16);

    DES3(kBuf + 8, bInitData, bTempData, 1);
    for (j = 0; j < 8; j++) {
        bInitData[j] = bTempData[j];
    }

    memcpy(kBuf, bCardKey, 16);
    DES3(kBuf, bInitData, bTempData, 0);
    for (j = 0; j < 4; j++) {
        bOutData[j] = bTempData[j];
    }

    return 0;
}

//选择翼机通环境
long SelectTelPSE(u8 *bAppInfo, int iMode) {
    u8 dwDataLen = 0;
    u8 bSendData[64];
    u8 bRecvData[128];
    unsigned char ucRespLen;
    char cResult;

    memset(bSendData, 0, 64);
    dwDataLen = 0;

    //报文内容
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0xA4;
    bSendData[dwDataLen++] = 0x04;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x10;
    if (iMode == 0) {
        //(1)校园翼机通 D156000040 1130300000010200000000
        bSendData[dwDataLen++] = 0xD1;
        bSendData[dwDataLen++] = 0x56;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x40;

        bSendData[dwDataLen++] = 0x11;
        bSendData[dwDataLen++] = 0x30;
        bSendData[dwDataLen++] = 0x30;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x01;
        bSendData[dwDataLen++] = 0x02;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
    } else {
        //(2)校园翼机通 D156000040 1020300000001100000000
        bSendData[dwDataLen++] = 0xD1;
        bSendData[dwDataLen++] = 0x56;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x40;

        bSendData[dwDataLen++] = 0x10;
        bSendData[dwDataLen++] = 0x20;
        bSendData[dwDataLen++] = 0x30;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x11;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
    }

    memset(bRecvData, 0, 128);
    cResult = Card_ProAPDU(bSendData, dwDataLen, bRecvData, &ucRespLen);
    if (cResult != 0) {
        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        //SW1,SW2:%02x %02x",bRecvData[ucRespLen-2],bRecvData[ucRespLen-1]);
        return 2;
    }

    memcpy(bAppInfo, bRecvData, 0x0D);

    return 0;
}

//选择所有校园翼机通环境 2015.0826
long SelectTelAllPSE(void) {
    long lngResult;
    u8 bAppInfo[64];

    memset(bAppInfo, 0, 64);

    lngResult = SelectTelPSE(bAppInfo, 0);
    if (lngResult == 0) {
        //选择校园翼机通成功
        return 0;
    } else {
        lngResult = SelectTelPSE(bAppInfo, 1);
        if (lngResult == 0) {
            //选择校园翼机通成功
            return 0;
        }
    }
    return 1;
}
/*
//选择一卡通应用
long SIMP_SelectApplication(void)
{
    //int i;
    char dwDataLen=0;
    u8 bSendData[16];
    u8 bRecvData[64];
    u8 ucRespLen;
    char cResult;

    dwDataLen=0;
    bSendData[dwDataLen++]=0x00;
    bSendData[dwDataLen++]=0xA4;
    bSendData[dwDataLen++]=0x00;
    bSendData[dwDataLen++]=0x00;
    bSendData[dwDataLen++]=0x02;
    bSendData[dwDataLen++]=0x3F;
    bSendData[dwDataLen++]=0x01;

    memset(bRecvData,0,64);
    cResult= Card_ProAPDU(bSendData,dwDataLen,bRecvData,&ucRespLen);
    if(cResult!=0)
    {
        return 1;
    }

    if(ucRespLen>64)
    {
        return 3;
    }

    if((bRecvData[ucRespLen-2]!=0x90) || (bRecvData[ucRespLen-1]!=0x00))
    {
        if(bRecvData[ucRespLen-1]!=0x90)
        {
            return 2;
        }
    }
    return 0;
}
*/
//选择所有一卡通应用 2015.0826
long SIMP_SelectAllApplication(void) {
    long lngResult;


    lngResult = SIMP_SelectApplication(0);
    if (lngResult == 0) {
        //选择所有一卡通应用成功
        return 0;
    } else {
        lngResult = SIMP_SelectApplication(1);
        if (lngResult == 0) {
            //选择所有一卡通应用成功
            return 0;
        } else {
            lngResult = SIMP_SelectApplication(2);
            if (lngResult == 0) {
                //选择所有一卡通应用成功
                return 0;
            }
        }
    }
    return 1;
}

//选择一卡通应用 使用AID模式 2015.0826
long SIMP_SelectApplication(int iMode) {
    u8 dwDataLen = 0;
    u8 bSendData[64];
    u8 bRecvData[128];
    unsigned char ucRespLen;
    char cResult;

    memset(bSendData, 0, 64);
    dwDataLen = 0;

    //报文内容
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0xA4;
    bSendData[dwDataLen++] = 0x04;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x10;

    if (iMode == 0) {
        //(3)DF01或 D156000040 1130300000000100000000
        bSendData[dwDataLen++] = 0xD1;
        bSendData[dwDataLen++] = 0x56;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x40;

        bSendData[dwDataLen++] = 0x11;
        bSendData[dwDataLen++] = 0x30;
        bSendData[dwDataLen++] = 0x30;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x01;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
    } else if (iMode == 1) {
        //(4)DF01或 8888888888 1130300000000100000000
        bSendData[dwDataLen++] = 0x88;
        bSendData[dwDataLen++] = 0x88;
        bSendData[dwDataLen++] = 0x88;
        bSendData[dwDataLen++] = 0x88;
        bSendData[dwDataLen++] = 0x88;

        bSendData[dwDataLen++] = 0x11;
        bSendData[dwDataLen++] = 0x30;
        bSendData[dwDataLen++] = 0x30;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x01;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
    } else {
        dwDataLen = 0;
        //(4)DF01
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0xA4;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x00;
        bSendData[dwDataLen++] = 0x02;
        bSendData[dwDataLen++] = 0x3F;
        bSendData[dwDataLen++] = 0x01;
    }

    memset(bRecvData, 0, 128);
    cResult = Card_ProAPDU(bSendData, dwDataLen, bRecvData, &ucRespLen);
    if (cResult != 0) {
        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        //SW1,SW2:%02x %02x",bRecvData[ucRespLen-2],bRecvData[ucRespLen-1]);
        return 2;
    }

    return 0;
}

//选择公共目录
long SIMP_SelectPublicDF(void) {
    u8 dwDataLen = 0;
    u8 bSendData[16];
    u8 bRecvData[64];
    u8 ucRespLen;
    char cResult;
    //int i;

    dwDataLen = 0;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0xA4;

    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x02;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x15;

    memset(bRecvData, 0, 64);
    cResult = Card_ProAPDU(bSendData, dwDataLen, bRecvData, &ucRespLen);
    if (cResult != 0) {
        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        if (bRecvData[ucRespLen - 1] != 0x00) {
            return 2;
        }
    }

    return 0;
}

//获取公共信息
long SIMP_ReadPublicInfo(void) {
    u8 dwDataLen = 0;
    u8 bSendData[16];
    u8 bRecvData[64];
    u8 ucRespLen;
    char cResult;

    dwDataLen = 0;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0xB0;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x0C;
    bSendData[dwDataLen++] = 0x08;

    memset(bRecvData, 0, 64);
    cResult = Card_ProAPDU(bSendData, dwDataLen, bRecvData, &ucRespLen);
    if (cResult != 0) {
        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        return 2;
    }

    //memcpy(bPublicInfo,bRecvData,8);
    //memcpy(bSPAppContextID,bPublicInfo,8);

    return 0;
}

//选择自定义目录
long SIMP_SelectSelfDF(void) {
    u8 dwDataLen = 0;
    u8 bSendData[16];
    u8 bRecvData[64];
    unsigned char ucRespLen;
    char cResult;
    //int i;

    dwDataLen = 0;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0xA4;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x02;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x19;

    memset(bRecvData, 0, 64);
    cResult = Card_ProAPDU(bSendData, dwDataLen, bRecvData, &ucRespLen);
    if (cResult != 0) {
        return 1;
    }


    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {

        return 2;
    }

    return 0;
}

//获取自定义应用数据
long SIMP_ReadSelfInfo(char cStartPos, char cInfoLen, u8 *bSelfInfo) {
    u8 dwDataLen = 0;
    u8 bSendData[16];
    u8 bRecvData[64];
    unsigned char ucRespLen;
    char cResult;

    dwDataLen = 0;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0xB0;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = cStartPos;
    bSendData[dwDataLen++] = cInfoLen;

    memset(bRecvData, 0, 64);
    cResult = Card_ProAPDU(bSendData, dwDataLen, bRecvData, &ucRespLen);
    if (cResult != 0) {
        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        return 2;
    }

    memcpy(bSelfInfo, bRecvData, cInfoLen);

    return 0;
}

/*************************************************************************/
//选择SimPass卡一卡通应用和自定义目录
/*************************************************************************/
long SIMP_SelectAPPANDSelfDF(void) {

    long lngResult;
    u8 bCardSerialID[8];

    lngResult = ReadCPUCardSID(bCardSerialID);
    if (lngResult != 0) {
        return 61;
    }
//    //选择一卡通应用
//    lngResult=SIMP_SelectApplication();
//    if(lngResult!=0)
//    {
//        return 61;
//    }
    //判断电信NFC卡 2015.0811
    //选择翼机通环境");
    SelectTelAllPSE();
    //选择一卡通应用
    //再次选择一卡通应用");
    lngResult = SIMP_SelectAllApplication();
    if (lngResult != 0) {
        //选择一卡通应用失败");
        return 61;
    }
    //选择自定义目录
    lngResult = SIMP_SelectSelfDF();
    if (lngResult != 0) {
        return 61;
    }

    return 0;
}

//写自定义应用数据
long SIMP_WriteSelfData(char cStartPos, char cInfoLen, u8 *bSelfInfo, u8 *bInData) {
    u8 dwDataLen;
    u8 bSendData[128];
    u8 bRecvData[256];
    unsigned char ucRespLen;
    char cResult;
    //int i;

    dwDataLen = 0;
    bSendData[dwDataLen++] = 0x04;
    bSendData[dwDataLen++] = 0xD6;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = cStartPos;
    bSendData[dwDataLen++] = cInfoLen + 4;
    memcpy(bSendData + dwDataLen, bSelfInfo, cInfoLen);
    dwDataLen = dwDataLen + cInfoLen;

    memcpy(bSendData + dwDataLen, bInData, 4);
    dwDataLen = dwDataLen + 4;

    memset(bRecvData, 0, sizeof(bRecvData));
    cResult = Card_ProAPDU(bSendData, dwDataLen, bRecvData, &ucRespLen);
    if (cResult != 0) {

        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        return 2;
    }

    return 0;
}

//SIMPASS卡写数据
long SIMP_WriteData(char cStartPos, char cInfoLen, u8 *bSelfInfo) {
    long lngResult;
    u8 bInData[8], rlen;

    SIMP_SelectAPPANDSelfDF();
    //写自定义数据流程
    lngResult = MSIM_Key16Mac(cStartPos, cInfoLen, bSelfInfo, bInData);
    if (lngResult == 0) {

        for (rlen = 0; rlen < 3; rlen++) {
            lngResult = SIMP_WriteSelfData(cStartPos, cInfoLen, bSelfInfo, bInData);
            if (lngResult == 0) {
                //Debug_Print("写自定义应用数据成功");
                return 0;
            }
        }
        if (rlen >= 3) {
            //sprintf(Debug_Str,"写自定义应用数据结果:%ld",lngResult);
            //Debug_Print(Debug_Str);
            //写自定义应用数据失败，重试1");
            return 62;
        }
    } else {
        return 62;
    }

    return 0;
}

//读simpass/uimpass卡
u8 ReadSIMPCardProcess(u8 *bCardContext) {

    long lngResult;
    //u8  bCardSerialID[8];
    u8 bTempContext[64] = {0};


#if 0
    //选择一卡通应用
    lngResult=SIMP_SelectApplication();
    if(lngResult!=0)
    {
        return 61;
    }
#endif
    //判断电信NFC卡 2015.0811
    //选择翼机通环境");
    //lngResult=SelectTelPSE(bAppInfo);
    lngResult = SelectTelAllPSE();
    if (lngResult != 0) {
        //选择翼机通环境失败");
    } else {
        //选择翼机通环境用成功");
    }

    //选择一卡通应用
    //选择一卡通应用");
    lngResult = SIMP_SelectAllApplication();
    if (lngResult != 0) {
        //选择应用失败");
        return 61;
    } else {
        //选择应用成功");
    }

    //选择公共目录
    lngResult = SIMP_SelectPublicDF();
    if (lngResult != 0) {
        return 61;
    }

    //读取公共信息
    lngResult = SIMP_ReadPublicInfo();
    if (lngResult != 0) {
        lngResult = SIMP_ReadPublicInfo();
        if (lngResult != 0) {
            return 61;
        }
    }

    //选择自定义目录1
    lngResult = SIMP_SelectSelfDF();
    if (lngResult != 0) {
        return 61;
    }

    //读取自定义应用数据2
    lngResult = SIMP_ReadSelfInfo(0, 32, bTempContext);
    if (lngResult != 0) {

        lngResult = SIMP_ReadSelfInfo(0, 32, bTempContext);
        if (lngResult != 0) {
            return 61;
        }
    }

    //选择自定义目录2
    lngResult = SIMP_SelectSelfDF();
    if (lngResult != 0) {
        return 61;
    }

    //读取自定义应用数据2
    lngResult = SIMP_ReadSelfInfo(32, 32, bTempContext + 32);
    if (lngResult != 0) {

        lngResult = SIMP_ReadSelfInfo(32, 32, bTempContext + 32);
        if (lngResult != 0) {
            return 61;
        }
    }
    memcpy(bCardContext, bTempContext, sizeof(bTempContext));
    return 0;

}

//-------------------------------------湖南大众建行CPU卡----------------------------------------------//

/**********************************************函数定义***********************************************
* 函数名称: long HN_CPU_SelectFile(void)
* 输入参数:
* 返回参数: long 0：成功，其他失败
* 功    能: 选择CPU卡应用文件
* 作    者: by linz
* 日    期: 2017/03/18
******************************************************************************************************/
//选择应用环境 新人行应用先select湖南建行环境AID
long HN_CPU_SelectCBCAid(void) {
    unsigned char i;
    unsigned char ucResult;
    unsigned char bTempContext[32];
    unsigned char bRespContext[128];
    unsigned char ucRespLen;

    //选择应用文件指令00 a4 04 00 0f 48 55 4e 2e 48 59 53 59 53 2e 44 44 46 30 31
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xA4;
    bTempContext[i++] = 0x04;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x0F;
    bTempContext[i++] = 0x48;
    bTempContext[i++] = 0x55;
    bTempContext[i++] = 0x4E;
    bTempContext[i++] = 0x2e;
    bTempContext[i++] = 0x48;
    bTempContext[i++] = 0x59;
    bTempContext[i++] = 0x53;
    bTempContext[i++] = 0x59;
    bTempContext[i++] = 0x53;
    bTempContext[i++] = 0x2e;
    bTempContext[i++] = 0x44;
    bTempContext[i++] = 0x44;
    bTempContext[i++] = 0x46;
    bTempContext[i++] = 0x30;
    bTempContext[i++] = 0x31;

    memset(bRespContext, 0, sizeof(bRespContext));
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}

//选择应用环境捷德新COS
long HN_CPU_SelectCBCAdf(void) {
    unsigned char i;
    unsigned char ucResult;
    unsigned char bTempContext[32];
    unsigned char bRespContext[128];
    unsigned char ucRespLen;

    //选择应用文件指令00 a4 04 00 09 41 44 46 34 33 30 30 44 31
    //00 a4 04 00 09 41 44 46 30 30 33 46 30 38
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xA4;
    bTempContext[i++] = 0x04;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x09;
    bTempContext[i++] = 0x41;
    bTempContext[i++] = 0x44;
    bTempContext[i++] = 0x46;
    bTempContext[i++] = 0x34;
    bTempContext[i++] = 0x33;
    bTempContext[i++] = 0x30;
    bTempContext[i++] = 0x30;
    bTempContext[i++] = 0x44;
    bTempContext[i++] = 0x31;

    memset(bRespContext, 0, sizeof(bRespContext));
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}

//选择应用环境恒宝COS
long HN_CPU_SelectHBcbcAdf(void) {
    unsigned char i;
    unsigned char ucResult;
    unsigned char bTempContext[32];
    unsigned char bRespContext[128];
    unsigned char ucRespLen;

    //选择应用文件指令00 a4 04 00 09 41 44 46 30 30 33 46 30 38
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xA4;
    bTempContext[i++] = 0x04;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x09;
    bTempContext[i++] = 0x41;
    bTempContext[i++] = 0x44;
    bTempContext[i++] = 0x46;
    bTempContext[i++] = 0x30;
    bTempContext[i++] = 0x30;
    bTempContext[i++] = 0x33;
    bTempContext[i++] = 0x46;
    bTempContext[i++] = 0x30;
    bTempContext[i++] = 0x38;

    memset(bRespContext, 0, sizeof(bRespContext));
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}

//选择应用文件
long HN_CPU_SelectFile(void) {
    unsigned char i;
    unsigned char ucResult;
    unsigned char bTempContext[32];
    unsigned char bRespContext[128];
    unsigned char ucRespLen;

    //选择应用文件指令00 A4 04 00 09 BA FE C4 CF BD A8 D0 01 01
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xA4;
    bTempContext[i++] = 0x04;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x09;
    bTempContext[i++] = 0xBA;
    bTempContext[i++] = 0xFE;
    bTempContext[i++] = 0xC4;
    bTempContext[i++] = 0xCF;
    bTempContext[i++] = 0xBD;
    bTempContext[i++] = 0xA8;
    bTempContext[i++] = 0xD0;
    bTempContext[i++] = 0x01;
    bTempContext[i++] = 0x01;

    memset(bRespContext, 0, sizeof(bRespContext));
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}

//读应用文件(信息)
long HN_CPU_ReadFile(unsigned char cPos, unsigned char cLen, unsigned char *bBinaryContext) {
    unsigned char i;
    unsigned char ucResult;
    unsigned char bTempContext[128];
    unsigned char bRespContext[128];
    unsigned char ucRespLen;

    //读应用文件
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xB0;
    bTempContext[i++] = 0x19;
    bTempContext[i++] = (cPos & 0x00FF);
    bTempContext[i++] = (cLen & 0x00FF);
    memset(bRespContext, 0, sizeof(bRespContext));
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);

    //ucResult:%d,%d",ucResult,ucRespLen);
    for (i = 0; i < ucRespLen; i++) {
        //%02x.",bRespContext[i]);
    }
    //\n");
    /////////
    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    memcpy(bBinaryContext, bRespContext, ucRespLen - 2);
    return 0;
}

//写二进制文件、写和读(信息)
//cFileID=15
long HN_CPU_WriteFile(u8 cPos, u8 cLen, u8 *bBinaryContext, u8 *bInData) {
    unsigned char i;
    unsigned char ucResult;
    unsigned char bTempContext[128];
    unsigned char bRespContext[128];
    unsigned char ucRespLen;

    //写二进制文件
    i = 0;
    bTempContext[i++] = 0x04;
    bTempContext[i++] = 0xD6;
    bTempContext[i++] = 0x19;

    bTempContext[i++] = (cPos & 0x00FF);

    bTempContext[i++] = cLen + 4;

    memcpy(bTempContext + i, bBinaryContext, cLen);
    i = i + cLen;

    memcpy(bTempContext + i, bInData, 4);
    i = i + 4;

    memset(bRespContext, 0, sizeof(bRespContext));
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}

//计算湖南建行卡卡写自定义数据MAC
long MHN_Key16Mac(u8 *cCardSID, char cStartPos, char cInfoLen, u8 *bSelfInfo, u8 *bOutData) {
    u8 bChallenge[4];
    u8 bInitData[96];
    u8 bTempData[96];
    u8 bSelfMACData[96];
    u8 bCardKey[16];
    u8 kBuf[16];
    long lngResult;
    u8 i, j, aLen;

    memset(bCardKey, 0, sizeof(bCardKey));
    //HN初始应用维护密钥:1扇区A密钥+控制位+B密钥
//    memcpy(bCardKey,s_UCardAuthKeyA[1],6);
//    bCardKey[6]=cCardSID[3];
//    bCardKey[7]=cCardSID[2];
//    bCardKey[8]=cCardSID[1];
//    bCardKey[9]=cCardSID[0];
//    memcpy(bCardKey+10,s_UCardAuthKeyB[1],6);

    lngResult = CPU_GetRandom(bChallenge, 4);
    if (lngResult != 0) {
        //Debug_Print("取随机数失败");
        return 1;
    }

    //初始值(随机数+00 00 00 00)
    memcpy(bInitData, bChallenge, 4);
    i = 4;
    bInitData[i++] = 0x00;
    bInitData[i++] = 0x00;
    bInitData[i++] = 0x00;
    bInitData[i++] = 0x00;

    //组织数据
    i = 0;
    bSelfMACData[i++] = 0x04;
    bSelfMACData[i++] = 0xD6;
    bSelfMACData[i++] = 0x19;
    bSelfMACData[i++] = cStartPos;
    bSelfMACData[i++] = cInfoLen + 4;
    memcpy(bSelfMACData + i, bSelfInfo, cInfoLen);
    i = i + cInfoLen;

    bSelfMACData[i++] = 0x80;
    while ((i & 7) != 0) {
        bSelfMACData[i++] = 0x00;
    }

    aLen = i;
    //DES3算法
    for (i = 0; i < (aLen >> 3); i++) {
        for (j = 0; j < 8; j++) {
            bInitData[j] ^= bSelfMACData[i * 8 + j];
        }

        memcpy(kBuf, bCardKey, 16);
        DES3(kBuf, bInitData, bTempData, 0);
        for (j = 0; j < 8; j++) {
            bInitData[j] = bTempData[j];
        }
    }
    memcpy(kBuf, bCardKey, 16);

    DES3(kBuf + 8, bInitData, bTempData, 1);
    for (j = 0; j < 8; j++) {
        bInitData[j] = bTempData[j];
    }

    memcpy(kBuf, bCardKey, 16);
    DES3(kBuf, bInitData, bTempData, 0);
    for (j = 0; j < 4; j++) {
        bOutData[j] = bTempData[j];
    }
    return 0;
}

long HN_CPU_WriteData(char cStartPos, char cInfoLen, u8 *bSelfInfo, u8 cHNcbcState) {
    long lngResult;
    u8 rlen;
    u8 resp[64];
    u8 bTempKeyMac[8];
    u8 bCardTempID[8];
    //进入正元密钥流程");

    ReadCPUCardSID(bCardTempID);

    //选择一卡通应用
    //选择应用环境 新人行应用先select湖南建行环境AID");
    lngResult = HN_CPU_SelectCBCAid();
    if (lngResult == 0) {
        if (cHNcbcState == 1) {
            //湖南建行环境恒宝COS ADF");
            lngResult = HN_CPU_SelectHBcbcAdf();
            if (lngResult != 0) {
                return 61;
            }
        } else {
            lngResult = HN_CPU_SelectFile();
            if (lngResult != 0) {
                //湖南建行环境捷德COS ADF");
                lngResult = HN_CPU_SelectCBCAdf();
                if (lngResult != 0) {
                    return 61;
                }

            }
        }
    } else {
        lngResult = HN_CPU_SelectFile();
        if (lngResult != 0) {
            //湖南建行环境捷德COS ADF");
            lngResult = HN_CPU_SelectCBCAdf();
            if (lngResult != 0) {
                return 61;
            }
        }
    }

    for (rlen = 0; rlen < 3; rlen++) {
        memset(bTempKeyMac, 0, sizeof(bTempKeyMac));
        lngResult = MHN_Key16Mac(bCardTempID, cStartPos, cInfoLen, bSelfInfo, bTempKeyMac);
        if (lngResult == 0) {
            lngResult = HN_CPU_WriteFile(cStartPos, cInfoLen, bSelfInfo, bTempKeyMac);
            if (lngResult == 0) {
                return 0;
            }
        }
    }
    if (rlen >= 3) {
        return 62;
    }
}


//读湖南大众建行卡
u8 ReadHnCPUCardProcess(u8 *bCardContext, u8 cHNcbcState) {
    long lngResult;
    u8 bTempContext[64];

    //3、选择应用文件
    //选择应用环境 新人行应用先select湖南建行环境AID");
    lngResult = HN_CPU_SelectCBCAid();
    if (lngResult == 0) {
        if (cHNcbcState == 1) {
            //湖南建行环境恒宝COS ADF");
            lngResult = HN_CPU_SelectHBcbcAdf();
            if (lngResult != 0) {
                return 61;
            }
        } else {
            lngResult = HN_CPU_SelectFile();
            if (lngResult != 0) {
                //湖南建行环境捷德COS ADF");
                lngResult = HN_CPU_SelectCBCAdf();
                if (lngResult != 0) {
                    return 61;
                }
            }
        }
    } else {
        lngResult = HN_CPU_SelectFile();
        if (lngResult != 0) {
            //湖南建行环境捷德COS ADF");
            lngResult = HN_CPU_SelectCBCAdf();
            if (lngResult != 0) {
                return 61;
            }
        }
    }

    //4、读应用文件
    //读应用文件1");
    lngResult = HN_CPU_ReadFile(0, 32, bTempContext);    //读、写驱动不支持一次读64字节，分两次读取。
    if (lngResult != 0) {
        return 61;
    }
    //读应用文件2");
    lngResult = HN_CPU_ReadFile(32, 32, bTempContext + 32);    //读、写驱动不支持一次读64字节，分两次读取。
    if (lngResult != 0) {
        return 61;
    }
    //读应用文件成功：%ld",lngResult);
    memcpy(bCardContext, bTempContext, sizeof(bTempContext));

    return 0;

}

//===========================================================================================//
//-------------------------------------NFC卡相关----------------------------------------------//
//选择支付系统环境
long NFC_SelectPSE(void) {
    u8 dwDataLen = 0;
    u8 bSendData[64];
    u8 bRecvData[256];
    unsigned char ucRespLen;
    char cResult;

    //选择DF目录
    dwDataLen = 0;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0xA4;
    bSendData[dwDataLen++] = 0x04;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x0e;

    //AID
    bSendData[dwDataLen++] = 0x33;
    bSendData[dwDataLen++] = 0x33;
    bSendData[dwDataLen++] = 0x41;
    bSendData[dwDataLen++] = 0x59;
    bSendData[dwDataLen++] = 0x2e;
    bSendData[dwDataLen++] = 0x53;
    bSendData[dwDataLen++] = 0x59;
    bSendData[dwDataLen++] = 0x53;
    bSendData[dwDataLen++] = 0x2e;
    bSendData[dwDataLen++] = 0x44;
    bSendData[dwDataLen++] = 0x44;
    bSendData[dwDataLen++] = 0x46;
    bSendData[dwDataLen++] = 0x30;
    bSendData[dwDataLen++] = 0x31;

    memset(bRecvData, 0, sizeof(bRecvData));
    cResult = Card_ProAPDU(bSendData, dwDataLen, bRecvData, &ucRespLen);
    if (cResult != 0) {
        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        return 2;
    }

    return 0;
}

//选择一卡通钱包应用
long NFC_SelectBurseApp(void) {
    u8 dwDataLen = 0;
    u8 bSendData[64];
    u8 bRecvData[256];
    unsigned char ucRespLen;
    char cResult;

    dwDataLen = 0;
    //选择DF目录
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0xA4;
    bSendData[dwDataLen++] = 0x04;
    bSendData[dwDataLen++] = 0x00;
    bSendData[dwDataLen++] = 0x07;

    //AID
    bSendData[dwDataLen++] = 0x41;
    bSendData[dwDataLen++] = 0x44;
    bSendData[dwDataLen++] = 0x44;
    bSendData[dwDataLen++] = 0x2e;
    bSendData[dwDataLen++] = 0x30;
    bSendData[dwDataLen++] = 0x30;
    bSendData[dwDataLen++] = 0x31;

    memset(bRecvData, 0, sizeof(bRecvData));

    cResult = Card_ProAPDU(bSendData, dwDataLen, bRecvData, &ucRespLen);
    if (cResult != 0) {
        return 1;
    }

    if ((bRecvData[ucRespLen - 2] != 0x90) || (bRecvData[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}

//读二进制文件、写和读(信息)
//cFileID=19
long NFC_ReadBinaryFile(char cFileID, unsigned char cPos, unsigned char cLen,
                        unsigned char *bBinaryContext) {
    unsigned char i;
    unsigned char ucResult;
    unsigned char bTempContext[64];
    unsigned char bRespContext[128];
    unsigned char ucRespLen;

    //读二进制文件
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xB0;
    bTempContext[i++] = 0x80 + cFileID;
    bTempContext[i++] = (cPos & 0x00FF);
    bTempContext[i++] = cLen;
    memset(bRespContext, 0, sizeof(bRespContext));
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);

    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    memcpy(bBinaryContext, bRespContext, ucRespLen - 2);
    return 0;
}

//写二进制文件、写和读(信息)
//cFileID=19
long NFC_WriteBinaryFile(unsigned char cPos, unsigned char cLen, unsigned char *bBinaryContext,
                         unsigned char *bInitMac) {
    unsigned char i;
    unsigned char ucResult;
    unsigned char bTempContext[64];
    unsigned char bRespContext[256];
    unsigned char ucRespLen;

    //写二进制文件
    i = 0;
    bTempContext[i++] = 0x04;
    bTempContext[i++] = 0xD6;

    bTempContext[i++] = 0x80 + 0x19;
    bTempContext[i++] = (cPos & 0x00FF);

    bTempContext[i++] = cLen + 4;

    memcpy(bTempContext + i, bBinaryContext, cLen);
    i = i + cLen;

    //MAC
    memcpy(bTempContext + i, bInitMac, 4);
    i = i + 4;

    memset(bRespContext, 0, 256);
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);

    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}

//选择NFC卡一卡通应用和自定义目录
long NFC_SelectAPPANDSelfDF(u8 *bCardTempID) {

    long lngResult;
    u8 rlen;
    u8 resp[64];

    ReadCPUCardSID(bCardTempID);

    //选择支付系统环境
    lngResult = NFC_SelectPSE();
    if (lngResult != 0) {
        return 61;
    }

    //选择一卡通应用
    lngResult = NFC_SelectBurseApp();
    if (lngResult != 0) {
        return 61;
    }

    return 1;

}

//NFC卡写数据
long NFC_WriteData(char cStartPos, char cInfoLen, u8 *bSelfInfo) {
#if 1
    int rlen;
    long lngResult;
    u8 cCardSID[8];
    u8 bTempKeyMac[8];

    NFC_SelectAPPANDSelfDF(cCardSID);
    //写自定义数据流程
    lngResult = MHN_Key16Mac(cCardSID, cStartPos, cInfoLen, bSelfInfo, bTempKeyMac);
    if (lngResult == 0) {

        for (rlen = 0; rlen < 3; rlen++) {
            lngResult = NFC_WriteBinaryFile(cStartPos, cInfoLen, bSelfInfo, bTempKeyMac);
            if (lngResult == 0) {
                //Debug_Print("写自定义应用数据成功");
                return 0;
            }
        }
        if (rlen >= 3) {
            //sprintf(Debug_Str,"写自定义应用数据结果:%ld",lngResult);
            //Debug_Print(Debug_Str);
            //写自定义应用数据失败，重试");
            return 62;
        }
    } else {
        return 62;
    }
#endif
    return 1;
}


//读NFC卡
u8 ReadNFCCardProcess(u8 *bCardContext) {
    u8 cResult;
    u8 bTempContext[64] = {0};

    //选择支付系统环境
    cResult = NFC_SelectPSE();
    if (cResult != 0) {
        return 61;
    }

    //选择一卡通钱包应用
    cResult = NFC_SelectBurseApp();
    if (cResult != 0) {
        return 61;
    }

    //读二进制文件
    cResult = NFC_ReadBinaryFile(0x19, 0, 32, bTempContext);
    if (cResult != 0) {
        return 61;
    }

    //读二进制文件
    cResult = NFC_ReadBinaryFile(0x19, 32, 32, bTempContext + 32);
    if (cResult != 0) {

        return 61;
    }
    memcpy(bCardContext, bTempContext, sizeof(bTempContext));
    return 0;

}


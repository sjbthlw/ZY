/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：CPUCard.c 
*
* 文件标识：CPUCard
* 摘    要：CPU卡功能函数集合
*			公用应用信息(0015) 96字节
*			持卡人信息	     (0016) 96字节
*			门禁考勤信息(0018) 64字节
*			水控应用信息(0019) 64字节
*			车载应用信息(0020) 64字节
*			扩展应用信息(0025) 4096字节
*			钱包应用信息(01-06) 64字节(6个钱包)
*
* 当前版本：V1.0
* 作    者：wcp
* 完成日期：2015/8/10
* 编译环境：D:\Program Files (x86)\IAR Systems\Embedded Workbench 6.5\arm
*
* 历史信息：
*******************************************************************************/

#include <android/log.h>
#include "../config.h"
#include "../bsp/pn512.h"
#include "../Deslib.h"
#include "../bsp/public.h"
#include "CPUCard.h"
#include "CardInfo.h"

u8 s_UCardAuthKeyA[32][6]; //用户交易卡密钥A
u8 s_UCardAuthKeyB[32][6]; //用户交易卡密钥B

#define TEST_MODE 0

//-------------------------------------复旦微CPU卡----------------------------------------------//

//微秒级 延时
static void Sleepus(int us)
{
    struct timeval delay;
    delay.tv_sec = 0;
    delay.tv_usec = us ; //  us
    select(0, NULL, NULL, NULL, &delay);
}
/**********************************************函数定义***************************************************** 
* 函数名称: u8 ReadCPUCardSID(u8 *cCardSID) 
* 输入参数: u8 *cCardSID 
* 返回参数: u8  
* 功    能: 读CPU卡卡号并选择CPU部分  
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/

u8 ReadCPUCardNum(u8 *cCardSID) {
    u8 cCardTempSID[16]={0};
    u8 cResult;
    ACTIVEPARAA pActiParaA;
    u8 resp[128]={0};
    u8 rlen = 0;

    memset(cCardTempSID, 0, sizeof(cCardTempSID));

    Card_RfReset(5, 5);
    Card_Halt();
    //Sleepus(1000);
    cResult = PiccActivate(1, ISO14443_3A_REQALL, &pActiParaA);
    if (cResult != 0) {
        return 2;
    }
    memcpy(cCardTempSID, pActiParaA.UID, 4);

    cResult = Card_SelectProCard(resp, &rlen);
    if (cResult != 0) {
        return 3;
    }
    memcpy(cCardSID, cCardTempSID, 4);
    return 0;
}

u8 ReadCPUCardSID(u8 *cCardSID) {
    u8 i;
    u8 cResult=-1;

    for(i=0;i<3;i++)
    {
        cResult=ReadCPUCardNum(cCardSID);
        if(cResult==0)
            break;
        else
            LOGD("ReadCPUCardSID失败:%d", i);
    }
    return 0;
}


/**********************************************函数定义***************************************************** 
* 函数名称: u8  CPU_GetRandom(u8 *data, u8 Length) 
* 输入参数: u8 *data 随机数数据, u8 Length 随机数长度
* 返回参数: 成功返回0,失败返回错误码
* 功    能: CPU卡取随机数  
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 CPU_GetRandom(u8 *data, u8 Length) {
    u8 cResult;
    u8 cPostLen = 0;
    u8 cPostData[128];
    u8 cRespLen = 0;
    u8 cRespData[128];

    // 取随机数
    cPostData[cPostLen++] = 0x00;
    cPostData[cPostLen++] = 0x84;
    cPostData[cPostLen++] = 0x00;
    cPostData[cPostLen++] = 0x00;
    cPostData[cPostLen++] = Length;

    cResult = Card_ProAPDU(cPostData, cPostLen, cRespData, &cRespLen);
    if (cResult != 0) {
        return 1;
    } else {
        if (cRespData[cRespLen - 2] != 0x90 || cRespData[cRespLen - 1] != 0x00) {
            return 2;
        }
        memcpy(data, cRespData, cRespLen - 2);
        return 0;
    }
}


/**********************************************函数定义***************************************************** 
* 函数名称: u8  CPU_SelectFile(u8 p1, u8 p2, u8 *data, u8 Length) 
* 输入参数: u8 p1,0x00(按文件标识符选择，选择当前目录下基本文件或子目录文件)
*                 0x04(用目录名称选择，选择与当前目录平级的目录、当前目录的下级子目录)
*           u8 p2, 0x00
*           u8 *data, 文件名
*           u8 Length   文件长度
* 返回参数: 成功返回0,失败返回错误码   
* 功    能: CPU卡选择目录或文件  
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 CPU_SelectFile(u8 p1, u8 p2, u8 *data, u8 Length) {
    u8 cResult;
    u8 cPostLen = 0;
    u8 cPostData[128];
    u8 cRespLen = 0;
    u8 cRespData[128];

    cPostData[cPostLen++] = 0x00;
    cPostData[cPostLen++] = 0xA4;
    cPostData[cPostLen++] = p1;
    cPostData[cPostLen++] = p2;
    cPostData[cPostLen++] = Length;

    memcpy(cPostData + cPostLen, data, Length);
    cPostLen += Length;

    cResult = Card_ProAPDU(cPostData, cPostLen, cRespData, &cRespLen);
    if (cResult != 0) {
        return 1;
    } else {
        if (cRespData[cRespLen - 2] != 0x90 || cRespData[cRespLen - 1] != 0x00) {
            return 2;
        }
        return 0;
    }
}

/**********************************************函数定义***************************************************** 
* 函数名称: u8  CPU_WriteBinary(u8 p1, u8 p2, u8 *data, u8 Length) 
* 输入参数: u8 p1,若P1的高三位为100,则低5位为短的文件标示符,P2为读的偏移量.
*           u8 p2,若P1的最高位不为1,则P1 P2为欲读文件的偏移量(P1为偏移量高字节,P2为低字节)
*           u8 *data, 写入数据
*           u8 Length 写入数据的长度
* 返回参数: 成功返回0,失败返回错误码  
* 功    能: CPU卡写二进制文件  
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 CPU_WriteBinary(u8 p1, u8 p2, u8 *data, u8 Length) {
    u8 cResult;
    u8 cPostLen = 0;
    u8 cPostData[128];
    u8 cRespLen = 0;
    u8 cRespData[128];

    cPostData[cPostLen++] = 0x00;
    cPostData[cPostLen++] = 0xD6;
    cPostData[cPostLen++] = p1;
    cPostData[cPostLen++] = p2;
    cPostData[cPostLen++] = Length;

    memcpy(cPostData + cPostLen, data, Length);
    cPostLen += Length;

    cResult = Card_ProAPDU(cPostData, cPostLen, cRespData, &cRespLen);
    if (cResult != 0) {
        return 1;
    } else {
        if (cRespData[cRespLen - 2] != 0x90 || cRespData[cRespLen - 1] != 0x00) {
            return 2;
        }
        return 0;
    }
}


/**********************************************函数定义***************************************************** 
* 函数名称: u8  CPU_ReadBinary(u8 p1, u8 p2, u8 *data, u8 Length) 
* 输入参数: u8 p1,若P1的高三位为100,则低5位为短的文件标示符,P2为读的偏移量.
*           u8 p2,若P1的最高位不为1,则P1 P2为欲读文件的偏移量(P1为偏移量高字节,P2为低字节)
*           u8 *data,读出数据
*           u8 Length 读出数据的长度   //0X00表示读出所以数据
* 返回参数: 成功返回0,失败返回错误码  
* 功    能: CPU卡读二进制文件
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 CPU_ReadBinary(u8 p1, u8 p2, u8 *data, u8 Length) {
    u8 cResult;
    u8 cPostLen = 0;
    u8 cPostData[128];
    u8 cRespLen = 0;
    u8 cRespData[128];

    cPostData[cPostLen++] = 0x00;
    cPostData[cPostLen++] = 0xB0;
    cPostData[cPostLen++] = p1;
    cPostData[cPostLen++] = p2;
    cPostData[cPostLen++] = Length;    //0X00表示读出所以数据

    cResult = Card_ProAPDU(cPostData, cPostLen, cRespData, &cRespLen);
    if (cResult != 0) {
        return 1;
    } else {
        if (cRespData[cRespLen - 2] != 0x90 || cRespData[cRespLen - 1] != 0x00) {
            return 2;
        }
        memcpy(data, cRespData, Length);
        return 0;
    }
}


/**********************************************函数定义***************************************************** 
* 函数名称: u8 CPU_SelectCardMF(void)
* 输入参数: void 
* 返回参数:  成功返回0,失败返回错误码 
* 功    能:   CPU卡选择MF主目录
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 CPU_SelectCardMF(void) {
    u8 cResult;
    u8 cPostLen = 0;
    u8 cPostData[16];

    cPostData[cPostLen++] = 0x3F;
    cPostData[cPostLen++] = 0x00;

    cResult = CPU_SelectFile(0x00, 0x00, cPostData, cPostLen);
    if (cResult != 0) {
        return 1;
    }
    return 0;
}


/**********************************************函数定义***************************************************** 
* 函数名称: u8 CPU_SelectCardAppFile(u16 wFID)
* 输入参数: u16 wFID 选择的文件标示符
* 返回参数: 成功返回0,失败返回错误码   
* 功    能: 选择CPU应用自定义信息文件
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 CPU_SelectCardAppFile(u16 wFID) {
    u8 cResult;
    u8 cPostLen = 0;
    u8 cPostData[16];

    cPostData[cPostLen++] = ((wFID & 0xff00) >> 8);
    cPostData[cPostLen++] = (wFID & 0x00ff);
    cResult = CPU_SelectFile(0x00, 0x00, cPostData, cPostLen);

    return cResult;
}


//读二进制文件、写和读(信息)
//cFileID=91
u8 CPU_ReadBinaryFile(u8 cPos, u8 *bBinaryContext) {
    u8 i;
    u8 ucResult;
    u8 bTempContext[64];
    u8 bRespContext[64];
    u8 ucRespLen;

    //读二进制文件
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xB0;
    bTempContext[i++] = (cPos & 0xFF00) >> 8;
    bTempContext[i++] = (cPos & 0x00FF);
    bTempContext[i++] = 0x00; //全部数据
    memset(bRespContext, 0, 64);
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

//================================================================================================================//
//基本信息1设置
u8 CPU_BasicInfo1_Set(u8 *bBasicContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];
    u8 bReadBasicContext[64];


    cResult = ReadCPUCardSID(bCardSerialID);
    if (cResult != 0) {
        //读卡号失败:%d",cResult);
        return 1;
    }
    cResult = memcmp(cCardSID, bCardSerialID, 4);
    if (cResult != 0) {
        return 2;
    }

    //选择MF目录
    cResult = CPU_SelectMFFile();
    if (cResult != 0) {
        return 3;
    }

    /*
    //取随机数8->DES3算法->外部认证
    //1号密钥
    cResult=CPU_ExterAuthen(1,2);
    if(cResult!=0)
    {
        return 9;
    }
    */
    //选择DF目录");
    cResult = CPU_SelectDFFile(0);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //6号密钥
    //外部认证2");
    cResult = CPU_ExterAuthen(2, 6);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    //选择二进制文件");
    cResult = CPU_SelectBinaryFile(1);
    if (cResult != 0) {
        return 7;
    }

    //写二进制文件
    cResult = CPU_WriteBinaryFile(0, 16, bBasicContext);
    if (cResult != 0) {
        return 8;
    }

    //读二进制文件
    cResult = CPU_ReadBinaryFile(0, bReadBasicContext);
    if (cResult != 0) {
        return 11;
    }

    //比较读写数据
    if (memcmp(bBasicContext, bReadBasicContext, 16) != 0) {
        return 12;
    }

    return 0;
}

//基本信息2设置
u8 CPU_BasicInfo2_Set(u8 *bBasicContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];
    u8 bReadBasicContext[64];

    cResult = ReadCPUCardSID(bCardSerialID);
    if (cResult != 0) {
        return 1;
    }
    cResult = memcmp(cCardSID, bCardSerialID, 4);
    if (cResult != 0) {
        return 2;
    }

    //选择MF目录
    cResult = CPU_SelectMFFile();
    if (cResult != 0) {
        return 3;
    }

    /*
    //取随机数8->DES3算法->外部认证
    //1号密钥
    //外部认证1");
    cResult=CPU_ExterAuthen(1,2);
    if(cResult!=0)
    {
        return 9;
    }
    */

    //选择DF目录
    cResult = CPU_SelectDFFile(0);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //6号密钥
    cResult = CPU_ExterAuthen(2, 6);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(1);
    if (cResult != 0) {
        return 7;
    }

    //写二进制文件
    cResult = CPU_WriteBinaryFile(16, 16, bBasicContext);
    if (cResult != 0) {
        return 8;
    }

    //读二进制文件
    cResult = CPU_ReadBinaryFile(0, bReadBasicContext);
    if (cResult != 0) {
        return 11;
    }

    //比较读写数据
    if (memcmp(bBasicContext, bReadBasicContext + 16, 16) != 0) {
        return 12;
    }

    return 0;
}

//基本信息设置(全部)
u8 CPU_BasicAllInfo_Set(u8 *bBasicContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];
    u8 bReadBasicContext[64];

    cResult = ReadCPUCardSID(bCardSerialID);
    if (cResult != 0) {
        return 1;
    }
    cResult = memcmp(cCardSID, bCardSerialID, 4);
    if (cResult != 0) {
        return 2;
    }

//    //选择MF目录
//    cResult=CPU_SelectMFFile();
//    if(cResult!=0)
//    {
//        return 3;
//    }

    /*
    //取随机数8->DES3算法->外部认证
    //1号密钥
    //外部认证1");
    cResult=CPU_ExterAuthen(1,2);
    if(cResult!=0)
    {
        return 9;
    }
    */

    //选择DF目录
    cResult = CPU_SelectDFFile(0);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //6号密钥
    cResult = CPU_ExterAuthen(2, 6);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(1);
    if (cResult != 0) {
        return 7;
    }

    //写二进制文件
    cResult = CPU_WriteBinaryFile(0, 32, bBasicContext);
    if (cResult != 0) {
        return 8;
    }

    //读二进制文件
    cResult = CPU_ReadBinaryFile(0, bReadBasicContext);
    if (cResult != 0) {
        return 11;
    }

    //比较读写数据
    if (memcmp(bBasicContext, bReadBasicContext, 32) != 0) {
        return 12;
    }

    return 0;
}


//扩展信息1设置
u8 CPU_ExternInfo1_Set(u8 *bExternContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];
    u8 bReadExternContext[64];

    cResult = ReadCPUCardSID(bCardSerialID);
    if (cResult != 0) {
        //读卡号失败:%d",cResult);
        return 1;
    }
    cResult = memcmp(cCardSID, bCardSerialID, 4);
    if (cResult != 0) {
        return 2;
    }


    //选择MF目录
    cResult = CPU_SelectMFFile();
    if (cResult != 0) {
        return 3;
    }

    /*
    //取随机数8->DES3算法->外部认证
    //1号密钥
    cResult=CPU_ExterAuthen(1,2);
    if(cResult!=0)
    {
        return 9;
    }
    */

    //选择DF目录
    cResult = CPU_SelectDFFile(0);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //6号密钥
    cResult = CPU_ExterAuthen(2, 6);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(2);
    if (cResult != 0) {
        return 7;
    }

    //写二进制文件
    cResult = CPU_WriteBinaryFile(0, 16, bExternContext);
    if (cResult != 0) {
        return 8;
    }

    //读二进制文件
    //读二进制文件");
    cResult = CPU_ReadBinaryFile(0, bReadExternContext);
    if (cResult != 0) {
        return 11;
    }

    //比较读写数据
    if (memcmp(bExternContext, bReadExternContext, 16) != 0) {
        return 12;
    }

    return 0;
}

//扩展信息2设置
u8 CPU_ExternInfo2_Set(u8 *bExternContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];
    u8 bReadExternContext[64];

    cResult = ReadCPUCardSID(bCardSerialID);
    if (cResult != 0) {
        //读卡号失败:%d",cResult);
        return 1;
    }
    cResult = memcmp(cCardSID, bCardSerialID, 4);
    if (cResult != 0) {
        return 2;
    }


    //选择MF目录
    cResult = CPU_SelectMFFile();
    if (cResult != 0) {
        return 3;
    }

    //取随机数8->DES3算法->外部认证
    //1号密钥
//    cResult=CPU_ExterAuthen(1,2);
//    if(cResult!=0)
//    {
//        return 9;
//    }

    //选择DF目录
    cResult = CPU_SelectDFFile(0);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //6号密钥
    cResult = CPU_ExterAuthen(2, 6);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(2);
    if (cResult != 0) {
        return 7;
    }

    //写二进制文件
    cResult = CPU_WriteBinaryFile(16, 16, bExternContext);
    if (cResult != 0) {
        return 8;
    }

    //读二进制文件
    //读二进制文件");
    cResult = CPU_ReadBinaryFile(0, bReadExternContext);
    if (cResult != 0) {
        return 11;
    }

    //比较读写数据
    if (memcmp(bExternContext, bReadExternContext + 16, 16) != 0) {
        return 12;
    }

    return 0;
}

//扩展信息3设置
u8 CPU_ExternInfo3_Set(u8 *bExternContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];
    u8 bReadExternContext[64];

    cResult = ReadCPUCardSID(bCardSerialID);
    if (cResult != 0) {
        //读卡号失败:%d",cResult);
        return 1;
    }
    cResult = memcmp(cCardSID, bCardSerialID, 4);
    if (cResult != 0) {
        return 2;
    }

//    //选择MF目录
//    cResult=CPU_SelectMFFile();
//    if(cResult!=0)
//    {
//        return 3;
//    }

    /*
    //取随机数8->DES3算法->外部认证
    //1号密钥
    cResult=CPU_ExterAuthen(1,2);
    if(cResult!=0)
    {
        return 9;
    }
    */
    //选择DF目录
    cResult = CPU_SelectDFFile(0);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //6号密钥
    cResult = CPU_ExterAuthen(2, 6);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(2);
    if (cResult != 0) {
        return 7;
    }

    //写二进制文件
    cResult = CPU_WriteBinaryFile(32, 16, bExternContext);
    if (cResult != 0) {
        return 8;
    }

    //读二进制文件
    //读二进制文件");
    cResult = CPU_ReadBinaryFile(0, bReadExternContext);
    if (cResult != 0) {
        return 11;
    }

    //比较读写数据
    if (memcmp(bExternContext, bReadExternContext + 32, 16) != 0) {
        return 12;
    }

    return 0;
}

//扩展信息设置(全部)
u8 CPU_ExternAllInfo_Set(u8 *bExternContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];
    u8 bReadExternContext[64];

    cResult = ReadCPUCardSID(bCardSerialID);
    if (cResult != 0) {
        //读卡号失败:%d",cResult);
        return 1;
    }
    cResult = memcmp(cCardSID, bCardSerialID, 4);
    if (cResult != 0) {
        return 2;
    }

//    //选择MF目录
//    cResult=CPU_SelectMFFile();
//    if(cResult!=0)
//    {
//        return 3;
//    }

    /*
    //取随机数8->DES3算法->外部认证
    //1号密钥
    cResult=CPU_ExterAuthen(1,2);
    if(cResult!=0)
    {
        return 9;
    }
    */

    //选择DF目录
    cResult = CPU_SelectDFFile(0);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //6号密钥
    cResult = CPU_ExterAuthen(2, 6);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(2);
    if (cResult != 0) {
        return 7;
    }

    //写二进制文件
    cResult = CPU_WriteBinaryFile(0, 48, bExternContext);
    if (cResult != 0) {
        return 8;
    }

    //读二进制文件
    cResult = CPU_ReadBinaryFile(0, bReadExternContext);
    if (cResult != 0) {
        return 11;
    }

    //比较读写数据
    if (memcmp(bExternContext, bReadExternContext, 48) != 0) {
        return 12;
    }

    return 0;
}

//钱包信息设置
u8 CPU_BurseInfo_Set(char cBurseID, u8 *bBurseContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];
    u8 bReadBurseContext[64];

    cResult = ReadCPUCardSID(bCardSerialID);
    if (cResult != 0) {
        //读卡号失败:%d",cResult);
        return 1;
    }
    cResult = memcmp(cCardSID, bCardSerialID, 4);
    if (cResult != 0) {
        return 2;
    }
//    //选择MF目录
//    cResult=CPU_SelectMFFile();
//    if(cResult!=0)
//    {
//        return 3;
//    }

    //取随机数8->DES3算法->外部认证
    //1号密钥
//    cResult=CPU_ExterAuthen(1,2);
//    if(cResult!=0)
//    {
//        return 9;
//    }

    //选择DF目录
    cResult = CPU_SelectDFFile(cBurseID);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //6+cBurseID号密钥
    cResult = CPU_ExterAuthen(2, 6 + cBurseID * 3);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(1);
    if (cResult != 0) {
        return 7;
    }

    //写二进制文件
    cResult = CPU_WriteBinaryFile(0, 16, bBurseContext);
    if (cResult != 0) {
        return 8;
    }

    //读二进制文件
    cResult = CPU_ReadBinaryFile(0, bReadBurseContext);
    if (cResult != 0) {
        return 11;
    }

    //比较读写数据
    if (memcmp(bBurseContext, bReadBurseContext, 16) != 0) {
        return 12;
    }
    return 0;
}

//基本和扩展信息读取
u8 CPU_BasicExternInfo_Get(u8 *bBasicContext, u8 *bExternContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];

//    cResult=ReadCPUCardSID(bCardSerialID);
//    if(cResult!=0)
//    {
//        return 1;
//    }
//    cResult=memcmp(cCardSID,bCardSerialID,4);
//    if(cResult!=0)
//    {
//        return 2;
//    }

//    //选择MF目录
//    cResult=CPU_SelectMFFile();
//    if(cResult!=0)
//    {
//        return 3;
//    }

    /*
    //取随机数8->DES3算法->外部认证
    //1号密钥
    //外部认证1");
    cResult=CPU_ExterAuthen(1,2);
    if(cResult!=0)
    {
        return 9;
    }
    */

    //选择DF目录
    cResult = CPU_SelectDFFile(0);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //5号密钥
    cResult = CPU_ExterAuthen(1, 5);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(1);
    if (cResult != 0) {
        return 7;
    }

    //读二进制文件
    cResult = CPU_ReadBinaryFile(0, bBasicContext);
    if (cResult != 0) {
        return 8;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(2);
    if (cResult != 0) {
        return 9;
    }

    //读二进制文件
    cResult = CPU_ReadBinaryFile(0, bExternContext);
    if (cResult != 0) {
        return 10;
    }

    return 0;
}

//基本信息读取
u8 CPU_BasicInfo_Get(u8 *bBasicContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];

//    cResult=ReadCPUCardSID(bCardSerialID);
//    if(cResult!=0)
//    {
//        return 1;
//    }
//    cResult=memcmp(cCardSID,bCardSerialID,4);
//    if(cResult!=0)
//    {
//        return 2;
//    }

//    //选择MF目录
//    cResult=CPU_SelectMFFile();
//    if(cResult!=0)
//    {
//        return 3;
//    }

    /*
    //取随机数8->DES3算法->外部认证
    //1号密钥
    //外部认证1");
    cResult=CPU_ExterAuthen(1,2);
    if(cResult!=0)
    {
        return 9;
    }
    */

    //选择DF目录
    cResult = CPU_SelectDFFile(0);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //5号密钥
    cResult = CPU_ExterAuthen(1, 5);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(1);
    if (cResult != 0) {
        return 7;
    }

    //读二进制文件
    cResult = CPU_ReadBinaryFile(0, bBasicContext);
    if (cResult != 0) {
        return 8;
    }
    return 0;
}

//扩展信息读取
u8 CPU_ExternInfo_Get(u8 *bExternContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];

//    cResult=ReadCPUCardSID(bCardSerialID);
//    if(cResult!=0)
//    {
//        return 1;
//    }
//    cResult=memcmp(cCardSID,bCardSerialID,4);
//    if(cResult!=0)
//    {
//        return 2;
//    }

//    //选择MF目录
//    cResult=CPU_SelectMFFile();
//    if(cResult!=0)
//    {
//        return 3;
//    }

    /*
    //取随机数8->DES3算法->外部认证
    //1号密钥
    cResult=CPU_ExterAuthen(1,2);
    if(cResult!=0)
    {
        return 9;
    }
    */

    //选择DF目录
    cResult = CPU_SelectDFFile(0);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //5号密钥
    cResult = CPU_ExterAuthen(1, 5);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(2);
    if (cResult != 0) {
        return 7;
    }

    //读二进制文件
    cResult = CPU_ReadBinaryFile(0, bExternContext);
    if (cResult != 0) {
        return 8;
    }
    return 0;

}

//钱包信息读取
u8 CPU_BurseInfo_Get(char cBurseID, u8 *bBurseContext, u8 *cCardSID) {
    u8 cResult;
    u8 bCardSerialID[8];

//    cResult=ReadCPUCardSID(bCardSerialID);
//    if(cResult!=0)
//    {
//        return 1;
//    }
//    cResult=memcmp(cCardSID,bCardSerialID,4);
//    if(cResult!=0)
//    {
//        return 2;
//    }

//    //选择MF目录
//    cResult=CPU_SelectMFFile();
//    if(cResult!=0)
//    {
//        return 3;
//    }

    /*
    //取随机数8->DES3算法->外部认证
    //1号密钥
    cResult=CPU_ExterAuthen(1,2);
    if(cResult!=0)
    {
        return 9;
    }
    */

    //选择DF目录
    cResult = CPU_SelectDFFile(cBurseID);
    if (cResult != 0) {
        return 5;
    }

    //取随机数8->DES3算法->外部认证
    //7+cBurseID号密钥
    cResult = CPU_ExterAuthen(1, 5 + cBurseID * 3);
    if (cResult != 0) {
        return 9;
    }

    //选择二进制文件
    cResult = CPU_SelectBinaryFile(1);
    if (cResult != 0) {
        return 7;
    }

    //读二进制文件
    cResult = CPU_ReadBinaryFile(0, bBurseContext);
    if (cResult != 0) {
        return 8;
    }

    return 0;
}

//外部认证
//cFileID:密钥文件号(0-2),cPassID:密钥号(1-64)
u8 CPU_ExterAuthen(char cFileID, char cPassID) {
    u8 i;
    u8 ucKey[16];
    u8 ucResult;
    u8 bTempContext[64];
    u8 bRespContext[64];
    u8 ucRespLen;

    //取随机数
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x84;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x08;

    memset(bRespContext, 0, 64);
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        //取随机数结果失败");
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        //取随机数内容失败");
        return 2;
    }

    memset(bTempContext, 0, 64);
    memcpy(bTempContext, bRespContext, 8);

    //DES算法
    memset(ucKey, 0, 16);
    if (cPassID == 0) {
        ucKey[0] = 0xFF;
        ucKey[1] = 0xFF;
        ucKey[2] = 0xFF;
        ucKey[3] = 0xFF;
        ucKey[4] = 0xFF;
        ucKey[5] = 0xFF;
        ucKey[6] = 0xFF;
        ucKey[7] = 0xFF;
        ucKey[8] = 0xFF;
        ucKey[9] = 0xFF;
        ucKey[10] = 0xFF;
        ucKey[11] = 0xFF;
        ucKey[12] = 0xFF;
        ucKey[13] = 0xFF;
        ucKey[14] = 0xFF;
        ucKey[15] = 0xFF;
    } else {
        if (TEST_MODE == 0) {
            //得到第几扇区的密钥
            if ((cPassID - 1) % 2 == 0) {
                memcpy(ucKey, s_UCardAuthKeyA[(cPassID - 1) / 2], 6);
            } else {
                memcpy(ucKey, s_UCardAuthKeyB[(cPassID - 1) / 2], 6);
            }
        } else {
            ucKey[0] = 0xFF;
            ucKey[1] = 0xFF;
            ucKey[2] = 0xFF;
            ucKey[3] = 0xFF;
            ucKey[4] = 0xFF;
            ucKey[5] = cPassID;
        }
    }
    DES3(ucKey, bTempContext, bRespContext, 0);//加密
    //外部认证
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x82;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = cFileID;
    bTempContext[i++] = 0x08;
    memcpy(bTempContext + i, bRespContext, 8);
    i = i + 8;

    memset(bRespContext, 0, 64);
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        LOGD("认证-Card_ProAPDU：%d",ucResult);
        return 3;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 4;
    }
    return 0;
}

//选择MF目录
u8 CPU_SelectMFFile(void) {
    u8 i;
    u8 ucResult;
    u8 bTempContext[64];
    u8 bRespContext[64];
    u8 ucRespLen;

    //选择MF目录00A4040009A00000000386980701
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xA4;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x02;
    bTempContext[i++] = 0x3F;
    bTempContext[i++] = 0x00;
    memset(bRespContext, 0, 64);

    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}

//选择DF目录
u8 CPU_SelectDFFile(char cDFID) {
    u8 i;
    u8 ucResult;
    u8 bTempContext[64];
    u8 bRespContext[64];
    u8 ucRespLen;

    //选择DF目录00A4040009A00000000386980701
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xA4;
    bTempContext[i++] = 0x04;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x05;

    bTempContext[i++] = 0xDF;
    bTempContext[i++] = cDFID;
    bTempContext[i++] = 0x02;
    bTempContext[i++] = 0x03;
    bTempContext[i++] = 0x04;
    memset(bRespContext, 0, 64);
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        LOGD("CPU_SelectDFFile-Card_ProAPDU：%d",ucResult);
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}


//选择二进制文件、写和读(信息)
//cFileID=11
u8 CPU_SelectBinaryFile(char cFileID) {
    u8 i;
    u8 ucResult;
    u8 bTempContext[64];
    u8 bRespContext[64];
    u8 ucRespLen;

    //选择二进制文件
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xA4;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0x02;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = cFileID + 0x10;
    memset(bRespContext, 0, 64);
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}

//写二进制文件、写和读(信息)
//cFileID=11
u8 CPU_WriteBinaryFile(u8 cPos, u8 cLen, u8 *bBinaryContext) {
    u8 i;
    u8 ucResult;
    u8 bTempContext[64];
    u8 bRespContext[64];
    u8 ucRespLen;

    //写二进制文件
    i = 0;
    bTempContext[i++] = 0x00;
    bTempContext[i++] = 0xD6;

    bTempContext[i++] = (cPos & 0xFF00) >> 8;
    bTempContext[i++] = (cPos & 0x00FF);
    bTempContext[i++] = cLen;

    memcpy(bTempContext + i, bBinaryContext, cLen);
    i = i + cLen;

    memset(bRespContext, 0, 64);
    ucResult = Card_ProAPDU(bTempContext, i, bRespContext, &ucRespLen);
    if (ucResult != 0) {
        return 1;
    }
    if ((bRespContext[ucRespLen - 2] != 0x90) || (bRespContext[ucRespLen - 1] != 0x00)) {
        return 2;
    }
    return 0;
}


#if 0
//计算NFC卡写自定义数据MAC
long NFC_Key16Mac(char cFileID,char cStartPos,char cInfoLen,BYTE *bSelfInfo)
{
    BYTE bChallenge[4];
    BYTE bInitData[96];
    BYTE bOutData[96];
    BYTE bSelfMACData[96];
    BYTE bCardKey[16];
    BYTE bNFCCardKey[16];
    BYTE kBuf[16];
    long lngResult;
    char i,j,aLen;

    memset(bCardKey,0,16);
    //NFC初始应用维护密钥:1扇区A密钥+卡号(高字节在前)+B密钥
    memcpy(bCardKey,WorkInfo.bUCardAuthKeyA[1],6);
    bCardKey[6]=CardAccInfo.bCardSerialID[3];
    bCardKey[7]=CardAccInfo.bCardSerialID[2];
    bCardKey[8]=CardAccInfo.bCardSerialID[1];
    bCardKey[9]=CardAccInfo.bCardSerialID[0];
    memcpy(bCardKey+10,WorkInfo.bUCardAuthKeyB[1],6);

    ConvertToNFCKey(bCardKey,bNFCCardKey);


    //取随机数
    lngResult=NFC_GetChallenge(bChallenge);
    if(lngResult!=0)
    {
        return 1;
    }
    //初始值(随机数+00 00 00 00)
    i=0;
    memcpy(bInitData+i,bChallenge,4);
    i=i+4;
    bInitData[i++]=0x00;
    bInitData[i++]=0x00;
    bInitData[i++]=0x00;
    bInitData[i++]=0x00;

    //组织数据
    i=0;
    bSelfMACData[i++]=0x04;
    bSelfMACData[i++]=0xD6;
    bSelfMACData[i++]=(0x80+cFileID);
    bSelfMACData[i++]=cStartPos;
    bSelfMACData[i++]=cInfoLen+4;
    memcpy(bSelfMACData+i,bSelfInfo,cInfoLen);
    i=i+cInfoLen;

    bSelfMACData[i++]=0x80;
    while(i%8!=0)
    {
        bSelfMACData[i++]=0x00;
    }

    aLen = i;

    //DES3算法
    for(i=0;i<aLen / 8;i++)
    {
        for(j=0;j<8;j++)
        {
           bInitData[j] ^= bSelfMACData[i*8+j];
        }

        memcpy(kBuf,bNFCCardKey,16);
        DES(kBuf,bInitData,bOutData,0);
        for(j=0;j<8;j++)
        {
           bInitData[j] = bOutData[j];
        }
    }
    memcpy(kBuf,bNFCCardKey,16);

    DES(kBuf+8,bInitData,bOutData,1);
    for(j=0;j<8;j++)
    {
       bInitData[j] = bOutData[j];
    }

    memcpy(kBuf,bNFCCardKey,16);
    DES(kBuf,bInitData,bOutData,0);
    for(j=0;j<4;j++)
    {
       bNFCSelfInfoMAC[j] = bOutData[j];
    }

    return 0;
}

//NFC卡片信息同步
long NFCCardInfoInStep(void)
{
    long lngResult;
    u8 bTempContext[50];
    //自定义应用数据64字节
    //memcpy(bTempContext,CardAccInfo.bNFCSelfInfoDate,50);
    //园区号
    bTempContext[6]=WorkInfo.cCampusID&0x00FF;
    //身份编号
    bTempContext[7]=WorkInfo.cStatusID&0x00FF;
    //个人编号 16字节
    memcpy(bTempContext+12,WorkInfo.cCardPerCode,16);
    //性别	   2字节
    memcpy(bTempContext+28,WorkInfo.cSexState,2);
    //姓名	   16字节
    memcpy(bTempContext+30,WorkInfo.cCardName,16);
    //交易密码
    bTempContext[46]=WorkInfo.lngPaymentPsw&0x00FF;
    bTempContext[47]=(WorkInfo.lngPaymentPsw&0xFF00)>>8;
    bTempContext[48]=(WorkInfo.lngPaymentPsw&0xFF0000)>>16;
    //写自定义数据流程
    lngResult=NFC_WriteData(0x19,0,50,bTempContext);
    if(lngResult!=0)
    {
        return 62;
    }
    else
    {}

    return 1;
}

//当出现E062时，重新写NFC卡
long NFC_ReWriteData(char cFileID,char cStartPos,char cInfoLen,BYTE *bSelfInfo)
{
    int i;
    long lngResult;
    int iBeepCount=0;
    unsigned char disk;
    BYTE bCardTempID[5];
    unsigned long lngStartTime;

    for(i=0;i<6;i++)
    {
        lngResult=ReadCardSerNumberA(bCardTempID);
        if(lngResult==1)
        {
            lngResult=memcmp(bCardTempID,CardAccInfo.bCardSerialID,4);
            if(lngResult==0)
            {
                lngResult=NFC_WriteData(cFileID,cStartPos,cInfoLen,bSelfInfo);
                if(lngResult==0)
                {
                    return 0;
                }
                else
                {}
            }
        }
        else
        {}
    }

    LED_AllClear();
    LED_Show("E062",1);

    lngStartTime=OS_Time;
    while(1)
    {
        lngResult=ReadCardSerNumberA(bCardTempID);
        if(lngResult==1)
        {
            lngResult=memcmp(bCardTempID,CardAccInfo.bCardSerialID,4);
            if(lngResult==0)
            {
                lngResult=NFC_WriteData(cFileID,cStartPos,cInfoLen,bSelfInfo);
                if(lngResult==0)
                {
                    return 0;
                }
            }
        }
        //超时退出
        if(OS_Time-lngStartTime>OPTCARD_TIMEOUT*100)
        {
            return 1;
        }
        //按键退出
        disk=GetChar();
        if(disk==FKeyD || disk==KeyC)
        {
            return 2;
        }
        iBeepCount++;
        if(iBeepCount>5)
        {
            iBeepCount=0;
            BeepOn(1);
        }
        Delay(1);
    }

    return 62;
}
#endif










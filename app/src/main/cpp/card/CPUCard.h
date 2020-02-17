
/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：CPUCard.h 
*
* 文件标识：CPUCard
* 摘    要：CPU卡功能函数集合
*
*
* 当前版本：V1.0
* 作    者：wcp
* 完成日期：2015/8/10
* 编译环境：D:\Program Files (x86)\IAR Systems\Embedded Workbench 6.5\arm
*
* 历史信息：
*******************************************************************************/
#ifndef __CPUCard_H
#define __CPUCard_H

#include "../config.h"

#ifdef __cplusplus
extern "C" {
#endif

/**********************************************函数定义*****************************************************
* 函数名称: u8 ReadCPUCardSID(u8 *cCardSID) 
* 输入参数: u8 *cCardSID 
* 返回参数: u8  
* 功    能: 读CPU卡卡号并选择CPU部分  
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 ReadCPUCardSID(u8 *cCardSID);


/**********************************************函数定义***************************************************** 
* 函数名称: u8  CPU_GetRandom(u8 *data, u8 Length) 
* 输入参数: u8 *data 随机数数据, u8 Length 随机数长度
* 返回参数: 成功返回0,失败返回错误码
* 功    能: CPU卡取随机数  
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 CPU_GetRandom(u8 *data, u8 Length);

//选择应用环境 新人行应用先select湖南建行环境AID
long HN_CPU_SelectCBCAid(void);

//选择应用环境
long HN_CPU_SelectCBCAdf(void);

//选择应用环境
long HN_CPU_SelectHBcbcAdf(void);

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
u8 CPU_SelectFile(u8 p1, u8 p2, u8 *data, u8 Length);


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
u8 CPU_WriteBinary(u8 p1, u8 p2, u8 *data, u8 Length);


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
u8 CPU_ReadBinary(u8 p1, u8 p2, u8 *data, u8 Length);

/**********************************************函数定义***************************************************** 
* 函数名称: u8  ExternalAuthen(u8 p1, 8 p2, u8 *data, u8 Length) 
* 输入参数: u8 p1 0x00, u8 p2 外部认证密钥标识符, 
*                         u8 *data 8字节加密后的数据, u8 Length 8字节长度
* 返回参数: 成功返回0，失败返回错误码   
* 功    能: CPU卡外部认证  
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 CPU_ExternalAuthen(u8 p1, u8 p2, u8 *data, u8 Length);


/**********************************************函数定义***************************************************** 
* 函数名称: u8 CPU_SelectCardMF(void)
* 输入参数: void 
* 返回参数:  成功返回0,失败返回错误码 
* 功    能:   CPU卡选择MF主目录
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 CPU_SelectCardMF(void);

/**********************************************函数定义*****************************************************
* 函数名称: u8 CPU_SelectCardAppFile(u16 wFID)
* 输入参数: u16 wFID 选择的文件标示符
* 返回参数: 成功返回0,失败返回错误码
* 功    能: 选择CPU应用自定义信息文件
* 作    者: by wcp
* 日    期: 2015/12/4
************************************************************************************************************/
u8 CPU_SelectCardAppFile(u16 wFID);

//================================================================================================================//
//基本信息1设置
u8 CPU_BasicInfo1_Set(u8 *bBasicContext, u8 *cCardSID);

//基本信息2设置
u8 CPU_BasicInfo2_Set(u8 *bBasicContext, u8 *cCardSID);

//基本信息设置(全部)
u8 CPU_BasicAllInfo_Set(u8 *bBasicContext, u8 *cCardSID);

//扩展信息1设置
u8 CPU_ExternInfo1_Set(u8 *bExternContext, u8 *cCardSID);

//扩展信息2设置
u8 CPU_ExternInfo2_Set(u8 *bExternContext, u8 *cCardSID);

//扩展信息3设置
u8 CPU_ExternInfo3_Set(u8 *bExternContext, u8 *cCardSID);

//扩展信息设置(全部)
u8 CPU_ExternAllInfo_Set(u8 *bExternContext, u8 *cCardSID);

//钱包信息设置
u8 CPU_BurseInfo_Set(char cBurseID, u8 *bBurseContext, u8 *cCardSID);

//基本和扩展信息读取
u8 CPU_BasicExternInfo_Get(u8 *bBasicContext, u8 *bExternContext, u8 *cCardSID);

//基本信息读取
u8 CPU_BasicInfo_Get(u8 *bBasicContext, u8 *cCardSID);

//扩展信息读取
u8 CPU_ExternInfo_Get(u8 *bExternContext, u8 *cCardSID);

//钱包信息读取
u8 CPU_BurseInfo_Get(char cBurseID, u8 *bBurseContext, u8 *cCardSID);

//外部认证
//cFileID:密钥文件号(0-2),cPassID:密钥号(1-64)
u8 CPU_ExterAuthen(char cFileID, char cPassID);

//选择MF目录
u8 CPU_SelectMFFile(void);

//选择DF目录
u8 CPU_SelectDFFile(char cDFID);

//选择二进制文件、写和读(信息)
//cFileID=11
u8 CPU_SelectBinaryFile(char cFileID);

//写二进制文件、写和读(信息)
//cFileID=11
u8 CPU_WriteBinaryFile(u8 cPos, u8 cLen, u8 *bBinaryContext);

//读二进制文件、写和读(信息)
//cFileID=91
u8 CPU_ReadBinaryFile(u8 cPos, u8 *bBinaryContext);


#ifdef __cplusplus
}
#endif

#endif











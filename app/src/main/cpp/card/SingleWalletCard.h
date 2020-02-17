/*******************************************************************************
* Copyright 2017, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：SingleWalletCard.h
*
* 文件标识： SingleWalletCard
* 摘    要：单钱包卡种功能函数头文件
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
#ifndef __SingleWalletCard_H
#define __SingleWalletCard_H

#include "../config.h"

#ifdef __cplusplus
extern "C" {
#endif


//-------------------------------------湖南大众建行CPU卡----------------------------------------------//

/**********************************************函数定义***********************************************
* 函数名称:
* 输入参数:
* 返回参数: long 0：成功，其他失败
* 功    能:
* 作    者: by linz
* 日    期: 2017/03/18
******************************************************************************************************/
//计算恒宝握奇通用版simpass卡写自定义数据MAC
long MSIM_Key16Mac(char cStartPos, char cInfoLen, u8 *bSelfInfo, u8 *bOutData);;

//选择翼机通环境
long SelectTelPSE(u8 *bAppInfo, int iMode);

//选择所有校园翼机通环境 2015.0826
long SelectTelAllPSE(void);


//选择所有一卡通应用 2015.0826
long SIMP_SelectAllApplication(void);

//选择一卡通应用 使用AID模式 2015.0826
long SIMP_SelectApplication(int iMode);

//获取公共信息
long SIMP_ReadPublicInfo(void);

//选择自定义目录
long SIMP_SelectSelfDF(void);

//获取自定义应用数据
long SIMP_ReadSelfInfo(char cStartPos, char cInfoLen, u8 *bSelfInfo);


/*************************************************************************/
//选择SimPass卡一卡通应用和自定义目录
/*************************************************************************/
long SIMP_SelectAPPANDSelfDF(void);


//写自定义应用数据
long SIMP_WriteSelfData(char cStartPos, char cInfoLen, u8 *bSelfInfo, u8 *bInData);


//SIMPASS卡写数据
long SIMP_WriteData(char cStartPos, char cInfoLen, u8 *bSelfInfo);

//读simpass/uimpass卡
u8 ReadSIMPCardProcess(u8 *bCardContext);


//-------------------------------------湖南大众建行CPU卡----------------------------------------------//

/**********************************************函数定义***********************************************
* 函数名称: long HN_CPU_SelectFile(void);
* 输入参数:
* 返回参数: long 0：成功，其他失败
* 功    能: 选择CPU卡应用文件
* 作    者: by linz
* 日    期: 2017/03/18
******************************************************************************************************/
//选择应用环境 新人行应用先select湖南建行环境AID
long HN_CPU_SelectCBCAid(void);

//选择应用环境捷德新COS
long HN_CPU_SelectCBCAdf(void);

//选择应用环境恒宝COS
long HN_CPU_SelectHBcbcAdf(void);


//选择应用文件
long HN_CPU_SelectFile(void);


//读应用文件(信息);
long HN_CPU_ReadFile(unsigned char cPos, unsigned char cLen, unsigned char *bBinaryContext);


//写二进制文件、写和读(信息);
//cFileID=15
long HN_CPU_WriteFile(u8 cPos, u8 cLen, u8 *bBinaryContext, u8 *bInData);


//计算湖南建行卡卡写自定义数据MAC
long MHN_Key16Mac(u8 *cCardSID, char cStartPos, char cInfoLen, u8 *bSelfInfo, u8 *bOutData);


long HN_CPU_WriteData(char cStartPos, char cInfoLen, u8 *bSelfInfo, u8 cHNcbcState);

//读湖南大众建行卡
u8 ReadHnCPUCardProcess(u8 *bCardContext, u8 cHNcbcState);


//===========================================================================================//
//-------------------------------------NFC卡相关----------------------------------------------//
//选择支付系统环境
long NFC_SelectPSE(void);


//选择一卡通钱包应用
long NFC_SelectBurseApp(void);


//读二进制文件、写和读(信息);
//cFileID=19
long NFC_ReadBinaryFile(char cFileID, unsigned char cPos, unsigned char cLen,
                        unsigned char *bBinaryContext);

//写二进制文件、写和读(信息);
//cFileID=19
long NFC_WriteBinaryFile(unsigned char cPos, unsigned char cLen, unsigned char *bBinaryContext,
                         unsigned char *bInitMac);

//选择NFC卡一卡通应用和自定义目录
long NFC_SelectAPPANDSelfDF(u8 *bCardTempID);


//NFC卡写数据
long NFC_WriteData(char cStartPos, char cInfoLen, u8 *bSelfInfo);


//读NFC卡
u8 ReadNFCCardProcess(u8 *bCardContext);


#ifdef __cplusplus
}
#endif

#endif

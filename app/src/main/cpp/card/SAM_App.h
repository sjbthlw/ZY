
/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：SAM_App.h
*
* 文件标识：SAM_App.h
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

#ifndef __SAM_APP_H
#define __SAM_APP_H

#include "../config.h"

#ifdef __cplusplus
extern "C" {
#endif


//获取相应数据(获取响应)
u8 PSAMSP_GetResponse(char cResponseLen, u8 *bResponseData);

//获取相应数据(获取响应M1)
u8 PSAMSP_M1GetResponse(char cResponseLen, u8 *bResponseData);

//PSAM卡初始化
u8 PSAMSP_Init(void);

//指定卡槽PSAM卡初始化
u8 PSAMSPSlot_Init(void);

//修改PSAM卡的PPS(波特率)
u8 PSAMSP_SetPsamPPS(u8 cBaudID);

//取随机数
u8 PSAMSP_GetChallenge(u8 *bChallenge, u8 cLen);

//选择目录
u8 PSAMSP_SelectPayDF(u8 cPSAMcardtype);

//PSAM选择文件
u8 PSAMSP_SelectFile(u8 p1, u8 p2, u8 *data, u8 Length);

//PSAM通用DES计算初始化 用加密密钥初始化 成功返回0,失败返回错误码    
u8 PSAMSP_InitDESCrypt(u8 p1, u8 p2, u8 *data, u8 Length);

// PSAM卡通用DES计算
u8 PSAMSP_DESCrypt(u8 *PSAM_RAND, u8 *PSAM_AUTH, u8 p1, u8 p2, u8 cPostLength, u8 *cRespLength);

// PSAM卡通用DES计算(M1)
u8 PSAMSP_M1DESCrypt(u8 *PSAM_RAND, u8 *PSAM_AUTH, u8 p1, u8 p2, u8 cPostLength, u8 *cRespLength);

//算出固定PSAM卡的MAC值
u8 PSAMSP_GETMAC(u8 *cMac);

//PSAM计算M1密钥
u8 PSAMSP_GETM1Key(u8 *cOutcKey);

#ifdef __cplusplus
}
#endif

#endif


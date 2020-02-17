/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：MifCard.h
*
* 文件标识：MifCard.h
* 摘    要：Mifare卡函数集合
*
*
* 当前版本：V1.0
* 作    者：wcp
* 完成日期：2015/12/14
* 编译环境：D:\Program Files (x86)\IAR Systems\Embedded Workbench 6.5\arm
*
* 历史信息：
*******************************************************************************/
#ifndef __MIFCARD_H
#define __MIFCARD_H

#include "../config.h"

#ifdef __cplusplus
extern "C" {
#endif

//读卡片属性
u8 ReadCardAttrib(u8 *cCardSID, u8 *SAK);

//读卡号A
u8 ReadCardSerID(u8 *cCardSID);

//读卡片数据
u8 ReadMifareBlock(u8 cBlockID, u8 *cCardSID, u8 *cCardContext);

//写卡片数据
u8 WriteMifareBlock(u8 cBlockID, u8 *cCardSID, u8 *cCardContext);

//写卡片数据(无认证)
u8 WriteMifareBlockNOAuthen(u8 cBlockID, u8 *cCardContext);

//读Mifare卡片扇区数据
u8 ReadMifareSector(u8 cSectorID, u8 *cCardSID, u8 cCardContext[][16]);

//读Mifare卡片钱包扇区数据
u8 ReadMifareBurseSector(u8 *cCardSID, u8 cBlockID, u8 *cCardContext, u8 cBlockBakID,
                         u8 *cCardBakContext);

//写Mifare卡片扇区数据
u8 WriteMifareSector(u8 cSectorID, u8 *cCardSID, u8 cCardContext[][16]);

//写Mifare卡片钱包扇区数据
u8 WriteMifareBurseSector(u8 *cCardSID, u8 cBlockID, u8 *cCardContext, u8 cBlockBakID,
                          u8 *cCardBakContext);

//读Mifare卡片扇区数据(固定密钥a0-a5)
u8 ReadMifareOpenSector(u8 cSectorID, u8 *cCardSID, u8 cCardContext[][16]);

//读卡片数据
u8 ReadPSCard(u8 *bCardKey, u8 *bCardSerID, int iBlockID, u8 *bCardContext);

//当出现E061时，重新读卡
u8 ReReadPSCard(u8 *bCardKey, u8 *bCardSerID, int iBlockID, u8 *bCardContext);


#ifdef __cplusplus
}
#endif


#endif



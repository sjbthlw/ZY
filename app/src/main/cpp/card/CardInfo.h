/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：CardInfo.h
*
* 文件标识：CardInfo.h
* 摘    要：卡片数据信息
*
*
* 当前版本：V1.0
* 作    者：wcp
* 完成日期：2015/12/5
* 编译环境：D:\Program Files (x86)\IAR Systems\Embedded Workbench 6.5\arm
*
* 历史信息：
*******************************************************************************/

#ifndef __CARDINFO_H
#define __CARDINFO_H

#include "../config.h"

#ifdef __cplusplus
extern "C" {
#endif



//CPU卡文件标示符
#define ETONG_FID    0x7F03        //智慧易通FID
#define PUBILC_FID    0x0015        //公共应用信息FID
#define CHOLD_FID    0x0016        //持卡人应用信息FID

#define BURSE_FID    0x0000        //钱包应用信息FID
#define BURSE1_FID    0x0001        //钱包1应用信息FID
#define BURSE2_FID    0x0002        //钱包2应用信息FID
#define BURSE3_FID    0x0003        //钱包3应用信息FID
#define BURSE4_FID    0x0004        //钱包4应用信息FID
#define BURSE5_FID    0x0005        //钱包5应用信息FID
#define BURSE6_FID    0x0006        //钱包6应用信息FID


extern u8 s_UCardAuthKeyA[32][6]; //用户交易卡密钥A
extern u8 s_UCardAuthKeyB[32][6]; //用户交易卡密钥B

//设置卡片密钥
void SetCardKey(u8 bUCardAuthKeyA[32][6], u8 bUCardAuthKeyB[32][6]);

//CPU卡读写属性
typedef struct {
    u16 wFID;        //文件标示符
    u8 cStartPos;    //起始地址
    u8 cInfoLen;        //长度

    u8 cCardSID[16];        //卡号

} ST_CPUCardAttr;

//卡片属性
typedef struct {
    u8 cCardType;        //卡片类型
    u8 cBasicSectorID;            //基本扇区号
    u8 cExtendSectorID;        //扩展扇区号

    u8 cWorkBurseID;        //工作钱包号
    u8 cChaseBurseID;       //追扣钱包号

    u8 cCardSID[16];        //卡号
    u8 cSAK;                //返回值 s70ka s50卡用

} ST_CardAttr;

//卡片基本信息(基础和扩展数据,钱包数据)
typedef struct {
    //基本扇区
    u8 cAgentID;                //代理号
    u16 iGuestID;               //客户号
    u8 cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
    u8 cCardAuthenCode[4];      //卡认证码	4	通过卡号+代理号+客户序号+客户标识码
    u8 cCardState;         //卡片状态	1	取消原有卡户类型（临时卡/正式卡）数据，本字节全部用于存储卡片状态。
    //考虑到兼容原有卡结构，读写该字节时仍需取后4bit解析，原有读写代码不变。
    //0:无效卡/黑名单;1:有效卡
    u32 lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
    u8 cCampusID;         //园区号	1	范围为1~250
    u8 cStatusID;          //身份编号	1	最大64种，1~64
    u16 iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
    u8 bBasic1Context[16];            //基本扇区第1块内容

    u32 lngAccountID;       //帐号	4	1～4294967296
    u8 cReservel[4];        //保留位	4	0
    u32 lngPaymentPsw;       //交易密码	3	六位数字密码
    u8 cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
    u8 cReservel1[4];        //保留位	4	补0
    u8 bBasic2Context[16];            //基本扇区第2块内容

    //扩展扇区
    char cAccName[16];          //卡户姓名
    char cSexState[2];          //性别
    u8 cCreateCardDate[3];      //开户日期
    u16 iDepartID;              //部门编号
    char cOtherLinkID[10];      //第三方对接关键字
    char cCardPerCode[16];      //个人编号

    u8 cBurseID;                //当前钱包号
    u8 cInStepState;            //是否有圈存

    //工作钱包信息
    s32 lngWorkBurseMoney;              //工作钱包余额
    u16 iWorkSubsidySID;                //补助流水号
    u16 iWorkBurseSID;                    //工作钱包流水号
    u16 iWorkLastPayDate;               //工作钱包末笔交易日期
    u32 lngWorkDayPaymentTotal;         //当日消费累计额
    u16 iWorkLastBusinessID;           //末笔交易营业号
    u16 iWorkDayPaymentCount;            //当日消费累计次
    u8 cWorkBurseAuthen[3];            //钱包认证码
    u8 bWorkBurseContext[16];           //当前钱包块的内容

    //追扣钱包信息
    s32 lngChaseBurseMoney;             //主钱包余额
    u16 iChaseSubsidySID;               //补助流水号
    u16 iChaseBurseSID;                 //主钱包流水号
    u16 iChaseLastPayDate;              //主钱包末笔交易日期
    u32 lngChaseDayPaymentTotal;        //当日消费累计额
    u16 iChaseLastBusinessID;           //末笔交易营业号
    u16 iChaseDayPaymentCount;            //当日消费累计次
    u8 cChaseBurseAuthen[3];           //钱包认证码
    u8 bChaseBurseContext[16];          //当前钱包块的内容

    s32 lngPayMoney;            //交易金额
    s32 lngWorkPayMoney;        //工作钱包交易金额
    s32 lngChasePayMoney;       //追扣钱包交易金额
    s32 lngManageMoney;         //管理费金额
    s32 lngPriMoney;            //优惠金额
    s32 lngSubMoney;            //补助金额
    u16 wSubsidySum;            //补助笔数(补助版本号之差)

    u32 lngInPayMoney;            //输入金额

} ST_CARDBASICPARAINFO;

//基础应用信息区
typedef struct {
    u8 cAgentID;                //代理号
    u16 iGuestID;               //客户号
    u8 cAuthenVer;             //认证版本号	1	范围为0~250，缺省为0
    u8 cCardAuthenCode[4];      //卡认证码	4	通过卡号+代理号+客户序号+客户标识码
    u8 cCardState;         //卡片状态	1	取消原有卡户类型（临时卡/正式卡）数据，本字节全部用于存储卡片状态。
    //考虑到兼容原有卡结构，读写该字节时仍需取后4bit解析，原有读写代码不变。
    //0:无效卡/黑名单;1:有效卡
    u32 lngCardID;          //卡内编号	3	用户卡管理，黑白名单，范围为1~100000
    u8 cCampusID;         //园区号	1	范围为1~250
    u8 cStatusID;          //身份编号	1	最大64种，1~64
    u16 iValidTime;         //有效时限	2	范围00~63年(7位)月(4位)日(5位)
    u8 bBasic1Context[16];            //基本扇区第1块内容

    u32 lngAccountID;       //帐号	4	1～4294967296
    u8 cReservel[4];        //保留位	4	0
    u32 lngPaymentPsw;       //交易密码	3	六位数字密码
    u8 cCardStructVer;      //卡结构版本号	1	ZYTK3.2使用1，ZYTK3.0原有保留位值为0
    u8 cReservel1[4];        //保留位	4	补0
    u8 bBasic2Context[16];            //基本扇区第2块内容

} ST_CARDBASEINFO;

//扩展应用信息区
typedef struct {
    //扩展扇区
    char cAccName[16];         //卡户姓名
    char cSexState[2];          //性别
    u8 cCreateCardDate[3];    //开户日期 年月日
    u16 iDepartID;            //部门编号
    char cOtherLinkID[10];      //第三方对接关键字
    char cCardPerCode[16];      //个人编号

} ST_CARDEXTENTINFO;

//钱包交易信息区
typedef struct {
    //钱包信息
    s32 lngBurseMoney;             //钱包余额   3
    u16 iSubsidySID;               //补助流水号  2
    u16 iBurseSID;                    //钱包流水号 2
    u16 iLastPayDate;              //钱包末笔交易日期   2
    u32 lngDayPaymentTotal;         //当日消费累计额   2
    u8 iLastBusinessID;            //最高位代表是否优惠(0：未优惠，1：已优惠)，低7位代表末笔营业号(1~127)
    u8 iDayPaymentCount;            //当日消费累计次   1
    u8 cBurseAuthen[3];            //钱包认证码

    u8 bBurseContext[16];           //当前钱包块的内容

    u8 cIsBlockID;                  //判断是正本还是副本 0:正 1:副(如果是副本则需要记录一条断点恢复流水)

} ST_CARDBURSEINFO;


#ifdef __cplusplus
}
#endif

#endif


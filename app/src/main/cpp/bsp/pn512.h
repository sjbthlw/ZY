//--------------文件信息---------------------------------------------------------------------------
// 文 件 名:    PN512.c
// 文件描述:    PN512底层函数
// 版    本:    V1.00
// 创 建 人:	曾本森
// 创建日期:	2010.01.26
// 说    明:	注意超时变量g_ucPCDTimeOutCnt定义和有关源代码。
//=================================================================================================
//-----------------修改记录------------------------------------------------------------------------
// 修改内容:  	
// 当前版本:
// 修 改 人:
// 修改日期:
// 注    意: 
//-------------------------------------------------------------------------------------------------
///////////////////////////////////////////////////////////////////////////////////////////////////
#ifndef __PN512_H
#define __PN512_H
//=================================================================================================

#ifdef __cplusplus
    extern "C" {
#endif

#include "../config.h"

//Mifare_One卡片命令字
#define PICC_REQIDL           0x26               //寻天线区内未进入休眠状态
#define PICC_REQALL           0x52               //寻天线区内全部卡
#define PICC_ANTICOLL1        0x93               //防冲撞
#define PICC_ANTICOLL2        0x95               //防冲撞
#define PICC_AUTHENT1A        0x60               //验证A密钥
#define PICC_AUTHENT1B        0x61               //验证B密钥
#define PICC_READ             0x30               //读块
#define PICC_WRITE            0xA0               //写块
#define PICC_DECREMENT        0xC0               //扣款
#define PICC_INCREMENT        0xC1               //充值
#define PICC_RESTORE          0xC2               //调块数据到缓冲区
#define PICC_TRANSFER         0xB0               //保存缓冲区中数据
#define PICC_HALT             0x50               //休眠

#define debug 0
//============ 函数剪裁 =====================================================================================
// 请选择要使用的函数，1：使用；0：不使用
#define	PICC_REQUEST_EN					1		// 请求命令
#define	PICC_ANTICOLL_EN				1		// 防碰撞命令
#define	PICC_SELECT_EN					1		// 选择卡命令
#define	PICC_HALTA_EN					1		// 挂起命令
#define	PICC_ACTIVATE_EN				0		// 激活
#define PICC_EX_CHANGE_BLOCK2_EN		0		// 数据交换块
#define PICC_EX_CHANGE_BLOCK_EN			0		// 数据交换块(带时间返回值)
//============ 常量定义 =====================================================================================
// ISO14443_3协议命令码
#define ISO14443_3A_REQALL           	0x52	// 请求所有的卡
#define ISO14443_3A_REQIDL           	0x26	// 请求空闲的卡
#define SELECT_CASCADE_LEVEL_1      	0x93	// 一级防碰撞/选择
#define SELECT_CASCADE_LEVEL_2      	0x95	// 二级防碰撞/选择
#define SELECT_CASCADE_LEVEL_3      	0x97	// 三级防碰撞/选择
#define HALTA_CMD                   	0x50	// 挂起

#define ISO14443_3A_EXPECT_TIMEOUT      0x01	// < Tells the library to expect a timeout.
#define ISO14443_3A_EXPECT_ACK          0x02   	// < Let the library expect an Acknowledge response.
#define ISO14443_3A_EXPECT_DATA         0x04   	// < The library shall expect data.
#define ISO14443_3A_ACK_MASK            0x0A	// ACK屏蔽码
// 请求命令参数定义
#define REQUEST_BITS                	0x07	// 请求命令的位数
#define ATQA_LENGTH                 	0x02	// ATQ字节数
// 防碰撞/选择命令参数定义
#define BITS_PER_BYTE               	0x08
#define UPPER_NIBBLE_SHIFT          	0x04
#define COMPLETE_UID_BITS           	0x28
#define NVB_MIN_PARAMETER           	0x20
#define NVB_MAX_PARAMETER           	0x70
#define MAX_CASCADE_LEVELS          	0x03	// 最大防碰撞/选择等级
#define SINGLE_UID_LENGTH           	0x20	//
#define CASCADE_BIT                 	0x04
#define SAK_LENGTH                  	0x01	// 选择应答的字节数
// 挂起命令参数定义
#define HALTA_PARAM                 	0x00	// 挂起命令参数
#define HALTA_CMD_LENGTH            	0x02	// 挂起命令的长度
// 数据交互命令参数定义
#define TX_CRC_EN						0x80	// 发送使能CRC
#define RX_CRC_EN						0x40	// 接收使能CRC
#define TX_RX_CRC_EN					0xC0	// 发送接收使能CRC
#define TX_RX_CRC_DIS					0x00	// 发送接收禁止CRC
//============= 全局变量和函数定义 ==========================================================================
typedef struct
{
    u8 ATQ[8];
    u8 UIDLen;
    u8 UID[12];
    u8 SAK;
} ACTIVEPARAA;

//=================================================================================================
#define	PN512							12
#define	HARDWARE_MODE					0		// 硬件模式，是否使用NRSTPD脚，1为使用
#define PCD_IRQ_EN						0		// 中断模式使能
#define PCD_MODE						PN512	// RC522	(默认值)
//=================================================================================================


//============ 函数剪裁 ===========================================================================
// 请选择要使用的函数，1：使用；0：不使用
#define PCD_CLOSE_EN				0
#define PCD_READ_REG_EN				0
#define PCD_WRITE_REG_EN			0
#define PCD_ISO_TYPE_EN				1			// PCD模式设置
#define	PCD_EX_CHANGE_BLOCK_EN		1			// 数据交互
#define PCD_CARD_REST_EN			1			// 卡片复位
#define PCD_SELF_TEST_EN			0			// 芯片自测试
//============ 常量定义 ===========================================================================
// PCD超时定时器参数
#define PCD_DELAY30MS				14
#define PCD_DELAY200MS				90			//timer with 2.222ms resolution
#define PCD_DELAY300MS				135 
#define PCD_DELAY400MS				180 
#define PCD_DELAY500MS				225 
#define PCD_DELAY600MS				270 
#define PCD_DELAY700MS				315 
#define PCD_DELAY800MS				360 
#define PCD_DELAY900MS				405 
//#define PCD_DELAY1000MS				450
//#define PCD_DELAY1000MS				550
#define PCD_DELAY1000MS				3000

// 超时定时器参数(302uS)
#define	FREQ_SPLI_302us					0x7FF	// 302us基数分频值	2^12 / 6.78
#define	FREQ_SPLI_4_7us					0x01F	// 4.7us基数分频值	2^5  / 6.78

#define	RIC_DELAY1MS					3		// 超时1ms 3
#define	RIC_DELAY2MS					7		// 超时2ms
#define	RIC_DELAY3MS					10		// 超时3ms
#define	RIC_DELAY4MS					14		// 超时4ms
#define	RIC_DELAY5MS					17		// 延时4.832ms
#define	RIC_DELAY10MS					33		// 延时9.966ms
#define	RIC_DELAY20MS					66		// 延时19.932ms
#define	RIC_DELAY50MS					166		// 延时50.132ms
#define	RIC_DELAY100MS					331		// 延时99.962ms
#define	RIC_DELAY200MS					662		// 延时199.924ms
#define	RIC_DELAY300MS					993		// 延时299.924ms
#define	RIC_DELAY400MS					1324	// 延时399.924ms
#define	RIC_DELAY500MS					1655	// 延时499.924ms
//#define	RIC_DELAY500MS					1000	// 延时499.924ms
// PCD配置模式
#define ISO14443_TYPEA					0
#define ISO14443_TYPEB					1
#define ISO18092_NFCIP					2		// RC523不支持
//============ 全局变量定义 =======================================================================
typedef struct
{
    u8  Cmd;                 				// 命令代码

#if PCD_IRQ_EN	

    u16 nBytesSent;          				// 已发送的字节数
    u16 nBytesToSend;        				// 将要发送的字节数
    u16 nBytesReceived;      				// 已接收的字节数
    u8  nBitsReceived;       				// 已接收的位数
	
    u8  CommIrqEn;
    u8  DivIrqEn;
    u8  WaitForComm;
    u8  WaitForDiv;
    u8  AllCommIrq;
    u8  AllDivIrq;
    u8  Irq;
    u8  Status;
    u8  DoRcv;
	
    u16 nBytes;								//
    u8  *pExBuf;								// 交互数据缓冲区
#else
    u8 nBytesSent;          					// 已发送的字节数
    u8 nBytesToSend;        					// 将要发送的字节数
    u8 nBytesReceived;      					// 已接收的字节数
    u8 nBitsReceived;       					// 已接收的位数
#endif				// PCD_IRQ_EN	
    u8  collPos;             				// 碰撞位置
} MfCmdInfo;



extern u16 g_ucPCDTmOut;		// 超时计数器
extern u8 g_ucTxConMask;
extern MfCmdInfo  MInfo;			// 定义命令信息

//函数功能:   	将A卡置为HALT状态
extern	u8 PiccHaltA(void);

// 函数功能:   	A型卡激活
extern u8 PiccActivate( u8 ucMode, u8 ucReqCode, ACTIVEPARAA *pActiParaA);

// 函数功能:    A型卡激活命令
extern	u8 PiccRequest(u8 ucReqCode, u8 *pATQ);

// 函数功能:    位方式防碰撞
extern u8 PiccAnticoll(u8 unMode, u8 ucSelCode, u8 ucBitCnt, u8 *pUID);

// 函数功能:    选择卡
extern	u8 PiccSelect(u8 ucSelCode, u8 *pUID, u8 *pSAK);


//打开读卡器
extern int PcdOpen(void);

// 函数功能:	关闭PCD
extern	void PcdClose(void);

// 函数原型:	void PcdISOType(u8 ucType)
extern void PcdISOType(u8 ucType);

// 函数功能:	配置芯片
extern	u8 PcdConfig(u8 ucType);

// 函数功能:	复位命令信息
extern	void ResetInfo(void);

// 函数功能:	设置定时器超时。
extern	void SetTimeOut(u32 _302us);


// 函数功能:	Pcd将数据发送到卡，然后等待接收从卡返回的数据。
extern	u8  PcdCmd(u8 ucCmd,u16 ucCmdLen,u8 *pInBuf,u8 *pOutBuf);

// 使用查询模式
extern 	u8  PcdCmdS(u8 ucCmd,u16 ucCmdLen,u8 *pInBuf,u8 *pOutBuf);


// 函数功能:	使RF场产生1个暂停，让卡复位
extern	void PcdReset(u8 ucPause_1ms, u8 ucWait_1ms);

extern int Pn512_open(void);

extern u8 PcdAuthState(u8 auth_mode,u8 addr,u8 *pKey,u8 *pSnr);
extern u8 PcdWrite(u8 addr,u8 *pData );
extern u8 PcdWriteA(u8 addr,u8 *_data);
extern u8 PcdRead(u8 addr,u8 *pData);


extern u8  M522PcdCmd(unsigned char cmd,
                 unsigned char *ExchangeBuf,
                 MfCmdInfo  *info);

// 函数功能:	使RF场产生1个暂停，让卡复位
extern void Card_RfReset(u8 ucPause_1ms,u8 ucWait_1ms);
extern u8 Card_Halt(void);
extern u8 Card_Request(u8 ucReqCode, u8 *pATQ);
extern u8 Card_AntiColl(u8 unMode, u8 ucSelCode, u8 ucBitCnt, u8 *pUID);
extern u8 Card_Select(u8 ucSelCode, u8 *pUID, u8 *pSAK);
extern u8 Card_LoadKey(u8 *pKey);
extern u8 Card_Authen(u8 *pSnr,u8 addr,u8 auth_mode);
extern u8 Card_Write( u8 addr, u8 *_data);
extern u8 Card_Read(u8 addr,u8 *pData);
extern u8 Card_SelectProCard(u8 *resp,u8 *rlen);
extern u8 Card_ProAPDU(u8 *comm,u8 len,u8 *resp,u8 *rlen);

//=================================================================================================
#ifdef __cplusplus
    }
#endif

#endif			// __PN512_H



#include <stdint.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/types.h>
#include <linux/spi/spidev.h>
#include <string.h>
#include <time.h>
#include <android/log.h>
#include "../bsp/public.h"
#include "statuscode.h"
#include "pn512reg.h"
#include "pn512.h"
#include "../config.h"



//============= 常量定义 ==========================================================================
#define PCD_FIFO_MAX_SIZE			64			// PCD FIFO的最大为64 - 2字节
#define PCD_FIFO_WATER_LEVEL		16			// PCD FIFO警戒线

#define MAXRLEN 64

static int this_fd;
static const char *device = "/dev/spidev32766.0";//需要chmod 777 spidev32766.0
//static const char *GPIO5_13 = "/sys/class/gpio/gpio1149/value";//RK3399
static const char *GPIO5_13 = "/sys/class/gpio/gpio141/value";//IMX8 需要chmod 777 value
int PN512_CS;
u8 mode = 0;
u8 bits = 8;
//u32 speed = 500000; // 500k
u32 speed = 1000000; // 1 MHz
u32 delay;

char cCIDIndex=0;
u16 g_ucPCDTmOut; 	   // 超时计数器
u8 g_ucTxConMask;
MfCmdInfo	MInfo;		   // 定义命令信息
volatile MfCmdInfo *MpIsrInfo = 0;

u8 s_pKey[6];
u16 iTimeOutCount=0;
u16 iTimeOutCountA=0;

#define ARRAY_SIZE(a) (sizeof(a) / sizeof((a)[0]))
#define TIMEOUT 1000


//毫秒级 延时
static void Sleepms(int ms)
{
    struct timeval delay;
    delay.tv_sec = 0;
    delay.tv_usec = ms * 1000;
    select(0, NULL, NULL, NULL, &delay);
}

//微秒级 延时
static void Sleepus(int us)
{
	struct timeval delay;
	delay.tv_sec = 0;
	delay.tv_usec = us ; //  us
	select(0, NULL, NULL, NULL, &delay);
}

static void pabort(const char *s)
{
	//LOGD(s);
	abort();
}

/////////////////////////////////////////////////////////////////////
//功    能：读RC632寄存器
//参数说明：Address[IN]:寄存器地址
//返    回：读出的值
/////////////////////////////////////////////////////////////////////
static unsigned char ReadRawRC(int filp,unsigned char register_address)
{
    int ret;
	unsigned char tx_buf[2];
	unsigned char rx_buf[2];
	unsigned short tx_buf16[1];
	unsigned short rx_buf16[1] = {0};

	register_address = ((register_address<<1)&0x7E)|0x80;

	tx_buf[0] = register_address;
	tx_buf[1] = 0x00;
	rx_buf[0] = 0x00;
	rx_buf[1] = 0x00;

	struct spi_ioc_transfer tr = {  //声明并初始化spi_ioc_transfer结构体 
	.tx_buf = (unsigned long)tx_buf, 
	.rx_buf = (unsigned long)rx_buf,
	.len = 2,
	.delay_usecs = delay, 
	.speed_hz = speed, 
	.bits_per_word = bits, 
	}; 

	write(PN512_CS, "0", 1);

	ret = ioctl(filp, SPI_IOC_MESSAGE(1), &tr);   //ioctl默认操作,传输数据 
	if (ret < 1) 
	{
		LOGD("can't send spi message");
		return -1;
	}
	write(PN512_CS, "1", 1);
	return rx_buf[1];
}

/////////////////////////////////////////////////////////////////////
//功    能：写RC632寄存器
//参数说明：Address[IN]:寄存器地址
//          value[IN]:写入的值
/////////////////////////////////////////////////////////////////////

static int WriteRawRC(int filp,unsigned char register_address,unsigned char data)
{
	int ret;
	unsigned char address;

	address = ((register_address<<1)&0x7E);
	char tx[2];

	tx[0]= address;
	tx[1] = data;

	struct spi_ioc_transfer tr = {  //声明并初始化spi_ioc_transfer结构体 
		.tx_buf = (unsigned long)tx, 
		.len = 2, 
		.delay_usecs = delay, 
		.speed_hz = speed, 
		.bits_per_word = bits, 
	}; 

	write(PN512_CS, "0", 1);

	//SPI_IOC_MESSAGE(1)的1表示spi_ioc_transfer的数量 
	ret = ioctl(filp, SPI_IOC_MESSAGE(1), &tr);   //ioctl默认操作,传输数据 
	if (ret < 1)
	{
		LOGD("can't send spi message"); 
	}
	write(PN512_CS, "1", 1);
	return ret;
}

/////////////////////////////////////////////////////////////////////
//功    能：置RC522寄存器位
//参数说明：reg[IN]:寄存器地址
//          mask[IN]:置位值
/////////////////////////////////////////////////////////////////////
void SetBitMask(int filp,unsigned char reg,unsigned char mask)  
{
	char tmp = 0x0;
	//LOGD("##########SetBitMask###########\n");
	tmp = ReadRawRC(filp,reg);
	WriteRawRC(filp,reg,tmp | mask);  // set bit mask	
}
/////////////////////////////////////////////////////////////////////
//功    能：清RC522寄存器位
//参数说明：reg[IN]:寄存器地址
//          mask[IN]:清位值
/////////////////////////////////////////////////////////////////////
void ClearBitMask(int filp,unsigned char reg,unsigned char mask)  
{
	char tmp = 0x0;
	//LOGD("##########ClearBitMask###########\n");
	tmp = ReadRawRC(filp,reg);
	//Sleepus(10000);
	WriteRawRC(filp,reg, tmp & ~mask);  // clear bit mask
}

/**************************************************
//功    能：复位RC522
//返    回: 成功返回MI_OK
 **************************************************/
static void PN512_Reset(void)
{
	system("echo 1 > /sys/class/gpio/gpio125/value");
	Sleepus(20000);
	system("echo 0 > /sys/class/gpio/gpio125/value");
	Sleepus(20000);
	system("echo 1 > /sys/class/gpio/gpio125/value");
	Sleepus(20000);
	// nanosleep(1);
}

/**************************************************
// 函数原型:    u8 RcGetReg(u8 ucucRegAddr)
// 函数功能:    从读写芯片指定的寄存器读出数据
// 入口参数:    u8 ucucRegAddr				// 读写芯片寄存器地址
// 出口参数:    -
// 返 回 值:    读出的值
// 描　  述:    
 **************************************************/
u8 RcGetReg(u8 ucRegAddr)
{
	int flip = this_fd;
	return ReadRawRC(flip,ucRegAddr);
}

/**************************************************
// 函数原型:    void RcSetReg(u8 ucRegAddr, u8 ucRegVal)
// 函数功能:    向读写芯片指定的寄存器写入数据
// 入口参数:    u8 ucucRegAddr				// 读写芯片寄存器地址
//              u8 ucRegVal					// 写入的值
// 出口参数:    -
// 返 回 值:    -
// 描　  述:    
 **************************************************/
void RcSetReg(u8 ucRegAddr, u8 ucRegVal)
{
	int flip;
	flip = this_fd;
	WriteRawRC(flip,ucRegAddr,ucRegVal);    
}

/**************************************************
// 函数原型:    void RcModifyReg(u8 ucRegAddr, u8 ucModifyVal,
//                               u8 ucMaskByte)
// 函数功能:    修改读写芯片指定地址的位
// 入口参数:    u8 ucRegAddr					// 读写芯片寄存器地址
//              u8 ucModifyVal				// 修改模式(置位/清位)
//              u8 ucMaskByte				// 修改的位(置1有效)
// 出口参数:    -
// 返 回 值:    -
// 描　  述:    
 **************************************************/
void RcModifyReg(u8 ucRegAddr, u8 ucModifyVal, u8 ucMaskByte)
{
	u8 ucRegVal;

	ucRegVal = RcGetReg(ucRegAddr);
	if(ucModifyVal)
	{
		ucRegVal |= ucMaskByte;
	}
	else
	{
		ucRegVal &= (~ucMaskByte);
	}
	RcSetReg(ucRegAddr, ucRegVal);
}
/**************************************************
// 函数原型:    void ReadFIFO(u8 idata *pBuf,u8 ucLen)
// 函数功能:    读FIFO中的数据
// 入口参数:    u8 ucLen						// 期望从FIFO中读出的字节数
// 出口参数:    u8 idata *pBuf				// 读出的数据
// 返 回 值:    -
// 描　  述:    为了加快速度，特殊处理(直接调用底层硬件)
 **************************************************/
void ReadFIFO(u8  *pBuf,u8 ucLen)
{
	int i;
	for(i=0;i<ucLen;i++)
	{
		pBuf[i] = RcGetReg(JREG_FIFODATA);
	}
}
/**************************************************
// 函数原型:    void ReadFIFO(u8 idata *pBuf,u8 ucLen)
// 函数功能:    向FIFO中写入数据
// 入口参数:    u8 idata *pBuf				// 写入的数据
//              u8 ucLen           			// 写入的字节数
// 出口参数:    
// 返 回 值:    -
// 描　  述:    
 **************************************************/

void WriteFIFO(u8  *pBuf,u8 ucLen)
{
	int i;
	for(i=0;i<ucLen;i++)
	{
		RcSetReg(JREG_FIFODATA, pBuf[i]);
	}
}

/**************************************************
// 函数原型:	void SetTimeOut(u32 _302us)
// 函数功能:	设置定时器超时。
// 入口参数:	u32 _302us			; 超时时间 = (_302us) * 302 (us)
// 出口参数:	-
// 返 回 值:	-
// 说    明:	在PcdConfig()中已将预分频器设置为每302us输出一计数脉冲。
 **************************************************/
void SetTimeOut(u32 _302us)
{
	RcSetReg(JREG_TRELOADLO, ((u8)(_302us & 0xff)));
	RcSetReg(JREG_TRELOADHI, ((u8)((_302us >> 8) & 0xff)));
}

short SetTimeOutA(unsigned int uiMicroSeconds)
{
	unsigned int RegVal;
	unsigned char TmpVal;
	RegVal = uiMicroSeconds / 100;
	/*
NOTE: The supported hardware range is bigger, since the prescaler here
is always set to 100 us.
*/
	if(RegVal >= 0xfff)
	{
		return STATUS_INVALID_PARAMETER;
	}
	RcModifyReg(JREG_TMODE, 1, JBIT_TAUTO);

	RcSetReg(JREG_TPRESCALER, 0xa6);

	TmpVal = RcGetReg(JREG_TMODE);
	TmpVal &= 0xf0;
	TmpVal |= 0x02;
	RcSetReg(JREG_TMODE, TmpVal);//82

	RcSetReg(JREG_TRELOADLO, ((unsigned char)(RegVal&0xff)));
	RcSetReg(JREG_TRELOADHI, ((unsigned char)((RegVal>>8)&0xff)));
	return STATUS_SUCCESS;
}


/**************************************************
// 函数原型:	void ResetInfo(void)
// 函数功能:	复位命令信息
// 入口参数:	-
// 出口参数:	-
// 返 回 值:	-
 **************************************************/

void ResetInfo(void)
{
	u8 i;
	u8 *BfrPtr = (u8 *)(&MInfo);

	for(i=0 ; i<sizeof(MfCmdInfo); i++)
		BfrPtr[i] = 0;
}
/**************************************************
// 函数原型:	u8  PcdCmd(u8 ucCmd,u16 nCmdLen,u8 *pExchangeBuf)
// 函数功能:	Pcd将数据发送到卡，然后等待接收从卡返回的数据。
// 入口参数:	u8 ucCmd						// 命令
//				u16 nCmdLen					// 命令长度
//				u8 *pExchangeBuf				// 发送数据缓冲区首址。
// 出口参数:	u8 *pExchangeBuf				// 接收数据缓冲区首址。
// 返 回 值:	STATUS_SUCCESS -- 操作成功，其他值 -- 操作失败
// 说    明:	-
 **************************************************/

// 使用查询模式
u8  PcdCmd(u8 ucCmd,u16 ucCmdLen,u8 *pInBuf,u8 *pOutBuf)
{
	u8  ucStatus = STATUS_SUCCESS;

	u8  CommIrqEn   = 0;
	u8  divIrqEn    = 0;
	u8  WaitForComm = JBIT_ERRI | JBIT_TXI;
	u8  WaitForDiv  = 0;
	u8  doReceive   = 0;
	u8  getRegVal,setRegVal;
	u8  nbytes;
	u16 nbits;

	MInfo.Cmd = ucCmd;
	MInfo.nBytesToSend = (u8)(ucCmdLen & 0xFF);
	RcSetReg(JREG_COMMIRQ, 0x7F);				
	RcSetReg(JREG_DIVIRQ, 0x7F);
	RcSetReg(JREG_FIFOLEVEL, JBIT_FLUSHBUFFER);

	getRegVal = RcGetReg(JREG_COMMAND);    		
	if(MInfo.Cmd == JCMD_TRANSCEIVE)
	{											
		setRegVal = (getRegVal & ~JMASK_COMMAND) | JCMD_TRANSCEIVE;
		RcSetReg(JREG_COMMAND, setRegVal);
	}
	else
	{											
		setRegVal = (getRegVal & ~JMASK_COMMAND);
		RcSetReg(JREG_COMMAND, setRegVal);
	}
	switch(MInfo.Cmd)
	{
	case JCMD_IDLE:
		WaitForComm = 0;
		WaitForDiv  = 0;
	break;

#ifndef RC522
	case JCMD_MEM:
		CommIrqEn = JBIT_IDLEI;
		WaitForComm = JBIT_IDLEI;
	break;
#endif

	case JCMD_CALCCRC:
		WaitForComm = 0;
		WaitForDiv  = 0;
	break;

	case JCMD_TRANSMIT:
		CommIrqEn = JBIT_TXI | JBIT_TIMERI;
		WaitForComm = JBIT_TXI;
	break;

	case JCMD_RECEIVE:
		CommIrqEn = JBIT_RXI | JBIT_TIMERI;		//| JBIT_ERRI;
		WaitForComm = JBIT_RXI | JBIT_TIMERI; 	//| JBIT_ERRI;
		doReceive = 1;
	break;

	case JCMD_TRANSCEIVE:
		CommIrqEn = JBIT_RXI | JBIT_TIMERI; 	//| JBIT_ERRI;
		WaitForComm = JBIT_RXI | JBIT_TIMERI;	//| JBIT_ERRI;
		doReceive = 1;
	break;

	case JCMD_AUTHENT:
		CommIrqEn = JBIT_IDLEI | JBIT_TIMERI;	//| JBIT_ERRI;
		WaitForComm = JBIT_IDLEI | JBIT_TIMERI;	//| JBIT_ERRI;
	break;

	case JCMD_SOFTRESET: 
		WaitForComm = 0;
		WaitForDiv  = 0;
	break;

	default:
		ucStatus = STATUS_UNSUPPORTED_COMMAND;
	}

	if(ucStatus == STATUS_SUCCESS)
	{										
		getRegVal = RcGetReg(JREG_COMMIEN);
		RcSetReg(JREG_COMMIEN, (u8)(getRegVal | CommIrqEn));

		getRegVal = RcGetReg(JREG_DIVIEN);
		RcSetReg(JREG_DIVIEN, (u8)(getRegVal | divIrqEn));

		WriteFIFO(pInBuf,MInfo.nBytesToSend);

		if(MInfo.Cmd == JCMD_TRANSCEIVE)		
		{										
			RcModifyReg(JREG_BITFRAMING, 1, JBIT_STARTSEND);
		}
		else
		{
			getRegVal = RcGetReg(JREG_COMMAND);
			RcSetReg(JREG_COMMAND, (u8)((getRegVal & ~JMASK_COMMAND) | MInfo.Cmd));
		}
		getRegVal = 0;        				
		setRegVal = 0;

		g_ucPCDTmOut = PCD_DELAY1000MS;
		while(!(WaitForComm ? (WaitForComm & setRegVal) : 1) ||
				!(WaitForDiv ? (WaitForDiv & getRegVal) :1))
		{
			setRegVal = RcGetReg(JREG_COMMIRQ);
			getRegVal = RcGetReg(JREG_DIVIRQ);

			g_ucPCDTmOut--;
			if(g_ucPCDTmOut == 0)
				break;

			Sleepus(1);
		}
		if(g_ucPCDTmOut == 0)
		{
			//LOGD("APDU等待超时\n");
			ucStatus = STATUS_ACCESS_TIMEOUT;
		}
		else
		{	        						
			WaitForComm = (u8)(WaitForComm & setRegVal);
			WaitForDiv  = (u8)(WaitForDiv & getRegVal);

			if (setRegVal & JBIT_TIMERI)	  
			{
				ucStatus = STATUS_IO_TIMEOUT;
			}
		}
	}

	RcModifyReg(JREG_COMMIEN, 0, CommIrqEn);  
	RcModifyReg(JREG_DIVIEN, 0, divIrqEn);

	//LOGD("doReceive:%d,ucStatus:%02x\n",doReceive,ucStatus);

	if(doReceive && (ucStatus == STATUS_SUCCESS))
	{ 										
		MInfo.nBytesReceived = RcGetReg(JREG_FIFOLEVEL);
		nbytes = MInfo.nBytesReceived;
		getRegVal = RcGetReg(JREG_CONTROL);
		MInfo.nBitsReceived = (u8)(getRegVal & 0x07);
		nbits = MInfo.nBitsReceived;

		getRegVal = RcGetReg(JREG_ERROR);

		if(getRegVal)        			
		{
			if(getRegVal & JBIT_COLLERR)
				ucStatus = STATUS_COLLISION_ERROR;
			else if(getRegVal & JBIT_PARITYERR)
				ucStatus = STATUS_PARITY_ERROR;     

			if(getRegVal & JBIT_PROTERR)
				ucStatus = STATUS_PROTOCOL_ERROR;  
			else if(getRegVal & JBIT_BUFFEROVFL)
				ucStatus = STATUS_BUFFER_OVERFLOW;  
			else if(getRegVal & JBIT_CRCERR)
			{   								
				if(MInfo.nBytesReceived == 0x01 &&
						(MInfo.nBitsReceived == 0x04 ||
						 MInfo.nBitsReceived == 0x00))
				{  
					pOutBuf[0] = RcGetReg(JREG_FIFODATA);
					MInfo.nBytesReceived = 1;
					ucStatus = STATUS_ACK_SUPPOSED;	
				}
				else
					ucStatus = STATUS_CRC_ERROR;    
			}
			else if(getRegVal & JBIT_TEMPERR)
				ucStatus = STATUS_TEMP_ERROR;  
			if(getRegVal & JBIT_WRERR)
				ucStatus = STATUS_FIFO_WRITE_ERROR; 
			if(ucStatus == STATUS_SUCCESS)
				ucStatus = STATUS_ERROR_NY_IMPLEMENTED;

			RcSetReg(JREG_ERROR, 0);
		}

		if(ucStatus != STATUS_ACK_SUPPOSED)	
		{
			ReadFIFO(pOutBuf,MInfo.nBytesReceived); 

			if(MInfo.nBitsReceived && MInfo.nBytesReceived)	
				MInfo.nBytesReceived --;
		}
	}
#if debug
	LOGD("PcdCmd receive datas: ");

	for(i=0;i<MInfo.nBytesReceived;i++)
	{
		LOGD("%02x",pOutBuf[i]);
	}
	LOGD("\n");
#endif
	RcSetReg(JREG_COMMIRQ, WaitForComm);
	RcSetReg(JREG_DIVIRQ, WaitForDiv);
	RcSetReg(JREG_FIFOLEVEL, JBIT_FLUSHBUFFER);
	RcSetReg(JREG_COMMIRQ, JBIT_TIMERI);
	RcSetReg(JREG_BITFRAMING, 0);
	return ucStatus;
}



/**************************************************
// 函数原型:	u8  PcdCmd(u8 ucCmd,u16 nCmdLen,u8 *pExchangeBuf)
// 函数功能:	Pcd将数据发送到卡，然后等待接收从卡返回的数据。
// 入口参数:	u8 ucCmd						// 命令
//				u16 nCmdLen					// 命令长度
//				u8 *pExchangeBuf				// 发送数据缓冲区首址。
// 出口参数:	u8 *pExchangeBuf				// 接收数据缓冲区首址。
// 返 回 值:	STATUS_SUCCESS -- 操作成功，其他值 -- 操作失败
// 说    明:	-
 **************************************************/

// 使用查询模式(等待超时缩短适用于读取卡号)
u8  PcdCmdS(u8 ucCmd,u16 ucCmdLen,u8 *pInBuf,u8 *pOutBuf)
{
	u8  ucStatus = STATUS_SUCCESS;

	u8  CommIrqEn   = 0;
	u8  divIrqEn    = 0;
	u8  WaitForComm = JBIT_ERRI | JBIT_TXI;
	u8  WaitForDiv  = 0;
	u8  doReceive   = 0;
	u8  getRegVal,setRegVal;
	u8  nbytes;
	u16 nbits;

	MInfo.Cmd = ucCmd;
	MInfo.nBytesToSend = (u8)(ucCmdLen & 0xFF);
	RcSetReg(JREG_COMMIRQ, 0x7F);
	RcSetReg(JREG_DIVIRQ, 0x7F);
	RcSetReg(JREG_FIFOLEVEL, JBIT_FLUSHBUFFER);

	getRegVal = RcGetReg(JREG_COMMAND);
	if(MInfo.Cmd == JCMD_TRANSCEIVE)
	{
		setRegVal = (getRegVal & ~JMASK_COMMAND) | JCMD_TRANSCEIVE;
		RcSetReg(JREG_COMMAND, setRegVal);
	}
	else
	{
		setRegVal = (getRegVal & ~JMASK_COMMAND);
		RcSetReg(JREG_COMMAND, setRegVal);
	}
	switch(MInfo.Cmd)
	{
	case JCMD_IDLE:
		WaitForComm = 0;
		WaitForDiv  = 0;
	break;

	case JCMD_CALCCRC:
		WaitForComm = 0;
		WaitForDiv  = 0;
	break;

	case JCMD_TRANSMIT:
		CommIrqEn = JBIT_TXI | JBIT_TIMERI;
		WaitForComm = JBIT_TXI;
	break;

	case JCMD_RECEIVE:
		CommIrqEn = JBIT_RXI | JBIT_TIMERI;		//| JBIT_ERRI;
		WaitForComm = JBIT_RXI | JBIT_TIMERI; 	//| JBIT_ERRI;
		doReceive = 1;
	break;

	case JCMD_TRANSCEIVE:
		CommIrqEn = JBIT_RXI | JBIT_TIMERI; 	//| JBIT_ERRI;
		WaitForComm = JBIT_RXI | JBIT_TIMERI;	//| JBIT_ERRI;
		doReceive = 1;
	break;

	case JCMD_AUTHENT:
		CommIrqEn = JBIT_IDLEI | JBIT_TIMERI;	//| JBIT_ERRI;
		WaitForComm = JBIT_IDLEI | JBIT_TIMERI;	//| JBIT_ERRI;
	break;

	case JCMD_SOFTRESET:
		WaitForComm = 0;
		WaitForDiv  = 0;
	break;

	default:
		ucStatus = STATUS_UNSUPPORTED_COMMAND;
	}

	if(ucStatus == STATUS_SUCCESS)
	{
		getRegVal = RcGetReg(JREG_COMMIEN);
		RcSetReg(JREG_COMMIEN, (u8)(getRegVal | CommIrqEn));

		getRegVal = RcGetReg(JREG_DIVIEN);
		RcSetReg(JREG_DIVIEN, (u8)(getRegVal | divIrqEn));

		WriteFIFO(pInBuf,MInfo.nBytesToSend);

		if(MInfo.Cmd == JCMD_TRANSCEIVE)
		{
			RcModifyReg(JREG_BITFRAMING, 1, JBIT_STARTSEND);
		}
		else
		{
			getRegVal = RcGetReg(JREG_COMMAND);
			RcSetReg(JREG_COMMAND, (u8)((getRegVal & ~JMASK_COMMAND) | MInfo.Cmd));
		}
		getRegVal = 0;
		setRegVal = 0;

		g_ucPCDTmOut = PCD_DELAY900MS;
		while(!(WaitForComm ? (WaitForComm & setRegVal) : 1) ||
				!(WaitForDiv ? (WaitForDiv & getRegVal) :1))
		{
			setRegVal = RcGetReg(JREG_COMMIRQ);
			getRegVal = RcGetReg(JREG_DIVIRQ);

			g_ucPCDTmOut--;
			if(g_ucPCDTmOut == 0)
				break;

			Sleepus(1);
		}
		if(g_ucPCDTmOut == 0)
		{
			LOGD("APDU等待超时\n");
			ucStatus = STATUS_ACCESS_TIMEOUT;
		}
		else
		{
			WaitForComm = (u8)(WaitForComm & setRegVal);
			WaitForDiv  = (u8)(WaitForDiv & getRegVal);

			if (setRegVal & JBIT_TIMERI)
			{
				ucStatus = STATUS_IO_TIMEOUT;
			}
		}
	}

	RcModifyReg(JREG_COMMIEN, 0, CommIrqEn);
	RcModifyReg(JREG_DIVIEN, 0, divIrqEn);

	//LOGD("doReceive:%d,ucStatus:%02x\n",doReceive,ucStatus);

	if(doReceive && (ucStatus == STATUS_SUCCESS))
	{
		MInfo.nBytesReceived = RcGetReg(JREG_FIFOLEVEL);
		nbytes = MInfo.nBytesReceived;
		getRegVal = RcGetReg(JREG_CONTROL);
		MInfo.nBitsReceived = (u8)(getRegVal & 0x07);
		nbits = MInfo.nBitsReceived;

		getRegVal = RcGetReg(JREG_ERROR);

		if(getRegVal)
		{
			if(getRegVal & JBIT_COLLERR)
				ucStatus = STATUS_COLLISION_ERROR;
			else if(getRegVal & JBIT_PARITYERR)
				ucStatus = STATUS_PARITY_ERROR;

			if(getRegVal & JBIT_PROTERR)
				ucStatus = STATUS_PROTOCOL_ERROR;
			else if(getRegVal & JBIT_BUFFEROVFL)
				ucStatus = STATUS_BUFFER_OVERFLOW;
			else if(getRegVal & JBIT_CRCERR)
			{
				if(MInfo.nBytesReceived == 0x01 &&
						(MInfo.nBitsReceived == 0x04 ||
						 MInfo.nBitsReceived == 0x00))
				{
					pOutBuf[0] = RcGetReg(JREG_FIFODATA);
					MInfo.nBytesReceived = 1;
					ucStatus = STATUS_ACK_SUPPOSED;
				}
				else
					ucStatus = STATUS_CRC_ERROR;
			}
			else if(getRegVal & JBIT_TEMPERR)
				ucStatus = STATUS_TEMP_ERROR;
			if(getRegVal & JBIT_WRERR)
				ucStatus = STATUS_FIFO_WRITE_ERROR;
			if(ucStatus == STATUS_SUCCESS)
				ucStatus = STATUS_ERROR_NY_IMPLEMENTED;

			RcSetReg(JREG_ERROR, 0);
		}

		if(ucStatus != STATUS_ACK_SUPPOSED)
		{
			ReadFIFO(pOutBuf,MInfo.nBytesReceived);

			if(MInfo.nBitsReceived && MInfo.nBytesReceived)
				MInfo.nBytesReceived --;
		}
	}
#if debug
	LOGD("PcdCmd receive datas: ");

	for(i=0;i<MInfo.nBytesReceived;i++)
	{
		LOGD("%02x",pOutBuf[i]);
	}
	LOGD("\n");
#endif
	RcSetReg(JREG_COMMIRQ, WaitForComm);
	RcSetReg(JREG_DIVIRQ, WaitForDiv);
	RcSetReg(JREG_FIFOLEVEL, JBIT_FLUSHBUFFER);
	RcSetReg(JREG_COMMIRQ, JBIT_TIMERI);
	RcSetReg(JREG_BITFRAMING, 0);

	return ucStatus;
}


/*************************************************
Function:       M522PcdCmd
Description:
implement a command
Parameter:
cmd            command code
ExchangeBuf    saved the data will be send to card and the data responed from the card
info           some information for the command
Return:
short      status of implement
 **************************************************/
u8  M522PcdCmd(unsigned char cmd,
		unsigned char *ExchangeBuf,
		MfCmdInfo  *info)
{
	short          status    = STATUS_SUCCESS;
	short          istatus    = STATUS_SUCCESS;

	unsigned char   commIrqEn   = 0;
	unsigned char   divIrqEn    = 0;
	unsigned char   waitForComm = JBIT_ERRI | JBIT_TXI;
	unsigned char   waitForDiv  = 0;
	unsigned char   doReceive   = 0;
	unsigned char   i;
	unsigned char   getRegVal,setRegVal;

	unsigned char  nbytes, nbits;
	unsigned int counter;

	/*remove all Interrupt request flags that are used during function,
	  keep all other like they are*/
	RcSetReg(JREG_COMMIRQ, waitForComm);
	RcSetReg(JREG_DIVIRQ, waitForDiv);
	RcSetReg(JREG_FIFOLEVEL, JBIT_FLUSHBUFFER);

	/*disable command or set to transceive*/
	getRegVal = RcGetReg(JREG_COMMAND);
	if(cmd == JCMD_TRANSCEIVE)
	{
		/*re-init the transceive command*/
		setRegVal = (getRegVal & ~JMASK_COMMAND) | JCMD_TRANSCEIVE;
		RcSetReg(JREG_COMMAND, setRegVal);//0c
	}
	else
	{
		/*clear current command*/
		setRegVal = (getRegVal & ~JMASK_COMMAND);
		RcSetReg(JREG_COMMAND, setRegVal);
	}
	MpIsrInfo = info;
	switch(cmd)
	{
	case JCMD_IDLE:         /* values are 00, so return immediately after all bytes written to FIFO */
		waitForComm = 0;
		waitForDiv  = 0;
	break;
	case JCMD_CALCCRC:      /* values are 00, so return immediately after all bytes written to FIFO */
		waitForComm = 0;
		waitForDiv  = 0;
	break;
	case JCMD_TRANSMIT:
		commIrqEn = JBIT_TXI | JBIT_TIMERI;
		waitForComm = JBIT_TXI;
	break;
	case JCMD_RECEIVE:
		commIrqEn = JBIT_RXI | JBIT_TIMERI | JBIT_ERRI;
		waitForComm = JBIT_RXI | JBIT_TIMERI | JBIT_ERRI;
		doReceive = 1;
	break;
	case JCMD_TRANSCEIVE:
		commIrqEn = JBIT_RXI | JBIT_TIMERI | JBIT_ERRI;
		waitForComm = JBIT_RXI | JBIT_TIMERI | JBIT_ERRI;
		doReceive = 1;
	break;
	case JCMD_AUTHENT:
		commIrqEn = JBIT_IDLEI | JBIT_TIMERI | JBIT_ERRI;
		waitForComm = JBIT_IDLEI | JBIT_TIMERI | JBIT_ERRI;
	break;
	case JCMD_SOFTRESET:    /* values are 0x00 for IrqEn and for waitFor, nothing to do */
		waitForComm = 0;
		waitForDiv  = 0;
	break;
	default:
		status = STATUS_UNSUPPORTED_COMMAND;
	}
	if(status == STATUS_SUCCESS)
	{
		/* activate necessary communication Irq's */
		getRegVal = RcGetReg(JREG_COMMIEN);
		RcSetReg(JREG_COMMIEN, getRegVal | commIrqEn);

		/* activate necessary other Irq's */
		getRegVal = RcGetReg(JREG_DIVIEN);
		RcSetReg(JREG_DIVIEN, getRegVal | divIrqEn);

		/*write data to FIFO*/
		for(i=0; i<MpIsrInfo->nBytesToSend; i++)
		{
			RcSetReg(JREG_FIFODATA, ExchangeBuf[i]);
		}

		/*do seperate action if command to be executed is transceive*/
		if(cmd == JCMD_TRANSCEIVE)
		{
			/*TRx is always an endless loop, Initiator and Target must set STARTSEND.*/
			RcModifyReg(JREG_BITFRAMING, 1, JBIT_STARTSEND);
		}
		else
		{
			getRegVal = RcGetReg(JREG_COMMAND);
			RcSetReg(JREG_COMMAND, (getRegVal & ~JMASK_COMMAND) | cmd);
		}

		/*polling mode*/
		getRegVal = 0;
		setRegVal = 0;
		counter = 0; /*Just for debug*/
		while(!(waitForComm ? (waitForComm & setRegVal) : 1) ||
				!(waitForDiv ? (waitForDiv & getRegVal) :1))
		{
			setRegVal = RcGetReg(JREG_COMMIRQ);
			getRegVal = RcGetReg(JREG_DIVIRQ);
			counter ++;
			if(counter > 0x1000)
			{
				LOGD("等待cmd命令超时\n");
				break;
			}
			Sleepus(1);
		}
		/*store IRQ bits for clearance afterwards*/
		waitForComm = (unsigned char)(waitForComm & setRegVal);
		waitForDiv  = (unsigned char)(waitForDiv & getRegVal);

		/*set status to Timer Interrupt occurence*/
		if (setRegVal & JBIT_TIMERI)
		{
			istatus = STATUS_IO_TIMEOUT;
		}
	}

	/*disable all interrupt sources*/
	RcModifyReg(JREG_COMMIEN, 0, commIrqEn);

	RcModifyReg(JREG_DIVIEN, 0, divIrqEn);

	if(doReceive && (istatus == STATUS_SUCCESS))
	{
		/*read number of bytes received (used for error check and correct transaction*/
		MpIsrInfo->nBytesReceived = RcGetReg(JREG_FIFOLEVEL);
		nbytes = MpIsrInfo->nBytesReceived;
		getRegVal = RcGetReg(JREG_CONTROL);
		MpIsrInfo->nBitsReceived = (unsigned char)(getRegVal & 0x07);
		nbits = MpIsrInfo->nBitsReceived;

		getRegVal = RcGetReg(JREG_ERROR);
		/*set status information if error occured*/
		if(getRegVal)
		{
			if(getRegVal & JBIT_COLLERR)
				istatus = STATUS_COLLISION_ERROR;         /* Collision Error */
			else if(getRegVal & JBIT_PARITYERR)
				istatus = STATUS_PARITY_ERROR;            /* Parity Error */

			if(getRegVal & JBIT_PROTERR)
				istatus = STATUS_PROTOCOL_ERROR;          /* Protocoll Error */
			else if(getRegVal & JBIT_BUFFEROVFL)
				istatus = STATUS_BUFFER_OVERFLOW;         /* BufferOverflow Error */
			else if(getRegVal & JBIT_CRCERR)
			{   /* CRC Error */
				if(MpIsrInfo->nBytesReceived == 0x01 &&
						(MpIsrInfo->nBitsReceived == 0x04 ||
						 MpIsrInfo->nBitsReceived == 0x00))
				{   /* CRC Error and only one byte received might be a Mifare (N)ACK */
					ExchangeBuf[0] = RcGetReg(JREG_FIFODATA);
					MpIsrInfo->nBytesReceived = 1;
					istatus = STATUS_ACK_SUPPOSED;        /* (N)ACK supposed */
				}
				else
					istatus = STATUS_CRC_ERROR;           /* CRC Error */
			}
			else if(getRegVal & JBIT_TEMPERR)
				istatus = STATUS_TEMP_ERROR;       /* Temperature Error */
			if(getRegVal & JBIT_WRERR)
				istatus = STATUS_FIFO_WRITE_ERROR;        /* Error Writing to FIFO */
			if(istatus == STATUS_SUCCESS)
				istatus = STATUS_ERROR_NY_IMPLEMENTED;    /* Error not yet implemented, shall never occur! */

			/* if an error occured, clear error register before IRQ register */
			RcSetReg(JREG_ERROR, 0);
		}

		/*read data from FIFO and set response parameter*/
		if(istatus != STATUS_ACK_SUPPOSED)
		{
			for(i=0; i<MpIsrInfo->nBytesReceived; i++)
			{
				ExchangeBuf[i] = RcGetReg(JREG_FIFODATA);
			}
			/*in case of incomplete last byte reduce number of complete bytes by 1*/
			if(MpIsrInfo->nBitsReceived && MpIsrInfo->nBytesReceived)
				MpIsrInfo->nBytesReceived --;
		}
	}
	RcSetReg(JREG_COMMIRQ, waitForComm);
	RcSetReg(JREG_DIVIRQ, waitForDiv);
	RcSetReg(JREG_FIFOLEVEL, JBIT_FLUSHBUFFER);
	RcSetReg(JREG_COMMIRQ, JBIT_TIMERI);
	RcSetReg(JREG_BITFRAMING, 0);
	return istatus;
}



/**************************************************
// 函数原型:   void PcdHardRst(void)
// 函数功能:   PCD硬件复位
// 入口参数:   -
// 出口参数:   -
// 返 回 值:   -
 **************************************************/
void PcdHardRst(void)
{
	PN512_Reset();
}
/**************************************************
// 函数原型:	void PcdReset(u8 ucPause_1ms,u8 ucWait_1ms)
// 函数功能:	使RF场产生1个暂停，让卡复位
// 入口参数:	u8 ucPause_1ms		; 暂停时间，关闭RF场该时间后重新打开，
//												; 若为0则不重新打开
//				u8 ucWait_1ms		; RF场重新打开后持续等待该时间，若为0则不等待
// 出口参数:	-
// 返 回 值:	-
// 说    明:	等待时间可根据卡的功耗而定，如Mifare1卡的功耗较小，等待数毫秒即可，
//				而CPU卡功耗较大，需要等待80毫秒左右。
 **************************************************/
void PcdReset(u8 ucPause_1ms,u8 ucWait_1ms)
{
	u8 RegVal;

	// 关闭RF场
	//RcModifyReg(JREG_TXCONTROL, 0, JBIT_TX2RFEN | JBIT_TX1RFEN);
	RcModifyReg(JREG_TXCONTROL,0,g_ucTxConMask);

	if(ucPause_1ms)
	{
		SetTimeOut((u32)((ucPause_1ms * 10)/3));
		RcModifyReg(JREG_CONTROL, 1, JBIT_TSTARTNOW);
		iTimeOutCount=0;
		do 	
		{
			//判断超时
			iTimeOutCount++;
			if(iTimeOutCount>TIMEOUT)
			{
				iTimeOutCount=0;
				LOGD("PiccActivate超时退出\n");
				break;
			}
			RegVal = RcGetReg(JREG_COMMIRQ);
			Sleepus(1);
		}
		while(!(RegVal & JBIT_TIMERI));	
		RcSetReg(JREG_COMMIRQ, JBIT_TIMERI);

		// 激活RF场
		//RcModifyReg(JREG_TXCONTROL, 1, JBIT_TX2RFEN | JBIT_TX1RFEN);
		RcModifyReg(JREG_TXCONTROL,1,g_ucTxConMask);
		if(ucWait_1ms)
		{
			SetTimeOut((u32)((ucWait_1ms * 10)/3));
			RcModifyReg(JREG_CONTROL, 1, JBIT_TSTARTNOW);
			iTimeOutCount=0;
			do 
			{
				//判断超时
				iTimeOutCount++;
				if(iTimeOutCount>TIMEOUT)
				{
					iTimeOutCount=0;
					LOGD("PiccActivate超时退出\n");
					break;
				}
				RegVal = RcGetReg(JREG_COMMIRQ);
				Sleepus(1);
			}
			while(!(RegVal & JBIT_TIMERI));	
			RcSetReg(JREG_COMMIRQ, JBIT_TIMERI);
		}
	}
}

//打开PN512读卡器
int Pn512_open(void)
{
	int ret = 0;
	int fd;

	fd = open(device, O_RDWR);
	PN512_CS = open(GPIO5_13, O_RDWR);

	if (fd < 0)
		pabort("can't open device");

	this_fd = fd;
	/*
	 * spi mode
	 */
	ret = ioctl(fd, SPI_IOC_WR_MODE, &mode);
	if (ret == -1)
		pabort("can't set spi mode");

	ret = ioctl(fd, SPI_IOC_RD_MODE, &mode);
	if (ret == -1)
		pabort("can't get spi mode");

	/*
	 * bits per word
	 */
	ret = ioctl(fd, SPI_IOC_WR_BITS_PER_WORD, &bits);
	if (ret == -1)
		pabort("can't set bits per word");

	ret = ioctl(fd, SPI_IOC_RD_BITS_PER_WORD, &bits);
	if (ret == -1)
		pabort("can't get bits per word");

	/*
	 * max speed hz
	 */
	ret = ioctl(fd, SPI_IOC_WR_MAX_SPEED_HZ, &speed);
	if (ret == -1)
		pabort("can't set max speed hz");

	ret = ioctl(fd, SPI_IOC_RD_MAX_SPEED_HZ, &speed);
	if (ret == -1)
		pabort("can't get max speed hz");

	LOGD("spi mode: %d\n", mode);
	LOGD("bits per word: %d\n", bits);
	LOGD("max speed: %d Hz (%d KHz)\n", speed, speed/1000);
	return fd;
}


//打开读卡器
int PcdOpen(void)
{
	return Pn512_open();
}
//关闭读卡器
void PcdClose(void)
{
	close(this_fd);
	this_fd =-1;
}

/**************************************************
// 函数原型:	void PcdISOType(u8 ucType)
// 函数功能:	修改PCD的模式
// 入口参数:	u8 ucType					// ISO14443_TYPEA,
//												// ISO14443_TYPEB
//												// ISO18092_NFCIP
// 出口参数:	-
// 返 回 值:	-
// 说    明:	默认模式为TYPEA
 **************************************************/
void PcdISOType(u8 ucType)
{
#if PCD_MODE == PN512
	// 使用PN512
	if(ucType == ISO18092_NFCIP)
	{
		RcSetReg(JREG_CONTROL, 0x10);
		RcSetReg(JREG_TXMODE, 0x92);
		RcSetReg(JREG_RXMODE, 0x96);
		//RcSetReg(JREG_TXCONTROL, 0x80);
		RcSetReg(JREG_TXASK, 0x37);	
		RcSetReg(JREG_RXTHRESHOLD, 0x55);
		RcSetReg(JREG_DEMOD, 0x41);
		//		RcSetReg(JREG_MIFNFC, 0x62);
		//		RcSetReg(JREG_TXBITPHASE, (RcGetReg(JREG_TXASK) & 0x80) | 0x0f);
		RcSetReg(JREG_RFCFG, 0x59);		
		RcSetReg(JREG_GSN, 0xFF);
		RcSetReg(JREG_CWGSP, 0x3F);	
		RcSetReg(JREG_MODGSP, 0x0F);
	}
	else if (ucType == ISO14443_TYPEB)
	{
		RcSetReg(JREG_TXASK, 0x00);		
		RcSetReg(JREG_CONTROL, 0x10);		
		RcSetReg(JREG_TXMODE, 0x03);
		RcSetReg(JREG_RXMODE, 0x0B);
		RcSetReg(JREG_TYPEB, 0x03);		

		RcSetReg(JREG_DEMOD, 0x4D);
		RcSetReg(JREG_GSN, 0xFF);
		RcSetReg(JREG_CWGSP, 0x3F);		
		//		RcSetReg(JREG_MODGSP, 0x1b);
		RcSetReg(JREG_MODGSP, 0x18);	
		RcSetReg(JREG_RXTHRESHOLD, 0x55);	
		RcSetReg(JREG_RFCFG, 0x68);		
	}
	else
	{
		RcSetReg(JREG_TXASK, 0x40); 	
		RcSetReg(JREG_CONTROL, 0x10);	
		RcSetReg(JREG_TXMODE, 0x00);	
		RcSetReg(JREG_RXMODE, 0x08);	

		RcSetReg(JREG_DEMOD, 0x4D);
		RcSetReg(JREG_CWGSP, 0x3F);			
		RcSetReg(JREG_RXTHRESHOLD, 0x84);	
		RcSetReg(JREG_RFCFG, 0x48);			
	}
#elif PCD_MODE == RC523
	// 使用RC523
	if(ucType == ISO14443_TYPEB)
	{
		RcSetReg(JREG_TXASK, 0x00);		
		RcSetReg(JREG_CONTROL, 0x10);		
		RcSetReg(JREG_TXMODE, 0x03);		
		RcSetReg(JREG_RXMODE, 0x0B);	
		RcSetReg(JREG_TYPEB, 0x03);			

		RcSetReg(JREG_DEMOD, 0x4d);
		RcSetReg(JREG_GSN, 0xff);
		RcSetReg(JREG_CWGSP, 0x3f);			
		//		RcSetReg(JREG_MODGSP, 0x1b);	
		RcSetReg(JREG_MODGSP, 0x18);		
		RcSetReg(JREG_RXTHRESHOLD, 0x55);	
		RcSetReg(JREG_RFCFG, 0x68);			
	}
	else
	{
		RcSetReg(JREG_TXASK, 0x40); 		
		RcSetReg(JREG_CONTROL, 0x10);		
		RcSetReg(JREG_TXMODE, 0x00);		
		RcSetReg(JREG_RXMODE, 0x08);		

		RcSetReg(JREG_DEMOD, 0x4D);
		RcSetReg(JREG_CWGSP, 0x3F);			
		RcSetReg(JREG_RXTHRESHOLD, 0x84);	
		RcSetReg(JREG_RFCFG, 0x48);			
	}
#else
	// 使用RC522
	ucType = ucType;
	RcSetReg(JREG_TXASK, 0x40); 	
	RcSetReg(JREG_CONTROL, 0x10);	
	RcSetReg(JREG_TXMODE, 0x00);		
	RcSetReg(JREG_RXMODE, 0x08);		

	RcSetReg(JREG_DEMOD, 0x4D);
	RcSetReg(JREG_CWGSP, 0x3F);			
	RcSetReg(JREG_RXTHRESHOLD, 0x84);		
	RcSetReg(JREG_RFCFG, 0x48);			
#endif
}

/**************************************************
// 函数原型:	u8 PcdConfig(u8 ucType)
// 函数功能:	配置芯片
// 入口参数:	u8 ucType					// TYPEA -- ISO14443A,TYPEB -- ISO14443B
// 出口参数:	-
// 返 回 值:	STATUS_SUCCESS -- 操作成功，其他值 -- 操作失败
// 说    明:	-
 **************************************************/
u8 PcdConfig(u8 ucType)
{
	u8 ucRegVal;

#if	HARDWARE_MODE
	PcdHardRst();								// 硬件复位
#else
	RcSetReg(JREG_COMMAND, JCMD_SOFTRESET);		// 复位芯片
#endif				// HARDWARE_MODE

	PcdISOType(ucType);

	RcSetReg(JREG_GSN, 0xF0 | 0x04);	
	ucRegVal = RcGetReg(JREG_GSN);
	LOGD("JREG_GSN = 0x%02X\n", ucRegVal);
	if(ucRegVal != 0xF4)			
		return STATUS_INIT_ERROR;

	RcSetReg(JREG_TPRESCALER, FREQ_SPLI_302us & 0xff);
	RcSetReg(JREG_TMODE, JBIT_TAUTO | ((FREQ_SPLI_302us >> 8) & JMASK_TPRESCALER_HI));						

	SetTimeOut(RIC_DELAY5MS);			

	RcModifyReg(JREG_TXCONTROL, 1, JBIT_TX2RFEN | JBIT_TX1RFEN);
	RcModifyReg(JREG_CONTROL, 1, JBIT_TSTARTNOW);
	iTimeOutCount=0;
	do
	{
		//判断超时
		iTimeOutCount++;
		if(iTimeOutCount>TIMEOUT)
		{
			iTimeOutCount=0;
			LOGD("PiccActivate超时退出\n");
			break;
		}
		ucRegVal = RcGetReg(JREG_COMMIRQ);
		Sleepus(1);
	}
	while(!(ucRegVal & JBIT_TIMERI));

	RcSetReg(JREG_COMMIRQ, JBIT_TIMERI);	
	RcSetReg(JREG_COMMAND, JCMD_IDLE);
	RcSetReg(JREG_ANALOGTEST, 0xCD);

	RcSetReg(JREG_TXSEL, 0x17);

#if PCD_IRQ_EN
	RcSetReg(JREG_WATERLEVEL, PCD_FIFO_WATER_LEVEL);	
#endif				// INIT_MODE_EN

	g_ucTxConMask = 0x03;
	//g_ucTxConMask = 0x83;

	return STATUS_SUCCESS;
}


/**************************************************
// 函数原型:    u8 PiccHaltA(void)
// 函数功能:   	将A卡置为HALT状态
// 入口参数:    -
// 出口参数:    -
// 返 回 值:    STATUS_SUCCESS -- 成功；其它值 -- 失败。
// 说    明:	-
 **************************************************/
u8 PiccHaltA(void)
{
	u8 ucStatus = STATUS_SUCCESS;
	u8 ucTempBuf[2];
	u8 ucOutTempBuf[64];

	RcModifyReg(JREG_TXMODE, 1, JBIT_CRCEN);
	RcModifyReg(JREG_RXMODE, 1, JBIT_CRCEN);

	ucTempBuf[0] = HALTA_CMD;
	ucTempBuf[1] = HALTA_PARAM;

	ResetInfo();
	SetTimeOut(RIC_DELAY1MS);

	ucStatus = PcdCmd(JCMD_TRANSCEIVE,HALTA_CMD_LENGTH,ucTempBuf,ucOutTempBuf);

	if(ucStatus == STATUS_IO_TIMEOUT)
		ucStatus = STATUS_SUCCESS;
	return ucStatus;
}

/**************************************************
// 函数原型:    u8 PiccRequest(u8 ucReqCode,u8 *pATQ)
// 函数功能:    A型卡激活命令
// 入口参数:    u8 ucReqCode					// 请求代码	ISO14443_3A_REQIDL	0x26	IDLE
//												// 			ISO14443_3A_REQALL	0x52	ALL
// 出口参数:    u8 *pATQ						// 请求回应码,2字节
// 返 回 值:    STATUSSUCCESS -- 成功；其它值 -- 失败。
// 说    明:	-
 **************************************************/
u8 PiccRequest(u8 ucReqCode, u8 *pATQ)
{
	u8 ucStatus = STATUS_SUCCESS;
	u8  ucTempBuf[2];
	u8 ucOutTempBuf[64];


	RcModifyReg(JREG_STATUS2, 0, JBIT_CRYPTO1ON);
	RcSetReg(JREG_COLL,JBIT_VALUESAFTERCOLL);
	RcModifyReg(JREG_TXMODE, 0, 0xF0);
	RcModifyReg(JREG_RXMODE, 0, 0xF0);
	RcSetReg(JREG_BITFRAMING, REQUEST_BITS);

	RcSetReg(JREG_TXCONTROL,(RcGetReg(JREG_TXCONTROL) & 0xFC) | g_ucTxConMask);

	ucTempBuf[0] = ucReqCode;
	ResetInfo();
	SetTimeOut(RIC_DELAY4MS);
	ucStatus = PcdCmdS(JCMD_TRANSCEIVE, 1, ucTempBuf,ucOutTempBuf);

	if(ucStatus == STATUS_SUCCESS || ucStatus == STATUS_COLLISION_ERROR)
	{
		if(MInfo.nBytesReceived != ATQA_LENGTH || MInfo.nBitsReceived != 0x00)
			ucStatus = STATUS_PROTOCOL_ERROR;
		else
			memcpy(pATQ, ucOutTempBuf, 2);
	}

	RcSetReg(JREG_BITFRAMING, 0);

	return ucStatus;
}
/**************************************************
// 函数原型:    u8 PiccSelect(u8 ucSelCode, u8 *pUID, u8 *pSAK)
// 函数功能:    选择卡
// 入口参数:    u8 ucSelCode					// 防碰撞代码	SELECT_CASCADE_LEVEL_1 0x93：第1级
//												//				SELECT_CASCADE_LEVEL_1 0x95：第2级
//												// 				SELECT_CASCADE_LEVEL_1 0x97：第3级
//				u8 *pUID						// 4字节UID。
// 出口参数:    u8 *pSAK						// 选择应答
// 返 回 值:    STATUS_SUCCESS -- 成功；其它值 -- 失败。
// 说    明:	-
 **************************************************/

u8 PiccSelect(u8 ucSelCode, u8 *pUID, u8 *pSAK)
{
	u8 ucStatus = STATUS_SUCCESS;
	u8 ucTempBuf[7];
	u8 ucOutTempBuf[64];

	//LOGD("PiccSelect\n");

	RcModifyReg(JREG_TXMODE, 1, JBIT_CRCEN);
	RcModifyReg(JREG_RXMODE, 1, JBIT_CRCEN);

	ucTempBuf[0] = ucSelCode;
	ucTempBuf[1] = NVB_MAX_PARAMETER;
	memcpy(&ucTempBuf[2], pUID, 4);
	ucTempBuf[6] = (u8)(pUID[0] ^ pUID[1] ^ pUID[2] ^ pUID[3]);

	ResetInfo();
	SetTimeOut(RIC_DELAY2MS);
	ucStatus = PcdCmdS(JCMD_TRANSCEIVE, 7, ucTempBuf,ucOutTempBuf);
	if(ucStatus == STATUS_SUCCESS)
	{
		if(MInfo.nBytesReceived == SAK_LENGTH && MInfo.nBitsReceived == 0)
			*pSAK = ucOutTempBuf[0];
		else
			ucStatus = STATUS_BITCOUNT_ERROR;
	}
	//LOGD("PiccSelect_over:%d\n",ucStatus);
	return ucStatus;
}


/**************************************************
// 函数原型:    u8 PiccAnticoll( u8 unMode, u8 ucSelCode, u8 ucBitCnt, u8 *pUID)
// 函数功能:    位方式防碰撞
// 入口参数:    u8 unMode					// 0 -- 执行防碰撞循环，1 -- 只执行一次防碰撞
//				u8 ucSelCode					// 防碰撞等级	SELECT_CASCADE_LEVEL_1 0x93：第1级
//												//				SELECT_CASCADE_LEVEL_1 0x95：第2级
//												// 				SELECT_CASCADE_LEVEL_1 0x97：第3级
//				u8 ucBitCnt					// 已知UID的位数。
//				u8 *pUID						// 已知的UID
// 出口参数:    u8 *pUID						// 返回UID缓冲区首址，4字节。
// 返 回 值:    STATUS_SUCCESS -- 成功；其它值 -- 失败。
// 说    明:	unMode = 0 时，只执行一次防碰撞，该模式用于同一时刻，只允许一张卡在天线感应区内
//				若ucBitCnt为0，则输入参数的pUID不用理会
 **************************************************/
u8 PiccAnticoll(u8 unMode, u8 ucSelCode, u8 ucBitCnt, u8 *pUID)
{
	u8 ucTempBuf[7];
	u8 ucOutTempBuf[64];
	u8 ucStatus = STATUS_SUCCESS;
	u8 nbits    = 0;
	u8 nBytes   = 0;
	u8 byteOffset;

	memset(ucTempBuf, 0, sizeof(ucTempBuf));

	RcModifyReg(JREG_TXMODE, 0, JBIT_CRCEN);
	RcModifyReg(JREG_RXMODE, 0, JBIT_CRCEN);
	RcSetReg(JREG_COLL, 0);

	iTimeOutCountA=0;
	while(ucStatus == STATUS_SUCCESS)
	{
		//判断超时
		iTimeOutCountA++;
		if(iTimeOutCountA>TIMEOUT)
		{
			iTimeOutCountA=0;
			LOGD("PiccAnticoll超时退出\n");
			break;
		}

		if(ucBitCnt > SINGLE_UID_LENGTH)
		{
			ucStatus = STATUS_INVALID_PARAMETER;
			break;
		}
		nbits = (u8)(ucBitCnt % BITS_PER_BYTE);
		nBytes = (u8)(ucBitCnt / BITS_PER_BYTE);
		if(nbits)
			nBytes++;
		ucTempBuf[0] = ucSelCode;
		ucTempBuf[1] = (u8)(NVB_MIN_PARAMETER + ((ucBitCnt / BITS_PER_BYTE) << UPPER_NIBBLE_SHIFT) + nbits);
		memcpy(&ucTempBuf[2], pUID, nBytes);

		RcSetReg(JREG_BITFRAMING, (u8)((nbits << UPPER_NIBBLE_SHIFT) | nbits));

		ResetInfo();
		SetTimeOut(RIC_DELAY10MS);				// 10ms
		ucStatus = PcdCmdS(JCMD_TRANSCEIVE, (u8)(nBytes + 2), ucTempBuf,ucOutTempBuf);

		if(ucStatus == STATUS_COLLISION_ERROR || ucStatus == STATUS_SUCCESS)
		{
			ucBitCnt = (u8)(ucBitCnt + MInfo.nBitsReceived + (MInfo.nBytesReceived << 3) - nbits);
			if((ucBitCnt) > COMPLETE_UID_BITS)
			{
				ucStatus = STATUS_BITCOUNT_ERROR;
				break;
			}

			if(MInfo.nBitsReceived)
				MInfo.nBytesReceived++;

			byteOffset = 0;
			if(nbits)
			{
				pUID[nBytes - 1] |= ucOutTempBuf[0];
				byteOffset++;
			}
			memcpy(&pUID[nBytes], &ucOutTempBuf[byteOffset], 4 - nBytes );

			if(ucStatus == STATUS_COLLISION_ERROR)
			{
				if (unMode == 1)
					break;
				ucStatus = STATUS_SUCCESS;
			}
			else
			{
				if((pUID[0] ^ pUID[1] ^ pUID[2] ^ pUID[3]) != ucOutTempBuf[4 - nBytes + byteOffset])
					ucStatus = STATUS_WRONG_UID_CHECKBYTE;
				break;
			}
		}
		Sleepus(1);
	}

	RcSetReg(JREG_BITFRAMING, 0);
	RcSetReg(JREG_COLL, JBIT_VALUESAFTERCOLL);	// activate values after coll
	return ucStatus;
}


/**************************************************
// 函数原型:    u8 PiccActivate( u8 ucMode, u8 ucReqCode,  ACTIVEPARAA *pActiveParaA)
// 函数功能:   	A型卡激活
// 入口参数:    u8 ucMode					// 0 -- 执行防碰撞循环，1 -- 只执行一次防碰撞
//				u8 ucReqCode					// 请求代码	ISO14443_3A_REQIDL	0x26	IDLE
//												// 			ISO14443_3A_REQALL	0x52	ALL
// 出口参数:    ACTIVEPARAA *pActiveParaA		// Typa A卡激活信息
// 返 回 值:    STATUS_SUCCESS -- 成功；其它值 -- 失败。
// 说    明:	unMode = 0 时，只执行一次防碰撞，该模式用于同一时刻，只允许一张卡在天线感应区内
 **************************************************/
u8 PiccActivate( u8 ucMode, u8 ucReqCode, ACTIVEPARAA *pActiParaA)
{
	u8 ucStatus;
	u8 ucSelCode;
	u8 ucUIDIndex;
	u8 ucTempUID[4];

	if ((ucStatus = PiccRequest(ucReqCode, pActiParaA->ATQ)) != STATUS_SUCCESS)
	{
		if ((ucStatus = PiccRequest(ucReqCode, pActiParaA->ATQ)) != STATUS_SUCCESS)
			return STATUS_NO_TARGET;
	}
	if((pActiParaA->ATQ[0] & 0x1F) == 0x00)
		return STATUS_NO_BITWISE_ANTICOLL;

	ucUIDIndex     = 0;
	pActiParaA->UIDLen = 0;
	ucSelCode = SELECT_CASCADE_LEVEL_1;

	iTimeOutCount=0;
	while(ucStatus == STATUS_SUCCESS)
	{
		//判断超时
		iTimeOutCount++;
		if(iTimeOutCount>TIMEOUT)
		{
			iTimeOutCount=0;
			LOGD("PiccActivate超时退出\n");
			break;
		}

		if(ucSelCode > SELECT_CASCADE_LEVEL_3)
		{
			ucStatus = STATUS_SERNR_ERROR;
			break;
		}
		ucStatus = PiccAnticoll( ucMode, ucSelCode, 0, ucTempUID);
		if (ucStatus == STATUS_SUCCESS)
		{
			ucStatus = PiccSelect(ucSelCode, ucTempUID, &pActiParaA->SAK);
			if (ucStatus == STATUS_SUCCESS)
			{
				ucSelCode += 2;
				if (pActiParaA->SAK & 0x04)
				{
					memcpy(&pActiParaA->UID[ucUIDIndex], &ucTempUID[1], 3);
					ucUIDIndex += 3;
				}
				else
				{
					memcpy(&pActiParaA->UID[ucUIDIndex], ucTempUID, 4);
					pActiParaA->UIDLen = ucUIDIndex + 4;
					break;
				}
			}
		}
		Sleepus(1);
	}
	return ucStatus;
}



/**************************************************
// 函数原型:	void Card_RfReset(u8 ucPause_1ms,u8 ucWait_1ms)
// 函数功能:	使RF场产生1个暂停，让卡复位
// 入口参数:	u8 ucPause_1ms		; 暂停时间，关闭RF场该时间后重新打开，
//												; 若为0则不重新打开
//				u8 ucWait_1ms		; RF场重新打开后持续等待该时间，若为0则不等待
// 出口参数:	-
// 返 回 值:	-
// 说    明:	等待时间可根据卡的功耗而定，如Mifare1卡的功耗较小，等待数毫秒即可，
//				而CPU卡功耗较大，需要等待80毫秒左右。
 **************************************************/
void Card_RfReset(u8 ucPause_1ms,u8 ucWait_1ms)
{
	u8 RegVal;

	//LOGD("Card_RfReset\n");
	// 关闭RF场
	RcModifyReg(JREG_TXCONTROL,0,g_ucTxConMask);
	if(ucPause_1ms)
	{
		SetTimeOut((u32)((ucPause_1ms * 10)/3));
		RcModifyReg(JREG_CONTROL, 1, JBIT_TSTARTNOW);
		iTimeOutCount=0;
		do
		{
			//判断超时
			iTimeOutCount++;
			if(iTimeOutCount>TIMEOUT)
			{
				iTimeOutCount=0;
				LOGD("Card_RfReset1超时退出\n");
				break;
			}
			RegVal = RcGetReg(JREG_COMMIRQ);
			Sleepus(1);
		}
		while(!(RegVal & JBIT_TIMERI));
		RcSetReg(JREG_COMMIRQ, JBIT_TIMERI);

		// 激活RF场
		RcModifyReg(JREG_TXCONTROL,1,g_ucTxConMask);
		if(ucWait_1ms)
		{
			SetTimeOut((u32)((ucWait_1ms * 10)/3));
			RcModifyReg(JREG_CONTROL, 1, JBIT_TSTARTNOW);
			iTimeOutCount=0;
			do
			{
				//判断超时
				iTimeOutCount++;
				if(iTimeOutCount>TIMEOUT)
				{
					iTimeOutCount=0;
					LOGD("Card_RfReset2超时退出\n");
					break;
				}
				RegVal = RcGetReg(JREG_COMMIRQ);
				Sleepus(1);
			}
			while(!(RegVal & JBIT_TIMERI));
			RcSetReg(JREG_COMMIRQ, JBIT_TIMERI);
		}
	}
	//LOGD("Card_RfReset_over\n");
}
//void CardReset(u8 ucPause_1ms,u8 ucWait_1ms)
//{
//	u8 RegVal;
//	u32	time;
//	// 关闭RF场
//	RcModifyReg(JREG_TXCONTROL,0,g_ucTxConMask);

//	if(ucPause_1ms)
//	{
//		time = s_TimingTick;
//		SetTimeOut((u32)((ucPause_1ms * 10)/3));
//		RcModifyReg(JREG_CONTROL, 1, JBIT_TSTARTNOW);
//		do{

//			RegVal = RcGetReg(JREG_COMMIRQ);
//			if(GetTimeOff(s_TimingTick, time) > 10)
//			{
//				break;
//			}
//		}
//		while(!(RegVal & JBIT_TIMERI));

//		RcSetReg(JREG_COMMIRQ, JBIT_TIMERI);

//		// 激活RF场
//		RcModifyReg(JREG_TXCONTROL,1,g_ucTxConMask);
//		if(ucWait_1ms)
//		{
//			time = s_TimingTick;
//			SetTimeOut((u32)((ucWait_1ms * 10)/3));
//			RcModifyReg(JREG_CONTROL, 1, JBIT_TSTARTNOW);

//			do {
//				RegVal = RcGetReg(JREG_COMMIRQ);
//				if(GetTimeOff(s_TimingTick, time) > 10)
//				{
//					break;
//				}
//			}
//			while(!(RegVal & JBIT_TIMERI));

//			RcSetReg(JREG_COMMIRQ, JBIT_TIMERI);
//		}
//	}
//}



/**************************************************
// 函数原型:    u8 Card_Halt(void)
// 函数功能:   	将A卡置为HALT状态
// 入口参数:    -
// 出口参数:    -
// 返 回 值:    STATUS_SUCCESS -- 成功；其它值 -- 失败。
// 说    明:	-
 **************************************************/

u8 Card_Halt(void)
{
	u8 ucStatus = STATUS_SUCCESS;
	u8 ucTempBuf[2];
	u8 ucOutTempBuf[64];

	RcModifyReg(JREG_TXMODE, 1, JBIT_CRCEN);
	RcModifyReg(JREG_RXMODE, 1, JBIT_CRCEN);

	ucTempBuf[0] = HALTA_CMD;
	ucTempBuf[1] = HALTA_PARAM;

	ResetInfo();
	SetTimeOut(RIC_DELAY1MS);

	ucStatus = PcdCmdS(JCMD_TRANSCEIVE,HALTA_CMD_LENGTH,ucTempBuf,ucOutTempBuf);

	if(ucStatus == STATUS_IO_TIMEOUT)
		ucStatus = STATUS_SUCCESS;

	return ucStatus;
}

/**************************************************
// 函数原型:    u8 Card_Request(u8 ucReqCode,u8 *pATQ)
// 函数功能:    A型卡激活命令
// 入口参数:    u8 ucReqCode					// 请求代码	ISO14443_3A_REQIDL	0x26	IDLE
//											// 			ISO14443_3A_REQALL	0x52	ALL
//             pATQ              // 请求回应码,2字节
// 返 回 值:    STATUSSUCCESS -- 成功；其它值 -- 失败。
// 说    明:	-
 **************************************************/
u8 Card_Request(u8 ucReqCode, u8 *pATQ)
{
	u8 ucStatus = STATUS_SUCCESS;
	u8  ucTempBuf[2];
	u8 ucOutTempBuf[64];

	RcModifyReg(JREG_STATUS2, 0, JBIT_CRYPTO1ON);
	RcSetReg(JREG_COLL,JBIT_VALUESAFTERCOLL);
	RcModifyReg(JREG_TXMODE, 0, 0xF0);
	RcModifyReg(JREG_RXMODE, 0, 0xF0);
	RcSetReg(JREG_BITFRAMING, REQUEST_BITS);


	RcSetReg(JREG_TXCONTROL,(RcGetReg(JREG_TXCONTROL) & 0xFC) | g_ucTxConMask);

	ucTempBuf[0] = ucReqCode;
	ResetInfo();
	SetTimeOut(RIC_DELAY1MS);
	ucStatus = PcdCmdS(JCMD_TRANSCEIVE, 1, ucTempBuf,ucOutTempBuf);

	if(ucStatus == STATUS_SUCCESS || ucStatus == STATUS_COLLISION_ERROR)
	{
		if(MInfo.nBytesReceived != ATQA_LENGTH || MInfo.nBitsReceived != 0x00)
			ucStatus = STATUS_PROTOCOL_ERROR;
		else
			memcpy(pATQ, ucOutTempBuf, 2);
	}

	RcSetReg(JREG_BITFRAMING, 0);
	return ucStatus;
}



/**************************************************
// 函数原型:    u8 PiccAnticoll( u8 unMode, u8 ucSelCode, u8 ucBitCnt, u8 *pUID)
// 函数功能:    位方式防碰撞
// 入口参数:    u8 unMode					// 0 -- 执行防碰撞循环，1 -- 只执行一次防碰撞
//				u8 ucSelCode					// 防碰撞等级	SELECT_CASCADE_LEVEL_1 0x93：第1级
//												//				SELECT_CASCADE_LEVEL_1 0x95：第2级
//												// 				SELECT_CASCADE_LEVEL_1 0x97：第3级
//				u8 ucBitCnt					// 已知UID的位数。
//				u8 *pUID						// 已知的UID
// 出口参数:    u8 *pUID						// 返回UID缓冲区首址，4字节。
// 返 回 值:    STATUS_SUCCESS -- 成功；其它值 -- 失败。
// 说    明:	unMode = 0 时，只执行一次防碰撞，该模式用于同一时刻，只允许一张卡在天线感应区内
//				若ucBitCnt为0，则输入参数的pUID不用理会
 **************************************************/

u8 Card_AntiColl(u8 unMode, u8 ucSelCode, u8 ucBitCnt, u8 *pUID)
{
	u8 ucTempBuf[7];
	u8 ucOutTempBuf[64];
	u8 ucStatus = STATUS_SUCCESS;
	u8 nbits    = 0;
	u8 nBytes   = 0;
	u8 byteOffset;

	memset(ucTempBuf, 0, sizeof(ucTempBuf));

	RcModifyReg(JREG_TXMODE, 0, JBIT_CRCEN);
	RcModifyReg(JREG_RXMODE, 0, JBIT_CRCEN);
	RcSetReg(JREG_COLL, 0);

	while(ucStatus == STATUS_SUCCESS)
	{
		//判断超时        
		if(ucBitCnt > SINGLE_UID_LENGTH)
		{
			ucStatus = STATUS_INVALID_PARAMETER;
			break;
		}
		nbits = (u8)(ucBitCnt % BITS_PER_BYTE);
		nBytes = (u8)(ucBitCnt / BITS_PER_BYTE);
		if(nbits)
			nBytes++;
		ucTempBuf[0] = ucSelCode;
		ucTempBuf[1] = (u8)(NVB_MIN_PARAMETER + ((ucBitCnt / BITS_PER_BYTE) << UPPER_NIBBLE_SHIFT) + nbits);
		memcpy(&ucTempBuf[2], pUID, nBytes);

		RcSetReg(JREG_BITFRAMING, (u8)((nbits << UPPER_NIBBLE_SHIFT) | nbits));

		ResetInfo();
		SetTimeOut(RIC_DELAY10MS);				// 10ms
		ucStatus = PcdCmdS(JCMD_TRANSCEIVE, (u8)(nBytes + 2), ucTempBuf,ucOutTempBuf);

		if(ucStatus == STATUS_COLLISION_ERROR || ucStatus == STATUS_SUCCESS)
		{
			ucBitCnt = (u8)(ucBitCnt + MInfo.nBitsReceived + (MInfo.nBytesReceived << 3) - nbits);
			if((ucBitCnt) > COMPLETE_UID_BITS)
			{
				ucStatus = STATUS_BITCOUNT_ERROR;
				break;
			}

			if(MInfo.nBitsReceived)
				MInfo.nBytesReceived++;

			byteOffset = 0;
			if(nbits)
			{
				pUID[nBytes - 1] |= ucOutTempBuf[0];
				byteOffset++;
			}
			memcpy(&pUID[nBytes], &ucOutTempBuf[byteOffset], 4 - nBytes );

			if(ucStatus == STATUS_COLLISION_ERROR)
			{
				if (unMode == 1)
					break;
				ucStatus = STATUS_SUCCESS;
			}
			else
			{
				if((pUID[0] ^ pUID[1] ^ pUID[2] ^ pUID[3]) != ucOutTempBuf[4 - nBytes + byteOffset])
					ucStatus = STATUS_WRONG_UID_CHECKBYTE;
				break;
			}
		}
		Sleepus(1);
	}

	RcSetReg(JREG_BITFRAMING, 0);
	RcSetReg(JREG_COLL, JBIT_VALUESAFTERCOLL);	// activate values after coll
	return ucStatus;
}


/**************************************************
// 函数原型:    u8 PiccSelect(u8 ucSelCode, u8 *pUID, u8 *pSAK)
// 函数功能:    选择卡
// 入口参数:    u8 ucSelCode					// 防碰撞代码	SELECT_CASCADE_LEVEL_1 0x93：第1级
//												//				SELECT_CASCADE_LEVEL_1 0x95：第2级
//												// 				SELECT_CASCADE_LEVEL_1 0x97：第3级
//				u8 *pUID						// 4字节UID。
// 出口参数:    u8 *pSAK						// 选择应答
// 返 回 值:    STATUS_SUCCESS -- 成功；其它值 -- 失败。
// 说    明:	-
 **************************************************/

u8 Card_Select(u8 ucSelCode, u8 *pUID, u8 *pSAK)
{
	u8 ucStatus = STATUS_SUCCESS;
	u8 ucTempBuf[7];
	u8 ucOutTempBuf[64];

	RcModifyReg(JREG_TXMODE, 1, JBIT_CRCEN);
	RcModifyReg(JREG_RXMODE, 1, JBIT_CRCEN);

	ucTempBuf[0] = ucSelCode;
	ucTempBuf[1] = NVB_MAX_PARAMETER;
	memcpy(&ucTempBuf[2], pUID, 4);
	ucTempBuf[6] = (u8)(pUID[0] ^ pUID[1] ^ pUID[2] ^ pUID[3]);

	ResetInfo();
	SetTimeOut(RIC_DELAY2MS);
	ucStatus = PcdCmdS(JCMD_TRANSCEIVE, 7, ucTempBuf,ucOutTempBuf);
	if(ucStatus == STATUS_SUCCESS)
	{
		if(MInfo.nBytesReceived == SAK_LENGTH && MInfo.nBitsReceived == 0)
			*pSAK = ucOutTempBuf[0];
		else
			ucStatus = STATUS_BITCOUNT_ERROR;
	}
	return ucStatus;
}


//下载密钥
u8 Card_LoadKey(u8 *pKey)
{
	memcpy(s_pKey,pKey,6);
	return 0;
}


/*************************************************
//功    能：验证卡片密码
//参数说明: auth_mode[IN]: 密码验证模式
//                 0x60 = 验证A密钥
//                 0x61 = 验证B密钥 
//          addr[IN]：块地址
//          pKey[IN]：密码
//          pSnr[IN]：卡片序列号，4字节
//返    回: 成功返回MI_OK
 **************************************************/
u8 Card_Authen(u8 *pSnr,u8 addr,u8 auth_mode)
{
	u8 ucStatus;
	u8 ucComMF522Buf[MAXRLEN];
	u8 ucOutTempBuf[64];

	if(auth_mode==0)//A密钥
	{
		auth_mode=PICC_AUTHENT1A;
	}
	else if(auth_mode==1)//B密钥
	{
		auth_mode=PICC_AUTHENT1B;
	}

	ucComMF522Buf[0] = auth_mode;
	ucComMF522Buf[1] = addr*4+3;
	memcpy(&ucComMF522Buf[2], s_pKey, 6);
	memcpy(&ucComMF522Buf[8], pSnr, 4); 

	ResetInfo();
	SetTimeOut(RIC_DELAY10MS);
	ucStatus = PcdCmdS(JCMD_AUTHENT,12,ucComMF522Buf,ucOutTempBuf);
	if ((ucStatus != STATUS_SUCCESS) || (!(RcGetReg(JREG_STATUS2) & 0x08)))
	{   
		ucStatus = STATUS_AUTHENT_ERROR;   
	}
	return ucStatus;
}



/*************************************************
Function:       Write
Description:
write 16 bytes data to a block
Parameter:
addr       the address of the block
_data      the data to write
Return:
short      status of implement
 **************************************************/

u8 Card_WriteA( u8 addr, u8 *_data)
{
	unsigned char SerBuffer[64];
	short status = STATUS_SUCCESS;
	ResetInfo();
	SerBuffer[0] = PICC_WRITE;
	SerBuffer[1] = addr;
	MInfo.nBytesToSend   = 2;
	SetTimeOutA(10000);
	status = M522PcdCmd(JCMD_TRANSCEIVE,
			SerBuffer,
			&MInfo);

	//LOGD("Write status1:%d \n",status);
	if (status != STATUS_IO_TIMEOUT)
	{
		if (MInfo.nBitsReceived != 4)
		{
			status = STATUS_BITCOUNT_ERROR;
		}
		else
		{
			SerBuffer[0] &= 0x0f;
			if ((SerBuffer[0] & 0x0a) == 0)
			{
				status = STATUS_AUTHENT_ERROR;
			}
			else
			{
				if (SerBuffer[0] == 0x0a)
				{
					status = STATUS_SUCCESS;
				}
				else
				{
					status = STATUS_INVALID_FORMAT;
				}
			}
		}
	}

	//LOGD("Write status2:%d \n",status);
	if ( status == STATUS_SUCCESS)
	{

		SetTimeOutA(5000);

		ResetInfo();
		memcpy(SerBuffer,_data,16);
		MInfo.nBytesToSend   = 16;
		status = M522PcdCmd(JCMD_TRANSCEIVE,
				SerBuffer,
				&MInfo);
		//LOGD("Write status3:%d \n",status);
		//if (status & 0x80)
		if(0)
		{
			//LOGD("Write status4:%d \n",(status & 0x80));
			status = STATUS_IO_TIMEOUT;
		}
		else
		{
			if (MInfo.nBitsReceived != 4)
			{
				status = STATUS_BITCOUNT_ERROR;
			}
			else
			{
				SerBuffer[0] &= 0x0f;
				if ((SerBuffer[0] & 0x0a) == 0)
				{
					status = STATUS_ACCESS_DENIED;
				}
				else
				{
					if (SerBuffer[0] == 0x0a)
					{
						status = STATUS_SUCCESS;
					}
					else
					{
						status = STATUS_INVALID_FORMAT;
					}
				}
			}
		}
	}
	return status;
}

u8 Card_Write( u8 addr, u8 *_data)
{
	u8 ucComMF522Buf[MAXRLEN];
	u8 ucOutTempBuf[MAXRLEN];
	short status = STATUS_SUCCESS;

	ResetInfo();
	ucComMF522Buf[0] = PICC_WRITE;
	ucComMF522Buf[1] = addr;
	MInfo.nBytesToSend   = 2;
	SetTimeOut(100);

	//    status = M522PcdCmd(JCMD_TRANSCEIVE,
	//                        SerBuffer,
	//                        &MInfo);

	status=PcdCmdS(JCMD_TRANSCEIVE,2,ucComMF522Buf,ucOutTempBuf);

	//LOGD("Write status1:%d \n",status);
	if (status != STATUS_IO_TIMEOUT)
	{
		if (MInfo.nBitsReceived != 4)
		{
			status = STATUS_BITCOUNT_ERROR;
		}
		else
		{
			ucOutTempBuf[0] &= 0x0f;
			if ((ucOutTempBuf[0] & 0x0a) == 0)
			{
				status = STATUS_AUTHENT_ERROR;
			}
			else
			{
				if (ucOutTempBuf[0] == 0x0a)
				{
					status = STATUS_SUCCESS;
				}
				else
				{
					status = STATUS_INVALID_FORMAT;
				}
			}
		}
	}

	//LOGD("Write status2:%d \n",status);
	if ( status == STATUS_SUCCESS)
	{
		SetTimeOut(50);

		ResetInfo();
		memcpy(ucComMF522Buf,_data,16);
		MInfo.nBytesToSend   = 16;
		//       status = M522PcdCmd(JCMD_TRANSCEIVE,
		//                           SerBuffer,
		//                           &MInfo);
		status=PcdCmdS(JCMD_TRANSCEIVE,16,ucComMF522Buf,ucOutTempBuf);

		//LOGD("Write status3:%d \n",status);
		//  if (status & 0x80)
		if(0)
		{
			//LOGD("Write status4:%d \n",(status & 0x80));
			status = STATUS_IO_TIMEOUT;
		}
		else
		{
			if (MInfo.nBitsReceived != 4)
			{
				status = STATUS_BITCOUNT_ERROR;
			}
			else
			{
				ucOutTempBuf[0] &= 0x0f;
				if ((ucOutTempBuf[0] & 0x0a) == 0)
				{
					status = STATUS_ACCESS_DENIED;
				}
				else
				{
					if (ucOutTempBuf[0] == 0x0a)
					{
						status = STATUS_SUCCESS;
					}
					else
					{
						status = STATUS_INVALID_FORMAT;
					}
				}
			}
		}
	}
	return status;
}


/**************************************************
//功    能：读取M1卡一块数据
//参数说明: addr[IN]：块地址
//          pData[OUT]：读出的数据，16字节
//			revlen:读回数据长度
//返    回: 成功返回MI_OK
 **************************************************/
u8 Card_Read(u8 addr,u8 *pData)
{
	u8 status;
	u8 ucComMF522Buf[MAXRLEN];
	u8 ucOutTempBuf[MAXRLEN];

	ucComMF522Buf[0] = PICC_READ;
	ucComMF522Buf[1] = addr;

	RcModifyReg(JREG_TXMODE, 1, JBIT_CRCEN);
	RcModifyReg(JREG_RXMODE, 1, JBIT_CRCEN);
	SetTimeOut(RIC_DELAY10MS);
	ResetInfo();

	status=PcdCmdS(JCMD_TRANSCEIVE,2,ucComMF522Buf,ucOutTempBuf);
	if(status == STATUS_SUCCESS)
		memcpy(pData, ucOutTempBuf, 16);
	else
		status = STATUS_BITCOUNT_ERROR;
	return status;	
}

//选择CPU卡
u8 Card_SelectProCard(u8 *resp,u8 *rlen)
{
	u8 CIDTemp;
	u8 temp[128],ramFIFO[64];
	u8 status;

	CIDTemp=0x01; 
	cCIDIndex=0;
	temp[0] = 0xe0; 
	temp[1] = 0x50|(CIDTemp&0xf); 

	SetTimeOut(RIC_DELAY10MS);	
	ResetInfo();
	status =PcdCmd(JCMD_TRANSCEIVE,2,temp,ramFIFO);
	if(status == STATUS_SUCCESS)
	{
		memcpy(resp,ramFIFO,MInfo.nBytesReceived);
		*rlen=MInfo.nBytesReceived;
	}		
	else
		status = STATUS_BITCOUNT_ERROR;
	return status;

}

/**********************************************函数定义*****************************************************
* 函数名称: u8 Card_ProAPDU(u8 *comm,u8 len,u8 *resp,u8 *rlen)
* 输入参数: u8 *comm:执行的数据	u8 *len:执行的数据长度
* 输出参数: u8 *resp:返回的数据	u8 *rlen:返回的数据长度
* 返回参数: u8
* 功    能: APDU指令
* 作    者: by wcp
* 日    期: 2015/8/24
************************************************************************************************************/
u8 Card_ProAPDU(u8 *comm,u8 len,u8 *resp,u8 *rlen)
{
	int i;
	int j;
	int iTotallen = 0;
	int cRespLen=0;
	u8 acktemp;
	u8 ramFIFO[512]={0};
	u8 ramResp[512]={0};

	iTotallen = len + 2;
	//判断发送长度是否大于64字节,如果大于64字节需要分包发送，先发送64字节然后在发送剩余字节。
	if (iTotallen > 64) {
		//LOGD("发送大于64字节的数据\n");
		//==============================先发前62字节========================
		if (cCIDIndex == 0) {
			ramFIFO[0] = 0x1A;
			ramFIFO[1] = 0x01;
			cCIDIndex = 1;
		} else {
			ramFIFO[0] = 0x1B;
			ramFIFO[1] = 0x01;
			cCIDIndex = 0;
		}
		for (i = 0; i < 62; i++) {
			ramFIFO[2 + i] = comm[i];
		}

		SetTimeOut(RIC_DELAY500MS);
		ResetInfo();
		acktemp = PcdCmd(JCMD_TRANSCEIVE, 64, ramFIFO, ramResp);
		if (acktemp == STATUS_SUCCESS) {
			//memcpy(ramResp,ramResp+2,MInfo.nBytesReceived-2);
		} else {
			acktemp = STATUS_BITCOUNT_ERROR;
			return acktemp;
		}
		memset(ramFIFO, 0, 128);
		memset(ramResp, 0, 128);

		//=============================再发剩余字节====================================
		if (cCIDIndex == 0) {
			ramFIFO[0] = 0x0A;
			ramFIFO[1] = 0x01;
			cCIDIndex = 1;
		} else {
			ramFIFO[0] = 0x0B;
			ramFIFO[1] = 0x01;
			cCIDIndex = 0;
		}

		for (i = 0; i < len - 62; i++) {
			ramFIFO[2 + i] = comm[62 + i];
		}
		len = len - 62 + 2;

		SetTimeOut(RIC_DELAY500MS);
		ResetInfo();
		acktemp = PcdCmd(JCMD_TRANSCEIVE, len, ramFIFO, ramResp);
		if (acktemp == STATUS_SUCCESS) {
			//memcpy(ramResp,ramResp+2,MInfo.nBytesReceived-2);
		} else {
			acktemp = STATUS_BITCOUNT_ERROR;
			return acktemp;
		}
	} else {
		//LOGD("发送小于64字节的数据\n");
		if (cCIDIndex == 0) {
			ramFIFO[0] = 0x0A;
			ramFIFO[1] = 0x01;
			cCIDIndex = 1;
		} else {
			ramFIFO[0] = 0x0B;
			ramFIFO[1] = 0x01;
			cCIDIndex = 0;
		}
		for (i = 0; i < len; i++) {
			ramFIFO[2 + i] = comm[i];
		}
		len = len + 2;

		SetTimeOut(RIC_DELAY500MS);
		ResetInfo();
		acktemp = PcdCmd(JCMD_TRANSCEIVE, len, ramFIFO, ramResp);
		if (acktemp == STATUS_SUCCESS) {
			//memcpy(ramResp,ramResp+2,MInfo.nBytesReceived-2);
		} else {
			//LOGD("等待PCDCmd命令失败:%d \n",acktemp);
			acktemp = STATUS_BITCOUNT_ERROR;
			return acktemp;
		}
	}
	//LOGD("ramResp=%02x.%02x.%02x.%02x.\n",ramResp[0],ramResp[1],ramResp[2],ramResp[3]);
	//2013.0319增加 修改了读CPU卡(创建二进制文件)时返回fa 01 01(此时CPU卡正在处理数据),将fa 01 01在发送一次，(完美了FM1702读卡驱动)
	if ((ramResp[0] == 0xFA) && (ramResp[1] == 0x01) && (ramResp[2] == 0x01)) {
		//CPU在发送一次
		for (j = 0; j < 150; j++) {
			ramFIFO[0] = 0xFA;
			ramFIFO[1] = 0x01;
			ramFIFO[2] = 0x01;
			len = 3;

			SetTimeOut(RIC_DELAY200MS);
			ResetInfo();
			acktemp = PcdCmd(JCMD_TRANSCEIVE, len, ramFIFO, ramResp);
			if (acktemp == STATUS_SUCCESS) {
				//memcpy(ramResp,ramResp+2,MInfo.nBytesReceived-2);
			} else {
				acktemp = STATUS_BITCOUNT_ERROR;
				return acktemp;
			}
			//LOGD("数据等待ramResp[0]=%02x\n",ramResp[0]);
			if (ramResp[0] != 0xFA) {
				//接收到的数据结束
				for (i = 0; i < MInfo.nBytesReceived - 2; i++) {
					resp[i] = ramResp[2 + i];
				}
				*rlen = MInfo.nBytesReceived - 2;
				return 0;
			}
		}
	}

	//2013.0314增加 修改了读>62字节数据，只返回61字节的BUG，(完美了FM1702读卡驱动)
	//判断是否有后续数据(接收数据>62时,如果有后续包,前一个字节是0x1B或是0x1A)
	for (j = 0; j < 5; j++)
	{
		if ((ramResp[0] == 0x1A) || (ramResp[0] == 0x1B)) {
			//LOGD("大数据:%d\n",MInfo.nBytesReceived);

			for (i = 0; i < MInfo.nBytesReceived - 2; i++) {
				resp[i+cRespLen] = ramResp[2 + i];
			}
			cRespLen = cRespLen + (MInfo.nBytesReceived - 2);

			//2013.0812增加 修改了发送后续包后APDU不可用的BUG(发送0xAA后转换0xAB)(完美了FM1702读卡驱动)
			if (cCIDIndex == 0) {
				ramFIFO[0] = 0xAA;
				ramFIFO[1] = 0x01;
				cCIDIndex = 1;
			} else {
				ramFIFO[0] = 0xAB;
				ramFIFO[1] = 0x01;
				cCIDIndex = 0;
			}
			len = 2;

			SetTimeOut(RIC_DELAY500MS);
			ResetInfo();
			acktemp = PcdCmd(JCMD_TRANSCEIVE, len, ramFIFO, ramResp);
			if (acktemp == STATUS_SUCCESS) {
				//memcpy(ramResp, ramResp + 2, MInfo.nBytesReceived - 2);
			} else {
				acktemp = STATUS_BITCOUNT_ERROR;
				return acktemp;
			}
//            for (i = 0; i < MInfo.nBytesReceived - 2; i++) {
//                resp[cRespLen + i] = ramResp[i];
//            }
//            cRespLen = cRespLen + (MInfo.nBytesReceived - 2);
//
//            LOGD("all cRespLen=%d\n",cRespLen);
//            *rlen = cRespLen;
//            return 0;
		} else {
			//LOGD("数据结束:%d\n",MInfo.nBytesReceived);
			for (i = 0; i < MInfo.nBytesReceived - 2; i++) {
				resp[i+cRespLen] = ramResp[2 + i];
			}
			cRespLen = cRespLen + (MInfo.nBytesReceived - 2);
			*rlen = cRespLen;
			//LOGD("数据总长度:%d\n",cRespLen);
			//*rlen = MInfo.nBytesReceived - 2;
			return 0;
		}
	}

}







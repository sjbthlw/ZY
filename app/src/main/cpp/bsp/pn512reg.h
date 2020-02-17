////////////////////////////////////////////Copyright (c)//////////////////////////////////////////
//                                   广州周立功单片机发展有限公司
//                                         研    究    所
//                                         智能识别产品线
//
//                                     http://www.zlgmcu.com
//
//-------------- 文件信息--------------------------------------------------------------------------
// 文 件 名:    PN512Reg.h
// 文件描述:    PN512寄存器定义
// 版    本:    V1.0 
// 创 建 人:    曾本森
// 创建日期:    2008.03.1
//=================================================================================================
//-------------- 修改记录------------------------------------------------------------------------
// 修改内容:
// 当前版本:    
// 修 改 人:
// 修改日期:
// 注    意: 
///////////////////////////////////////////////////////////////////////////////////////////////////
#ifndef __PN512REG_H
#define __PN512REG_H

#ifdef __cplusplus
extern "C" {
#endif




//============== MF RC522寄存器定义 ===============================================================
// 页零；命令和状态寄存器
#define     JREG_RFU00            0x00        // 保留
#define     JREG_COMMAND            0x01        // 命令寄存器
#define     JREG_COMMIEN            0x02        // 中断使能寄存器 IRQ、TX、RX、IDLE、HI、LO、ERR、TIMER
#define     JREG_DIVIEN            0x03        // 中断使能寄存器 IRQ、SIGIN、MODE、CRC、RFON、RFOFF
#define     JREG_COMMIRQ            0x04        // 中断标志
#define     JREG_DIVIRQ            0x05        // 中断标志
#define     JREG_ERROR            0x06        // 错误状态寄存器
#define     JREG_STATUS1            0x07        // 状态寄存器1
#define     JREG_STATUS2            0x08        // 状态寄存器2
#define     JREG_FIFODATA            0x09        // FIFO
#define     JREG_FIFOLEVEL        0x0A        // FIFO存储字节数寄存器
#define     JREG_WATERLEVEL        0x0B        // FIFO上溢、下溢报警深度
#define     JREG_CONTROL            0x0C        // 控制寄存器（定时器控制，接收的位）
#define     JREG_BITFRAMING        0x0D        // Bit帧控制寄存器
#define     JREG_COLL                0x0E        // 冲突位寄存器
#define     JREG_RFU0F            0x0F        // 保留
// 页一；通信寄存器
#define     JREG_RFU10            0x10        // 保留
#define     JREG_MODE                0x11        // 通信模式寄存器
#define     JREG_TXMODE            0x12        // 发送控制寄存器（CRC和速率控制）
#define     JREG_RXMODE            0x13        // 接收控制寄存器（CRC和速率控制）
#define     JREG_TXCONTROL        0x14        // TX输出控制寄存器
#define     JREG_TXASK            0x15        // 发送器ASK调制系数控制寄存器
#define     JREG_TXSEL            0x16        // 发送控制寄存器（信号输入/输出选择）
#define     JREG_RXSEL            0x17        // 接收控制寄存器（信号输入选择）
#define     JREG_RXTHRESHOLD        0x18        // 接收控制寄存器（译码器阈值设置）
#define     JREG_DEMOD            0x19        // 接收控制寄存器（解调器设置）
#define     JREG_FELNFC1            0x1A        // 定义 FelCa 同步字节
#define     JREG_FELNFC2            0x1B        // 定义接收的最大包（FelCa）
#define     JREG_MIFNFC            0x1C        // 定义ISO14443A/Mifare/NFC的工作模式
#define     JREG_MANUALRCV          0x1D        // 接收器微调寄存器
#define     JREG_TYPEB            0x1E        // TypeB
#define     JREG_SERIALSPEED        0x1F        // 串行UART速率控制寄存器
// 页二；配置寄存器
#define     JREG_RFU20            0x20        // 保留
#define     JREG_CRCRESULT1        0x21        // CRC计算结果寄存器（MSB）
#define     JREG_CRCRESULT2        0x22        // CRC计算结果寄存器（LSB）
#define     JREG_GSNOFF            0x23        // 保留
#define     JREG_MODWIDTH            0x24        // 调制宽度控制寄存器
#define     JREG_TXBITPHASE        0x25        // 保留
#define     JREG_RFCFG            0x26        // 接收器增益控制寄存器
#define     JREG_GSN                0x27        // 天线驱动输出脚电导控制寄存器（N,控制功率）
#define     JREG_CWGSP            0x28        // 天线驱动输出脚电导控制寄存器（P,控制功率）
#define     JREG_MODGSP            0x29        // 天线驱动输出脚电导控制寄存器（调节调制系数）
#define     JREG_TMODE            0x2A        // 定时器设置寄存器
#define     JREG_TPRESCALER        0x2B        // 定时器分频系数寄存器
#define     JREG_TRELOADHI        0x2C        // 16位定时器重装值（Hi）
#define     JREG_TRELOADLO        0x2D        // 16位定时器重装值（Lo）
#define     JREG_TCOUNTERVALHI    0x2E        // 16位定时器当值（Hi）
#define     JREG_TCOUNTERVALLO    0x2F        // 16位定时器当值（Lo）
// 页三；测试寄存器
#define     JREG_RFU30            0x30        // 保留
#define     JREG_TESTSEL1            0x31        // 测试寄存器
#define     JREG_TESTSEL2            0x32        // 测试寄存器
#define     JREG_TESTPINEN        0x33        // 测试寄存器
#define     JREG_TESTPINVALUE        0x34        // 测试寄存器
#define     JREG_TESTBUS            0x35        // 内部测试总线状态
#define     JREG_AUTOTEST            0x36        // 自测试控制寄存器
#define     JREG_VERSION            0x37        // 版本
#define     JREG_ANALOGTEST        0x38        // AUX管脚控制寄存器
#define     JREG_TESTDAC1            0x39        // DAC1测试值
#define     JREG_TESTDAC2            0x3A        // DAC1测试值
#define     JREG_TESTADC            0x3B        // ADC_I和ADC_Q的值
#define     JREG_ANALOGUETEST1    0x3C        // 测试寄存器
#define     JREG_RFT3D            0x3D        // 测试寄存器
#define     JREG_RFT3E            0x3E        // 测试寄存器
#define     JREG_RFT3F            0x3F        // 测试寄存器

//============== PN512 命令码定义 ===============================================================
#define     JCMD_IDLE                0x00        // 空闲命令
#define    JCMD_CONFIG                0x01        // 配置命令（PN512）
#define    JCMD_MEM                0x01        // 存储25字节到内部缓冲区（RC52x）
#define    JCMD_GENRANID            0x02        // 获取10字节的随机数
#define     JCMD_CALCCRC            0x03        // 计算CRC
#define     JCMD_TRANSMIT            0x04        // 发送数据
#define     JCMD_NOCMDCHANGE        0x07        // 修改命令寄存器的其他位（不改变当前命令）
#define     JCMD_RECEIVE            0x08        // 激活接收器
#define     JCMD_TRANSCEIVE            0x0C        // 发送FIFO中的数据到天线，传输后激活接收电路
#define    JCMD_AUTOCOLL            0x0D
#define     JCMD_AUTHENT            0x0E        // MIFARE认证
#define     JCMD_SOFTRESET        0x0F        // 软件复位
//============= 位定义 ============================================================================
// JREG_COMMAND                     0x01        // 命令寄存器           	
#define     JBIT_RCVOFF            0x20        // 模拟部分开/关控制位
#define     JBIT_POWERDOWN        0x10        // PN511电源控制(软件掉电)

// JREG_COMMIEN                     0x02        // 中断使能寄存器       
#define     JBIT_IRQINV            0x80        // 设置中断脚输出模式

// JREG_DIVIEN                      0x03        // 中断使能寄存器       
#define     JBIT_IRQPUSHPULL        0x80        // 设置中断脚输出模式

// JREG_COMMIEN/JREG_COMMIRQ         0x02, 0x04	// 中断使能/请求        
#define     JBIT_TXI                0x40        // 发送中断使能/请求
#define     JBIT_RXI                0x20        // 接收中断使能/请求
#define     JBIT_IDLEI            0x10        // 空闲中断使能/请求
#define     JBIT_HIALERTI            0x08        // FIFO高中断使能/请求
#define     JBIT_LOALERTI            0x04        // FIFO低中断使能/请求
#define     JBIT_ERRI                0x02        // 错误中断使能/请求
#define     JBIT_TIMERI            0x01        // 定时器中断使能/请求

// JREG_DIVIEN/JREG_DIVIRQ           0x03, 0x05	// 中断使能/请求        
#define     JBIT_SIGINACTI        0x10        // Sigin信号中断使能/请求
#define     JBIT_MODE                0x08        // 模式中断使能/请求（PN512）
#define     JBIT_CRCI                0x04        // CRC中断使能/请求
#define     JBIT_RFON                0x02        // RFON中断使能/请求（PN512）
#define     JBIT_RFOFF            0x01        // RFOFF中断使能/请求（PN512）

// JREG_COMMIRQ/JREG_DIVIRQ         0x04, 0x05  // 中断设置             
#define     JBIT_SET                0x80        // 设置清除中断标志位

// Error Register 					0x06		// 错误寄存器
#define     JBIT_WRERR            0x80        // 写FIFO冲突错误
#define     JBIT_TEMPERR            0x40        // 芯片温度过热
#define     JBIT_BUFFEROVFL        0x10        // FIFO溢出错误
#define     JBIT_COLLERR            0x08        // 冲突位错误
#define     JBIT_CRCERR            0x04        // CRC校验错误
#define     JBIT_PARITYERR        0x02        // 奇偶校验错误
#define     JBIT_PROTERR            0x01        // 协议错误

// JREG_STATUS1                     0x07		// 状态寄存器1      		
#define     JBIT_RFFREQOK           0x80        // 指示RF的频率，若外部RF场的频率在12MHz~15MHz,
// 该位置1，否则置0
#define     JBIT_CRCOK              0x40        // CRC计算结果指示，若CRC计算为0，该位置位
#define     JBIT_CRCREADY           0x20        // CRC计算完毕，该位置位
#define     JBIT_IRQ                0x10        // 中断请求，有任意中断，该位置1
#define     JBIT_TRUNNUNG           0x08        //
#define     JBIT_EXRFON             0x04        // 外部RF场状态寄存器        
#define     JBIT_HIALERT            0x02        // FIFO高
#define     JBIT_LOALERT            0x01        // FIFO低

// Status 2 Register				0x08)
#define     JBIT_TEMPSENSOFF        0x80        // 温度传感器开关
#define     JBIT_I2CFORCEHS        0x40        // IIC高速模式
#define    JBIT_TARGETACTIVATED    0x10
#define     JBIT_CRYPTO1ON        0x08        // Crypto 加密模式

// FIFOLevel Register				0X0A
#define     JBIT_FLUSHBUFFER        0x80        // 1 清空FIFO

// ControlReg						0x0C		// 包含不同的控制位
#define     JBIT_TSTOPNOW            0x80        // 1 定时器立即停止
#define     JBIT_TSTARTNOW        0x40        // 1 定时器立即启动
#define        JBIT_WRNFCIDTOFIFO        0x20        // 1 产生10字节NFCID	PN51x
#define        JBIT_INITIATOR            0X10        // 1 卡模式				PN51x

// BitFramiReg						0x0D		// 面向位帧
#define     JBIT_STARTSEND        0x80        // 1 启动数据发送

// CollReg							0x0E		// RF接口检测到的冲突位
#define     JBIT_VALUESAFTERCOLL    0x80        // 0 冲突位后的所接收的数据将被清除

// Page1
// ModeReg							0x11		// 定义发送和接收模式（包括CRC初值定义）
#define     JBIT_MSBFIRST            0x80        // CRC高位在前 RC523
#define     JBIT_TXWAITRF            0x20
#define     JBIT_POLSIGIN            0x08        // SIGIN 极性


// TxModeReg						0x12		// 定义发送器波特率和模式
#define     JBIT_INVMOD            0x08        // 1 发送数据调制反向

// RxModeReg						0x13		// 定义接收器波特率和模式
#define     JBIT_RXNOERR            0x08        // 1 接收的位少于4位，将被忽略

// 定义 TxModeReg和RxModeReg共有的
#define     JBIT_106KBPS            0x00
#define     JBIT_212KBPS            0x10
#define     JBIT_424KBPS            0x20
#define     JBIT_848KBPS            0x30        // RC523

#define     JBIT_CRCEN            0x80        // CRC使能
#define     JBIT_ISO14443A        0x00        // 数据传输模式TypeA	RC523
#define     JBIT_ISO14443B        0x03        // 数据传输模式TypeB	RC523

// TxControlReg						0x14		// 天线驱动脚控制逻辑
#define     JBIT_INVTX2ON            0x80        // 1 若TX2使能，则TX2信号输出反向
#define     JBIT_INVTX1ON            0x40        // 1 若TX1使能，则TX1信号输出反向
#define     JBIT_INVTX2OFF        0x20        // 1 若TX2禁止，则TX2信号输出反向
#define     JBIT_INVTX1OFF        0x10        // 1 若TX1禁止，则TX1信号输出反向
#define     JBIT_TX2CW            0x08        // 1 TX2传输未调制的载波信号；
#define     JBIT_TX2RFEN            0x02        // 1 TX2传输调制的载波信号
#define     JBIT_TX1RFEN            0x01        // 1 TX1传输调制的载波信号

// JREG_TXAUTO                                  // 控制和设置天线驱动器 0x15		
#define     JBIT_AUTORFOFF          0x80        // RF场自动关闭模式(NFCIP-1模式)
#define     JBIT_FORCE100ASK        0x40        // 100%ASK调制模式设置(独立于ModGsPReg)
#define     JBIT_AUTOWAKEUP         0x20        // 软件掉电模式下自动唤醒
#define     JBIT_CAON               0x08        // 
#define     JBIT_INITIALRFON        0x04        // 
#define     JBIT_TX2RFAUTOEN        0x02        // TX2在外部RF场关闭后自动打开
#define     JBIT_TX1RFAUTOEN        0x01        // TX1在外部RF场关闭后自动打开

// DemodReg							0x19		// 解调器设置
#define     JBIT_FIXIQ            0x20        // 解调器通道选择

// TModeReg							0x2A		// 定时器模式设置
#define     JBIT_TAUTO            0x80        // 1 定时器自动启动和停止
#define     JBIT_TAUTORESTART        0x10        // 1 定时器重新计数；
// 0 计数器归0，并产生中断信号
//============= UART通信波特率设置 ================================================================
// BR_T0 = JREG_SERIALSPEED[7:5]
// BR_T1 = JREG_SERIALSPEED[4:0]
// BR_T0 = 0, BaudRate = 27.12MHz/(BR_T1+1)
// BR_T0 = 1, BaudRate = 27.12MHz/(BR_T1+33)/2^(BR_T0-1)	
#define        RCBR_7200                0xFA
#define        RCBR_9600                0xEB
#define        RCBR_14400                0xDA
#define        RCBR_19200                0xCB
#define        RCBR_38400                0xAB
#define        RCBR_57600                0x9A
#define        RCBR_115200                0x7A
#define        RCBR_128000                0x74
#define        RCBR_230400                0x5A
#define        RCBR_460800                0x3A
#define        RCBR_921600                0x1C
#define        RCBR_1228800            0x15
//============= 位屏蔽码 ========================================================================
// Command register             	0x01
#define     JMASK_COMMAND            0x0F        // JREG_COMMAND 命令屏蔽码

// Waterlevel register          	0x0B
#define     JMASK_WATERLEVEL        0x3F        // JREG_WATERLEVEL FIFO水平线屏蔽码

// Control register             	0x0C
#define     JMASK_RXBITS            0x07        // JREG_CONTROL 最后接收到的字节的有效位的数目屏蔽吗

// Mode register                	0x11
#define     JMASK_CRCPRESET        0x03        // JREG_MODE CRC预置值的选择屏蔽码

// TxMode register              	0x12, 0x13
#define     JMASK_SPEED            0x70        // JREG_RXMODE 发送/接收数据位数率屏蔽码

// TxSel register               	0x16
#define     JMASK_DRIVERSEL        0x30        // JREG_TXSEL TX1、TX2输出选择屏蔽码
#define     JMASK_SIGOUTSEL        0x0F        // JREG_TXSEL MFOUT信号源选择屏蔽码

// RxSel register               	0x17
#define     JMASK_UARTSEL            0xC0        // JREG_RXSEL 非接触式UART输入信号选择屏蔽码
#define     JMASK_RXWAIT            0x3F        // JREG_RXSEL 接收等待时间设置屏蔽码

// RxThreshold register         	0x18
#define     JMASK_MINLEVEL        0xF0        // JREG_RXTHRESHOLD 定义接收译码器输入的最小信号强度
#define     JMASK_COLLEVEL        0x07        // JREG_RXTHRESHOLD 定义冲突位信号的强度屏蔽码

// Demod register               	0x19
#define     JMASK_ADDIQ            0xC0        // JREG_DEMOD 接收过程中I/Q通道的选择屏蔽码
#define     JMASK_TAURCV            0x0C        // JREG_DEMOD 接收过程中内部PLL的时间常数屏蔽码
#define     JMASK_TAUSYNC            0x03        // JREG_DEMOD 突发过程中，内部PLL的时间常数屏蔽码

// RFCfg register               	0x26
#define     JMASK_RXGAIN            0x70        // JREG_RFCFG 接收信号电压增益因子屏蔽码

// GsN register                 	0x27
#define     JMASK_CWGSN            0xF0        // JREG_GSN N驱动器的电导屏蔽码
#define     JMASK_MODGSN            0x0F        // JREG_GSN N驱动器的调制系数屏蔽码

// CWGsP register               	0x28
#define     JMASK_CWGSP            0x3F        // JREG_CWGSP P驱动器的电导屏蔽码

// ModGsP register              	0x29
#define     JMASK_MODGSP            0x3F        // JREG_MODGSP P驱动器的调制系数屏蔽码

// TMode register               	0x2A
#define     JMASK_TGATED            0x60        // JREG_TMODE 定时器门工作模式屏蔽码
#define     JMASK_TPRESCALER_HI    0x0F        // JREG_TMODE 定时器高字节屏蔽码

#ifdef __cplusplus
}
#endif
//=================================================================================================
#endif              // __PN512REG_H


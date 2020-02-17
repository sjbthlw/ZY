/////////////////////////////////////////////////Copyright (c)///////////////////////////////////////////////
//                                        广州周立功单片机发展有限公司
//                                              研    究    所
//                                                金卡产品线
//
//                                            http://www.zlgmcu.com
//
//-------------- 文件信息------------------------------------------------------------------------------------
// 文 件 名:    StatusCode.h
// 文件描述:    状态代码
// 版    本:    V1.0 
// 创 建 人:    曾本森
// 创建日期:    2008.01.09
//===========================================================================================================
//-------------- 修改记录------------------------------------------------------------------------------------
// 修改内容:	将有符号数改为无符号数
// 当前版本:    
// 修 改 人:	曾本森
// 修改日期:	2009.06.24
// 注    意: 
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
#ifndef __STATUS_CODE_H
#define __STATUS_CODE_H

#ifdef __cplusplus
extern "C" {
#endif

#ifdef __cplusplus
}
#endif


//===========================================================================================================
#define STATUS_SUCCESS                  (0)        // 操作成功

#define STATUS_IO_TIMEOUT               0xFF    // 无应答错误
#define STATUS_CRC_ERROR                0xFE    // CRC校验错误
#define STATUS_PARITY_ERROR             0xFD    // 奇偶校验错误
#define STATUS_BITCOUNT_ERROR           0xFC    // 接收位计数器错误
#define STATUS_FRAMING_ERROR            0xFB    // 帧错误
#define STATUS_COLLISION_ERROR          0xFA    // 位冲突错误
#define STATUS_BUFFER_TOO_SMALL         0xF9    // 缓冲区过小
#define STATUS_ACCESS_DENIED            0xF8    // 写禁止
#define STATUS_BUFFER_OVERFLOW          0xF7    // BUF溢出错误
#define STATUS_PROTOCOL_ERROR           0xF6    // 通信协议有误
#define STATUS_ERROR_NY_IMPLEMENTED     0xF5    // 未执行
#define STATUS_FIFO_WRITE_ERROR         0xF4    // FIFO写错误
#define STATUS_USERBUFFER_FULL          0xF3    // 用户缓冲区满
#define STATUS_INVALID_PARAMETER        0xF2    // 无效参数
#define STATUS_UNSUPPORTED_PARAMETER    0xF1    // 不支持的参数
#define STATUS_UNSUPPORTED_COMMAND      0xF0    // 不支持的命令
#define STATUS_INTERFACE_ERROR          0xEF    // 主机接口错误
#define STATUS_INVALID_FORMAT           0xEE    // 无效格式
#define STATUS_INTERFACE_NOT_ENABLED    0xED    // 接口未激活
#define STATUS_AUTHENT_ERROR            0xEC    // 验证错误
#define STATUS_ACK_SUPPOSED             0xEB    // NACK
#define STATUS_BLOCKNR_NOT_EQUAL        0xEA    // 通信块错误

#define STATUS_TARGET_DEADLOCKED        0xE9    // 目标死锁
#define STATUS_TARGET_SET_TOX           0xE8    // 目标设置超时
#define STATUS_TARGET_RESET_TOX         0xE7    // 目标复位
#define STATUS_WRONG_UID_CHECKBYTE      0xE6    // 目标UID检测错误
#define STATUS_WRONG_HALT_FORMAT        0xE5    // 挂起格式错误

#define STATUS_ID_ALREADY_IN_USE        0xE4    // ID已被使用
#define STATUS_INSTANCE_ALREADY_IN_USE  0xE3    // INSTANCE 已被使用
#define STATUS_ID_NOT_IN_USE            0xE2    // 指定的ID不存在
#define STATUS_NO_ID_AVAILABLE          0xE1    // 无空闲ID可用

#define STATUS_OTHER_ERROR              0xE0    // 其他错误
#define STATUS_INSUFFICIENT_RESOURCES   0xDF    // 系统资源不足
#define STATUS_INVALID_DEVICE_STATE     0xDE    // 驱动错误
#define STATUS_TEMP_ERROR                0xDD    // 温度有误

#define STATUS_INIT_ERROR                0xDC    // 初始化错误
#define STATUS_NO_BITWISE_ANTICOLL        0xDB    // 不支持Bit帧防冲突
#define STATUS_SERNR_ERROR                0xDA    // 不支持的防碰撞等级
#define STATUS_NY_IMPLEMENTED            0xD9    // 不支持的命令
#define STATUS_ACCESS_TIMEOUT            0xD8    // 访问超时
#define STATUS_NO_RF_FIELD                0xD7    // 无RF场错误
#define STATUS_NO_TARGET                0xD5    // 无目标
#define STATUS_BCC_ERROR                0xD4    // BCC校验错误


// ISO14443 命令错误代码
#define STATUS_ATS_ERROR                0xCE    // ATS错误
#define STATUS_PPS_ERROR                0xCD    // PPSS错误
#define STATUS_BLOCK_FORMAT_ERROR       0xCC    // 分组帧格式错误
#define STATUS_CID_ERROR                0xCB    // CID错误
#define STATUS_PCB_ERROR                0xCA    // PCB错误
#define STATUS_NAK_ERROR                0xC9    // NAK错误
#define STATUS_LEN_ERROR                0xC8    // 长度错误
#define STATUS_SEND_ERROR                0xC7    // 发送错误
#define STATUS_APDU_ERROR                0xC6    // APDU命令错误
#define STATUS_PERORM_ERROR                0xC4    // 命令执行结果错误
#define STATUS_INVALID_VALUE            0xC3    // 值块格式错误

// PLUS卡错误代码
#define STATUS_PLUS_GENERAL_ERROR        0xBF    // 普通操作错误
#define STATUS_PLUS_LENGTH_ERROR        0xBC    // 长度错误
#define STATUS_PLUS_NOT_SATISFIED        0xBB    // 安全等级不足
#define STATUS_PLUS_NO_BNR                0xBA    // 不存在的块
#define STATUS_PLUS_BNR_INVALID            0xB9    // 无效的块地址
#define STATUS_PLUS_MAC_INVALID            0xB8    // 无效的MAC
#define STATUS_PLUS_CMD_OVERFLOW        0xB7    // 命令(数据)溢出
#define STATUS_PLUS_AUTH_ERROR            0xB6    // 验证错误

#define STATUS_PLUS_NOT_SUPPORT_VC        0xB5    // 不支持虚拟卡操作
#define STATUS_PLUS_VPC_TIMEOUT            0xB4    // 存在中继攻击

#define STATUS_PLUS_PCD_CAP_ERROR        0xAF    // PCD能力有误
//============= 通信类状态 ==================================================================================
#define STATUS_COMM_HEAD_ERR            0x01    // 帧头错误
#define STATUS_COMM_SEQ_ERR                0x02    // 包号错误
#define STATUS_COMM_LEN_ERR                0x03    // 信息长度错误
#define STATUS_COMM_EDC_ERR                0x04    // 帧尾校验错误
#define STATUS_COMM_RS_ERR                0x05    // 接收/发送器错误
#define STATUS_COMM_RD_ERR                0x06    // 接收的数据域有误
#define STATUS_COMM_SD_ERR                0x07    // 发送的数据域有误
#define STATUS_COMM_NO_DATA_ERR            0x08    // 无数据错误
//============= 读写器类状态 ================================================================================
#define STATUS_MEM_ACCESS_OVER            0x10    // 存储器访问越界
#define STATUS_MEM_WRITE_ERR            0x11    // 存储器写错误
#define STATUS_MEM_BANNED                0x12    // 禁止访问
#define STATUS_MEM_INVALID_PARAMETER    0x13    // 无效参数

#define STATUS_7816_OVERTIME_ERR        0x20    // 超时错
#define STATUS_7816_STARTE_ERR            0x21    // 起始位错
#define STATUS_7816_PARITY_ERR            0x22    // 校验错
#define STATUS_7816_STOP_ERR            0x23    // 停止位错
#define STATUS_7816_ATRE_ERR            0x24    // 复位应答错
#define STATUS_7816_PPS_ERR                0x25    // PPS错误
#define STATUS_7816_CARDNOE_ERR            0x26    // 未选择卡错误
#define STATUS_7816_NAD_ERR                0x27    // NAD错误
#define STATUS_7816_RCV_ERR                0x28    // 接收错误
#define STATUS_7816_RCV_LEN_ERR            0x29    // 接收长度错误
#define STATUS_7816_EDC_ERR                0x2A    // 校验字节有误
#define STATUS_7816_OTHER_ERR            0x2B    // 其他有误

// 汉王模块用错误代码
#define STATUS_HW_UNSUPPORTED_COMMAND    0x0B    // 不支持的命令
#define STATUS_HW_INVALID_PARAMETER        0x0C    // 无效参数
#define STATUS_HW_VAL_FORMAT_ERR        0x16    // 值块格式错误
//===========================================================================================================
#endif          // __STATUS_CODE_H


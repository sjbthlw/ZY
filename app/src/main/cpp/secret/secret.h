#ifndef __SECRET_H__
#define __SECRET_H__

#include "../config.h"

#ifdef __cplusplus
extern "C" {
#endif


//客户交易卡解密函数
void SCardSecret(u8 *bCardSerialID, u8 *bSCardSecret);

#ifdef __cplusplus
}
#endif

#endif


#ifndef __SECLIB_H__
#define __SECLIB_H__


#ifdef __cplusplus
extern "C" {
#endif

#include "config.h"


long Get_AuthenCodeUWallet(unsigned long dwCardNum,
                           unsigned short wClientNum,
                           u8 AgentNum,
                           long lMonCurr,
                           u8 Sector,
                           unsigned short wWalletSID);

unsigned long Get_AuthenCodeUCard(unsigned long dwCardNum,
                                  unsigned short wClientNum,
                                  long lClientSign,
                                  u8 AgentNum);

long Get_AuthenCodeOper(long lOptPWD, u8 *pBuffer, int BufLen);

void Get_SectKeyUCard(int Sector, int ClientNum, int AgentNum,
                      int isKeyA, unsigned char *pKey);

void Get_SectKeyUCard2X(unsigned short ClientNum, unsigned char *pKey);

void SECLIB_Get_AuthenCodeUWallet(u8 *pBuffer, u8 *pSeed);

void SECLIB_Get_AuthenCodeUCard(u8 *pBuffer, u8 *pSeed);

void SECLIB_CycleAdd(u8 *pBuffer, int Len);

void SECLIB_Get_SectKeyUCard(u8 *pBuffer, u8 *pSeed);

void SECLIB_Get_AuthenCodeOper(u8 *pBuffer);


#ifdef __cplusplus
}
#endif

#endif
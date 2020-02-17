
#include "SECLIB.h"
#include "config.h"

typedef union VarDWORD {
    struct {
        unsigned char Byte1, Byte2, Byte3, Byte4;
    } AsBytes;
    //unsigned char AsBytes[4];
    struct {
        unsigned short WORD1, WORD2;
    } AsWord;
    struct {
        short Short1, Short2;
    } AsShort;
    long AsInt32;
    unsigned long AsDWord;
} VarDWORD;

void SECLIB_CycleAdd(u8 *pBuffer, int Len) {
    int i;
    for (i = 0; i < Len - 1; ++i)
        pBuffer[i + 1] += pBuffer[i];
    pBuffer[0] += pBuffer[Len - 1];
}

void SECLIB_Get_AuthenCodeOper(u8 *pBuffer) {
    int i;
    pBuffer[0] ^= 0x47;
    pBuffer[1] ^= 0x55;
    pBuffer[2] ^= 0x27;
    pBuffer[3] ^= 0x13;

    for (i = 0; i < 3; ++i)    // Iterate
    {
        SECLIB_CycleAdd(pBuffer, 4);
        pBuffer[0] += (pBuffer[3] << 4);
        pBuffer[0] += (pBuffer[3] >> 4);
    }
    for (i = 0; i < 4; ++i)    // Iterate
    {
        SECLIB_CycleAdd(pBuffer, 8);
        pBuffer[0] &= 0xAA;
        pBuffer[0] += (pBuffer[7] << 3);
        pBuffer[0] += (pBuffer[7] >> 5);
    }
}

long Get_AuthenCodeOper(long lOptPWD, u8 *pBuffer, int BufLen) {
    if (BufLen >= 8) {
        VarDWORD vdw;
        vdw.AsInt32 = lOptPWD;
        pBuffer[0] = vdw.AsBytes.Byte1; //
        pBuffer[1] = vdw.AsBytes.Byte2; //(lngpw & 0x0000ff00) >> 8;
        pBuffer[2] = vdw.AsBytes.Byte3; //(lngpw & 0x00ff0000) >> 16;
        pBuffer[3] = vdw.AsBytes.Byte4; //(lngpw & 0xff000000) >> 24;
        //密码种子,//登录字种子0x1F58763A;
        pBuffer[4] = 0x3a;
        pBuffer[5] = 0x76;
        pBuffer[6] = 0x58;
        pBuffer[7] = 0x1f;
        SECLIB_Get_AuthenCodeOper(pBuffer);
        return 8;
    }
    return -1;
}

unsigned long Get_AuthenCodeUCard(unsigned long dwCardNum,
                                  unsigned short wClientNum,
                                  long lClientSign,
                                  u8 AgentNum) {
    VarDWORD vdw;
    u8 Buffer[16];
    u8 Seed[4];

    Buffer[0] = 0;

    vdw.AsDWord = dwCardNum;
    Buffer[1] = vdw.AsBytes.Byte1;
    Buffer[2] = vdw.AsBytes.Byte2;
    Buffer[3] = vdw.AsBytes.Byte3;
    Buffer[4] = vdw.AsBytes.Byte4;

    Buffer[5] = AgentNum;

    vdw.AsInt32 = wClientNum;
    Buffer[6] = vdw.AsBytes.Byte1;
    Buffer[7] = vdw.AsBytes.Byte2;

    vdw.AsInt32 = lClientSign;
    Buffer[8] = vdw.AsBytes.Byte1;
    Buffer[9] = vdw.AsBytes.Byte2;
    Buffer[10] = vdw.AsBytes.Byte3;
    Buffer[11] = vdw.AsBytes.Byte4;

    Buffer[12] = 0;
    Buffer[13] = 0;
    Buffer[14] = 0;
    Buffer[15] = 0;

    Seed[0] = 0x97;
    Seed[1] = 0x18;
    Seed[2] = 0x45;
    Seed[3] = 0x63;

    SECLIB_Get_AuthenCodeUCard(Buffer, Seed);

    vdw.AsBytes.Byte1 = Buffer[0];
    vdw.AsBytes.Byte2 = Buffer[1];
    vdw.AsBytes.Byte3 = Buffer[2];
    vdw.AsBytes.Byte4 = Buffer[3];
    return vdw.AsDWord;
}

void SECLIB_Get_AuthenCodeUCard(u8 *pBuffer, u8 *pSeed) {
    pBuffer[0] ^= 0xB7;
    pBuffer[1] ^= 0x19;
    pBuffer[2] ^= 0xA2;
    pBuffer[3] ^= 0x65;

    pBuffer[0] ^= pSeed[0];
    pBuffer[1] ^= pSeed[1];
    pBuffer[2] ^= pSeed[2];
    pBuffer[3] ^= pSeed[3];

    pBuffer[0] ^= pBuffer[12];
    pBuffer[1] ^= pBuffer[13];
    pBuffer[2] ^= pBuffer[14];
    pBuffer[3] ^= pBuffer[15];

    SECLIB_CycleAdd(pBuffer, 4);
    pBuffer[0] += (pBuffer[3] << 4);
    pBuffer[0] += (pBuffer[3] >> 4);

    pBuffer[0] += pBuffer[7];
    pBuffer[1] += pBuffer[4];
    pBuffer[2] += pBuffer[5];
    pBuffer[3] += pBuffer[6];

    SECLIB_CycleAdd(pBuffer, 4);
    pBuffer[0] &= 0x7E;
    pBuffer[0] += (pBuffer[3] << 4);
    pBuffer[0] += (pBuffer[3] >> 4);

    pBuffer[0] += pBuffer[10];
    pBuffer[1] += pBuffer[11];
    pBuffer[2] += pBuffer[8];
    pBuffer[3] += pBuffer[9];

    int i;
    for (i = 0; i < 4; ++i)    // Iterate
    {
        SECLIB_CycleAdd(pBuffer, 4);
        pBuffer[0] &= 0xE7;
        pBuffer[0] += (pBuffer[3] << 4);
        pBuffer[0] += (pBuffer[3] >> 4);
    }
}

long Get_AuthenCodeUWallet(unsigned long dwCardNum,
                           unsigned short wClientNum,
                           u8 AgentNum, long lMonCurr,
                           u8 Sector, unsigned short wWalletSID) {
    VarDWORD vdw;
    u8 Buffer[16];
    u8 Seed[4];

    Buffer[0] = 0;

    vdw.AsDWord = dwCardNum;
    Buffer[1] = vdw.AsBytes.Byte1;
    Buffer[2] = vdw.AsBytes.Byte2;
    Buffer[3] = vdw.AsBytes.Byte3;
    Buffer[4] = vdw.AsBytes.Byte4;

    Buffer[5] = AgentNum;

    vdw.AsInt32 = wClientNum;
    Buffer[6] = vdw.AsBytes.Byte1;
    Buffer[7] = vdw.AsBytes.Byte2;

    Buffer[8] = Sector;

    vdw.AsInt32 = lMonCurr;
    Buffer[9] = vdw.AsBytes.Byte1;
    Buffer[10] = vdw.AsBytes.Byte2;
    Buffer[11] = vdw.AsBytes.Byte3;
    Buffer[12] = vdw.AsBytes.Byte4;

    vdw.AsInt32 = wWalletSID;
    Buffer[13] = vdw.AsBytes.Byte1;
    Buffer[14] = vdw.AsBytes.Byte2;
    Buffer[15] = 0;//SpinEdit_Pos.Value;

    Seed[0] = 0x97;
    Seed[1] = 0x18;
    Seed[2] = 0x45;
    Seed[3] = 0x63;

    SECLIB_Get_AuthenCodeUWallet(Buffer, Seed);

    vdw.AsBytes.Byte1 = Buffer[0];
    vdw.AsBytes.Byte2 = Buffer[1];
    vdw.AsBytes.Byte3 = Buffer[2];
    vdw.AsBytes.Byte4 = 0;//Buffer[3];
    return vdw.AsInt32;     //3 字节
}

void SECLIB_Get_AuthenCodeUWallet(u8 *pBuffer, u8 *pSeed) {
    pBuffer[0] ^= 0x7A;
    pBuffer[1] ^= 0xB9;
    pBuffer[2] ^= 0x8C;
    pBuffer[3] ^= 0xF3;

    pBuffer[0] ^= pSeed[0];
    pBuffer[1] ^= pSeed[1];
    pBuffer[2] ^= pSeed[2];
    pBuffer[3] ^= pSeed[3];

    pBuffer[0] ^= pBuffer[12];
    pBuffer[1] ^= pBuffer[13];
    pBuffer[2] ^= pBuffer[14];
    pBuffer[3] ^= pBuffer[15];

    SECLIB_CycleAdd(pBuffer, 4);
    pBuffer[0] += (pBuffer[3] << 4);
    pBuffer[0] += (pBuffer[3] >> 4);

    pBuffer[0] += pBuffer[7];
    pBuffer[1] += pBuffer[4];
    pBuffer[2] += pBuffer[5];
    pBuffer[3] += pBuffer[6];

    SECLIB_CycleAdd(pBuffer, 4);
    pBuffer[0] &= 0x5E;
    pBuffer[0] += (pBuffer[3] << 4);
    pBuffer[0] += (pBuffer[3] >> 4);

    pBuffer[0] += pBuffer[10];
    pBuffer[1] += pBuffer[11];
    pBuffer[2] += pBuffer[8];
    pBuffer[3] += pBuffer[9];

    int i;
    for (i = 0; i < 4; ++i)    // Iterate
    {
        SECLIB_CycleAdd(pBuffer, 4);
        pBuffer[0] &= 0xED;
        pBuffer[0] += (pBuffer[3] << 4);
        pBuffer[0] += (pBuffer[3] >> 4);
    }
}

void SECLIB_Get_SectKeyUCard(u8 *pBuffer, u8 *pSeed) {
    //register u8 *Buffer;
    //register u8  I;

    pBuffer[0] ^= 0xAF;
    pBuffer[1] ^= 0x17;
    pBuffer[2] ^= 0x92;
    pBuffer[3] ^= 0x68;

    pBuffer[0] ^= pSeed[0];
    pBuffer[1] ^= pSeed[1];
    pBuffer[2] ^= pSeed[2];
    pBuffer[3] ^= pSeed[3];

    int i, j;
    for (i = 0; i < 2; ++i) {
        for (j = 0; j < 3; ++j) {
            pBuffer[j + 1] += pBuffer[j];
        }
        pBuffer[0] &= 0xFE;
        pBuffer[0] += (pBuffer[3] << 5);
        pBuffer[0] += (pBuffer[3] >> 3);
    }

    pBuffer[0] ^= pSeed[8];
    pBuffer[1] ^= pSeed[9];
    pBuffer[2] ^= pSeed[10];
    pBuffer[3] ^= pSeed[11];

    pBuffer[0] += pBuffer[8];
    pBuffer[1] += pBuffer[9];
    pBuffer[2] += pBuffer[10];
    pBuffer[3] += pBuffer[11];

    for (i = 0; i < 3; ++i) {
        for (j = 0; j < 3; ++j) {
            pBuffer[j + 1] += pBuffer[j];
        }
        pBuffer[0] += (pBuffer[3] << 3);
        pBuffer[0] += (pBuffer[3] >> 5);
    }

    pBuffer[4] ^= pSeed[7];
    pBuffer[5] ^= pSeed[6];
    pBuffer[6] ^= pSeed[5];
    pBuffer[7] ^= pSeed[4];

    for (i = 0; i < 3; ++i) {
        for (j = 0; j < 7; ++j) {
            pBuffer[j + 1] += pBuffer[j];
        }
        pBuffer[0] += (pBuffer[7] << 4);
        pBuffer[0] += (pBuffer[7] >> 4);
    }

    pBuffer[4] ^= pSeed[12];
    pBuffer[5] ^= pSeed[13];
    pBuffer[6] ^= pSeed[14];
    pBuffer[7] ^= pSeed[15];

    for (i = 0; i < 2; ++i) {
        for (j = 0; j < 7; ++j) {
            pBuffer[j + 1] += pBuffer[j];
        }
        pBuffer[0] &= 0xF7;
        pBuffer[0] += (pBuffer[7] << 4);
        pBuffer[0] += (pBuffer[7] >> 4);
    }
}

//生成卡片密钥
//参数 ：扇区号[0~15]，客户号,代理号,A密钥
void Get_SectKeyUCard(int Sector, int ClientNum, int AgentNum,
                      int isKeyA, unsigned char *pKey) {
    u8 aBytes[16];
    aBytes[0] = 0;
    aBytes[1] = AgentNum;
    aBytes[2] = (u8) ClientNum;
    aBytes[3] = ClientNum >> 8;
    aBytes[4] = Sector;
    if (isKeyA)
        aBytes[5] = 0; //AB
    else
        aBytes[5] = 1;
    aBytes[6] = 0;
    aBytes[7] = 0;
    aBytes[8] = 0;
    aBytes[9] = 0;
    aBytes[10] = 0;
    aBytes[11] = 0;

    aBytes[12] = 0;
    aBytes[13] = 0;
    aBytes[14] = 0;
    aBytes[15] = 0;

    u8 CardMakeSeed_User[] = {
            0x13, 0x57, 0x86, 0x42, 0x21, 0x34, 0x58, 0x65,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    SECLIB_Get_SectKeyUCard(aBytes, CardMakeSeed_User);
    // sprintf(pKey, "%02X%02X%02X%02X%02X%02X", aBytes[0], aBytes[1], aBytes[2], aBytes[3], aBytes[4], aBytes[5]);
    memcpy(pKey, aBytes, 6);
}

void Get_SectKeyUCard2X(unsigned short ClientNum, unsigned char *pKey) {
/*	VarDWORD aVarDWord;
	aVarDWord.AsInt32 = ClientNum;
	sprintf(pKey, "A5130312%02X%02X", aVarDWord.AsBytes.Byte2, aVarDWord.AsBytes.Byte1);*/
    pKey[0] = 0xA5;
    pKey[1] = 0x13;
    pKey[2] = 0x03;
    pKey[3] = 0x12;
    pKey[4] = ClientNum >> 8;
    pKey[5] = (u8) ClientNum;
}

#include "SecLIB_Define.h"
#include "SecLib_Seeds.h"
#include "SecLIB_POS.h"


TSECLIB_BUFFER SECLIB_Buffer;


#define  aRandomNum  SECLIB_Buffer.DWords[0]
#define  aRandomSeed SECLIB_Buffer.Words[2]


#define Authen_Seed SECLIB_Buffer.CardAuthen.CalcSeed
#define Buffer      SECLIB_Buffer.Work1.Bytes
#define I  SECLIB_Buffer.Work.i


#define I  SECLIB_Buffer.Work.i


#define Buffer    SECLIB_Buffer.Work1.Bytes
#define I  SECLIB_Buffer.Work.i
#define J  SECLIB_Buffer.Work.j

//密钥生成函数
//In:8字节数据+4字节生成种子
//Out:8字节生成数据
void SECLIB_Get_SectKeySCard() {
    Buffer[0] = Buffer[0] xor 0x33;
    Buffer[1] = Buffer[1] xor 0x4E;
    Buffer[2] = Buffer[2] xor 0x27;
    Buffer[3] = Buffer[3] xor 0x56;

    Buffer[0] = Buffer[0] xor Buffer[16 + 0];
    Buffer[1] = Buffer[1] xor Buffer[16 + 1];
    Buffer[2] = Buffer[2] xor Buffer[16 + 2];
    Buffer[3] = Buffer[3] xor Buffer[16 + 3];

    Buffer[0] = Buffer[0] xor Buffer[12 + 0];
    Buffer[1] = Buffer[1] xor Buffer[12 + 1];
    Buffer[2] = Buffer[2] xor Buffer[12 + 2];
    Buffer[3] = Buffer[3] xor Buffer[12 + 3];

    for (I = 0; I < 7; I++) {
        for (J = 0; J < 4 - 1; J++) {
            Buffer[J + 1] += Buffer[J];
        };
        Buffer[0] = Buffer[0] band 0x9B;
        Buffer[0] += (Buffer[4 - 1] shl 4);
        Buffer[0] += (Buffer[4 - 1] shr 4);
    };

    Buffer[4] ^= Buffer[8 + 3];
    Buffer[5] ^= Buffer[8 + 2];
    Buffer[6] ^= Buffer[8 + 1];
    Buffer[7] ^= Buffer[8 + 0];

    for (I = 0; I < 7; I++) {
        for (J = 0; J < 8 - 1; J++) {
            Buffer[J + 1] += Buffer[J];
        };
        Buffer[0] = Buffer[0] band 0xB9;
        Buffer[0] += (Buffer[8 - 1] shl 4);
        Buffer[0] += (Buffer[8 - 1] shr 4);
    };

}


#define Buffer    SECLIB_Buffer.Work1.Bytes
#define I  SECLIB_Buffer.Work.i
#define J  SECLIB_Buffer.Work.j


#define delta 0x9E3779B9
#define Cipher_n 16
#define Cipher_logn 4

#define c CoreCipherSeed_DWordL
#define d CoreCipherSeed_DWordH
#define y SECLIB_Buffer.Work2.cy
#define z SECLIB_Buffer.Work2.cz
#define a SECLIB_Buffer.Work2.ca
#define b SECLIB_Buffer.Work2.cb
#define sum  SECLIB_Buffer.Work2.ctmp0
#define tmp1 SECLIB_Buffer.Work2.ctmp1
#define tmp2  SECLIB_Buffer.Work2.ctmp2
#define tmp3  SECLIB_Buffer.Work2.ctmp3

#define n  SECLIB_Buffer.Work1.i

//**************************************************************
#define i  SECLIB_Buffer.Work.j

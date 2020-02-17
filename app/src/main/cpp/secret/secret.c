#include "secret.h"
#include "SecLIB_POS.h"
#include "../config.h"

//客户设密卡设用例程
void SCardSecret(u8 *bCardSerialID, u8 *bSCardSecret) {
    unsigned char InCardNo[4];
    unsigned char InSeed[4];
    u8 i;

    memcpy(InCardNo, bCardSerialID, 4);

    //读基础数据
    //读设备初始卡数据
    //ReadPCardData(bReadContext);

    //设密卡种子
    InSeed[0] = 0x37;
    InSeed[1] = 0x65;
    InSeed[2] = 0x88;
    InSeed[3] = 0x29;

    //设密卡密钥生成
    //准备数据
    //In Params
    SECLIB_Buffer.Params.InOut[0] = 0x00;
    SECLIB_Buffer.Params.InOut[1] = 0x01;
    SECLIB_Buffer.Params.InOut[2] = 0x0f;
    SECLIB_Buffer.Params.InOut[3] = 0x27;
    for (i = 0; i < 4; i++)
        SECLIB_Buffer.Params.InOut[i + 4] = InCardNo[i];
    SECLIB_Buffer.Params.InOut[8] = 0x00;
    SECLIB_Buffer.Params.InOut[9] = 0x00; //A=0 B=1
    for (i = 0; i < 2 + 4; i++)
        SECLIB_Buffer.Params.InOut[i + 10] = 0x00;
    //Seed
    for (i = 0; i < 4; i++)
        SECLIB_Buffer.Params.Seed[i] = InSeed[i];

    SECLIB_Get_SectKeySCard(); //客户设密卡密码生成
    //BE5557534F24

    //取出数据
    for (i = 0; i < 6; i++) {
        bSCardSecret[i] = SECLIB_Buffer.Params.InOut[i];
    }
}

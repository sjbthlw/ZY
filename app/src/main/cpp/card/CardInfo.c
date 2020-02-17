
#include "../config.h"

#include "CardInfo.h"


//设置卡片密钥
void SetCardKey(u8 bUCardAuthKeyA[32][6], u8 bUCardAuthKeyB[32][6]) {
    u8 i;

    for (i = 0; i < 32; i++) {
        //用户交易卡密钥A
        memcpy(s_UCardAuthKeyA[i], bUCardAuthKeyA[i], 6);
        //用户交易卡密钥B
        memcpy(s_UCardAuthKeyB[i], bUCardAuthKeyB[i], 6);
    }
}

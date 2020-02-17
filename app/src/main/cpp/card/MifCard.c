/*******************************************************************************
* Copyright 2015, 浙江正元智慧科技股份有限公司
* All right reserved
*
* 文件名称：MifCard.c
*
* 文件标识：MifCard.c
* 摘    要：Mifare卡片函数集合
*
*
* 当前版本：V1.0
* 作    者：wcp
* 完成日期：2015/12/14
* 编译环境：D:\Program Files (x86)\IAR Systems\Embedded Workbench 6.5\arm
*
* 历史信息：
*******************************************************************************/

#include "../config.h"
#include <android/log.h>
#include "../bsp/public.h"
#include "../bsp/pn512.h"
#include "MifCard.h"
#include "CardInfo.h"

u8 s_UCardAuthKeyA[32][6]; //用户交易卡密钥A
u8 s_UCardAuthKeyB[32][6]; //用户交易卡密钥B

//微秒级 延时
static void Sleepus(int us) {
    struct timeval delay;
    delay.tv_sec = 0;
    delay.tv_usec = us; //  us
    select(0, NULL, NULL, NULL, &delay);
}

//读卡片属性
u8 ReadCardAttrib(u8 *cCardSID, u8 *SAK) {
    u8 cResult;
    ACTIVEPARAA pActiParaA;

    Card_RfReset(5, 5);
    Card_Halt();
    Sleepus(500);
    cResult = PiccActivate(1, ISO14443_3A_REQALL, &pActiParaA);
    if (cResult != 0) {
        return 2;
    }
//    LOGD("card SAK:%02x\n",pActiParaA.SAK);
//    LOGD("card ATQ:%02x%02x\n",pActiParaA.ATQ[0],pActiParaA.ATQ[1]);
//    LOGD("card number:%02x%02x%02x%02x\n",
//    pActiParaA.UID[3],pActiParaA.UID[2],pActiParaA.UID[1],pActiParaA.UID[0]);

    *SAK = pActiParaA.SAK;
    memcpy(cCardSID, pActiParaA.UID, 4);

    return 0;
}

//读卡号A
u8 ReadCardSerNum(u8 *cCardSID) {
    u8 cResult;
    ACTIVEPARAA pActiParaA;

    Card_RfReset(5, 5);
    Card_Halt();
    //Sleepus(1000);
    cResult = PiccActivate(1, ISO14443_3A_REQALL, &pActiParaA);
    if (cResult != 0) {
        return 2;
    }
    memcpy(cCardSID, pActiParaA.UID, 4);
    return 0;
}

//读卡号A
u8 ReadCardSerID(u8 *cCardSID) {
    u8 i;
    u8 cResult=-1;

    for(i=0;i<3;i++)
    {
        cResult=ReadCardSerNum(cCardSID);
        if(cResult==0)
            break;
        else
            LOGD("ReadCardSerID失败:%d", i);
    }
    return cResult;
}

//读卡片数据
u8 ReadMifareBlock(u8 cBlockID, u8 *cCardSID, u8 *cCardContext) {
    u8 cSectorID;
    u8 cResult;
    u8 cCardSerialID[8]={0};
    u8 cKey[8]={0};

    LOGD("块号:%d", cBlockID);
    cSectorID = cBlockID / 4;

    cResult = ReadCardSerID(cCardSerialID);
    if (cResult == 0) {
        cResult = memcmp(cCardSerialID, cCardSID, 4);
        if (cResult != 0) {
            LOGD("Read Card ERR 1");
            return 61;
        }
    } else {
        LOGD("Read Card ERR 0");
        return 61;
    }

    //密钥
    memcpy(cKey, s_UCardAuthKeyB[cSectorID], 6);

    // 1、下载密钥
    cResult = Card_LoadKey(cKey);
    if (cResult != 0) {
        //LOGD("LoadKey %d",cResult);
        return 61;
    }
    // 2、认证密钥
    cResult = Card_Authen(cCardSerialID, cSectorID, 1);
    if (cResult != 0) {
        //LOGD("Authen %d",cResult);
        return 61;
    }
    // 3、读卡片数据
    cResult = Card_Read(cBlockID, cCardContext);
    if (cResult != 0) {
        //LOGD("Read %d",cResult);
        return 61;
    }
    return 0;
}

//写卡片数据
u8 WriteMifareBlock(u8 cBlockID, u8 *cCardSID, u8 *cCardContext) {
    u8 cSectorID;
    u8 cResult;
    u8 cCardContextTemp[16]={0};
    u8 cCardSerialID[8]={0};
    u8 cKey[8]={0};

    LOGD("写卡片块号:%d", cBlockID);
    cSectorID = cBlockID / 4;

    cResult = ReadCardSerID(cCardSerialID);
    if (cResult == 0) {
        cResult = memcmp(cCardSerialID, cCardSID, 4);
        if (cResult != 0) {
            LOGD("Read Card ERR 1");
            return 61;
        }
    } else {
        LOGD("Read Card ERR 0");
        return 61;
    }

    //密钥
    memcpy(cKey, s_UCardAuthKeyB[cSectorID], 6);
    // 1、下载密钥
    cResult = Card_LoadKey(cKey);
    if (cResult != 0) {
        //LOGD("LoadKey %d",cResult);
        return 61;
    }
    // 2、认证密钥
    cResult = Card_Authen(cCardSerialID, cSectorID, 1);
    if (cResult != 0) {
        //LOGD("Authen %d",cResult);
        return 61;
    }
    // 3、写卡片数据
    cResult = Card_Write(cBlockID, cCardContext);
    if (cResult != 0) {
        //LOGD("Write %d",cResult);
        return 62;
    }
    // 4、读卡片数据
    cResult = Card_Read(cBlockID, cCardContextTemp);
    if (cResult != 0) {
        //LOGD("Read %d",cResult);
        return 61;
    }
    // 5、比较读写数据是否一致
    cResult = memcmp(cCardContext, cCardContextTemp, 16);
    if (cResult != 0) {
        //LOGD("读写数据不一致");
        return 61;
    }
    return 0;
}

//写卡片数据(无认证)
u8 WriteMifareBlockNOAuthen(u8 cBlockID, u8 *cCardContext) {
    u8 cSectorID;
    u8 cResult;
    u8 cCardContextTemp[16]={0};

    LOGD("写卡片块号:%d", cBlockID);
    cSectorID = cBlockID / 4;

    // 3、写卡片数据
    cResult = Card_Write(cBlockID, cCardContext);
    if (cResult != 0) {
        //LOGD("Write %d",cResult);
        return 62;
    }

    // 4、读卡片数据
    cResult = Card_Read(cBlockID, cCardContextTemp);
    if (cResult != 0) {
        //LOGD("Read %d",cResult);
        return 61;
    }
    // 5、比较读写数据是否一致
    cResult = memcmp(cCardContext, cCardContextTemp, 16);
    if (cResult != 0) {
        //LOGD("读写数据不一致");
        return 61;
    }

    return 0;
}

//读Mifare卡片扇区数据
u8 ReadMifareSector(u8 cSectorID, u8 *cCardSID, u8 cCardContext[][16]) {
    u8 i;
    u8 cResult;
    u8 cCardSerialID[8]={0};
    u8 cKey[8]={0};

    LOGD("扇区号:%d", cSectorID);
    cResult = ReadCardSerID(cCardSerialID);
    if (cResult == 0) {
        cResult = memcmp(cCardSerialID, cCardSID, 4);
        if (cResult != 0) {
            LOGD("Read Card ERR 1");
            return 61;
        }
    } else {
        LOGD("Read Card ERR 0");
        return 61;
    }
    //密钥
    memcpy(cKey, s_UCardAuthKeyB[cSectorID], 6);
    // 1、下载密钥
    cResult = Card_LoadKey(cKey);
    if (cResult != 0) {
        LOGD("LoadKey %d", cResult);
        return 61;
    }
    // 2、认证密钥
    cResult = Card_Authen(cCardSerialID, cSectorID, 1);
    if (cResult != 0) {
        LOGD("Authen %d", cResult);
        return 61;
    }
    // 3、读卡片数据
    for (i = 0; i < 3; i++) {
        cResult = Card_Read(cSectorID * 4 + i, cCardContext[i]);
        if (cResult != 0) {
            LOGD("Read %d", cResult);
            return 61;
        }
    }
    return 0;
}

//读Mifare卡片钱包扇区数据
u8 ReadMifareBurseSector(u8 *cCardSID, u8 cBlockID, u8 *cCardContext, u8 cBlockBakID,
                         u8 *cCardBakContext) {
    u8 i;
    u8 cResult;
    u8 cSectorID;
    u8 cCardSerialID[8]={0};
    u8 cKey[8]={0};

    memset(cCardSerialID, 0, sizeof(cCardSerialID));
    memset(cKey, 0, sizeof(cKey));

    cSectorID = cBlockID / 4;
    LOGD("扇区号:%d", cSectorID);
    cResult = ReadCardSerID(cCardSerialID);
    if (cResult == 0) {
        cResult = memcmp(cCardSerialID, cCardSID, 4);
        if (cResult != 0) {
            LOGD("Read Card ERR 1");
            return 61;
        }
    } else {
        LOGD("Read Card ERR 0");
        return 61;
    }

    //密钥
    memcpy(cKey, s_UCardAuthKeyB[cSectorID], 6);

    // 1、下载密钥
    cResult = Card_LoadKey(cKey);
    if (cResult != 0) {
        LOGD("LoadKey %d", cResult);
        return 61;
    }
    // 2、认证密钥
    cResult = Card_Authen(cCardSerialID, cSectorID, 1);
    if (cResult != 0) {
        LOGD("Authen %d", cResult);
        return 61;
    }
    // 3、读卡片数据
    cResult = Card_Read(cBlockID, cCardContext);
    if (cResult != 0) {
        LOGD("Read %d", cResult);
        return 61;
    }

    // 3、读卡片数据
    cResult = Card_Read(cBlockBakID, cCardBakContext);
    if (cResult != 0) {
        LOGD("Read %d", cResult);
        return 61;
    }
    return 0;
}

//写Mifare卡片扇区数据
u8 WriteMifareSector(u8 cSectorID, u8 *cCardSID, u8 cCardContext[][16]) {
    u8 i;
    u8 cResult;
    u8 cCardContextTemp[16]={0};
    u8 cCardSerialID[8]={0};
    u8 cKey[8]={0};

    LOGD("扇区号:%d", cSectorID);
    cResult = ReadCardSerID(cCardSerialID);
    if (cResult == 0) {
        cResult = memcmp(cCardSerialID, cCardSID, 4);
        if (cResult != 0) {
            LOGD("Read Card ERR 1");
            return 61;
        }
    } else {
        LOGD("Read Card ERR 0");
        return 61;
    }

    //密钥
    memcpy(cKey, s_UCardAuthKeyB[cSectorID], 6);

    // 1、下载密钥
    cResult = Card_LoadKey(cKey);
    if (cResult != 0) {
        LOGD("LoadKey %d", cResult);
        return 61;
    }
    // 2、认证密钥
    cResult = Card_Authen(cCardSerialID, cSectorID, 1);
    if (cResult != 0) {
        LOGD("Authen %d", cResult);
        return 61;
    }

    for (i = 0; i < 3; i++) {
        //第0扇区的第0块不允许写
        if ((cSectorID == 0) && (i == 0)) {
            LOGD("cSectorID: %d-%d no write", cSectorID, i);
            continue;
        }
        LOGD("cSectorID: %d-%d ", cSectorID, i);

        memset(cCardContextTemp, 0, sizeof(cCardContextTemp));
        cResult = memcmp(cCardContext[i], cCardContextTemp, sizeof(cCardContextTemp));
        if (cResult == 0) {
            LOGD("写数据相同，跳过：%d", cResult);
            continue;
        }

        // 3、写卡片数据
        cResult = Card_Write(cSectorID * 4 + i, cCardContext[i]);
        if (cResult != 0) {
            LOGD("Write %d", cResult);
            return 62;
        }
        // 4、读卡片数据
        cResult = Card_Read(cSectorID * 4 + i, cCardContextTemp);
        if (cResult != 0) {
            LOGD("Read %d", cResult);
            return 61;
        }
        // 5、比较读写数据是否一致
        cResult = memcmp(cCardContext[i], cCardContextTemp, 16);
        if (cResult != 0) {
            LOGD("读写数据不一致");
            return 61;
        }
    }
    return 0;
}

//写Mifare卡片钱包扇区数据
u8 WriteMifareBurseSector(u8 *cCardSID, u8 cBlockID, u8 *cCardContext, u8 cBlockBakID,
                          u8 *cCardBakContext) {
    //u8 i;
    u8 cResult;
    u8 cSectorID;
    u8 cCardContextTemp[16]={0};
    u8 cCardSerialID[8]={0};
    u8 cKey[8]={0};

    cSectorID = cBlockID / 4;
    LOGD("扇区号:%d", cSectorID);

    cResult = ReadCardSerID(cCardSerialID);
    if (cResult == 0) {
        cResult = memcmp(cCardSerialID, cCardSID, 4);
        if (cResult != 0) {
            LOGD("Read Card ERR 1");
            return 61;
        }
    } else {
        LOGD("Read Card ERR 0");
        return 61;
    }

    //密钥
    memcpy(cKey, s_UCardAuthKeyB[cSectorID], 6);

    // 1、下载密钥
    cResult = Card_LoadKey(cKey);
    if (cResult != 0) {
        LOGD("LoadKey %d", cResult);
        return 61;
    }
    // 2、认证密钥
    cResult = Card_Authen(cCardSerialID, cSectorID, 1);
    if (cResult != 0) {
        LOGD("Authen %d", cResult);
        return 61;
    }

    // 3、写卡片数据
    cResult = Card_Write(cBlockID, cCardContext);
    if (cResult != 0) {
        LOGD("Write %d", cResult);
        return 61;
    }
    // 4、读卡片数据
    memset(cCardContextTemp, 0, sizeof(cCardContextTemp));
    cResult = Card_Read(cBlockID, cCardContextTemp);
    if (cResult != 0) {
        LOGD("Read %d", cResult);
        return 61;
    }
    // 5、比较读写数据是否一致
    cResult = memcmp(cCardContext, cCardContextTemp, 16);
    if (cResult != 0) {
        LOGD("读写数据不一致");
        return 61;
    }

    //钱包副本
    // 3、写卡片数据
    cResult = Card_Write(cBlockBakID, cCardBakContext);
    if (cResult != 0) {
        LOGD("Write %d", cResult);
        return 71;
    }
    // 4、读卡片数据
    memset(cCardContextTemp, 0, sizeof(cCardContextTemp));
    cResult = Card_Read(cBlockBakID, cCardContextTemp);
    if (cResult != 0) {
        LOGD("Read %d", cResult);
        return 71;
    }
    // 5、比较读写数据是否一致
    cResult = memcmp(cCardBakContext, cCardContextTemp, 16);
    if (cResult != 0) {
        LOGD("读写数据不一致");
        return 71;
    }
    return 0;
}

//读Mifare卡片扇区数据(固定密钥a0-a5)
u8 ReadMifareOpenSector(u8 cSectorID, u8 *cCardSID, u8 cCardContext[][16]) {
    u8 i;
    u8 cResult;
    u8 cCardSerialID[8]={0};
    u8 cKey[8]={0};

    LOGD("扇区号:%d", cSectorID);

    cResult = ReadCardSerID(cCardSerialID);
    if (cResult == 0) {
        cResult = memcmp(cCardSerialID, cCardSID, 4);
        if (cResult != 0) {
            LOGD("Read Card ERR 1");
            return 63;
        }
    } else {
        LOGD("Read Card ERR 0");
        return 63;
    }
//    cKey[0]=0xa0;cKey[1]=0xa1;cKey[2]=0xa2;
//    cKey[3]=0xa3;cKey[4]=0xa4;cKey[5]=0xa5;
    //密钥
    memcpy(cKey, s_UCardAuthKeyB[cSectorID], 6);
    // 1、下载密钥
    cResult = Card_LoadKey(cKey);
    if (cResult != 0) {
        LOGD("LoadKey %d", cResult);
        return 61;
    }
    // 2、认证密钥
    cResult = Card_Authen(cCardSerialID, cSectorID, 1);
    if (cResult != 0) {
        LOGD("Authen %d", cResult);
        return 61;
    }
    // 3、读卡片数据
    for (i = 0; i < 3; i++) {
        cResult = Card_Read(cSectorID * 4 + i, cCardContext[i]);
        if (cResult != 0) {
            LOGD("Read %d", cResult);
            return 61;
        }
    }
    return 0;
}

//读卡片数据
u8 ReadPSCard(u8 *bCardKey, u8 *bCardSerID, int iBlockID, u8 *bCardContext) {
    u8 cResult;
    u8 bCardSerialID[8]={0};

    cResult = ReadCardSerID(bCardSerialID);
    if (cResult == 0) {
        cResult = memcmp(bCardSerialID, bCardSerID, 4);
        if (cResult != 0) {
            LOGD("Read Card ERR 1");
            return 61;
        }
    } else {
        LOGD("Read Card ERR 0");
        return 61;
    }

    // 1、下载密钥
    cResult = Card_LoadKey(bCardKey);
    if (cResult != 0) {
        LOGD("Card_LoadKey %d", cResult);
        return 61;
    }
    // 2、认证密钥
    cResult = Card_Authen(bCardSerID, iBlockID / 4, 0);
    if (cResult != 0) {
        LOGD("Card_Authen %d", cResult);
        return 61;
    }
    // 3、读卡片数据
    cResult = Card_Read(iBlockID, bCardContext);
    if (cResult != 0) {
        LOGD("Card_Read %d", cResult);
        return 61;
    }
    return 0;
}

//当出现E061时，重新读卡
u8 ReReadPSCard(u8 *bCardKey, u8 *bCardSerID, int iBlockID, u8 *bCardContext) {
    int i;
    long lngResult;
    u8 bCardTempID[8]={0};

    for (i = 0; i < 6; i++) {
        lngResult = ReadCardSerID(bCardTempID);
        if (lngResult == 0) {
            lngResult = memcmp(bCardTempID, bCardSerID, 4);
            if (lngResult == 0) {
                lngResult = ReadPSCard(bCardKey, bCardSerID, iBlockID, bCardContext);
                if (lngResult == 0) {
                    return 0;
                }
            }
        }
    }
    return 61;
}



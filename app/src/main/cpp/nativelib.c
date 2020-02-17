#include <jni.h>
#include <stdint.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <string.h>
#include <time.h>
#include <termios.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>

#include <stddef.h>
#include "android/log.h"
#include "SECLIB.h"

#include "bsp/pn512.h"
#include "card/MifCard.h"
#include "card/CardInfo.h"
#include "card/CPUCard.h"
#include "card/SAM_App.h"
#include "bsp/rtc.h"
#include "bsp/uartqr.h"
#include "bsp/public.h"
#include "secret/secret.h"
#include "bsp/sam.h"
#include "bsp/uartprinter.h"
#include "bsp/uartdockpos.h"


//static const char *TAG="JNI";
//#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
//#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
//#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)



JNIEXPORT jstring JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_stringFromJNI(JNIEnv *env, jobject instance) {

    // TODO
    return (*env)->NewStringUTF(env, "Hello from C++");
}

/*-----------------------密钥管理-----------------------*/
////客户设密卡设用例程
//JNIEXPORT void JNICALL
//Java_com_hzsun_mpos_nativelib_nativelib_nSCardSecret(JNIEnv *env, jobject instance,
//                                                    jbyteArray bCardSerialID_,
//                                                    jbyteArray bSCardSecret_) {
//    jbyte *bCardSerialID = (*env)->GetByteArrayElements(env, bCardSerialID_, NULL);
//    jbyte *bSCardSecret = (*env)->GetByteArrayElements(env, bSCardSecret_, NULL);
//
//    // TODO
//    SCardSecret(bCardSerialID,bSCardSecret);
//
//    (*env)->ReleaseByteArrayElements(env, bCardSerialID_, bCardSerialID, 0);
//    (*env)->ReleaseByteArrayElements(env, bSCardSecret_, bSCardSecret, 0);
//}

////解密函数调用例(A密钥)
//JNIEXPORT void JNICALL
//Java_com_hzsun_mpos_nativelib_nativelib_nUCardDecryptA(JNIEnv *env, jobject instance,
//                                                      jobjectArray bUCardSecret,
//                                                      jbyteArray bSCardData_) {
//    jbyte *bSCardData = (*env)->GetByteArrayElements(env, bSCardData_, NULL);
//
//    // TODO
//    jobjectArray DArrayTemp;
//    jclass intArrCls = (*env)->FindClass(env, "[I");
//    if ( NULL== intArrCls)
//        return ;
//
//    DArrayTemp = (*env)->NewObjectArray(env, 2 * 4, intArrCls, NULL);
//
//    int row = (*env)->GetArrayLength(env, bUCardSecret);//获得行数
//    jarray myarray = ((*env)->GetObjectArrayElement(env, bUCardSecret, 0));
//    int col =(*env)->GetArrayLength(env, myarray); //获得列数
//    u8 bUCardSecretTemp[row][col];
//
//    UCardDecryptA(bUCardSecretTemp,bSCardData);
//
//    //(*env)->ReleaseByteArrayElements(env, bSCardData_, bSCardData, 0);
//}
//
//JNIEXPORT jobjectArray JNICALL
//Java_com_hzsun_mpos_nativelib_nativelib_nUCardDecryptA(JNIEnv *env, jobject instance,
//                                                      jbyteArray bSCardData_) {
//    jbyte *bSCardData = (*env)->GetByteArrayElements(env, bSCardData_, NULL);
//
//    // TODO
//    int i;
//    int iRow=32;
//    int iCol=6;
//    u8 bUCardSecretTemp[32][6];
//    jobjectArray DArrayTemp;
////    int row = (*env)->GetArrayLength(env, bUCardSecret);//获得行数
////    jarray myarray = ((*env)->GetObjectArrayElement(env, bUCardSecret, 0));
////    int col =(*env)->GetArrayLength(env, myarray); //获得列数
//
//    // 1.获得一个byte型二维数组类的引用
//    jclass byteArrCls = (*env)->FindClass(env, "[B");
//    if ( NULL== byteArrCls)
//        NULL ;
//
//    // 2.创建一个数组对象（里面每个元素用clsIntArray表示）
//    DArrayTemp = (*env)->NewObjectArray(env, iRow, byteArrCls, NULL);
//    if (DArrayTemp == NULL)
//    {
//        return NULL;
//    }
//    for (i = 0; i < iRow; ++i)
//        memset(bUCardSecretTemp[i],0x00,6);
//
//    UCardDecryptA(bUCardSecretTemp,bSCardData);
//
//    // 3.为数组元素赋值
//    for (i = 0; i < iRow; ++i)
//    {
//        jbyte buff[6];
//        jbyteArray byteArr = (*env)->NewByteArray(env,iRow);
//        if (byteArr == NULL)
//        {
//            return NULL;
//        }
//        memcpy(buff,bUCardSecretTemp[i],6);
//
//        (*env)->SetByteArrayRegion(env,byteArr, 0,iCol,buff);
//        (*env)->SetObjectArrayElement(env,DArrayTemp, i, byteArr);
//        (*env)->DeleteLocalRef(env,byteArr);
//    }
//
//    (*env)->ReleaseByteArrayElements(env, bSCardData_, bSCardData, 0);
//
//    return DArrayTemp;
//}
//
////解密函数调用例(B密钥)
//JNIEXPORT jobjectArray JNICALL
//Java_com_hzsun_mpos_nativelib_nativelib_nUCardDecryptB(JNIEnv *env, jobject instance,
//                                                       jbyteArray bSCardData_) {
//    jbyte *bSCardData = (*env)->GetByteArrayElements(env, bSCardData_, NULL);
//
//    // TODO
//    int i;
//    int iRow=32;
//    int iCol=6;
//    u8 bUCardSecretTemp[32][6];
//    jobjectArray DArrayTemp;
////    int row = (*env)->GetArrayLength(env, bUCardSecret);//获得行数
////    jarray myarray = ((*env)->GetObjectArrayElement(env, bUCardSecret, 0));
////    int col =(*env)->GetArrayLength(env, myarray); //获得列数
//
//    // 1.获得一个byte型二维数组类的引用
//    jclass byteArrCls = (*env)->FindClass(env, "[B");
//    if ( NULL== byteArrCls)
//        NULL ;
//
//    // 2.创建一个数组对象（里面每个元素用clsIntArray表示）
//    DArrayTemp = (*env)->NewObjectArray(env, iRow, byteArrCls, NULL);
//    if (DArrayTemp == NULL)
//    {
//        return NULL;
//    }
//    for (i = 0; i < iRow; ++i)
//        memset(bUCardSecretTemp[i],0x00,6);
//
//    UCardDecryptB(bUCardSecretTemp,bSCardData);
//
//    // 3.为数组元素赋值
//    for (i = 0; i < iRow; ++i)
//    {
//        jbyte buff[6];
//        jbyteArray byteArr = (*env)->NewByteArray(env,iRow);
//        if (byteArr == NULL)
//        {
//            return NULL;
//        }
//        memcpy(buff,bUCardSecretTemp[i],6);
//
//        (*env)->SetByteArrayRegion(env,byteArr, 0,iCol,buff);
//        (*env)->SetObjectArrayElement(env,DArrayTemp, i, byteArr);
//        (*env)->DeleteLocalRef(env,byteArr);
//    }
//
//    (*env)->ReleaseByteArrayElements(env, bSCardData_, bSCardData, 0);
//
//    return DArrayTemp;
//}
//
//JNIEXPORT void JNICALL
//Java_com_hzsun_mpos_nativelib_nativelib_nUCardBurAuthen(JNIEnv *env, jobject instance,
//                                                       jbyteArray strCardSerID_, jint iAgentID,
//                                                       jint iUserID, jint iSector,
//                                                       jlong lngBurMoney, jint iBurseNoteID,
//                                                       jbyteArray bBurseAuthen_) {
//    jbyte *strCardSerID = (*env)->GetByteArrayElements(env, strCardSerID_, NULL);
//    jbyte *bBurseAuthen = (*env)->GetByteArrayElements(env, bBurseAuthen_, NULL);
//
//    // TODO
//    UCardBurAuthen(strCardSerID, iAgentID, iUserID, iSector, lngBurMoney,iBurseNoteID,bBurseAuthen);
//
//    (*env)->ReleaseByteArrayElements(env, strCardSerID_, strCardSerID, 0);
//    (*env)->ReleaseByteArrayElements(env, bBurseAuthen_, bBurseAuthen, 0);
//}

//生成卡片密钥
//参数 ：扇区号[0~15]，客户号,代理号,A密钥
JNIEXPORT void JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nGet_1SectKeyUCard(JNIEnv *env, jobject instance,
                                                           jint Sector, jint ClientNum,
                                                           jint AgentNum, jint isKeyA,
                                                           jbyteArray pKey_) {
    jbyte *pKey = (*env)->GetByteArrayElements(env, pKey_, NULL);

    Get_SectKeyUCard(Sector, ClientNum, AgentNum, isKeyA, pKey);

    (*env)->ReleaseByteArrayElements(env, pKey_, pKey, 0);
}

//生成钱包认证码
JNIEXPORT jlong JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nGet_1AuthenCodeUWallet(JNIEnv *env, jobject instance,
                                                                jlong dwCardNum, jint wClientNum,
                                                                jint AgentNum, jlong lMonCurr,
                                                                jint Sector, jint wWalletSID) {

    return Get_AuthenCodeUWallet(dwCardNum, wClientNum, AgentNum, lMonCurr, Sector, wWalletSID);

}
//设置密钥
JNIEXPORT void JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nSetCardKey(JNIEnv *env, jobject instance,
                                                    jobjectArray bUCardAuthKeyA,
                                                    jobjectArray bUCardAuthKeyB) {

    int i = 0;
    int iRow = 32;
    int iCol = 6;
    u8 bUCardAuthKeyTempA[32][6];
    u8 bUCardAuthKeyTempB[32][6];


    // 3.为数组元素赋值
    for (i = 0; i < iRow; ++i) {
        jbyte buff[iCol];
        jbyteArray byteArr = ((*env)->GetObjectArrayElement(env, bUCardAuthKeyA, i));
        (*env)->GetByteArrayRegion(env, byteArr, 0, iCol, buff);
        memcpy(bUCardAuthKeyTempA[i], buff, iCol);
    }
    for (i = 0; i < iRow; ++i) {
        jbyte buff[iCol];
        jbyteArray byteArr = ((*env)->GetObjectArrayElement(env, bUCardAuthKeyB, i));
        (*env)->GetByteArrayRegion(env, byteArr, 0, iCol, buff);
        memcpy(bUCardAuthKeyTempB[i], buff, iCol);
    }
    SetCardKey(bUCardAuthKeyTempA, bUCardAuthKeyTempB);

    return;

}

/*-----------------------RTC驱动-----------------------*/
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nRTCTest(JNIEnv *env, jobject instance) {

    int status;

    status = RTC_test();
    return status;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nGetRTCDateTime(JNIEnv *env, jobject instance,
                                                        jbyteArray bCurrDateTime_) {
    jbyte *bCurrDateTime = (*env)->GetByteArrayElements(env, bCurrDateTime_, NULL);

    int status;

    status = GetRTCDateTime(bCurrDateTime);

    (*env)->ReleaseByteArrayElements(env, bCurrDateTime_, bCurrDateTime, 0);

    return status;
}

/*-----------------------读卡器驱动-----------------------*/
//初始化读卡器
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReaderInit(JNIEnv *env, jobject instance) {

    int status;

    status = PcdOpen();
    if (status < 0) {
        LOGD("==读头打开初始化失败==\n");
        return status;
    }
    status = PcdConfig(ISO14443_TYPEA);
    if (status != 0) {
        LOGD("==读头初始化卡片类型失败==\n");
        return status;
    }
    LOGD("==读头打开初始化成功==\n");
    return 0;
}

//关闭化读卡器
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReaderClose(JNIEnv *env, jobject instance) {

    // TODO
    PcdClose();
    LOGD("==读头关闭==\n");
    return 0;
}

//选择CPU卡部分
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nCardSelectProCard(JNIEnv *env, jobject instance,
                                                           jbyteArray resp_) {
    jbyte *resp = (*env)->GetByteArrayElements(env, resp_, NULL);

    int iRet = 0;
    u8 rlen[1];
    iRet = Card_SelectProCard(resp, rlen);
    if (iRet == 0) {
        iRet = rlen[0];
    } else {
        iRet = 0;
    }
    (*env)->ReleaseByteArrayElements(env, resp_, resp, 0);

    return iRet;
}


//读取卡号UID
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReaderCardUID(JNIEnv *env, jobject instance,
                                                       jbyteArray bCardUID_) {
    jbyte *bCardUID = (*env)->GetByteArrayElements(env, bCardUID_, NULL);

    int iRet = 0;
    iRet = ReadCardSerID(bCardUID);
    (*env)->ReleaseByteArrayElements(env, bCardUID_, bCardUID, 0);

    return iRet;
}

//读取卡号UID和属性
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReadCardAttrib(JNIEnv *env, jobject instance,
                                                        jbyteArray bCardUID_, jbyteArray SAK_) {
    jbyte *bCardUID = (*env)->GetByteArrayElements(env, bCardUID_, NULL);
    jbyte *SAK = (*env)->GetByteArrayElements(env, SAK_, NULL);

    int iRet = 0;
    iRet = ReadCardAttrib(bCardUID, SAK);
    (*env)->ReleaseByteArrayElements(env, bCardUID_, bCardUID, 0);
    (*env)->ReleaseByteArrayElements(env, SAK_, SAK, 0);

    return iRet;
}

//读取CPU卡UID并选择cpu部分
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReadCPUCardSID(JNIEnv *env, jobject instance,
                                                        jbyteArray cCardSID_) {
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    int iRet = 0;
    iRet = ReadCPUCardSID(cCardSID);

    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);
    return iRet;
}

//读卡片数据
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReadMifareBlock(JNIEnv *env, jobject instance,
                                                         jbyte cBlockID, jbyteArray cCardSID_,
                                                         jbyteArray cCardContext_) {
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);
    jbyte *cCardContext = (*env)->GetByteArrayElements(env, cCardContext_, NULL);

    int iRet = 0;
    iRet = ReadMifareBlock(cBlockID, cCardSID, cCardContext);

    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);
    (*env)->ReleaseByteArrayElements(env, cCardContext_, cCardContext, 0);
    return iRet;

}
//写卡片数据
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nWriteMifareBlock(JNIEnv *env, jobject instance,
                                                          jbyte cBlockID, jbyteArray cCardSID_,
                                                          jbyteArray cCardContext_) {
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);
    jbyte *cCardContext = (*env)->GetByteArrayElements(env, cCardContext_, NULL);

    int iRet = 0;
    iRet = WriteMifareBlock(cBlockID, cCardSID, cCardContext);

    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);
    (*env)->ReleaseByteArrayElements(env, cCardContext_, cCardContext, 0);

    return iRet;
}
//写卡片数据(无认证)
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nWriteMifareBlockNOAuthen(JNIEnv *env, jobject instance,
                                                                  jbyte cBlockID,
                                                                  jbyteArray cCardContext_) {
    jbyte *cCardContext = (*env)->GetByteArrayElements(env, cCardContext_, NULL);

    int iRet = 0;
    iRet = WriteMifareBlockNOAuthen(cBlockID, cCardContext);

    (*env)->ReleaseByteArrayElements(env, cCardContext_, cCardContext, 0);

    return iRet;
}

//读Mifare卡片扇区数据
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReadMifareSector(JNIEnv *env, jobject instance,
                                                          jbyte cSectorID, jbyteArray cCardSID_,
                                                          jobjectArray cCardContext) {
    int i = 0;
    int iRet = 0;
    int iRow = 3;
    int iCol = 16;
    u8 cCardContextTemp[3][16];
    jobjectArray DArrayTemp;

    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    // 1.获得一个byte型二维数组类的引用
    jclass byteArrCls = (*env)->FindClass(env, "[B");
    if (NULL == byteArrCls)
        NULL;

    // 2.创建一个数组对象（里面每个元素用clsIntArray表示）
    DArrayTemp = (*env)->NewObjectArray(env, iRow, byteArrCls, NULL);
    if (DArrayTemp == NULL) {
        return -1;
    }

    for (i = 0; i < iRow; ++i)
        memset(cCardContextTemp[i], 0x00, 16);

    iRet = ReadMifareSector(cSectorID, cCardSID, cCardContextTemp);
    if (iRet == 0) {
        // 3.为数组元素赋值
        for (i = 0; i < iRow; ++i) {
            jbyte buff[iCol];
            jbyteArray byteArr = (*env)->NewByteArray(env, iCol);
            if (byteArr == NULL) {
                return -1;
            }
            memcpy(buff, cCardContextTemp[i], iCol);

            (*env)->SetByteArrayRegion(env, byteArr, 0, iCol, buff);
            (*env)->SetObjectArrayElement(env, cCardContext, i, byteArr);
            (*env)->DeleteLocalRef(env, byteArr);
        }
        //cCardContext=DArrayTemp;
    }
    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);

    return iRet;
}

//读Mifare卡片钱包扇区数据
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReadMifareBurseSector(JNIEnv *env, jobject instance,
                                                               jbyteArray cCardSID_, jbyte cBlockID,
                                                               jbyteArray cCardContext_,
                                                               jbyte cBlockBakID,
                                                               jbyteArray cCardBakContext_) {
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);
    jbyte *cCardContext = (*env)->GetByteArrayElements(env, cCardContext_, NULL);
    jbyte *cCardBakContext = (*env)->GetByteArrayElements(env, cCardBakContext_, NULL);

    int iRet = 0;
    iRet = ReadMifareBurseSector(cCardSID, cBlockID, cCardContext, cBlockBakID, cCardBakContext);

    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);
    (*env)->ReleaseByteArrayElements(env, cCardContext_, cCardContext, 0);
    (*env)->ReleaseByteArrayElements(env, cCardBakContext_, cCardBakContext, 0);

    return iRet;
}

//写Mifare卡片扇区数据
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nWriteMifareSector(JNIEnv *env, jobject instance,
                                                           jbyte cSectorID, jbyteArray cCardSID_,
                                                           jobjectArray cCardContext) {

    int i = 0;
    int iRet = 0;
    int iRow = 3;
    int iCol = 16;
    u8 cCardContextTemp[3][16];
    jobjectArray DArrayTemp;

    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    int row = (*env)->GetArrayLength(env, cCardContext);//获得行数
    jarray myarray = ((*env)->GetObjectArrayElement(env, cCardContext, 0));
    int col = (*env)->GetArrayLength(env, myarray); //获得列数

    // 3.为数组元素赋值
    for (i = 0; i < iRow; ++i) {
        jbyte buff[iCol];
        jbyteArray byteArr = ((*env)->GetObjectArrayElement(env, cCardContext, i));
        (*env)->GetByteArrayRegion(env, byteArr, 0, iCol, buff);
        memcpy(cCardContextTemp[i], buff, iCol);
    }
    iRet = WriteMifareSector(cSectorID, cCardSID, cCardContextTemp);

    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);

    return iRet;
}

//写Mifare卡片钱包扇区数据
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nWriteMifareBurseSector(JNIEnv *env, jobject instance,
                                                                jbyteArray cCardSID_,
                                                                jbyte cBlockID,
                                                                jbyteArray cCardContext_,
                                                                jbyte cBlockBakID,
                                                                jbyteArray cCardBakContext_) {
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);
    jbyte *cCardContext = (*env)->GetByteArrayElements(env, cCardContext_, NULL);
    jbyte *cCardBakContext = (*env)->GetByteArrayElements(env, cCardBakContext_, NULL);

    int iRet = 0;
    iRet = WriteMifareBurseSector(cCardSID, cBlockID, cCardContext, cBlockBakID, cCardBakContext);

    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);
    (*env)->ReleaseByteArrayElements(env, cCardContext_, cCardContext, 0);
    (*env)->ReleaseByteArrayElements(env, cCardBakContext_, cCardBakContext, 0);

    return iRet;
}
//读Mifare卡片扇区数据(固定密钥a0-a5)
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReadMifareOpenSector(JNIEnv *env, jobject instance,
                                                              jbyte cSectorID, jbyteArray cCardSID_,
                                                              jobjectArray cCardContext) {

    int i = 0;
    int iRet = 0;
    int iRow = 3;
    int iCol = 16;
    u8 cCardContextTemp[3][16];
    jobjectArray DArrayTemp;

    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    // 1.获得一个byte型二维数组类的引用
    jclass byteArrCls = (*env)->FindClass(env, "[B");
    if (NULL == byteArrCls)
        NULL;

    // 2.创建一个数组对象（里面每个元素用clsIntArray表示）
    DArrayTemp = (*env)->NewObjectArray(env, iRow, byteArrCls, NULL);
    if (DArrayTemp == NULL) {
        return -1;
    }

    for (i = 0; i < iRow; ++i)
        memset(cCardContextTemp[i], 0x00, 16);

    iRet = ReadMifareOpenSector(cSectorID, cCardSID, cCardContextTemp);
    if (iRet == 0) {
        // 3.为数组元素赋值
        for (i = 0; i < iRow; ++i) {
            jbyte buff[iCol];
            jbyteArray byteArr = (*env)->NewByteArray(env, iCol);
            if (byteArr == NULL) {
                return -1;
            }
            memcpy(buff, cCardContextTemp[i], iCol);

            (*env)->SetByteArrayRegion(env, byteArr, 0, iCol, buff);
            (*env)->SetObjectArrayElement(env, cCardContext, i, byteArr);
            (*env)->DeleteLocalRef(env, byteArr);
        }
        //cCardContext=DArrayTemp;
    }
    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);

    return iRet;
}
//读卡片数据
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReadPSCard(JNIEnv *env, jobject instance,
                                                    jbyteArray bCardKey_, jbyteArray bCardSerID_,
                                                    jint iBlockID, jbyteArray bCardContext_) {
    jbyte *bCardKey = (*env)->GetByteArrayElements(env, bCardKey_, NULL);
    jbyte *bCardSerID = (*env)->GetByteArrayElements(env, bCardSerID_, NULL);
    jbyte *bCardContext = (*env)->GetByteArrayElements(env, bCardContext_, NULL);

    int iRet = 0;
    iRet = ReadPSCard(bCardKey, bCardSerID, iBlockID, bCardContext);

    (*env)->ReleaseByteArrayElements(env, bCardKey_, bCardKey, 0);
    (*env)->ReleaseByteArrayElements(env, bCardSerID_, bCardSerID, 0);
    (*env)->ReleaseByteArrayElements(env, bCardContext_, bCardContext, 0);

    return iRet;
}

//当出现E061时，重新读卡
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nReReadPSCard(JNIEnv *env, jobject instance,
                                                      jbyteArray bCardKey_, jbyteArray bCardSerID_,
                                                      jint iBlockID, jbyteArray bCardContext_) {
    jbyte *bCardKey = (*env)->GetByteArrayElements(env, bCardKey_, NULL);
    jbyte *bCardSerID = (*env)->GetByteArrayElements(env, bCardSerID_, NULL);
    jbyte *bCardContext = (*env)->GetByteArrayElements(env, bCardContext_, NULL);

    int iRet = 0;
    iRet = ReReadPSCard(bCardKey, bCardSerID, iBlockID, bCardContext);

    (*env)->ReleaseByteArrayElements(env, bCardKey_, bCardKey, 0);
    (*env)->ReleaseByteArrayElements(env, bCardSerID_, bCardSerID, 0);
    (*env)->ReleaseByteArrayElements(env, bCardContext_, bCardContext, 0);

    return iRet;
}

//基本信息设置(全部)
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nCPU_1BasicAllInfo_1Set(JNIEnv *env, jobject instance,
                                                                jbyteArray bBasicContext_,
                                                                jbyteArray cCardSID_) {
    jbyte *bBasicContext = (*env)->GetByteArrayElements(env, bBasicContext_, NULL);
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    int iRet = 0;
    iRet = CPU_BasicAllInfo_Set(bBasicContext, cCardSID);

    (*env)->ReleaseByteArrayElements(env, bBasicContext_, bBasicContext, 0);
    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);

    return iRet;
}
//扩展信息设置(全部)
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nCPU_1ExternAllInfo_1Set(JNIEnv *env, jobject instance,
                                                                 jbyteArray bExternContext_,
                                                                 jbyteArray cCardSID_) {
    jbyte *bExternContext = (*env)->GetByteArrayElements(env, bExternContext_, NULL);
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    int iRet = 0;
    iRet = CPU_ExternAllInfo_Set(bExternContext, cCardSID);

    (*env)->ReleaseByteArrayElements(env, bExternContext_, bExternContext, 0);
    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);

    return iRet;
}
//钱包信息设置
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nCPU_1BurseInfo_1Set(JNIEnv *env, jobject instance,
                                                             jint cBurseID,
                                                             jbyteArray bBurseContext_,
                                                             jbyteArray cCardSID_) {
    jbyte *bBurseContext = (*env)->GetByteArrayElements(env, bBurseContext_, NULL);
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    int iRet = 0;
    iRet = CPU_BurseInfo_Set(cBurseID, bBurseContext, cCardSID);

    (*env)->ReleaseByteArrayElements(env, bBurseContext_, bBurseContext, 0);
    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);

    return iRet;
}


JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nCPU_1BasicExternInfo_1Get(JNIEnv *env, jobject instance,
                                                                   jbyteArray bBasicContext_,
                                                                   jbyteArray bExternContext_,
                                                                   jbyteArray cCardSID_) {
    jbyte *bBasicContext = (*env)->GetByteArrayElements(env, bBasicContext_, NULL);
    jbyte *bExternContext = (*env)->GetByteArrayElements(env, bExternContext_, NULL);
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    int iRet = 0;
    iRet = CPU_BasicExternInfo_Get(bBasicContext, bExternContext, cCardSID);

    (*env)->ReleaseByteArrayElements(env, bBasicContext_, bBasicContext, 0);
    (*env)->ReleaseByteArrayElements(env, bExternContext_, bExternContext, 0);
    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);

    return iRet;
}

//基本信息读取
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nCPU_1BasicInfo_1Get(JNIEnv *env, jobject instance,
                                                             jbyteArray bBasicContext_,
                                                             jbyteArray cCardSID_) {
    jbyte *bBasicContext = (*env)->GetByteArrayElements(env, bBasicContext_, NULL);
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    int iRet = 0;
    iRet = CPU_BasicInfo_Get(bBasicContext, cCardSID);

    (*env)->ReleaseByteArrayElements(env, bBasicContext_, bBasicContext, 0);
    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);

    return iRet;
}
//扩展信息读取
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nCPU_1ExternInfo_1Get(JNIEnv *env, jobject instance,
                                                              jbyteArray bExternContext_,
                                                              jbyteArray cCardSID_) {
    jbyte *bExternContext = (*env)->GetByteArrayElements(env, bExternContext_, NULL);
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    int iRet = 0;
    iRet = CPU_ExternInfo_Get(bExternContext, cCardSID);;

    (*env)->ReleaseByteArrayElements(env, bExternContext_, bExternContext, 0);
    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);

    return iRet;
}
//钱包信息读取
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nCPU_1BurseInfo_1Get(JNIEnv *env, jobject instance,
                                                             jint cBurseID,
                                                             jbyteArray bBurseContext_,
                                                             jbyteArray cCardSID_) {
    jbyte *bBurseContext = (*env)->GetByteArrayElements(env, bBurseContext_, NULL);
    jbyte *cCardSID = (*env)->GetByteArrayElements(env, cCardSID_, NULL);

    int iRet = 0;
    iRet = CPU_BurseInfo_Get(cBurseID, bBurseContext, cCardSID);

    (*env)->ReleaseByteArrayElements(env, bBurseContext_, bBurseContext, 0);
    (*env)->ReleaseByteArrayElements(env, cCardSID_, cCardSID, 0);

    return iRet;
}

/*-----------------------QRCode Uart Fun-----------------------*/
JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nUartQR_1Init(JNIEnv *env, jobject instance, jint iBaudID) {

    int iRet = 0;
    iRet = UartQR_Init(iBaudID);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1ScanQRCode(JNIEnv *env, jobject instance,
                                                        jbyteArray cQRCodeInfo_) {
    jbyte *cQRCodeInfo = (*env)->GetByteArrayElements(env, cQRCodeInfo_, NULL);

    int iRet = 0;
    iRet = QR_ScanQRCode(cQRCodeInfo);

    (*env)->ReleaseByteArrayElements(env, cQRCodeInfo_, cQRCodeInfo, 0);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1DeviceReset(JNIEnv *env, jobject instance) {

    int iRet = 0;
    iRet = QR_DeviceReset();
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1SetDevicePrompt(JNIEnv *env, jobject instance,
                                                             jint cMode) {
    int iRet = 0;
    iRet = QR_SetDevicePrompt(cMode);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1SetDeviceLed(JNIEnv *env, jobject instance,
                                                          jint cMode) {
    int iRet = 0;
    iRet = QR_SetDeviceLed(cMode);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1SetDeviceReadMode(JNIEnv *env, jobject instance,
                                                               jint cMode) {
    int iRet = 0;
    iRet = QR_SetDeviceReadMode(cMode);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1SetDeviceReadEnable(JNIEnv *env, jobject instance,
                                                                 jint cMode) {
    int iRet = 0;
    iRet = QR_SetDeviceReadEnable(cMode);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1SetDeviceReadInterval(JNIEnv *env, jobject instance,
                                                                   jint cType) {
    int iRet = 0;
    iRet = QR_SetDeviceReadInterval(cType);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1SetBAUD(JNIEnv *env, jobject instance, jint BaudID) {

    int iRet = 0;
    iRet = QR_SetBAUD(BaudID);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1GetDeviceInfo(JNIEnv *env, jobject instance,
                                                           jbyteArray cDeviceInfo_) {
    jbyte *cDeviceInfo = (*env)->GetByteArrayElements(env, cDeviceInfo_, NULL);

    int iRet = 0;
    iRet = QR_GetDeviceInfo(cDeviceInfo);
    (*env)->ReleaseByteArrayElements(env, cDeviceInfo_, cDeviceInfo, 0);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1SetAutoSenLevel(JNIEnv *env, jobject instance,
                                                             jint cLevel) {
    int iRet = 0;
    iRet = QR_SetAutoSenLevel(cLevel);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1GetDisSenseRet(JNIEnv *env, jobject instance,
                                                            jint iCount) {
    int iRet = 0;
    iRet = QR_GetDisSenseRet(iCount);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1SetHostCommand(JNIEnv *env, jobject instance) {
    int iRet = 0;
    iRet = QR_SetHostCommand();
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1SetEndMark(JNIEnv *env, jobject instance) {

    int iRet = 0;
    iRet = QR_SetEndMark();
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nQR_1ClearRecvData(JNIEnv *env, jobject instance,
                                                           jint iTime) {

    int iRet = 0;
    iRet = UartClearQR_RecvData(iTime);
    return iRet;

}

JNIEXPORT void JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nSCardSecret(JNIEnv *env, jobject instance,
                                                     jbyteArray bCardSerialID_,
                                                     jbyteArray bSCardSecret_) {
    jbyte *bCardSerialID = (*env)->GetByteArrayElements(env, bCardSerialID_, NULL);
    jbyte *bSCardSecret = (*env)->GetByteArrayElements(env, bSCardSecret_, NULL);

    // TODO
    SCardSecret(bCardSerialID, bSCardSecret);

    (*env)->ReleaseByteArrayElements(env, bCardSerialID_, bCardSerialID, 0);
    (*env)->ReleaseByteArrayElements(env, bSCardSecret_, bSCardSecret, 0);
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nIOPortCtrl(JNIEnv *env, jobject instance, jstring path_,
                                                    jint val) {
    const char *path = (*env)->GetStringUTFChars(env, path_, 0);

    // TODO
    return sysfs_write(path, val);

    (*env)->ReleaseStringUTFChars(env, path_, path);
}

JNIEXPORT void JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nLedShow(JNIEnv *env, jobject instance, jchar color,
                                                 jint level) {

    // TODO
    LedShow(color, level);
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nPSAMSP_1Init(JNIEnv *env, jobject instance) {

    // TODO
    return PSAMSP_Init();
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nSAM_1EXCHANGE_1APDU(JNIEnv *env, jobject instance,
                                                             jbyteArray apdu_buf_, jint apdu_len,
                                                             jbyteArray response_,
                                                             jintArray data_len_, jint wait_time) {
    jbyte *apdu_buf = (*env)->GetByteArrayElements(env, apdu_buf_, NULL);
    jbyte *response = (*env)->GetByteArrayElements(env, response_, NULL);
    jint *data_len = (*env)->GetIntArrayElements(env, data_len_, NULL);

    // TODO
    int iRet = 0;
    iRet = SAM_EXCHANGE_APDU(apdu_buf, apdu_len, response, data_len, wait_time);

    (*env)->ReleaseByteArrayElements(env, apdu_buf_, apdu_buf, 0);
    (*env)->ReleaseByteArrayElements(env, response_, response, 0);
    (*env)->ReleaseIntArrayElements(env, data_len_, data_len, 0);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nUartPrinter_1Init(JNIEnv *env, jobject instance,
                                                           jint baudID) {

    // TODO
    int iRet = 0;
    iRet = UartPrinter_Init(baudID);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nUartPrinter_1SendData(JNIEnv *env, jobject instance,
                                                               jbyteArray data_, jint len) {
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

    // TODO
    int iRet = 0;
    iRet = UartPrinter_SendData(data, len);
    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nUartPrinter_1RecvData(JNIEnv *env, jobject instance,
                                                               jbyteArray data_, jint len,
                                                               jint wait_time) {
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

    // TODO
    int iRet = 0;
    iRet = UartPrinter_RecvData(data, len, wait_time);
    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nUartDockpos_1Init(JNIEnv *env, jobject instance,
                                                               jint baudID) {

    // TODO
    int iRet = 0;
    iRet = UartDockpos_Init(baudID);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nUartDockpos_1SendData(JNIEnv *env, jobject instance,
                                                                   jbyteArray data_, jint len) {
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

    // TODOM
    int iRet = 0;
    iRet = UartDockpos_SendData(data, len);
    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nUartDockpos_1RecvData(JNIEnv *env, jobject instance,
                                                                   jbyteArray data_) {
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    // TODO
    int iRet = 0;
    iRet = UartDockpos_RecvData(data);
    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    return iRet;
}

JNIEXPORT jint JNICALL
Java_com_hzsun_mpos_nativelib_nativelib_nUartDockpos_1ClearData(JNIEnv *env, jobject instance) {
    return UartDockpos_ClearData();
}




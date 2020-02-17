#include <stdio.h>
#include <unistd.h>
#include <string.h>

#include "Deslib.h"


#define DES_LEN 64  //DES算法加密的数据位数
#define Ex_LEN  48  //扩展置换扩展后的数据位数
#define PC1_LEN 56  //子密钥产生过程中的置换选择1的数据位数
#define PC2_LEN 48  //子密钥产生过程中的置换选择2的数据位数
#define P_LEN   32  //P置换的数据位数


const unsigned char key_SAVE[] = {0x01, 0x23, 0xd5, 0xa7, 0x89, 0xab, 0xcd, 0xef, 0xfe, 0xdc, 0xba,
                                  0x98, 0xe6, 0x54, 0x32, 0x1f};    //密钥存放加密密钥

//IP置换
unsigned char IP_Permutation_Data[DES_LEN] = {
        57, 49, 41, 33, 25, 17, 9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7,
        56, 48, 40, 32, 24, 16, 8, 0,
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6};

//扩展置换
unsigned char Ex_Permutation_Data[Ex_LEN] = {
        31, 0, 1, 2, 3, 4,
        3, 4, 5, 6, 7, 8,
        7, 8, 9, 10, 11, 12,
        11, 12, 13, 14, 15, 16,
        15, 16, 17, 18, 19, 20,
        19, 20, 21, 22, 23, 24,
        23, 24, 25, 26, 27, 28,
        27, 28, 29, 30, 31, 0};
//P置换
unsigned char P_Permutation_Data[P_LEN] = {
        15, 6, 19, 20, 28, 11, 27, 16,
        0, 14, 22, 25, 4, 17, 30, 9,
        1, 7, 23, 13, 31, 26, 2, 8,
        18, 12, 29, 5, 21, 10, 3, 24};


//子密钥生成
//利用56位密码生成16个子密钥
//置换选择1：PC1
unsigned char PC1_data[PC1_LEN] = {
        56, 48, 40, 32, 24, 16, 8,
        0, 57, 49, 41, 33, 25, 17,
        9, 1, 58, 50, 42, 34, 26,
        18, 10, 2, 59, 51, 43, 35,
        62, 54, 46, 38, 30, 22, 14,
        6, 61, 53, 45, 37, 29, 21,
        13, 5, 60, 52, 44, 36, 28,
        20, 12, 4, 27, 19, 11, 3};


//置换选择2：PC2
unsigned char PC2_data[PC2_LEN] = {
        13, 16, 10, 23, 0, 4, 2, 27,
        14, 5, 20, 9, 22, 18, 11, 3,
        25, 7, 15, 6, 26, 19, 12, 1,
        40, 51, 30, 36, 46, 54, 29, 39,
        50, 44, 32, 47, 43, 48, 38, 55,
        33, 52, 45, 41, 49, 35, 28, 31};

//逆IP置换
unsigned char DeIP_data[DES_LEN] = {
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25,
        32, 0, 40, 8, 48, 16, 56, 24};

//产生子密钥
//产生16个子密钥过程中每次左循环移位的位数
int RL_data[16] = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};

//S盒变换
//S盒变换数据
unsigned char SBox_data1[64] = {
        14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
        0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
        4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
        15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13};
unsigned char SBox_data2[64] = {
        15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
        3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
        0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
        13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9};
unsigned char SBox_data3[64] = {
        10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8,
        13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
        13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7,
        1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12};
unsigned char SBox_data4[64] = {
        7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15,
        13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
        10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4,
        3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14};
unsigned char SBox_data5[64] = {
        2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9,
        14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
        4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14,
        11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3};
unsigned char SBox_data6[64] = {
        12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11,
        10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
        9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6,
        4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13};
unsigned char SBox_data7[64] = {
        4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1,
        13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
        1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2,
        6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12};
unsigned char SBox_data8[64] = {
        13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7,
        1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
        7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8,
        2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11};


//通用数据置换函数。p_data指向置换的数据规则，len指明置换的数据bit位数。
void Permutation(unsigned char *data_in, unsigned char *data_out, unsigned char *p_data, int len) {
    register int i, j, t;
    register unsigned char *p;

    if (len <= 0) return;
    p = p_data;
    t = ((len - 1) >> 3) + 1;
    for (i = 0; i < t; i++) {
        j = i << 3;
        data_out[i] = 0;
        data_out[i] = ((data_in[p[j] >> 3] << (p[j] & 7)) & 0x80)
                      | (((data_in[p[j + 1] >> 3] << (p[j + 1] & 7)) >> 1) & 0x40)
                      | (((data_in[p[j + 2] >> 3] << (p[j + 2] & 7)) >> 2) & 0x20)
                      | (((data_in[p[j + 3] >> 3] << (p[j + 3] & 7)) >> 3) & 0x10)
                      | (((data_in[p[j + 4] >> 3] << (p[j + 4] & 7)) >> 4) & 0x08)
                      | (((data_in[p[j + 5] >> 3] << (p[j + 5] & 7)) >> 5) & 0x04)
                      | (((data_in[p[j + 6] >> 3] << (p[j + 6] & 7)) >> 6) & 0x02)
                      | (((data_in[p[j + 7] >> 3] << (p[j + 7] & 7)) >> 7) & 0x01);
    }
}


void IP_Permutation(unsigned char *data_in, unsigned char *data_out) {
    Permutation(data_in, data_out, IP_Permutation_Data, DES_LEN);
}


void Ex_Permutation(unsigned char *data_in, unsigned char *data_out) {
    Permutation(data_in, data_out, Ex_Permutation_Data, Ex_LEN);
}


void P_Permutation(unsigned char *data_in, unsigned char *data_out) {
    Permutation(data_in, data_out, P_Permutation_Data, P_LEN);
}

void PC1_Permutation(unsigned char *data_in, unsigned char *data_out) {
    Permutation(data_in, data_out, PC1_data, PC1_LEN);
}


void PC2_Permutation(unsigned char *data_in, unsigned char *data_out) {
    Permutation(data_in, data_out, PC2_data, PC2_LEN);
}


void DeIP_Permutation(unsigned char *data_in, unsigned char *data_out) {
    Permutation(data_in, data_out, DeIP_data, DES_LEN);
}


void Produce_SubKey(unsigned char *key, SUBKEY_Type *subkey) {
    unsigned char pc1_data_out[PC1_LEN / 8], pc2_data_out[PC2_LEN / 8];
    register int i;
    unsigned char tmp1, tmp2, tmp3;

    PC1_Permutation(key, pc1_data_out);  //对密钥进行PC1置换(选择置换1)。
    for (i = 0; i < 16; i++) {
        if (i < 2 || i == 8 || i == 15) {  //循环左移一位
            tmp1 = (pc1_data_out[0] & 0x80) >> 3;
            pc1_data_out[0] = (pc1_data_out[0] << 1) | ((pc1_data_out[1] & 0x80) >> 7);
            pc1_data_out[1] = (pc1_data_out[1] << 1) | ((pc1_data_out[2] & 0x80) >> 7);
            pc1_data_out[2] = (pc1_data_out[2] << 1) | ((pc1_data_out[3] & 0x80) >> 7);
            tmp2 = pc1_data_out[3];
            tmp3 = (pc1_data_out[3] & 0x08) >> 3;
            pc1_data_out[3] = ((tmp2 << 1) & 0xe0) | tmp1;
            pc1_data_out[3] |= ((tmp2 << 1) & 0x0f) | ((pc1_data_out[4] & 0x80) >> 7);
            pc1_data_out[4] = (pc1_data_out[4] << 1) | ((pc1_data_out[5] & 0x80) >> 7);
            pc1_data_out[5] = (pc1_data_out[5] << 1) | ((pc1_data_out[6] & 0x80) >> 7);
            pc1_data_out[6] = (pc1_data_out[6] << 1) | tmp3;
        } else {  //循环左移2位
            tmp1 = (pc1_data_out[0] & 0xc0) >> 2;
            pc1_data_out[0] = (pc1_data_out[0] << 2) | ((pc1_data_out[1] & 0xc0) >> 6);
            pc1_data_out[1] = (pc1_data_out[1] << 2) | ((pc1_data_out[2] & 0xc0) >> 6);
            pc1_data_out[2] = (pc1_data_out[2] << 2) | ((pc1_data_out[3] & 0xc0) >> 6);
            tmp2 = pc1_data_out[3];
            tmp3 = (pc1_data_out[3] & 0x0c) >> 2;
            pc1_data_out[3] = ((tmp2 << 2) & 0xc0) | tmp1;
            pc1_data_out[3] |= ((tmp2 << 2) & 0x0f) | ((pc1_data_out[4] & 0xc0) >> 6);
            pc1_data_out[4] = (pc1_data_out[4] << 2) | ((pc1_data_out[5] & 0xc0) >> 6);
            pc1_data_out[5] = (pc1_data_out[5] << 2) | ((pc1_data_out[6] & 0xc0) >> 6);
            pc1_data_out[6] = (pc1_data_out[6] << 2) | tmp3;
        }
        PC2_Permutation(pc1_data_out, pc2_data_out);  //pc2_data_out即为子密钥Ki
        memcpy(subkey[i].subkeystr, pc2_data_out, 6);
    }
}

void S_Box(unsigned char *data_in, unsigned char *data_out)  //输入48位，输出32位。
{
    register int row, col;

    memset(data_out, 0, 4);
    //data_out[0]
    row = ((data_in[0] & 0x80) >> 6) | ((data_in[0] & 0x04) >> 2);
    col = (data_in[0] & 0x78) >> 3;
    data_out[0] = SBox_data1[(row << 4) + col] << 4;
    row = (data_in[0] & 0x02) | ((data_in[1] & 0x10) >> 4);
    col = ((data_in[0] & 0x01) << 3) | ((data_in[1] & 0xe0) >> 5);
    data_out[0] |= SBox_data2[(row << 4) + col];

    //data_out[1]
    row = ((data_in[1] & 0x08) >> 2) | ((data_in[2] & 0x40) >> 6);
    col = ((data_in[1] & 0x07) << 1) | ((data_in[2] & 0x80) >> 7);
    data_out[1] = SBox_data3[(row << 4) + col] << 4;
    row = ((data_in[2] & 0x20) >> 4) | (data_in[2] & 0x01);
    col = (data_in[2] & 0x1e) >> 1;
    data_out[1] |= SBox_data4[(row << 4) + col];

    //data_out[2]
    row = ((data_in[3] & 0x80) >> 6) | ((data_in[3] & 0x04) >> 2);
    col = (data_in[3] & 0x78) >> 3;
    data_out[2] = SBox_data5[(row << 4) + col] << 4;
    row = (data_in[3] & 0x02) | ((data_in[4] & 0x10) >> 4);
    col = ((data_in[3] & 0x01) << 3) | ((data_in[4] & 0xe0) >> 5);
    data_out[2] |= SBox_data6[(row << 4) + col];

    //data_out[3]
    row = ((data_in[4] & 0x08) >> 2) | ((data_in[5] & 0x40) >> 6);
    col = ((data_in[4] & 0x07) << 1) | ((data_in[5] & 0x80) >> 7);
    data_out[3] = SBox_data7[(row << 4) + col] << 4;
    row = ((data_in[5] & 0x20) >> 4) | (data_in[5] & 0x01);
    col = (data_in[5] & 0x1e) >> 1;
    data_out[3] |= SBox_data8[(row << 4) + col];
}

//加密，共进行16轮。
void DES_Circle(unsigned char *data_in, unsigned char *data_out, unsigned char *subkey) {
    unsigned char *pl, *pr;  //指向data_in的左32位和右32位
    unsigned char ex_data_out[Ex_LEN >> 3], sbox_data_out[4], p_data_out[P_LEN >> 3];
    register int i, j;

    pl = data_in;
    pr = data_in + 4;
    Ex_Permutation(pr, ex_data_out);
    j = Ex_LEN >> 3;
    for (i = 0; i < j; i++)
        ex_data_out[i] ^= subkey[i];
    S_Box(ex_data_out, sbox_data_out);
    P_Permutation(sbox_data_out, p_data_out);

    memcpy(data_out, pr, 4);
    for (i = 0; i < 4; i++) data_out[i + 4] = pl[i] ^ p_data_out[i];
}


//mode==0:加密； mode!=0:解密
void DES(unsigned char *key, unsigned char *data_in, unsigned char *data_out, int mode) {
    unsigned char data[8], data1[8], data2[8];
    SUBKEY_Type subkey[16];
    int i;
    IP_Permutation(data_in, data);  //IP置换

    Produce_SubKey((unsigned char *) key, subkey);  //产生子密钥
    memcpy(data1, data, 8);
    for (i = 0; i < 16; i++) {
        if (mode == 0) {  //加密
            DES_Circle(data1, data2, subkey[i].subkeystr);
        } else {
            DES_Circle(data1, data2, subkey[15 - i].subkeystr);
        }
        memcpy(data1, data2, 8);
    }
    //32位对换，数据记录在data1中
    memcpy(data1, data2 + 4, 4);
    memcpy(data1 + 4, data2, 4);
    DeIP_Permutation(data1, data_out);
}

//3DES计算，mode=0:加密，mode=1:解密
//key:16字节密钥
//data_in:8字节明文
//data_out:输出的8字节密文
void DES3(unsigned char *key, unsigned char *data_in, unsigned char *data_out, int mode) {
    int m1, m2;
    unsigned char data[8];
    if (mode == 0) {
        m1 = 0;
        m2 = 1;
    }//加密
    else {
        m1 = 1;
        m2 = 0;
    }
    memcpy(data, data_in, 8);
    DES(key, data, data_out, m1);
    memcpy(data, data_out, 8);
    DES(key + 8, data, data_out, m2);
    memcpy(data, data_out, 8);
    DES(key, data, data_out, m1);
    return;
}

//用固定密钥加密密钥数据，并转换格式。data_in为16字节，data_out为16字节
//type=0表示加密，为1表示解密。
void Key_encrypt(unsigned char *data_in, unsigned char *data_out, int type) {
    int k;
    unsigned char tmpchar[16];
    memcpy(tmpchar, data_in, 16);
    if (type == 1)    //解密
        for (k = 8; k < 16; k++) tmpchar[k] = ~tmpchar[k];

    DES3((unsigned char *) key_SAVE, tmpchar, data_out, type);
    DES3((unsigned char *) key_SAVE, tmpchar + 8, data_out + 8, type);

    if (type == 0)    //加密
        for (k = 8; k < 16; k++) data_out[k] = ~data_out[k];
}

//根据输入的18个字节产生18个密钥
//data_in的长度是18个字节，data_out的长度是16*18个字节
void Produce_key(unsigned char *data_in, unsigned char *data_out) {
    unsigned char R1[16], Ri[16], Key[16 * 18];//,EKey[16*18];
    int c, i, j;
    int order[16] = {3, 5, 9, 12, 7, 1, 0, 14, 8, 4, 10, 2, 6, 13, 15, 11};
    unsigned char tmpchar[16];

    //R1为Key
    for (i = 0, c = 0; i < 18; i++) {
        if (i == 3 || i == 15) continue;
        R1[c] = data_in[i];
        c++;
    }
    //计算18个密钥
    for (i = 0; i < 18; i++) {
        for (j = i, c = 0; c < 16; j++, c++) {
            Ri[c] = data_in[j % 18];
        }
        DES3(R1, Ri, Key + i * 16, 0);
        DES3(R1, Ri + 8, Key + i * 16 + 8, 0);
    }

    //重置密钥的顺序
    for (i = 0; i < 18; i++) {
        memcpy(tmpchar, Key + i * 16, 16);
        for (j = 0; j < 16; j++) Key[i * 16 + j] = tmpchar[order[j]];
    }
    //对密钥进行加密
    for (i = 0; i < 18; i++) {
        Key_encrypt(Key + i * 16, data_out + i * 16, 0);
    }
    return;
}

//将加密的密钥恢复成为真正的密钥
//data_in的长度是16*18个字节，data_out的长度是16*18个字节
void Restore_key(unsigned char *data_in, unsigned char *data_out) {
    int i, j;
    int order[16] = {3, 5, 9, 12, 7, 1, 0, 14, 8, 4, 10, 2, 6, 13, 15, 11};
    unsigned char tmpchar[16];

    //对密钥进行解密
    for (i = 0; i < 18; i++) {
        Key_encrypt(data_in + i * 16, data_out + i * 16, 1);
    }
    //恢复密钥的顺序

    for (i = 0; i < 18; i++) {
        memcpy(tmpchar, data_out + i * 16, 16);
        for (j = 0; j < 16; j++) data_out[i * 16 + order[j]] = tmpchar[j];
    }
}

//计算卡认证码
//data_in：卡唯一代码(4字节)+发行行政区域代码最后一字节(1字节)+发行流水号(3字节)
//mac：卡认证码
//返回：0表示成功，其他表示出错。
int ComputerCardMac(unsigned char *data_in, unsigned char *mac) {
    unsigned char Key[16 * 18], EKey[16 * 18], data[8];

    Restore_key(EKey, Key);
    DES3(Key + 16 * 16, data_in, data, 0);
    memcpy(mac, data + 4, 4);
    return (0);
}

//计算卡认证码
//data_in：卡唯一代码(4字节)+发行行政区域代码最后一字节(1字节)+发行流水号(3字节)
//mac：卡认证码
//返回：0表示成功，其他表示出错。
int ComputeMAC(unsigned char *EKey, unsigned char *data_in, unsigned char *mac) {
    unsigned char Key[16 * 18], data[8];

    Restore_key(EKey, Key);
    DES3(Key + 16 * 16, data_in, data, 0);
    memcpy(mac, data + 4, 4);
    return (0);
}

//计算扇区密钥
//data_in：卡唯一代码(4字节)+发行行政区域代码(3字节)+发行流水号(3字节)+卡认证码(4字节)+扇区功能码(16字节)
//sector_flag：16字节，针对每个字节，其值为非0表示计算该扇区的密钥。
//data_out：返回密钥，其字节数为需要计算密钥的扇区数*6，其有效的长度由函数调用者来保证。
//type：0表示计算KEYA，其他表示计算KEYB。
//返回：0表示卡认证码正确，1表示卡认证码错误，2表示密钥数据不存在或者出错。
int ComputerSectorKey(unsigned char *data_in, unsigned char *sector_flag, unsigned char *data_out,
                      int type) {
    unsigned char Key[16 * 18], EKey[16 * 18];
    unsigned char tmpdata[8], mac[8];

    //计算扇区密钥
    int i, c;

    Restore_key(EKey, Key);
    //认证卡认证码

    memcpy(tmpdata, data_in, 4);    //卡唯一代码
    memcpy(tmpdata + 4, data_in + 6, 4);    //发行行政区域代码最后一字节+发行流水号3字节
    DES3(Key + 16 * 16, tmpdata, mac, 0);
    if (memcmp(data_in + 10, mac + 4, 4) != 0) return (1);

    memcpy(tmpdata, data_in, 4);    //卡唯一代码
    memcpy(tmpdata + 4, data_in + 6, 3);    //发行行政区域代码最后一字节+发行流水号低两字节
    for (i = 0, c = 0; i < 16; i++) {
        if (sector_flag[i] == 0) continue;
        tmpdata[7] = data_in[14 + i];
        if (type == 0) {//计算KEYA
            DES3(Key + 17 * 16, tmpdata, mac, 0);
        } else DES3(Key + i * 16, tmpdata, mac, 0);
        memcpy(data_out + c * 6, mac + 2, 6);
        c++;
    }
    return (0);
}

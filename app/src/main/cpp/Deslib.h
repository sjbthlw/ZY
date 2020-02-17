
#ifndef DESLIB_H
#define DESLIB_H


#ifdef __cplusplus
extern "C" {
#endif


typedef struct {
    unsigned char subkeystr[6];
} SUBKEY_Type;


//通用数据置换函数。p_data指向置换的数据规则，len指明置换的数据bit位数。
void Permutation(unsigned char *data_in, unsigned char *data_out, unsigned char *p_data, int len);

void IP_Permutation(unsigned char *data_in, unsigned char *data_out);

void Ex_Permutation(unsigned char *data_in, unsigned char *data_out);

void P_Permutation(unsigned char *data_in, unsigned char *data_out);

void PC1_Permutation(unsigned char *data_in, unsigned char *data_out);

void PC2_Permutation(unsigned char *data_in, unsigned char *data_out);

void DeIP_Permutation(unsigned char *data_in, unsigned char *data_out);

void Produce_SubKey(unsigned char *key, SUBKEY_Type *subkey);

void S_Box(unsigned char *data_in, unsigned char *data_out);

//加密，共进行16轮。
void DES_Circle(unsigned char *data_in, unsigned char *data_out, unsigned char *subkey);

//mode==0:加密； mode!=0:解密
void DES(unsigned char *key, unsigned char *data_in, unsigned char *data_out, int mode);

//3DES计算，mode=0:加密，mode=1:解密
//key:16字节密钥
//data_in:8字节明文
//data_out:输出的8字节密文
void DES3(unsigned char *key, unsigned char *data_in, unsigned char *data_out, int mode);

//用固定密钥加密密钥数据，并转换格式。data_in为16字节，data_out为16字节
//type=0表示加密，为1表示解密。
void Key_encrypt(unsigned char *data_in, unsigned char *data_out, int type);

//根据输入的18个字节产生18个密钥
//data_in的长度是18个字节，data_out的长度是16*18个字节
void Produce_key(unsigned char *data_in, unsigned char *data_out);

//将加密的密钥恢复成为真正的密钥
//data_in的长度是16*18个字节，data_out的长度是16*18个字节
void Restore_key(unsigned char *data_in, unsigned char *data_out);

//计算卡认证码
//data_in：卡唯一代码(4字节)+发行行政区域代码最后一字节(1字节)+发行流水号(3字节)
//mac：卡认证码
//返回：0表示成功，其他表示出错。
int ComputerCardMac(unsigned char *data_in, unsigned char *mac);

//计算卡认证码
//data_in：卡唯一代码(4字节)+发行行政区域代码最后一字节(1字节)+发行流水号(3字节)
//mac：卡认证码
//返回：0表示成功，其他表示出错。
int ComputeMAC(unsigned char *EKey, unsigned char *data_in, unsigned char *mac);

//计算扇区密钥
//data_in：卡唯一代码(4字节)+发行行政区域代码(3字节)+发行流水号(3字节)+卡认证码(4字节)+扇区功能码(16字节)
//sector_flag：16字节，针对每个字节，其值为非0表示计算该扇区的密钥。
//data_out：返回密钥，其字节数为需要计算密钥的扇区数*6，其有效的长度由函数调用者来保证。
//type：0表示计算KEYA，其他表示计算KEYB。
//返回：0表示卡认证码正确，1表示卡认证码错误，2表示密钥数据不存在或者出错。
int ComputerSectorKey(unsigned char *data_in, unsigned char *sector_flag, unsigned char *data_out,
                      int type);


#ifdef __cplusplus
}
#endif

#endif
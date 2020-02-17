#ifndef HSECLIB_PROCS_CASE1
#define HSECLIB_PROCS_CASE1

#define idata

#define BYTE  unsigned char
#define WORD  unsigned short
#define DWORD unsigned long

//接口参数传递参数块及工作数据区结构
typedef
union {
    //IO参数结构
    struct {
        //输入或输出参数缓冲区
        BYTE InOut[16];
        //输入种子数据区
        BYTE Seed[16];
        BYTE Reserved[16];
    } Params;
    //工作数据结构
    BYTE Bytes[48];
    WORD Words[24];
    DWORD DWords[12];
    struct {
        BYTE BytesF[16];
        DWORD DWord1;
        WORD Worda, Wordb, Wordc, Wordd;
        BYTE k, j, i, Count;
        BYTE BytesB[16];
    } Work;
    struct {
        BYTE Bytes[28];
        BYTE k;
        BYTE j;
        BYTE i;
        BYTE Count;
        BYTE BytesB[16];
    } Work1;
    struct {
        DWORD cy;
        DWORD cz;
        DWORD ca;
        DWORD cb;
        DWORD DWord1;
        WORD Worda, Wordb, Wordc, Wordd;
        BYTE k, j, i, Count;
        DWORD ctmp0;
        DWORD ctmp1;
        DWORD ctmp2;
        DWORD ctmp3;
    } Work2;
} TSECLIB_BUFFER;

//存储需求50字节idata空间
extern TSECLIB_BUFFER SECLIB_Buffer;


//客户设密卡扇区密码生成函数
//种子数据: 4字节。
//输入参数：版本号(1字节)；代理号（1字节）；客户号(2字节)；卡序列号(4字节) ；设密卡生成密钥(4字节)；保留=0(4字节)。
//输出参数：加密的A/B密码(6*2字节) 。
extern void SECLIB_Get_SectKeySCard(void);

#endif

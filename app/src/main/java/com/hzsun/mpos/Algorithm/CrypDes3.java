/**
 *
 */
package com.hzsun.mpos.Algorithm;

/**
 * @Author: XiangGW
 * @Description: TODO
 * @Date: 2015-8-10
 */
public class CrypDes3 {

    public CrypDes3() {

    }

    // IP置换
    private byte[] IP_Permutation_Data = {57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47,
            39, 31, 23, 15, 7, 56, 48, 40, 32, 24, 16, 8, 0, 58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22,
            14, 6};

    // 逆IP置换
    private byte[] DeIP_data = {39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12, 52, 20,
            60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25, 32, 0, 40, 8, 48, 16, 56, 24};

    // 置换选择1：PC1
    private byte[] PC1_data = {56, 48, 40, 32, 24, 16, 8, 0, 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 62, 54,
            46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 60, 52, 44, 36, 28, 20, 12, 4, 27, 19, 11, 3};

    // 置换选择2：PC2
    private byte[] PC2_data = {13, 16, 10, 23, 0, 4, 2, 27, 14, 5, 20, 9, 22, 18, 11, 3, 25, 7, 15, 6, 26, 19, 12, 1, 40, 51, 30, 36, 46, 54, 29,
            39, 50, 44, 32, 47, 43, 48, 38, 55, 33, 52, 45, 41, 49, 35, 28, 31};

    // 扩展置换
    private byte[] Ex_Permutation_Data = {31, 0, 1, 2, 3, 4, 3, 4, 5, 6, 7, 8, 7, 8, 9, 10, 11, 12, 11, 12, 13, 14, 15, 16, 15, 16, 17, 18, 19, 20,
            19, 20, 21, 22, 23, 24, 23, 24, 25, 26, 27, 28, 27, 28, 29, 30, 31, 0};

    // S盒变换
    // S盒变换数据
    private byte[] SBox_data1 = {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7, 0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8, 4, 1,
            14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0, 15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13};

    private byte[] SBox_data2 = {15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10, 3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5, 0, 14,
            7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15, 13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9};

    private byte[] SBox_data3 = {10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8, 13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1, 13, 6,
            4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7, 1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12};

    private byte[] SBox_data4 = {7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15, 13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9, 10, 6,
            9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4, 3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14};

    private byte[] SBox_data5 = {2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9, 14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6, 4, 2,
            1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14, 11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3};

    private byte[] SBox_data6 = {12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11, 10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8, 9, 14,
            15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6, 4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13};

    private byte[] SBox_data7 = {4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1, 13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6, 1, 4,
            11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2, 6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12};

    private byte[] SBox_data8 = {13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7, 1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2, 7, 11,
            4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8, 2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11};

    // P置换
    private byte[] P_Permutation_Data = {15, 6, 19, 20, 28, 11, 27, 16, 0, 14, 22, 25, 4, 17, 30, 9, 1, 7, 23, 13, 31, 26, 2, 8, 18, 12, 29, 5, 21,
            10, 3, 24};

    // DES算法加密的数据位数
    private final int DES_LEN = 64;
    // 扩展置换扩展后的数据位数
    private final int Ex_LEN = 48;
    // 子密钥产生过程中的置换选择1的数据位数
    private final int PC1_LEN = 56;
    // 子密钥产生过程中的置换选择2的数据位数
    private final int PC2_LEN = 48;
    // P置换的数据位数
    private final int P_LEN = 32;

    /**
     * 将目标数据数组用指定的值赋值
     *
     * @param TargetData 目标数据数组
     * @param Data       指定的数据
     * @param nLen       长度
     * @Author: XiangGW
     * @Description: TODO
     * @Date: 2015-8-10
     */
    public void memset(byte[] TargetData, byte Data, int nLen) {
        for (int i = 0; i < nLen; i++)
            TargetData[i] = Data;
    }

    /**
     * 将数组中的数据复制到指定数组中
     *
     * @param TargetData  目标数组
     * @param TargetStart 目标数组起始位置
     * @param SourceData  数据源数组
     * @param SourceStart 数据源起始位置
     * @param nLen        长度
     * @Author: XiangGW
     * @Description: TODO
     * @Date: 2015-8-10
     */
    public void memcpy(byte[] TargetData, int TargetStart, byte[] SourceData, int SourceStart, int nLen) {
        System.arraycopy(SourceData, SourceStart, TargetData, TargetStart, nLen);
    }

    /**
     * DES3加解密
     *
     * @param key      密钥因子，16字节
     * @param data_in  要操作的数据，8字节
     * @param data_out 传出加解密的结果数据，8字节
     * @param mode     0－加密，1－解密
     * @Author: XiangGW
     * @Description: TODO
     * @Date: 2015-8-11
     */
    public void DES3(byte[] key, byte[] data_in, byte[] data_out, int mode) {
        int m1, m2;
        byte[] data = new byte[8];
        if (mode == 0) {
            // 加密
            m1 = 0;
            m2 = 1;
        } else {
            m1 = 1;
            m2 = 0;
        }
        memcpy(data, 0, data_in, 0, 8);
        DES(key, 0, data, data_out, m1);
        memcpy(data, 0, data_out, 0, 8);
        DES(key, 8, data, data_out, m2);
        memcpy(data, 0, data_out, 0, 8);
        DES(key, 0, data, data_out, m1);
    }

    private void DES(byte[] key, int nStart, byte[] data_in, byte[] data_out, int mode) {
        byte[] data = new byte[8];
        byte[] data1 = new byte[8];
        byte[] data2 = new byte[8];
        SUBKEY_Type[] subkey = new SUBKEY_Type[16];
        int i = 0;
        for (i = 0; i < 16; i++)
            subkey[i] = new SUBKEY_Type();
        // IP置换
        IP_Permutation(data_in, 0, data);
        // 产生子密钥
        Produce_SubKey(key, nStart, subkey);
        memcpy(data1, 0, data, 0, 8);
        for (i = 0; i < 16; i++) {
            if (mode == 0) {
                // 加密
                DES_Circle(data1, data2, subkey[i].subkeystr);
            } else {
                DES_Circle(data1, data2, subkey[15 - i].subkeystr);
            }
            memcpy(data1, 0, data2, 0, 8);
        }
        // 32位对换,数据记录在data1中
        memcpy(data1, 0, data2, 4, 4);
        memcpy(data1, 4, data2, 0, 4);
        DeIP_Permutation(data1, 0, data_out);
    }

    // 通用数据置换函数。p_data指向置换的数据规则,len指明置换的数据bit位数
    private void IP_Permutation(byte[] data_in, int nStart, byte[] data_out) {
        Permutation(data_in, nStart, data_out, IP_Permutation_Data, DES_LEN);
    }

    private void Produce_SubKey(byte[] key, int nStart, SUBKEY_Type[] subkey) {
        byte[] pc1_data_out = new byte[PC1_LEN / 8];
        byte[] pc2_data_out = new byte[PC2_LEN / 8];
        int i;
        byte tmp1, tmp2, tmp3;

        // 对密钥进行PC1置换(选择置换1)
        PC1_Permutation(key, nStart, pc1_data_out);
        for (i = 0; i < 16; i++) {
            if (i < 2 || i == 8 || i == 15) { // 循环左移一位
                tmp1 = (byte) ((pc1_data_out[0] & 0x80) >> 3);
                pc1_data_out[0] = (byte) ((pc1_data_out[0] << 1) | ((pc1_data_out[1] & 0x80) >> 7));
                pc1_data_out[1] = (byte) ((pc1_data_out[1] << 1) | ((pc1_data_out[2] & 0x80) >> 7));
                pc1_data_out[2] = (byte) ((pc1_data_out[2] << 1) | ((pc1_data_out[3] & 0x80) >> 7));
                tmp2 = pc1_data_out[3];
                tmp3 = (byte) ((pc1_data_out[3] & 0x08) >> 3);
                pc1_data_out[3] = (byte) (((tmp2 << 1) & 0xe0) | tmp1);
                pc1_data_out[3] |= (byte) (((tmp2 << 1) & 0x0f) | ((pc1_data_out[4] & 0x80) >> 7));
                pc1_data_out[4] = (byte) ((pc1_data_out[4] << 1) | ((pc1_data_out[5] & 0x80) >> 7));
                pc1_data_out[5] = (byte) ((pc1_data_out[5] << 1) | ((pc1_data_out[6] & 0x80) >> 7));
                pc1_data_out[6] = (byte) ((pc1_data_out[6] << 1) | tmp3);
            } else { // 循环左移2位
                tmp1 = (byte) ((pc1_data_out[0] & 0xc0) >> 2);
                pc1_data_out[0] = (byte) ((pc1_data_out[0] << 2) | ((pc1_data_out[1] & 0xc0) >> 6));
                pc1_data_out[1] = (byte) ((pc1_data_out[1] << 2) | ((pc1_data_out[2] & 0xc0) >> 6));
                pc1_data_out[2] = (byte) ((pc1_data_out[2] << 2) | ((pc1_data_out[3] & 0xc0) >> 6));
                tmp2 = pc1_data_out[3];
                tmp3 = (byte) ((pc1_data_out[3] & 0x0c) >> 2);
                pc1_data_out[3] = (byte) (((tmp2 << 2) & 0xc0) | tmp1);
                pc1_data_out[3] |= (byte) (((tmp2 << 2) & 0x0f) | ((pc1_data_out[4] & 0xc0) >> 6));
                pc1_data_out[4] = (byte) ((pc1_data_out[4] << 2) | ((pc1_data_out[5] & 0xc0) >> 6));
                pc1_data_out[5] = (byte) ((pc1_data_out[5] << 2) | ((pc1_data_out[6] & 0xc0) >> 6));
                pc1_data_out[6] = (byte) ((pc1_data_out[6] << 2) | tmp3);
            }

            // pc2_data_out即为子密钥Ki
            PC2_Permutation(pc1_data_out, 0, pc2_data_out);
            memcpy(subkey[i].subkeystr, 0, pc2_data_out, 0, 6);
        }
    }

    private void DeIP_Permutation(byte[] data_in, int nStart, byte[] data_out) {
        Permutation(data_in, nStart, data_out, DeIP_data, DES_LEN);
    }

    private void PC1_Permutation(byte[] data_in, int nStart, byte[] data_out) {
        Permutation(data_in, nStart, data_out, PC1_data, PC1_LEN);
    }

    private void PC2_Permutation(byte[] data_in, int nStart, byte[] data_out) {
        Permutation(data_in, nStart, data_out, PC2_data, PC2_LEN);
    }

    private void Ex_Permutation(byte[] data_in, int nStart, byte[] data_out) {
        Permutation(data_in, nStart, data_out, Ex_Permutation_Data, Ex_LEN);
    }

    private void P_Permutation(byte[] data_in, int nStart, byte[] data_out) {
        Permutation(data_in, nStart, data_out, P_Permutation_Data, P_LEN);
    }

    private void Permutation(byte[] data_in, int nStart, byte[] data_out, byte[] p_data, int len) {
        int i, j, t;
        byte[] p;

        if (len <= 0)
            return;
        p = p_data;
        t = ((len - 1) >> 3) + 1;
        for (i = 0; i < t; i++) {
            j = i << 3;
            data_out[i] = 0;
            data_out[i] = (byte) (((data_in[(p[j] >> 3) + nStart] << (p[j] & 7)) & 0x80)
                    | (((data_in[(p[j + 1] >> 3) + nStart] << (p[j + 1] & 7)) >> 1) & 0x40)
                    | (((data_in[(p[j + 2] >> 3) + nStart] << (p[j + 2] & 7)) >> 2) & 0x20)
                    | (((data_in[(p[j + 3] >> 3) + nStart] << (p[j + 3] & 7)) >> 3) & 0x10)
                    | (((data_in[(p[j + 4] >> 3) + nStart] << (p[j + 4] & 7)) >> 4) & 0x08)
                    | (((data_in[(p[j + 5] >> 3) + nStart] << (p[j + 5] & 7)) >> 5) & 0x04)
                    | (((data_in[(p[j + 6] >> 3) + nStart] << (p[j + 6] & 7)) >> 6) & 0x02) | (((data_in[(p[j + 7] >> 3) + nStart] << (p[j + 7] & 7)) >> 7) & 0x01));
        }
    }

    // 加密，共进行16轮。
    private void DES_Circle(byte[] data_in, byte[] data_out, byte[] subkey) {
        byte[] pl = new byte[4];
        // 指向data_in的左32位和右32位
        byte[] pr = new byte[4];
        byte[] ex_data_out = new byte[Ex_LEN >> 3];
        byte[] sbox_data_out = new byte[4];
        byte[] p_data_out = new byte[P_LEN >> 3];
        int i, j;
        memcpy(pl, 0, data_in, 0, 4);
        memcpy(pr, 0, data_in, 4, 4);
        Ex_Permutation(pr, 0, ex_data_out);
        j = Ex_LEN >> 3;
        for (i = 0; i < j; i++)
            ex_data_out[i] ^= subkey[i];
        S_Box(ex_data_out, sbox_data_out);
        P_Permutation(sbox_data_out, 0, p_data_out);

        memcpy(data_out, 0, pr, 0, 4);
        for (i = 0; i < 4; i++)
            data_out[i + 4] = (byte) (pl[i] ^ p_data_out[i]);
    }

    // 输入48位，输出32位
    private void S_Box(byte[] data_in, byte[] data_out) {
        int row, col;
        memset(data_out, (byte) 0, 4);
        // data_out[0]
        row = ((data_in[0] & 0x80) >> 6) | ((data_in[0] & 0x04) >> 2);
        col = (data_in[0] & 0x78) >> 3;
        data_out[0] = (byte) (SBox_data1[(row << 4) + col] << 4);
        row = (data_in[0] & 0x02) | ((data_in[1] & 0x10) >> 4);
        col = ((data_in[0] & 0x01) << 3) | ((data_in[1] & 0xe0) >> 5);
        data_out[0] |= SBox_data2[(row << 4) + col];

        // data_out[1]
        row = ((data_in[1] & 0x08) >> 2) | ((data_in[2] & 0x40) >> 6);
        col = ((data_in[1] & 0x07) << 1) | ((data_in[2] & 0x80) >> 7);
        data_out[1] = (byte) (SBox_data3[(row << 4) + col] << 4);
        row = ((data_in[2] & 0x20) >> 4) | (data_in[2] & 0x01);
        col = (data_in[2] & 0x1e) >> 1;
        data_out[1] |= SBox_data4[(row << 4) + col];

        // data_out[2]
        row = ((data_in[3] & 0x80) >> 6) | ((data_in[3] & 0x04) >> 2);
        col = (data_in[3] & 0x78) >> 3;
        data_out[2] = (byte) (SBox_data5[(row << 4) + col] << 4);
        row = (data_in[3] & 0x02) | ((data_in[4] & 0x10) >> 4);
        col = ((data_in[3] & 0x01) << 3) | ((data_in[4] & 0xe0) >> 5);
        data_out[2] |= SBox_data6[(row << 4) + col];

        // data_out[3]
        row = ((data_in[4] & 0x08) >> 2) | ((data_in[5] & 0x40) >> 6);
        col = ((data_in[4] & 0x07) << 1) | ((data_in[5] & 0x80) >> 7);
        data_out[3] = (byte) (SBox_data7[(row << 4) + col] << 4);
        row = ((data_in[5] & 0x20) >> 4) | (data_in[5] & 0x01);
        col = (data_in[5] & 0x1e) >> 1;
        data_out[3] |= SBox_data8[(row << 4) + col];
    }

    /**
     * @param kBuf    秘钥
     * @param pRadom  随机数
     * @param pRanAdd 1
     * @param data_in
     * @param dlen
     * @param pMac
     */
    public void MSIM_Key16Mac(byte[] kBuf, byte[] pRadom, byte pRanAdd, byte[] data_in, short dlen, byte[] pMac) {
        byte[] aBuf = new byte[512];
        byte[] bBuf = new byte[512];
        byte[] cBuf = new byte[512];

        int i, j, aLen, bLen;
        boolean sFlag = pRanAdd == 1 ? true : false;
        // if(pRanAdd==1)
        // {
        // sFlag=true;
        // }
        // else {
        // sFlag=false;
        // }
        bLen = 0;
        if (sFlag) {
            // 加随机数
            for (i = 0; i < 4; i++) {
                bBuf[bLen++] = pRadom[i];
            }
            // 加零
            for (i = 0; i < 4; i++) {
                bBuf[bLen++] = 0x00;
            }
        } else {
            // 加零
            for (i = 0; i < 8; i++) {
                bBuf[bLen++] = 0x00;
            }
        }

        aLen = 0;
        for (i = 0; i < dlen; i++) {
            aBuf[aLen++] = data_in[i];
        }
        // fill
        if (aLen % 8 == 0) {
            aBuf[aLen++] = (byte) 0x80;
            for (i = 0; i < 7; i++) {
                aBuf[aLen++] = (byte) 0x00;
            }
        } else {
            aBuf[aLen++] = (byte) 0x80;
            if (aLen % 8 != 0) {
                do {
                    aBuf[aLen++] = 0x00;
                } while (aLen % 8 != 0);
            }
        }
        for (i = 0; i < aLen / 8; i++) {
            for (j = 0; j < 8; j++) {
                bBuf[j] ^= aBuf[i * 8 + j];
            }
            DES(kBuf, 0, bBuf, cBuf, 0);
            for (j = 0; j < 8; j++) {
                bBuf[j] = cBuf[j];
            }
        }
        DES(kBuf, 8, bBuf, cBuf, 1);
        for (j = 0; j < 8; j++) {
            bBuf[j] = cBuf[j];
        }

        DES(kBuf, 0, bBuf, cBuf, 0);
        for (j = 0; j < 4; j++) {
            pMac[j] = cBuf[j];
        }
    }
}

class SUBKEY_Type {
    public byte[] subkeystr = new byte[6];
}

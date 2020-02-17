/**
 *
 */
package com.hzsun.mpos.Algorithm;

/**
 * @Author: XiangGW
 * @Description: TODO
 * @Date: 2015-7-8
 */
public class DES3 {

    public DES3() {

    }

    /**
     * @param
     * @param src 被加密的源数据
     * @return mode 0:加密 1:解密
     */
    public static byte[] encryptionByDES3(int integer, byte[] src, int mode) {
        /*
         * System提供了一个静态方法arraycopy(), arraycopy(Object src, int srcPos, Object
         * dest, int destPos, int length) src:源数组； srcPos:源数组要复制的起始位置；
         * dest:目的数组； destPos:目的数组放置的起始位置； length:复制的长度。
         */
        // 数据加密 MACKey做密钥对data数据做三次DES3加密 -- 0 加密，1解密
        // 先将数据转成8的倍数的数组,再进行加密
        byte[] srckey = Integer2bytes(integer);
        byte[] key = getByteToDoubleByKey(srckey);
        int ys = src.length % 8;
        int len = ys == 0 ? src.length : src.length + (8 - ys);
        byte[] out = new byte[len];

        byte[] out_src = new byte[len];
        System.arraycopy(src, 0, out_src, 0, src.length);

        CrypDes3 cr3 = new CrypDes3();
        int count = len / 8;
        byte[] in_mp = new byte[8 * count];
        byte[] out_mp = new byte[8 * count];
        for (int i = 1; i < (count + 1); i++) {
            if (i == 1) {
                // 默认0-7使用DES3加密
                cr3.DES3(key, out_src, out, mode);
            } else {
                // 取值8-15使用DES3加密
                int sc = 8 * (i - 1);
                System.arraycopy(out_src, sc, in_mp, 0, 8);
                cr3.DES3(key, in_mp, out_mp, mode);
                System.arraycopy(out_mp, 0, out, sc, 8);
            }
        }
        return out;
    }

    private static byte[] Integer2bytes(int number) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) ((number >> (i * 8)) & 0xff);
        }
        return bytes;
    }

    /**
     * @param srckey 传入的需要加密因子
     * @return 16位的加密因子
     */
    private static byte[] getByteToDoubleByKey(byte[] srckey) {
        byte[] key = getStringToByte(srckey, 4);
        byte[] newkey = new byte[4];
        for (int i = 0; i < 4; i++) {
            newkey[i] = (byte) ~key[i]; // 按位补运算符翻转操作数的每一位
        }
        byte[] creatkey = new byte[8];
        creatkey = getMergerByte(key, newkey);
        byte[] newcreatkey = new byte[8];
        for (int j = 0; j < 8; j++) {
            newcreatkey[j] = (byte) (creatkey[j] ^ 0xFF); // 按位异或操作符，两个操作数的某一位不相同时候结果的该位就为1
        }
        // 合并成16位的数组
        byte[] lastkey = getMergerByte(creatkey, newcreatkey);
        // 对16位的数组，再进行一次计算生成
        return lastkey;
    }

    // String与byte[]转换
    private static byte[] getStringToByte(byte[] by, int bs) {
        byte[] b = new byte[bs];
        if (by.length < bs) {
            System.arraycopy(by, 0, b, 0, by.length);
        } else {
            System.arraycopy(by, 0, b, 0, bs);
        }
        return b;
    }

    // java 合并两个byte数组
    private static byte[] getMergerByte(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

}
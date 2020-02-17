package com.hzsun.mpos.Public;

public class Crc8 {


    static byte[] Crc8_tab = {(byte) 0X00, (byte) 0X5E, (byte) 0XBC, (byte) 0XE2, (byte) 0X61, (byte) 0X3F, (byte) 0XDD, (byte) 0X83, (byte) 0XC2,
            (byte) 0X9C, (byte) 0X7E, (byte) 0X20, (byte) 0XA3, (byte) 0XFD, (byte) 0X1F, (byte) 0X41, (byte) 0X9D, (byte) 0XC3, (byte) 0X21,
            (byte) 0X7F, (byte) 0XFC, (byte) 0XA2, (byte) 0X40, (byte) 0X1E, (byte) 0X5F, (byte) 0X01, (byte) 0XE3, (byte) 0XBD, (byte) 0X3E,
            (byte) 0X60, (byte) 0X82, (byte) 0XDC, (byte) 0X23, (byte) 0X7D, (byte) 0X9F, (byte) 0XC1, (byte) 0X42, (byte) 0X1C, (byte) 0XFE,
            (byte) 0XA0, (byte) 0XE1, (byte) 0XBF, (byte) 0X5D, (byte) 0X03, (byte) 0X80, (byte) 0XDE, (byte) 0X3C, (byte) 0X62, (byte) 0XBE,
            (byte) 0XE0, (byte) 0X02, (byte) 0X5C, (byte) 0XDF, (byte) 0X81, (byte) 0X63, (byte) 0X3D, (byte) 0X7C, (byte) 0X22, (byte) 0XC0,
            (byte) 0X9E, (byte) 0X1D, (byte) 0X43, (byte) 0XA1, (byte) 0XFF, (byte) 0X46, (byte) 0X18, (byte) 0XFA, (byte) 0XA4, (byte) 0X27,
            (byte) 0X79, (byte) 0X9B, (byte) 0XC5, (byte) 0X84, (byte) 0XDA, (byte) 0X38, (byte) 0X66, (byte) 0XE5, (byte) 0XBB, (byte) 0X59,
            (byte) 0X07, (byte) 0XDB, (byte) 0X85, (byte) 0X67, (byte) 0X39, (byte) 0XBA, (byte) 0XE4, (byte) 0X06, (byte) 0X58, (byte) 0X19,
            (byte) 0X47, (byte) 0XA5, (byte) 0XFB, (byte) 0X78, (byte) 0X26, (byte) 0XC4, (byte) 0X9A, (byte) 0X65, (byte) 0X3B, (byte) 0XD9,
            (byte) 0X87, (byte) 0X04, (byte) 0X5A, (byte) 0XB8, (byte) 0XE6, (byte) 0XA7, (byte) 0XF9, (byte) 0X1B, (byte) 0X45, (byte) 0XC6,
            (byte) 0X98, (byte) 0X7A, (byte) 0X24, (byte) 0XF8, (byte) 0XA6, (byte) 0X44, (byte) 0X1A, (byte) 0X99, (byte) 0XC7, (byte) 0X25,
            (byte) 0X7B, (byte) 0X3A, (byte) 0X64, (byte) 0X86, (byte) 0XD8, (byte) 0X5B, (byte) 0X05, (byte) 0XE7, (byte) 0XB9, (byte) 0X8C,
            (byte) 0XD2, (byte) 0X30, (byte) 0X6E, (byte) 0XED, (byte) 0XB3, (byte) 0X51, (byte) 0X0F, (byte) 0X4E, (byte) 0X10, (byte) 0XF2,
            (byte) 0XAC, (byte) 0X2F, (byte) 0X71, (byte) 0X93, (byte) 0XCD, (byte) 0X11, (byte) 0X4F, (byte) 0XAD, (byte) 0XF3, (byte) 0X70,
            (byte) 0X2E, (byte) 0XCC, (byte) 0X92, (byte) 0XD3, (byte) 0X8D, (byte) 0X6F, (byte) 0X31, (byte) 0XB2, (byte) 0XEC, (byte) 0X0E,
            (byte) 0X50, (byte) 0XAF, (byte) 0XF1, (byte) 0X13, (byte) 0X4D, (byte) 0XCE, (byte) 0X90, (byte) 0X72, (byte) 0X2C, (byte) 0X6D,
            (byte) 0X33, (byte) 0XD1, (byte) 0X8F, (byte) 0X0C, (byte) 0X52, (byte) 0XB0, (byte) 0XEE, (byte) 0X32, (byte) 0X6C, (byte) 0X8E,
            (byte) 0XD0, (byte) 0X53, (byte) 0X0D, (byte) 0XEF, (byte) 0XB1, (byte) 0XF0, (byte) 0XAE, (byte) 0X4C, (byte) 0X12, (byte) 0X91,
            (byte) 0XCF, (byte) 0X2D, (byte) 0X73, (byte) 0XCA, (byte) 0X94, (byte) 0X76, (byte) 0X28, (byte) 0XAB, (byte) 0XF5, (byte) 0X17,
            (byte) 0X49, (byte) 0X08, (byte) 0X56, (byte) 0XB4, (byte) 0XEA, (byte) 0X69, (byte) 0X37, (byte) 0XD5, (byte) 0X8B, (byte) 0X57,
            (byte) 0X09, (byte) 0XEB, (byte) 0XB5, (byte) 0X36, (byte) 0X68, (byte) 0X8A, (byte) 0XD4, (byte) 0X95, (byte) 0XCB, (byte) 0X29,
            (byte) 0X77, (byte) 0XF4, (byte) 0XAA, (byte) 0X48, (byte) 0X16, (byte) 0XE9, (byte) 0XB7, (byte) 0X55, (byte) 0X0B, (byte) 0X88,
            (byte) 0XD6, (byte) 0X34, (byte) 0X6A, (byte) 0X2B, (byte) 0X75, (byte) 0X97, (byte) 0XC9, (byte) 0X4A, (byte) 0X14, (byte) 0XF6,
            (byte) 0XA8, (byte) 0X74, (byte) 0X2A, (byte) 0XC8, (byte) 0X96, (byte) 0X15, (byte) 0X4B, (byte) 0XA9, (byte) 0XF7, (byte) 0XB6,
            (byte) 0XE8, (byte) 0X0A, (byte) 0X54, (byte) 0XD7, (byte) 0X89, (byte) 0X6B, (byte) 0X35};

    /**
     * 计算数组的Crc8校验值
     *
     * @param data 需要计算的数组
     * @return Crc8校验值
     */
    public static byte calcCrc8(byte[] data) {
        return calcCrc8(data, 0, data.length, (byte) 0);
    }

    /**
     * 计算Crc8校验值
     *
     * @param data   数据
     * @param offset 起始位置
     * @param len    长度
     * @return 校验值
     */
    public static byte calcCrc8(byte[] data, int offset, int len) {
        return calcCrc8(data, offset, len, (byte) 0);
    }

    /**
     * 计算Crc8校验值
     *
     * @param data   数据
     * @param offset 起始位置
     * @param len    长度
     * @param preval 之前的校验值
     * @return 校验值
     */
    public static byte calcCrc8(byte[] data, int offset, int len, byte preval) {
        byte ret = preval;
        for (int i = offset; i < (offset + len); ++i) {
            ret = Crc8_tab[(0X00FF & (ret ^ data[i]))];
        }
        return ret;
    }

    //CRC-8/XMODEM
    public static byte crc8_ccitt(byte[] data, int len) {
        return calcCrc8(data, 0, len, (byte) 0);
    }


    // 测试
    public static void main(String[] args) {
        byte crc = Crc8.calcCrc8(new byte[]{1, 1, 1, 1, 1, 0, 0, 2, 10, 11, 12, 1, 0, 1, 4});
        System.out.println("" + Integer.toHexString(0x00ff & crc));
    }

}

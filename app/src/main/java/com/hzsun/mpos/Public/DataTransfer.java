package com.hzsun.mpos.Public;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class DataTransfer {

    /**
     * int转byte
     *
     * @param value
     * @return byte字节数组，低位在前高位在后
     */
    public static byte[] int2Bytes(int value, int len) {
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return b;
    }

    public static int byte2int(byte b) {
        return b >= 0 ? b : 256 + b;
    }

    /**
     * byte数组转换为无符号short整数
     *
     * @param bytes byte数组
     * @return short整数
     */
    public static int byte2ToUnsignedShort(byte[] bytes, int beg) {
        int high = bytes[beg];
        int low = bytes[beg + 1];
        return (low & 0xFF) | (high << 8 & 0xFF00);
    }

    /**
     * byte数组转换为无符号short整数
     *
     * @param bytes byte数组
     * @return short整数
     */
    public static int byte2Short(byte[] bytes, int beg) {
        int low = bytes[beg];
        int high = bytes[beg + 1];
        return (low & 0xFF) | (high << 8 & 0xFF00);
    }

    public static int byte2ShortBIG(byte[] bytes, int beg) {
        int low = bytes[beg];
        int high = bytes[beg + 1];
        return (high & 0xFF) | (low << 8 & 0xFF00);
    }

    public static long bytes2Un3Integer(byte[] bytes, int beg) {
        if (bytes == null) {
            return 0;
        }
        long num = (bytes[beg] & 0xff) + ((bytes[beg + 1] & 0xff) << 8) + ((bytes[beg + 2] & 0xff) << 16);

        return num;
    }

    public static long bytes2UnInteger(byte[] bytes, int beg) {
        if (bytes == null) {
            return 0;
        }
        long num = (bytes[beg] & 0xff) + ((bytes[beg + 1] & 0xff) << 8) + ((bytes[beg + 2] & 0xff) << 16) + ((bytes[beg + 3] & 0xff) << 24);

        return num;
    }

    //十进制转换成十六进制
    public static void ChangeToHex(byte[] Context, int iLen) {

        for (int i = 0; i < iLen; i++) {
            Context[i] = (byte) (((((Context[i] & 0xff) / 16) * 10) + ((Context[i] & 0xff) % 16)) & 0xff);
            //Log.d(TAG,String.format("%02x",Context[i]));
        }
    }


    /**
     * 毫秒转日期
     *
     * @param milliscond 毫秒数
     * @return
     */
    public static String[] toData(long milliscond) {
        Date date = new Date(milliscond);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String data = simpleDateFormat.format(gc.getTime());
        String[] strData = data.split(",");
        return strData;
    }


}

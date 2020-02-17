package com.hzsun.mpos.Public;

public class NumConvertUtils {


    public static int getUnsignedByte(byte data) {      //将data字节型数据转换为0~255 (0xFF 即BYTE)。
        return data & 0x0FF;
    }

    public static int getUnsignedByte(short data) {      //将data字节型数据转换为0~65535 (0xFFFF 即 WORD)。
        return data & 0x0FFFF;
    }

    public static long getUnsignedIntt(int data) {     //将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
        return data & 0x0FFFFFFFFl;
    }

    public static long getUnsignedLong(long data) {     //将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
        return data & 0xFFFFFFFFFFFFFFFFL;
    }


    public static int bytes2Integer(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            return 0;
        }
        return (bytes[0] & 0xff) + ((bytes[1] & 0xff) << 8) + ((bytes[2] & 0xff) << 16) + ((bytes[3] & 0xff) << 24);
    }

    public static long bytes2UnInteger(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            return 0;
        }
        long num = (bytes[0] & 0xff) + ((bytes[1] & 0xff) << 8) + ((bytes[2] & 0xff) << 16) + ((bytes[3] & 0xff) << 24);

        return num;
    }

    public static short bytes2Short(byte[] bytes) {
        if (bytes == null || bytes.length != 2) {
            return 0;
        }
        return (short) ((bytes[0] & 0xff) + ((bytes[1] & 0xff) << 8));
    }

    public static int bytes2ShortInteger(byte[] bytes) {
        if (bytes == null || bytes.length != 2) {
            return 0;
        }
        return ((bytes[0] & 0xff) + ((bytes[1] & 0xff) << 8));
    }

    public static int Short2int(short num) {
        return (num & 0xff) + (((num >> 8) & 0xff) << 8);
    }

    public static byte[] Integer2bytes(int number) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) ((number >> (i * 8)) & 0xff);
        }
        return bytes;
    }

    public static byte[] Long2bytes(long number) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) ((number >> (i * 8)) & 0xff);
        }
        return bytes;
    }

    public static byte[] UnInteger2bytes(long number) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) ((number >> (i * 8)) & 0xff);
        }
        return bytes;
    }


    public static int Short2Integer(short number) {
        return number & 0xffff;
    }

    public static byte[] Short2bytes(short number) {
        byte[] bytes = new byte[2];
        for (int i = 0; i < 2; i++) {
            bytes[i] = (byte) ((number >> (i * 8)) & 0xff);
        }
        return bytes;
    }

    // 按年钱包复位时间点：CN2*4，4个MMDD，不启用时为0000。
    public static byte[] MMDD2BCD(String str) {
        byte[] bytes = new byte[8];
        String[] bcdarr = str.trim().split(";");
        if (bcdarr == null || bcdarr.length < 4) {
            String[] strarr = new String[4];
            for (int i = 0; i < 4; i++) {
                if (bcdarr != null && bcdarr.length >= (i + 1)) {
                    if (bcdarr[i].length() >= 4) {
                        strarr[i] = bcdarr[i];
                    } else {
                        strarr[i] = "0000";
                    }

                } else {
                    strarr[i] = "0000";
                }
            }
            bcdarr = strarr;
        }
        for (int i = 0; i < 4; i++) {
            String bcd = bcdarr[i];
            bytes[i * 2] = (byte) ((Integer.parseInt(bcd.charAt(0) + "") << 4) + Integer.parseInt(bcd.charAt(1) + ""));
            bytes[(i * 2 + 1)] = (byte) ((Integer.parseInt(bcd.charAt(2) + "") << 4) + Integer.parseInt(bcd.charAt(3) + ""));
        }
        return bytes;
    }

    // 按年钱包复位时间点：CN2*4，4个HHMM，不启用时为0000。
    public static byte[] HHMM2BCD(String str) {
        byte[] bytes = new byte[2];
        String bcd = str.trim();
        bytes[0] = (byte) ((Integer.parseInt(bcd.charAt(0) + "") << 4) + Integer.parseInt(bcd.charAt(1) + ""));
        bytes[1] = (byte) ((Integer.parseInt(bcd.charAt(2) + "") << 4) + Integer.parseInt(bcd.charAt(3) + ""));
        return bytes;
    }

    public static byte[] YYMMDD2BCD(String str) {
        byte[] bytes = new byte[3];
        String bcd = str.trim();
        bytes[0] = (byte) ((Integer.parseInt(bcd.charAt(0) + "") << 4) + Integer.parseInt(bcd.charAt(1) + ""));
        bytes[1] = (byte) ((Integer.parseInt(bcd.charAt(2) + "") << 4) + Integer.parseInt(bcd.charAt(3) + ""));
        bytes[2] = (byte) ((Integer.parseInt(bcd.charAt(4) + "") << 4) + Integer.parseInt(bcd.charAt(5) + ""));
        return bytes;

    }

    public static byte[] YYMMDD2Bytes(String str) {
        byte[] bytes = new byte[3];
        String bcd = str.trim();
        bytes[0] = (byte) (Integer.parseInt(bcd.substring(0, 2)));
        bytes[1] = (byte) (Integer.parseInt(bcd.substring(2, 4)));
        bytes[2] = (byte) (Integer.parseInt(bcd.substring(4, 6)));
        return bytes;

    }

    public static byte[] Add2BCD(int num) {
        byte[] bytes = new byte[5];
        String bcd = NumConvertUtils.addZeroForString(Integer.toString(num), "left", 10);
        bytes[0] = (byte) ((Integer.parseInt(bcd.charAt(0) + "") << 4) + Integer.parseInt(bcd.charAt(1) + ""));
        bytes[1] = (byte) ((Integer.parseInt(bcd.charAt(2) + "") << 4) + Integer.parseInt(bcd.charAt(3) + ""));
        bytes[2] = (byte) ((Integer.parseInt(bcd.charAt(4) + "") << 4) + Integer.parseInt(bcd.charAt(5) + ""));
        bytes[3] = (byte) ((Integer.parseInt(bcd.charAt(6) + "") << 4) + Integer.parseInt(bcd.charAt(7) + ""));
        bytes[4] = (byte) ((Integer.parseInt(bcd.charAt(8) + "") << 4) + Integer.parseInt(bcd.charAt(9) + ""));
        return bytes;

    }

    public static byte Str2SingleBCD(String bcd) {
        bcd = addZeroForString(bcd.trim(), "left", 2);
        return (byte) ((Integer.parseInt(bcd.charAt(0) + "") << 4) + Integer.parseInt(bcd.charAt(1) + ""));

    }


    public static byte[] Str2SingleBCD(String bcd, int size) {
        byte[] bytes = new byte[size];
        String bcdmap = NumConvertUtils.addZeroForString(bcd.trim(), "right", size * 2);
        for (int i = 0; i < size; i++) {
            String bcdstr = bcdmap.substring(i * 2, (i + 1) * 2);
            bytes[i] = (byte) ((Integer.parseInt(bcdstr.charAt(0) + "", 16) << 4) + Integer.parseInt(bcdstr.charAt(1) + "", 16));
        }
        return bytes;
    }

    public static byte[] Str2SingleByte(String bcd, int size) {
        byte[] bytes = new byte[size];
        String bcdmap = NumConvertUtils.addZeroForString(bcd.trim(), "right", size * 2);
        for (int i = 0; i < size; i++) {
            String bcdstr = bcdmap.substring(i * 2, (i + 1) * 2);
            try {
                bytes[i] = (byte) ((NumberFormat.formatHexString(bcdstr.charAt(0) + "") << 4) + NumberFormat.formatHexString(bcdstr.charAt(1) + ""));
            } catch (Exception e) {
                bytes[i] = 0;
            }

        }
        return bytes;
    }

    /**
     * @Description 把int转换成3个字节
     **/

    public static byte[] Integer2Byte3(int num) {
//		StringBuffer numbuffer=new StringBuffer();
//		if(num<0){
//			numbuffer.append("1");
//			num=Math.abs(num);
//		}else{
//			numbuffer.append("0");
//		}
//		byte[] bytes=new byte[3];
//		String numstr=addZeroForString(Integer.toBinaryString(num), "left", 23);
//		numbuffer.append(numstr);
//		String formatstr=numbuffer.toString();
//		for(int i=0, j=3;i<3;i++,j--){
//			String sub=formatstr.substring((j-1)*8,j*8);
//			bytes[i]=ParseMessageUtils.bit2byte(sub);
//		}
        byte[] bytes = Integer2bytes(num);
        byte[] result = new byte[3];
        System.arraycopy(bytes, 0, result, 0, 3);
        return result;
    }


    public static int byte3ToInteger(byte[] bytes) {
        byte[] result = new byte[4];
        System.arraycopy(bytes, 0, result, 0, 3);
        if ((bytes[2] & 0x80) == 0x80) {
            result[3] = (byte) 0xff;
        }
        return bytes2Integer(result);
    }

    public static String addZeroForString(String str, String direction, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                if (direction.equalsIgnoreCase("left")) {
                    sb.append("0").append(str);// 左补0
                } else if (direction.equalsIgnoreCase("right")) {
                    sb.append(str).append("0");// 右补0
                }
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    public static String addspacesForString(Object object, String direction, int strLength) {
        String str = "";
        if (object != null) {
            str = object.toString();
        }
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                if (direction.equalsIgnoreCase("left")) {
                    sb.append(" ").append(str);// 左补0
                } else if (direction.equalsIgnoreCase("right")) {
                    sb.append(str).append(" ");// 右补0
                }
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    public static String Bytes2Hexstr(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();
        for (byte b : bytes) {
            buffer.append(addZeroForString(Integer.toHexString(b & 0xff), "left", 2));
        }
        return buffer.toString();
    }

    public static String Bytes2Hex(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (byte b : bytes) {
            buffer.append("0x" + addZeroForString(Integer.toHexString(b & 0xff), "left", 2));
            buffer.append(",");
        }
        buffer.append("]");
        return buffer.toString();
    }

    public static byte[] strToByteArray(String str) {
        if (str == null) {
            return null;
        }
        byte[] byteArray = str.getBytes();
        return byteArray;
    }

    //byte[]转十六进制String
    //所谓十六进制String，就是字符串里面的字符都是十六进制形式，因为一个byte是八位，可以用两个十六进制位来表示，因此，byte数组中的每个元素可以转换为两个十六进制形式的char，所以最终的HexString的长度是byte数组长度的两倍。闲话少说上代码：
    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //十六进制String转byte[]
    //没什么好说的了，就是byte[]转十六进制String的逆过程，放代码：
    public static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static String reverseString(String Str) {
        return new StringBuffer(Str).reverse().toString();
    }

    public static long byte2Long(byte[] bytes) {
        long num = 0;
        if ((bytes[8] & 0x80) == 0x80) {
            for (int i = 0; i < 7; i++) {
                num += ((~bytes[i]) & 0xff) << (i * 8);
            }
            num += (((~bytes[8]) & 0xff) | 0x80) << 64;

        } else {
            for (int i = 0; i < 8; i++) {
                num += (bytes[i] & 0xff) << (i * 8);
            }
        }
        return num;
    }

    public static String byte2BCD(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();
        for (byte bt : bytes) {
            buffer.append(Integer.toHexString((bt & 0xff) >> 4));
            buffer.append(Integer.toHexString(bt & 0x0f));
        }
        return buffer.toString();
    }
}


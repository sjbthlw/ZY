package com.hzsun.mpos.Public;

import static java.util.Arrays.fill;

public class Utility {

    public static final String FLAG = "Flag";

    public static int memcmp(byte[] cData, byte[] cData1, int iLen) {

        for (int i = 0; i < iLen; i++) {
            if (cData[i] != cData1[i])
                return 1;
        }
        return 0;
    }

    public static int memcmp(byte[] cData, int iDataPos, byte[] cData1, int iDataPos1, int iLen) {

        for (int i = 0; i < iLen; i++) {
            if (cData[i + iDataPos] != cData1[i + iDataPos1])
                return 1;
        }
        return 0;
    }

    public static void memcpy(byte[] destData, byte[] srcData, int iLen) {
        if (destData.length >= srcData.length)
            iLen = srcData.length;
        else
            iLen = destData.length;
        System.arraycopy(srcData, 0, destData, 0, iLen);
    }

    public static void memcpy(byte[] destData, int destPos, byte[] srcData, int srcPos, int iLen) {

        System.arraycopy(srcData, srcPos, destData, destPos, iLen);
    }

    public static void memset(byte[] Data, byte bValue, int iLen) {
        fill(Data, (byte) bValue);
    }

}



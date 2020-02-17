package com.hzsun.mpos.data;

import android.util.Log;

import com.hzsun.mpos.Public.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

import struct.JavaStruct;
import struct.StructException;

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_SingleBond_File;

public class OddKeyInfoRW {


    private static final String TAG = "OddKeyInfoRW";
    private final static int VersionLen = 4;//版本号4字节

    //创建单键工作参数文件
    public static int CreateOddKeyInfo() {
        //printf("------------OddKey file size %d\n-------", FileLen);
        //创建文件
        if (FileUtils.CreateDataFile(en_SingleBond_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取单键工作参数版本号
    public static byte[] ReadOddKeyVer() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_SingleBond_File);
        if (sFileName == "")
            return null;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                byte[] bytes = new byte[4];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                return bytes;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            }
        } else {
            Log.e(TAG, "单键工作参数不存在,重新创建文件");
            CreateOddKeyInfo();
            byte[] bytes = new byte[4];
            return bytes;
        }
        return null;
    }

    //写单键参数版本号
    public static int WriteOddKeyVer(byte[] byteVer) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_SingleBond_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            byte[] bytes = new byte[4];
            System.arraycopy(byteVer, 0, bytes, 0, bytes.length);

            RandomAccessFile random = null;
            try {
                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.write(bytes);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                //文件读写出现异常！
                Log.d(TAG, e.getMessage());

            } finally {
                try {
                    random.close();
                    return 0;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    //读取单键工作参数文件
    public static OddKeyInfo ReadOddKeyInfo() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_SingleBond_File);
        if (sFileName == "")
            return null;

        lngPos = VersionLen;
        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                byte[] bytes = new byte[1024];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                OddKeyInfo StructData = new OddKeyInfo();
                JavaStruct.unpack(StructData, bytes, ByteOrder.LITTLE_ENDIAN);
                //JavaStruct.unpack(StructData, bytes);
                return StructData;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            } catch (StructException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            }
        } else {
            Log.e(TAG, "单键工作参数不存在,重新创建文件");
            CreateOddKeyInfo();
            OddKeyInfo StructData = new OddKeyInfo();
            return StructData;
        }
        return null;
    }

    //写单键参数数据
    public static int WriteAllOddKeyInfo(OddKeyInfo StructData) {
        long lngPos = VersionLen;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_SingleBond_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            try {
                byte[] bytes = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN);
                //byte[] bytes = JavaStruct.pack(StructData);
                RandomAccessFile random = null;
                try {
                    random = new RandomAccessFile(fFile, "rwd");
                    random.seek(lngPos);
                    random.write(bytes);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    //文件读写出现异常！
                    Log.d(TAG, e.getMessage());

                } finally {
                    try {
                        random.close();
                        return 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (StructException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static int SaveInitOddKeyInfo(byte[] bytes) {
        int iRet;
        int i = 0, n = 0;
        byte OneDownloadCount;
        int lngTemp;
        int wCRC16;
        OddKeyInfo StructData = new OddKeyInfo();

        i = 0;

        //单次下发数量
        OneDownloadCount = (byte) (bytes[i++] & 0xff);

        //单键组号
        StructData.cOddKeyID = (byte) (bytes[i++] & 0xff);

        //key 0--key 9 key Y1-Y4
        for (n = 0; n < 14; n++) {
            lngTemp = (bytes[i++] & 0xff);
            lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
            StructData.wKeyMoney[n] = (short) lngTemp;
        }
        StructData.CRC16[0] = (byte) (bytes[i++] & 0xff);
        StructData.CRC16[1] = (byte) (bytes[i++] & 0xff);

        //保存参数
        iRet = WriteAllOddKeyInfo(StructData);
        if (iRet != 0)
            return iRet;

        return 0;
    }

}

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

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_FTPServer_File;

public class FTPInfoRW {

    private static final String TAG = "FTPInfoRW";

    //创建FTP工作参数文件
    public static int CreateFTPInfo() {
        //printf("------------FTPInfo file size %d\n-------", FileLen);
        //创建文件
        if (FileUtils.CreateDataFile(en_FTPServer_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取FTP工作参数文件
    public static FTPInfo ReadFTPInfo() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FTPServer_File);
        if (sFileName == "")
            return null;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                byte[] bytes = new byte[1024];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                FTPInfo StructData = new FTPInfo();
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
        }
        return null;
    }

    //写FTP参数数据
    public static int WriteAllFTPInfo(FTPInfo StructData) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FTPServer_File);
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


    //网络写ftp参数
    public static int SaveFTPServerInfo(byte[] bytes) {
//        FTP服务器IP	4字节
//        FTP端口号	2字节	默认 21
//        FTP用户名	16字节	字符形式
//        FTP密码	16字节	字符形式
//        下载照片文件夹	16字节	字符形式，用户照片以账号命名，如“12345678.png”
//        上传照片文件夹	16字节	字符形式，抓拍照片以站点号+站点流水号+账号命名，如“6_1_12345678.png”

        int iRet;
        int n = 0;
        byte[] bt2 = new byte[2];
        byte[] bt4 = new byte[4];
        byte[] bt8 = new byte[8];
        byte[] bt16 = new byte[16];
        byte[] bt64 = new byte[64];
        byte[] bt32 = new byte[32];

        FTPInfo StructData = new FTPInfo();
        System.arraycopy(bytes, n, StructData.cFTPServerIP, 0, 4);
        n += 4;
        //端口
        StructData.wFTPServerPort = bytes[n] + (bytes[n + 1] << 8);
        n += 2;
        //用户名
        System.arraycopy(bytes, n, StructData.cFTPUserName, 0, 16);
        n += 16;
        //密码
        System.arraycopy(bytes, n, StructData.cFTPPassWord, 0, 16);
        n += 16;
        //上传文件夹路径
        System.arraycopy(bytes, n, StructData.cDownFTPPath, 0, 16);
        n += 16;
        //上传文件夹路径
        System.arraycopy(bytes, n, StructData.cUpFTPPath, 0, 16);

        iRet = WriteAllFTPInfo(StructData);

        return iRet;
    }
}

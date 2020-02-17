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

import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_LocalInfo_File;

public class LocalInfoRW {

    private static final String TAG = "LocalInfoRW";

    //创建本地工作参数文件
    public static int CreateLocalInfo() {
        //printf("------------Local file size %d\n-------", FileLen);
        //创建文件
        if (FileUtils.CreateDataFile(en_LocalInfo_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取本地工作参数文件
    public static LocalInfo ReadLocalInfo() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_LocalInfo_File);
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
                LocalInfo StructData = new LocalInfo();
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

    //写本地参数数据
    public static int WriteAllLocalInfo(LocalInfo StructData) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_LocalInfo_File);
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

    //设置相关参数
    public static void SetLocalInfo() {
        g_LocalInfo.cFaceDetectFlag = 1;
        g_LocalInfo.iMaxFaceNum = 100000;
        if (g_LocalInfo.iPupilDistance == 0)
            g_LocalInfo.iPupilDistance = 10;  // 瞳距

        if (g_LocalInfo.fLiveThrehold == 0.0)
            g_LocalInfo.fLiveThrehold = (float) 0.20; //人脸活体检测率

        if (g_LocalInfo.fFraction == 0.0)
            g_LocalInfo.fFraction = (float) 0.65;      //人脸相识率

        if (g_LocalInfo.iPayShowTime == 0)
            g_LocalInfo.iPayShowTime = 3;//默认值

        if (g_LocalInfo.cPowerSaveTimeA < 1)
            g_LocalInfo.cPowerSaveTimeA = 5;//默认值

        if (g_LocalInfo.cPowerSaveTimeA > 30)
            g_LocalInfo.cPowerSaveTimeA = 30;//默认值

    }

}

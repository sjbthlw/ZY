package com.hzsun.mpos.data;

import android.util.Log;

import com.hzsun.mpos.Public.FileUtils;
import com.hzsun.mpos.Public.NumConvertUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

import struct.JavaStruct;
import struct.StructException;

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_System_File;

public class SystemInfoRW {

    private static final String TAG = "SystemInfoRW";

    //创建系统工作参数文件
    public static int CreateSystemInfo() {
        //创建文件
        if (FileUtils.CreateDataFile(en_System_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取系统工作参数文件
    public static SystemInfo ReadSystemInfo() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_System_File);
        if (sFileName == "")
            return null;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                byte[] bytes = new byte[2048];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                SystemInfo StructData = new SystemInfo();
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
            Log.e(TAG, "系统工作参数文件不存在,重新创建文件");
            CreateSystemInfo();
            SystemInfo StructData = new SystemInfo();
            return StructData;
        }
        return null;
    }

    //写系统参数数据
    public static int WriteAllSystemInfo(SystemInfo StructData) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_System_File);
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

    //写系统参数版本号
    public static int WriteSystemVer(byte[] cSystemVer) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_System_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            //byte[] bytes = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN);
            //byte[] bytes = JavaStruct.pack(StructData);
            byte[] bytes = new byte[4];
            System.arraycopy(cSystemVer, 0, bytes, 0, bytes.length);

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

    //网络写系统参数
    public static int SaveSystemInfo(byte[] bytes) {

//        版本号	4字节
//        代理号	1字节
//        客户号	2字节
//        基本扇区号	1字节
//        扩展扇区号	1字节
//        卡种识别	1字节	0 按各自卡种识别 ；1 当M1卡识别
//        是否启用支付宝管控	1字节	0 不启用 1启用
//        代理服务器长度	1字节
//        代理服务器内容	X字节	支付宝管控SDK的代理服务器
//        人脸服务器长度	1字节
//        人脸服务器内容	X字节	支付宝管控SDK的代理服务器
//        是否启用人脸识别	1字节	0 不启用 1启用

//        参数校验码	2字节	从“代理号”到“卡种启用情况”的累加和

        int iRet;
        int index = 0;
        byte[] bt2 = new byte[2];

        SystemInfo StructData = new SystemInfo();
        //版本号 4
        System.arraycopy(bytes, index, StructData.cVersion, 0, 4);
        index += 4;
        // 代理号 1
        StructData.cAgentID = bytes[index++];
        //客户号
        System.arraycopy(bytes, index, bt2, 0, 2);
        index += 2;
        StructData.iGuestID = (NumConvertUtils.bytes2Short(bt2));
        StructData.cBasicSectorID = bytes[index++];            //基本扇区号
        StructData.cExtendSectorID = bytes[index++];        //扩展扇区号
        StructData.cCardMode = bytes[index++];                //卡片识别模式 0：正常 1：只读M1
        StructData.cOnlyOnlineMode = (byte) ((bytes[index++] & 0xf0) >> 4);            // 在线交易模式 0：不启用 1：启用  前4位为在线刷卡交易模式，后4位为是否启用支付宝管控
        StructData.cAliPayCtlSDK = (byte) (bytes[index] & 0x0f);            //支付宝管控启用 0：不启用 1：启用  前4位为在线刷卡交易模式，后4位为是否启用支付宝管控
        StructData.cProxyServerLen = (char) (bytes[index++] & 0xff);        //代理服务器长度	1字节
        if ((StructData.cProxyServerLen > 0) && (StructData.cProxyServerLen < 256)) {
            //代理服务器内容	X字节	支付宝管控SDK的代理服务器
            System.arraycopy(bytes, index, StructData.strProxyServer, 0, StructData.cProxyServerLen);
            String strProxyServerURL = new String(StructData.strProxyServer, 0, StructData.cProxyServerLen);
            Log.e(TAG, "strProxyServerURL:" + strProxyServerURL);
        } else {
            StructData.iFacehttpLength = 0;
            Log.e(TAG, "人脸HTTP地址长度错误");
        }
        index = index + StructData.cProxyServerLen;
        StructData.iFacehttpLength = (char) (bytes[index++] & 0xff);        //人脸服务器长度	1字节

        if (Character.isDigit(StructData.iFacehttpLength)) {  // 判断是否是数字
            int num = Integer.parseInt(String.valueOf(StructData.iFacehttpLength));
            Log.e(TAG, "人脸服务器长度:" + num);
        }

        //人脸http地址
        if ((StructData.iFacehttpLength > 0) && (StructData.iFacehttpLength < 256)) {
            //人脸服务器内容	X字节
            System.arraycopy(bytes, index, StructData.FaceHTTPServerAdr, 0, StructData.iFacehttpLength);
            String strFacepathURL = new String(StructData.FaceHTTPServerAdr, 0, StructData.iFacehttpLength);
            Log.e(TAG, "strFacepathURL:" + strFacepathURL);
        } else {
            StructData.iFacehttpLength = 0;
            Log.e(TAG, "人脸HTTP地址长度错误");
        }
        index = index + StructData.iFacehttpLength;
        StructData.cFaceDetectFlag = (byte) (bytes[index++] & 0xff);        //是否支持人脸识别
        Log.e(TAG, "是否支持人脸识别:" + StructData.cFaceDetectFlag);
        //校验码
        StructData.CRC16[0] = bytes[index++];
        StructData.CRC16[1] = bytes[index++];

        iRet = WriteAllSystemInfo(StructData);
        return iRet;
    }



}

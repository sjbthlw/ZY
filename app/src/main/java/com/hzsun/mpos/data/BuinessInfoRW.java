package com.hzsun.mpos.data;

import android.util.Log;

import com.hzsun.mpos.Public.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import struct.JavaStruct;
import struct.StructException;

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_TimeGroup_File;

public class BuinessInfoRW {


    private static final String TAG = "BuinessInfoRW";
    private final static int VersionLen = 4;//版本号4字节
    private final static int DataLen = 10;//每组参数10字节
    private final static int BuinessCount = 128;//128组营业分组

    //创建营业分组工作参数文件
    public static int CreateBuinessInfo() {
        //创建文件
        if (FileUtils.CreateDataFile(en_TimeGroup_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取营业分组工作参数版本号
    public static byte[] ReadBuinessVer() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_TimeGroup_File);
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
        }
        return null;
    }

    //写营业分组工作参数版本号
    public static int WriteBuinessVer(byte[] byteVer) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_TimeGroup_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.write(byteVer);

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

    //读取营业分组工作参数文件
    public static BuinessInfo ReadBuinessInfo(byte cBuinessID) {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_TimeGroup_File);
        if (sFileName == "")
            return null;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                BuinessInfo StructData = new BuinessInfo();
                int iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                byte[] bytes = new byte[iDataLen];
                lngPos = VersionLen + iDataLen * cBuinessID;
                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
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

    //读取所有营业分组工作参数文件
    public static List<BuinessInfo> ReadAllBuinessInfo() {
        long lngPos = 0;

        List<BuinessInfo> list = new ArrayList<BuinessInfo>();

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_TimeGroup_File);
        if (sFileName == "")
            return null;

        lngPos = VersionLen;
        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                BuinessInfo StructData = new BuinessInfo();
                int iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                byte[] bytes = new byte[iDataLen * BuinessCount];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                for (int i = 0; i < BuinessCount; i++) {
                    byte[] Tempbytes = new byte[iDataLen];
                    System.arraycopy(bytes, i * iDataLen, Tempbytes, 0, iDataLen);
                    StructData = new BuinessInfo();
                    JavaStruct.unpack(StructData, Tempbytes, ByteOrder.LITTLE_ENDIAN);
                    //JavaStruct.unpack(StructData, Tempbytes);
                    list.add(i, StructData);
                }

                return list;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, e.getMessage());
            } catch (StructException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        } else {
            Log.e(TAG, "营业分组参数不存在,重新创建文件");
            CreateBuinessInfo();
            return list;
        }
        return null;
    }

    //写营业分组参数数据
    public static int WriteBuinessInfo(BuinessInfo StructData, int cBuinessID) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_TimeGroup_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            try {
                byte[] bytes = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN);
                int iDataLen = bytes.length;
                lngPos = VersionLen + iDataLen * cBuinessID;
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

    //写所有营业分组参数数据
    public static int WriteAllBuinessInfo(List<BuinessInfo> list) {
        int lngPos = 0;
        int byteCount = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_TimeGroup_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            try {
                BuinessInfo StructData = new BuinessInfo();
                int iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                byte[] bytes = new byte[iDataLen * BuinessCount];

                for (int i = 0; i < list.size(); i++) {
                    byte[] Tempbytes = JavaStruct.pack(list.get(i), ByteOrder.LITTLE_ENDIAN);
                    System.arraycopy(Tempbytes, 0, bytes, i * iDataLen, iDataLen);
                    byteCount = byteCount + iDataLen;
                }
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

    //清除营业分组参数数据
    public static int ClearBuinessInfo(int cBuinessID) {
        int lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_TimeGroup_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                BuinessInfo StructData = new BuinessInfo();
                int iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                lngPos = VersionLen + iDataLen * cBuinessID;
                byte[] bytes = new byte[iDataLen];
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

    //网络写营业分组数据
    public static int SaveInitBuinessInfo(byte[] bytes) {
        int iRet;
        int i = 0, j = 0;
        byte cBusinessID = 0;
        int OneDownloadCount = 0;

        /*
        数量	1字节

        时间段序号	1字节
        起始时间	2字节
        结束时间	2字节
        校验码	2字节
        ……
         **/

        i = 0;
        //单次下发数量
        OneDownloadCount = bytes[i++];
        Log.i(TAG, "下发数量：" + OneDownloadCount);

        for (j = 0; j < OneDownloadCount; j++) {
            BuinessInfo pBuinessInfo = new BuinessInfo();

            //时段序列号
            cBusinessID = bytes[i++];
            pBuinessInfo.cBusinessID = cBusinessID;

            //起始时间
            pBuinessInfo.cStartTime[0] = bytes[i++];
            pBuinessInfo.cStartTime[1] = bytes[i++];

            //结束时间
            pBuinessInfo.cEndTime[0] = bytes[i++];
            pBuinessInfo.cEndTime[1] = bytes[i++];

            //校验码
            pBuinessInfo.CRC16[0] = bytes[i++];
            pBuinessInfo.CRC16[1] = bytes[i++];

            iRet = WriteBuinessInfo(pBuinessInfo, (byte) (cBusinessID - 1));
            if (iRet != 0)
                return -1;
        }
        return 0;
    }

}

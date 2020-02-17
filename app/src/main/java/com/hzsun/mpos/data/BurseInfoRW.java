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

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_Purse_File;

public class BurseInfoRW {

    private static final String TAG = "BurseInfoRW";
    private final static int VersionLen = 4;//版本号4字节
    private final static int DataLen = 10;//每组参数10字节

    //创建钱包工作参数文件
    public static int CreateBurseInfo() {
        //创建文件
        if (FileUtils.CreateDataFile(en_Purse_File) != 0) {
            return -1;
        }
        return 0;
    }

    //删除钱包数据
    public static int ClearAllPurseInfo() {
        return 0;
    }

    //读取钱包工作参数版本号
    public static byte[] ReadBurseVer() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_Purse_File);
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
            Log.e(TAG, "钱包工作参数文件不存在,重新创建文件");
            CreateBurseInfo();
            byte[] bytes = new byte[4];
            return bytes;
        }
        return null;
    }

    //写钱包工作参数版本号
    public static int WriteBurseVer(byte[] byteVer) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_Purse_File);
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

    //读取钱包工作参数文件
    public static BurseInfo ReadBurseInfo(byte cBurseID) {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_Purse_File);
        if (sFileName == "")
            return null;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                BurseInfo StructData = new BurseInfo();
                int iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                lngPos = VersionLen + iDataLen * cBurseID;
                byte[] bytes = new byte[iDataLen];

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

    //读取所有钱包工作参数文件
    public static List<BurseInfo> ReadAllBurseInfo() {
        int lngPos = 0;
        int byteCount = 0;

        List<BurseInfo> list = new ArrayList<BurseInfo>();

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_Purse_File);
        if (sFileName == "")
            return null;

        lngPos = VersionLen;
        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                BurseInfo StructData = new BurseInfo();
                int iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                byte[] bytes = new byte[iDataLen * 8];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                for (int i = 0; i < 8; i++) {
                    byte[] Tempbytes = new byte[iDataLen];
                    StructData = new BurseInfo();
                    System.arraycopy(bytes, i * iDataLen, Tempbytes, 0, iDataLen);
                    JavaStruct.unpack(StructData, Tempbytes, ByteOrder.LITTLE_ENDIAN);
                    //JavaStruct.unpack(StructData, Tempbytes);
                    list.add(i, StructData);
                }
                return list;
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
            Log.e(TAG, "钱包工作参数文件不存在,重新创建文件");
            CreateBurseInfo();
            return list;
        }
        return null;
    }

    //写钱包参数数据
    public static int WriteBurseInfo(BurseInfo StructData, byte cBurseID) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_Purse_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            try {
                byte[] bytes = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN);
                int iDataLen = bytes.length;
                //byte[] bytes = JavaStruct.pack(StructData);
                lngPos = VersionLen + iDataLen * cBurseID;
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

    //写所有钱包参数数据
    public static int WriteAllBurseInfo(List<BurseInfo> list) {
        int lngPos = VersionLen;
        int byteCount = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_Purse_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            BurseInfo StructData = new BurseInfo();
            try {
                int iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                byte[] bytes = new byte[iDataLen * 8];
                for (int i = 0; i < list.size(); i++) {
                    StructData = list.get(i);
                    byte[] Tempbytes = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN);
                    //byte[] bytes = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN);
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

    //网络写初始钱包数据
    public static int SaveInitPurseInfo(byte[] bytes) {
        int iRet = 0;
        int i = 0, j = 0;
        byte cBurseID, OneDownloadCount;
        List<BurseInfo> listStructData = new ArrayList<BurseInfo>();

        i = 0;
        //单次下发钱包数量
        OneDownloadCount = bytes[i++];
        Log.i(TAG, "单次下发钱包数量 :" + OneDownloadCount);

        //for(j=0; j<OneDownloadCount; j++)
        for (j = 0; j < 8; j++) {
            //电子钱包号
            cBurseID = bytes[i++];
            Log.i(TAG, "电子钱包号:" + cBurseID);

            if (cBurseID > 8) {
                Log.i(TAG, "钱包号超过范围");
                return 1;
            }
            BurseInfo StructData = new BurseInfo();
            StructData.cBurseID = cBurseID;
            StructData.cCanPermitChase = bytes[i++];           //是否允许追扣	1	0不允许 1允许
            StructData.cCanMoneyClear = bytes[i++];            //是否允许余额周期复位	1	0不允许 1允许
            StructData.cMoneyClearUnit = bytes[i++];           //复位周期单位	1	0年 1月
            System.arraycopy(bytes, i, StructData.cMoneyClearTime, 0, 2);//复位周期	1
            i = i + 2;
            StructData.cBlockID = bytes[i++];                 //工作钱包对应块号	1	正本
            StructData.cBakBlockID = bytes[i++];             //工作钱包对应块号	1	备份
            //校验码
            StructData.CRC16[0] = bytes[i++];
            StructData.CRC16[1] = bytes[i++];

            listStructData.add(j, StructData);
        }
        iRet = WriteAllBurseInfo(listStructData);

        return iRet;
    }
}

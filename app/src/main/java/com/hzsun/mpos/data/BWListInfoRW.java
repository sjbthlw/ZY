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

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_CardBWList_File;

public class BWListInfoRW {


    private static final String TAG = "BWListInfoRW";
    private final static int DataLen = 24;//信息头长度

    //创建黑白名单参数文件
    public static int CreateBWListInfo() {
        //创建文件
        if (FileUtils.CreateDataFile(en_CardBWList_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取黑白名单参数数据
    public static BWListInfo ReadBWListInfo() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_CardBWList_File);
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
                BWListInfo StructData = new BWListInfo();
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

    //写黑白名单参数数据
    public static int WriteBWListInfo(BWListInfo StructData) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_CardBWList_File);
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


    //读取黑白名单所有参数数据
    public static BWListAllInfo ReadAllBWListInfo() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_CardBWList_File);
        if (sFileName == "")
            return null;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                byte[] bytes = new byte[1024 * 256];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                BWListAllInfo StructAllData = new BWListAllInfo();
                BWListInfo StructData = new BWListInfo();
                int iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                //黑白名单信息头 22字节
                JavaStruct.unpack(StructAllData.BlackWListInfo, bytes, ByteOrder.LITTLE_ENDIAN);
                //JavaStruct.unpack(StructData, bytes);
                //黑白名单数据
                System.arraycopy(bytes, iDataLen, StructAllData.BlackBit, 0, StructAllData.BlackBit.length);

                return StructAllData;
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

    //写黑白名单参数数据
    public static int WriteAllBWListInfo(BWListInfo StructData) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_CardBWList_File);
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

    //保存初始黑名名单参数
    public static int SaveInitBitBWList(byte[] Buffer, byte[] BlackBit) {
        int i;
        long SaveAddr;
        int num;
        long FilePos;
        int FileLen;

        Log.i(TAG, "SaveInitBitBWList");
        i = 0;

        /*
        黑白名单字节数量	2字节	<=512
        存储地址	4字节
        黑白名单Bit位字节	X字节
        */

        //黑白名单字节数量
        num = (Buffer[i++] & 0xff);
        num += (Buffer[i++] & 0xff) * 256;

        Log.i(TAG, "bit blackwhite count :" + num);

        //存储地址
        SaveAddr = (Buffer[i] & 0xff);
        i++;
        SaveAddr += (Buffer[i] & 0xff) * 256;
        i++;
        SaveAddr += (Buffer[i] & 0xff) * 256 * 256;
        i++;
        SaveAddr += ((long) Buffer[i] & 0xff) * 256 * 256 * 256;
        i++;

        Log.i(TAG, "bit blackwhite SaveAddr == " + SaveAddr);

        //黑白名单bit位字节
        System.arraycopy(Buffer, i, BlackBit, (int) SaveAddr, num);

        //计算写入的位置
        FilePos = (DataLen + (SaveAddr));
        FileLen = num;
        byte[] WriteBytes = new byte[num];
        System.arraycopy(Buffer, i, WriteBytes, 0, num);
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_CardBWList_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                random = new RandomAccessFile(fFile, "rwd");
                random.seek(FilePos);
                random.write(WriteBytes);

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


    //保存变更黑名名单参数
    public static int SaveChangeBYTEBWList(byte[] Buffer, byte[] BlackBit) {
        int i, j;
        long FilePos, FileLen;
        short OneDownloadCount, IOFlag;
        long CardCode;

        int Index = 0, StartIndex = 0, EndIndex = 0;
        byte m;

        /*
        变更数量	1字节	<20
        变更标志	1字节	1进 2出
        卡内编号	3字节
        */

        i = 0;
        //黑白名单数量
        OneDownloadCount = (short) (Buffer[i++] & 0xff);

        //黑白名单
        for (j = 0; j < OneDownloadCount; j++) {
            //变更标志
            IOFlag = (short) (Buffer[i++] & 0xff);
            //卡内编号
            CardCode = (Buffer[i++] & 0xff);
            CardCode += ((Buffer[i++] & 0xff) << 8);
            CardCode += ((Buffer[i++] & 0xff) << 16);

            //更新bit数组
            Index = (int) ((CardCode - 1) / 8);
            m = (byte) ((CardCode - 1) % 8);

            Log.i(TAG, "CardCode:" + CardCode + " IOFlag:" + IOFlag + "Index:" + Index);

            if (IOFlag == 1) {
                //白名单
                BlackBit[Index] |= (0x01 << (m));
            } else if (IOFlag == 2) {
                //黑名单
                BlackBit[Index] &= (~(0x01 << (m)));
            }
        }
        //计算写入的位置
        FilePos = DataLen;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_CardBWList_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                random = new RandomAccessFile(fFile, "rwd");
                random.seek(FilePos);
                random.write(BlackBit);

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
}

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

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_IdentityDiscount_File;

public class StatusPriInfoRW {

    private static final String TAG = "StatusPriInfoRW";
    private final static int VersionLen = 4;//版本号4字节
    private final static int DataLen = 19;//每组身份优惠参数16字节+身份号

    //创建身份优惠工作参数文件
    public static int CreateStatusPriInfo() {
        //创建文件
        if (FileUtils.CreateDataFile(en_IdentityDiscount_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取身份优惠工作参数版本号
    public static byte[] ReadStatusPriVer() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_IdentityDiscount_File);
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
            Log.e(TAG, "身份优惠工作参数不存在,重新创建文件");
            CreateStatusPriInfo();
            byte[] bytes = new byte[4];
            return bytes;
        }
        return null;
    }


    //写身份优惠工作参数版本号
    public static int WriteStatusPriVer(byte[] byteVer) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_IdentityDiscount_File);
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

    //读取身份优惠工作参数文件
    public static StatusPriInfo ReadStatusPriInfo(int cStatusID) {
        long lngPos = 0;
        long lngBurPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_IdentityDiscount_File);
        if (sFileName == "")
            return null;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                StatusPriInfo StructData = new StatusPriInfo();
                int iDataLen = 0;
                iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                byte[] bytes = new byte[iDataLen];
                lngPos = VersionLen + iDataLen * cStatusID;
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
        } else {
            CreateStatusPriInfo();
            InitAllStatusPriInfo();
        }
        return null;
    }

    //读取所有身份优惠工作参数文件
    public static List<StatusPriInfo> ReadAllStatusPriInfo() {
        List<StatusPriInfo> Statuslist = new ArrayList<StatusPriInfo>();

        for (byte cStatusID = 0; cStatusID < 64; cStatusID++) {
            StatusPriInfo StructStatusInfo = new StatusPriInfo();
            StructStatusInfo = ReadStatusPriInfo(cStatusID);
            if (StructStatusInfo != null)
                Statuslist.add(cStatusID, StructStatusInfo);
            else
                return null;
        }

        return Statuslist;
    }

    //写身份优惠参数数据
    public static int WriteStatusPriInfo(StatusPriInfo StructData, byte cStatusID) {
        int lngPos = 0;
        int lngBurPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_IdentityDiscount_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            try {
                byte[] bytes = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN);
                lngPos = VersionLen + bytes.length * cStatusID;
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

    //写所有身份优惠参数数据
    public static int WriteAllStatusPriInfo(List<StatusPriInfo> Statuslist) {
        int iRet = 0;
        long lngPos = 0;

        for (byte cStatusID = 0; cStatusID < 64; cStatusID++) {
            iRet = WriteStatusPriInfo(Statuslist.get(cStatusID), cStatusID);
            if (iRet != -1) {
                continue;
            } else {
                return -1;
            }
        }
        return 0;
    }

    //写所有身份优惠参数数据
    public static int InitAllStatusPriInfo() {
        int iRet = 0;
        long lngPos = 0;

        for (byte cStatusID = 0; cStatusID < 64; cStatusID++) {
            StatusPriInfo pStatusPriInfo = new StatusPriInfo();
            iRet = WriteStatusPriInfo(pStatusPriInfo, cStatusID);
            if (iRet != -1) {
                continue;
            } else {
                return -1;
            }
        }
        return 0;
    }


    //清除身份优惠参数数据
    public static int ClearStatusPriInfo(byte cStatusID) {
        int lngPos = 0;
        int byteCount = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_IdentityDiscount_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            try {
                StatusPriInfo StructData = new StatusPriInfo();
                int iDataLen = 0;
                iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                byte[] bytes = new byte[iDataLen];
                lngPos = VersionLen + iDataLen * cStatusID;

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

    //网络写身份优惠参数
    public static int SaveInitStatusPri(byte[] bytes) {
        int iRet = 0;
        int i = 0, j = 0;
        byte cStatusID;
        byte OneDownloadCount;
        //List<StatusPriInfo> StatusPrilist=new ArrayList<StatusPriInfo>();

        /*
        数量	1字节	<=40

        //身份号	1字节
        char cStatusID;
        //优惠时段范围	16字节
        u8 bPrivilegeTime[16];
        //校验码
        u8 CRC16[2];
        ……
         */

        //单次下发数量
        OneDownloadCount = bytes[i++];

        for (j = 0; j < OneDownloadCount; j++) {
            StatusPriInfo pStatusPriInfo = new StatusPriInfo();
            //身份号
            cStatusID = bytes[i++];
            if ((cStatusID > 64) || (cStatusID < 1)) {
                Log.i(TAG, "身份号超过范围");
                return -1;
            }

            //身份号	1字节
            pStatusPriInfo.cStatusID = cStatusID;
            //优惠时段范围	16字节
            System.arraycopy(bytes, i, pStatusPriInfo.bPrivilegeTime, 0, 16);
            i = i + 16;
            //校验码
            pStatusPriInfo.CRC16[0] = bytes[i++];
            pStatusPriInfo.CRC16[1] = bytes[i++];

            //保存参数
            iRet = WriteStatusPriInfo(pStatusPriInfo, (byte) (cStatusID - 1));
            if (iRet != 0)
                return -1;
        }
        return 0;
    }

}

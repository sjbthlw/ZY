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

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_IdentityWallet_File;

public class StatusBurInfoRW {

    private static final String TAG = "StatusBurInfoRW";
    private final static int VersionLen = 4;//版本号4字节
    private final static int BurDataLen = 61;//每组身份钱包参数61字节
    private final static int DataLen = 1 + BurDataLen * 8 + 16;//每组参数410字节(64个身份)

//    //身份参数
//    public static class StatusInfo {
//
//        byte cStatusID;					//身份号
//        //身份钱包参数 //每个身份包含8组钱包参数
//        //com.hzsun.mpos.data.StatusBurInfo[] StatusBurInfoArr=new StatusBurInfo[8];
//        List<StatusBurInfo> StatusInfolist = new ArrayList<StatusBurInfo>();
//        byte[] bPaymentLimitTime=new byte[16];     //消费限次时段
//
//    }

    //创建身份钱包工作参数文件
    public static int CreateStatusBurInfo() {
        //Log.i(TAG,"------------StatusBurInfo file size %d\n-------", FileLen);
        //创建文件
        if (FileUtils.CreateDataFile(en_IdentityWallet_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取身份钱包工作参数版本号
    public static byte[] ReadStatusBurVer() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_IdentityWallet_File);
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
            Log.e(TAG, "身份钱包工作参数不存在,重新创建文件");
            CreateStatusBurInfo();
            byte[] bytes = new byte[4];
            return bytes;
        }
        return null;
    }


    //写身份钱包工作参数版本号
    public static int WriteStatusBurVer(byte[] byteVer) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_IdentityWallet_File);
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

    //删除身份钱包工作参数文件
    public static int ClearStatusBur(int cStatusID) {
        int lngPos = 0;
        int lngBurPos = 0;
        int lngStatusInfoLen = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_IdentityWallet_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            try {
                StatusBurInfo StructData = new StatusBurInfo();
                int iDataLen = 0;
                iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                //身份钱包参数 //每个身份包含1身份号+8组钱包参数+16字节消费限次时段
                lngStatusInfoLen = 1 + iDataLen * 8 + 16;
                byte[] bytes = new byte[lngStatusInfoLen];
                lngPos = VersionLen + lngStatusInfoLen * (cStatusID - 1);

                RandomAccessFile random = null;
                try {
                    random = new RandomAccessFile(fFile, "rwd");
                    random.seek(lngPos);
                    random.write(bytes);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    //文件读写出现异常！
                    Log.e(TAG, e.getMessage());
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

    //读取身份钱包工作参数文件
    public static StatusInfo ReadStatusBurInfo(int cStatusID) {
        long lngPos = 0;
        long lngBurPos = 0;
        int lngStatusInfoLen = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_IdentityWallet_File);
        if (sFileName == "")
            return null;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                StatusBurInfo StructData = new StatusBurInfo();
                int iDataLen = 0;
                iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                //身份钱包参数 每个身份包含1身份号+8组钱包参数+16字节消费限次时段
                lngStatusInfoLen = 1 + iDataLen * 8 + 16;
                byte[] bytes = new byte[lngStatusInfoLen];
                lngPos = VersionLen + lngStatusInfoLen * (cStatusID);

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                StatusInfo StatusStructData = new StatusInfo();
                StatusStructData.cStatusID = bytes[0];//身份号
                lngBurPos = 1;
                //身份钱包参数 //每个身份包含8组钱包参数
                List<StatusBurInfo> StatusInfolist = new ArrayList<StatusBurInfo>();
                byte[] BurBytes = new byte[iDataLen];
                for (int i = 0; i < 8; i++) {
                    StatusBurInfo StructDataA = new StatusBurInfo();
                    System.arraycopy(bytes, (int) lngBurPos, BurBytes, 0, iDataLen);
                    JavaStruct.unpack(StructDataA, BurBytes, ByteOrder.LITTLE_ENDIAN);
                    //JavaStruct.unpack(StructDataA, bytes);
                    StatusStructData.StatusInfolist.add(i, StructDataA);
                    lngBurPos = (lngBurPos + iDataLen);
                }
                //消费限次时段
                System.arraycopy(bytes, (int) lngBurPos, StatusStructData.bPaymentLimitTime, 0, 16);
                return StatusStructData;
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
            Log.e(TAG, "身份钱包工作参数不存在,重新创建文件");
            CreateStatusBurInfo();
        }
        return null;
    }

    //读取所有身份钱包工作参数文件
    public static List<StatusInfo> ReadAllStatusBurInfo() {
        List<StatusInfo> Statuslist = new ArrayList<StatusInfo>();

        for (int cStatusID = 0; cStatusID < 64; cStatusID++) {
            StatusInfo StructStatusInfo = new StatusInfo();
            StructStatusInfo = ReadStatusBurInfo(cStatusID);
            if (StructStatusInfo != null)
                Statuslist.add(cStatusID, StructStatusInfo);
            else
                return null;
        }
        return Statuslist;
    }

    //写身份钱包参数数据
    public static int WriteStatusBurInfo(StatusInfo StructStatusData, int cStatusID) {
        int lngPos = 0;
        int lngBurPos = 0;
        int lngStatusInfoLen = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_IdentityWallet_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            try {
                StatusBurInfo StructData = new StatusBurInfo();
                int iDataLen = 0;
                iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
                lngStatusInfoLen = 1 + iDataLen * 8 + 16;
                byte[] bytes = new byte[lngStatusInfoLen];

                bytes[lngBurPos++] = StructStatusData.cStatusID;//身份号
                //身份钱包参数 //每个身份包含8组钱包参数
                List<StatusBurInfo> StatusInfolist = new ArrayList<StatusBurInfo>();
                for (int i = 0; i < 8; i++) {
                    byte[] BurBytes = JavaStruct.pack(StructStatusData.StatusInfolist.get(i), ByteOrder.LITTLE_ENDIAN);
                    System.arraycopy(BurBytes, 0, bytes, lngBurPos, iDataLen);
                    lngBurPos = lngBurPos + iDataLen;
                }
                //消费限次时段
                System.arraycopy(StructStatusData.bPaymentLimitTime, 0, bytes, (int) lngBurPos, 16);
                lngBurPos = lngBurPos + 16;
                lngPos = VersionLen + lngStatusInfoLen * (cStatusID - 1);
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

    //写所有身份钱包参数数据
    public static int InitAllStatusBurInfo(List<StatusInfo> Statuslist) {
        int iRet = 0;
        long lngPos = 0;

        for (byte cStatusID = 0; cStatusID < 64; cStatusID++) {
            iRet = WriteStatusBurInfo(Statuslist.get(cStatusID), cStatusID);
            if (iRet != -1) {
                continue;
            } else {
                return -1;
            }
        }
        return 0;
    }

    //写所有身份钱包参数数据
    public static int WriteAllStatusBurInfo(List<StatusInfo> Statuslist) {
        int iRet = 0;
        long lngPos = 0;

        for (byte cStatusID = 0; cStatusID < 64; cStatusID++) {
            iRet = WriteStatusBurInfo(Statuslist.get(cStatusID), cStatusID);
            if (iRet != -1) {
                continue;
            } else {
                return -1;
            }
        }
        return 0;
    }

    //网络写身份钱包参数
    public static int SaveInitStatusBurInfo(byte[] bytes) {
        int iRet = 0;
        int i = 0, j = 0;
        byte cBurseID;
        byte cStatusID;
        long lngTemp;


    /*
    身份号	1字节

    钱包号n	1字节
    N号钱包优惠模式	1字节	0:不启用,1:折扣优惠,2:定额优惠,3:定额优惠
    N号钱包折扣优惠	1字节
    N号钱包定额优惠	2字节
    N号钱包定额消费	2字节
    N号钱包存款手续费比率	1字节
    N号钱包取款手续费比率	1字节
    N号钱包单笔消费密码限额	3字节
    N号钱包日累消费密码限额	3字节
    n号钱包日累消费金额上限	3字节
    N号钱包日累消费次数上限	1字节
    N号钱包余额上限	3字节
    N号钱包消费管理费模式	1字节	0:不启用,1:按比率管理费,2:按金额管理费
    N号钱包按比率管理费	1字节
    N号钱包按金额管理费	2字节
    校验码	2字节	从钱包号到钱包余额上限的累加和

    消费限次时段范围	16字节	新增，按身份循环
    */

        i = 0;
        //身份号
        cStatusID = (byte) (bytes[i++] & 0xff);
        if (cStatusID > 64) {
            Log.i(TAG, "身份号超过范围");
            return 1;
        }
        StatusInfo pStatusInfo = new StatusInfo();
        pStatusInfo.cStatusID = cStatusID;

        //最多8个钱包
        for (j = 0; j < 8; j++) {
            StatusBurInfo pStatusBurInfo = new StatusBurInfo();

            //钱包号
            cBurseID = bytes[i];
            if (cBurseID > 8) {
                Log.i(TAG, "身份钱包号超过范围");
                return 1;
            }
            //判断当前身份钱包号
            if (cBurseID == (j + 1)) {
                pStatusBurInfo.cBurseID = cBurseID;   //钱包号
                i++;
                //优惠模式(0:不优惠,1:折扣优惠 2:定额优惠 3:定额消费)
                pStatusBurInfo.cPrivilegeMode = (byte) (bytes[i++] & 0xff);
                //是否启动优惠限次
                pStatusBurInfo.cPrivLimitOne = (byte) (bytes[i++] & 0xff);
                //折扣优惠
                pStatusBurInfo.cPrivilegeDis = (byte) (bytes[i++] & 0xff);
                //定额优惠
                lngTemp = (bytes[i++] & 0xff);
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
                pStatusBurInfo.cBookPrivelege = (int) lngTemp;
                //定额消费
                lngTemp = (bytes[i++] & 0xff);
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
                pStatusBurInfo.cBookMoney = (int) lngTemp;
                //存款手续费比率
                pStatusBurInfo.cFundMoneyRate = (char) (bytes[i++] & 0xff);
                //取款手续费比率
                pStatusBurInfo.cFetchMoneyRate = (char) (bytes[i++] & 0xff);
                //钱包单笔消费密码限额
                lngTemp = (bytes[i++] & 0xff);
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256 * 256;
                pStatusBurInfo.lngSinglePayPswLim = lngTemp;
                //钱包日累消费密码限额
                lngTemp = (bytes[i++] & 0xff);
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256 * 256;
                pStatusBurInfo.lngDayPayPswLim = lngTemp;
                //钱包日累消费金额上限
                lngTemp = (bytes[i++] & 0xff);
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256 * 256;
                pStatusBurInfo.lngDayTotalMoneyLim = lngTemp;
                //钱包日累消费次数上限
                pStatusBurInfo.cDayTotalCountLim = (bytes[i++] & 0xff);
                //钱包余额上限制
                lngTemp = (bytes[i++] & 0xff);
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256 * 256;
                pStatusBurInfo.lngBurseMoneyLim = lngTemp;
                //钱包消费管理费模式
                pStatusBurInfo.cManagementMode = (byte) (bytes[i++] & 0xff);
                //钱包管理费比率
                pStatusBurInfo.cManageMoneyRate = (char) (bytes[i++] & 0xff);
                //钱包管理费
                lngTemp = (bytes[i++] & 0xff);
                lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
                pStatusBurInfo.iManageMoney = (int) lngTemp;
                //Log.d(TAG,String.format("钱包管理费:%d-%d",lngTemp,pStatusBurInfo.iManageMoney));
                //校验码	2字节	从钱包号到钱包余额上限的累加和
                pStatusBurInfo.CRC16[0] = (byte) (bytes[i++] & 0xff);
                pStatusBurInfo.CRC16[1] = (byte) (bytes[i++] & 0xff);

                pStatusInfo.StatusInfolist.add(j, pStatusBurInfo);
            } else {
                Log.i(TAG, "不是当前身份钱包序号");
                pStatusInfo.StatusInfolist.add(j, pStatusBurInfo);
                continue;
            }
        }
        System.arraycopy(bytes, i, pStatusInfo.bPaymentLimitTime, 0, 16);
        i = i + 16;

        //保存参数
        iRet = WriteStatusBurInfo(pStatusInfo, cStatusID);
        if (iRet != 0)
            return -1;

        return 0;
    }

}

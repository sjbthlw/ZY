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

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_Station_File;

public class StationInfoRW {


    private static final String TAG = "StationInfoRW";

    //创建站点工作参数文件
    public static int CreateStationInfo() {
        //创建文件
        if (FileUtils.CreateDataFile(en_Station_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取站点工作参数文件
    public static StationInfo ReadStationInfo() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_Station_File);
        if (sFileName == "")
            return null;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                StationInfo StructData = new StationInfo();
                int iDataLen = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN).length;
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
        } else {
            Log.e(TAG, "站点工作参数文件不存在,重新创建文件");
            CreateStationInfo();
            StationInfo StructData = new StationInfo();
            return StructData;
        }
        return null;
    }

    //写站点参数数据
    public static int WriteAllStationInfo(StationInfo StructData) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_Station_File);
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

    //写站点参数版本号
    public static int WriteStationVer(byte[] cStationVer) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_Station_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            //byte[] bytes = JavaStruct.pack(StructData, ByteOrder.LITTLE_ENDIAN);
            //byte[] bytes = JavaStruct.pack(StructData);
            byte[] bytes = new byte[4];
            System.arraycopy(cStationVer, 0, bytes, 0, bytes.length);

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

    //网络写站点参数
    public static int SaveStationInfo(byte[] bytes) {
//        参数版本号	4字节
//        站点号	2字节
//        设备类型	2字节
//        最多允许脱机天数	1字节
//        开通园区范围	32字节
//        黑白名单类型	1字节	1黑名单 2白名单
//        操作密码	3字节
//        设置密码	3字节
//        高级密码	3字节
//        商户号	2字节
//        工作钱包号	1字节
//        被追扣钱包号	1字节
//        转帐钱包号	1字节
//        混合卡识别模式	1字节	从高到低，每两个Bit表示一项参数定义，依次为：
//        混合卡识别模式（1：混合卡；2:M1卡；3：Cpu卡；）、RFUIM卡使用几号卡（1-3）、SIMPASS使用模式（0-M1,1-CPU）、启用卡种(0-<复旦微或移动NFC卡>和SIMPASS卡都没有启用，1-启用复旦微卡或移动NFC卡、2-只启用SIMPASS卡、3-<复旦微或移动NFC>和SIMPASS卡都启用)
//        是否允许脱机交易	1字节
//        在线单笔消费限额	2字节
//        脱机单笔消费限额	2字节
//        是否允许查交易累计	1字节
//        是否允许撤销当前交易	1字节
//        终端限制参数	1字节	从高到低位
//        第1-2位：预留
//        第3位：启用消费限次时，0：不允许追扣消费 1：允许
//        第4位：消费限次   0：不启用 1：启用
//        第5位：身份参数限制   0：启用  1：不启用
//        第6-7位：消费单位 0：分；1：角；2：元
//        第8位 ：消费管理费 0-不收；1-收取。

        int iRet, i;
        int index = 0;
        long lngTemp = 0;
        byte[] bt2 = new byte[2];
        byte[] bt4 = new byte[4];
        byte[] bt8 = new byte[8];
        byte[] bt16 = new byte[16];
        byte[] bt64 = new byte[64];
        byte[] bt32 = new byte[32];

        i = 0;
        StationInfo StructData = new StationInfo();
        //版本号 4
        System.arraycopy(bytes, i, StructData.cVersion, 0, 4);
        i = i + 4;
        //站点号
        lngTemp = (bytes[i++] & 0xff);
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
        StructData.iStationID = (int) lngTemp;

        //站点类型
        lngTemp = (bytes[i++] & 0xff);
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
        StructData.iStationClass = (int) lngTemp;

        //最多允许脱机天数
        StructData.cCanOffCount = (byte) (bytes[i++] & 0xff);

        //园区范围
        System.arraycopy(bytes, i, StructData.bCampusArea, 0, 32);
        i = i + 32;

        //黑白名单类型
        StructData.cBWListClass = (byte) (bytes[i++] & 0xff);

        //操作密码
        lngTemp = (bytes[i++] & 0xff);
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256 * 256;
        StructData.lngOptionPsw = (int) lngTemp;

        //设置密码
        lngTemp = (bytes[i++] & 0xff);
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256 * 256;
        StructData.lngSetupPsw = (int) lngTemp;

        //维护密码
        lngTemp = (bytes[i++] & 0xff);
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256 * 256;
        StructData.lngAdvancePsw = lngTemp;

        //商户号
        lngTemp = (bytes[i++] & 0xff);
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
        StructData.iShopUserID = (int) lngTemp;

        //工作钱包号
        StructData.cWorkBurseID = (byte) (bytes[i++] & 0xff);
        //被追扣钱包号
        StructData.cChaseBurseID = (byte) (bytes[i++] & 0xff);
        //转帐钱包号
        StructData.cTransBurseID = (byte) (bytes[i++] & 0xff);

        //运用卡片类型 64
        //混合卡识别模式
        byte bTemp = 0;
        bTemp = (byte) ((bytes[i] >> 6) & 0x03);
        StructData.cUseCardClass = bTemp;

        //RFUIM卡使用几号卡
        bTemp = (byte) ((bytes[i] >> 4) & 0x03);
        StructData.cRFUIMCardID = bTemp;

        //SIMPass卡使用模式
        bTemp = (byte) ((bytes[i] >> 2) & 0x03);
        StructData.cSIMPassType = bTemp;

        //启用卡种(0-复旦微和SIMPASS卡都没有启用，1-只启用复旦微卡、2-只启用SIMPASS卡、3-复旦微和SIMPASS卡都启用)
        bTemp = (byte) ((bytes[i]) & 0x03);
        StructData.cUseCardType = bTemp;
        i++;

        //是否允许脱机交易
        StructData.cCanOffPayment = (byte) (bytes[i++] & 0xff);

        //联机单笔限额
        lngTemp = (bytes[i++] & 0xff);
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
        StructData.lngOnPermitLimit = (int) lngTemp;

        //脱机单笔限额
        lngTemp = (bytes[i++] & 0xff);
        lngTemp = lngTemp + (bytes[i++] & 0xff) * 256;
        StructData.lngOffPermitLimit = (int) lngTemp;

        //允许查汇总(0:不允许,1:运行)
        StructData.cCanPermitStat = (byte) (bytes[i++] & 0xff);

        //允许销帐(0:不允许,1:运行)
        StructData.cCanQuashPayment = (byte) (bytes[i++] & 0xff);

        Log.i(TAG, "是否允许销帐:" + StructData.cCanQuashPayment);

        Log.i(TAG, ("终端限制参数:" + bytes[i]));

        //是否收取消费管理费 (0-不收消费管理费；1-收取消费管理费)
        bTemp = (byte) ((bytes[i]) & 0x01);
        StructData.cCanManagementFee = bTemp;

        //消费单位 0:分 1:角 2:元
        bTemp = (byte) ((bytes[i] >> 1) & 0x03);
        StructData.cPaymentUnit = bTemp;

        //是否启用解除身份参数(日累超限、超额)限制 0:启用 1:不启用
        bTemp = (byte) ((bytes[i] >> 3) & 0x01);
        StructData.cCanStatusLimitFee = bTemp;

        //是否启用消费限次 0:no 1:yes
        bTemp = (byte) ((bytes[i] >> 4) & 0x01);
        StructData.cPaymentLimit = bTemp;

        //启用消费限次后是否允许追扣消费 0:no 1 yes
        bTemp = (byte) ((bytes[i] >> 5) & 0x01);
        StructData.cCanChasePayment = bTemp;

        StructData.CRC16[0] = (byte) (bytes[i++] & 0xff);
        StructData.CRC16[1] = (byte) (bytes[i++] & 0xff);

        iRet = WriteAllStationInfo(StructData);
        return iRet;
    }

}

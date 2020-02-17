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

import static com.hzsun.mpos.Global.Global.MAXBOOKSCOUNT;
import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_WasteBooks_File;

public class WasteBooksRW {

    private static final String TAG = "WasteBooksRW";
    private final static int HeadDataLen = 66;//信息头长度
    private final static int BookDataLen = 128;//流水数据长度

    //创建黑白名单参数文件
    public static int CreateWasteBooks() {
        //创建文件
        if (FileUtils.CreateDataFile(en_WasteBooks_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取流水文件信息头
    public static WasteBookInfo ReadWasteBookInfo() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_WasteBooks_File);
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
                WasteBookInfo StructData = new WasteBookInfo();
                JavaStruct.unpack(StructData, bytes, ByteOrder.LITTLE_ENDIAN);
                //JavaStruct.unpack(StructData, bytes);
                return StructData;
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
        }
        return null;
    }

    //写流水文件信息头
    public static int WriteWasteBookInfo(WasteBookInfo StructData) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_WasteBooks_File);
        if (sFileName == "")
            return -1;

        Object object = new Object();
        synchronized (object) {
            WasteBookInfo StructDataA = new WasteBookInfo();
            try {
                StructDataA = (WasteBookInfo) StructData.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            File fFile = new File(sFileName);
            if (fFile.exists() && fFile.isFile()) {
                try {
                    byte[] bytes = JavaStruct.pack(StructDataA, ByteOrder.LITTLE_ENDIAN);
                    //byte[] bytes = JavaStruct.pack(StructDataA);
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
        }
        return -1;
    }

    //读取流水数据
    public static WasteBooks ReadWasteBooksData(int iRecordID) {
        long lngPos = 0;
        int iDip = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_WasteBooks_File);
        if (sFileName == "")
            return null;

        if (iRecordID < 1)
            return null;

        //判断MAXBOOKSCOUNT的整数倍
        iDip = (int) (iRecordID / MAXBOOKSCOUNT);
        iRecordID = (iRecordID % MAXBOOKSCOUNT);

        if ((iRecordID == 0) && (iDip != 0)) {
            Log.d(TAG, "iRecordID到达倍数\n");
            iRecordID = MAXBOOKSCOUNT;
        }

        lngPos = HeadDataLen + ((iRecordID - 1) * BookDataLen);
        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                byte[] bytes = new byte[1024];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                WasteBooks StructData = new WasteBooks();
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

    //读取流水金额数据
    public static long ReadWasteBooksMoney(long iStartRecordID, long iEndRecordID) {
        long i = 0;
        long lngPos = 0;
        long lngResult = 0;
        int iSDip = 0;
        int iEDip = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_WasteBooks_File);
        if (sFileName == "")
            return 0;

        if (iEndRecordID - iStartRecordID == 0)
            return 0;

        //判断是否超出整数倍
        iSDip = (int) (iStartRecordID / MAXBOOKSCOUNT);
        iStartRecordID = (iStartRecordID % MAXBOOKSCOUNT);

        iEDip = (int) (iEndRecordID / MAXBOOKSCOUNT);
        iEndRecordID = (iEndRecordID % MAXBOOKSCOUNT);

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                random = new RandomAccessFile(fFile, "rwd");
                if (iEndRecordID < iStartRecordID) {
                    for (i = iStartRecordID; i < MAXBOOKSCOUNT; i++) {
                        lngPos = HeadDataLen + (i * BookDataLen);
                        byte[] bytes = new byte[256];
                        random.seek(lngPos);
                        random.read(bytes);
                        //判断交易类型
                        int cPaymentType = bytes[25] & 0xff;

                        if ((cPaymentType == 0) || (cPaymentType == 30) || (cPaymentType == 60) ||
                                (cPaymentType == 23) || (cPaymentType == 53) || (cPaymentType == 83) ||
                                (cPaymentType == 8) || (cPaymentType == 38) || (cPaymentType == 68)) {
                            lngResult = lngResult + ((bytes[26] & 0xff) + (bytes[27] & 0xff) * 256);
                        } else if ((cPaymentType == 3) || (cPaymentType == 33) || (cPaymentType == 63) ||
                                (cPaymentType == 24) || (cPaymentType == 54) || (cPaymentType == 84))//03:交易冲正；33 角  63元
                        {
                            lngResult = lngResult - ((bytes[26] & 0xff) + (bytes[27] & 0xff) * 256);
                        }
                    }
                    for (i = 0; i < iEndRecordID; i++) {
                        lngPos = HeadDataLen + (i * BookDataLen);
                        byte[] bytes = new byte[256];
                        random.seek(lngPos);
                        random.read(bytes);
                        //判断交易类型
                        int cPaymentType = bytes[25] & 0xff;

                        if ((cPaymentType == 0) || (cPaymentType == 30) || (cPaymentType == 60) ||
                                (cPaymentType == 23) || (cPaymentType == 53) || (cPaymentType == 83) ||
                                (cPaymentType == 8) || (cPaymentType == 38) || (cPaymentType == 68)) {
                            lngResult = lngResult + ((bytes[26] & 0xff) + (bytes[27] & 0xff) * 256);
                        } else if ((cPaymentType == 3) || (cPaymentType == 33) || (cPaymentType == 63) ||
                                (cPaymentType == 24) || (cPaymentType == 54) || (cPaymentType == 84))//03:交易冲正；33 角  63元
                        {
                            lngResult = lngResult - ((bytes[26] & 0xff) + (bytes[27] & 0xff) * 256);
                        }
                    }
                } else {
                    for (i = iStartRecordID; i <= iEndRecordID; i++) {
                        lngPos = HeadDataLen + (i * BookDataLen);
                        byte[] bytes = new byte[256];
                        random.seek(lngPos);
                        random.read(bytes);
                        //判断交易类型
                        int cPaymentType = bytes[25] & 0xff;

                        if ((cPaymentType == 0) || (cPaymentType == 30) || (cPaymentType == 60) ||
                                (cPaymentType == 23) || (cPaymentType == 53) || (cPaymentType == 83) ||
                                (cPaymentType == 8) || (cPaymentType == 38) || (cPaymentType == 68)) {
                            lngResult = lngResult + ((bytes[26] & 0xff) + (bytes[27] & 0xff) * 256);
                        } else if ((cPaymentType == 3) || (cPaymentType == 33) || (cPaymentType == 63) ||
                                (cPaymentType == 24) || (cPaymentType == 54) || (cPaymentType == 84))//03:交易冲正；33 角  63元
                        {
                            lngResult = lngResult - ((bytes[26] & 0xff) + (bytes[27] & 0xff) * 256);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            } finally {
                try {
                    random.close();
                    return lngResult;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    //写流水数据
    public static int WriteWasteBooksData(WasteBooks StructData, int iRecordID) {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_WasteBooks_File);
        if (sFileName == "")
            return -1;

        lngPos = HeadDataLen + ((iRecordID) * BookDataLen);
        Object object = new Object();
        synchronized (object) {
            WasteBooks StructDataA = new WasteBooks();
            try {
                StructDataA = (WasteBooks) StructData.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            File fFile = new File(sFileName);
            if (fFile.exists() && fFile.isFile()) {
                try {
                    byte[] bytes = JavaStruct.pack(StructDataA, ByteOrder.LITTLE_ENDIAN);
                    //byte[] bytes = JavaStruct.pack(StructDataA);
                    RandomAccessFile random = null;
                    try {
                        random = new RandomAccessFile(fFile, "rwd");
                        random.seek(lngPos);
                        random.write(bytes);
                        //random.write(bytes, 0, BookDataLen);//此方法无效
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
        }
        return -1;
    }

    public static int ReadPayRecordsData(byte[] bReadDataBuf, long iRecordID, int i) {
        long lngPos = 0;
        int iDip = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_WasteBooks_File);
        if (sFileName == "")
            return -1;

        if (iRecordID < 1)
            return -2;

        //判断MAXBOOKSCOUNT的整数倍
        iDip = (int) (iRecordID / MAXBOOKSCOUNT);
        iRecordID = (iRecordID % MAXBOOKSCOUNT);

        if ((iRecordID == 0) && (iDip != 0)) {
            Log.d(TAG, "iRecordID到达倍数\n");
            iRecordID = MAXBOOKSCOUNT;
        }
        //计算读取位置
        lngPos = HeadDataLen + ((iRecordID - 1) * BookDataLen);
        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                byte[] bytes = new byte[BookDataLen * i];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                System.arraycopy(bytes, 0, bReadDataBuf, 0, BookDataLen * i);
                return 0;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            }
        }
        return -1;
    }
}

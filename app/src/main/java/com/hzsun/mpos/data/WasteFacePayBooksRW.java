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
import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_FaceWasteBooks_File;

public class WasteFacePayBooksRW {

    private static final String TAG = "WasteFacePayBooksRW";
    private final static int HeadDataLen = 66;//信息头长度
    private final static int BookDataLen = 128;//流水数据长度

    //创建黑白名单参数文件
    public static int CreateWasteFacePayBooks() {
        //创建文件
        if (FileUtils.CreateDataFile(en_FaceWasteBooks_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取流水文件信息头
    public static WasteFaceBookInfo ReadWasteFacePayBookInfo() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceWasteBooks_File);
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
                WasteFaceBookInfo StructData = new WasteFaceBookInfo();
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
            Log.e(TAG, "人脸流水记录文件不存在,重新创建文件");
            CreateWasteFacePayBooks();
            WasteFaceBookInfo StructData = new WasteFaceBookInfo();
            return StructData;
        }
        return null;
    }

    //写流水文件信息头
    public static int WriteWasteFacePayBookInfo(WasteFaceBookInfo StructData) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceWasteBooks_File);
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

    //读取流水数据
    public static WasteFacePayBooks ReadWasteFacePayBooksData(int iRecordID) {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceWasteBooks_File);
        if (sFileName == "")
            return null;

        if (iRecordID < 1)
            return null;

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
                WasteFacePayBooks StructData = new WasteFacePayBooks();
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

    //写流水数据
    public static int WriteWasteFacePayBooksData(WasteFacePayBooks StructData, int iRecordID) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceWasteBooks_File);
        if (sFileName == "")
            return -1;

        lngPos = HeadDataLen + ((iRecordID) * BookDataLen);
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
                    //random.write(bytes, 0, BookDataLen);//此方法无效

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

    public static int ReadPayRecordsData(byte[] bReadDataBuf, long iRecordID, int i) {
        long lngPos = 0;
        int iDip = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceWasteBooks_File);
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

        lngPos = HeadDataLen + ((iRecordID - 1) * BookDataLen);
        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                byte[] bytes = new byte[128 * i];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                System.arraycopy(bytes, 0, bReadDataBuf, 0, BookDataLen * i);

//                    WasteFacePayBooks StructData = new WasteFacePayBooks();
//                    JavaStruct.unpack(StructData, bytes, ByteOrder.LITTLE_ENDIAN);
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

    public static WasteFacePayBooks ReadPayRecordsInfo(long iRecordID) {
        long lngPos = 0;
        int iDip = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceWasteBooks_File);
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
                WasteFacePayBooks StructData = new WasteFacePayBooks();
                JavaStruct.unpack(StructData, bytes, ByteOrder.LITTLE_ENDIAN);

                return StructData;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            } catch (StructException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

package com.hzsun.mpos.data;

import android.util.Log;

import com.hzsun.mpos.Public.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;

import struct.JavaStruct;
import struct.StructException;

import static com.hzsun.mpos.Public.FileUtils.EN_FileType.en_FaceCodeInfo_File;
import static com.hzsun.mpos.Public.Publicfun.ByteToString;
import static com.hzsun.mpos.Public.Utility.memcpy;


public class FaceCodeInfoRW {

    private static final String TAG = "FaceCodeInfoRW";

    //创建人脸工作参数文件
    public static int CreateFaceCodeInfo() {
        //创建文件
        if (FileUtils.CreateDataFile(en_FaceCodeInfo_File) != 0) {
            return -1;
        }
        return 0;
    }

    //读取人脸工作参数文件
    public static FaceCodeInfo ReadFaceCodeInfo() {
        long lngPos = 0;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceCodeInfo_File);
        if (sFileName == "")
            return null;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            FaceCodeInfo StructData = new FaceCodeInfo();
            try {
                byte[] bytes = new byte[1024];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);

                JavaStruct.unpack(StructData, bytes, ByteOrder.LITTLE_ENDIAN);
                //JavaStruct.unpack(StructData, bytes);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, e.getMessage());
            } catch (StructException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            } finally {
                try {
                    random.close();
                    return StructData;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e(TAG, "人脸工作参数不存在,重新创建文件");
            CreateFaceCodeInfo();
            FaceCodeInfo StructData = new FaceCodeInfo();
            return StructData;
        }
        return null;
    }

    //写人脸参数数据
    public static int WriteAllFaceCodeInfo(FaceCodeInfo StructData) {
        long lngPos = 0;

        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceCodeInfo_File);
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

    //写人脸信息数据
    public static int WriteFaceCodeInfoData(byte[] bytes, int iIndex) {
        long lngPos = 0;
        lngPos = 64 + iIndex * 64;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceCodeInfo_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                byte[] bytetmp = new byte[64];
                memcpy(bytetmp, bytes, bytes.length);
                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.write(bytetmp);

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
        }
        return -1;
    }


    //写人脸信息所有数据
    public static int WriteFaceCodeInfoAllData(byte[] bytes) {
        long lngPos = 64;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceCodeInfo_File);
        if (sFileName == "")
            return -1;

        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
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


    public static ArrayList<String> ReadFaceCodeInfoData() {
        long lngFileLen = 0;
        long lngPos = 64;
        //获取文件名
        String sFileName = FileUtils.GetFileName(en_FaceCodeInfo_File);
        if (sFileName == "")
            return null;

        //计算读取位置
        File fFile = new File(sFileName);
        if (fFile.exists() && fFile.isFile()) {
            lngFileLen = fFile.length();
            Log.d(TAG, "人脸数据信息文件大小:" + lngFileLen);
            if (lngFileLen <= 64)
                return null;

            RandomAccessFile random = null;
            try {
                byte[] bytes = new byte[(int) lngFileLen - 64];

                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.read(bytes);
                random.close();
                ArrayList<String> ArrayListTemp = new ArrayList<>();
                int iFaceInfoCnt = 0;
                iFaceInfoCnt = (int) ((lngFileLen - 64) / 64);
                Log.d(TAG, "人脸特征码数:" + iFaceInfoCnt);
                for (int i = 0; i < iFaceInfoCnt; i++) {
                    byte[] bytetmp = new byte[64];
                    memcpy(bytetmp, 0, bytes, i * 64, bytetmp.length);
                    //String strTemp= new String (bytetmp);
                    String strTemp = ByteToString(bytetmp);
                    ArrayListTemp.add(strTemp);
                }
                return ArrayListTemp;
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
}

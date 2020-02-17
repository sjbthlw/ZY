package com.hzsun.mpos.thread;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.huiyuenet.faceCheck.FaceCheck;
import com.huiyuenet.faceCheck.THFI_Param;
import com.hzsun.mpos.Public.FileUtils;
import com.hzsun.mpos.Public.Publicfun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.hzsun.mpos.Global.Global.EXTFACEPATH;
import static com.hzsun.mpos.Global.Global.ZYTKFacePath;
import static com.hzsun.mpos.Global.Global.g_FaceIdentInfo;

//写特征码到文件
public class WriteFeatureThread extends Thread {

    private String TAG = getClass().getSimpleName();
    private Handler handler;
    private static final int FFSize = 2560; //人脸特征码大小
    private volatile boolean isStart = false;

    public WriteFeatureThread() {
        Log.i(TAG, "WriteFeatureThread 构造");
    }

    public WriteFeatureThread(Handler handler) {
        Log.i(TAG, "WriteFeatureThread 构造");
        this.handler = handler;
    }

    public void StopThread() {
        isStart = false;

        try {
            this.join();
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void run() {
        int iResult = 0;
        int iID = 0;
        int iFeatureID = 0;
        int iTemp = 0;
        byte[] featureSingle = new byte[FFSize];
        List<String> FaceNameList = new ArrayList<>();

        //本机导入
        String strFacePath = EXTFACEPATH;
        String[] strFileNames = Publicfun.GetFileName(strFacePath, ".v10");
        int iFileNamesCnt = strFileNames.length;
        if (strFileNames != null)
            isStart = true;

        FaceNameList = g_FaceIdentInfo.FaceNameList;
        while (isStart) {
            if (iID >= strFileNames.length)
                break;

            String fileName = strFileNames[iID];
            //判断特征码是否存在,存在则替换否则新增
            iFeatureID = GetListStrID(fileName.split("\\.")[0], g_FaceIdentInfo.FaceNameList);
            if (iFeatureID == -1) {
                iFeatureID = FaceNameList.size();
            }
            iID++;
            FileInputStream fis = null;
            File fFile = new File(strFacePath + fileName);
            try {
                fis = new FileInputStream(fFile);
                fis.read(featureSingle);

                iResult = WriteFaceFeatureData(featureSingle, iFeatureID);
                if (iResult != 0) {
                    Log.e(TAG, "addFeature end addResult:" + iResult);
                    break;
                }
                //写入人脸内存
                iResult = FaceCheck.addFeature(iFeatureID, featureSingle);
                if (iResult != 0) {
                    Log.e("LoadFaceFeature", "LoadFeature addFeature end iResult:" + iResult);
                    break;
                }
                //判断是新增还是替换
                if (iFeatureID == FaceNameList.size())
                    FaceNameList.add(iFeatureID, fFile.getName().split("\\.")[0]);//新增
                else
                    FaceNameList.set(iFeatureID, fFile.getName().split("\\.")[0]);//替换

                int iPercent = Publicfun.CalcRatio(iID, iFileNamesCnt);
                if ((iPercent > 1) && (iTemp != iPercent)) {
                    iTemp = iPercent;
                    if ((iPercent % 10) == 0) {
                        Message msg = Message.obtain();
                        msg.what = 1;
                        msg.obj = (int) iPercent;
                        if (handler != null)
                            handler.sendMessage(msg);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //将face文件名list写入文件
        String FaceNamefile = ZYTKFacePath + "facename.ini";
        FileUtils.WriteListToFile((ArrayList) FaceNameList, FaceNamefile);
        g_FaceIdentInfo.FaceNameList = FaceNameList;
        g_FaceIdentInfo.iListNum = g_FaceIdentInfo.FaceNameList.size();
        THFI_Param.EnrolledNum = g_FaceIdentInfo.iListNum;
        THFI_Param.FaceName = g_FaceIdentInfo.FaceNameList;
        Message msg = Message.obtain();
        msg.what = 2;
        if (handler != null)
            handler.sendMessage(msg);
    }

    private int GetListStrID(String strIn, List<String> listIn) {
        int i = 0;
        int iRet = 0;

        if (listIn.size() != 0) {
            for (i = 0; i < listIn.size(); i++) {
                if (strIn.equals(listIn.get(i))) {
                    iRet = i;
                    break;
                }
            }
        }
        if (i == listIn.size())
            return -1;
        else
            return iRet;
    }

    //写人脸数据到文件中
    private int WriteAllFaceFeatureData() {
        int iResult = 0;
        int iFeatureID = 0;
        byte[] featureSingle = new byte[FFSize];
        List<String> FaceNameList = new ArrayList<>();

        Message msg = Message.obtain();

        //本机导入
        String strFacePath = EXTFACEPATH;
        String[] strFileNames = Publicfun.GetFileName(strFacePath, ".v10");
        int iFileNamesCnt = strFileNames.length;
        if (strFileNames != null) {
            //加载外部人脸特征码到本地文件,//将v10文件合并成一个大文件
            iFeatureID = 0;
            //将v10文件合并成一个大文件
            for (String fileName : strFileNames) {

                File currentFile = new File(strFacePath + fileName);
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(currentFile);
                    fis.read(featureSingle);

                    iResult = WriteFaceFeatureData(featureSingle, iFeatureID);
                    if (iResult != 0) {
                        Log.i(TAG, "addFeature end addResult:" + iResult);
                        break;
                    }
                    FaceNameList.add(iFeatureID, currentFile.getName().split("\\.")[0]);
                    iFeatureID++;

                    NumberFormat nt = NumberFormat.getPercentInstance();
                    //设置百分数精确度2即保留两位小数
                    nt.setMinimumFractionDigits(0);
                    float percent = (float) iFeatureID / iFileNamesCnt;
                    int iPercent = (int) (percent * 100);
                    if (iPercent > 1) {
                        if ((iPercent % 10) == 0) {
                            msg.what = 1;
                            msg.obj = (int) iPercent;
                            if (handler != null)
                                handler.sendMessage(msg);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fis != null) fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //将face文件名list写入文件
            String FaceNamefile = ZYTKFacePath + "facename.ini";
            FileUtils.WriteListToFile((ArrayList) FaceNameList, FaceNamefile);
            msg.what = 2;
            if (handler != null)
                handler.sendMessage(msg);
            return 0;
        }

        return 0;
    }

    //写人脸数据到文件中
    public int WriteFaceFeatureData(byte[] feature, int iFeatureID) {
        long lngPos = 0;

        //获取文件名
        String sFileName = ZYTKFacePath + "facedata.v";

        lngPos = ((iFeatureID) * FFSize);
        File fFile = new File(sFileName);

        if (!fFile.exists() || !fFile.isFile()) {
            try {
                fFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "create File \"+filename+\" is fail! \\r\\n" + e.getMessage());
            }
        }
        if (fFile.exists() && fFile.isFile()) {
            RandomAccessFile random = null;
            try {
                random = new RandomAccessFile(fFile, "rwd");
                random.seek(lngPos);
                random.write(feature);
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
        } else {
        }
        return -1;
    }
}

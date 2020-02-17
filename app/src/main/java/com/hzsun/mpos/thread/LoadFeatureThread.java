package com.hzsun.mpos.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.huiyuenet.faceCheck.FaceCheck;
import com.huiyuenet.faceCheck.THFI_Param;
import com.hzsun.mpos.Public.Publicfun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.hzsun.mpos.Global.Global.FACEFEASIZE;
import static com.hzsun.mpos.Global.Global.ZYTKFacePath;
import static com.hzsun.mpos.Global.Global.g_FaceIdentInfo;

public class LoadFeatureThread extends Thread {

    private String TAG = getClass().getSimpleName();
    private Handler handler;
    private volatile boolean isStart = false;
    private long s_lngStart;

    public LoadFeatureThread(Handler handler) {
        Log.i(TAG, "LoadFeatureThread 构造");
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
        File folder = new File(ZYTKFacePath);
        if (!folder.exists()) {
            return;
        }
        iResult = Publicfun.ReadFaceIdentInfo();//获取人脸名单数量
        if (iResult == 0) {
            Message msg = Message.obtain();
            msg.what = 100;
            if (handler != null)
                handler.sendMessage(msg);
            return;
        }
        s_lngStart = System.currentTimeMillis();

        File fFile = new File(ZYTKFacePath + "facedata.v");
        g_FaceIdentInfo.iListNum = g_FaceIdentInfo.FaceNameList.size();
        if (fFile.exists() && fFile.isFile()) {
            //清除人脸特征数据
            THFI_Param.EnrolledNum = 0;
            THFI_Param.FaceName.clear();
            FaceCheck.clearFeature();

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fFile);
                for (int iEnrolledNum = 0; iEnrolledNum < g_FaceIdentInfo.iListNum; iEnrolledNum++) {
                    byte[] feature = new byte[FACEFEASIZE];
                    fis.read(feature);
                    iResult = FaceCheck.addFeature(iEnrolledNum, feature);
                    if (iResult != 0) {
                        Log.e("LoadFaceFeature", "LoadFeature addFeature end iResult:" + iResult);
                        iResult = 2;
                        break;
                    }
//                    int iPercent = Publicfun.CalcRatio(iEnrolledNum, g_FaceIdentInfo.iListNum);
//                    if ((iPercent > 1) && (iTemp != iPercent)) {
//                        iTemp = iPercent;
//                        if ((iPercent % 10) == 0) {
//                        }
//                    }
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
        } else {
            iResult = 3;
        }
        if (iResult == 0) {
            THFI_Param.EnrolledNum = g_FaceIdentInfo.iListNum;
            THFI_Param.FaceName = g_FaceIdentInfo.FaceNameList;
            Log.e(TAG, "=========加载人脸特征码耗时" + (System.currentTimeMillis() - s_lngStart));
            Log.e(TAG, "=========加载人脸特征码数量" + g_FaceIdentInfo.iListNum);

            Message msg = Message.obtain();
            msg.what = 1;
            if (handler != null)
                handler.sendMessage(msg);
        } else {
            Message msg = Message.obtain();
            msg.what = 100;
            if (handler != null)
                handler.sendMessage(msg);
        }
        return;
    }
}

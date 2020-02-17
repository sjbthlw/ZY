package com.hzsun.mpos.thread;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.huiyuenet.faceCheck.FaceCheck;
import com.huiyuenet.faceCheck.THFI_Param;
import com.hzsun.mpos.Public.FileUtils;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Usb.USBManager;
import com.hzsun.mpos.data.FaceCodeInfoRW;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static com.hzsun.mpos.Global.Global.ZYTKFacePath;
import static com.hzsun.mpos.Global.Global.g_FaceIdentInfo;
import static com.hzsun.mpos.Public.Publicfun.GetFaceListPos;

/**
    * @author  zytk wcp
    * @time  2019/12/10
    *@description  通过USB特征码文件写入
 */
public class WriteUsbFeatureThread extends Thread {

    private String TAG = getClass().getSimpleName();
    private Handler handler;
    private USBManager usbManager;
    private FileSystem currentFs;

    private static final int FFSize = 2560; //人脸特征码大小
    private volatile boolean isStart = false;

    public WriteUsbFeatureThread(Context context, USBManager usbManager, Handler handler) {
        Log.i(TAG, "WriteUsbFeatureThread 构造");
        this.handler = handler;
        this.usbManager = usbManager;
    }

    public boolean GetFlag() {
        return isStart;
    }

    public void StopThread() {
        isStart = false;
//
//        try{
//            this.join();
//        }catch (InterruptedException e){
//        }
    }

    @Override
    public void run() {
        int iResult = 0;
        int iID = 0;
        int iFeatureID = 0;
        int iTemp = 0;
        byte[] featureSingle = new byte[FFSize];
        List<String> FaceNameList = new ArrayList<>();

        //判断U盘是否插入
        currentFs = usbManager.GetCurFS();
        if (currentFs == null) {
            Message msg = Message.obtain();
            msg.what = 10;
            if (handler != null)
                handler.sendMessage(msg);
            return;
        }
        //U盘导入
        String strUFacePath = "FeaturePath/";
        //获取U盘 FeaturePath 下的.v10特征文件
        UsbFile UFile = currentFs.getRootDirectory(); //获取U盘根目录
        String[] str = strUFacePath.split("/");
        for (int i = 0; i < str.length; i++) {
            try {
                if (UFile.isDirectory()) //判断是否是文件夹
                {
                    if (isContain(UFile.list(), str[i])) {
                        for (UsbFile fil : UFile.listFiles()) {
                            if (fil.getName().equals(str[i])) {
                                UFile = fil;
                                if (UFile != null) {
                                    Log.i(TAG, "获取到USB文件" + UFile.getName());
                                    isStart = true;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String strTarFileName = UFile.getName();
        Log.i("strTarFileName", "获取到USB文件夹" + strTarFileName);

        if (isStart == false) {
            Message msg = Message.obtain();
            msg.what = 20;
            if (handler != null)
                handler.sendMessage(msg);

            return;
        }
        if (g_FaceIdentInfo.iListNum != 0)
            FaceNameList = g_FaceIdentInfo.FaceNameList;

        iID = 0;
        while (isStart) {
            try {
                long FileListCnt = UFile.list().length;
                if (iID >= FileListCnt)
                    break;
                for (UsbFile otgFile : UFile.listFiles()) {

                    if (isStart == false)
                        break;

                    String fileName = otgFile.getName();
                    //判断是否是v10特征码
                    if (fileName.endsWith(".v10") != true) {
                        continue;
                    }
                    String accnum = otgFile.getName().split("\\.")[0];
                    String strTemp=accnum + ","+ " " + ","+ " ";
                    byte[] sbTemp = strTemp.getBytes();
                    iFeatureID = GetFaceListPos(accnum, FaceNameList);
                    if ((iFeatureID != 0)&&(FaceNameList.size()!=iFeatureID)) {
                        FaceNameList.set(iFeatureID, strTemp);
                        FaceCodeInfoRW.WriteFaceCodeInfoData(sbTemp, iFeatureID);
                    } else {
                        FaceNameList.add(strTemp);
                        iFeatureID = FaceNameList.size() - 1;
                        FaceCodeInfoRW.WriteFaceCodeInfoData(sbTemp, iFeatureID);
                    }
                    //获取U盘特征码文件流
                    InputStream fis = new UsbFileInputStream(otgFile);
                    fis.read(featureSingle);
                    //写特征码数据到本地文件
                    iResult = WriteFaceFeatureData(iFeatureID, featureSingle);
                    if (iResult != 0) {
                        Log.e(TAG, "addFeature end addResult:" + iResult);
                        break;
                    }
                    //写入人脸内存
                    iResult = FaceCheck.addFeature(iFeatureID, featureSingle);
                    if (iResult != 0) {
                        Log.e("WriteUsbFeatureThread", "WriteUsbFeatureThread addFeature end iResult:" + iResult);
                        //break;
                    }
                    int iPercent = Publicfun.CalcRatio(iID, (int) FileListCnt);
                    if ((iPercent > 1) && (iTemp != iPercent)) {
                        iTemp = iPercent;
                        if ((iPercent % 1) == 0) {
                            Message msg = Message.obtain();
                            msg.what = 1;
                            msg.obj = (int) iPercent;
                            if (handler != null)
                                handler.sendMessage(msg);
                        }
                    }
                    iID++;
                }
                break;
            } catch (IOException e) {
                e.printStackTrace();
                isStart = false;
                break;
            }
        }
        isStart = false;
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

    //使用循环判断U盘中是否包含某文件
    private boolean isContain(String[] arr, String targetValue) {
        for (String s : arr) {
            if (s.equals(targetValue))
                return true;
        }
        return false;
    }

    private void copySdata(UsbFile otgFile, String sdPath) {
        String filePath = sdPath + "/" + otgFile.getName();
        final File f = new File(filePath);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream os = new FileOutputStream(f);
            InputStream is = new UsbFileInputStream(otgFile);
            redFileStream(os, is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFile(String LocalPath, UsbFile folder) {
        String path = null;
        if (folder.isDirectory()) {  //如果选择是个文件夹
            try {
                File file = new File(LocalPath + "/" + folder.getName());
                if (!file.exists()) {
                    file.mkdir();
                }
                for (UsbFile otgFile : folder.listFiles()) {
                    path = LocalPath + "/" + folder.getName();
                    readFile(path, otgFile);
                }
            } catch (IOException e) {
            }
        } else { //如果选了一个文件
            path = LocalPath;
            copySdata(folder, path);
        }
    }

    private void redFileStream(OutputStream os, InputStream is) throws IOException {
        /**
         *  写入文件到U盘同理 要获取到UsbFileOutputStream后 通过
         *  f.createNewFile();调用 在U盘中创建文件 然后获取os后
         *  可以通过输出流将需要写入的文件写到流中即可完成写入操作
         */
        int bytesRead = 0;
        byte[] buffer = new byte[1024 * 8];
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
        os.close();
        is.close();
    }

    //获取文件夹文件名
    private List<String> GetUFileName(UsbFile folder, final String strNameSuffix) {
        if (folder.isDirectory()) {  //如果选择是个文件夹
            try {
                List<String> FaceNameList = new ArrayList<>();
                String[] strFileNames = folder.list();
                for (int i = 0; i < strFileNames.length; i++) {
                    if (strFileNames[i].endsWith(strNameSuffix)) {
                        FaceNameList.add(strFileNames[i]);
                    }
                }
                return FaceNameList;

            } catch (IOException e) {
            }
        } else { //如果是一个文件
            return null;
        }
        return null;
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
    public int WriteFaceFeatureData(int iFeatureID, byte[] feature) {
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

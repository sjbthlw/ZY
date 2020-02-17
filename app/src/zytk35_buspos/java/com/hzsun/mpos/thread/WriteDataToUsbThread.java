package com.hzsun.mpos.thread;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Usb.USBManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.hzsun.mpos.Global.Global.ZYTKPath;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Public.Publicfun.GetSDFileCnt;

public class WriteDataToUsbThread extends Thread {

    private String TAG = getClass().getSimpleName();
    private Handler handler;
    private USBManager usbManager;
    private FileSystem currentFs;
    private volatile boolean isStart = false;

    private int iID = 0;
    private int iTemp = 0;
    private int FileListCnt = 0;

    public WriteDataToUsbThread(Context context, USBManager usbManager, Handler handler) {
        Log.i(TAG, "WriteDataToUsbThread 构造");
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
        int iTemp = 0;

        //判断U盘是否插入
        currentFs = usbManager.GetCurFS();
        if (currentFs == null) {
            Message msg = Message.obtain();
            msg.what = 10;
            if (handler != null)
                handler.sendMessage(msg);
            return;
        }
        //导出文件到U盘
        String strFoldPath = ZYTKPath;
        File ZYTKFile = new File(strFoldPath);
        if (!ZYTKFile.exists()) {
            Log.e(TAG, "copyFolder: cannot create directory.");
            Message msg = Message.obtain();
            msg.what = 20;
            if (handler != null)
                handler.sendMessage(msg);
            return;
        }
        String otgPath = "";//U盘根目录
        {
            UsbFile root = currentFs.getRootDirectory(); //获取U盘根目录
            UsbFile otgFile = root;
            try {
                if (otgPath == "") {
                    otgFile = root;
                } else {
                    String[] str = otgPath.split("/");
                    for (int i = 0; i < str.length; i++) {
                        if (isContain(otgFile.list(), str[i])) {
                            for (UsbFile fil : otgFile.listFiles()) {
                                if (fil.getName().equals(str[i])) {
                                    otgFile = fil;
                                }
                            }
                        } else {
                            otgFile = otgFile.createDirectory(str[i]);
                        }
                    }
                }
                for (UsbFile uf : otgFile.listFiles()) {
                    if (uf.getName().equals(ZYTKFile.getName())) { //目录或文件存在
                        Log.e(TAG, "目录或文件存在,删除");
                        uf.delete();
                    }
                }
                isStart = true;
                FileListCnt = GetSDFileCnt(ZYTKFile);
                readSDFile(ZYTKFile, otgFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Message msg = Message.obtain();
        msg.what = 5;
        if (handler != null)
            handler.sendMessage(msg);
    }

    private void readSDFile(final File f, UsbFile folder) {
        UsbFile usbFile = null;

        if (f.isDirectory()) { //如果选择是个文件夹
            try {
                Log.d(TAG, f.getName());
                if (f.getName().equals("zytk")) {
                    String strDtTmp = Publicfun.toDataA(System.currentTimeMillis());
                    usbFile = folder.createDirectory(f.getName() + "_" + "" + g_StationInfo.iStationID + "_" + strDtTmp);
                } else {
                    usbFile = folder.createDirectory(f.getName());
                }
                for (File sdFile : f.listFiles()) {

                    if(sdFile.getName().equals("iap"))
                        continue;

                    if(sdFile.getName().equals("logiap"))
                        continue;

                    if(sdFile.getName().equals("FaceResource"))
                        continue;

                    if(sdFile.getName().equals("facedata.v"))
                        continue;

                    if(sdFile.getName().equals("zytk35_buspos.apk"))
                        continue;

                    if (isStart == false) {
                        Log.d(TAG, "退出 readSDFile");
                        Message msg = Message.obtain();
                        msg.what = 5;
                        if (handler != null)
                            handler.sendMessage(msg);

                        return;
                    }
                    readSDFile(sdFile, usbFile);
                }
            } catch (IOException e) {
            }
        } else { //如果选了一个文件
            try {
                usbFile = folder.createFile(f.getName());
                copyData(f, usbFile);
            } catch (IOException e) {
            }
        }
    }

    private void copyData(File sdFile, UsbFile finalOtgFile) {
        FileInputStream fis = null;//读取选择的文件的
        try {
            fis = new FileInputStream(sdFile);
            UsbFileOutputStream uos = new UsbFileOutputStream(finalOtgFile);
            readFileStream(uos, fis);

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFileStream(OutputStream os, InputStream is) throws IOException {
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

    //使用循环判断U盘中是否包含某文件
    private boolean isContain(String[] arr, String targetValue) {
        for (String s : arr) {
            if (s.equals(targetValue))
                return true;
        }
        return false;
    }


}

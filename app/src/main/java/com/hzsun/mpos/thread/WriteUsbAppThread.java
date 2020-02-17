
package com.hzsun.mpos.thread;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.Usb.USBManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.hzsun.mpos.Global.Global.APPFILE_NAME;
import static com.hzsun.mpos.Global.Global.IAPAPPFILE_NAME;
import static com.hzsun.mpos.Global.Global.ZYTK35Path;

//写特征码到文件
public class WriteUsbAppThread extends Thread {

    private String TAG = getClass().getSimpleName();
    private Handler handler;
    private USBManager usbManager;
    private FileSystem currentFs;
    private int s_Type;

    private volatile boolean isStart = false;

    public WriteUsbAppThread(Context context, USBManager usbManager, Handler handler, int iType) {
        Log.i(TAG, "WriteUsbAppThread 构造");
        this.handler = handler;
        this.usbManager = usbManager;
        s_Type = iType;
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
        //U盘导入
        String strAppName = "";
        if (s_Type == 0)
            strAppName = APPFILE_NAME;
        else if (s_Type == 1)
            strAppName = IAPAPPFILE_NAME;
        else
            strAppName = APPFILE_NAME;
        //获取U盘 FeaturePath 下的.v10特征文件
        UsbFile UFile = currentFs.getRootDirectory(); //获取U盘根目录
        try {
            if (UFile.isDirectory()) //判断是否是文件夹
            {
                if (isContain(UFile.list(), strAppName)) {
                    for (UsbFile fil : UFile.listFiles()) {
                        if (fil.getName().equals(strAppName)) {
                            UFile = fil;
                            if (UFile != null) {
                                Log.i(TAG, "获取到USB文件" + UFile.getName());
                                isStart = true;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isStart == false) {
            Message msg = Message.obtain();
            msg.what = 20;
            if (handler != null)
                handler.sendMessage(msg);

            return;
        }

        String AppfilePath = ZYTK35Path + UFile.getName();
        long AppLength = UFile.getLength();
        final File f = new File(AppfilePath);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream os = new FileOutputStream(f);
            InputStream is = new UsbFileInputStream(UFile);
            int bytesRead = 0;
            int bytesReadTotal = 0;
            byte[] buffer = new byte[1024 * 1024];
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                bytesReadTotal = bytesReadTotal + bytesRead;
                int iPercent = Publicfun.CalcRatio(bytesReadTotal, (int) AppLength);
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
                if (isStart == false)
                    break;
            }
            os.flush();
            os.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        isStart = false;

        Message msg = Message.obtain();
        if (s_Type == 0)
            msg.what = 3;
        else if (s_Type == 1)
            msg.what = 4;
        else
            msg.what = 3;

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
}

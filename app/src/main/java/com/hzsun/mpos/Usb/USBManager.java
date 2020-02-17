package com.hzsun.mpos.Usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.github.mjdev.libaums.partition.Partition;
import com.hzsun.mpos.Public.ToastUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hzsun.mpos.Public.ToastUtils.BOTTOM;
import static com.hzsun.mpos.Public.ToastUtils.FAIL;
import static com.hzsun.mpos.Public.ToastUtils.SUCCESS;
import static com.hzsun.mpos.Public.ToastUtils.WARN;

public class USBManager {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbMassStorageDevice[] storageDevices;
    private Context context;
    private FileSystem currentFs;
    private ExecutorService executorService;

    public USBManager(Context context) {
        this.context = context;
        registerReceiver();
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        //获取存储设备
        storageDevices = UsbMassStorageDevice.getMassStorageDevices(context);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        if (storageDevices.length == 0) {
            ToastUtils.showText(context, "没有检测到USB设备！", WARN, BOTTOM, Toast.LENGTH_LONG);
        } else {
            for (UsbMassStorageDevice device : storageDevices) { //可能有几个 一般只有一个 因为大部分手机只有1个otg插口
                if (!usbManager.hasPermission(device.getUsbDevice())) {//有就直接读取设备是否有权限
                    usbManager.requestPermission(device.getUsbDevice(), pendingIntent); //没有就去发起意图申请权限,该代码执行后，系统弹出一个对话框，申请USB权限
                } else {
                    readDevice(device);
                }
            }
        }
        //线程
        executorService = Executors.newCachedThreadPool();//30大小的线程池
    }

    /**
     * 新建文件并写入内容
     *
     * @param directoryName 新建的文件夹
     * @param fileName      新建的文件
     * @param content       文件内容
     */
    public void creatTxt2OTG(String directoryName, String fileName, String content) {
        if (currentFs == null) {
            ToastUtils.showText(context, "没有检测到USB设备！", WARN, BOTTOM, Toast.LENGTH_LONG);
            return;
        }
        UsbFile root = currentFs.getRootDirectory(); //获取U盘根目录
        UsbFile newDir = null;
        UsbFile file = null;
        try {
            //一级文件夹下文件
            UsbFile[] files = root.listFiles();
            if (isContain(root.list(), directoryName)) {  //文件夹存在
                for (UsbFile fil : files) {
                    if (fil.getName().equals(directoryName)) {
                        newDir = fil;
                    }
                }
            } else {  //文件夹不存在
                if (!directoryName.isEmpty()) {
                    newDir = root.createDirectory(directoryName);
                }
            }
            //二级文件夹下文件
            UsbFile[] dirFile = newDir.listFiles();
            if (isContain(newDir.list(), fileName)) {  //文件存在
                for (UsbFile fil : dirFile) {
                    if (fil.getName().equals(fileName)) {
                        file = fil;
                    }
                }
            } else {  //文件不存在
                if (!fileName.isEmpty()) {
                    file = newDir.createFile(fileName);
                }
            }
            content = content + "\r\n";
            OutputStream os = new UsbFileOutputStream(file);
            os.write(content.getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * OTG（文件或文件夹）拷贝到sd卡
     *
     * @param otgPath U盘文件路径 （"文件夹1/文件夹2/文件夹3"）
     * @param sdPath  sd卡路径
     */
    public void otg2sd(final String otgPath, final String sdPath) {
        if (currentFs == null) {
            ToastUtils.showText(context, "没有检测到USB设备！", WARN, BOTTOM, Toast.LENGTH_LONG);
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                //handler.sendEmptyMessage(SHOW);
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
                    readFile(sdPath, otgFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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

    /**
     * sd卡（文件或文件夹）拷贝到OTG
     *
     * @param otgPath OTG的目录路径（"文件夹1/文件夹2/文件夹3"），根目录传""
     * @param sdFile  SD卡的文件
     */
    public void sd2otg(final String otgPath, final File sdFile) {
        if (currentFs == null) {
            ToastUtils.showText(context, "没有检测到USB设备！", WARN, BOTTOM, Toast.LENGTH_LONG);
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
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
                        if (uf.getName().equals(sdFile.getName())) { //目录或文件存在
                            uf.delete();
                        }
                    }
                    readSDFile(sdFile, otgFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void readSDFile(final File f, UsbFile folder) {
        UsbFile usbFile = null;
        if (f.isDirectory()) { //如果选择是个文件夹
            try {
                usbFile = folder.createDirectory(f.getName());
                for (File sdFile : f.listFiles()) {
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
            redFileStream(uos, fis);
            ToastUtils.showText(context, "拷贝到U盘成功！", WARN, BOTTOM, Toast.LENGTH_LONG);
        } catch (Exception e) {
            e.printStackTrace();
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

    //读取大文件流
    private void redBFileStream(OutputStream os, InputStream is) throws IOException {
        /**
         *  写入文件到U盘同理 要获取到UsbFileOutputStream后 通过
         *  f.createNewFile();调用 在U盘中创建文件 然后获取os后
         *  可以通过输出流将需要写入的文件写到流中即可完成写入操作
         */
        int bytesRead = 0;
        byte[] buffer = new byte[1024 * 1024];
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
        os.close();
        is.close();
    }


    //使用循环判断U盘中是否包含某文件
    public static boolean isContain(String[] arr, String targetValue) {
        for (String s : arr) {
            if (s.equals(targetValue))
                return true;
        }
        return false;
    }

    /**
     * OTG（文件或文件夹）拷贝到sd卡
     */
    public boolean saveUSbFileToLocal(String UdiskPath, String LocalPath) {
        if (currentFs == null) {
            ToastUtils.showText(context, "没有检测到USB设备！", WARN, BOTTOM, Toast.LENGTH_LONG);
            return false;
        }
        UsbFile root = currentFs.getRootDirectory(); //获取U盘根目录
        UsbFile otgFile = root;
        try {
            if (UdiskPath == "") {
                otgFile = root;
            } else {
                String[] str = UdiskPath.split("/");
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
            readFile(LocalPath, otgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 复制 USB文件到本地
     *
     * @param savePath 复制的目标文件路径
     * @return 复制结果
     */
    public boolean WriteUSbFileToLocal(String UdiskPath, String savePath) {
        boolean result;
        if (currentFs == null) {
            ToastUtils.showText(context, "没有检测到USB设备！", WARN, BOTTOM, Toast.LENGTH_LONG);
            return false;
        }
        UsbFile UFile = currentFs.getRootDirectory(); //获取U盘根目录
        String[] str = UdiskPath.split("/");
        for (int i = 0; i < str.length; i++) {
            try {
                if (UFile.isDirectory()) //判断是否是文件夹
                {
                    if (isContain(UFile.list(), str[i])) {
                        for (UsbFile fil : UFile.listFiles()) {
                            if (fil.getName().equals(str[i])) {
                                UFile = fil;
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
        //获取U盘文件流
        try {
            String strTarFileName = UFile.getName();
            Log.i("strTarFileName", "获取到USB文件夹" + strTarFileName);

            //copySdata(UFile,savePath);

            String filePath = savePath + "/" + UFile.getName();
            File fLocal = new File(filePath);
            if (!fLocal.exists()) {
                fLocal.createNewFile();
            }
            //开始写入
            UsbFileInputStream uis = new UsbFileInputStream(UFile);//读取选择的文件的
            FileOutputStream fos = new FileOutputStream(fLocal);
            //这里uis.available一直为0
//            int avi = uis.available();
            long avi = UFile.getLength();
            int writeCount = 0;
            int bytesRead;
            byte[] buffer = new byte[1024 * 1024];
            while ((bytesRead = uis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                writeCount += bytesRead;
//                Log.e(TAG, "Progress : write : " + writeCount + " All : " + avi);
            }
            fos.flush();
            uis.close();
            fos.close();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public FileSystem GetCurFS() {
        return currentFs;
    }

    private void readDevice(UsbMassStorageDevice device) {
        try {
            device.init();//初始化
            //Only uses the first partition on the device
            Partition partition = device.getPartitions().get(0);
            currentFs = partition.getFileSystem();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerReceiver() {
        //监听otg插入 拔出
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        context.registerReceiver(mUsbReceiver, usbDeviceStateFilter);
        //注册监听自定义广播
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbReceiver, filter);
    }

    public void unRegisterReceiver() {
        if (mUsbReceiver != null) {//有注册就有注销
            context.unregisterReceiver(mUsbReceiver);
            mUsbReceiver = null;
        }
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION://接受到自定义广播
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {  //允许权限申请
                        if (usbDevice != null) {  //Do something
                            //用户已授权，可以进行读取操作
                            for (UsbMassStorageDevice device : storageDevices) {
                                if (usbDevice.equals(device.getUsbDevice())) {
                                    readDevice(device);
                                }
                            }
                        } else {
                            ToastUtils.showText(context, "没有检测到USB设备！", WARN, BOTTOM, Toast.LENGTH_LONG);
                        }
                    } else {
                        ToastUtils.showText(context, "用户未授权，读取失败！", FAIL, BOTTOM, Toast.LENGTH_LONG);
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED://接收到存储设备插入广播
                    UsbDevice device_add = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device_add != null) {
                        ToastUtils.showText(context, "检测到U盘已插入！", SUCCESS, BOTTOM, Toast.LENGTH_LONG);
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED://接收到存储设备拔出广播
                    UsbDevice device_remove = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device_remove != null) {
                        ToastUtils.showText(context, "检测到U盘已拔出！", WARN, BOTTOM, Toast.LENGTH_LONG);
                    }
                    break;
            }
        }
    };

}

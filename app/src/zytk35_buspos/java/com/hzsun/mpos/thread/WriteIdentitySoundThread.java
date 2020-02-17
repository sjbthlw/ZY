package com.hzsun.mpos.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hzsun.mpos.Usb.USBManager;
import com.hzsun.mpos.data.IdentitySoundRW;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteIdentitySoundThread extends Thread {

    private String TAG = WriteIdentitySoundThread.class.getSimpleName();
    private USBManager usbManager;
    private Handler handler;
    private FileSystem currentFs;
    private String IDENTITY_SOUND_FILE = "IdentitySound.txt";
    private boolean isExist;

    public WriteIdentitySoundThread(USBManager usbManager, Handler handler) {
        this.handler = handler;
        this.usbManager = usbManager;
    }

    @Override
    public void run() {
        super.run();
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
        isExist = false;
        UsbFile UFile = currentFs.getRootDirectory(); //获取U盘根目录
        UsbFile[] usbFiles = new UsbFile[0];
        try {
            usbFiles = UFile.listFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != usbFiles && usbFiles.length > 0) {
            for (UsbFile usbFile : usbFiles) {
                Log.i(TAG, "file:" + usbFile.getName());
                if (usbFile.getName().equals(IDENTITY_SOUND_FILE)) {
                    isExist = true;
                    writeIdentityFile(usbFile);
                    return;
                }
            }
        }
        if (!isExist) {
            Message msg = Message.obtain();
            msg.what = 20;
            if (handler != null)
                handler.sendMessage(msg);
        }
    }

    private void writeIdentityFile(UsbFile usbFile) {
        UsbFile descFile = usbFile;
        //读取文件内容
        InputStream is = new UsbFileInputStream(descFile);
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(is));
            String read;
            while ((read = bufferedReader.readLine()) != null) {
                sb.append(read);
            }
            String json = sb.toString();
            Gson gson = new Gson();
            LinkedHashMap<String, String> map;
            map = gson.fromJson(json, LinkedHashMap.class);
            if (map != null && map.size() > 0) {
                boolean isvalide = true;
                for (String key : map.keySet()) {
                    if (!isNumeric(key)) {
                        isvalide = false;
                        break;
                    } else {
                        int num = Integer.parseInt(key);
                        if (num >= 0 && num <= 127) {
                            String value = map.get(key);
                            if (value.equals("linshicard") || value.equals("xueshengcard")
                                    || value.equals("xiaoyuancard") || value.equals("jiaogongcard")
                                    || value.equals("xiaoyoucard")) {
                                isvalide = true;
                            } else {
                                isvalide = false;
                                break;
                            }
                        } else {
                            isvalide = false;
                            break;
                        }
                    }
                }
                if (isvalide) {
                    if (IdentitySoundRW.WriteIdentitySoundFile(json) == 0) {
                        Log.i(TAG, "写入成功");
                        Message msg = Message.obtain();
                        msg.what = 6;
                        if (handler != null)
                            handler.sendMessage(msg);
                    } else {
                        Log.i(TAG, "写入失败");
                        Message msg = Message.obtain();
                        msg.what = 7;
                        if (handler != null)
                            handler.sendMessage(msg);
                    }
                } else {
                    Log.i(TAG, "文件格式不正确,导入失败");
                    Message msg = Message.obtain();
                    msg.what = 8;
                    if (handler != null)
                        handler.sendMessage(msg);
                }

            } else {
                Log.i(TAG, "文件格式不正确,导入失败");
                Message msg = Message.obtain();
                msg.what = 8;
                if (handler != null)
                    handler.sendMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            Log.i(TAG, "文件格式不正确,导入失败");
            Message msg = Message.obtain();
            msg.what = 8;
            if (handler != null)
                handler.sendMessage(msg);

        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}

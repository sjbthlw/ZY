package com.hzsun.mpos.QRCodeWork;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.input.InputManager;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.KeyEvent;

import com.hzsun.mpos.MyApplication;

public class QRScanHelper {

    //shift键是否打开
    private static boolean mCaps;
    private static String result;

    /**
     * 检测输入设备是否是扫码器
     *
     * @param context
     * @return 是的话返回true，否则返回false
     */
    public boolean isScannerInput(Context context, KeyEvent event) {
        if (event.getDevice() == null) {
            return false;
        }
//        event.getDevice().getControllerNumber();
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            //实体按键，若按键为返回、音量加减、返回false
            return false;
        }
        if (event.getDevice().getSources() == (InputDevice.SOURCE_KEYBOARD | InputDevice.SOURCE_DPAD | InputDevice.SOURCE_CLASS_BUTTON)) {
            //虚拟按键返回false
            return false;
        }
        Configuration cfg = context.getResources().getConfiguration();
        return cfg.keyboard != Configuration.KEYBOARD_UNDEFINED;
    }

    //检查shift键
    public void checkLetterStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                //按着shift键，表示大写
                mCaps = true;
            } else {
                //松开shift键，表示小写
                mCaps = false;
            }
        }
    }

    public char getInputCode(KeyEvent event) {
        int keyCode = event.getKeyCode();
        char aChar;

        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            //字母
            aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            //数字
            aChar = (char) ('0' + keyCode - KeyEvent.KEYCODE_0);
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            aChar = 0;
        } else {
            //其他符号
            aChar = (char) event.getUnicodeChar();
        }
        return aChar;
    }

    public String getResult() {
        return result == null ? "" : result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public static int startReadQRCode(byte[] codeInfo) {
        if (TextUtils.isEmpty(result)) {
            return 0;
        } else {
            byte[] buffer = result.getBytes();
            int len = buffer.length;
            System.arraycopy(result.getBytes(), 0, codeInfo, 0, len);
            result = null;
            return len;
        }
    }

    public static boolean hasUSBQRSCanDevice() {
        boolean isExist = false;
        Context context = MyApplication.myApp.getApplicationContext();
        InputManager im = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
        int[] devices = im.getInputDeviceIds();
        for (int id : devices) {
            InputDevice device = im.getInputDevice(id);
            if (device.getName().toLowerCase().contains("gadget")) {
                isExist = true;
            }
        }
        return isExist;
    }


    public static void clearQRCode(){
        if(!TextUtils.isEmpty(result)){
            result=null;
        }
    }


}

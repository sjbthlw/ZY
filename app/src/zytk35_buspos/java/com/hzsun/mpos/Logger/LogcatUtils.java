package com.hzsun.mpos.Logger;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.hzsun.mpos.Global.Global.LogPath;

public class LogcatUtils {

    private static final String TAG = "LogcatUtils";
    private static Process p = null;
    private static DataOutputStream os = null;


    public static void recordLog() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        prepareLogPath();

        File filename = new File(LogPath + File.separator + sdf.format(now));
        try {
            Runtime.getRuntime().exec("logcat -c");

            //String cmdParam = new String("logcat -b main -v time -f ");
            String cmdParam = new String("logcat ACodec:S JNI:S art:S OpenGLRenderer:S mali_winsys:S AudioTrack:S ViewRootImpl:S -f ");

            //logcat | grep zytk35_buspos
            //String cmdParam = new String("logcat Publicfun:D *:S");
            //String[] cmdParam=new String[]{ "logcat -b main -v time -f ","|find","com.hzsun.mpos" };

            Runtime.getRuntime().exec(cmdParam + filename.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "have exception");
            e.printStackTrace();
        }
    }

    private static void prepareLogPath() {

        Log.d(TAG, "logpath is :" + LogPath);
        if (createPath(LogPath)) {
            try {
                p = Runtime.getRuntime().exec("/system/bin/sh -");
                os = new DataOutputStream(p.getOutputStream());
            } catch (IOException e) {
                Log.d(TAG, "IOException1");
                e.printStackTrace();
            } catch (SecurityException e) {
                Log.d(TAG, "IOException2");
                e.printStackTrace();
            }
            Log.d(TAG, "init logcat service");
        }
    }

    private static boolean createPath(String path) {

        Log.d(TAG, "path is " + path);
        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdirs()) {
                Log.d(TAG, "logpath create successfully");
                return true;
            } else {
                Log.d(TAG, "logpath create fail!!!");
                return false;
            }
        } else {
            Log.d(TAG, "logpath already exists");
            return true;
        }
    }


}

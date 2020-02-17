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
            String cmdParam = new String
            ("logcat InputDispatcher:S Sensors:S CameraHal_Marvin:S CameraHal:S ACodec:S  art:S OpenGLRenderer:S mali_winsys:S AudioTrack:S ViewRootImpl:S FsInfoStructure:S FAT:S FatDirectory:S ClusterChain:S ExifInterface:S ExifInterface_JNI:S -b main -v time -f ");

            Runtime.getRuntime().exec(cmdParam + filename.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "have IOException"+e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "have Exception"+e.getMessage());
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
                Log.d(TAG, "IOException1"+e.getMessage());
                e.printStackTrace();
            } catch (SecurityException e) {
                Log.d(TAG, "SecurityException"+e.getMessage());
                e.printStackTrace();
            }catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "have Exception"+e.getMessage());
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

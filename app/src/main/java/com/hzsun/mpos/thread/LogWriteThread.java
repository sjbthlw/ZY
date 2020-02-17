package com.hzsun.mpos.thread;

import android.util.Log;

import com.hzsun.mpos.queuelogs.LogMsg;
import com.hzsun.mpos.queuelogs.LogQueueMsg;

import static com.hzsun.mpos.Global.Global.FACEPICCOUNT;
import static com.hzsun.mpos.Global.Global.LOGCOUNT;
import static com.hzsun.mpos.Global.Global.LogPath;
import static com.hzsun.mpos.Global.Global.PicPath;
import static com.hzsun.mpos.Logger.LogcatUtils.recordLog;
import static com.hzsun.mpos.Public.Publicfun.CheckAndDelFile;

public class LogWriteThread extends Thread {

    private String strShellCmd = "";

    public LogWriteThread(String strShell) {
        Log.i("LogWriteThread", "LogWriteThread 构造");
        strShellCmd = strShell;
    }

    public void writeFile() {
        LogMsg msg = null;
        while (true) {
            try {
                msg = LogQueueMsg.pollmsg();
                if (msg == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    LogQueueMsg.get(msg.getDevicenum()).info(msg.getMsg());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable er) {
                er.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        CheckAndDelFile(FACEPICCOUNT, PicPath);
        CheckAndDelFile(LOGCOUNT, LogPath);
        recordLog();

    }
}

package com.hzsun.mpos.queuelogs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class LogQueueMsg {
    private static Map<Integer, Logger> map = new ConcurrentHashMap<Integer, Logger>();
    private static LinkedBlockingQueue<LogMsg> msgqueue = new LinkedBlockingQueue<LogMsg>();
    private static LinkedBlockingQueue<LogMsg> recqueue = new LinkedBlockingQueue<LogMsg>();
    private static LinkedBlockingQueue<LogMsg> sedqueue = new LinkedBlockingQueue<LogMsg>();

    public static void offermsg(LogMsg msg) {
        msgqueue.offer(msg);
    }

    public static void offermsg(int devicenum, String str) {
        LogMsg msg = new LogMsg();
        msg.setDevicenum(devicenum);
        msg.setMsg(str);
        offermsg(msg);
    }

    public static LogMsg pollmsg() {
        return msgqueue.poll();
    }

    public static void offerrec(LogMsg msg) {
        recqueue.offer(msg);
    }

    public static void offerrec(int devicenum, byte[] data) {

        LogMsg msg = new LogMsg();
        msg.setDevicenum(devicenum);
        msg.setData(data);
        offerrec(msg);
    }

    public static LogMsg pollrec() {
        return recqueue.poll();
    }

    public static void offersed(LogMsg msg) {
        sedqueue.offer(msg);
    }

    public static void offersed(int devicenum, byte[] data) {

        LogMsg msg = new LogMsg();
        msg.setDevicenum(devicenum);
        msg.setData(data);
        offersed(msg);
    }

    public static LogMsg pollsed() {
        return recqueue.poll();
    }

    public static Logger get(Integer devicenum) {
        return map.get(devicenum);
    }

    public static void set(Integer devicenum, Logger logger) {
        map.put(devicenum, logger);
    }

    public static void remove(Integer devicenum) {
        map.remove(devicenum);
    }


}

package com.hzsun.mpos.Logger;

import android.util.Log;


/**
 * Title:L
 * Description: log统一管理类
 *
 * @date 2016年12月16日 下午6:19:29
 */
public class LogUtil {


    public static boolean isDebug = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化
    private static final String TAG = "LogUtil";
    //单例
    private static LogUtil logUtil;
    //打印调试开关
    private static boolean IS_DEBUG = true;
    //Log 单词打印的最大长度
    private static final int MAX_LENGTH = 3 * 1024;

    //单例模式初始化
    public static LogUtil getInstance() {
        if (logUtil == null) {
            logUtil = new LogUtil();
        }
        return logUtil;
    }

    /**
     * 获取 TAG 信息：文件名以及行数
     *
     * @return TAG 信息
     */
    private synchronized String getTAG() {
        StringBuilder tag = new StringBuilder();
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return "";
        }
        for (StackTraceElement st : sts) {
            //筛选获取需要打印的TAG
            if (!st.isNativeMethod() && !st.getClassName().equals(Thread.class.getName()) && !st.getClassName().equals(this.getClass().getName())) {
                //获取文件名以及打印的行数
                tag.append("(").append(st.getFileName()).append(":").append(st.getLineNumber()).append(")");
                return tag.toString();
            }
        }
        return "";
    }

    /**
     * Log.e 打印
     *
     * @param text 需要打印的内容
     */
    public synchronized void e(String text) {
        if (IS_DEBUG) {
            for (String str : splitStr(text)) {
                Log.e(getTAG(), str);
            }
        }
    }

    /**
     * Log.d 打印
     *
     * @param text 需要打印的内容
     */
    public synchronized void d(String text) {
        if (IS_DEBUG) {
            for (String str : splitStr(text)) {
                Log.d(getTAG(), str);
            }
        }
    }

    /**
     * Log.w 打印
     *
     * @param text 需要打印的内容
     */
    public synchronized void w(String text) {
        if (IS_DEBUG) {
            for (String str : splitStr(text)) {
                Log.w(getTAG(), str);
            }
        }
    }

    /**
     * Log.i 打印
     *
     * @param text 需要打印的内容
     */
    public synchronized void i(String text) {
        if (IS_DEBUG) {
            for (String str : splitStr(text)) {
                Log.i(getTAG(), str);
            }
        }
    }

    /**
     * Log.e 打印格式化后的JSON数据
     *
     * @param json 需要打印的内容
     */
    public synchronized void json(String json) {
        if (IS_DEBUG) {
            String tag = getTAG();
            try {
                //转化后的数据
                String logStr = formatJson(json);
                for (String str : splitStr(logStr)) {
                    Log.e(getTAG(), str);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(tag, e.toString());
            }
        }
    }

    /**
     * 数据分割成不超过MAX_LENGTH的数据
     *
     * @param str 需要分割的数据
     * @return 分割后的数组
     */
    private String[] splitStr(String str) {
        //字符串长度
        int length = str.length();
        //返回的数组
        String[] strs = new String[length / MAX_LENGTH + 1];
        //
        int start = 0;
        for (int i = 0; i < strs.length; i++) {
            //判断是否达到最大长度
            if (start + MAX_LENGTH < length) {
                strs[i] = str.substring(start, start + MAX_LENGTH);
                start += MAX_LENGTH;
            } else {
                strs[i] = str.substring(start, length);
                start = length;
            }
        }
        return strs;
    }


    /**
     * 格式化
     *
     * @param jsonStr json数据
     * @return 格式化后的json数据
     * @author lizhgb
     * @link https://my.oschina.net/jasonli0102/blog/517052
     */
    private String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr))
            return "";
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '"':
                    if (last != '\\') {
                        isInQuotationMarks = !isInQuotationMarks;
                    }
                    sb.append(current);
                    break;
                case '{':
                case '[':
                    sb.append(current);
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent++;
                        addIndentBlank(sb, indent);
                    }
                    break;
                case '}':
                case ']':
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent--;
                        addIndentBlank(sb, indent);
                    }
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\' && !isInQuotationMarks) {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }
        return sb.toString();
    }

    /**
     * 在 StringBuilder指定位置添加 space
     *
     * @param sb     字符集
     * @param indent 添加位置
     * @author lizhgb
     * @link https://my.oschina.net/jasonli0102/blog/517052
     */
    private void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }


    // 下面是传入自定义tag的函数
    public static synchronized void i(String tag, String msg) {
        if (isDebug && (null != msg)) {
            String[] infos = getAutoJumpLogInfos();
            Log.i(infos[0], tag + " : " + msg + " : " + infos[1] + infos[2]);
        }
    }

    public static synchronized void d(String tag, String msg) {
        if (isDebug && (null != msg)) {
            String[] infos = getAutoJumpLogInfos();
            Log.d(infos[0], tag + " : " + msg + " : " + infos[1] + infos[2]);
        }
    }

    public static synchronized void v(String tag, String msg) {
        if (isDebug && (null != msg)) {
            String[] infos = getAutoJumpLogInfos();
            Log.v(infos[0], tag + " : " + msg + " : " + infos[1] + infos[2]);
        }
    }

    public static synchronized void w(String tag, String msg) {
        if (isDebug && (null != msg)) {
            String[] infos = getAutoJumpLogInfos();
            Log.w(infos[0], tag + " : " + msg + " : " + infos[1] + infos[2]);
        }
    }

    public static synchronized void e(String tag, String msg) {
        if (isDebug && (null != msg)) {
            String[] infos = getAutoJumpLogInfos();
            Log.e(infos[0], tag + " : " + msg + " : " + infos[1] + infos[2]);
        }
    }


    /**
     * 分段打印出较长log文本
     *
     * @param logContent 打印文本
     * @param showLength 规定每段显示的长度（AndroidStudio控制台打印log的最大信息量大小为4k）
     * @param tag        打印log的标记
     */
    public static void showLargeLog(String logContent, int showLength, String tag) {
        if (logContent.length() > showLength) {
            String show = logContent.substring(0, showLength);
            e(tag, show);
            /*剩余的字符串如果大于规定显示的长度，截取剩余字符串进行递归，否则打印结果*/
            if ((logContent.length() - showLength) > showLength) {
                String partLog = logContent.substring(showLength, logContent.length());
                showLargeLog(partLog, showLength, tag);
            } else {
                String printLog = logContent.substring(showLength, logContent.length());
                e(tag, printLog);
            }

        } else {
            e(tag, logContent);
        }
    }

    /**
     * 显示Log信息（带行号）
     *
     * @param logLevel 1 v ; 2 d ; 3 i ; 4 w ; 5 e .
     * @param info     显示的log信息
     */
    public static void ShowLog(int logLevel, String info) {
        String[] infos = getAutoJumpLogInfos();
        switch (logLevel) {
            case 1:
                if (isDebug)
                    Log.v(infos[0], info + " : " + infos[1] + infos[2]);
                break;
            case 2:
                if (isDebug)
                    Log.d(infos[0], info + " : " + infos[1] + infos[2]);
                break;
            case 3:
                if (isDebug)
                    Log.i(infos[0], info + " : " + infos[1] + infos[2]);
                break;
            case 4:
                if (isDebug)
                    Log.w(infos[0], info + " : " + infos[1] + infos[2]);
                break;
            case 5:
                if (isDebug)
                    Log.e(infos[0], info + " : " + infos[1] + infos[2]);
                break;
        }
    }

    /**
     * 获取打印信息所在方法名，行号等信息
     *
     * @return
     */
    private static String[] getAutoJumpLogInfos() {
        String[] infos = new String[]{"", "", ""};
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length < 5) {
            Log.e("MyLogger", "Stack is too shallow!!!");
            return infos;
        } else {
            infos[0] = elements[4].getClassName().substring(
                    elements[4].getClassName().lastIndexOf(".") + 1);
            infos[1] = elements[4].getMethodName() + "()";
            infos[2] = " at (" + elements[4].getClassName() + ".java:"
                    + elements[4].getLineNumber() + ")";
            return infos;
        }
    }
}

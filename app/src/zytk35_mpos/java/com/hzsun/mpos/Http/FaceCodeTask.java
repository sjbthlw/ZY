package com.hzsun.mpos.Http;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.huiyuenet.faceCheck.FaceCheck;
import com.huiyuenet.faceCheck.FaceFunction;
import com.huiyuenet.faceCheck.THFI_FacePos;
import com.huiyuenet.faceCheck.THFI_Param;
import com.hzsun.mpos.Public.FileUtils;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.data.FaceCodeInfoRW;
import com.hzsun.mpos.data.WasteFacePayBooks;
import com.hzsun.mpos.data.WasteFacePayBooksRW;
import com.hzsun.mpos.thread.LoadFeatureThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.hzsun.mpos.Activity.CardActivity.HTTPPHOTO;
import static com.hzsun.mpos.Global.Global.APPSTARTUP;
import static com.hzsun.mpos.Global.Global.APPUPABN;
import static com.hzsun.mpos.Global.Global.APPUPING;
import static com.hzsun.mpos.Global.Global.APPUPOVER;
import static com.hzsun.mpos.Global.Global.FACEFEASIZE;
import static com.hzsun.mpos.Global.Global.LAN_DEVTYPE;
import static com.hzsun.mpos.Global.Global.PhotoPath;
import static com.hzsun.mpos.Global.Global.PicPath;
import static com.hzsun.mpos.Global.Global.ROMUPABN;
import static com.hzsun.mpos.Global.Global.ROMUPING;
import static com.hzsun.mpos.Global.Global.ROMUPOVER;
import static com.hzsun.mpos.Global.Global.SDPath;
import static com.hzsun.mpos.Global.Global.ZYTK35Path;
import static com.hzsun.mpos.Global.Global.ZYTKFacePath;
import static com.hzsun.mpos.Global.Global.g_HttpCommInfo;
import static com.hzsun.mpos.Global.Global.g_UICardHandler;
import static com.hzsun.mpos.Global.Global.g_UIMainHandler;
import static com.hzsun.mpos.Global.Global.g_BasicInfo;
import static com.hzsun.mpos.Global.Global.g_FaceCodeInfo;
import static com.hzsun.mpos.Global.Global.g_FaceIdentInfo;
import static com.hzsun.mpos.Global.Global.g_StationInfo;
import static com.hzsun.mpos.Global.Global.g_SystemInfo;
import static com.hzsun.mpos.Global.Global.g_WasteFaceBookInfo;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.MyApplication.uiHandler;
import static com.hzsun.mpos.Public.FileUtils.getFileSize;
import static com.hzsun.mpos.Public.FileUtils.getImageType;
import static com.hzsun.mpos.Public.FileUtils.readStream;
import static com.hzsun.mpos.Public.FileUtils.renameFile;
import static com.hzsun.mpos.Public.NumConvertUtils.getUnsignedIntt;
import static com.hzsun.mpos.Public.Publicfun.GetAppFileName;
import static com.hzsun.mpos.Public.Publicfun.GetFaceNumListPos;
import static com.hzsun.mpos.Public.Publicfun.GetMultiAccID;
import static com.hzsun.mpos.Public.Publicfun.ReadSofeVerInfoFile;
import static com.hzsun.mpos.Public.Publicfun.RunShellCmd;
import static com.hzsun.mpos.Public.Publicfun.WriteSofeVerInfoFile;
/*
人脸特征码存储根据人脸全字段名单存储和 accnum名单存储，查看accnum的方式定位位置替换原有的名单字段(GetFaceNumListPos)
 */

public class FaceCodeTask {

    private static final String TAG = "FaceCodeTask";

    private String strDevicesn = ""; //终端序列号
    private String strEpid = "";    //使用单位

    private String typenum = "";     //typenum 设备类型
    private String modelnum = "";  //modelnum 设备型号

    private int sInitReflash;  //初始化刷新
    private long slngTimer;
    private long slngErrCnt;
    private int sInitFaceCodeID;  //人脸码名单位置id
    private Timer HttpTimer;
    private TimerTask HttpTimetask;

    private GetHttpMsgThread ThreadgetHttpMsg;
    private HttpRecvThread ThreadHttpRecv;
    private GetFaceCodeThread ThreadGetFaceCode;
    // 用于将从Http服务器获取的消息显示出来
    public static Handler gHttpRecvHandler;

    public static List<Userinfo> gUserInfoList;    //下发所有信息
    public static ArrayList<String> gStrUserInfoList;  //名字，地址，标记

    public static final int GETVERSION_CODE = 1;
    public static final int GETINITDATA_CODE = 2;
    public static final int GETCHANGEDATA_CODE = 3;
    public static final int GETPHOTO_CODE = 4;
    public static final int UPDATEAPP_CODE = 5;
    public static final int UPLOADRECORD_CODE = 6;
    public static final int GETINITPIC_CODE = 7;
    public static final int GETCHANGEPIC_CODE = 8;

    //------------------事件状态位-----------------
    public static final int HEVT_NULL = 0;
    public static final int HEVT_GETFACEVER = 1;                //获取特征码版本号
    public static final int HEVT_GETFACEINITDATA = 2;                //获取初始特征码数据列表
    public static final int HEVT_GETFACECHANDATA = 3;                //获取变更特征码数据列表
    public static final int HEVT_GETPHOTOINFO = 4;                //获取用户照片数据地址
    public static final int HEVT_GETAPPINFO = 5;                //获取程序升级app地址
    public static final int HEVT_UPLOADRECORD = 6;                //获取上次流水结果
    public static final int HEVT_GETFACEINITPIC = 7;                //获取初始图片数据列表
    public static final int HEVT_GETFACECHANPIC = 8;                //获取变更图片数据列表

    public static final int HEVT_CONTIMEOUT = 100;                  //http连接超时
    public static final int HEVT_READTIMEOUT = 101;                  //http读取数据超时
    public static final int HEVT_WRITETIMEOUT = 102;                  //http发送数据超时
    public static final int HEVT_DISCONNECT = 200;               //http断开

    public void FaceCodeTask() {
        Log.d(TAG, "FaceCodeTask: 构造");
    }


    public void Init() {
        Log.d(TAG, "FaceCodeTask 初始化");

        Log.d(TAG, String.format("人脸特征本地版本号:%d", g_FaceCodeInfo.lngLocalVer));
        Log.d(TAG, String.format("人脸特征平台版本号:%d", g_FaceCodeInfo.lngPlatVer));

        slngTimer = 0;
        slngErrCnt=0;
        sInitReflash = 0;
        sInitFaceCodeID = g_FaceIdentInfo.FaceNameList.size();

        //g_HttpCommInfo.iFaceServeType=1; //测试老版本协议

        strDevicesn = String.format("%02x.%02x.%02x.%02x.%02x.%02x",
                g_BasicInfo.cTerminalSerID[0], g_BasicInfo.cTerminalSerID[1], g_BasicInfo.cTerminalSerID[2],
                g_BasicInfo.cTerminalSerID[3], g_BasicInfo.cTerminalSerID[4], g_BasicInfo.cTerminalSerID[5]); //终端序列号 2e4a42cdb6f4
        strEpid = "";    //使用单位

        //发送请求人脸服务
        if (ThreadgetHttpMsg == null) {
            ThreadgetHttpMsg = new GetHttpMsgThread();
            ThreadgetHttpMsg.start();
        }

        //处理接收人脸服务应答
        if (ThreadHttpRecv == null) {
            ThreadHttpRecv = new HttpRecvThread();
            ThreadHttpRecv.start();
        }

        //根据特征码路径获取特征码数据
        if (ThreadGetFaceCode == null) {
            ThreadGetFaceCode = new GetFaceCodeThread();
            ThreadGetFaceCode.start();
        }

        HttpTimer = new Timer();
        HttpTimetask = new TimerTask() {
            @Override
            public void run() {

                SendHttpVerMSG();
            }
        };
        // 参数：1000，延时1秒后执行。 100，每隔0.1秒执行1次task。
        HttpTimer.schedule(HttpTimetask, 1000, 1000);
    }

    //处理HTTP接收的报文数据
    class HttpRecvThread extends Thread {

        private boolean isStart = false;

        public void StopThread() {
            isStart = false;
            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            super.run();
            isStart = true;
            {
                Looper.prepare();
                gHttpRecvHandler = new Handler() {

                    int cResult = 0;
                    String strRecvData = "";

                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case HEVT_NULL:
                                Log.d(TAG, "HEVT_NULL 清除标记");
                                g_HttpCommInfo.lngSendHttpStatus = 0;
                                g_HttpCommInfo.cRecvWaitState = 0;
                                g_WorkInfo.cUpdateState = 0;
                                break;

                            case HEVT_GETFACEVER:               //获取特征码版本号
                                Log.d(TAG, "获取特征码版本号");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000001) == 0x00000001) {
                                    g_HttpCommInfo.lngSendHttpStatus = g_HttpCommInfo.lngSendHttpStatus - 0x00000001;
                                    Log.d(TAG, "获取特征码版本号接收");
                                }
                                strRecvData = (String) msg.obj;
                                try {
                                    GetVersion jsVerInfo = new Gson().fromJson(strRecvData, GetVersion.class);
                                    String strMsg = jsVerInfo.getMsg();
                                    if (strMsg.equals("SUCCESS")) {
                                        //比较版本号是否一致
                                        //判断是初始还是变更
                                        int iVersion = Integer.parseInt(jsVerInfo.getVersion());
                                        if ((g_FaceCodeInfo.lngLocalVer == 0)//初始版本号为0
                                                || (g_FaceCodeInfo.lngPlatVer == iVersion))//记录的初始获取的平台版本号和下发的是否一致
                                        //if (g_FaceCodeInfo.lngLocalVer == 0)//初始版本号为0
                                        {
                                            Log.d(TAG, "获取到的初始版本号:" + jsVerInfo.getVersion());
                                            if (iVersion != g_FaceCodeInfo.lngLocalVer) {
                                                Log.d(TAG, "版本号不一致,初始:" + g_FaceCodeInfo.lngLocalVer + "," + iVersion);
                                                //记录初始版本号
                                                //更新版本号
                                                g_FaceCodeInfo.lngPlatVer = iVersion;

                                                FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                                                Log.d(TAG, "记录平台版本号:" + iVersion);

                                                if (g_HttpCommInfo.iFaceServeType  == 0) {
                                                    //0x00000100 //获取初始图片数据列表
                                                    g_HttpCommInfo.lngSendHttpStatus |= 0x00000100;
                                                } else {
                                                    //0x00000002 获取人脸特征码数据表
                                                    g_HttpCommInfo.lngSendHttpStatus |= 0x00000002;
                                                }
                                            } else {
                                                if ((iVersion == g_FaceCodeInfo.lngLocalVer)
                                                        && (iVersion == g_FaceCodeInfo.lngPlatVer)
                                                        && (sInitReflash == 0)) {
                                                    sInitReflash = 1;
                                                    Log.d(TAG, "更新人脸特征码内存");
                                                    if (g_SystemInfo.cFaceDetectFlag == 1) {
                                                        LoadFeatureThread ThreadLoadFeature = new LoadFeatureThread(uiHandler);
                                                        ThreadLoadFeature.start();
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.d(TAG, "获取到的变更版本号:" + iVersion);
                                            if (iVersion != g_FaceCodeInfo.lngLocalVer) {
                                                g_FaceCodeInfo.lngPlatVer = iVersion;
                                                Log.d(TAG, "版本号不一致,变更:" + g_FaceCodeInfo.lngLocalVer + "," + jsVerInfo.getVersion());
                                                if (g_HttpCommInfo.iFaceServeType == 0) {
                                                    //0x00000200 //获取变更图片数据列表
                                                    g_HttpCommInfo.lngSendHttpStatus |= 0x00000200;
                                                } else {
                                                    //0x00000002 获取变更人脸特征码数据表
                                                    g_HttpCommInfo.lngSendHttpStatus |= 0x00000004;
                                                }
                                            }
                                        }
                                    } else {
                                        Log.d(TAG, jsVerInfo.getMsg());
                                    }
                                } catch (JsonSyntaxException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "JsonSyntaxException:" + e.getMessage());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Exception:" + e.getMessage());
                                }
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_GETFACEINITDATA:               //获取初始特征码数据列表
                                Log.d(TAG, "获取初始特征码数据列表");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000002) == 0x00000002) {
                                    g_HttpCommInfo.lngSendHttpStatus = g_HttpCommInfo.lngSendHttpStatus - 0x00000002;
                                    Log.d(TAG, "获取初始特征码数据列表接收");
                                }
                                strRecvData = (String) msg.obj;
                                try {
                                    GetData jsFaceInitDataInfo = new Gson().fromJson(strRecvData, GetData.class);
                                    if (jsFaceInitDataInfo.getMsg().equals("SUCCESS")) {
                                        //请求成功
                                        String userInfo = jsFaceInitDataInfo.getUser_info();
                                        AnalyzeFaceCodeData(userInfo, 0);
                                        if (gUserInfoList.size() != 0) {
                                            Log.d(TAG, "==========开始下载初始特征码数据=========");
                                            g_HttpCommInfo.cDownMode = 0;
                                            g_HttpCommInfo.cGetFaceCodeState = 1;
                                            g_HttpCommInfo.lngSendHttpStatus |= 0x00000002; //再次下载
                                        } else {
                                            Log.d(TAG, "==========下载初始特征码数据结束=========");
                                        }
                                    } else {
                                        //Log.d(TAG, jsFaceInitDataInfo.getMsg());
                                        //数据库没有对应的查询记录
                                        if (jsFaceInitDataInfo.getMsg().equals("数据库没有对应的查询记录")) {
                                            Log.d(TAG, String.format("版本号 lngLocalVer:%d-lngPlatVer:%d", g_FaceCodeInfo.lngLocalVer, g_FaceCodeInfo.lngPlatVer));
                                            if (g_FaceCodeInfo.lngLocalVer > g_FaceCodeInfo.lngPlatVer)
                                                g_FaceCodeInfo.lngPlatVer = g_FaceCodeInfo.lngLocalVer;
                                            else {
                                                g_FaceCodeInfo.lngLocalVer++;
                                                if (g_FaceCodeInfo.lngLocalVer >= g_FaceCodeInfo.lngPlatVer)
                                                    g_FaceCodeInfo.lngLocalVer = g_FaceCodeInfo.lngPlatVer;
                                            }
                                            FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                                            g_HttpCommInfo.lngSendHttpStatus |= 0x00000001;
                                        }
                                    }
                                } catch (JsonSyntaxException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "JsonSyntaxException:" + e.getMessage());
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Exception:" + e.getMessage());
                                }
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_GETFACEINITPIC:  //获取初始图片数据列表
                                Log.d(TAG, "获取初始图片数据列表");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000100) == 0x00000100) {
                                    g_HttpCommInfo.lngSendHttpStatus = g_HttpCommInfo.lngSendHttpStatus - 0x00000100;
                                    Log.d(TAG, "获取初始图片数据列表接收");
                                }
                                strRecvData = (String) msg.obj;
                                try {
                                    GetData jsFaceInitDataInfo = new Gson().fromJson(strRecvData, GetData.class);
                                    if (jsFaceInitDataInfo.getMsg().equals("SUCCESS")) {
                                        //请求成功
                                        String userInfo = jsFaceInitDataInfo.getUser_info();
                                        AnalyzeFaceCodeData(userInfo, 0);
                                        if (gUserInfoList.size() != 0) {
                                            Log.d(TAG, "==========开始下载图片初始特征码数据=========");
                                            g_HttpCommInfo.cDownMode = 0;
                                            g_HttpCommInfo.cGetFaceCodeState = 1;
                                            g_HttpCommInfo.lngSendHttpStatus |= 0x00000100;
                                        } else {
                                            Log.d(TAG, "==========下载图片初始特征码数据结束=========");
                                        }
                                    } else {
                                        //Log.d(TAG, jsFaceInitDataInfo.getMsg());
                                        //数据库没有对应的查询记录
                                        if (jsFaceInitDataInfo.getMsg().equals("数据库没有对应的查询记录")) {
                                            Log.d(TAG, String.format("版本号 lngLocalVer:%d-lngPlatVer:%d", g_FaceCodeInfo.lngLocalVer, g_FaceCodeInfo.lngPlatVer));
                                            if (g_FaceCodeInfo.lngLocalVer > g_FaceCodeInfo.lngPlatVer)
                                                g_FaceCodeInfo.lngPlatVer = g_FaceCodeInfo.lngLocalVer;
                                            else {
                                                g_FaceCodeInfo.lngLocalVer++;
                                                if (g_FaceCodeInfo.lngLocalVer >= g_FaceCodeInfo.lngPlatVer)
                                                    g_FaceCodeInfo.lngLocalVer = g_FaceCodeInfo.lngPlatVer;
                                            }
                                            FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                                            g_HttpCommInfo.lngSendHttpStatus |= 0x00000001;
                                        } else if (jsFaceInitDataInfo.getMsg().equals("调用方法名出错!") &&
                                                jsFaceInitDataInfo.getCode().equals("A10009")) {
                                            g_HttpCommInfo.iFaceServeType = 1;
                                            Log.e("获取老版本特征码", "数据列表");
                                            g_HttpCommInfo.lngSendHttpStatus |= 0x00000002;
                                        }
                                    }
                                } catch (JsonSyntaxException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "JsonSyntaxException:" + e.getMessage());
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Exception:" + e.getMessage());
                                }
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_GETFACECHANDATA:               //获取变更特征码数据列表
                                Log.d(TAG, "获取变更特征码数据列表");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000004) == 0x00000004) {
                                    g_HttpCommInfo.lngSendHttpStatus = g_HttpCommInfo.lngSendHttpStatus - 0x00000004;
                                    Log.d(TAG, "获取变更特征码数据列表接收");
                                }
                                strRecvData = (String) msg.obj;
                                try {
                                    GetData jsFaceChangeDataInfo = new Gson().fromJson(strRecvData, GetData.class);
                                    if (jsFaceChangeDataInfo.getMsg().equals("SUCCESS")) {
                                        //请求成功
                                        String userInfo = jsFaceChangeDataInfo.getUser_info();
                                        AnalyzeFaceCodeData(userInfo, 1);
                                        if (gUserInfoList.size() != 0) {
                                            Log.d(TAG, "==========开始下载变更特征码数据=========");
                                            g_HttpCommInfo.cDownMode = 1;
                                            g_HttpCommInfo.cGetFaceCodeState = 1;
                                            g_HttpCommInfo.lngSendHttpStatus |= 0x00000004;
                                        } else {
                                            Log.d(TAG, "==========下载变更特征码数据结束=========");
                                        }
                                    } else {
                                        Log.d(TAG, jsFaceChangeDataInfo.getMsg());
                                        //数据库没有对应的查询记录
                                        if (jsFaceChangeDataInfo.getMsg().equals("数据库没有对应的查询记录")) {
                                            Log.d(TAG, String.format("版本号 lngLocalVer:%d-lngPlatVer:%d", g_FaceCodeInfo.lngLocalVer, g_FaceCodeInfo.lngPlatVer));
                                            if (g_FaceCodeInfo.lngLocalVer > g_FaceCodeInfo.lngPlatVer)
                                                g_FaceCodeInfo.lngPlatVer = g_FaceCodeInfo.lngLocalVer;
                                            else {
                                                g_FaceCodeInfo.lngLocalVer++;
                                                if (g_FaceCodeInfo.lngLocalVer >= g_FaceCodeInfo.lngPlatVer)
                                                    g_FaceCodeInfo.lngLocalVer = g_FaceCodeInfo.lngPlatVer;
                                            }
                                            FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                                        }
                                    }
                                } catch (JsonSyntaxException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "JsonSyntaxException:" + e.getMessage());
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Exception:" + e.getMessage());
                                }
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_GETFACECHANPIC:  //获取变更图片数据列表
                                Log.d(TAG, "获取变更图片数据列表");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000200) == 0x00000200) {
                                    g_HttpCommInfo.lngSendHttpStatus = g_HttpCommInfo.lngSendHttpStatus - 0x00000200;
                                    Log.d(TAG, "获取变更图片数据列表接收");
                                }
                                strRecvData = (String) msg.obj;
                                try {
                                    GetData jsFaceChangeDataInfo = new Gson().fromJson(strRecvData, GetData.class);
                                    if (jsFaceChangeDataInfo.getMsg().equals("SUCCESS")) {
                                        //请求成功
                                        String userInfo = jsFaceChangeDataInfo.getUser_info();
                                        AnalyzeFaceCodeData(userInfo,1);
                                        if (gUserInfoList.size() != 0) {
                                            Log.d(TAG, "==========开始下载图片变更特征码数据=========");
                                            g_HttpCommInfo.cDownMode = 1;
                                            g_HttpCommInfo.cGetFaceCodeState = 1;
                                            g_HttpCommInfo.lngSendHttpStatus |= 0x00000200;
                                        } else {
                                            Log.d(TAG, "==========下载图片变更特征码数据结束=========");
                                        }
                                    } else {
                                        Log.d(TAG, jsFaceChangeDataInfo.getMsg());
                                        //数据库没有对应的查询记录
                                        if (jsFaceChangeDataInfo.getMsg().equals("数据库没有对应的查询记录")) {
                                            Log.d(TAG, String.format("版本号 lngLocalVer:%d-lngPlatVer:%d", g_FaceCodeInfo.lngLocalVer, g_FaceCodeInfo.lngPlatVer));
                                            if (g_FaceCodeInfo.lngLocalVer > g_FaceCodeInfo.lngPlatVer)
                                                g_FaceCodeInfo.lngPlatVer = g_FaceCodeInfo.lngLocalVer;
                                            else {
                                                g_FaceCodeInfo.lngLocalVer++;
                                                if (g_FaceCodeInfo.lngLocalVer >= g_FaceCodeInfo.lngPlatVer)
                                                    g_FaceCodeInfo.lngLocalVer = g_FaceCodeInfo.lngPlatVer;
                                            }
                                            FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                                        } else if (jsFaceChangeDataInfo.getMsg().equals("调用方法名出错!") &&
                                                jsFaceChangeDataInfo.getCode().equals("A10009")) {
                                            g_HttpCommInfo.iFaceServeType = 1;
                                            Log.e("获取老版本变更特征码", "数据列表");
                                            g_HttpCommInfo.lngSendHttpStatus |= 0x00000004;
                                        }
                                    }
                                } catch (JsonSyntaxException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "JsonSyntaxException:" + e.getMessage());
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Exception:" + e.getMessage());
                                }
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_GETPHOTOINFO:               //获取用户照片数据地址
                                Log.d(TAG, "获取用户照片数据地址");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000008) == 0x00000008) {
                                    g_HttpCommInfo.lngSendHttpStatus = g_HttpCommInfo.lngSendHttpStatus - 0x00000008;
                                    Log.d(TAG, "获取用户照片数据地址接收");
                                }
                                strRecvData = (String) msg.obj;
                                //Log.d(TAG, strRecvData);
                                try {
                                    GetPhoto jsPhotoInfo = new Gson().fromJson(strRecvData, GetPhoto.class);
                                    if (jsPhotoInfo.getMsg().equals("SUCCESS")) {
                                        //请求成功
                                        String userInfo = jsPhotoInfo.getUser_info();
                                        PhotoInfo photoInfo = AnalyzePhotoData(userInfo);
                                        GetUserPhotoThread ThreadGetUserPhoto = new GetUserPhotoThread(photoInfo);
                                        ThreadGetUserPhoto.start();
                                    } else {
                                        Log.d(TAG, jsPhotoInfo.getMsg());
                                    }
                                } catch (JsonSyntaxException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "JsonSyntaxException:" + e.getMessage());
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Exception:" + e.getMessage());
                                }
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_GETAPPINFO:               //获取程序升级app地址
                                Log.d(TAG, "获取程序升级app地址");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000010) == 0x00000010) {
                                    g_HttpCommInfo.lngSendHttpStatus = g_HttpCommInfo.lngSendHttpStatus - 0x00000010;
                                    Log.d(TAG, "获取程序升级app地址接收");
                                }
                                strRecvData = (String) msg.obj;
                                try {
                                    GetAppVer jsAppVerInfo = new Gson().fromJson(strRecvData, GetAppVer.class);
                                    if (jsAppVerInfo.getMsg().equals("SUCCESS")) {
                                        //请求成功
                                        String strDownAppVer = jsAppVerInfo.getSoftwareversion();
                                        Log.d(TAG, "接收到的程序版本号:" + strDownAppVer);
                                        //35需要处理版本号3.5.19.108-->3.5.19.0108
                                        String[] strTemp = strDownAppVer.split("\\.");
                                        if (strTemp[3].length() != 4) {
                                            strDownAppVer = strTemp[0] + "." + strTemp[1] + "." + strTemp[2] + ".0" + strTemp[3];
                                            Log.d(TAG, "处理后的程序版本号:" + strDownAppVer);
                                        }
                                        g_WorkInfo.strAppSoftWareVer = strDownAppVer;

                                        //判断是否存在现有的apk                                        
                                        g_WorkInfo.strAppFileName = GetAppFileName();
										Log.d(TAG, "读apk文件名：" + g_WorkInfo.strAppFileName);
                                        if ((!g_WorkInfo.strAppFileName.equals("")) && (g_WorkInfo.strAppFileName.length() > 23)) {
                                            String strTmpVer = g_WorkInfo.strAppFileName.substring(12, 23);
                                            if (strTmpVer.equals(strDownAppVer)) {
                                                Log.d(TAG, " 版本号一致退出升级：" + strTmpVer);
                                                g_WorkInfo.cUpdateState = 0;
                                                g_HttpCommInfo.cRecvWaitState = 0;
                                                break;
                                            }
                                        }
                                        String strLocalAppVer = ReadSofeVerInfoFile().substring(0, 11);
                                        Log.d(TAG, " 本机的程序版本号:" + strLocalAppVer);
                                        if ((jsAppVerInfo.getSoftwareversion() != null) && (jsAppVerInfo.getSoftware_url() != null)) {
                                            //判断是否升级
                                            if (strDownAppVer.equals(strLocalAppVer)) {
                                                Log.d(TAG, " 版本号一致:");
                                                g_WorkInfo.cUpdateState = 0;
                                            } else {
                                                Log.d(TAG, " 版本号不一致，获取程序包:");
                                                Message msga = Message.obtain();
                                                msga.what = APPSTARTUP;
                                                if (g_UIMainHandler != null)
                                                    g_UIMainHandler.sendMessage(msga);

                                                g_WorkInfo.cUpdateState = 2;
                                                String strSoftware_url = jsAppVerInfo.getSoftware_url();
                                                GetAppDataThread ThreadGetAppData = new GetAppDataThread(strSoftware_url);
                                                ThreadGetAppData.start();
                                            }
                                        }
                                    } else {
                                        Log.e(TAG, jsAppVerInfo.getMsg());
                                        //不升级进入刷卡界面
                                        g_WorkInfo.cUpdateState = 0;
                                    }
                                } catch (JsonSyntaxException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "JsonSyntaxException:" + e.getMessage());
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Exception:" + e.getMessage());
                                }
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_UPLOADRECORD:               //上传人脸流水结果
                                Log.d(TAG, "上传人脸流水结果");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000020) == 0x00000020) {
                                    g_HttpCommInfo.lngSendHttpStatus = g_HttpCommInfo.lngSendHttpStatus - 0x00000020;
                                    Log.d(TAG, "获取上传人脸流水结果接收");
                                }
                                strRecvData = (String) msg.obj;
                                try {
                                    GetUpRecord jsUpRecordInfo = new Gson().fromJson(strRecvData, GetUpRecord.class);
                                    if ((jsUpRecordInfo.getCode()!=null)&&(jsUpRecordInfo.getCode().equals("00000"))) {
                                        //请求成功
                                        Log.d(TAG, "接收到的Mag:" + jsUpRecordInfo.getMsg());
                                    } else {
                                        if (jsUpRecordInfo.getCode()!=null)
                                            Log.e(TAG, jsUpRecordInfo.getCode());
                                        if (jsUpRecordInfo.getMsg()!=null)
                                            Log.e(TAG, jsUpRecordInfo.getMsg());

                                        FileUtils.writeToFile("上传人脸照片流水失败:"+g_WorkInfo.lngEndFRecordID + "\r\n",PicPath, "upfacepicerr.txt");
                                    }
                                    //判断上传流水和记录流水的大小
                                    if (g_WasteFaceBookInfo.WriterIndex < g_WorkInfo.lngEndFRecordID) {
                                        Log.e(TAG, "记录人脸流水小于上传流水" );
                                        break;
                                    }
                                    g_WasteFaceBookInfo.TransferIndex = g_WorkInfo.lngEndFRecordID;
                                    cResult = WasteFacePayBooksRW.WriteWasteFacePayBookInfo(g_WasteFaceBookInfo);
                                    if (cResult == 0) {
                                        Log.d(TAG, String.format("已记录流水号:%d,已上传流水号:%d", g_WasteFaceBookInfo.WriterIndex, g_WasteFaceBookInfo.TransferIndex));
                                    } else {
                                        Log.e(TAG, String.format("写参数数据失败:%d", cResult));
                                        break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "Exception:" + e.getMessage());
                                }
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_CONTIMEOUT:               //http连接超时
                                Log.e(TAG, "http HEVT_CONTIMEOUT 连接超时");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000010) == 0x00000010) {
                                    g_WorkInfo.cUpdateState = 0;
                                }
                                g_HttpCommInfo.lngSendHttpStatus = 0;
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_READTIMEOUT:               //http读取数据超时
                                Log.e(TAG, "http HEVT_READTIMEOUT 读取数据超时");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000010) == 0x00000010) {
                                    g_WorkInfo.cUpdateState = 0;
                                }
                                g_HttpCommInfo.lngSendHttpStatus = 0;
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_WRITETIMEOUT:               //http发送数据超时
                                Log.e(TAG, "http HEVT_WRITETIMEOUT 发送数据超时");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000010) == 0x00000010) {
                                    g_WorkInfo.cUpdateState = 0;
                                }
                                g_HttpCommInfo.lngSendHttpStatus = 0;
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;

                            case HEVT_DISCONNECT:               //http断开
                                Log.e(TAG, "http HEVT_DISCONNECT 断开");
                                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000010) == 0x00000010) {
                                    g_WorkInfo.cUpdateState = 0;
                                }
                                g_HttpCommInfo.lngSendHttpStatus = 0;
                                g_HttpCommInfo.cRecvWaitState = 0;
                                break;
                        }
                        super.handleMessage(msg);
                    }
                };
                Looper.loop();
            }
        }
    }

    //获取HTTP信息线程
    class GetHttpMsgThread extends Thread {

        private String TAG = getClass().getSimpleName();
        private volatile boolean isStart = false;

        public GetHttpMsgThread() {
            Log.d(TAG, "GetHttpMsgThread 构造");
        }

        public void StopThread() {
            isStart = false;

            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }
        @Override
        public void run() {
            isStart = true;

            while (isStart) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (Exception e) {
                }
                SendHttpMsgHand();
            }
        }
    }

    //获取特征码数据
    class GetFaceCodeThread extends Thread {

        private String TAG = getClass().getSimpleName();
        private volatile boolean isStart = false;
        private volatile boolean isPause = false;
        private volatile int sMode = 0;
        private List<Userinfo> sUserInfoList;

        public GetFaceCodeThread() {
            Log.d(TAG, "GetFaceCodeThread 构造");
            sUserInfoList = new ArrayList<Userinfo>();
            sMode = 0;
        }

        public void SetInfo(List<Userinfo> UserInfoList, int iMode) {
            sUserInfoList = UserInfoList;
            sMode = iMode;
        }

        public void StopThread() {
            isStart = false;

            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }

        //获取并保存初始特征码数据
        public int SaveInitFaceCodeData() {
            int j = 0;
            int iRet = 0;
            int iErrCnt = 0;
            int iReDownFlag=0;
            //int iFaceCodePos=0;

            for (j = 0; j < gUserInfoList.size(); j++) {
                if(iReDownFlag==1) //重新下载
                {
                    if(j > 0)
                        j = j - 1;
                    Log.e(TAG, "重新下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                }
                if (gUserInfoList.get(j).flag.equals("3")
                        || (gUserInfoList.get(j).feature_url == null)) {
                    Log.d(TAG, "删除的账号:" + gUserInfoList.get(j).accnum + "-" + gUserInfoList.get(j).flag);
                    continue;
                }
                if (gUserInfoList.get(j).accnum.equals("")) {
                    Log.e(TAG, "准备下载的账号为空:" + gUserInfoList.get(j).accnum);
                    continue;
                }

                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    iRet = 0;
                    Log.d(TAG, "准备下载的账号:"+gUserInfoList.get(j).accnum);
                    Log.d(TAG, "准备下载的版本号:"+gUserInfoList.get(j).version);
                    URL url = new URL(gUserInfoList.get(j).feature_url);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int code = connection.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        //服务器成功处理了部分 GET 请求。
                        inputStream = connection.getInputStream();
                        byte[] feature = FileUtils.SaveFaceCodeData(inputStream, g_FaceIdentInfo.FaceNameList.size());
                        if (feature == null) {
                            Log.e(TAG, "特征码数据错误");
                            Log.d(TAG, "准备下载的账号:" + gUserInfoList.get(j).accnum);
                            continue;
                        }
                        String accname = gUserInfoList.get(j).accname.length() < 16 ? gUserInfoList.get(j).accname : gUserInfoList.get(j).accname.substring(0, 15);
                        String strTemp = gUserInfoList.get(j).accnum + ","
                                + accname + ","
                                + gUserInfoList.get(j).flag;
                        //Log.d(TAG, strTemp);
                        byte[] sbTemp = strTemp.getBytes();
                        FaceCodeInfoRW.WriteFaceCodeInfoData(sbTemp, g_FaceIdentInfo.FaceNameList.size());
                        g_FaceIdentInfo.FaceNameList.add(strTemp);
                        g_FaceIdentInfo.FaceAccList.add(gUserInfoList.get(j).accnum);

                        if (feature != null) {
                            int iResult = FaceCheck.addFeature(sInitFaceCodeID, feature);
                            if (iResult != 0) {
                                Log.e("addFeature", "addFeature end iResult:" + iResult);
                            }
                        }
                        sInitFaceCodeID++;
                        Log.e(TAG, "下载特征码数据的账号成功:" + gUserInfoList.get(j).version+"-"+ gUserInfoList.get(j).accnum);
                        g_FaceCodeInfo.lngLocalVer = Long.parseLong(gUserInfoList.get(j).version);
                        FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);

                        iRet = 0;
                        iErrCnt = 0;
                        iReDownFlag=0;
                    } else {
                        Log.e(TAG, "返回码:" + code);
                        Log.e(TAG, "下载错误的账号:" + gUserInfoList.get(j).accnum);
                        Log.e(TAG, "下载错误的账号状态:" + gUserInfoList.get(j).flag);
                        Log.e(TAG, "下载错误的账号地址:" + gUserInfoList.get(j).feature_url);
                        Log.e(TAG, "下载错误的版本号:" + gUserInfoList.get(j).version);
                        iRet = 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "下载出现异常：" + e.toString());
                    iRet = 2;
                } finally {
                    try {
                        connection.disconnect();
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (iRet != 0) {
                    Log.e(TAG, "处理异常:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                    if (iRet == 1) {
                        iErrCnt++;
                        if (iErrCnt > 10) {
                            Log.e(TAG, "处理异常跳过下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                            iRet = 0;
                            iErrCnt = 0;
                            iReDownFlag=0;
                            SystemClock.sleep(100);
                        } else {
                            Log.e(TAG, "处理异常重新下载:" + j + "acc-" + gUserInfoList.get(j).accnum+ "ver-" + gUserInfoList.get(j).version);
                            iReDownFlag=1;//重新下载
                        }
                        continue;
                    } else if (iRet == 2) {
                        iReDownFlag=1;//重新下载
                        iErrCnt++;
                        SystemClock.sleep(100);
                        if (iErrCnt > 10) {
                            Log.e(TAG, "处理异常,断开连接重新下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                            iRet = 0;
                            iErrCnt = 0;
                            iReDownFlag=0;
                            break;
                        }
                    }
                }
            }
            //更新版本号
            if (j < 1)
                return 1;

            try {
                g_FaceCodeInfo.lngLocalVer = Long.parseLong(gUserInfoList.get(j - 1).version);
                FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                Log.d(TAG, "更新的初始版本号:" + gUserInfoList.get(j - 1).version);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "初始版本号数据转换异常:" + e.getMessage());
            }
            return 0;
        }

        //获取并保存初始特征码图片数据
        private int SaveInitFacePicData() {
            int j = 0;
            int iRet = 0;
            int iErrCnt = 0;
            int iReDownFlag=0;
            //int iFaceCodePos=0;

            for (j = 0; j < gUserInfoList.size(); j++) {
                if(iReDownFlag==1) //重新下载
                {
                    if(j > 0)
                        j = j - 1;
                    Log.e(TAG, "重新下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                }
                if (gUserInfoList.get(j).flag.equals("3")
                        || (gUserInfoList.get(j).pic_url == null)) {
                    Log.d(TAG, "删除的账号:" + gUserInfoList.get(j).accnum + "-" + gUserInfoList.get(j).flag);
                    continue;
                }
                if (gUserInfoList.get(j).accnum.equals("")) {
                    Log.e(TAG, "准备下载的账号为空:" + gUserInfoList.get(j).accnum);
                    continue;
                }
                if (gUserInfoList.get(j).pic_url == null)
                    continue;
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    iRet = 0;
                    Log.d(TAG, "准备下载的账号:"+gUserInfoList.get(j).accnum);
                    Log.d(TAG, "准备下载的版本号:"+gUserInfoList.get(j).version);
                    URL url = new URL(gUserInfoList.get(j).pic_url);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int code = connection.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        //服务器成功处理了部分 GET 请求。
                        inputStream = connection.getInputStream();
                        String accname = gUserInfoList.get(j).accname.length() < 16 ? gUserInfoList.get(j).accname : gUserInfoList.get(j).accname.substring(0, 15);
                        String strTemp = gUserInfoList.get(j).accnum + ","
                                + accname + ","
                                + gUserInfoList.get(j).flag;
                        //将数据转成bitmap
                        byte[] data = readStream(inputStream);
                        if (data != null) {
                            int iFileLen = data.length;
                            Log.d(TAG, "下载人脸照片文件大小:" + iFileLen);
                            if(getImageType(data) == 0){    //判断图片格式 png jpg bmp
                                //FileUtils.SaveFaceCodeFile(data, PhotoPath, "face_"+gUserInfoList.get(j).accnum + ".jpg");

                                if((iFileLen < (512 * 1024))&&(iFileLen > (20 * 1024))){
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    //人脸图片转换成人脸特征码
                                    if (bitmap2featureInit(bitmap, strTemp, gUserInfoList.get(j).accnum) == 0)
                                        Log.e(TAG, "下载特征码图片的账号成功:" + gUserInfoList.get(j).version + "-" + gUserInfoList.get(j).accnum);
                                    else
                                        Log.e(TAG, "下载特征码图片的账号失败:" + gUserInfoList.get(j).version + "-" + gUserInfoList.get(j).accnum);
                                    try {
                                        g_FaceCodeInfo.lngLocalVer = Long.parseLong(gUserInfoList.get(j).version);
                                        FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "初始版本号数据转换异常:" + e.getMessage());
                                    }
                                }else{
                                    Log.e(TAG, "下载的特征码照片太大无法转换:" + gUserInfoList.get(j).accnum);
                                    FileUtils.writeToFile(gUserInfoList.get(j).accnum + ",下载图片失败特征码照片太大无法转换" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                                }
                            }else {
                                Log.e(TAG, "下载的图片格式不正确");
                                FileUtils.writeToFile(strTemp + ",下载的图片格式不正确" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                            }
                            iRet = 0;
                            iErrCnt = 0;
                            iReDownFlag = 0;
                        } else {
                            Log.e(TAG, "返回码:" + code);
                            Log.e(TAG, "下载错误的账号:" + gUserInfoList.get(j).accnum);
                            Log.e(TAG, "下载错误的账号状态:" + gUserInfoList.get(j).flag);
                            Log.e(TAG, "下载错误的账号地址:" + gUserInfoList.get(j).pic_url);
                            Log.e(TAG, "下载错误的版本号:" + gUserInfoList.get(j).version);
                            FileUtils.writeToFile(strTemp + ",下载图片失败" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                            iRet = 1;
                        }
                    } else {
                        Log.e(TAG, "返回码:" + code);
                        Log.e(TAG, "下载错误的账号:" + gUserInfoList.get(j).accnum);
                        Log.e(TAG, "下载错误的账号状态:" + gUserInfoList.get(j).flag);
                        Log.e(TAG, "下载错误的账号地址:" + gUserInfoList.get(j).pic_url);
                        Log.e(TAG, "下载错误的版本号:" + gUserInfoList.get(j).version);
                        iRet = 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "下载出现异常：" + e.toString());
                    iRet = 2;
                } finally {
                    try {
                        connection.disconnect();
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (iRet != 0) {
                    Log.e(TAG, "处理异常:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                    if (iRet == 1) {
                        iErrCnt++;
                        if (iErrCnt > 10) {
                            Log.e(TAG, "处理异常跳过下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                            iRet = 0;
                            iErrCnt = 0;
                            iReDownFlag=0;
                            SystemClock.sleep(100);
                        } else {
                            Log.e(TAG, "处理异常重新下载:" + j + "acc-" + gUserInfoList.get(j).accnum+ "ver-" + gUserInfoList.get(j).version);
                            iReDownFlag=1;//重新下载
                        }
                        continue;
                    } else if (iRet == 2) {
                        iReDownFlag=1;//重新下载
                        iErrCnt++;
                        SystemClock.sleep(100);
                        if (iErrCnt > 10) {
                            Log.e(TAG, "处理异常,断开连接重新下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                            iRet = 0;
                            iErrCnt = 0;
                            iReDownFlag=0;
                            break;
                        }
                    }
                }
            }
            //更新版本号
            if (j < 1)
                return 1;
            try {
                g_FaceCodeInfo.lngLocalVer = Long.parseLong(gUserInfoList.get(j - 1).version);
                FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                Log.d(TAG, "更新的初始版本号:" + gUserInfoList.get(j - 1).version);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "初始版本号数据转换异常:" + e.getMessage());
            }
            return 0;
        }

        //人脸图片转换人脸特征码(初始)
        private int bitmap2featureInit(Bitmap mBitmap, String strTemp, String accnum) {
            if (mBitmap == null) {
                FileUtils.writeToFile(strTemp + ",下载图片失败" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                return 1;
            }
            THFI_FacePos[] facePos = new THFI_FacePos[1];
            facePos[0] = new THFI_FacePos();
            byte[] pixelsBGR = FaceFunction.getPixelsBGR(mBitmap);
            if (pixelsBGR == null) {
                FileUtils.writeToFile(strTemp + ",下载图片的像素太大" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                return 1;
            }
            int faceNum = FaceFunction.faceDetect(pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), 30, facePos);
            if (faceNum == 1) {
                Log.e(TAG, "人脸图片转换人脸特征码:"+strTemp);
                byte[] mFaceFeatures = FaceFunction.faceFeatures(pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), 30, facePos);
                if (mFaceFeatures == null) {
                    Log.e(TAG, "图片转换特征码失败");
                    FileUtils.writeToFile(strTemp+",图片转换特征码失败" + "\r\n",ZYTKFacePath, "featureCodeExt.txt");
                    return 1;
                }
                byte[] feature = FileUtils.SaveFaceCodeData(mFaceFeatures, g_FaceIdentInfo.FaceNameList.size());
                if (feature == null) {
                    Log.e(TAG, "图片转换特征码数据错误1");
                    FileUtils.writeToFile(strTemp+",图片转换特征码数据错误1" + "\r\n",ZYTKFacePath, "featureCodeExt.txt");
                    return 1;
                }
                byte[] sbTemp = strTemp.getBytes();
                FaceCodeInfoRW.WriteFaceCodeInfoData(sbTemp, g_FaceIdentInfo.FaceNameList.size());
                g_FaceIdentInfo.FaceNameList.add(strTemp);
                g_FaceIdentInfo.FaceAccList.add(accnum);

                if (feature != null) {
                    int iResult = FaceCheck.addFeature(sInitFaceCodeID, feature);
                    if (iResult != 0) {
                        Log.e("addFeature", "addFeature end iResult:" + iResult);
                    }
                }
                sInitFaceCodeID++;
            } else {
                if (faceNum > 1) {
                    Log.e(TAG, "下载的人脸图片多人脸");
                    FileUtils.writeToFile(strTemp + ",下载的人脸图片多人脸" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                } else {
                    Log.e(TAG, "下载的人脸图片无人脸");
                    FileUtils.writeToFile(strTemp + ",下载的人脸图片无人脸" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                }
                return 1;
            }
            return 0;
        }

        //获取并保存变更特征码数据
        public int SaveChangeFaceCodeData() {
            int j = 0;
            int iRet = 0;
            int iErrCnt = 0;
            int iReDownFlag=0;
            int iFaceCodePos = 0;

            for (j = 0; j < gUserInfoList.size(); j++) {
                iFaceCodePos = 0;
                if(iReDownFlag==1) //重新下载
                {
                    if(j > 0)
                        j = j - 1;
                    Log.e(TAG, "重新下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                }
                //判断状态 1新增 2 修改 3删除
                if (gUserInfoList.get(j).flag.equals("3")) {
                    Log.d(TAG, "删除的账号:" + gUserInfoList.get(j).accnum + ","
                            + gUserInfoList.get(j).accname + ","
                            + gUserInfoList.get(j).flag);

                    if (gUserInfoList.get(j).accnum.equals("")) {
                        Log.e(TAG, "准备下载的账号为空:" + gUserInfoList.get(j).accnum);
                        continue;
                    }

                    //更新accnum名单列表
                    iFaceCodePos = GetFaceNumListPos(gUserInfoList.get(j).accnum, g_FaceIdentInfo.FaceAccList);
                    //Log.d(TAG, "iFaceCodePos:" + iFaceCodePos);
                    if (iFaceCodePos >= g_FaceIdentInfo.FaceAccList.size())
                        continue;

                    String accname = gUserInfoList.get(j).accname.length() < 16 ? gUserInfoList.get(j).accname : gUserInfoList.get(j).accname.substring(0, 15);
                    String strTemp = gUserInfoList.get(j).accnum + ","
                            + accname + ","
                            + gUserInfoList.get(j).flag;
                    //Log.d(TAG,strTemp);
                    g_FaceIdentInfo.FaceNameList.set(iFaceCodePos, strTemp);
                    g_FaceIdentInfo.FaceAccList.set(iFaceCodePos, gUserInfoList.get(j).accnum);
                    FileUtils.DelFaceCodeData(iFaceCodePos);

                    byte[] sbTemp = strTemp.getBytes();
                    FaceCodeInfoRW.WriteFaceCodeInfoData(sbTemp, iFaceCodePos);

                    byte[] feature = new byte[FACEFEASIZE];
                    int iResult = FaceCheck.addFeature(iFaceCodePos, feature);
                    if (iResult != 0) {
                        Log.e("addFeature", "addFeature end iResult:" + iResult);
                    }
                    continue;
                }
                if (gUserInfoList.get(j).feature_url == null)
                    continue;

                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    iRet = 0;
                    Log.d(TAG, "准备下载的账号:"+gUserInfoList.get(j).accnum);
                    Log.d(TAG, "准备下载的版本号:"+gUserInfoList.get(j).version);
                    URL url = new URL(gUserInfoList.get(j).feature_url);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int code = connection.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        //服务器成功处理了部分 GET 请求。
                        inputStream = connection.getInputStream();
                        //判断是新增还是修改
                        String accname = gUserInfoList.get(j).accname.length() < 16 ? gUserInfoList.get(j).accname : gUserInfoList.get(j).accname.substring(0, 15);

                        String strTemp = gUserInfoList.get(j).accnum + ","
                                + accname + ","
                                + gUserInfoList.get(j).flag;
                        //Log.d(TAG, strTemp);

                        //更新accnum名单列表
                        iFaceCodePos = GetFaceNumListPos(gUserInfoList.get(j).accnum, g_FaceIdentInfo.FaceAccList);
                        byte[] feature = FileUtils.SaveFaceCodeData(inputStream, iFaceCodePos);
                        if (feature == null) {
                            Log.e(TAG, "特征码数据错误");
                            Log.d(TAG, "准备下载的账号:" + gUserInfoList.get(j).accnum);
                            continue;
                        }
                        if (iFaceCodePos >= g_FaceIdentInfo.FaceAccList.size()) {
                            g_FaceIdentInfo.FaceNameList.add(strTemp);
                            g_FaceIdentInfo.FaceAccList.add(gUserInfoList.get(j).accnum);
                        } else {
                            g_FaceIdentInfo.FaceNameList.set(iFaceCodePos, strTemp);
                            g_FaceIdentInfo.FaceAccList.set(iFaceCodePos, gUserInfoList.get(j).accnum);
                        }

                        byte[] sbTemp = strTemp.getBytes();
                        Log.d(TAG, "iFaceCodePos:" + iFaceCodePos);
                        FaceCodeInfoRW.WriteFaceCodeInfoData(sbTemp, iFaceCodePos);

                        if (feature != null) {
                            int iResult = FaceCheck.addFeature(iFaceCodePos, feature);
                            if (iResult != 0) {
                                Log.e("addFeature", "addFeature end iResult:" + iResult);
                            }
                        }
                        Log.e(TAG, "下载特征码数据的账号成功:" + gUserInfoList.get(j).version+"-"+ gUserInfoList.get(j).accnum);
                        try {
                            //更新版本号
                            g_FaceCodeInfo.lngLocalVer = Long.parseLong(gUserInfoList.get(j).version);
                            FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "更新的变更版本号转换异常:" + e.getMessage());
                        }
                        iRet = 0;
                        iErrCnt = 0;
                        iReDownFlag=0;
                    } else {
                        Log.e(TAG, "返回码:" + code);
                        Log.e(TAG, "下载错误的账号:" + gUserInfoList.get(j).accnum);
                        Log.e(TAG, "下载错误的账号状态:" + gUserInfoList.get(j).flag);
                        Log.e(TAG, "下载错误的账号地址:" + gUserInfoList.get(j).feature_url);
                        Log.e(TAG, "下载错误的版本号:" + gUserInfoList.get(j).version);
                        iRet = 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "下载出现异常：" + e.toString());
                    iRet = 2;
                } finally {
                    try {
                        connection.disconnect();
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (iRet != 0) {
                    Log.e(TAG, "处理异常:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                    if (iRet == 1) {
                        iErrCnt++;
                        if (iErrCnt > 10) {
                            Log.e(TAG, "处理异常跳过下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                            iRet = 0;
                            iErrCnt = 0;
                            iReDownFlag=0;
                            SystemClock.sleep(100);
                        } else {
                            Log.e(TAG, "处理异常重新下载:" + j + "acc-" + gUserInfoList.get(j).accnum+ "ver-" + gUserInfoList.get(j).version);
                            iReDownFlag=1;//重新下载
                        }
                        continue;
                    } else if (iRet == 2) {
                        iReDownFlag=1;//重新下载
                        iErrCnt++;
                        SystemClock.sleep(100);
                        if (iErrCnt > 10) {
                            Log.e(TAG, "处理异常,断开连接重新下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                            iRet = 0;
                            iErrCnt = 0;
                            iReDownFlag=0;
                            break;
                        }
                    }
                }
            }
            if (j < 1) {
                return 1;
            }
            try {
                //更新版本号
                g_FaceCodeInfo.lngLocalVer = Long.parseLong(gUserInfoList.get(j - 1).version);
                FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                Log.d(TAG, "更新的变更版本号:" + gUserInfoList.get(j - 1).version);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "更新的变更版本号转换异常:" + e.getMessage());
            }
            return 0;
        }

        //获取并保存变更特征码图片数据
        private int SaveChangeFacePicData() {
            int j = 0;
            int iRet = 0;
            int iErrCnt = 0;
            int iReDownFlag=0;
            int iFaceCodePos = 0;

            for (j = 0; j < gUserInfoList.size(); j++) {
                iFaceCodePos = 0;
                if(iReDownFlag==1) //重新下载
                {
                    if(j > 0)
                        j = j - 1;
                    Log.e(TAG, "重新下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                }

                //判断状态 1新增 2 修改 3删除
                if (gUserInfoList.get(j).flag.equals("3")) {
                    Log.d(TAG, "删除的账号:" + gUserInfoList.get(j).accnum + ","
                            + gUserInfoList.get(j).accname + ","
                            + gUserInfoList.get(j).flag);

                    if (gUserInfoList.get(j).accnum.equals("")) {
                        Log.e(TAG, "准备下载的账号为空:" + gUserInfoList.get(j).accnum);
                        continue;
                    }

                    //更新accnum名单列表
                    iFaceCodePos = GetFaceNumListPos(gUserInfoList.get(j).accnum, g_FaceIdentInfo.FaceAccList);
                    if (iFaceCodePos >= g_FaceIdentInfo.FaceAccList.size())
                        continue;

                    String accname = gUserInfoList.get(j).accname.length() < 16 ? gUserInfoList.get(j).accname : gUserInfoList.get(j).accname.substring(0, 15);
                    String strTemp = gUserInfoList.get(j).accnum + ","
                            + accname + ","
                            + gUserInfoList.get(j).flag;
                    //Log.d(TAG,strTemp);
                    g_FaceIdentInfo.FaceNameList.set(iFaceCodePos, strTemp);
                    g_FaceIdentInfo.FaceAccList.set(iFaceCodePos, gUserInfoList.get(j).accnum);
                    FileUtils.DelFaceCodeData(iFaceCodePos);

                    byte[] sbTemp = strTemp.getBytes();
                    FaceCodeInfoRW.WriteFaceCodeInfoData(sbTemp, iFaceCodePos);

                    byte[] feature = new byte[FACEFEASIZE];
                    int iResult = FaceCheck.addFeature(iFaceCodePos, feature);
                    if (iResult != 0) {
                        Log.e("addFeature", "addFeature end iResult:" + iResult);
                    }
                    continue;
                }
                if (gUserInfoList.get(j).pic_url == null)
                    continue;

                HttpURLConnection connection = null;
                InputStream inputStream = null;
                try {
                    iRet = 0;
                    Log.d(TAG, "准备下载的账号:"+gUserInfoList.get(j).accnum);
                    Log.d(TAG, "准备下载的版本号:"+gUserInfoList.get(j).version);
                    URL url = new URL(gUserInfoList.get(j).pic_url);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int code = connection.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        //服务器成功处理了部分 GET 请求。
                        inputStream = connection.getInputStream();
                        //判断是新增还是修改
                        String accname = gUserInfoList.get(j).accname.length() < 16 ? gUserInfoList.get(j).accname : gUserInfoList.get(j).accname.substring(0, 15);
                        String strTemp = gUserInfoList.get(j).accnum + ","
                                + accname + ","
                                + gUserInfoList.get(j).flag;
                        //Log.d(TAG, strTemp);
                        byte[] data = readStream(inputStream);
                        if (data != null) {
                            int iFileLen = data.length;
                            Log.d(TAG, "下载人脸照片文件大小:" + iFileLen);
                            if(getImageType(data) == 0){    //判断图片格式 png jpg bmp
                                //FileUtils.SaveFaceCodeFile(data, PhotoPath, "face_"+gUserInfoList.get(j).accnum + ".jpg");
                                if((iFileLen < (512 * 1024))&&(iFileLen > (20 * 1024))){
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    if (bitmap2featureChange(bitmap, strTemp, gUserInfoList.get(j).accnum) == 0)
                                        Log.e(TAG, "下载特征码图片的账号成功:" + gUserInfoList.get(j).version + "-" + gUserInfoList.get(j).accnum);
                                    else
                                        Log.e(TAG, "下载特征码图片的账号失败:" + gUserInfoList.get(j).version + "-" + gUserInfoList.get(j).accnum);
                                    try {
                                        //更新版本号
                                        g_FaceCodeInfo.lngLocalVer = Long.parseLong(gUserInfoList.get(j).version);
                                        FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "更新的变更版本号转换异常:" + e.getMessage());
                                    }
                                }else{
                                    Log.e(TAG, "下载的特征码照片太大无法转换:" + gUserInfoList.get(j).accnum);
                                    FileUtils.writeToFile(gUserInfoList.get(j).accnum + ",下载图片失败特征码照片太大无法转换" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                                }
                            }else {
                                Log.e(TAG, "下载的图片格式不正确");
                                FileUtils.writeToFile(strTemp + ",下载的图片格式不正确" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                            }
                            iRet = 0;
                            iErrCnt = 0;
                            iReDownFlag = 0;
                        } else {
                            Log.e(TAG, "返回码:" + code);
                            Log.e(TAG, "下载错误的账号:" + gUserInfoList.get(j).accnum);
                            Log.e(TAG, "下载错误的账号状态:" + gUserInfoList.get(j).flag);
                            Log.e(TAG, "下载错误的账号地址:" + gUserInfoList.get(j).pic_url);
                            Log.e(TAG, "下载错误的版本号:" + gUserInfoList.get(j).version);
                            FileUtils.writeToFile(strTemp + ",下载图片失败" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                            iRet = 1;
                        }

                    } else {
                        Log.e(TAG, "返回码:" + code);
                        Log.e(TAG, "下载错误的账号:" + gUserInfoList.get(j).accnum);
                        Log.e(TAG, "下载错误的账号状态:" + gUserInfoList.get(j).flag);
                        Log.e(TAG, "下载错误的账号地址:" + gUserInfoList.get(j).pic_url);
                        Log.e(TAG, "下载错误的版本号:" + gUserInfoList.get(j).version);
                        iRet = 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "下载出现异常：" + e.toString());
                    iRet = 2;
                } finally {
                    try {
                        connection.disconnect();
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (iRet != 0) {
                    Log.e(TAG, "处理异常:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                    if (iRet == 1) {
                        iErrCnt++;
                        if (iErrCnt > 10) {
                            Log.e(TAG, "处理异常跳过下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                            iRet = 0;
                            iErrCnt = 0;
                            iReDownFlag=0;
                            SystemClock.sleep(100);
                        } else {
                            Log.e(TAG, "处理异常重新下载:" + j + "acc-" + gUserInfoList.get(j).accnum+ "ver-" + gUserInfoList.get(j).version);
                            iReDownFlag=1;//重新下载
                        }
                        continue;
                    } else if (iRet == 2) {
                        iReDownFlag=1;//重新下载
                        iErrCnt++;
                        SystemClock.sleep(100);
                        if (iErrCnt > 10) {
                            Log.e(TAG, "处理异常,断开连接重新下载:" + j + " acc-" + gUserInfoList.get(j).accnum+ " ver-" + gUserInfoList.get(j).version);
                            iRet = 0;
                            iErrCnt = 0;
                            iReDownFlag=0;
                            break;
                        }
                    }
                }
            }
            if (j < 1) {
                return 1;
            }
            try {
                //更新版本号
                g_FaceCodeInfo.lngLocalVer = Long.parseLong(gUserInfoList.get(j - 1).version);
                FaceCodeInfoRW.WriteAllFaceCodeInfo(g_FaceCodeInfo);
                Log.d(TAG, "更新的变更版本号:" + gUserInfoList.get(j - 1).version);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "更新的变更版本号转换异常:" + e.getMessage());
            }
            return 0;
        }

        //人脸图片转换人脸特征码(变更)
        private int bitmap2featureChange(Bitmap mBitmap, String strTemp, String accnum) {
            if (mBitmap == null) {
                FileUtils.writeToFile(strTemp + ",下载图片失败" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                return 1;
            }
            int iFaceCodePos = 0;
            THFI_FacePos[] facePos = new THFI_FacePos[1];
            facePos[0] = new THFI_FacePos();
            byte[] pixelsBGR = FaceFunction.getPixelsBGR(mBitmap);
            if (pixelsBGR == null) {
                FileUtils.writeToFile(strTemp + ",下载图片的像素太大" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                return 1;
            }
            int faceNum = FaceFunction.faceDetect(pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), 30, facePos);
            if (faceNum == 1) {
                Log.e(TAG, "变更人脸图片转换人脸特征码:"+strTemp);
                byte[] mFaceFeatures = FaceFunction.faceFeatures(pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), 30, facePos);
                if (mFaceFeatures == null) {
                    Log.e(TAG, "图片转换特征码失败");
                    FileUtils.writeToFile(strTemp+",图片转换特征码数据失败" + "\r\n",ZYTKFacePath, "featureCodeExt.txt");
                    return 1;
                }
                iFaceCodePos = GetFaceNumListPos(accnum, g_FaceIdentInfo.FaceAccList);
                byte[] feature = FileUtils.SaveFaceCodeData(mFaceFeatures, iFaceCodePos);
                if (feature == null) {
                    Log.e(TAG, "图片转换特征码数据错误1");
                    FileUtils.writeToFile(strTemp+",图片转换特征码数据错误1" + "\r\n",ZYTKFacePath, "featureCodeExt.txt");
                    return 1;
                }
                if (iFaceCodePos >= g_FaceIdentInfo.FaceAccList.size()) {  //新增
                    g_FaceIdentInfo.FaceNameList.add(strTemp);
                    g_FaceIdentInfo.FaceAccList.add(accnum);
                } else {  //修改
                    g_FaceIdentInfo.FaceNameList.set(iFaceCodePos, strTemp);
                    g_FaceIdentInfo.FaceAccList.set(iFaceCodePos, accnum);
                }

                byte[] sbTemp = strTemp.getBytes();
                FaceCodeInfoRW.WriteFaceCodeInfoData(sbTemp, iFaceCodePos);

                if (feature != null) {
                    //Log.e(TAG, "变更人脸图片加载特征码到内存");
                    int iResult = FaceCheck.addFeature(iFaceCodePos, feature);
                    if (iResult != 0) {
                        Log.e("addFeature", "addFeature end iResult:" + iResult);
                    }
                }
            } else {
                if (faceNum > 1) {
                    Log.e(TAG, "下载的人脸图片多人脸");
                    FileUtils.writeToFile(strTemp + ",下载的人脸图片多人脸" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                } else {
                    Log.e(TAG, "下载的人脸图片无人脸");
                    FileUtils.writeToFile(strTemp + ",下载的人脸图片无人脸" + "\r\n", ZYTKFacePath, "featureCodeExt.txt");
                }
                return 1;
            }
            return 0;
        }

        @Override
        public void run() {
            isStart = true;

            while (isStart) {
                try {
                    Thread.sleep(100);//延时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (g_HttpCommInfo.cGetFaceCodeState == 0)
                    continue;

                long lngStart = System.currentTimeMillis();
                if (g_HttpCommInfo.iFaceServeType == 0) {  //新版本图片特征码服务
                    if (g_HttpCommInfo.cDownMode == 0)//判断是初始还是变更
                    {
                        SaveInitFacePicData();
                        Log.e(TAG, "=========下载初始图片特征码耗时:" + (System.currentTimeMillis() - lngStart));
                    } else {
                        SaveChangeFacePicData();
                        Log.e(TAG, "=========下载变更图片特征码耗时:" + (System.currentTimeMillis() - lngStart));
                    }
                } else {     //特征服务
                    if (g_HttpCommInfo.cDownMode == 0)//判断是初始还是变更
                    {
                        SaveInitFaceCodeData();
                        Log.e(TAG, "=========下载初始特征码耗时:" + (System.currentTimeMillis() - lngStart));
                    } else {
                        SaveChangeFaceCodeData();
                        Log.e(TAG, "=========下载变更特征码耗时:" + (System.currentTimeMillis() - lngStart));
                    }
                }
                //更新名单列表
                //FileUtils.WriteListToFile((ArrayList) g_FaceIdentInfo.FaceNameList, ZYTKFacePath+"facename.ini");
                g_FaceIdentInfo.iListNum = g_FaceIdentInfo.FaceNameList.size();
                Log.d(TAG, "人脸特征码名单数量:" + g_FaceIdentInfo.iListNum);
                //更新特征码数据
                THFI_Param.EnrolledNum = g_FaceIdentInfo.iListNum;
                THFI_Param.FaceName = g_FaceIdentInfo.FaceNameList;
                g_HttpCommInfo.cGetFaceCodeState = 0;//下载完成
            }
        }
    }

    //获取用户照片数据线程
    class GetUserPhotoThread extends Thread {

        private String TAG = getClass().getSimpleName();
        private volatile boolean isStart = false;
        private PhotoInfo sPhotoInfo;

        public GetUserPhotoThread(PhotoInfo photoInfo) {
            //Log.d(TAG, "GetUserPhotoThread 构造");
            sPhotoInfo = new PhotoInfo();
            sPhotoInfo = photoInfo;
        }

        @Override
        public void run() {

            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                Log.d(TAG, "准备下载照片的账号:"+sPhotoInfo.accnum);
                Log.d(TAG, "准备下载照片的账号地址:"+sPhotoInfo.photo_url);
                URL url = new URL(sPhotoInfo.photo_url);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(60 * 1000);
                connection.setReadTimeout(60 * 1000);
                connection.setRequestMethod("GET");
                connection.connect();
                int code = connection.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    //服务器成功处理了部分 GET 请求。
                    //int iFileLen = connection.getContentLength();
                    //Log.d(TAG, "用户照片文件大小:" + iFileLen);
                    //if((iFileLen < (512 * 1024))&&(iFileLen > (20 * 1024))){

                    inputStream = connection.getInputStream();
                    int iRet = FileUtils.SavePhotoData(inputStream, PhotoPath, sPhotoInfo.accnum + ".jpg");
                    if(iRet != 0){
                        String strAccID = GetMultiAccID(sPhotoInfo.accnum);//判断是否是多人脸
                        Message msg = Message.obtain();
                        msg.what = HTTPPHOTO;
                        msg.obj = strAccID;
                        g_UICardHandler.sendMessage(msg);
                    }else{
                        Log.e(TAG, "下载照片太大或者太小");
                    }
                } else {
                    Log.e(TAG, "下载照片失败:"+code);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.disconnect();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //获取程序APP线程
    class GetAppDataThread extends Thread {

        private String TAG = getClass().getSimpleName();
        private volatile boolean isStart = false;
        private String sSoftware_url;

        public GetAppDataThread(String software_url) {
            Log.d(TAG, "GetAppDataThread 构造");
            sSoftware_url = software_url;
        }

        @Override
        public void run() {

            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                Log.d(TAG, "准备下载的地址:" + sSoftware_url);
                URL url = new URL(sSoftware_url);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(60 * 1000);
                connection.setReadTimeout(60 * 1000);
                connection.setRequestMethod("GET");
                connection.connect();
                int code = connection.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    //服务器成功处理了部分 GET 请求。
                    int iAPKFileLen = connection.getContentLength();
                    Log.d(TAG, "文件大小:" + iAPKFileLen);
                    if (iAPKFileLen == -1){
                        Log.e(TAG, "下载程序出现异常，文件长度错误");
                        Message msg = Message.obtain();
                        msg.what = APPUPABN;
                        if (g_UIMainHandler != null)
                            g_UIMainHandler.sendMessage(msg);

                        return;
                    }
                    //根据大小判断升级包类型(>200m的包认为是系统升级包update.zip)
                    String strAPPFileName = "";
                    inputStream = connection.getInputStream();
                    byte[] buffer = new byte[1024 * 1024];
                    int length = 0;
                    int lengthTmp = 0;
                    int iTemp = 0;
                    if (iAPKFileLen > 100 * 1024 * 1024) {
                        Log.d(TAG, "文件大小超出200m，为系统升级包");
                        strAPPFileName = "/update_temp.zip";//文件名命名为temp，避免升级过程中出现提示升级系统的界面
                        Log.d(TAG, strAPPFileName);
                        File file = new File(SDPath, strAPPFileName);
                        OutputStream out = new FileOutputStream(file);

                        while ((length = inputStream.read(buffer)) != -1) {
                            out.write(buffer, 0, length);
                            lengthTmp = lengthTmp + length;
                            int iPercent = Publicfun.CalcRatio(lengthTmp, (int) iAPKFileLen);
                            if ((iPercent > 1) && (iTemp != iPercent)) {
                                iTemp = iPercent;
                                if ((iPercent % 1) == 0) {
                                    Log.d(TAG, "系统文件下载进度:"+iPercent);
                                    Message msg = Message.obtain();
                                    msg.what = ROMUPING;
                                    msg.obj = (int) iPercent;
                                    if (g_UIMainHandler != null)
                                        g_UIMainHandler.sendMessage(msg);
                                    if (g_UICardHandler != null)
                                        g_UICardHandler.sendMessage(msg);
                                }
                            }
                            try {
                                Thread.sleep(1);//延时
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        //判断程序是否正确
                        long lngFileLen = getFileSize(SDPath + strAPPFileName);
                        if (iAPKFileLen != lngFileLen) {
                            Log.e(TAG, "下载ota出现异常，文件长度错误");
                            Message msg = Message.obtain();
                            msg.what = ROMUPABN;
                            if (g_UIMainHandler != null)
                                g_UIMainHandler.sendMessage(msg);

                            return;
                        }
                        Log.d(TAG, "系统ota下载完成,重命名");
                        RunShellCmd("rename "+SDPath + "/update_temp.zip"+" "+SDPath + "/update.zip");
                        WriteSofeVerInfoFile(g_WorkInfo.strAppSoftWareVer);//写程序版本号
                        try {
                            Thread.sleep(2000);//延时1s
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Message msg = Message.obtain();
                        msg.what = ROMUPOVER;
                        if (g_UIMainHandler != null){
                            Log.d(TAG, "系统ota下载完成,发送主界面处理");
                            g_UIMainHandler.sendMessage(msg);
                        }
                        else{
                            //判断update.zip的包是否存在
                            g_WorkInfo.cUpdateState = 0;
                            File SystemUpdateOTA = new File(SDPath+"/update.zip");
                            if (SystemUpdateOTA.exists()) {
                                Log.d(TAG, "准备升级系统安装包,请勿断电");
                                try {
                                    Thread.sleep(2000);//延时1s
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Log.e(TAG, "系统重启");
                                RunShellCmd("reboot");
                            }
                        }
                    }
                    else
                    {
                        Log.d(TAG, "文件为应用程序apk");
                        //app名称  zytk35_mpos_版本号.apk zytk35_mpos_3.5.19.0508.apk
                        strAPPFileName = "zytk35_mpos_temp.apk";
                        Log.d(TAG, strAPPFileName);
                        File file = new File(ZYTK35Path, strAPPFileName);
                        OutputStream out = new FileOutputStream(file);

                        while ((length = inputStream.read(buffer)) != -1) {
                            out.write(buffer, 0, length);
                            lengthTmp = lengthTmp + length;
                            int iPercent = Publicfun.CalcRatio(lengthTmp, (int) iAPKFileLen);
                            if ((iPercent > 1) && (iTemp != iPercent)) {
                                iTemp = iPercent;
                                if ((iPercent % 1) == 0) {
                                    Message msg = Message.obtain();
                                    msg.what = APPUPING;
                                    msg.obj = (int) iPercent;
                                    if (g_UIMainHandler != null)
                                        g_UIMainHandler.sendMessage(msg);
                                    if (g_UICardHandler != null)
                                        g_UICardHandler.sendMessage(msg);
                                }
                            }
                        }
                        //判断程序是否正确
                        long lngFileLen = getFileSize(ZYTK35Path + strAPPFileName);
                        if (iAPKFileLen != lngFileLen) {
                            Log.e(TAG, "下载app出现异常，文件长度错误");
                            Message msg = Message.obtain();
                            msg.what = APPUPABN;
                            if (g_UIMainHandler != null)
                                g_UIMainHandler.sendMessage(msg);

                            return;
                        }
						Log.d(TAG, "程序下载完成,重命名");
                        String strReAPPFileName = "zytk35_mpos_" + g_WorkInfo.strAppSoftWareVer + ".apk";
                        renameFile(ZYTK35Path + strAPPFileName, ZYTK35Path + strReAPPFileName);
                        Message msg = Message.obtain();
                        msg.what = APPUPOVER;
                        if (g_UIMainHandler != null)
                            g_UIMainHandler.sendMessage(msg);
                        else
                            g_WorkInfo.cUpdateState = 0;
                    }
                } else {
                    Log.e(TAG, "下载apk错误返回码:" + code);
                    Message msg = Message.obtain();
                    msg.what = APPUPABN;
                    if (g_UIMainHandler != null)
                        g_UIMainHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "下载app出现异常：" + e.toString());
                Message msg = Message.obtain();
                msg.what = APPUPABN;
                if (g_UIMainHandler != null)
                    g_UIMainHandler.sendMessage(msg);
            } finally {
                try {
                    connection.disconnect();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "下载app出现异常2：" + e.toString());
                    Message msg = Message.obtain();
                    msg.what = APPUPABN;
                    if (g_UIMainHandler != null)
                        g_UIMainHandler.sendMessage(msg);
                }
            }
        }
    }

    //发送http报文处理
    public void SendHttpMsgHand() {
        //运行的状态(0:不运行,1:联机,2:脱机,3:联机(营业时段外),4:重新联机5:开机自动联机6:连接签到服务)
        if (g_WorkInfo.cRunState == 1) {
            if ((getUnsignedIntt((int) g_HttpCommInfo.lngSendHttpStatus) > 0)
                    && (g_HttpCommInfo.cRecvWaitState == 0)) {
                //获取用户照片地址(实时获取，卡户校验时)
                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000008) == 0x00000008) {
                    //置位接收标记
                    g_HttpCommInfo.cRecvWaitState = 1;
                    Log.d(TAG,"获取账户证件照:"+g_WorkInfo.strAccCode);
                    GetUserPhotoInfo(g_WorkInfo.strAccCode);
                }

                //获取初始人脸特征码数据表
                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000002) == 0x00000002) {
                    if (g_HttpCommInfo.cGetFaceCodeState == 1)
                        return;

                    //置位接收标记
                    g_HttpCommInfo.cRecvWaitState = 1;
                    //获取人脸特征码数据列表
                    GetFaceInitCodeInfo(String.valueOf(g_FaceCodeInfo.lngLocalVer), strDevicesn, strEpid);
                }

                //获取初始图片数据列表
                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000100) == 0x00000100) {
                    if (g_HttpCommInfo.cGetFaceCodeState == 1)
                        return;

                    //置位接收标记
                    g_HttpCommInfo.cRecvWaitState = 1;
                    //获取人脸特征码数据列表
                    GetFaceInitPicInfo(String.valueOf(g_FaceCodeInfo.lngLocalVer), strDevicesn, strEpid);
                }
                //获取变更人脸特征码数据表
                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000004) == 0x00000004) {
                    if (g_HttpCommInfo.cGetFaceCodeState == 1)
                        return;

                    //置位接收标记
                    g_HttpCommInfo.cRecvWaitState = 1;
                    //获取变更人脸特征码数据列表
                    GetFaceChangeCodeInfo(String.valueOf(g_FaceCodeInfo.lngLocalVer), strDevicesn, strEpid);
                }

                //获取变更人脸图片数据表
                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000200) == 0x00000200) {
                    if (g_HttpCommInfo.cGetFaceCodeState == 1)
                        return;

                    //置位接收标记
                    g_HttpCommInfo.cRecvWaitState = 1;
                    //获取变更人脸特征码数据列表

                    GetFaceChangePicInfo(String.valueOf(g_FaceCodeInfo.lngLocalVer), strDevicesn, strEpid);
                }
                //获取版本号优先级最低
                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000001) == 0x00000001) {
                    if (g_HttpCommInfo.cGetFaceCodeState == 1)
                        return;

                    //置位接收标记
                    g_HttpCommInfo.cRecvWaitState = 1;
                    //获取人脸特征版本号
                    GetFaceCodeVer(strDevicesn, strEpid);
                }

                //获取应用程序app地址
                if (((g_HttpCommInfo.lngSendHttpStatus & 0x00000010) == 0x00000010) && (g_WorkInfo.cUpdateState == 0)) {
                    //置位接收标记
                    g_HttpCommInfo.cRecvWaitState = 1;
                    typenum = "" + LAN_DEVTYPE;     //typenum 设备类型
                    modelnum = "0";
                    GetUpdateAppInfo(typenum, modelnum);
                }
                //上传人脸流水
                if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000020) == 0x00000020) {
                    if (g_HttpCommInfo.cGetFaceCodeState == 1)
                        return;

                    //置位接收标记
                    g_HttpCommInfo.cRecvWaitState = 1;

                    modelnum = "" + 0;  //modelnum 设备型号
                    Log.d(TAG, "设备型号:" + modelnum);
                    int iRet=UploadFaceRecord(strDevicesn, modelnum);
                    if(iRet!=0)
                    {
                        Log.d(TAG,"上传人脸流水失败:"+g_WasteFaceBookInfo.TransferIndex);
                        if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000020) == 0x00000020) {
                            g_HttpCommInfo.lngSendHttpStatus = g_HttpCommInfo.lngSendHttpStatus - 0x00000020;
                        }
                        g_WasteFaceBookInfo.TransferIndex = g_WorkInfo.lngEndFRecordID;
                        WasteFacePayBooksRW.WriteWasteFacePayBookInfo(g_WasteFaceBookInfo);
                        g_HttpCommInfo.cRecvWaitState = 0;
                    }
                }
            }
        } else {
            if (g_WorkInfo.cUpdateState == 1) {
                if ((getUnsignedIntt((int) g_HttpCommInfo.lngSendHttpStatus) > 0)
                        && (g_HttpCommInfo.cRecvWaitState == 0)) {
                    //获取应用程序app地址
                    if ((g_HttpCommInfo.lngSendHttpStatus & 0x00000010) == 0x00000010) {
                        //置位接收标记
                        g_HttpCommInfo.cRecvWaitState = 1;

//                        long lngTemp=(g_WorkInfo.cDeviceType[0]&0xff);
//                        lngTemp=lngTemp+(g_WorkInfo.cDeviceType[1]&0xff)*256;
//                        //g_TerminalInfo.wDeviceType
//                        typenum=""+lngTemp;     //typenum 设备类型
//                        Log.d(TAG,"设备类型:"+typenum);
//
//                        lngTemp=(g_WorkInfo.cDeviceMode[0]&0xff);
//                        lngTemp=lngTemp+(g_WorkInfo.cDeviceMode[1]&0xff)*256;
//                        //g_TerminalInfo.wDeviceModel
//                        modelnum=""+lngTemp;  //modelnum 设备型号
//                        Log.d(TAG,"设备型号:"+modelnum);

                        typenum = "" + LAN_DEVTYPE;     //typenum 设备类型
                        modelnum = "0";
                        GetUpdateAppInfo(typenum, modelnum);
                    }
                }
            }
        }
    }

    //发送版本号报文
    public void SendHttpVerMSG() {
        if (g_BasicInfo.cSystemState != 100)
            return;

        slngTimer++;
        //系统运行中
        if ((g_WorkInfo.cRunState == 1)
                && (g_SystemInfo.cFaceDetectFlag == 1)
                //&& (g_HttpCommInfo.lngSendHttpStatus == 0)
                && (g_HttpCommInfo.cRecvWaitState == 0)) {
            if (slngTimer % 30 == 0) {
                Log.d(TAG, "获取特征码参数版本号");
                g_HttpCommInfo.lngSendHttpStatus |= 0x00000001;
            }
        } else {
            if (slngTimer % 30 == 0) {
                Log.d(TAG, String.format("特征码无法获取:%d-%d-%d-%d", g_WorkInfo.cRunState,
                        g_SystemInfo.cFaceDetectFlag,
                        g_HttpCommInfo.lngSendHttpStatus,
                        g_HttpCommInfo.cRecvWaitState));
                slngErrCnt++;
                if(slngErrCnt>10){
                    Log.d(TAG, "将人脸服务发送标记清除");
                    g_HttpCommInfo.lngSendHttpStatus=0;
                    g_HttpCommInfo.cRecvWaitState=0;
                }
            }
        }
        if ((g_WorkInfo.cRunState == 1)
                && (g_HttpCommInfo.lngSendHttpStatus == 0)
                && (g_HttpCommInfo.cRecvWaitState == 0)
                && (g_WorkInfo.cUpdateState == 0)) {
            if (slngTimer % 43 == 0) {
                Log.d(TAG, "获取应用程序app");
                g_HttpCommInfo.lngSendHttpStatus |= 0x00000010;
            }
        }
    }

    //获取人脸特征码版本号
    public static void GetFaceCodeVer(String devicesn, String epid) {
        Map<String, String> map = new HashMap<>();
        map.put("device_sn", devicesn);
        map.put("epid", epid);
        String jsonData = DataAnalyze.PostRequest(map,null, GETVERSION_CODE);
        if (jsonData == null) {
            Log.e(TAG, "无法连接服务器，请重新检查网络");
            gHttpRecvHandler.sendEmptyMessage(HEVT_NULL);
        } else {
            Message message = Message.obtain();
            message.obj = jsonData;
            message.what = HEVT_GETFACEVER;
            if(gHttpRecvHandler!=null)
                gHttpRecvHandler.sendMessage(message);
        }
    }

    //获取初始人脸特征码数据列表
    public static void GetFaceInitCodeInfo(String strVersion, String devicesn, String epid) {
        Map<String, String> map = new HashMap<>();
        map.put("devicesn", devicesn);
        map.put("epid", epid);
        map.put("version", strVersion);
        String jsonData = DataAnalyze.PostRequest(map,null, GETINITDATA_CODE);
        if (jsonData == null) {
            Log.e(TAG, "无法连接服务器，请重新检查网络");
            if(gHttpRecvHandler!=null)
                gHttpRecvHandler.sendEmptyMessage(HEVT_NULL);
        } else {
            Message message = Message.obtain();
            message.obj = jsonData;
            message.what = HEVT_GETFACEINITDATA;
            if(gHttpRecvHandler!=null)
                gHttpRecvHandler.sendMessage(message);
        }
    }

    //获取初始人脸图片数据列表
    public static void GetFaceInitPicInfo(String strVersion, String devicesn, String epid) {
        Map<String, String> map = new HashMap<>();
        map.put("devicesn", devicesn);
        map.put("epid", epid);
        map.put("version", strVersion);
        String jsonData = DataAnalyze.PostRequest(map, null, GETINITPIC_CODE);
        //Log.e("获取初始化人脸图片数据列表", jsonData);
        if (jsonData == null) {
            Log.e(TAG, "无法连接服务器，请重新检查网络");
            if (gHttpRecvHandler != null)
                gHttpRecvHandler.sendEmptyMessage(HEVT_NULL);
        } else {
            Message message = Message.obtain();
            message.obj = jsonData;
            message.what = HEVT_GETFACEINITPIC;
            if (gHttpRecvHandler != null)
                gHttpRecvHandler.sendMessage(message);
        }
    }

    //获取变更人脸特征码数据列表
    public static void GetFaceChangeCodeInfo(String strVersion, String devicesn, String epid) {
        Map<String, String> map = new HashMap<>();
        map.put("devicesn", devicesn);
        map.put("epid", epid);
        map.put("version", strVersion);
        String jsonData = DataAnalyze.PostRequest(map,null, GETCHANGEDATA_CODE);
        if (jsonData == null) {
            Log.e(TAG, "无法连接服务器，请重新检查网络");
            if(gHttpRecvHandler!=null)
                gHttpRecvHandler.sendEmptyMessage(HEVT_NULL);
        } else {
            Message message = Message.obtain();
            message.obj = jsonData;
            message.what = HEVT_GETFACECHANDATA;
            if(gHttpRecvHandler!=null)
                gHttpRecvHandler.sendMessage(message);
        }
    }

    //获取变更人脸图片数据列表
    public static void GetFaceChangePicInfo(String strVersion, String devicesn, String epid) {
        Map<String, String> map = new HashMap<>();
        map.put("devicesn", devicesn);
        map.put("epid", epid);
        map.put("version", strVersion);
        String jsonData = DataAnalyze.PostRequest(map, null, GETCHANGEPIC_CODE);
        if (jsonData == null) {
            Log.e(TAG, "无法连接服务器，请重新检查网络");
            if (gHttpRecvHandler != null)
                gHttpRecvHandler.sendEmptyMessage(HEVT_NULL);
        } else {
            Message message = Message.obtain();
            message.obj = jsonData;
            message.what = HEVT_GETFACECHANPIC;
            if (gHttpRecvHandler != null)
                gHttpRecvHandler.sendMessage(message);
        }
    }
    //获取用户照片数据地址 accnum 一卡通账号
    public static void GetUserPhotoInfo(String accnum) {
        Map<String, String> map = new HashMap<>();
        map.put("accnum", accnum);
        String jsonData = DataAnalyze.PostRequest(map,null, GETPHOTO_CODE);
        if (jsonData == null) {
            Log.e(TAG, "无法连接服务器，请重新检查网络");
            if(gHttpRecvHandler!=null)
                gHttpRecvHandler.sendEmptyMessage(HEVT_NULL);
        } else {
            Message message = Message.obtain();
            message.obj = jsonData;
            message.what = HEVT_GETPHOTOINFO;
            if(gHttpRecvHandler!=null)
                gHttpRecvHandler.sendMessage(message);
        }
    }

    //获取程序升级app地址 typenum 设备类型 modelnum 设备型号
    public static void GetUpdateAppInfo(String typenum, String modelnum) {
        Map<String, String> map = new HashMap<>();
        map.put("typenum", typenum);
        map.put("modelnum", modelnum);
        String jsonData = DataAnalyze.PostRequest(map,null, UPDATEAPP_CODE);
        if (jsonData == null) {
            Log.e(TAG, "无法连接服务器，请重新检查网络");
            if (gHttpRecvHandler != null) {
                gHttpRecvHandler.sendEmptyMessage(HEVT_NULL);
            }
        } else {
            Message message = Message.obtain();
            message.obj = jsonData;
            message.what = HEVT_GETAPPINFO;
            if (gHttpRecvHandler != null) {
                gHttpRecvHandler.sendMessage(message);
            }
        }
    }

    //设备上传人脸照片消费记录接口
    public static int UploadFaceRecord(String devicesn, String modelnum) {
        int cResult;
        long lngTempIndex;
        Map<String, String> map = new HashMap<>();
//        devicesns	String	是	10	设备序列号
//        devicemodel	String	是	10	设备类型
//        deviceid	String	是	32	站点号
//        accnum	String	是	32	账号
//        personcode	String	是	32	学号/个人编号(改成人脸相识度)
//        totalamount	String	是   消费金额
//        ewalletid	String	是	3	钱包号
//        transTime	String	是    交易时间
//        devicesid	String	是	32	终端流水号

        //报文内容
        lngTempIndex=g_WorkInfo.lngStartFRecordID + 1;
        Log.d(TAG, String.format("上传人脸流水指针lngTempIndex:%d", lngTempIndex));

        WasteFacePayBooks StructData=new WasteFacePayBooks();
        StructData = WasteFacePayBooksRW.ReadPayRecordsInfo(lngTempIndex);
        if (StructData == null) {
            Log.d(TAG, ("读取人脸流水记录失败"));
            return 1;
        }

        int iTerminalID=(StructData.cStationID[0]& 0xff)
                +(StructData.cStationID[1]& 0xff)*256;

        long lngAccountID=(StructData.cAccountID[0]& 0xff)
                +(StructData.cAccountID[1]& 0xff)*256+
                +(StructData.cAccountID[2]& 0xff)*256*256+
                +(StructData.cAccountID[3]& 0xff)*256*256*256;

        long lngPayMoney=(StructData.cPaymentMoney[0]& 0xff)
                +(StructData.cPaymentMoney[1]& 0xff)*256+
                +(StructData.cPaymentMoney[2]& 0xff)*256*256;

        long lngPayRecordID=(StructData.cPayRecordID[0]& 0xff)
                +(StructData.cPayRecordID[1]& 0xff)*256+
                +(StructData.cPayRecordID[2]& 0xff)*256*256+
                +(StructData.cPayRecordID[3]& 0xff)*256*256*256;

        String strMatchScore = ""+StructData.fMatchScore;//人脸相识度

        String strPayDatetime=String.format("20%02d-%02d-%02d %02d:%02d:%02d",
                                StructData.cPaymentTime[0],StructData.cPaymentTime[1],StructData.cPaymentTime[2],
                                StructData.cPaymentTime[3],StructData.cPaymentTime[4],StructData.cPaymentTime[5]);

        Log.d(TAG,strPayDatetime);

        map.put("devicesns", devicesn);      //设备序列号
        map.put("devicemodel", modelnum);    //设备类型
        map.put("deviceid", ""+iTerminalID);       //站点号
        map.put("accnum", ""+lngAccountID);         //账号
        map.put("personcode", strMatchScore);     //学号/个人编号(改成人脸相识度)
        map.put("totalamount", ""+lngPayMoney);    //消费金额
        map.put("ewalletid", ""+g_StationInfo.cWorkBurseID);      //钱包号
        map.put("transTime", strPayDatetime);// 交易时间
        map.put("devicesid", ""+lngPayRecordID);      //终端流水号


        String strDateTime = String.format("%02d%02d%02d%02d%02d%02d",
                StructData.cPaymentTime[0], StructData.cPaymentTime[1],
                StructData.cPaymentTime[2], StructData.cPaymentTime[3],
                StructData.cPaymentTime[4], StructData.cPaymentTime[5]);//交易时间
        String strAccID = ""+lngAccountID;//账号

        String strFileName = strDateTime + "_" + strAccID + ".jpg";
        //判断文件是否存在
        File fFile = new File(PicPath + strFileName);
        if (!fFile.exists()) {
            //Log.e("UploadFaceRecord","人脸图片不存在");
            strFileName = strDateTime + "_" + strAccID + "_" + strMatchScore + ".jpg";//增加了人脸相识度的图片
            File fFile1 = new File(PicPath + strFileName);
            if (!fFile1.exists()) {
                Log.e("UploadFaceRecord", "人脸图片不存在1");
                return 2;
            }
        }
        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("image", PicPath + strFileName);

        String jsonData = DataAnalyze.PostRequest(map, fileMap, UPLOADRECORD_CODE);
        if (jsonData == null) {
            Log.e(TAG, "无法连接服务器，请重新检查网络");
            if (gHttpRecvHandler != null) {
                gHttpRecvHandler.sendEmptyMessage(HEVT_NULL);
            }
            return 3;
        } else {
            Message message = Message.obtain();
            message.obj = jsonData;
            message.what = HEVT_UPLOADRECORD;
            if (gHttpRecvHandler != null) {
                gHttpRecvHandler.sendMessage(message);
            }
        }
        return 0;
    }

    // 处理解析特征码数据信息
    private List<Userinfo> AnalyzeFaceCodeData(String StrUserInfo, int cDownMode) {
        String version = null;

        try {
            Log.d(TAG, "解析特征码数据信息:" + StrUserInfo);
            JSONArray jsonArray = new JSONArray(StrUserInfo);
            if (gUserInfoList == null) {
                gUserInfoList = new ArrayList<>();
            } else {
                gUserInfoList.clear();
            }
            gStrUserInfoList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String strTemp = "";
                Userinfo userinfo = new Userinfo();
                JSONObject jsonObject = jsonArray.getJSONObject(i);

//                public String accnum;		//String  必选    3       账号
//                public String campusid;	//String	可选  	8	园区编号
//                public String epid;		//String	可选  	8	使用单位
//                public String accclassid;	//String	必选	8	账户身份
//                public String personcode;	//String	必选	32	个人编号
//                public String feature_url;     	//String  必选	64      特征码地址
//                public String flag;		//String  必选	32	标记 1新增 2 修改 3删除
//                public String version;		//String  必选    11      版本号
//                accname	String	必选	32	一卡通用户名
//                cardcode	String	必选	20	一卡通卡内编号
//                if(jsonObject.optString("accnum")!=null
//                        && jsonObject.optString("campusid")!=null
//                        && jsonObject.optString("epid")!=null
//                        && jsonObject.optString("accclassid")!=null
//                        && jsonObject.optString("accname")!=null
//                        && jsonObject.optString("cardcode")!=null
//                        && jsonObject.optString("feature_url")!=null
//                        && jsonObject.optString("flag")!=null
//                        && jsonObject.optString("version")!=null)
                {
                    userinfo.accnum = jsonObject.optString("accnum");
                    userinfo.campusid = jsonObject.optString("campusid");
                    userinfo.epid = jsonObject.optString("epid");
                    userinfo.accclassid = jsonObject.optString("accclassid");
                    userinfo.accname = jsonObject.optString("accname");
                    userinfo.cardcode = jsonObject.optString("cardcode");
                    //userinfo.personcode=jsonObject.optString("personcode");
                    if (g_HttpCommInfo.iFaceServeType == 0) {
                        userinfo.pic_url = jsonObject.optString("pic_url");
                    } else if (g_HttpCommInfo.iFaceServeType == 1) {
                        userinfo.feature_url = jsonObject.optString("feature_url");
                    }
                    userinfo.flag = jsonObject.optString("flag");
                    userinfo.version = jsonObject.optString("version");

                    if (g_HttpCommInfo.iFaceServeType == 0) {
                        strTemp = userinfo.accnum + "," + userinfo.pic_url + "," + userinfo.flag;
                    } else if (g_HttpCommInfo.iFaceServeType == 1) {
                        strTemp = userinfo.accnum + "," + userinfo.feature_url + "," + userinfo.flag;
                    }
                    Log.d(TAG, strTemp);
                    gStrUserInfoList.add(strTemp);
                    gUserInfoList.add(userinfo);
                    version = userinfo.version;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception:" + e.getMessage());
        }
        return gUserInfoList;
    }

    // 处理解析照片数据信息
    private PhotoInfo AnalyzePhotoData(String strPhotoInfo) {
        String version = null;
        PhotoInfo photoInfo = new PhotoInfo();
        try {
            JSONObject jsonObject = new JSONObject(strPhotoInfo);

//                public String accnum;		//String  必选    3       账号
//                public String photo_url;	//String	必选	64            	  图片数据
//                public String flag;		//String            	必选	32		 标记
//                public String version;	//String            	必选    11            	   版本号

            if (jsonObject.optString("accnum") != null && jsonObject.optString("photo_url") != null) {
                photoInfo.accnum = jsonObject.optString("accnum");
                photoInfo.photo_url = jsonObject.optString("photo_url");
                //photoInfo.flag=jsonObject.optString("flag");
                //photoInfo.version=jsonObject.optString("version");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception:" + e.getMessage());
        }
        return photoInfo;
    }


}

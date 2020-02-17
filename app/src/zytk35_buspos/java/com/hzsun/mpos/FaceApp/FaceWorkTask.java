package com.hzsun.mpos.FaceApp;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.huiyuenet.faceCheck.FaceFunction;
import com.huiyuenet.faceCheck.THFI_FacePos;
import com.huiyuenet.faceCheck.THFI_Param;
import com.hzsun.mpos.Public.FileUtils;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.SerialWork.SerialWorkTask;
import com.hzsun.mpos.data.FacePayInfo;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static com.hzsun.mpos.Global.Global.FACE_INVALID;
import static com.hzsun.mpos.Global.Global.FACE_SAMEACC;
import static com.hzsun.mpos.Global.Global.FACE_UNREGIST;
import static com.hzsun.mpos.Global.Global.NETCOM_ERR;
import static com.hzsun.mpos.Global.Global.OK;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CommInfo;
import static com.hzsun.mpos.Global.Global.g_FacePayInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.GetMultiAccID;
import static com.hzsun.mpos.Public.Publicfun.ShowCardPaying;
import static com.hzsun.mpos.Public.Publicfun.ShowFaceErrorInfo;
import static com.hzsun.mpos.Sound.SoundPlay.VoicePlay;

public class FaceWorkTask {

    private static final String TAG = "FaceWorkTask";

    private static DetectFaceThread ThreadFaceDetect;
    private static FaceCheckThread ThreadFaceCheck;

    private DecimalFormat format = new DecimalFormat("0.0000");

    private long s_lngStart = 0;
    private long s_lngEnd = 0;
    private static long s_lngGetBmpCnt = 0;
    private static int s_MatchIndex = -1;
    private static int s_DetecteCnt = 0;
    private static int s_CheckCnt = 0;

    private volatile float s_LiveScore = 0f;

    private static boolean s_DetecteFlag = false;
    private volatile boolean s_LiveCheckFlag = true;
    private volatile boolean s_FaceCheckFlag = true;      //开始比对标记

    private static int s_Faceborder = 200;       //人脸边框
    private static int s_FacePD = 100;       //瞳距

    public FaceWorkTask() {
        Log.d(TAG, "FaceWorkTask: 构造");
    }

    public void Init() {
        Log.d(TAG, "FaceWorkTask 初始化");

        ThreadFaceDetect = new DetectFaceThread();
        ThreadFaceDetect.start();

        ThreadFaceCheck = new FaceCheckThread();
        ThreadFaceCheck.start();
    }

    public static void StartDetecte(boolean bFlag) {

        if (bFlag) {
            Log.d(TAG, "开始检测人脸，清除人脸缓存数据");
            s_DetecteFlag = bFlag;
            s_DetecteCnt = 0;
            s_MatchIndex = -1;
            DetectFaceThread.setDataNull();
            ThreadFaceCheck.setDataNull();
        } else {
            s_DetecteFlag = bFlag;
            Log.d(TAG, "停止检测人脸");
        }
    }

    public static boolean isDetecteFlag() {
        return s_DetecteFlag;
    }

    //人脸检测线程
    public static class DetectFaceThread extends Thread {

        private Object object = new Object();
        private volatile boolean isDetecting = false;
        private static boolean isFacePosValid = false;

        private static Rect[] rect = null;
        private static Bitmap mBitmap;

        public DetectFaceThread() {
        }

        public void stopDetect() {
            isDetecting = false;
            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }

        public static void setDataNull() {
            rect = null;
            //mBitmap= null;
            //s_DetecteFlag=false;
        }

        public static Rect[] getFaceRect() {
            if (isFacePosValid) {
                isFacePosValid = false;
                return rect;
            }
            return null;
        }

        public static void setBitmap(Bitmap bitmap) {
            if (s_DetecteFlag == true) {
                mBitmap = bitmap;
                s_lngGetBmpCnt++;
                //Log.d(TAG,"获取的bmp数:"+s_lngGetBmpCnt);
            }
        }

        @Override
        public void run() {
            isDetecting = true;

            while (isDetecting) {
                try {
                    if (g_WorkInfo.cBackActivityState == 0) {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } else {
                        TimeUnit.MILLISECONDS.sleep(600);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (s_DetecteFlag == false) {
                    continue;
                }
                try {
                    if (mBitmap != null) {
                        THFI_FacePos[] facePos = new THFI_FacePos[THFI_Param.MAX_FACE_NUMS];

                        for (int i = 0; i < facePos.length; i++) {
                            facePos[i] = new THFI_FacePos();
                        }
                        if (mBitmap == null)
                            continue;
                        byte[] pixelsBGR = FaceFunction.getPixelsBGR(mBitmap);
                        int faceNum = FaceFunction.faceDetect(pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), 30, facePos);
                        if (faceNum >= 1) {
                            if (g_WorkInfo.cBackActivityState != 0){
                                Log.e("省电模式下人脸数",faceNum+"");
                                g_WorkInfo.cDetectFaceState=1;
                                continue;
                            }
                            g_WorkInfo.lngPowerSaveCnt=System.currentTimeMillis();
                            rect = new Rect[faceNum];
                            for (int i = 0; i < faceNum; i++) {
                                rect[i] = new Rect(facePos[i].rcFace.left, facePos[i].rcFace.top, facePos[i].rcFace.right, facePos[i].rcFace.bottom);
                            }
                            //判断人脸边框
                            //if(facePos[0].rcFace.bottom-facePos[0].rcFace.top> s_Faceborder)
                            //判断瞳距
                            //if(facePos[0].ptRightEye.x-facePos[0].ptLeftEye.x> s_FacePD)
                            if (true) {
                                if (mBitmap == null)
                                    continue;
                                ThreadFaceCheck.setImageData(pixelsBGR, mBitmap, mBitmap.getWidth(), mBitmap.getHeight(), facePos);   // 启动比对检测
                            }
                            isFacePosValid = true;
                        } else {
                            ShowFaceErrorInfo(OK);
                            s_CheckCnt = 0;
                            s_MatchIndex = -1;
                            s_DetecteCnt = 0;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "------>" + e.toString());
                } finally {
                    if (mBitmap != null && !mBitmap.isRecycled()) {
                        //mBitmap.recycle();
                        mBitmap = null;
                    }
                }
            }
        }
    }

    //人脸比对线程
    public class FaceCheckThread extends Thread {

        private Object object = new Object();
        private volatile boolean isDetecting = false;
        private byte[] mPixelsBGR;
        private int mWidth;
        private int mHeight;
        private THFI_FacePos[] mFaceRect;
        private byte[] mFaceFeatures;
        private Bitmap mBitmap;

        public FaceCheckThread() {
        }

        public void stopDetect() {
            isDetecting = false;
            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }

        public void setDataNull() {
            mPixelsBGR = null;
            mWidth = 0;
            mHeight = 0;
            mFaceRect = null;
            mFaceFeatures = null;
            mBitmap = null;
            s_FaceCheckFlag = false;
        }

        public void setImageData(byte[] pixelsBGR, Bitmap inBitmap, int width, int height, THFI_FacePos[] rect) {
            mPixelsBGR = pixelsBGR;
            mBitmap = inBitmap;
            mWidth = width;
            mHeight = height;
            mFaceRect = rect;
            s_FaceCheckFlag = true;
            Log.d(TAG, "=======开始活体检测和比对=======");
        }

        @Override
        public void run() {
            isDetecting = true;

            while (isDetecting) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (Exception e) {
                }
                if (s_DetecteFlag == false) {
                    s_CheckCnt = 0;
                    continue;
                }
                mFaceFeatures = null;
                try {
                    if ((mPixelsBGR != null) && (mFaceRect != null) && s_FaceCheckFlag) {
                        synchronized (object) {
                            mFaceFeatures = FaceFunction.faceFeatures(mPixelsBGR, mWidth, mHeight, 30, mFaceRect);
                            if (mFaceFeatures != null) {
                                s_FaceCheckFlag = false;
                                //比对
                                float[] matchScore = new float[1];
                                final int matchIndex = FaceFunction.faceComparison1ToNMem(mFaceFeatures, matchScore);
                                if (matchIndex >= 0) {
                                    String strTmp = THFI_Param.FaceName.get(matchIndex);
                                    Log.e(TAG, "=======检测结果成功：" + matchIndex + " 返回名称：" + strTmp);

                                    if (g_WorkInfo.cQrCodestatus != 2) {
                                        ShowFaceErrorInfo(NETCOM_ERR);
                                        continue;
                                    }
                                    if (g_WorkInfo.cBackActivityState != 0)
                                        continue;
                                    if (true) {
                                        Log.e(TAG, "========开启人脸同脸校验=========");
                                        //判断一次识别过程中人脸是否一致
                                        if (s_DetecteCnt > 0) {
                                            if (matchIndex != s_MatchIndex) {
                                                s_DetecteCnt = 0;
                                                s_MatchIndex = -1;
                                                Log.e(TAG, "=======检测结果不一致：" + matchIndex + "," + s_MatchIndex);
                                                continue;
                                            } else {
                                                Log.e(TAG, "5.=======检测结果一致：" + matchIndex);
                                            }
                                        } else {
                                            if (s_MatchIndex != matchIndex) {
                                                s_DetecteCnt++;
                                                s_MatchIndex = matchIndex;
                                                Log.e(TAG, "=======第一次检测：" + matchIndex);
                                                continue;
                                            } else {
                                                Log.e(TAG, "=======第一次检测返回成功：" + matchIndex + "-" + s_MatchIndex);
                                                continue;
                                            }
                                        }
                                    }
                                    String strTmpList[] = strTmp.split(",");
                                    String strAccID = strTmpList[0];
                                    String strName = strTmpList[1];
                                    String strFlag = strTmpList[2];
                                    if (strFlag.equals("3"))//删除的人脸名单
                                    {
                                        Log.e(TAG, "人脸信息未注册");
                                        ShowFaceErrorInfo(FACE_UNREGIST);
                                        continue;
                                    }
                                    //是否活体检测
                                    s_LiveScore = 1;
                                    if (g_LocalInfo.cFaceLiveFlag == 0) {
                                        s_lngStart = System.currentTimeMillis();
                                        s_LiveScore = FaceFunction.faceLiveCheck(mPixelsBGR);
                                        Log.e(TAG, "=========活体检测耗时" + (System.currentTimeMillis() - s_lngStart));
                                        Log.e(TAG, " ========活体检测结果：" + format.format(s_LiveScore));
                                    }
                                    if (s_LiveScore < g_LocalInfo.fLiveThrehold) {
                                        Log.e(TAG, "无效人脸");
                                        //VoicePlay("face_invalid");
                                        ShowFaceErrorInfo(FACE_INVALID);
                                        continue;
                                    }
                                    s_MatchIndex = -1;
                                    s_DetecteCnt = 0;

                                    g_FacePayInfo = new FacePayInfo();
                                    Publicfun.GetCurrDateTime(g_FacePayInfo.cPayDataTime);
                                    g_FacePayInfo.lngOrderNum = g_WorkInfo.lngOrderNum;
                                    g_FacePayInfo.cAccName = strName.getBytes("GB2312");
                                    g_FacePayInfo.fMatchScore = matchScore[0];
                                    strAccID = GetMultiAccID(strAccID);//判断是否是多人脸
                                    try {
                                        g_FacePayInfo.lngAccountID = Long.parseLong(strAccID);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "数据转换异常:" + e.getMessage());
                                        VoicePlay("face_invalid");
                                        continue;
                                    }
                                    Log.d(TAG, "6.收到的人脸账号为：" + g_FacePayInfo.lngAccountID);
                                    //同脸判断
                                    if ((g_WorkInfo.lngAccountID == g_FacePayInfo.lngAccountID) && (g_LocalInfo.iAccSameTime != 0)) {
                                        if ((System.currentTimeMillis() - g_WorkInfo.lngAccSameCnt) < g_LocalInfo.iAccSameTime * 1000) {
                                            Log.e(TAG, "相同脸账号退出:" + g_WorkInfo.lngAccountID);
                                            ShowFaceErrorInfo(FACE_SAMEACC);
                                            continue;
                                        }
                                    }
                                    s_DetecteFlag = false;

                                    if (g_LocalInfo.cDockposFlag != 1
                                            || (g_LocalInfo.cDockposFlag == 1 && SerialWorkTask.getTradeState() == SerialWorkTask.STATE_PAYING)) {

                                        if (g_LocalInfo.iWithholdState == 1) {
                                            g_CardBasicInfo.lngAccountID = g_FacePayInfo.lngAccountID;
                                            g_WorkInfo.cOtherQRFlag = 4;  //在线代扣交易
                                            g_WorkInfo.cScanQRCodeFlag = 0;
                                            g_CommInfo.cWithholdInfoStatus = 1;
                                            g_CommInfo.lngSendComStatus |= 0x00010000;
                                        } else {
                                            g_WorkInfo.cScanQRCodeFlag = 0;
                                            g_WorkInfo.cOtherQRFlag = 2;//人脸识别消费
                                            g_CommInfo.cGetQRCodeInfoStatus = 1;
                                            g_CommInfo.lngSendComStatus |= 0x00000800;
                                        }
                                        g_Nlib.QR_SetDeviceReadEnable(2);  //结束识读
                                        ShowCardPaying();
                                    } else if (g_LocalInfo.cDockposFlag == 1 && SerialWorkTask.getTradeState() == SerialWorkTask.STATE_QUERYING) {
                                        //启用对接机,不进行支付,用来查询身份信息
                                        SerialWorkTask.onQueryDone(2);
                                    }
                                    if (mBitmap != null) {
                                        String strDtTmp = String.format("%02d%02d%02d%02d%02d%02d",
                                                g_FacePayInfo.cPayDataTime[0], g_FacePayInfo.cPayDataTime[1],
                                                g_FacePayInfo.cPayDataTime[2], g_FacePayInfo.cPayDataTime[3],
                                                g_FacePayInfo.cPayDataTime[4], g_FacePayInfo.cPayDataTime[5]);
                                        Log.d(TAG, "记录人脸图片:" + strDtTmp + "_" + strAccID + "_" + g_FacePayInfo.fMatchScore);
                                        FileUtils.SaveBitmap(mBitmap, strDtTmp + "_" + strAccID + "_" + g_FacePayInfo.fMatchScore);
                                    }
                                } else {
                                    Log.e(TAG, "检测结果失败");
                                    s_DetecteCnt = 0;
                                    s_MatchIndex = -1;
                                    s_CheckCnt++;
                                    if (s_CheckCnt > 3) {
                                        s_CheckCnt = 0;
                                        ShowFaceErrorInfo(FACE_UNREGIST);
                                    }
                                    continue;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "------>" + e.toString());
                } finally {
                    mPixelsBGR = null;
                    mFaceRect = null;
                    mFaceFeatures = null;
                }
            }
        }
    }


}

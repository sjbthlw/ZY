package com.hzsun.mpos.FaceApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import com.huiyuenet.faceCheck.FaceFunction;
import com.huiyuenet.faceCheck.THFI_FacePos;
import com.huiyuenet.faceCheck.THFI_Param;
import com.hzsun.mpos.Activity.CardPreActivity;
import com.hzsun.mpos.Public.FileUtils;
import com.hzsun.mpos.Public.Publicfun;
import com.hzsun.mpos.data.FacePayInfo;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static com.hzsun.mpos.Global.Global.CAMERA_NUM;
import static com.hzsun.mpos.Global.Global.g_CardBasicInfo;
import static com.hzsun.mpos.Global.Global.g_CommInfo;
import static com.hzsun.mpos.Global.Global.g_FacePayInfo;
import static com.hzsun.mpos.Global.Global.g_LocalInfo;
import static com.hzsun.mpos.Global.Global.g_Nlib;
import static com.hzsun.mpos.Global.Global.g_WorkInfo;
import static com.hzsun.mpos.Public.Publicfun.CameraDeal;
import static com.hzsun.mpos.Public.Publicfun.GetMultiAccID;
import static com.hzsun.mpos.Public.Publicfun.ShowCardPaying;
import static com.hzsun.mpos.Sound.SoundPlay.VoicePlay;

public class FaceWorkTask {

    private static final String TAG = "FaceWorkTask";

    private static DetectFaceThread ThreadFaceDetect;
    private static FaceCheckThread ThreadFaceCheck;

    private static DecimalFormat format = new DecimalFormat("0.0000");

    private long s_lngStart = 0;
    private long s_lngEnd = 0;
    private static long s_lngGetBmpCnt = 0;
    private static int s_MatchIndex = -1;
    private static int s_DetecteCnt = 0;
    private static int s_CheckCnt = 0;

    private static volatile float s_LiveScore = 0f;

    private static boolean s_DetecteFlag = false;
    private volatile boolean s_LiveCheckFlag = true;
    private static volatile boolean s_FaceCheckFlag = true;      //开始比对标记

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
            CameraDeal(1);
            s_DetecteFlag = bFlag;
            s_DetecteCnt = 0;
            s_MatchIndex = -1;
            DetectFaceThread.setDataNull();
            ThreadFaceCheck.setDataNull();
        } else {
            Log.d(TAG, "停止检测人脸");
            CameraDeal(0);
            s_DetecteFlag = bFlag;
        }
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
            }
        }

        @Override
        public void run() {
            isDetecting = true;

            while (isDetecting) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
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
                            //Log.e(TAG,"-------1.检测到是人脸-------");
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
    public static class FaceCheckThread extends Thread {

        private Object object = new Object();
        private volatile boolean isDetecting = false;
        private byte[] mPixelsBGR;
        private int mWidth;
        private int mHeight;
        private THFI_FacePos[] mFaceRect;
        private byte[] mFaceFeatures;
        private Bitmap mBitmap;
        private static Bitmap mInfrBitmap;
		private boolean isIn = false;

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
            mInfrBitmap=null;
            s_FaceCheckFlag = false;
        }

        public void setImageData(byte[] pixelsBGR, Bitmap inBitmap, int width, int height, THFI_FacePos[] rect) {
            mPixelsBGR = pixelsBGR;
            mBitmap = inBitmap;
            mWidth = width;
            mHeight = height;
            mFaceRect = rect;
            s_FaceCheckFlag = true;
        }

        public static void setInfrBitmap(Bitmap bitmap) {
            if (s_DetecteFlag == true) {
                mInfrBitmap = bitmap;
            }
        }

        @Override
        public void run() {
            isDetecting = true;

            while (isDetecting) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (s_DetecteFlag == false) {
                    s_CheckCnt = 0;
                    continue;
                }
                try {
                    if ((mPixelsBGR != null) && (mFaceRect != null) && s_FaceCheckFlag) {
                        synchronized (object) {
                            //Log.e(TAG,"=======2.开始对比人脸特征=======");
                            mFaceFeatures = null;
                            mFaceFeatures = FaceFunction.faceFeatures(mPixelsBGR, mWidth, mHeight, 30, mFaceRect);
                            if (mFaceFeatures != null) {
                                s_FaceCheckFlag = false;
                                //比对
                                float[] matchScore = new float[1];
                                final int matchIndex = FaceFunction.faceComparison1ToNMem(mFaceFeatures, matchScore);
                                if (matchIndex >= 0) {
                                    String strTmp = THFI_Param.FaceName.get(matchIndex);
                                    Log.e(TAG, "=======4.检测结果成功：" + matchIndex + " 返回名称：" + strTmp);

                                    if (g_WorkInfo.cQrCodestatus != 2) {
                                        VoicePlay("net_err");
                                        continue;
                                    }
                                    if(g_WorkInfo.cBackActivityState!=0)
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
                                        VoicePlay("face_unregist");
                                        continue;
                                    }
                                    //是否活体检测
                                    s_LiveScore = 1;
                                    if (g_LocalInfo.cFaceLiveFlag == 0) {
                                        Log.e(TAG, "摄像头个数:" + CAMERA_NUM + "");
                                        s_LiveScore = FaceFunction.faceLiveCheck(mPixelsBGR);
                                        if (s_LiveScore < g_LocalInfo.fLiveThrehold) {
                                            Log.e(TAG, "请放入人脸");
                                            VoicePlay("put_face");
                                            continue;
                                        }
                                        if(CAMERA_NUM==2){
                                            if(mInfrBitmap!=null){
                                                int faceNum = checkFaceNum(mInfrBitmap);
                                                Log.e("红外检测到人脸数",faceNum+"");
                                                if (faceNum<1){
                                                    Log.e(TAG, "请放入人脸");
                                                    VoicePlay("put_face");
                                                    continue;
                                                }
                                            }else {
                                                Log.e(TAG, "红外摄像头出现故障");
                                            }
                                        }
                                    }
                                    s_DetecteFlag = false;
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
                                    if (g_LocalInfo.iWithholdState == 1) {
                                        g_CardBasicInfo.lngAccountID = g_FacePayInfo.lngAccountID;
                                        g_WorkInfo.cOtherQRFlag = 4;  //在线代扣交易
                                        g_WorkInfo.cScanQRCodeFlag = 0;
                                        g_CommInfo.cWithholdInfoStatus = 1;
                                        g_CommInfo.lngSendComStatus |= 0x00010000;
                                    } else {
                                        g_WorkInfo.cOtherQRFlag = 2;//人脸识别消费
                                        g_WorkInfo.cScanQRCodeFlag = 0;
                                        g_CommInfo.cGetQRCodeInfoStatus = 1;
                                        g_CommInfo.lngSendComStatus |= 0x00000800;
                                    }
                                    g_Nlib.QR_SetDeviceReadEnable(2);  //结束识读
                                    ShowCardPaying();
                                    if (mBitmap != null) {
                                        String strDtTmp = String.format("%02d%02d%02d%02d%02d%02d",
                                                g_FacePayInfo.cPayDataTime[0], g_FacePayInfo.cPayDataTime[1],
                                                g_FacePayInfo.cPayDataTime[2], g_FacePayInfo.cPayDataTime[3],
                                                g_FacePayInfo.cPayDataTime[4], g_FacePayInfo.cPayDataTime[5]);
                                        Log.d(TAG, "记录人脸图片:" + strDtTmp + "_" + strAccID+ "_" + g_FacePayInfo.fMatchScore);
                                        FileUtils.SaveBitmap(mBitmap, strDtTmp + "_" + strAccID+ "_" + g_FacePayInfo.fMatchScore);
                                    }
                                } else {
                                    Log.e(TAG, "检测结果失败");
                                    s_DetecteCnt = 0;
                                    s_MatchIndex = -1;
                                    s_CheckCnt++;
                                    if (s_CheckCnt > 4) {
                                        s_CheckCnt = 0;
                                        VoicePlay("face_unregist");
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
                    //Log.e(TAG,"=======7.对比人脸特征和活体结束=======");
                    mPixelsBGR = null;
                    mFaceRect = null;
                    mFaceFeatures = null;
                }
            }
        }

        //检测红外摄像头人脸数
        private int checkFaceNum(Bitmap mInfrBitmap) {
            THFI_FacePos[] facePos = new THFI_FacePos[THFI_Param.MAX_FACE_NUMS];

            for (int i = 0; i < facePos.length; i++) {
                facePos[i] = new THFI_FacePos();
            }
            byte[] pixelsBGR = FaceFunction.getPixelsBGR(mInfrBitmap);
            int faceNum = FaceFunction.faceDetect(pixelsBGR, mInfrBitmap.getWidth(), mInfrBitmap.getHeight(), 30, facePos);
            return faceNum;
        }

        //判断人脸是否在框内
        private boolean isFaceIn(THFI_FacePos[] mFaceRect) {
            int left = mFaceRect[0].rcFace.left;
            int top = mFaceRect[0].rcFace.top;
            int right = mFaceRect[0].rcFace.right;
            int bottom = mFaceRect[0].rcFace.bottom;
            int x = left + (right - left) / 2;
            int y = top + (bottom - top) / 2;
            PointF point = new PointF(x, y);
            PointF pointC = new PointF(250, 280);
            return isPointInCircle(point, pointC, 120);
        }

        private boolean isPointInCircle(PointF pointF, PointF circle, float radius) {
            return Math.pow((pointF.x - circle.x), 2) + Math.pow((pointF.y - circle.y), 2) <= Math.pow(radius, 2);
        }

        private float BGRLive() {
            float liveScore = FaceFunction.faceLiveCheck(mPixelsBGR);  //正常
            Log.e(TAG, "BGR活体分数:" + liveScore + "");
            return liveScore;
        }

//        private float INFRLive(Bitmap infrBitmap) {
//            float[] liveScore = new float[2];
//            byte[] pixelsBGR;
//
//            if (infrBitmap == null) {
//                return 0;
//            }
//            THFI_FacePos[] mFaceRectInfr = new THFI_FacePos[]{new THFI_FacePos()};
//            pixelsBGR = FaceFunction.getPixelsBGR(infrBitmap);
//            int num = FaceFunction.faceDetect(pixelsBGR, infrBitmap.getWidth(), infrBitmap.getHeight(), 30, mFaceRectInfr);
//            Log.e(TAG, "红外人脸个数:" + num + "");
//            if (num > 0) {
//                /**红外活体检测**/
//                liveScore = FaceFunction.faceLiveCheck(null, pixelsBGR, mWidth, mHeight, 60, null, mFaceRectInfr);  //红外
//                Log.e(TAG, "红外活体分数:" + liveScore[0] + "");
//                return liveScore[0];
//            } else {
//                return 0;
//            }
//        }
    }


}

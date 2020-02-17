package com.huiyuenet.faceCheck;

import android.content.Context;

/**
 * Created by liuzhiwei on 2018-03-13.
 */

public class FaceCheck {

    static {
        System.loadLibrary("FaceCheck");
        //System.out.println("so包加载结果====="+nativeClassInit());
        /***
         * #include <string.h>     #include "THFaceImage_i.h"
         */
    }

    public static native int ver1N(int matchNum, byte[] feature, int[] matchIndex, float[] matchScore);

    public static native int allocateFeatureMemory(int capacity);
    public static native int addFeature(int index, byte[] feature);
    public static native int DetectLive1(byte[] bgrImageData, int width, int height, float[] score);

//    public static native int DetectLive2(byte[] bgrImageData, byte[] irImageData, THFI_FacePos[] facePosRgb, THFI_FacePos[] facePosIr, int width, int height, int threshold, float[] score);

    public static native void clearFeature();
//    public static native void getFeature(int index, byte[] feature);
    public static native int getPixelsBGR(byte[] dest, byte[] src, int w, int h);
    public static native String GetSDKVersionInfo();

    /********************************人脸检测**************************************/

    /**
     * 初始化引擎
     * @param read 存放 libTHDetect_dpbin.so文件的目录
     * @param write 临时读写目录，供算法初始化使用
     * @return
     */
    public static native int Init(String read, String write, Context context);

    /**
     * 人脸检测
     * @param nChannelID 检测通道id
     * @param pImage 图片数组
     * @param bpp 图片位数 24,8
     * @param nWidth 图片宽度
     * @param nHeight 图片高度
     * @param pfps 检测后的人脸坐标点数组
     * @param nMaxFaceNums 最大检测数
     * @param nSampleSize 缩放比例
     * @return
     */
    public static native int CheckFace(short nChannelID, byte[] pImage, int bpp, int nWidth, int nHeight, THFI_FacePos[] pfps, int nMaxFaceNums, int nSampleSize);

    /**
     * 释放引擎
     * @return
     */
    public static native void Release();

    /********************************人脸比对**************************************/
    /**
     * 初始化人脸引擎
     * @param read 核心库文件读取目录
     * @param write 初始化引擎时使用的临时写目录
     * @return
     */
    public static native int InitFaceEngine (String read, String write);

    /**
     * 获取特征点长度
     * @return
     */
    public static native int GetFeaturesSize ();

    /**
     * 特征点抽取
     * @param pImage 待抽取图片数据
     * @param nWidth 图片宽
     * @param nHeight 图片高
     * @param nChannel 色彩通道，必须填3
     * @param pfps 人脸坐标点，由人脸检测模块得到
     * @param pFeature 返回的人脸特征
     * @return
     */
    public static native int GetFeatures (byte[] pImage, int nWidth, int nHeight, int nChannel, THFI_FacePos pfps, byte[] pFeature);

    /**
     * 人脸比对
     * @param pFeature1 待比对人脸1
     * @param pFeature2 待比对人脸2
     * @return
     */
    public static native float FaceCompare (byte[] pFeature1,  byte[] pFeature2);

    /**
     * 释放引擎
     */
    public static native void FaceEngineRelease();


}

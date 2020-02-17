package com.huiyuenet.faceCheck;

import java.util.ArrayList;
import java.util.List;

public class THFI_Param {
	public static int nMinFaceSize = 50;//min face width size can be detected,default is 50 pixels
	public static int nRollAngle = 30;//max face roll angle,default is 30(degree)
	public static int bOnlyDetect = -1;//ingored
	public static int dwReserved = 0;//reserved value,must be NULL
    public static int faceFeatureSize = 2560;

    public static final String SUFFIX = ".v10";

//    public static final int IMG_HEIGHT = 480;
//    public static final int IMG_WIDTH = 640;
//
    public static final int IMG_HEIGHT = 600;
    public static final int IMG_WIDTH = 800;

    public static final int PICTURE_IMG_HEIGHT = 480;
    public static final int PICTURE_IMG_WIDTH = 640;

//    public static final int IMG_HEIGHT = 720;
//    public static final int IMG_WIDTH = 1280;

    public static final int SAMPLE_SIZE = 320;  // 360

    public static final int MAX_FACE_NUMS = 1;  // 最大支持的检测人脸数量

    public static final float LIVE_THRESHOLD = 0.2f;

    public static List<String> FaceName = new ArrayList<>();
    public static int EnrolledNum = 0;

    public static long DetectTime = 0;
    public static final int OPERATE_TIME_OUT = 5000;    // 建模、比对超时时间（单位：ms）
    public static int CameraID = 0;

    /* * 错误码 * */
    public static final int SUCCESS = 0;     // 成功
    public static final int ERR_OVER_CUR_CAPACITY = -1000;     // 特征容量超过当前已分配空间限制，需要重新分配空间
    public static final int ERR_OVER_MAX_CAPACITY = -1001;   // 特征容量超过限制，目前最大支持 200000
    public static final int ERR_INVALID_PARAM = -1002;     // 应用层传入参数错误
    public static final int ERR_UNINIT = -1003;      // 未执行初始化相关方法
    /* * 错误码 * */

}




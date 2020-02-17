package com.huiyuenet.faceCheck;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.hzsun.mpos.Global.Global.g_LocalInfo;

/**
 * Created by liuzhiwei on 2018-06-27.
 */

public class FaceFunction {
    /**是否打印日志*/
    public static boolean PRINTLOG = false;
    private static String targer = "face";
    /**人脸特征点建模存放路径*/
    public static final String PATH = Environment.getExternalStorageDirectory().toString() + "/zytk/facepath/";
    public static String savePath = PATH;

    /**人脸比对通过分数*/
    public static float fraction = 0.65f;

//    public static byte[] bgrPixels;

    public synchronized static float faceLiveCheck(byte[] pixelsBGR){
//        if(bgrPixels == null){
//            bgrPixels = getPixelsBGR(bmp);
//        }
        if(pixelsBGR == null){
            return -1;
        }

        float[] liveScore = new float[1];
        FaceCheck.DetectLive1(pixelsBGR, THFI_Param.IMG_HEIGHT, THFI_Param.IMG_WIDTH, liveScore);

        return liveScore[0];
    }

    /**
     * 抽取特征点
     * @param pixelsBGR 待抽取特征点的图片（请保证图片是正的）
     * @param angle 图片允许偏移的最大角度（建议建模时20度以内，检测时30度以内）
     * @param facePos 图片对应的人脸位置
     * @return
     */
    public static int faceDetect(byte[] pixelsBGR, int width, int height, int angle, THFI_FacePos[] facePos){

//        bgrPixels = getPixelsBGR(bmp);
        byte[] bgrPixels = pixelsBGR;

//        Bitmap saveBmp = Bitmap.createBitmap(THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, bmp.getConfig());
//        ByteBuffer dest = ByteBuffer.wrap(bytes);
//        saveBmp.copyPixelsFromBuffer(dest);
//
//        try{
//            FileOutputStream fos = new FileOutputStream("/sdcard/facepic/" + System.currentTimeMillis() + "_123.jpg");
//            saveBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//
//        }catch (Exception e){
//        }

//        float[] liveScore = new float[1];

//        int liveResult = FaceCheck.DetectLive(bgrPixels, THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, liveScore);

        //long start = System.currentTimeMillis();
        int ret = -1;

        synchronized (PATH) {
            ret = FaceCheck.CheckFace(
                    (short) 0, bgrPixels, 24,
                    width, height,
                    facePos,
                    THFI_Param.MAX_FACE_NUMS, THFI_Param.SAMPLE_SIZE);
        }
        //THFI_Param.DetectTime = (System.currentTimeMillis() - start);
        if (ret > 0) {
            //检测到人脸，判断角度和人脸坐标准确度
            THFI_FacePos pos = facePos[0];
            if (pos.fAngle.confidence < 0.7
                    || Math.abs(pos.fAngle.pitch) > angle
                    || Math.abs(pos.fAngle.roll) > angle) {
                printLog("人脸定位准确度不足或人脸角度大于" + angle);
            }
        } else {
            //未检测到人脸
            //printLog("未检测到人脸  ret=" + ret);
        }
        return ret;
    }

    /**
     * 抽取特征点
     * @param  //bimp 待抽取特征点的图片（请保证图片是正的）
     * @param angle 图片允许偏移的最大角度（建议建模时20度以内，检测时30度以内）
     * @param facePos 图片对应的人脸位置
     * @return
     */
    public synchronized static byte[] faceFeatures(byte[] pixelsBGR, int width, int height, int angle, THFI_FacePos[] facePos) {

        if(pixelsBGR == null || facePos == null || facePos[0] == null){
            return null;
        }

//        if (bmp == null || bmp.isRecycled()) {
//            printLog("图片为空或已被释放");
//            return null;
//        }

//        byte[] pixelsBGR = getPixelsBGR(bmp);
//
//        int ret = FaceCheck.CheckFace(
//                (short)0, pixelsBGR, 24,
//                bmp.getWidth(), bmp.getHeight(),
//                facePos,
//                THFI_Param.MAX_FACE_NUMS, THFI_Param.SAMPLE_SIZE);

        int ret = 1;
        if (ret > 0) {
            //检测到人脸，判断角度和人脸坐标准确度
            THFI_FacePos pos = facePos[0];

            if (pos.fAngle.confidence < 0.7
                    || Math.abs(pos.fAngle.pitch) > angle
                    || Math.abs(pos.fAngle.roll) > angle) {
                printLog("人脸定位准确度不足或人脸角度大于" + angle);
                return null;
            }

            int size = FaceCheck.GetFeaturesSize();
            byte[] feature = new byte[size];

            ret = FaceCheck.GetFeatures(pixelsBGR, width, height, 3, facePos[0], feature);
            if (ret == 1) {
                return feature;
            }
        } else {
            //未检测到人脸
            printLog("未检测到人脸  ret=" + ret);
            return null;
        }
        printLog("特征点抽取失败  ret=" + ret);
        return null;
    }

//    /**
//     * 抽取特征点
//     * @param bmp 待抽取特征点的图片（请保证图片是正的）
//     * @param angle 图片允许偏移的最大角度（建议建模时20度以内，检测时30度以内）
//     * @return
//     */
//    public static byte[] faceFeatures (Bitmap bmp, int angle) {
//        if (bmp == null || bmp.isRecycled()) {
//            printLog("图片为空或已被释放");
//            return null;
//        }
//        byte[] bytes = getPixelsBGR(bmp);
//
//        THFI_FacePos[] facePos = new THFI_FacePos[THFI_Param.MAX_FACE_NUMS];
//        for(int i=0; i<facePos.length; i++){
//            facePos[i] = new THFI_FacePos();
//        }
//
//        int ret = FaceCheck.CheckFace((short)0,bytes,24,bmp.getWidth(), bmp.getHeight(),facePos,THFI_Param.MAX_FACE_NUMS,THFI_Param.SAMPLE_SIZE);
//
//        if (ret > 0) {
//            //检测到人脸，判断角度和人脸坐标准确度
//            THFI_FacePos pos = facePos[0];
//            if (pos.fAngle.confidence < 0.7 || Math.abs(pos.fAngle.pitch) > angle
//                    || Math.abs(pos.fAngle.roll) > angle) {
//                printLog("人脸定位准确度不足或人脸角度大于" + angle);
//                return null;
//            }
//            int size = FaceCheck.GetFeaturesSize();
//            byte[] feature = new byte[size];
//            ret = FaceCheck.GetFeatures(bytes, bmp.getWidth(), bmp.getHeight(), 3, facePos[0], feature);
//            if (ret == 1) {
//                return feature;
//            }
//            printLog("特征点抽取失败  ret="+ret);
//            return null;
//        } else {
//            //未检测到人脸
//            printLog("未检测到人脸  ret="+ret);
//            return null;
//        }
//    }


    /**
     * 单个建模
     * @param feature 需要保存的特征
     * @return
     */
    public static boolean saveFaceFeatures(byte[] feature, String userName) {
        if (!createDir())
            return false;

//        byte[] feature = faceFeatures(bmp, 20, facePos);
        if (feature == null) {
            printLog("建模失败 特征点为空");
            return false;
        }

        THFI_Param.FaceName.add(THFI_Param.EnrolledNum, userName);
        FaceCheck.addFeature(THFI_Param.EnrolledNum, feature);
        THFI_Param.EnrolledNum++;

        boolean result = getFile(feature, savePath, userName + THFI_Param.SUFFIX);
        if (!result)
            printLog("模型文件保存失败");

        return result;
    }


//    /**
//     * 人脸1比1
//     * @param bmp1 比对者
//     * @param bmp2 被比对者
//     * @return
//     */
//    public static boolean faceComparison1To1 (Bitmap bmp1, Bitmap bmp2) {
//        byte[] feature1 = faceFeatures(bmp1, 30);
//        byte[] feature2 = faceFeatures(bmp1, 30);
//        if (feature1 == null || feature2 == null) {
//            printLog("特征点抽取失败");
//            return false;
//        }
//
//        float f = FaceCheck.FaceCompare(feature1, feature2);
//        printLog("人脸比对输出分数=="+f);
//        return f >= fraction;
//    }

    /**
     * 人脸比对 1比N
     * @param feature 比对者
     * @return 是否有匹配的人脸
     */
    public static int faceComparison1ToNMem(byte[] feature,float[] matchScore) {
        // 判断人脸特征点存放目录是否存在
        File dic = new File(savePath);
        if (!dic.exists()) {
            printLog("模型库不存在");
            return -1;
        }
//        long start = System.currentTimeMillis();
//        byte[] feature = faceFeatures(bmp, 30, facePos);

        if (feature == null) {
            return -1;
        }
        int[] matchIndex = new int[1];
        //float[] matchScore = new float[1];

        FaceCheck.ver1N(THFI_Param.EnrolledNum, feature, matchIndex, matchScore);
        if (matchScore[0] > g_LocalInfo.fFraction) {
            printLog("识别对比度  matchScore[0]=" + matchScore[0]);
            if(matchScore[0]==( float)0.9999)
            {
                printLog("识别对比度异常");
                return -1;
            }
            return matchIndex[0];
        } else {
            printLog("识别对比度小于  matchScore[0]=" + matchScore[0]);
            return -1;
        }
    }

//    /**
//     * 人脸比对 1比N
//     * @param bmp 比对者
//     * @return 是否有匹配的人脸
//     */
//    public static boolean faceComparison1ToN (Bitmap bmp) {
//
//        // 判断人脸特征点存放目录是否存在
//        File dic = new File(savePath);
//        if (!dic.exists()) {
//            printLog("模型库不存在");
//            return false;
//        }
//        byte[] feature = faceFeatures(bmp, 30);
//        if (feature == null) {
//            return false;
//        }
//        String[] dicNames = dic.list();
//
//        for (int i = 0; i < dicNames.length; i++) {
//            byte[] b = getBytes(savePath+dicNames[i]);
//            float f = FaceCheck.FaceCompare(feature, b);
//            if (f >= fraction) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * 获取bitmap中的像素数据
     * @param bmp
     * @return
     */
    public static byte[] getPixelsBGR(Bitmap bmp) {    // 耗时 2s
//        int w = bmp.getWidth();
//        int h = bmp.getHeight();
//
//        byte[] pixels = new byte[w * h * 3]; // Allocate for RGB
//        int k = 0;
//        for (int x = 0; x < h; x++) {
//            for (int y = 0; y < w; y++) {
//                int color = bmp.getPixel(y, x);
//
//                pixels[k] = (byte) Color.red(color);
//                pixels[k + 1] = (byte) Color.green(color);
//                pixels[k + 2] = (byte) Color.blue(color);
//                k+=3;
//            }
//        }
//        return pixels;

        int w = bmp.getWidth();
        int h = bmp.getHeight();
//        int sizeBitmap = bmp.getByteCount();   //获取bitmap的大小 = w*h*4
        byte[] byteArrayDest = new byte[w * h * 3];  //开辟数组，存放像素内容
        byte[] byteArraySrc = new byte[w * h * 4];  //开辟数组，存放像素内容

        ByteBuffer src = ByteBuffer.wrap(byteArraySrc);
        bmp.copyPixelsToBuffer(src);  //从bitmap中取像素值到buffer中，像素值类型为ARGB
        FaceCheck.getPixelsBGR(byteArrayDest, byteArraySrc,  w, h);

//        Bitmap saveBmp = Bitmap.createBitmap(bmp);
//        ByteBuffer dest = ByteBuffer.wrap(byteArrayDest);
//        saveBmp.copyPixelsFromBuffer(dest);
//
//        try{
//            FileOutputStream fos = new FileOutputStream("/sdcard/facesave/" + System.currentTimeMillis() + "_yx.jpg");
//            saveBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//
//        }catch (Exception e){
//        }

        return byteArrayDest;
    }

//    /**
//     *
//     * @param bmp
//     * @param rotateType
//     * @return
//     */
//    public static byte[] getRotatedPixelsBGR(Bitmap bmp, int rotateType)
//    {
//        int w = bmp.getWidth();
//        int h = bmp.getHeight();
//
//        byte[] pixels = new byte[w * h * 3]; // Allocate for RGB
//
//        int k = 0;
//        if (rotateType == 1)
//        {
//            for (int x = 0; x < w; x++) {
//                for (int y = 0; y < h; y++) {
//                    int color = bmp.getPixel(x, y);
//                    pixels[k] = (byte)Color.red(color);
//                    pixels[k + 1] = (byte)Color.green(color);
//                    pixels[k + 2] = (byte)Color.blue(color);
//                    k += 3;
//                }
//            }
//        }
//        if (rotateType == 2)
//        {
//            for (int x = 0; x < w; x++) {
//                for (int y = 0; y < h; y++) {
//                    int color = bmp.getPixel(w-1-x, y);
//                    pixels[k] = (byte)Color.red(color);
//                    pixels[k + 1] = (byte)Color.green(color);
//                    pixels[k + 2] = (byte)Color.blue(color);
//                    k += 3;
//                }
//            }
//        }
//        if (rotateType == 3)
//        {
//            for (int x = 0; x < w; x++) {
//                for (int y = 0; y < h; y++) {
//                    int color = bmp.getPixel(x, h-1-y);
//                    pixels[k] = (byte)Color.red(color);
//                    pixels[k + 1] = (byte)Color.green(color);
//                    pixels[k + 2] = (byte)Color.blue(color);
//                    k += 3;
//                }
//            }
//        }
//        if (rotateType == 4)
//        {
//            for (int x = 0; x < w; x++) {
//                for (int y = 0; y < h; y++) {
//                    int color = bmp.getPixel(w - 1 - x, h - 1 - y);
//                    pixels[k] = (byte)Color.red(color);
//                    pixels[k + 1] = (byte)Color.green(color);
//                    pixels[k + 2] = (byte)Color.blue(color);
//                    k += 3;
//                }
//            }
//        }
//        return pixels;
//    }

    /**
     * 打印日志
     *
     * @param msg 详细信息
     */
    private static void printLog(String msg) {
        if (PRINTLOG)
            Log.d(targer, msg);
    }

    /**
     * 创建模型库存放目录
     *
     * @return
     */
    private static boolean createDir() {
        if (savePath == null || savePath == "") {
            printLog("模型存放路径不能为空");
            return false;
        }
        //创建文件夹
        File file = new File(savePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return true;
    }

    /**
     * 根据byte数组，生成文件
     */
    public static boolean getFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {// 判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return true;
    }

//
//    /**
//     * 获得指定文件的byte数组
//     */
//    private static byte[] getBytes(String filePath) {
//        byte[] buffer = null;
//        try {
//            File file = new File(filePath);
//            FileInputStream fis = new FileInputStream(file);
//            ByteArrayOutputStream bos = new ByteArrayOutputStream(THFI_Param.faceFeatureSize);
//            byte[] b = new byte[THFI_Param.faceFeatureSize];
//            int n;
//            while ((n = fis.read(b)) != -1) {
//                bos.write(b, 0, n);
//            }
//            fis.close();
//            bos.close();
//            buffer = bos.toByteArray();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return buffer;
//    }

}

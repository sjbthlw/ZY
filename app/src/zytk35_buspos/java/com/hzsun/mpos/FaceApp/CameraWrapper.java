package com.hzsun.mpos.FaceApp;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.huiyuenet.faceCheck.THFI_Param;

import java.util.List;

@SuppressWarnings("deprecation")
public class CameraWrapper {

    private Camera mFaceCamera;
    private SurfaceTexture mPreviewSurface;
    private SurfaceHolder mSurfaceHolder;
    private Camera.PreviewCallback mPreviewCallback;
    public boolean isPreviewing = false;
    public Activity mActivity;

    public CameraWrapper(Activity context, Camera.PreviewCallback previewCallback, SurfaceHolder surfaceHolder) {
        this.mActivity = context;
        this.mPreviewSurface = new SurfaceTexture(0);
        this.mPreviewCallback = previewCallback;
        this.mSurfaceHolder = surfaceHolder;
    }

    public void openCamera(Activity activity) {

        this.mActivity = activity;

        if (mFaceCamera == null) {
            mFaceCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
//            mFaceCamera.setDisplayOrientation(90);
        }
        updateCameraParameters();
        setCameraDisplayOrientation(activity);
    }

    public void closeCamera() {
        if (mFaceCamera != null) {
            stopPreview();
            mFaceCamera.setPreviewCallback(null);
            mFaceCamera.release();
            mFaceCamera = null;
        }
    }

    public boolean startPreview() {

        if (mFaceCamera == null) {
            openCamera(mActivity);
        }

        mFaceCamera.setPreviewCallback(mPreviewCallback);
        mFaceCamera.startPreview();
        isPreviewing = true;

        return true;
    }

    public boolean stopPreview() {

        if (mFaceCamera != null) {
            mFaceCamera.stopPreview();
            mFaceCamera.setPreviewCallback(null);
        }
        isPreviewing = false;

        return true;
    }

    private void updateCameraParameters() {

        if (mFaceCamera == null) {
            return;
        }
        try {
            Parameters params = mFaceCamera.getParameters();

            List<Camera.Size> perSizes = params.getSupportedPreviewSizes();
//            for (int i = 0; i < perSizes.size(); i++) {
//                Log.i("预览宽", String.valueOf(perSizes.get(i).width));
//                Log.i("预览高", String.valueOf(perSizes.get(i).height));
//                Log.i("///////////","///////////");
//            }
            params.setPreviewSize(THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT);
            params.setPreviewFormat(ImageFormat.NV21);
            mFaceCamera.setParameters(params);
//            mFaceCamera.setPreviewDisplay(mSurfaceHolder);
            mFaceCamera.setPreviewTexture(mPreviewSurface);
        }
//        {
//            Parameters params = mFaceCamera.getParameters();
//
//            List<Camera.Size> perSizes = params.getSupportedPreviewSizes();
////            for (int i = 0; i < perSizes.size(); i++) {
////                Log.i("预览宽", String.valueOf(perSizes.get(i).width));
////                Log.i("预览高", String.valueOf(perSizes.get(i).height));
////                Log.i("///////////", "///////////");
////            }
//
//            int width=params.getSupportedPreviewSizes().get(0).width;
//            int height=params.getSupportedPreviewSizes().get(0).height;
//            params.setPreviewSize(width, height);
//            params.setPictureSize(width, height);
//
////            params.setPreviewSize(, THFI_Param.IMG_HEIGHT);
//            params.setPreviewSize(THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT);
//            params.setPreviewFormat(ImageFormat.NV21);
//            mFaceCamera.setParameters(params);
//            mFaceCamera.setPreviewDisplay(mSurfaceHolder);
////            mFaceCamera.setPreviewTexture(mPreviewSurface);
//        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getDisplayRotation(Activity activity) {

        if (activity == null) {
            return 0;
        }

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    private void setCameraDisplayOrientation(Activity activity) {
        // See android.hardware.Camera.setCameraDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        int degrees = getDisplayRotation(activity);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mFaceCamera.setDisplayOrientation(result);   // 修改这里的值并不会影响Camera预览方向
    }


}

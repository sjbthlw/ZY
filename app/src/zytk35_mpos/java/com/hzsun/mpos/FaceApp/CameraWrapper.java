package com.hzsun.mpos.FaceApp;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.Surface;

import com.huiyuenet.faceCheck.THFI_Param;

import java.util.List;

@SuppressWarnings("deprecation")
public class CameraWrapper {

    private Camera mFaceCamera;
    private SurfaceTexture mPreviewSurface;
    private Camera.PreviewCallback mPreviewCallback;
    public boolean isPreviewing = false;
    public Activity mActivity;
    private int cameraId = -1;

    public CameraWrapper(Activity context, Camera.PreviewCallback previewCallback) {
        this.mActivity = context;
        this.mPreviewSurface = new SurfaceTexture(0);
        this.mPreviewCallback = previewCallback;
    }

    public boolean openCamera(Activity activity, int cameraId) {

        this.mActivity = activity;
        this.cameraId = cameraId;

        if (mFaceCamera == null) {
            try {
                mFaceCamera = Camera.open(cameraId);
            } catch (RuntimeException e) {
                Log.e("出现异常","摄像头:"+cameraId);
                Log.e("摄像头出现异常",e.getMessage());
                return false;
            }
            mFaceCamera.setDisplayOrientation(270);
        }
        updateCameraParameters();
        setCameraDisplayOrientation(activity);
        return true;
    }

    public void closeCamera() {
        if (mFaceCamera != null) {
            stopPreview();
            mFaceCamera.setPreviewCallback(null);
            mFaceCamera.release();
            mFaceCamera = null;
        }
    }

    //打开摄像头并启用回调
    public boolean startPreview() {
        boolean isOpened = true;
        if (mFaceCamera == null) {
            isOpened = openCamera(mActivity, cameraId);
        }
        if (isOpened){
            mFaceCamera.setPreviewCallback(mPreviewCallback);
            mFaceCamera.startPreview();
            isPreviewing = true;
            return true;
        }else {
            return false;
        }
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
        } catch (Exception e) {
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
        Camera.getCameraInfo(THFI_Param.CameraID, info);
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

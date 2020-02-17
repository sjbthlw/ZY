package com.hzsun.mpos.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

/**
 * This class is a simple View to display the faces.
 */
public class FaceOverlayView extends View {

    private Paint mPaint;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private Rect[] rects;
    Path mPath = new Path();

    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public FaceOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        // We want a green box around the face:
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, metrics);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(stroke);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    //根据检测结果更新检测框
    public void update(Rect[] rects) {
        this.rects = rects;
        mPath.reset();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rects != null && rects.length > 0) {
            float scaleX = (float) getWidth() / (float) mPreviewWidth;
            float scaleY = (float) getHeight() / (float) mPreviewHeight;
            canvas.save();
            for (Rect rect : rects) {
                if (rect != null && rect.width() > 0 && rect.height() > 0) {
                    rect.left *= scaleX;
                    rect.top *= scaleY;
                    rect.right *= scaleX;
                    rect.bottom *= scaleY;
                    float width = (rect.right - rect.left);
                    float height = (rect.top - rect.bottom);
                    int widthLength = (int) width / 4;
//                    int heightLength = (int) height / 4;
                    int heightLength = (int) width / 4;

                    mPath.moveTo(rect.left, rect.top + heightLength);
                    mPath.lineTo(rect.left, rect.top);
                    mPath.lineTo(rect.left + widthLength, rect.top);

                    mPath.moveTo(rect.left + widthLength, rect.bottom);
                    mPath.lineTo(rect.left, rect.bottom);
                    mPath.lineTo(rect.left, rect.bottom - heightLength);

                    mPath.moveTo(rect.right, rect.top + heightLength);
                    mPath.lineTo(rect.right, rect.top);
                    mPath.lineTo(rect.right - widthLength, rect.top);

                    mPath.moveTo(rect.right - widthLength, rect.bottom);
                    mPath.lineTo(rect.right, rect.bottom);
                    mPath.lineTo(rect.right, rect.bottom - heightLength);

                    canvas.drawPath(mPath, mPaint);
                }
            }
            canvas.restore();
        }
    }

    public void setPreviewSize(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
    }
}
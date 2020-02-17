package com.hzsun.mpos.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.hzsun.mpos.R;


public class PayLoadingView extends View implements ValueAnimator.AnimatorUpdateListener {

    private Paint mPaint;
    private Paint mPaint2;
    private Path mPath;
    private PathMeasure measure;
    private float v;
    private ValueAnimator anim;
    private Path dstPath;
    private float strokeWidth = 7;
    private float strokeWidth2 = 3;
    private boolean isInit;
    private RectF rectF;
    private float step;

    public PayLoadingView(Context context) {
        this(context, null);
    }

    public PayLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PayLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint2 = new Paint();
        mPaint2.setStrokeWidth(strokeWidth2);
        mPaint2.setColor(getResources().getColor(R.color.gray6a6));
        mPaint2.setAntiAlias(true);
        mPaint2.setStyle(Paint.Style.STROKE);

        mPath = new Path();
        measure = new PathMeasure();
        anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(1500);
        anim.addUpdateListener(this);
        anim.start();
        dstPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInit) {
            step = Math.min(getWidth(), getHeight()) - strokeWidth;
            rectF = new RectF(strokeWidth, strokeWidth, step, step);
            mPath.addArc(rectF, 90, 360);
            mPath.addArc(rectF, 0, 90);
            measure.setPath(mPath, false);
            isInit = true;
        }
        dstPath.reset();
        float len = measure.getLength();
        measure.getSegment(0, len * v, dstPath, true);
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), step / 2, mPaint2);
        canvas.drawPath(dstPath, mPaint);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        v = (float) animation.getAnimatedValue();
        if (v != 1) {
            invalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (anim != null) {
            anim.cancel();
            anim = null;
        }
    }
}

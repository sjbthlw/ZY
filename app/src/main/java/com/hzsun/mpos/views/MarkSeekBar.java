package com.hzsun.mpos.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.hzsun.mpos.R;


/**
 * 作者：created by dell on 2019/2/25 15:28
 */
@SuppressLint("AppCompatCustomView")
public class MarkSeekBar extends SeekBar {

    /**
     * 刻度线画笔
     */
    private Paint mRulerPaint;

    /**
     * 刻度线的个数,等分数等于刻度线的个数加1
     */
    private int mRulerCount = 4;

    /**
     * 每条刻度线的宽度
     */
    private int mRulerWidth = 6;

    /**
     * 刻度线的颜色
     */
    private int mRulerColor = getResources().getColor(R.color.color_seekBar_mark);

    /**
     * 滑块上面是否要显示刻度线
     */
    private boolean isShowTopOfThumb = false;

    private BubbleIndicator mBubbleIndicator;

    private Drawable mThumbDrawable;

    private double markPercent = 0.6;

    private boolean isMark = true;

    public MarkSeekBar(Context context) {
        this(context, null);
        init();
    }

    public MarkSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public MarkSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        mBubbleIndicator = new BubbleIndicator(context, attrs, defStyleAttr, "100");
        setOnSeekBarChangeListener(mOnSeekBarChangeListener);
    }

    public void isMarkPercent(Boolean b) {
        isMark = b;
    }

    public void setMarkPercent(double markPercent) {
        this.markPercent = markPercent;
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        mThumbDrawable = thumb;
    }

    /**
     * 初始化
     */
    private void init() {
        //创建绘制刻度线的画笔
        mRulerPaint = new Paint();
        mRulerPaint.setColor(mRulerColor);
        mRulerPaint.setAntiAlias(true);

        //Api21及以上调用，去掉滑块后面的背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSplitTrack(false);
        }
    }


    /**
     * 重写onDraw方法绘制刻度线
     *
     * @param canvas
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //极限条件校验
        if (getWidth() <= 0 || mRulerCount <= 0) {
            return;
        }
        //seekBar长度
        int totalLength = (getWidth() - getPaddingLeft() - getPaddingRight());
        //计算刻度线的顶部坐标和底部坐标
        int rulerTop = getHeight() / 2 - getMinimumHeight() / 2;
        int rulerBottom = rulerTop + getMinimumHeight();
        int rulerLeft = (int) (totalLength * markPercent + getPaddingLeft());
        int rulerRight = rulerLeft + mRulerWidth;
        //获取滑块的位置信息
        Rect thumbRect = null;
        if (getThumb() != null) {
            thumbRect = getThumb().getBounds();
        }
        //判断是否需要绘制刻度线
        if (!isShowTopOfThumb && thumbRect != null && rulerLeft - getPaddingLeft() > thumbRect.left && rulerRight - getPaddingLeft() < thumbRect.right) {
            return;
        }

        if (isMark) {
            //进行绘制
            canvas.drawRect(rulerLeft, rulerTop, rulerRight, rulerBottom, mRulerPaint);
        }

    }

    private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mBubbleIndicator.hideIndicator();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mBubbleIndicator.showIndicator(seekBar, mThumbDrawable.getBounds());
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser)
                mBubbleIndicator.moveIndicator(mThumbDrawable.getBounds(), progress);
        }
    };

}

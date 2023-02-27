package com.widget.heartrate;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * by wenguan.chen 2023.02.09
 */
public class HeartRateView extends View {

    private List<Integer> mSourceData = new ArrayList<>();
    // 心率曲线
    private Path mPath = new Path();
    private float x, y, x2, y2, x3, y3, x4, y4, lastPx, lastPy;
    //最小心率值
    private int mMinValue = 0;
    //最大心率值
    private int mMaxValue = 190;
    //屏幕上显示的最大的心率值个数
    private int mMaxHearRateNumber = 60;

    private int mViewHeight;

    // 心率曲线距离上下左右的间距
    private float mMarginLeft = 0;
    private float mMarginRight = 0;
    private float mMarginTop = 0;
    private float mMarginBottom = 0;

    // 曲线颜色
    private int mStrokeColor = Color.parseColor("#67E516");
    // 曲线下方阴影颜色
    private int mStrokeShadowColor = Color.parseColor("#66A0F431");
    // 曲线线条大小
    private float mStrokeWidth = 2;
    // 曲线阴影大小
    private float mStrokeShadowWidth = 2;
    // 心率曲线画笔
    private Paint mValuePaint;
    // 两个心率值之间的间距
    private float mHearRateItemMargin = 0;
    // X轴平移刷新属性动画
    private ValueAnimator mRefreshAnim = null;

    public HeartRateView(Context context) {
        this(context, null);
    }

    public HeartRateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeartRateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HeartRateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.HeartChartView,
                defStyleAttr, defStyleRes);

        mMinValue = attributes.getInteger(R.styleable.HeartChartView_minValue, 70);
        mMaxValue = attributes.getInteger(R.styleable.HeartChartView_maxValue, 190);

        mMaxHearRateNumber = attributes.getInteger(R.styleable.HeartChartView_maxHeartRateNum, 60);
        mMarginLeft = attributes.getDimension(R.styleable.HeartChartView_marginLeft, 5);
        mMarginRight = attributes.getDimension(R.styleable.HeartChartView_marginRight, 5);
        mMarginTop = attributes.getDimension(R.styleable.HeartChartView_marginTop, 5);
        mMarginBottom = attributes.getDimension(R.styleable.HeartChartView_marginBottom, 5);

        mStrokeWidth = attributes.getDimension(R.styleable.HeartChartView_strokeWidth, 5);
        mStrokeShadowWidth = attributes.getDimension(R.styleable.HeartChartView_strokeShadowWidth, 10);
        mStrokeColor = attributes.getColor(R.styleable.HeartChartView_strokeColor, Color.parseColor("#67E516"));
        mStrokeShadowColor = attributes.getColor(R.styleable.HeartChartView_strokeShadowColor, Color.parseColor("#9964BC1C"));
        attributes.recycle();

        mValuePaint = new Paint();
        initAnim();
    }

    private void initAnim() {
        mRefreshAnim = ObjectAnimator.ofFloat(this, "shiftXRatio", 0f, 1f);
        mRefreshAnim.setDuration(1000);
        mRefreshAnim.setInterpolator(new LinearInterpolator());
        mRefreshAnim.addListener(mAnimListener);
        mRefreshAnim.setRepeatCount(ValueAnimator.INFINITE);
    }

    private Animator.AnimatorListener mAnimListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(@NonNull Animator animator) { }

        @Override
        public void onAnimationEnd(@NonNull Animator animator) { }

        @Override
        public void onAnimationCancel(@NonNull Animator animator) { }

        @Override
        public void onAnimationRepeat(@NonNull Animator animator) {
            startNextAnim();
        }
    };

    /**
     * 执行下一个点平移动画
     */
    private void startNextAnim() {
        mSourceData.remove(0);
        invalidatePath(0);
        if (mSourceData.size() <= mMaxHearRateNumber) {
            mRefreshAnim.cancel();
        }
    }

    /**
     * 重新绘制刷新心率曲线
     *
     * @param offsetX
     */
    private void invalidatePath(float offsetX) {
        generateNewPath(offsetX);
        invalidate();
    }

    /**
     * setShiftXRatio
     *
     * @param shiftRatio float
     */
    public void setShiftXRatio(float shiftRatio) {
        invalidatePath(mHearRateItemMargin * shiftRatio);
    }

    /**
     * 添加数据
     *
     * @param value
     */
    public void addData(int value) {
        if (value < mMinValue) {
            value = mMinValue;
        }
        if (value > mMaxValue) {
            value = mMaxValue;
        }
        mSourceData.add(value);
        if (mSourceData.size() <= mMaxHearRateNumber) {
            invalidatePath(0);
        } else {
            if (!mRefreshAnim.isStarted()) {
                mRefreshAnim.start();
            }
        }
    }

    /**
     * 设置最大值
     *
     * @param maxValue
     */
    public void setMaxValue(int maxValue){
        mMaxValue = maxValue;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mViewHeight = getMeasuredHeight();
        //计算X轴两个点之间的间隔
        mHearRateItemMargin = (getMeasuredWidth() - mMarginLeft - mMarginRight) * 1.0f / mMaxHearRateNumber;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRefreshAnim.cancel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mValuePaint.setAntiAlias(true);
        mValuePaint.setStyle(Paint.Style.STROKE);
        mValuePaint.setStrokeWidth(mStrokeWidth);
        mValuePaint.setColor(mStrokeColor);
        // 线条下方阴影
        mValuePaint.setShadowLayer(5, 0, 10, mStrokeShadowColor);
        canvas.drawPath(mPath, mValuePaint);
        mValuePaint.reset();
        mValuePaint.setColor(mStrokeColor);
        // 绘制最右侧圆点
        canvas.drawCircle(lastPx, lastPy, 6, mValuePaint);
    }

    /**
     * 构造刷新的路线数据
     *
     * @param offset X轴偏移量
     */
    private void generateNewPath(float offset) {
        mPath.reset();
        for (int i = 0; i < mSourceData.size(); i++) {
            // x,y表示当前点  x4,y4表示下一个点 x2,x3都是属于中间的点
            x = mMarginLeft + mHearRateItemMargin * i - offset;
            y = getHearRateValueToViewHeight(mSourceData.get(i));
            if (i == mSourceData.size() - 1) {
                x4 = x;
                y4 = y;
                lastPx = x;
                lastPy = y;
            } else {
                x4 = mMarginLeft + mHearRateItemMargin * (i + 1) - offset;
                y4 = getHearRateValueToViewHeight(mSourceData.get(i + 1));
            }
            x2 = x3 = (x + x4) / 2;
            y2 = y;
            y3 = y4;
            if (i == 0) {
                mPath.moveTo(x, y);
                mPath.lineTo(x, y);
            }
            if (i != mSourceData.size() - 1) {
                mPath.cubicTo(x2, y2, x3, y3, x4, y4);
            }
        }
    }

    private float getHearRateValueToViewHeight(int value) {
        if (value == mMinValue) {
            return mViewHeight - mMarginBottom - mStrokeShadowWidth;
        } else if (value == mMaxValue) {
            return mMarginTop;
        } else {
            return mViewHeight - (mMarginBottom + (mViewHeight - mMarginTop - mMarginBottom - mStrokeShadowWidth) * 1.0f / (mMaxValue - mMinValue) * (value - mMinValue));
        }
    }
}

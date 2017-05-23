package com.icodeyou.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class DownloadView extends View {
    private float mWidth;
    private float mHeight;
    private float mHeightValue;

    private Paint mBgPaint;
    private Paint mTextPaint;

    private float mCurrLength;
    private Status mStatus = Status.NORMAL;

    private float mTextSize;
    private int mTextColor;
    private int mBgColor;
    private int mProgressColor;

    // 收缩动画
    private ValueAnimator mShrinkAnim;
    // 收缩动画时长
    private long mShrinkDuration;

    // 旋转动画
    private ValueAnimator mPrepareRotateAnim;
    // 旋转动画时长
    private long mPrepareRotateAnimDuration;
    // 画布旋转角度
    private float mPrepareRotateAngle;
    // 旋转动画旋转速度
    private float mPrepareRotateAnimSpeed;

    // 展开动画
    private ValueAnimator mExpandAnim;
    private long mExpandAnimDuration;
    // 准备动画右移动
    private float mExpandTranslationX;

    // 加载转圈动画
    private ValueAnimator mLoadRotateAnim;
    // 加载转圈动画速度
    private int mLoadRotateAnimSpeed;
    // 加载转圈画布旋转角度
    private int mLoadAngle;

    private int mProgress;

    // 正弦移动点的动画
    private ValueAnimator mMovePointAnim;
    private int mMovePointDuration;
    private MovePoint[] mFourMovePoints = new MovePoint[4];
    private boolean mIsStopped;
    private float mMoveXFractionWhenPause;
    private OnProgressChangeListener mOnProgressChangeListener;

    public DownloadView(Context context) {
        this(context, null);
    }
    public DownloadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public DownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        initArgs(attrs);
        initPaint();
        initAnim();
    }

    private void initArgs(AttributeSet attrs) {
        if (mHeightValue == 0)
            mHeightValue = 50f;

        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.DownloadView);
        // 基本画笔参数
        mTextSize = attributes.getInt(R.styleable.DownloadView_textSize, 40);
        mTextColor = attributes.getColor(R.styleable.DownloadView_textColor, 0xFFFFFFFF);
        mBgColor = attributes.getColor(R.styleable.DownloadView_backgroundColor, 0xFF00CC99);
        mProgressColor = attributes.getColor(R.styleable.DownloadView_progressColor, 0x4400CC99);

        // 动画参数
        mShrinkDuration = attributes.getInt(R.styleable.DownloadView_shrinkDuration, 1000);
        mPrepareRotateAnimDuration = attributes.getInt(R.styleable.DownloadView_prepareRotateAnimDuration, 1000);
        mPrepareRotateAnimSpeed = attributes.getInt(R.styleable.DownloadView_prepareRotateAnimSpeed, 10);
        mExpandAnimDuration = attributes.getInt(R.styleable.DownloadView_expandAnimDuration, 1000);
        mLoadRotateAnimSpeed = attributes.getInt(R.styleable.DownloadView_loadRotateAnimSpeed, 5);
        mMovePointDuration = attributes.getInt(R.styleable.DownloadView_movePointDuration, 3000);
        attributes.recycle();
    }

    private void initPaint() {
        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(mBgColor);
        mBgPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
    }

    private void initAnim() {
        // 收缩动画
        mShrinkAnim = ValueAnimator.ofFloat(0, 1);
        mShrinkAnim.setDuration(mShrinkDuration);
        mShrinkAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mStatus = Status.START;
            }
        });
        mShrinkAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrLength = mWidth * (1 - animation.getAnimatedFraction());
                if (mCurrLength < mHeight) {
                    mCurrLength = mHeight;
                    mShrinkAnim.cancel();
                    mPrepareRotateAnim.start();
                }
                invalidate();
            }
        });

        // 旋转动画
        mPrepareRotateAnim = ValueAnimator.ofFloat(0, 1);
        mPrepareRotateAnim.setDuration(mPrepareRotateAnimDuration);
        mPrepareRotateAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mStatus = Status.PREPARE;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPrepareRotateAnim.cancel();
                mExpandAnim.start();
            }
        });
        mPrepareRotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPrepareRotateAngle += mPrepareRotateAnimSpeed;
                invalidate();
            }
        });

        // 展开动画
        mExpandAnim = ValueAnimator.ofFloat(0 ,1);
        mExpandAnim.setDuration(mExpandAnimDuration);
        mExpandAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mStatus = Status.EXPAND;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mExpandAnim.cancel();
                mLoadRotateAnim.start();
                mMovePointAnim.start();
            }
        });
        mExpandAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrLength = mHeight + (mWidth - mHeight) * animation.getAnimatedFraction();
                mExpandTranslationX = (mWidth / 2 - mHeight / 2) * animation.getAnimatedFraction();
                invalidate();
            }
        });

        // 加载转圈动画
        mLoadRotateAnim = ValueAnimator.ofFloat(0, 1);
        mLoadRotateAnim.setDuration(Integer.MAX_VALUE);
        mLoadRotateAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mStatus = Status.LOAD;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLoadRotateAnim.cancel();
            }
        });
        mLoadRotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLoadAngle += mLoadRotateAnimSpeed;
                invalidate();
            }
        });

        // 正弦移动点的动画
        mMovePointAnim = ValueAnimator.ofFloat(0, 1);
        mMovePointAnim.setDuration(mMovePointDuration);
        mMovePointAnim.setRepeatCount(ValueAnimator.INFINITE);
        mMovePointAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (int i = 0; i < mFourMovePoints.length; i++) {
                    mFourMovePoints[i].isDraw = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                for (int i = 0; i < mFourMovePoints.length; i++) {
                    mFourMovePoints[i].isDraw = true;
                }
            }
        });
        mMovePointAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i = 0; i < mFourMovePoints.length; i++) {
                    mFourMovePoints[i].moveX = mFourMovePoints[i].startX - mFourMovePoints[0].startX * animation.getAnimatedFraction();
                    if (mFourMovePoints[i].moveX < mHeight / 2f) {
                        mFourMovePoints[i].isDraw = false;
                    }
                    mFourMovePoints[i].moveY = getMoveY(mFourMovePoints[i].moveX);
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 收缩动画
        if (mStatus == Status.START || mStatus == Status.NORMAL) {
            float left = (mWidth - mCurrLength) / 2f;
            float right = (mWidth + mCurrLength) / 2f;
            canvas.drawRoundRect(new RectF(left, 0, right, mHeight), mHeight / 2f, mHeight / 2f, mBgPaint);
            if (mStatus == Status.NORMAL) {
                Paint.FontMetrics fm = mTextPaint.getFontMetrics();
                float y = mHeight / 2 + (fm.descent - fm.ascent) / 2 - fm.descent;
                canvas.drawText("下载", mWidth / 2, y, mTextPaint);
            }
        }

        // 预备期旋转动画
        if (mStatus == Status.PREPARE) {
            canvas.drawCircle(mWidth / 2f, mHeight / 2f, mHeight / 2f, mBgPaint);
            canvas.save();
            mTextPaint.setStyle(Paint.Style.FILL);
            canvas.rotate(mPrepareRotateAngle, mWidth / 2, mHeight / 2);
            // 中心大圆的参数
            float centerX = mWidth / 2f;
            float centerY = mHeight / 2f;
            float centerR = mHeight / 2f / 3f;
            canvas.drawCircle(centerX, centerY, centerR, mTextPaint);
            // 其他三个小圆的参数
            float minR = centerR / 2f;
            float minX = centerX;
            float minY = centerY - centerR - minR / 3;
            canvas.drawCircle(minX, minY, minR, mTextPaint);
            canvas.rotate(120, mWidth / 2, mHeight / 2);
            canvas.drawCircle(minX, minY, minR, mTextPaint);
            canvas.rotate(120, mWidth / 2, mHeight / 2);
            canvas.drawCircle(minX, minY, minR, mTextPaint);
            canvas.restore();
        }

        // 展开动画
        if (mStatus == Status.EXPAND) {
            float left = (mWidth - mCurrLength) / 2f;
            float right = (mWidth + mCurrLength) / 2f;
            canvas.drawRoundRect(new RectF(left, 0, right, mHeight), mHeight / 2f, mHeight / 2f, mBgPaint);
            mTextPaint.setStyle(Paint.Style.FILL);
            canvas.save();
            canvas.translate(mExpandTranslationX, 0);
            // 中心大圆的参数
            float centerX = mWidth / 2f;
            float centerY = mHeight / 2f;
            float centerR = mHeight / 2f / 3f;
            canvas.drawCircle(centerX, centerY, centerR, mTextPaint);
            // 其他三个小圆的参数
            float minR = centerR / 2f;
            float minX = centerX;
            float minY = centerY - centerR - minR / 3;
            canvas.drawCircle(minX, minY, minR, mTextPaint);
            canvas.rotate(120, mWidth / 2, mHeight / 2);
            canvas.drawCircle(minX, minY, minR, mTextPaint);
            canvas.rotate(120, mWidth / 2, mHeight / 2);
            canvas.drawCircle(minX, minY, minR, mTextPaint);
            canvas.restore();
        }

        if (mStatus == Status.LOAD || mStatus == Status.END) {
            float left = (mWidth - mCurrLength) / 2f;
            float right = (mWidth + mCurrLength) / 2f;
            mBgPaint.setColor(mProgressColor);
            canvas.drawRoundRect(new RectF(left, 0, right, mHeight), mHeight / 2f, mHeight / 2f, mBgPaint);
            if (mProgress < 100) {
                for (int i = 0; i < mFourMovePoints.length; i++) {
                    if (mFourMovePoints[i].isDraw)
                        canvas.drawCircle(mFourMovePoints[i].moveX, mFourMovePoints[i].moveY, mFourMovePoints[i].radius, mTextPaint);
                }
            }
            float progressRight = mProgress / 100f * mWidth;
            mBgPaint.setColor(mBgColor);
            canvas.save();
            canvas.clipRect(0, 0, progressRight, mHeight);
            canvas.drawRoundRect(new RectF(left, 0, right, mHeight), mHeight / 2f, mHeight / 2f, mBgPaint);
            canvas.restore();

            if (mProgress < 100) {
                // 画右侧的中心大圆和周围三个小圆
                float centerR = mHeight / 2;
                float centerX = mWidth - centerR;
                float centerY = mHeight / 2;
                canvas.drawCircle(centerX, centerY, centerR, mBgPaint);
                canvas.save();
                mTextPaint.setStyle(Paint.Style.FILL);
                canvas.rotate(mLoadAngle, centerX, centerY);
                float minR = 5;
                float minX = centerX;
                float minY = mHeight / 2 / 2;
                canvas.drawCircle(minX, minY, minR, mTextPaint);
                canvas.rotate(60, centerX, centerY);
                minR = 7;
                canvas.drawCircle(minX, minY, minR, mTextPaint);
                canvas.rotate(60, centerX, centerY);
                minR = 9;
                canvas.drawCircle(minX, minY, minR, mTextPaint);
                canvas.rotate(60, centerX, centerY);
                minR = 11;
                canvas.drawCircle(minX, minY, minR, mTextPaint);
                canvas.restore();
            }

            Paint.FontMetrics fm = mTextPaint.getFontMetrics();
            float y = mHeight / 2 + (fm.descent - fm.ascent) / 2 - fm.descent;
            canvas.drawText(mProgress + "%", mWidth / 2, y, mTextPaint);
        }
    }

    /**
     * 下载的状态枚举
     */
    public enum Status {
        /** 未开始，正常 */
        NORMAL,
        /** 开始，收缩 */
        START,
        /** 圆圈加载 */
        PREPARE,
        /** 扩张 */
        EXPAND,
        /** 正式加载 */
        LOAD,
        /** 结束 */
        END
    }

    /**
     * 开始下载
     */
    public void start() {
        mShrinkAnim.start();
    }

    /**
     * 恢复下载
     */
    public void resume() {
        if (mStatus != Status.LOAD)
            return;
        this.mIsStopped = false;
        mMovePointAnim.setCurrentFraction(mMoveXFractionWhenPause);
        mLoadRotateAnim.start();
        mMovePointAnim.start();
        if (mOnProgressChangeListener != null)
            mOnProgressChangeListener.onContinue();
    }

    /**
     * 暂停
     */
    public void pause() {
        if (mStatus != Status.LOAD)
            return;
        this.mIsStopped = true;
        mMoveXFractionWhenPause = mMovePointAnim.getAnimatedFraction();
        cancelAllAnimations();
        if (mOnProgressChangeListener != null)
            mOnProgressChangeListener.onPause();
    }

    /**
     * 取消
     */
    public void cancel() {
        if (mStatus != Status.LOAD)
            return;
        mStatus = Status.NORMAL;
        mProgress = 0;
        cancelAllAnimations();
        mIsStopped = false;
        invalidate();
        if (mOnProgressChangeListener != null)
            mOnProgressChangeListener.onCancel();
    }

    /**
     * 取消所有动画
     */
    private void cancelAllAnimations() {
        mShrinkAnim.cancel();
        mPrepareRotateAnim.cancel();
        mExpandAnim.cancel();
        mLoadRotateAnim.cancel();
        mMovePointAnim.cancel();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        float widthValue = getScreenSize() * 4f / 5;

        if (widthMode != MeasureSpec.EXACTLY) {
            if (widthMode == MeasureSpec.AT_MOST) {
                if (width > widthValue) {
                    width = (int) (widthValue);
                }
            } else {
                width = (int) widthValue;
            }
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            if (heightMode == MeasureSpec.AT_MOST) {
                if (height > dip2px(mHeightValue)) {
                    height = dip2px(mHeightValue);
                }
            } else {
                height = dip2px(mHeightValue);
            }
        }
        mWidth = width;
        mHeight = height;
        mCurrLength = width;
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mFourMovePoints[0] = new MovePoint((float) ((mWidth - mHeight / 2f) * 0.88), 0, dip2px(4));
        mFourMovePoints[1] = new MovePoint((float) ((mWidth - mHeight / 2f) * 0.83), 0, dip2px(3));
        mFourMovePoints[2] = new MovePoint((float) ((mWidth - mHeight / 2f) * 0.78), 0, dip2px(2));
        mFourMovePoints[3] = new MovePoint((float) ((mWidth - mHeight / 2f) * 0.70), 0, dip2px(5));
    }

    private static class MovePoint {
        /** 起始点 */
        float startX;
        /** 绘制的X坐标 */
        float moveX;
        /** 绘制的Y坐标 */
        float moveY;
        /** 绘制圆的半径 */
        float radius;
        /** 是否绘制标志位 */
        boolean isDraw;

        public MovePoint(float startX, float moveY, float radius) {
            this.startX = startX;
            this.moveY = moveY;
            this.radius = radius;
        }
    }

    /**
     * 设置进度
     */
    public void setProgress(int progress) {
        if (mStatus != Status.LOAD)
            throw new RuntimeException("不是加载状态，不能设置进度值");
        if (mIsStopped)
            return;
        mProgress = progress;
        if (mProgress >= 100) {
            mIsStopped = true;
            mStatus = Status.END;
            mLoadRotateAnim.cancel();
            return;
        }
        invalidate();
    }

    public boolean isLoading() {
        return mStatus == Status.LOAD && mIsStopped == false;
    }

    public interface OnProgressChangeListener {
        /** 暂停 */
        void onPause();
        /** 继续 */
        void onContinue();
        /** 取消 */
        void onCancel();
    }

    private float getMoveY(float moveX) {
        return (float) (mHeight / 2 + (mHeight / 2 - mFourMovePoints[3].radius) * Math.sin(4 * Math.PI * moveX / (mWidth - mHeight) + mHeight / 2));
    }

    private int getScreenSize() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private int dip2px(float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (int) dip, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAllAnimations();
    }

    public void setOnProgressChangeListener(OnProgressChangeListener mOnProgressChangeListener) {
        this.mOnProgressChangeListener = mOnProgressChangeListener;
    }

    // 设置收缩动画时长
    private DownloadView setShrinkDuration(long duration) {
        mShrinkDuration = duration;
        return this;
    }

    // 设置准备动画时长
    private DownloadView setPrepareAnimDuration(long duration) {
        mPrepareRotateAnimDuration = duration;
        return this;
    }

    // 设置准备动画旋转速率
    private DownloadView setPrepareAnimSpeed(int speed) {
        mPrepareRotateAnimSpeed = speed;
        return this;
    }

    // 设置文本大小
    private DownloadView setTextSize(int size) {
        mTextSize = size;
        return this;
    }

    // 设置背景色
    private DownloadView setBackground(int color) {
        mBgColor = color;
        return this;
    }

    // 设置文本颜色
    private DownloadView setTextColor(int color) {
        mTextColor = color;
        return this;
    }

    // 设置展开动画时间
    private DownloadView setExpandAnimDuration(int duration) {
        mExpandAnimDuration = duration;
        return this;
    }

    // 设置加载时右侧旋转动画速度
    private DownloadView setLoadRotateSpeed(int speed) {
        mLoadRotateAnimSpeed = speed;
        return this;
    }

    // 设置正弦小点移动的速度
    private DownloadView setLoadPointsSpeed(int speed) {
        mMovePointDuration = speed;
        return this;
    }

    // 设置进度颜色
    private DownloadView setProgressColor(int color) {
        mProgressColor = color;
        return this;
    }
}
package com.douyinloadingview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;


/**
 * Created by ccy(17022) on 2018/8/31 上午11:24
 * 仿抖音v2.5  加载框
 */
public class DYLoadingView extends View {

    //默认值
    private final float RADIUS = dp2px(6);
    private final float GAP = dp2px(0.8f);
    private static final float RTL_SCALE = 0.7f;
    private static final float LTR_SCALR = 1.3f;
    private static final int LEFT_COLOR = 0XFFFF4040;
    private static final int RIGHT_COLOR = 0XFF00EEEE;
    private static final int MIX_COLOR = Color.BLACK;
    private static final int DURATION = 350;
    private static final int PAUSE_DUARTION = 80;
    private static final float SCALE_START_FRACTION = 0.2f;
    private static final float SCALE_END_FRACTION = 0.8f;


    //属性
    private float radius1; //初始时左小球半径
    private float radius2; //初始时右小球半径
    private float gap; //两小球直接的间隔
    private float rtlScale; //小球从右边移动到左边时大小倍数变化(rtl = right to left)
    private float ltrScale;//小球从左边移动到右边时大小倍数变化
    private int color1;//初始左小球颜色
    private int color2;//初始右小球颜色
    private int mixColor;//两小球重叠处的颜色
    private int duration; //小球一次移动时长
    private int pauseDuration;//小球一次移动后停顿时长
    private float scaleStartFraction; //小球一次移动期间，进度在[0,scaleStartFraction]期间根据rtlScale、ltrScale逐渐缩放，取值为[0,0.5]
    private float scaleEndFraction;//小球一次移动期间，进度在[scaleEndFraction,1]期间逐渐恢复初始大小,取值为[0.5,1]

    //绘图
    private Paint paint1, paint2, mixPaint;
    private Path ltrPath, rtlPath, mixPath;
    private float distance; //小球一次移动距离(即两球圆点之间距离）

    //动画
    private ValueAnimator anim;
    private float fraction; //小球一次移动动画的进度百分比
    boolean isAnimCanceled = false;
    boolean isLtr = true;//true = 【初始左球】当前正【从左往右】移动,false = 【初始左球】当前正【从右往左】移动


    public DYLoadingView(Context context) {
        this(context, null);
    }

    public DYLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DYLoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DYLoadingView);
        radius1 = ta.getDimension(R.styleable.DYLoadingView_radius1, RADIUS);
        radius2 = ta.getDimension(R.styleable.DYLoadingView_radius2, RADIUS);
        gap = ta.getDimension(R.styleable.DYLoadingView_gap, GAP);
        rtlScale = ta.getFloat(R.styleable.DYLoadingView_rtlScale, RTL_SCALE);
        ltrScale = ta.getFloat(R.styleable.DYLoadingView_ltrScale, LTR_SCALR);
        color1 = ta.getColor(R.styleable.DYLoadingView_color1, LEFT_COLOR);
        color2 = ta.getColor(R.styleable.DYLoadingView_color2, RIGHT_COLOR);
        mixColor = ta.getColor(R.styleable.DYLoadingView_mixColor, MIX_COLOR);
        duration = ta.getInt(R.styleable.DYLoadingView_duration, DURATION);
        pauseDuration = ta.getInt(R.styleable.DYLoadingView_pauseDuration, PAUSE_DUARTION);
        scaleStartFraction = ta.getFloat(R.styleable.DYLoadingView_scaleStartFraction, SCALE_START_FRACTION);
        scaleEndFraction = ta.getFloat(R.styleable.DYLoadingView_scaleEndFraction, SCALE_END_FRACTION);
        ta.recycle();

        checkAttr();
        distance = gap + radius1 + radius2;

        initDraw();

        initAnim();

    }


    /**
     * 属性合法性检查校正
     */
    private void checkAttr() {
        radius1 = radius1 > 0 ? radius1 : RADIUS;
        radius2 = radius2 > 0 ? radius2 : RADIUS;
        gap = gap >= 0 ? gap : GAP;
        rtlScale = rtlScale >= 0 ? rtlScale : RTL_SCALE;
        ltrScale = ltrScale >= 0 ? ltrScale : LTR_SCALR;
        duration = duration > 0 ? duration : DURATION;
        pauseDuration = pauseDuration >= 0 ? pauseDuration : PAUSE_DUARTION;
        if (scaleStartFraction < 0 || scaleStartFraction > 0.5f) {
            scaleStartFraction = SCALE_START_FRACTION;
        }
        if (scaleEndFraction < 0.5 || scaleEndFraction > 1) {
            scaleEndFraction = SCALE_END_FRACTION;
        }
    }

    /**
     * 初始化绘图数据
     */
    private void initDraw() {
        paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mixPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint1.setColor(color1);
        paint2.setColor(color2);
        mixPaint.setColor(mixColor);

        ltrPath = new Path();
        rtlPath = new Path();
        mixPath = new Path();
    }

    private void initAnim() {
        fraction = 0.0f;

        stop();

        anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.setDuration(duration);
        if (pauseDuration > 0) {
            anim.setStartDelay(pauseDuration);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
        } else {
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setRepeatMode(ValueAnimator.RESTART);
            anim.setInterpolator(new LinearInterpolator());
        }
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fraction = animation.getAnimatedFraction();
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                isLtr = !isLtr;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                isLtr = !isLtr;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isAnimCanceled) {
                    anim.start();
                }
            }
        });

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);

        //WRAP_CONTENT时控件大小为最大可能的大小,保证显示的下
        float maxScale = Math.max(rtlScale, ltrScale);
        maxScale = Math.max(maxScale, 1);

        if (wMode != MeasureSpec.EXACTLY) {
            wSize = (int) (gap + (2 * radius1 + 2 * radius2) * maxScale + dp2px(1));  //宽度= 间隙 + 2球直径*最大比例 + 1dp
        }
        if (hMode != MeasureSpec.EXACTLY) {

            hSize = (int) (2 * Math.max(radius1, radius2) * maxScale + dp2px(1)); // 高度= 1球直径*最大比例 + 1dp
        }
        setMeasuredDimension(wSize, hSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerY = getMeasuredHeight() / 2.0f;

        float ltrInitRadius, rtlInitRadius;
        Paint ltrPaint, rtlPaint;

        //确定当前【从左往右】移动的是哪颗小球
        if (isLtr) {
            ltrInitRadius = radius1;
            rtlInitRadius = radius2;
            ltrPaint = paint1;
            rtlPaint = paint2;
        } else {
            ltrInitRadius = radius2;
            rtlInitRadius = radius1;
            ltrPaint = paint2;
            rtlPaint = paint1;
        }


        float ltrX = getMeasuredWidth() / 2.0f - distance / 2.0f;
        ltrX = ltrX + (distance * fraction);//当前从左往右的球的X坐标

        float rtlX = getMeasuredWidth() / 2.0f + distance / 2.0f;
        rtlX = rtlX - (distance * fraction);//当前从右往左的球的X坐标

        //计算小球移动过程中的大小变化
        float ltrBallRadius, rtlBallRadius;
        if (fraction <= scaleStartFraction) { //动画进度[0,scaleStartFraction]时，球大小由1倍逐渐缩放至ltrScale/rtlScale倍
            float scaleFraction = 1.0f / scaleStartFraction * fraction; //百分比转换 [0,scaleStartFraction]] -> [0,1]
            ltrBallRadius = ltrInitRadius * (1 + (ltrScale - 1) * scaleFraction);
            rtlBallRadius = rtlInitRadius * (1 + (rtlScale - 1) * scaleFraction);
        } else if (fraction >= scaleEndFraction) { //动画进度[scaleEndFraction,1]，球大小由ltrScale/rtlScale倍逐渐恢复至1倍
            float scaleFraction = (fraction - 1) / (scaleEndFraction - 1); //百分比转换，[scaleEndFraction,1] -> [1,0]
            ltrBallRadius = ltrInitRadius * (1 + (ltrScale - 1) * scaleFraction);
            rtlBallRadius = rtlInitRadius * (1 + (rtlScale - 1) * scaleFraction);
        } else { //动画进度[scaleStartFraction,scaleEndFraction]，球保持缩放后的大小
            ltrBallRadius = ltrInitRadius * ltrScale;
            rtlBallRadius = rtlInitRadius * rtlScale;
        }

        ltrPath.reset();
        ltrPath.addCircle(ltrX, centerY, ltrBallRadius, Path.Direction.CW);
        rtlPath.reset();
        rtlPath.addCircle(rtlX, centerY, rtlBallRadius, Path.Direction.CW);
        mixPath.op(ltrPath, rtlPath, Path.Op.INTERSECT);

        canvas.drawPath(ltrPath, ltrPaint);
        canvas.drawPath(rtlPath, rtlPaint);
        canvas.drawPath(mixPath, mixPaint);
    }


    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }


    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }


    //公开方法

    /**
     * 停止动画
     */
    public void stop() {
        if (anim != null) {
            anim.cancel();
            anim = null;
        }
    }

    /**
     * 开始动画
     */
    public void start() {
        if (anim == null) {
            initAnim();
        }
        if (anim.isRunning()) {
            anim.cancel();
        }

        post(new Runnable() {
            @Override
            public void run() {
                isAnimCanceled = false;
                isLtr = false;
                anim.start();
            }
        });
    }

    /**
     * 设置小球半径和两小球间隔
     */
    public void setRadius(float radius1, float radius2, float gap) {
        stop();
        this.radius1 = radius1;
        this.radius2 = radius2;
        this.gap = gap;
        checkAttr();
        distance = gap + radius1 + radius2;
        requestLayout(); //可能涉及宽高变化
    }


    /**
     * 设置小球颜色和重叠处颜色
     */
    public void setColors(int color1, int color2, int mixColor) {
        this.color1 = color1;
        this.color2 = color2;
        this.mixColor = color2;
        checkAttr();
        paint1.setColor(color1);
        paint2.setColor(color2);
        mixPaint.setColor(mixColor);
        invalidate();
    }

    /**
     * 设置动画时长
     *
     * @param duration      {@link #duration}
     * @param pauseDuration {@link #pauseDuration}
     */
    public void setDuration(int duration, int pauseDuration) {
        this.duration = duration;
        this.pauseDuration = pauseDuration;
        checkAttr();
        initAnim();
    }

    /**
     * 设置移动过程中缩放倍数
     *
     * @param ltrScale {@link #ltrScale}
     * @param rtlScale {@link #rtlScale}
     */
    public void setScales(float ltrScale, float rtlScale) {
        stop();
        this.ltrScale = ltrScale;
        this.rtlScale = rtlScale;
        checkAttr();
        requestLayout(); //可能涉及宽高变化
    }

    /**
     * 设置缩放开始、结束的范围
     *
     * @param scaleStartFraction {@link #scaleStartFraction}
     * @param scaleEndFraction   {@link #scaleEndFraction}
     */
    public void setStartEndFraction(float scaleStartFraction, float scaleEndFraction) {
        this.scaleStartFraction = scaleStartFraction;
        this.scaleEndFraction = scaleEndFraction;
        checkAttr();
        invalidate();
    }


    public float getRadius1() {
        return radius1;
    }

    public float getRadius2() {
        return radius2;
    }

    public float getGap() {
        return gap;
    }

    public float getRtlScale() {
        return rtlScale;
    }

    public float getLtrScale() {
        return ltrScale;
    }

    public int getColor1() {
        return color1;
    }

    public int getColor2() {
        return color2;
    }

    public int getMixColor() {
        return mixColor;
    }

    public int getDuration() {
        return duration;
    }

    public int getPauseDuration() {
        return pauseDuration;
    }

    public float getScaleStartFraction() {
        return scaleStartFraction;
    }

    public float getScaleEndFraction() {
        return scaleEndFraction;
    }

}

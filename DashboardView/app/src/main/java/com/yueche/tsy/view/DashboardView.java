package com.yueche.tsy.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * 汽车仪表盘
 * @author zhangwei
 * @time 2019.4.25
 * 参考woxingxiao 和 luoxiaoke
 */
public class DashboardView extends View {

    private int mRadius; // 扇形半径
    private int mStartAngle = 135; //150; // 起始角度
    private int mSweepAngle = 270; //240; // 绘制角度
    private int mMin = 0; // 最小值
    private int mMax = 180; // 最大值
    private int mSection = 9; // 值域（mMax-mMin）等分份数
    private int mPortion = 4; // 一个mSection等分份数
    private String mHeaderText = "km/h"; // 表头
    private int mSpeedValue = mMin; // 实时速度
    private int mStrokeWidth; // 画笔宽度
    private int mLength1; // 长刻度的相对圆弧的长度
    private int mPLRadius; // 指针长半径

    private int mPadding;
    private float mCenterX, mCenterY; // 圆心坐标
    private Paint mPaint;
    private RectF mRectFArc; // 仪表盘上最外圈的圆（第三层）
    private RectF mRectFArcInner;// 仪表盘上次外圈的圆（第四层）
    private Rect mRectText;
    private String[] mTexts;

    private int mCenterRadius; // 中心圆的半径，显示测速和单位
    private int mOutsideRadius; // 外部圆的半径，外层是画的阴影

    // 渐变圆心
    private Paint speedAreaPaint;

    // 中心圆的渐变效果
    private Paint mCenterPaint;

    // 外部的两个圈子，一个白色
    private RectF mOutsideWhiteRectF, mOutsideOilRectF;
    private int mOutSideSpace;// 圈圈外层的外色圈子和用油量的间距

    //  用油量的值
    private float mOilValue = 0.7f;// 用油量的值，是一个百分比，最大值100%，最小0%
    private int mOilValueStartAngle = 120;// 用油量开始转角
    private int mOilSweepAngle = 36;// 用油量的最大转角度
    private int mOilValueNum = 6;// 一共4份

    /**
     * 画转速圆
     */
    private int mRadiusRmp;// 转速半径
    private float mCenterXRmp, mCenterYRmp; // 圆心坐标
    // 外部的两个圈子，一个白色
    private RectF mOutsideWhiteRectFRmp, mOutsideOilRectFRmp;

    private RectF mRectFArcRmp; // 仪表盘上最外圈的圆（第三层）
    private RectF mRectFArcInnerRmp;// 仪表盘上次外圈的圆（第四层）

    private float mRmpValue = 0;// 转速
    private float mTmpValue = 20;// 温度值
    private String mRmpUnit = "x 1000rmp";// 转速单位
    private float mTmpValueMin = 0;//  温度最小值
    private float mTmpValueMax = 100;//温度最大值
    private int mTmpValueStartAngle = 30;// 用油量开始转角
    private String[] mTexts1;//  转速的刻盘值
    private int mMinRmp = 0; // 最小值
    private int mMaxRmp = 9; // 最大值
    private int mCenterRadiusRmp; // 中心圆的半径，显示测速和单位
    private int mOutsideRadiusRmp; // 外部圆的半径，外层是画的阴影

    public DashboardView(Context context) {
        this(context, null);
    }

    public DashboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mStrokeWidth = dp2px(3);
        mLength1 = dp2px(12) + mStrokeWidth;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectFArc = new RectF();
        mRectText = new Rect();
        mRectFArcInner = new RectF();
        mOutsideWhiteRectF = new RectF();
        mOutsideOilRectF = new RectF();

        mRectFArcInnerRmp = new RectF();
        mOutsideOilRectFRmp = new RectF();
        mRectFArcRmp = new RectF();

        mTexts = new String[mSection + 1]; // 需要显示mSection + 1个刻度读数
        for (int i = 0; i < mTexts.length; i++) {
            int n = (mMax - mMin) / mSection;
            mTexts[i] = String.valueOf(mMin + i * n);
        }

        mTexts1 = new String[mSection + 1];
        for (int i = 0; i < mTexts1.length; i++) {
            int n = (mMaxRmp - mMinRmp) / mSection;
            mTexts1[i] = String.valueOf(mMin + i * n);
        }

        //设置抗锯齿
        speedAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        speedAreaPaint.setAntiAlias(true);
        //设置画笔样式
        speedAreaPaint.setStyle(Paint.Style.FILL);

        mCenterPaint = new Paint();
        mCenterPaint.setAntiAlias(true);
        mCenterPaint.setAntiAlias(true);
        mCenterPaint.setStrokeWidth(mStrokeWidth * 2);
        // 设置速度范围扇形的渐变颜色
        int[] innerColors = new int[]{ContextCompat.getColor(getContext(), R.color.center_circle_inner)
                , ContextCompat.getColor(getContext(), R.color.center_circle_outside)};
        Shader mShader1 = new LinearGradient(mCenterX - mCenterRadius, mCenterY, mCenterX + mCenterRadius, mCenterY,
                innerColors, null, Shader.TileMode.CLAMP);
        mCenterPaint.setShader(mShader1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureWidth(widthMeasureSpec);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 画阴影效果
         */
        drawShader(canvas);

        /**
         * 外部圈
         */
        drawOutCircleBackground(canvas);

        /**
         * 画刻度
         */
        drawPonitValue1(canvas);

        /**
         * 画圆弧
         */
        drawCircleBackground(canvas);

        /**
         * 画刻度值
         */
        drawPonitValue(canvas, true);
        drawPonitValue(canvas,false);

        /**
         * 画中心圆效果
         */
        drawCenterCircle(canvas);

        /**
         *  画指针
         */
        drawPonitView(canvas);

        /**
         * 画值和单位
         */
        drawValueUnit(canvas);
    }

    /**
     * 初始化半径等数据
     * @param widthMeasureSpec
     */
    private void measureWidth(int widthMeasureSpec){
        mPadding = Math.max(
                Math.max(getPaddingLeft(), getPaddingTop()),
                Math.max(getPaddingRight(), getPaddingBottom())
        );
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());

        /**
         * 内外圈
         */
        mOutsideRadius = mRadius * 3 / 5;
        mCenterRadius = mRadius / 3;
        mOutSideSpace = dp2px(10);

        int width = resolveSize(dp2px(260), widthMeasureSpec);
//        mRadius = (width - mPadding * 2 - mStrokeWidth * 2) / 2  - 50;
        mRadius = (width - getPaddingLeft() - getPaddingRight() - mStrokeWidth * 2) / 2  - 50;

        // 由起始角度确定的高度
        float[] point1 = getCoordinatePoint(mRadius, mStartAngle, true);
        // 由结束角度确定的高度
        float[] point2 = getCoordinatePoint(mRadius, mStartAngle + mSweepAngle,true);
        int height = (int) Math.max(point1[1] + mRadius + mStrokeWidth * 2,
                point2[1] + mRadius + mStrokeWidth * 2);
        setMeasuredDimension(width, height + getPaddingTop() + getPaddingBottom());

        mCenterX = getMeasuredWidth() / 2f;
        mCenterY = getPaddingTop() + mRadius ;

        mRectFArc.set(
                mCenterX - mRadius - mStrokeWidth * 2 ,
                mCenterY - mStrokeWidth * 2 - mRadius,
                mCenterX + mRadius + mStrokeWidth * 2,
                mCenterY + mStrokeWidth * 2  + mRadius
        );

        mRectFArcInner.set(
                mCenterX - mRadius - mStrokeWidth ,
                mCenterY - mStrokeWidth  - mRadius,
                mCenterX + mRadius + mStrokeWidth ,
                mCenterY + mStrokeWidth  + mRadius
        );

        mPaint.setTextSize(sp2px(16));
        mPaint.getTextBounds("0", 0, "0".length(), mRectText);

        mPLRadius = mRadius - dp2px(30);

        mOutsideWhiteRectF.set(
                mCenterX - mRadius - mStrokeWidth * 2 - mOutSideSpace,
                mCenterY - mStrokeWidth * 2 - mRadius - mOutSideSpace,
                mCenterX + mRadius + mStrokeWidth * 2 + mOutSideSpace,
                mCenterY + mStrokeWidth * 2  + mRadius + mOutSideSpace
        );

        mOutsideOilRectF.set(
                mCenterX - mRadius - mStrokeWidth * 2 - mOutSideSpace * 2,
                mCenterY - mStrokeWidth * 2 - mRadius - mOutSideSpace * 2,
                mCenterX + mRadius + mStrokeWidth * 2 + mOutSideSpace * 2,
                mCenterY + mStrokeWidth * 2  + mRadius + mOutSideSpace * 2
        );

        /**
         * 转速
         */
        mRadiusRmp = mRadius / 2 ;
        mCenterRadiusRmp = mRadiusRmp / 3;
        mOutsideRadiusRmp = mRadiusRmp * 3 /5;
        mCenterXRmp = mCenterX + mRadiusRmp * 2 / 3 + dp2px(30);
        mCenterYRmp = mCenterY + mRadius + dp2px(40);
        mOutsideWhiteRectFRmp = new RectF();
        float maxLeft = mCenterXRmp - mRadiusRmp - mStrokeWidth * 2 - mOutSideSpace;
        float maxTop = mCenterYRmp - mStrokeWidth * 2 - mRadiusRmp - mOutSideSpace;
        float maxRight = mCenterXRmp + mRadiusRmp + mStrokeWidth * 2 + mOutSideSpace;
        float maxBottom = mCenterYRmp + mStrokeWidth * 2  + mRadiusRmp + mOutSideSpace;
        mOutsideOilRectFRmp.set(maxLeft + dp2px(5), maxTop + dp2px(5), maxRight - dp2px(5), maxBottom - dp2px(5));
        mOutsideWhiteRectFRmp.set(maxLeft + mOutSideSpace, maxTop + mOutSideSpace, maxRight  - mOutSideSpace, maxBottom  - mOutSideSpace);

        mRectFArcInnerRmp.set(maxLeft + mOutSideSpace + mStrokeWidth * 2, maxTop + mOutSideSpace + mStrokeWidth * 2,maxRight - mOutSideSpace - mStrokeWidth * 2, maxBottom - mOutSideSpace - mStrokeWidth * 2);
        mRectFArcRmp.set(maxLeft + mOutSideSpace + mStrokeWidth * 1.6f, maxTop + mOutSideSpace + mStrokeWidth* 1.6f,maxRight - mOutSideSpace - mStrokeWidth* 1.6f, maxBottom  - mOutSideSpace - mStrokeWidth * 1.6f);
    }

    /**
     * 画外部外色圈子
     * @param canvas
     */
    private void drawOutCircleBackground(Canvas canvas){
        int degree = mSpeedValue < mMax ? (mSpeedValue * mSweepAngle / mMax) : (mMax * mSweepAngle / mMax);
        /**
         * 画最外圈渐变的白色
         */
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setShader(outsideCircleGradinent(true));
        canvas.drawArc(mOutsideWhiteRectF, mStartAngle, degree, false, mPaint);

        /**
         * 最外圈的用油量
         */
        mPaint.setShader(null);

        // 用油量
        drawOilView(canvas);

        // 温度
        drawTmpView(canvas);

        /**
         *  转速
         */
        float mRmpDegree = mSweepAngle * mRmpValue / mMaxRmp;
        if(mRmpDegree > mSweepAngle)
            mRmpDegree = mSweepAngle;
        //画最外圈渐变的白色
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
//        mPaint.setColor(Color.argb(10, 255, 255, 255));
        mPaint.setShader(outsideCircleGradinent(false));
        canvas.drawArc(mOutsideWhiteRectFRmp, mStartAngle, mRmpDegree, false, mPaint);

        mPaint.setShader(null);
    }

    /**
     * 画外圈油量 和 图片
     * @param canvas
     */
    private void drawOilView(Canvas canvas){
        float middle = mOilValueStartAngle + mOilValue * mOilSweepAngle;
        mPaint.setStrokeWidth(mStrokeWidth * 2);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setAntiAlias(true);
        int mPiaceValue = mOilSweepAngle / mOilValueNum;
        for (int start = mOilValueStartAngle; start < (mOilSweepAngle + mOilValueStartAngle); start += mPiaceValue) {// 循环角度，画出环块
            if(start + mPiaceValue >= middle && start <= middle){// 存在中间值
                float mMiddleTmp = (middle - start)/2;
                // 画后面空的
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.oil_empty));
                canvas.drawArc(mOutsideOilRectF, start + mMiddleTmp, mPiaceValue/2 - mMiddleTmp, false, mPaint);

                // 画前面的
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.oil_full));
                canvas.drawArc(mOutsideOilRectF, start, mMiddleTmp, false, mPaint);
            }else
            if(start < middle){
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.oil_full));
                canvas.drawArc(mOutsideOilRectF, start, mPiaceValue/2, false, mPaint);
            }else{
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.oil_empty));
                canvas.drawArc(mOutsideOilRectF, start, mPiaceValue/2, false, mPaint);
            }
        }

        /**
         * 画图片
         */
        float[] p = getCoordinatePoint(mRadius + mOutSideSpace * 2, mOilValueStartAngle, true);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.dashboard_oli);
        canvas.drawBitmap(bmp, p[0] + dp2px(15), p[1] - dp2px(8), mPaint);
    }

    /**
     * 画外圈温度 和 图片
     * @param canvas
     */
    private void drawTmpView(Canvas canvas){
        float middle = mTmpValueStartAngle + mTmpValue * 1f /mTmpValueMax * mOilSweepAngle;
        mPaint.setStrokeWidth(mStrokeWidth * 1.2f);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
        mPaint.setAntiAlias(true);
        int mPiaceValue = mOilSweepAngle / mOilValueNum;
        for (int start = mTmpValueStartAngle; start < (mOilSweepAngle + mTmpValueStartAngle); start += mPiaceValue) {// 循环角度，画出环块
            if(start + mPiaceValue >= middle && start <= middle){// 存在中间值
                float mMiddleTmp = (middle - start)/2;
                // 画后面空的
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.oil_empty));
                canvas.drawArc(mOutsideOilRectFRmp, start + mMiddleTmp, mPiaceValue/2 - mMiddleTmp, false, mPaint);

                // 画前面的
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.oil_full));
                canvas.drawArc(mOutsideOilRectFRmp, start, mMiddleTmp, false, mPaint);
            }else
            if(start < middle){
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.oil_full));
                canvas.drawArc(mOutsideOilRectFRmp, start, mPiaceValue/2, false, mPaint);
            }else{
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.oil_empty));
                canvas.drawArc(mOutsideOilRectFRmp, start, mPiaceValue/2, false, mPaint);
            }
        }

        /**
         * 画图片
         */
        float[] p = getCoordinatePoint(mRadiusRmp + mOutSideSpace, mTmpValueStartAngle + 36, false);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.dashboard_temperature);
        canvas.drawBitmap(bmp, p[0] - dp2px(20), p[1] - dp2px(8), mPaint);
    }
    /**
     * 外圈的渐变色
     * @param speedOrRmp 是车速还是转速，车速 ture，false转速
     * @return
     */
    private SweepGradient outsideCircleGradinent(boolean speedOrRmp) {
        float x = mCenterX;
        float y = mCenterY;
        if(!speedOrRmp){
            x = mCenterXRmp;
            y = mCenterYRmp;
        }

        int degree = 255 * mSpeedValue / 180; //mSpeedValue < mMax ? (mSpeedValue * mSweepAngle) : (mMax * mSweepAngle);
        degree = degree > 255 ? 255 : degree;
        SweepGradient sweepGradient = new SweepGradient(x, y,
                new int[]{Color.argb(10, 255, 255, 255), Color.argb(250, 255, 255, 255)},
                new float[]{0, degree}//degree
        );
        Matrix matrix = new Matrix();
        matrix.setRotate(mStartAngle - 1, x, y);
        sweepGradient.setLocalMatrix(matrix);

        return sweepGradient;
    }

    /**
     * 车速扫描的渐变色
     * @param speedOrRmp 是车速还是转速，车速 ture，false转速
     * @return
     */
    private SweepGradient innerSweepGradient(boolean speedOrRmp) {
        float x = mCenterX;
        float y = mCenterY;
        if(!speedOrRmp){
            x = mCenterXRmp;
            y = mCenterYRmp;
        }
        int degree = 255 * mSpeedValue / 180; //mSpeedValue < mMax ? (mSpeedValue * mSweepAngle) : (mMax * mSweepAngle);
        degree = degree > 255 ? 255 : degree;
        SweepGradient sweepGradient = new SweepGradient(x, y,
                new int[]{Color.argb(40, 17, 98, 167), Color.argb(250, 17, 98, 167)},
                new float[]{0, degree}
        );
        Matrix matrix = new Matrix();
        matrix.setRotate(mStartAngle - 1, x, y);
        sweepGradient.setLocalMatrix(matrix);

        return sweepGradient;
    }

    /**
     * 画圆形背景
     */
    private void drawCircleBackground(Canvas canvas){

        // 外圈
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_light));
        canvas.drawArc(mRectFArc, mStartAngle, mSweepAngle, false, mPaint);

        // 内圈
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth * 2);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.circle_inner_bg));
        canvas.drawArc(mRectFArcInner, mStartAngle, mSweepAngle, false, mPaint);

        // ------------------------- 转速 ------------------------
        // 外圈
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth/2);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_light));
        canvas.drawArc(mRectFArcRmp, mStartAngle, mSweepAngle, false, mPaint);

        // 内圈
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.circle_inner_bg));
        canvas.drawArc(mRectFArcInnerRmp, mStartAngle, mSweepAngle, false, mPaint);
    }

    /**
     * 画阴影
     * @param canvas
     */
    private void drawShader(Canvas canvas){
        //绘制速度范围扇形区域
        speedAreaPaint.setShader(innerSweepGradient(true));
        int degree = mSpeedValue < mMax ? (mSpeedValue * mSweepAngle / mMax) : (mMax * mSweepAngle / mMax);
        canvas.drawArc(mRectFArcInner, mStartAngle, degree, true, speedAreaPaint);

        //绘制速度范围扇形区域
        float mRmpDegree = mSweepAngle * mRmpValue / mMaxRmp;
        if(mRmpDegree > mSweepAngle)
            mRmpDegree = mSweepAngle;
        speedAreaPaint.setShader(innerSweepGradient(false));
        canvas.drawArc(mRectFArcInnerRmp, mStartAngle, mRmpDegree, true, speedAreaPaint);
    }
    /**
     *  画指针
     * @param canvas
     */
    private void drawPonitView(Canvas canvas){
        /**
         * 画指针
         */
        float θ = mStartAngle + mSweepAngle * (mSpeedValue - mMin) / (mMax - mMin); // 指针与水平线夹角
        mPaint.setStrokeWidth(4);//r / 3
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.point_color));
        float[] p1 = getCoordinatePoint(mPLRadius, θ,true);
        // 计算sin 和cos 计算变化圆心的x y
        float κ =  mStartAngle + mSpeedValue * mSweepAngle / mMax;
        float x = (float) (mCenterX + mCenterRadius * Math.cos(Math.PI* κ/180));
        float y = (float) (mCenterY + mCenterRadius * Math.sin(Math.PI* κ/180));

        canvas.drawLine(p1[0], p1[1], x, y, mPaint);

    }

    /**
     * 画当前值和单位
     * @param canvas
     */
    private void drawValueUnit(Canvas canvas){
        /**
         * 画当前车速值
         */
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.point_color));
        mPaint.setTextSize(sp2px(26));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        canvas.drawText(String.valueOf(mSpeedValue), mCenterX, mCenterY, mPaint);// - mTextHeight

        /**
         * 画单位
         */
        mPaint.setTextSize(sp2px(12));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        mPaint.getTextBounds(mHeaderText, 0, mHeaderText.length(), mRectText);
        canvas.drawText(mHeaderText, mCenterX, mCenterY + mCenterRadius / 2.5f, mPaint);


        /**
         * 画当前车速值
         */
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.point_color));
        mPaint.setTextSize(sp2px(16));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        canvas.drawText(String.valueOf(mRmpValue), mCenterXRmp, mCenterYRmp - dp2px(3), mPaint);// - mTextHeight

        /**
         * 画单位
         */
        mPaint.setTextSize(sp2px(8));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        mPaint.getTextBounds(mRmpUnit, 0, mRmpUnit.length(), mRectText);
        canvas.drawText(mRmpUnit, mCenterXRmp, mCenterYRmp + mCenterRadiusRmp / 2.8f, mPaint);
    }

    /**
     * 画刻盘数字
     */
    private void drawPonitValue(Canvas canvas, boolean speedOrRmp){
        int radius = mRadius;
        String[] mTexts = this.mTexts;
        float mValue = mSpeedValue;
        if(!speedOrRmp){
            radius = mRadiusRmp;
            mTexts = this.mTexts1;
            mValue = mRmpValue;
        }

        /**
         * 画长刻度读数
         */
        if(speedOrRmp)
            mPaint.setTextSize(sp2px(16));
        else
            mPaint.setTextSize(sp2px(12));

        mPaint.setStyle(Paint.Style.FILL);
        float α;
        float[] p;
        float angle = mSweepAngle * 1f / mSection;
        for (int i = 0; i <= mSection; i++) {
            α = mStartAngle + angle * i;
            p = getCoordinatePoint(radius - mLength1, α,speedOrRmp);
            if (α % 360 > 135 && α % 360 < 225) {
                mPaint.setTextAlign(Paint.Align.LEFT);
            } else if ((α % 360 >= 0 && α % 360 < 45) || (α % 360 > 315 && α % 360 <= 360)) {
                mPaint.setTextAlign(Paint.Align.RIGHT);
            } else {
                mPaint.setTextAlign(Paint.Align.CENTER);
            }
            if(mValue > Integer.parseInt(mTexts[i]))
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.point_color));
            else
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.degree_scale));

            if(!speedOrRmp && i >= 7){
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.degree_scale_red));
            }

            mPaint.getTextBounds(mHeaderText, 0, mTexts[i].length(), mRectText);
            int txtH = mRectText.height();
            if(speedOrRmp){//车速
                if (i <= 1 || i >= mSection - 1) {
                    canvas.drawText(mTexts[i], p[0], p[1] + txtH / 2, mPaint);
                } else if (i == 3) {
                    canvas.drawText(mTexts[i], p[0] + txtH / 2, p[1] + txtH, mPaint);
                } else if (i == mSection - 3) {
                    canvas.drawText(mTexts[i], p[0] - txtH / 2, p[1] + txtH, mPaint);
                } else {
                    canvas.drawText(mTexts[i], p[0], p[1] + txtH, mPaint);
                }
            }else{//转速
                if (i <= 1 || i >= mSection - 1) {
                    canvas.drawText(mTexts[i], p[0], p[1] + txtH / 2, mPaint);
                } else if (i >= 3 && i <= 6) {
                    canvas.drawText(mTexts[i], p[0], p[1]+ txtH / 2, mPaint);
                } else if (i < 3){
                    canvas.drawText(mTexts[i], p[0] - txtH/3, p[1] + txtH/4, mPaint);
                }else{
                    canvas.drawText(mTexts[i], p[0] + txtH/2, p[1] + txtH/4, mPaint);
                }
            }
        }
    }

    /**
     * 画刻盘
     */
    private void drawPonitValue1(Canvas canvas){
        /**
         * 画长刻度读数
         */
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setTextSize(sp2px(16));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.degree_scale));
        float α;
        float[] p;
        float[] p1;
        float angle = mSweepAngle * 1f / (mSection * mPortion);
        // 画车速
        for (int i = 0; i <= mSection * mPortion; i++) {
            α = mStartAngle + angle * i;
            p1 = getCoordinatePoint(mRadius, α, true);
            if(i % mPortion == 0){// 画长刻度
                p = getCoordinatePoint(mRadius - mLength1 / 2, α, true);
                canvas.drawLine(p[0], p[1] ,p1[0], p1[1] , mPaint);
            }else{// 画短刻度
                p = getCoordinatePoint(mRadius - mLength1 / 4, α, true);
                canvas.drawLine(p[0], p[1] ,p1[0], p1[1] , mPaint);
            }
        }

        // 画转速
        mPaint.setStrokeWidth(mStrokeWidth/2);
        for (int i = 0; i <= mSection * mPortion; i++) {
            α = mStartAngle + angle * i;
            p1 = getCoordinatePoint(mRadiusRmp, α, false);
            if(i % mPortion == 0){// 画长刻度
                p = getCoordinatePoint(mRadiusRmp - mLength1 / 2, α, false);
                canvas.drawLine(p[0], p[1] ,p1[0], p1[1] , mPaint);
            }else{// 画短刻度
                p = getCoordinatePoint(mRadiusRmp - mLength1 / 4, α, false);
                canvas.drawLine(p[0], p[1] ,p1[0], p1[1] , mPaint);
            }
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 根据角度和半径计算圆上的点
     * @param radius
     * @param angle
     * @return
     */
    public float[] getCoordinatePoint(int radius, float angle, boolean speedOrRmp) {
        float x = mCenterX;
        float y = mCenterY;
        if(!speedOrRmp){
            x = mCenterXRmp;
            y = mCenterYRmp;
        }
        float[] point = new float[2];

        double arcAngle = Math.toRadians(angle); //将角度转换为弧度
        if (angle < 90) {
            point[0] = (float) (x + cos(arcAngle) * radius);
            point[1] = (float) (y + sin(arcAngle) * radius);
        } else if (angle == 90) {
            point[0] = x;
            point[1] = y + radius;
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0;
            point[0] = (float) (x - cos(arcAngle) * radius);
            point[1] = (float) (y + sin(arcAngle) * radius);
        } else if (angle == 180) {
            point[0] = x - radius;
            point[1] = y;
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0;
            point[0] = (float) (x - cos(arcAngle) * radius);
            point[1] = (float) (y - sin(arcAngle) * radius);
        } else if (angle == 270) {
            point[0] = x;
            point[1] = y - radius;
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0;
            point[0] = (float) (x + cos(arcAngle) * radius);
            point[1] = (float) (y - sin(arcAngle) * radius);
        }

        return point;
    }

    /**
     * 画中部的效果和圆
     */
    private void drawCenterCircle(Canvas canvas) {
        /**
         * 外部圆
         */
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.center_radius_color));
        canvas.drawCircle(mCenterX, mCenterY, mOutsideRadius, mPaint);

        // 中心圆心
        canvas.drawCircle(mCenterX, mCenterY,mCenterRadius, mCenterPaint);
        /**
         * 画中间圆
         */
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.center_radius_color));
        canvas.drawCircle(mCenterX, mCenterY, mCenterRadius - 20, mPaint);

        /**
         * -----------------转速--------------------------
         */
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.center_radius_color));
        canvas.drawCircle(mCenterXRmp, mCenterYRmp, mOutsideRadiusRmp, mPaint);

        mCenterPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawCircle(mCenterXRmp, mCenterYRmp, mCenterRadiusRmp + dp2px(4), mCenterPaint);

        /**
         * 画中间圆
         */
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.center_radius_color));
        canvas.drawCircle(mCenterXRmp, mCenterYRmp, mCenterRadiusRmp, mPaint);
    }

    /**
     * 获取车速
     * @return
     */
    public int getSpeedValue() {
        return mSpeedValue;
    }

    /**
     * 设置车速
     * @param velocity
     */
    public void setSpeedValue(int velocity) {
        if (mSpeedValue == velocity || velocity < mMin || velocity > mMax) {
            return;
        }

        mSpeedValue = velocity;
        postInvalidate();
    }

    /**
     * 设置转速
     * @param mValue
     */
    public void setRmpValue(float mValue){
        if(mValue < 0)
            mValue = 0;
        if(mValue > 9)
            mValue = 9;
        mRmpValue = mValue;
        postInvalidate();
    }


    /**
     * 获取用油量的值
     * @return
     */
    public float getOilValue(){
        return mOilValue;
    }

    /**
     * 设置当前用油量的值
     * @param value
     */
    public void setOilValue(float value){
        if(value < 0)
            value = 0;
        if(value > 1)
            value = 1;

        mOilValue = value;
        postInvalidate();
    }

    /**
     * 设置温度值
     * @param value
     */
    public void setTemperature(float value){
        mTmpValue = value;
        postInvalidate();
    }

}

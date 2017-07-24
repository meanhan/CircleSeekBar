package com.meanhan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.meanhan.circleprogressbar.R;
import com.meanhan.utils.DisplayUtil;


public class CircleProgressBar extends View {
    // 屏幕的宽高
    private int width;
    private int height;
    // arcPoint坐标
    private int arcX = 0;
    private int arcY = 0;
    private int process;
    private double curDegree;
    private Point arcPoint = null;
    // 初始和跨越角度
    private float startAngle = 135.0f;
    private float sweepAngle = 270.0f;
    // 背景画笔
    private Paint bgPaint;
    // 弧形条的宽度 即bgPaint的宽度
    private float bgThickness = DisplayUtil.dip2Pix(getContext(), 13);
    private int bgColor ;
    // 内景画笔
    private Paint fgPaint;
    // 弧形条内部的宽度
    protected float fgThickness = DisplayUtil.dip2Pix(getContext(), 13);
    private int fgColor;
    //拖动球  画笔
    private Paint thumbPaint;
    private int thumbWidth = DisplayUtil.dip2Pix(getContext(), 13);
    private int thumbColor;
    //文字 画笔
    private Paint textPaint;
    private String textValue = "";
    private int textPaintColor;
    private int textPaintSize = DisplayUtil.dip2Pix(getContext(), 12);
    //拖动球 距离 圆弧 距离
    public int THUMB_SPACE = DisplayUtil.dip2Pix(getContext(), 22);
    //拖动球所在区域
    private RectF thumbRectf;
    // 画弧形的矩阵区域
    private RectF mRadialScoreRect;
    // 圆的直径
    private int mDiameter;
    // 矩形区域坐标
    private float right;
    private float bottom;
    private float left;
    private float top;
    // 弧形的半径
    protected float mRadius;
    // 弧形的圆心点
    public Point centerPoint = null;

    // 当前的进度所对应的角度
    public double degree;
    // 当前进度所对应的值
    public int currentValue;
    public int upValue;
    //最大值 最小值
    public int maxValue;
    public int minValue;

    private boolean isMove = false;
    //是否按住小球
    private boolean isThumbSelected = false;
    //是否可拖动
    private boolean mEnabled = true;
    /**
     * 提供给外部访问值的接口
     */
    private OnMoveViewValueChanged mMove;
    private OnUpViewValueChanged mUp;

    public void setEnabled(boolean mEnabled) {
        this.mEnabled = mEnabled;
    }

    public int getProcess() {
        return process;
    }

    public void setCurDegree(double curDegree) {
        this.curDegree = curDegree;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getUpValue() {
        return upValue;
    }

    public void setUpValue(int upValue) {
        this.upValue = upValue;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }


    public interface OnMoveViewValueChanged {
        void onMoveChanged(String value);
    }

    public interface OnUpViewValueChanged {
        void onUpChanged(String value);
    }

    public void setOnMoveViewValueChanged(OnMoveViewValueChanged move) {
        mMove = move;
    }

    public void setOnUpViewValueChanged(OnUpViewValueChanged up) {
        mUp = up;
    }


    public CircleProgressBar(Context context) {
        this(context , null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context , attrs , 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /**
         * 获得我们所定义的自定义样式属性
         */
        TypedArray attrArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, defStyle, 0);
        int n = attrArray.getIndexCount();
        for (int i = 0; i < n; i++)
        {
            int attr = attrArray.getIndex(i);
            switch (attr)
            {
                case R.styleable.CircleProgressBar_startAngle:
                    startAngle = attrArray.getFloat(attr, 135.0f);
                    break;
                case R.styleable.CircleProgressBar_sweepAngle:
                    sweepAngle = attrArray.getFloat(attr, 270.0f);
                    break;
                case R.styleable.CircleProgressBar_backgroundThickness:
                    bgThickness = attrArray.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 13, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.CircleProgressBar_foregroundThickness:
                    bgThickness = attrArray.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 13, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.CircleProgressBar_backgroundColor:
                    bgColor = attrArray.getColor(attr, Color.TRANSPARENT);
                    break;
                case R.styleable.CircleProgressBar_foregroundColor:
                    fgColor = attrArray.getColor(attr, Color.GREEN);
                    break;
                case R.styleable.CircleProgressBar_maxValue:
                    maxValue = attrArray.getInt(attr, 100);
                    break;
                case R.styleable.CircleProgressBar_minValue:
                    minValue = attrArray.getInt(attr, 0);
                    break;
                case R.styleable.CircleProgressBar_thumbWidth:
                    thumbWidth = attrArray.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 13, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.CircleProgressBar_thumbColor:
                    thumbColor = attrArray.getColor(attr, Color.RED);
                    break;
                case R.styleable.CircleProgressBar_textSize:
                    // 默认设置为16sp，TypeValue也可以把sp转化为px
                    textPaintSize = attrArray.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.CircleProgressBar_textColor:
                    textPaintColor = attrArray.getColor(attr, Color.WHITE);
                    break;

            }

        }
        attrArray.recycle();
        init(context);
    }

    /**
     * 初始化一些成员变量
     * @param context
     */

    private void init(Context context) {
        //拖动球
        thumbPaint = new Paint();
        thumbPaint.setAntiAlias(true);
        thumbPaint.setColor(Color.parseColor("#3370CC"));
        thumbPaint.setStrokeWidth(1);
        // 背景Paint
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Style.STROKE);
        bgPaint.setStrokeWidth(bgThickness);
        bgPaint.setColor(Color.parseColor("#E5E5E5"));
        // 设置背景为透明
        // bgPaint.setAlpha(0);
        BlurMaskFilter blurMaskFilter = new BlurMaskFilter(1, Blur.INNER);
        bgPaint.setMaskFilter(blurMaskFilter);
        // 内景Paint 设置渐变色
        int[] colors = {0xFFE5BD7D, 0xFFFAAA64, 0xFFFFFFFF, 0xFF6AE2FD,
                0xFF8CD0E5, 0xFFA3CBCB, 0xFFBDC7B3, 0xFFD1C299, 0xFFE5BD7D};
        SweepGradient mSweepGradient = new SweepGradient(360, 360, colors, null);
        fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fgPaint.setStyle(Style.STROKE);
        fgPaint.setStrokeWidth(fgThickness);
        fgPaint.setShader(mSweepGradient);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor("#FFFFFF"));
        textPaint.setTextSize(textPaintSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        mRadialScoreRect = new RectF(0, 0, mDiameter, mDiameter);
        centerPoint = new Point((int) (left + mRadius), (int) (top + mRadius));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /**
         * Determine the diameter and the radius based on device orientation
         * 为了与背景图片重合,按比例将圆弧半径缩小
         */
        if (w > h) {
            mDiameter = h;
            mRadius = ((mDiameter / 2 - (getPaddingTop() + getPaddingBottom())) * 7) / 10;
        } else {
            mDiameter = w;
            mRadius = ((mDiameter / 2 - (getPaddingLeft() + getPaddingRight())) * 7) / 10;
        }
        // Init the draw arc Rect object
        left = (getWidth() / 2) - mRadius + getPaddingLeft();
        right = (getWidth() / 2) + mRadius - getPaddingRight();
        top = (getHeight() / 2) - mRadius + getPaddingTop();
        bottom = (getHeight() / 2) + mRadius - getPaddingBottom();
        mRadialScoreRect = new RectF(left, top, right, bottom);

        centerPoint = new Point((int) (left + mRadius), (int) (top + mRadius));
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*
         * startAngle 开始角度 sweepAngle 跨越角度 false 不画中心和弧线的连线 drawArc 绘制弧线
         */
        canvas.drawArc(mRadialScoreRect, startAngle, sweepAngle, false, bgPaint);

        if (!isMove) {
            setCurDegree(process * 270 / 100);
            setArcPoint(curDegree);
        }

        drawProgress(canvas);
        drawThumb(canvas);
        drawText(canvas);

    }

    private void drawThumb(Canvas canvas) {

        if (arcPoint != null) {
            canvas.drawCircle(arcPoint.x, arcPoint.y, thumbWidth, thumbPaint);
        }
    }

    private void drawProgress(Canvas canvas) {
        RectF fgRect = new RectF(left, top, right, bottom);
        canvas.drawArc(fgRect, startAngle, (float) curDegree, false, fgPaint);
    }

    private void drawText(Canvas canvas) {

        if (arcPoint != null) {
            canvas.drawText(textValue, arcPoint.x, arcPoint.y + thumbWidth / 2, textPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnabled) {
            return true;
        }
        float x = 0;
        float y = 0;
        x = event.getX();
        y = event.getY();

        degree = getAngle(x, y, centerPoint);
        if (thumbRectf.contains(x, y)) {
            setArcPoint(degree);
            setCurDegree(degree);
        } else {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x1 = event.getX();
                float y1 = event.getY();
                if (thumbRectf.contains(x1, y1)) {
                    isThumbSelected = true;
                } else {
                    isThumbSelected = false;
                }

                break;

            case MotionEvent.ACTION_MOVE:
                isMove = true;
                setCurrentValue((int) Math.round((degree * 100) / 270));
                if (isThumbSelected) {
                    textValue = getValueByProgress(getCurrentValue());
                    setArcPoint(degree);
                    setCurDegree(degree);
                }
                if (mMove != null) {
                    mMove.onMoveChanged(getValueByProgress(getCurrentValue()));
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                setUpValue((int) Math.round((degree * 100) / 270));
                if (isThumbSelected) {
                    textValue = getValueByProgress(getCurrentValue());
                    setArcPoint(degree);
                    setCurDegree(degree);
                }
                if (mUp != null) {
                    mUp.onUpChanged(getValueByProgress(getUpValue()));
                }
                isThumbSelected = false;
                invalidate();
                break;

        }

        return true;
    }

    // 根据中心点计算手指touch的角度
    private double getAngle(Float x, Float y, Point point) {
        float a = x - point.x;
        float b = y - point.y;
        double angle = Math.toDegrees(Math.atan2(b, a)) + 225;
        if (angle > 360) {
            angle = angle - 360;
        }
        if (angle > 270) {
            // 在第三象限
            if (a < 0) {
                angle = 0;
            }
            // 在第二象限
            else {
                angle = 270;
            }
        }
        return angle;
    }


    /**
     * 计算出当前点击位置对应的弧线上的坐标点
     *
     * @param degree
     */

    public void setArcPoint(double degree) {
        double radians = Math.toRadians(degree - 45);
        double incrementX = Math.cos(radians) * (mRadius + THUMB_SPACE);
        double incrementY = Math.sin(radians) * (mRadius + THUMB_SPACE);
        arcX = (int) (centerPoint.x - incrementX);
        arcY = (int) (centerPoint.y - incrementY);

        if (mRadius != 0) {
            arcPoint = new Point(arcX, arcY);
            thumbRectf = new RectF(arcX - thumbWidth - THUMB_SPACE, arcY - thumbWidth - THUMB_SPACE,
                    arcX + thumbWidth + THUMB_SPACE, arcY + thumbWidth + THUMB_SPACE);
        }
    }

    // 提供对应修改角度方法,实现point位置的显示
    public void setProcess(String processValue) {
        textValue = processValue;
        this.process = getProgressByValue(processValue);
        setCurDegree(process * 270 / 100);
        setArcPoint(curDegree);
        isMove = false;
        postInvalidate();
    }

    //根据初始值计算进度
    public int getProgressByValue(String value) {
        int progressValue;
        float i = Float.parseFloat(value);
        progressValue = (int) Math.round((i - minValue) * 100 / (maxValue - minValue));
        return progressValue;
    }

    //根据进度计算值
    public String getValueByProgress(int progress) {
        String value = "";
        value = progress * (maxValue - minValue) / 100 + minValue + "";
        return value;
    }

}

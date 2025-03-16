package com.example.player.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.MotionEventCompat;

import com.example.player.R;

import java.util.Arrays;

public class TouchView extends View implements BaseTouchView {

    public interface OperationListener {
        void onDeleteClick(TouchView stickerView);

        void onEdit(TouchView stickerView);

        void onTop(TouchView stickerView);

//        dx是从左下到左上
        void onMove(TouchView touchView, float dx, float dy);
        void onScale(TouchView touchView, float scale);
        void onRotate(TouchView touchView, float degrees, float px, float py);
    }

    private static final String TAG = "TouchView";

    private float[] mModelViewMatrix;

    private Paint localPaint;

    private Bitmap deleteBitmap;
    private Bitmap resizeBitmap;
    private Bitmap flipVBitmap;
    private Bitmap topBitmap;

    private static final float BITMAP_SCALE = 0.7f;

    /**
     * 是否在编辑模式
     */
    private boolean isInEdit = true;

//
//    private int mLayerWidth;
//    private int mLayerHeight;

    private int mContentWidth;
    private int mContentHeight;

    // 左上，右上，右下，左下
    private float[] transformedCorners = new float[8];

    private boolean isInRomate = false;
    private boolean isInResize = false;
    /**
     * 是否在四条线内部
     */
    private boolean isInSide;
    private float lastX, lastY;
    //是否是第二根手指放下
    private boolean isPointerDown = false;
    //手指移动距离必须超过这个数值
    private final float pointerLimitDis = 20f;
    //双指缩放时的初始距离
    private float oldDis;
    private final float pointerZoomCoeff = 0.09f;
    private float MIN_SCALE = 0.5f;

    private float MAX_SCALE = 1.2f;

    private int viewWidth;
    private int viewHeight;
    //    触摸点
    private PointF mid = new PointF();
    //    中心点
    private PointF centerPoint = new PointF();
    /**
     * 对角线的长度
     */
    private float lastLength;
    //    禁用旋转
    private boolean enableRotate = false;
    private float lastRotateDegree;
    private double halfDiagonalLength;


    private OperationListener operationListener;


    public TouchView(Context context) {
        super(context);
        init();
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TouchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setOperationListener(OperationListener operationListener) {
        this.operationListener = operationListener;
    }

//    public void setLayerSize(int width, int height) {
//        mLayerWidth = width;
//        mLayerHeight = height;
//    }


    private void init() {
        localPaint = new Paint();
        localPaint.setColor(getResources().getColor(R.color.white));
        localPaint.setAntiAlias(true);
        localPaint.setDither(true);
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setStrokeWidth(2.0f);

        initBitmaps();
    }

    private void initBitmaps() {
        topBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_scaling);
        deleteBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_delete);
        resizeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_rotate);
        flipVBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_scaling);
        resizeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_rotate);


        deleteBitmapWidth = (int) (deleteBitmap.getWidth() * BITMAP_SCALE);
        deleteBitmapHeight = (int) (deleteBitmap.getHeight() * BITMAP_SCALE);

        resizeBitmapWidth = (int) (resizeBitmap.getWidth() * BITMAP_SCALE);
        resizeBitmapHeight = (int) (resizeBitmap.getHeight() * BITMAP_SCALE);

        flipVBitmapWidth = (int) (flipVBitmap.getWidth() * BITMAP_SCALE);
        flipVBitmapHeight = (int) (flipVBitmap.getHeight() * BITMAP_SCALE);

        topBitmapWidth = (int) (topBitmap.getWidth() * BITMAP_SCALE);
        topBitmapHeight = (int) (topBitmap.getHeight() * BITMAP_SCALE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
    }

    @Override
    public void setModelViewMatrix(float[] modelViewMatrix) {
        if (Arrays.equals(mModelViewMatrix, modelViewMatrix)) {
            Log.d(TAG, "setModelViewMatrix方法，mModelViewMatrix相同，不作处理");
            return;
        }
        mModelViewMatrix = modelViewMatrix;
        invalidate();
    }

    @Override
    public void setInEdit(boolean isInEdit) {
        this.isInEdit = isInEdit;
        invalidate();
    }

    Path path = new Path();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw方法----->");

        // debug
        // canvas.drawColor(Color.argb(50, 0, 0, 255));

        if (mModelViewMatrix == null) {
            Log.d(TAG, "onDraw方法，mModelViewMatrix为空");
            return;
        }

        Log.d(TAG, "onDraw方法，mModelViewMatrix----->");

        // 定义四个顶点的坐标
        float[] corners = {
            -0.5f, -0.5f,
                0.5f, -0.5f,
                0.5f, 0.5f,
                -0.5f, 0.5f
        };

        // 计算矩阵的中心
        float centerX = mModelViewMatrix[12];
        float centerY = mModelViewMatrix[13];
        centerPoint.set(centerX, centerY);

        // 变换顶点坐标
        for (int i = 0; i < 4; i++) {
            int j = i * 2;
            float x = corners[j];
            float y = corners[j + 1];
            // 进行变换
            transformedCorners[j] = mModelViewMatrix[0] * x + mModelViewMatrix[4] * y + centerX;
            transformedCorners[j + 1] = mModelViewMatrix[1] * x + mModelViewMatrix[5] * y + centerY;
            transformedCorners[j + 1] = viewHeight - transformedCorners[j + 1];
        }

        mContentWidth = (int) (transformedCorners[2] - transformedCorners[0]);
        mContentHeight = (int) (transformedCorners[7] - transformedCorners[1]);
        setDiagonalLength();

        fixCoordinate();

        if (isInEdit) {
            drawBorder(canvas);
            drawButtons(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        boolean handled = true;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isInButton(event, dst_delete)) {
                    isInRomate = false;
                    if (operationListener != null) {
                        operationListener.onDeleteClick(this);
                    }
                } else if (isInResize(event)) {
                    isInRomate = false;
                    isInResize = true;
//                    lastRotateDegree = rotationToStartPoint(event);
                    midPointToStartPoint(event);
                    lastLength = diagonalLength(event);
                }
//                else if (isInButton(event, dst_flipV)) {
//                    isInRomate = false;
//                }
//                else if (isInButton(event, dst_top)) {
//                    X = (dst_top.left + dst_resize.right) / 2;
//                    Y = (dst_top.top + dst_resize.bottom) / 2;
//                    if (enableRotate) {
//                        matrix.postRotate(lastRotateDegree + 90, X, Y);
//                        lastRotateDegree += 90;
//                    }
//                    isInRomate = true;
//                    invalidate();
//                    if (enableRotate && operationListener != null) {
//                        operationListener.onRotate(this, lastRotateDegree + 90, X, Y);
//                    }
//                }
                else if (isInContent(event)) {
                    isInSide = true;
                    isInRomate = false;
                    lastX = event.getX(0);
                    lastY = event.getY(0);
                } else {
                    isInRomate = false;
                    handled = false;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (spacing(event) > pointerLimitDis) {
                    oldDis = spacing(event);
                    isPointerDown = true;
                    midPointToStartPoint(event);
                } else {
                    isPointerDown = false;
                }
                isInSide = false;
                isInResize = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // 双指缩放
                if (isPointerDown) {
                    float scale;
                    float disNew = spacing(event);
                    if (disNew == 0 || disNew < pointerLimitDis) {
                        scale = 1;
                    } else {
                        scale = disNew / oldDis;
                        //缩放缓慢
                        scale = (scale - 1) * pointerZoomCoeff + 1;
                    }
                    int oringinWidth = mContentWidth;
                    float scaleTemp = (scale * Math.abs(dst_flipV.left - dst_resize.left)) / oringinWidth;
                    if (((scaleTemp <= MIN_SCALE)) && scale < 1 ||
                            (scaleTemp >= MAX_SCALE) && scale > 1) {
                        scale = 1;
                    } else {
                        lastLength = diagonalLength(event);
                        Log.d(TAG, "scale:" + scale + " x:");
                        operationListener.onScale(this, scale);
                    }

//                    matrix.postScale(scale, scale, mid.x, mid.y);
//                    mScaleX = scale;
//                    mScaleY = scale;
//                    invalidate();
                } else if (isInResize) {
                    if (enableRotate) {
                        // 计算图片中心点
                        PointF centerPoint = new PointF();
                        midDiagonalPoint(centerPoint);
                        // 计算旋转角度
                        float currentDegree = rotationToCenterPoint(event, centerPoint);
                        float rotateDegree = currentDegree - lastRotateDegree;
                        // 以中心点旋转
                        if (operationListener != null && rotateDegree != 0) {
                            operationListener.onRotate(this, rotateDegree, centerPoint.x, centerPoint.y);
//                        matrix.postRotate(rotateDegree, centerPoint.x, centerPoint.y);
                            lastRotateDegree = currentDegree;
                        }

                    }

                    float diagonalLength = diagonalLength(event);
                    float scale = diagonalLength / lastLength;

//                    if (((diagonalLength / halfDiagonalLength <= MIN_SCALE)) && scale < 1 ||
//                            (diagonalLength / halfDiagonalLength >= MAX_SCALE) && scale > 1) {
//                        scale = 1;
//                        if (!isInResize(event)) {
//                            isInResize = false;
//                        }
//                    } else
                    {
                        lastLength = diagonalLength(event);
                    }

                    if (operationListener != null && scale != 1) {
                        operationListener.onScale(this, scale);
                    }

//                    matrix.postScale(scale, scale, mid.x, mid.y);
//                    mScaleX = scale;
//                    mScaleY = scale;
//                    invalidate();

//                    if (enableRotate && operationListener != null) {
//                        // 计算图片中心点
//                        PointF centerPoint = new PointF();
//                        midDiagonalPoint(centerPoint);
//                        operationListener.onRotate(this, lastRotateDegree, centerPoint.x, centerPoint.y);
//                    }
                } else if (isInSide) {
                    float x = event.getX(0);
                    float y = event.getY(0);

                    float dx = x - lastX;
                    float dy = y - lastY;
                    if (operationListener != null) {
                        operationListener.onMove(this, dx, 0 - dy);
                    }
//                    //TODO 移动区域判断 不能超出屏幕
//                    matrix.postTranslate(x - lastX, y - lastY);
//                    posX = x;
//                    posY = y;
                    lastX = x;
                    lastY = y;
//                    invalidate();
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isInResize = false;
                isInSide = false;
                isPointerDown = false;

//                X = (dst_top.left + dst_resize.right) / 2;
//                Y = (dst_top.top + dst_resize.bottom) / 2;
//                rotateDegree = lastRotateDegree;
//                Log.d(TAG, "leftBottomX:" + leftBottomX);
//                Log.d(TAG, "leftBottomY:" + leftBottomY);
//                Log.d(TAG, "viewWidth:" + viewWidth);
//                Log.d(TAG, "viewHeight:" + viewHeight);
                break;

        }
//        if (handled && operationListener != null) {
//            operationListener.onEdit(this);
//        }
        return handled;
    }

    private void setDiagonalLength() {
        halfDiagonalLength = Math.hypot(mContentWidth, mContentHeight) / 2;
    }

    /**
     * 计算对角线交叉的位置
     *
     * @param paramPointF
     */
    private void midDiagonalPoint(PointF paramPointF) {
//        float[] arrayOfFloat = new float[9];
//        this.matrix.getValues(arrayOfFloat);
//        float f1 = 0.0F * arrayOfFloat[0] + 0.0F * arrayOfFloat[1] + arrayOfFloat[2];
//        float f2 = 0.0F * arrayOfFloat[3] + 0.0F * arrayOfFloat[4] + arrayOfFloat[5];
//        float f3 = arrayOfFloat[0] * this.mBitmap.getWidth() + arrayOfFloat[1] * this.mBitmap.getHeight() + arrayOfFloat[2];
//        float f4 = arrayOfFloat[3] * this.mBitmap.getWidth() + arrayOfFloat[4] * this.mBitmap.getHeight() + arrayOfFloat[5];
//        float f5 = f1 + f3;
//        float f6 = f2 + f4;
//        paramPointF.set(f5 / 2.0F, f6 / 2.0F);
    }

    /**
     * 计算触摸点相对于图片中心点的旋转角度
     * @param event 触摸事件
     * @param centerPoint 图片中心点
     * @return 旋转角度
     */
    private float rotationToCenterPoint(MotionEvent event, PointF centerPoint) {
        double arc = Math.atan2(event.getY(0) - centerPoint.y, event.getX(0) - centerPoint.x);
        return (float) Math.toDegrees(arc);
    }

    /**
     * 触摸点到矩形中点的距离
     *
     * @param event
     * @return
     */
    private float diagonalLength(MotionEvent event) {
//        float diagonalLength = (float) Math.hypot(event.getX(0) - mid.x, event.getY(0) - mid.y);
        float diagonalLength = (float) Math.hypot(event.getX(0) - centerPoint.x, event.getY(0) - centerPoint.y);

        return diagonalLength;
    }

    /**
     * 触摸的位置和图片左上角位置的中点
     *
     * @param event
     */
    private void midPointToStartPoint(MotionEvent event) {
//        float[] arrayOfFloat = new float[9];
//        matrix.getValues(arrayOfFloat);
//        float f1 = 0.0f * arrayOfFloat[0] + 0.0f * arrayOfFloat[1] + arrayOfFloat[2];
//        float f2 = 0.0f * arrayOfFloat[3] + 0.0f * arrayOfFloat[4] + arrayOfFloat[5];
        float f3 = transformedCorners[0] + event.getX(0);
        float f4 = transformedCorners[1] + event.getY(0);
        mid.set(f3 / 2, f4 / 2);
    }

    /**
     * 计算双指之间的距离
     */
    private float spacing(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    /**
     * 是否在四条线内部
     * 图片旋转后 可能存在菱形状态 不能用4个点的坐标范围去判断点击区域是否在图片内
     *
     * @return
     */
    private boolean isInContent(MotionEvent event) {
        if (this.mContentWidth == 0 || this.mContentHeight == 0) {
            return false;
        }

        float[] arrayOfFloat2 = new float[4];
        float[] arrayOfFloat3 = new float[4];
        //确定X方向的范围
        arrayOfFloat2[0] = transformedCorners[0];//左上的x
        arrayOfFloat2[1] = transformedCorners[2];//右上的x
        arrayOfFloat2[2] = transformedCorners[4];//右下的x
        arrayOfFloat2[3] = transformedCorners[6];//左下的x
        //确定Y方向的范围
        arrayOfFloat3[0] = transformedCorners[1];//左上的y
        arrayOfFloat3[1] = transformedCorners[3];//右上的y
        arrayOfFloat3[2] = transformedCorners[5];//右下的y
        arrayOfFloat3[3] = transformedCorners[7];//左下的y
        return pointInRect(arrayOfFloat2, arrayOfFloat3, event.getX(0), event.getY(0));
    }

    /**
     * 判断点是否在一个矩形内部
     *
     * @param xRange
     * @param yRange
     * @param x
     * @param y
     * @return
     */
    private boolean pointInRect(float[] xRange, float[] yRange, float x, float y) {
        //四条边的长度
        double a1 = Math.hypot(xRange[0] - xRange[1], yRange[0] - yRange[1]);
        double a2 = Math.hypot(xRange[1] - xRange[2], yRange[1] - yRange[2]);
        double a3 = Math.hypot(xRange[3] - xRange[2], yRange[3] - yRange[2]);
        double a4 = Math.hypot(xRange[0] - xRange[3], yRange[0] - yRange[3]);
        //待检测点到四个点的距离
        double b1 = Math.hypot(x - xRange[0], y - yRange[0]);
        double b2 = Math.hypot(x - xRange[1], y - yRange[1]);
        double b3 = Math.hypot(x - xRange[2], y - yRange[2]);
        double b4 = Math.hypot(x - xRange[3], y - yRange[3]);

        double u1 = (a1 + b1 + b2) / 2;
        double u2 = (a2 + b2 + b3) / 2;
        double u3 = (a3 + b3 + b4) / 2;
        double u4 = (a4 + b4 + b1) / 2;

        //矩形的面积
        double s = a1 * a2;
        //海伦公式 计算4个三角形面积
        double ss = Math.sqrt(u1 * (u1 - a1) * (u1 - b1) * (u1 - b2))
                + Math.sqrt(u2 * (u2 - a2) * (u2 - b2) * (u2 - b3))
                + Math.sqrt(u3 * (u3 - a3) * (u3 - b3) * (u3 - b4))
                + Math.sqrt(u4 * (u4 - a4) * (u4 - b4) * (u4 - b1));
        return Math.abs(s - ss) < 0.5;
    }


    /**
     * 触摸是否在某个button范围
     *
     * @param event
     * @param rect
     * @return
     */
    private boolean isInButton(MotionEvent event, Rect rect) {
        int left = rect.left;
        int right = rect.right;
        int top = rect.top;
        int bottom = rect.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    /**
     * 触摸是否在拉伸区域内
     *
     * @param event
     * @return
     */
    private boolean isInResize(MotionEvent event) {
        int left = -20 + this.dst_resize.left;
        int top = -20 + this.dst_resize.top;
        int right = 20 + this.dst_resize.right;
        int bottom = 20 + this.dst_resize.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    private void drawBorder(Canvas canvas) {
        // 创建路径
        path.reset();
        path.moveTo(transformedCorners[0], transformedCorners[1]);
        for (int i = 1; i < 4; i++) {
            int j = i * 2;
            path.lineTo(transformedCorners[j], transformedCorners[j + 1]);
        }
        path.close();

        // 绘制路径
        canvas.drawPath(path, localPaint);
    }

    private Rect dst_delete = new Rect();
    private Rect dst_resize = new Rect();
    private Rect dst_flipV = new Rect();
    private Rect dst_top = new Rect();

    private int deleteBitmapWidth;
    private int deleteBitmapHeight;
    private int resizeBitmapWidth;
    private int resizeBitmapHeight;
    //水平镜像
    private int flipVBitmapWidth;
    private int flipVBitmapHeight;
    //置顶
    private int topBitmapWidth;
    private int topBitmapHeight;

    private void drawButtons(Canvas canvas) {
        //删除在右上角
        dst_delete.left = (int) (transformedCorners[2] - deleteBitmapWidth / 2);
        dst_delete.right = (int) (transformedCorners[2] + deleteBitmapWidth / 2);
        dst_delete.top = (int) (transformedCorners[3] - deleteBitmapHeight / 2);
        dst_delete.bottom = (int) (transformedCorners[3] + deleteBitmapHeight / 2);
        //拉伸等操作在右下角
        dst_resize.left = (int) (transformedCorners[4] - resizeBitmapWidth / 2);
        dst_resize.right = (int) (transformedCorners[4] + resizeBitmapWidth / 2);
        dst_resize.top = (int) (transformedCorners[5] - resizeBitmapHeight / 2);
        dst_resize.bottom = (int) (transformedCorners[5] + resizeBitmapHeight / 2);
        //垂直镜像在左上角
        dst_top.left = (int) (transformedCorners[0] - flipVBitmapWidth / 2);
        dst_top.right = (int) (transformedCorners[0] + flipVBitmapWidth / 2);
        dst_top.top = (int) (transformedCorners[1] - flipVBitmapHeight / 2);
        dst_top.bottom = (int) (transformedCorners[1] + flipVBitmapHeight / 2);
        //水平镜像在左下角
        dst_flipV.left = (int) (transformedCorners[6] - topBitmapWidth / 2);
        dst_flipV.right = (int) (transformedCorners[6] + topBitmapWidth / 2);
        dst_flipV.top = (int) (transformedCorners[7] - topBitmapHeight / 2);
        dst_flipV.bottom = (int) (transformedCorners[7] + topBitmapHeight / 2);

        canvas.drawBitmap(deleteBitmap, null, dst_delete, null);
        canvas.drawBitmap(resizeBitmap, null, dst_resize, null);
        canvas.drawBitmap(flipVBitmap, null, dst_flipV, null);
        canvas.drawBitmap(topBitmap, null, dst_top, null);

    }

    private float[] tmpCorners = new float[8];
    //实测发现边框和内容不对齐，OPENGL用的是左下坐标系，而android用的是左上坐标系，所以需要修正坐标
    private void fixCoordinate() {
        System.arraycopy(transformedCorners, 0, tmpCorners, 0, 8);
        transformedCorners[0] = tmpCorners[6];
        transformedCorners[1] = tmpCorners[7];

        transformedCorners[2] = tmpCorners[4];
        transformedCorners[3] = tmpCorners[5];

        transformedCorners[4] = tmpCorners[2];
        transformedCorners[5] = tmpCorners[3];

        transformedCorners[6] = tmpCorners[0];
        transformedCorners[7] = tmpCorners[1];
    }


}


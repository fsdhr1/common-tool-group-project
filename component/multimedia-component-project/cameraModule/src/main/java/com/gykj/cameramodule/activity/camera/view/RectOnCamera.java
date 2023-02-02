package com.gykj.cameramodule.activity.camera.view;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import com.blankj.utilcode.util.SizeUtils;
import com.gykj.utils.DateUtil;
import com.gykj.utils.OrientationUtil;
import com.gykj.utils.RoundUtil;

/**
 * Created by zy on 2017/2/23.
 */
public class RectOnCamera extends View implements Rotatable, SensorEventListener {

    private static final String TAG = "CameraSurfaceView";
    /**
     *
     */
    private CameraSurfaceView cameraSurfaceView;
    /**
     *
     */
    private Context context;
    /**
     * 屏幕宽
     */
    private int screenWidth;
    /**
     * 屏幕高
     */
    private int screenHeight;
    /**
     * 绘制非文字
     */
    private Paint mPaint;
    /**
     * 绘制文字
     */
    private Paint mPaintText;
    /**
     *
     */
    private RectF mRectF;
    /**
     * 圆
     */
    private Point centerPoint;
    /**
     * 圆的半径
     */
    private int radio;
    /**
     * 角度
     */
    private int orientation = 0;
    /**
     *
     */
    private int lastOrientation = -1;
    /**
     * 方向监听
     */
    private OrientationEventListener orientationEvent;
    /**
     * 文字
     */
    private String str = "正在获取信息;正在获取信息;正在获取信息";
    /**
     * 聚焦的回调接口
     */
    private IAutoFocus mIAutoFocus;


    public RectOnCamera(Context context) {
        this(context, null);
        getScreenMetrix(context);
        initView(context);
    }

    public RectOnCamera(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        getScreenMetrix(context);
        initView(context);
    }

    public RectOnCamera(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getScreenMetrix(context);
        initView(context);
    }

    private void getScreenMetrix(final Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
        orientation = OrientationUtil.getOrientation();
        this.context = context;
        /**
         * 重力监听
         */
        orientationEvent = new OrientationListener(context);
        orientationEvent.enable();
    }

    int panelHigh;
    String[] textArray;
    private int bottomPadding;
    private void initView(Context context) {
        mPaint = new Paint();
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        mPaint.setAntiAlias(true);// 抗锯齿
        mPaint.setDither(true);// 防抖动
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);// 空心
        double fontSize = screenWidth * 0.03;
        panelHigh = (int) (fontSize * 7);
        bottomPadding = SizeUtils.dp2px(115);
        /**
         * 绘制文字
         */
        mPaintText.setTextSize((float) fontSize);
        mPaintText.setTextAlign(Paint.Align.LEFT);
        mPaintText.setTypeface(Typeface.DEFAULT_BOLD);//采用默认的宽度
        mPaintText.setAntiAlias(true);  //抗锯齿
        mPaintText.setStrokeWidth(0);
        mPaintText.setAlpha(100);
        mPaintText.setStyle(Paint.Style.FILL); //空心
        mPaintText.setColor(Color.WHITE);//采用的颜色
        //mPaintText.setShadowLayer(1f, 0f, 3f, Color.LTGRAY);


        int marginLeft = (int) (screenWidth * 0.05);
        int marginTop = (int) (screenHeight * 0.05);
        mRectF = new RectF(marginLeft, marginTop, screenWidth - marginLeft, screenHeight - marginTop);

        centerPoint = new Point(screenWidth / 2, screenHeight / 2);
        radio = (int) (screenWidth * 0.05);
        countTime();
    }

    String dateStr;
    int textX = -1;
    int textY = -1;
    int rotate = -1;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.GREEN);
        canvas.drawCircle(centerPoint.x, centerPoint.y, radio, mPaint);// 外圆
        canvas.drawCircle(centerPoint.x, centerPoint.y, radio - 5, mPaint); // 内圆
        if (!isOrdinary) {
            textArray = str.split(";");
            if (textArray.length < 6) {
                if (orientation == 0) {
                    textX = 50;
                    textY = screenHeight - panelHigh - bottomPadding;
                    rotate = 0;
                }
                if (orientation == 90) {
                    textX = screenWidth - panelHigh;
                    textY = screenHeight - SizeUtils.dp2px(115);;
                    rotate = 270;
                }
                if (orientation == 180) {
                    textX = screenWidth - 50;
                    textY = panelHigh;
                    rotate = 180;
                }
                if (orientation == 270) {
                    textX = panelHigh;
                    textY = 50;
                    rotate = 90;
                }

                canvas.rotate(rotate, textX, textY);
                canvas.drawText("方向：" + cameraOrientation, textX, textY - mPaintText.getTextSize() * 2, mPaintText);
                canvas.drawText(textArray[0], textX, textY, mPaintText);
                canvas.drawText(textArray[1], textX, textY + mPaintText.getTextSize() * 2, mPaintText);
                canvas.drawText(textArray[2], textX, textY + mPaintText.getTextSize() * 4, mPaintText);
                //  dateStr = "时间：" + DateUtil.dateTimeToStr(new Date());
                canvas.drawText(dateStr == null ? "时间：" + DateUtil.dateTimeToStr(new Date()) : dateStr, textX, textY + mPaintText.getTextSize() * 6, mPaintText);
            } else {
                if (orientation == 0) {
                    textX = 50;
                    textY = screenHeight - panelHigh - bottomPadding;
                    rotate = 0;
                }
                if (orientation == 90) {
                    textX = screenWidth - panelHigh;
                    textY = screenHeight - 50;
                    rotate = 270;
                }
                if (orientation == 180) {
                    textX = screenWidth - 50;
                    textY = panelHigh;
                    rotate = 180;
                }
                if (orientation == 270) {
                    textX = panelHigh;
                    textY = 50;
                    rotate = 90;
                }

                canvas.rotate(rotate, textX, textY);
                for (int i = 0; i < textArray.length; i++) {
                    canvas.drawText(textArray[i], textX - 10, textY + mPaintText.getTextSize() * i, mPaintText);
                }
                // dateStr = "拍照时间：" + DateUtil.dateToStr(new Date());
                canvas.drawText(dateStr == null ? "时间：" + DateUtil.dateTimeToStr(new Date()) : dateStr, textX - 10, textY + mPaintText.getTextSize() * 6, mPaintText);
            }


            /*canvas.drawText("方向:" + cameraOrientation, textX, textY, mPaintText);
            String[] textArray = str.split(";");
            canvas.drawText(textArray[0], textX, textY, mPaintText);
            canvas.drawText(textArray[1], textX, textY + mPaintText.getTextSize() * 2, mPaintText);
            canvas.drawText(textArray[2], textX, textY + mPaintText.getTextSize() * 4, mPaintText);
            dateStr = "时间：" + DateUtil.dateTimeToStr(new Date()) + "|" + cameraOrientation;
            canvas.drawText(dateStr, textX, textY + mPaintText.getTextSize() * 6, mPaintText);*/
        }
    }


    public int getJustRotate() {
        if (rotate == -1) {
            return 90;
        }
        return 90 - rotate;
    }

    public int getRotate() {
        return rotate;
    }

    Object[] waterMarks;

    public Object[] getWaterMark() {
        if (str.split(";").length < 6) {
            String desc1 = "方向：" + cameraOrientation + ";" + str + ";" + dateStr;
            Object desc2 = (float) textX / screenWidth;
            Object desc3 = (float) textY / screenHeight;
            //waterMarks = new Object[]{"方向：" + cameraOrientation + ";" + str + ";" + dateStr, (float) textX / screenWidth, (float) textY / screenHeight};
            waterMarks = new Object[]{desc1, desc2, desc3};
            return waterMarks;
        } else {
            String desc1 = str + ";" + dateStr;
            Object desc2 = (float) textX / screenWidth;
            Object desc3 = (float) textY / screenHeight;
            //waterMarks = new Object[]{"方向：" + cameraOrientation + ";" + str + ";" + dateStr, (float) textX / screenWidth, (float) textY / screenHeight};
            waterMarks = new Object[]{desc1, desc2, desc3};
            return waterMarks;
        }
    }


    private double startDis;
    private static final int MODE_INIT = 0;
    private static final int MODE_ZOOM = 1;
    private int mode = MODE_INIT;// 初始状态

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = MODE_INIT;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: //多点触控
                mode = MODE_ZOOM;
                /** 计算两个手指间的距离 */
                startDis = spacing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_ZOOM) {
                    //只有同时触屏两个点的时候才执行
                    if (event.getPointerCount() >= 2) {
                        System.out.println("多根手指移动");
                        double endDis = spacing(event);// 结束距离
                        //每变化10f zoom变1
                        int scale = (int) ((endDis - startDis) / 10f);
                        if (scale >= 1 || scale <= -1) {
                            int zoom = cameraSurfaceView.getZoom() + scale;
                            //zoom不能超出范围
                            if (zoom > cameraSurfaceView.getMaxZoom()) {
                                zoom = cameraSurfaceView.getMaxZoom();
                            }
                            if (zoom < 0) {
                                zoom = 0;
                            }
                            cameraSurfaceView.setZoom(zoom);
                            //将最后一次的距离设为当前距离
                            startDis = endDis;
                        }
                    }
                } else {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    System.out.println("一根手指移动");
                    centerPoint = new Point(x, y);
                    //invalidate();
                    postInvalidate();
                    if (mIAutoFocus != null) {
                        mIAutoFocus.autoFocus();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }


    /**
     * 两点的距离
     */
    private double spacing(MotionEvent event) {
        if (event == null) {
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return Math.sqrt(x * x + y * y);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        orientationEvent.enable();
    }

    /**
     * 方向监听接口
     *
     * @param orientation
     * @param isAnimator
     */
    @Override
    public void setOrientation(int orientation, boolean isAnimator) {
        //invalidate();//view重汇
        postInvalidate();
    }


    public interface IAutoFocus {
        void autoFocus();
    }


    /**
     * 设置预览显示的文字
     *
     * @param text
     */
    public void setWaterMark(String text) {
        this.str = text;
        //invalidate();//view重汇
        postInvalidate();
    }

    public void setIAutoFocus(IAutoFocus mIAutoFocus) {
        this.mIAutoFocus = mIAutoFocus;
    }


    public void setCameraSurfaceView(CameraSurfaceView cameraSurfaceView) {
        this.cameraSurfaceView = cameraSurfaceView;
    }

    private class OrientationListener extends OrientationEventListener {

        public OrientationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int o) {
            orientation = RoundUtil.roundOrientation(o, orientation);
            if (orientation != lastOrientation) {
                setOrientation(orientation, false);
                lastOrientation = orientation;
            }
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //System.out.println("handleMessage日期监听");

            dateStr = "时间：" + DateUtil.dateTimeToStr(new Date());
            postInvalidate();
        }
    };

    /**
     * 事件重绘，每隔一秒重绘一次，用来 模拟 事件变换
     */
    private Timer timer;
    private TimerTask task;
    private int delay = 0;//执行延迟
    private int interval = 1000;//执行间隔

    private void countTime() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                handler.obtainMessage(0).sendToTarget();
            }
        };
        timer.schedule(task, delay, interval);

    }

    private boolean isOrdinary;

    public void setIsOrdinary(boolean isOrdinary) {
        this.isOrdinary = isOrdinary;
    }


    private SensorManager mSensorManager;
    // 加速度传感器
    private Sensor accelerometer;
    // 地磁场传感器
    private Sensor magnetic;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    public void onCreate() {
        // 实例化传感器管理者
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // 初始化加速度传感器
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void onResume() {
        // 注册监听
        mSensorManager.registerListener(this, accelerometer, Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, magnetic, Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void onPause() {
        // 解除注册
        mSensorManager.unregisterListener(this);
        timer.cancel();
    }

    /**
     *
     */
    boolean listenDirection = true;

    public void listenDirection(boolean listenDirection) {
        this.listenDirection = listenDirection;
    }

    /**
     * 方向监听
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values;
        }
        computePhoneGesture();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private String cameraOrientation = "未知方向";

    /**
     * 计算手机姿态
     */
    double azimuth;
    double pitch;
    double roll;

    private void computePhoneGesture() {
        if (!listenDirection) return;
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        azimuth = Math.toDegrees(values[0]);
        pitch = Math.toDegrees(values[1]);
        roll = Math.toDegrees(values[2]);

        if ((pitch > -45 && pitch < 45) && ((roll > -45 && roll < 45) || roll < -135 || roll > 135)) {
            if (!cameraOrientation.equals("未知方向")) {
                cameraOrientation = "未知方向";
            }
            return;
        }

        if ((pitch > -45 && pitch < 45) && (roll >= 45 && roll <= 135)) {
            String temp = calculateCameraOrientation(azimuth - 90 > -180 ? (azimuth - 90) : (azimuth + 270));
            if (!cameraOrientation.equals(temp)) {
                cameraOrientation = temp;
            }
            return;
        }
        if ((pitch > -45 && pitch < 45) && (roll >= -135 && roll <= -45)) {
            String temp = calculateCameraOrientation(azimuth + 90 < 180 ? (azimuth + 90) : (270 - azimuth));
            if (!cameraOrientation.equals(temp)) {
                cameraOrientation = temp;
            }
            return;
        }
        if ((pitch > -90 && pitch < -45)) {
            //屏幕向上 机头方向就是摄像头方向
            if (roll > -90 && roll < 90) {
                String temp = calculateCameraOrientation(azimuth);
                if (!cameraOrientation.equals(temp)) {
                    cameraOrientation = temp;
                }
            }
            //屏幕向下
            if (roll < -90 || roll > 90) {
                String temp = null;
                if (azimuth > -180 && azimuth < -90) {
                    temp = calculateCameraOrientation(azimuth + 180);
                }
                if (azimuth > 90 && azimuth < 180) {
                    temp = calculateCameraOrientation(azimuth - 180);
                }
                if (azimuth > 0 && azimuth < 90) {
                    temp = calculateCameraOrientation(azimuth - 180);
                }
                if (azimuth > -90 && azimuth < 0) {
                    temp = calculateCameraOrientation(azimuth + 180);
                }
                if (!cameraOrientation.equals(temp)) {
                    cameraOrientation = temp;
                }
            }
            return;
        }
        //机头向下
        if ((pitch > 45 && pitch < 90)) {
            //屏幕向上
            if (roll > -90 && roll < 90) {
                String temp = null;
                if (azimuth > -180 && azimuth < -90) {
                    temp = calculateCameraOrientation(azimuth + 180);
                }
                if (azimuth > 90 && azimuth < 180) {
                    temp = calculateCameraOrientation(azimuth - 180);
                }
                if (azimuth > 0 && azimuth < 90) {
                    temp = calculateCameraOrientation(azimuth - 180);
                }
                if (azimuth > -90 && azimuth < 0) {
                    temp = calculateCameraOrientation(azimuth + 180);
                }
                if (!cameraOrientation.equals(temp)) {
                    cameraOrientation = temp;
                }
            }
            //屏幕向下
            if (roll < -90 || roll > 90) {
                String temp = calculateCameraOrientation(azimuth);
                if (!cameraOrientation.equals(temp)) {
                    cameraOrientation = temp;
                }
            }
            return;
        }
    }

    private static int currentCameraType = 2;// 当前打开的摄像头标记
    public static final int FRONT = 1;// 前置摄像头标记
    public static final int BACK = 2;// 后置摄像头标记

    public static void setCurrentCameraType(int type) {
        currentCameraType = type;
    }

    // 计算方向(手机机头指向位置)
    private String calculateCameraOrientation(double azimuth) {
        String orientation = null;
        // 前置摄像头
        if (currentCameraType == FRONT) {
            if (azimuth >= -5 && azimuth < 5) {
                orientation = "正南";
            } else if (azimuth >= 5 && azimuth < 85) {
                orientation = "西南";
            } else if (azimuth >= 85 && azimuth <= 95) {
                orientation = "正西";
            } else if (azimuth >= 95 && azimuth < 175) {
                orientation = "西北";
            } else if ((azimuth >= 175 && azimuth <= 180)
                    || azimuth >= -180 && azimuth < -175) {
                orientation = "北";
            } else if (azimuth >= -175 && azimuth < -95) {
                orientation = "东北";
            } else if (azimuth >= -95 && azimuth < -85) {
                orientation = "正东";
            } else if (azimuth >= -85 && azimuth < -5) {
                orientation = "东南";
            } else {
                orientation = "无法获取方向";
            }
        } else {// 后置摄像头
            if (azimuth >= -5 && azimuth < 5) {
                orientation = "北";
            } else if (azimuth >= 5 && azimuth < 85) {
                orientation = "东北";
            } else if (azimuth >= 85 && azimuth <= 95) {
                orientation = "正东";
            } else if (azimuth >= 95 && azimuth < 175) {
                orientation = "东南";
            } else if ((azimuth >= 175 && azimuth <= 180)
                    || azimuth >= -180 && azimuth < -175) {
                orientation = "正南";
            } else if (azimuth >= -175 && azimuth < -95) {
                orientation = "西南";
            } else if (azimuth >= -95 && azimuth < -85) {
                orientation = "正西";
            } else if (azimuth >= -85 && azimuth < -5) {
                orientation = "西北";
            } else {
                orientation = "无法获取方向";
            }
        }
        return orientation;
    }

    /**
     * 返回拍照方向
     *
     * @return
     */
    public String getOrientation() {
        return cameraOrientation;
    }
}

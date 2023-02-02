package com.gradtech.mapframev10.core.marker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.gradtech.mapframev10.R;
import com.gradtech.mapframev10.core.maps.GMapView;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.ScreenCoordinate;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.gestures.GesturesPluginImpl;
import com.mapbox.maps.plugin.gestures.OnMoveListener;


import static android.content.Context.SENSOR_SERVICE;

/**
 * @ClassName LocView
 * @Description TODO 定位组件
 * @Author: fs
 * @Date: 2021/5/28 16:01
 * @Version 2.0
 */
@SuppressLint("ViewConstructor")
public final class LocView extends FrameLayout implements SensorEventListener, OnMoveListener {

    private SensorManager sensorManager;

    private Bitmap directorBitmap;

    private Bitmap bitmap;

    private Bitmap rotateBitmap;

    private int iconRes = -1;
    /**
     * 屏幕坐标
     */
    private float x;
    /**
     * 屏幕坐标
     */
    private float y;
    /**
     * 旋转
     */
    private float rotate;
    /**
     * 画笔
     */
    private Paint paint;
    private Paint paint1;

    private boolean obtainPosition = false;

    private GMapView gMapView;

    private Double lat;
    private Double lon;

    private float[] gravity = new float[3];
    private float[] geomagnetic= new float[3];


    public LocView(GMapView gMapView) {
        super(gMapView.getContext());
        this.gMapView = gMapView;
        init();
    }

    private void init() {
        x = 0;
        y = 0;
        rotate = 0;

        paint = new Paint();
        /**
         * 方向传感器
         */
        sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);//获得管理
       /* sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME
        );
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
*/
        Sensor magneticSensor = null;
        Sensor accelerometerSensor = null;
        if (sensorManager != null) { // 初始化两个传感器
            // getDefaultSensor:获取Sensor,使用给定的类型和唤醒属性返回传感器。
            magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (magneticSensor != null) {
            assert sensorManager != null;
            sensorManager.registerListener(this, magneticSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }

        if (accelerometerSensor != null) {
            assert sensorManager != null;
            sensorManager.registerListener(this, accelerometerSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }

        setWillNotDraw(false);
        /**
         * 地图监听
         */
        if (gMapView != null && gMapView.getMapboxMap() != null) {
            GesturesPluginImpl plugin = gMapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
            plugin.addOnMoveListener(this);
        }
        if (iconRes == -1) {
            iconRes = R.mipmap.gmmfc_location_icon;
        }
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getContext().getResources(), iconRes);
        }

    }

    @Override
    public void destroyDrawingCache() {
        super.destroyDrawingCache();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        gMapView = null;
        bitmap.recycle();
        rotateBitmap.recycle();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (!obtainPosition) {
            return;
        }

        if (rotateBitmap.isRecycled()) {
            return;
        }
        //设置canvas画布背景为白色
        canvas.drawColor(Color.TRANSPARENT);
        int w = rotateBitmap.getWidth();
        int h = rotateBitmap.getHeight();
        //在画布上绘制旋转后的位图·
        //canvas.drawBitmap(directorBitmap, x - directorBitmap.getWidth() / 2, y - directorBitmap.getHeight() / 2, paint);
        paint1 = new Paint();
        paint1.setColor(Color.parseColor("#337bd5"));
        paint1.setAlpha(60);
        paint1.setAntiAlias(true);
        double metersPerPixel = gMapView.getMapboxMap().getMetersPerPixelAtLatitude(lat);
        canvas.drawCircle(x, y, 30 / (float) metersPerPixel, paint1);
        canvas.drawBitmap(rotateBitmap, x - w / 2f, y - h / 2f, paint);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
       // this.rotate = event.values[0];

        // SensorEvent:保存精度(accuracy)、传感器类型(sensor)、时间戳(timestamp)
        // 以及不同传感器(Sensor)具有的不同传感器数组(values)。

        // TYPE_MAGNETIC_FIELD:描述磁场传感器类型的常量。
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
           // Log.e(TAG, "onSensorChanged 得到磁场传感器: " + Arrays.toString(geomagnetic));
        }

        // TYPE_ACCELEROMETER:描述加速度传感器类型的常量。
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
           // Log.e(TAG, "onSensorChanged 得到加速度传感器: " + Arrays.toString(gravity));
        }
        getValue();

        draw();
    }
    public void getValue() {
        //初始化数组
        float[] values = new float[3]; // 用来保存手机的旋转弧度
        float[] r = new float[9]; // 被填充的旋转矩阵

        // 传入gravity和geomagnetic，通过计算它们得到旋转矩阵R。
        // 而第二个参数倾斜矩阵I是用于将磁场数据转换进实际的重力坐标系中的，一般默认设置为NULL即可。
        SensorManager.getRotationMatrix(r, null, gravity, geomagnetic);

        // 根据旋转矩阵R计算设备的方向，将结果存储在values中。
        // values[0]记录着手机围绕 Z 轴的旋转弧度，
        // values[1]记录着手机围绕 X 轴的旋转弧度，
        // values[2]记录着手机围绕 Y 轴的旋转弧度。
        SensorManager.getOrientation(r, values);

        // 旋转弧度转为角度
        float pitch = (float) Math.toDegrees(values[0]);
        float pitchx = (float) Math.toDegrees(values[1]);
        rotate = pitch;
      //  Configuration.ORIENTATION_LANDSCAPE
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public void onMoveBegin(@NonNull MoveGestureDetector detector) {
        draw();
    }

    @Override
    public boolean onMove(@NonNull MoveGestureDetector detector) {
        draw();
        return false;
    }

    @Override
    public void onMoveEnd(@NonNull MoveGestureDetector detector) {
        draw();
    }

    private void draw() {
        if (lat == null || lon == null) {
            Toast.makeText(getContext(), "未获取到位置", Toast.LENGTH_SHORT).show();
            return;
        }
        obtainPosition = true;
        Point point =  Point.fromLngLat(lon,lat);
        ScreenCoordinate screenCoordinate = gMapView.getMapboxMap().pixelForCoordinate(point);
        x = (float)screenCoordinate.getX();
        y = (float)screenCoordinate.getY();
        /**
         *
         */
        Matrix matrix = new Matrix();
        //if (directorBitmap == null)
        //    directorBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_nav_director);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        //缩放原图
        matrix.reset();
        matrix.postScale(1f, 1f);

        int rotation = ((Activity)getContext()).getWindowManager().getDefaultDisplay()
                .getRotation();
        Log.i("方位角",rotation+"");
        if (rotation == Surface.ROTATION_0) {
            rotation = 0;
            matrix.postRotate(rotate, w / 2f, h / 2f);
        } else if (rotation == Surface.ROTATION_90) {
            rotation = 90;
            matrix.postRotate(rotate+90, w / 2f, h / 2f);
        } else if (rotation == Surface.ROTATION_180) {
            rotation = 180;
            matrix.postRotate(rotate+180, w / 2f, h / 2f);
        } else if (rotation == Surface.ROTATION_270) {
            matrix.postRotate(rotate+270, w / 2f, h / 2f);
        }

       // matrix.postRotate(rotate, w / 2f, h / 2f);


        //matrix.postRotate(rotate, 200, 200);
        rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        postInvalidate();
    }

    /**
     * 设置定位点
     *
     * @param latitude  纬度
     * @param longitude 经度
     */
    public void setLocation(Double latitude, Double longitude) {
        lat = latitude;
        lon = longitude;

        draw();
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }


}

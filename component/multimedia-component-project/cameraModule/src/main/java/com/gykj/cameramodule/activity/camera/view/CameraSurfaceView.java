package com.gykj.cameramodule.activity.camera.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zy on 2017/2/23.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback, ICameraOperation {

    private static final String TAG = "CameraSurfaceView";

    private Context mContext;
    private SurfaceHolder holder;
    private Camera mCamera;

    private int mScreenWidth;
    private int mScreenHeight;

    /**
     * 拍照结果回调
     */
    private IPhotoCallBack iPhotoCallBack;


    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getScreenMetrix(context);
        initView();
    }

    private void getScreenMetrix(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;
    }

    private void initView() {
        holder = getHolder();//获得surfaceHolder引用
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置类型
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        if (mCamera == null) {
            try {
                mCamera = Camera.open();//开启相机
                mCamera.setPreviewDisplay(holder);//摄像头画面显示在Surface上
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (flashListener!=null){
            flashListener.hasFlash(hasFlash());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
        //设置参数并开始预览
        setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        mCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        mCamera.stopPreview();//停止预览
        mCamera.release();//释放相机资源
        mCamera = null;
        holder = null;
    }

    @Override
    public void onAutoFocus(boolean success, Camera Camera) {
        if (success) {
            Log.i(TAG, "onAutoFocus success=" + success);
        }
    }

    // 拍照瞬间调用
    private Camera.ShutterCallback shutter = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.i(TAG, "shutter");
        }
    };

    // 获得没有压缩过的图片数据
    private Camera.PictureCallback raw = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {
            Log.i(TAG, "raw");

        }
    };

    //创建jpeg图片回调数据对象
    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {
            try {
                if (iPhotoCallBack != null) {
                    iPhotoCallBack.getPhotoResult(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    mCamera.stopPreview();// 关闭预览
                    mCamera.startPreview();// 开启预览
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public Camera getCamera() {
        return mCamera;
    }

    private int currentCameraType = 2;// 当前打开的摄像头标记
    public static final int FRONT = 1;// 前置摄像头标记
    public static final int BACK = 2;// 后置摄像头标记

    /**
     * 相机反转
     */
    public void changeCamera() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();// 停止预览
                mCamera.release();// 释放相机资源
                if (currentCameraType == FRONT) {// 当前前置切换成后置
                    mCamera = openCamera(BACK);
                    RectOnCamera.setCurrentCameraType(BACK);
                } else if (currentCameraType == BACK) {// 当前后置切换成前置
                    mCamera = openCamera(FRONT);
                    RectOnCamera.setCurrentCameraType(FRONT);
                }
                mCamera.setPreviewDisplay(getHolder());
                // 设置参数并开始预览
                setCameraParams(mCamera, mScreenWidth, mScreenHeight);// 相机反转
                mCamera.startPreview();// 开启预览
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Camera openCamera(int type) {
        int frontIndex = -1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras(); // get cameras number
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontIndex = cameraIndex;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backIndex = cameraIndex;
            }
        }

        currentCameraType = type;
        if (type == FRONT && frontIndex != -1) {
            return Camera.open(frontIndex);
        } else if (type == BACK && backIndex != -1) {
            return Camera.open(backIndex);
        }
        return null;
    }

    // 自动对焦
    public void setAutoFocus() {
        if (mCamera != null)
            mCamera.autoFocus(this);
    }

    public void takePicture() {
        //设置参数,并拍照
        setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        // 当调用camera.takePiture方法后，camera关闭了预览，这时需要调用startPreview()来重新开启预览
        try {
            mCamera.takePicture(null, null, jpeg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasFlash() {
        if (mCamera == null) {
            return false;
        }

        Camera.Parameters parameters;
        try {
            parameters = mCamera.getParameters();
        } catch (RuntimeException ignored)  {
            return false;
        }

        if (parameters.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }

        return true;
    }

    public void openLightOn() {
        if (null == mCamera) {
            mCamera = Camera.open();
        }
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        mCamera.setParameters(parameters);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
            }
        });
        mCamera.startPreview();
    }

    public void closeLightOff() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();// 停止预览
                mCamera.release();// 释放相机资源
                mCamera = openCamera(BACK);
                RectOnCamera.setCurrentCameraType(BACK);
                mCamera.setPreviewDisplay(getHolder());
                // 设置参数并开始预览
                setCameraParams(mCamera, mScreenWidth, mScreenHeight);// 相机反转
                mCamera.startPreview();// 开启预览
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void setCameraParams(Camera camera, int width, int height) {
        Log.i(TAG, "setCameraParams  width=" + width + "  height=" + height);
        Camera.Parameters parameters = mCamera.getParameters();
        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        Camera.Size picSize = getProperBestSize(pictureSizeList, ((float) height / width));
        if (null == picSize) {
            //Log.i(TAG, "null == picSize");
            picSize = parameters.getPictureSize();
        }
        float w = picSize.width;
        float h = picSize.height;
        Log.i(TAG, "picSize:w = " + w + "h = " + h);
        // 设置照片的大小
       /* if (currentCameraType == FRONT) {
            parameters.setPictureSize(pictureSizeList.get(0).width, pictureSizeList.get(0).height);
        } else {
            Log.i("PictureSize", "PictureSize:w = " + w + "h = " + h);
            parameters.setPictureSize((int) w, (int) h);
        }*/
        parameters.setPictureSize((int) w, (int) h);
        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Camera.Size preSize = getProperSize(previewSizeList, picSize, ((float) height) / width);
        if (null != preSize) {
            // 设置预览照片的大小
            parameters.setPreviewSize(preSize.width, preSize.height);
        }
        // 根据选出的PictureSize重新设置SurfaceView大小
        this.setLayoutParams(new FrameLayout.LayoutParams(width, height));
        parameters.setJpegQuality(100); // 设置照片质量
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
        }
        if (currentCameraType == FRONT) {
            // 设置JPG照片的质量
            parameters.set("jpeg-quality", 100);
        }
        mCamera.cancelAutoFocus();//自动对焦。
        mCamera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        mCamera.setParameters(parameters);
    }

    public int getCurrentCameraType() {
        return currentCameraType;
    }


    /**
     * 从列表中选取合适的分辨率(选择最高清效果最好的比例)
     * 默认w:h = 4:3
     * <p>注意：参数hight 和 width 对应关系已经转变
     * <p/>
     */
    private Camera.Size getPicturSize(List<Camera.Size> pictureSizeList, int hight, int width) {

        //匹配屏幕比例的摄像头集合
        Map<Integer, Camera.Size> recordSize = new HashMap<>();

        for (Camera.Size size : pictureSizeList) {
            if (size.width == hight && size.height == width) {
                return size;
            }
            /*两个分数进行通分*/
            int lcm = LCM(size.height, width);
            int h = (lcm / size.height) * size.width;
            int h1 = (lcm / width) * hight;
            if (h == h1) {
                recordSize.put(lcm, size);
            }
        }
        //所有符合比例得相机挑选最接近的
        if (recordSize.size() > 0) {
            int max = 0;
            //循环获取最大公倍数
            for (Integer index : recordSize.keySet()) {
                max = max > index ? max : index;
            }
            //初始化最大公倍数，获取最小公倍数
            for (Integer index : recordSize.keySet()) {
                max = max > index ? index : max;
            }
            return recordSize.get(max);
        }

        for (Camera.Size size : pictureSizeList) {
            float curRatio = ((float) size.width) / size.height;
            // 默认w:h = 4:3
            if (curRatio == 4f / 3) {
                return size;
            }
        }

        return null;
    }

    /**
     * @param a 参数a
     * @param b 参数b
     * @return int 返回两个参数的最小公倍数
     * @throws
     * @explain LCM方法: 求出参数a和参数b的最小公倍数
     * @author 叶清逸
     * @date 2018年7月28日 下午11:11:36
     */
    public static int LCM(int a, int b) {
        /*排序保证a始终小于b*/
        if (a > b) {
            int t = a;
            a = b;
            b = t;
        }
        /*先求出最大公约数*/
        int c = a;
        int d = b;
        int gcd = 0;
        while (c % d != 0) {
            //k保存余数
            int k = c % d;
            //除数变为c
            c = d;
            //被除数变为余数
            d = k;
        }
        /*辗转相除结束后的c即为所求的最大公约数*/
        gcd = d;

        /*使用公式算出最小公倍数*/
        int lcm = a * b / gcd;

        return lcm;
    }


    /**
     * 从列表中选取合适的分辨率(选择最高清效果最好的比例)
     * 默认w:h = 16:9
     * <p>注意：这里的w对应屏幕的height
     * h对应屏幕的width<p/>
     */
    private Camera.Size getProperBestSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        List<Camera.Size> list = new ArrayList<>();
        for (int i = pictureSizeList.size() - 1; i >= 0; i--) {
            Camera.Size size = pictureSizeList.get(i);
            if (mScreenWidth == size.width && mScreenHeight == size.height) {
                list.add(size);
                break;
            }
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                list.add(size);
            }
        }

        if (list.size() == 0) {
            for (int i = pictureSizeList.size() - 1; i >= 0; i--) {
                Camera.Size size = pictureSizeList.get(i);
                if (equalRate(size, 1.7777f)) {
                    list.add(size);
                }
            }
        }
        return list.size() == 0 ? pictureSizeList.get(0) : list.get(list.size() / 3);
    }

    /**
     * 从列表中选取合适的分辨率
     * 默认w:h = 4:3
     * <p>注意：这里的w对应屏幕的height
     * h对应屏幕的width<p/>
     */
    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, Camera.Size picSize, float screenRatio) {
        Log.i(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = pictureSizeList.get(0);
        List<Camera.Size> list = new ArrayList<>();
        for (Camera.Size size : pictureSizeList) {
            if (picSize.width == size.width && picSize.height == size.height) {
                result = size;
                break;
            }
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                list.add(size);
            }
        }
        if (list.size() == 0) {
            for (int i = pictureSizeList.size() - 1; i >= 0; i--) {
                Camera.Size size = pictureSizeList.get(i);
                if (equalRate(size, 1.7777f)) {
                    list.add(size);
                }
            }
            result = list.get(list.size() / 3);
        }
        return result;
    }

    private boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.2) {
            return true;
        } else {
            return false;
        }
    }

    public interface IPhotoCallBack {
        public void getPhotoResult(byte[] data);
    }

    public void setIPhotoCallBack(IPhotoCallBack iPhotoCallBack) {
        this.iPhotoCallBack = iPhotoCallBack;
    }


    /**
     * 一些相机的操作
     */
    @Override
    public void switchCamera() {

    }

    @Override
    public void switchFlashMode() {

    }

    @Override
    public boolean takePhoto() {
        return false;
    }

    @Override
    public int getMaxZoom() {
        if (mCamera == null) return -1;
        Camera.Parameters parameters = mCamera.getParameters();
        if (!parameters.isZoomSupported()) return -1;
        return parameters.getMaxZoom() > 40 ? 40 : parameters.getMaxZoom();
    }

    private int zoom;

    @Override
    public void setZoom(int zoom) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setZoom(zoom);
        mCamera.setParameters(parameters);
        this.zoom = zoom;
    }

    @Override
    public int getZoom() {
        return zoom;
    }

    @Override
    public void releaseCamera() {

    }
    private FlashListener flashListener;
    public void setFlashListener(FlashListener listener){
        flashListener = listener;
    }

    public interface FlashListener{
        void hasFlash(Boolean isHasFlash);
    }
}

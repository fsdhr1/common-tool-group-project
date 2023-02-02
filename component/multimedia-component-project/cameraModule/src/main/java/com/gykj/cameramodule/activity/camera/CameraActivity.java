package com.gykj.cameramodule.activity.camera;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.cazaea.sweetalert.SweetAlertDialog;
import com.gykj.base.activity.BaseActivity;
import com.gykj.base.photoview.PhotoView;
import com.gykj.cameramodule.R;
import com.gykj.cameramodule.activity.camera.view.CameraSurfaceView;
import com.gykj.cameramodule.activity.camera.view.RectOnCamera;
import com.gykj.cameramodule.activity.image.ImageViewPager;
import com.gykj.cameramodule.bean.ImagePagerBean;
import com.gykj.cameramodule.bean.PhotoFile;
import com.gykj.location.LocationClient;
import com.gykj.location.LocationType;
import com.gykj.location.ServiceLocation;
import com.gykj.utils.BitmapUtil;
import com.gykj.utils.FileUtil;
import com.gykj.utils.TPoolUtil;

import static com.gykj.cameramodule.activity.camera.view.CameraSurfaceView.FRONT;

public class CameraActivity extends BaseActivity
        implements View.OnClickListener,
        RectOnCamera.IAutoFocus,
        CameraSurfaceView.IPhotoCallBack {

    private int OPERATION_FLAG = CANCEL;// 什么操作都未执行
    private final static int SAVE_SUCCESS = 1000;// 图片保存成功
    private final static int SAVE_FAIL = 1001;
    private final static int TAKE_SUCCESS = 1002;
    private final static int ENVIRONMENT_ERROR = 1003;
    private final static int TAKE_ERROR = 1004;
    // 回调结果
    public final static int SUCCESS = 2001;
    public final static int FAIL = 2002;
    public final static int CANCEL = 2003;

    private int rotate;
    private ServiceLocation _serviceLocation;
    /**
     * 定位权限
     */
    String[] MANIFEST_STORAGE = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.VIBRATE
    };

    private CameraSurfaceView mCameraSurfaceView;
    private RectOnCamera mRectOnCamera;
    /**
     * 拍照按钮
     */
    private ImageButton takePicBtn;
    /**
     * 相机反转
     */
    private ImageButton reverseCamera;
    /**
     * 图片结果查看
     */
    private PhotoView pvRes;
    /**
     * 相机拍照页面
     */
    private RelativeLayout rlCamera;
    /**
     * 拍照按钮
     */
    private LinearLayout rlRes;
    /**
     * 确认选择当前图片
     */
    private ImageView ivImgSelect;
    /**
     * 取消选择当前图片
     */
    private ImageView ivImgCancel,ivLinghtTogle,ivThumbnail;
    private RelativeLayout rlLinghtTogle;
    /**
     * 文件存储的路径
     */
    private String filePath;

    /**
     * 村名称
     */
    private String areaName = "未知村";
    /**
     * 村代码
     */
    private String areaCode;
    /**
     * 是否为普通拍照模式
     */
    private boolean isOrdinary = true;
    /**
     * 批处理
     */
    private boolean isBatch = false;
    /**
     * 拓展名
     */
    private String extendName;
    /**
     * 水印信息
     */
    private String waterMarkerText;
    /**
     * 获取区划全称的地址如果不传就用这个默认的
     */
    private String areaNameUrl = "https://api.agribigdata.com.cn/agribigdata-server-base/api/v1/dicts/level/wkt";

    private SweetAlertDialog sweetAlertDialog;

    private boolean isColose =true;
    /**
     * 当前的Bitmap
     */
    private Bitmap bitmap;
    private Object[] waterMarks;
    private ArrayList<String> imgPath = new ArrayList<>();
    private ArrayList<PhotoFile> photoFiles = new ArrayList<>();

    // 定位权限 文件夹读写权限 相机权限
    protected String[] MANIFEST_PERMISSION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE,
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera_activity);

        /**
         * 公交车 注册
         */
        EventBus.getDefault().register(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 请求权限
            requestPermissions(MANIFEST_PERMISSION, 1000);
        }

        mCameraSurfaceView = findViewById(R.id.cameraSurfaceView);
        mRectOnCamera = findViewById(R.id.rectOnCamera);
        mRectOnCamera.onCreate();
        takePicBtn = findViewById(R.id.takePic);
        reverseCamera = findViewById(R.id.reverseCamera);
        pvRes = findViewById(R.id.pvRes);
        pvRes.enable();
        rlCamera = findViewById(R.id.rlCamera);
        rlRes = findViewById(R.id.rlRes);
        ivImgSelect = findViewById(R.id.ivImgSelect);
        ivImgCancel = findViewById(R.id.ivImgCancel);
        ivLinghtTogle = findViewById(R.id.iv_open_or_close_linght);
        rlLinghtTogle = findViewById(R.id.rl_light_toggle);
        ivThumbnail = findViewById(R.id.ivThumbnail);

        ivThumbnail.setOnClickListener(this);
        ivLinghtTogle.setOnClickListener(this);
        ivImgSelect.setOnClickListener(this);
        ivImgCancel.setOnClickListener(this);
        mCameraSurfaceView.setIPhotoCallBack(this);// 拍照结果回调监听
        mRectOnCamera.setIAutoFocus(this);
        takePicBtn.setOnClickListener(this);
        reverseCamera.setOnClickListener(this);

        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);

        Intent intent = getIntent();
        if (intent != null) {
            filePath = intent.getStringExtra("filePath");
            areaName = intent.getStringExtra("areaName");
            areaCode = intent.getStringExtra("areaCode");
            isOrdinary = intent.getBooleanExtra("isOrdinary", true);
            isBatch = intent.getBooleanExtra("isBatch", false);
            extendName = intent.getStringExtra("extendName");
            waterMarkerText = intent.getStringExtra("waterMarkerText");
            areaNameUrl = intent.hasExtra("areaNameUrl") ? intent.getStringExtra("areaNameUrl") : areaNameUrl;
        }
        mRectOnCamera.setIsOrdinary(isOrdinary);
        mRectOnCamera.setCameraSurfaceView(mCameraSurfaceView);

        initialize();
        registerEvent();
        initClass();
        mCameraSurfaceView.setFlashListener(new CameraSurfaceView.FlashListener() {
            @Override
            public void hasFlash(Boolean isHasFlash) {
                if (isHasFlash){
                    rlLinghtTogle.setVisibility(View.VISIBLE);
                }else {
                    rlLinghtTogle.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void initialize() {

    }

    /**
     * 权限许可确认
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        /**
         * 启动定位服务
         * Context var1
         * boolean var2  是否启动百度定位
         * boolean var3  是否常驻
         */
        if (!isOrdinary) {
            LocationClient.getInstance().startLocation(this, LocationType.COMMON, null);
        }

    }

    /**
     * 监听位置
     *
     * @param serviceLocation
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ServiceLocation serviceLocation) {
        this._serviceLocation = serviceLocation;
        if (_serviceLocation == null || _serviceLocation.getLatitude() == null || _serviceLocation.getLongitude() == null)
            return;
        if (areaName == null) areaName = getIntent().getStringExtra("areaName");
        String text = "北纬：" + String.format("%.6f", _serviceLocation.getLatitude()) + ";" + "东经：" + String.format("%.6f", _serviceLocation.getLongitude()) + ";" + "地点：" + areaName;
        if (waterMarkerText != null) {
            text = waterMarkerText + ";" + text;
        }
        mRectOnCamera.setWaterMark(text);
    }

    @Override
    protected void registerEvent() {

    }

    @Override
    protected void initClass() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }

    private void destroy() {
        // 回收资源
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        /**
         * 公交车 取消注册
         */
        EventBus.getDefault().unregister(this);
        // 停止定位服务
        LocationClient.getInstance().stopLocation(this,LocationType.COMMON);

        System.gc();
    }


    Handler handler = new NoWeakHandler(this);

    private static class NoWeakHandler extends Handler {
        private WeakReference<CameraActivity> mWeakReference;

        NoWeakHandler(CameraActivity cameraActivity) {
            mWeakReference = new WeakReference<>(cameraActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            CameraActivity cameraActivity = mWeakReference.get();
            if (cameraActivity == null) return;
            int what = msg.what;
            cameraActivity.OPERATION_FLAG = what;
            switch (what) {
                case SAVE_SUCCESS:
                    if (!cameraActivity.isBatch) {
                        if (cameraActivity.sweetAlertDialog!=null){
                            cameraActivity.sweetAlertDialog.dismiss();
                        }
                        cameraActivity.finish();
                    } else {
                        cameraActivity.sweetAlertDialog.dismiss();
                        cameraActivity.rlRes.setVisibility(View.GONE);
                        cameraActivity.rlCamera.setVisibility(View.VISIBLE);
                        cameraActivity.imgPath.add(cameraActivity.filePath);
                        String desc = cameraActivity.waterMarks == null ? "" : cameraActivity.waterMarks[0].toString();
                        if (cameraActivity.waterMarkerText != null) {
                            desc = desc + ";方向：" + cameraActivity.mRectOnCamera.getOrientation();
                        }
                        PhotoFile photoFile = new PhotoFile("现场照片", cameraActivity.filePath, desc);
                        cameraActivity.photoFiles.add(photoFile);
                        cameraActivity.ivThumbnail.setImageURI(Uri.parse(cameraActivity.filePath));
                    }
                    break;
                case SAVE_FAIL:
                    cameraActivity.sweetAlertDialog.setCancelText("保存失败").changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    break;
                case TAKE_SUCCESS:
                    cameraActivity.sweetAlertDialog.dismiss();
                    cameraActivity.bitmap = (Bitmap) msg.obj;
                    cameraActivity.rlRes.setVisibility(View.VISIBLE);
                    cameraActivity.rlCamera.setVisibility(View.GONE);
                    cameraActivity.pvRes.setImageBitmap(Bitmap.createBitmap(cameraActivity.bitmap));
                    break;
                case ENVIRONMENT_ERROR:
                    Toast.makeText(cameraActivity, "没有检测到内存卡", Toast.LENGTH_SHORT).show();
                    break;
                case TAKE_ERROR:
                    Toast.makeText(cameraActivity, "未能获取照片,请重拍！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.takePic) {
            sweetAlertDialog.setTitleText("处理中...").show();
            mCameraSurfaceView.takePicture();
        }
        if (v.getId() == R.id.reverseCamera) {
            if (checkCamera()) {
                // 相机反转
                mCameraSurfaceView.changeCamera();
                if (mCameraSurfaceView.getCurrentCameraType()==FRONT){
                    isColose = true;
                    ivLinghtTogle.setImageResource(R.mipmap.camera_module_icon_open_linght);
                }
            } else {
                Toast.makeText(this, "没有检测到前置摄像头", Toast.LENGTH_SHORT).show();
            }
        }
        if (v.getId() == R.id.ivImgSelect) {
            sweetAlertDialog.setTitleText("保存中...").show();
            doSomeBackGround();
        }
        if (v.getId() == R.id.ivImgCancel) {
            rlRes.setVisibility(View.GONE);
            rlCamera.setVisibility(View.VISIBLE);
        }
        if (v.getId() == R.id.iv_open_or_close_linght){
            if (mCameraSurfaceView.getCurrentCameraType()==FRONT){
                return;
            }
            if (isColose){
                isColose =false;
                ivLinghtTogle.setImageResource(R.mipmap.camera_module_icon_close_linght);
                mCameraSurfaceView.openLightOn();
            }else {
                isColose =true;
                ivLinghtTogle.setImageResource(R.mipmap.camera_module_icon_open_linght);
                mCameraSurfaceView.closeLightOff();
            }
        }

        if (v.getId()==R.id.ivThumbnail){
            if (imgPath!=null&&imgPath.size()>0){
                ArrayList<ImagePagerBean> imagePagerBeans = new ArrayList<>();
                for (String item : imgPath) {
                    String path_img =  item;
                    ImagePagerBean imagePagerBean = new ImagePagerBean(path_img, "预览", path_img);
                    imagePagerBeans.add(imagePagerBean);
                }
                if (imagePagerBeans.size() > 0) {
                    ImageViewPager.start(this, imagePagerBeans, imagePagerBeans.size()-1);
                }
            }else {
                ToastUtils.showShort("请先拍摄照片");
            }
        }
    }



    /**
     * 检查设备是否有前置摄像头
     *
     * @return
     */
    private boolean checkCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    /**
     * 图片保存
     */
    private void doSomeBackGround() {
        TPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                if (isBatch) {
                    if (filePath == null) {
                        filePath = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/" + System.currentTimeMillis() + ".jpg";
                    } else {
                        String picName;
                        if (areaCode != null) {
                            picName = System.currentTimeMillis() + "_" + areaCode + "_" + (extendName == null ? "" : extendName) + ".jpg";
                        } else {
                            picName = System.currentTimeMillis() + "_" + (extendName == null ? "" : extendName) + ".jpg";
                        }
                        if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg")) {
                            filePath = FileUtil.getParentPathByPath(filePath) + File.separator + picName;
                        } else {
                            if (filePath.endsWith("/")) {
                                filePath = filePath + picName;
                            } else {
                                filePath = filePath + File.separator + picName;
                            }

                        }
                    }
                } else {
                    if (filePath == null) {
                        filePath = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/" + System.currentTimeMillis() + ".jpg";
                    } else {
                        String picName;
                        if (areaCode != null) {
                            picName = System.currentTimeMillis() + "_" + areaCode + "_" + (extendName == null ? "" : extendName) + ".jpg";
                        } else {
                            picName = System.currentTimeMillis() + "_" + (extendName == null ? "" : extendName) + ".jpg";
                        }
                        if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg")) {
                            filePath = FileUtil.getParentPathByPath(filePath) + File.separator + picName;
                        } else {
                            if (filePath.endsWith("/")) {
                                filePath = filePath + picName;
                            } else {
                                filePath = filePath + File.separator + picName;
                            }
                        }
                    }
                }
                boolean b = BitmapUtil.bitmap2File(bitmap, filePath, 80);
                if (b) {
                    handler.obtainMessage(SAVE_SUCCESS).sendToTarget();//保存成功
                } else {
                    handler.obtainMessage(SAVE_FAIL).sendToTarget();
                }
            }
        });
    }

    @Override
    public void autoFocus() {
        mCameraSurfaceView.setAutoFocus();
    }


    /**
     * 拍照结果回调(图片旋转并保存)
     *
     * @param data
     */
    //Bitmap bm;
    @Override
    public void getPhotoResult(final byte[] data) {

        TPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                int len = Math.max(opts.outHeight, opts.outWidth);
                int sampleSize = Math.round((float) len / 1000);
                if (sampleSize <= 0) sampleSize = 1;
                opts.inSampleSize = sampleSize;
                opts.inJustDecodeBounds = false;
                // 获得图片
                Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    rotate = mRectOnCamera.getJustRotate();
                    if (mCameraSurfaceView.getCurrentCameraType() != FRONT) {
                        //照片转正
                        tempBitmap = BitmapUtil.rotatingBitmap(tempBitmap, rotate);//bitmap旋转，防止照片倒立
                    } else {
                        int rotate = mRectOnCamera.getRotate();
                        if (rotate != 90) {
                            tempBitmap = BitmapUtil.rotatingBitmap(tempBitmap, rotate - 90);//bitmap旋转，防止照片倒立
                        }
                        // 镜像翻转功能
                        tempBitmap = BitmapUtil.convertBitmap(tempBitmap);
                    }
                    double parcent = percentage(SizeUtils.dp2px(80),ScreenUtils.getScreenHeight());
                    int height = multiplyDouble(parcent,tempBitmap.getHeight());
                    tempBitmap =ImageUtils.clip(tempBitmap,0,0, tempBitmap.getWidth(),tempBitmap.getHeight()-height,true);
                    if (!isOrdinary) {
                        //获取水印信息
                        waterMarks = mRectOnCamera.getWaterMark();
                        bitmap = BitmapUtil.addMark2Bitmap(tempBitmap, waterMarks);//图片加水印
                    } else {
                        bitmap = tempBitmap;
                    }
                     //int height =SizeUtils.dp2px(80)-(ScreenUtils.getScreenHeight()-bitmap.getHeight());

                    if (bitmap == null) {
                        handler.obtainMessage(TAKE_ERROR).sendToTarget();
                    } else {
                        handler.obtainMessage(TAKE_SUCCESS, bitmap).sendToTarget();
                    }
                } else {
                    handler.obtainMessage(ENVIRONMENT_ERROR).sendToTarget();
                }
            }
        });
    }

    public int multiplyDouble(Double a,int b){
        BigDecimal d1 =  BigDecimal.valueOf(a);
        BigDecimal d2 =  BigDecimal.valueOf(b);
        return d1.multiply(d2).intValue();
    }

    public double percentage(int fz ,int fm){
       BigDecimal a = BigDecimal.valueOf(fz);
       BigDecimal b = BigDecimal.valueOf(fm);
       return a.divide(b,2, RoundingMode.UP).doubleValue();
    }
    public Double toDouble(Object value,Double defaultValue){
        if (null==value){
            return defaultValue;
        }
        return Double.parseDouble(value.toString());
    }
    /**
     *
     */
    @Override
    public void finish() {
        Intent it = new Intent();
        switch (OPERATION_FLAG) {
            case CANCEL:
                setResult(CANCEL, it);
                break;
            case SAVE_SUCCESS:
                if (!isBatch) {
                    it.putExtra("path", filePath);
                    String desc = waterMarks == null ? "" : waterMarks[0].toString();
                    if (waterMarkerText != null) {
                        it.putExtra("desc", desc + ";方向：" + mRectOnCamera.getOrientation());
                    } else {
                        it.putExtra("desc", desc);
                    }
                    setResult(SUCCESS, it);
                } else {
                   /* imgPath.add(filePath);
                    photoFile photoFile = new photoFile("现场照片", filePath, waterMarks == null ? "" : waterMarks[0].toString());
                    photoFiles.add(photoFile);*/
                }
                break;
            case SAVE_FAIL:
                setResult(FAIL, it);
                break;
        }
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (isBatch) {
            Intent it = new Intent();
            it.putExtra("isBatch", isBatch);
            it.putStringArrayListExtra("path", imgPath);
            it.putExtra("pathPhotoFile", photoFiles);
            String desc = waterMarks == null ? "" : waterMarks[0].toString();
            if (waterMarkerText != null) {
                it.putExtra("desc", desc + ";方向：" + mRectOnCamera.getOrientation());
            } else {
                it.putExtra("desc", desc);
            }
            setResult(SUCCESS, it);
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRectOnCamera.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            destroy();
        }
        mRectOnCamera.onPause();
    }



}

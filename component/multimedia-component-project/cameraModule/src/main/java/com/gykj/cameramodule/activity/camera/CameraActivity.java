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

    private int OPERATION_FLAG = CANCEL;// ????????????????????????
    private final static int SAVE_SUCCESS = 1000;// ??????????????????
    private final static int SAVE_FAIL = 1001;
    private final static int TAKE_SUCCESS = 1002;
    private final static int ENVIRONMENT_ERROR = 1003;
    private final static int TAKE_ERROR = 1004;
    // ????????????
    public final static int SUCCESS = 2001;
    public final static int FAIL = 2002;
    public final static int CANCEL = 2003;

    private int rotate;
    private ServiceLocation _serviceLocation;
    /**
     * ????????????
     */
    String[] MANIFEST_STORAGE = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.VIBRATE
    };

    private CameraSurfaceView mCameraSurfaceView;
    private RectOnCamera mRectOnCamera;
    /**
     * ????????????
     */
    private ImageButton takePicBtn;
    /**
     * ????????????
     */
    private ImageButton reverseCamera;
    /**
     * ??????????????????
     */
    private PhotoView pvRes;
    /**
     * ??????????????????
     */
    private RelativeLayout rlCamera;
    /**
     * ????????????
     */
    private LinearLayout rlRes;
    /**
     * ????????????????????????
     */
    private ImageView ivImgSelect;
    /**
     * ????????????????????????
     */
    private ImageView ivImgCancel,ivLinghtTogle,ivThumbnail;
    private RelativeLayout rlLinghtTogle;
    /**
     * ?????????????????????
     */
    private String filePath;

    /**
     * ?????????
     */
    private String areaName = "?????????";
    /**
     * ?????????
     */
    private String areaCode;
    /**
     * ???????????????????????????
     */
    private boolean isOrdinary = true;
    /**
     * ?????????
     */
    private boolean isBatch = false;
    /**
     * ?????????
     */
    private String extendName;
    /**
     * ????????????
     */
    private String waterMarkerText;
    /**
     * ????????????????????????????????????????????????????????????
     */
    private String areaNameUrl = "https://api.agribigdata.com.cn/agribigdata-server-base/api/v1/dicts/level/wkt";

    private SweetAlertDialog sweetAlertDialog;

    private boolean isColose =true;
    /**
     * ?????????Bitmap
     */
    private Bitmap bitmap;
    private Object[] waterMarks;
    private ArrayList<String> imgPath = new ArrayList<>();
    private ArrayList<PhotoFile> photoFiles = new ArrayList<>();

    // ???????????? ????????????????????? ????????????
    protected String[] MANIFEST_PERMISSION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE,
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // ????????????
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera_activity);

        /**
         * ????????? ??????
         */
        EventBus.getDefault().register(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // ????????????
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
        mCameraSurfaceView.setIPhotoCallBack(this);// ????????????????????????
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
     * ??????????????????
     *
     * @param requestCode  ?????????
     * @param permissions  ??????
     * @param grantResults ??????
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        /**
         * ??????????????????
         * Context var1
         * boolean var2  ????????????????????????
         * boolean var3  ????????????
         */
        if (!isOrdinary) {
            LocationClient.getInstance().startLocation(this, LocationType.COMMON, null);
        }

    }

    /**
     * ????????????
     *
     * @param serviceLocation
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ServiceLocation serviceLocation) {
        this._serviceLocation = serviceLocation;
        if (_serviceLocation == null || _serviceLocation.getLatitude() == null || _serviceLocation.getLongitude() == null)
            return;
        if (areaName == null) areaName = getIntent().getStringExtra("areaName");
        String text = "?????????" + String.format("%.6f", _serviceLocation.getLatitude()) + ";" + "?????????" + String.format("%.6f", _serviceLocation.getLongitude()) + ";" + "?????????" + areaName;
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
        // ????????????
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        /**
         * ????????? ????????????
         */
        EventBus.getDefault().unregister(this);
        // ??????????????????
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
                            desc = desc + ";?????????" + cameraActivity.mRectOnCamera.getOrientation();
                        }
                        PhotoFile photoFile = new PhotoFile("????????????", cameraActivity.filePath, desc);
                        cameraActivity.photoFiles.add(photoFile);
                        cameraActivity.ivThumbnail.setImageURI(Uri.parse(cameraActivity.filePath));
                    }
                    break;
                case SAVE_FAIL:
                    cameraActivity.sweetAlertDialog.setCancelText("????????????").changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    break;
                case TAKE_SUCCESS:
                    cameraActivity.sweetAlertDialog.dismiss();
                    cameraActivity.bitmap = (Bitmap) msg.obj;
                    cameraActivity.rlRes.setVisibility(View.VISIBLE);
                    cameraActivity.rlCamera.setVisibility(View.GONE);
                    cameraActivity.pvRes.setImageBitmap(Bitmap.createBitmap(cameraActivity.bitmap));
                    break;
                case ENVIRONMENT_ERROR:
                    Toast.makeText(cameraActivity, "????????????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case TAKE_ERROR:
                    Toast.makeText(cameraActivity, "??????????????????,????????????", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.takePic) {
            sweetAlertDialog.setTitleText("?????????...").show();
            mCameraSurfaceView.takePicture();
        }
        if (v.getId() == R.id.reverseCamera) {
            if (checkCamera()) {
                // ????????????
                mCameraSurfaceView.changeCamera();
                if (mCameraSurfaceView.getCurrentCameraType()==FRONT){
                    isColose = true;
                    ivLinghtTogle.setImageResource(R.mipmap.camera_module_icon_open_linght);
                }
            } else {
                Toast.makeText(this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
            }
        }
        if (v.getId() == R.id.ivImgSelect) {
            sweetAlertDialog.setTitleText("?????????...").show();
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
                    ImagePagerBean imagePagerBean = new ImagePagerBean(path_img, "??????", path_img);
                    imagePagerBeans.add(imagePagerBean);
                }
                if (imagePagerBeans.size() > 0) {
                    ImageViewPager.start(this, imagePagerBeans, imagePagerBeans.size()-1);
                }
            }else {
                ToastUtils.showShort("??????????????????");
            }
        }
    }



    /**
     * ????????????????????????????????????
     *
     * @return
     */
    private boolean checkCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    /**
     * ????????????
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
                    handler.obtainMessage(SAVE_SUCCESS).sendToTarget();//????????????
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
     * ??????????????????(?????????????????????)
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
                // ????????????
                Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    rotate = mRectOnCamera.getJustRotate();
                    if (mCameraSurfaceView.getCurrentCameraType() != FRONT) {
                        //????????????
                        tempBitmap = BitmapUtil.rotatingBitmap(tempBitmap, rotate);//bitmap???????????????????????????
                    } else {
                        int rotate = mRectOnCamera.getRotate();
                        if (rotate != 90) {
                            tempBitmap = BitmapUtil.rotatingBitmap(tempBitmap, rotate - 90);//bitmap???????????????????????????
                        }
                        // ??????????????????
                        tempBitmap = BitmapUtil.convertBitmap(tempBitmap);
                    }
                    double parcent = percentage(SizeUtils.dp2px(80),ScreenUtils.getScreenHeight());
                    int height = multiplyDouble(parcent,tempBitmap.getHeight());
                    tempBitmap =ImageUtils.clip(tempBitmap,0,0, tempBitmap.getWidth(),tempBitmap.getHeight()-height,true);
                    if (!isOrdinary) {
                        //??????????????????
                        waterMarks = mRectOnCamera.getWaterMark();
                        bitmap = BitmapUtil.addMark2Bitmap(tempBitmap, waterMarks);//???????????????
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
                        it.putExtra("desc", desc + ";?????????" + mRectOnCamera.getOrientation());
                    } else {
                        it.putExtra("desc", desc);
                    }
                    setResult(SUCCESS, it);
                } else {
                   /* imgPath.add(filePath);
                    photoFile photoFile = new photoFile("????????????", filePath, waterMarks == null ? "" : waterMarks[0].toString());
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
                it.putExtra("desc", desc + ";?????????" + mRectOnCamera.getOrientation());
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

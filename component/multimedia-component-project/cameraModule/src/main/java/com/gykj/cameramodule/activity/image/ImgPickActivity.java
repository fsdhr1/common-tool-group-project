package com.gykj.cameramodule.activity.image;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cazaea.sweetalert.SweetAlertDialog;
import com.gykj.base.adapter.BaseRecycleAdapter;
import com.gykj.cameramodule.R;
import com.gykj.cameramodule.activity.camera.CameraActivity;
import com.gykj.cameramodule.adapter.ImgPickAdapter;
import com.gykj.cameramodule.bean.ImagePagerBean;
import com.gykj.cameramodule.bean.ImgFolderBean;
import com.gykj.location.LocationClient;
import com.gykj.location.LocationType;
import com.gykj.location.ServiceLocation;
import com.gykj.utils.BitmapUtil;
import com.gykj.utils.DateUtil;
import com.gykj.utils.EasyPermissions;
import com.gykj.utils.FileUtil;
import com.gykj.utils.NetUtil;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.gykj.cameramodule.activity.camera.CameraActivity.SUCCESS;

/**
 * Description: ??????
 * Created by jyh
 * DateTime: 2021-07-19 13:28
 * package: com.gykj.cameramodule.activity.image
 */
public class ImgPickActivity extends RxAppCompatActivity
        implements SensorEventListener,
        View.OnClickListener,
        BaseRecycleAdapter.IViewClickListener,
        BaseRecycleAdapter.ILongViewClickListener,
        PopupWindow.OnDismissListener,
        EasyPermissions.PermissionCallbacks {

    Uri _imgUri;
    ContentResolver _contentResolver;
    Cursor _cursor;
    HashSet<String> _dirPaths;// ??????????????? ????????????????????????????????????
    List<ImgFolderBean> _listImgFolders;// ????????????????????????????????????
    int _picsSize;// ?????????????????????????????????
    ImgFolderBean _imgFolderBean;// ??????????????????????????????
    File _imgDir;
    List<String> _imgNames;// ?????????????????????

    private LinearLayout ll_title;
    private ImageView iv_back;
    private RecyclerView _rvImgPicker;
    private TextView _tvChooseDir, _tvPreview;

    private ImgPickAdapter _imgPickAdapter;
    private ImgDirPopWindow imgDirPopWindow;

    // ???????????? ????????????????????? ????????????
    protected String[] MANIFEST_PERMISSION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE,
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    // ????????????????????????????????????????????????????????????
    private String areaNameUrl = "https://api.agribigdata.com.cn/agribigdata-server-base/api/v1/dicts/level/wkt";
    private boolean mIsOrdinary;// ????????????????????????????????? ????????????true?????????????????????
    private boolean isAddMark = false;// ??????????????????
    private String mFirstPath;
    private String mBasePath;
    private String mExtendName;// ?????????
    private String mAreaName;
    private String mAreaCode;
    private String waterMarkerText;
    private boolean isHideTitle = false;// ?????????????????????
    private String backgroundColorTitle;// ??????????????????

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ???????????????????????????????????????
//        if (getSupportActionBar() != null)
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.camera_img_pick_aty_layout);

        /**
         * ????????? ??????
         */
        EventBus.getDefault().register(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // ????????????
            requestPermissions(MANIFEST_PERMISSION, 1000);
        }

        initialize();
        registerEvent();
        initClass();
    }

    @Override
    public void onBackPressed() {
        confirm();
    }

    /****************************************PermissionCallbacks****************************************/
    // ???????????????????????????????????????
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    // ??????????????????????????????
    @Override
    public void onPermissionsAllGranted() {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        finish();
    }

    /****************************************PermissionCallbacks****************************************/

    protected void initialize() {
        ll_title = findViewById(R.id.ll_title);
        iv_back = findViewById(R.id.iv_back);
        _rvImgPicker = findViewById(R.id.rvImgPicker);
        _tvChooseDir = findViewById(R.id.tvChooseDir);
        _tvPreview = findViewById(R.id.tvPreview);

        areaNameUrl = getIntent().hasExtra("areaNameUrl") ? getIntent().getStringExtra("areaNameUrl") : areaNameUrl;
        mIsOrdinary = getIntent().getBooleanExtra("isOrdinary", true);
        mFirstPath = getIntent().getStringExtra("firstPath");
        mBasePath = getIntent().getStringExtra("basePath");
        mExtendName = getIntent().getStringExtra("extendName");
        mAreaName = getIntent().getStringExtra("areaName");
        mAreaCode = getIntent().getStringExtra("areaCode");
        waterMarkerText = getIntent().getStringExtra("waterMarkerText");
        isHideTitle = getIntent().getBooleanExtra("isHideTitle", false);
        backgroundColorTitle = getIntent().getStringExtra("backgroundColorTitle");
        if (isHideTitle) {
            ll_title.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(backgroundColorTitle)) {
            int color = Color.parseColor(backgroundColorTitle);
            ll_title.setBackgroundColor(color);
        }
    }

    protected void registerEvent() {
        iv_back.setOnClickListener(this);
        _tvChooseDir.setOnClickListener(this);
        _tvPreview.setOnClickListener(this);
    }

    protected void initClass() {
        imgDirPopWindow = new ImgDirPopWindow(this);
        imgDirPopWindow.getPopupWindow().setAnimationStyle(R.style.camera_module_PopupAnimBottom);
        imgDirPopWindow.setOnDismissListener(this);
        imgDirPopWindow.setiViewClickListener(this);

        _rvImgPicker.setLayoutManager(new GridLayoutManager(this, 3));
        _imgPickAdapter = new ImgPickAdapter(_rvImgPicker);
        _imgPickAdapter.setIViewClickListener(this);
        _imgPickAdapter.setILongViewClickListener(this);
        _rvImgPicker.setAdapter(_imgPickAdapter);

        // ??????????????????
        initSensor();

        scanSystemImg();
    }

    /**
     * ??????????????????????????????
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
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
        if (!mIsOrdinary) {
            LocationClient.getInstance().startLocation(this, LocationType.COMMON, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * ????????? ????????????
         */
        EventBus.getDefault().unregister(this);
        // ??????????????????
        LocationClient.getInstance().stopLocation(this,LocationType.COMMON);
    }

    private void confirm() {
        List<String> list = _imgPickAdapter.getPickedItem();
        if (list == null || list.size() == 0) {
            finish();
            return;
        }
        if (mIsOrdinary) {// ???????????????????????????
            finish();
            return;
        }
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitleText("??????")
                .setContentText("??????????????????????????????????")
                .setCancelText("???")
                .setConfirmText("???")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        isAddMark = true;
                        finish();
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        sweetAlertDialog.show();
    }

    private void scanSystemImg() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                if (_listImgFolders == null) {
                    scanImgDir();
                }
                emitter.onNext(1);
                if (_imgFolderBean != null) {
                    getImgFromFolder(_imgFolderBean);
                }
                emitter.onNext(2);
            }
        })
                .subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    public void onNext(Integer i) {
                        if (i == 1) {
                            System.out.println("???????????????????????????");
                            imgDirPopWindow.setImgFolderBeans(_listImgFolders);
                        }
                        if (i == 2) {
                            if (_imgFolderBean != null)
                                _imgPickAdapter.setImgParentPath(_imgFolderBean.getDir());
                            List<String> temp = new ArrayList<>();
                            if (_imgNames != null) temp.addAll(_imgNames);
                            temp.add(0, "CAMERA");
                            _imgPickAdapter.replaceAll(temp);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void scanImgDir() {
        _imgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        _listImgFolders = new ArrayList<>();
        _dirPaths = new HashSet<>();
        _contentResolver = getContentResolver();
        // ?????????jpeg???png?????????
        _cursor = _contentResolver.query(_imgUri, null, MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?", new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_TAKEN + " desc");

        String firstImage = null;   // ?????????uri?????????????????????????????????
        while (_cursor.moveToNext()) { // ?????????????????????
            String path = _cursor.getString(_cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if (firstImage == null) {// ??????????????????????????????
                firstImage = path;
            }
            File parentFile = new File(path).getParentFile(); // ??????????????????????????????
            if (parentFile == null) {
                continue;
            }
            String dirPath = parentFile.getAbsolutePath();
            ImgFolderBean imageFolder = null;
            if (_dirPaths.contains(dirPath)) {  // ????????????HashSet????????????????????????????????????????????????????????????????????????????????????????????????~~???
                continue;
            } else {
                _dirPaths.add(dirPath);
                imageFolder = new ImgFolderBean();// ?????????imageFolder
                imageFolder.setDir(dirPath);
                imageFolder.setFirstImagePath(path);
            }
            System.out.println(11);

            int picSize = 0;
            try {
                picSize = parentFile.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        if (filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".jpeg")) {
                            return true;
                        }
                        return false;
                    }
                }).length;
            } catch (Exception e) {
                e.getMessage();
            }

            imageFolder.setCount(picSize);
            _listImgFolders.add(imageFolder);
            if (picSize > _picsSize) {
                _picsSize = picSize;
                _imgDir = parentFile;
                _imgFolderBean = imageFolder;
            }
        }
        _cursor.close();
        // ????????????????????????HashSet???????????????????????????
        _dirPaths.clear();
        _dirPaths = null;

        // ?????????????????????????????????????????????????????????????????????????????????????????????
        if (mFirstPath != null && !mFirstPath.equals("")) {
            List<Object> list = FileUtil.getFileNameInFolder(mFirstPath, ".jpg", false);
            if (list != null && list.size() > 0) {
                _picsSize = list.size();
                _imgDir = new File(mFirstPath);
                ImgFolderBean imageFolder = new ImgFolderBean();
                imageFolder.setDir(mFirstPath);
                imageFolder.setFirstImagePath(list.get(0).toString());
                _imgFolderBean = imageFolder;
            }
        }
    }


    private void getImgFromFolder(ImgFolderBean imgFolderBean) {
        String parentPath = imgFolderBean.getDir();
        _imgDir = new File(parentPath);// ????????????????????????
        // ????????????????????????????????????????????????
        File[] files = _imgDir.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return -1;
                else if (diff == 0)
                    return 0;
                else
                    return 1;
            }

            public boolean equals(Object obj) {
                return true;
            }
        });
        List<String> v = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().toLowerCase().endsWith(".jpg") || files[i].getName().toLowerCase().endsWith(".png") || files[i].getName().toLowerCase().endsWith(".jpeg")) {
                v.add(files[i].getName());
            }
        }
        if (v.size() > 0)
            _imgNames = Arrays.asList(v.toArray(new String[v.size()]));

    }

    /**
     * ????????????
     */
    private void openImageViewPager() {
        List<String> temp = _imgPickAdapter.getPickedItem();
        if (temp == null) return;
        String path = _imgPickAdapter.getParentPath();
        if (path == null) return;
        ArrayList<ImagePagerBean> imagePagerBeans = new ArrayList<>();
        for (String item : temp) {
            String path_img = path + "/" + item;
            ImagePagerBean imagePagerBean = new ImagePagerBean(path_img, "??????", path_img);
            imagePagerBeans.add(imagePagerBean);
        }
        if (imagePagerBeans.size() > 0) {
            ImageViewPager.start(this, imagePagerBeans, 0);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvChooseDir) {
            imgDirPopWindow.getPopupWindow().showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }
        if (v.getId() == R.id.tvPreview) {
            getImgFromFolder(_imgFolderBean);
            openImageViewPager();
        }
        // ????????????
        if (v.getId() == R.id.iv_back) {
            onBackPressed();
        }
    }

    @Override
    public void onClick(View view, Object data, int position) {
        if (view.getId() == R.id.ivCamera) {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("isBatch", getIntent().getBooleanExtra("isBatch", true));// ?????????
            intent.putExtra("extendName", mExtendName);
            intent.putExtra("filePath", getIntent().getStringExtra("filePath"));
            intent.putExtra("areaName", mAreaName);
            intent.putExtra("areaCode", mAreaCode);
            intent.putExtra("isOrdinary", mIsOrdinary);
            if (waterMarkerText != null) {
                intent.putExtra("waterMarkerText", waterMarkerText);
            }
            intent.putExtra("areaNameUrl", areaNameUrl);
            startActivityForResult(intent, 100);
        }
        if (view.getId() == R.id.llParent) {
            _imgFolderBean = (ImgFolderBean) data;
            scanSystemImg();
            imgDirPopWindow.getPopupWindow().dismiss();
        }
    }

    @Override
    public void onLongViewClick(View view, final Object data, final int position) {

    }

    private String dj;
    private String bw;
    private String dateStr;

    @Override
    public void finish() {
        List<String> list = _imgPickAdapter.getPickedItem();

        dj = "???????????????";
        bw = "???????????????";
        if (_serviceLocation != null && _serviceLocation.getLatitude() != null && _serviceLocation.getLongitude() != null) {
            dj = String.format("%.6f", _serviceLocation.getLongitude());
            bw = String.format("%.6f", _serviceLocation.getLatitude());
        }
        if (areaName == null) areaName = mAreaName;

        if (!NetUtil.hasInternet(this)) {
            if (waterMark == null) {
                waterMark = "?????????" + cameraOrientation + ";" + "?????????" + bw + ";" + "?????????" + dj + ";" + "?????????" + areaName + ";?????????" + DateUtil.dateTimeToStr(new Date());
            }
            addMarkerAndfinish(list);
            return;
        }

        dateStr = "?????????" + DateUtil.dateTimeToStr(new Date());
        waterMark = "?????????" + cameraOrientation + ";" + "?????????" + bw + ";" + "?????????" + dj + ";" + "?????????" + areaName + ";" + dateStr;
        addMarkerAndfinish(list);
    }

    private void addMarkerAndfinish(List<String> temp) {
        Intent it = new Intent();
        if (temp == null) {
            it.putExtra("flag", "nothing");
            setResult(SUCCESS, it);
            // ??????????????????
            mSensorManager.unregisterListener(this);
            super.finish();
            return;
        }

        ArrayList<String> temp1 = new ArrayList<>();
        String path = _imgPickAdapter.getParentPath();
        String filePath;
        for (String p : temp) {
            if (mAreaCode != null) {
                filePath = mBasePath + System.currentTimeMillis() + "_" + mAreaCode + "_" + mExtendName + ".jpg";
            } else {
                filePath = mBasePath + System.currentTimeMillis() + "_" + mExtendName + ".jpg";
            }
            Bitmap bitmap = BitmapUtil.getBitMBitmap(path + "/" + p);
            if (isAddMark) {
                Bitmap markBitmap;
                if (waterMarkerText != null) {
                    if (_serviceLocation == null) {
                        waterMark = waterMarkerText.replace("location", "????????????????????????");
                    } else {
                        String wz = String.format("%.6f", _serviceLocation.getLongitude()) + "," + String.format("%.6f", _serviceLocation.getLatitude());
                        waterMark = waterMarkerText.replace("location", wz);
                    }
                    markBitmap = BitmapUtil.addMark2Bitmap(bitmap, waterMark + ";" + dateStr);
                } else {
                    markBitmap = BitmapUtil.addMark2Bitmap(bitmap, waterMark);
                }
                boolean b = BitmapUtil.bitmap2File(markBitmap, filePath, 70);
                if (b) {
                    temp1.add(filePath);
                }
                if (!markBitmap.isRecycled()) {
                    markBitmap.recycle();
                }
            } else {
                boolean b = BitmapUtil.bitmap2File(bitmap, filePath, 70);
                if (b) {
                    temp1.add(filePath);
                }
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }

        }
        System.out.println(temp);
        it.putExtra("path", path);
        it.putStringArrayListExtra("items", temp1);
        it.putExtra("flag", "pickImg");
        if (waterMarkerText != null) {
            it.putExtra("desc", waterMark + ";" + dateStr + ";?????????" + cameraOrientation);
        } else {
            it.putExtra("desc", waterMark);
        }

        if (_imgFolderBean != null) it.putExtra("albumpath", _imgFolderBean.getDir());
        setResult(SUCCESS, it);
        // ??????????????????
        mSensorManager.unregisterListener(ImgPickActivity.this);
        ImgPickActivity.super.finish();
    }

    @Override
    public void onDismiss() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (data == null) return;
            boolean isBatch = data.getBooleanExtra("isBatch", false);
            if (resultCode == SUCCESS) {
                if (!isBatch) {
                    Intent it = new Intent();
                    it.putExtra("path", data.getStringExtra("path"));
                    it.putExtra("desc", data.getStringExtra("desc"));
                    it.putExtra("flag", "camera");
                    setResult(SUCCESS, it);
                    super.finish();
                } else {
                    Intent it = new Intent();
                    it.putStringArrayListExtra("path", data.getStringArrayListExtra("path"));
                    it.putExtra("pathPhotoFile", data.getSerializableExtra("pathPhotoFile"));
                    it.putExtra("desc", data.getStringExtra("desc"));
                    it.putExtra("flag", "camera");
                    setResult(SUCCESS, it);
                    super.finish();
                }
            }
        }

    }

    /**
     * ????????????
     *
     * @param serviceLocation
     */
    ServiceLocation _serviceLocation;
    private String waterMark;
    private String areaName;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ServiceLocation serviceLocation) {
        this._serviceLocation = serviceLocation;
        if (_serviceLocation == null) return;
        if (areaName == null) areaName = mAreaName;
        waterMark = "?????????" + cameraOrientation + ";" + "?????????" + String.format("%.6f", _serviceLocation.getLatitude()) + ";"
                + "?????????" + String.format("%.6f", _serviceLocation.getLongitude()) + ";" + "?????????"
                + areaName + ";?????????" + DateUtil.dateTimeToStr(new Date());
        if (waterMarkerText != null) {
            waterMark = waterMarkerText + ";" + waterMark;
        }
    }


    private SensorManager mSensorManager;
    // ??????????????????
    private Sensor accelerometer;
    // ??????????????????
    private Sensor magnetic;

    /**
     * ??????????????????
     */
    private void initSensor() {
        // ??????????????????????????????????????????????????????????????????
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // ???????????????????????????
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // ???????????????????????????
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // ??????????????????????????????
        mSensorManager.registerListener(this, accelerometer, Sensor.TYPE_ACCELEROMETER);
        // ???????????????????????????
        mSensorManager.registerListener(this, magnetic, Sensor.TYPE_MAGNETIC_FIELD);
    }

    /******************************??????SensorEventListener ????????????******************************/

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    /**
     * ????????????
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {// ??????????????????
            accelerometerValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {// ???????????????
            magneticFieldValues = event.values;
        }
        // ??????????????????????????????
        computePhoneGesture();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /******************************??????SensorEventListener ????????????******************************/

    private String cameraOrientation = "????????????";

    /**
     * ??????????????????
     */
    double azimuth;// ?????????
    double pitch;
    double roll;

    private void computePhoneGesture() {
        // ????????????getOrientation()??????????????????????????????????????????
        float[] values = new float[3];
        // ??????rotationMatrix?????????
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        azimuth = Math.toDegrees(values[0]);
        pitch = Math.toDegrees(values[1]);
        roll = Math.toDegrees(values[2]);

        if ((pitch > -45 && pitch < 45) && ((roll > -45 && roll < 45) || roll < -135 || roll > 135)) {
            if (!cameraOrientation.equals("????????????")) {
                cameraOrientation = "????????????";
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
            // ???????????? ?????????????????????????????????
            if (roll > -90 && roll < 90) {
                String temp = calculateCameraOrientation(azimuth);
                if (!cameraOrientation.equals(temp)) {
                    cameraOrientation = temp;
                }
            }
            // ????????????
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
        // ????????????
        if ((pitch > 45 && pitch < 90)) {
            // ????????????
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
            // ????????????
            if (roll < -90 || roll > 90) {
                String temp = calculateCameraOrientation(azimuth);
                if (!cameraOrientation.equals(temp)) {
                    cameraOrientation = temp;
                }
            }
            return;
        }
    }

    /**
     * ????????????(????????????????????????)
     *
     * @param azimuth ?????????
     * @return
     */
    private String calculateCameraOrientation(double azimuth) {
        String orientation = null;
        if (azimuth >= -5 && azimuth < 5) {
            System.out.println("???");
            orientation = "???";
        } else if (azimuth >= 5 && azimuth < 85) {
            System.out.println("??????");
            orientation = "??????";
        } else if (azimuth >= 85 && azimuth <= 95) {
            System.out.println("??????");
            orientation = "??????";
        } else if (azimuth >= 95 && azimuth < 175) {
            System.out.println("??????");
            orientation = "??????";
        } else if ((azimuth >= 175 && azimuth <= 180)
                || azimuth >= -180 && azimuth < -175) {
            System.out.println("??????");
            orientation = "??????";
        } else if (azimuth >= -175 && azimuth < -95) {
            System.out.println("??????");
            orientation = "??????";
        } else if (azimuth >= -95 && azimuth < -85) {
            System.out.println("??????");
            orientation = "??????";
        } else if (azimuth >= -85 && azimuth < -5) {
            System.out.println("??????");
            orientation = "??????";
        } else {
            orientation = "??????????????????";
        }
        return orientation;
    }
}

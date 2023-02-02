package com.gykj.grandphotos.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.gykj.grandphotos.GrandPhotoPickers;
import com.gykj.grandphotos.R;
import com.gykj.grandphotos.constant.GrandCode;
import com.gykj.grandphotos.result.Result;
import com.gykj.grandphotos.setting.Setting;
import com.gykj.grandphotos.ui.adapter.AlbumItemsAdapter;
import com.gykj.grandphotos.ui.dialog.LoadingDialog;
import com.gykj.grandphotos.ui.widget.PressedTextView;
import com.gykj.grandphotos.utils.Color.ColorUtils;
import com.gykj.grandphotos.utils.String.StringUtils;
import com.gykj.grandphotos.utils.bitmap.SaveBitmapCallBack;
import com.gykj.grandphotos.utils.media.MediaScannerConnectionUtils;
import com.gykj.grandphotos.utils.permission.PermissionUtil;
import com.gykj.grandphotos.utils.settings.SettingsUtils;
import com.gykj.grandphotos.utils.system.SystemUtils;
import com.gykj.grandphotos.config.GrandPhotoHelper;
import com.gykj.grandphotos.constant.Key;
import com.gykj.grandphotos.models.album.AlbumModel;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;
import com.gykj.grandphotos.ui.adapter.PhotosAdapter;
import com.gykj.grandphotos.utils.bitmap.BitmapUtils;
import com.gykj.grandphotos.utils.file.FileUtils;
import com.gykj.grandphotos.utils.uri.UriUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class GrandPhotosActivity extends AppCompatActivity implements AlbumItemsAdapter.OnClickListener, PhotosAdapter.OnClickListener, View.OnClickListener {

    private File mTempImageFile;

    private AlbumModel albumModel;
    private ArrayList<Object> photoList = new ArrayList<>();
    private ArrayList<Object> albumItemList = new ArrayList<>();

    private ArrayList<GrandPhotoBean> resultList = new ArrayList<>();

    private RecyclerView rvPhotos;
    private PhotosAdapter photosAdapter;
    private GridLayoutManager gridLayoutManager;

    private RelativeLayout rootViewAlbumItems;

    private PressedTextView tvDone, tvPreview;
    private TextView tvOriginal;

    private int currAlbumItemIndex = 0;

    private TextView tvTitle;

    private LinearLayout mSecondMenus;

    private RelativeLayout permissionView;
    private TextView tvPermission;
    private View mBottomBar;

    private boolean isQ = false;

    public static long startTime = 0;

    public static boolean doubleClick() {
        long now = System.currentTimeMillis();
        if (now - startTime < 600) {
            return true;
        }
        startTime = now;
        return false;
    }

    public static void start(Activity activity, int requestCode) {
        if (doubleClick()) return;
        Intent intent = new Intent(activity, GrandPhotosActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void start(Fragment fragment, int requestCode) {
        if (doubleClick()) return;
        Intent intent = new Intent(fragment.getActivity(), GrandPhotosActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void start(androidx.fragment.app.Fragment fragment, int requestCode) {
        if (doubleClick()) return;
        Intent intent = new Intent(fragment.getContext(), GrandPhotosActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_photos);
        hideActionBar();
        adaptationStatusBar();
        loadingDialog = LoadingDialog.get(this);
        isQ = Build.VERSION.SDK_INT == Build.VERSION_CODES.Q;
        initSomeViews();
        if (PermissionUtil.checkAndRequestPermissionsInActivity(this, getNeedPermissions())) {
            hasPermissions();
        } else {
            permissionView.setVisibility(View.VISIBLE);
        }
    }

    private void adaptationStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int statusColor = getWindow().getStatusBarColor();
            if (statusColor == Color.TRANSPARENT) {
                statusColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
            }
            if (ColorUtils.isWhiteColor(statusColor)) {
                SystemUtils.getInstance().setStatusDark(this, true);
            }
        }
    }

    private void initSomeViews() {
        mBottomBar = findViewById(R.id.m_bottom_bar);
        permissionView = findViewById(R.id.rl_permissions_view);
        tvPermission = findViewById(R.id.tv_permission);
        rootViewAlbumItems = findViewById(R.id.root_view_album_items);
        tvTitle = findViewById(R.id.tv_title);
        setClick(R.id.iv_back);
    }

    private void hasPermissions() {
        permissionView.setVisibility(View.GONE);
        // 是否只开启相机
        if (Setting.onlyStartCamera) {
            launchCamera(GrandCode.REQUEST_CAMERA);
            return;
        }
        AlbumModel.CallBack albumModelCallBack = new AlbumModel.CallBack() {
            @Override
            public void onAlbumWorkedCallBack() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                        onAlbumWorkedDo();
                    }
                });
            }
        };
        loadingDialog.show();
        albumModel = AlbumModel.getInstance();
        albumModel.query(this, albumModelCallBack);
    }

    protected String[] getNeedPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        return new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionUtil.onPermissionResult(this, permissions, grantResults,
                new PermissionUtil.PermissionCallBack() {
                    @Override
                    public void onSuccess() {
                        hasPermissions();
                    }

                    @Override
                    public void onShouldShow() {
                        tvPermission.setText(R.string.permissions_again_easy_photos);
                        permissionView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (PermissionUtil.checkAndRequestPermissionsInActivity(GrandPhotosActivity.this, getNeedPermissions())) {
                                    hasPermissions();
                                }
                            }
                        });

                    }

                    @Override
                    public void onFailed() {
                        tvPermission.setText(R.string.permissions_die_easy_photos);
                        permissionView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                SettingsUtils.startMyApplicationDetailsForResult(GrandPhotosActivity.this,
                                        getPackageName());
                            }
                        });

                    }
                });
    }


    /**
     * 启动相机
     *
     * @param requestCode startActivityForResult的请求码
     */
    private void launchCamera(int requestCode) {
        if (TextUtils.isEmpty(GrandPhotoHelper.getFileProviderPath()))
            throw new RuntimeException("AlbumBuilder" + " : 请执行 setFileProviderAuthority()方法");
        if (!cameraIsCanUse()) {
            permissionView.setVisibility(View.VISIBLE);
            tvPermission.setText(R.string.permissions_die_easy_photos);
            permissionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SettingsUtils.startMyApplicationDetailsForResult(GrandPhotosActivity.this,
                            getPackageName());
                }
            });
            return;
        }
        toAndroidCamera(requestCode);
    }

    /**
     * 启动系统相机
     *
     * @param requestCode 请求相机的请求码
     */
    private Uri photoUri = null;

    private void toAndroidCamera(int requestCode) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null ||
                this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {

            if (isQ) {
                photoUri = createImageUri();
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(cameraIntent, requestCode);
                return;
            }

            createCameraTempImageFile();
            if (mTempImageFile != null && mTempImageFile.isFile()) {

                Uri imageUri = UriUtils.getUri(this, mTempImageFile);

                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //对目标应用临时授权该Uri所代表的文件
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION); //对目标应用临时授权该Uri所代表的文件

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//将拍取的照片保存到指定URI
                startActivityForResult(cameraIntent, requestCode);
            } else {
                Toast.makeText(getApplicationContext(), R.string.camera_temp_file_error_easy_photos,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.msg_no_camera_easy_photos, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private Uri createImageUri() {
        //设置保存参数到ContentValues中
        ContentValues contentValues = new ContentValues();
        //设置文件名
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME,
                String.valueOf(System.currentTimeMillis()));
        //兼容Android Q和以下版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
            //RELATIVE_PATH是相对路径不是绝对路径;照片存储的地方为：存储/Pictures
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures");
        }
        //设置文件类型
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG");
        //执行insert操作，向系统文件夹中添加文件
        //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
        return getContentResolver().insert(MediaStore.Images.Media.getContentUri("external"),
                contentValues);
    }


    private void createCameraTempImageFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (null == dir) {
            dir = new File(Environment.getExternalStorageDirectory(),
                    File.separator + "DCIM" + File.separator + "Camera" + File.separator);
        }
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                dir = getExternalFilesDir(null);
                if (null == dir || !dir.exists()) {
                    dir = getFilesDir();
                    if (null == dir || !dir.exists()) {
                        dir = getFilesDir();
                        if (null == dir || !dir.exists()) {
                            String cacheDirPath =
                                    File.separator + "data" + File.separator + "data" + File.separator + getPackageName() + File.separator + "cache" + File.separator;
                            dir = new File(cacheDirPath);
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                        }
                    }
                }
            }
        }

        try {
            mTempImageFile = File.createTempFile("IMG", ".jpg", dir);
        } catch (IOException e) {
            e.printStackTrace();
            mTempImageFile = null;
        }

    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GrandCode.REQUEST_SETTING_APP_DETAILS) {
            if (PermissionUtil.checkAndRequestPermissionsInActivity(this, getNeedPermissions())) {
                hasPermissions();
            } else {
                permissionView.setVisibility(View.VISIBLE);
            }
            return;
        }
        switch (resultCode) {
            case RESULT_OK:
                if (GrandCode.REQUEST_CAMERA == requestCode) {
                    if (isQ) {
                        onCameraResultForQ();
                        return;
                    }

                    if (mTempImageFile == null || !mTempImageFile.isFile()) {
                        throw new RuntimeException("GrandPhotos拍照保存的图片不存在");
                    }
                    onCameraResult();
                    return;
                }

                if (GrandCode.REQUEST_PREVIEW_ACTIVITY == requestCode) {
                    if (data.getBooleanExtra(Key.PREVIEW_CLICK_DONE, false)) {
                        done();
                        return;
                    }
                    photosAdapter.change();
                    processOriginalMenu();
                    shouldShowMenuDone();
                    return;
                }

                break;
            case RESULT_CANCELED:
                if (GrandCode.REQUEST_CAMERA == requestCode) {
                    // 删除临时文件
                    if (mTempImageFile != null && mTempImageFile.exists()) {
                        mTempImageFile.delete();
                        mTempImageFile = null;
                    }
                    return;
                }

                if (GrandCode.REQUEST_PREVIEW_ACTIVITY == requestCode) {
                    processOriginalMenu();
                    return;
                }
                break;
            default:
                break;
        }
    }

    String folderPath;
    String albumName;

    private void addNewPhoto(GrandPhotoBean photo) {
        photo.selectedOriginal = Setting.selectedOriginal;

        if (!isQ) {
            MediaScannerConnectionUtils.refresh(this, photo.path);
            folderPath = new File(photo.path).getParentFile().getAbsolutePath();
            albumName = StringUtils.getLastPathSegment(folderPath);
        }

        String albumItem_all_name = albumModel.getAllAlbumName(this);
        albumModel.album.getAlbumItem(albumItem_all_name).addImageItem(0, photo);

        albumModel.album.addAlbumItem(albumName, folderPath, photo.path, photo.uri);
        albumModel.album.getAlbumItem(albumName).addImageItem(0, photo);

        albumItemList.clear();
        albumItemList.addAll(albumModel.getAlbumItems());

        photoList.clear();
        ArrayList<GrandPhotoBean> mCurrAlbumItemPhotos = albumModel.getCurrAlbumItemPhotos(0);
        photoList.addAll(mCurrAlbumItemPhotos);
        photosAdapter.change();
//        if (Setting.hasAlbumItemsAd()) {
//            int albumItemsAdIndex = 2;
//            if (albumItemList.size() < albumItemsAdIndex + 1) {
//                albumItemsAdIndex = albumItemList.size() - 1;
//            }
//            albumItemList.add(albumItemsAdIndex, Setting.albumItemsAdView);
//        }
//        albumItemsAdapter.notifyDataSetChanged();

        if (Setting.count == 1) {
            Result.clear();
            int res = Result.addPhoto(photo);
            onSelectorOutOfMax(res);
        } else {
            if (Result.count() >= Setting.count) {
                onSelectorOutOfMax(null);
            } else {
                int res = Result.addPhoto(photo);
                onSelectorOutOfMax(res);
            }
        }
//        rvAlbumItems.scrollToPosition(0);
//        albumItemsAdapter.setSelectedPosition(0);
        shouldShowMenuDone();
    }

    private GrandPhotoBean getPhoto(Uri uri) {
        GrandPhotoBean p = null;
        String path;
        String name;
        long dateTime;
        String type;
        long size;
        int width = 0;
        int height = 0;
        int orientation = 0;
        String[] projections = AlbumModel.getInstance().getProjections();
        boolean shouldReadWidth = projections.length > 8;
        Cursor cursor = getContentResolver().query(uri, projections, null, null, null);
        if (cursor == null) {
            return null;
        }
        int albumNameCol = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);

        if (cursor.moveToFirst()) {
            path = cursor.getString(7);
            name = cursor.getString(2);
            dateTime = cursor.getLong(3);
            type = cursor.getString(4);
            size = cursor.getLong(5);
            if (shouldReadWidth) {
                width = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH));
                height = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT));
                orientation =
                        cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.ORIENTATION));
                if (90 == orientation || 270 == orientation) {
                    int temp = width;
                    width = height;
                    height = temp;
                }
            }
            if (albumNameCol > 0) {
                albumName = cursor.getString(albumNameCol);
                folderPath = albumName;
            }
            p = new GrandPhotoBean(name, uri, path, dateTime, width, height, orientation, size, type);
        }
        cursor.close();

        return p;
    }

    private void onCameraResultForQ() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final GrandPhotoBean photo = getPhoto(photoUri);
                if (photo == null) {
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        loadingDialog.dismiss();
//                        if (Setting.onlyStartCamera || albumModel.getAlbumItems().isEmpty()) {
                        Intent data = new Intent();
                        photo.selectedOriginal = Setting.selectedOriginal;
                        resultList.add(photo);
                        data.putParcelableArrayListExtra(GrandPhotoPickers.RESULT_PHOTOS, resultList);
                        data.putExtra(GrandPhotoPickers.RESULT_SELECTED_ORIGINAL,
                                Setting.selectedOriginal);
                        setResult(RESULT_OK, data);
                        finish();
//                            return;
//                        }
//
//                        addNewPhoto(photo);
//                        Result.clear();
//                        Result.addPhoto(photo);
//                        PreviewActivity.start(GrandPhotosActivity.this);
                    }
                });

            }
        }).start();
    }

    private void onCameraResult() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss",
                        Locale.getDefault());
                String imageName = "IMG_%s.jpg";
                String filename = String.format(imageName, dateFormat.format(new Date()));
                File reNameFile = new File(mTempImageFile.getParentFile(), filename);
                if (!reNameFile.exists()) {
                    if (mTempImageFile.renameTo(reNameFile)) {
                        mTempImageFile = reNameFile;
                    }
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mTempImageFile.getAbsolutePath(), options);
                MediaScannerConnectionUtils.refresh(GrandPhotosActivity.this, mTempImageFile);//
                // 更新媒体库

                Uri uri = UriUtils.getUri(GrandPhotosActivity.this, mTempImageFile);
                int orientation = 0;
                int width = options.outWidth;
                int height = options.outHeight;

//                ExifInterface exif = null;
//                try {
//                    exif = new ExifInterface(mTempImageFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if (null != exif) {
//                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
//                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
//                        width = options.outHeight;
//                        height = options.outWidth;
//                    }
//                }

                final GrandPhotoBean photo = new GrandPhotoBean(mTempImageFile.getName(), uri,
                        mTempImageFile.getAbsolutePath(),
                        mTempImageFile.lastModified() / 1000, width, height, orientation,
                        mTempImageFile.length(),
                        options.outMimeType);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        if (Setting.onlyStartCamera || albumModel.getAlbumItems().isEmpty()) {
                        Intent data = new Intent();
                        photo.selectedOriginal = Setting.selectedOriginal;
                        resultList.add(photo);
                        data.putParcelableArrayListExtra(GrandPhotoPickers.RESULT_PHOTOS, resultList);
                        data.putExtra(GrandPhotoPickers.RESULT_SELECTED_ORIGINAL, Setting.selectedOriginal);
                        setResult(RESULT_OK, data);
                        finish();
//                            return;
//                        }
//
//                        addNewPhoto(photo);
//                        Result.clear();
//                        Result.addPhoto(photo);
//                        PreviewActivity.start(GrandPhotosActivity.this);
                    }
                });
            }
        }).start();

    }


    private void onAlbumWorkedDo() {
        initView();
    }

    private void initView() {

        if (albumModel.getAlbumItems().isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.no_photos_easy_photos, Toast.LENGTH_LONG).show();
            return;
        }

        mSecondMenus = findViewById(R.id.m_second_level_menu);
        int columns = getResources().getInteger(R.integer.photos_columns_easy_photos);
        tvDone = findViewById(R.id.tv_done);
        rvPhotos = findViewById(R.id.rv_photos);
        ((SimpleItemAnimator) rvPhotos.getItemAnimator()).setSupportsChangeAnimations(false);
        //去除item更新的闪光
        photoList.clear();
        ArrayList<GrandPhotoBean> mCurrAlbumItemPhotos = albumModel.getCurrAlbumItemPhotos(0);
        photoList.addAll(mCurrAlbumItemPhotos);
        photosAdapter = new PhotosAdapter(this, photoList, this);
        gridLayoutManager = new GridLayoutManager(this, columns);
        rvPhotos.setLayoutManager(gridLayoutManager);
        rvPhotos.setAdapter(photosAdapter);
        tvOriginal = findViewById(R.id.tv_original);
        if (Setting.showOriginalMenu) {
            processOriginalMenu();
        } else {
            tvOriginal.setVisibility(View.GONE);
        }
        tvPreview = findViewById(R.id.tv_preview);

        initAlbumItems();
        shouldShowMenuDone();
        setClick(R.id.tv_clear, R.id.tv_puzzle);
        setClick(rootViewAlbumItems, tvDone, tvOriginal, tvPreview);

    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void initAlbumItems() {
        albumItemList.clear();
        albumItemList.addAll(albumModel.getAlbumItems());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.root_view_album_items == id) {
        } else if (R.id.iv_back == id) {
            onBackPressed();
        } else if (R.id.tv_done == id) {
            done();
        } else if (R.id.tv_clear == id) {
            Result.removeAll();
            photosAdapter.change();
            shouldShowMenuDone();
        } else if (R.id.tv_original == id) {
            if (!Setting.originalMenuUsable) {
                Toast.makeText(getApplicationContext(), Setting.originalMenuUnusableHint, Toast.LENGTH_SHORT).show();
                return;
            }
            Setting.selectedOriginal = !Setting.selectedOriginal;
            processOriginalMenu();
        } else if (R.id.tv_preview == id) {
            PreviewActivity.start(GrandPhotosActivity.this);
        }
    }

    private void done() {
        resultFast();
    }

//    private void resultUseWidth() {
//        loadingDialog.show();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int size = Result.photos.size();
//                try {
//                    for (int i = 0; i < size; i++) {
//                        GrandPhotoBean photo = Result.photos.get(i);
//                        if (photo.width == 0 || photo.height == 0) {
//                            BitmapUtils.calculateLocalImageSizeThroughBitmapOptions(photo);
//                        }
//                        if (BitmapUtils.needChangeWidthAndHeight(photo)) {
//                            int h = photo.width;
//                            photo.width = photo.height;
//                            photo.height = h;
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadingDialog.dismiss();
//                        resultFast();
//                    }
//                });
//            }
//        }).start();
//    }

    private int fileSize = 0;

    /**
     * 对获取到数据进行整理并返回
     */
    private void resultFast() {
        // 清空一下数据
        resultList.clear();
        String floder = getExternalCacheDir().getAbsolutePath() + "/grandPhoto";
        final File mFile = new File(floder);
        if (!mFile.exists()) {
            mFile.mkdirs();
        }
        final ArrayList<GrandPhotoBean> mPhotos = Result.photos;
        fileSize = 0;
        boolean hasRoate = false;// 循环查看是否被旋转过，如果旋转过则需要等线程执行完成
        for (int i = 0; i < mPhotos.size(); i++) {
            final GrandPhotoBean mPhoto = mPhotos.get(i);
            // 取余4=0代表没有转圈
            if (mPhoto.getRotateDegree() % 4 == 0) {
                fileSize++;
            } else {
                hasRoate = true;
                File oldFile = null;// 要下载的文件
                if (isQ) {
                    oldFile = FileUtils.getFile(GrandPhotosActivity.this, mPhoto.uri);
                } else {
                    oldFile = new File(mPhoto.path);
                }
                if (oldFile.exists()) {
                    Bitmap mOldbitmapFile = BitmapFactory.decodeFile(oldFile.getAbsolutePath());
                    Matrix mRoateMatrix = new Matrix();
                    mRoateMatrix.setRotate(mPhoto.getRotateDegree() * 90);
                    Bitmap mBitmap = Bitmap.createBitmap(mOldbitmapFile, 0, 0, mOldbitmapFile.getWidth(), mOldbitmapFile.getHeight(), mRoateMatrix, true);
                    String mAbsolutePath = mFile.getAbsolutePath();
                    final long start = System.currentTimeMillis();
                    BitmapUtils.saveBitmapToDir(GrandPhotosActivity.this, mAbsolutePath, "temFile_", mBitmap, false, new SaveBitmapCallBack() {
                        @Override
                        public void onSuccess(File file) {
                            fileSize++;
                            mPhoto.uri = FileUtils.getUri(file);
                            mPhoto.path = file.getAbsolutePath();
                            if (fileSize == mPhotos.size()) {
                                long end = System.currentTimeMillis();
                                long diff = end - start;
                                Log.e("resultFast: ", " --- " + diff + " ---");
                                Intent intent = new Intent();
                                Result.processOriginal();
                                resultList.addAll(mPhotos);
                                intent.putParcelableArrayListExtra(GrandPhotoPickers.RESULT_PHOTOS, resultList);
                                intent.putExtra(GrandPhotoPickers.RESULT_SELECTED_ORIGINAL,
                                        Setting.selectedOriginal);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }

                        @Override
                        public void onIOFailed(IOException exception) {
                        }

                        @Override
                        public void onCreateDirFailed() {
                        }
                    });
                }
            }
        }
        // 所有照片没有旋转过则返回数据
        if (!hasRoate) {
            Intent intent = new Intent();
            Result.processOriginal();
            resultList.addAll(mPhotos);
            intent.putParcelableArrayListExtra(GrandPhotoPickers.RESULT_PHOTOS, resultList);
            intent.putExtra(GrandPhotoPickers.RESULT_SELECTED_ORIGINAL,
                    Setting.selectedOriginal);
            setResult(RESULT_OK, intent);
            finish();
        }

    }


    private void processOriginalMenu() {
        if (!Setting.showOriginalMenu) return;
        if (Setting.selectedOriginal) {
            tvOriginal.setTextColor(ContextCompat.getColor(this, R.color.easy_photos_fg_accent));
        } else {
            if (Setting.originalMenuUsable) {
                tvOriginal.setTextColor(ContextCompat.getColor(this,
                        R.color.easy_photos_fg_primary));
            } else {
                tvOriginal.setTextColor(ContextCompat.getColor(this,
                        R.color.easy_photos_fg_primary_dark));
            }
        }
    }


    @Override
    public void onAlbumItemClick(int position, int realPosition) {
        updatePhotos(realPosition);
    }

    private void updatePhotos(int currAlbumItemIndex) {
        this.currAlbumItemIndex = currAlbumItemIndex;
        photoList.clear();
        ArrayList<GrandPhotoBean> mItemPhotos = albumModel.getCurrAlbumItemPhotos(currAlbumItemIndex);
        photoList.addAll(mItemPhotos);
        photosAdapter.change();
        rvPhotos.scrollToPosition(0);
    }

    private void shouldShowMenuDone() {
        if (Result.isEmpty()) {
            if (View.VISIBLE == tvDone.getVisibility()) {
                ScaleAnimation scaleHide = new ScaleAnimation(1f, 0f, 1f, 0f);
                scaleHide.setDuration(200);
                tvDone.startAnimation(scaleHide);
            }
            tvDone.setVisibility(View.INVISIBLE);
            tvPreview.setVisibility(View.INVISIBLE);
        } else {
            if (View.INVISIBLE == tvDone.getVisibility()) {
                ScaleAnimation scaleShow = new ScaleAnimation(0f, 1f, 0f, 1f);
                scaleShow.setDuration(200);
                tvDone.startAnimation(scaleShow);
            }
            tvDone.setVisibility(View.VISIBLE);
            tvPreview.setVisibility(View.VISIBLE);
        }
        tvDone.setText(getString(R.string.selector_action_done_easy_photos, Result.count(),
                Setting.count));
    }

    @Override
    public void onCameraClick() {
        launchCamera(GrandCode.REQUEST_CAMERA);
    }

    @Override
    public void onPhotoClick(GrandPhotoBean mItem, int position, int realPosition) {
        if (!mItem.selected) {
            Toast.makeText(this, "请选中后再查看图片", Toast.LENGTH_SHORT).show();
            return;
        }
        Result.setListNoClick();
        mItem.isClick = true;
        PreviewActivity.start(GrandPhotosActivity.this);
    }

    @Override
    public void onSelectorOutOfMax(@Nullable Integer result) {
        if (result == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.selector_reach_max_image_hint_easy_photos,
                    Setting.count), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void onSelectorChanged() {
        shouldShowMenuDone();
    }


    @Override
    public void onBackPressed() {
        if (albumModel != null) albumModel.stopQuery();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (albumModel != null) albumModel.stopQuery();
        super.onDestroy();
    }


    private void setClick(@IdRes int... ids) {
        for (int id : ids) {
            findViewById(id).setOnClickListener(this);
        }
    }

    private void setClick(View... views) {
        for (View v : views) {
            v.setOnClickListener(this);
        }
    }

    /**
     * 返回true 表示可以使用  返回false表示不可以使用
     */
    public boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters(); //针对魅族手机
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isCanUse;
    }
}

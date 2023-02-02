package com.gykj.grandphotos.Builder;

import android.app.Activity;
import android.net.Uri;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.gykj.grandphotos.result.Result;
import com.gykj.grandphotos.setting.Setting;
import com.gykj.grandphotos.callback.SelectCallback;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;
import com.gykj.grandphotos.ui.GrandPhotosActivity;
import com.gykj.grandphotos.utils.result.GrandPhotoResult;
import com.gykj.grandphotos.utils.uri.UriUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * GrandPhotoPhotos的启动管理器
 */
public class AlbumBuilder {


    private static AlbumBuilder instance;
    private WeakReference<Activity> mActivity;
    private WeakReference<Fragment> mFragmentV;
    private StartupType startupType;

    private AlbumBuilder(FragmentActivity activity, StartupType startupType) {
        mActivity = new WeakReference<Activity>(activity);
        this.startupType = startupType;
    }

    private AlbumBuilder(Fragment fragment, StartupType startupType) {
        mFragmentV = new WeakReference<Fragment>(fragment);
        this.startupType = startupType;
    }

    /**
     * 内部处理相机和相册的实例
     *
     * @param activity Activity的实例
     */

    private static AlbumBuilder with(FragmentActivity activity, StartupType startupType) {
        clear();
        instance = new AlbumBuilder(activity, startupType);
        return instance;
    }

    private static AlbumBuilder with(Fragment fragmentV, StartupType startupType) {
        clear();
        instance = new AlbumBuilder(fragmentV, startupType);
        return instance;
    }






    /**
     * 创建相册
     *
     * @param activity    上下文
     * @return
     */
    public static AlbumBuilder createAlbum(FragmentActivity activity) {
        return AlbumBuilder.with(activity,StartupType.ALBUM_CAMERA);
    }

    public static AlbumBuilder createAlbum(Fragment fragmentV) {
        return AlbumBuilder.with(fragmentV,StartupType.ALBUM_CAMERA);
    }

    /**
     * 创建相机
     */
    public static AlbumBuilder createCamera(FragmentActivity activity) {
        return AlbumBuilder.with(activity, StartupType.CAMERA);
    }

    public static AlbumBuilder createCamera(Fragment fragmentV) {
        return AlbumBuilder.with(fragmentV, StartupType.CAMERA);
    }


    /**
     * 设置选择数
     *
     * @param selectorMaxCount 最大选择数
     * @return AlbumBuilder
     */
    public AlbumBuilder setMaxCount(int selectorMaxCount) {
        Setting.count = selectorMaxCount;
        return AlbumBuilder.this;
    }

    /**
     * 启动模式
     * CAMERA-相机
     * ALBUM_CAMERA-带有相机按钮的相册专辑
     */
    private enum StartupType {
        CAMERA,
        ALBUM_CAMERA
    }


    /**
     * 设置默认选择图片集合
     *
     * @param selectedPhotos 默认选择图片集合
     * @return AlbumBuilder
     */
    public AlbumBuilder setSelectedPhotos(ArrayList<GrandPhotoBean> selectedPhotos) {
        Setting.selectedPhotos.clear();
        if (selectedPhotos.isEmpty()) {
            return AlbumBuilder.this;
        }
        Setting.selectedPhotos.addAll(selectedPhotos);
        Setting.selectedOriginal = selectedPhotos.get(0).selectedOriginal;
        return AlbumBuilder.this;
    }

    /**
     * 设置默认选择图片地址集合
     *
     * @param selectedPhotoPaths 默认选择图片地址集合
     * @return AlbumBuilder
     * @Deprecated android 10 不推荐使用直接使用Path方式，推荐使用Photo类
     */
    @Deprecated
    public AlbumBuilder setSelectedPhotoPaths(ArrayList<String> selectedPhotoPaths) {
        Setting.selectedPhotos.clear();
        ArrayList<GrandPhotoBean> selectedPhotos = new ArrayList<>();
        for (String path : selectedPhotoPaths) {
            File file = new File(path);
            Uri uri = null;
            if (null != mActivity && null != mActivity.get()) {
                uri = UriUtils.getUri(mActivity.get(), file);
            }
            if (null != mFragmentV && null != mFragmentV.get()) {
                uri = UriUtils.getUri(mFragmentV.get().getActivity(), file);
            }
            if (uri == null) {
                uri = Uri.fromFile(file);
            }
            GrandPhotoBean photo = new GrandPhotoBean(null, uri, path, 0, 0, 0, 0, 0, null);
            selectedPhotos.add(photo);
        }
        Setting.selectedPhotos.addAll(selectedPhotos);
        return AlbumBuilder.this;
    }


    /**
     * 启动，onActivityResult方式
     *
     * @param requestCode startActivityForResult的请求码
     */

    public void start(int requestCode) {
        setSettingParams();
        launchGrandPhotosActivity(requestCode);
    }

    private void setSettingParams() {
        switch (startupType) {
            case CAMERA:
                Setting.onlyStartCamera = true;
                break;
            case ALBUM_CAMERA:
                Setting.onlyStartCamera = false;
                break;
        }
    }

    /**
     * 启动，链式调用
     */
    public void start(SelectCallback callback) {
        if (null != mActivity && null != mActivity.get() && mActivity.get() instanceof FragmentActivity) {
            GrandPhotoResult.get((FragmentActivity) mActivity.get()).startGrandPhoto(callback);
            return;
        }
        if (null != mFragmentV && null != mFragmentV.get()) {
            GrandPhotoResult.get(mFragmentV.get()).startGrandPhoto(callback);
            return;
        }
        throw new RuntimeException("mActivity or mFragmentV maybe null, you can not use this " +
                "method... ");
    }

    /**
     * 正式启动
     *
     * @param requestCode startActivityForResult的请求码
     */
    private void launchGrandPhotosActivity(int requestCode) {
        if (null != mActivity && null != mActivity.get()) {
            GrandPhotosActivity.start(mActivity.get(), requestCode);
            return;
        }
        if (null != mFragmentV && null != mFragmentV.get()) {
            GrandPhotosActivity.start(mFragmentV.get(), requestCode);
        }
    }

    /**
     * 清除所有数据
     */
    private static void clear() {
        Result.clear();
        Setting.clear();
        instance = null;
    }

    public void startCamera() {

    }
}

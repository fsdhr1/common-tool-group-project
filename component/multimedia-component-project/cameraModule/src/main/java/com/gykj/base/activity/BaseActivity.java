package com.gykj.base.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gykj.cameramodule.R;
import com.gykj.utils.EasyPermissions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2018/9/13.
 */

public abstract class BaseActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 禁止横屏
        setVolumeControlStream(AudioManager.STREAM_MUSIC);// 使得音量键控制媒体声音
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iOnActivityResults != null) {
            iOnActivityResults.clear();
        }
        iOnActivityResults = null;
    }

    protected abstract void initialize();

    protected abstract void registerEvent();

    protected abstract void initClass();


    /**
     * *********************************************************************************************
     * 常用权限
     * *********************************************************************************************
     */
    // 文件夹读写权限
    protected String[] MANIFEST_PHONE_STATE = new String[]{Manifest.permission.READ_PHONE_STATE};
    // 文件夹读写权限
    protected String[] MANIFEST_STORAGE = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    // 定位权限
    protected String[] MANIFEST_LOCATION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE};
    // 相机权限
    protected String[] MANIFEST_CAMERA = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 权限回调接口
     */
    private CheckPermListener checkPermListener;

    public interface CheckPermListener {
        //权限通过后的回调方法
        void superPermission();
    }

    public void checkPermission(int resString, int flag, String[] mPerms, CheckPermListener listener) {
        checkPermListener = listener;
        if (EasyPermissions.hasPermissions(this, mPerms)) {
            if (checkPermListener != null) checkPermListener.superPermission();
        } else {
            EasyPermissions.requestPermissions(this, getString(resString), flag, mPerms);
        }
    }

    public void checkPermission(int flag, String[] mPerms, CheckPermListener listener) {
        checkPermListener = listener;
        if (EasyPermissions.hasPermissions(this, mPerms)) {
            if (checkPermListener != null) checkPermListener.superPermission();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.perm_tip), flag, mPerms);
        }
    }

    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        //同意了某些权限可能不是全部
    }


    @Override
    public void onPermissionsAllGranted() {
        //同意了全部权限的回调
        if (checkPermListener != null)
            checkPermListener.superPermission();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        EasyPermissions.checkDeniedPermissionsNeverAskAgain(this, getString(R.string.perm_tip), R.string.perm_setting, R.string.perm_cancel, null, perms);
    }


    /**
     * *********************************************************************************************
     * activity结果回调
     * *********************************************************************************************
     */
    private List<IOnActivityResult> iOnActivityResults;

    public void addIOnActivityResults(IOnActivityResult iOnActivityResult) {
        if (iOnActivityResults == null) iOnActivityResults = new ArrayList<>();
        iOnActivityResults.add(iOnActivityResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (iOnActivityResults != null) {
            for (IOnActivityResult iOnActivityResult : iOnActivityResults) {
                iOnActivityResult.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * *********************************************************************************************
     * 提示框
     * *********************************************************************************************
     */
    public void showToast(Object info) {
        Toast.makeText(this, info + "", Toast.LENGTH_SHORT).show();
    }

    public void showLongToast(Object info) {
        Toast.makeText(this, info + "", Toast.LENGTH_LONG).show();
    }
}

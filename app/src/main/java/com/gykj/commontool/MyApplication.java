package com.gykj.commontool;

import android.app.Application;

import com.grandtech.mapframe.core.maps.GMapView;
import com.gykj.grandphotos.config.GrandPhotoHelper;
import com.gykj.mvpbasemodule.MvpBaseModule;
import com.gykj.networkmodule.NetworkHelper;
import com.mapbox.android.core.crashreporter.CrashReport;
import com.gykj.autoupdate.utils.UpdateServiceHelper;
import com.gykj.commontool.autoupdatetest.Constants;

/**
 * @author zyp
 * 2021/2/26
 */
public class MyApplication extends Application {

    public GMapView gMapView;

    @Override
    public void onCreate() {
        super.onCreate();
        MvpBaseModule.init(this);
        NetworkHelper.init(this, "http://gykj123.cn:8849/grandtech-service-snyzt/");
        NetworkHelper.setToken(this, "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiLogL_kvbPolYrkuKrkuroiLCJjb250ZXh0VXNlcklkIjoicDlGQjJDQTM4RDREOTQ0MjA4QjAwNDI2MkMwMEM2Rjc4IiwiY29udGV4dE5hbWUiOiJnZW8iLCJjb250ZXh0RGVwdElkIjoiMSIsImNvbnRleHRBcHBsaWNhdGlvbklkIjoiMSIsImV4cCI6MjYxMzIxODk2NDd9.dM92KheaX7Yp7GsHB_iFubyzV8KClL9mFKDy0a1AwXM");
        NetworkHelper.addDefaultSuccessCode(200);
        //
        GrandPhotoHelper.init("com.gykj.commontool.autoupdatefileprovider");

        // 启动自动更新检测服务
        UpdateServiceHelper.getInstance(this, Constants.YourApp_Key, Constants.YourApp_SignType)
                .setOtherCheckUrl(null)
                .setOutsideAuthority("com.gykj.commontool.autoupdatefileprovider")
                .setShowDialogActivityName("AutoUpdateTestActivity") // 设置允许弹窗的页面
                .setNumOfFailedReCheck(5) // 检查新版本失败 重试次数
                .setDelayMillisOfReCheck(3000) // 检查新版本失败 重试间隔
                .setTimeOfKeepWatchOnActivity(5000) // 弹窗监测 持续时长
                .setDelayMillisOfReShowDialog(3000) // 弹窗失败 重试间隔
                .startCheck();
    }
}

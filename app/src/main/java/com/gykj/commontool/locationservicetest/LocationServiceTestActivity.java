package com.gykj.commontool.locationservicetest;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gykj.locationservice.LocationClient;
import com.gykj.locationservice.ServiceLocation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 定位服务
 * Created by jyh on 2021-03-16
 */
public class LocationServiceTestActivity extends AppCompatActivity {

    // 定位权限
    protected String[] MANIFEST_LOCATION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 公交车 注册
         */
        EventBus.getDefault().register(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 请求权限
            requestPermissions(MANIFEST_LOCATION, 1000);
        }
    }

    /**
     * 请求权限的结果的回调
     * @param requestCode
     * @param permissions
     * @param grantResults
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
        LocationClient.getInstance().startLoc(this, false, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 停止定位服务
        LocationClient.getInstance().stopLoc(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            /**
             * 公交车 取消注册
             */
            EventBus.getDefault().unregister(this);
            System.out.println("注销公交车");
        }
    }

    /**
     * 接收定位服务信息
     *
     * @param serviceLocation
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ServiceLocation serviceLocation) {
        if (serviceLocation != null)
            Toast.makeText(this, serviceLocation.getLatitude() + ", " + serviceLocation.getLongitude(), Toast.LENGTH_SHORT)
                    .show();
    }


}

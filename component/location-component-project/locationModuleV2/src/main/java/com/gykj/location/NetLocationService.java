package com.gykj.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.gykj.location.base.BaseService;
import com.gykj.location.base.BaseServiceBinder;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by ZhaiJiaChang.
 * <p>
 * Date: 2021/12/7
 * 写此类的目的是拆分成不同种类的服务
 */
public class NetLocationService extends BaseService implements GpsStatus.Listener, LocationListener {

    private LocationManager locationManager;
    private ServiceLocation serviceLocation;
    //设置监听器，自动更新的最小时间为间隔15秒，最小位移变化超过5米
    private int minTime = 5;
    private int minDistance = 2;
    private int interval = 5;// 获取经纬度的时间间隔
    private Location mLocation;


    /**
     * 在Oncreate中在创建线程前确定是否需要穿件线程
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void doInBackGroundingInitialize() {
        serviceRunningInCreate = true;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(this);
        serviceLocation = new ServiceLocation();
        //
        listenLoc();
        getLocation();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            listenLoc();
        }
    };

    /**
     * onCreate 线程中正在执行的逻辑
     */
    @Override
    protected void doInBackGrounding() {
        try {
            Thread.sleep(interval * 1000);
            handler.obtainMessage().sendToTarget();
            getLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * onStartCommand 开启线程之前执行的逻辑
     */
    @Override
    protected void doCommandInBackGroundingInitialize() {

    }

    /**
     * onStartCommand 在线程中执行的逻辑
     */
    @Override
    protected void doCommandInBackGrounding() {

    }

    @SuppressLint("MissingPermission")
    private void listenLoc() {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        BaseServiceBinder baseServiceBinder = new BaseServiceBinder();
        baseServiceBinder.setBaseService(this);
        return baseServiceBinder;
    }


    /**
     * 获取经纬度信息
     */
    @SuppressLint("MissingPermission")
    private void getLocation() {

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {// NET_WORK
            serviceLocation.setLocationType(LocationType.NETWORK.getType());
        }

        if (mLocation == null){
            String provider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER).getName();
            mLocation = locationManager.getLastKnownLocation(provider);
        }

        synchronized (serviceLocation) {
            if (mLocation != null) {
                serviceLocation.setLocation(mLocation);
                serviceLocation.setLatitude(mLocation.getLatitude());
                serviceLocation.setLongitude(mLocation.getLongitude());
            }
            EventBus.getDefault().post(serviceLocation);
        }
    }

    @Override
    public void onGpsStatusChanged(int mI) {

    }

    // 获取定位信息
    @Override
    public void onLocationChanged(Location loc) {
        mLocation = loc;
    }

    @Override
    public void onStatusChanged(String mS, int mI, Bundle mBundle) {

    }

    @Override
    public void onProviderEnabled(String mS) {

    }

    @Override
    public void onProviderDisabled(String mS) {

    }
}

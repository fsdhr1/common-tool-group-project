package com.gykj.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.gykj.location.base.BaseService;
import com.gykj.location.base.BaseServiceBinder;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;

/**
 * Created by zy on 2018/3/30.
 * 定位服务 在整个app内广播位置
 */

public class CommonService extends BaseService implements LocationListener, GpsStatus.Listener {

    /**
     * 定位时间间隔
     */
    private int interval = 5;
    /**
     *
     */
    private LocationManager locationManager;

    private Location mLocation = null;

    private ServiceLocation.ESignal eSignal;
    private ServiceLocation serviceLocation;

    @SuppressLint("MissingPermission")
    @Override
    protected void doInBackGroundingInitialize() {
        /**
         * 公交车 注册
         */
        isFrontService = false;
        //frontServiceNotificationTitle = "APP定位服务";
        //frontServiceNotificationContent = "运行中...";
        serviceRunningInCreate = true;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(this);
        serviceLocation = new ServiceLocation();
        serviceLocation.setLocationType(LocationType.COMMON.getType());
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
     * 子线程
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

    @Override
    protected void doCommandInBackGroundingInitialize() {

    }

    @Override
    protected void doCommandInBackGrounding() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**
         * 公交车 取消注册
         */
        locationManager.removeGpsStatusListener(this);
        locationManager.removeUpdates(this);
        //locationManager=null;
        handler = null;

    }

    //设置监听器，自动更新的最小时间为间隔15秒，最小位移变化超过5米
    private int minTime = 5;
    private int minDistance = 2;

    /**
     * 被动监听
     */
    @SuppressLint("MissingPermission")
    private void listenLoc() {
        System.out.println("被动监听");

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
            return;
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) return;
        mLocation = location;
        System.out.println("sss");
//        if (cLocation != null) {
//            synchronized (cLocation) {
//                cLocation = location;
//                if (serviceLocation != null) {
//                    synchronized (serviceLocation) {
//                        if (location != null) {
//                            serviceLocation.setLatitude(location.getLatitude());
//                            serviceLocation.setLongitude(location.getLongitude());
//                        }
//                    }
//                }
//                /**
//                 * 广播出去
//                 */
//                if (serviceLocation != null) {
//                    EventBus.getDefault().post(serviceLocation);
//                    System.out.println(cLocation.getLatitude() + "|" + cLocation.getLongitude());
//                }
//
//            }
//        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        BaseServiceBinder baseServiceBinder = new BaseServiceBinder();
        baseServiceBinder.setBaseService(this);
        return baseServiceBinder;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        switch (status) {
            //GPS状态为可见时
            case LocationProvider.AVAILABLE:
                Log.i("onStatusChanged", "当前GPS状态为可见状态");
                break;
            //GPS状态为服务区外时
            case LocationProvider.OUT_OF_SERVICE:
                Log.i("onStatusChanged", "当前GPS状态为服务区外状态");
                break;
            //GPS状态为暂停服务时
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.i("onStatusChanged", "当前GPS状态为暂停服务状态");
                break;
        }


    }


    @Override
    public void onProviderEnabled(String s) {
        // Provider被enable时触发此函数，比如GPS打开
        System.out.println("GPS被打开 ！");
    }

    @Override
    public void onProviderDisabled(String s) {
        // Provider被disable时触发此函数，比如GPS被关闭
        System.out.println("GPS被关闭 ！");
    }


    /**
     * 主动监听
     */
    @SuppressWarnings("MissingPermission")
    private void getLocation() {
        try {
            System.out.println("主动获取");
            String provider = "unKnow";
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                System.out.println("GPS定位开启！");
                provider = locationManager.getProvider(LocationManager.GPS_PROVIDER).getName();
                // 如果没有获取到位置，则获取GPS最后一个点
                if (mLocation == null) {
                    mLocation = locationManager.getLastKnownLocation(provider);
                }
                // 如果最后一个点也没有，那就使用Net定位方式
                if (mLocation != null) {
                    if (serviceLocation != null) {
                        serviceLocation.setLatitude(mLocation.getLatitude());
                        serviceLocation.setLongitude(mLocation.getLongitude());
                        postValue(serviceLocation);
                        return;
                    }
                }
            }
            //
            // 如果获取不到Gps那就获取Net
            boolean mProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (mProviderEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
            }

            boolean mB = serviceLocation != null;
            boolean mb = (serviceLocation.geteSignal() == null || ServiceLocation.ESignal.LOW == serviceLocation.geteSignal());
            if (mB && mProviderEnabled && mb) {
                System.out.println("NET定位开启！");
                provider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER).getName();
                if (mLocation == null) {
                    mLocation = locationManager.getLastKnownLocation(provider);
                }

                if (mLocation != null) {
                    serviceLocation.setLatitude(mLocation.getLatitude());
                    serviceLocation.setLongitude(mLocation.getLongitude());
                    postValue(serviceLocation);
                    return;
                }
            }
            // 如果什么也没获取到返回一个对象里边是空值
            postValue(new ServiceLocation());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postValue(ServiceLocation mServiceLocation) {
        EventBus.getDefault().post(mServiceLocation);
        mLocation = null;
    }


    // GPS状态变化时的回调，如卫星数
    @Override
    public void onGpsStatusChanged(int event) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (event == GpsStatus.GPS_EVENT_STARTED) {
                Log.d("zmenaGPS", "GPS event started ");
            }
            if (event == GpsStatus.GPS_EVENT_STOPPED) {
                Log.d("zmenaGPS", "GPS event stopped ");
            }
            if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                Log.d("zmenaGPS", "GPS fixace ");
            }
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                Log.d("zmenaGPS", "GPS EVET NECO ");
                @SuppressLint("MissingPermission") GpsStatus status = locationManager.getGpsStatus(null); //取当前状态
                updateGpsStatus(event, status);
            }
        }
    }


    private void updateGpsStatus(int event, GpsStatus status) {
        if (status == null) return;
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            int count = 0;
            int valid = 0;
            GpsSatellite s;
            while (it.hasNext() && count <= maxSatellites) {
                s = it.next();
                count++;
                if (s.getSnr() > 30) {
                    valid++;
                }
            }
            if (serviceLocation == null) {
                serviceLocation.seteSignal(ServiceLocation.ESignal.LOW);
                return;
            }
            synchronized (serviceLocation) {
                if (valid >= 6) {
                    //表示有信号
                    eSignal = ServiceLocation.ESignal.HIGH;
                }
                if (valid >= 4 && valid < 6) {
                    //表示有信号
                    eSignal = ServiceLocation.ESignal.MIDDLE;
                }
                if (valid < 4) {
                    //信号弱或无信号
                    eSignal = ServiceLocation.ESignal.LOW;
                }
                serviceLocation.seteSignal(eSignal);
                serviceLocation.setSatelliteCount(count);
                serviceLocation.setValidSatelliteCount(valid);
            }
        }
    }

    public ServiceLocation getServiceLocation() {
        return serviceLocation;
    }

}

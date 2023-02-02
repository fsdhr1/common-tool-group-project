package com.gykj.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.gykj.location.base.BaseService;
import com.gykj.location.base.BaseServiceBinder;

import org.greenrobot.eventbus.EventBus;

import java.util.Iterator;


public class GpsLocationService extends BaseService implements LocationListener, GpsStatus.Listener {
    private LocationManager locationManager;



    private ServiceLocation serviceLocation;
    private LocaCallback mLocaCallback;
    private Location mLocation;

    @SuppressLint("MissingPermission")
    @Override
    protected void doInBackGroundingInitialize() {
        isFrontService = false;
        serviceRunningInCreate = true;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(this);// 卫星状态的监听 回调在 onGpsStatusChanged
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mLocaCallback = new LocaCallback();
            // https://blog.csdn.net/qiqigeermumu/article/details/108261920
            locationManager.registerGnssStatusCallback(mLocaCallback);
        }
        serviceLocation = new ServiceLocation();
        listenLoc();
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
            Thread.sleep(3 * 1000);
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
        doInBackGrounded();
        locationManager.removeGpsStatusListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.unregisterGnssStatusCallback(mLocaCallback);
        }
    }

    //设置监听器，自动更新的最小时间为间隔15秒，最小位移变化超过5米
    private int minTime = 3000;
    private float minDistance = 1f;

    /**
     * 被动监听
     */
    @SuppressLint("MissingPermission")
    private void listenLoc() {
        System.out.println("被动监听");
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {// Gps未打开
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
        }
    }


    @Override
    public void onLocationChanged(Location loc) {
        mLocation = loc;
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
        if (LocationProvider.OUT_OF_SERVICE == status) {
            System.out.println("provider" + "GPS服务丢失,切换至网络定位");
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

    // 监听当前定位的是哪个卫星(GPS、北斗、伽利略......)
    // https://developer.android.com/reference/android/location/GpsSatellite#usedInFix()
    @RequiresApi(api = Build.VERSION_CODES.N)
    public class LocaCallback extends GnssStatus.Callback {
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            super.onSatelliteStatusChanged(status);
            int satelliteCount = status.getSatelliteCount();
            //解析组装卫星信息
            makeGnssStatus(status, satelliteCount);
        }

        @Override
        public void onStarted() {
            super.onStarted();
        }

        @Override
        public void onStopped() {
            super.onStopped();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void makeGnssStatus(GnssStatus status, int satelliteCount) {
        //当前可以获取到的卫星总数，然后遍历
        String text = " ";
        int UnKnowCount = 0;
        String UnKnowName = "";
        int BeiDouCount = 0;
        String BeiDouName = "";
        int GpsCount = 0;
        String GpsName = "";
        int GlonassCount = 0;
        String GlonassName = "";
        int GalileoCount = 0;
        String GalieoName = "";
        int SbasCount = 0;
        String SbasName = "";
        int QzssCount = 0;
        String QzssName = "";
        //
        if (satelliteCount > 0) {
            for (int i = 0; i < satelliteCount; i++) {
                //GnssStatus的大部分方法参数传入的就是卫星数量的角标
                boolean used = status.usedInFix(i);// 获取当前卫星是否被使用；
                if (!used) {// 没有被使用不在往下进行
                    continue;
                }
                int type = status.getConstellationType(i);// 获取卫星的类型；
                float zaosheng = status.getCn0DbHz(i);// 获取当前卫星的噪声密度
                if (type == GnssStatus.CONSTELLATION_UNKNOWN) {// UNKNOWN
                    UnKnowCount++;
                    UnKnowName += zaosheng + ",";
                } else if (type == GnssStatus.CONSTELLATION_BEIDOU) {// 北斗
                    BeiDouCount++;
                    BeiDouName += zaosheng + ",";
                } else if (type == GnssStatus.CONSTELLATION_GPS) {
                    GpsCount++;
                    GpsName += zaosheng + ",";
                } else if (type == GnssStatus.CONSTELLATION_GLONASS) {
                    GlonassCount++;
                    GlonassName += zaosheng + ",";
                } else if (type == GnssStatus.CONSTELLATION_QZSS) {
                    QzssCount++;
                    QzssName += zaosheng + ",";
                } else if (type == GnssStatus.CONSTELLATION_GALILEO) {
                    GalileoCount++;
                    GalieoName += zaosheng + ",";
                } else if (type == GnssStatus.CONSTELLATION_SBAS) {
                    SbasCount++;
                    SbasName += zaosheng + ",";
                }
            }
            if (UnKnowCount > 0)
                text += "unknow: usedCount " + UnKnowCount + ",信噪值" + UnKnowName;
            if (BeiDouCount > 0)
                text += "beidou: usedCount " + BeiDouCount + ",信噪值" + BeiDouName;
            if (GpsCount > 0)
                text += "gps: usedCount " + GpsCount + ",信噪值" + GpsName;
            if (GlonassCount > 0)
                text += "Glonass: usedCount " + GlonassCount + ",信噪值" + GlonassName;
            if (GalileoCount > 0)
                text += "Galileo: usedCount " + GalileoCount + ",信噪值" + GalieoName;
            if (SbasCount > 0)
                text += "Sbas: usedCount " + SbasCount + ",信噪值" + SbasName;
            if (QzssCount > 0)
                text += "Qzss: usedCount " + QzssCount + ",信噪值" + QzssName;
            Log.e("makeGnssStatus: ", text);
            serviceLocation.setSatelliteInfo(text);
        }
    }


    /**
     * 主动监听
     */
    @SuppressWarnings("MissingPermission")
    private void getLocation() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (serviceLocation != null && mLocation != null) {
                    synchronized (serviceLocation) {
                        serviceLocation.setLocationType(LocationType.GPS.getType());
                        serviceLocation.setLatitude(mLocation.getLatitude());
                        serviceLocation.setLongitude(mLocation.getLongitude());
                        EventBus.getDefault().post(serviceLocation);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // GPS状态变化时的回调，如卫星数
    @Override
    public void onGpsStatusChanged(int event) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (event == GpsStatus.GPS_EVENT_STARTED) {
                Log.i("zmenaGPS", "GPS event started ");
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
            int maxSatellites = status.getMaxSatellites();// 最大卫星数量
            Iterator<GpsSatellite> it = status.getSatellites().iterator();// getSatellites 能够获取到的卫星的集合
            int count = 0;
            int valid = 0;
            GpsSatellite s;
            while (it.hasNext() && count <= maxSatellites) {
                s = it.next();
                count++;
                if (s.getSnr() > 30) {//getSnr 获取信燥值，值越大信号越好，如果值为0说明没连接卫星
                    valid++;
                }
                boolean used = s.usedInFix();//该卫星是否在被使用
                float snr = s.getSnr();// 信噪比
            }
            if (serviceLocation == null) {
                serviceLocation.seteSignal(ServiceLocation.ESignal.LOW);
                return;
            }
            ServiceLocation.ESignal eSignal = null;
            synchronized (serviceLocation) {
                if (valid >= 6) {
                    //表示有信号
                    eSignal = ServiceLocation.ESignal.HIGH;
                } else if (valid >= 4 && valid < 6) {
                    //表示有信号
                    eSignal = ServiceLocation.ESignal.MIDDLE;
                } else if (valid < 4) {
                    //信号弱或无信号
                    eSignal = ServiceLocation.ESignal.LOW;
                }
                serviceLocation.seteSignal(eSignal);
                serviceLocation.setGpsSignal(valid);
            }
        }
    }

    public ServiceLocation getServiceLocation() {
        return serviceLocation;
    }

}

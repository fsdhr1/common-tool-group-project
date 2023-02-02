package com.gykj.location;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.blankj.utilcode.util.PermissionUtils;
import com.gykj.location.interfaces.LocationCallBack;
import com.gykj.location.base.BaseService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 定位服务对外启动类
 */
public class LocationClient {
    private static LocationClient locationClient;
    private com.baidu.location.LocationClient mLocationClient = null;

    /**
     * LocationType 页面的类型
     * List 当前类型下拥有使用此种定位的数量
     * HashMap 存储单个页面的上下文和接口回调实例
     * Context 负责启动service
     * LocationCallBack 接口回调
     */
    private HashMap<LocationType, ArrayList<HashMap<Context, LocationCallBack>>> mLocationRegistMap = new HashMap<>();


    public static synchronized LocationClient getInstance() {
        if (locationClient == null) {
            locationClient = new LocationClient();
        }
        return locationClient;
    }

    /**
     * 根据每个服务的类型里包含的页面接口的集合，通过遍历的形式将值通过接口回调传递给每个页面
     * 保证了单例类里只能使用一个接口的解决；
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainThread(ServiceLocation mLocation) {
        if (mLocation != null) {
            long start = System.currentTimeMillis();
            if (mLocationRegistMap != null) {
                Iterator<Map.Entry<LocationType, ArrayList<HashMap<Context, LocationCallBack>>>> mIterator = mLocationRegistMap.entrySet().iterator();
                while (mIterator.hasNext()) {
                    Map.Entry<LocationType, ArrayList<HashMap<Context, LocationCallBack>>> mNext = mIterator.next();
                    List<HashMap<Context, LocationCallBack>> mValue = mNext.getValue();
                    for (HashMap<Context, LocationCallBack> mMap : mValue) {
                        Iterator<Map.Entry<Context, LocationCallBack>> mBackIter = mMap.entrySet().iterator();
                        while (mBackIter.hasNext()) {
                            Map.Entry<Context, LocationCallBack> mEntry = mBackIter.next();
                            Context mKey = mEntry.getKey();
                            LocationCallBack mCallBack = mEntry.getValue();
                            if (mCallBack != null) {
                                mCallBack.onLocationCallBack(mKey, mLocation);
                            }
                        }
                    }
                }
            }
            long end = System.currentTimeMillis();
            long diff = end - start;
            Log.e("onLocationCallBack: ", " --- " + diff + " --- ");
        }
    }

    /**
     * 根据枚举类型开启服务
     */
    public void startLocation(Context context, LocationType mType, LocationCallBack mCallBack) {
        // 定位权限
        if (!hasPermission()) {
            showWarnDialog(context);
            return;
        }
        // 定位开关
        if (!isLocationSwitchOpen(context, mType)) {
            showSettingSwitchOpenDialog(context);
            return;
        }
        // 添加EventBus
        if (!EventBus.getDefault().isRegistered(locationClient)) {
            EventBus.getDefault().register(locationClient);
        }
        // 某种类型的服务和服务在哪些地方用到了的接口回调都添加到Map里
        // 如果context已添加过并且服务已启动则不再重复添加
        if (serviceIsAlive(context, mType) && checkServiceAdd(context, mType) ) {
            Log.i("onLocationCallBack:", " --- 已被添加过 --- ");
            return;
        }
        Log.i("onLocationCallBack:", " --- 无添加过，味道好 --- ");
        addLocation2Map(context, mType, mCallBack);
        if (mType == LocationType.GPS) {// GPS
            Intent intent = new Intent(context, GpsLocationService.class);
            context.startService(intent);
        } else if (mType == LocationType.NETWORK) {// NETWORK
            Intent intent = new Intent(context, NetLocationService.class);
            context.startService(intent);
        } else if (mType == LocationType.COMMON) {// 通用
            Intent intent = new Intent(context, CommonService.class);
            context.startService(intent);
        } else if (mType == LocationType.BAIDU) {// 百度
            if (mLocationClient == null) {
                initLocationClient(context);
            }
            requestLocation();
        }

    }

    // 校验服务是否已经被添加过 true添加过 false没添加
    private boolean checkServiceAdd(Context mContext, LocationType mType) {
        ArrayList<HashMap<Context, LocationCallBack>> mArrayList = mLocationRegistMap.get(mType);
        if (mArrayList != null) {
            for (HashMap<Context, LocationCallBack> mMap : mArrayList) {
                Set<Context> mKeySet = mMap.keySet();
                if (mKeySet.contains(mContext)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 只要有一个地方使用Service就往里添加一个CallBack对象
     */
    private void addLocation2Map(Context mContext, LocationType mType, LocationCallBack mCallBack) {
        ArrayList<HashMap<Context, LocationCallBack>> mListMap = mLocationRegistMap.get(mType);
        if (mListMap == null) {
            mListMap = new ArrayList<>();
        }
        HashMap<Context, LocationCallBack> mMap = new HashMap<>();
        mMap.put(mContext, mCallBack);
        mListMap.add(mMap);
        Log.e("onLocationCallBack: ", " --- add list.size " + mListMap.size() + " --- ");
        mLocationRegistMap.put(mType, mListMap);
    }

    /**
     * 记录结束某一次监听，等某个类型的服务都remove后就可以结束某个服务了
     * 如果是Net或GPS则判断接口list是否为0，为0说明没地方用了可以结束服务；
     * 如果是百度则直接停止就行
     */
    public void stopLocation(Context context, LocationType mType) {
        // Net或GPS
        if (mType == LocationType.NETWORK || mType == LocationType.GPS || mType == LocationType.COMMON) {
            if (mLocationRegistMap.containsKey(mType)) {
                List<HashMap<Context, LocationCallBack>> mListMaps = mLocationRegistMap.get(mType);
                if (mListMaps != null) {
//                    for (HashMap<Context, LocationCallBack> mMap : mListMaps) {
                    for (Iterator<HashMap<Context, LocationCallBack>> mIterator = mListMaps.iterator(); mIterator.hasNext(); ) {
                        HashMap<Context, LocationCallBack> mMap = mIterator.next();
                        if (mMap.containsKey(context)) {
                            mIterator.remove();
                        }
                    }
                    Log.e("addLocation2Map: ", " --- remove list.size " + mListMaps.size() + " --- ");
                    if (mListMaps.size() == 0 && serviceIsAlive(context, mType)) {
                        stopLocationType(context, mType);
                    }
                }
            }
        } else if (mType == LocationType.BAIDU) {
            //百度
            if (mLocationClient != null) {
                mLocationClient.stop();
            }
        }
    }


    private void stopLocationType(Context context, LocationType mType) {
        Class mClass = null;
        if (mType == LocationType.GPS) {
            mClass = GpsLocationService.class;
        } else if (mType == LocationType.NETWORK) {
            mClass = NetLocationService.class;
        } else if (mType == LocationType.COMMON) {
            mClass = CommonService.class;
        }
        Intent intent = new Intent(context, mClass);
        context.stopService(intent);
    }

    private boolean serviceIsAlive(Context context, LocationType mType) {
        BaseService mBaseService = null;
        String serviceName = "";
        if (mType == LocationType.GPS) {// GPS
            serviceName = GpsLocationService.class.getName();
        } else if (mType == LocationType.NETWORK) {
            serviceName = NetLocationService.class.getName();
        } else if (mType == LocationType.COMMON) {
            serviceName = CommonService.class.getName();
        }
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(1000);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断开关是否打开
     */
    private boolean isLocationSwitchOpen(Context mContext, LocationType mType) {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean mProviderEnabled = false;
        if (mType == LocationType.GPS) {
            mProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } else if (mType == LocationType.NETWORK) {
            mProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } else if (mType == LocationType.BAIDU) {
            mProviderEnabled = true;
        } else if (mType == LocationType.COMMON) {
            boolean isNetOpen = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean isGpsOpen = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            mProviderEnabled = isNetOpen && isGpsOpen;
        }
        return mProviderEnabled;
    }

    private void showSettingSwitchOpenDialog(Context context) {
        android.app.AlertDialog.Builder mBuilder = new android.app.AlertDialog.Builder(context)
                .setTitle("请开启位置服务开关并使用“GPS、WLAN、移动网络”模式")
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface mDialogInterface, int mI) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent); //此为设置完成后返回到获取界面
                        mDialogInterface.dismiss();
                    }
                });
        mBuilder.show();
    }

    /**
     * 移除某一类型的定位
     */
    public void stopTypeLocation(Context context, LocationType mType) {
        ArrayList<HashMap<Context, LocationCallBack>> mArrayList = mLocationRegistMap.get(mType);
        if (mArrayList != null) {
            mArrayList.clear();
        }
        if (mType == LocationType.BAIDU) {
            //百度
            if (mLocationClient != null && mLocationClient.isStarted()) {
                mLocationClient.stop();
            }
        } else {
            stopLocationType(context, mType);
        }
    }


//    public void startLoc(Context context, boolean isBD, boolean isPermanent) {
//        if (isBD) {
//            if (mIntentService != null) {
//                try {
//                    context.stopService(mIntentService);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            if (mLocationClient == null) initLocationClient(context);
//            requestLocation();
//        } else {
//            if (mLocationClient != null) mLocationClient.stop();
//            mIntentService = new Intent(context, CommonService.class);
//            /*context.startService(locIntentSer);*/
//            isBind = context.bindService(mIntentService, conn, BIND_AUTO_CREATE);
//        }
//    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

//    public void stopLoc(Context context) {
//        if (mIntentService != null) {
//            try {
//                //context.stopService(locIntentSer);
//                if (isBind) {
//                    context.unbindService(conn);
//                    isBind = false;
//                } else {
//                    context.stopService(mIntentService);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (mLocationClient != null) mLocationClient.stop();
//
//    }

    private void initLocationClient(Context context) {
        mLocationClient = new com.baidu.location.LocationClient(context.getApplicationContext());
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                Gps gps = PositionUtil.bd09_To_Gps84(bdLocation.getLatitude(), bdLocation.getLongitude());
                ServiceLocation serviceLocation = new ServiceLocation();
                serviceLocation.setLatitude(gps.getWgLat());
                serviceLocation.setLongitude(gps.getWgLon());
                serviceLocation.setLocationType(LocationType.BAIDU.getType());
                EventBus.getDefault().post(serviceLocation);
            }
        });
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        //可选，默认false，设置是否开启Gps定位
        option.setOpenGps(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //定位SDK能够返回三种坐标类型的经纬度（国内），分别是GCJ02（国测局坐标,默认）、BD09（百度墨卡托坐标）和BD09ll（百度经纬度坐标）
        option.setCoorType("BD09ll");
        option.setScanSpan(3000);
        mLocationClient.setLocOption(option);
    }

    public void requestLocation() {
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        mLocationClient.requestLocation();
    }

    private void showWarnDialog(Context mContext) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext)
                .setTitle("请同意以下必备权限,否则无法获取位置信息")
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface mDialogInterface, int mI) {
                        mDialogInterface.dismiss();
                        PermissionUtils.permission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).request();
                    }
                });
        mBuilder.show();
    }

    private boolean hasPermission() {
        if (PermissionUtils.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
            return true;
        }
        return false;
    }

}

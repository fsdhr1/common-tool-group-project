package com.gykj.commontool.locationTest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gykj.commontool.R;
import com.gykj.location.LocationClient;
import com.gykj.location.LocationType;
import com.gykj.location.ServiceLocation;
import com.gykj.location.interfaces.LocationCallBack;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LocationServiceTestActivity2 extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_service);

        /**
         * 公交车 注册
         */
        EventBus.getDefault().register(this);
        //
        findViewById(R.id.bt_GPS).setOnClickListener(this);
        findViewById(R.id.bt_net).setOnClickListener(this);
        findViewById(R.id.bt_bd).setOnClickListener(this);
        findViewById(R.id.bt_1).setOnClickListener(this);

        findViewById(R.id.bt_stop_Gps).setOnClickListener(this);
        findViewById(R.id.bt_stop_Net).setOnClickListener(this);
        findViewById(R.id.bt_stop_bd).setOnClickListener(this);
        findViewById(R.id.bt_stop_common).setOnClickListener(this);

        findViewById(R.id.bt_type_net).setOnClickListener(this);
        findViewById(R.id.bt_type_Gps).setOnClickListener(this);
        findViewById(R.id.bt_type_common).setOnClickListener(this);

        findViewById(R.id.bt_2).setOnClickListener(this);
        findViewById(R.id.bt_common).setOnClickListener(this);


    }


    @Override
    public void onClick(View mView) {
        int mId = mView.getId();
        if (mId == R.id.bt_GPS) {
            getGpsLocation();
        } else if (mId == R.id.bt_net) {
            getNetLocation();
        } else if (mId == R.id.bt_common) {
            getCommonLocation();
        } else if (mId == R.id.bt_bd) {
            getBaiDuLocation();
        } else if (mId == R.id.bt_stop_Gps) {
            stopService(this, LocationType.GPS);
        } else if (mId == R.id.bt_stop_Net) {
            stopService(this, LocationType.NETWORK);
        } else if (mId == R.id.bt_stop_bd) {
            stopService(this, LocationType.BAIDU);
        } else if (mId == R.id.bt_stop_common) {
            stopService(this, LocationType.COMMON);
        } else if (mId == R.id.bt_type_net) {
            stopTypeService(this, LocationType.NETWORK);
        } else if (mId == R.id.bt_type_Gps) {
            stopTypeService(this, LocationType.GPS);
        } else if (mId == R.id.bt_type_common) {
            stopTypeService(this, LocationType.COMMON);
        } else if (mId == R.id.bt_1) {
            Intent intent = new Intent(getBaseContext(), Location_Test1_Activity.class);
            startActivity(intent);
        } else if (mId == R.id.bt_2) {
            Intent intent = new Intent(getBaseContext(), Location_Test2_Activity.class);
            startActivity(intent);
        }
    }

    private void stopTypeService(Context context, LocationType mType) {
        LocationClient.getInstance().stopTypeLocation(context, mType);
    }

    private void stopService(Context context, LocationType mType) {
        LocationClient.getInstance().stopLocation(context, mType);
    }

    private void getCommonLocation() {
        LocationClient.getInstance().startLocation(this, LocationType.COMMON, new LocationCallBack() {
            @Override
            public void onLocationCallBack(Context mContext, ServiceLocation mBean) {
                String text = " --- getCommonLocation " + mBean.getLatitude() + " -- " + mBean.getLongitude() + " -- ";
                Log.e("onLocationCallBack: ", text);
                Toast.makeText(LocationServiceTestActivity2.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBaiDuLocation() {
        LocationClient.getInstance().startLocation(this, LocationType.BAIDU, new LocationCallBack() {
            @Override
            public void onLocationCallBack(Context mContext, ServiceLocation mBean) {
                String text = " --- getBaiDuLocation " + mBean.getLatitude() + " -- " + mBean.getLongitude() + " -- ";
                Log.e("onLocationCallBack: ", text);
                Toast.makeText(LocationServiceTestActivity2.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNetLocation() {
        LocationClient.getInstance().startLocation(this, LocationType.NETWORK, new LocationCallBack() {
            @Override
            public void onLocationCallBack(Context mContext, ServiceLocation mBean) {
                String text = " --- getNetLocation " + mBean.getLatitude() + " -- " + mBean.getLongitude() + " -- ";
                Log.e("onLocationCallBack: ", text);
                Toast.makeText(LocationServiceTestActivity2.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getGpsLocation() {
        LocationClient.getInstance().startLocation(this, LocationType.GPS, new LocationCallBack() {
            @Override
            public void onLocationCallBack(Context mContext, ServiceLocation mBean) {
                String text = " --- getGpsLocation " + mBean.getLatitude() + " -- " + mBean.getLongitude() + " -- ";
                Log.e("onLocationCallBack: ", text);
                Toast.makeText(LocationServiceTestActivity2.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 请求权限的结果的回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

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
//        if (serviceLocation != null)
//            Toast.makeText(this, serviceLocation.getLatitude() + ", " + serviceLocation.getLongitude(), Toast.LENGTH_SHORT)
//                    .show();
    }


}

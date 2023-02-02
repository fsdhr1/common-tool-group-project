package com.gykj.commontool.locationTest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gykj.commontool.R;
import com.gykj.location.LocationClient;
import com.gykj.location.LocationType;
import com.gykj.location.ServiceLocation;
import com.gykj.location.interfaces.LocationCallBack;

import java.util.ArrayList;

public class Location_Test2_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_service_test2);


        LocationClient.getInstance().startLocation(Location_Test2_Activity.this, LocationType.NETWORK, new LocationCallBack() {
            @Override
            public void onLocationCallBack(Context mContext, ServiceLocation mBean) {
                Log.e("onLocationCallBack: ", " --- Test 222 " + mBean.getLatitude() + " -- " + mBean.getLongitude());
            }
        });

        findViewById(R.id.bt_test1_net_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                //
                LocationClient.getInstance().startLocation(Location_Test2_Activity.this, LocationType.NETWORK, new LocationCallBack() {
                    @Override
                    public void onLocationCallBack(Context mContext, ServiceLocation mBean) {
                        Log.e("onLocationCallBack: ", " --- Test 222 " + mBean.getLatitude() + " -- " + mBean.getLongitude());
                    }
                });
                //
                ActivityManager myManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(1000);
                for (int i = 0; i < runningService.size(); i++) {
                    Log.i("onLocationCallBack：",""+runningService.get(i).service.getClassName().toString());
                }
                Log.i("onLocationCallBack：",""+runningService.size());
            }
        });

        findViewById(R.id.bt_test1_net_end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                LocationClient.getInstance().stopLocation(Location_Test2_Activity.this, LocationType.NETWORK);
            }
        });

    }

}
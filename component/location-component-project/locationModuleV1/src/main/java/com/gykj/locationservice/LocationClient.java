package com.gykj.locationservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.greenrobot.eventbus.EventBus;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * 定位服务对外启动类
 */
public class LocationClient {
    private static LocationClient locationClient;
    private Intent locIntentSer;
    private boolean isPermanent = false;

    private boolean isBind = false;// 判断服务是否注册

    public static synchronized LocationClient getInstance() {
        if (locationClient == null) {
            locationClient = new LocationClient();
        }
        return locationClient;
    }

    public void startLoc(Context context, boolean isBD, boolean isPermanent) {
        if (this.isPermanent) return;
        this.isPermanent = isPermanent;//判断是不是常驻
        if (isBD) {
            if (locIntentSer != null) {
                try {
                    context.stopService(locIntentSer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            locIntentSer = new Intent(context, LocService.class);
            /*context.startService(locIntentSer);*/
            isBind = context.bindService(locIntentSer, conn, BIND_AUTO_CREATE);
        }
    }

    private boolean isBinderService = false;
    private ServiceConnection conn = new ServiceConnection() {
        private ServiceLocation _serviceLocation;

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            isBinderService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBinderService = false;
        }
    };

    public void stopLoc(Context context) {
        if (isPermanent) return;
        if (locIntentSer != null) {
            try {
                //context.stopService(locIntentSer);
                if (isBind) {
                    context.unbindService(conn);
                    isBind = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

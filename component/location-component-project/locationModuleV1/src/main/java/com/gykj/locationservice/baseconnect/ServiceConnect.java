package com.gykj.locationservice.baseconnect;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2017/9/5.
 */

public class ServiceConnect implements ServiceConnection {

    private boolean isServiceConnect;

    private List<IConnect> iConnects;

    public ServiceConnect() {
        init();
        this.isServiceConnect = false;
    }

    public ServiceConnect(IConnect iConnect) {
        init();
        if (iConnects != null && !iConnects.contains(iConnect)) {
            this.iConnects.add(iConnect);
        }
        this.isServiceConnect = false;
    }

    public void setIConnect(IConnect iConnect) {
        if (iConnects != null && !iConnects.contains(iConnect)) {
            this.iConnects.add(iConnect);
        }
    }

    private void init() {
        iConnects = new ArrayList<>();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (iConnects != null) {
            for (int i = 0; i < iConnects.size(); i++) {
                iConnects.get(i).isConnected(iBinder);
            }
        }
        isServiceConnect = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        destroy();
    }

    public boolean isServiceConnect() {
        return isServiceConnect;
    }

    public void destroy() {
        if (iConnects != null) {
            for (int i = 0; i < iConnects.size(); i++) {
                iConnects.get(i).disConnected();
            }
            iConnects.clear();
        }
        isServiceConnect = false;
    }
}
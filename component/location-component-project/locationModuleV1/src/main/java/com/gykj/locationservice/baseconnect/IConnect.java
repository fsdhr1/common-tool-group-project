package com.gykj.locationservice.baseconnect;

import android.os.IBinder;

/**
 * Created by zy on 2017/9/5.
 */

public interface IConnect {
    public void isConnected(IBinder iBinder);

    public void disConnected();
}

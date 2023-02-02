package com.gykj.locationservice.baseservices.v2.i;

/**
 * Created by zy on 2017/11/9.
 */

public interface IBaseServiceLifeCycle {
    public void serviceCreate();

    public void serviceStartCommand();

    public void serviceRunning(Object o);

    public void serviceUnBinder();

    public void serviceDestroy();
}

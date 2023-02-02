package com.gykj.locationservice.baseservices.v2.binder;

import android.os.Binder;

import com.gykj.locationservice.baseservices.v2.BaseService;


/**
 * Created by zy on 2017/11/9.
 */

public class BaseServiceBinder extends Binder {
    private BaseService baseService;

    public BaseService getBaseService() {
        return baseService;
    }

    public void setBaseService(BaseService baseService) {
        this.baseService = baseService;
    }


}

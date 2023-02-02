package com.gykj.commontool.networktest;

import com.gykj.commontool.networktest.beans.AdministrativeResultBean;
import com.gykj.networkmodule.IBaseApi;
import com.gykj.networkmodule.RealApi;
import com.gykj.networkmodule.RequestBuilder;

@RealApi(com.gykj.commontool.networktest.MapApi.class)
public interface TestMapApi extends IBaseApi {
    RequestBuilder getGeos(java.lang.String name);

    RequestBuilder getTags(java.lang.String username);

    RequestBuilder<AdministrativeResultBean> getAdministrativeByCoor(java.lang.String lon, java.lang.String lat);
}
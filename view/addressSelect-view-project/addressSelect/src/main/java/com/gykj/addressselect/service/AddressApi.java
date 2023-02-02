package com.gykj.addressselect.service;


import java.util.HashMap;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by ZhaiJiaChang.
 * <p>
 */
public interface AddressApi {

    @POST(Constant.SERVER_GSS_SYS_SERVER + "api/v1/sys/area/subarea")
    Observable<String> getAddressByQhdm(@Body() HashMap mHashMap);

    // 获取乡镇下的村
    @GET(Constant.SERVER_GSS_SYS_SERVER + "api/v1/area/getCunByXiang")
    Observable<String> httpVillages(@Query("code") String code);
}

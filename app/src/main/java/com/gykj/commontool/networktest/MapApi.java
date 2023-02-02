package com.gykj.commontool.networktest;

import java.util.List;


import com.gykj.commontool.networktest.beans.AdministrativeResultBean;
import com.gykj.commontool.networktest.beans.GeoBean;
import com.gykj.commontool.networktest.beans.SourceInfoBean;
import com.gykj.commontool.networktest.beans.TagBean;
import com.gykj.networkmodule.beans.DataResponse;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * @author zyp
 * 2021/6/16
 */
public interface MapApi {
    @GET("http://gykj123.cn:8849/grandtech-middleground-service/api/v1/tbdmEnumeratordomain/getXzqInfoList")
    Observable<DataResponse<List<GeoBean>>> getGeos(@Query("name") String name);

    @GET("http://gykj123.cn:8849/grandtech-service-snyzt/api/v1/bookmark/get_bookmark")
    Observable<DataResponse<List<TagBean>>> getTags(@Query("username")String username);

    @GET("api/v1/xzq/getXzqInfoByPt")
    Observable<AdministrativeResultBean> getAdministrativeByCoor(@Query("lon")String lon, @Query("lat")String lat);

    @GET
    Observable<SourceInfoBean> getSourceInfo(@Header("Authorization") String token, @Url String url);
}

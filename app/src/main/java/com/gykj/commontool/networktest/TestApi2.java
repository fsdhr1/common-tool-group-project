package com.gykj.commontool.networktest;

import com.gykj.commontool.networktest.beans.WeatherResultBean;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * @author zyp
 * 2021/3/1
 */
public interface TestApi2 {
    /**
     * 获取天气
     * @param city 城市名称 ex:沈阳 沈阳市
     */
    @PUT("http://wthrcdn.etouch.cn/weather_mini")
    Observable<WeatherResultBean> getWeatherInfo(@Query("city") String city);


    /**
     * 获取天气
     * @param city 城市名称 ex:沈阳 沈阳市
     */
    @POST("http://wthrcdn.etouch.cn/weather_mini")
    Observable<WeatherResultBean> getWeatherInfo2(@Query("city") String city);
}

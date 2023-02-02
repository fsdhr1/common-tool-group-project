package com.grandtech.mapframe.core.net;

import android.util.Log;

import com.grandtech.mapframe.core.util.SimpleMap;
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @ClassName BoxHttpClient
 * @Description TODO 地图HttpClient
 * @Author: fs
 * @Date: 2021/5/11 13:16
 * @Version 2.0
 */
public final class BoxHttpClient {

    private OkHttpClient client;

    private SimpleMap<String, Long> expiresMap;

    private String mToken ;

    private static volatile BoxHttpClient singleton;

    private List<Interceptor> interceptors;

    private BoxHttpClient() {
        client = initBuilder().dispatcher(getDispatcher()).build();
        HttpRequestUtil.setOkHttpClient(client);
        expiresMap = new SimpleMap<>();
    }

    public static BoxHttpClient getSingleton() {
        if (singleton == null) {
            synchronized (BoxHttpClient.class) {
                if (singleton == null) {
                    singleton = new BoxHttpClient();
                }
            }
        }
        return singleton;
    }

    /**
     * 判断是否初始化
     * @return
     */
    public static boolean isInit() {
        if (singleton == null) {
           return false;
        }
        return true;
    }
    /**
     * 清空拦截器
     */
    public void clearInterceptor(){
        interceptors = null;
    }

    /**
     * 设置网络请求Token
     * @param mToken
     */
    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    public BoxHttpClient addInterceptor(Interceptor interceptor) {
        if(interceptors == null){
            interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
        client = initBuilder(interceptors).dispatcher(getDispatcher()).build();
        HttpRequestUtil.setOkHttpClient(client);
        return this;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void addLayerExpires(String layerName, Long expiresTime) {
        expiresMap.push(layerName, expiresTime);
    }

    public void removeLayerExpires(String layerName) {
        expiresMap.remove(layerName);
    }

    private OkHttpClient.Builder initBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(getInterceptor());
        return builder;
    }
    private OkHttpClient.Builder initBuilder(List<Interceptor> interceptors) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(getInterceptor());
        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }
        return builder;
    }

    private Dispatcher getDispatcher() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(15);
        return dispatcher;
    }

    public Interceptor getInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                String requestUrl = chain.request().url().toString();
                Log.i("gmbgl-BoxHttpClient",requestUrl);
                Long request_time = getStandardUtcTime();
                Long expires_time = dispatchExpiresTime(requestUrl);
                Request.Builder builder = request.newBuilder();
                builder.url(requestUrl);
                if(mToken!=null){
                    builder.addHeader("Authorization", mToken);
                }
                builder.addHeader("request_time", request_time + "");
                if (expires_time != null) {
                    builder.addHeader("expires_time", expires_time.toString());
                }

                Response response = chain.proceed(builder.build());
                return response;
            }
        };
    }
    private Long dispatchExpiresTime(String url) {
        if (expiresMap == null) {
            return null; //ms
        } else {
            for (String layerName : expiresMap.keySet()) {
                if (url.contains(layerName + "/")) {
                    return expiresMap.get(layerName);
                }
            }
        }
        return null; //ms
    }
    /**
     * utc时间
     *
     * @return
     */
    private  long getStandardUtcTime() {
        Calendar cal = Calendar.getInstance();
        long mills = cal.getTimeInMillis();
        return mills;
    }
}

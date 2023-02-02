package com.gykj.addressselect.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitUtil {
//    private static String mToken = "";

    //连接超时
    private static long CONNECT_TIMEOUT = 60L;
    //阅读超时
    private static long READ_TIMEOUT = 10L;
    //写入超时
    private static long WRITE_TIMEOUT = 10L;

    private static volatile OkHttpClient mOkHttpClient;
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    /**
     * 响应头拦截器，用来配置缓存策略
     */
    private static final Interceptor mRewriteCacheControlInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request().newBuilder()
                    .addHeader("appId", "4e753883766b44dea6a07b2c3fbe7e90")
//                    .addHeader("Authorization", mToken)
                    .build();//获得上一个请求
            Response originalResponse = chain.proceed(request);
            return originalResponse;
        }
    };

    private static final HttpLoggingInterceptor mLoggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    /**
     * 获取OkHttpClient实例
     *
     * @return
     */
    private static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            synchronized (RetrofitUtil.class) {
                if (mOkHttpClient == null) {
                    mOkHttpClient = new OkHttpClient.Builder()
                            .eventListenerFactory(OkHttpEventListener.FACTORY)
                            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                            .addInterceptor(mRewriteCacheControlInterceptor)
                            .addInterceptor(mLoggingInterceptor)
                            .build();
                }
            }
        }
        return mOkHttpClient;
    }

    public static <T> T create(Class<T> clazz) {
//        mToken = token;
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Config.BASE_URL)
                .client(getOkHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(clazz);
    }

}

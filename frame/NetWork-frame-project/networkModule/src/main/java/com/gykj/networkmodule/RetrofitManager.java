package com.gykj.networkmodule;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.Cache;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * @author zyp
 * 2019-05-09
 */
public class RetrofitManager {
    //连接超时
    public long CONNECT_TIMEOUT = 60L;
    //阅读超时
    public long READ_TIMEOUT = 10L;
    //写入超时
    public long WRITE_TIMEOUT = 10L;
    //设缓存有效期为1天
    private static final long CACHE_STALE_SEC = 60 * 60 * 24 * 1;
    //查询缓存的Cache-Control设置，为only-if-cached时只查询缓存而不会请求服务器，max-stale可以配合设置缓存失效时间
   // public static final String CACHE_CONTROL_CACHE = "only-if-cached, max-stale=" + CACHE_STALE_SEC;
    //查询网络的Cache-Control设置
    //(假如请求了服务器并在a时刻返回响应结果，则在max-age规定的秒数内，浏览器将不会发送对应的请求到服务器，数据由缓存直接返回)
    //public static final String CACHE_CONTROL_NETWORK = "Cache-Control: public, max-age=10";
    // 避免出现 HTTP 403 Forbidden，参考：http://stackoverflow.com/questions/13670692/403-forbidden-with-java-but-not-web-browser
    private static final String AVOID_HTTP403_FORBIDDEN = "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
    private volatile OkHttpClient mOkHttpClient;
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private String token = "";
    private String baseUrl = "";
    private Set<Interceptor> customerInterceptors = new HashSet<>();
    private static Map<String,String> globalHeaders = new HashMap<>();
    RetrofitManager(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 响应头拦截器，用来配置缓存策略
     */
    private final Interceptor mRewriteCacheControlInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Headers headers = chain.request().headers();
            Set<String> headerNames = headers.names();
            Request.Builder builder = chain.request().newBuilder();
            if (!headerNames.contains("Authorization")) {
                builder.addHeader("Authorization", token);
            }
            for (String headerName : globalHeaders.keySet()) {
                if(!headerNames.contains(headerName)){
                    builder.addHeader(headerName, Objects.requireNonNull(globalHeaders.get(headerName)));
                }
            }
            Request request = builder.build();
            Response originalResponse = chain.proceed(request);
            if (NetworkUtils.isConnected()) {
                //有网的时候读接口上的@Headers里的配置，可以在这里进行统一的设置
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma")
                        .build();
            }
            return originalResponse;
        }
    };

    String getToken() {
        return token;
    }

    void setToken(String token) {
        this.token = token;
    }

    String getBaseUrl() {
        return baseUrl;
    }

    void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private static final HttpLoggingInterceptor mLoggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);
    /**
     * 日志拦截器
     */
    /*private static final Interceptor mLoggingIntercepter = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            String isSuccess = response.isSuccessful() ? "true" : "false";
            //Logger.w(isSuccess);
            ResponseBody body = response.body();
            BufferedSource source = body.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = Charset.defaultCharset();
            MediaType contentType = body.contentType();
            if (contentType != null && contentType.charset() != null) {
                charset = contentType.charset();
            }
            String bodyString = buffer.clone().readString(charset);
            //Logger.w(String.format("Received response json string " + bodyString));
            return response;
        }
    };*/

    /**
     * 获取OkHttpClient实例
     *
     * @return
     */
    private OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            synchronized (RetrofitManager.class) {
                Cache cache = new Cache(new File(Utils.getApp().getCacheDir(), "HttpCache"), 1024 * 1024 * 100);
                if (mOkHttpClient == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder()
                            .eventListenerFactory(OkHttpEventListener.FACTORY)
                            //.dns(OkHttpDNS.getInstance(App.mContext))
                            .cache(cache)
                            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                            .addInterceptor(mRewriteCacheControlInterceptor)
                            .addNetworkInterceptor(mLoggingInterceptor);
                    for (Interceptor customerInterceptor : customerInterceptors) {
                        builder.addInterceptor(customerInterceptor);
                    }
                    mOkHttpClient = builder.build();
                }
            }
        }
        return mOkHttpClient;
    }


    <T> T create(Class<T> clazz) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                .client(getOkHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                //.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(clazz);
    }

    void addNetInterceptor(Interceptor interceptor) {
        if (!customerInterceptors.contains(interceptor)) {
            mOkHttpClient = null;
            customerInterceptors.add(interceptor);
        }
    }

    void addGlobalHeader(String headerName, String headerValue) {
        globalHeaders.put(headerName,headerValue);
    }
}

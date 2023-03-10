package com.gykj.autoupdate.net;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Http网络工具,基于OkHttp
 */

public class HttpUtil {
    private OkHttpClient mOkHttpClient;
    private static HttpUtil mInstance;
    private final static long CONNECT_TIMEOUT = 60;//超时时间，秒
    private final static long READ_TIMEOUT = 60;//读取时间，秒
    private final static long WRITE_TIMEOUT = 60;//写入时间，秒

    /**
     * @param url        下载链接
     * @param startIndex 下载起始位置
     * @param endIndex   结束为止
     * @param callback   回调
     * @throws IOException
     */
    public void downloadFileByRange(String url, long startIndex, long endIndex, Callback callback) throws IOException {
        // 创建一个Request
        // 设置分段下载的头信息。 Range:做分段数据请求,断点续传指示下载的区间。格式: Range bytes=0-1024或者bytes:0-1024
        Request request = new Request.Builder().header("RANGE", "bytes=" + startIndex + "-" + endIndex)
                .url(url)
                .build();
        doAsync(request, callback);
    }

    public void getContentLength(String url, Callback callback) throws IOException {
        // 创建一个Request
        Request request = new Request.Builder()
                .url(url)
                .build();
        doAsync(request, callback);
    }

    /**
     * 异步请求
     */
    private void doAsync(Request request, Callback callback) throws IOException {
        //创建请求会话
        Call call = mOkHttpClient.newCall(request);
        //同步执行会话请求
        call.enqueue(callback);
    }

    /**
     * 同步请求
     */
    private Response doSync(Request request) throws IOException {

        //创建请求会话
        Call call = mOkHttpClient.newCall(request);
        //同步执行会话请求
        return call.execute();
    }


    /**
     * @return HttpUtil实例对象
     */
    public static HttpUtil getInstance() {
        if (null == mInstance) {
            synchronized (HttpUtil.class) {
                if (null == mInstance) {
                    mInstance = new HttpUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * 构造方法,配置OkHttpClient
     */
    public HttpUtil() {
        //创建okHttpClient对象
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        mOkHttpClient = builder.build();
    }

    public void postBody(String url, HashMap<String, Object> paramsMap, Callback callback) throws IOException {
        // RequestBody
        String bodyStr =new Gson().toJson(paramsMap);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyStr);// 设置编码格式

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Log.i(this.getClass().getSimpleName(), "开始网络请求...");
        Log.i(this.getClass().getSimpleName(), "请求地址：" + url);
        Log.i(this.getClass().getSimpleName(), "header：" + request.headers().toString());
        Log.i(this.getClass().getSimpleName(), "入参：" + bodyStr);
        doAsync(request, callback);
    }
}

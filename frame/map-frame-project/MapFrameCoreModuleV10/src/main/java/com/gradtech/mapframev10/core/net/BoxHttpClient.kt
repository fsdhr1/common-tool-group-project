package com.gradtech.mapframev10.core.net

import android.util.Log
import com.mapbox.common.*
import java.util.*
import java.util.Collections.singleton

/**
 * @ClassName BoxHttpClient
 * @Description TODO 一些网络设置
 * @Author: fs
 * @Date: 2022/12/28 8:35
 * @Version 2.0
 */
class BoxHttpClient  : HttpServiceInterceptorInterface{


    companion object {
        private var instance: BoxHttpClient? = null
        @Synchronized
        fun instance(): BoxHttpClient {
            if (instance == null) instance = BoxHttpClient()
            return instance!!
        }
        fun cancel() {
            instance?.clearInterceptors();
            instance = null
        }
    }

    private var mInterceptors : ArrayList<HttpServiceInterceptorInterface> = arrayListOf();

    private val expiresMap:HashMap<String, Long> = hashMapOf<String, Long>()

    private var mToken : String? = null;

    private constructor(){
        HttpServiceFactory.getInstance().setInterceptor(this);
    }

    public fun addInterceptors(httpServiceInterceptorInterface: HttpServiceInterceptorInterface){
        mInterceptors.add(httpServiceInterceptorInterface);
    }

    fun clearInterceptors(){
        mInterceptors.clear();
    }

    fun addLayerExpires(layerName: String, expiresTime: Long) {
        expiresMap.put(layerName, expiresTime)
    }

    fun removeLayerExpires(layerName: String?) {
        expiresMap.remove(layerName)
    }

    /**
     * 设置网络请求Token
     * @param mToken
     */
    fun setToken(token: String) {
        this.mToken = token
    }

    override fun onRequest(request: HttpRequest): HttpRequest {
        val requestUrl: String = request.url;
        Log.i("gmbgl-BoxHttpClient", requestUrl)
        val request_time = getStandardUtcTime()
        val expires_time = dispatchExpiresTime(requestUrl)
        val headers = request.headers
        headers.put("request_time", request_time.toString() + "")
        if (expires_time != null) {
            headers.put("expires_time", expires_time.toString())
        }
        if (mToken != null) {
            headers.put("Authorization", mToken)
        }
        var build = request.toBuilder().headers(headers).build()
        //外部的拦截器
        for (mInterceptor in mInterceptors) {
            build = mInterceptor.onRequest(build)
        }
        return build
    }

    override fun onDownload(download: DownloadOptions): DownloadOptions {
        var download1: DownloadOptions = download;
        for (mInterceptor in mInterceptors) {
            download1 = mInterceptor.onDownload(download1)
        }
        return download1;
    }

    override fun onResponse(response: HttpResponse): HttpResponse {
        var response1: HttpResponse = response;
        for (mInterceptor in mInterceptors) {
            response1 = mInterceptor.onResponse(response1)
        }
        return response1
    }

    private fun dispatchExpiresTime(url: String): Long? {
        if (expiresMap == null) {
            return null //ms
        } else {
            for (layerName in expiresMap.keys) {
                if (url.contains("$layerName/")) {
                    return expiresMap.get(layerName)
                }
            }
        }
        return null //ms
    }

    /**
     * utc时间
     *
     * @return
     */
    private fun getStandardUtcTime(): Long {
        val cal = Calendar.getInstance()
        return cal.timeInMillis
    }
}
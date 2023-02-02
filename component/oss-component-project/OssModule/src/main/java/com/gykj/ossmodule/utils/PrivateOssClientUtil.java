package com.gykj.ossmodule.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.google.gson.Gson;
import com.gykj.ossmodule.BaseConfig.Constants;
import com.gykj.ossmodule.bean.OssFileBean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by ren on 2021/4/8
 */
public class PrivateOssClientUtil {
    private static final HttpLoggingInterceptor mLoggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    /**
     * 获取私有云上传签名
     */
    public static class GetTokenTask extends AsyncTask<String, Integer, String> {
        public interface TokenCallBack {
            void getTokenSuccess(String token);
        }

        TokenCallBack mCallBack;
        private String mUrl ;

        public GetTokenTask(String url,TokenCallBack tokenCallBack) {
            this.mCallBack = tokenCallBack;
            mUrl = url;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            if(mUrl == null){
                return null;
            }
            String token = "";
            Request request = new Request.Builder()
                    .url(mUrl)
                    .get()
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(mLoggingInterceptor)
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                String responseStr = response.body().string();
                Map responseMap = new Gson().fromJson(responseStr, Map.class);
                if (responseMap != null && responseMap.get("result") != null && !TextUtils.isEmpty(responseMap.get("result").toString())) {
                    token = responseMap.get("result").toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return token;
        }


        @Override
        protected void onPostExecute(String s) {
            mCallBack.getTokenSuccess(s);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}

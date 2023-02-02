package com.gykj.ossmodule.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

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
public class OssClientUtil {
    private final String mToken;
    private final Context mContext;
    private Map<OssFileBean, OSSClient> mOssClientList = new HashMap<>();

    // 网络拦截器
    private static final HttpLoggingInterceptor mLoggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

//    private static final Interceptor myResponseInterceptor = new Interceptor() {
//        @Override
//        public Response intercept(Chain chain) throws IOException {
//            Request request = chain.request();
//            System.out.println("url：" + request.url().toString());
//            System.out.println("入参：" + GsonUtils.toJson(request.body()));
//
//            Response response = chain.proceed(request);
//            String responseStr = response.body().string();
//            System.out.println("出参：" + responseStr);
//
//            return response.newBuilder()
//                    .body(ResponseBody.create(response.body().contentType(), responseStr))
//                    .build();
//        }
//    };

    public OssClientUtil(Context context, String token) {
        this.mContext = context;
        this.mToken = token;
    }

    /**
     * 获取OSSClient或者初始化OSSClient
     *
     * @param ossFileBean
     * @return
     */
    public OSSClient getOssClient(OssFileBean ossFileBean) {
        OSSClient ossClient = mOssClientList.get(ossFileBean);
        if (ossClient == null) {
            ossClient = initOssClient(ossFileBean);
            mOssClientList.put(ossFileBean, ossClient);
        }
        return ossClient;
    }

    /**
     * 初始化ossClient
     *
     * @param ossFileBean
     * @return
     */
    private OSSClient initOssClient(OssFileBean ossFileBean) {
        OSSCredentialProvider credentialProvider = new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String content) {
                String url = null;
                try {
                    url = Constants.OSS_GET_SIGNATURE + "?s=" + URLEncoder.encode(content, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }

                Request request = new Request.Builder()
                        .url(url)
                        .header("Signature", mToken)
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(mLoggingInterceptor)
                        .build();
                Call call = okHttpClient.newCall(request);
                try {
                    Response response = call.execute();
                    String sign = response.body().string();
                    return "OSS " + Constants.OSS_ACCESSKEYID + ":" + sign;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        // 配置类如果不设置，会有默认配置。
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒。
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒。
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个。
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次。

        OSSLog.enableLog();

        OSSClient ossClient = new OSSClient(mContext, ossFileBean.getEndpoint(), credentialProvider, conf);
        return ossClient;
    }


    /**
     * 获取国源token
     */
    public static class GetTokenTask extends AsyncTask<String, Integer, String> {
        public interface TokenCallBack {
            void getTokenSuccess(String token);
        }

        TokenCallBack mCallBack;

        public GetTokenTask(TokenCallBack tokenCallBack) {
            this.mCallBack = tokenCallBack;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String token = "";
            String mUrl = Constants.GET_TOKEN + "?key=" + strings[0] + "&secret=" + strings[1];
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

package com.gykj.commontool.networktest;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.gykj.commontool.R;
import com.gykj.commontool.networktest.beans.SourceInfoBean;
import com.gykj.commontool.networktest.beans.WeatherResultBean;
import com.gykj.networkmodule.NetworkHelper;
import com.gykj.networkmodule.RequestBuilder;
import com.gykj.networkmodule.RequestHandler;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @author zyp
 * 2021/3/1
 */
public class NetworkTestActivity extends AppCompatActivity {

    private TextView mTv_log;
    private StringBuilder mStringBuilder;
    private NetworkHelper.NetWorkResult<WeatherResultBean> mNetWorkResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networktest);
        mTv_log = findViewById(R.id.tv_log);
        mTv_log.setMovementMethod(ScrollingMovementMethod.getInstance());
        mStringBuilder = new StringBuilder();
        mNetWorkResult = new NetworkHelper.NetWorkResult<WeatherResultBean>() {
            @Override
            public void onStart(Disposable disposable) {
                //loading
                mStringBuilder.append("开始请求网络\n");
                mTv_log.setText(mStringBuilder.toString());
            }

            @Override
            public void onRequestInvoke(Observable observable) {
                mStringBuilder.append("开始执行请求\n");
                mTv_log.setText(mStringBuilder.toString());
            }

            @Override
            public void onRequestEnd() {
                //loading end
                mStringBuilder.append("请求返回结束!\n");
                mTv_log.setText(mStringBuilder.toString());
            }

            @Override
            public void onSuccess(WeatherResultBean data) {
                mStringBuilder.append(data.toString());
                mStringBuilder.append("\n");
                mTv_log.setText(mStringBuilder.toString());
            }

            @Override
            public void onError(int err, String errMsg, Object[] args ,Throwable t, WeatherResultBean data) {

            }
        };
    }

    public void requestNetwork(View view) {
        NetworkHelper networkHelper = NetworkHelper.getInstance();
        networkHelper.request(TestApi.class, mNetWorkResult).getWeatherInfo("沈阳");
    }

    public void newRequest(View view) {
        //获取请求Handler
        RequestHandler requestHandler = NetworkHelper.getInstance().getRequestHandler();
        //构建请求
        RequestBuilder requestBuilder = requestHandler.buildRequest(ITestApiProxy.class).getWeatherInfo("沈阳").callBack(mNetWorkResult);
        RequestBuilder requestBuilder1 = requestHandler.buildRequest(ITestApiProxy.class).getWeatherInfo("大连").callBack(mNetWorkResult);
        RequestBuilder requestBuilder2 = requestHandler.buildRequest(ITestApiProxy.class).getWeatherInfo("重庆").callBack(mNetWorkResult);
        requestBuilder.request();//直接请求
       // requestBuilder.preRequest(requestBuilder1).request();//在执行这个请求之前请求
       // requestBuilder.postRequest(requestBuilder1).postRequest(requestBuilder2).request();//在执行这个请求之后请求
        //requestHandler.buildOrderedRequest(requestBuilder,requestBuilder1,requestBuilder2).request();//链式请求 上一个请求返回成功后再执行下一个请求
  /*     requestHandler.buildRequest(TestMapApi.class).getAdministrativeByCoor("101.7363421","37.3463561").callBack(new NetworkHelper.NetWorkResult<AdministrativeResultBean>() {
           @Override
           public void onRequestInvoke(Observable observable) {
               
           }

           @Override
           public void onRequestEnd() {

           }

           @Override
           public void onSuccess(AdministrativeResultBean data) {
               String administrative = data.getAdministrative();
               System.out.println();
           }
       }).request();
        
       requestHandler.buildRequest(IMapApiProxy.class).getAdministrativeByCoor("101.7363421","37.3463561").callBack(new NetworkHelper.NetWorkResult<AdministrativeResultBean>() {
           @Override
           public void onRequestInvoke(Observable observable) {
               
           }

           @Override
           public void onRequestEnd() {

           }

           @Override
           public void onSuccess(AdministrativeResultBean data) {

           }
       });*/
    }
    
    public void geoTest(View view){
        RequestHandler requestHandler = NetworkHelper.getInstance().getRequestHandler();
        requestHandler.buildRequest(IMapApiProxy.class).getSourceInfo("","https://api.grandtechmap.com/vector-service/api/v1/tilesets/gykj/dt_zzdk?access_token=tiletoken.mtyynje0mtm4mjyzotzjztgzmmqzzdmzzjrmogjhndvjowqwowjjy2m2njrk")
                .callBack(new NetworkHelper.NetWorkResult<SourceInfoBean>() {
                    @Override
                    public void onRequestInvoke(Observable observable) {

                    }

                    @Override
                    public void onRequestEnd() {

                    }

                    @Override
                    public void onSuccess(SourceInfoBean data) {
                        System.out.println();
                    }
                }).request();
        
    }
}

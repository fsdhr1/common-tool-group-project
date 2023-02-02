package com.gykj.networkmodule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.collection.ArrayMap;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @author zyp
 * 2021/5/27
 */
public class RequestBuilder<R> {
    private Class clazz;
    private Method mMethod;
    private List<Object> params;
    private NetworkHelper.NetWorkResult mNetWorkResult;
    private Object hashObj;
    public RequestBuilder(Class clazz, Method method, Object hashObj, List<Object> params) {
        this.clazz = clazz;
        mMethod = method;
        this.hashObj = hashObj;
        this.params = params;
    }
    
    public RequestBuilder<R> callBack(NetworkHelper.NetWorkResult<R> netWorkResult){
        mNetWorkResult = netWorkResult;
        return this;
    }
    public void request(){
        Object request = NetworkHelper.getInstance().request(clazz, mNetWorkResult,hashObj);
        Object[] args = new Object[params.size()];
        int i = 0;
        for (Object param : params) {
            args[i] = param;
            i++;
        }
        try {
            mMethod.invoke(request,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
        }
    }

    /**
     * 前置请求
     * @param requestBuilder
     * @return
     */
    public RequestBuilder preRequest(RequestBuilder requestBuilder){
        requestBuilder.request();
        return this;
    }

    /**
     * 后置请求
     * @param requestBuilder
     * @return
     */
    public RequestBuilder postRequest(RequestBuilder requestBuilder){
        request();
        return requestBuilder;
    }
    
    void postChainRequest(int requestIndex,RequestBuilder ... requestBuilders){
        Object request = NetworkHelper.getInstance().request(clazz, new NetworkHelper.NetWorkResult<Object>() {
            @Override
            public void onRequestInvoke(Observable observable) {
                if(mNetWorkResult != null){
                    mNetWorkResult.onRequestInvoke(observable);
                }
                
            }

            @Override
            public void onRequestEnd() {
                if(mNetWorkResult != null){
                    mNetWorkResult.onRequestEnd();
                }
               
            }

            @Override
            public void onSuccess(Object data) {
               
            }

            @Override
            public void onSuccess(Object data, Object... args) {
                onSuccess(data);
                if(mNetWorkResult != null){
                    mNetWorkResult.onSuccess(data,args);
                }
                int requestInd = requestIndex + 1;
                if(requestInd < requestBuilders.length){
                    RequestBuilder requestBuilder = requestBuilders[requestInd];
                    requestBuilder.postChainRequest(requestInd,requestBuilders);
                }
            }

            @Override
            public void onStart(Disposable disposable) {
                if(mNetWorkResult != null){
                    mNetWorkResult.onStart(disposable);
                }
               
            }

            @Override
            public void onError(int err, String errMsg, Object[] args,Throwable t, Object data) {
                if(mNetWorkResult != null){
                    mNetWorkResult.onError(err,errMsg,args,t,data);
                }
               
            }
        },hashObj);
        Object[] args = new Object[params.size()];
        int i = 0;
        for (Object param : params) {
            args[i] = param;
            i++;
        }
        try {
            mMethod.invoke(request,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
        }
    }
}

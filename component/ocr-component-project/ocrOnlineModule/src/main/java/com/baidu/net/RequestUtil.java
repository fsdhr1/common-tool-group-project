package com.baidu.net;

import com.baidu.net.bean.DataResponse;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import retrofit2.HttpException;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by jyh on 2019-08-06.
 */

public class RequestUtil {

    public static <C,R> C request(Class<C> c,NetWorkResult<R> r, String token) {
        C IApi = RetrofitUtil.create(c, token);
        Object o = Proxy.newProxyInstance(c.getClassLoader(),
                new Class[]{c}, new InvokeRequestHandler<R>(IApi,r));
        return (C) o;
    }

    public interface NetWorkResult<R>{
        default void onStart(Disposable disposable) {

        }

        void onSuccess(R data);

        default void onError(int err, String errMsg, Throwable t, R data) {

        }
    }

    private static final class InvokeRequestHandler<R> implements InvocationHandler {
        private Object obj;
        private NetWorkResult<R> mNetWorkResult;
        public InvokeRequestHandler(Object o,NetWorkResult<R> netWorkResult){
            obj = o;
            mNetWorkResult = netWorkResult;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object invoke = method.invoke(obj, args);
            if(invoke instanceof Observable){
                Observable<R> observable = (Observable)invoke ;
                Observable<R> finalObservable = observable;
                Observable<R> finalObservable1 = observable;
                Disposable disposable =
                        observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<R>() {
                            @Override
                            public void accept(R dataResponse) throws Exception {
                                if (dataResponse instanceof DataResponse) {
                                    int code = ((DataResponse) dataResponse).getCode();
                                    if (code == 0) {
                                        mNetWorkResult.onSuccess(dataResponse);
                                    } else {
                                        String value = getRequestUrl(method);
                                        ToastUtils.showShort(((DataResponse) dataResponse).getMsg());
                                        LogUtils.eTag("NetworkError", ((DataResponse) dataResponse).getCode() + "---" +
                                                ((DataResponse) dataResponse).getMsg() + "---" + value);
                                        mNetWorkResult.onError(((DataResponse) dataResponse).getCode(),
                                                ((DataResponse) dataResponse).getMsg(), null, dataResponse);
                                    }
                                } else {
                                    mNetWorkResult.onSuccess(dataResponse);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                String value = getRequestUrl(method);
                                if (throwable instanceof HttpException) {
                                    HttpException httpException = (HttpException) throwable;
                                    ToastUtils.showShort("加载失败:" + httpException.getMessage());

                                    LogUtils.eTag("NetworkError", httpException.response().raw().toString());
                                    if (mNetWorkResult != null) {
                                        mNetWorkResult.onError(httpException.code(), throwable.getMessage(), throwable, null);
                                    }
                                } else if (throwable instanceof SocketTimeoutException) {
                                    //SocketTimeoutException socketTimeoutException = (SocketTimeoutException) throwable;
                                    ToastUtils.showShort("加载失败:" + "响应超时");
                                    LogUtils.eTag("NetworkError", "响应超时:" + value);
                                    if (mNetWorkResult != null) {
                                        mNetWorkResult.onError(-2, throwable.getMessage(), throwable, null);
                                    }
                                } else if (throwable instanceof UnknownHostException) {
                                    UnknownHostException unknownHostException = (UnknownHostException) throwable;
                                    ToastUtils.showShort("未知主机:" + unknownHostException.getMessage());
                                    LogUtils.eTag("NetworkError", "未知主机:" +
                                            unknownHostException.getMessage() + "----" + value);
                                } else {
                                    ToastUtils.showShort("加载失败:" + throwable.getMessage());
                                    LogUtils.eTag("NetworkError", throwable.getMessage() + "::" + getRequestUrl(method));
                                    if (mNetWorkResult != null) {
                                        mNetWorkResult.onError(-1, throwable.getMessage(), throwable, null);
                                    }
                                }
                                throwable.printStackTrace();
                            }
                        });
                mNetWorkResult.onStart(disposable);
            }
            return invoke;
        }
    }

    private static String getRequestUrl(Method method) {
        String value = "";
        POST postAnnotation = method.getAnnotation(POST.class);
        GET getAnnotation = method.getAnnotation(GET.class);
        DELETE deleteAnnotation = method.getAnnotation(DELETE.class);
        if(postAnnotation != null){
            value = "post:"+postAnnotation.value();
        }else if(getAnnotation != null){
            value = "get:"+getAnnotation.value();
        }else if(deleteAnnotation != null){
            value = "delete:"+deleteAnnotation.value();
        }
        return value;
    }

}

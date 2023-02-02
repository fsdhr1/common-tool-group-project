package com.gykj.networkmodule;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Application;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.gykj.networkmodule.beans.DataResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import retrofit2.HttpException;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class NetworkHelper {
    private static ConcurrentHashMap<Object, RetrofitManager> mManagerHashMap = new ConcurrentHashMap<>();
    private static SoftReference<Application> applicationSoftReference;
    private ConcurrentHashMap<Observable, Disposable> networkMap;
    private static NetworkHelper mNetworkHelper;
    private static Map<Object, RequestHandler> handlerMap = new HashMap<>();
    private static Set<Integer> mDefaultSuccessCode = new HashSet<>();

    public RequestHandler getRequestHandler() {
        return getRequestHandler(null);
    }

    public RequestHandler getRequestHandler(Object hashObj) {
        if (applicationSoftReference == null) {
            LogUtils.e("请先初始化网络模块!");
            return null;
        }
        if (hashObj == null) {
            hashObj = applicationSoftReference.get();
        }
        RequestHandler requestHandler = handlerMap.get(hashObj);
        if (requestHandler == null) {
            requestHandler = new RequestHandler(hashObj);
            handlerMap.put(hashObj, requestHandler);
        }
        return requestHandler;
    }

    /**
     * 初始化网络工具类
     *
     * @param application 应用applicaion实例
     * @param baseUrl     网络请求基础url
     * @param hashObj     网络实例 用来处理多module的问题
     */
    public static RetrofitManager init(Application application, String baseUrl, Object hashObj) {
        RetrofitManager retrofitManager = new RetrofitManager(baseUrl);
        mManagerHashMap.put(hashObj, retrofitManager);
        applicationSoftReference = new SoftReference<>(application);
        Utils.init(application);
        return retrofitManager;
    }

    /**
     * 单moudle或所有moudle都用一个请求实体使用
     *
     * @param application
     * @param baseUrl
     */
    public static RetrofitManager init(Application application, String baseUrl) {
        return init(application, baseUrl, application);
    }

    /**
     * 设置拦截器
     *
     * @param interceptor 设置网络请求的拦截器
     */
    public static void addNetInterceptor(Interceptor interceptor) {
        addNetInterceptor(applicationSoftReference.get(), interceptor);
    }

    public static void addNetInterceptor(Object hashObj, Interceptor interceptor) {
        RetrofitManager retrofitManager = mManagerHashMap.get(hashObj);
        if (retrofitManager != null) {
            retrofitManager.addNetInterceptor(interceptor);
        }
    }
    
    public static void addGlobalHeader(Object hashObj,String headerName,String headerValue){
        RetrofitManager retrofitManager = mManagerHashMap.get(hashObj);
        if(retrofitManager != null){
            retrofitManager.addGlobalHeader(headerName,headerValue);
        }
    }
    
    /*public static void addGlobalHeader(String headerName,String headerValue){
        Set<Object> objects = mManagerHashMap.keySet();
        for (Object object : objects) {
            addGlobalHeader(object,headerName,headerValue);
        }
    }*/
    
    private NetworkHelper() {
        networkMap = new ConcurrentHashMap<>();
        mDefaultSuccessCode.add(0);
    }

    public static boolean addDefaultSuccessCode(int code) {
        return mDefaultSuccessCode.add(code);
    }

    /**
     * 返回网络请求工具实例
     *
     * @return 网络请求实例
     */
    public static NetworkHelper getInstance() {
        if (mNetworkHelper == null) {
            mNetworkHelper = new NetworkHelper();
        }
        return mNetworkHelper;
    }

    /**
     * 设置请求的token
     *
     * @param hashObj
     * @param token   网络请求的token
     */
    public static void setToken(Object hashObj, String token) {
        RetrofitManager retrofitManager = mManagerHashMap.get(hashObj);
        if (retrofitManager != null) {
            retrofitManager.setToken(token);
        }
    }

    public static <T> T create(Object hashObj, Class<T> c) {
        RetrofitManager retrofitManager = mManagerHashMap.get(hashObj);
        if (retrofitManager != null) {
            return retrofitManager.create(c);
        }
        return null;
    }

    public <C, R> C request(Class<C> c, NetWorkResult<R> r) {
        return request(c, r, null);
    }

    /**
     * 发起请求
     *
     * @param c   API接口类
     * @param r   网络请求返回类
     * @param <R> 返回数据类型
     * @return 返回Class对应的实体类
     */
    public <C, R> C request(Class<C> c, NetWorkResult<R> r, Object hashObj) {
        if (applicationSoftReference == null) {
            ToastUtils.showShort("请先初始化网络模块");
            return null;
        }
        if (hashObj == null) {
            hashObj = applicationSoftReference.get();
        }
        C IApi = NetworkHelper.create(hashObj, c);
        Object o = Proxy.newProxyInstance(c.getClassLoader(),
                new Class[]{c}, new InvokeRequestHandler<R>(IApi, r));
        return (C) o;
    }

    private final class InvokeRequestHandler<R> implements InvocationHandler {
        private Object obj;
        private NetWorkResult<R> mNetWorkResult;

        public InvokeRequestHandler(Object o, NetWorkResult<R> netWorkResult) {
            obj = o;
            mNetWorkResult = netWorkResult;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                Object invoke = method.invoke(obj, args);
                if (invoke instanceof Observable) {
                    Observable<R> observable = (Observable) invoke;
                    mNetWorkResult.onRequestInvoke(observable);
                    Observable<R> finalObservable = observable;
                    Observable<R> finalObservable1 = observable;
                    Disposable disposable =
                            observable.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(dataResponse -> {
                                        networkMap.remove(finalObservable);
                                        if (dataResponse instanceof DataResponse) {
                                            int code = ((DataResponse) dataResponse).getCode();
                                            if (mDefaultSuccessCode.contains(code)) {
                                                mNetWorkResult.onSuccess(dataResponse, args);
                                            } else {
                                                String value = getRequestUrl(method);
                                                LogUtils.eTag("NetworkError", ((DataResponse) dataResponse).getCode() + "---" +
                                                        ((DataResponse) dataResponse).getMsg() + "---" + value);
                                                String msg = ((DataResponse) dataResponse).getMsg();
                                                String errorMsg;
                                                if (TextUtils.isEmpty(msg)) {
                                                    errorMsg = value;
                                                } else {
                                                    errorMsg = value + "--" + msg;
                                                }
                                                mNetWorkResult.onError(((DataResponse) dataResponse).getCode(),
                                                        errorMsg, args, null, dataResponse);
                                            }
                                        } else {
                                            mNetWorkResult.onSuccess(dataResponse, args);
                                        }
                                        mNetWorkResult.onRequestEnd();
                                    }, throwable -> {
                                        networkMap.remove(finalObservable1);
                                        String value = getRequestUrl(method);
                                        if (throwable instanceof HttpException) {
                                            HttpException httpException = (HttpException) throwable;
                                            if (httpException.response().code() == 401) {
                                                // Token失效，跳转登录页面

                                            } else {
                                                ToastUtils.showShort("加载失败:" + httpException.getMessage());
                                            }
                                            LogUtils.eTag("NetworkError", httpException.response().raw().toString());
                                            if (mNetWorkResult != null) {
                                                mNetWorkResult.onError(httpException.code(), value + "--" + throwable.getMessage(), args, throwable, null);
                                            }
                                        } else if (throwable instanceof SocketTimeoutException) {
                                            //SocketTimeoutException socketTimeoutException = (SocketTimeoutException) throwable;
                                            ToastUtils.showShort("加载失败:" + "响应超时");
                                            LogUtils.eTag("NetworkError", "响应超时:" + value);
                                            if (mNetWorkResult != null) {
                                                mNetWorkResult.onError(-2, value + "--" + throwable.getMessage(), args, throwable, null);
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
                                                mNetWorkResult.onError(-1, value + "--" + throwable.getMessage(), args, throwable, null);
                                            }
                                        }
                                        throwable.printStackTrace();
                                    /*if (networkMap.size() == 0 && mView != null) {
                                        mView.hideLoading();
                                    }*/
                                        mNetWorkResult.onRequestEnd();
                                    });
                    networkMap.put(observable, disposable);
                    mNetWorkResult.onStart(disposable);
                }
                return invoke;
            } catch (InvocationTargetException e) {
               throw e.getCause();
            }
        }
    }

    private String getRequestUrl(Method method) {
        String value = "";
        POST postAnnotation = method.getAnnotation(POST.class);
        GET getAnnotation = method.getAnnotation(GET.class);
        DELETE deleteAnnotation = method.getAnnotation(DELETE.class);
        if (postAnnotation != null) {
            value = "post:" + postAnnotation.value();
        } else if (getAnnotation != null) {
            value = "get:" + getAnnotation.value();
        } else if (deleteAnnotation != null) {
            value = "delete:" + deleteAnnotation.value();
        }
        return value;
    }

    public interface NetWorkResult<R> {
        /**
         * 开始请求网络
         *
         * @param disposable 网络请求handler
         */
        default void onStart(Disposable disposable) {
        }

        /**
         * 开始执行请求
         *
         * @param observable 用于绑定网络请求生命周期
         */
        void onRequestInvoke(Observable observable);

        /**
         * 请求返回结束
         */
        void onRequestEnd();

        /**
         * 请求成功
         *
         * @param data 返回的响应数据实体
         */
        void onSuccess(R data);

        default void onSuccess(R data, Object... args) {
            onSuccess(data);
        }

        /**
         * 请求失败
         *
         * @param err    错误码
         * @param errMsg 错误信息
         * @param t      抛出的错误异常
         * @param data   返回的响应数据实体
         */
        default void onError(int err, String errMsg, Object[] oriArgs, Throwable t, @Nullable R data) {

        }
    }

    /**
     * 取消所有请求
     */
    public synchronized void cancelAllRequest() {
        Iterator<Map.Entry<Observable, Disposable>> iterator = networkMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Observable, Disposable> next = iterator.next();
            Disposable value = next.getValue();
            if (!value.isDisposed()) {
                value.dispose();
            }
            iterator.remove();
        }
    }


    public ConcurrentHashMap<Observable, Disposable> getNetworkMap() {
        return networkMap;
    }

    protected boolean isHaveNetworkRequest() {
        return networkMap.size() != 0;
    }
}

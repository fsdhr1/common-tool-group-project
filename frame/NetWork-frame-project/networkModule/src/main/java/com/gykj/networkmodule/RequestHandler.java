package com.gykj.networkmodule;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.collection.ArrayMap;

/**
 * @author zyp
 * 2021/5/27
 */
public class RequestHandler {
    private Object hashObj;

    RequestHandler() {

    }
    
    public RequestHandler(Object hashObj) {
        this.hashObj = hashObj;
    }

    private RequestBuilder genRequestBuilder(Class<?> apiClass, Method method, Object... args) {
        List<Object> params = new ArrayList<>();
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            params.add(args[i]);
        }
        return new RequestBuilder(apiClass, method, hashObj, params);
    }

    public <T extends IBaseApi> T buildRequest(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                RealApi annotation = clazz.getAnnotation(RealApi.class);
                Class realClass = annotation.value();
                Method declaredMethod = realClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                return genRequestBuilder(realClass, declaredMethod, args);
            }
        });
        return (T) o;
    }

    private static Class<?>[] getArgsType(Object[] args) {
        Class<?>[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        return classes;
    }

    public OrderedRequests buildOrderedRequest(RequestBuilder... requestBuilders) {
        return new OrderedRequests(requestBuilders);
    }

}

package com.grandtech.mapframe.core.layer;

/**
 * @ClassName IDoneLisennerCallBack
 * @Description TODO 异步函数完成回调
 * @Author: fs
 * @Date: 2021/6/30 13:22
 * @Version 2.0
 */
public interface IDoneListenerCallBack<T> {

    void onDone(T result);
}

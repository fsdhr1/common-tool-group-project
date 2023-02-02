package com.grandtech.mapframe.core;

import android.content.Context;
import android.util.Log;

import com.grandtech.mapframe.core.rules.Rules;
import com.mapbox.mapboxsdk.Mapbox;

/**
 * @ClassName GMap
 * @Description TODO 初始化mapbox实例
 * @Author: fs
 * @Date: 2021/2/19 14:01
 * @Version 2.0
 */
public class GMap implements Rules {
    /**
     * TODO 获取实例
     * @param context
     */
    public static void getInstance(Context context){
        Mapbox.getInstance(context, context.getString(R.string.mapbox_access_token));
    }
}

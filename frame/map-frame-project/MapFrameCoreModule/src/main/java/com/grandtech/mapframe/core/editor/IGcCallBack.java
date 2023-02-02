package com.grandtech.mapframe.core.editor;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Feature;

import java.util.List;

/**
 * @ClassName IGcCallBack
 * @Description TODO  gc方法执行后回调，
 * @Author: fs
 * @Date: 2021/4/23 11:07
 * @Version 2.0
 */
public interface IGcCallBack {

    /**
     * gc方法执行后回调，
     */
    void onGcCallBack(List<Feature> features);

    void onError(@NonNull Throwable e);
}

package com.gykj.grandphotos.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

/**
 * 自定义图片加载方式
 */
public interface ImageEngine {
    /**
     * 加载图片到ImageView
     *
     * @param context   上下文
     * @param uri 图片Uri
     * @param imageView 加载到的ImageView
     */
    //安卓10推荐uri，并且path的方式不再可用
    void loadPhoto(@NonNull Context context, @NonNull Uri uri,@NonNull ImageView imageView);


    /**
     * 获取图片加载框架中的缓存Bitmap，不用拼图功能可以直接返回null
     *
     * @param context 上下文
     * @param uri    图片路径
     * @param width   图片宽度
     * @param height  图片高度
     * @return Bitmap
     */
    //安卓10推荐uri，并且path的方式不再可用
    Bitmap getCacheBitmap(@NonNull Context context,@NonNull Uri uri, int width, int height) throws Exception;


}

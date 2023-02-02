package com.gykj.grandphotos;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.gykj.grandphotos.Builder.AlbumBuilder;
import com.gykj.grandphotos.engine.GlideEngine;
import com.gykj.grandphotos.engine.ImageEngine;
import com.gykj.grandphotos.models.album.AlbumModel;
import com.gykj.grandphotos.utils.bitmap.BitmapUtils;
import com.gykj.grandphotos.utils.bitmap.SaveBitmapCallBack;
import com.gykj.grandphotos.utils.media.MediaScannerConnectionUtils;

import java.io.File;
import java.util.List;

/**
 * 启动管理器
 */
public class GrandPhotoPickers {

    //返回数据Key
    public static final String RESULT_PHOTOS = "keyOfGrandPhotosResult";
    public static final String RESULT_SELECTED_ORIGINAL = "keyOfGrandPhotosResultSelectedOriginal";

    /**
     * 预加载
     * 调不调用该方法都可以，不调用不影响正常使用
     * 第一次扫描媒体库可能会慢，调用预加载会使真正打开相册的速度加快
     * 若调用该方法，建议自行判断代码书写位置，建议在用户打开相册的3秒前调用，比如app主页面或调用相册的上一页
     * 该方法如果没有授权读取权限的话，是无效的，所以外部加不加权限控制都可以，加的话保证执行，不加也不影响程序正常使用
     *
     * @param cxt 上下文
     */
    public static void preLoad(Context cxt) {
        AlbumModel.getInstance().query(cxt, null);
    }


    /**
     * 创建相册
     *
     * @param activity 上下文
     * @return AlbumBuilder 建造者模式配置其他选项
     */
    public static AlbumBuilder createBuilder(FragmentActivity activity) {
        return AlbumBuilder.createAlbum(activity);
    }

    public static AlbumBuilder createBuilder(androidx.fragment.app.Fragment fragmentV) {
        return AlbumBuilder.createAlbum(fragmentV);
    }

    /**
     * 相机配置
     *
     * @param activity
     * @return
     */
    public static AlbumBuilder createCamera(FragmentActivity activity) {
        return AlbumBuilder.createCamera(activity);
    }

    public static AlbumBuilder createCamera(androidx.fragment.app.Fragment fragmentV) {
        return AlbumBuilder.createCamera(fragmentV);
    }

}

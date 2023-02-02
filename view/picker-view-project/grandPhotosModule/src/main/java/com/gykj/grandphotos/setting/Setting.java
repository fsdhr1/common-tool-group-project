package com.gykj.grandphotos.setting;

import androidx.annotation.IntDef;

import com.gykj.grandphotos.engine.GlideEngine;
import com.gykj.grandphotos.engine.ImageEngine;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * GrandPhotos的设置值
 */

public class Setting {
    public static int count = 1;

    public static ArrayList<GrandPhotoBean> selectedPhotos = new ArrayList<>();
    public static boolean showOriginalMenu = false;
    public static boolean originalMenuUsable = false;
    public static String originalMenuUnusableHint = "";
    public static boolean selectedOriginal = false;
    public static ImageEngine imageEngine = GlideEngine.getInstance();

    public static final int LIST_FIRST = 0;
    public static boolean onlyStartCamera = false;// 是否只调用相机组件


    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {LIST_FIRST})
    public @interface Location {

    }

    public static void clear() {
        count = 1;
        selectedPhotos.clear();
        showOriginalMenu = false;
        originalMenuUsable = false;
        originalMenuUnusableHint = "";
        selectedOriginal = false;
    }


}

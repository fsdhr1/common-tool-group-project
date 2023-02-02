package com.gykj.grandphotos.callback;

import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;

import java.util.ArrayList;

public abstract class SelectCallback {
    /**
     * 选择结果回调
     *
     * @param photos     返回对象集合：如果你需要了解图片的宽、高、大小、用户是否选中原图选项等信息，可以用这个
     * @param isOriginal 返回图片地址集合时如果你需要知道用户选择图片时是否选择了原图选项，用如下方法获取
     */
    public abstract void onResult(ArrayList<GrandPhotoBean> photos, boolean isOriginal);

    /**
     * 什么都没选，取消选择回调
     */
    public abstract void onCancel();
}

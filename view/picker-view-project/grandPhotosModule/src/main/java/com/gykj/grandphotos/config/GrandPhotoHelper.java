package com.gykj.grandphotos.config;

/**
 * Created by ZhaiJiaChang.
 * <p>
 * Date: 2021/11/24
 */
public class GrandPhotoHelper {
    //
    private static String fileProviderPath = "";

    /**
     * 传入fileprovider参数
     */
    public static void init(String mFileProviderPath) {
        fileProviderPath = mFileProviderPath;
    }

    /**
     * 获取fileProvider
     */
    public static String getFileProviderPath() {
        return fileProviderPath;
    }
}

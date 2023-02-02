package com.gykj.autoupdate.utils;

import android.os.Environment;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class FileUtil {

    /**
     * 获取下载文件的名称
     *
     * @param url
     * @return
     */
    public static String getFileName(String url) {
        // 防止出现中文乱码
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (url.contains("?")) {
            url = url.split("\\?")[0];
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 默认下载目录
     *
     * @return
     */
    public static String getDownloadDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "grandtech/autoupdate" + File.separator;
    }
}

package com.gykj.autoupdate.oss;

public interface DownloadListener {

    /**
     * 下载进度发生改变
     * @param index 正在下载第几个文件
     * @param currentSize 当前下载量
     * @param totalSize 当前文件总大小
     */
    void onProgressChange(int index, long currentSize, long totalSize);

    /**
     * 下载成功返回结果
     * @param path
     */
    void onUpSuccess(String path);

    /**
     * 下载失败触发
     * @param exception
     */
    void onFaile(Exception exception);
}

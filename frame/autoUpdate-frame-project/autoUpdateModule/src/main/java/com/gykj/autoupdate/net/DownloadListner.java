package com.gykj.autoupdate.net;

/**
 * 下载监听
 *
 */
public interface DownloadListner {
    void onFinished();

    void onProgress(int progress);

    void onPause();

    void onCancel();
}

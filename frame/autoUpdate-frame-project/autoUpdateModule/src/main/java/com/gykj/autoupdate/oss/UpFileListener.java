package com.gykj.autoupdate.oss;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;

import java.util.List;

/**
 * 上传监听
 */
public interface UpFileListener {
    /**
     * 上传进度发生改变
     * @param index 正在上传第几个文件
     * @param currentSize 当前上传量
     * @param totalSize 当前文件总大小
     */
    void onProgressChange(int index, long currentSize, long totalSize);

    /**
     * 上传成功返回结果
     * @param files
     */
    void onUpSuccess(List<OSSFile> files);

    /**
     * 上传失败触发
     * @param clientException
     * @param serviceException
     */
    void onFaile(ClientException clientException, ServiceException serviceException);
}

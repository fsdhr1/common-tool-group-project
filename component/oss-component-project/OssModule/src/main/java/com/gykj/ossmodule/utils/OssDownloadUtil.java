package com.gykj.ossmodule.utils;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.gykj.ossmodule.bean.OssFileBean;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ren on 2021/4/8
 */
public class OssDownloadUtil {
    /**
     * 流式下载
     *
     * @param oss
     * @param ossFileBean
     * @param localPath
     * @param listener
     */
    public static void downloadObjectToFile(OSSClient oss, OssFileBean ossFileBean, final String localPath, final DownloadListener listener) {
        // download object to file
        GetObjectRequest get = new GetObjectRequest(ossFileBean.getBucketName(), ossFileBean.getKey());
        //设置下载进度回调
        get.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize, false);
            }
        });
        OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    inputStream = result.getObjectContent();
                    outputStream = new FileOutputStream(localPath);

                    byte[] buffer = new byte[2048];
                    int len;

                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    listener.onSuccess(localPath);
                } catch (Exception e) {
                    OSSLog.logInfo(e.toString());
                    listener.onFail(e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientException, ServiceException serviceException) {
                listener.onFail(clientException);
            }
        });
    }
}

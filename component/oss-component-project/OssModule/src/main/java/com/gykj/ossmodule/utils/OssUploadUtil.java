package com.gykj.ossmodule.utils;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.gykj.ossmodule.bean.OssFileBean;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ren on 2021/4/8
 */
public class OssUploadUtil {
    /**
     * 生成路径
     *
     * @return
     */
    public static String generateRouter() {
        // 年月日文件夹命名
        String[] strNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString().split("-");
        // 路径加入uuid
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        // 拼接路径
        String path = "mobile/" + strNow[0] + "/" + strNow[1] + "/" + strNow[2] + "/" + uuid + "/";
        return path;
    }

    /**
     * 异步上传单文件
     *
     * @param oss
     * @param ossFileBean
     * @param uploadFile
     * @param listener
     */
    public static void uploadAsync(OSSClient oss, OssFileBean ossFileBean, File uploadFile, final UploadListener listener) {
        if (!uploadFile.isFile() || !uploadFile.exists()) {
            listener.onFail(new ClientException("文件不存在"), null);
        }
        final String mBucketName = ossFileBean.getBucketName();
        final String mEndpoint = ossFileBean.getEndpoint();
        final String mAccessKeyId = ossFileBean.getAccessKeyId();
        String key = ossFileBean.getKey();
        if (TextUtils.isEmpty(key)) {
            key = generateRouter() + uploadFile.getName();
            ossFileBean.setKey(key);
        }
        final String mKey = key;

        // 构造上传请求。
        PutObjectRequest put = new PutObjectRequest(ossFileBean.getBucketName(), ossFileBean.getKey(), uploadFile.getAbsolutePath());

        // 异步上传时可以设置进度回调。
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");
                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());

                OssFileBean ossFile = new OssFileBean(mEndpoint, mBucketName, mAccessKeyId, mKey);

                List<OssFileBean> resultInfo = new ArrayList<>();
                resultInfo.add(ossFile);
                listener.onSuccess(resultInfo);
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // 请求异常。
                if (clientExcepion != null) {
                    // 本地异常，如网络异常等。
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常。
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
                listener.onFail(clientExcepion, serviceException);
            }
        });
    }

    /**
     * 多文件上传
     *
     * @param oss
     * @param ossFileBean
     * @param uploadFiles
     * @param listener
     */
    public static void uploadMultipleAsync(final OSSClient oss, OssFileBean ossFileBean, final List<File> uploadFiles, final UploadListener listener) {
        final String mBucketName = ossFileBean.getBucketName();
        final String mEndpoint = ossFileBean.getEndpoint();
        final String mAccessKeyId = ossFileBean.getAccessKeyId();
        final List<OssFileBean> resultInfo = new ArrayList<>();

        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                for (int i = 0; i < uploadFiles.size(); i++) {
                    //生成随机名字，避免文件名重复
                    final String key = generateRouter() + uploadFiles.get(i).getName();
                    // 构造上传请求。
                    PutObjectRequest put = new PutObjectRequest(mBucketName, key, uploadFiles.get(i).getAbsolutePath());

                    try {
                        PutObjectResult putResult = oss.putObject(put);
                        Log.d("PutObject", "UploadSuccess");
                        Log.d("ETag", putResult.getETag());
                        Log.d("RequestId", putResult.getRequestId());

                        int jd = i + 1;
                        // Thread.sleep(1000*3); // 休眠3秒
//                        emitter.onNext("上传中:" + jd + "/" + uploadFiles.size());
                        emitter.onNext(jd);

                        OssFileBean ossFile = new OssFileBean(mEndpoint, mBucketName, mAccessKeyId, key);
                        resultInfo.add(ossFile);

                        // 全部上传完返回
                        if (resultInfo.size() == uploadFiles.size()) {
                            emitter.onComplete();
                        }
                    } catch (ClientException e) {
                        // 客户端异常，例如网络异常等。
                        e.printStackTrace();
                        emitter.onNext(null);
                        listener.onFail(e, null);
                        break;
                        // subscriber.onCompleted();
                    } catch (ServiceException e) {
                        // 服务端异常。
                        Log.e("RequestId", e.getRequestId());
                        Log.e("ErrorCode", e.getErrorCode());
                        Log.e("HostId", e.getHostId());
                        Log.e("RawMessage", e.getRawMessage());
                        emitter.onNext(null);
                        listener.onFail(new ClientException("oss服务端异常"), e);
                        break;
                        // subscriber.onCompleted();
                    }
                }
            }
        }, BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.io()) // 设置被观察者在io线程中进行
                .observeOn(AndroidSchedulers.mainThread()) // 设置观察者在主线程中进行
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Integer jd) {
                        if (jd == null) {
                            listener.onFail(new ClientException("上传失败"), null);
                            return;
                        }
                        listener.onProgressChange(jd, 0, 0);
                    }

                    @Override
                    public void onError(Throwable t) {
                        listener.onFail(new ClientException("上传失败"), null);
                    }

                    @Override
                    public void onComplete() {
                        listener.onSuccess(resultInfo);
                    }
                });
    }
}

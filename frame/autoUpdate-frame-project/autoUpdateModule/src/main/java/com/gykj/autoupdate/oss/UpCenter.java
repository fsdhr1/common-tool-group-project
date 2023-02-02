package com.gykj.autoupdate.oss;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 阿里云上传文件
 */
public class UpCenter {
    private String sign_server_url = "";//自签名服务器地址

    private String endPoint;//阿里云上传节点地址
    private String bucketName;//阿里云OSS空间名称
    private OSS oss;//oss对象
    private String accessKeyId;//阿里云生成的keyId 阿里云生成
    private List<OSSFile> resultInfo;//返回结果
    //private Context context;
   // private LoadingDialog loadingDialog ;
    public UpCenter(String endPoint, String bucketName, String accessKeyId, Context context, String token, String sign_server_url) {
        this.endPoint = endPoint;
        this.bucketName = bucketName;
        this.accessKeyId = accessKeyId;
        this.sign_server_url = sign_server_url;
        if(context instanceof Activity){
            initOSS(((Activity) context).getApplication(),token);
        }else {
            initOSS(context,token);
        }
    }
    //注意给Okhttp添加全局token
    /**
     * 初始化配置
     */
    private void initOSS(Context context, final String token) {
        //从本地签名服务器获取签名
        OSSCredentialProvider credentialProvider = new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String s) {

                OkHttpClient okHttpClient = new OkHttpClient();
                String url = null;
                try {
                    url = sign_server_url + "?s=" + URLEncoder.encode(s, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }

                Request request = new Request.Builder()
                        .url(url)
                        .header("Authorization", token)
                        .build();
                Call call = okHttpClient.newCall(request);
                Response response = null;
                try {
                    response = call.execute();
                    String sign = response.body().string();
                    return "OSS " + accessKeyId + ":" + sign;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        // 配置类如果不设置，会有默认配置。
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒。
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒。
        conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个。
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次。

        //把网络访问的代码放在这里
        oss = new OSSClient(context, endPoint, credentialProvider,conf);


    }

    public void upFiles(final Context context, final List<FileInformation> files, final UpFileListener listener) {
       // if(loadingDialog == null){
         //   loadingDialog = new LoadingDialog(context);
       // }
      // loadingDialog.show();
        //生成随机名字，避免文件名重复
      String path = generatePath();
      resultInfo = new ArrayList<>();
      for (int i=0;i<files.size();i++){
          //生成随机名字，避免文件名重复
         final String key = path + files.get(i).getName();
          // 构造上传请求。
          PutObjectRequest put = new PutObjectRequest(bucketName, key, files.get(i).getPath());

          // 异步上传时可以设置进度回调。
          final int index = i;
          put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
              @Override
              public void onProgress(PutObjectRequest request, final long currentSize, final long totalSize) {
                  //计算上传进度
                  Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                  ((Activity)context).runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          int jd = index+1;
                         // loadingDialog.setMsg("上传中:"+jd+"/"+files.size()+"%");
                      }
                  });
                  listener.onProgressChange(index, currentSize, totalSize);
              }
          });

          OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
              //上传成功
              @Override
              public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                  OSSFile ossFile = new OSSFile();
                  ossFile.setEndPoint(endPoint);
                  ossFile.setFileName(files.get(index).getName());
                  ossFile.setKey(key);
                  resultInfo.add(ossFile);
                  /**
                   * 全部上传完返回
                   */
                  if(resultInfo.size()==files.size()){
                      //loadingDialog.cancel();
                      listener.onUpSuccess(resultInfo);
                  }
              }

              @Override
              public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                  //loadingDialog.cancel();
                  listener.onFaile(clientExcepion, serviceException);
              }
          });
      }
    }





    /**
     * 上传单个文件
     *
     * @param file
     */
    public void upFile(final Activity activity, final FileInformation file, final UpFileListener listener) {

        /*final LoadingDialog loadingDialog = new LoadingDialog(activity);
        loadingDialog.show();*/
        //生成随机名字，避免文件名重复
        final String key = generatePath() + file.getName();
        // 构造上传请求。
        PutObjectRequest put = new PutObjectRequest(bucketName, key, file.getPath());

        // 异步上传时可以设置进度回调。
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, final long currentSize, final long totalSize) {
                //计算上传进度
                //Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onProgressChange(0, currentSize, totalSize);
                    }
                });

            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            //上传成功
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                OSSFile ossFile = new OSSFile();
                ossFile.setEndPoint(endPoint);
                ossFile.setFileName(file.getName());
                ossFile.setKey(key);
                ossFile.setOssAccessKeyId(accessKeyId);
                ossFile.setOssBucketName(bucketName);
                resultInfo = new ArrayList<>();
                resultInfo.add(ossFile);
                //loadingDialog.cancel();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUpSuccess(resultInfo);
                    }
                });
            }

            @Override
            public void onFailure(PutObjectRequest request, final ClientException clientExcepion, final ServiceException serviceException) {
                //loadingDialog.cancel();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFaile(clientExcepion, serviceException);
                    }
                });

            }
        });


    }
    /**
     * 上传单个文件
     *
     * @param file
     */
    public void upFile(final FileInformation file, final UpFileListener listener) {
        //if(loadingDialog == null){
            //loadingDialog = new LoadingDialog(context);
       // }
      //  loadingDialog.show();
        //生成随机名字，避免文件名重复
        final String key = generatePath() + file.getName();
        // 构造上传请求。
        PutObjectRequest put = new PutObjectRequest(bucketName, key, file.getPath());

        // 异步上传时可以设置进度回调。
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                //计算上传进度
                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                listener.onProgressChange(0, currentSize, totalSize);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            //上传成功
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                OSSFile ossFile = new OSSFile();
                ossFile.setEndPoint(endPoint);
                ossFile.setFileName(file.getName());
                ossFile.setKey(key);
                resultInfo = new ArrayList<>();
                resultInfo.add(ossFile);
                //loadingDialog.cancel();
                listener.onUpSuccess(resultInfo);
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                //loadingDialog.cancel();
                listener.onFaile(clientExcepion, serviceException);
            }
        });


    }


    /**
     * 生成路径的规则
     *
     * @return
     */
    private String generatePath() {
        //年月日文件夹命名
        String[] strNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString().split("-");
        //路径加入uuid
        String uuid = UUID.randomUUID().toString();
        //去掉“-”符号
        uuid = uuid.replaceAll("-", "");

        String path = "mobile/"+strNow[0] + "/" + strNow[1] + "/" + strNow[2] + "/" + uuid + "/";

        return path;
    }

    /**
     * 根据文件信息生成临时文件url
     *
     * @param key
     * @return
     * @throws ClientException
     */
    public String generateTempUrl(String key) throws ClientException {
        return oss.presignConstrainedObjectURL(bucketName, key, 30 * 60);
    }


    public boolean isExist(String key){
        try {
            if (oss.doesObjectExist(bucketName, key)) {
                return true;
            } else {
               return false;
            }
        } catch (ClientException e) {
            // 本地异常如网络异常等
            e.printStackTrace();
            return false;
        } catch (ServiceException e) {
            // 服务异常
            Log.e("ErrorCode", e.getErrorCode());
            Log.e("RequestId", e.getRequestId());
            Log.e("HostId", e.getHostId());
            Log.e("RawMessage", e.getRawMessage());
            return false;
        }
    }


    public boolean deleteFile(String key){
        try {
            if(isExist(key))
            // 删除文件。
            oss.deleteObject(new DeleteObjectRequest(bucketName,key));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
    public void close(){
       // context = null;
        oss = null;
        //loadingDialog = null;
    }

    public String getDownloadUrl(String key){
        try {
            return oss.presignConstrainedObjectURL(bucketName, key, 30*60);

        } catch (ClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 流式下载
     * @param key
     * @param localPath
     * @param listener
     */
    public void downloadObjectToFile(String key, final String localPath, final DownloadListener listener)  {
        // download object to file
        GetObjectRequest get = new GetObjectRequest(bucketName, key);
        OSSAsyncTask getTask = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                try {
                    long length = result.getContentLength();
                    byte[] buffer = new byte[(int) length];
                    int readCount = 0;
                    while (readCount < length) {
                        readCount += result.getObjectContent().read(buffer, readCount, (int) length - readCount);
                    }
                    FileOutputStream fout = new FileOutputStream(localPath);
                    fout.write(buffer);
                    fout.close();
                    listener.onUpSuccess(localPath);
                } catch (Exception e) {
                    OSSLog.logInfo(e.toString());
                    listener.onFaile(e);
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientException, ServiceException serviceException) {
                listener.onFaile(clientException);
            }
        });
    }

}

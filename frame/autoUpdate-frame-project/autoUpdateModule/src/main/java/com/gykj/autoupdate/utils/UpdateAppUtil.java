package com.gykj.autoupdate.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gykj.autoupdate.BaseConfig.Constants;
import com.gykj.autoupdate.bean.AliOssBasePath;
import com.gykj.autoupdate.bean.AppPath;
import com.gykj.autoupdate.bean.BaseResult;
import com.gykj.autoupdate.bean.SysVersion;
import com.gykj.autoupdate.net.HttpUtil;
import com.gykj.autoupdate.oss.UpCenter;
import com.gykj.autoupdate.weight.ProgressDialog;
import com.gykj.autoupdate.weight.UpdateDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateAppUtil {
    private static volatile UpdateAppUtil instance;
    private static Context mContext;
    public static String mOutsideAuthority;
    private UpCenter upCenter;
    private String OSS_Token;
    private String OSS_EndPoint;
    private String OSS_BucketName;
    private String OSS_AccessKeyId;
    private String OSS_FileKey;
    private int dialogImg = -1;
    private int dialogButtonTextColor = -1;
    private int dialogButtonColor = -1;

    private UpdateAppUtil(Context context) {
        if (context != null) {
            mContext = context;
        }
    }

    /**
     * 使用默认的Authority：${applicationId}.autoupdatefileprovider
     *
     * @param ctx
     * @return
     */
    @Deprecated
    public static UpdateAppUtil getInstance(Context ctx) {
        if (instance == null) {
            synchronized (UpdateAppUtil.class) {
                if (instance == null) {
                    instance = new UpdateAppUtil(ctx);
                }
            }
        }
        if (instance != null) {
            mContext = ctx;
        }
        return instance;
    }

    /**
     * 传入自定义的外部Authority
     *
     * @param ctx
     * @param outsideAuthority 外部Authority
     * @return
     */
    public static UpdateAppUtil getInstance(Context ctx, String outsideAuthority) {
        mOutsideAuthority = outsideAuthority;
        return getInstance(ctx);
    }

    public void finish() {
        instance = null;
    }


    /**
     * 自定义dialogImg 颜色
     *
     * @param dialogImg
     * @param dialogButtonTextColor
     * @param dialogButtonColor
     */
    public void setCustomUpdateDialog(int dialogImg, int dialogButtonTextColor, int dialogButtonColor) {
        this.dialogImg = dialogImg;
        this.dialogButtonTextColor = dialogButtonTextColor;
        this.dialogButtonColor = dialogButtonColor;
    }

    // 检查新版本回调
    public interface CheckNewVersionCallBack {
        void onCheckNewVersionFail(Exception e);

        void onCheckNewVersionSuccess(BaseResult<SysVersion> baseResult);
    }

    // 检查新版本回调（弃用）
    @Deprecated
    public interface CheckVersionCallBack {
        void onCheckVersionCallBack(SysVersion sysVersion);
    }

    // 热修复回调
    public interface hotFixCallBack {
        void onHotFixVetrsionCallBack(int hotFixVersion);
    }

    // OSSSigned回调
    public interface GetOSSSignedURLCallBack {
        void onSuccess(String ossSignedUrl);
    }

    // 获取更新版本信息
    @Deprecated
    public void checkVersionMsg(String applicationKey, String signType, String checkUrl, final CheckVersionCallBack checkVersionCallBack) {
        // 1.检查新版本
        checkNewVersionMsg(applicationKey, signType, checkUrl, new CheckNewVersionCallBack() {
            @Override
            public void onCheckNewVersionFail(Exception e) {
                e.printStackTrace();
                checkVersionCallBack.onCheckVersionCallBack(null);
            }

            @Override
            public void onCheckNewVersionSuccess(BaseResult<SysVersion> baseResult) {
                checkVersionCallBack.onCheckVersionCallBack(baseResult.getData());
            }
        });
    }


    /**
     * 获取更新版本信息
     *
     * @param applicationKey
     * @param signType
     * @param checkUrl
     * @param checkNewVersionCallBack
     */
    public void checkNewVersionMsg(String applicationKey, String signType, String checkUrl, final CheckNewVersionCallBack checkNewVersionCallBack) {
        if (TextUtils.isEmpty(applicationKey)) {
            checkNewVersionCallBack.onCheckNewVersionFail(new Exception("applicationKey为空！"));
            return;
        }
        if (TextUtils.isEmpty(signType)) {
            checkNewVersionCallBack.onCheckNewVersionFail(new Exception("signType为空！"));
            return;
        }
        if (!TextUtils.isEmpty(checkUrl)) {
            Constants.CHECK_VERSION_URL = checkUrl;
        }
        // 获取versionCode,versionName
        int versionCode = 0;
        String versionName = "";
        try {
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            versionCode = packInfo.versionCode;
            versionName = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            checkNewVersionCallBack.onCheckNewVersionFail(e);
        }

        // 网络请求checkVersion
        if (!TextUtils.isEmpty(applicationKey)
                && !TextUtils.isEmpty(signType)
                && versionCode != 0
                && !TextUtils.isEmpty(versionName)) {
            try {
                final HashMap<String, Object> map = new HashMap<>();
                map.put("applicationKey", applicationKey);
                map.put("versionCode", versionCode);
                map.put("versionName", versionName);
                map.put("versionSign", signType);

                ProgressDialog.showProgressDialog(ActivityUtils.getTopActivity(), "加载中...");
                HttpUtil.getInstance().postBody(Constants.CHECK_VERSION_URL, map, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        ProgressDialog.closeProgressDialog();
                        Log.e("--checkNewVersionMsg--", "onFailure: " + e.getMessage());
                        checkNewVersionCallBack.onCheckNewVersionFail(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        ProgressDialog.closeProgressDialog();
                        Log.d("--checkNewVersionMsg--", "response=" + response.toString());
                        if (response.code() != 200) {
                            checkNewVersionCallBack.onCheckNewVersionFail(new Exception("检测更新 服务异常" + response.toString()));
                            return;
                        }

                        String responseBodyStr = response.body().string();
                        Log.d("--checkNewVersionMsg--", "出参: responseBody=" + responseBodyStr);
                        // 解析返回json
                        BaseResult<SysVersion> baseBean = new Gson().fromJson(responseBodyStr,
                                new TypeToken<BaseResult<SysVersion>>() {
                                }.getType());
                        if (baseBean.getCode() != 0) {
                            checkNewVersionCallBack.onCheckNewVersionFail(new Exception("检测更新 服务异常" + responseBodyStr));
                            return;
                        }
                        checkNewVersionCallBack.onCheckNewVersionSuccess(baseBean);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                checkNewVersionCallBack.onCheckNewVersionFail(e);
            }
        }
    }


    /**
     * 检测新版本
     *
     * @param applicationKey
     * @param signType
     * @param checkUrl
     * @param mHotFixCallBacks
     */
    public void checkVersion(String applicationKey, String signType, String checkUrl, final hotFixCallBack mHotFixCallBacks) {
        // 1.获取更新版本信息
        checkNewVersionMsg(applicationKey, signType, checkUrl, new CheckNewVersionCallBack() {
            @Override
            public void onCheckNewVersionFail(final Exception e) {
                e.printStackTrace();
                ToastUtils.showShort(e.getMessage());
            }

            @Override
            public void onCheckNewVersionSuccess(BaseResult<SysVersion> baseResult) {
                // 新版本信息
                final SysVersion sysVersion = baseResult.getData();
                // 热修复版本
                int hotfixVersion = baseResult.getHotfixVersion();

                if (sysVersion != null) {
                    // 2.处理oss的新版本信息，获取阿里云下载地址
                    dealOSSSysVersion(baseResult, new GetOSSSignedURLCallBack() {
                        @Override
                        public void onSuccess(String ossSignedUrl) {
                            // 3.展示更新弹窗
                            showUpdateDialog(sysVersion, ossSignedUrl,mHotFixCallBacks);
                        }
                    });
                } else {
                    if (mHotFixCallBacks != null && hotfixVersion != 0) {
                        mHotFixCallBacks.onHotFixVetrsionCallBack(hotfixVersion);
                    }
                }
            }
        });
    }

    /**
     * 处理oss的新版本信息，获取阿里云下载地址
     *
     * @param baseResult
     * @param getOSSSignedURLCallBack
     */
    public void dealOSSSysVersion(BaseResult<SysVersion> baseResult, GetOSSSignedURLCallBack getOSSSignedURLCallBack) {
        if (baseResult == null) {
            return;
        }
        // OSS_Token
        OSS_Token = baseResult.getToken();
        // 解析新版本信息
        SysVersion sysVersion = baseResult.getData();
        List<AppPath> appPathList = new Gson().fromJson(sysVersion.getVersionPath(),
                new TypeToken<List<AppPath>>() {
                }.getType());
        if (appPathList != null && appPathList.size() > 0) {
            // 下载路径
            OSS_FileKey = appPathList.get(0).getPath();

            // 解析OSS配置信息
            AliOssBasePath ossDesc = new Gson().fromJson(appPathList.get(0).getDesc(),
                    new TypeToken<AliOssBasePath>() {
                    }.getType());
            OSS_EndPoint = TextUtils.isEmpty(ossDesc.getEndpoint()) ? "" : ossDesc.getEndpoint();
            OSS_BucketName = TextUtils.isEmpty(ossDesc.getBucket()) ? "" : ossDesc.getBucket();
            OSS_AccessKeyId = TextUtils.isEmpty(ossDesc.getAccessKeyId()) ? "" : ossDesc.getAccessKeyId();
        }
        // 获取阿里云下载地址
        getOSSSignedUrl(getOSSSignedURLCallBack);
    }

    /**
     * 获取阿里云下载地址
     *
     * @param getOSSSignedURLCallBack
     */
    private void getOSSSignedUrl(final GetOSSSignedURLCallBack getOSSSignedURLCallBack) {
        Log.e("initUpCenter", "initUpCenter");
        if (upCenter == null) {
            upCenter = new UpCenter(OSS_EndPoint, OSS_BucketName, OSS_AccessKeyId, mContext, OSS_Token, Constants.OSS_SIGN_URL);
        }
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                String url = upCenter.getDownloadUrl(OSS_FileKey);
                emitter.onNext(url);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(String url) {
                if (getOSSSignedURLCallBack != null) {
                    getOSSSignedURLCallBack.onSuccess(url);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
            }

        });
    }


    /**
     * 展示更新弹窗
     *
     * @param sysVersion
     * @param downloadUrl
     */
    public void showUpdateDialog(SysVersion sysVersion, String downloadUrl, final hotFixCallBack mHotFixCallBacks) {
        if (sysVersion == null) {
            return;
        }
        if (TextUtils.isEmpty(downloadUrl)) {
            return;
        }
        // 提示更新Dialog
        Activity topActivity = ActivityUtils.getTopActivity();
        UpdateDialog updateDialog = new UpdateDialog(topActivity, downloadUrl, sysVersion,
                dialogImg, dialogButtonTextColor, dialogButtonColor, true);
        updateDialog.setCloseClickListener(new UpdateDialog.CloseLisener() {
            @Override
            public void closed() {
                if (mHotFixCallBacks!=null){
                    mHotFixCallBacks.onHotFixVetrsionCallBack(-1);
                }
            }
        });
        if (!updateDialog.isShowing() && !topActivity.isFinishing()) {
            updateDialog.show();
        }
    }

    //下载第三方apk
    /*public void updateOhterAPK(String url, Context context, boolean isMultithread, DownloadListner downloadListner) {
        //  String urls ="https://ram-public.oss-cn-zhangjiakou.aliyuncs.com/appdownload/xbtxby/1.0.5/%E5%8D%8F%E4%BF%9D%E9%80%9A%28xby%29_beta_105_v_1.0.5_20190911112104.apk";
        if (downloadListner == null) {
            downloadApk(url, context, isMultithread);
        } else {
            downloadApkNoDialog(url, context, isMultithread, downloadListner);
        }
    }*/

    /**
     * 下载apk
     */
    /*private void downloadApkNoDialog(final String url, final Context context, boolean isMultithread, final DownloadListner downloadListner) {
        DownloadManager mDownloadManager = DownloadManager.getInstance();
        mDownloadManager.setMultithread(isMultithread);//设置这个参数就行
        mDownloadManager.add(url, new DownloadListner() {
            @Override
            public void onFinished() {
                downloadListner.onFinished();
                String fileName = FileUtil.getFileName(url);
                InstallUtil installUtil = new InstallUtil(context, FileUtil.getDownloadDirectory() + fileName);
                installUtil.install();

            }

            @Override
            public void onProgress(int progress) {
                downloadListner.onProgress(progress);
            }

            @Override
            public void onPause() {
                downloadListner.onPause();
            }

            @Override
            public void onCancel() {
                downloadListner.onCancel();
            }
        });
        mDownloadManager.download(url);
    }*/

    /**
     * 下载apk
     */
    /*private void downloadApk(final String url, final Context context, boolean isMultithread) {
        final ProgressDialog progressDialog = ProgressDialog.createDialog(context);
        progressDialog.show();
        DownloadManager mDownloadManager = DownloadManager.getInstance();
        mDownloadManager.setMultithread(isMultithread);//设置这个参数就行
        mDownloadManager.add(url, new DownloadListner() {
            @Override
            public void onFinished() {
                progressDialog.dismiss();
                String fileName = FileUtil.getFileName(url);
                InstallUtil installUtil = new InstallUtil(context, FileUtil.getDownloadDirectory() + fileName);
                installUtil.install();

            }

            @Override
            public void onProgress(int progress) {
                //int pro = (int) (progress * 100);
                progressDialog.setMessage("进度:" + progress + "%");
            }

            @Override
            public void onPause() {

            }

            @Override
            public void onCancel() {

            }
        });
        mDownloadManager.download(url);
    }*/


}

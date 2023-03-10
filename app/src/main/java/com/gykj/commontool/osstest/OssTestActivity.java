package com.gykj.commontool.osstest;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.gykj.commontool.R;
import com.gykj.ossmodule.BaseConfig.Constants;
import com.gykj.ossmodule.LoadingDialog;
import com.gykj.ossmodule.bean.OssFileBean;
import com.gykj.ossmodule.utils.DownloadListener;
import com.gykj.ossmodule.utils.OssClientUtil;
import com.gykj.ossmodule.utils.OssDownloadUtil;
import com.gykj.ossmodule.utils.OssUploadUtil;
import com.gykj.ossmodule.utils.UploadListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by ren on 2021/6/17
 */

public class OssTestActivity extends AppCompatActivity {
    private EditText etBucketName;
    private EditText etKey;
    private TextView tvImgPath;
    private ImageView ivImg;
    private TextView tvLogdownload;
    private Button btUpload;
    private Button btUploadMutiple;
    private TextView tvError;
    private TextView tvLogupload;

    private String bucketName;
    private String key;
    private OSSClient ossClient;

    private String mLocalImgPath = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oss_test);
        initView();
    }

    private void initView() {
        etBucketName = findViewById(R.id.et_bucketName);
        etKey = findViewById(R.id.et_key);
        tvImgPath = findViewById(R.id.tv_imgpath);
        ivImg = findViewById(R.id.iv_img);
        tvLogdownload = findViewById(R.id.tv_log_download);
        btUpload = findViewById(R.id.bt_upload);
        btUploadMutiple = findViewById(R.id.bt_uploadMultiple);
        tvError = findViewById(R.id.tv_error);
        tvLogupload = findViewById(R.id.tv_log_upload);

        // ??????aaaa.png??????
        String localDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "grandtech/oss" + File.separator;
        File destDir = new File(localDirPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        mLocalImgPath = localDirPath + "aaaa.png";
        tvImgPath.setText(mLocalImgPath);


        showImg();

        // ?????????ossClient
//        initOssClient("token");

        // ?????????token??????initOssClient(),??????token???????????????????????????
        getGYToken();


    }


    private void getGYToken() {
        String key = "4d3a69e6879a437abdbff9d46e8dafa8";
        String secret = "adb39bd3ef3c4677936aa739229dc69f";

        new OssClientUtil.GetTokenTask(new OssClientUtil.GetTokenTask.TokenCallBack() {
            @Override
            public void getTokenSuccess(String token) {
                LogUtils.e("token=", token);

                if (TextUtils.isEmpty(token)) {
                    ToastUtils.showShort("??????token??????");
                }
                initOssClient(token);
            }
        }).execute(key, secret);
    }

    /**
     * ?????????OSSClient
     *
     * @param token
     */
    private void initOssClient(String token) {
        bucketName = TextUtils.isEmpty(etBucketName.getText().toString()) ? Constants.OSS_BUCKET_NAME : etBucketName.getText().toString();


        // ??????ossClientUtil
        OssClientUtil ossClientUtil = new OssClientUtil(this, token);
        // ??????OSSClient
        ossClient = ossClientUtil.getOssClient(new OssFileBean(Constants.OSS_ENDPOINT, bucketName, Constants.OSS_ACCESSKEYID));
    }


    /**
     * ????????????
     *
     * @param view
     */
    public void download(View view) {
        // ??????????????????oss??????
        key = TextUtils.isEmpty(etKey.getText().toString()) ? "" : etKey.getText().toString();
        OssFileBean ossFileBean = new OssFileBean(Constants.OSS_ENDPOINT, bucketName, Constants.OSS_ACCESSKEYID, key);

        // ????????????????????????
        OssDownloadUtil.downloadObjectToFile(ossClient, ossFileBean, mLocalImgPath, new DownloadListener() {
            @Override
            public void onProgressChange(int index, long currentSize, long totalSize) {

            }

            @Override
            public void onSuccess(String path) {
                ToastUtils.showShort("????????????");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // log
                        StringBuilder sb = new StringBuilder();
                        sb.append("?????????????????????");
                        sb.append("\n").append("??????????????????").append(path);
                        tvLogdownload.setText(sb.toString());

                        showImg();
                    }
                });
            }

            @Override
            public void onFail(Exception exception) {

            }
        });
    }

    /**
     * ????????????
     *
     * @param view
     */
    public void upload(View view) {
        // ??????????????????????????????
        String uploadFilePath = mLocalImgPath;
        OssUploadUtil.uploadAsync(ossClient, new OssFileBean(Constants.OSS_ENDPOINT, bucketName, Constants.OSS_ACCESSKEYID), new File(uploadFilePath), new UploadListener() {
            @Override
            public void onProgressChange(int index, long currentSize, long totalSize) {

            }

            @Override
            public void onSuccess(List<OssFileBean> files) {
                ToastUtils.showShort("????????????");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String key = "";
                        if (files != null && files.size() > 0) {
                            key = files.get(0).getKey();
                        }
                        // log
                        StringBuilder sb = new StringBuilder();
                        sb.append("?????????????????????");
                        sb.append("\n").append("?????????oss???").append(key);
                        tvLogupload.setText(sb.toString());
                    }
                });
            }

            @Override
            public void onFail(ClientException clientException, ServiceException serviceException) {
                clientException.printStackTrace();
                ToastUtils.showShort(clientException.getMessage());
            }
        });
    }

    /**
     * ???????????????
     *
     * @param view
     */
    public void uploadMultiple(View view) {
        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.show();
        loadingDialog.setMsg("?????????");

        ArrayList<File> files = new ArrayList<>();
        files.add(new File(mLocalImgPath));
        files.add(new File(mLocalImgPath));
        files.add(new File(mLocalImgPath));
        OssUploadUtil.uploadMultipleAsync(ossClient, new OssFileBean(Constants.OSS_ENDPOINT, bucketName, Constants.OSS_ACCESSKEYID), files, new UploadListener() {
            @Override
            public void onProgressChange(int index, long currentSize, long totalSize) {
                loadingDialog.setMsg("?????????:" + index + "/" + files.size());
            }

            @Override
            public void onSuccess(List<OssFileBean> files) {
                loadingDialog.cancel();
                ToastUtils.showShort("????????????");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (files != null && files.size() > 0) {
                            // log
                            StringBuilder sb = new StringBuilder();
                            sb.append("?????????????????????");
                            for (OssFileBean ossFileBean :
                                    files) {
                                sb.append("\n").append("?????????oss???").append(ossFileBean.getKey());

                            }
                            tvLogupload.setText(sb.toString());
                        }
                    }
                });
            }

            @Override
            public void onFail(ClientException clientException, ServiceException serviceException) {
                loadingDialog.cancel();
                clientException.printStackTrace();
                ToastUtils.showShort(clientException.getMessage());
            }
        });
    }

    private void showImg() {
        // ????????????????????????
        if (new File(mLocalImgPath).exists()) {
            // ????????????
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(OssTestActivity.this)
                            .load(mLocalImgPath)
                            .error(getResources().getDrawable(R.drawable.error_circle))
                            .into(ivImg);
                }
            });
            // ??????????????????
            btUpload.setClickable(true);
            tvError.setVisibility(View.GONE);
        } else {
            btUpload.setClickable(false);
            tvError.setVisibility(View.VISIBLE);
        }
    }
}

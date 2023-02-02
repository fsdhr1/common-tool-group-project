package com.gykj.autoupdate.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.gykj.autoupdate.R;
import com.gykj.autoupdate.utils.InstallUtil;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;


public class ApplyActivity extends Activity {
    private int REQUESTCODE = 10086;
    private Uri mApkUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mApkUri = (Uri) bundle.get("apkUri");
        }
        showDialog();
    }

    private void showDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("安装应用需要打开未知来源权限，请去设置中开启权限")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        d.dismiss();

                        //https://blog.csdn.net/changmu175/article/details/78906829
                        Uri packageURI = Uri.parse("package:" + InstallUtil.getAppProcessName(ApplyActivity.this));
                        //注意这个是8.0新API
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                        startActivityForResult(intent, REQUESTCODE);
                    }
                }).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE && resultCode == RESULT_OK) {
            boolean isGranted = this.getPackageManager().canRequestPackageInstalls();
            if (isGranted) {
                Intent install = new Intent(Intent.ACTION_VIEW);
                //由于没有在Activity环境下启动Activity,设置下面的标签
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                install.setDataAndType(mApkUri, "application/vnd.android.package-archive");
                startActivity(install);

                // 结束当前Activity
                finish();
            } else {
                showDialog();
            }
        } else {
            showDialog();
        }
    }
}

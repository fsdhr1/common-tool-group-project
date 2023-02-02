package com.gykj.autoupdate.utils;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import com.gykj.autoupdate.ui.ApplyActivity;

public class InstallUtil {
    private Context mAct;
    private String mPath;//下载下来后文件的路径
    public static int UNKNOWN_CODE = 2018;


    public InstallUtil(Context mAct, String mPath) {
        this.mAct = mAct;
        this.mPath = mPath;
    }

    public void install() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startInstallO();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                startInstallN();
            } else {
                startInstall();
            }
//            ActivityUtils.finishAllActivities();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * android1.x-6.x
     */
    private void startInstall() throws Exception {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.parse("file://" + mPath);
        install.addCategory("android.intent.category.DEFAULT");
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        install.setDataAndType(uri, "application/vnd.android.package-archive");

        mAct.startActivity(install);
    }

    /**
     * android7.x
     */
    private void startInstallN() throws Exception {
        // 获取外部应用配置的authority，如果为空，默认使用${applicationId}.autoupdatefileprovider
        String authority = !TextUtils.isEmpty(UpdateAppUtil.mOutsideAuthority) ?
                UpdateAppUtil.mOutsideAuthority : getAuthority();

        //参数1 上下文, 参数2 在AndroidManifest中的android:authorities值, 参数3  共享的文件
        Uri apkUri = FileProvider.getUriForFile(mAct, authority, new File(mPath));
        Intent install = new Intent(Intent.ACTION_VIEW);
        //由于没有在Activity环境下启动Activity,设置下面的标签
        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //添加这一句表示对目标应用临时授权该Uri所代表的文件
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        mAct.startActivity(install);
    }

    /**
     * android8.x
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallO() throws Exception {
        // 获取是否允许安装未知来源权限
        // 要求项目build.gradle中targetSdkVersion是26以上才能获取正确的canRequestPackageInstalls，否则就一直返回false。
        boolean isGranted = mAct.getPackageManager().canRequestPackageInstalls();
        if (isGranted) {
            startInstallN();//安装应用的逻辑(写自己的就可以)
        } else {
            // 获取外部应用配置的authority，如果为空，默认使用${applicationId}.autoupdatefileprovider
            String authority = !TextUtils.isEmpty(UpdateAppUtil.mOutsideAuthority) ?
                    UpdateAppUtil.mOutsideAuthority : getAuthority();

            //参数1 上下文, 参数2 在AndroidManifest中的android:authorities值, 参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(mAct, authority, new File(mPath));

            // 跳转申请允许安装外部应用页面
            Intent intent = new Intent(mAct, ApplyActivity.class);
            intent.putExtra("apkUri", apkUri);
            mAct.startActivity(intent);
           /* new AlertDialog.Builder(mAct)
                    .setCancelable(false)
                    .setTitle("安装应用需要打开未知来源权限，请去设置中开启权限")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int w) {
                            //https://blog.csdn.net/changmu175/article/details/78906829
                            Uri packageURI = Uri.parse("package:" + mAct.getPackageName());
                            //注意这个是8.0新API
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                            mAct.startActivity(intent);
                        }
                    })
                    .show();*/
        }
    }


    /**
     * 获取默认的FileProvider
     * 返回：${applicationId}.autoupdatefileprovider
     */
    private String getAuthority() {
        String authority = "";
        // ${applicationId}.autoupdatefileprovider
       authority = getAppProcessName(mAct) + ".autoupdatefileprovider";

        // 默认自动更新module中配置的fileprovider
       // authority = "com.gykj.autoupdate.autoupdatefileprovider";
        return authority;
    }

    /**
     * 获取当前应用程序的包名
     *
     * @param context 上下文对象
     * @return 返回包名
     */
    public static String getAppProcessName(Context context) {
        //当前应用pid
        int pid = android.os.Process.myPid();
        //任务管理类
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //遍历所有应用
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid)//得到当前应用
            {
                return info.processName;//返回包名
            }
        }
        return "";
    }
}

package com.gykj.autoupdate.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gykj.autoupdate.R;
import com.gykj.autoupdate.bean.BaseResult;
import com.gykj.autoupdate.bean.SysVersion;
import com.gykj.autoupdate.utils.UpdateAppUtil;

/**
 * 自动更新检测服务
 * Created by ren on 2022/11/2
 */
public class CheckUpdateService extends Service {
    // 服务绑定对象
    private IBinder myBinder = new MyBinder();
    private SysVersion mCheckedSysVersion;
    private String mOssSignedUrl;
    private UpdateAppUtil mUpdateAppUtil;
    // 要展示更新弹窗的指定页面
    public static String mShowDialogActivityName;
    // 检测失败重试次数
    public static int mNumOfFailedReCheck = 3;
    // 检测失败重试间隔
    public static long mDelayMillisOfReCheck = 3000;
    // 弹窗失败，持续监测页面时长
    public static long mMillisOfKeepWatchOnActivity = -1;
    // 弹窗失败重试间隔
    public static long mDelayMillisOfReShowDialog = 3000;
    private long mTimeOfStartTryShowDialog;

    public class MyBinder extends Binder {
        // 获取绑定的服务
        public CheckUpdateService getService() {
            return CheckUpdateService.this;
        }
    }

    enum CheckHandlerMsgEnum {
        ReCheck(1),
        ReShowDialog(2),
        StopService(3);

        int what;

        CheckHandlerMsgEnum(int what) {
            this.what = what;
        }
    }

    private final Handler mCheckHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == CheckHandlerMsgEnum.ReCheck.what) {
                // 判断重新检测次数
                Log.e(getClass().getName(), String.format("剩余重试次数: %s", mNumOfFailedReCheck));
                if (mNumOfFailedReCheck <= 0) {
                    this.removeMessages(msg.what);
                    // 停止服务
                    this.sendEmptyMessage(CheckHandlerMsgEnum.StopService.what);
                    return;
                }

                // 检测新版本
                Log.e(getClass().getName(), "开始启动重新检测新版本信息！");
                mNumOfFailedReCheck--;

                Bundle bundle = msg.getData();
                checkUpdate(bundle.getString("checkApplicationKey"), bundle.getString("checkSignType"), bundle.getString("checkUrl"));
            } else if (msg.what == CheckHandlerMsgEnum.ReShowDialog.what) {
                // 判断监测时长
                long mWatchedTime = System.currentTimeMillis() - mTimeOfStartTryShowDialog;
                Log.e(getClass().getName(), String.format("已持续监测: %s ms, 设定监测时长%s ms", mWatchedTime, mMillisOfKeepWatchOnActivity));
                if (mMillisOfKeepWatchOnActivity != -1 && mWatchedTime >= mMillisOfKeepWatchOnActivity) {
                    this.removeMessages(msg.what);
                    // 停止服务
                    this.sendEmptyMessage(CheckHandlerMsgEnum.StopService.what);
                    return;
                }

                // 检测是否可以弹窗
                tryShowUpdateDialog();
            } else if (msg.what == CheckHandlerMsgEnum.StopService.what) {
                Log.e(getClass().getName(), "收到消息，要结束service");
                CheckUpdateService.this.stopSelf();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(getClass().getName(), "onCreate");
        // 展示前台通知栏
//        showForegroundNotification();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(getClass().getName(), "onBind");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(getClass().getName(), "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(getClass().getName(), "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(getClass().getName(), "onDestroy");
    }

    /**
     * 检测新版本
     *
     * @param checkApplicationKey
     * @param checkSignType
     * @param checkUrl
     */
    public void checkUpdate(final String checkApplicationKey, final String checkSignType, final String checkUrl) {
        // 获取util单例
        mUpdateAppUtil = UpdateAppUtil.getInstance(getApplicationContext(), "com.gykj.commontool.autoupdatefileprovider");
        // 1.检测新版本信息
        mUpdateAppUtil.checkNewVersionMsg(checkApplicationKey, checkSignType, checkUrl, new UpdateAppUtil.CheckNewVersionCallBack() {
            @Override
            public void onCheckNewVersionFail(Exception e) {
                e.printStackTrace();
                ToastUtils.showShort(e.getMessage());
                Log.e(getClass().getName(), "检测新版本信息失败，稍后重试...");

                // 重新检测新版本信息
                Bundle bundle = new Bundle();
                bundle.putString("checkApplicationKey", checkApplicationKey);
                bundle.putString("checkSignType", checkSignType);
                bundle.putString("checkUrl", checkUrl);
                Message msg = new Message();
                msg.what = CheckHandlerMsgEnum.ReCheck.what;
                msg.setData(bundle);
                mCheckHandler.sendMessageDelayed(msg, mDelayMillisOfReCheck);
            }

            @Override
            public void onCheckNewVersionSuccess(final BaseResult<SysVersion> baseResult) {
                // 2.解析新版本信息
                mUpdateAppUtil.dealOSSSysVersion(baseResult, new UpdateAppUtil.GetOSSSignedURLCallBack() {
                    @Override
                    public void onSuccess(String ossSignedUrl) {
                        // 3.尝试更新弹窗
                        mCheckedSysVersion = baseResult.getData();
                        mOssSignedUrl = ossSignedUrl;

                        mTimeOfStartTryShowDialog = System.currentTimeMillis();
                        tryShowUpdateDialog();
                    }
                });
            }
        });
    }

    // 尝试更新弹窗
    private void tryShowUpdateDialog() {
        // 获取当前活动activity
        Activity mTopActivity = ActivityUtils.getTopActivity();
        Log.e(getClass().getName(), String.format("当前活动activity：[%s]，尝试显示版本更新弹窗...", mTopActivity.getComponentName().getClassName()));
        if (!TextUtils.isEmpty(mShowDialogActivityName) && !mTopActivity.getComponentName().getClassName().contains(mShowDialogActivityName)) {
            Log.e(getClass().getName(), String.format("非[%s]，不允许弹窗，稍后重试...", mShowDialogActivityName));
            mCheckHandler.sendEmptyMessageDelayed(CheckHandlerMsgEnum.ReShowDialog.what, mDelayMillisOfReShowDialog);
            return;
        }
        mUpdateAppUtil.showUpdateDialog(mCheckedSysVersion, mOssSignedUrl,null);
        Log.e(getClass().getName(), "弹窗成功！");
        // 停止服务
        mCheckHandler.sendEmptyMessage(CheckHandlerMsgEnum.StopService.what);
    }

    /**
     * 展示前台通知栏
     */
    private void showForegroundNotification() {
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // 注意notification也要适配Android 8 哦
            // 将该服务转为前台服务
            startForeground(0x0001, new Notification());// 通知栏标识符 前台进程对象唯一ID
        }*/
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // 注意notification也要适配Android 8 哦
            NotificationChannel channel = new NotificationChannel("MyCheckUpdateService_ID", "MyCheckUpdateService_NAME", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLightColor(Color.RED);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }

            Notification notification = new Notification.Builder(this, "MyCheckUpdateService_ID")
                    .setTicker("Nature")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("这是一个测试标题")
                    .setContentText("这是一个测试内容")
                    .build();
//            notification.flags |= Notification.FLAG_NO_CLEAR;
            // 将该服务转为前台服务
            startForeground(0x0001, notification);// 通知栏标识符 前台进程对象唯一ID
        }
    }
}

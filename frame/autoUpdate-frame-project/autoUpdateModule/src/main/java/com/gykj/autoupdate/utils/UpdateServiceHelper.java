package com.gykj.autoupdate.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.gykj.autoupdate.ui.CheckUpdateService;

/**
 * Created by ren on 2022/11/2
 */
public class UpdateServiceHelper {

    private static UpdateServiceHelper mUpdateServiceHelper;
    private final Context mContext;
    private static String mCheckApplicationKey;
    private static String mCheckSignType;
    private static String mCheckUrl;
    private boolean isBind = false;

    private UpdateServiceHelper(Context context) {
        this.mContext = context;
    }

    private static synchronized UpdateServiceHelper getInstance(Context context) {
        if (mUpdateServiceHelper == null) {
            mUpdateServiceHelper = new UpdateServiceHelper(context);
        }
        return mUpdateServiceHelper;
    }

    public static synchronized UpdateServiceHelper getInstance(Context context, String applicationKey, String signType) {
        mCheckApplicationKey = applicationKey;
        mCheckSignType = signType;
        return getInstance(context);
    }

    /**
     * 传入自定义的外部Authority
     * <p>
     * 不传默认使用的Authority：${applicationId}.autoupdatefileprovider
     *
     * @param outsideAuthority
     */
    public UpdateServiceHelper setOutsideAuthority(String outsideAuthority) {
        UpdateAppUtil.mOutsideAuthority = outsideAuthority;
        return mUpdateServiceHelper;
    }

    /**
     * 设置检测url
     * <p>
     * 不设置默认使用国源的大数据平台地址
     *
     * @param otherCheckUrl
     */
    public UpdateServiceHelper setOtherCheckUrl(String otherCheckUrl) {
        mCheckUrl = otherCheckUrl;
        return mUpdateServiceHelper;
    }

    /**
     * 设置要展示更新弹窗的指定页面，
     * <p>
     * 不指定的话在默认栈顶页面展示
     *
     * @param showDialogActivityName
     */
    public UpdateServiceHelper setShowDialogActivityName(String showDialogActivityName) {
        CheckUpdateService.mShowDialogActivityName = showDialogActivityName;
        return mUpdateServiceHelper;
    }

    /**
     * 检测失败，设置重试次数。
     * <p>
     * 不设置默认重试3次
     *
     * @param reCheckNum
     */
    public UpdateServiceHelper setNumOfFailedReCheck(int reCheckNum) {
        CheckUpdateService.mNumOfFailedReCheck = reCheckNum;
        return mUpdateServiceHelper;
    }

    /**
     * 检测失败，设置重试间隔。
     * <p>
     * 不设置默认重试3s
     *
     * @param delayMillis
     */
    public UpdateServiceHelper setDelayMillisOfReCheck(long delayMillis) {
        CheckUpdateService.mDelayMillisOfReCheck = delayMillis;
        return mUpdateServiceHelper;
    }

    /**
     * 弹窗失败，设置持续监测时长
     * <p>
     * 不设置默认一直监测
     *
     * @param keepWatchMillis
     */
    public UpdateServiceHelper setTimeOfKeepWatchOnActivity(long keepWatchMillis) {
        CheckUpdateService.mMillisOfKeepWatchOnActivity = keepWatchMillis;
        return mUpdateServiceHelper;
    }

    /**
     * 弹窗失败，设置重试间隔
     * <p>
     * 不设置默认重试3s
     *
     * @param delayMillis
     */
    public UpdateServiceHelper setDelayMillisOfReShowDialog(long delayMillis) {
        CheckUpdateService.mDelayMillisOfReShowDialog = delayMillis;
        return mUpdateServiceHelper;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(getClass().getName(), "onServiceConnected");
            // 检查权限
            PermisionUtils.verifyStoragePermissions(ActivityUtils.getTopActivity());

            // 获取绑定的service
            CheckUpdateService myService = ((CheckUpdateService.MyBinder) service).getService();
            // 开始检测更新
            myService.checkUpdate(mCheckApplicationKey, mCheckSignType, mCheckUrl);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(getClass().getName(), "onServiceDisconnected");
        }
    };

    /**
     * 开启服务
     */
    public void startCheck() {
        Intent checkUpdateServiceIntent = new Intent(mContext, CheckUpdateService.class);
        mContext.bindService(checkUpdateServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        isBind = true;
    }

    /**
     * 停止服务
     */
    public void stopCheck() {
        try {
            if (isBind) {
                mContext.unbindService(mServiceConnection);
                isBind = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

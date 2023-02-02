package com.gykj.locationservice.baseservices.v1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.gykj.locationservice.baseservices.v1.i.IBaseServiceLifeCycle;

import java.lang.reflect.Method;

/**
 * Created by zy on 2016/11/25.
 * 前台服务基类
 */

public abstract class BaseFrontService extends Service {

    protected IBaseServiceLifeCycle iBaseServiceLifeCycle;
    /**
     * 自定义通知栏布局
     */
    protected int noticeId = 0x004;
    protected NotificationManager manager;
    protected Notification notification;
    protected RemoteViews contentView;


    protected static final String TAG = "BaseFrontService";
    protected boolean mReflectFlg = false;
    protected static final int NOTIFICATION_ID = 1; // 如果id设置为0,会导致不能设置为前台service
    protected static final Class<?>[] mSetForegroundSignature = new Class[]{boolean.class};
    protected static final Class<?>[] mStartForegroundSignature = new Class[]{int.class, Notification.class};
    protected static final Class<?>[] mStopForegroundSignature = new Class[]{boolean.class};

    protected NotificationManager mNM;
    protected Method mSetForeground;
    protected Method mStartForeground;
    protected Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        if (iBaseServiceLifeCycle != null) {
            iBaseServiceLifeCycle.serviceCreate();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");
        if (iBaseServiceLifeCycle != null) {
            iBaseServiceLifeCycle.serviceStartCommand();
        }
        createFrontNotice();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (iBaseServiceLifeCycle != null) {
            iBaseServiceLifeCycle.serviceDestroy();
        }
        stopForegroundCompat(NOTIFICATION_ID);
    }

    public void setIBaseServiceLifeCycle(IBaseServiceLifeCycle iBaseServiceLifeCycle) {
        this.iBaseServiceLifeCycle = iBaseServiceLifeCycle;
    }

    void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(this, args);
        } catch (Exception e) {
            Log.w("ApiDemos", "Unable to invoke method", e);
        }
    }

    //创建前台通知
    protected void createFrontNotice() {
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            mStartForeground = BaseFrontService.class.getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = BaseFrontService.class.getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            mStartForeground = mStopForeground = null;
        }
        try {
            mSetForeground = getClass().getMethod("setForeground", mSetForegroundSignature);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
        }
        setNoticeStyle();
        startForegroundCompat(noticeId, notification);
    }

    protected abstract void setNoticeStyle();

    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    protected void startForegroundCompat(int id, Notification notification) {
        if (mReflectFlg) {
            // If we have the new startForeground API, then use it.
            if (mStartForeground != null) {
                mStartForegroundArgs[0] = Integer.valueOf(id);
                mStartForegroundArgs[1] = notification;
                invokeMethod(mStartForeground, mStartForegroundArgs);
                return;
            }
            // Fall back on the old API.
            mSetForegroundArgs[0] = Boolean.TRUE;
            invokeMethod(mSetForeground, mSetForegroundArgs);
            mNM.notify(id, notification);
        } else {
            /* 还可以使用以下方法，当sdk大于等于5时，调用sdk现有的方法startForeground设置前台运行，
             * 否则调用反射取得的sdk level 5（对应Android 2.0）以下才有的旧方法setForeground设置前台运行 */
            if (Build.VERSION.SDK_INT >= 5) {
                startForeground(id, notification);
            } else {
                // Fall back on the old API.
                mSetForegroundArgs[0] = Boolean.TRUE;
                invokeMethod(mSetForeground, mSetForegroundArgs);
                mNM.notify(id, notification);
            }
        }
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id) {
        if (mReflectFlg) {
            // If we have the new stopForeground API, then use it.
            if (mStopForeground != null) {
                mStopForegroundArgs[0] = Boolean.TRUE;
                invokeMethod(mStopForeground, mStopForegroundArgs);
                return;
            }
            // Fall back on the old API.  Note to cancel BEFORE changing the
            // foreground state, since we could be killed at that point.
            mNM.cancel(id);
            mSetForegroundArgs[0] = Boolean.FALSE;
            invokeMethod(mSetForeground, mSetForegroundArgs);
        } else {
            /* 还可以使用以下方法，当sdk大于等于5时，调用sdk现有的方法stopForeground停止前台运行，
             * 否则调用反射取得的sdk level 5（对应Android 2.0）以下才有的旧方法setForeground停止前台运行 */
            if (Build.VERSION.SDK_INT >= 5) {
                stopForeground(true);
            } else {
                // Fall back on the old API.  Note to cancel BEFORE changing the
                // foreground state, since we could be killed at that point.
                mNM.cancel(id);
                mSetForegroundArgs[0] = Boolean.FALSE;
                invokeMethod(mSetForeground, mSetForegroundArgs);
            }
        }
    }

}

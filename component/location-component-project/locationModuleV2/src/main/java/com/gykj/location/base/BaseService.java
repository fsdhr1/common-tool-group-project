package com.gykj.location.base;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.gykj.location.interfaces.IBaseServiceLifeCycle;

import java.lang.reflect.Method;
import java.util.Random;

/**
 * Created by zy on 2017/11/11.
 */

public abstract class BaseService extends Service {

    protected IBaseServiceLifeCycle iBaseServiceLifeCycle;

    protected BaseServiceBinder baseServiceBinder;

    protected boolean serviceRunningInCreate = false;
    protected boolean serviceRunningInCommand = false;


    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("返回Binder");
        baseServiceBinder = new BaseServiceBinder();
        baseServiceBinder.setBaseService(this);
        return baseServiceBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("创建");
        doInBackGroundingInitialize();
        if (iBaseServiceLifeCycle != null) {
            iBaseServiceLifeCycle.serviceCreate();
        }
        if (serviceRunningInCreate) {
            new Thread() {
                @Override
                public void run() {
                    while (serviceRunningInCreate) {
                        //System.out.println("doInBackGrounding");
                        doInBackGrounding();
                    }
                    doInBackGrounded();
                }
            }.start();
        }
    }

    protected abstract void doInBackGroundingInitialize();

    protected abstract void doInBackGrounding();

    protected abstract void doCommandInBackGroundingInitialize();

    protected abstract void doCommandInBackGrounding();

    protected void doInBackGrounded() {
        serviceRunningInCreate = false;
        serviceRunningInCommand = false;
        stopSelf();
        //System.out.println("》》》》服务自己停止");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (iBaseServiceLifeCycle != null) {
            iBaseServiceLifeCycle.serviceStartCommand();
        }
        System.out.println("onStartCommand");
        doCommandInBackGroundingInitialize();

        if (serviceRunningInCommand) {
            new Thread() {
                @Override
                public void run() {
                    while (serviceRunningInCommand) {
                        System.out.println("doCommandInBackGrounding");
                        doCommandInBackGrounding();
                    }
                    doInBackGrounded();
                }
            }.start();
        }

        if (isFrontService) {
            super.onStartCommand(intent, flags, startId);
            createFrontNotice();
            return START_STICKY;
        } else {
            return super.onStartCommand(intent, flags, startId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceRunningInCreate = false;
        serviceRunningInCommand = false;
        if (iBaseServiceLifeCycle != null) {
            iBaseServiceLifeCycle.serviceDestroy();
        }
        if (isFrontService) {
            stopForegroundCompat(NOTIFICATION_ID);
        }
        System.out.println("销毁");
    }

    public void setIBaseServiceLifeCycle(IBaseServiceLifeCycle iBaseServiceLifeCycle) {
        this.iBaseServiceLifeCycle = iBaseServiceLifeCycle;
    }


    //===========================前台服务的通知============================
    protected NotificationManager notificationManager;
    protected NotificationCompat.Builder notificationCompatBuilder;

    /**
     * 是否创建前台服务
     */
    protected boolean isFrontService = false;
    protected int frontServiceNotificationId;
    protected Notification frontServiceNotification;
    protected String frontServiceNotificationTitle;
    protected String frontServiceNotificationContent;

    private boolean mReflectFlg = false;
    private static final int NOTIFICATION_ID = 1; // 如果id设置为0,会导致不能设置为前台service
    private static final Class<?>[] mSetForegroundSignature = new Class[]{boolean.class};
    private static final Class<?>[] mStartForegroundSignature = new Class[]{int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[]{boolean.class};

    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    //创建前台通知
    protected void createFrontNotice() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        try {
            mStartForeground = BaseService.class.getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = BaseService.class.getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            mStartForeground = mStopForeground = null;
        }
        try {
            mSetForeground = getClass().getMethod("setForeground", mSetForegroundSignature);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
        }
        int max = 100000;
        int min = 10000;
        Random random = new Random();
        frontServiceNotificationId = random.nextInt(max) % (max - min + 1) + min;
        frontServiceNoticeStyle();
        startForegroundCompat(frontServiceNotificationId, frontServiceNotification);
    }

    /**
     * 创建通知栏
     */
    protected void frontServiceNoticeStyle() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        if (notificationCompatBuilder == null) {
            notificationCompatBuilder = new NotificationCompat.Builder(this);
        }

        //定义一个PendingIntent，当用户点击通知时，跳转到某个Activity(也可以发送广播等)
        //Intent intent = new Intent(this, activity);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        frontServiceNotification = notificationCompatBuilder
                .setContentTitle(frontServiceNotificationTitle)
                .setContentText(frontServiceNotificationContent)
                .setWhen(System.currentTimeMillis())
                //.setSmallIcon(R.mipmap.ic_loc)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_loc))
                //.setContentIntent(pendingIntent)
                .build();
        notificationManager.notify(frontServiceNotificationId, frontServiceNotification);
    }

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
            notificationManager.notify(id, notification);
        } else {
            /* 还可以使用以下方法，当sdk大于等于5时，调用sdk现有的方法startForeground设置前台运行，
             * 否则调用反射取得的sdk level 5（对应Android 2.0）以下才有的旧方法setForeground设置前台运行 */
            if (Build.VERSION.SDK_INT >= 5) {
                startForeground(id, notification);
            } else {
                // Fall back on the old API.
                mSetForegroundArgs[0] = Boolean.TRUE;
                invokeMethod(mSetForeground, mSetForegroundArgs);
                notificationManager.notify(id, notification);
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
            notificationManager.cancel(id);
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
                notificationManager.cancel(id);
                mSetForegroundArgs[0] = Boolean.FALSE;
                invokeMethod(mSetForeground, mSetForegroundArgs);
            }
        }
    }

    void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(this, args);
        } catch (Exception e) {
            Log.w("ApiDemos", "Unable to invoke method", e);
        }
    }
}

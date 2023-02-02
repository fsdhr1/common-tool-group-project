package com.gradtech.mapframev10.core.snapshot.custom;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName DbCommand
 * @Description TODO
 * @Author: fs
 * @Date: 2021/10/21 14:15
 * @Version 2.0
 */
public abstract class DbCommand<T> {

    //最大可用的CPU核数
    public static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    //线程最大的空闲存活时间，单位为秒
    public static final int KEEPALIVETIME = 60;
    //任务缓存队列长度
    public static final int BLOCKINGQUEUE_LENGTH = 500;

    //线程池
    private static ThreadPoolExecutor threadPool;
    //线程工厂
    private static ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "myThreadPool thread:" + integer.getAndIncrement());
        }
    };
    static {
        threadPool = new ThreadPoolExecutor(PROCESSORS * 2, PROCESSORS * 4, KEEPALIVETIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(BLOCKINGQUEUE_LENGTH),threadFactory);
    }
    // 主线程消息队列的 Handler
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    // 执行数据库操作
    public final void execute() {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    postResult(doInBackground());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    // 将结果投递到 UI 线程
    private void postResult(final T result) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                onPostExecute(result);
            }
        });
    }

    // 在后台执行的数据库操作
    protected abstract T doInBackground();

    // 在 UI 线程执行的操作
    protected void onPostExecute(T result) {

    }


}

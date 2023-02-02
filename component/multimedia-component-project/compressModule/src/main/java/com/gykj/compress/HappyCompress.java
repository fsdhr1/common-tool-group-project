package com.gykj.compress;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.gykj.compress.interfaces.onHappyCompressListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by ZhaiJiaChang.
 * <p>
 * Date: 2022/8/17
 */
public class HappyCompress {

    private String mTargetDir;
    private String mCustomPackage;
    private int mLeastCompressSize = 200;//压缩成不大于200kb的图片
    private onHappyCompressListener mCompressListener;
    private List<File> mSourceList;
    private Handler mHandler;
    private Context mContext;
    private CompositeDisposable mDisposable;

    public HappyCompress(Builder builder) {
        this.mTargetDir = builder.mTargetDir;
        this.mSourceList = builder.mSourceList;
        this.mCompressListener = builder.mCompressListener;
        this.mLeastCompressSize = builder.mLeastCompressSize;
        this.mContext = builder.context;
        this.mCustomPackage = builder.mCustomPackage;
        //
        mDisposable = new CompositeDisposable();
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        private Context context;
        private String mTargetDir;
        private String mCustomPackage = "HappyCompress";// 默认文件名称
        private int mLeastCompressSize = 200;// 默认200kb
        private List<File> mSourceList;
        private onHappyCompressListener mCompressListener;


        Builder(Context context) {
            this.context = context;
            this.mSourceList = new ArrayList<>();
        }

        /**
         * 添加文件
         */
        public Builder load(File file) {
            if (FileCheckUtils.checkFile(file)) {
                mSourceList.add(file);
            }
            return this;
        }


        /**
         * 添加图片存储路径
         */
        public Builder load(final String string) {
            if (!TextUtils.isEmpty(string)) {
                load(new File(string));
            }
            return this;
        }

        /**
         * 添加列表
         */
        public <T> Builder load(List<T> list) {
            for (T src : list) {
                if (src instanceof String) {
                    load((String) src);
                } else if (src instanceof File) {
                    load((File) src);
                } else {
                    throw new IllegalArgumentException("图片类型添加错误，图片必须是存储路径、文件中的一种");
                }
            }
            return this;
        }

        /**
         * 压缩后图片的存储路径
         */
        public Builder setTargetDir(String targetDir) {
            this.mTargetDir = targetDir;
            return this;
        }

        /**
         * 压缩后图片的存储文件夹
         */
        public Builder setCustomDirName(String mCustomPackage) {
            this.mCustomPackage = mCustomPackage;
            return this;
        }

        /**
         * 添加图片最大限制，超过这个限制就会被压缩
         * 默认200KB，小于200kb不压缩
         */
        public Builder ignoreBy(int size) {
            this.mLeastCompressSize = size;
            return this;
        }

        /**
         * 设置压缩回调的监听
         */
        public Builder setOnCompressListener(onHappyCompressListener mListener) {
            this.mCompressListener = mListener;
            return this;
        }

        /**
         * 创建对象 内部调用
         */
        private HappyCompress build() {
            return new HappyCompress(this);
        }

        /**
         * 真正开始的方法
         */
        public void startCompress() {
            build().startCompress();
        }
    }

    /**
     * 开始压缩
     */
    private void startCompress() {
        if (mSourceList == null || mSourceList.size() == 0) {
            Toast.makeText(mContext, "请添加图片", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mCompressListener == null) {
            Toast.makeText(mContext, "请添加图片压缩的回调", Toast.LENGTH_SHORT).show();
            return;
        }
        //
        // 开始
        mDisposable.clear();
        mCompressListener.onStart();
        mDisposable.add(Flowable.just(mSourceList)
                .observeOn(Schedulers.io())
                .map(new Function<List<File>, List<File>>() {
                    @Override
                    public List<File> apply(@NonNull List<File> list) throws Exception {
//                        startTime = System.currentTimeMillis();
                        List<File> mResult = new ArrayList<>();
                        Iterator<File> iterator = mSourceList.iterator();
                        while (iterator.hasNext()) {
                            mResult.add(compressReal(mContext, iterator.next()));
                            iterator.remove();
                        }
                        for (File mFile : mResult) {
                            Log.e("mDisposable: ", " -- fileName -- " + mFile.getPath());
                        }
                        return mResult;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        //
                        mCompressListener.onError(throwable);
                    }
                }).onErrorResumeNext(Flowable.<List<File>>empty())
                .subscribe(new Consumer<List<File>>() {
                    @Override
                    public void accept(@NonNull List<File> list) {
                        // 结束
                        long end = System.currentTimeMillis();
                        mCompressListener.onSuccess(list);
//                        Log.e("get: ", " -- apply 切换到主线程 -- " + Thread.currentThread().getName() + " -- 耗时 " + (end - startTime) / 1000);
                        for (File file : list) {
                            Log.e("mDisposable: ", file.getAbsolutePath());
                        }
                    }
                }));
    }


    private File compressReal(Context context, File mSourceFile) throws IOException {
        File result;

//        File outFile = FileCheckUtils.getCreateExternalCacheDir(context, FileCheckUtils.extSuffix(mSourceFile));
        File outFile = FileCheckUtils.getCreateExternalCacheDir(context, mCustomPackage);

        String filename = mSourceFile.getName();
        outFile = FileCheckUtils.getCustomReNameFile(context, outFile.getAbsolutePath(), filename);
        Log.e("get: ", " -- startCompress -- ");
        // 执行开始
        if (!mSourceFile.getName().endsWith(".gif")
                && FileCheckUtils.needCompress(mLeastCompressSize, mSourceFile)) {
            result = new HappyCompressUtils(mSourceFile, outFile).compress(mLeastCompressSize);
        } else {
            result = mSourceFile;
        }
        Log.e("get: ", " -- endCompress -- ");
        return result;
    }


}

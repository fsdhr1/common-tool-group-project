package com.gykj.compress.interfaces;

import java.io.File;
import java.util.List;

/**
 * Created by ZhaiJiaChang.
 * <p>
 * Date: 2022/8/17
 */
public interface onHappyCompressListener {

    default void onStart() {
    }

    void onSuccess(List<File> mFiles);

    default void onError(Throwable mThrowable) {
    }

}

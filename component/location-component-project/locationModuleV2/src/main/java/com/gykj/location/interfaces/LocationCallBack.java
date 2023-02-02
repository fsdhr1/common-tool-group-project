package com.gykj.location.interfaces;

import android.content.Context;

import com.gykj.location.ServiceLocation;

/**
 * Created by ZhaiJiaChang.
 * <p>
 * Date: 2021/12/7
 */
public interface LocationCallBack {
    void onLocationCallBack(Context mContext, ServiceLocation mBean);
}

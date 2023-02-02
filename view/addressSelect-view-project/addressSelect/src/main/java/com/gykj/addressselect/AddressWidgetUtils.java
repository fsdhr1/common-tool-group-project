package com.gykj.addressselect;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * Created by ZhaiJiaChang.
 *
 * @author 10765
 */
public class AddressWidgetUtils {


    /**
     * 获取缓存
     */
    public AddressWidget getWidgetCache(FragmentActivity mActivity) {
        return AddressWidget.getInstance().getWidgetCached(mActivity);
    }

    public AddressWidget getWidgetCache(Fragment mFragment) {
        return AddressWidget.getInstance().getWidgetCached(mFragment);
    }

    /**
     * 重新创建
     */
    public AddressWidget getWidgetNew(FragmentActivity mActivity) {
        return AddressWidget.getNewInstance().createNewWidget(mActivity);
    }

    public AddressWidget getWidgetNew(Fragment mFragment) {
        return AddressWidget.getNewInstance().createNewWidget(mFragment);
    }

    public static AddressWidgetUtils getInstance() {
        return CreateSingle.mUtils;
    }

    public static AddressWidgetUtils getNewInstance() {
        return new AddressWidgetUtils();
    }


    /**
     * 页面结束时调用
     */
    public AddressWidget onWidgetDestory() {
        AddressWidget mInstance = AddressWidget.getInstance();
        mInstance.onWidgetDestory();
        return mInstance;
    }

    private static class CreateSingle {
        private static final AddressWidgetUtils mUtils = new AddressWidgetUtils();
    }

}

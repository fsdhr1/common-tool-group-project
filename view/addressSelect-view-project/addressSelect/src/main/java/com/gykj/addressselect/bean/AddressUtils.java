package com.gykj.addressselect.bean;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;


public class AddressUtils {
    public AddressUtils() {
    }

    public static boolean isEmpty(List list) {
        return list == null || list.size() == 0;
    }

    public static boolean notEmpty(List list) {
        return list != null && list.size() > 0;
    }

    public static <T> ArrayList<T> getArrayList(List<T> mList) {
        ArrayList<T> mArrayList = new ArrayList<>();
        if (mList == null) {
            return mArrayList;
        }
        for (T mBean : mList) {
            mArrayList.add(mBean);
        }
        return mArrayList;
    }

    /**
     * 查看是不是选择的顶部的全部区划
     */
    public static boolean isParentAddress(AddreBean mBean) {
        boolean mCustom = mBean.getCustom();
        return mCustom;
    }
}

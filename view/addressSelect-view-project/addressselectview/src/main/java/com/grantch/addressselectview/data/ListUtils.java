package com.grantch.addressselectview.data;

import java.util.ArrayList;
import java.util.List;


public class ListUtils {
    public ListUtils() {
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
}

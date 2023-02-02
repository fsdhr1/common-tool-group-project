package com.gradtech.mapframev10.core.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zy
 * @date 2017/6/17
 * 数组与集合之间的转换
 */

public final class CollectUtil {

    private CollectUtil() {

    }

    /**
     * 集合转数组
     *
     * @param list
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T[] list2TArray(List list, Class clazz) {
        T[] TArray = null;
        if (list != null && list.size() > 0) {
            TArray = (T[]) Array.newInstance(clazz, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(TArray, i, list.get(i));
            }
        }
        return TArray;
    }


    /**
     * 集合转数组
     *
     * @param list
     * @return
     */
    public static int[] list2TArray(List list) {
        int[] TArray = null;
        if (list != null && list.size() > 0) {
            TArray = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                TArray[i] = (int) list.get(i);
            }
        }
        return TArray;
    }


    /**
     * 集合转数组
     *
     * @param list
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T[] tArray2List(List list, Class clazz) {
        T[] TArray = null;
        if (list != null && list.size() > 0) {
            TArray = (T[]) Array.newInstance(clazz, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(TArray, i, list.get(i));
            }
        }
        return TArray;
    }

    /**
     * 数组转集合
     *
     * @param array
     * @param <T>
     * @return
     */
    public static <T> List<T> array2List(T[] array) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            T t = array[i];
            list.add(t);
        }
        return list;
    }

    public static <T> List<Object> ListT2ListObj(List<T> list) {
        List<Object> objList = new ArrayList<>();
        for (Object e : list) {
            Object obj = (Object) e;
            objList.add(obj);
        }
        return objList;
    }


    public static <T> T[] removeNullFromArray(T[] array, Class clazz) {
        if (array == null) {
            return null;
        }
        List<T> list = null;
        T t = null;
        for (int i = 0; i < array.length; i++) {
            t = array[i];
            if (list == null) {
                list = new ArrayList<>();
            }
            if (t != null) {
                list.add(t);
            }
        }
        if (list == null) {
            return null;
        }
        return tArray2List(list, clazz);
    }

}

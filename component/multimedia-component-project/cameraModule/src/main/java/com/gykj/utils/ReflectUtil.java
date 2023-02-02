package com.gykj.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by zy on 2017/1/20.
 * 反射帮助
 */
public final class ReflectUtil {

    private ReflectUtil() {
    }

    /**
     * @param obj
     * @param name
     * @param <T>
     * @return
     */
    public static <T> T getValByNumberVar(Object obj, String name) {
        if (obj == null) {
            return null;
        }
        try {
            Field field = getFieldByName(obj.getClass(), name);
            if (field == null) return null;
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) return null;
            field.setAccessible(true);
            Object val = field.get(obj);
            if (val == null) return null;
            return (T) val;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getFieldByName(Class clazz, String name) {
        if (clazz == null || name == null) {
            return null;
        }
        try {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals(name)) {
                    return field;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

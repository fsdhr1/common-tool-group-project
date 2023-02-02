package com.grandtech.mapframe.orm.data.model;

import androidx.room.TypeConverter;

/**
 * @ClassName q
 * @Description TODO
 * @Author: fs
 * @Date: 2021/8/19 16:17
 * @Version 2.0
 */
public class Converters {
    @TypeConverter
    public static GEOMETRY fromString(String shape) {
        return new GEOMETRY();
    }
    @TypeConverter
    public static String fromGEOMETRY(GEOMETRY geometry) {
        return "json";
    }
}

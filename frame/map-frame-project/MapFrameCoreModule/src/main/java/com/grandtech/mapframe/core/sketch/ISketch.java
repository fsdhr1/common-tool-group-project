package com.grandtech.mapframe.core.sketch;

import androidx.annotation.NonNull;

import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.mapbox.geojson.Geometry;

/**
 * @ClassName ISketch
 * @Description TODO 绘制需要实现的通用接口
 * @Author: fs
 * @Date: 2021/6/10 16:48
 * @Version 2.0
 */
public interface ISketch {


    /**
     * 设置绘制类型
     */
    public void setSketchType(@NonNull GeometryType geometryType);

    /**
     * 启动绘制
     */
    public void startSketch();


    public void stopSketch();

    /**
     * 判断当前绘制工具是否启动
     */
    public Boolean isStartSketch();


    /**
     * @Description 获取图形
     */
    public Geometry getSketchGeometry();


    /**
     * @Description 给绘制工具添加初始图形
     * @param geometry
     */
    public void addSketchGeometry(@NonNull Geometry geometry);
}

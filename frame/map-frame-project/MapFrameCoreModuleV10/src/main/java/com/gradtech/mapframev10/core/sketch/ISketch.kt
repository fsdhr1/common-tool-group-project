package com.gradtech.mapframev10.core.sketch

import com.gradtech.mapframev10.core.enumeration.GeometryType
import com.mapbox.geojson.Geometry

/**
 * @ClassName ISketch
 * @Description TODO
 * @Author: fs
 * @Date: 2022/8/3 13:48
 * @Version 2.0
 */
interface ISketch {
    /**
     * 设置绘制类型
     */
    fun setSketchType(geometryType: GeometryType)

    /**
     * 启动绘制
     */
    fun startSketch()
    fun stopSketch()

    /**
     * 判断当前绘制工具是否启动
     */
    fun isStartSketch():Boolean

    /**
     * @Description 获取图形
     */
    fun getSketchGeometry(): SketchGeometry?

    /**
     * @Description 给绘制工具添加初始图形
     * @param geometry
     */
    fun setSketchGeometry(geometry: Geometry)

    fun clearSketch()

    fun tryUnDo()

    fun tryReDo()
}
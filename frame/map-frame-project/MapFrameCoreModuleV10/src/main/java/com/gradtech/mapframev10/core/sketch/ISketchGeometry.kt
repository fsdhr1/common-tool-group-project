package com.gradtech.mapframev10.core.sketch

import com.gradtech.mapframev10.core.enumeration.GeometryType
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point

/**
 * @ClassName SketchGeometry
 * @Description TODO 自定义Geometry
 * @Author: fs
 * @Date: 2022/8/19 8:27
 * @Version 2.0
 */
interface ISketchGeometry {
    /**
     * isSimulate 用于判断是否只是返回一个Geometry，并不改变对象内实际的Geometry
     */
    fun addPoint(point:Point,isSimulate:Boolean = false):Geometry?

    fun undo()

    fun redo()

    fun addPoints(points:MutableList<Point>):Geometry?

    fun setGeometry(geometry: Geometry?)

    fun getGeometry(): Geometry?;


    fun setGeometryType(geometryType: GeometryType)

    fun getGeometryType():GeometryType;

    fun updatePoint(oldTagPoint: Point, tagpoint: Point,isSimulate:Boolean = false):Geometry? ;
}
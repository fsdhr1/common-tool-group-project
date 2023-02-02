package com.gradtech.mapframev10.core.sketch

import android.util.Log
import com.gradtech.mapframev10.core.enumeration.GeometryType
import com.gradtech.mapframev10.core.util.GsonFactory
import com.gradtech.mapframev10.core.util.Transformation
import com.mapbox.geojson.*
import com.mapbox.turf.TurfConversion
import com.mapbox.turf.TurfMeta.coordAll
import org.jetbrains.annotations.NotNull

/**
 * @ClassName SketchGeometry
 * @Description TODO
 * @Author: fs
 * @Date: 2022/8/19 8:39
 * @Version 2.0
 */
class SketchGeometry : ISketchGeometry {

    private var mGeometry: Geometry? = null;

    lateinit private var mGeometryType: GeometryType;

    private var mGeometryHistoryUndo: ArrayList<String> = arrayListOf();

    private var mGeometryHistoryRedo: ArrayList<String> = arrayListOf();

    private var mCurrent = 0;

    constructor(mGeometryType: GeometryType) {
        this.mGeometryType = mGeometryType
    }


    override fun addPoint(@NotNull point: Point,isSimulate:Boolean):Geometry? {
        val toJson = GsonFactory.getFactory().comMapBoxGson.toJson(mGeometry);
        if(!isSimulate){
            mGeometryHistoryUndo.add(toJson);
        }
        if (mGeometryType == GeometryType.point) {
            mGeometry = point;
        }
        if (mGeometryType == GeometryType.multiPoint) {
            if (mGeometry == null) {
                var list: ArrayList<Point> = arrayListOf();
                list.add(point);
                mGeometry = MultiPoint.fromLngLats(list)
            } else {
                val coordAll = coordAll(mGeometry as MultiPoint)
                coordAll.add(point)
                mGeometry = MultiPoint.fromLngLats(coordAll);
            }
        }
        if (mGeometryType == GeometryType.line||mGeometryType == GeometryType.slideLine) {
            if (mGeometry == null) {
                var list: ArrayList<Point> = arrayListOf();
                list.add(point);
                mGeometry = LineString.fromLngLats(list)
            } else {
                val coordAll = coordAll(mGeometry as LineString)
                coordAll.add(point)
                mGeometry = LineString.fromLngLats(coordAll);
            }
        }
        if (mGeometryType == GeometryType.multiLine) {
            if (mGeometry == null) {
                var list: ArrayList<Point> = arrayListOf();
                list.add(point);
                mGeometry = MultiLineString.fromLineString(LineString.fromLngLats(list))
            } else {
                val coordinates = (mGeometry as MultiLineString).coordinates()
                coordinates.get(coordinates.size - 1).add(point)
                mGeometry = MultiLineString.fromLngLats(coordinates);
            }
        }
        if (mGeometryType == GeometryType.polygon||mGeometryType == GeometryType.slidePolygon) {
            if (mGeometry == null) {
                var list: MutableList<Point> = arrayListOf();
                list.add(point);
                list.add(point);
                var list2list: MutableList<MutableList<Point>> = arrayListOf();
                list2list.add(list)
                mGeometry = Polygon.fromLngLats(list2list)
            } else {
                val coordinates = (mGeometry as Polygon).coordinates()
                val coordLast = coordinates.get(coordinates.size - 1)
                coordLast.add(coordLast.size - 1, point)
                mGeometry = Polygon.fromLngLats(coordinates);
            }
        }
        if (mGeometryType == GeometryType.multiPolygon) {
            if (mGeometry == null) {
                var list: MutableList<Point> = arrayListOf();
                list.add(point);
                list.add(point);
                var list2list: MutableList<MutableList<Point>> = arrayListOf();
                list2list.add(list)
                mGeometry = MultiPolygon.fromPolygon(Polygon.fromLngLats(list2list))
            } else {
                val coordinates = (mGeometry as MultiPolygon).coordinates()
                val coordLast = coordinates.get(coordinates.size - 1)
                val coordpLast = coordLast.get(coordLast.size - 1)
                coordpLast.add(coordpLast.size - 1, point)
                mGeometry = MultiPolygon.fromLngLats(coordinates);
            }
        }

        if(isSimulate){
            var geometry = Transformation.geoJsonStr2BoxGeometry(GsonFactory.getFactory().comMapBoxGson.toJson(mGeometry))
            mGeometry = Transformation.geoJsonStr2BoxGeometry(toJson)
            return geometry;
        }else{
            return mGeometry;
        }

    }

    override fun undo() {
        if(mGeometryHistoryUndo.size == 0) {
            return
        }
        mGeometryHistoryRedo.add(GsonFactory.getFactory().comMapBoxGson.toJson(mGeometry))
        mGeometry = Transformation.geoJsonStr2BoxGeometry(mGeometryHistoryUndo[mGeometryHistoryUndo.size-1])
        mGeometryHistoryUndo.removeAt(mGeometryHistoryUndo.size-1);

    }

    override fun redo() {
        if(mGeometryHistoryRedo.size == 0) {
            return
        }
        mGeometryHistoryUndo.add(GsonFactory.getFactory().comMapBoxGson.toJson(mGeometry))
        mGeometry =Transformation.geoJsonStr2BoxGeometry(mGeometryHistoryRedo[mGeometryHistoryRedo.size-1])
        mGeometryHistoryRedo.removeAt(mGeometryHistoryRedo.size-1);
    }

    override fun addPoints(points: MutableList<Point>) : Geometry? {
        return null;
    }

    override fun setGeometry(geometry: Geometry?) {
        if (geometry != null && getGeometryType(geometry) != mGeometryType) {
            return;
        }
        mGeometry = geometry;
        if(mGeometry == null){
            return
        }
        val toJson = GsonFactory.getFactory().comMapBoxGson.toJson(mGeometry);
        mGeometryHistoryUndo.add(toJson);
    }

    override fun getGeometry(): Geometry? {
        return mGeometry;
    }

    /**
     * 每次mGeometry有值后不可再设置
     */
    override fun setGeometryType(geometryType: GeometryType) {
        if (mGeometry != null) return
        mGeometryType = geometryType;
    }

    override fun getGeometryType(): GeometryType {
        return mGeometryType
    }

    override fun updatePoint(oldTagPoint: Point, newPoint: Point,isSimulate :Boolean):Geometry? {
        val toJson = GsonFactory.getFactory().comMapBoxGson.toJson(mGeometry)
        if(!isSimulate){
            mGeometryHistoryUndo.add(toJson);
        }
        var old = "[" + oldTagPoint.longitude() + "," + oldTagPoint.latitude() + "]"
        var new = "[" + newPoint.longitude() + "," + newPoint.latitude() + "]"
        val replace = toJson?.replace(old, new) ?: return null
        if (mGeometry is Point) {
            if(isSimulate){
                var geometry = Point.fromJson(replace);
                return geometry;
            }
            mGeometry = Point.fromJson(replace);
        }
        if (mGeometry is MultiPoint) {
            if(isSimulate){
                var geometry = MultiPoint.fromJson(replace);
                return geometry;
            }
            mGeometry = MultiPoint.fromJson(replace);
        }
        if (mGeometry is LineString) {
            if(isSimulate){
                var geometry = LineString.fromJson(replace);
                return geometry;
            }
            mGeometry = LineString.fromJson(replace)
        }
        if (mGeometry is MultiLineString) {
            if(isSimulate){
                var geometry = MultiLineString.fromJson(replace);
                return geometry;
            }
            mGeometry = MultiLineString.fromJson(replace)
        }
        if (mGeometry is Polygon) {
            if(isSimulate){
                var geometry = Polygon.fromJson(replace);
                return geometry;
            }
            mGeometry = Polygon.fromJson(replace)
        }
        if (mGeometry is MultiPolygon) {
            if(isSimulate){
                var geometry = MultiPolygon.fromJson(replace);
                return geometry;
            }
            mGeometry = MultiPolygon.fromJson(replace)
        }
        if(mGeometry == null){
            return null;
        }
        return null;
        // mGeometry = Transformation.jstGeometry2Geometry(Transformation.wkt2JtsGeometry(Transformation.boxGeometry2Wkt(mGeometry)).union(Transformation.wkt2JtsGeometry(Transformation.boxGeometry2Wkt(newPoint))))
    }

    /**
     * 添加一个点位于某两个点中间
     */
    fun updateMidPoint(onePoint: Point, twoPoint: Point, midpoint: Point,isSimulate:Boolean = false):Geometry? {
        val toJson1= GsonFactory.getFactory().comMapBoxGson.toJson(mGeometry);
        if(!isSimulate){
            mGeometryHistoryUndo.add(toJson1);
        }

        if (mGeometry is Point) {

        }
        if (mGeometry is MultiPoint) {

        }
        if (mGeometry is LineString) {
            val coordinates = (mGeometry as LineString).coordinates()
            var anchorIndex = -1;
            for (i in 0..coordinates.size-1){
                val get = coordinates.get(i)
                if (get == onePoint || get == twoPoint) {
                    anchorIndex = i;
                    break
                }
            }
            if (anchorIndex != -1) {
                coordinates.add(anchorIndex + 1, midpoint);
            }
        }
        if (mGeometry is MultiLineString) {
            val coordinates = (mGeometry as MultiLineString).coordinates()
            var anchorIndex = -1;
            for (coordinate in coordinates) {
                for (i in 0..coordinate.size-1) {
                    val get = coordinate.get(i)
                    if (get == onePoint || get == twoPoint) {
                        anchorIndex = i;
                        break
                    }
                }
                if (anchorIndex != -1) {
                    coordinate.add(anchorIndex + 1, midpoint);
                    break
                }
            }
        }
        if (mGeometry is Polygon) {
            val coordinates = (mGeometry as Polygon).coordinates()
            var anchorIndex = -1;
            for (coordinate in coordinates) {
                for (i in 0..coordinate.size-1) {
                    val get = coordinate.get(i)
                    if (get == onePoint || get == twoPoint) {
                        if (i == 0 && coordinate.size > 1) {
                            val get1 = coordinate.get(1)
                            if (get1 != onePoint && get1 != twoPoint) {
                                anchorIndex = coordinate.size - 2
                            }else{
                                anchorIndex = i;
                            }
                        } else {
                            anchorIndex = i;
                        }
                        break;
                    }
                }
                if (anchorIndex != -1) {
                    coordinate.add(anchorIndex + 1, midpoint);
                    break;
                }
            }
        }
        if (mGeometry is MultiPolygon) {
            val coordinates = (mGeometry as MultiPolygon).coordinates()
            var anchorIndex = -1;
            for (coordinate in coordinates) {
                for (mutableList in coordinate) {
                    for (i in 0..mutableList.size-1) {
                        val get = mutableList.get(i)
                        if (get == onePoint || get == twoPoint) {
                            if (i == 0 && mutableList.size > 1) {
                                val get1 = mutableList.get(1)
                                if (get1 != onePoint && get1 != twoPoint) {
                                    anchorIndex = mutableList.size - 2
                                }else{
                                    anchorIndex = i;
                                }
                            } else {
                                anchorIndex = i;
                            }
                            break;
                        }
                    }
                    if (anchorIndex != -1) {
                        mutableList.add(anchorIndex + 1, midpoint);
                        break;
                    }
                }
            }
        }
        if(isSimulate){
            var geometry = Transformation.geoJsonStr2BoxGeometry(GsonFactory.getFactory().comMapBoxGson.toJson(mGeometry))
            mGeometry = Transformation.geoJsonStr2BoxGeometry(toJson1)
            return geometry;
        }else{
            return mGeometry;
        }
    }

    /**
     * 返回图形的类型
     */
    private fun getGeometryType(@NotNull geometry: Geometry): GeometryType? {
        if (geometry is Point) {
            return GeometryType.point;
        }
        if (geometry is MultiPoint) {
            return GeometryType.multiPoint;
        }
        if (geometry is LineString) {
            return GeometryType.line;
        }
        if (geometry is MultiLineString) {
            return GeometryType.multiLine;
        }
        if (geometry is Polygon) {
            return GeometryType.polygon;
        }
        if (geometry is MultiPolygon) {
            return GeometryType.multiPolygon;
        }
        return null;
    }


}
package com.gradtech.mapframev10.core.select

import com.gradtech.mapframev10.core.util.FeatureSet
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.QueriedFeature
import com.mapbox.maps.ScreenCoordinate
import org.jetbrains.annotations.NotNull

/**
 * @ClassName ISelectionSet
 * @Description TODO
 * @Author: fs
 * @Date: 2022/9/8 15:57
 * @Version 2.0
 */
interface ISelectionSet {
    enum class SelectStatus {
        //选中状态 未选中 选中 反选
        selectedNull, addSelected, cancelSelected
    }

    /**
     * 调用select*等方法后的回调
     */
    interface IQueryFeaturesCallback{
        fun runCallBack(selectStatus: ISelectionSet.SelectStatus, selectSet: HashMap<String, FeatureSet>)
    }

    /**
     * 初始化设置查询规则
     */
    fun initQuery(customIds: LinkedHashMap<String, String>? = null, @NotNull selectSetting: SelectSetting);

    /***
     * 根据点查询
     */
    fun selectByPoint(@NotNull point: Point, selectedFeatures: MutableList<QueriedFeature>? = null, @NotNull callback: ISelectionSet.IQueryFeaturesCallback)

    /**
     * 根据屏幕坐标查询
     */
    fun selectByPoint(@NotNull screenCoordinate: ScreenCoordinate, selectedFeatures: MutableList<QueriedFeature>? = null, @NotNull callback: ISelectionSet.IQueryFeaturesCallback)

    /**
     * 根据一个闭合图形查询
     */
    fun selectByPolygonOrLine(geometry: Geometry, selectedFeatures: MutableList<QueriedFeature>?, callback: ISelectionSet.IQueryFeaturesCallback)

    /**
     * 清空渲染
     */
    fun clearRender()

    /**
     * 清空选择集
     */
    fun clearSelectSet()

    /**
     * 获取当前选中结果集
     */
    fun getSelectSet(): HashMap<String, FeatureSet>

}
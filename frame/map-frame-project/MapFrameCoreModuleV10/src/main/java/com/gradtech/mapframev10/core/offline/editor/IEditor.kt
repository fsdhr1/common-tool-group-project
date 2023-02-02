package com.gradtech.mapframev10.core.offline.editor

import android.content.Context
import com.gradtech.mapframev10.core.util.FeatureSet
import com.mapbox.geojson.Geometry

/**
 * @ClassName IEditor
 * @Description TODO
 * @Author: fs
 * @Date: 2022/12/20 14:50
 * @Version 2.0
 */
interface IEditor {
    /**
     * 合并
     * @param flag
     * @param selectSet
     */
    fun unionFeatures(context: Context, flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>)

    /**
     * 删除
     * @param flag
     * @param selectSet
     */
    fun delFeatures(flag: String, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>)

    /**
     * 分割
     * @param flag
     * @param selectSet
     * @param geometry
     */
    fun splitFeatures(flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>, geometry: Geometry)

    /**
     * 扣除
     * @param flag
     * @param selectSet
     * @param geometry
     */
    fun digFeatures(flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>, geometry: Geometry)

    /**
     * 新增
     * @param flag
     * @param geometry
     */
    fun drawFeature(flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, geometry: Geometry)

    /**
     * 新增批量
     * @param flag
     * @param selectSet
     */
    fun addFeatures(flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>)

    /**
     * 编辑
     * @param flag
     * @param selectSet
     * @param newGeometry
     */
    fun editFeature(flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>, newGeometry: Geometry)
}
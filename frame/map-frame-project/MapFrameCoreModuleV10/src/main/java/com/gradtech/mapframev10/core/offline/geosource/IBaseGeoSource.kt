package com.gradtech.mapframev10.core.offline.geosource

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import org.jetbrains.annotations.NotNull

/**
 * @ClassName IBaseGeoSource
 * @Description TODO 用于离线数据管理的geosource
 * @Author: fs
 * @Date: 2022/9/16 9:55
 * @Version 2.0
 */
interface IBaseGeoSource {


    fun setSourceUrl(@NotNull cachePath: String): IBaseGeoSource

    fun getGeoSource():GeoJsonSource?

    fun initDraw()

    fun query(): String?

    fun queryCache(): String?

    /**查询Feature根据条件
     * @param con
     * @return List<Feature>
    </Feature> */
    fun queryFeatures(@NotNull con: HashMap<String, Any>): List<Feature>

    /**查询Feature根据条件模糊
     * @param con
     * @return List<Feature>
    </Feature> */
    fun queryFeaturesLike(@NotNull con: Map<String, Any>): List<Feature>

    /**查询Feature根据主键
     * @param key
     * @param value
     * @return
     */
    fun queryFeature(@NotNull key: String, @NotNull value: String): Feature?

    /**插入有缓存null值判断
     * @param feature
     * @return
     */
    fun insert(@NotNull feature: Feature): Feature?

    /**插入无缓存null值判断
     * @param feature
     * @return
     */
    fun insertCache(@NotNull feature: Feature): Feature?

    /**批量插入有缓存null值判断
     * @param features
     * @return
     */
    fun inserts(@NotNull features: List<Feature>): FeatureCollection?

    /**批量插入无缓存null值判断 可异步执行
     * @param features
     * @return
     */
    fun insertsCacheUnSync(@NotNull features: List<Feature>, iGcCallBack: IGcCallBack?)

    /**删除有缓存null值判断
     * @param feature
     * @return
     */
    fun delete(@NotNull feature: Feature): Boolean

    /**
     * 删除无缓存null值判断
     * @param feature
     * @return
     */
    fun deleteCache(@NotNull feature: Feature): Boolean

    /**根据条件删除
     * @param con
     * @return List<Feature>
    </Feature> */
    fun deleteFeatures(@NotNull con: HashMap<String, Any>): Boolean

    /**
     * 删除所有有缓存null值判断
     * @return
     */
    fun deleteAll(): Boolean

    /**更新有缓存null值判断
     * @param feature
     * @return
     */
    fun update(feature: Feature?): Boolean

    /**更新无缓存null值判断
     * @param feature
     * @return
     */
    fun updateCache(feature: Feature?): Boolean

    interface IGcCallBack{
        /**
         * gc方法执行后回调，
         */
        fun onGcCallBack(features: List<Feature?>?)

        fun onError(e: Throwable)
    }
}
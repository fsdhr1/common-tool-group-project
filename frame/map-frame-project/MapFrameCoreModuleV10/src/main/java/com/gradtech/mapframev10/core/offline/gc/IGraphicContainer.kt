package com.gradtech.mapframev10.core.offline.gc

import com.gradtech.mapframev10.core.offline.geosource.IBaseGeoSource
import com.mapbox.geojson.Feature
import org.jetbrains.annotations.NotNull

/**
 * @ClassName IGraphicContainer
 * @Description TODO
 * @Author: fs
 * @Date: 2022/9/16 14:28
 * @Version 2.0
 */
interface IGraphicContainer {

    /**
     * 绑定到地图
     */
    fun bindMap()

    /**
     * 解除绑定
     */
    fun unbindMap()

    /**
     * 获取当前操作图层id
     * @return
     */
    fun getQueryLayerId(): String?

    /**
     * 获取当前显示层的layerid
     * @return
     */
    fun getRenderLayerId(): String?

    /**
     * 插入
     * @param feature
     * @param updateTargetSource 是否设置目标图层样式
     * @return
     */
    fun insert(@NotNull feature: Feature, updateTargetSource: Boolean = false): Feature?

    /**
     * 批量插入
     * @param features
     * @param updateTargetSource 是否设置目标图层样式
     * @param sync 是否同步 默认true
     * @return
     */
    fun inserts(@NotNull features: List<Feature>, iGcCallBack: IBaseGeoSource.IGcCallBack?,updateTargetSource: Boolean = false, sync: Boolean = true)

    /**查询Feature根据条件
     * @param con
     * @return List<Feature>
    </Feature> */
    fun queryFeatures(@NotNull con: HashMap<String, Any>): List<Feature>

    /**查询Feature根据条件模糊
     * @param con
     * @return List<Feature>
    </Feature> */
    fun queryFeaturesLike(@NotNull con: HashMap<String, Any>): List<Feature>

    /**查询Feature根据属性
     * @param key
     * @param value
     * @return
     */
    fun queryFeature(@NotNull key: String, @NotNull value: String): Feature?

    /**删除
     * @param feature
     * @return
     */
    fun delete(@NotNull feature: Feature): Boolean

    /**批量删除
     * @param features
     * @return
     */
    fun deletes(@NotNull features: List<Feature>): Boolean

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

    /**更新
     * @param feature
     * @return
     */
    fun update(@NotNull feature: Feature?): Boolean

    /**批量更新
     * @param features
     * @return
     */
    fun updates(@NotNull features: List<Feature>): Boolean
}
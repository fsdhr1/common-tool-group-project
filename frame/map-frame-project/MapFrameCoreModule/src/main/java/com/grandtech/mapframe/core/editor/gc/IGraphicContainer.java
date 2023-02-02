package com.grandtech.mapframe.core.editor.gc;

import androidx.annotation.NonNull;

import com.grandtech.mapframe.core.editor.IGcCallBack;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.mapbox.geojson.Feature;

import java.util.List;
import java.util.Map;

/**
 * @ClassName IGraphicContainer
 * @Description TODO
 * @Author: fs
 * @Date: 2021/4/20 9:34
 * @Version 2.0
 */
public interface IGraphicContainer {


    /**
     *获取当前操作图层id
     * @return
     */
    String getQueryLayerId();

    /**
     * 获取当前显示层的layerid
     * @return
     */
    String getRenderLayerId();

    /**
     *插入
     * @param feature
     * @param updateTargetSource 是否设置目标图层样式
     * @return
     */
    Feature insert(Feature feature, boolean updateTargetSource);

    /**
     *批量插入
     * @param features
     * @param updateTargetSource 是否设置目标图层样式
     * @param sync 是否同步
     * @return
     */
    void inserts(@NonNull List<Feature> features , boolean updateTargetSource, boolean sync, IGcCallBack iGcCallBack);

    /**查询Feature根据条件
     * @param con
     * @return List<Feature>
     */
    public List<Feature> queryFeatures(@NonNull Map<String, Object> con);

    /**查询Feature根据条件模糊
     * @param con
     * @return List<Feature>
     */
    public List<Feature> queryFeaturesLike(@NonNull Map<String, Object> con);

    /**查询Feature根据属性
     * @param key
     *  @param value
     * @return
     */
    public Feature queryFeature(@NonNull String key, @NonNull String value);

    /**删除
     * @param feature
     * @return
     */
    public Boolean delete(Feature feature);

    /**批量删除
     * @param features
     * @return
     */
    public Boolean deletes(List<Feature> features);

    /**根据条件删除
     * @param con
     * @return List<Feature>
     */
    public Boolean deleteFeatures(@NonNull Map<String, Object> con);

    /**
     * 删除所有有缓存null值判断
     * @return
     */
    public Boolean deleteAll();

    /**更新
     * @param feature
     * @return
     */
    public Boolean update(Feature feature);

    /**批量更新
     * @param features
     * @return
     */
    public Boolean updates(List<Feature> features);
}

package com.grandtech.mapframe.core.editor.gc;

import androidx.annotation.NonNull;

import com.grandtech.mapframe.core.editor.IGcCallBack;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.util.List;
import java.util.Map;

/**
 * @ClassName BaseGeoSource
 * @Description TODO 操作缓存内的数据
 * @Author: fs
 * @Date: 2021/4/20 9:16
 * @Version 2.0
 */

public interface IBaseGeoSource {


    public String query();

    public String queryCache();

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

    /**查询Feature根据主键
     * @param key
     *  @param value
     * @return
     */
    public Feature queryFeature(@NonNull String key, @NonNull String value);

    /**插入有缓存null值判断
     * @param feature
     * @return
     */
    public Feature insert(Feature feature);

    /**插入无缓存null值判断
     * @param feature
     * @return
     */
    public Feature insertCache(Feature feature);

    /**批量插入有缓存null值判断
     * @param features
     * @return
     */
    public FeatureCollection inserts(List<Feature> features);

    /**批量插入无缓存null值判断 可异步执行
     * @param features
     * @return
     */
    public void insertsCacheSync(List<Feature> features, IGcCallBack iGcCallBack);

    /**删除有缓存null值判断
     * @param feature
     * @return
     */
    public Boolean delete(Feature feature);

    /**
     * 删除无缓存null值判断
     * @param feature
     * @return
     */
    public Boolean deleteCache(Feature feature);

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

    /**更新有缓存null值判断
     * @param feature
     * @return
     */
    public Feature update(Feature feature);

    /**更新无缓存null值判断
     * @param feature
     * @return
     */
    public Feature updateCache(Feature feature);


}

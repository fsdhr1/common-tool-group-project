package com.grandtech.mapframe.core.editor;

import android.content.Context;

import com.grandtech.mapframe.core.util.FeatureSet;
import com.mapbox.geojson.Geometry;

import java.util.Map;

/**
 *
 * @author fs
 * @date 2021/4/16
 */

public interface IEditor {

    /**
     * 合并
     * @param flag
     * @param selectSet
     */
    public void unionFeatures(final Context context, final String flag, Boolean updateTargetSource , final IToolInterceptor iToolInterceptor, final Map<String, FeatureSet> selectSet);

    /**
     * 删除
     * @param flag
     * @param selectSet
     */
    public void delFeatures(final String flag, final IToolInterceptor iToolInterceptor, final Map<String, FeatureSet> selectSet);

    /**
     * 分割
     * @param flag
     * @param selectSet
     * @param geometry
     */
    public void splitFeatures(final String flag, Boolean updateTargetSource ,final IToolInterceptor iToolInterceptor, final Map<String, FeatureSet> selectSet, final Geometry geometry);

    /**
     * 扣除
     * @param flag
     * @param selectSet
     * @param geometry
     */
    public void digFeatures(final String flag, Boolean updateTargetSource ,final IToolInterceptor iToolInterceptor, final Map<String, FeatureSet> selectSet, final Geometry geometry);

    /**
     * 新增
     * @param flag
     * @param geometry
     */
    public void drawFeature(final String flag, Boolean updateTargetSource ,final IToolInterceptor iToolInterceptor, final Geometry geometry);

    /**
     * 新增批量
     * @param flag
     * @param selectSet
     */
    public void addFeatures(final String flag, Boolean updateTargetSource ,final IToolInterceptor iToolInterceptor, final Map<String, FeatureSet> selectSet);

    /**
     * 编辑
     * @param flag
     * @param selectSet
     * @param newGeometry
     */
    public void editFeature(final String flag, Boolean updateTargetSource ,final IToolInterceptor iToolInterceptor, final Map<String, FeatureSet> selectSet, final Geometry newGeometry);


}

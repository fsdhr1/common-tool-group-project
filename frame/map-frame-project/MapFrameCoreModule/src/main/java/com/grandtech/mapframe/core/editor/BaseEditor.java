package com.grandtech.mapframe.core.editor;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.grandtech.mapframe.core.editor.gc.GraphicSetting;
import com.grandtech.mapframe.core.editor.gc.IGraphicContainer;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.FeatureUtil;
import com.grandtech.mapframe.core.util.GsonFactory;
import com.grandtech.mapframe.core.util.Transformation;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图形处理 区别于GraphicContainerImpl 在GC之上增加回调，可以和选择集配套使用
 * 、实现一些常用的工具新增，绘制合并，扣除，分割等  添加基础属性-回调-执行工具操作（分割合并等）-回调-设置tagsouce属性
 * @author fs
 * @date 2021/4/16
 */
public final class BaseEditor implements IEditor {

    private IGraphicContainer mIGraphicContainer;

    public BaseEditor(IGraphicContainer iGraphicContainer) {
        this.mIGraphicContainer = iGraphicContainer;
    }

    public IGraphicContainer getGraphicContainer() {
        return mIGraphicContainer;
    }

    @Override
    public void drawFeature(String flag, Boolean updateTargetSource, IToolInterceptor iToolInterceptor, Geometry geometry) {
        Feature feature = Feature.fromGeometry(geometry);
        if(!featureValid(feature)){
            return;
        }
        assembleNewDraw(feature);
        if (iToolInterceptor != null) {
            if (iToolInterceptor.doHandle(feature, flag)) {
                return;
            }
        }
        mIGraphicContainer.insert(feature,updateTargetSource);
        if (iToolInterceptor != null) {
            iToolInterceptor.afterHandle(feature, flag);
        }
    }

    @Override
    public void addFeatures(String flag,Boolean updateTargetSource, IToolInterceptor iToolInterceptor, Map<String, FeatureSet> selectSet) {
        //获取结果集
        List<FeatureSet> setList = FeatureSet.convert2FeatureSets(selectSet);
        assembleCopy(setList);
        if (iToolInterceptor != null) {
            if (iToolInterceptor.doHandle(setList, flag)) {
                return;
            }
        }
        List<Feature> features = FeatureSet.convert2Features(setList);
        if(!featuresValid(features)){
            return;
        }
        //结果集存入临时图层
        mIGraphicContainer.inserts(features, updateTargetSource, false, new IGcCallBack() {
            @Override
            public void onGcCallBack(List<Feature> features) {
                if (iToolInterceptor != null) {
                    iToolInterceptor.afterHandle(setList, flag);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.fillInStackTrace();
            }
        });
    }

    @Override
    public void editFeature(String flag,Boolean updateTargetSource, IToolInterceptor interceptor, Map<String, FeatureSet> selectSet, Geometry newGeometry) {
        List<FeatureSet> featureSets = FeatureSet.convert2FeatureSets(selectSet);
        if (featureSets == null) {
            return;
        }
        FeatureSet featureSet = featureSets.get(0);
        Feature srcFeature = featureSet.features().get(0);
        Feature feature = Feature.fromGeometry(newGeometry, srcFeature.properties());
        if(!featureValid(feature)){
            return;
        }
        assembleEdit(feature, featureSet.getLayerId(), FeatureUtil.getFeatureId(featureSet.getLayerId(),srcFeature));
        if (interceptor != null) {
            if (interceptor.doHandle(feature, flag)) {
                return;
            }
        }
        mIGraphicContainer.insert(feature,updateTargetSource);
        if (interceptor != null) {
            interceptor.afterHandle(feature, flag);
        }
    }

    @Override
    public void unionFeatures(final Context context, String flag,Boolean updateTargetSource, IToolInterceptor interceptor, Map<String, FeatureSet> selectSet) {
        //获取结果集
        List<Geometry> geometries = FeatureSet.convert2Geometries(selectSet);
        float tolerance = 2.0f;//设定合并的容差，以后容差放到系统设置中
        //modify by clj 20190321 对于合并出来的多边形求外包突壳，作为合并后的多边形
        com.vividsolutions.jts.geom.Geometry unionGeo = Transformation.mergeGeometries(geometries, tolerance);
        if (unionGeo == null) {
            Toast.makeText(context, String.format("地块之间相离超过%s 米!", tolerance), Toast.LENGTH_LONG).show();
            return;
        }
        if (!unionGeo.isValid()) {
            Toast.makeText(context, "合并结果不合法,请重新选择!", Toast.LENGTH_LONG).show();
            return;
        }
        try{
            
            Transformation.compute84GeoJsonArea((Geometry) Transformation.jstGeometry2GeoJson(unionGeo));
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        Feature feature = Feature.fromGeometry((Geometry) Transformation.jstGeometry2GeoJson(unionGeo));
        if(!featureValid(feature)){
            return;
        }
        assembleUnion(FeatureSet.convert2FeatureSets(selectSet), feature);
        if (interceptor != null) {
            if (interceptor.doHandle(feature, flag)) {
                return;
            }
        }
        //结果集存入临时图层
        mIGraphicContainer.insert(feature,updateTargetSource);

        if (interceptor != null) {
            interceptor.afterHandle(feature, flag);
        }
    }
    @Override
    public void splitFeatures(String flag,Boolean updateTargetSource, IToolInterceptor interceptor, Map<String, FeatureSet> selectSet, Geometry geometry) {
        try{
            List<FeatureSet> featureSets = FeatureSet.convert2FeatureSets(selectSet);
            if (featureSets == null) {
                return;
            }
            FeatureSet featureSet;
            List<FeatureSet> _featureSets = new ArrayList<>();
            for (int i = 0; i < featureSets.size(); i++) {
                List<Feature> features = Transformation.divisionFeatures(featureSets.get(i).features(), geometry);
                featureSet = FeatureSet.fromFeatures(features);
                featureSet.setLayerId(featureSets.get(i).getLayerId()).setSourceId(featureSets.get(i).getSourceId());
                _featureSets.add(featureSet);
            }
            assembleSplit(_featureSets);
            if (interceptor != null) {
                if (interceptor.doHandle(_featureSets, flag)) {
                    return;
                }
            }
            mIGraphicContainer.inserts(FeatureSet.convert2Features(_featureSets), updateTargetSource, false, new IGcCallBack() {
                @Override
                public void onGcCallBack(List<Feature> features) {
                    if (interceptor != null) {
                        interceptor.afterHandle(_featureSets, flag);
                    }
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    e.fillInStackTrace();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void digFeatures(String flag,Boolean updateTargetSource, IToolInterceptor interceptor, Map<String, FeatureSet> selectSet, Geometry geometry) {
        List<FeatureSet> featureSets = FeatureSet.convert2FeatureSets(selectSet);
        if (featureSets == null||featureSets.size()==0) {
            return;
        }
        FeatureSet featureSet = featureSets.get(0);
        List<Feature> features = featureSet.features();
        if (features == null||features.size()==0) {
            return;
        }
        Feature srcFeature = features.get(0);
        Geometry _geometry = Transformation.diffFeature(srcFeature.geometry(), geometry);
        if(_geometry==null) {
            return;
        }

        Feature feature = Feature.fromGeometry(_geometry, srcFeature.properties());
        if(!featureValid(feature)){
            return;
        }
        assembleDig(feature, featureSet.getLayerId(), FeatureUtil.getFeatureId(featureSet.getLayerId(),srcFeature));
        if (interceptor != null) {
            if (interceptor.doHandle(feature, flag)) {
                return;
            }
        }
        mIGraphicContainer.insert(feature,updateTargetSource);
        if (interceptor != null) {
            interceptor.afterHandle(feature, flag);
        }
    }

    @Override
    public void delFeatures(String flag, IToolInterceptor interceptor, Map<String, FeatureSet> selectSet) {
        //获取结果集
        if (interceptor != null) {
            if (interceptor.doHandle(selectSet, flag)) {
                return;
            }
        }
       List<Feature> features = FeatureSet.convert2Features(selectSet);
        if(!featuresValid(features)){
            return;
        }
        //结果集存入临时图层
        mIGraphicContainer.deletes(features);
        //对外通知
        Map<String, FeatureSet> selectSetClone = selectSetClone(selectSet);
        if (interceptor != null) {
            interceptor.afterHandle(selectSetClone, flag);
        }
    }

    /**
     * 克隆选择集
     * @param selectSet
     * @return
     */
    private Map<String, FeatureSet> selectSetClone(Map<String, FeatureSet> selectSet) {
        if (selectSet == null) {
            return null;
        }
        Map<String, FeatureSet> clone = new HashMap<>(selectSet.size());
        FeatureSet featureSet;
        for (String name : selectSet.keySet()) {
            featureSet = selectSet.get(name);
            if (featureSet == null) {
                clone.put(name, null);
            } else {
                clone.put(name, featureSet.clone());
            }
        }
        return clone;
    }

    /**
     * 新绘制一块
     *
     * @param feature
     * @return
     */
    private Feature assembleNewDraw(Feature feature) {
        if (feature == null) {
            return feature;
        }
        feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, "");
        feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, "");
        Double[] areaAndLen = Transformation.compute84GeoJsonLenAndArea(feature);
        feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0]);
        feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1]);
        if(!feature.hasProperty(GraphicSetting.SOURCE_FIELD)){
            feature.addStringProperty(GraphicSetting.SOURCE_FIELD, "");
        }
        return feature;
    }

    /**
     * @param setList
     * @return
     */
    private void assembleCopy(List<FeatureSet> setList) {
        if (setList == null) {
            return;
        }
        FeatureSet featureSet;
        for (int i = 0; i < setList.size(); i++) {
            featureSet = setList.get(i);
            String fromLayerIdField = featureSet.getLayerId();
            Double area;
            List<Feature> features = featureSet.features();
            if (features == null) {
                return;
            }
            Feature feature;
            String id;
            for (int j = 0, len = features.size(); j < len; j++) {
                feature = features.get(j);
                Double[] areaAndLen = Transformation.compute84GeoJsonLenAndArea(feature);
                feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0]);
                feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1]);
                feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, fromLayerIdField);
                id = FeatureUtil.getFeatureId(featureSet.getLayerId(),feature);
                feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, id);
                feature.removeProperty(GraphicSetting.UID_FIELD);
            }
            featureSet.replaceAll(features);
        }
    }

    private void assembleUnion(List<FeatureSet> featureSets, Feature feature) {
        Double[] areaAndLen = Transformation.compute84GeoJsonLenAndArea(feature);
        feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0]);
        feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1]);

        FeatureSet featureSet;
        StringBuilder id = new StringBuilder();
        StringBuilder layerId = new StringBuilder();
        for (int i = 0; i < featureSets.size(); i++) {
            featureSet = featureSets.get(i);
            List<Feature> features = featureSet.features();
            for (int j = 0; j < features.size(); j++) {
                id.append(FeatureUtil.getFeatureId(featureSet.getLayerId(),features.get(j))).append(",");
                layerId.append(featureSet.getLayerId()).append(",");
            }
        }
        id.deleteCharAt(id.length() - 1);
        layerId.deleteCharAt(layerId.length() - 1);
        feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, layerId.toString());
        feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, id.toString());
        feature.removeProperty(GraphicSetting.UID_FIELD);
    }

    private void assembleSplit(List<FeatureSet> setList) {
        if (setList == null) {
            return;
        }
        FeatureSet featureSet;
        for (int i = 0; i < setList.size(); i++) {
            featureSet = setList.get(i);
            String fromLayerIdField = featureSet.getLayerId();
            Double area;
            List<Feature> features = featureSet.features();
            if (features == null) {
                return;
            }
            for (int j = 0, len = features.size(); j < len; j++) {
                Feature feature = features.get(j);
                Double[] areaAndLen = Transformation.compute84GeoJsonLenAndArea(feature);
                feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0]);
                feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1]);
                feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, fromLayerIdField);
                String id = FeatureUtil.getFeatureId(featureSet.getLayerId(),feature);
                feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, id);
                feature.removeProperty(GraphicSetting.UID_FIELD);
            }
            featureSet.replaceAll(features);
        }
    }

    private void assembleDig(Feature feature, String fromLyrId, String fromUid) {
        Double[] areaAndLen = Transformation.compute84GeoJsonLenAndArea(feature);
        feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0]);
        feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1]);
        feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, fromLyrId);
        feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, fromUid);
    }

    private void assembleEdit(Feature feature, String fromLyrId, String fromUid) {
        Double[] areaAndLen = Transformation.compute84GeoJsonLenAndArea(feature);
        feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0]);
        feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1]);
        feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, fromLyrId);
        feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, fromUid);
    }

    /**
     * 校验feature是否合法
     */
    private Boolean featuresValid(List<Feature> features){
        if(features==null){
            return false;
        }
        for(Feature feature : features){
            if(feature.geometry()==null){
                return false;
            }
        }
        return true;
    }
    private Boolean featureValid(Feature feature){
        if(feature==null){
            return false;
        }
        if(feature.geometry()==null){
            return false;
        }
        return true;
    }
}

package com.grandtech.mapframe.core.select;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.internal.LinkedTreeMap;
import com.grandtech.mapframe.core.editor.gc.BaseGeoSource;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.util.CollectUtil;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.FeatureUtil;
import com.grandtech.mapframe.core.util.GsonFactory;
import com.grandtech.mapframe.core.util.LayerUtil;
import com.grandtech.mapframe.core.util.ObjectUtil;
import com.grandtech.mapframe.core.util.Transformation;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.VectorSource;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import static com.grandtech.mapframe.core.util.FeatureUtil.firstId;
import static com.grandtech.mapframe.core.util.FeatureUtil.getIdField;
import static com.grandtech.mapframe.core.util.FeatureUtil.secondId;
import static com.grandtech.mapframe.core.util.FeatureUtil.thirdId;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;

/**
 * @ClassName SelectionSet
 * @Description TODO ???????????????
 * @Author: fs
 * @Date: 2021/4/6 16:16
 * @Version 2.0
 */
public class SelectionSet {


    private Map<String, FeatureSet> _selectSet;

    //key:????????????Id,val???????????????id
    private Map<String, String> _queryIdMap;

    /**
     * ???????????????????????????
     */
    private int _count = 0;

    private SelectSetting _selectSetting;

    /**
     * ????????????
     */
    private GMapView _gMapView;

    private List<ISelectSetChange> changeListening;


    public SelectionSet(GMapView gMapView) {
        this._gMapView = gMapView;
    }

    /**
     * @param customIds      ???????????????????????????????????????????????????uuid??????objectid??????????????????????????????????????????customId???
     *                      ??????????????????????????? uuid???objectid???customId
     * @param selectSetting
     * @return
     */
    public SelectionSet initQuery(@NonNull LinkedHashMap customIds, SelectSetting selectSetting) {
        FeatureUtil.setCustomId(customIds);
        return initQuery(selectSetting);
    }

    /**
     * ?????????????????????
     */
    public SelectionSet initQuery(SelectSetting selectSetting) {
        this._selectSetting = selectSetting;
        this._queryIdMap = new LinkedTreeMap<>();
        this._selectSet = new HashMap<>();
        List<String> layerIds = _selectSetting.getLayersAsList();
        if (layerIds == null || layerIds.size() == 0) {
            Toast.makeText(_gMapView.getContext(), "?????????????????????????????????", Toast.LENGTH_LONG).show();
            return this;
        }
        Source source;
        Layer layer;
        String queryLayerId;
        for (int i = 0; i < layerIds.size(); i++) {
            String id = layerIds.get(i);
            if (id.contains("??????") || id.contains("_fill") || id.contains("_render")
                    || id.contains("highLightLayerId")) {
                continue;
            }
            layer = _gMapView.getMapBoxMap().getStyle().getLayer(layerIds.get(i));
            String sourceId = _gMapView.getStyleLayerManager().getSourceIdByLayerId(layerIds.get(i));
            if (sourceId == null || "".equals(sourceId)) {
                continue;
            }
            source = _gMapView.getStyleLayerManager().getSource(_gMapView.getStyleLayerManager().getSourceIdByLayerId(layerIds.get(i)));
            if (layer == null || source == null) {
                continue;
            }
            queryLayerId = layer.getId();
            /*if ((layer instanceof LineLayer)) {
                queryLayerId = String.format("%s_query", layer.getId());
                Expression expression = _gMapView.getStyleLayerManager().getLayerFilter(layer);
                FillLayer queryLayer = new FillLayer(queryLayerId, source.getId())
                        .withSourceLayer(_gMapView.getStyleLayerManager().getSourceLayerId(layer))
                        .withProperties(PropertyFactory.fillOpacity(0f));
                if (expression != null) {
                    queryLayer.setFilter(expression);
                }

                queryLayer.setMaxZoom(layer.getMaxZoom());
                queryLayer.setMinZoom(layer.getMinZoom());

                _gMapView.getStyleLayerManager().addLayer(queryLayer);
            }*/
           /* if ((layer instanceof CircleLayer)) {
                queryLayerId = String.format("%s_query", layer.getId());
                Expression expression = _gMapView.getStyleLayerManager().getLayerFilter(layer);
                CircleLayer queryLayer = new CircleLayer(queryLayerId, source.getId())
                        .withSourceLayer(_gMapView.getStyleLayerManager().getSourceLayerId(layer))
                        .withProperties(PropertyFactory.circleOpacity(0f));
                if (expression != null) {
                    queryLayer.setFilter(expression);
                }

                queryLayer.setMaxZoom(layer.getMaxZoom());
                queryLayer.setMinZoom(layer.getMinZoom());

                _gMapView.getStyleLayerManager().addLayer(queryLayer);
            }*/
            if (!_queryIdMap.containsKey(layerIds.get(i))) {
                _queryIdMap.put(layerIds.get(i), queryLayerId);
            }
        }
        return this;
    }

    /**
     * ??????
     *
     * @param pointF
     * @param pointSelFeatures ?????????????????????????????????????????????????????????
     * @return
     */
    public SelectStatus selectByPoint(PointF pointF, Map<String, FeatureSet> pointSelFeatures) {
        SelectStatus selectStatus = SelectStatus.selectedNull;
        if (!notifyBeforeSelectSetChange()) {
            return selectStatus;
        }
        if (_queryIdMap == null) {
            return selectStatus;
        }
        String queryLayerId;
        long start = System.currentTimeMillis();
        for (String layerId : _queryIdMap.keySet()) {
            queryLayerId = _queryIdMap.get(layerId);
            boolean hasFeatures = false;
            Layer layer = _gMapView.getStyleLayerManager().getLayer(queryLayerId);
            if (layer.getVisibility().getValue().equals(NONE)) {
                continue;
            }
            List<Feature> features = _gMapView.getMapBoxMap().queryRenderedFeatures(pointF, queryLayerId);
            if (features == null || features.size() == 0) {
                if (layer instanceof CircleLayer || layer instanceof LineLayer) {
                    Projection projection = _gMapView.getMapBoxMap().getProjection();
                    LatLng latLng = projection.fromScreenLocation(pointF);
                    com.mapbox.geojson.Polygon polygon = TurfTransformation.circle(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()), 1000, TurfConstants.UNIT_METRES);
                    double[] bboxs = TurfMeasurement.bbox(polygon);
                    PointF pointF1 = projection.toScreenLocation(new LatLng(bboxs[1], bboxs[0]));
                    PointF pointF2 = projection.toScreenLocation(new LatLng(bboxs[3], bboxs[2]));
                    RectF rect = new RectF(new Double(pointF1.x).intValue(), new Double(pointF1.y).intValue(), new Double(pointF2.x).intValue(), new Double(pointF2.y).intValue());
                    features = _gMapView.getMapBoxMap().queryRenderedFeatures(rect, queryLayerId);
                }
                if (features == null || features.size() == 0) {
                    continue;
                }
            }
            //??????????????????????????????Feature
            Feature selectFeature = features.get(0);
            if (selectFeature != null) {
                hasFeatures = true;
            }
            String idField = getIdField(queryLayerId,selectFeature);//?????????feature id ??????
            if (!hasFeatures) {
                continue;//??????????????????????????????????????????????????????
            }
            if (idField == null) {
                Toast.makeText(_gMapView.getContext(), layerId + "???????????????????????????", Toast.LENGTH_LONG).show();
                continue;
            }
            selectStatus = SelectStatus.addSelected;
            if (pointSelFeatures != null) {
                String sourceId = _gMapView.getStyleLayerManager().getSourceIdByLayerId(layerId);
                List<Feature> featureList = new ArrayList<>();
                featureList.add(selectFeature);
                FeatureSet featureSet = FeatureSet.fromFeatures(featureList);
                featureSet.setSourceId(sourceId).setLayerId(layerId);
                pointSelFeatures.put(layerId, featureSet);
            }

            FeatureSet featureSet;
            if (_selectSetting.isMulti) {
                if (_selectSet.get(layerId) != null) {
                    featureSet = _selectSet.get(layerId);
                    List<Feature> selectedFeatures = featureSet.features();
                    int index = FeatureUtil.findFeature(selectedFeatures, selectFeature, idField);
                    if (index >= 0) {
                        //??????????????????
                        selectStatus = SelectStatus.cancelSelected;
                        selectedFeatures.remove(index);
                    } else {
                        /*if (getSelectCount() >= 20) {
                            Toast.makeText(_gMapView.getContext(), String.format("?????????????????????????????????%s !", 20), Toast.LENGTH_LONG).show();
                            return SelectStatus.selectedNull;
                        }*/
                        selectedFeatures.add(selectFeature);
                    }
                    featureSet = FeatureSet.fromFeatures(selectedFeatures);
                } else {
                    featureSet = FeatureSet.fromFeature(selectFeature);
                }

            }
            //????????????
            else {
                if (_selectSet.containsKey(layerId)) {
                    featureSet = _selectSet.get(layerId);
                    List<Feature> selectedFeatures = featureSet.features();
                    int index = FeatureUtil.findFeature(selectedFeatures, selectFeature, idField);
                    _selectSet.clear();
                    clearRender();
                    if (index >= 0) {
                        //??????????????????
                        selectStatus = SelectStatus.cancelSelected;
                        selectedFeatures.remove(index);
                        _selectSet.remove(layerId);
                        if (selectedFeatures.size() > 0) {
                            featureSet = FeatureSet.fromFeatures(selectedFeatures);
                        } else {
                            featureSet = null;
                        }
                    } else {
                        featureSet = FeatureSet.fromFeature(selectFeature);
                    }

                } else {
                    _selectSet.clear();
                    clearRender();
                    featureSet = FeatureSet.fromFeature(selectFeature);
                }
            }
            if (featureSet != null) {
                String sourceId = _gMapView.getStyleLayerManager().getSourceIdByLayerId(layerId);
                featureSet.setSourceId(sourceId).setLayerId(layerId);
                _selectSet.put(layerId, featureSet);
            }
            if (!notifySelectSetChangeIng(selectFeature, selectStatus)) {
                return selectStatus;
            }
            //???????????????????????????????????????
            if (_selectSetting.isPriority) {
                break;
            }

        }
        notifySelectSetChanged();
        render();
        return selectStatus;
    }


    public enum SelectStatus {
        selectedNull,
        addSelected,
        cancelSelected
    }


    private int getSelectCount() {
        int count = 0;
        if (_selectSet != null) {
            List<Feature> features;
            for (String name : _selectSet.keySet()) {
                features = _selectSet.get(name).features();
                count += features == null ? 0 : features.size();
            }
        }
        return count;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public SelectionSet render() {
        if (_selectSet == null || _selectSetting == null || !_selectSetting.isRender()) {
            return this;
        }
        List<Feature> features = new ArrayList<>();
        FeatureSet featureSet;
        clearRender();
        for (String key : _selectSet.keySet()) {
            featureSet = _selectSet.get(key);
            features.addAll(featureSet.features());
            renderLayer(featureSet);
        }
        //renderLayer.renderFeature(features);
        return this;
    }

    private void renderLayer(FeatureSet featureSet) {
        if (featureSet == null) {
            return;
        }
        List<Feature> features = featureSet.features();
        String layerId = featureSet.getLayerId();
        Layer layer = _gMapView.getMapBoxMap().getStyle().getLayer(layerId);
        if (ObjectUtil.isEmpty(features)) {
            Layer layerV = _gMapView.getMapBoxMap().getStyle().getLayer(layerId + "_render");
            clearRenderStyle(layerV);
            return;
        }
        setSelRenderStyle(layer, features);
    }

    public SelectionSet clearRender() {
        if (cacheTempLayer != null) {
            for (String key : cacheTempLayer.keySet()) {
                Layer layer = cacheTempLayer.get(key);
                clearRenderStyle(layer);
            }
        }
        return this;
    }

    /**
     * ??????????????????????????????
     *
     * @param layer
     * @param features
     */
    private Map<String, Layer> cacheTempLayer = new HashMap<>();
    private int renderColor = Color.YELLOW;

    private void setSelRenderStyle(Layer layer, List<Feature> features) {
        if (layer instanceof LineLayer) {
            LineLayer render;
            String key = layer.getId() + "_render";
            if (cacheTempLayer.containsKey(key)) {
                render = (LineLayer) cacheTempLayer.get(key);
            } else {
                render = LayerUtil.lineRenderLine((LineLayer) layer);
                _gMapView.getStyleLayerManager().addLayer(render);
                cacheTempLayer.put(key, render);

            }
            Expression expression;
            List<Expression> expressions = new ArrayList<>();
            for (int i = 0, len = features.size(); i < len; i++) {
                expression = createExpression(layer.getId(),features.get(i));
                expressions.add(expression);
                expressions.add(Expression.color(renderColor));
            }
            expressions.add(Expression.color(Color.TRANSPARENT));
            Expression[] array = CollectUtil.list2TArray(expressions, Expression.class);
            Expression renderE = Expression.switchCase(array);
            if (render instanceof LineLayer) {
                render.setProperties(PropertyFactory.lineColor(renderE));
            }
        }
        if (layer instanceof FillLayer) {
            LineLayer render;
            String key = layer.getId() + "_render";
            if (cacheTempLayer.containsKey(key)) {
                render = (LineLayer) cacheTempLayer.get(key);
            } else {
                render = LayerUtil.fillRenderLine((FillLayer) layer);
                _gMapView.getStyleLayerManager().addLayer(render);
                cacheTempLayer.put(key, render);
            }
            Expression expression;
            List<Expression> expressions = new ArrayList<>();
            for (int i = 0, len = features.size(); i < len; i++) {
                expression = createExpression(layer.getId(),features.get(i));
                expressions.add(expression);
                expressions.add(Expression.color(renderColor));
            }
            expressions.add(Expression.color(Color.TRANSPARENT));
            Expression[] array = CollectUtil.list2TArray(expressions, Expression.class);
            Expression renderE = Expression.switchCase(array);
            if (render instanceof LineLayer) {
                render.setProperties(PropertyFactory.lineColor(renderE));
            }
        }
        if (layer instanceof CircleLayer) {
            CircleLayer render;
            String key = layer.getId() + "_render";
            if (cacheTempLayer.containsKey(key)) {
                render = (CircleLayer) cacheTempLayer.get(key);
            } else {
                render = LayerUtil.circleRenderCircle((CircleLayer) layer);
                _gMapView.getStyleLayerManager().addLayer(render);
                cacheTempLayer.put(key, render);
            }
            Expression expression;
            List<Expression> expressions = new ArrayList<>();
            for (int i = 0, len = features.size(); i < len; i++) {
                expression = createExpression(layer.getId(),features.get(i));
                expressions.add(expression);
                expressions.add(Expression.color(renderColor));
            }
            expressions.add(Expression.color(Color.TRANSPARENT));
            Expression[] array = CollectUtil.list2TArray(expressions, Expression.class);
            Expression renderE = Expression.switchCase(array);
            if (render instanceof CircleLayer) {
                render.setProperties(PropertyFactory.circleColor(renderE));
            }
        }
    }

    private void clearRenderStyle(Layer layer) {
        if (layer == null) {
            return;
        }
        if (layer instanceof LineLayer) {
            layer.setProperties(PropertyFactory.lineColor(Color.TRANSPARENT));
        }
        if (layer instanceof FillLayer) {
            layer.setProperties(PropertyFactory.fillColor(Color.TRANSPARENT));
        }
        if (layer instanceof CircleLayer) {
            layer.setProperties(PropertyFactory.circleColor(Color.TRANSPARENT));
        }
    }

    private Expression createExpression(String layerid,Feature feature) {
        String customId = FeatureUtil.getCustomId(layerid);
        if (!ObjectUtil.isEmpty(customId)&&feature.hasProperty(customId)&&!ObjectUtil.isEmpty(feature.getStringProperty(customId).trim())) {
            String id = feature.getStringProperty(customId);
            return Expression.eq(Expression.get(customId), id);
        } else if (feature.hasProperty(firstId)&&!ObjectUtil.isEmpty(feature.getStringProperty(firstId).trim())) {
            return Expression.eq(Expression.get(firstId), feature.getStringProperty(firstId));
        } else if (feature.hasProperty(secondId)) {
            //modify by clj ?????????objectid???7?????????????????????????????????
            Number objId = feature.getNumberProperty(secondId);
            if (objId.longValue() <= 9999999) {
                return Expression.eq(Expression.get(secondId), objId);
            } else if (feature.hasProperty(thirdId)&&!ObjectUtil.isEmpty(feature.getStringProperty(thirdId).trim())) {
                return Expression.eq(Expression.get(thirdId), feature.getStringProperty(thirdId));
            }
        }
        return Expression.eq(Expression.get("1"), "1");
    }

    private boolean notifyBeforeSelectSetChange() {
        for (int i = 0; changeListening != null && i < changeListening.size(); i++) {
            if (!changeListening.get(i).beforeSelectSetChange()) {
                return false;
            }
        }
        return true;
    }

    private boolean notifySelectSetChangeIng(Feature feature, SelectStatus selectStatus) {
        for (int i = 0; changeListening != null && i < changeListening.size(); i++) {
            if (!changeListening.get(i).selectSetChangeIng(feature, selectStatus)) {
                return false;
            }
        }
        return true;
    }

    private void notifySelectSetChanged() {
        this._count = getSelectCount();
        for (int i = 0; changeListening != null && i < changeListening.size(); i++) {
            changeListening.get(i).selectSetChanged();
        }
    }

    public SelectionSet clearSelectSet() {
        if (_selectSet != null) {
            _selectSet.clear();
        }
        _count = 0;
        notifySelectSetChanged();
        return this;
    }


    public Map<String, FeatureSet> getSelectSet() {
        return _selectSet;
    }

    /**
     * ????????????????????????????????????
     * ??????false??????????????????
     */
    public interface ISelectSetChange {
        public boolean beforeSelectSetChange();

        public boolean selectSetChangeIng(Feature feature, SelectStatus selectStatus);

        public void selectSetChanged();
    }

    public void addChangeListening(ISelectSetChange iSelectSetChange) {
        if (changeListening == null) {
            changeListening = new ArrayList<>();
        }
        if (!changeListening.contains(iSelectSetChange)) {
            changeListening.add(iSelectSetChange);
        }
    }

    public void removeChangeListening(ISelectSetChange iSelectSetChange) {
        if (!changeListening.contains(iSelectSetChange)) {
            changeListening.remove(iSelectSetChange);
        }
    }


}

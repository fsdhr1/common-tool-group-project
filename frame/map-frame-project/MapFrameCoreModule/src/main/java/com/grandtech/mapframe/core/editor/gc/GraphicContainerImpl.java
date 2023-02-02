package com.grandtech.mapframe.core.editor.gc;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.grandtech.mapframe.core.editor.IGcCallBack;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.FileUtil;
import com.grandtech.mapframe.core.util.ObjectUtil;
import com.grandtech.mapframe.core.util.StringEngine;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GraphicContainer
 * @Description TODO 临时图层操作容器
 * @Author: fs
 * @Date: 2021/4/20 9:16
 * @Version 2.0
 */
public class GraphicContainerImpl implements IGraphicContainer {

    //临时图层source
    private final String TEMP_SOURCEID = "tempSource";
    private final String QUERY_LAYER = "_querylayer";
    private final String TEMP_LAYER = "_templayer";
    private final String TARG_LAYER = "_targlayer";

    private GMapView mGMapView;

    //private GraphicSetting mGraphicSetting;
    //标记图层
    private String mTargetLayerId;

    private GeometryType mGeometryType;

    /**
     * 点数据源
     */
    private BaseGeoSource mPointSource;
    /**
     * 线数据源
     */
    private BaseGeoSource mLineSource;
    /**
     * 面数据源
     */
    private BaseGeoSource mFillSource;
    /**
     * 点图层
     */
    private CircleLayer mPointLayer;
    /**
     * 线图层
     */
    private LineLayer mLineLayer;
    /**
     * 面图层
     */
    private FillLayer mFillLayer;

    /**
     * 当前临时图层数据源
     */
    private BaseGeoSource mCurrentSource;
    /**
     * 临时显示图层
     */
    private Layer mTempLayer;
    /**
     * 临时编辑图层
     */
    private Layer mTargetLayer;
    /**
     * 临时查询图层
     */
    private Layer mQueryLayer;

    /**
     * 目标编辑图层数据源id
     */
    private String mTargetSourceId;


    public GraphicContainerImpl(GMapView gMapView, GraphicSetting graphicSetting) {
        this.mGMapView = gMapView;
        initSetting(graphicSetting);
        bindMap();
    }

    private void initSetting(GraphicSetting graphicSetting) {
        if (graphicSetting == null) {
            return;
        }
        //  mGraphicSetting = graphicSetting;
        String cachePathParent = FileUtil.getParentPathByPath(graphicSetting.getCachePath());
        String cacheFileName = FileUtil.getFileNameByPath(graphicSetting.getCachePath());

        mTargetLayerId = graphicSetting.getTargetLayerId();
        mGeometryType = graphicSetting.getGeometryType();
        if (mGeometryType == GeometryType.point) {
            mPointSource = new BaseGeoSource(TEMP_SOURCEID + graphicSetting.getCachePath() + "_point");
            mPointSource.setSourceUrl(cachePathParent + File.separator + cacheFileName + "_point").initDraw();
            mCurrentSource = mPointSource;
        }
        if (mGeometryType == GeometryType.line) {
            mLineSource = new BaseGeoSource(TEMP_SOURCEID + graphicSetting.getCachePath() + "_line");
            mLineSource.setSourceUrl(cachePathParent + File.separator + cacheFileName + "_line").initDraw();
            mCurrentSource = mLineSource;
        }
        if (mGeometryType == GeometryType.polygon) {
            mFillSource = new BaseGeoSource(TEMP_SOURCEID + graphicSetting.getCachePath() + "_polygon");
            mFillSource.setSourceUrl(cachePathParent + File.separator + cacheFileName + "_polygon").initDraw();
            mCurrentSource = mFillSource;
        }
    }

    /**
     * 临时图层容器绑定到地图
     */
    @SuppressLint("LongLogTag")
    private void bindMap() {
        if (mTargetLayerId.equals("")) {
            Toast.makeText(mGMapView.getContext(), "请先初始化要编辑的图层", Toast.LENGTH_LONG).show();
            return;
        }
        mTargetSourceId = mGMapView.getStyleLayerManager().getSourceIdByLayerId(mTargetLayerId);
        Layer cloneLayer = mGMapView.getMapBoxMap().getStyle().getLayer(mTargetLayerId);
        if (cloneLayer != null) {
            //根据要编辑的图层克隆一个图层样式，设置为临时图层数据源
            mTargetLayer = cloneTargetLayer(mCurrentSource, cloneLayer);
            //设置过滤表达式
            Expression targetLayerExp = Expression.eq(Expression.get(GraphicSetting.SOURCE_FIELD), mTargetSourceId);
            mGMapView.getStyleLayerManager().setFilter2Layer(mTargetLayer, targetLayerExp);
        } else {
            Log.i("gmbgl-GraphicContainerImpl",String.format("未找到要编辑的图层[%s]", mTargetLayerId));
        }
        float minZoom = 13;
        float maxZoom = 18;
        if (mTargetLayer != null) {
            minZoom = mTargetLayer.getMinZoom();
            maxZoom = mTargetLayer.getMaxZoom();
        }

        //设置过滤条件
        Expression tempLayerExp = Expression.eq(Expression.get(GraphicSetting.SOURCE_FIELD), "");

        if (mGeometryType == GeometryType.polygon) {
            mFillLayer = new FillLayer(mFillSource.getId() + QUERY_LAYER, mFillSource.getId())
                    .withProperties(PropertyFactory.fillOutlineColor(Color.GREEN),
                            PropertyFactory.fillColor(Color.GREEN),
                            PropertyFactory.fillOpacity(0.0f));
            mFillLayer.setMinZoom(minZoom);
            mFillLayer.setMaxZoom(maxZoom);
            mQueryLayer = mFillLayer;

            mLineLayer = new LineLayer(mFillSource.getId() + TEMP_LAYER, mFillSource.getId())
                    .withProperties(PropertyFactory.lineColor(Color.GREEN),
                            PropertyFactory.lineWidth(3f),
                            PropertyFactory.lineOpacity(1f));
            mLineLayer.setMinZoom(minZoom);
            mLineLayer.setMaxZoom(maxZoom);

            mLineLayer.setFilter(tempLayerExp);
            mTempLayer = mLineLayer;
        }
        if (mGeometryType == GeometryType.line) {
            mLineLayer = new LineLayer(mLineSource.getId() + TEMP_LAYER, mLineSource.getId())
                    .withProperties(PropertyFactory.lineColor(Color.GREEN),
                            PropertyFactory.lineWidth(3f),
                            PropertyFactory.lineOpacity(1f));
            mLineLayer.setMinZoom(minZoom);
            mLineLayer.setMaxZoom(maxZoom);
            mLineLayer.setFilter(tempLayerExp);
            mTempLayer = mLineLayer;
            mQueryLayer = mLineLayer;
        }
        if (mGeometryType == GeometryType.point) {
            mPointLayer = new CircleLayer(mPointSource.getId() + TEMP_LAYER, mPointSource.getId())
                    .withProperties(
                            PropertyFactory.circleColor(Color.GREEN),
                            PropertyFactory.circleRadius(6f));
            mPointLayer.setMinZoom(minZoom);
            mPointLayer.setMaxZoom(maxZoom);
            mPointLayer.setFilter(tempLayerExp);
            mTempLayer = mPointLayer;
            mQueryLayer = mPointLayer;
        }

        mGMapView.getStyleLayerManager().addSource(mPointSource);
        mGMapView.getStyleLayerManager().addSource(mLineSource);
        mGMapView.getStyleLayerManager().addSource(mFillSource);
        boolean haslayer = mGMapView.getStyleLayerManager().hasLayer(mTargetLayerId);
        if(haslayer){
            mGMapView.getStyleLayerManager().addLayerAbove(mTempLayer, mTargetLayerId);
            if(mTargetLayer != null){
                mGMapView.getStyleLayerManager().addLayerAbove(mTargetLayer, mTargetLayerId);
            }
            mGMapView.getStyleLayerManager().addLayerAbove(mQueryLayer, mTargetLayerId);
        }else {
            mGMapView.getStyleLayerManager().addLayer(mTempLayer);
            if(mTargetLayer != null){
                mGMapView.getStyleLayerManager().addLayer(mTargetLayer);
            }
            mGMapView.getStyleLayerManager().addLayer(mQueryLayer);
        }

    }




    public void unBindMap() {

    }

    /**
     * 克隆目标图层
     *
     * @param source
     * @param cloneLayer
     * @return
     */
    private Layer cloneTargetLayer(BaseGeoSource source, Layer cloneLayer) {
        if (cloneLayer instanceof FillLayer) {
            FillLayer fLyr = new FillLayer(source.getId() + TARG_LAYER, source.getId());
            String fillColor = mGMapView.getStyleLayerManager().getStylePropertyItem(cloneLayer.getId(), "fill-color");
            //Log.i("gmap",fillColor);
            fillColor = "#eb23e4";
            copyProperties(cloneLayer, fLyr, fillColor);
            return fLyr;
        }
        if (cloneLayer instanceof LineLayer) {
            LineLayer lLyr = new LineLayer(source.getId() + TARG_LAYER, source.getId());
            copyProperties(cloneLayer, lLyr, "");
            return lLyr;
        }
        if (cloneLayer instanceof CircleLayer) {
            CircleLayer pLyr = new CircleLayer(source.getId() + TARG_LAYER, source.getId());
            copyProperties(cloneLayer, pLyr, "");
            return pLyr;
        }
        return null;
    }

    private void copyProperties(Layer layer1, Layer layer2, String fill_color) {
        if (layer1 instanceof FillLayer && (layer2 instanceof FillLayer)) {
            FillLayer fillLayer1 = (FillLayer) layer1;
            FillLayer fillLayer2 = (FillLayer) layer2;

            fillLayer2.withProperties(
                    PropertyFactory.fillAntialias(fillLayer1.getFillAntialias().value),
                    PropertyFactory.fillOpacity(fillLayer1.getFillOpacity().value),
                    PropertyFactory.fillColor(fillLayer1.getFillColor().getExpression()),
                    PropertyFactory.fillOutlineColor(fillLayer1.getFillOutlineColor().value),
                    PropertyFactory.fillPattern(fillLayer1.getFillPattern().value),
                    PropertyFactory.fillTranslate(fillLayer1.getFillTranslate().value),
                    PropertyFactory.fillTranslateAnchor(fillLayer1.getFillTranslateAnchor().value)
            );
            //  fillLayer2.withProperties()
            if (fill_color != null && !fill_color.equals("")) {
                fillLayer2.setProperties(PropertyFactory.fillColor(fill_color));
            }

            fillLayer2.setMaxZoom(fillLayer1.getMaxZoom());
            fillLayer2.setMinZoom(fillLayer1.getMinZoom());
        }
        if (layer1 instanceof LineLayer && layer2 instanceof LineLayer) {
            LineLayer fillLayer1 = (LineLayer) layer1;
            LineLayer fillLayer2 = (LineLayer) layer2;

            fillLayer2.setProperties(
                    PropertyFactory.lineBlur(fillLayer1.getLineBlur().value),
                    PropertyFactory.lineCap(fillLayer1.getLineCap().value),
                    PropertyFactory.lineColor(fillLayer1.getLineColor().value),
                    PropertyFactory.lineDasharray(fillLayer1.getLineDasharray().value),
                    PropertyFactory.lineGapWidth(fillLayer1.getLineGapWidth().value),
                    PropertyFactory.lineJoin(fillLayer1.getLineJoin().value),
                    PropertyFactory.lineMiterLimit(fillLayer1.getLineMiterLimit().value),
                    PropertyFactory.lineOffset(fillLayer1.getLineOffset().value),
                    PropertyFactory.lineOpacity(fillLayer1.getLineOpacity().value),
                    PropertyFactory.linePattern(fillLayer1.getLinePattern().value),
                    PropertyFactory.lineWidth(fillLayer1.getLineWidth().value),
                    PropertyFactory.lineRoundLimit(fillLayer1.getLineRoundLimit().value)
            );
            fillLayer2.setMaxZoom(fillLayer1.getMaxZoom());
            fillLayer2.setMinZoom(fillLayer1.getMinZoom());

        }
        if (layer1 instanceof SymbolLayer && layer2 instanceof SymbolLayer) {
            SymbolLayer fillLayer1 = (SymbolLayer) layer1;
            SymbolLayer fillLayer2 = (SymbolLayer) layer2;

            fillLayer2.setProperties(
                    PropertyFactory.textColor(Color.WHITE),
                    PropertyFactory.textSize(fillLayer1.getTextSize().value),
                    PropertyFactory.iconAllowOverlap(fillLayer1.getIconAllowOverlap().value),
                    PropertyFactory.iconAnchor(fillLayer1.getIconAnchor().value),
                    PropertyFactory.iconHaloBlur(fillLayer1.getIconHaloBlur().value),
                    PropertyFactory.iconHaloColor(fillLayer1.getIconHaloColor().value),
                    PropertyFactory.iconHaloWidth(fillLayer1.getIconHaloWidth().value),
                    PropertyFactory.iconIgnorePlacement(fillLayer1.getIconIgnorePlacement().value),
                    PropertyFactory.iconImage(fillLayer1.getIconImage().value),
                    PropertyFactory.iconKeepUpright(fillLayer1.getIconKeepUpright().value),
                    PropertyFactory.iconOffset(fillLayer1.getIconOffset().value),
                    PropertyFactory.iconOptional(fillLayer1.getIconOptional().value),
                    PropertyFactory.iconPadding(fillLayer1.getIconPadding().value),
                    PropertyFactory.textFont(fillLayer1.getTextFont().value),
                    PropertyFactory.textOpacity(fillLayer1.getTextOpacity().value),
                    PropertyFactory.textHaloWidth(fillLayer1.getTextHaloWidth().value),
                    PropertyFactory.textHaloColor(fillLayer1.getTextHaloColor().value),
                    PropertyFactory.textHaloBlur(fillLayer1.getIconHaloBlur().value),
                    PropertyFactory.textLineHeight(fillLayer1.getTextLineHeight().value),
                    PropertyFactory.textAnchor(fillLayer1.getIconAnchor().value),
                    PropertyFactory.textOffset(fillLayer1.getTextOffset().value),
                    PropertyFactory.textField(fillLayer1.getTextField().value)
            );
            fillLayer2.setMaxZoom(fillLayer1.getMaxZoom());
            fillLayer2.setMinZoom(fillLayer1.getMinZoom());
        }
    }

    @Override
    public String getQueryLayerId() {
        return mQueryLayer.getId();
    }

    @Override
    public String getRenderLayerId() {
        if (mGeometryType == GeometryType.polygon||mGeometryType == GeometryType.line) {
            if(mLineLayer == null) {
                return null;
            }
            return mLineLayer.getId();
        }
        if (mGeometryType == GeometryType.point) {
            if(mPointLayer == null) {
                return null;
            }
            return mPointLayer.getId();
        }
        return null;
    }


    @Override
    public Feature insert(Feature feature, boolean updateTargetSource) {
        if (updateTargetSource) {
            feature.addStringProperty(GraphicSetting.SOURCE_FIELD, mTargetSourceId);
        }
        //判断是否有需要进行关联删除的临时图层要素
        if (!feature.hasProperty(GraphicSetting.FROM_LAYER_ID_FIELD) || !feature.hasProperty(GraphicSetting.FROM_UID_FIELD)) {
            return mCurrentSource.insert(feature);
        }
        String fromLayerId = feature.getStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD);
        String fromUid = feature.getStringProperty(GraphicSetting.FROM_UID_FIELD);
        if (!fromLayerId.equals("") && !fromUid.equals("")) {
            String[] lyrIds = fromLayerId.split(",");
            String[] uids = fromUid.split(",");
            for (int i = 0; i < lyrIds.length; i++) {
                String lyrId = lyrIds[i];
                if (!lyrId.equals(getQueryLayerId())) {
                    continue;
                }
                //如果是来源临时图层的要素，则删除
                String uid = uids[i];
                Feature fea = mCurrentSource.queryFeature(GraphicSetting.UID_FIELD, uid);
                if (fea == null) {
                    continue;
                }
                mCurrentSource.delete(fea);
            }
        }
        feature = assemble(feature);
        return mCurrentSource.insert(feature);
    }

    @Override
    public void inserts(@NonNull List<Feature> features, boolean updateTargetSource, boolean sync, IGcCallBack iGcCallBack) {
        try {
            for (Feature feature : features) {
                if (updateTargetSource) {
                    feature.addStringProperty(GraphicSetting.SOURCE_FIELD, mTargetSourceId);
                }
                feature = assemble(feature);
                if (feature == null || mCurrentSource == null) {
                    continue;
                }
                //判断是否有需要进行关联删除的临时图层要素
                if (!feature.hasProperty(GraphicSetting.FROM_LAYER_ID_FIELD) || !feature.hasProperty(GraphicSetting.FROM_UID_FIELD)) {
                    if (sync) {
                        mCurrentSource.insert(feature);
                    }
                    continue;
                }
                String fromLayerId = feature.getStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD);
                String fromUid = feature.getStringProperty(GraphicSetting.FROM_UID_FIELD);
                if (!fromLayerId.equals("") && !fromUid.equals("")) {
                    String[] lyrIds = fromLayerId.split(",");
                    String[] uids = fromUid.split(",");
                    for (int i = 0; i < lyrIds.length; i++) {
                        String lyrId = lyrIds[i];
                        if (!lyrId.equals(getQueryLayerId())) {
                            continue;
                        }
                        //如果是来源临时图层的要素，则删除
                        String uid = uids[i];
                        Feature fea = mCurrentSource.queryFeature(GraphicSetting.UID_FIELD, uid);
                        if (fea == null) {
                            continue;
                        }
                        mCurrentSource.delete(fea);
                    }
                }
                if (sync) {
                    mCurrentSource.insert(feature);
                }
            }
            if (!sync) {
                mCurrentSource.insertsCacheSync(features, iGcCallBack);
            } else {
                iGcCallBack.onGcCallBack(features);
            }
        } catch (Exception e) {
            iGcCallBack.onError(e);
        }

    }

    @Override
    public List<Feature> queryFeatures(Map<String, Object> con) {
        if (mCurrentSource == null) {
            return null;
        }
        return mCurrentSource.queryFeatures(con);
    }

    @Override
    public List<Feature> queryFeaturesLike(Map<String, Object> con) {
        if (mCurrentSource == null) {
            return null;
        }
        return mCurrentSource.queryFeaturesLike(con);
    }

    @Override
    public Feature queryFeature(String key, String value) {
        if (mCurrentSource == null) {
            return null;
        }
        return mCurrentSource.queryFeature(key, value);
    }

    @Override
    public Boolean delete(Feature feature) {
        if (mCurrentSource == null) {
            return null;
        }
        return mCurrentSource.delete(feature);
    }

    @Override
    public Boolean deletes(List<Feature> features) {
        boolean des = false;
        if (mCurrentSource == null) {
            return des;
        }
        for (Feature feature : features) {
            des = mCurrentSource.delete(feature);
        }
        return des;
    }

    @Override
    public Boolean deleteFeatures(@NonNull Map<String, Object> con) {
        if (mCurrentSource == null) {
            return false;
        }
        return mCurrentSource.deleteFeatures(con);
    }

    @Override
    public Boolean deleteAll() {
        if (mCurrentSource == null) {
            return false;
        }
        return mCurrentSource.deleteAll();
    }

    @Override
    public Boolean update(Feature feature) {
        if (mCurrentSource == null) {
            return false;
        }
        mCurrentSource.update(feature);
        return true;
    }

    @Override
    public Boolean updates(List<Feature> features) {
        if (mCurrentSource == null) {
            return false;
        }
        for (Feature feature : features) {
            mCurrentSource.update(feature);
        }
        return true;
    }


    /**
     * 补充临时图层必须有的字段属性，uuid sourceField  tag
     *
     * @param feature
     * @return
     */
    private Feature assemble(Feature feature) {
        String uid = StringEngine.get32UUID();
        if (!feature.hasProperty(GraphicSetting.UID_FIELD)) {
            feature.addStringProperty(GraphicSetting.UID_FIELD, uid);
        } else {
            uid = feature.getStringProperty(GraphicSetting.UID_FIELD);
        }

        if (!feature.hasProperty(GraphicSetting.SOURCE_FIELD)) {
            feature.addStringProperty(GraphicSetting.SOURCE_FIELD, "");
        }
        if (!feature.hasProperty(GraphicSetting.SOURCE_TAG)) {
            feature.addStringProperty(GraphicSetting.SOURCE_TAG, GraphicSetting.SOURCE_TAG);
        }
        return Feature.fromGeometry(feature.geometry(), feature.properties(), uid);
    }


}

package com.gradtech.mapframev10.core.select

import android.graphics.Color
import android.graphics.PointF
import android.widget.Toast
import com.gradtech.mapframev10.core.maps.GMapView
import com.gradtech.mapframev10.core.util.FeatureSet
import com.gradtech.mapframev10.core.util.FeatureUtil
import com.gradtech.mapframev10.core.util.LayerUtil
import com.gradtech.mapframev10.core.util.Transformation
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.*
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.color
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.literal
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.turf.TurfJoins
import com.mapbox.turf.TurfMeasurement
import org.jetbrains.annotations.NotNull

/**
 * @ClassName SelectionAnnotations
 * @Description TODO
 * @Author: fs
 * @Date: 2022/9/8 15:35
 * @Version 2.0
 */
class SelectionSet constructor(mapview: GMapView) : ISelectionSet {

    private var mSelectSetting: SelectSetting? = null

    private var mCustomIds: LinkedHashMap<String, String>? = null

    private val mGMapView: GMapView = mapview

    private var _selectSet: HashMap<String, FeatureSet> = hashMapOf()

    private var cacheTempLayer: HashMap<String, Layer> = hashMapOf()

    private val FLAG = "_selectionrender";
    private val renderColor = Color.YELLOW



    override fun initQuery(customIds: LinkedHashMap<String, String>?, @NotNull selectSetting: SelectSetting) {
        this.mSelectSetting = selectSetting
        this.mCustomIds = customIds
        FeatureUtil.setCustomId(mCustomIds);
    }

    fun getSelectSetting():SelectSetting?{
        return mSelectSetting;
    }

    override fun selectByPoint(point: Point, selectedFeatures: MutableList<QueriedFeature>?, @NotNull callback: ISelectionSet.IQueryFeaturesCallback) {
        val pixelForCoordinate = mGMapView.getMapboxMap().pixelForCoordinate(point)
        selectByPoint(pixelForCoordinate, selectedFeatures, callback)
    }

    override fun selectByPoint(screenCoordinate: ScreenCoordinate, selectedFeatures: MutableList<QueriedFeature>?, callback: ISelectionSet.IQueryFeaturesCallback) {
        if (mSelectSetting == null) return;
        val layersAsList = mSelectSetting!!.getLayersAsList()
        var isNoFillLayer = false
        layersAsList?.forEach() { it ->
            val layer = mGMapView.getMapboxMap().getStyle()?.getLayer(it)
            if (layer is LineLayer || layer is CircleLayer) {
                isNoFillLayer = true
            }
        }
        var renderedQueryGeometry = RenderedQueryGeometry(screenCoordinate)
        if (isNoFillLayer) {
            renderedQueryGeometry = RenderedQueryGeometry(screenBoxFromPixel(screenCoordinate))
        }
        select(renderedQueryGeometry, selectedFeatures, callback)
    }

    override fun selectByPolygonOrLine(geometry: Geometry, selectedFeatures: MutableList<QueriedFeature>?, callback: ISelectionSet.IQueryFeaturesCallback) {
        if (geometry is Point || geometry is MultiPoint) {
            return
        }
        val bboxs = TurfMeasurement.bbox(geometry)
        val pointF1: ScreenCoordinate = mGMapView.getMapboxMap().pixelForCoordinate(Point.fromLngLat(bboxs[0], bboxs[1]))
        val pointF2: ScreenCoordinate = mGMapView.getMapboxMap().pixelForCoordinate(Point.fromLngLat(bboxs[2], bboxs[3]))
        var renderedQueryGeometry = RenderedQueryGeometry(ScreenBox(pointF1, pointF2))
        if (geometry is Polygon) {
            select(renderedQueryGeometry, selectedFeatures, callback, geometry)
        }
        if (geometry is LineString) {
            geometry.coordinates().add(geometry.coordinates().get(0))
            if (geometry.coordinates().size < 4) {
                return
            }
            select(renderedQueryGeometry, selectedFeatures, callback, Polygon.fromLngLats(mutableListOf(geometry.coordinates())))
        }

    }

    private fun select(renderedQueryGeometry: RenderedQueryGeometry, selectedFeatures: MutableList<QueriedFeature>?, callback: ISelectionSet.IQueryFeaturesCallback, polygon: Polygon? = null) {
        var selectStatus = ISelectionSet.SelectStatus.selectedNull
        if (mSelectSetting == null) return;
        val layersAsList = mSelectSetting!!.getLayersAsList()
        var layerSourceMap = hashMapOf<String, String>()
        var seletLayer = arrayListOf<String>()
        layersAsList?.forEach() { it ->
            val layer = mGMapView.getMapboxMap().getStyle()?.getLayer(it)
            if (layer?.visibility != Visibility.NONE) {
                seletLayer.add(it)
                val sourceId = mGMapView.getStyleLayerManager().getSourceIdByLayerId(it)
                if (sourceId != null) {
                    layerSourceMap.put(sourceId, it)
                }
            }
        }
        mGMapView.getMapboxMap().queryRenderedFeatures(renderedQueryGeometry, RenderedQueryOptions(seletLayer, null)) { expected: Expected<String, MutableList<QueriedFeature>> ->
            val value = expected.value ?: return@queryRenderedFeatures
            if (value.size > 0) {
                selectStatus = ISelectionSet.SelectStatus.addSelected
            }
            var fristLayerId = ""
            if (mSelectSetting!!.isPriority()) {
                for (s in seletLayer) {
                    for (queriedFeature in value) {
                        val source = queriedFeature.source
                        val layerId: String = layerSourceMap.get(source)!!
                        if (s.equals(layerId)) {
                            fristLayerId = s;
                            break
                        }
                    }
                    if (!fristLayerId.equals("")) {
                        break
                    }
                }
            }
            var queriedFeatures = mutableListOf<QueriedFeature>()
            queriedFeatures.addAll(value)
            //先过滤反选
            for (queriedFeature in value) {
                val source = queriedFeature.source
                val layerId: String = layerSourceMap.get(source)!!
                var featureSet = _selectSet.get(layerId)
                val idField = FeatureUtil.getIdField(layerId, queriedFeature.feature)
                if (idField == null) {
                    Toast.makeText(mGMapView.context, layerId + "要素缺少唯一值属性", Toast.LENGTH_LONG).show()
                    continue
                }
                val index = FeatureUtil.findFeature(featureSet?.features(), queriedFeature.feature, idField)
                if (index >= 0) {
                    if(mSelectSetting!!.canCancelSelected()){
                        //记录反选标识
                        selectStatus = ISelectionSet.SelectStatus.cancelSelected
                        featureSet?.features()?.removeAt(index)
                        if (featureSet?.features()?.size == 0) {
                            _selectSet.remove(layerId)
                        }
                    }else{
                        featureSet?.features()?.set(index,queriedFeature.feature)
                    }
                    queriedFeatures.remove(queriedFeature)

                    continue
                }
            }
            if (polygon != null) {
                queriedFeatures = Transformation.getCovers(polygon, queriedFeatures)
            }
            for (queriedFeature in queriedFeatures) {
                val source = queriedFeature.source
                val layerId: String = layerSourceMap.get(source)!!
                //设置了优先选择那么不是
                if (!fristLayerId.equals("") && !fristLayerId.equals(layerId)) {
                    continue
                }
                val idField = FeatureUtil.getIdField(layerId, queriedFeature.feature)
                if (idField == null) {
                    Toast.makeText(mGMapView.context, layerId + "要素缺少唯一值属性", Toast.LENGTH_LONG).show()
                    continue
                }
                if (selectedFeatures != null) {
                    selectedFeatures.add(queriedFeature)
                }
                var featureSet = _selectSet.get(layerId)
                if (featureSet == null) {
                    featureSet = FeatureSet.fromFeature(queriedFeature.feature)
                    featureSet!!.setSourceId(source).setLayerId(layerId)
                    _selectSet.put(layerId, featureSet);
                } else {
                    if (mSelectSetting!!.isMulti()) {
                        featureSet.append(queriedFeature.feature)
                    } else {
                        featureSet = FeatureSet.fromFeature(queriedFeature.feature)
                        featureSet!!.setSourceId(source).setLayerId(layerId)
                        _selectSet.put(layerId, featureSet);
                    }
                }
            }
            render()
            callback.runCallBack(selectStatus, _selectSet)
        }
    }

    override fun clearRender() {
        clearAllRenderStyle()
    }

    override fun clearSelectSet() {
        _selectSet.clear()
    }

    override fun getSelectSet(): HashMap<String, FeatureSet> {
        return _selectSet
    }

    private fun render() {
        clearAllRenderStyle()
        val keys = _selectSet.keys
        for (layerid in keys) {
            setSelRenderStyle(layerid, _selectSet.get(layerid)!!)
        }
    }

    private fun setSelRenderStyle(layerid: String, get: FeatureSet) {
        val layer = mGMapView.getMapboxMap().getStyle()?.getLayer(layerid)
        if (layer is FillLayer) {
            if (!cacheTempLayer.containsKey(layer.layerId + FLAG)) {
                val fillRenderLine = LayerUtil.fillRenderLine(layer)
                mGMapView.getMapboxMap().getStyle()?.addLayer(fillRenderLine)
                cacheTempLayer.put(layer.layerId + FLAG, fillRenderLine)
            }
            val layerRender = cacheTempLayer.get(layer.layerId + FLAG)
            if (get.features() == null) return
            (layerRender as LineLayer).lineColor(createRenderExpression(layerid, get.features()!!))
        }
        if (layer is LineLayer) {
            if (!cacheTempLayer.containsKey(layer.layerId + FLAG)) {
                val lineRenderLine = LayerUtil.lineRenderLine(layer)
                mGMapView.getMapboxMap().getStyle()?.addLayer(lineRenderLine)
                cacheTempLayer.put(layer.layerId + FLAG, lineRenderLine)
            }
            val layerRender = cacheTempLayer.get(layer.layerId + FLAG)
            if (get.features() == null) return
            (layerRender as LineLayer).lineColor(createRenderExpression(layerid, get.features()!!))
        }
        if (layer is CircleLayer) {
            if (!cacheTempLayer.containsKey(layer.layerId + FLAG)) {
                val circleRenderCircle = LayerUtil.circleRenderCircle(layer)
                mGMapView.getMapboxMap().getStyle()?.addLayer(circleRenderCircle)
                cacheTempLayer.put(layer.layerId + FLAG, circleRenderCircle)
            }
            val layerRender = cacheTempLayer.get(layer.layerId + FLAG)
            if (get.features() == null) return
            (layerRender as LineLayer).lineColor(createRenderExpression(layerid, get.features()!!))
        }
    }

    private fun createRenderExpression(layerid: String, features: List<Feature>): Expression {
        var expressions = arrayListOf<Expression>()
        for (feature in features) {
            val idField = FeatureUtil.getIdField(layerid, feature) ?: continue;
            if (idField.equals(FeatureUtil.secondId)) {
                val eq = Expression.eq(Expression.get(idField), literal(feature.getNumberProperty(idField).toLong()))
                expressions.add(eq)
                expressions.add(color(renderColor))
            } else {
                val eq = Expression.eq(Expression.get(idField), literal(feature.getStringProperty(idField)))
                expressions.add(eq)
                expressions.add(color(renderColor))
            }
        }
        expressions.add(color(Color.TRANSPARENT))
        val toTypedArray = expressions.toTypedArray()
        val renderE: Expression = Expression.switchCase(*toTypedArray)
        return renderE
    }


    private fun clearAllRenderStyle() {
        for (mutableEntry in cacheTempLayer) {
            clearRenderStyle(mutableEntry.value)
        }
    }

    private fun clearRenderStyle(layer: Layer?) {
        if (layer == null) {
            return
        }
        if (layer is LineLayer) {
            layer.lineColor(Color.TRANSPARENT)
        }
        if (layer is FillLayer) {
            layer.fillColor(Color.TRANSPARENT)
        }
        if (layer is CircleLayer) {
            layer.circleColor(Color.TRANSPARENT)
        }
    }


    private fun screenBoxFromPixel(pixel: ScreenCoordinate) = ScreenBox(
            ScreenCoordinate(pixel.x - 18.0, pixel.y - 18.0),
            ScreenCoordinate(pixel.x + 18.0, pixel.y + 18.0)
    )


}
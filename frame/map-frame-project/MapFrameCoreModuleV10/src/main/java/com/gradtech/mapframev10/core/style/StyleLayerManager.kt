package com.gradtech.mapframev10.core.style

import android.graphics.Color
import android.util.Log
import androidx.annotation.NonNull
import com.google.gson.JsonParser
import com.gradtech.mapframev10.core.rules.Rules
import com.gradtech.mapframev10.core.util.StringEngine
import com.gradtech.mapframev10.core.util.Transformation
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Geometry
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.StyleObjectInfo
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerAbove
import com.mapbox.maps.extension.style.layers.generated.*
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.Source
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.VectorSource
import com.mapbox.maps.extension.style.sources.getSource
import org.jetbrains.annotations.NotNull
import java.util.*

/**
 * @ClassName StyleManager
 * @Description TODO
 * @Author: fs
 * @Date: 2022/9/5 13:23
 * @Version 2.0
 */

class StyleLayerManager constructor(@NonNull mapBoxMap: MapboxMap) : Rules {

    private val mMapBoxMap = mapBoxMap

    /**
     * 存储配图的layer的json
     */
    private var mLayerJsonMap: HashMap<String, String>? = null

    /**
     * 存储配图的source的json
     */
    private val mSourceJsonMap: HashMap<String, String>? = null

    /**
     * @Description 获取地图样式
     * @return
     */
    fun getMapStyleStr(): String? {
        return if (this.mMapBoxMap == null) {
            null
        } else this.mMapBoxMap.getStyle()?.styleJSON
    }

    /**
     * 根据itme 获取到最底层地图样式
     *
     * @return
     */
    fun <T> getStylePropertyItem(@NotNull vararg item: String): T? {
        return try {
            val mapStyle = getMapStyleStr()
            if (item == null) {
                return mapStyle as T?
            }
            var property = mapStyle
            var propertyItem: String
            for (element in item) {
                propertyItem = element
                property = if (propertyItem is String) {
                    StringEngine.getMinJsonStructure(property, "\"" + propertyItem + "\"")
                } else {
                    StringEngine.getMinJsonStructure(property, propertyItem)
                }
            }
            val styleJsonObj = JsonParser().parse(property).asJsonObject
            val jsonPrimitive = styleJsonObj.getAsJsonPrimitive(item[item.size - 1])
            Transformation.analysisJsonPrimitive(jsonPrimitive) as T
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取layer:groups
     * @return
     */
    fun getStyleLayerGroups(mapStyleJson: String, layerMap: LinkedHashMap<String, Layer>): LinkedHashMap<String, List<Layer>>? {
        val mapStyleStyleJsonObj = JsonParser().parse(mapStyleJson).asJsonObject
        val propertyItem = "mapbox:groups"
        val layerJsonArray = mapStyleStyleJsonObj["layers"].asJsonArray
        val styleJsonObj = mapStyleStyleJsonObj["metadata"].asJsonObject
        val groupsJsonObject = styleJsonObj[propertyItem].asJsonObject
        val groupIds: MutableList<String> = ArrayList()
        val layerJsonStringList: MutableList<String> = ArrayList()
        for (i in 0 until layerJsonArray.size()) {
            val jsonElement = layerJsonArray[i] ?: continue
            layerJsonStringList.add(jsonElement.toString())
            val jsonObject = jsonElement.asJsonObject
            if (jsonObject.has("metadata")) {
                val metadata = jsonObject["metadata"].asJsonObject
                if (metadata.has("mapbox:group")) {
                    val groupName = metadata["mapbox:group"].asString
                    if (groupIds.contains(groupName)) {
                        continue
                    }
                    groupIds.add(groupName)
                }
            }
        }
        val groups: LinkedHashMap<String, List<Layer>> = LinkedHashMap<String, List<Layer>>()
        for (groupNameId in groupIds) {
            val layerList: MutableList<Layer> = ArrayList<Layer>()
            if (mLayerJsonMap == null) {
                val size = layerMap.size / 0.75.toInt() + 1
                mLayerJsonMap = HashMap<String, String>(size)
            }
            for ((key, value) in layerMap) {
                val start = System.currentTimeMillis()
                var layerjson: String? = null
                for (i in layerJsonStringList.indices) {
                    layerjson = layerJsonStringList[i]
                    if (!layerjson.contains(key)) {
                        layerjson = null
                        continue
                    }
                    break
                }
                val end = System.currentTimeMillis()
                Log.i("gmbgl", "耗时：" + (end - start))
                if (layerjson == null || !layerjson.contains(groupNameId)) {
                    continue
                }
                mLayerJsonMap?.put(key, layerjson)
                Log.i("gmbgl", "layer.getId()：$key")
                layerList.add(value)
            }
            if ("undefined" == groupNameId) {
                continue
            }
            val groupName = groupsJsonObject[groupNameId].asJsonObject["name"].asString
            groups[groupName] = layerList
        }
        return groups
    }

    /**
     * 通过图层id获取sourceId
     *
     * @param layerId
     * @return
     */
    fun getSourceIdByLayerId(layerId: String?): String? {
        val layer: Layer? = layerId?.let { mMapBoxMap.getStyle()?.getLayer(it) }
        if (layer is FillLayer) {
            return (layer as FillLayer).sourceId
        }
        if (layer is LineLayer) {
            return (layer as LineLayer).sourceId
        }
        if (layer is SymbolLayer) {
            return (layer as SymbolLayer).sourceId
        }
        if (layer is CircleLayer) {
            return (layer as CircleLayer).sourceId
        }
        if (layer is HeatmapLayer) {
            return (layer as HeatmapLayer).sourceId
        }
        if (layer is FillExtrusionLayer) {
            return (layer as FillExtrusionLayer).sourceId
        }
        return if (layer is RasterLayer) {
            (layer as RasterLayer).sourceId
        } else null
    }

    /**
     * 根据sourceid获取source
     * @param sourceId 数据源id
     * @return
     */
    fun getSource(sourceId: String?): Source? {
        return if (sourceId == null) {
            null
        } else mMapBoxMap.getStyle()?.getSource(sourceId)
    }

    /**
     * 获取SourceUri
     * @param SourceId
     * @return
     */
    fun getSourceUri(sourceId: String): String? {
         try {
            var sourcr = mMapBoxMap.getStyle()?.getSource(sourceId) as VectorSource
            if(sourcr.url!=null && !"".equals(sourcr.url) ){
                return sourcr.url
            }
            val tiles = sourcr.tiles
            return tiles?.get(0)?.replace("/{z}/{x}/{y}.pbf", "")

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
           return null
        }
    }

    /**
     * 将layer拷贝成样式透明的FillLayer
     * @param layer
     * @return FillLayer
     */
    fun layerClone2TransparentFillLayer(layerId: String): FillLayer? {
        val layer = mMapBoxMap.getStyle()?.getLayer(layerId) ?: return null
        val cloneLayerId = String.format("%s_TransparentFillLayer", layer.layerId)
        val source: Source = getSource(getSourceIdByLayerId(layer.layerId)) ?: return null
        val expression: Expression? = getLayerFilter(layer)
        val cloneLayer: FillLayer = FillLayer(cloneLayerId, source.sourceId)
        cloneLayer.sourceLayer(if (getSourceLayerId(layer) == null) source.sourceId!! else getSourceLayerId(layer)!!)
        cloneLayer.fillOpacity(0.0)
        if (expression != null) {
            cloneLayer.filter(expression)
        }
        if (layer.maxZoom != null) {
            cloneLayer.maxZoom(layer.maxZoom!!)
        }
        if (layer.minZoom != null) {
            cloneLayer.minZoom(layer.minZoom!!)
        }
        return cloneLayer
    }

    /**
     * 获取图层的过滤条件
     * @param layer
     * @return
     */
    fun getLayerFilter(layer: Layer?): Expression? {
        if (layer == null) {
            return null
        }
        if (layer is FillLayer) {
            val fillLayer: FillLayer = layer as FillLayer
            return fillLayer.filter
        }
        if (layer is LineLayer) {
            val lineLayer: LineLayer = layer as LineLayer
            return lineLayer.filter
        }
        if (layer is SymbolLayer) {
            val symbolLayer: SymbolLayer = layer as SymbolLayer
            return symbolLayer.filter
        }
        return null
    }

    /**
     * 获取layer的SourceLayerId
     * @param layer
     * @return
     */
    fun getSourceLayerId(layer: Layer): String? {
        if (layer is LineLayer) {
            return (layer as LineLayer).sourceLayer
        }
        if (layer is FillLayer) {
            return (layer as FillLayer).sourceLayer
        }
        return if (layer is CircleLayer) {
            (layer as CircleLayer).sourceLayer
        } else null
    }

    /**
     * 添加一个图层到地图上
     *
     * @param layer
     */
    fun addLayer(layer: Layer) {
        if (mMapBoxMap == null) {
            return
        }
        if (!hasLayer(layer)) {
            Objects.requireNonNull(mMapBoxMap.getStyle())?.addLayer(layer)
        }
    }

    /**
     * 覆盖在指定图层上
     *
     * @param layer
     * @param aboveLayerId
     */
    fun addLayerAbove(@NotNull layer: Layer, aboveLayerId: String) {
        val layers: List<StyleObjectInfo>? = mMapBoxMap.getStyle()?.styleLayers
        if (mMapBoxMap == null) {
            return
        }
        var _layer: StyleObjectInfo
        var index = -1
        if(layers == null){
            return
        }
        for (i in layers.indices) {
            _layer = layers[i]
            if (_layer.getId() == aboveLayerId) {
                index = i
                break
            }
        }
        if (index == -1) {
            if (!hasLayer(layer)) {
                mMapBoxMap.getStyle()?.addLayer(layer)
            }
        } else {
            if (!hasLayer(layer)) {
                Log.i("1", layer.layerId);
                mMapBoxMap.getStyle()?.addLayerAbove(layer, aboveLayerId)
            }
        }
    }

    /**
     * 移除layer
     */
    fun removeLayer(@NotNull layer: Layer) {
        if (mMapBoxMap == null) {
            return
        }
        if (hasLayer(layer)) {
            Objects.requireNonNull(mMapBoxMap.getStyle())?.removeStyleLayer(layer.layerId)
        }
    }

    /**
     * 移除layer
     */
    fun removeLayer(@NotNull layerId: String) {
        if (mMapBoxMap == null) {
            return
        }
        if (hasLayer(layerId)) {
            Objects.requireNonNull(mMapBoxMap.getStyle())?.removeStyleLayer(layerId)
        }
    }

    /**
     * 判断是否包含某一个图层
     *
     * @param layer
     * @return
     */
    fun hasLayer(@NotNull layer: Layer): Boolean {
        if (layer == null) {
            return false
        }
        val list = (mMapBoxMap.getStyle()?.styleLayers
                ?: return false)
        for (_layer in list) {
            if (_layer.getId() == layer.layerId) {
                return true
            }
        }
        return false
    }

    /**
     * 判断是否包含某一个图层
     *
     * @param layerId
     * @return
     */
    fun hasLayer(@NotNull layerId: String): Boolean {
        if (layerId == null) {
            return false
        }
        val list = (mMapBoxMap.getStyle()?.styleLayers
                ?: return false)
        for (_layer in list) {
            if (_layer.getId() == layerId) {
                return true
            }
        }
        return false
    }

    /**
     * 添加source
     */
    fun addSource(@NotNull source: Source) {
        try {
            if (!hasSource(source)) {
                mMapBoxMap.getStyle()?.addSource(source)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 判断source是否存在
     * @param source
     * @return
     */
    fun hasSource(@NotNull source: Source): Boolean {
        return mMapBoxMap.getStyle()?.getSource(source.sourceId) != null
    }

    /**
     * 创建一个简单的基本的GeoJsonSource
     * @param sourceId
     * @return
     */
    fun createSimpleGeoJsonSource(@NotNull sourceId: String): GeoJsonSource? {
        return if (sourceId == null) {
            null
        } else GeoJsonSource.Builder(sourceId).build()
    }
    /**
     * 判断source是否存在
     * @param source
     * @return
     */
    fun hasSource(@NotNull sourceId: String): Boolean {
        return mMapBoxMap.getStyle()?.getSource(sourceId) != null
    }


    /**
     * 移除source
     */
    fun removeSource(@NotNull sourceId: String){
        mMapBoxMap.getStyle()?.removeStyleSource(sourceId);
    }
    /**
     * 移除source
     */
    fun removeSource(@NotNull source: Source){
        mMapBoxMap.getStyle()?.removeStyleSource(source.sourceId);
    }

    /**
     *
     * 获取layer
     *
     * @param layerId
     * @return
     */
    fun getLayer(layerId: String?): Layer? {
        return if (layerId == null) {
            null
        } else mMapBoxMap.getStyle()!!.getLayer(layerId)
    }

    /**
     * 设置图层的过滤条件
     * @param layer
     * @param expression
     */
    fun setFilter2Layer(layer: Layer?, expression: Expression?) {
        if (layer == null) {
            return
        }
        if (expression == null) {
            return
        }
        if (layer is FillLayer) {
            (layer as FillLayer).filter(expression)
            return
        }
        if (layer is LineLayer) {
            (layer as LineLayer).filter(expression)
            return
        }
        if (layer is SymbolLayer) {
            (layer as SymbolLayer).filter(expression)
            return
        }
        /*if (layer instanceof FillExtentLayer) {
            ((FillExtentLayer) layer).setFilter(expression);
            return;
        }*/if (layer is CircleLayer) {
            (layer as CircleLayer).filter(expression)
            return
        }
    }


    /**
     * 高亮
     * @param wkt
     * @param lineColor 不传默认#a8ffff
     */
    fun highLightRegion(@NotNull wkt: String, lineColor: Int?) {
        if (wkt == null) {
            return
        }
        val geometry: Geometry = Transformation.wkt2BoxGeometry(wkt)
        highLightRegion(geometry, lineColor)
    }

    /**
     * 高亮当前图形
     * @param geometry
     * @param lineColor 不传默认#a8ffff
     */
    fun highLightRegion(@NotNull geometry: Geometry, lineColor: Int?) {
        var lineColor = lineColor
        val highLightLayerId = "highLightLayerId"
        val highLightSourceId = "highLightSourceId"
        var highLightSource: GeoJsonSource?
        var highLightLayer: LineLayer?
        if (!hasSource(highLightSourceId)) {
            highLightSource = createSimpleGeoJsonSource(highLightSourceId)
        } else {
            highLightSource = getSource(highLightSourceId) as GeoJsonSource?
        }
        if (!hasLayer(highLightLayerId)) {
            if (lineColor == null) {
                lineColor = Color.parseColor("#a8ffff")
            }
            highLightLayer = LineLayer(highLightLayerId, highLightSourceId)
                    .sourceLayer(highLightSourceId).lineColor(lineColor).lineWidth(3.0);
        } else {
            highLightLayer = getLayer(highLightLayerId) as LineLayer?
        }
        highLightSource?.feature(Feature.fromGeometry(geometry))
        addSource(highLightSource!!)
        if (!hasLayer(highLightLayer!!)) {
            addLayer(highLightLayer)
        }
    }

    /**
     * 高亮当前图形
     * @param featureCollection
     * @param lineColor 不传默认#a8ffff
     */
    fun highLightRegion(@NotNull featureCollection: FeatureCollection, lineColor: Int?) {
        var lineColor = lineColor
        val highLightLayerId = "highLightLayerId"
        val highLightSourceId = "highLightSourceId"
        val highLightSource: GeoJsonSource?
        val highLightLayer: LineLayer?
        if (!hasSource(highLightSourceId)) {
            highLightSource = createSimpleGeoJsonSource(highLightSourceId)
        } else {
            highLightSource = getSource(highLightSourceId) as GeoJsonSource?
        }
        if (!hasLayer(highLightLayerId)) {
            if (lineColor == null) {
                lineColor = Color.parseColor("#a8ffff")
            }
            highLightLayer = LineLayer(highLightLayerId, highLightSourceId)
                    .sourceLayer(highLightSourceId)
                    .lineColor(lineColor)
                    .lineWidth(3.0)

        } else {
            highLightLayer = getLayer(highLightLayerId) as LineLayer?
        }
        highLightSource?.featureCollection(featureCollection)
        addSource(highLightSource!!)
        if (!hasLayer(highLightLayer!!)) {
            addLayer(highLightLayer!!)
        }
    }

    /**
     * 取消高亮
     */
    fun cancelHighLightRegion() {
        val highLightLayerId = "highLightLayerId"
        val highLightSourceId = "highLightSourceId"
        removeLayer(highLightLayerId)
        removeSource(highLightSourceId)
    }
}
package com.gradtech.mapframev10.core.offline.gc

import android.graphics.Color
import android.util.Log
import android.widget.Toast
import com.gradtech.mapframev10.core.enumeration.GeometryType
import com.gradtech.mapframev10.core.maps.GMapView
import com.gradtech.mapframev10.core.offline.gc.GraphicSetting.*
import com.gradtech.mapframev10.core.offline.geosource.BaseGeoSource
import com.gradtech.mapframev10.core.offline.geosource.IBaseGeoSource
import com.gradtech.mapframev10.core.util.FileUtil
import com.gradtech.mapframev10.core.util.StringEngine
import com.mapbox.geojson.Feature
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.generated.*
import org.jetbrains.annotations.NotNull
import java.io.File

/**
 * @ClassName GraphicContainer
 * @Description TODO
 * @Author: fs
 * @Date: 2022/9/16 14:35
 * @Version 2.0
 */
class GraphicContainer:IGraphicContainer {
    //临时图层source
    private val TEMP_SOURCEID = "tempSource"
    private val QUERY_LAYER = "_querylayer"
    private val TEMP_LAYER = "_templayer"
    private val TARG_LAYER = "_targlayer"

    private lateinit var mGMapView:GMapView
    private lateinit var mGraphicSetting: GraphicSetting

    private var mPointSource:BaseGeoSource? = null
    private var mLineSource:BaseGeoSource? = null
    private var mFillSource:BaseGeoSource? = null
    private var mCurrentSource:BaseGeoSource?= null

    /**
     * 临时显示图层
     */
    private var mTempLayer: Layer? = null

    /**
     * 临时编辑图层
     */
    private var mTargetLayer: Layer? = null

    /**
     * 临时查询图层
     */
    private var mQueryLayer: Layer? = null
    

    //标记图层
    private var mTargetLayerId: String? = null

    /**
     * 目标编辑图层数据源id
     */
    private var mTargetSourceId: String? = null

    private var mLineLayer:LineLayer? = null
    private var mPointLayer: CircleLayer? = null

    constructor(gMapView: GMapView, graphicSetting: GraphicSetting){
        mGMapView = gMapView
        mGraphicSetting = graphicSetting
        bindMap()
    }

    private fun initSetting(@NotNull graphicSetting: GraphicSetting) {
        if (graphicSetting == null) {
            return
        }

        val cachePathParent: String = FileUtil.getParentPathByPath(graphicSetting.getCachePath())
        val cacheFileName: String = FileUtil.getFileNameByPath(graphicSetting.getCachePath())
        var geometryType = graphicSetting.getGeometryType()
        if (geometryType == GeometryType.point) {
            mPointSource = BaseGeoSource(TEMP_SOURCEID + graphicSetting.getCachePath() + "_point", cachePathParent + File.separator + cacheFileName + "_point")
            mPointSource!!.initDraw()
            mCurrentSource = mPointSource
        }
        if (geometryType == GeometryType.line) {
            mLineSource = BaseGeoSource(TEMP_SOURCEID + graphicSetting.getCachePath() + "_line", cachePathParent + File.separator + cacheFileName + "_line")
            mLineSource!!.initDraw()
            mCurrentSource = mLineSource
        }
        if (geometryType == GeometryType.polygon) {
            mFillSource = BaseGeoSource(TEMP_SOURCEID + graphicSetting.getCachePath() + "_polygon", cachePathParent + File.separator + cacheFileName + "_polygon")
            mFillSource!!.initDraw()
            mCurrentSource = mFillSource
        }
        mTargetLayerId = graphicSetting.getTargetLayerId()
        if (mTargetLayerId == "") {
            Toast.makeText(mGMapView.context, "请先初始化目标图层", Toast.LENGTH_LONG).show()
            return
        }
        mTargetSourceId = mGMapView.getStyleLayerManager().getSourceIdByLayerId(mTargetLayerId)
        val cloneLayer = mGMapView.getStyleLayerManager().getLayer(mTargetLayerId)
        if (cloneLayer != null) {
            //根据要编辑的图层克隆一个图层样式，设置为临时图层数据源
            mTargetLayer = cloneTargetLayer(mCurrentSource!!, cloneLayer)
            //设置过滤表达式
            val targetLayerExp: Expression = Expression.eq(Expression.get(GraphicSetting.SOURCE_FIELD), Expression.literal(mTargetSourceId!!))
            mGMapView.getStyleLayerManager().setFilter2Layer(mTargetLayer, targetLayerExp)
            mGMapView.getStyleLayerManager().addLayer(mTargetLayer!!);
        } else {
            Log.i("gmbgl-GraphicC", String.format("未找到要编辑的图层[%s]", mTargetLayerId))
        }
        var minZoom :Double?= 13.0
        var maxZoom :Double?= 18.0
        if (mTargetLayer != null) {
            minZoom = mTargetLayer?.minZoom
            maxZoom = mTargetLayer?.maxZoom
        }
        //设置过滤条件
        val tempLayerExp: Expression = Expression.eq(Expression.get(GraphicSetting.SOURCE_FIELD), Expression.literal(""))

        if (geometryType == GeometryType.polygon) {
            val sourceId = mFillSource?.getGeoSource()?.sourceId!!
            var mFillLayer:FillLayer = fillLayer(sourceId + QUERY_LAYER, sourceId){
                fillColor(Color.GREEN).fillOpacity(0.0).fillOutlineColor(Color.GREEN)
            }

            mFillLayer.minZoom(minZoom!!)
            mFillLayer.maxZoom(maxZoom!!)
            mLineLayer = lineLayer(sourceId + TEMP_LAYER, sourceId){
                lineColor(Color.GREEN).lineWidth(3.0).lineOpacity(1.0)
            }
            mFillLayer.minZoom(minZoom)
            mFillLayer.maxZoom(maxZoom)
            mLineLayer!!.filter(tempLayerExp)
            mTempLayer = mLineLayer

            mQueryLayer = mFillLayer
        }
        if (geometryType == GeometryType.line) {
            val sourceId = mLineSource?.getGeoSource()?.sourceId!!
            var mLineLayer = lineLayer(sourceId + TEMP_LAYER, sourceId){
               lineColor(Color.GREEN).lineWidth(3.0).lineOpacity(1.0)
            }
            mLineLayer.minZoom(minZoom!!)
            mLineLayer.maxZoom(maxZoom!!)
            mLineLayer.filter(tempLayerExp)
            mTempLayer = mLineLayer
            mQueryLayer = mLineLayer
        }
        if (geometryType == GeometryType.point) {
            val sourceId = mPointSource?.getGeoSource()?.sourceId!!
            var mPointLayer = circleLayer(sourceId + TEMP_LAYER, sourceId){
                circleColor(Color.GREEN).circleRadius(6.0)
            }
            mPointLayer.minZoom(minZoom!!)
            mPointLayer.maxZoom(maxZoom!!)
            mPointLayer.filter(tempLayerExp)
            mTempLayer = mPointLayer
            mQueryLayer = mPointLayer
        }
        mGMapView.getStyleLayerManager().addSource(mCurrentSource!!.getGeoSource()!!)
        val haslayer = mGMapView.getStyleLayerManager().hasLayer(mTargetLayerId!!)
        if (haslayer) {
            mGMapView.getStyleLayerManager().addLayerAbove(mTempLayer!!, mTargetLayerId!!)
            if (mTargetLayer != null) {
                mGMapView.getStyleLayerManager().addLayerAbove(mTargetLayer!!, mTargetLayerId!!)
            }
            mGMapView.getStyleLayerManager().addLayerAbove(mQueryLayer!!, mTargetLayerId!!)
        } else {
            mGMapView.getStyleLayerManager().addLayer(mTempLayer!!)
            if (mTargetLayer != null) {
                mGMapView.getStyleLayerManager().addLayer(mTargetLayer!!)
            }
            mGMapView.getStyleLayerManager().addLayer(mQueryLayer!!)
        }
    }
    
    override fun bindMap() {
        initSetting(mGraphicSetting)

    }

    override fun unbindMap() {
        TODO("Not yet implemented")
    }

    override fun getQueryLayerId(): String? {
        return mQueryLayer?.layerId;
    }

    override fun getRenderLayerId(): String? {
        if (mGraphicSetting.geometryType == GeometryType.polygon || mGraphicSetting.geometryType == GeometryType.line) {
            return if (mLineLayer == null) {
                null
            } else mLineLayer?.layerId
        }
        return if (mGraphicSetting.geometryType == GeometryType.point) {
            if (mPointLayer == null) {
                null
            } else mPointLayer?.layerId
        } else null
    }

    override fun insert(feature: Feature, updateTargetSource: Boolean): Feature? {
        if (updateTargetSource) {
            feature.addStringProperty(SOURCE_FIELD, mTargetSourceId)
        }
        //判断是否有需要进行关联删除的临时图层要素
        //判断是否有需要进行关联删除的临时图层要素
        if (!feature.hasProperty(FROM_LAYER_ID_FIELD) || !feature.hasProperty(FROM_UID_FIELD)) {
            return mCurrentSource!!.insert(feature)
        }
        val fromLayerId = feature.getStringProperty(FROM_LAYER_ID_FIELD)
        val fromUid = feature.getStringProperty(FROM_UID_FIELD)
        if (fromLayerId != "" && fromUid != "") {
            val lyrIds = fromLayerId.split(",".toRegex()).toTypedArray()
            val uids = fromUid.split(",".toRegex()).toTypedArray()
            for (i in lyrIds.indices) {
                val lyrId = lyrIds[i]
                if (lyrId != getQueryLayerId()) {
                    continue
                }
                //如果是来源临时图层的要素，则删除
                val uid = uids[i]
                val fea = mCurrentSource!!.queryFeature(UID_FIELD, uid)
                        ?: continue
                mCurrentSource!!.delete(fea)
            }
        }
        assemble(feature)
        return mCurrentSource!!.insert(feature)
    }

    override fun inserts(features: List<Feature>, iGcCallBack: IBaseGeoSource.IGcCallBack?, updateTargetSource: Boolean, sync: Boolean) {
        try {
            for (feature in features) {
                if (updateTargetSource) {
                    feature.addStringProperty(GraphicSetting.SOURCE_FIELD, mTargetSourceId)
                }
                assemble(feature)
                if (feature == null || mCurrentSource == null) {
                    continue
                }
                //判断是否有需要进行关联删除的临时图层要素
                if (!feature.hasProperty(GraphicSetting.FROM_LAYER_ID_FIELD) || !feature.hasProperty(GraphicSetting.FROM_UID_FIELD)) {
                    if (sync) {
                        mCurrentSource!!.insert(feature)
                    }
                    continue
                }
                val fromLayerId = feature.getStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD)
                val fromUid = feature.getStringProperty(GraphicSetting.FROM_UID_FIELD)
                if (fromLayerId != "" && fromUid != "") {
                    val lyrIds = fromLayerId.split(",".toRegex()).toTypedArray()
                    val uids = fromUid.split(",".toRegex()).toTypedArray()
                    for (i in lyrIds.indices) {
                        val lyrId = lyrIds[i]
                        if (lyrId != getQueryLayerId()) {
                            continue
                        }
                        //如果是来源临时图层的要素，则删除
                        val uid = uids[i]
                        val fea = mCurrentSource!!.queryFeature(GraphicSetting.UID_FIELD, uid)
                                ?: continue
                        mCurrentSource!!.delete(fea)
                    }
                }
                if (sync) {
                    mCurrentSource!!.insert(feature)
                }
            }
            if (!sync) {
                mCurrentSource!!.insertsCacheUnSync(features, iGcCallBack)
            } else {
                iGcCallBack!!.onGcCallBack(features)
            }
        } catch (e: Exception) {
            iGcCallBack!!.onError(e)
        }
    }

    override fun queryFeatures(con: HashMap<String, Any>): List<Feature> {
      return mCurrentSource!!.queryFeatures(con)
    }

    override fun queryFeaturesLike(con: HashMap<String, Any>): List<Feature> {
        return mCurrentSource!!.queryFeaturesLike(con)
    }

    override fun queryFeature(key: String, value: String): Feature? {
        return mCurrentSource!!.queryFeature(key, value)
    }

    override fun delete(feature: Feature): Boolean {
        return mCurrentSource!!.delete(feature)
    }

    override fun deletes(features: List<Feature>): Boolean {
        var des = false
        if (mCurrentSource == null) {
            return des
        }
        for (feature in features) {
            des = mCurrentSource!!.delete(feature)
        }
        return des
    }

    override fun deleteFeatures(con: HashMap<String, Any>): Boolean {
        return if (mCurrentSource == null) {
            false
        } else mCurrentSource!!.deleteFeatures(con)
    }

    override fun deleteAll(): Boolean {
        return if (mCurrentSource == null) {
            false
        } else mCurrentSource!!.deleteAll()
    }

    override fun update(feature: Feature?): Boolean {
        if (mCurrentSource == null) {
            return false
        }
        mCurrentSource!!.update(feature)
        return true
    }

    override fun updates(features: List<Feature>): Boolean {
        if (mCurrentSource == null) {
            return false
        }
        for (feature in features) {
            mCurrentSource!!.update(feature)
        }
        return true
    }


    /**
     * 克隆目标图层
     *
     * @param source
     * @param cloneLayer
     * @return
     */
    private fun cloneTargetLayer(source: BaseGeoSource, cloneLayer: Layer): Layer? {
        val sourceId = source.getGeoSource()!!.sourceId
        if (cloneLayer is FillLayer) {
            val fLyr: FillLayer = FillLayer(sourceId + TARG_LAYER, sourceId)
            var fillColor ="";
            //Log.i("gmap",fillColor);
            fillColor = "#eb23e4"
            copyProperties(cloneLayer, fLyr, fillColor)
            return fLyr
        }
        if (cloneLayer is LineLayer) {
            val lLyr: LineLayer = LineLayer(sourceId + TARG_LAYER, sourceId)
            copyProperties(cloneLayer, lLyr, "")
            return lLyr
        }
        if (cloneLayer is CircleLayer) {
            val pLyr: CircleLayer = CircleLayer(sourceId + TARG_LAYER, sourceId)
            copyProperties(cloneLayer, pLyr, "")
            return pLyr
        }
        return null
    }

    private fun copyProperties(layer1: Layer, layer2: Layer, fill_color: String?) {
        if (layer1 is FillLayer && layer2 is FillLayer) {
            val fillLayer1: FillLayer = layer1
            val fillLayer2: FillLayer = layer2
            fillLayer2.fillAntialias(fillLayer1.fillAntialias!!).
                        fillOpacity(fillLayer1.fillOpacity!!).
                        fillColor(fillLayer1.fillColor!!).
            fillOutlineColor(fillLayer1.fillOutlineColor!!).
            fillPattern(fillLayer1.fillPattern!!).
            fillTranslate(fillLayer1.fillTranslate!!).
            fillTranslateAnchor(fillLayer1.fillTranslateAnchor!!)

            if (fill_color != null && fill_color != "") {
                fillLayer2.fillColor(fill_color)
            }
            fillLayer2.maxZoom(fillLayer1.maxZoom!!)
            fillLayer2.minZoom(fillLayer1.minZoom!!)
        }
        if (layer1 is LineLayer && layer2 is LineLayer) {
            val fillLayer1: LineLayer = layer1
            val fillLayer2: LineLayer = layer2
            if(fillLayer1.lineBlur!=null){
                fillLayer2.lineBlur(fillLayer1.lineBlur!!);
            }
            if(fillLayer1.lineCap!=null){
                fillLayer2.lineCap(fillLayer1.lineCap!!);
            }
            if(fillLayer1.lineColor!=null){
                fillLayer2.lineColor(fillLayer1.lineColor!!);
            }
            if(fillLayer1.lineDasharray!=null){
                fillLayer2.lineDasharray(fillLayer1.lineDasharray!!);
            }
            if(fillLayer1.lineGapWidth!=null){
                fillLayer2.lineGapWidth(fillLayer1.lineGapWidth!!);
            }
            if(fillLayer1.lineJoin!=null){
                fillLayer2.lineJoin(fillLayer1.lineJoin!!);
            }
            if(fillLayer1.lineMiterLimit!=null){
                fillLayer2.lineMiterLimit(fillLayer1.lineMiterLimit!!);
            }
            if(fillLayer1.lineOpacity!=null){
                fillLayer2.lineOpacity(fillLayer1.lineOpacity!!);
            }
            if(fillLayer1.lineOffset!=null){
                fillLayer2.lineOffset(fillLayer1.lineOffset!!);
            }
            if(fillLayer1.linePattern!=null){
                fillLayer2.linePattern(fillLayer1.linePattern!!);
            }
            if(fillLayer1.lineWidth!=null){
                fillLayer2.lineWidth(fillLayer1.lineWidth!!);
            }
            if(fillLayer1.lineRoundLimit!=null){
                fillLayer2.lineRoundLimit(fillLayer1.lineRoundLimit!!);
            }
            if(fillLayer1.maxZoom!=null){
                fillLayer2.maxZoom(fillLayer1.maxZoom!!);
            }
            if(fillLayer1.minZoom!=null){
                fillLayer2.minZoom(fillLayer1.minZoom!!);
            }
        }
        if (layer1 is SymbolLayer && layer2 is SymbolLayer) {
            val fillLayer1: SymbolLayer = layer1
            val fillLayer2: SymbolLayer = layer2
            fillLayer2.textColor(fillLayer1.textColor!!).
            textSize(fillLayer1.textSize!!).
            iconAllowOverlap(fillLayer1.iconAllowOverlap!!).
            iconAnchor(fillLayer1.iconAnchor!!).
            iconHaloBlur(fillLayer1.iconHaloBlur!!).
            iconHaloColor(fillLayer1.iconHaloColor!!).
            iconHaloWidth(fillLayer1.iconHaloWidth!!).
            iconIgnorePlacement(fillLayer1.iconIgnorePlacement!!).
            iconImage(fillLayer1.iconImage!!).
            iconKeepUpright(fillLayer1.iconKeepUpright!!).
            iconOffset(fillLayer1.iconOffset!!).
            iconOptional(fillLayer1.iconOptional!!).
            iconPadding(fillLayer1.iconPadding!!).
            textFont(fillLayer1.textFont!!).
            textOpacity(fillLayer1.textOpacity!!).
            textHaloWidth(fillLayer1.textHaloWidth!!).
            textHaloBlur(fillLayer1.textHaloBlur!!).
            textLineHeight(fillLayer1.textLineHeight!!).
            textAnchor(fillLayer1.textAnchor!!).
            textOffset(fillLayer1.textOffset!!).
            textField(fillLayer1.textField!!)
            fillLayer2.maxZoom(fillLayer1.maxZoom!!)
            fillLayer2.minZoom(fillLayer1.minZoom!!)
        }
    }

    /**
     * 补充临时图层必须有的字段属性，uuid sourceField  tag
     *
     * @param feature
     * @return
     */
    private fun assemble(feature: Feature): Feature {
        var uid: String? = StringEngine.get32UUID()
        if (!feature.hasProperty(GraphicSetting.UID_FIELD)) {
            feature.addStringProperty(GraphicSetting.UID_FIELD, uid)
        } else {
            uid = feature.getStringProperty(GraphicSetting.UID_FIELD)
        }
        if (!feature.hasProperty(GraphicSetting.SOURCE_FIELD)) {
            feature.addStringProperty(GraphicSetting.SOURCE_FIELD, "")
        }
        if (!feature.hasProperty(GraphicSetting.SOURCE_TAG)) {
            feature.addStringProperty(GraphicSetting.SOURCE_TAG, GraphicSetting.SOURCE_TAG)
        }
        return Feature.fromGeometry(feature.geometry(), feature.properties(), uid)
    }

}
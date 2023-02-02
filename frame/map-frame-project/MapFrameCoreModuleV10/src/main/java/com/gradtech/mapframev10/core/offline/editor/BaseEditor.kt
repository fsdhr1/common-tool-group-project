package com.gradtech.mapframev10.core.offline.editor

import android.content.Context
import android.widget.Toast
import com.gradtech.mapframev10.core.offline.gc.GraphicSetting
import com.gradtech.mapframev10.core.offline.gc.IGraphicContainer
import com.gradtech.mapframev10.core.offline.geosource.IBaseGeoSource
import com.gradtech.mapframev10.core.util.FeatureSet
import com.gradtech.mapframev10.core.util.FeatureUtil
import com.gradtech.mapframev10.core.util.Transformation
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Geometry
import java.util.*

/**
 * @ClassName BaseEditor
 * @Description TODO
 * @Author: fs
 * @Date: 2022/12/20 15:00
 * @Version 2.0
 */
class BaseEditor constructor(graphicContainer: IGraphicContainer) :IEditor {

    private val mGraphicContainer: IGraphicContainer = graphicContainer
    
    fun getGraphicContainer(): IGraphicContainer {
        return mGraphicContainer
    }

    override fun unionFeatures(context: Context, flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>) {
        //获取结果集

        //获取结果集
        val geometries: List<Geometry> = FeatureSet.convert2Geometries(selectSet)
        val tolerance = 2.0f //设定合并的容差，以后容差放到系统设置中
        //modify by clj 20190321 对于合并出来的多边形求外包突壳，作为合并后的多边形
        val unionGeo: com.vividsolutions.jts.geom.Geometry = Transformation.mergeGeometries(geometries, tolerance.toDouble())
        if (unionGeo == null) {
            Toast.makeText(context, String.format("地块之间相离超过%s 米!", tolerance), Toast.LENGTH_LONG).show()
            return
        }
        if (!unionGeo.isValid) {
            Toast.makeText(context, "合并结果不合法,请重新选择!", Toast.LENGTH_LONG).show()
            return
        }
        try {
            Transformation.compute84GeoJsonArea(Transformation.jstGeometry2GeoJson(unionGeo) as Geometry)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        val feature = Feature.fromGeometry(Transformation.jstGeometry2GeoJson(unionGeo) as Geometry)
        if (!featureValid(feature)) {
            return
        }
        assembleUnion(FeatureSet.convert2FeatureSets(selectSet), feature)
        if (iToolInterceptor != null) {
            if (iToolInterceptor.doHandle(feature, flag)) {
                return
            }
        }
        //结果集存入临时图层
        //结果集存入临时图层
        mGraphicContainer.insert(feature, updateTargetSource)

        if (iToolInterceptor != null) {
            iToolInterceptor.afterHandle(feature, flag)
        }
    }

    override fun delFeatures(flag: String, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>) {
        //获取结果集

        //获取结果集
        if (iToolInterceptor != null) {
            if (iToolInterceptor.doHandle(selectSet, flag)) {
                return
            }
        }
        val features: List<Feature> = FeatureSet.convert2Features(selectSet)
        if (!featuresValid(features)) {
            return
        }
        //结果集存入临时图层
        //结果集存入临时图层
        mGraphicContainer.deletes(features)
        //对外通知
        //对外通知
        val selectSetClone: Map<String, FeatureSet?>? = selectSetClone(selectSet)
        if (iToolInterceptor != null) {
            iToolInterceptor.afterHandle(selectSetClone, flag)
        }
    }

    override fun splitFeatures(flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>, geometry: Geometry) {
        try {
            val featureSets: List<FeatureSet> = FeatureSet.convert2FeatureSets(selectSet)
                    ?: return
            var featureSet: FeatureSet
            val _featureSets: MutableList<FeatureSet> = ArrayList<FeatureSet>()
            for (i in featureSets.indices) {
                val features: List<Feature> = Transformation.divisionFeatures(featureSets[i].features(), geometry)
                featureSet = FeatureSet.fromFeatures(features)
                featureSet.setLayerId(featureSets[i].getLayerId()).setSourceId(featureSets[i].getSourceId())
                _featureSets.add(featureSet)
            }
            assembleSplit(_featureSets)
            if (iToolInterceptor != null) {
                if (iToolInterceptor.doHandle(_featureSets, flag)) {
                    return
                }
            }
            mGraphicContainer.inserts(FeatureSet.convert2Features(_featureSets), object : IBaseGeoSource.IGcCallBack {
                override fun onGcCallBack(features: List<Feature?>?) {
                    if (iToolInterceptor != null) {
                        iToolInterceptor.afterHandle(_featureSets, flag)
                    }
                }

                override fun onError(e: Throwable) {
                    e.fillInStackTrace()
                }
            }, updateTargetSource, false)

          
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun digFeatures(flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>, geometry: Geometry) {
        val featureSets: List<FeatureSet> = FeatureSet.convert2FeatureSets(selectSet)
        if (featureSets == null || featureSets.size == 0) {
            return
        }
        val featureSet: FeatureSet = featureSets[0]
        val features: List<Feature>? = featureSet.features()
        if (features == null || features.size == 0) {
            return
        }
        val srcFeature = features[0]
        val _geometry: Geometry = Transformation.diffFeature(srcFeature.geometry(), geometry)
                ?: return

        val feature = Feature.fromGeometry(_geometry, srcFeature.properties())
        if (!featureValid(feature)) {
            return
        }
        assembleDig(feature, featureSet.getLayerId(), FeatureUtil.getFeatureId(featureSet.getLayerId(), srcFeature))
        if (iToolInterceptor != null) {
            if (iToolInterceptor.doHandle(feature, flag)) {
                return
            }
        }
        mGraphicContainer.insert(feature, updateTargetSource)
        if (iToolInterceptor != null) {
            iToolInterceptor.afterHandle(feature, flag)
        }
    }

    override fun drawFeature(flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, geometry: Geometry) {
        val feature = Feature.fromGeometry(geometry)
        if (!featureValid(feature)) {
            return
        }
        assembleNewDraw(feature)
        if (iToolInterceptor != null) {
            if (iToolInterceptor.doHandle(feature, flag)) {
                return
            }
        }
        mGraphicContainer.insert(feature, updateTargetSource)
        iToolInterceptor.afterHandle(feature, flag)
    }

    override fun addFeatures(flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>) {
        //获取结果集
        val setList: List<FeatureSet> = FeatureSet.convert2FeatureSets(selectSet)
        assembleCopy(setList)
        if (iToolInterceptor != null) {
            if (iToolInterceptor.doHandle(setList, flag)) {
                return
            }
        }
        val features: List<Feature> = FeatureSet.convert2Features(setList)
        if (!featuresValid(features)) {
            return
        }
        //结果集存入临时图层
        mGraphicContainer.inserts(features, object : IBaseGeoSource.IGcCallBack {
            override fun onGcCallBack(features: List<Feature?>?) {
                if (iToolInterceptor != null) {
                    iToolInterceptor.afterHandle(setList, flag)
                }
            }

            override fun onError(e: Throwable) {
                e.fillInStackTrace()
            }
        }, updateTargetSource, false)
    }

    override fun editFeature(flag: String, updateTargetSource: Boolean, iToolInterceptor: IToolInterceptor, selectSet: Map<String, FeatureSet>, newGeometry: Geometry) {
        val featureSets: List<FeatureSet> = FeatureSet.convert2FeatureSets(selectSet)
                ?: return
        val featureSet: FeatureSet = featureSets[0]
        val srcFeature: Feature? = featureSet.features()?.get(0)
        val feature = Feature.fromGeometry(newGeometry, srcFeature?.properties())
        if (!featureValid(feature)) {
            return
        }
        assembleEdit(feature, featureSet.getLayerId(), FeatureUtil.getFeatureId(featureSet.getLayerId(), srcFeature))
        if (iToolInterceptor != null) {
            if (iToolInterceptor.doHandle(feature, flag)) {
                return
            }
        }
        mGraphicContainer.insert(feature, updateTargetSource)
        if (iToolInterceptor != null) {
            iToolInterceptor.afterHandle(feature, flag)
        }
    }

    /**
     * 校验feature是否合法
     */
    private fun featuresValid(features: List<Feature>?): Boolean {
        if (features == null) {
            return false
        }
        for (feature in features) {
            if (feature.geometry() == null) {
                return false
            }
        }
        return true
    }

    private fun featureValid(feature: Feature?): Boolean {
        if (feature == null) {
            return false
        }
        return if (feature.geometry() == null) {
            false
        } else true
    }

    /**
     * 新绘制一块
     *
     * @param feature
     * @return
     */
    private fun assembleNewDraw(feature: Feature?): Feature? {
        if (feature == null) {
            return feature
        }
        feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, "")
        feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, "")
        val areaAndLen: Array<Double> = Transformation.compute84GeoJsonLenAndArea(feature)
        feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0])
        feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1])
        if (!feature.hasProperty(GraphicSetting.SOURCE_FIELD)) {
            feature.addStringProperty(GraphicSetting.SOURCE_FIELD, "")
        }
        return feature
    }

    private fun assembleUnion(featureSets: List<FeatureSet>, feature: Feature) {
        val areaAndLen: Array<Double> = Transformation.compute84GeoJsonLenAndArea(feature)
        feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0])
        feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1])
        var featureSet: FeatureSet
        val id = StringBuilder()
        val layerId = StringBuilder()
        for (i in featureSets.indices) {
            featureSet = featureSets[i]
            val features: MutableList<Feature> = featureSet.features() ?: continue
            for (j in features.indices) {
                id.append(FeatureUtil.getFeatureId(featureSet.layerId, features[j])).append(",")
                layerId.append(featureSet.getLayerId()).append(",")
            }
        }
        id.deleteCharAt(id.length - 1)
        layerId.deleteCharAt(layerId.length - 1)
        feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, layerId.toString())
        feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, id.toString())
        feature.removeProperty(GraphicSetting.UID_FIELD)
    }

    /**
     * 克隆选择集
     * @param selectSet
     * @return
     */
    private fun selectSetClone(selectSet: Map<String, FeatureSet>): Map<String, FeatureSet?>? {
        if (selectSet == null) {
            return null
        }
        val clone: MutableMap<String, FeatureSet?> = HashMap<String, FeatureSet?>(selectSet.size)
        var featureSet: FeatureSet?
        for (name in selectSet.keys) {
            featureSet = selectSet[name]
            if (featureSet == null) {
                clone[name] = null
            } else {
                clone[name] = featureSet.clone()
            }
        }
        return clone
    }

    private fun assembleSplit(setList: List<FeatureSet>?) {
        if (setList == null) {
            return
        }
        var featureSet: FeatureSet
        for (i in setList.indices) {
            featureSet = setList[i]
            val fromLayerIdField: String = featureSet.getLayerId()
            var area: Double
            val features: List<Feature> = featureSet.features()
                    ?: return
            var j = 0
            val len = features.size
            while (j < len) {
                val feature = features[j]
                val areaAndLen: Array<Double> = Transformation.compute84GeoJsonLenAndArea(feature)
                feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0])
                feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1])
                feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, fromLayerIdField)
                val id: String = FeatureUtil.getFeatureId(featureSet.getLayerId(), feature)
                feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, id)
                feature.removeProperty(GraphicSetting.UID_FIELD)
                j++
            }
            featureSet.replaceAll(features)
        }
    }
    
    private fun assembleDig(feature: Feature, fromLyrId: String, fromUid: String) {
        val areaAndLen: Array<Double> = Transformation.compute84GeoJsonLenAndArea(feature)
        feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0])
        feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1])
        feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, fromLyrId)
        feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, fromUid)
    }

    /**
     * @param setList
     * @return
     */
    private fun assembleCopy(setList: List<FeatureSet>?) {
        if (setList == null) {
            return
        }
        var featureSet: FeatureSet
        for (i in setList.indices) {
            featureSet = setList[i]
            val fromLayerIdField: String = featureSet.getLayerId()
            var area: Double
            val features: List<Feature> = featureSet.features()
                    ?: return
            var feature: Feature
            var id: String
            var j = 0
            val len = features.size
            while (j < len) {
                feature = features[j]
                val areaAndLen: Array<Double> = Transformation.compute84GeoJsonLenAndArea(feature)
                feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0])
                feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1])
                feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, fromLayerIdField)
                id = FeatureUtil.getFeatureId(featureSet.getLayerId(), feature)
                feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, id)
                feature.removeProperty(GraphicSetting.UID_FIELD)
                j++
            }
            featureSet.replaceAll(features)
        }
    }

    private fun assembleEdit(feature: Feature, fromLyrId: String, fromUid: String) {
        val areaAndLen: Array<Double> = Transformation.compute84GeoJsonLenAndArea(feature)
        feature.addNumberProperty(GraphicSetting.LEN_FILED, areaAndLen[0])
        feature.addNumberProperty(GraphicSetting.MJ_FILED, areaAndLen[1])
        feature.addStringProperty(GraphicSetting.FROM_LAYER_ID_FIELD, fromLyrId)
        feature.addStringProperty(GraphicSetting.FROM_UID_FIELD, fromUid)
    }
}
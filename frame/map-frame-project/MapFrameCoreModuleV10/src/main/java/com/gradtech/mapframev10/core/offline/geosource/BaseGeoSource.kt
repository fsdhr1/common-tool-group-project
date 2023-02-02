package com.gradtech.mapframev10.core.offline.geosource

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.gradtech.mapframev10.core.util.CacheBoxUtil
import com.gradtech.mapframev10.core.util.FeatureUtil
import com.gradtech.mapframev10.core.util.Transformation
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NotNull
import java.io.File
import java.util.*

/**
 * @ClassName BaseGeoSource
 * @Description TODO
 * @Author: fs
 * @Date: 2022/9/16 9:57
 * @Version 2.0
 */
class BaseGeoSource : IBaseGeoSource {

    private val keyWords = arrayOf("uuid")

    /**
     * 缓存GeoJson
     */
    private var cacheBoxUtil: CacheBoxUtil? = null

    private var id: String

    /**
     * 缓存GeoJson路径
     */
    private var cachePath: String

    private var featureCollection: FeatureCollection? = null

    private var geojosnSource: GeoJsonSource? = null

    @JvmOverloads
    constructor(@NotNull id: String, @NotNull cachePath: String) {
        this.id = id
        this.cachePath = cachePath
        initDraw()
    }

    override fun setSourceUrl(cachePath: String): IBaseGeoSource {
        this.cachePath = cachePath
        return this
    }

    override fun getGeoSource(): GeoJsonSource? = geojosnSource;

    override fun initDraw() {
        if (geojosnSource == null) {
            geojosnSource = geoJsonSource(id)
        }
        if (cachePath != null) {
            cacheBoxUtil = CacheBoxUtil.get(File(cachePath), Long.MAX_VALUE, Int.MAX_VALUE)
        }
        val query = query()
        if (query != null) {
            featureCollection = FeatureCollection.fromJson(query()!!)
        }
        if (featureCollection == null) {
            featureCollection = FeatureCollection.fromFeatures(ArrayList())
        }
        if(featureCollection!!.features()!!.size>0){
            geojosnSource?.featureCollection(featureCollection!!)
        }

    }

    override fun query(): String? {
        var json: String? = null
        if (cacheBoxUtil != null) {
            json = queryCache()
        }
        return json
    }

    override fun queryCache(): String? {
        return cacheBoxUtil!!.getAsString(geojosnSource?.sourceId)
    }

    override fun queryFeatures(con: HashMap<String, Any>): List<Feature> {
        return FeatureUtil.queryFeatureCollection(featureCollection, con)
    }

    override fun queryFeaturesLike(con: Map<String, Any>): List<Feature> {
        return FeatureUtil.queryFeatureCollectionLike(featureCollection, con)
    }

    override fun queryFeature(key: String, value: String): Feature? {
        return FeatureUtil.queryFeatureFormCollection(featureCollection, key, value)
    }

    override fun insert(feature: Feature): Feature? {
        if (cacheBoxUtil != null) {
            //更新内存和缓存
            val feature = insertCache(feature)
            return feature
        }
        return null
    }

    override fun insertCache(feature: Feature): Feature {
        featureCollection!!.features()!!.add(feature)
        val json: String = Transformation.mapBoxObj2Json(featureCollection)
        cacheBoxUtil!!.put(id, json)
        geojosnSource?.featureCollection(featureCollection!!)
        return feature
    }

    override fun inserts(features: List<Feature>): FeatureCollection? {
        if (cacheBoxUtil != null) {
            //更新内存和缓存
            featureCollection!!.features()!!.addAll(features)
            val json: String = Transformation.mapBoxObj2Json(featureCollection)
            cacheBoxUtil!!.put(id, json)
            geojosnSource?.featureCollection(featureCollection!!)
        }
        return featureCollection
    }

    override fun insertsCacheUnSync(features: List<Feature>, iGcCallBack: IBaseGeoSource.IGcCallBack?) {
        // 协程构建器         主线程
        val launch = GlobalScope.launch(Dispatchers.Main) {
            //任务调度器    子线程
            val featureCollection = withContext(Dispatchers.IO) {
                inserts(features)
            }
            if (featureCollection != null) {
                iGcCallBack?.onGcCallBack(featureCollection.features())
            } else {
                iGcCallBack?.onError(Throwable())
            }

        }
      //  launch.start()

    }

    override fun delete(feature: Feature): Boolean {
        var flag = false
        if (cacheBoxUtil == null) {
            return flag
        }
        if (cacheBoxUtil != null) {
            //更新内存和缓存
            flag = deleteCache(feature)
        }
        return flag
    }

    override fun deleteCache(feature: Feature): Boolean {
        var flag = false
        val delIndex: Int = FeatureUtil.queryFeatureFormCollection(featureCollection, feature, *keyWords)
        if (delIndex != -1) {
            featureCollection!!.features()!!.removeAt(delIndex)
            this.geojosnSource?.featureCollection(featureCollection!!)
            //更新缓存数据
            val json: String = Transformation.mapBoxObj2Json(featureCollection)
            cacheBoxUtil!!.put(id, json)
            flag = true
        }
        return flag
    }

    override fun deleteFeatures(con: HashMap<String, Any>): Boolean {
        var flag = false
        if (cacheBoxUtil == null) {
            return flag
        }
        val features = queryFeatures(con)
        for (feature in features) {
            flag = deleteCache(feature)
        }
        return flag
    }

    override fun deleteAll(): Boolean {
        var res = false
        if (cacheBoxUtil == null) {
            return false
        }
        if (cacheBoxUtil != null) {
            //更新内存和缓存
            featureCollection!!.features()!!.clear()
            this.geojosnSource?.featureCollection(featureCollection!!)
            val json: String = Transformation.mapBoxObj2Json(featureCollection)
            cacheBoxUtil!!.put(id, json)
            res = true
        }
        return res
    }

    override fun update(feature: Feature?): Boolean {
        if (cacheBoxUtil == null) {
            return false
        }
        if (cacheBoxUtil != null) {
            //更新内存和缓存
            updateCache(feature)
            return true;
        }
        return false
    }

    override fun updateCache(feature: Feature?): Boolean {
        val index: Int = FeatureUtil.queryFeatureFormCollection(featureCollection, feature)
        return if (index != -1) {
            featureCollection!!.features()!![index] = feature
            this.geojosnSource?.featureCollection(featureCollection!!)
            //更新缓存数据
            val json: String = Transformation.mapBoxObj2Json(featureCollection)
            cacheBoxUtil!!.put(id, json)
            true
        } else {
            false
        }
    }


}
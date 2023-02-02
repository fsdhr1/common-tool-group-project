package com.grandtech.mapframe.core.editor.gc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grandtech.mapframe.core.editor.IGcCallBack;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.util.CacheBoxUtil;
import com.grandtech.mapframe.core.util.FeatureUtil;
import com.grandtech.mapframe.core.util.Transformation;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @ClassName BaseGeoSource
 * @Description TODO 临时图层
 * @Author: fs
 * @Date: 2021/4/20 9:16
 * @Version 2.0
 */

public  class BaseGeoSource extends GeoJsonSource implements IBaseGeoSource {

    private final String[] keyWords = {"uuid"};
    /**
     * 缓存GeoJson
     */
    private CacheBoxUtil cacheBoxUtil;
    /**
     * 缓存GeoJson路径
     */
    private String cachePath;

    private FeatureCollection featureCollection;

    public BaseGeoSource(String id) {
        super(id);
    }

    public BaseGeoSource(String id, String cachePath) {
        super(id);
        setSourceUrl(cachePath).initDraw();
    }

    public BaseGeoSource setSourceUrl(String cachePath) {
        this.cachePath = cachePath;
        return this;
    }


    public void initDraw() {
        if (cachePath != null) {
            cacheBoxUtil = CacheBoxUtil.get(new File(cachePath), Long.MAX_VALUE, Integer.MAX_VALUE);
        }
        featureCollection = FeatureCollection.fromJson(query());
        if (featureCollection == null) {
            featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());
        }
        this.setGeoJson(featureCollection);
    }

    @Override
    public String query() {
        String json = null;
        if (cacheBoxUtil != null) {
            json = queryCache();
        }
        return json;
    }


    @Override
    public String queryCache() {
        return cacheBoxUtil.getAsString(getId());
    }

    @Override
    public Feature queryFeature(String key, String value) {
        return FeatureUtil.queryFeatureFormCollection(featureCollection, key, value);
    }

    @NonNull
    @Override
    public List<Feature> querySourceFeatures(@Nullable Expression filter) {
        return super.querySourceFeatures(filter);
    }

    @NonNull
    public Feature querySourceFeature(String key, String val) {
        Feature feature = FeatureUtil.queryFeatureFormCollection(featureCollection, key, val);
        return feature;
    }

    @NonNull
    public List<Feature> queryAllSourceFeatures() {

        return featureCollection.features();
    }

    @Override
    public Feature insert(Feature feature) {
        if (cacheBoxUtil != null) {
            //更新内存和缓存
            feature = this.insertCache(feature);
        }
        return feature;
    }


    @Override
    public Feature insertCache(Feature feature) {
        featureCollection.features().add(feature);
        String json = Transformation.mapBoxObj2Json(featureCollection);
        cacheBoxUtil.put(getId(), json);
        this.setGeoJson(featureCollection);
        return feature;
    }

    @Override
    public FeatureCollection inserts(List<Feature> features) {
        if (cacheBoxUtil != null) {
            //更新内存和缓存
            final String id =getId();
            featureCollection.features().addAll(features);
            String json = Transformation.mapBoxObj2Json(featureCollection);
            cacheBoxUtil.put(id, json);
        }
        return featureCollection;
    }
    @Override
    public void insertsCacheSync(final List<Feature> features, IGcCallBack iGcCallBack) {
        String id =getId();
        Observable.create((ObservableOnSubscribe<FeatureCollection>) emitter -> {
            featureCollection.features().addAll(features);
            String json = Transformation.mapBoxObj2Json(featureCollection);
            cacheBoxUtil.put(id, json);
            emitter.onNext(featureCollection);
            emitter.onComplete();
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FeatureCollection>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull FeatureCollection featureCollection) {
                        setGeoJson(featureCollection);
                        iGcCallBack.onGcCallBack(features);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        e.fillInStackTrace();
                        iGcCallBack.onError( e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public Boolean delete(Feature feature) {
        boolean flag = false;
        if (cacheBoxUtil == null) {
            return flag;
        }
        if (cacheBoxUtil != null) {
            //更新内存和缓存
            flag = this.deleteCache(feature);
        }
        return flag;
    }

    @Override
    public Boolean deleteCache(Feature feature) {
        boolean flag = false;
        int delIndex = FeatureUtil.queryFeatureFormCollection(featureCollection, feature, keyWords);
        if (delIndex != -1) {
            featureCollection.features().remove(delIndex);
            this.setGeoJson(featureCollection);
            //更新缓存数据
            String json = Transformation.mapBoxObj2Json(featureCollection);
            cacheBoxUtil.put(getId(), json);
            flag = true;
        }
        return flag;
    }

    @Override
    public Boolean deleteFeatures(@NonNull Map<String, Object> con) {
        boolean flag = false;
        if (cacheBoxUtil == null) {
            return flag;
        }
        List<Feature> features = queryFeatures(con);
        for(Feature feature : features){
            flag = deleteCache(feature);
        }
        return flag;
    }

    @Override
    public Boolean deleteAll() {
        boolean res = false;
        if ( cacheBoxUtil == null) {
            return null;
        }
        if (cacheBoxUtil != null) {
            //更新内存和缓存
            featureCollection.features().clear();
            this.setGeoJson(featureCollection);
            String json = Transformation.mapBoxObj2Json(featureCollection);
            cacheBoxUtil.put(getId(), json);
            res = true;
        }
        return res;
    }

    @Override
    public Feature update(Feature feature) {
        if (cacheBoxUtil == null) {
            return null;
        }
        if (cacheBoxUtil != null) {
            //更新内存和缓存
            feature = updateCache(feature);
        }
        return feature;
    }

    @Override
    public Feature updateCache(Feature feature) {
        int index = FeatureUtil.queryFeatureFormCollection(featureCollection, feature);
        if (index != -1) {
            featureCollection.features().set(index, feature);
            this.setGeoJson(featureCollection);
            //更新缓存数据
            String json = Transformation.mapBoxObj2Json(featureCollection);
            cacheBoxUtil.put(getId(), json);
            return feature;
        } else {
            return null;
        }
    }

    @Override
    public List<Feature> queryFeatures(Map<String, Object> con) {
        return FeatureUtil.queryFeatureCollection(featureCollection, con);
    }

    ;
    @Override
    public List<Feature> queryFeaturesLike(Map<String, Object> con) {
        return FeatureUtil.queryFeatureCollectionLike(featureCollection, con);
    }
}

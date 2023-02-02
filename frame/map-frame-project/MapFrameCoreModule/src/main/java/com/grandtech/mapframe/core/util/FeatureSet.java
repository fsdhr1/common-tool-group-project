package com.grandtech.mapframe.core.util;



import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.GsonBuilder;
import com.grandtech.mapframe.core.rules.Rules;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.gson.BoundingBoxTypeAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zy on 2019/1/10.
 * featureSet里的数据必须是同一个sourceId的
 */

public class FeatureSet implements GeoJson, Serializable, Rules {

    private String layerId;

    private String sourceId;

    private String geoJson;
    private FeatureCollection featureCollection;

    private String[] keyWords;

    public FeatureSet() {
    }

    private FeatureSet(String geoJson) {
        featureCollection = FeatureCollection.fromJson(geoJson);
      //  this.geoJson = geoJson;
    }
    private FeatureSet(FeatureCollection featureCollection) {
       this.featureCollection = featureCollection;
        //  this.geoJson = geoJson;
    }
    public String getGeoJson() {
        return toJson(featureCollection);
    }

    public FeatureSet setGeoJson(String geoJson) {
        featureCollection = FeatureCollection.fromJson(geoJson);
        //this.geoJson = geoJson;
        return this;
    }

    /**
     * geoJson赋值，防止网络传输后端无法解析featureCollection
     * @return
     */
    public FeatureSet jsonSerializable(){
        geoJson = toJson(featureCollection);
        return this;
    }

    public String getLayerId() {
        return layerId;
    }

    public FeatureSet setLayerId(String layerId) {
        this.layerId = layerId;
        return this;
    }

    public String getSourceId() {
        return sourceId;
    }

    public FeatureSet setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public String[] getKeyWords() {
        return keyWords;
    }

    public FeatureSet setKeyWords(String[] keyWords) {
        this.keyWords = keyWords;
        return this;
    }

    @NonNull
    @Override
    public String type() {
        return null;
    }

    @Nullable
    @Override
    public BoundingBox bbox() {
        return null;
    }

    @Nullable
    public List<Feature> features() {
     //   FeatureCollection featureCollection = FeatureCollection.fromJson(geoJson);
        return featureCollection == null ? null : featureCollection.features();
    }

    @Override
    public String toJson() {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(Point.class, new CustomPointSerializer(9));
        gson.registerTypeAdapter(BoundingBox.class, new BoundingBoxTypeAdapter());
        return gson.create().toJson(this);
    }

    /**
     * 自定义FeatureCollection 转json 防止坐标精度丢失
     *
     * @param collection
     * @return
     */
    public static String toJson(FeatureCollection collection) {
        if(collection == null ){
            return null;
        }
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(Point.class, new CustomPointSerializer(9));
        gson.registerTypeAdapter(BoundingBox.class, new BoundingBoxTypeAdapter());
        return gson.create().toJson(collection);
    }

    public FeatureSet append(Feature feature) {
        if(featureCollection == null){
            featureCollection = FeatureCollection.fromFeature(feature);
        }else {
            featureCollection.features().add(feature);
        }
        return this;
    }

    public FeatureSet append(Feature[] feature) {
        if(featureCollection == null){
            featureCollection = FeatureCollection.fromFeatures(feature);
        }else {
            featureCollection.features().addAll(Arrays.asList(feature));
        }
        return this;
    }

    public FeatureSet append(List<Feature> feature) {
        if(featureCollection == null){
            featureCollection = FeatureCollection.fromFeatures(feature);
        }else {
            featureCollection.features().addAll(feature);
        }
        return this;
    }

    public FeatureSet append(String json) {
        if(featureCollection == null){
            featureCollection = FeatureCollection.fromJson(json);
        }else {
            FeatureCollection append = FeatureCollection.fromJson(json);
            featureCollection.features().addAll(append.features());
        }
        return this;
    }

    //----------------------------------------------------------------------------------------------

    public FeatureSet replaceAll(Feature feature) {
        featureCollection = FeatureCollection.fromFeature(feature);
        return this;
    }

    public FeatureSet replaceAll(Feature[] feature) {
        featureCollection = FeatureCollection.fromFeatures(feature);
        return this;
    }

    public FeatureSet replaceAll(List<Feature> feature) {
        featureCollection = FeatureCollection.fromFeatures(feature);
        return this;
    }

    public FeatureSet replaceAll(String json) {
        featureCollection = FeatureCollection.fromJson(json);
        return this;
    }

    public FeatureSet clearAll() {
        featureCollection.features().clear();
        return this;
    }

    @Override
    public FeatureSet clone() {
        FeatureSet featureSet = new FeatureSet(featureCollection);
        featureSet.setLayerId(layerId);
        featureSet.setKeyWords(keyWords);
        featureSet.setSourceId(sourceId);
        return featureSet;
    }

    /**
     * *********************************************************************************************
     */

    public static FeatureSet fromJson(@NonNull String json) {
        FeatureCollection featureCollection = FeatureCollection.fromJson(json);
        if (featureCollection == null) {
            return null;
        }
        FeatureSet featureSet = new FeatureSet(featureCollection);
        return featureSet;
    }

    public static FeatureSet fromFeatures(@NonNull Feature[] features) {
        List<Feature> list = Arrays.asList(features);
        List arrList = new ArrayList(list);
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(arrList);
        FeatureSet featureSet = new FeatureSet(featureCollection);
        return featureSet;
    }

    public static FeatureSet fromFeatures(@NonNull List<Feature> features) {
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);
        if (featureCollection == null) {
            return null;
        }
        FeatureSet featureSet = new FeatureSet(featureCollection);
        return featureSet;
    }


    public static FeatureSet fromFeature(@NonNull Feature feature) {
        List<Feature> features = new ArrayList<>();
        features.add(feature);
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);
        if (featureCollection == null) {
            return null;
        }
        FeatureSet featureSet = new FeatureSet(featureCollection);
        return featureSet;
    }

    /**
     * *********************************************************************************************
     */

    public static List<FeatureSet> convert2FeatureSets(Map<String, FeatureSet> featureSetMap) {
        if (featureSetMap == null) {
            return null;
        }
        List<FeatureSet> list = new ArrayList<>();
        for (String name : featureSetMap.keySet()) {
            list.add(featureSetMap.get(name));
        }
        return list;
    }

    public static Map<String, FeatureSet> convert2MapFeatureSet(FeatureSet featureSet) {
        if (featureSet == null) {
            return null;
        }
        Map<String, FeatureSet> featureSetMap = new HashMap<>();
        featureSetMap.put(featureSet.getLayerId(), featureSet);
        return featureSetMap;
    }

    public static Map<String, FeatureSet> convert2MapFeatureSet(List<FeatureSet> featureSets) {
        if (featureSets == null) {
            return null;
        }
        Map<String, FeatureSet> featureSetMap = new HashMap<>();
        FeatureSet featureSet;
        for (int i = 0, len = featureSets.size(); i < len; i++) {
            featureSet = featureSets.get(i);
            if (featureSet == null) {
                continue;
            }
            featureSetMap.put(featureSet.getLayerId(), featureSet);
        }
        return featureSetMap;
    }

    public static Map<String, FeatureSet> convert2MapFeatureSet(Map<String, List<Feature>> featureMap) {
        if (featureMap == null) {
            return null;
        }
        List<Feature> features;
        FeatureSet featureSet;
        Map<String, FeatureSet> res = new HashMap<>();
        for (String layerId : featureMap.keySet()) {
            features = featureMap.get(layerId);
            featureSet = FeatureSet.fromFeatures(features);
            res.put(layerId, featureSet);
        }
        return res;
    }

    public static List<Feature> convert2Features(List<FeatureSet> featureSets) {
        FeatureSet featureSet;
        List<Feature> features = new ArrayList<>();
        for (int i = 0, len = featureSets.size(); i < len; i++) {
            featureSet = featureSets.get(i);
            features.addAll(featureSet.features());
        }
        return features;
    }

    public static List<Feature> convert2Features(Map<String, FeatureSet> featureSetMap) {
        List<FeatureSet> temp = convert2FeatureSets(featureSetMap);
        return convert2Features(temp);
    }

    public static List<Geometry> convert2Geometries(Map<String, FeatureSet> featureSetMap) {
        List<Feature> temp = convert2Features(featureSetMap);
        if (temp == null) {
            return null;
        }
        List<Geometry> geometries = new ArrayList<>();
        for (int i = 0; i < temp.size(); i++) {
            geometries.add(temp.get(i).geometry());
        }
        return geometries;
    }


}

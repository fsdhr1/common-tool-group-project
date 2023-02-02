package com.gradtech.mapframev10.core.snapshot.bean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.gradtech.mapframev10.core.rules.Rules;
import com.gradtech.mapframev10.core.util.FeatureSet;
import com.gradtech.mapframev10.core.util.GsonFactory;
import com.gradtech.mapframev10.core.util.TransformationForTurf;
import com.mapbox.geojson.Feature;
import com.mapbox.maps.CoordinateBounds;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SnapParam
 * @Description TODO 快照实体
 * @Author: fs
 * @Date: 2021/7/29 13:20
 * @Version 2.0
 */

public class SnapParam implements Serializable, Rules {
    /**
     * 默认添加的数据，默认添加到dksource
     */
    private Feature feature;
    /**
     * 存储路径
     */
    private String filePath;
    /*
    *水印
     */
    private List<String> waterMark;
    /**
     * 要显示在快照上的数据集合FeatureSet的sourceId代表要添加到的source，前提是source必须是GeoJsonSource
     */
    private List<FeatureSet> featureSets;

    private CoordinateBounds latLngBounds;

    private String base64;

    public SnapParam() {}

    public SnapParam(Feature feature, String filePath, List<String> waterMark) {
        this.feature = feature;
        this.filePath = filePath;
        this.waterMark = waterMark;

    }

    public SnapParam(Feature feature, List<FeatureSet> featureSets,String filePath, List<String> waterMark) {
        this.feature = feature;
        this.filePath = filePath;
        this.waterMark = waterMark;
        this.featureSets = featureSets;
    }
    public SnapParam(List<FeatureSet> featureSets,String filePath, List<String> waterMark) {
        this.filePath = filePath;
        this.waterMark = waterMark;
        this.featureSets = featureSets;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<String> getWaterMark() {
        return waterMark;
    }

    public void setWaterMark(List<String> waterMark) {
        this.waterMark = waterMark;
    }

    public List<FeatureSet> getFeatureSets() {
        return featureSets;
    }

    public void setFeatureSets(List<FeatureSet> featureSets) {
        this.featureSets = featureSets;
    }

    public void setLatLngBounds(CoordinateBounds latLngBounds) {
        this.latLngBounds = latLngBounds;
    }

    /**
     * 获取快照需要设置的bounds
     * @return
     */
    public CoordinateBounds getLatLngBounds(){
        if(latLngBounds == null){
            List<Feature> features = new ArrayList<>();
            if(getFeature() != null){
                features.add(getFeature());
            }
            if(getFeatureSets()!=null){
                for(FeatureSet featureSet :featureSets){
                    features.addAll(featureSet.features());
                }
            }
            return TransformationForTurf.features2BoxLatLngBounds(features);
        }else {
            return latLngBounds;
        }
    }

    public String toJson(){
       return GsonFactory.getFactory().getComMapBoxGson().toJson(this);
    }

    public static SnapParam fromJson(String snapParamJson){
        try{
            JsonParser jsonParser = new JsonParser();
            JsonObject snapParamJsonObj = jsonParser.parse(snapParamJson).getAsJsonObject();
            if(snapParamJsonObj == null) {
                return null;
            }
            SnapParam snapParam = new SnapParam();
            if(snapParamJsonObj.has("feature")){
               Feature feature =  Feature.fromJson(snapParamJsonObj.getAsJsonObject("feature").toString());
                snapParam.setFeature(feature);
            }
            if(snapParamJsonObj.has("filePath")){
                snapParam.setFilePath(snapParamJsonObj.getAsJsonPrimitive("filePath").getAsString());
            }
            if(snapParamJsonObj.has("waterMark")){
                JsonArray jsonArray = snapParamJsonObj.getAsJsonArray("waterMark");
                if(jsonArray!=null){
                    List<String> waters = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        waters.add(jsonElement.getAsString());
                    }
                    snapParam.setWaterMark(waters);
                }
            }
            if(snapParamJsonObj.has("featureSets")){
                JsonArray jsonArray = snapParamJsonObj.getAsJsonArray("featureSets");
                if(jsonArray!=null){
                    List<FeatureSet> featureSets = new ArrayList<>();
                    for (JsonElement jsonElement : jsonArray) {
                        JsonElement JsonElementF= jsonElement.getAsJsonObject().get("featureCollection");
                        if(JsonElementF == null){
                            continue;
                        }
                        FeatureSet featureSet = FeatureSet.fromJson(JsonElementF.toString());
                        if(featureSet == null){
                            continue;
                        }
                        JsonElementF = jsonElement.getAsJsonObject().get("sourceId");
                        if(JsonElementF == null){
                            continue;
                        }
                        featureSet.setSourceId(JsonElementF.getAsString());
                        JsonElementF = jsonElement.getAsJsonObject().get("layerId");
                        if(JsonElementF == null){
                            continue;
                        }
                        featureSet.setLayerId(JsonElementF.getAsString());
                        featureSets.add(featureSet);
                    }
                    if(featureSets.size()>0){
                        snapParam.setFeatureSets(featureSets);
                    }

                }
            }
            return snapParam;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}

package com.gradtech.mapframev10.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zy on 2018/7/27.
 * feature与实体的关系
 */

public class FeatureUtil {

    public static final String firstId = "uuid";
    public static final String secondId = "objectid";
    public static final String thirdId = "properties";
    private static LinkedHashMap<String,String> customIds = new LinkedHashMap<>();
    public static boolean hasAttributes(Feature feature, String... attrName) {
        boolean res = true;
        if (feature == null || attrName == null || attrName.length == 0) {
            return false;
        }
        for (int i = 0; i < attrName.length; i++) {
            if (!feature.hasProperty(attrName[i])) {
                res = false;
            }
        }
        return res;
    }

    public static String getCustomId(String layerid) {
        return customIds.get(layerid);
    }

    public static void setCustomId(LinkedHashMap<String,String> customIdMap) {
        if(customIdMap!=null){
            FeatureUtil.customIds = customIdMap;
        }
    }

    public static Object getFeatureAttributeAsObj(Feature feature, String attrName) {
        if (feature == null || attrName == null || !feature.hasProperty(attrName)) {
            return null;
        }
        JsonElement jsonElement = feature.getProperty(attrName);
        if (jsonElement == null) {
            return null;
        }
        if (jsonElement.isJsonNull()) {
            return null;
        }
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isString()) {
                return jsonPrimitive.getAsString();
            } else if (jsonPrimitive.isNumber()) {
                return jsonPrimitive.getAsNumber();
            } else if (jsonPrimitive.isBoolean()) {
                return jsonPrimitive.getAsBoolean();
            }
        }
        return null;
    }



    public static String getIdField(String layerid,Feature feature) {
        String customId = layerid == null||layerid.equals("")?null:customIds.get(layerid);
        if(!ObjectUtil.isEmpty(customId)&&feature.hasProperty(customId)&&!ObjectUtil.isEmpty(feature.getStringProperty(customId).trim())){
            return customId;
        }else if (feature.hasProperty(firstId)&&!ObjectUtil.isEmpty(feature.getStringProperty(firstId).trim())) {
            return firstId;
        } else if (feature.hasProperty(secondId)) {
            return secondId;
        } else  {
            return null;
        }
    }

    public static String getFeatureId(String layerid,Feature feature) {
        String idField = getIdField(layerid,feature);
        if (idField != null) {
            return feature.getStringProperty(idField);
        }
        return null;
    }

    public static boolean featureEqual(Feature f1, Feature f2) {
        try {
            String idf1 = getFeatureId(null,f1);
            String idf2 = getFeatureId(null,f2);
            if (idf1 != null && idf2 != null) {
                return idf1.equals(idf2);
            }
            String json1 = f1.toJson();
            String json2 = f2.toJson();
            if (json1.equals(json2)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean featureEqual(Feature f1, Feature f2, Map<String, String> mapAttr) {
        if (mapAttr == null) {
            return featureEqual(f1, f2);
        }
        Object o1, o2;
        String key2;
        Boolean equal = true;
        for (String key1 : mapAttr.keySet()) {
            key2 = mapAttr.get(key1);
            o1 = getFeatureAttributeAsObj(f1, key1);
            o2 = getFeatureAttributeAsObj(f2, key2);
            if (!ObjectUtil.baseTypeIsEqual(o1, o2)) {
                equal = false;
                break;
            }
        }
        return equal;
    }

    public static boolean featureEqual(Feature f1, Feature f2, String... attrs) {
        if (attrs == null || attrs.length == 0) {
            return featureEqual(f1, f2);
        }
        Object o1, o2;
        Boolean equal = true;
        for (String key : attrs) {
            o1 = getFeatureAttributeAsObj(f1, key);
            o2 = getFeatureAttributeAsObj(f2, key);
            if (!ObjectUtil.baseTypeIsEqual(o1, o2)) {
                equal = false;
                break;
            }
        }
        return equal;
    }

    public static int findFeature(List<Feature> features1, Feature f2, String keyWord) {
        if(features1 == null){
            return -1;
        }
        for (int i = 0; i < features1.size(); i++) {
            Feature f1 = features1.get(i);
            boolean equal = featureEqual(f1, f2, keyWord);
            if (equal) {
                return i;
            }
        }
        return -1;
    }
    public static int findFeature(List<Feature> features1, Feature f2) {
        for (int i = 0; i < features1.size(); i++) {
            Feature f1 = features1.get(i);
            boolean equal =f1.equals(f2);
            if (equal) {
                return i;
            }
        }
        return -1;
    }
    public static List<Geometry> features2Geometries(List<Feature> features) {
        if (features == null) {
            return null;
        }
        List<Geometry> geometries = new ArrayList<>();
        for (Feature f : features) {
            geometries.add(f.geometry());
        }
        return geometries;
    }


    public static double getFeatureArea(Feature feature, String mjField) {
        Double area = 0.00d;
        try {
            Number _mj = getFeatureAttributeAsT(feature, mjField);
            if (_mj != null) {
                return _mj.doubleValue();
            }
            area = Transformation.compute84GeoJsonArea(feature.geometry());
            return area;
        } catch (Exception e) {
            return area;
        }
    }


    public static <T> T getFeatureAttributeAsT(Feature feature, String attrName) {
        if (feature == null || attrName == null || !feature.hasProperty(attrName)) {
            return null;
        }
        JsonElement jsonElement = feature.getProperty(attrName);
        if (jsonElement == null) {
            return null;
        }
        if (jsonElement.isJsonNull()) {
            return null;
        }
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isString()) {
                return (T) jsonPrimitive.getAsString();
            } else if (jsonPrimitive.isNumber()) {
                return (T) jsonPrimitive.getAsNumber();
            } else if (jsonPrimitive.isBoolean()) {
                boolean b = jsonPrimitive.getAsBoolean();
                return (T) new Boolean(b);
            }
        }
        return null;
    }

    /**
     * 根据属性查询featureCollection
     * @param featureCollection
     * @param key
     * @param value
     * @return
     */
    public static Feature queryFeatureFormCollection(FeatureCollection featureCollection, String key, String value) {
        if (featureCollection == null || key.equals("") || value.equals("")) {
            return null;
        }
        Iterator<Feature> featureIterator = featureCollection.features().iterator();
        Feature feature;
        while (featureIterator.hasNext()) {
            feature = featureIterator.next();
            if (feature.hasProperty(key)) {
                if (feature.getStringProperty(key).equals(value)) {
                    return feature;
                }
            }
        }
        return null;
    }

    public static int queryFeatureFormCollection(FeatureCollection featureCollection, Feature condition, String... keyWords) {
        if (featureCollection == null || condition == null) {
            return -1;
        }
        Iterator<Feature> featureIterator = featureCollection.features().iterator();
        Feature feature;
        int index = -1;
        boolean equal;
        while (featureIterator.hasNext()) {
            index++;
            feature = featureIterator.next();
            equal = FeatureUtil.featureEqual(feature, condition, keyWords);
            if (equal) {
                return index;
            }
        }
        return -1;
    }

    public static List<Feature> queryFeatureCollection(FeatureCollection featureCollection, Map<String, Object> con) {
        if (featureCollection == null || con == null || con.size() == 0) {
            return null;
        }
        Iterator<Feature> featureIterator = featureCollection.features().iterator();
        Feature feature;
        List<Feature> list = new ArrayList<>();
        while (featureIterator.hasNext()) {
            feature = featureIterator.next();
            boolean isEqual = false;
            for (String key : con.keySet()) {
                if (feature.hasProperty(key)) {
                    if ((feature.getStringProperty(key)).equals(con.get(key))) {
                        isEqual = true;
                        continue;
                    }
                    isEqual = false;
                    break;
                } else {
                    isEqual = false;
                    break;
                }
            }
            if (isEqual) {
                list.add(feature);
            }
        }
        return list;
    }

    public static List<Feature> queryFeatureCollectionLike(FeatureCollection featureCollection, Map<String, Object> con) {
        if (featureCollection == null || con == null || con.size() == 0) {
            return null;
        }
        Iterator<Feature> featureIterator = featureCollection.features().iterator();
        Feature feature;
        List<Feature> list = new ArrayList<>();
        while (featureIterator.hasNext()) {
            feature = featureIterator.next();
            boolean isEqual = false;
            for (String key : con.keySet()) {
                if (feature.hasProperty(key)) {
                    if ((feature.getStringProperty(key)).contains(con.get(key).toString())) {
                        isEqual = true;
                        continue;
                    }
                    isEqual = false;
                    break;
                } else {
                    isEqual = false;
                    break;
                }
            }
            if (isEqual) {
                list.add(feature);
            }
        }
        return list;
    }

}

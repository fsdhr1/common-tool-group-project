package com.grandtech.mapframe.core.util;

import com.grandtech.mapframe.core.rules.Rules;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.turf.TurfMeasurement;
import com.vividsolutions.jts.geom.Envelope;

import java.util.List;

/**
 * @ClassName TurfTransformation
 * @Description TODO 基于turf的一些空间数据校验和转换
 * @Author: fs
 * @Date: 2021/7/30 9:23
 * @Version 2.0
 */
public class TransformationForTurf implements Rules {

    /**
     * 获取图形的LatLngBounds
     *  a double array defining the bounding box in this order {@code [minX, minY, maxX, maxY]}
     * @param feature
     * @return
     */
    public static LatLngBounds feature2LatLngBounds(Feature feature) {
        try {
            double[] doubles = TurfMeasurement.bbox(feature);
            LatLngBounds latLngBounds = LatLngBounds.from(doubles[3],  doubles[2], doubles[1],doubles[0]);
            return latLngBounds;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取要素集合最大的外包矩形
     * @param features
     * @return
     */
    public static LatLngBounds features2BoxLatLngBounds(List<Feature> features) {
        if (features == null || features.size() == 0) {
            return null;
        }
        try {
            double[] doubles = TurfMeasurement.bbox(FeatureCollection.fromFeatures(features));
            LatLngBounds latLngBounds = LatLngBounds.from(doubles[3],  doubles[2], doubles[1],doubles[0]);
            return latLngBounds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

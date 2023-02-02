package com.grandtech.mapframe.core.sketch.geometry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grandtech.mapframe.core.sketch.SketchUtil;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiPoint;
import com.mapbox.geojson.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zy
 * @date 2018/12/20
 */

public class SketchMultiPoint implements Geometry, Serializable {
    List<Point> points;

    public SketchMultiPoint(List<Point> points) {
        //实现深copy深
        this.points = SketchUtil.deepCopy1DimensionList(points);
    }


    @NonNull
    @Override
    public String type() {
        return "MultiPoint";
    }

    @Override
    public String toJson() {
        return null;
    }

    @Nullable
    @Override
    public BoundingBox bbox() {
        return null;
    }

    /**
     * 深copy
     */
    @Nullable

    public List<Point> coordinates() {
        return this.points;
    }


    /**
     * 点集合
     */
    private List<SketchPoint> sketchPoints;

    public List<SketchPoint> getSketchPoints() {
        return sketchPoints;
    }

    private void setSketchPoints(List<SketchPoint> sketchPoints) {
        this.sketchPoints = sketchPoints;
    }

    public static SketchMultiPoint fromLngLats(@NonNull List<Point> points) {
        SketchMultiPoint sketchMultiPoint = new SketchMultiPoint(points);
        SketchPoint sketchPoint;
        List<SketchPoint> temp = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            sketchPoint = SketchPoint.fromLngLat(points.get(i), SketchPoint.V_POINT);
            temp.add(sketchPoint);
        }
        sketchMultiPoint.setSketchPoints(temp);
        return sketchMultiPoint;
    }

    public static SketchMultiPoint fromGeometry(@NonNull Geometry geometry) {
        List<Point> points = ((MultiPoint) geometry).coordinates();
        return fromLngLats(points);
    }
}

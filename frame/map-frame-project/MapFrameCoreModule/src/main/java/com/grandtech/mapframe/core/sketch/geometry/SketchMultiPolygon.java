package com.grandtech.mapframe.core.sketch.geometry;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grandtech.mapframe.core.sketch.SketchUtil;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2018/12/20.
 */

public class SketchMultiPolygon implements Geometry, Serializable {

    List<List<List<Point>>> points;

    int fillColor;
    int strokeColor;
    float alpha;
    float width;

    public SketchMultiPolygon(List<List<List<Point>>> points) {
        this.points = SketchUtil.deepCopy3DimensionList(points);
    }


    private List<SketchPolygon> sketchPolygons;

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public List<SketchPolygon> getSketchPolygons() {
        return sketchPolygons;
    }

    private void setSketchPolygons(List<SketchPolygon> sketchPolygons) {
        this.sketchPolygons = sketchPolygons;
    }

    @Override
    @NonNull
    public String type() {
        return "MultiPolygon";
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

    @NonNull

    public List<List<List<Point>>> coordinates() {
        return this.points;
    }


    public static SketchMultiPolygon fromLngLats(@NonNull List<List<List<Point>>> points, int fillColor, int strokeColor, float alpha, float width) {
        SketchMultiPolygon sketchMultiPolygon = new SketchMultiPolygon(points);
        sketchMultiPolygon.setFillColor(fillColor);
        sketchMultiPolygon.setStrokeColor(strokeColor);
        sketchMultiPolygon.setAlpha(alpha);
        sketchMultiPolygon.setWidth(fillColor);
        SketchPolygon sketchPolygon;
        List<SketchPolygon> temp = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            sketchPolygon = SketchPolygon.fromLngLats(points.get(i), fillColor, strokeColor, alpha, width);
            temp.add(sketchPolygon);
        }
        sketchMultiPolygon.setSketchPolygons(temp);
        return sketchMultiPolygon;
    }

    public static SketchMultiPolygon fromGeometry(@NonNull Geometry geometry, int fillColor, int strokeColor, float alpha, float width) {
        //List<List<List<Point>>> points = ((MultiPolygon) geometry).coordinates();
        List<List<List<Point>>> points = ((MultiPolygon) geometry).coordinates();
        points = open3DimensionRing(points);
        return fromLngLats(points, fillColor, strokeColor, alpha, width);
    }


    private static List<List<Point>> open2DimensionRing(List<List<Point>> lists) {
        try {
            List<Point> temp;
            List<List<Point>> res = new ArrayList<>();
            for (int i = 0; i < lists.size(); i++) {
                temp = new ArrayList<>();
                temp.addAll(lists.get(i));
                if (temp.get(0).toJson().equals(temp.get(temp.size() - 1).toJson())) {
                    temp.remove(temp.size() - 1);
                }
                res.add(temp);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<List<List<Point>>> open3DimensionRing(List<List<List<Point>>> lists) {
        List<List<Point>> temp;
        List<List<List<Point>>> res = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            temp = open2DimensionRing(lists.get(i));
            if (temp != null) {
                res.add(temp);
            }
        }
        return res;
    }


    public void drawToMap(MapboxMap mapboxMap) {
        List<SketchPolygon> sketchPolygons = getSketchPolygons();
        if (sketchPolygons == null) {
            return;
        }
        for (SketchPolygon sketchPolygon : sketchPolygons) {
            sketchPolygon.drawToMap(mapboxMap);
        }
    }

    public void clearFromMap(MapboxMap mapboxMap) {
        List<SketchPolygon> sketchPolygons = getSketchPolygons();
        if (sketchPolygons == null) {
            return;
        }
        for (SketchPolygon sketchPolygon : sketchPolygons) {
            sketchPolygon.clearFromMap(mapboxMap);
        }
    }
}

package com.grandtech.mapframe.core.sketch.geometry;

import android.graphics.PointF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grandtech.mapframe.core.sketch.SketchUtil;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiLineString;
import com.mapbox.geojson.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2018/12/20.
 */

public class SketchMultiLineString implements Geometry, Serializable {

    List<List<Point>> points;
    List<List<PointF>> pointFS;
    int strokeColor;
    float alpha;
    float width;

    List<SketchLineString> sketchLineStrings;

    public SketchMultiLineString(List<List<Point>> points) {
        //实现深copy深
        this.points = SketchUtil.deepCopy2DimensionList(points);
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

    public List<SketchLineString> getSketchLineStrings() {
        return sketchLineStrings;
    }

    public void setSketchLineStrings(List<SketchLineString> sketchLineStrings) {
        this.sketchLineStrings = sketchLineStrings;
    }

    @Override
    @NonNull
    public String type() {
        return "MultiLineString";
    }

    @Override
    public String toJson() {
        return null;
    }

    @Override
    @Nullable
    public BoundingBox bbox() {
        return null;
    }

    @NonNull
    public List<List<Point>> coordinates() {
        return this.points;
    }


    public static SketchMultiLineString fromLngLats(@NonNull List<List<Point>> points, int strokeColor, float alpha, float width) {

        SketchMultiLineString sketchMultiLineString = new SketchMultiLineString(points);
        sketchMultiLineString.setStrokeColor(strokeColor);
        sketchMultiLineString.setAlpha(alpha);
        sketchMultiLineString.setWidth(width);

        SketchLineString sketchLineString;
        List<SketchLineString> temp = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            sketchLineString = SketchLineString.fromLngLats(points.get(i), strokeColor, alpha, width);
            temp.add(sketchLineString);
        }
        sketchMultiLineString.setSketchLineStrings(temp);
        return sketchMultiLineString;
    }

    public static SketchMultiLineString fromGeometry(@NonNull Geometry geometry, int strokeColor, float alpha, float width) {
        List<List<Point>> points = ((MultiLineString) geometry).coordinates();
        return fromLngLats(points, strokeColor, alpha, width);
    }
}

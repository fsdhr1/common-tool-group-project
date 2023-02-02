package com.grandtech.mapframe.core.sketch.geometry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grandtech.mapframe.core.sketch.SketchUtil;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2018/12/20.
 */

public class SketchPolygon implements Geometry, Serializable {

    List<List<Point>> points;

    int fillColor;
    int strokeColor;
    float alpha;
    float width;

    List<SketchLineString> sketchLineStrings;

    PolygonOptions polygonOptions;

    private SketchPolygon(List<List<Point>> points) {
        this.points = SketchUtil.deepCopy2DimensionList(points);
    }

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

    public List<SketchLineString> getSketchLineStrings() {
        return sketchLineStrings;
    }

    private void setSketchLineStrings(List<SketchLineString> sketchLineStrings) {
        this.sketchLineStrings = sketchLineStrings;
    }

    public PolygonOptions getPolygonOptions() {
        return polygonOptions;
    }

    public void setPolygonOptions(PolygonOptions polygonOptions) {
        this.polygonOptions = polygonOptions;
    }


    @NonNull
    @Override
    public String type() {
        return "Polygon";
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

    @Nullable

    public List<List<Point>> coordinates() {
        return this.points;
    }

    public void drawToMap(MapboxMap mapboxMap) {
        List<SketchLineString> sketchLineStrings = getSketchLineStrings();
        List<SketchPoint> sketchPoints;
        for (SketchLineString sketchLineString : sketchLineStrings) {
            mapboxMap.addPolyline(sketchLineString.getPolylineOptions());
            sketchPoints = sketchLineString.getVSketchPoints();
            if (sketchPoints != null) {
                for (SketchPoint sketchPoint : sketchPoints) {
                    mapboxMap.addMarker(sketchPoint.getSymbolType());
                }
            }
            sketchPoints = sketchLineString.getMSketchPoints();
            if (sketchPoints != null) {
                for (SketchPoint sketchPoint : sketchPoints) {
                    mapboxMap.addMarker(sketchPoint.getSymbolType());
                }
            }
        }
        mapboxMap.addPolygon(getPolygonOptions());
    }

    public void clearFromMap(MapboxMap mapboxMap) {
        List<SketchLineString> sketchLineStrings = getSketchLineStrings();
        List<SketchPoint> sketchPoints;
        for (SketchLineString sketchLineString : sketchLineStrings) {
            mapboxMap.removePolyline(sketchLineString.getPolylineOptions().getPolyline());
            sketchPoints = sketchLineString.getVSketchPoints();
            if (sketchPoints != null) {
                for (SketchPoint sketchPoint : sketchPoints) {
                    mapboxMap.removeMarker(sketchPoint.getSymbolType().getMarker());
                }
            }
            sketchPoints = sketchLineString.getMSketchPoints();
            if (sketchPoints != null) {
                for (SketchPoint sketchPoint : sketchPoints) {
                    mapboxMap.removeMarker(sketchPoint.getSymbolType().getMarker());
                }
            }
        }
        mapboxMap.removePolygon(getPolygonOptions().getPolygon());
    }


    public static SketchPolygon fromLngLats(@NonNull List<List<Point>> points, int fillColor, int strokeColor, float alpha, float width) {
        SketchPolygon sketchPolygon = new SketchPolygon(points);
        sketchPolygon.setFillColor(fillColor);
        sketchPolygon.setStrokeColor(strokeColor);
        sketchPolygon.setAlpha(alpha);
        sketchPolygon.setWidth(width);

        PolygonOptions polygonOptions = null;
        for (int i = 0; i < points.size(); i++) {
            List<LatLng> temp = SketchUtil.listPoint2ListLatLng(points.get(i));
            if (i == 0) {
                polygonOptions = new PolygonOptions().addAll(temp).fillColor(fillColor).strokeColor(strokeColor).alpha(alpha);
            } else {
                polygonOptions.addHole(temp);
            }
        }
        sketchPolygon.setPolygonOptions(polygonOptions);

        SketchLineString sketchLineString;
        List<SketchLineString> temp = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            sketchLineString = SketchLineString.fromLngLats(points.get(i), true, strokeColor, alpha, width);
            if (sketchLineString != null) {
                temp.add(sketchLineString);
            }
        }
        sketchPolygon.setSketchLineStrings(temp);
        return sketchPolygon;
    }

    public static SketchPolygon fromGeometry(@NonNull Geometry geometry, int fillColor, int strokeColor, float alpha, float width) {
        if(geometry instanceof Polygon){
            List<List<Point>> points = ((Polygon) geometry).coordinates();
            points = open2DimensionRing(points);
            return fromLngLats(points, fillColor, strokeColor, alpha, width);
        }else if(geometry instanceof MultiPolygon){
            List<List<List<Point>>> coordinates = ((MultiPolygon) geometry).coordinates();
            List<List<Point>> lists = new ArrayList<>();
            for (List<List<Point>> coordinate : coordinates) {
                lists.addAll(open2DimensionRing(coordinate));
            }
            return  fromLngLats(lists, fillColor, strokeColor, alpha, width);
        }
     return null;
    }

    private static List<List<Point>> open2DimensionRing(List<List<Point>> lists) {
        List<Point> temp;
        List<List<Point>> res = new ArrayList<>();
        for (int i = 0, len = lists.size(); i < len; i++) {
            temp = new ArrayList<>();
            temp.addAll(lists.get(i));
            if (temp.get(0).toJson().equals(temp.get(temp.size() - 1).toJson())) {
                temp.remove(temp.size() - 1);
            }
            res.add(temp);
        }
        return res;
    }
}

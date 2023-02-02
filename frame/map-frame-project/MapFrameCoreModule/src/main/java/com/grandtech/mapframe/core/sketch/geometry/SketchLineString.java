package com.grandtech.mapframe.core.sketch.geometry;

import android.graphics.PointF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grandtech.mapframe.core.sketch.SketchUtil;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2018/12/20.
 */

public class SketchLineString implements Geometry, Serializable {

    List<Point> points;
    List<PointF> pointFS;
    boolean isRing = false;
    int strokeColor;
    float alpha;
    float width;

    List<SketchPoint> vSketchPoints;

    List<SketchPoint> mSketchPoints;
    /**
     * 线样式
     */
    PolylineOptions polylineOptions;

    public SketchLineString(List<Point> points) {
        //深copy
        this.points = SketchUtil.deepCopy1DimensionList(points);
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

    @Override
    @NonNull
    public String type() {
        return "LineString";
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
    public List<Point> coordinates() {
        return this.points;
    }


    public List<SketchPoint> getVSketchPoints() {
        return vSketchPoints;
    }

    public void setVSketchPoints(List<SketchPoint> vSketchPoints) {
        this.vSketchPoints = vSketchPoints;
    }

    public List<SketchPoint> getMSketchPoints() {
        return mSketchPoints;
    }

    public void setMSketchPoints(List<SketchPoint> mSketchPoints) {
        this.mSketchPoints = mSketchPoints;
    }

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    private void setPolylineOptions(PolylineOptions polylineOptions) {
        this.polylineOptions = polylineOptions;
    }

    public boolean isRing() {
        return isRing;
    }

    public void setRing(boolean ring) {
        isRing = ring;
    }

    public void drawToMap(MapboxMap mapboxMap) {
        mapboxMap.addPolyline(getPolylineOptions());
        List<SketchPoint> sketchPoints;
        sketchPoints = getVSketchPoints();
        if (sketchPoints != null) {
            for (SketchPoint sketchPoint : sketchPoints) {
                mapboxMap.addMarker(sketchPoint.getSymbolType());
            }
        }
        sketchPoints = getMSketchPoints();
        if (sketchPoints != null) {
            for (SketchPoint sketchPoint : sketchPoints) {
                mapboxMap.addMarker(sketchPoint.getSymbolType());
            }
        }
    }

    public void clearFromMap(MapboxMap mapboxMap) {
        mapboxMap.removePolyline(getPolylineOptions().getPolyline());
        List<SketchPoint> sketchPoints;
        sketchPoints = getVSketchPoints();
        if (sketchPoints != null) {
            for (SketchPoint sketchPoint : sketchPoints) {
                mapboxMap.removeMarker(sketchPoint.getSymbolType().getMarker());
            }
        }
        sketchPoints = getMSketchPoints();
        if (sketchPoints != null) {
            for (SketchPoint sketchPoint : sketchPoints) {
                mapboxMap.removeMarker(sketchPoint.getSymbolType().getMarker());
            }
        }
    }


    public static SketchLineString fromLngLats(@NonNull List<Point> points, int strokeColor, float alpha, float width) {
        //线
        SketchLineString sketchLineString = new SketchLineString(points);
        sketchLineString.setStrokeColor(strokeColor);
        sketchLineString.setAlpha(alpha);
        sketchLineString.setWidth(width);

        List<LatLng> temp = SketchUtil.listPoint2ListLatLng(points);
        sketchLineString.setPolylineOptions(new PolylineOptions().addAll(temp).color(strokeColor).alpha(1).width(width));
        //顶点
        SketchPoint sketchPoint;
        List<SketchPoint> vTemp = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (i != points.size() - 1) {
                sketchPoint = SketchPoint.fromLngLat(points.get(i), SketchPoint.V_POINT);
            } else {
                sketchPoint = SketchPoint.fromLngLat(points.get(i), SketchPoint.C_POINT);
            }
            vTemp.add(sketchPoint);
        }
        sketchLineString.setVSketchPoints(vTemp);
        //中点
       /* List<SketchPoint> mTemp = new ArrayList<>();
        points = SketchUtil.lineStringMidPoints(points);
        for (int i = 0; i < points.size(); i++) {
            sketchPoint = SketchPoint.fromLngLat(points.get(i), SketchPoint.M_POINT);
            mTemp.add(sketchPoint);
        }
        sketchLineString.setMSketchPoints(mTemp);*/
        return sketchLineString;
    }

    public static SketchLineString fromLngLats(@NonNull List<Point> points, boolean isRing, int strokeColor, float alpha, float width) {
        if (!isRing) {
            return SketchLineString.fromLngLats(points, strokeColor, 1, width);
        }
        //线
        SketchLineString sketchLineString = new SketchLineString(points);
        sketchLineString.setStrokeColor(strokeColor);
        sketchLineString.setAlpha(alpha);
        sketchLineString.setWidth(width);
        sketchLineString.setRing(isRing);
        try {
            List<LatLng> temp = SketchUtil.listPoint2ListLatLng(points);
            if (temp == null || temp.size() == 0) {
                return null;
            }
            temp.add(temp.get(0));
            sketchLineString.setPolylineOptions(new PolylineOptions().addAll(temp).color(strokeColor).alpha(1).width(width));
            //顶点
            SketchPoint sketchPoint;
            SketchPoint closeSketchPoint;
            List<SketchPoint> vTemp = new ArrayList<>();
            for (int i = 0; i < points.size(); i++) {
                if (i != points.size() - 1) {
                    sketchPoint = SketchPoint.fromLngLat(points.get(i), SketchPoint.V_POINT);
                } else {
                    sketchPoint = SketchPoint.fromLngLat(points.get(i), SketchPoint.C_POINT);
                }
                vTemp.add(sketchPoint);
            }
            sketchLineString.setVSketchPoints(vTemp);
            //中点
           /* List<SketchPoint> mTemp = new ArrayList<>();
            points = SketchUtil.ringMidPoints(points);
            for (int i = 0; i < points.size(); i++) {
                sketchPoint = SketchPoint.fromLngLat(points.get(i), SketchPoint.M_POINT);
                mTemp.add(sketchPoint);
            }
            sketchLineString.setMSketchPoints(mTemp);*/
            return sketchLineString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SketchLineString fromGeometry(@NonNull Geometry geometry, int strokeColor, float alpha, float width) {
        List<Point> points = ((LineString) geometry).coordinates();
        return fromLngLats(points, strokeColor, alpha, width);
    }

    public static SketchLineString fromGeometry(@NonNull Geometry geometry, boolean isRing, int strokeColor, float alpha, float width) {
        List<Point> points = ((LineString) geometry).coordinates();
        return fromLngLats(points, isRing, strokeColor, 1, width);
    }
}

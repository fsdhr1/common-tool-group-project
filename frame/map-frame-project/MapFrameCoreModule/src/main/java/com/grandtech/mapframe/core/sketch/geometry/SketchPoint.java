package com.grandtech.mapframe.core.sketch.geometry;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grandtech.mapframe.core.GMap;
import com.grandtech.mapframe.core.R;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.CoordinateContainer;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2018/12/20.
 */

public class SketchPoint implements CoordinateContainer<List<Double>>, Serializable {
    /**
     * 点的类型
     */
    public static final int V_POINT = 100;
    public static final int M_POINT = 200;
    public static final int C_POINT = 300;

    int pointType;

    Point point;

    /**
     * 点样式
     */
    private MarkerOptions symbolType;

    public SketchPoint(Point point) {
        this.point = Point.fromLngLat(point.longitude(), point.latitude());
    }


    public MarkerOptions getSymbolType() {
        return symbolType;
    }

    private void setSymbolType(MarkerOptions symbolType) {
        this.symbolType = symbolType;
    }

    public int getPointType() {
        return pointType;
    }

    public void setPointType(int pointType) {
        this.pointType = pointType;
    }

    @Override
    @NonNull
    public String type() {
        return "Point";
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

    @Override
    @NonNull
    public List<Double> coordinates() {
        List<Double> temp = new ArrayList<>();
        temp.add(point.longitude());
        temp.add(point.latitude());
        return temp;
    }

    public static SketchPoint fromLngLat(Point point, int pointType) {
        Icon icon = null;
        if (pointType == V_POINT) {
            icon = IconFactory.getInstance(Mapbox.getApplicationContext()).fromResource(R.mipmap.gmmfc_draw_shape);
        } else if (pointType == M_POINT) {
            icon = IconFactory.getInstance(Mapbox.getApplicationContext()).fromResource(R.mipmap.gmmfc_draw_tool_m);
        } else if (pointType == C_POINT) {
            icon = IconFactory.getInstance(Mapbox.getApplicationContext()).fromResource(R.mipmap.gmmfc_draw_shape);
        }
        SketchPoint sketchPoint = new SketchPoint(point);
        sketchPoint.setPointType(pointType);
        sketchPoint.setSymbolType(new MarkerOptions().position(new LatLng(point.latitude(), point.longitude())).setIcon(icon));
        return sketchPoint;
    }


    public static SketchPoint fromGeometry(@NonNull Geometry geometry, int pointType) {
        return fromLngLat((Point) geometry, pointType);
    }

}

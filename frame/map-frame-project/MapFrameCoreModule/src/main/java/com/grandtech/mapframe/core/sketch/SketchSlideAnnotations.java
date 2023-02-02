package com.grandtech.mapframe.core.sketch;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.util.GsonFactory;
import com.grandtech.mapframe.core.util.Transformation;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SketchSlideLineAnnotations
 * @Description TODO 绘制SlideLine(类似于涂鸦，划线)
 * @Author: fs
 * @Date: 2021/6/10 14:27
 * @Version 2.0
 */
public class SketchSlideAnnotations extends SketchMapEvnet implements ISketch {

    private GMapView mGMapView;
    private boolean isStartSketch;
    private GeoJsonSource geoJsonSource;
    private List<Point> points = new ArrayList<>();
    private GeoJson geoJson;
    private GeometryType mGeometryType = GeometryType.slideLine;

    public SketchSlideAnnotations(GMapView gMapView) {
        mGMapView = gMapView;
    }

    @Override
    public void setSketchType(@NonNull GeometryType geometryType) {
        if (geometryType != null) {
            mGeometryType = geometryType;
        }
    }

    public GeometryType getGeometryType() {
        return mGeometryType;
    }

    @Override
    public void startSketch() {
        isStartSketch = true;
        points = new ArrayList<>();
        if (GeometryType.slideLine.equals(mGeometryType)) {
            geoJsonSource = new GeoJsonSource("scrawl" + System.currentTimeMillis());
            mGMapView.getStyleLayerManager().addSource(geoJsonSource);
            LineLayer lineLayer = new LineLayer(geoJsonSource.getId() + "layer", geoJsonSource.getId())
                    .withProperties(PropertyFactory.lineColor(Color.parseColor("#94FDD6")),
                            PropertyFactory.lineWidth(3f),
                            PropertyFactory.lineOpacity(1f));
            mGMapView.getStyleLayerManager().addLayer(lineLayer);
        }
        if (GeometryType.slidePolygon.equals(mGeometryType)) {
            geoJsonSource = new GeoJsonSource("scrawl" + System.currentTimeMillis());
            mGMapView.getStyleLayerManager().addSource(geoJsonSource);
            FillLayer fillLayer = new FillLayer(geoJsonSource.getId() + "layer", geoJsonSource.getId())
                    .withProperties(PropertyFactory.lineColor(Color.parseColor("#94FDD6")),
                            PropertyFactory.fillOpacity(0.5f),
                            PropertyFactory.fillColor("#94FDD6"),
                            PropertyFactory.lineWidth(3f),
                            PropertyFactory.lineOpacity(1f));
            mGMapView.getStyleLayerManager().addLayer(fillLayer);
        }

    }

    /**
     * 清空绘制
     */
    public void clearSketch() {
        if (this.points != null) {
            points.clear();
        }
        if (this.geoJson != null) {
            geoJson = null;
        }
        List<List<Point>> ps = new ArrayList<>();
        Polygon polygon = Polygon.fromLngLats(ps);
        geoJsonSource.setGeoJson(polygon);
    }

    @Override
    public void stopSketch() {
        isStartSketch = false;
        points.clear();
      /*  Map<String,Object> map = new HashMap();
        map.put("type","LineString");
        map.put("coordinates",points);
        geoJsonSource.setGeoJson(GsonFactory.getFactory().getComMapBoxGson().toJson(map));*/
        mGMapView.getStyleLayerManager().removeLayer(geoJsonSource.getId() + "layer");
        mGMapView.getStyleLayerManager().removeSource(geoJsonSource);
        geoJsonSource = null;
        geoJson = null;
    }

    @Override
    public Boolean isStartSketch() {
        return isStartSketch;
    }

    @Override
    public Geometry getSketchGeometry() {
        if (geoJson == null) {
            return null;
        }
        //面要收尾闭合
        if (GeometryType.slidePolygon.equals(mGeometryType)) {
            List<List<Point>> ps = new ArrayList<>();
            points.add(points.get(0));
            ps.add(points);
            Polygon polygon = Polygon.fromLngLats(ps);
            return (Geometry) Transformation.geoJsonStr2GeoJson(polygon.toJson());
        }
        return (Geometry) Transformation.geoJsonStr2GeoJson(geoJson.toJson());
    }

    @Override
    public void addSketchGeometry(@NonNull Geometry geometry) {

    }

    @Override
    public boolean onTouchMoving(MotionEvent motionEvent) {
        try {
            LatLng latLng = mGMapView.getMapBoxMap().getProjection().fromScreenLocation(new PointF(motionEvent.getX(), motionEvent.getY()));
            Point point = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
            if (GeometryType.slidePolygon.equals(mGeometryType)) {
                if (points.contains(point)) {
                    return true;
                }
                points.add(point);
                if (points.size() < 3) {
                    return true;
                }
                List<List<Point>> ps = new ArrayList<>();
                //points.add(points.get(0));
                ps.add(points);
                Polygon polygon = Polygon.fromLngLats(ps);
                geoJson = polygon;
            }
            if (GeometryType.slideLine.equals(mGeometryType)) {
                points.add(point);
                if (points.size() < 2) {
                    return true;
                }
                LineString lineString = LineString.fromLngLats(points);
                geoJson = lineString;
            }

            geoJsonSource.setGeoJson((Geometry) geoJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onTouchCancel(MotionEvent motionEvent) {
        if (iOnAfterSlideListener != null && geoJson != null) {
            iOnAfterSlideListener.onAfterSlideListener((Geometry) geoJson);
        }
        return true;
    }

    //绘制完回调
    public interface IOnAfterSlideListener {
        public void onAfterSlideListener(Geometry geometry);
    }

    private IOnAfterSlideListener iOnAfterSlideListener;

    public void setIOnAfterSlideListener(IOnAfterSlideListener iOnAfterSlideListener) {
        this.iOnAfterSlideListener = iOnAfterSlideListener;
    }

}

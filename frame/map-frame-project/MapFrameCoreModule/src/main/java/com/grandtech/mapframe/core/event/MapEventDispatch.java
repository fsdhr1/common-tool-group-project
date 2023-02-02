package com.grandtech.mapframe.core.event;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.grandtech.mapframe.core.sketch.SketchMapEvnet;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

/**
 * Created by zy on 2018/4/2.
 */

public class MapEventDispatch extends MapExtentEvent implements MapboxMap.OnMapClickListener,
        MapboxMap.OnMapLongClickListener,
        MapboxMap.OnMarkerClickListener,
        MapboxMap.OnFpsChangedListener,
        MapboxMap.OnFlingListener,
        MapboxMap.OnCameraMoveStartedListener,
        MapboxMap.OnCameraMoveListener,
        MapboxMap.OnCameraMoveCanceledListener,
        MapboxMap.OnCameraIdleListener,
        MapboxMap.OnPolygonClickListener,
        MapboxMap.OnPolylineClickListener {


    public MapEventDispatch() {
    }

    private IMapEvent mIMapEvent;

    private SketchMapEvnet mSketchMapEvnet;

    public SketchMapEvnet getSketchMapEvnet() {
        return mSketchMapEvnet;
    }

    public void setSketchMapEvnet(SketchMapEvnet mSketchMapEvnet) {
        this.mSketchMapEvnet = mSketchMapEvnet;
    }

    public IMapEvent getCurIMapEvent() {
        return mIMapEvent;
    }

    public void setCurIMapEvent(IMapEvent iMapEvent) {
        this.mIMapEvent = iMapEvent;
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        boolean b = false;
        if(mSketchMapEvnet!=null){
            b = mSketchMapEvnet.onMapClick(point);
        }
        if (mIMapEvent != null&&(mSketchMapEvnet==null||!b)) {
            return mIMapEvent.onMapClick(point);
        }
        return b;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        boolean b = false;
        if(mSketchMapEvnet!=null){
            b = mSketchMapEvnet.onMapLongClick(point);
        }
        if (mIMapEvent != null&&(mSketchMapEvnet==null||!b)) {
            return mIMapEvent.onMapLongClick(point);
        }
        return b;
    }



    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        boolean b = false;
        if(mSketchMapEvnet!=null){
            b = mSketchMapEvnet.onMarkerClick(marker);
        }
        if (mIMapEvent != null&&(mSketchMapEvnet==null||!b)) {
            return mIMapEvent.onMarkerClick(marker);
        }
        return b;
    }

    @Override
    public void onFpsChanged(double fps) {
        if (mIMapEvent != null) {
            mIMapEvent.onFpsChanged(fps);
        }
    }

    @Override
    public void onFling() {
        if (mIMapEvent != null) {
            mIMapEvent.onFling();
        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (mIMapEvent != null) {
            mIMapEvent.onCameraMoveStarted(reason);
        }
    }

    @Override
    public void onCameraIdle() {
        if (mIMapEvent != null) {
            mIMapEvent.onCameraIdle();
        }
    }

    @Override
    public void onCameraMoveCanceled() {
        if (mIMapEvent != null) {
            mIMapEvent.onCameraMoveCanceled();
        }
    }

    @Override
    public void onCameraMove() {
        if (mIMapEvent != null) {
            mIMapEvent.onCameraMove();
        }
    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {
        if (mIMapEvent != null) {
            mIMapEvent.onPolygonClick(polygon);
        }
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        if (mIMapEvent != null) {
            mIMapEvent.onPolylineClick(polyline);
        }
    }

    @Override
    public boolean onTouchStart(MotionEvent motionEvent) {
        boolean b = false;
        if(mSketchMapEvnet!=null){
            b = mSketchMapEvnet.onTouchStart(motionEvent);
        }
        if (mIMapEvent != null&&(mSketchMapEvnet==null||!b)) {
            return mIMapEvent.onTouchStart(motionEvent);
        }
        return b;
    }

    @Override
    public boolean onTouchMoving(MotionEvent motionEvent) {
        boolean b = false;
        if(mSketchMapEvnet!=null){
            b = mSketchMapEvnet.onTouchMoving(motionEvent);
        }
        if (mIMapEvent != null&&(mSketchMapEvnet==null||!b)) {
            return mIMapEvent.onTouchMoving(motionEvent);
        }
        return b;
    }

    @Override
    public boolean onTouchCancel(MotionEvent motionEvent) {
        boolean b = false;
        if(mSketchMapEvnet!=null){
            b = mSketchMapEvnet.onTouchCancel(motionEvent);
        }
        if (mIMapEvent != null&&(mSketchMapEvnet==null||!b)) {
            return mIMapEvent.onTouchCancel(motionEvent);
        }
        return b;
    }
}

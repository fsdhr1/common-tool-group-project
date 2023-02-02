package com.grandtech.mapframe.core.sketch;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.grandtech.mapframe.core.event.IMapEvent;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * @ClassName SketchMapEvnet
 * @Description TODO
 * @Author: fs
 * @Date: 2021/3/11 13:30
 * @Version 2.0
 */
public class SketchMapEvnet implements IMapEvent {
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        return false;
    }

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onCameraMoveStarted(int reason) {

    }

    @Override
    public void onCompassAnimation() {

    }

    @Override
    public void onCompassAnimationFinished() {

    }

    @Override
    public void onFling() {

    }

    @Override
    public void onFpsChanged(double fps) {

    }

    @Override
    public void onScroll() {

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {

    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }

    @Override
    public boolean onTouchStart(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onTouchMoving(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onTouchCancel(MotionEvent motionEvent) {
        return false;
    }
}

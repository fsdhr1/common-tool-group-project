package com.gradtech.mapframev10.core.event;


import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Point;


public interface IMapEvent {
    default public boolean onMapClick(@NonNull Point point) {
        return false;
    }

    default public boolean onMapLongClick(@NonNull Point point) {
        return false;
    }

    default public void onCameraIdle() {

    }

    default public void onCameraMove() {

    }

    default public void onCameraMoveStarted(int reason) {

    }

    default public void onFling() {

    }

    default public void onFpsChanged(double fps) {

    }

    default public void onScroll(){

    }

    //=========================android原生事件==========================

    default public boolean onTouchStart(MotionEvent motionEvent) {
        return false;
    }

    default public boolean onTouchMoving(MotionEvent motionEvent) {
        return false;
    }

    default public boolean onTouchCancel(MotionEvent motionEvent) {
        return false;
    }
}

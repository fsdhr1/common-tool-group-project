package com.grandtech.mapframe.core.event;


import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 *
 * @author zy
 * @date 2018/3/29
 */

public interface IMapEvent {
    public boolean onMapClick(@NonNull LatLng point);

    public boolean onMapLongClick(@NonNull LatLng point);

    public void onCameraIdle();

    public void onCameraMoveCanceled();

    public void onCameraMove();

    public void onCameraMoveStarted(int reason);

    public void onCompassAnimation();

    public void onCompassAnimationFinished();

    public void onFling();

    public void onFpsChanged(double fps);

    public void onScroll();

    public boolean onMarkerClick(@NonNull Marker marker);

    public void onPolygonClick(@NonNull Polygon polygon);

    public void onPolylineClick(@NonNull Polyline polyline);

    //=========================android原生事件==========================

    public boolean onTouchStart(MotionEvent motionEvent);

    public boolean onTouchMoving(MotionEvent motionEvent);

    public boolean onTouchCancel(MotionEvent motionEvent);
}

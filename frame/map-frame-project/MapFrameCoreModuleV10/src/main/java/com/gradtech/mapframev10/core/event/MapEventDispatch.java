package com.gradtech.mapframev10.core.event;

import android.view.MotionEvent;

import com.gradtech.mapframev10.core.sketch.ISketch;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.android.gestures.StandardScaleGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.plugin.gestures.OnFlingListener;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.gestures.OnScaleListener;
import com.mapbox.maps.renderer.OnFpsChangedListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class MapEventDispatch extends MapExtentEvent implements OnMapClickListener, OnMapLongClickListener, OnFpsChangedListener, OnFlingListener, OnMoveListener, OnScaleListener {


    public MapEventDispatch() {
    }

    private List<IMapEvent> mIMapEvent = new ArrayList<>();

    public void addIMapEvent(IMapEvent iMapEvent) {
        if(iMapEvent instanceof ISketch){
            mIMapEvent.add(0,iMapEvent);
            return;
        }
        mIMapEvent.add(iMapEvent);
    }

    public void removeIMapEvent(IMapEvent iMapEvent){
        mIMapEvent.remove(iMapEvent);
    }

    @Override
    public boolean onTouchStart(MotionEvent motionEvent) {
        boolean flag = false;
        for (IMapEvent iMapEvent : mIMapEvent) {
            flag = iMapEvent.onTouchStart(motionEvent);
            if(flag) {
                return flag;
            }
        }
        return flag;
    }

    @Override
    public boolean onTouchMoving(MotionEvent motionEvent) {
        boolean flag = false;
        for (IMapEvent iMapEvent : mIMapEvent) {
            flag = iMapEvent.onTouchMoving(motionEvent);
            if(flag) {
                return flag;
            }
        }
        return flag;
    }

    @Override
    public boolean onTouchCancel(MotionEvent motionEvent) {
        boolean flag = false;
        for (IMapEvent iMapEvent : mIMapEvent) {
            flag = iMapEvent.onTouchCancel(motionEvent);
            if(flag) {
                return flag;
            }
        }
        return flag;
    }


    @Override
    public void onFling() {
        for (IMapEvent iMapEvent : mIMapEvent) {
            iMapEvent.onFling();
        }
    }

    @Override
    public boolean onMapClick(@NotNull Point point) {
        boolean flag = false;
        for (IMapEvent iMapEvent : mIMapEvent) {
            flag = iMapEvent.onMapClick(point);
            if(flag) {
                return flag;
            }
        }
        return flag;
    }

    @Override
    public boolean onMapLongClick(@NotNull Point point) {
        boolean flag = false;
        for (IMapEvent iMapEvent : mIMapEvent) {
            flag = iMapEvent.onMapLongClick(point);
            if(flag) {
                return flag;
            }
        }
        return flag;
    }

    @Override
    public void onFpsChanged(double v) {
        for (IMapEvent iMapEvent : mIMapEvent) {
            iMapEvent.onFpsChanged(v);
        }
    }

    @Override
    public boolean onMove(@NotNull MoveGestureDetector moveGestureDetector) {
        for (IMapEvent iMapEvent : mIMapEvent) {
            iMapEvent.onCameraMove();
        }
        return false;
    }

    @Override
    public void onMoveBegin(@NotNull MoveGestureDetector moveGestureDetector) {
        for (IMapEvent iMapEvent : mIMapEvent) {
            iMapEvent.onCameraMoveStarted((int)moveGestureDetector.getMoveThreshold());
        }
    }

    @Override
    public void onMoveEnd(@NotNull MoveGestureDetector moveGestureDetector) {
        for (IMapEvent iMapEvent : mIMapEvent) {
            iMapEvent.onCameraIdle();
        }
    }

    @Override
    public void onScale(@NotNull StandardScaleGestureDetector standardScaleGestureDetector) {
        for (IMapEvent iMapEvent : mIMapEvent) {
            iMapEvent.onScroll();
        }
    }

    @Override
    public void onScaleBegin(@NotNull StandardScaleGestureDetector standardScaleGestureDetector) {

    }

    @Override
    public void onScaleEnd(@NotNull StandardScaleGestureDetector standardScaleGestureDetector) {

    }
}

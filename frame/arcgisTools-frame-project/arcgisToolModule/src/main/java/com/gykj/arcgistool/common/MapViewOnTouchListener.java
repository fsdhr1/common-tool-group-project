package com.gykj.arcgistool.common;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.gykj.arcgistool.listener.IOnTouchListener;

/**
 *  @author jzq
 *  @date 2012/4/15
 * Arcgis地图事件扩展
 */
public class MapViewOnTouchListener implements IOnTouchListener {
    @Override
    public boolean onMultiPointerTap(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTouchDrag(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onUp(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onRotate(MotionEvent motionEvent, double v) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
}

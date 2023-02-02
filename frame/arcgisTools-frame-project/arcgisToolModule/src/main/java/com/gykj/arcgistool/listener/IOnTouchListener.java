package com.gykj.arcgistool.listener;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * 地图触摸事件接口
 * @author jzq
 */
public interface IOnTouchListener {
    /**
     *
     * @param motionEvent
     * @return
     */
    boolean onMultiPointerTap(MotionEvent motionEvent);

    /**
     *
     * @param motionEvent
     * @return
     */
    boolean onDoubleTouchDrag(MotionEvent motionEvent);

    /**
     *
     * @param motionEvent
     * @return
     */
    boolean onUp(MotionEvent motionEvent);

    /***
     *
     * @param motionEvent
     * @param v
     * @return
     */
    boolean onRotate(MotionEvent motionEvent, double v);

    /***
     *
     * @param e
     * @return
     */
    boolean onSingleTapConfirmed(MotionEvent e);

    /***
     *
     * @param e
     * @return
     */
    boolean onDoubleTap(MotionEvent e);

    /***
     *
     * @param e
     * @return
     */
    boolean onDoubleTapEvent(MotionEvent e);

    /***
     *
     * @param e
     * @return
     */
    boolean onDown(MotionEvent e);

    /***
     *
     * @param e
     */
    void onShowPress(MotionEvent e);

    /***
     *
     * @param e
     * @return
     */
    boolean onSingleTapUp(MotionEvent e);

    /**
     *
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return
     */
    boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

    /**
     *
     * @param e
     */
    void onLongPress(MotionEvent e);

    /**
     *
     * @param e1
     * @param e2
     * @param velocityX
     * @param velocityY
     * @return
     */
    boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);

    /***
     *
     * @param detector
     * @return
     */
    boolean onScale(ScaleGestureDetector detector);

    /**
     *
     * @param detector
     * @return
     */
    boolean onScaleBegin(ScaleGestureDetector detector);

    /**
     *
     * @param detector
     */
    void onScaleEnd(ScaleGestureDetector detector);

    /**
     *
     * @param v
     * @param event
     * @return
     */
    boolean onTouch(View v, MotionEvent event);
}

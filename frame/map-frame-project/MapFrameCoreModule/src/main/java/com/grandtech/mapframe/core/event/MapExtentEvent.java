package com.grandtech.mapframe.core.event;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zy
 * @date 2018/4/4
 */

public abstract class MapExtentEvent implements View.OnTouchListener {

    private List<View.OnTouchListener> mOnTouchListenerList;


    public MapExtentEvent() {
        this.mOnTouchListenerList = new ArrayList<>();
    }

    public abstract boolean onTouchStart(MotionEvent motionEvent);

    public abstract boolean onTouchMoving(MotionEvent motionEvent);

    public abstract boolean onTouchCancel(MotionEvent motionEvent);

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        for (View.OnTouchListener onTouchListener : mOnTouchListenerList) {
            onTouchListener.onTouch(view,motionEvent);
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            return onTouchStart(motionEvent);
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            return onTouchMoving(motionEvent);
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
            return onTouchCancel(motionEvent);
        }
        return false;
    }

    public void addOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mOnTouchListenerList.add(onTouchListener);
    }

    public void removeOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mOnTouchListenerList.remove(onTouchListener);
    }
}

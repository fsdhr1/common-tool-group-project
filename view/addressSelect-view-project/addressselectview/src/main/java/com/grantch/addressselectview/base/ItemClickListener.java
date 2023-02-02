package com.grantch.addressselectview.base;


import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 点击事件
 * Created by zy on 2016/7/16.
 */

public class ItemClickListener extends RecyclerView.SimpleOnItemTouchListener {

    private OnItemClickListener clickListener;
    private GestureDetectorCompat gestureDetector;


    /**
     *  如果设置了对Item的事件监听，可能会引起复杂的RecycleView事件的冲突。需要合理使用
     * @param <H>
     */
    public interface OnItemClickListener<H> {
        /**
         * 点击时回调
         *
         * @param view     点击的View
         * @param position 点击的位置
         */
        void onItemClick(H view, int position);

        /**
         * 长点击时回调
         *
         * @param view     点击的View
         * @param position 点击的位置
         */
        void onItemLongClick(H view, int position);
    }

    public ItemClickListener(final RecyclerView recyclerView, OnItemClickListener listener) {
        this.clickListener = listener;
        gestureDetector = new GestureDetectorCompat(recyclerView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (childView != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                            clickListener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView));
                        }
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (childView != null && clickListener != null) {
                            clickListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                        }
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }
}

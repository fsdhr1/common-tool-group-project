package com.grandtech.mapframe.ui.view.holder;

import android.view.View;

import androidx.customview.widget.ViewDragHelper;

/**
 * @ClassName ViewMoveHolder
 * @Description TODO
 * @Author: fs
 * @Date: 2021/5/17 10:08
 * @Version 2.0
 */
public final class ViewMoveHolder {

    private View view;
    private ViewDragHelper viewDragHelper;

    public ViewMoveHolder() {

    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public ViewDragHelper getViewDragHelper() {
        return viewDragHelper;
    }

    public void setViewDragHelper(ViewDragHelper viewDragHelper) {
        this.viewDragHelper = viewDragHelper;
    }

}

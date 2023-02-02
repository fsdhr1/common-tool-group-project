package com.gradtech.mapframev10.core.snapshot.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.mapbox.maps.MapView;

import java.util.List;

/**
 * @ClassName SquareBoxMapView
 * @Description TODO 矩形框快照
 * @Author: fs
 * @Date: 2021/8/3 15:27
 * @Version 2.0
 */

public class SquareMapView extends MapView {

    /**
     * 正方形截屏框
     */
    private SquareView mSquareView;

    public SquareMapView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SquareMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SquareMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mSquareView = new SquareView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSquareView.setLayoutParams(layoutParams);
        this.addView(mSquareView);
    }

    public Rect getSquareRect() {
        return mSquareView.getRectIn();
    }

    public void setWarter(List<String> warter){
        mSquareView.setWarter(warter);
    }

}

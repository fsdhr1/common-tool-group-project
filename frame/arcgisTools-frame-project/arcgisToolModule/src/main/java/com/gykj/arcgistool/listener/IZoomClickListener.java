package com.gykj.arcgistool.listener;

import android.view.View;

/***
 * 地图视图工具条事件接口
 */
public interface IZoomClickListener {
    void zoomInClick(View view);
    void zoomOutClick(View view);
    void mapRotateClick(View view);
    void mapMainClick(View view);
}

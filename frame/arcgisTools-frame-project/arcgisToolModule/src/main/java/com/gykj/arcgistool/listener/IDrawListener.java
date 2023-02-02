package com.gykj.arcgistool.listener;

import com.esri.arcgisruntime.mapping.view.Graphic;
import com.gykj.arcgistool.common.GraphType;
import com.gykj.arcgistool.common.Variable;

/**
 * graphic绘制接口
 */
public interface IDrawListener {
    /**
     * 绘制结束
     * @param graphType
     * @param graphic
     */
    void onDrawEnd(GraphType graphType, Graphic graphic);

    /**
     * 绘制开始
     * @param graphType
     */
    void onDrawStart(GraphType graphType);
}

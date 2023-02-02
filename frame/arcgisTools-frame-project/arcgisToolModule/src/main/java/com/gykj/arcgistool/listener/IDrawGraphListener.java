package com.gykj.arcgistool.listener;

import com.esri.arcgisruntime.mapping.view.Graphic;
import com.gykj.arcgistool.common.GraphType;
import com.gykj.arcgistool.common.Variable;

/**
 *工具条绘图事件接口
 */
public interface IDrawGraphListener {
    /***
     * 绘制结束
     * @param graphType
     * @param graphic
     */
    void drawEnd(GraphType graphType, Graphic graphic);

    /**
     * 开始绘制
     * @param graphType
     */
    void drawStart(GraphType graphType);
}

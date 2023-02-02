package com.gykj.arcgistool.listener;

import com.gykj.arcgistool.entity.DrawEntity;

/***
 * 测量工具条点击事件接口
 */
public interface IMeasureClickListener {
    /***
     * 上一步
     * @param hasPrev
     */
    void prevClick(boolean hasPrev);

    /**
     * 下一步
     * @param hasNext
     */
    void nextClick(boolean hasNext);

    /***
     * 线长
     */
    void lengthClick();

    /**
     * 面积
     */
    void areaClick();

    /**
     * 清除
     * @param draw
     */
    void clearClick(DrawEntity draw);

    /**
     * 绘制结束
     * @param draw
     */
    void endClick(DrawEntity draw);
}

package com.grandtech.mapframe.ui.view;

import java.util.List;

import android.view.View;

/**
 * @ClassName IToolView
 * @Description TODO ToolView公共接口
 * @Author: fs
 * @Date: 2021/8/9 9:41
 * @Version 2.0
 */
public interface IToolView {

    /**
     * 设置选中
     * @param checked
     */
    public void setChecked(boolean checked) ;

    /**
     * 获取选中状态
     * @return
     */
    public boolean isChecked();


    /**
     * 获取工具名称
     * @return
     */
    public String getmToolName();

    /**
     * 判断是不是地图绘制相关的工具 false表示不是,默认false
     * @return
     */
    public boolean isMapSketchTool();

    /**
     * 控制搜索工具栏默认给 false
     * @return
     */
    public boolean canPutNavigate();

    /**
     * 点击
     * @param iClick
     */
    public void setIClick(IClick iClick);

    /**
     * 是否和其他按钮互斥 true为互斥
     * @return
     */
    public boolean isExclusion();

    /**
     * 获取互斥的toolname
     * @return
     */
    public List<String> getExclusionToolNames() ;

    /**
     * 长按
     * @param iLongClick
     */
    public void setILongClick(ILongClick iLongClick);

    public interface ILongClick {
        public void toolViewLongClick(IToolView view);
    }

    public interface IClick {
        public void toolViewClick(IToolView view);
    }
    
    public View getToolViewRoot();
}

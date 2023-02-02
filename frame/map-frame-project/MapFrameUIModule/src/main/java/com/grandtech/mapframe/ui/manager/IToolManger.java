package com.grandtech.mapframe.ui.manager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.grandtech.mapframe.ui.view.DragLayout;
import com.grandtech.mapframe.ui.view.IToolView;
import com.grandtech.mapframe.ui.view.ToolGroupLayout;
import com.grandtech.mapframe.ui.view.ToolView;

import java.util.List;

/**
 * @ClassName IUiManger
 * @Description TODO 工具管理类
 * @Author: fs
 * @Date: 2021/5/19 13:21
 * @Version 2.0
 */
public interface IToolManger {


    /**
     *TODO 初始化加载布局
     * @param context
     * @param viewGroup  装载工具布局的容器对象
     * @param isCustom 是否自定义布局，true的话，初始为空布局
     */
    public ToolManager init(Context context , ViewGroup viewGroup,Boolean isCustom);

    /**
     * TODO 添加工具栏view
     * @param view
     * @param needToolBar 是否需要包装成工具组 ture则会在外面自动套一层ToolGroupLayout
     * @param widgetGravity
     */
    public void addToolBarView(View view, boolean needToolBar, WidgetGravityEnum widgetGravity);

    /**
     * TODO 添加自定义view
     * @param view
     * @param layoutParams 参数
     */
    public void addView(View view, DragLayout.LayoutParams layoutParams);

    /**
     * 移除view
     * @param view
     */
    public void removeView(View view);

    /**
     * TODO 获取所有的ToolView实例
     *  @return
     */
    public  <T extends IToolView> List<T>  getToolViews();
    /**
     * TODO 获取ToolView实例根据工具名
     *  @param toolName  工具名
     *  @return
     */
    public  <T extends IToolView> T getToolViewByName(String toolName);

    /**
     * TODO 获取toolGroup实例根据toolGroup名
     *  @param toolGroupName  toolGroup名
     *  @return
     */
    public ToolGroupLayout getToolGroupViewByName(String toolGroupName);

    /**
     * TODO 根据toolview关闭开启其所在导航栏
     * @param toolView
     */
    public void toolBarNavigate(IToolView toolView);

    /**
     *TODO  激活工具（需要代码自动激活工具的情况下使用）
     * @param toolView
     */
    public void activateTool(IToolView toolView);
    /**
     *TODO  取消激活工具（需要代码自动激活工具的情况下使用）
     * @param toolView
     */
    public void deactivateTool(IToolView toolView);
}

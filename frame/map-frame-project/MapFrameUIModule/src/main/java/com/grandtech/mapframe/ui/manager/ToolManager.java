package com.grandtech.mapframe.ui.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import com.grandtech.mapframe.ui.R;
import com.grandtech.mapframe.ui.view.DragLayout;
import com.grandtech.mapframe.ui.view.IToolView;
import com.grandtech.mapframe.ui.view.ToolGroupLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;

/**
 * @ClassName UiManager
 * @Description TODO 地图上的UI组件，操作只涉及样式改变和监听回调，不处理功能和业务
 * @Author: fs
 * @Date: 2021/5/19 13:20
 * @Version 2.0
 */
public final class ToolManager implements IToolManger, IToolView.IClick,IToolView.ILongClick {
    /**
     * 布局
     */
    private DragLayout mToolDragLayout;
    /**
     * 激活的工具名称
     */
    private IToolView mActivateTool;

    private OnToolViewActivateListener mOnToolClickListener;

    private ToolGroupLayout mToolGroupDraw;
    /**
     *TODO  创建ToolManager
     * @return ToolManager
     */
    public static ToolManager newInstance() {
        return new ToolManager();
    }

    @Override
    public ToolManager init(@NonNull Context context,@NonNull ViewGroup viewGroup,@NonNull Boolean isCustom) {
        Log.i("init", "init");
        mToolDragLayout = (DragLayout) View.inflate(context, R.layout.map_frame_ui_tool_drag_layout, null);
        Log.i("init1", "init");
        viewGroup.addView(mToolDragLayout);
        mToolGroupDraw = mToolDragLayout.findViewById(R.id.toolGroupDraw);
        if(isCustom){
            ToolGroupLayout toolGroupBase = mToolDragLayout.findViewById(R.id.toolGroupBase);
            mToolDragLayout.removeView(toolGroupBase);
            ToolGroupLayout  toolGroupNav = mToolDragLayout.findViewById(R.id.toolGroupNav);
            mToolDragLayout.removeView(toolGroupNav);
            return this;
        }
        initEvent();
        return this;
    }

    @Override
    public void addToolBarView(View view,boolean needToolBar,WidgetGravityEnum widgetGravity) {
        DragLayout.LayoutParams layoutParams = new DragLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (widgetGravity == WidgetGravityEnum.TOP || widgetGravity == WidgetGravityEnum.LEFT_TOP) {
            layoutParams.addRule(DragLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(DragLayout.ALIGN_PARENT_LEFT);
        } else if (widgetGravity == WidgetGravityEnum.BOTTOM || widgetGravity == WidgetGravityEnum.LEFT_BOTTOM) {
            layoutParams.addRule(DragLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(DragLayout.ALIGN_PARENT_BOTTOM);
        } else if (widgetGravity == WidgetGravityEnum.RIGHT_TOP || widgetGravity == WidgetGravityEnum.RIGHT) {
            layoutParams.addRule(DragLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(DragLayout.ALIGN_PARENT_TOP);
        } else if (widgetGravity == WidgetGravityEnum.RIGHT_BOTTOM) {
            layoutParams.addRule(DragLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(DragLayout.ALIGN_PARENT_BOTTOM);
        } else if (widgetGravity == WidgetGravityEnum.CENTER) {
            layoutParams.addRule(DragLayout.CENTER_HORIZONTAL);
            layoutParams.addRule(DragLayout.CENTER_VERTICAL);
        } else if (widgetGravity == WidgetGravityEnum.LEFT_CENTER) {
            layoutParams.addRule(DragLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(DragLayout.CENTER_VERTICAL);
        } else if (widgetGravity == WidgetGravityEnum.TOP_CENTER) {
            layoutParams.addRule(DragLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(DragLayout.CENTER_HORIZONTAL);
        } else if (widgetGravity == WidgetGravityEnum.BOTTOM_CENTER) {
            layoutParams.addRule(DragLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(DragLayout.CENTER_HORIZONTAL);
        } else if (widgetGravity == WidgetGravityEnum.RIGHT_CENTER) {
            layoutParams.addRule(DragLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(DragLayout.CENTER_VERTICAL);
        }
        if(needToolBar){
            ViewGroup barBox = (ViewGroup) View.inflate(view.getContext(), R.layout.map_frame_ui_tool_bar_box, null);
            barBox.addView(view);
            mToolDragLayout.addView(barBox,layoutParams);
        }else {
            mToolDragLayout.addView(view,layoutParams);
        }
        initEvent();

    }

    @Override
    public void addView(@NonNull View view, @NonNull DragLayout.LayoutParams layoutParams) {
        mToolDragLayout.addView(view,layoutParams);
        initEvent();
    }

    @Override
    public void removeView(View view) {
        mToolDragLayout.removeView(view);
    }

    /**
     * TODO 点击回调监听
     * @param onToolClickListener
     */
    public void setOnToolClickListener(OnToolViewActivateListener onToolClickListener) {
        this.mOnToolClickListener = onToolClickListener;
    }

    @Override
    public void toolViewClick(IToolView view) {
        activate(view);
    }

    @Override
    public void toolViewLongClick(IToolView view) {

    }
    /**
     * TODO 获取所有的ToolView实例
     * @return
     */
    @Override
    public  <T extends IToolView> List<T> getToolViews() {
        int childCount = mToolDragLayout.getChildCount();
        List<T> viewChilds = new ArrayList<>();
        for(int i =0; i<childCount ;i++){
            View view = mToolDragLayout.getChildAt(i);
            if (view instanceof IToolView)
            {
                viewChilds.add((T)view);
                continue;
            }
            if (!(view instanceof ToolGroupLayout))
            {
                continue;
            }
            int childCountl2 = ((ToolGroupLayout)view).getChildCount();
            for(int j =0; j<childCountl2 ;j++){
                View viewl2 = ((ToolGroupLayout)view).getChildAt(j);
                if(!(viewl2 instanceof IToolView))
                {
                    continue;
                }
                viewChilds.add((T)viewl2);
            }
        }
        return viewChilds;
    }
    /**
     * TODO 获取ToolView实例根据工具名
     * @param toolName TODO 工具名
     * @return
     */
    @Override
    public  <T extends IToolView> T getToolViewByName(String toolName) {
        int childCount = mToolDragLayout.getChildCount();
        for(int i =0; i<childCount ;i++){
            View view = mToolDragLayout.getChildAt(i);
            if(view instanceof IToolView){
                if(((IToolView) view).getmToolName().equals(toolName)){
                    return (T) view;
                }
                continue;
            }
            if (!(view instanceof ToolGroupLayout))
            {
                continue;
            }
            int childCountl2 = ((ToolGroupLayout)view).getChildCount();
            for(int j =0; j<childCountl2 ;j++){
                View viewl2 = ((ToolGroupLayout)view).getChildAt(j);
                if(!(viewl2 instanceof IToolView))
                {
                    continue;
                }
                if(!((IToolView)viewl2).getmToolName().equals(toolName)){
                    continue;
                }
                return (T)viewl2;
            }
        }
        return null;
    }

    @Override
    public ToolGroupLayout getToolGroupViewByName(String toolGroupName) {
        return getToolGroupViewByName(mToolDragLayout,toolGroupName);
    }

    private ToolGroupLayout getToolGroupViewByName(ViewGroup viewGroup,String toolGroupName){
        int childCount = viewGroup.getChildCount();
        for(int i =0; i<childCount ;i++){
            View view = viewGroup.getChildAt(i);
            if (view instanceof ToolGroupLayout) {
                if(toolGroupName.equals(((ToolGroupLayout)view).getToolGroupName())){
                    return (ToolGroupLayout)view;
                }
            }
            if(view instanceof ViewGroup){
                ToolGroupLayout toolGroupLayout =  getToolGroupViewByName((ViewGroup)view,toolGroupName);
                if(toolGroupLayout != null){
                    return toolGroupLayout;
                }
            }

        }
        return null;
    }

    @Override
    public void toolBarNavigate(IToolView toolView) {
        if(!(toolView instanceof View)){
            return;
        }
        ViewParent viewParent = ((View)toolView).getParent();
        if(viewParent instanceof ToolGroupLayout){
            int childCountl2 = ( (ToolGroupLayout)viewParent).getChildCount();
            for(int j =0; j<childCountl2 ;j++){
                View viewl2 = ( (ToolGroupLayout)viewParent).getChildAt(j);
                if(!(viewl2 instanceof IToolView))
                {
                    continue;
                }
                if(((IToolView)viewl2).getmToolName().equals(toolView.getmToolName())){
                    continue;
                }
                if(toolView.isChecked()&&((IToolView)viewl2).canPutNavigate()){
                    viewl2.setVisibility(View.GONE);
                }else {
                    viewl2.setVisibility(View.VISIBLE);
                }

            }
        }
    }

    @Override
    public void activateTool(IToolView toolView) {
        toolView.setChecked(true);
        activate(toolView);
    }

    @Override
    public void deactivateTool(IToolView toolView) {
        toolView.setChecked(false);
        activate(toolView);
    }

    /**
     * 获取Draw工具组
     * @return
     */
    public ToolGroupLayout getToolGroupDraw() {
        return mToolGroupDraw;
    }

    /**
     * 激活工具
     * @param view
     */
    private void activate(IToolView view){
        if(view.isExclusion()){
            if( view.isChecked()){
                this.mActivateTool = view;
                exclusion(view);
                if(mOnToolClickListener!=null){
                    mOnToolClickListener.activate(view);
                }
                if(view.isMapSketchTool()){
                    mToolGroupDraw.setVisibility(View.VISIBLE);
                }
            }else {
                this.mActivateTool = null;
                if(mOnToolClickListener!=null){
                    mOnToolClickListener.deactivate(view);
                }
                if(view.isMapSketchTool()){
                    mToolGroupDraw.setVisibility(View.GONE);
                }
            }
        }else {
            if(mOnToolClickListener!=null){
                mOnToolClickListener.activate(view);
            }
        }
    }

    /**
     * 工具互斥
     * @param toolView
     */
    private void exclusion(IToolView toolView){
        List<IToolView> toolViews = getToolViews();
        for(IToolView tv : toolViews){
            if(!tv.isExclusion()){
                continue;
            }
            List<String> exclusionToolNames =toolView.getExclusionToolNames();
            if(exclusionToolNames != null && exclusionToolNames.size()>0 && exclusionToolNames.contains(tv.getmToolName())){
                if(tv.isChecked()){
                    tv.setChecked(false);
                    if(mOnToolClickListener!=null){
                        mOnToolClickListener.deactivate(tv);
                    }
                }
            }else {
                if(!tv.getmToolName().equals(toolView.getmToolName())){
                    if(tv.isChecked()){
                        tv.setChecked(false);
                        if(mOnToolClickListener!=null){
                            mOnToolClickListener.deactivate(tv);
                        }
                    }
                }
            }
        }
    }


    /**
     * 注册toolview点击和长按回调事件
     */
    private void initEvent() {
        List<IToolView> toolViews = getToolViews();
        for(IToolView tv : toolViews){
            tv.setIClick(this);
            tv.setILongClick(this);
        }
    }


    /**
     * 工具点击回调监听
     */
    public interface OnToolViewActivateListener{
        /**
         * 激活点击
         * @param toolView
         */
        void activate(IToolView toolView);

        /**
         * 关闭工具点击
         * @param toolView
         */
        void deactivate(IToolView toolView);
    }
}

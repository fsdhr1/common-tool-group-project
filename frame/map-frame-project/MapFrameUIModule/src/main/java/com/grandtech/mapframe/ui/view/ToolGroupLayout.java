package com.grandtech.mapframe.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grandtech.mapframe.ui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ToolGroupLayout
 * @Description TODO 工具组
 * @Author: fs
 * @Date: 2021/5/13 10:15
 * @Version 2.0
 */

public final class ToolGroupLayout extends LinearLayout {

    private boolean childEventIntercept = false;

    private String mToolGroupName;

    public ToolGroupLayout(Context context) {
        super(context);
    }
    public ToolGroupLayout(Context context,@NonNull String toolGroupName) {
        super(context);
        this.mToolGroupName = toolGroupName;
    }
    public ToolGroupLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if(attrs!=null){
            TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.map_ui);
            String toolGroupName = attrArray.getString(R.styleable.map_ui_map_ui_tool_group_name);
            this.mToolGroupName = toolGroupName == null?this.mToolGroupName:toolGroupName;
            attrArray.recycle();
        }
    }

    public ToolGroupLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(attrs!=null){
            TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.map_ui);
            String toolGroupName = attrArray.getString(R.styleable.map_ui_map_ui_tool_group_name);
            this.mToolGroupName = toolGroupName == null?this.mToolGroupName:toolGroupName;
            attrArray.recycle();
        }
    }

    public String getToolGroupName() {
        return mToolGroupName;
    }

    /**
     * 设置名称
     * @param mToolGroupName
     */
    public void setToolGroupName(String mToolGroupName) {
        this.mToolGroupName = mToolGroupName;
    }

    /**
     * 重写这个方法，返回true就行了
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        return childEventIntercept;
    }


    public boolean isChildEventIntercept() {
        return childEventIntercept;
    }

    public void setChildEventIntercept(boolean childEventIntercept) {
        this.childEventIntercept = childEventIntercept;
    }


    /**
     * TODO 获取所有的ToolView实例
     * @return
     */

    public List<IToolView> getToolViews() {
        int childCount = getChildCount();
        List<IToolView> viewChilds = new ArrayList<>();
        for(int i =0; i<childCount ;i++){
            View view = getChildAt(i);
            if (view instanceof IToolView)
            {
                viewChilds.add((IToolView)view);
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
                viewChilds.add((IToolView)viewl2);
            }
        }
        return viewChilds;
    }
    /**
     * TODO 获取ToolView实例根据工具名
     * @param toolName TODO 工具名
     * @return
     */
    public  IToolView getToolViewByName(String toolName) {
        int childCount = getChildCount();
        for(int i =0; i<childCount ;i++){
            View view = getChildAt(i);
            if(view instanceof ToolView){
                if(((ToolView) view).getmToolName().equals(toolName)){
                    return (ToolView) view;
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
                return (IToolView)viewl2;
            }
        }
        return null;
    }
}

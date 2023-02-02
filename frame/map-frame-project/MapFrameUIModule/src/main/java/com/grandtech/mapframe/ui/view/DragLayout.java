package com.grandtech.mapframe.ui.view;

import android.content.Context;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;


import androidx.customview.widget.ViewDragHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.grandtech.mapframe.ui.util.ViewsTreeUtil;
import com.grandtech.mapframe.ui.view.holder.ViewMoveHolder;

import java.util.ArrayList;
import java.util.List;


/**
 * @ClassName DragLayoutV2
 * @Description TODO 可移动的view
 * @Author: fs
 * @Date: 2021/5/13 10:08
 * @Version 2.0
 */
public final class DragLayout extends RelativeLayout {
    private static final String TAG = "DragLayout";
    /**
     *
     */
    private Context context;
    /**
     * 当前移动的View
     */
    private int index;
    /**
     *
     */
    private List<View> views;
    /**
     *
     */
    private List<ViewMoveHolder> viewMoveHolders;


    public DragLayout(Context context) {
        this(context, null);
        init(context);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        viewMoveHolders = new ArrayList<>();
        this.context = context;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                index = getView(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (index == -1) {
            return super.onInterceptTouchEvent(ev);
        } else {
            ViewMoveHolder viewMoveHelper = viewMoveHolders.get(index);
            ViewDragHelper viewDragHelper = viewMoveHelper.getViewDragHelper();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    viewMoveHelper.getViewDragHelper().cancel();
                    break;
            }
            return viewDragHelper.shouldInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (index == -1) {
            return super.onTouchEvent(ev);
        } else {
            ViewMoveHolder viewMoveHelper = viewMoveHolders.get(index);
            ViewDragHelper viewDragHelper = viewMoveHelper.getViewDragHelper();
            viewDragHelper.processTouchEvent(ev);
            return true;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        getMoveViewReady();
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        getMoveViewReady();
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        getMoveViewReady();
    }

    /**
     * 遍历查询可移动控件
     */
    private void getMoveViewReady() {
        ViewDragCallBack viewDragCallBack;
        ViewMoveHolder viewMoveHelper;
        ViewDragHelper viewDragHelper = null;
        views = ViewsTreeUtil.find(this, "move");
        for (int i = 0; i < views.size(); i++) {
            boolean flag = false;
            for (int j = 0; j < viewMoveHolders.size(); j++) {
                viewMoveHelper = viewMoveHolders.get(j);
                View view = viewMoveHelper.getView();
                if (view == views.get(i)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                continue;
            }
            viewDragCallBack = new ViewDragCallBack(views.get(i).getId());
            if (viewDragHelper == null) {
                viewDragHelper = ViewDragHelper.create(this, 1f, viewDragCallBack);
            }

            viewMoveHelper = new ViewMoveHolder();
            viewMoveHelper.setView(views.get(i));
            viewMoveHelper.setViewDragHelper(viewDragHelper);
            viewMoveHolders.add(viewMoveHelper);
        }
        viewMoveHolders.size();
    }

    /**
     * 输出最后一个（显示在最上面的）
     *
     * @param ev
     * @return
     */
    private int getView(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        int p = -1;
        for (int i = 0; i < views.size(); i++) {
            View view = views.get(i);
            if (view.getVisibility() != VISIBLE) {
                continue;
            }
            int w = view.getWidth();
            int h = view.getHeight();
            int left = view.getLeft();
            int top = view.getTop();
            if (x < left + w && x > left && y > top && y < top + h) {
                p = i;
            }
        }
        //不等于-1还要判断子布局是否有滑动，如果有则优先子布局
        if (p != -1) {
            View view = views.get(p);
            int left = view.getLeft();
            int top = view.getTop();
            if (childNeedTouchEvent(view, ev, left, top)) {
                p = -1;
            }
        }
        return p;
    }

    /**
     * 判断子布局是否需要滑动
     *
     * @return
     */
    private boolean childNeedTouchEvent(View view, MotionEvent ev, int fleft, int ftop) {
        float x = ev.getX();
        float y = ev.getY();
        if (!(view instanceof ViewGroup)) {
            return false;
        }
        int childCount = ((ViewGroup) view).getChildCount();
        for (int i = 0; i < childCount; i++) {
            View cv = ((ViewGroup) view).getChildAt(i);
            int w = cv.getWidth();
            int h = cv.getHeight();
            int left = cv.getLeft() + fleft;
            int top = cv.getTop() + ftop;
            if (x < left + w && x > left && y > top && y < top + h) {
                if (cv instanceof RecyclerView) {
                    return true;
                }
                if (cv instanceof AbsListView) {
                    return true;
                }
                if (cv instanceof ScrollView) {
                    return true;
                }
                if (cv instanceof INeedTouchEventView) {
                    return true;
                }
                if (cv instanceof ViewGroup) {
                    if (childNeedTouchEvent(cv, ev, left, top)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private class ViewDragCallBack extends ViewDragHelper.Callback {

        int left;

        int top;

        int id;

        View view;

        LayoutParams layoutParams;


        public ViewDragCallBack(int id) {
            this.id = id;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            this.view = child;

            left = view.getLeft();
            top = view.getTop();

            return id == child.getId() && "move".equals(child.getTag());
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return getMeasuredWidth() - child.getMeasuredWidth();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return getMeasuredHeight() - child.getMeasuredHeight();
        }

        /**
         * 处理水平方向上的拖动
         *
         * @param child 拖动的View
         * @param left  移动到x轴的距离
         * @param dx    建议的移动的x距离
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //System.out.println("child:" + child + "|left:" + left + "|dx:" + dx);
            //两个if主要是让view在ViewGroup中
            if (left < getPaddingLeft()) {
                return getPaddingLeft();
            }

            if (left > getWidth() - child.getMeasuredWidth()) {
                return getWidth() - child.getMeasuredWidth();
            }
            this.left = left;
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            //System.out.println("child:" + child + "top:" + top + "|dy:" + dy);
            //两个if主要是让view在ViewGroup中
            if (top < getPaddingTop()) {
                return getPaddingTop();
            }

            if (top > getHeight() - child.getMeasuredHeight() - getPaddingBottom()) {
                return getHeight() - child.getMeasuredHeight() - getPaddingBottom();
            }
            this.top = top;
            return top;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            switch (state) {
                case ViewDragHelper.STATE_DRAGGING://正在拖动过程中

                    System.out.println(">>>正在拖动过程中");

                    break;
                case ViewDragHelper.STATE_IDLE://view没有被拖动，或者正在进行fling
                    LayoutParams temp = (LayoutParams) view.getLayoutParams();
                    layoutParams = new LayoutParams(temp.width, temp.height);
                    //layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.leftMargin = left;
                    layoutParams.topMargin = top;
                    view.setLayoutParams(layoutParams);
                    System.out.println(">>>view没有被拖动，或者正在进行fling");
                    break;
                case ViewDragHelper.STATE_SETTLING://fling完毕后被放置到一个位置

                    System.out.println(">>>fling完毕后被放置到一个位置");

                    break;
            }
            super.onViewDragStateChanged(state);
        }
    }

    /**
     * 继承此接口的view认定为处理TouchEvent，不允许滑动
     */
    public interface INeedTouchEventView {
    }
}

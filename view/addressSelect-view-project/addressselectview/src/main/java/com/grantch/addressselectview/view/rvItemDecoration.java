package com.grantch.addressselectview.view;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.grantch.addressselectview.base.BaseRecycleAdapter;
import com.grantch.addressselectview.base.BaseViewHolder;
import com.grantch.addressselectview.view.rvDecoration;

/**
 * ************************
 * 项目名称：commontool
 *
 * @Author hxh
 * 创建时间：2021/12/9   14:04
 * ************************
 */
public class rvItemDecoration extends RecyclerView.ItemDecoration implements rvDecoration {

    private Rect mHeaderRect = null;
    private int  mHeaderPosition = -1;

    /**
     * 把要固定的View绘制在上层
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        if (parent.getAdapter() instanceof BaseRecycleAdapter && parent.getChildCount() > 0) {
            BaseRecycleAdapter adapter = (BaseRecycleAdapter) parent.getAdapter();
            //找到要固定的pin view
            View firstView = parent.getChildAt(0);
            int firstAdapterPosition = parent.getChildAdapterPosition(firstView);
            int pinnedHeaderPosition = getPinnedHeaderViewPosition(firstAdapterPosition, adapter);
            mHeaderPosition = pinnedHeaderPosition;
            if (pinnedHeaderPosition != -1) {
                BaseViewHolder viewHolder = adapter.onCreateViewHolder(parent,
                        adapter.getItemViewType(pinnedHeaderPosition));
                adapter.onBindViewHolder(viewHolder, pinnedHeaderPosition);
                //要固定的view
                View pinnedHeaderView = viewHolder.itemView;
                ensurePinnedHeaderViewLayout(pinnedHeaderView, parent);
                int sectionPinOffset = 0;
                for (int index = 0; index < parent.getChildCount(); index++) {
                    if (adapter.isPinnedPosition(parent.getChildAdapterPosition(parent.getChildAt(index)))) {
                        View sectionView = parent.getChildAt(index);
                        int sectionTop = sectionView.getTop();
                        int pinViewHeight = pinnedHeaderView.getHeight();
                        if (sectionTop < pinViewHeight && sectionTop > 0) {
                            sectionPinOffset = sectionTop - pinViewHeight;
                        }
                    }
                }
                int saveCount = c.save();
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) pinnedHeaderView.getLayoutParams();
                if (layoutParams == null) {
                    throw new NullPointerException("HeaderItemDecoration");
                }
                c.translate(layoutParams.leftMargin, sectionPinOffset);
                c.clipRect(0, 0, parent.getWidth(), pinnedHeaderView.getMeasuredHeight());
                pinnedHeaderView.draw(c);
                c.restoreToCount(saveCount);
                if (mHeaderRect == null) {
                    mHeaderRect = new Rect();
                }
                mHeaderRect.set(0, 0, parent.getWidth(), pinnedHeaderView.getMeasuredHeight() + sectionPinOffset);
            } else {
                mHeaderRect = null;
            }

        }
    }

    /**
     * 要给每个item设置间距主要靠这个函数来实现
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    }

    /**
     * 根据第一个可见的adapter的位置去获取临近的一个要固定的position的位置
     *
     * @param adapterFirstVisible 第一个可见的adapter的位置
     * @return -1：未找到 >=0 找到位置
     */
    private int getPinnedHeaderViewPosition(int adapterFirstVisible, BaseRecycleAdapter adapter) {
        for (int index = adapterFirstVisible; index >= 0; index--) {
            if (adapter.isPinnedPosition(index)) {
                return index;
            }
        }
        return -1;
    }

    private void ensurePinnedHeaderViewLayout(View pinView, RecyclerView recyclerView) {
        if (pinView.isLayoutRequested()) {
            /**
             * 用的是RecyclerView的宽度测量，和RecyclerView的宽度一样
             */
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) pinView.getLayoutParams();
            if (layoutParams == null) {
                throw new NullPointerException("HeaderItemDecoration");
            }
            int widthSpec = View.MeasureSpec.makeMeasureSpec(
                    recyclerView.getMeasuredWidth() - layoutParams.leftMargin - layoutParams.rightMargin, View.MeasureSpec.EXACTLY);

            int heightSpec;
            if (layoutParams.height > 0) {
                heightSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.height, View.MeasureSpec.EXACTLY);
            } else {
                heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            }
            pinView.measure(widthSpec, heightSpec);
            pinView.layout(0, 0, pinView.getMeasuredWidth(), pinView.getMeasuredHeight());
        }
    }

    @Override
    public Rect getHeaderRect() {
        return mHeaderRect;
    }

    @Override
    public int getHeaderPosition() {
        return mHeaderPosition;
    }
}

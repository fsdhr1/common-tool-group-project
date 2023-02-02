package com.grantch.addressselectview.base;


import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by zy on 2017/8/12.
 */

public class BaseOnScrollListener extends RecyclerView.OnScrollListener {

    private IScrollListen iScrollListen;

    public BaseOnScrollListener(IScrollListen iScrollListen) {
        this.iScrollListen = iScrollListen;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

        System.out.println("滚动状态" + newState);

    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (!recyclerView.canScrollVertically(-1)) {
            if (iScrollListen != null) {
                iScrollListen.onScrolledToTop();
            }
        } else if (!recyclerView.canScrollVertically(1)) {
            if (iScrollListen != null) {
                iScrollListen.onScrolledToBottom();
            }
        } else if (dy < 0) {
            if (iScrollListen != null) {
                System.out.println("向上滚动");
                iScrollListen.onScrolledUp();
            }
        } else if (dy > 0) {
            if (iScrollListen != null) {
                System.out.println("向下滚动");
                iScrollListen.onScrolledDown();
            }
        }

    }

    public interface IScrollListen {
        public void onScrollState();

        public void onScrolledUp();

        public void onScrolledDown();

        public void onScrolledToTop();

        public void onScrolledToBottom();
    }
}

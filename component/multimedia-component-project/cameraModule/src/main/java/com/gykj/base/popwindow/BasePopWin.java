package com.gykj.base.popwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

/**
 * Created by zy on 2018/8/12.
 */

public abstract class BasePopWin {

    protected Context context;
    protected Activity activity;
    /**
     * 组件的View
     */
    protected View view;

    protected Integer w, h;
    protected PopupWindow _popWindow;

    public BasePopWin(Context context) {
        init(context);
    }

    public BasePopWin(Context context, Integer w) {
        init(context);
        this.w = w;
    }

    public BasePopWin(Context context, Integer w, Integer h) {
        init(context);
        this.w = w;
        this.h = h;
    }

    protected void init(Context context) {
        this.context = context;
        this.activity = (Activity) context;

        setContentView();
        initialize();
        afterInitialize();
        registerEvent();
    }

    public abstract void setContentView();

    protected void setContentView(int layoutId) {
        view = View.inflate(context, layoutId, null);
    }

    public abstract void initialize();

    public void afterInitialize() {
        if (w == null) {
            w = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        if (h == null) {
            h = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        _popWindow = new PopupWindow(view, w, h);
        _popWindow.setFocusable(true);
        _popWindow.setOutsideTouchable(true);
        _popWindow.setBackgroundDrawable(new ColorDrawable(0x30000000));
    }

    public abstract void registerEvent();

    public PopupWindow getPopupWindow() {
        return _popWindow;
    }

    public void simpleShow(View view) {
        _popWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    public void dismiss() {
        _popWindow.dismiss();
    }

    /**
     * @param anchor v
     * @param xoff   x轴偏移
     * @param yoff   y轴偏移
     */
    public void showAsDropDown(View anchor, final int xoff, final int yoff) {
        if (Build.VERSION.SDK_INT >= 24) {
            Rect visibleFrame = new Rect();
            anchor.getGlobalVisibleRect(visibleFrame);
            int height = anchor.getResources().getDisplayMetrics().heightPixels - visibleFrame.bottom;
            _popWindow.setHeight(height);
            _popWindow.showAsDropDown(anchor, xoff, yoff);
        } else {
            _popWindow.showAsDropDown(anchor, xoff, yoff);
        }
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener dismissListen) {
        _popWindow.setOnDismissListener(dismissListen);
    }


    //---------------------------回调--------------------------------
    public interface ICallBack {
        public void cancel(boolean flag, String className, Object obj);
    }

    protected ICallBack iCallBack;

    public void setICallBack(ICallBack iCallBack) {
        this.iCallBack = iCallBack;
    }
}

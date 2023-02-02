package com.gykj.commontool.maptest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SizeUtils;
import com.grandtech.mapframe.ui.util.MeasureUtil;


/**
 * @ClassName MyLinearLayout
 * @Description TODO
 * @Author: fs
 * @Date: 2021/8/11 16:24
 * @Version 2.0
 */
public class TriangleLinearLayout extends ViewGroup {
    public TriangleLinearLayout(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public TriangleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public TriangleLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public TriangleLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
    }



    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 创建画笔
        Paint p = new Paint();
        // 设置红色
        p.setStyle(Paint.Style.FILL);//充满
        p.setColor(Color.RED);
        p.setAntiAlias(true);// 设置画笔的锯齿效果
        Path path1=new Path();
        path1.moveTo(getTop(), getLeft());
        path1.lineTo(getTop()+ MeasureUtil.dp2px(getContext(),45), getLeft());
        path1.lineTo(getTop()+ MeasureUtil.dp2px(getContext(),45), getLeft()+MeasureUtil.dp2px(getContext(),24));
        path1.lineTo(getTop()+MeasureUtil.dp2px(getContext(),25), getLeft()+MeasureUtil.dp2px(getContext(),24));
        path1.lineTo(getTop()+MeasureUtil.dp2px(getContext(),22.5f), getLeft()+MeasureUtil.dp2px(getContext(),24));
        path1.lineTo(getTop()+MeasureUtil.dp2px(getContext(),20), getLeft()+MeasureUtil.dp2px(getContext(),24));
        path1.lineTo(getTop(), getLeft()+MeasureUtil.dp2px(getContext(),24));
        path1.close();//封闭
        canvas.drawPath(path1, p);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}

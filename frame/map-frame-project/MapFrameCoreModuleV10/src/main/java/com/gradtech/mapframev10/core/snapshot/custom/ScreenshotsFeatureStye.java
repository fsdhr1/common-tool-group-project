package com.gradtech.mapframev10.core.snapshot.custom;

import android.graphics.Color;


import com.gradtech.mapframev10.core.rules.Rules;
import com.mapbox.geojson.Feature;

import java.io.Serializable;

/**
 * @ClassName ScreenshotsFeatureStye
 * @Description TODO 每个图斑的样式
 * @Author: fs
 * @Date: 2022/8/10 10:02
 * @Version 2.0
 */
public class ScreenshotsFeatureStye implements Serializable , Rules {
    //填充斜线
    public static final String CELL = "CELL";
    //填充铺满
    public static final String FILL = "FILL";
    //填充铺满
    public static final String NULL = "NULL";
    //是否绘制图形的坐标
    private boolean drawPolygonMark =true;

    //图形轮廓宽度默认1
    private float strokeSize = 2f;
    //地块边线颜色
    private int color = Color.RED;
    //地块填充颜色 如果为null 则不填充
    private int fillColor = Color.RED;
    //地块填充样式 斜线、铺满
    private String fillType=NULL;

    private Feature feature;

    public boolean isDrawPolygonMark() {
        return drawPolygonMark;
    }

    public void setDrawPolygonMark(boolean drawPolygonMark) {
        this.drawPolygonMark = drawPolygonMark;
    }

    public float getStrokeSize() {
        return strokeSize;
    }

    public void setStrokeSize(float strokeSize) {
        this.strokeSize = strokeSize;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public String getFillType() {
        return fillType;
    }

    public void setFillType(String fillType) {
        this.fillType = fillType;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}

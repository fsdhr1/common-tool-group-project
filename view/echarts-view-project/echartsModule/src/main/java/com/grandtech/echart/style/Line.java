package com.grandtech.echart.style;

import java.io.Serializable;

/**
 * @ClassName Line
 * @Description TODO
 * @Author: fs
 * @Date: 2021/12/6 11:09
 * @Version 2.0
 */
public class Line implements Serializable {

    private Object padding;

    public Line padding(Object padding) {
        this.padding = padding;
        return this;
    }

    public Object padding() {
        return this.padding;
    }

    private Object color;

    public Line color(Object color) {
        this.color = color;
        return this;
    }

    public Object color() {
        return this.color;
    }

    private Object textShadowColor;

    public Line textShadowColor(Object textShadowColor) {
        this.textShadowColor = textShadowColor;
        return this;
    }

    public Object textShadowColor() {
        return this.textShadowColor;
    }

    private Object textShadowBlur;

    public Line textShadowBlur(Object textShadowBlur) {
        this.textShadowBlur = textShadowBlur;
        return this;
    }

    public Object textShadowBlur() {
        return this.textShadowBlur;
    }

    private Integer textShadowOffsetX;

    public Line textShadowOffsetX(Integer textShadowOffsetX) {
        this.textShadowOffsetX = textShadowOffsetX;
        return this;
    }

    public Integer textShadowOffsetX() {
        return this.textShadowOffsetX;
    }

    private Integer textShadowOffsetY;

    public Line textShadowOffsetY(Integer textShadowOffsetY) {
        this.textShadowOffsetY = textShadowOffsetY;
        return this;
    }

    public Integer textShadowOffsetY() {
        return this.textShadowOffsetY;
    }

    private Integer fontSize;

    public Line fontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public Integer fontSize() {
        return this.fontSize;
    }

    private Integer fontWeight;

    public Line fontWeight(Integer fontWeight) {
        this.fontWeight = fontWeight;
        return this;
    }

    public Integer fontWeight() {
        return this.fontWeight;
    }
}

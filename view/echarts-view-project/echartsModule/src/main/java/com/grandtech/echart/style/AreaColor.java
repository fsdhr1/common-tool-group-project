package com.grandtech.echart.style;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName AreaColor
 * @Description TODO
 * @Author: fs
 * @Date: 2021/12/3 11:23
 * @Version 2.0
 */
@Getter
@Setter
public class AreaColor implements Serializable {
    private static final long serialVersionUID = -6547716731700677834L;

    /**
     * 颜色
     */
    private Object color;
    /**
     * 填充样式，目前仅支持'default'(实填充)
     */
    private Object type;

    private Object image;

    private Object repeat;

    public Object image(){
        return this.image;
    }

    public AreaColor image(Object image){
        this.image = image;
        return this;
    }

    public Object repeat(){
        return this.repeat;
    }

    public AreaColor repeat(Object repeat){
        this.repeat = repeat;
        return this;
    }

    /**
     * 获取color值
     */
    public Object color() {
        return this.color;
    }

    /**
     * 设置color值
     *
     * @param color
     */
    public AreaColor color(Object color) {
        this.color = color;
        return this;
    }

    /**
     * 获取type值
     */
    public Object type() {
        return this.type;
    }

    /**
     * 设置type值
     *
     * @param type
     */
    public AreaColor type(Object type) {
        this.type = type;
        return this;
    }

    /**
     * 获取typeDefault值
     */
    public AreaColor typeDefault() {
        this.type = "default";
        return this;
    }

    /**
     * 获取color值
     */
    public Object getColor() {
        return color;
    }

    /**
     * 设置color值
     *
     * @param color
     */
    public void setColor(Object color) {
        this.color = color;
    }

    /**
     * 获取type值
     */
    public Object getType() {
        return type;
    }

    /**
     * 设置type值
     *
     * @param type
     */
    public void setType(Object type) {
        this.type = type;
    }

}

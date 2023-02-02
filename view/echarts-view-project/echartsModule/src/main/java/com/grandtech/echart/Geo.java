package com.grandtech.echart;

import com.grandtech.echart.code.Roam;
import com.grandtech.echart.style.ItemStyle;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liuzh
 * @since 2016-02-28 17:05
 */
@Getter
@Setter
public class Geo extends Basic<Geo> implements Component {
    private String map;
    private Roam roam;
    private ItemStyle label;
    private ItemStyle itemStyle;

    private Double aspectScale;

    private Object layoutCenter;

    private Object layoutSize;

    private Boolean silent;

    public Boolean silent() {
        return this.silent;
    }


    public Geo layoutSize(Boolean silent) {
        this.silent = silent;
        return this;
    }

    public Object layoutSize() {
        return this.layoutSize;
    }

    public Geo layoutSize(Object layoutSize) {
        this.layoutSize = layoutSize;
        return this;
    }

    public Object layoutCenter() {
        return this.layoutCenter;
    }

    public Geo layoutCenter(Object layoutCenter) {
        this.layoutCenter = layoutCenter;
        return this;
    }

    public Double aspectScale() {
        return this.aspectScale;
    }

    public Geo aspectScale(Double aspectScale) {
        this.aspectScale = aspectScale;
        return this;
    }

    public String map() {
        return this.map;
    }

    public Geo map(String map) {
        this.map = map;
        return this;
    }

    public Roam roam() {
        return this.roam;
    }

    public Geo roam(Roam roam) {
        this.roam = roam;
        return this;
    }

    public ItemStyle label() {
        if (this.label == null) {
            this.label = new ItemStyle();
        }
        return this.label;
    }

    public Geo label(ItemStyle label) {
        this.label = label;
        return this;
    }

    public ItemStyle itemStyle() {
        if (this.itemStyle == null) {
            this.itemStyle = new ItemStyle();
        }
        return this.itemStyle;
    }

    public Geo itemStyle(ItemStyle itemStyle) {
        this.itemStyle = itemStyle;
        return this;
    }
}

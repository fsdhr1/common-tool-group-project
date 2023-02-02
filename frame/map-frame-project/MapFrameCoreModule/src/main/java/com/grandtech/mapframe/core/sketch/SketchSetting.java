package com.grandtech.mapframe.core.sketch;

import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.rules.Rules;

import java.io.Serializable;


public class SketchSetting implements Serializable , Rules {

    private String inFlag;

    private GeometryType sketchType;

    public SketchSetting(String inFlag, GeometryType sketchType) {
        this.inFlag = inFlag;
        this.sketchType = sketchType;
    }

    public String getInFlag() {
        return inFlag;
    }

    public GeometryType getSketchType() {
        return sketchType;
    }

    public void setSketchType(GeometryType sketchType) {
        this.sketchType = sketchType;
    }
}

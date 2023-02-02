package com.grandtech.mapframe.core.editor.gc;


import androidx.annotation.NonNull;

import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.rules.Rules;

/**
 *
 * @author fs
 * @date 2021/04/10
 */

public class GraphicSetting implements Rules {

    ///////////////////////////////////////////////////////
    //临时图层的固定属性字段
    public static final String SOURCE_FIELD = "targetSource";
    public static final String FROM_LAYER_ID_FIELD = "fromLayerId";
    public static final String FROM_UID_FIELD = "fromUid";
    public static final String UID_FIELD = "uuid";
    public static final String MJ_FILED = "mj";
    /**
     * //长度
     */
    public static final String LEN_FILED = "len";
    public static final String SOURCE_TAG = "sourceTag";
    String cachePath;
    String targetLayerId;
    GeometryType geometryType;
    public GraphicSetting(@NonNull GeometryType geoType, @NonNull String targetLyrId, @NonNull String cachePath) {
        this.cachePath = cachePath;
        this.targetLayerId = targetLyrId;
        this.geometryType = geoType;
    }

    public String getTargetLayerId(){return  targetLayerId;}

    public String getCachePath() {
        return cachePath;
    }

    public GeometryType getGeometryType(){return  geometryType;}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GraphicSetting graphicSetting = (GraphicSetting) o;
        return cachePath.equals(graphicSetting.getCachePath());
    }

    @Override
    public int hashCode() {
        int result = cachePath.hashCode();
        result = 31 * result;
        return result;
    }
}

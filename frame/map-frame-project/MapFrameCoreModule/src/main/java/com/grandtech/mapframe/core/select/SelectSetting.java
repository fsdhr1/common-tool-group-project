package com.grandtech.mapframe.core.select;

import com.grandtech.mapframe.core.rules.Rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by zy on 2019/1/3.
 */

public class SelectSetting  implements Rules {

    List<String> layerIds = new ArrayList<>();

    private int maxSelectCount = 20;

    boolean isMulti = true;
    /**
     * 是否优先选择
     */
    boolean isPriority = false;

    /**
     * 是否渲染结果集
     */
    boolean isRender = true;

    private LinkedHashMap layerIdSelectMap;



    public SelectSetting(List<String> layerIds) {
        if (layerIds == null) {
            return;
        }
        this.layerIds.addAll(layerIds);
    }

    public SelectSetting(String... layerIds) {
        if (layerIds == null) {
            return;
        }
        this.layerIds.addAll(Arrays.asList(layerIds));
    }



    public SelectSetting(boolean isPriority, List<String> layerIds) {
        if (layerIds == null) {
            return;
        }
        this.isPriority = isPriority;
        this.layerIds.addAll(layerIds);
    }

    public SelectSetting(boolean _isMulti, boolean isPriority, String... layerIds) {
        if (layerIds == null) {
            return;
        }
        this.isMulti = _isMulti;
        this.isPriority = isPriority;
        this.layerIds.addAll(Arrays.asList(layerIds));
    }



    public SelectSetting(boolean isMulti, boolean isRender, boolean isPriority, List<String> layerIds) {
        if (layerIds == null) {
            return;
        }
        this.isMulti = isMulti;
        this.isRender = isRender;
        this.isPriority = isPriority;
        this.layerIds.addAll(layerIds);
    }

    public SelectSetting(boolean isMulti, boolean isRender, boolean isPriority, String... layerIds) {
        if (layerIds == null) {
            return;
        }
        this.isMulti = isMulti;
        this.isRender = isRender;
        this.isPriority = isPriority;
        this.layerIds.addAll(Arrays.asList(layerIds));
    }


    public SelectSetting(boolean isRender, boolean isPriority, List<String> layerIds) {
        if (layerIds == null) {
            return;
        }
        this.isRender = isRender;
        this.isPriority = isPriority;
        this.layerIds.addAll(layerIds);
    }

    public LinkedHashMap getLayerIdSelectId() {
        return layerIdSelectMap;
    }

    public void setLayerIdSelect(LinkedHashMap layerIdSelectMap) {
        this.layerIdSelectMap = layerIdSelectMap;
    }

    public List<String> getLayersAsList() {
        return layerIds;
    }

    public boolean isPriority() {
        return isPriority;
    }

    public boolean isRender() {
        return isRender;
    }

    public boolean isMulti() {
        return isMulti;
    }

    public int getMaxSelectCount(){return  maxSelectCount;}
}

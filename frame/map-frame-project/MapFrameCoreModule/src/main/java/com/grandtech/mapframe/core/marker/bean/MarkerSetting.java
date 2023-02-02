package com.grandtech.mapframe.core.marker.bean;

import com.grandtech.mapframe.core.rules.Rules;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;

/**
 * @ClassName MarkerBean
 * @Description TODO Marker信息实体
 * @NonNull String markerId, @NonNull LatLng latLng,@NonNull final Integer iconfinal ,String title,final String snippet, final boolean move
 * @Author: fs
 * @Date: 2021/6/11 8:53
 * @Version 2.0
 */
public class MarkerSetting implements Serializable, Rules {

    private String markerId;

    private LatLng latLng;

    private Integer icon;

    private String title;

    private String snippet;

    private boolean move;

    public MarkerSetting() {
    }

    public MarkerSetting(String markerId, LatLng latLng, Integer icon, String title, String snippet, boolean move) {
        this.markerId = markerId;
        this.latLng = latLng;
        this.icon = icon;
        this.title = title;
        this.snippet = snippet;
        this.move = move;
    }

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public boolean isMove() {
        return move;
    }

    public void setMove(boolean move) {
        this.move = move;
    }
}

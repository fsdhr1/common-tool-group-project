package com.gykj.commontool.networktest.beans;

import java.io.Serializable;

public class TagBean implements Serializable {
    private String createtime;
    private String name;
    private String layers_status;
    private String location;
    private String markers;
    private String username;

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getCreatetime() {
        return this.createtime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setLayers_status(String layers_status) {
        this.layers_status = layers_status;
    }

    public String getLayers_status() {
        return this.layers_status;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }

    public void setMarkers(String markers) {
        this.markers = markers;
    }

    public String getMarkers() {
        return this.markers;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

}
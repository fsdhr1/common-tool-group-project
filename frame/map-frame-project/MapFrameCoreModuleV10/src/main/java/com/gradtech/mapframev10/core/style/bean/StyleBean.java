package com.gradtech.mapframev10.core.style.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName StyleBean
 * @Description TODO
 * @Author: fs
 * @Date: 2021/6/7 8:55
 * @Version 2.0
 */
public class StyleBean implements Serializable {

    private Date updatedAt;
    private Date createdAt;
    private String owner;
    private double zoom;
    private Map<String,String> sources;
    private String name;
    private Map<String,String> metadata;
    private String sprite;
    private String glyphs;
    private List<Map<String,String>> layers;
    private int pitch;
    private int bearing;
    private List<Double> center;
    private int version;
    private List<String> tags;
    private String type;
    private String scope;
    private String style_id;
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
    public String getOwner() {
        return owner;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }
    public double getZoom() {
        return zoom;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getSprite() {
        return sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public String getGlyphs() {
        return glyphs;
    }

    public void setGlyphs(String glyphs) {
        this.glyphs = glyphs;
    }


    public void setPitch(int pitch) {
        this.pitch = pitch;
    }
    public int getPitch() {
        return pitch;
    }

    public void setBearing(int bearing) {
        this.bearing = bearing;
    }
    public int getBearing() {
        return bearing;
    }



    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
    public String getScope() {
        return scope;
    }

    public void setStyle_id(String style_id) {
        this.style_id = style_id;
    }
    public String getStyle_id() {
        return style_id;
    }


    public Map<String, String> getSources() {
        return sources;
    }

    public void setSources(Map<String, String> sources) {
        this.sources = sources;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public List<Map<String, String>> getLayers() {
        return layers;
    }

    public void setLayers(List<Map<String, String>> layers) {
        this.layers = layers;
    }

    public List<Double> getCenter() {
        return center;
    }

    public void setCenter(List<Double> center) {
        this.center = center;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

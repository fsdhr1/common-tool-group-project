package com.gykj.cameramodule.bean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jsb on 2018/5/25 0025.
 */

public class PhotoFile implements Serializable {

    private String name;

    private String path;

    private String desc;

    public PhotoFile() {

    }

    public PhotoFile(String name, String path, String desc) {
        this.name = name;
        this.path = path;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static List<PhotoFile> formJson(String json) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        gson.serializeNulls();
        return gson.fromJson(json, new TypeToken<List<PhotoFile>>() {
        }.getType());
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        gson.serializeNulls();
        return gson.toJson(this);
    }

    /**
     * 解析desc获取里面的属性
     *
     * @param key
     * @return
     */
    public String getAttrFromDesc(String key) {
        if (desc == null) return null;
        String descr = desc.replace("；", ";");
        descr = descr.replace("：", ":");
        String[] descs = descr.split(";");
        for (String de : descs) {
            if (de.contains(key)) {
                String[] des = de.split("\\:");
                if (des.length == 2) {
                    return des[1];
                } else {
                    return null;
                }
            }
        }
        return null;
    }
}

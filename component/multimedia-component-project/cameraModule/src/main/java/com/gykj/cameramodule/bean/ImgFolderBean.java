package com.gykj.cameramodule.bean;

import java.io.Serializable;

/**
 * Created by zy on 2018/8/10.
 */

public class ImgFolderBean implements Serializable {
    private String dir; //图片文件夹路径
    private String firstImgPath;//第一张图片的路径
    private String name;//文件夹名称
    private int count;//文件夹里图片数量


    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf);
    }

    public String getFirstImagePath() {
        return firstImgPath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImgPath = firstImagePath;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

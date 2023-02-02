package com.gykj.autoupdate.bean;

import java.io.Serializable;

/**
 * App下载路劲
 */
public class AppPath implements Serializable {

    /**
     * path : mobile/2019/08/29/791f1e56c8774816857cfad51118201e/PigBodyMeasure0823.apk
     * name : apk文件
     * desc : {"accessKeyId":"LTAIP816Jan0Nvjq","bucket":"ram-public","endpoint":"http://oss-cn-zhangjiakou.aliyuncs.com"}
     */

    private String path;
    private String name;
    private String desc;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

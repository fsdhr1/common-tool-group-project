package com.gykj.selectlinearlayout.bean;

import java.io.Serializable;

/**
 * Created by jyh on 2021-02-26
 */
public class Dict implements Serializable {

    private String key;
    private String value;
    private String desc;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return value;
    }
}

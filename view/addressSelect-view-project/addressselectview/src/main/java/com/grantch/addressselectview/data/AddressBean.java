package com.grantch.addressselectview.data;

import java.io.Serializable;

public class AddressBean<T> implements Serializable {
    public int id;
    public String qhmc;
    public String qhdm;
    public int type =1;
    public T t;

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public AddressBean(){}

    public AddressBean(String qhmc, String qhdm){
        this.qhmc = qhmc;
        this.qhdm = qhdm;
    }

    public AddressBean(String qhmc, int type){
        this.qhmc = qhmc;
        this.type = type;
    }

    public AddressBean(String qhmc){
        this.qhmc = qhmc;
    }

    public boolean select;

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean mSelect) {
        select = mSelect;
    }

    public int getId() {
        return id;
    }

    public void setId(int mId) {
        id = mId;
    }

    public String getQhmc() {
        return qhmc;
    }

    public void setQhmc(String mQhmc) {
        qhmc = mQhmc;
    }

    public String getQhdm() {
        return qhdm;
    }

    public void setQhdm(String mQhdm) {
        qhdm = mQhdm;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AddreBean{" +
                "qhmc='" + qhmc + '\'' +
                ", qhdm='" + qhdm + '\'' +
                '}';
    }
}
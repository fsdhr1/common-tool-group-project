package com.gykj.commontool.networktest.beans;


import java.io.Serializable;

public class GeoBean implements Serializable {
    private String shape;
    private String qhdm;
    private String qhmc;
    private Double mj;

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getShape() {
        return this.shape;
    }

    public void setQhdm(String qhdm) {
        this.qhdm = qhdm;
    }

    public String getQhdm() {
        return this.qhdm;
    }

    public void setQhmc(String qhmc) {
        this.qhmc = qhmc;
    }

    public String getQhmc() {
        return this.qhmc;
    }

    public void setMj(Double mj) {
        this.mj = mj;
    }

    public Double getMj() {
        return this.mj;
    }

}
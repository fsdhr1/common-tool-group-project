package com.gykj.addressselect.bean;

public class AddreBean {
    public int id;
    public String qhmc;
    public String qhdm;
    public boolean select;
    /**
     * 是否是自定义的
     */
    private boolean mCustom;

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

    public void setCustom(boolean mCustom) {
        this.mCustom = mCustom;
    }

    public boolean getCustom() {
        return mCustom;
    }
}
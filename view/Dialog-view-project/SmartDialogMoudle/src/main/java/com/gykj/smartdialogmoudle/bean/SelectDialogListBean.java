package com.gykj.smartdialogmoudle.bean;


import java.io.Serializable;

public class SelectDialogListBean<T>  implements Serializable {
    /**
     * id : 6
     * isDelete : 0
     * category : project_year
     * key : 10002
     * value : 2019
     * remark :
     * type :
     */

    private int id;
    private int isDelete;
    private boolean isSelect=false;//用于多选
    private String category;
    private String key;
    private String value;//显示的item
    private T data;

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(int isDelete) {
        this.isDelete = isDelete;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
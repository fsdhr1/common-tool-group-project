package com.gykj.autoupdate.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zy on 2018/5/5.
 */

public class BaseResult<T> implements Serializable {
    //判断标示
    private int code;
    //提示信息
    private String msg;
    //
    private String message;
    //显示数据（用户需要关心的数据）
    private T data;

    private List<T> datas;

    private Map user;

    private Map dept;

    private Map deptroot;

    private String token;

    private String areacode;

    private int hotfixVersion;

    public int getHotfixVersion() {
        return hotfixVersion;
    }

    public void setHotfixVersion(int mHotfixVersion) {
        hotfixVersion = mHotfixVersion;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<T> getDatas() {
        return datas;
    }

    public void addData(T data) {
        if (datas == null) datas = new ArrayList<>();
        datas.add(data);
    }

    public static <M> M convert(Object data) {
        try {
            return (M) data;
        } catch (Exception e) {
            return null;
        }
    }

    public Map getUser() {
        return user;
    }

    public void setUser(Map user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public Map getDept() {
        return dept;
    }

    public void setDept(Map dept) {
        this.dept = dept;
    }

    public Map getDeptroot() {
        return deptroot;
    }

    public void setDeptroot(Map deptroot) {
        this.deptroot = deptroot;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAreacode() {
        return areacode;
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

    public boolean isSuccess() {
        if (code == 0 || code == 200) return true;
        else return false;
    }

    public boolean isFail() {
        return !isSuccess();
    }
}

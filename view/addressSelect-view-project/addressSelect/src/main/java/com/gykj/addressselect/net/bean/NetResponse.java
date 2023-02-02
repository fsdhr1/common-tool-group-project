package com.gykj.addressselect.net.bean;

import java.util.ArrayList;
import java.util.List;

public class NetResponse<T> {

    //判断标示
    private int code;
    //提示信息
    private String msg;
    //
    private String message;
    //显示数据（用户需要关心的数据）
    private T data;

    private List<T> datas;


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

    public boolean isSuccess() {
        if (code == 0 || code == 200) return true;
        else return false;
    }

    public boolean isFail() {
        return !isSuccess();
    }

}

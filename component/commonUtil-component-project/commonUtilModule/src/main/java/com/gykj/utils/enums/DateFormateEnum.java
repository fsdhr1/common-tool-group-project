package com.gykj.utils.enums;

/**
 * Created by ZhaiJiaChang.
 * <p>
 * Date: 2021/12/27
 */
public enum DateFormateEnum {

    yyyy_MM_dd_HH_mm_ss("yyyy-MM-dd HH:mm:ss"),
    yyyy_MM_dd("yyyy-MM-dd"),
    HH_mm_ss("HH:mm:ss"),
    yyyy("yyyy"),
    ;

    private String msg;
    DateFormateEnum(String formate) {
        msg = formate;
    }

    public String getMsg() {
        return msg;
    }


}

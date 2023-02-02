package com.grandtech.echart.style;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName Rich
 * @Description TODO
 * @Author: fs
 * @Date: 2021/12/6 11:05
 * @Version 2.0
 */
@Getter
@Setter
public class Rich implements Serializable {

   private Line fline;

    private Line tline;

    public Rich fline(Line fline) {
        this.fline = fline;
        return this;
    }

    public Object fline() {
        return this.fline;
    }
    public Rich tline(Line tline) {
        this.tline = tline;
        return this;
    }

    public Object tline() {
        return this.tline;
    }


}

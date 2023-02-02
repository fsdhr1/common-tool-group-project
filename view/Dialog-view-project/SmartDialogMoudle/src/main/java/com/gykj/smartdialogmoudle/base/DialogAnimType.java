package com.gykj.smartdialogmoudle.base;

/**
 * ************************
 * 项目名称：commontool
 *
 * @Author hxh
 * 创建时间：2021/11/3   15:01
 * ************************
 */
public enum DialogAnimType {
    NONE("None", -1),
    TRANSLATE_B_T("BottomToTop", 1),
    TRANSLATE_T_B("TopToBottom", 2),
    TRANSLATE_L_R("LeftToRright", 3),
    TRANSLATE_R_L("RrightToLeft", 4),
    SCALE_V("scale_vertical", 5),
    SCALE_H("scale_horizont", 6),
    ROTATE_V("rotate_vertical", 7),
    ROTATE_H("rotate_horizont", 8),
    ALPHA("alpha", 9);

    private String name;
    private int value;

    private DialogAnimType(String name, int index) {
        this.name = name;
        this.value = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

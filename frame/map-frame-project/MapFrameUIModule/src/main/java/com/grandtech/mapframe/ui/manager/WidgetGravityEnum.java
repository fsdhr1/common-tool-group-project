package com.grandtech.mapframe.ui.manager;

import android.view.ViewGroup;

import com.grandtech.mapframe.ui.view.DragLayout;

/**
 * @ClassName WidgetGravityEnum
 * @Description TODO
 * @Author: fs
 * @Date: 2021/5/28 13:20
 * @Version 2.0
 */
public enum WidgetGravityEnum {
    RIGHT("RIGHT", 5),
    LEFT("LEFT", 3),
    TOP("TOP", 48),
    CENTER("CENTER", 17),
    BOTTOM("BOTTOM", 80),
    RIGHT_TOP("RIGHT_TOP", 53),
    RIGHT_CENTER("RIGHT_CENTER", 21),
    RIGHT_BOTTOM("RIGHT_BOTTOM", 85),
    LEFT_TOP("LEFT_TOP", 51),
    LEFT_CENTER("LEFT_CENTER", 19),
    LEFT_BOTTOM("LEFT_BOTTOM", 83),
    TOP_CENTER("TOP_CENTER", 49),
    BOTTOM_CENTER("BOTTOM_CENTER", 81);

    private String name;
    private int value;

    WidgetGravityEnum(String name, int value) {
        this.name = name;
        this.value = value;
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

    /**
     * 根据枚举描名称获取枚举对象
     */
    public static WidgetGravityEnum getEnumByName(String name) {
        WidgetGravityEnum type = null;
        for (int i = 0; i < WidgetGravityEnum.values().length; i++) {
            if (WidgetGravityEnum.values()[i].toString().equalsIgnoreCase(name.trim())) {
                type = WidgetGravityEnum.values()[i];
                break;
            }
        }
        return type;
    }

    /**
     * 根据枚举整型表达式获取枚举对象
     */
    public static WidgetGravityEnum getEnumByValue(int value) {
        WidgetGravityEnum type = null;
        for (int i = 0; i < WidgetGravityEnum.values().length; i++) {
            if (WidgetGravityEnum.values()[i].getValue() == value) {
                type = WidgetGravityEnum.values()[i];
                break;
            }
        }
        return type;
    }
}

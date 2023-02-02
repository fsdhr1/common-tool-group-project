package com.grandtech.mapframe.ui.manager;

/**
 * @ClassName EToolName
 * @Description TODO 工具名枚举
 * @Author: fs
 * @Date: 2021/5/19 14:55
 * @Version 2.0
 */
public enum ToolNameEnum {
    /**
     *
     */
    SELECT("tool_select"),

    DRAW("绘制"),

    ADD("添加"),

    MERGE("合并"),

    CUT("分割"),

    DIG("扣除"),

    SKETCH_CANCEL("绘制结束"),

    SKETCH_CANDO("绘制后退"),

    SKETCH_UNDO("绘制前退"),

    SKETCH_SAVE("绘制保存"),

    SKETCH_ADD("绘制加点"),

    ISEARCH("i查询"),

    IMAGESWITCHING("影像切换"),

    IN("放大"),

    OUT("缩小"),

    LOC("定位"),

    NAV("工具栏收缩");

    String name;

    ToolNameEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

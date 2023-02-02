package com.grantch.addressselectview.data;

/**
 * ************************
 * 项目名称：commontool
 *
 * @Author hxh
 * 创建时间：2021/12/13   10:42
 * ************************
 */
public enum  LevelType {
    PROVINCE(5),CITY(4),COUNTY(3),TOWN(2),VILLAGE(1);
    private int mLevel;
    LevelType(int level){
        this.mLevel = level;
    }

    public int getmLevel() {
        return mLevel;
    }
}

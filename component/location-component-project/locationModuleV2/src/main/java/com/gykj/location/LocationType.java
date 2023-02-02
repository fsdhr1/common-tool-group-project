package com.gykj.location;

/**
 * Created by ZhaiJiaChang.
 * <p>
 * Date: 2021/12/9
 */
public enum LocationType {
    GPS(1),
    NETWORK(2),
    BAIDU(3),
    COMMON(4),//混合定位
    ;
    private int type;

    public int getType() {
        return type;
    }

    LocationType(int type) {
        this.type = type;
    }
}

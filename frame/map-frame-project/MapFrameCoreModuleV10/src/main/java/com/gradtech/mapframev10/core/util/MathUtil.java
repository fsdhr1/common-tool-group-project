package com.gradtech.mapframev10.core.util;

import java.math.BigDecimal;

/**
 * @ClassName MathUtil
 * @Description TODO
 * @Author: fs
 * @Date: 2022/9/2 9:22
 * @Version 2.0
 */
public class MathUtil {

    /**
     * 四舍五入保留小数位数
     * @param d
     * @return
     */
    public static double double2HALF_UP(double d,int scale){
        BigDecimal bigDecimal = new BigDecimal(d);
        return bigDecimal.setScale(scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}

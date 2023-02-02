package com.gykj.arcgistool.utils;

import android.content.Context;
import android.util.TypedValue;

import com.gykj.arcgistool.common.Measure;
import com.gykj.arcgistool.common.Variable;

import java.math.BigDecimal;

public class Util {
    public static int valueToSp(Context context, int value){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    public static String forMatDouble(double num){
        BigDecimal b = new BigDecimal(num);
        double df = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return df+"";
    }

    public static String lengthFormat(double length){
        if(length<0) {
            return "无效数据";
        }
        if(length<1000){
            BigDecimal b = new BigDecimal(length);
            return b.intValue()+"米";
        }else{
            double f=length*0.001;
            BigDecimal b = new BigDecimal(f);
            return b.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue()+"公里";
        }
    }

    public static String areaFormat(double area){
        if(area<0) {
            return "无效数据";
        }
        if(area<1000){
            BigDecimal b = new BigDecimal(area);
            return b.intValue()+"平方米";
        }else if(area<10000){
            double mu=area*0.0015;
            BigDecimal b = new BigDecimal(mu);
            return b.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue()+"亩";
        }else if(area<1000000){
            double gongqing=area*0.0001;
            BigDecimal b=new BigDecimal(gongqing);
            return  b.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue()+"公顷";
        }else{
            double pfgl=area*0.000001;
            BigDecimal b=new BigDecimal(pfgl);
            return  b.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue()+"平方公里";
        }
    }

    public static double lengthChange(double length, Measure type){
        if(type== Measure.M) {
            return length;
        }
        if(type==Measure.KM || type==Measure.KIM) {
            return mToKm(length);
        }
        return 0;
    }

    public static double mToKm(double length){
        return length/1000;
    }

    public static String lengthEnameToCname(Measure type){
        switch (type){
            case M:
                return "米";
            case KM:
                return "千米";
            case KIM:
                return "公里";
            case M2:
                return "平方米";
            case KM2:
                return "平方千米";
            case HM2:
                return "公顷";
            case A2:
                return "亩";
            default:
                return "未知";
        }
    }
    public static double areaChange(double area, Measure type){
        switch (type){
            case M2:
                return area;
            case KM2:
                return m2ToKm2(area);
            case HM2:
                return m2ToHm2(area);
            case A2:
                return m2ToA2(area);
            default:
                return 0;
        }
    }

    public static double m2ToKm2(double area){
        return area/1000;
    }
    public static double m2ToHm2(double area){
        return area/10000;
    }

    public static double m2ToA2(double area){
        return area/666.67;
    }

}

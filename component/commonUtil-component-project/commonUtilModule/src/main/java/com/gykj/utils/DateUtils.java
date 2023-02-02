package com.gykj.utils;

import android.text.TextUtils;

import com.gykj.utils.enums.DateFormateEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtils {
    private static final long ONE_DAY = 86400000l;

    // 用来全局控制上一周，本周，下一周的周数变化
    private static int weeks = 0;

    /**
     * @return 返回当前日期字符串，以格式：yyyy-MM-dd输出。
     */
    public static String getCurrentTime(String formate) {
        SimpleDateFormat df = new SimpleDateFormat(formate);
        String s = df.format(new Date());
        return s;
    }

    /**
     * @return返回当前年，输出格式为：yyyy。
     */
    public static String getCurrentYear() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        String s = df.format(new Date());
        return s;
    }

    /**
     * @param d
     * @return根据输入时间，格式化输出年信息：yyyy。
     */
    public static String getCurrentYear(Date d) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        String s = df.format(d);
        return s;
    }

    /**
     * @return返回当前月信息：MM。
     */
    public static String getCurrentMonth() {
        SimpleDateFormat df = new SimpleDateFormat("MM");
        String s = df.format(new Date());
        return s;
    }


    /**
     * @param time
     * @return 将字符转换为日期yyyy-MM-dd
     */
    public static Date strToDate(String time, DateFormateEnum mFormateEnum) {
        Date date = null;
        if (time != null) {
            SimpleDateFormat df = new SimpleDateFormat(mFormateEnum.getMsg());
            try {
                date = df.parse(time);
            } catch (ParseException e) {
                // log.error("DateParse Error!");
            }
        }
        return date;
    }



    /**
     * @param date
     * @param day
     * @return 在指定日期增加指定天数
     */
    public static Date add(Date date, int day) {
        date = new Date(date.getTime() + day * ONE_DAY);
        return date;
    }

    /**
     * @param date
     * @return 日期增加1天
     */
    public static Date addDay(Date date) {
        return add(date, 1);
    }

    /**
     * @param date
     * @return 日期减一天
     */
    public static Date subDay(Date date) {
        return add(date, -1);
    }


    /**
     * @param startYear
     * @param startMonth
     * @param endYear
     * @param endMonth
     * @return 根据起始年月和终止年月计算共有月数
     */
    public static int compareMonth(String startYear, String startMonth, String endYear, String endMonth) {
        return (Integer.parseInt(endYear) - Integer.parseInt(startYear)) * 12
                + (Integer.parseInt(endMonth) - Integer.parseInt(startMonth));

    }

    /**
     * @param sDate
     * @return 获取年月日期操作类
     */
    public static String getYearMonth(String sDate) {
        Date date1 = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String s = null;
        try {
            date1 = df.parse(sDate);
            df.applyPattern("yyMM");
            s = df.format(date1);
        } catch (ParseException e) {
            return s;
        }
        return s;
    }


    /**
     * @param sDate
     * @return 返回当前日期年月日字符串
     */
    public static String getYearMonthDay(String sDate) {
        Date date1 = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String s = null;
        try {
            date1 = df.parse(sDate);
            df.applyPattern("yyMMdd");
            s = df.format(date1);
        } catch (ParseException e) {
            return s;
        }
        return s;
    }


    /**
     * @param date
     * @return 将指定日期返回特定字符串
     */
    public static String date2Str(Date date, DateFormateEnum mFormateEnum) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat(mFormateEnum.getMsg());
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    /**
     * @param sDate
     * @return 获取指定日期对象的月份
     */
    public static String getMonth(String sDate) {
        Date date1 = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String s = null;
        try {
            date1 = df.parse(sDate);
            df.applyPattern("MM");
            s = df.format(date1);
        } catch (ParseException e) {
            return s;
        }
        return s;

    }

    /**
     * @param sDate1
     * @param sDate2
     * @return 获取两个输入日期的时间差，单位：天
     */
    public static int compareDate(String sDate1, String sDate2) {

        Date date1 = null;
        Date date2 = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            date1 = dateFormat.parse(sDate1);
            date2 = dateFormat.parse(sDate2);
        } catch (ParseException e) {

        }

        long dif = 0;
        if (date2.after(date1))
            dif = (date2.getTime() - date1.getTime()) / 1000 / 60 / 60 / 24;
        else
            dif = (date1.getTime() - date2.getTime()) / 1000 / 60 / 60 / 24;

        return (int) dif;
    }

    /**
     * @param start
     * @param end
     * @return 获取两个输入日期的时间差
     */
    public static String compareDayHourMinite(long start, long end) {
        String result = "";
        if (start > end) return result;
        long diff = end - start;//这样得到的差值是微秒级别
        long days = diff / (1000 * 60 * 60 * 24);
        long hours = (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (diff - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
        if (days > 0)
            result += days + "天";
        if (hours > 0)
            result += hours + "小时";
        if (minutes > 0)
            result += minutes + "分";
        return result;
    }

    // 将String 转成 long
    public static long stringToLong(String strTime, String formatType) {
        try {
            if (TextUtils.isEmpty(strTime)) return 0;
            Date date = stringToDate(strTime, formatType); // String类型转成date类型
            if (date == null) {
                return 0;
            } else {
                long currentTime = dateToLong(date); // date类型转成long类型
                return currentTime;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // date要转换的date类型的时间
    public static long dateToLong(Date date) {
        return date.getTime();
    }

    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }


    /**
     * @param sDate
     * @param sTag
     * @return ……
     */
    public static int getDate(String sDate, String sTag) {
        int iSecondMinusPos = sDate.lastIndexOf('-');
        if (sTag.equalsIgnoreCase("y")) {
            return Integer.parseInt(sDate.substring(0, 4));
        } else if (sTag.equalsIgnoreCase("m")) {
            return Integer.parseInt(sDate.substring(5, iSecondMinusPos));
        } else
            return Integer.parseInt(sDate.substring(iSecondMinusPos + 1));
    }

    /**
     * @return 获取本周所剩天数
     */
    public static int getDayOfWeek() {

        Calendar toDay = Calendar.getInstance();

        toDay.setFirstDayOfWeek(Calendar.MONDAY);

        int ret = toDay.get(Calendar.DAY_OF_WEEK) - 1;

        if (ret == 0) {
            ret = 7;
        }

        return ret;
    }

    /**
     * @return 获取当月的第一天
     */
    public static String getFirstDayOfMonth(DateFormateEnum mFormateEnum) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        ca.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDate = ca.getTime();
        ca.add(Calendar.MONTH, 1);
        ca.add(Calendar.DAY_OF_MONTH, -1);
        // Date lastDate = ca.getTime();

        return date2Str(firstDate,mFormateEnum);

    }

    /**
     * @return 获取当月的最后一天
     */
    public static String getLastDayOfMonth(DateFormateEnum mFormateEnum) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        ca.set(Calendar.DAY_OF_MONTH, 1);
        // Date firstDate = ca.getTime();
        ca.add(Calendar.MONTH, 1);
        ca.add(Calendar.DAY_OF_MONTH, -1);
        Date lastDate = ca.getTime();
        return date2Str(lastDate,mFormateEnum);
    }


    /**
     * @return 昨天
     */
    public static String yesterday() {
        Calendar calendar = Calendar.getInstance();
        StringBuffer param = new StringBuffer();
        calendar.add(Calendar.DATE, -1);
        param.append(String.valueOf(calendar.get(Calendar.YEAR))).append("-")
                .append(String.valueOf(calendar.get(Calendar.MONTH) + 1)).append("-")
                .append(String.valueOf(calendar.get(Calendar.DATE)));
        return param.toString();
    }


    /**
     * @return 最近7天
     */
    public static String lately7Day(DateFormateEnum mFormateEnum) {
        Calendar calendar = Calendar.getInstance();
        StringBuffer param = new StringBuffer();
        calendar.add(Calendar.DATE, -7);
        param.append(String.valueOf(calendar.get(Calendar.YEAR))).append("-")
                .append(String.valueOf(calendar.get(Calendar.MONTH) + 1)).append("-")
                .append(String.valueOf(calendar.get(Calendar.DATE)));
        return param.toString() + "," + date2Str(new Date(),mFormateEnum);
    }

    /**
     * @return 最近30天
     */
    public static String lately30Day(DateFormateEnum mFormateEnum) {
        Calendar calendar = Calendar.getInstance();
        StringBuffer param = new StringBuffer();
        calendar.add(Calendar.DATE, -30);
        param.append(String.valueOf(calendar.get(Calendar.YEAR))).append("-")
                .append(String.valueOf(calendar.get(Calendar.MONTH) + 1)).append("-")
                .append(String.valueOf(calendar.get(Calendar.DATE)));
        return param.toString() + "," + date2Str(new Date(),mFormateEnum);
    }

    /**
     * @return 去年
     */
    public static String lastYear() {
        Calendar calendar = Calendar.getInstance();
        StringBuffer param = new StringBuffer();
        calendar.add(Calendar.YEAR, -1);
        param.append(String.valueOf(calendar.get(Calendar.YEAR)));
        return param.toString();
    }

    /**
     * @return 上月
     */
    public static String LastMonth() {
        Calendar calendar = Calendar.getInstance();
        StringBuffer param = new StringBuffer();
        calendar.add(Calendar.MONTH, -1);
        param.append(String.valueOf(calendar.get(Calendar.YEAR))).append("-")
                .append(String.valueOf(calendar.get(Calendar.MONTH) + 1));
        return param.toString();
    }


    /**
     * @return 获得上周星期日的日期
     */
    public static String getPreviousWeekSunday(DateFormateEnum mFormateEnum) {
        weeks = 0;
        weeks--;
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + weeks);
        Date monday = currentDate.getTime();
        String preMonday = date2Str(monday,mFormateEnum);
        return preMonday;
    }

    /**
     * @return获得上周星期一的日期
     */
    public static String getPreviousWeekday(DateFormateEnum mFormateEnum) {
        weeks--;
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks);
        Date monday = currentDate.getTime();
        String preMonday = date2Str(monday,mFormateEnum);
        return preMonday;
    }

    /**
     * @return 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
     */
    private static int getMondayPlus() {
        Calendar cd = Calendar.getInstance();
        //
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1
        if (dayOfWeek == 1) {
            return 0;
        } else {
            return 1 - dayOfWeek;
        }
    }

    /**
     * 这个方法获取的结果是24小时制的，月份也正确 网络时间
     *
     * @return
     */
    public static String getTimeZone() {
        SimpleDateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dff.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        String ee = dff.format(new Date());
        System.out.println("ee=" + ee);
        return ee;
    }

    /**
     * 两个日期之间的时间差
     */
    public static long getDistanceTime(Date startTime, Date endTime) {
        long time1 = startTime.getTime();
        long time2 = endTime.getTime();

        long diff;
        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        return diff;
    }

    /***
     * 两个日期之间的时间差
     * @param startTime
     * @param endTime
     * @return 精确到秒 例如 3天4小时5分钟22秒
     */
    public static String getDistanceString(Date startTime, Date endTime) {
        long diff = getDistanceTime(startTime, endTime);
        int day = (int) (diff / (24 * 60 * 60 * 1000));
        long restH = diff - day * 24 * 60 * 60 * 1000;
        int hour = (int) (restH / (60 * 60 * 1000));
        long resM = restH - hour * 60 * 60 * 1000;

        return "";
    }


}

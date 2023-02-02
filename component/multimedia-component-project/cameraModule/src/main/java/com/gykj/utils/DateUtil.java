package com.gykj.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * @author kuchanly
 * @category 日期操作类
 */
public class DateUtil {
    private static final long ONE_DAY = 86400000l;

    // 用来全局控制上一周，本周，下一周的周数变化
    private static int weeks = 0;

    /**
     * @return 返回当前日期字符串，以格式：yyyy-MM-dd输出。
     */
    public static String getCurrentDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String s = df.format(new Date());
        return s;
    }

    /**
     * @return 返回当前时间，以格式：HH:mm:ss输出。
     */
    public static String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String s = df.format(new Date());
        return s;
    }

    /**
     * utc时间
     *
     * @return
     */
    public static long getUTCTimeStr() {
        Calendar cal = Calendar.getInstance();
        long mills = cal.getTimeInMillis() + 8 * 60 * 60 * 1000;
        System.out.println("UTC = " + mills);
        return mills;
    }

    /**
     * utc时间戳转 时间
     *
     * @return
     */
    public static String uTCTime2DataTime(String utcTimeStr) {
        Date date = new Date(Long.parseLong(utcTimeStr));
        return dateTimeToStr(date);
    }

    /**
     * @return 返回当前时间，以秒形式输出。
     */
    public static String getCurrentm() {
        SimpleDateFormat df = new SimpleDateFormat("ss");
        String s = df.format(new Date());
        return s;
    }

    /**
     * @return返回当前时间，以HHmmss形式输出
     */
    public static String getCurrentHhMmSs() {

        SimpleDateFormat df = new SimpleDateFormat("HHmmss");
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
    public static String getYear(Date d) {
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
     * @param sDate
     * @return 获取输入日期是每周的第几天
     */
    public static String getDayInWeek(String sDate) {
        Date date = strToDate(sDate);
        SimpleDateFormat df = new SimpleDateFormat("EEE");
        String s = df.format(date);
        return s;
    }

    /**
     * @param str
     * @return 将字符转换为日期yyyy-MM-dd
     */
    public static Date strToDate(String str) {
        Date date = null;
        if (str != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                date = df.parse(str);
            } catch (ParseException e) {
                // log.error("DateParse Error!");
            }
        }
        return date;
    }

    /**
     * @param str
     * @return 字符串转日期类型yyyy-MM-dd HH:mm:ss
     */
    public static Date strToDateTime(String str) {
        Date date = null;
        if (str != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = df.parse(str);
            } catch (ParseException e) {
                // log.error("DateParse Error!");
            }
        }
        return date;
    }

    /**
     * @return 获取当前时间字符串值
     */
    public static String getDAT() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String s = df.format(new Date());
        return s;
    }

    /**
     * @return 获取当前时间字符串值
     */
    public static String getFileExtend() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String s = df.format(new Date());
        return s;
    }

    /**
     * @return 获取当前时间字符串值
     */
    public static String getVersionExtend() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String s = df.format(new Date());
        return s;
    }

    public static String getOriginal() {
        return "0000_00_00_00_00_00";
    }

    /**
     * @return 获取当前时间字符串值
     */
    public static String getDAT(String DateStr, Date date) {
        SimpleDateFormat df = new SimpleDateFormat(DateStr);
        String s = df.format(date);
        return s;
    }

    /**
     * @param date
     * @return 返回指定格式的日期字符串。
     */
    public static String dateTimeToStr(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    public static Long getLongDate(Date date) {
        return date.getTime();
    }

    /**
     * @param date
     * @return 返回指定格式的日期字符串。
     */
    public static String dateTimeToAccessFormat(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (date != null) {
            str = df.format(date);

        }
        return str.replace(" ", "T");
    }

    /**
     * @param date
     * @return 将指定日期返回特定字符串
     */
    public static String dateToStr(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    /**
     * @param d
     * @return 将指定日期返回特定字符串
     */
    public static String dateToStr(long d) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = new Date(d);
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    public static String dateToYearMonthDay(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    public static String dateToYearMonth(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    // 创建日期年月日目录路径
    public static String dateToPathYearMonthDay(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy年\\MM月\\dd日");
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    /**
     * @param date
     * @return 创建日期年月日目录路径 以逗号分割
     */
    public static String dateToPathYearMonthDaySplit(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy年,MM月,dd日");
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    /**
     * @param date
     * @return 创建日期年月目录路径
     */
    public static String dateToPathYearMonth(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy年\\MM月");
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    /**
     * @param date
     * @return 创建日期年月目录路径 以逗号分割
     */
    public static String dateToPathYearMonthSplit(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy年,MM月");
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    /**
     * @param date
     * @return 创建日期年目录路径
     */
    public static String dateToPathYear(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy年");
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    /**
     * @param date
     * @return 返回当前指定日期的中文字符格式
     */
    public static String dateToStrCh(Date date) {
        String str = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy MM dd ");
        if (date != null) {
            str = df.format(date);
        }
        return str;
    }

    /**
     * @param date
     * @param i
     * @return 在指定日期增加指定天数
     */
    public static Date add(Date date, int i) {
        date = new Date(date.getTime() + i * ONE_DAY);
        return date;
    }

    /**
     * @param date
     * @return 日期增加1天
     */
    public static Date add(Date date) {
        return add(date, 1);
    }

    /**
     * @param date
     * @return 日期减一天
     */
    public static Date sub(Date date) {
        return add(date, -1);
    }

    /**
     * @return 返回昨天日期
     */
    public static String getBeforeDate() {
        Date date = DateUtil.sub(new Date());
        return DateUtil.dateToStr(date);

    }

    /**
     * @return 回去当前日期及时间
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = df.format(new Date());
        return s;
    }

    /**
     * @return 获取当前日期周
     */
    public static String getCurrentDateWeek() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy MM dd EEE");
        String s = df.format(new Date());
        return s;

    }

    /**
     * @return 获取当前日期周EN格式
     */
    public static String getCurrentDateWeekEn() {
        SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy ", new Locale("en"));
        String s = df.format(new Date());
        return s;

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
     * @param date
     * @return 根据当前日期，获取年月信息
     */
    public static String getYearMonth(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyMM");
        String s = null;

        s = df.format(date);

        return s;

    }

    /**
     * @param date
     * @return 获取当前时间的月和日信息
     */
    public static String getMonthDay(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("MM dd ");
        String s = null;

        s = df.format(date);

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
     * @return 获取指定日期的当天开始时间
     */
    public static String getStartQueryTime(String date) {
        return DateUtil.dateToStr(DateUtil.strToDate(date)) + " 00:00:00";
    }

    /**
     * @param date
     * @return 获取指定日期的当天结束时间
     */
    public static String getEndQueryTime(String date) {
        return DateUtil.dateToStr(DateUtil.strToDate(date)) + " 23:59:59";
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
    public static String getFirstDayOfMonth() {
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        ca.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDate = ca.getTime();
        ca.add(Calendar.MONTH, 1);
        ca.add(Calendar.DAY_OF_MONTH, -1);
        // Date lastDate = ca.getTime();

        return dateToStr(firstDate);

    }

    /**
     * @return 获取当月的最后一天
     */
    public static String getLastDayOfMonth() {
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        ca.set(Calendar.DAY_OF_MONTH, 1);
        // Date firstDate = ca.getTime();
        ca.add(Calendar.MONTH, 1);
        ca.add(Calendar.DAY_OF_MONTH, -1);
        Date lastDate = ca.getTime();
        return dateToStr(lastDate);
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.getCurrentTime());

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
    public static String lately7Day() {
        Calendar calendar = Calendar.getInstance();
        StringBuffer param = new StringBuffer();
        calendar.add(Calendar.DATE, -7);
        param.append(String.valueOf(calendar.get(Calendar.YEAR))).append("-")
                .append(String.valueOf(calendar.get(Calendar.MONTH) + 1)).append("-")
                .append(String.valueOf(calendar.get(Calendar.DATE)));
        return param.toString() + "," + dateToYearMonthDay(new Date());
    }

    /**
     * @return 最近30天
     */
    public static String lately30Day() {
        Calendar calendar = Calendar.getInstance();
        StringBuffer param = new StringBuffer();
        calendar.add(Calendar.DATE, -30);
        param.append(String.valueOf(calendar.get(Calendar.YEAR))).append("-")
                .append(String.valueOf(calendar.get(Calendar.MONTH) + 1)).append("-")
                .append(String.valueOf(calendar.get(Calendar.DATE)));
        return param.toString() + "," + dateToYearMonthDay(new Date());
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
     * @return 本周 以,隔开
     */
    public static String thisWeek() {
        Calendar c = Calendar.getInstance();
        int weekday = c.get(7) - 1;
        System.out.println("周天数：" + weekday);
        c.add(5, -weekday);
        String thisWeek = dateToStr(c.getTime());
        System.out.println("本周开始时间：" + dateToStr(c.getTime()));
        c.add(5, 7);
        System.out.println("本周开始结束：" + dateToStr(c.getTime()));
        thisWeek += "," + dateToStr(c.getTime());
        return thisWeek;
    }

    /**
     * @return 上周以, 隔开
     */
    public static String lastWeek() {
        /*
         * Calendar calendar = Calendar.getInstance(); int
         * minus=calendar.get(GregorianCalendar.DAY_OF_WEEK)+1;
         * calendar.add(GregorianCalendar.DATE,-minus); String end=new
         * java.sql.Date(calendar.getTime().getTime()).toString();
         * calendar.add(GregorianCalendar.DATE,-4); String begin=new
         * java.sql.Date(calendar.getTime().getTime()).toString();
         */
        String beginTime = getPreviousWeekday();
        String endTime = getPreviousWeekSunday();
        return beginTime + "," + endTime;
    }

    /**
     * @return 获得上周星期日的日期
     */
    public static String getPreviousWeekSunday() {
        weeks = 0;
        weeks--;
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + weeks);
        Date monday = currentDate.getTime();
        String preMonday = dateToYearMonthDay(monday);
        return preMonday;
    }

    /**
     * @return获得上周星期一的日期
     */
    public static String getPreviousWeekday() {
        weeks--;
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks);
        Date monday = currentDate.getTime();
        String preMonday = dateToYearMonthDay(monday);
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
}

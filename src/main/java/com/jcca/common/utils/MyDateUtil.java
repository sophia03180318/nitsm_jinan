package com.jcca.common.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具
 */
@Slf4j
public class MyDateUtil {
    /**
     * ldt时间获取毫秒数
     *
     * @param ldt
     * @return
     */
    public static String ltdToMillisecond(LocalDateTime ldt) {
        //获取毫秒数
        Long milliSecond = ldt.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return milliSecond.toString();
    }

    /**
     * 毫秒数转时间
     *
     * @param ms
     * @return
     */
    public static DateTime msToDateTime(String ms) {
        DateTime date = DateUtil.date(Convert.toLong(ms));
        return date;
    }

    ;

    /**
     * 输出执行时间
     *
     * @param name
     * @param starttime
     */
    public static String execTime(String name, Long starttime) {
        Double time = Convert.toDouble(DateUtil.spendMs(starttime));
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":").append("运行时间:").append(time).append("毫秒,").append(NumberUtil.decimalFormat("#.###", time / 1000))
                .append("秒,").append(NumberUtil.decimalFormat("#.##", time / 1000 / 60)).append("分钟");
        return sb.toString();
    }

    public static String getNowStr() {
        String str = DateUtil.format(new Date(), "yyyy-MM-dd-HH:mm:ss");
        return str;
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /*
     *获得当前月--开始日期
     * @Param 例：“2021-12-03”
     * @return String 例：“2021-12-01 00:00:00”
     * @Author syt
     * @Date 2021/12/3 11:41
     */
    public static String getMinMonthDateToString(String date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateFormat.parse(date));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return dateFormat.format(calendar.getTime()) + " 00:00:00";
    }


    /**
     * 获得当前月--结束日期
     *
     * @return String 例：“2021-12-31 59:59:59”
     * @Param 例：“2021-12-03”
     * @Author syt
     * @Date 2021/12/3 11:41
     */
    public static String getMaxMonthDateToString(String date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateFormat.parse(date));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return dateFormat.format(calendar.getTime()) + " 23:59:59";
    }


    /*
     *获得当前月--开始日期
     * @Param 例：“2021-12-03”
     * @return String 例：“2021-12-01 00:00:00”
     * @Author syt
     * @Date 2021/12/3 11:41
     */
    public static Date getMinMonthDate(String date) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(date));
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            Date parse = dateFormat.parse(dateFormat.format(calendar.getTime()) + " 00:00:00");
            return parse;
        } catch (java.text.ParseException e) {
            log.error("获取月份第一天解析失败1", e);
        }
        return null;
    }

    /**
     * 获得当前月--结束日期
     *
     * @return String 例：“2021-12-31 59:59:59”
     * @Param 例：“2021-12-03”
     * @Author syt
     * @Date 2021/12/3 11:41
     */
    public static Date getMaxMonthDate(String date) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(date));
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date parse = dateFormat.parse(dateFormat.format(calendar.getTime()) + " 59:59:59");
            return parse;
        } catch (java.text.ParseException e) {
            log.error("获取月份最后一天解析失败2", e);
        }
        return null;
    }

}

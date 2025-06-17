package com.jcca.web2.util;

/**
 * @author: hhw
 * @description: TimeToCronConverter主要是用来将时间转换为cron表达式
 * @date: 2025-02-25  17:11
 * @since: 2.0.11.0
 */
public class TimeToCronConverter {
    /**
     * 将时分秒转换为cron表达式
     *
     * @param hour   小时 (0-23)
     * @param minute 分钟 (0-59)
     * @param second 秒 (0-59)
     * @return cron表达式
     */
    public static String convertToCron(int hour, int minute, int second) {
        // 验证输入的时间是否合法
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("小时必须在0到23之间");
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("分钟必须在0到59之间");
        }
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException("秒必须在0到59之间");
        }

        // 构建cron表达式
        return String.format("%d %d %d * * ?", second, minute, hour);
    }

    /**
     * 时间转表达式
     *
     * @param time HH:mm:ss
     * @return
     */
    public static String convertToCron(String time) {
        String[] split = time.split(":");
        int hour = Integer.parseInt(split[0]);
        int minute = Integer.parseInt(split[1]);
        int econd = Integer.parseInt(split[2]);
        return convertToCron(hour, minute, econd);
    }
}

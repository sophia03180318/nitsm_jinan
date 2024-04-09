package com.jcca.common.utils;

import cn.hutool.core.util.StrUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

/**
 * snmp 分析时间
 *
 * @author Lvyp
 */
public class SnmpTimeUtil {


    /**
     * 将snmp采集到的时间16进制转为时间
     * yyyy-MM-dd HH:mm:ss SSS
     *
     * @param result
     * @return
     * @throws Exception
     */
    public static String getTimeStr(String result) throws Exception {
        if (StrUtil.isEmpty(result)) {
            throw new Exception("采集结果是空的");
        }
        String[] split = result.trim().split(":");
        if (split.length != 8) {
            throw new Exception("采集结果长度不为8，不符合规范:" + result);
        }
        Integer year = Integer.valueOf(split[0] + split[1], 16);
        Integer month = Integer.valueOf(split[2], 16);
        Integer day = Integer.valueOf(split[3], 16);
        Integer hours = Integer.valueOf(split[4], 16);
        Integer minute = Integer.valueOf(split[5], 16);
        Integer second = Integer.valueOf(split[6], 16);
        Integer millisecond = Integer.valueOf(split[7], 16);

        return year + "-" + month + "-" + day + " " + hours + ":" + minute + ":" + second + " " + millisecond;
    }

    /**
     * 将snmp采集到的时间16进制转为时间
     * yyyy-MM-dd HH:mm
     *
     * @param result
     * @return
     * @throws Exception
     */
    public static String getTimeStr2(String result) throws Exception {
        if (StrUtil.isEmpty(result)) {
            throw new Exception("采集结果是空的");
        }
        int timeLength = 6;
        int timeLength2 = 7;

        String[] split = result.trim().split(":");
        if (split.length == timeLength) {
            String year = Integer.valueOf(split[0] + split[1], 16) + "";
            String month = formatTime(Integer.valueOf(split[2], 16));
            String day = formatTime(Integer.valueOf(split[3], 16));
            String hours = formatTime(Integer.valueOf(split[4], 16));
            String minute = formatTime(Integer.valueOf(split[5], 16));

            return year + "-" + month + "-" + day + " " + hours + ":" + minute;
        } else if (split.length == timeLength2) {
            String year = Integer.valueOf(split[0] + split[1], 16) + "";
            String month = formatTime(Integer.valueOf(split[2], 16));
            String day = formatTime(Integer.valueOf(split[3], 16));
            String hours = formatTime(Integer.valueOf(split[4], 16));
            String minute = formatTime(Integer.valueOf(split[5], 16));
            String second = formatTime(Integer.valueOf(split[6], 16));

            return year + "-" + month + "-" + day + " " + hours + ":" + minute + ":" + second;
        }

        throw new Exception("采集结果长度不为6，不符合规范:" + result);
    }

    /**
     * 格式化时间
     *
     * @param oldTime
     * @return
     */
    private static String formatTime(Integer oldTime) {
        int verifyTimeLength = 1;
        String oldTimeStr = oldTime + "";
        if (oldTimeStr.length() == verifyTimeLength) {
            return "0" + oldTime;
        }
        return oldTime + "";
    }

    /**
     * 16进制ASCII码转字符工具
     *
     * @param octetString
     * @return
     */
    public static String asciiToString(String octetString) {
        if (!octetString.contains(":")) {
            return octetString;
        }
        if (!isAscii(octetString)) {
            return octetString;
        }

        try {
            String[] temps = octetString.split(":");
            byte[] bs = new byte[temps.length];
            for (int i = 0; i < temps.length; i++) {
                bs[i] = (byte) Integer.parseInt(temps[i], 16);
            }
            String encodename = EncodeUtils.getEncode(new BufferedInputStream(new ByteArrayInputStream(bs)), true);
            if (StrUtil.isEmpty(encodename)) {
                encodename = "UTF-8";
            }
            return new String(bs, encodename);
        } catch (Exception e) {
            return octetString;
        }
    }

    /**
     * 验证是否是ASCII字符串
     *
     * @param str
     * @return
     */
    public static Boolean isAscii(String str) {
        return str.matches("^[a-zA-Z0-9][a-zA-Z0-9]:{1}([a-zA-Z0-9][a-zA-Z0-9]:{1})*([a-zA-Z0-9][a-zA-Z0-9])?");
    }

}
